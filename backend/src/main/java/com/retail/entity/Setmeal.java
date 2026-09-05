package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 套餐（多个商品组合，如「下午茶双人餐」）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("setmeal")
public class Setmeal extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long categoryId;

    private BigDecimal price;

    private String description;

    private String image;

    /** 状态：1 起售 0 停售 */
    private Integer status;
}
