package com.retail.controller.ai;

import com.retail.ai.AgentToolService;
import com.retail.common.result.Result;
import com.retail.dto.ToolQueryDTO;
import com.retail.entity.Orders;
import com.retail.entity.Product;
import com.retail.entity.Promotion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dify Agent Tools 回调端点
 * 在 Dify 平台配置为自定义工具（OpenAPI schema），请求头需携带 X-Tool-Key
 */
@RestController
@RequestMapping("/api/ai/tools")
@RequiredArgsConstructor
@Api(tags = "AI-Agent工具回调")
public class AiToolController {

    private final AgentToolService agentToolService;

    @PostMapping("/order-query")
    @ApiOperation("Tool：订单查询")
    public Result<List<Orders>> orderQuery(@RequestBody ToolQueryDTO query) {
        return Result.success(agentToolService.queryOrder(query.getUserId(), query.getOrderNo(), query.getStatus()));
    }

    @PostMapping("/product-search")
    @ApiOperation("Tool：商品搜索")
    public Result<List<Product>> productSearch(@RequestBody ToolQueryDTO query) {
        return Result.success(agentToolService.searchProduct(query.getKeyword()));
    }

    @PostMapping("/discount-query")
    @ApiOperation("Tool：优惠查询")
    public Result<List<Promotion>> discountQuery(@RequestBody ToolQueryDTO query) {
        return Result.success(agentToolService.queryDiscount(query.getKeyword()));
    }
}
