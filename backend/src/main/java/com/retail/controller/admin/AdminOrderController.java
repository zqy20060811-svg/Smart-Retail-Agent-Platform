package com.retail.controller.admin;

import com.retail.common.result.PageResult;
import com.retail.common.result.Result;
import com.retail.dto.OrderPageQueryDTO;
import com.retail.entity.Orders;
import com.retail.service.OrdersService;
import com.retail.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：订单管理（接单/派送/完成，状态变更经 WebSocket 推送用户）
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Api(tags = "管理端-订单管理")
public class AdminOrderController {

    private final OrdersService ordersService;

    @GetMapping("/page")
    @ApiOperation("订单分页（全部用户）")
    public Result<PageResult<Orders>> page(OrderPageQueryDTO queryDTO) {
        return Result.success(ordersService.pageQuery(null, queryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(ordersService.getDetail(id, null));
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("更新订单状态（3已接单 4派送中 5已完成），实时推送用户")
    public Result<Void> status(@PathVariable Long id, @PathVariable Integer status) {
        ordersService.updateStatus(id, status);
        return Result.success();
    }
}
