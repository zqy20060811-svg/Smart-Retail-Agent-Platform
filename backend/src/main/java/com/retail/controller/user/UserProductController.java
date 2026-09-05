package com.retail.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.common.result.PageResult;
import com.retail.common.result.Result;
import com.retail.dto.ProductPageQueryDTO;
import com.retail.entity.Category;
import com.retail.entity.Product;
import com.retail.entity.Setmeal;
import com.retail.service.CategoryService;
import com.retail.service.ProductService;
import com.retail.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端用户：商品/分类/套餐浏览
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Api(tags = "用户端-商品浏览")
public class UserProductController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final SetmealService setmealService;

    @GetMapping("/categories")
    @ApiOperation("分类列表")
    public Result<List<Category>> categories() {
        return Result.success(categoryService.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)));
    }

    @GetMapping("/products")
    @ApiOperation("商品分页/条件查询")
    public Result<PageResult<Product>> products(ProductPageQueryDTO queryDTO) {
        // 用户端只看起售商品
        queryDTO.setStatus(1);
        return Result.success(productService.pageQuery(queryDTO));
    }

    @GetMapping("/products/{id}")
    @ApiOperation("商品详情（走缓存）")
    public Result<Product> productDetail(@PathVariable Long id) {
        return Result.success(productService.getByIdWithCache(id));
    }

    @GetMapping("/setmeals")
    @ApiOperation("套餐分页查询")
    public Result<PageResult<Setmeal>> setmeals(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Setmeal> p = setmealService.page(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, 1).orderByDesc(Setmeal::getUpdateTime));
        return Result.success(new PageResult<>(p.getTotal(), p.getRecords()));
    }
}
