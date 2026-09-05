package com.retail.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 管理端商品新增/编辑
 */
@Data
public class ProductDTO implements Serializable {

    private Long id;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名不能为空")
    private String name;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private String description;
    private String image;
    private Integer status;
    private Integer stock;
}
