package com.retail.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.common.result.PageResult;
import com.retail.common.result.Result;
import com.retail.entity.Category;
import com.retail.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端：商品分类管理
 */
@RestController
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
@Api(tags = "管理端-分类管理")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/page")
    @ApiOperation("分类分页")
    public Result<PageResult<Category>> page(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "20") Integer pageSize,
                                             @RequestParam(required = false) String keyword) {
        Page<Category> p = categoryService.page(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Category>()
                        .like(StringUtils.hasText(keyword), Category::getName, keyword)
                        .orderByAsc(Category::getSort));
        return Result.success(new PageResult<>(p.getTotal(), p.getRecords()));
    }

    @GetMapping("/list")
    @ApiOperation("全部分类（商品表单下拉用）")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort)));
    }

    @PostMapping
    @ApiOperation("新增分类")
    public Result<Void> save(@RequestBody Category category) {
        category.setId(null);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑分类")
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("启用/禁用分类")
    public Result<Void> status(@PathVariable Long id, @PathVariable Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类（逻辑删除）")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }
}
