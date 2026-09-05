package com.retail.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.retail.common.result.PageResult;
import com.retail.dto.ProductDTO;
import com.retail.dto.ProductPageQueryDTO;
import com.retail.entity.Product;

public interface ProductService extends IService<Product> {

    /** 分页/条件查询商品 */
    PageResult<Product> pageQuery(ProductPageQueryDTO queryDTO);

    /**
     * 按 ID 查询商品详情（走 Redis 缓存，含穿透/击穿/雪崩保护）
     */
    Product getByIdWithCache(Long id);

    /** 新增或更新商品（更新后淘汰缓存） */
    void saveOrUpdateProduct(ProductDTO productDTO);

    /** 起售/停售（淘汰缓存） */
    void toggleStatus(Long id, Integer status);
}
