package com.retail.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品分页查询
 */
@Data
public class ProductPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    private Long categoryId;
    /** 商品名模糊搜索 */
    private String keyword;
    /** 状态过滤（管理端用） */
    private Integer status;
}
