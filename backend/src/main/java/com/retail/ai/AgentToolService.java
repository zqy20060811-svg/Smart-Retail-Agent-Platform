package com.retail.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.retail.entity.Orders;
import com.retail.entity.Product;
import com.retail.entity.Promotion;
import com.retail.service.OrdersService;
import com.retail.service.ProductService;
import com.retail.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 工具集：把订单查询、商品搜索、优惠查询封装为可供 Dify Agent 调用的能力。
 * 同时供：
 *  1. /api/ai/tools/** 端点（Dify 平台远程 Tool 回调）
 *  2. 本地 mock 降级时的简单意图命中
 */
@Service
@RequiredArgsConstructor
public class AgentToolService {

    private final OrdersService ordersService;
    private final ProductService productService;
    private final PromotionService promotionService;

    /** 订单查询：按用户/订单号/状态 */
    public List<Orders> queryOrder(Long userId, String orderNo, Integer status) {
        return ordersService.list(new LambdaQueryWrapper<Orders>()
                .eq(userId != null, Orders::getUserId, userId)
                .eq(status != null, Orders::getStatus, status)
                .like(StringUtils.hasText(orderNo), Orders::getOrderNo, orderNo)
                .orderByDesc(Orders::getCreateTime)
                .last("limit 10"));
    }

    /** 商品搜索：按名称模糊，仅返回起售商品 */
    public List<Product> searchProduct(String keyword) {
        return productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .like(StringUtils.hasText(keyword), Product::getName, keyword)
                .orderByDesc(Product::getSales)
                .last("limit 10"));
    }

    /** 优惠查询：进行中的活动，可按关键词过滤 */
    public List<Promotion> queryDiscount(String keyword) {
        LocalDateTime now = LocalDateTime.now();
        return promotionService.list(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getStatus, 1)
                .le(Promotion::getStartTime, now)
                .ge(Promotion::getEndTime, now)
                .and(StringUtils.hasText(keyword),
                        w -> w.like(Promotion::getTitle, keyword).or().like(Promotion::getContent, keyword))
                .last("limit 10"));
    }
}
