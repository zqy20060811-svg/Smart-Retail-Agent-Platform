package com.retail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.retail.common.constant.MessageConstant;
import com.retail.common.enums.OrderStatusEnum;
import com.retail.common.exception.BusinessException;
import com.retail.common.result.PageResult;
import com.retail.dto.OrderCreateDTO;
import com.retail.dto.OrderPageQueryDTO;
import com.retail.entity.OrderItem;
import com.retail.entity.Orders;
import com.retail.entity.Product;
import com.retail.mapper.OrderItemMapper;
import com.retail.mapper.OrdersMapper;
import com.retail.service.OrdersService;
import com.retail.service.ProductService;
import com.retail.vo.OrderVO;
import com.retail.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务实现
 * 骨架说明：下单即视为已支付待接单（跳过支付/购物车流程），
 * 库存扣减、幂等、支付回调等后续按 TODO 扩展。
 */
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, OrderCreateDTO createDTO) {
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPhone(createDTO.getPhone());
        order.setAddress(createDTO.getAddress());
        order.setRemark(createDTO.getRemark());
        // 骨架：直接进入待接单
        order.setStatus(OrderStatusEnum.PENDING_ACCEPT.getCode());
        order.setPayStatus(1);

        BigDecimal amount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (OrderCreateDTO.OrderItemDTO itemDTO : createDTO.getItems()) {
            Product product = productService.getByIdWithCache(itemDTO.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(MessageConstant.PRODUCT_NOT_FOUND + ": id=" + itemDTO.getProductId());
            }
            BigDecimal itemAmount = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getNumber()));
            amount = amount.add(itemAmount);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setAmount(product.getPrice());
            item.setNumber(itemDTO.getNumber());
            items.add(item);

            // TODO: 库存扣减、销量增加（建议乐观锁防超卖）
        }
        order.setAmount(amount);
        save(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return order.getOrderNo();
    }

    @Override
    public PageResult<Orders> pageQuery(Long userId, OrderPageQueryDTO queryDTO) {
        Page<Orders> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(userId != null, Orders::getUserId, userId)
                .eq(queryDTO.getStatus() != null, Orders::getStatus, queryDTO.getStatus())
                .like(StringUtils.hasText(queryDTO.getOrderNo()), Orders::getOrderNo, queryDTO.getOrderNo())
                .orderByDesc(Orders::getCreateTime);
        page(page, wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public OrderVO getDetail(Long id, Long userId) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (userId != null && !userId.equals(order.getUserId())) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setItems(orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id)));
        return vo;
    }

    @Override
    public void cancel(Long userId, Long id) {
        Orders order = getById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getStatus() > OrderStatusEnum.PENDING_ACCEPT.getCode()) {
            throw new BusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        updateById(order);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // TODO: 状态机校验（参考 OrderStatusEnum 合法流转）
        order.setStatus(status);
        updateById(order);

        // WebSocket 实时推送给用户
        String msg = String.format("{\"type\":\"orderStatus\",\"orderId\":%d,\"status\":%d,\"statusDesc\":\"%s\"}",
                id, status, OrderStatusEnum.descOf(status));
        WebSocketServer.sendToUser(order.getUserId(), msg);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
