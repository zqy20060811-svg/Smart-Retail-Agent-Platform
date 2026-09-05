package com.retail.vo;

import com.retail.entity.OrderItem;
import com.retail.entity.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单详情：订单主信息 + 明细列表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Orders {

    private List<OrderItem> items;
}
