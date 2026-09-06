package com.retail.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.common.result.PageResult;
import com.retail.common.result.Result;
import com.retail.entity.Promotion;
import com.retail.service.PromotionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：优惠活动管理（优惠数据供 AI「优惠查询」工具检索）
 */
@RestController
@RequestMapping("/api/admin/promotion")
@RequiredArgsConstructor
@Api(tags = "管理端-优惠活动")
public class AdminPromotionController {

    private final PromotionService promotionService;

    @GetMapping("/page")
    @ApiOperation("优惠活动分页")
    public Result<PageResult<Promotion>> page(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "20") Integer pageSize,
                                              @RequestParam(required = false) String keyword) {
        Page<Promotion> p = promotionService.page(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Promotion>()
                        .like(StringUtils.hasText(keyword), Promotion::getTitle, keyword)
                        .orderByDesc(Promotion::getCreateTime));
        return Result.success(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    @ApiOperation("新增活动")
    public Result<Void> save(@RequestBody Promotion promotion) {
        promotion.setId(null);
        if (promotion.getStatus() == null) {
            promotion.setStatus(1);
        }
        promotionService.save(promotion);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑活动")
    public Result<Void> update(@RequestBody Promotion promotion) {
        promotionService.updateById(promotion);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("启用/停用活动")
    public Result<Void> status(@PathVariable Long id, @PathVariable Integer status) {
        Promotion promotion = new Promotion();
        promotion.setId(id);
        promotion.setStatus(status);
        promotionService.updateById(promotion);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除活动（逻辑删除）")
    public Result<Void> delete(@PathVariable Long id) {
        promotionService.removeById(id);
        return Result.success();
    }
}
