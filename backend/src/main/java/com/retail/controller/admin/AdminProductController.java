package com.retail.controller.admin;

import com.retail.common.result.PageResult;
import com.retail.common.result.Result;
import com.retail.dto.ProductDTO;
import com.retail.dto.ProductPageQueryDTO;
import com.retail.entity.Product;
import com.retail.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端：商品管理
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Api(tags = "管理端-商品管理")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping("/page")
    @ApiOperation("商品分页（含停售）")
    public Result<PageResult<Product>> page(ProductPageQueryDTO queryDTO) {
        return Result.success(productService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @ApiOperation("商品详情")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getByIdWithCache(id));
    }

    @PostMapping
    @ApiOperation("新增商品")
    public Result<Void> save(@Valid @RequestBody ProductDTO productDTO) {
        productDTO.setId(null);
        productService.saveOrUpdateProduct(productDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("编辑商品")
    public Result<Void> update(@Valid @RequestBody ProductDTO productDTO) {
        productService.saveOrUpdateProduct(productDTO);
        return Result.success();
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("起售/停售")
    public Result<Void> status(@PathVariable Long id, @PathVariable Integer status) {
        productService.toggleStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除商品（逻辑删除）")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
