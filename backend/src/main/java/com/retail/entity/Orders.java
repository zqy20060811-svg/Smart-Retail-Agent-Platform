package com.retail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Orders extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号（业务唯一，高频查询字段，已建索引） */
    private String orderNo;

    private Long userId;

    private BigDecimal amount;

    /** 订单状态，见 OrderStatusEnum */
    private Integer status;

    /** 支付状态：0 未支付 1 已支付 2 已退款 */
    private Integer payStatus;

    /** 订单备注（如：少冰、不加糖） */
    private String remark;
}
