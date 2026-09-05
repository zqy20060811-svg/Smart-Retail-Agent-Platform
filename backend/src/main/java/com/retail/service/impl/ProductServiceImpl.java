package com.retail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.common.constant.RedisConstant;
import com.retail.common.result.PageResult;
import com.retail.dto.ProductDTO;
import com.retail.dto.ProductPageQueryDTO;
import com.retail.entity.Product;
import com.retail.mapper.ProductMapper;
import com.retail.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现
 *
 * 缓存保护：
 *  - 穿透：DB 查不到也缓存空值（短 TTL）
 *  - 击穿：重建缓存用 SETNX 互斥锁，只放一个请求回源
 *  - 雪崩：TTL 加随机偏移
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult<Product> pageQuery(ProductPageQueryDTO queryDTO) {
        Page<Product> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getStatus() != null, Product::getStatus, queryDTO.getStatus())
                .like(StringUtils.hasText(queryDTO.getKeyword()), Product::getName, queryDTO.getKeyword())
                .orderByDesc(Product::getUpdateTime);
        page(page, wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public Product getByIdWithCache(Long id) {
        String key = RedisConstant.PRODUCT_PREFIX + id;

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            // 命中空值占位：防穿透
            if (RedisConstant.EMPTY_CACHE.equals(cached)) {
                return null;
            }
            return (Product) cached;
        }

        // 2. 未命中，抢互斥锁（防击穿）
        String lockKey = RedisConstant.PRODUCT_LOCK_PREFIX + id;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        try {
            if (Boolean.TRUE.equals(locked)) {
                // 双重检查：拿到锁后可能别的线程已回源完成
                Object doubleCheck = redisTemplate.opsForValue().get(key);
                if (doubleCheck != null) {
                    return RedisConstant.EMPTY_CACHE.equals(doubleCheck) ? null : (Product) doubleCheck;
                }
                // 回源 DB
                Product product = getById(id);
                if (product == null) {
                    // 空值缓存，短 TTL，防穿透
                    redisTemplate.opsForValue().set(key, RedisConstant.EMPTY_CACHE,
                            RedisConstant.EMPTY_TTL, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, product,
                            RedisConstant.PRODUCT_TTL + ThreadLocalRandom.current().nextLong(300),
                            TimeUnit.SECONDS);
                }
                return product;
            }
            // 没抢到锁，短暂等待后重试读缓存
            Thread.sleep(50);
            Object retry = redisTemplate.opsForValue().get(key);
            if (retry instanceof Product) {
                return (Product) retry;
            }
            return getById(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return getById(id);
        } finally {
            if (Boolean.TRUE.equals(locked)) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    @Override
    public void saveOrUpdateProduct(ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        saveOrUpdate(product);
        // 写操作淘汰缓存
        redisTemplate.delete(RedisConstant.PRODUCT_PREFIX + product.getId());
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        updateById(product);
        redisTemplate.delete(RedisConstant.PRODUCT_PREFIX + id);
    }
}
