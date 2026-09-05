package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品（零售单品，如茶饮/小食）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String name;

    private BigDecimal price;

    private String description;

    private String image;

    /** 状态：1 起售 0 停售 */
    private Integer status;

    /** 累计销量 */
    private Integer sales;

    /** 库存 */
    private Integer stock;
}
