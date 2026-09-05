package com.retail.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单分页查询
 */
@Data
public class OrderPageQueryDTO implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    /** 订单状态，见 OrderStatusEnum */
    private Integer status;
    /** 订单号模糊搜索（管理端用） */
    private String orderNo;
}
