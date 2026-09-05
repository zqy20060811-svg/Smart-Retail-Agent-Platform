package com.retail.controller.user;

import com.retail.common.context.BaseContext;
import com.retail.common.result.PageResult;
import com.retail.common.result.Result;
import com.retail.dto.OrderCreateDTO;
import com.retail.dto.OrderPageQueryDTO;
import com.retail.entity.Orders;
import com.retail.service.OrdersService;
import com.retail.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * C 端用户：订单
 */
@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
@Api(tags = "用户端-订单")
public class UserOrderController {

    private final OrdersService ordersService;

    @PostMapping
    @ApiOperation("创建订单")
    public Result<String> create(@Valid @RequestBody OrderCreateDTO createDTO) {
        return Result.success(ordersService.createOrder(BaseContext.getCurrentId(), createDTO));
    }

    @GetMapping
    @ApiOperation("我的订单分页")
    public Result<PageResult<Orders>> page(OrderPageQueryDTO queryDTO) {
        return Result.success(ordersService.pageQuery(BaseContext.getCurrentId(), queryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(ordersService.getDetail(id, BaseContext.getCurrentId()));
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@PathVariable Long id) {
        ordersService.cancel(BaseContext.getCurrentId(), id);
        return Result.success();
    }
}
