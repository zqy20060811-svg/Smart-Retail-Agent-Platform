package com.retail.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.retail.common.enums.OrderStatusEnum;
import com.retail.common.result.Result;
import com.retail.entity.Orders;
import com.retail.entity.Product;
import com.retail.service.CategoryService;
import com.retail.service.OrdersService;
import com.retail.service.ProductService;
import com.retail.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端：工作台统计
 */
@RestController
@RequestMapping("/api/admin/workspace")
@RequiredArgsConstructor
@Api(tags = "管理端-工作台")
public class AdminWorkspaceController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrdersService ordersService;

    @GetMapping("/stats")
    @ApiOperation("工作台概览数据")
    public Result<Map<String, Object>> stats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        List<Orders> todayOrders = ordersService.list(new LambdaQueryWrapper<Orders>()
                .ge(Orders::getCreateTime, todayStart));
        // 今日营业额：未取消订单金额合计
        BigDecimal todaySales = todayOrders.stream()
                .filter(o -> !OrderStatusEnum.CANCELLED.getCode().equals(o.getStatus()))
                .map(Orders::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userService.count());
        stats.put("productCount", productService.count());
        stats.put("onSaleProductCount", productService.count(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        stats.put("categoryCount", categoryService.count());
        stats.put("orderCount", ordersService.count());
        stats.put("todayOrderCount", todayOrders.size());
        stats.put("todaySales", todaySales);
        // 待接单数量（商家最需要关注的指标）
        stats.put("pendingOrderCount", ordersService.count(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, OrderStatusEnum.PENDING_ACCEPT.getCode())));
        return Result.success(stats);
    }
}
