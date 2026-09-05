package com.retail.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 订单状态（轻量状态机，后续可扩展合法流转校验）
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING_PAYMENT(1, "待付款"),
    PENDING_ACCEPT(2, "待接单"),
    ACCEPTED(3, "已接单"),
    DELIVERY(4, "派送中"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消");

    private final Integer code;
    private final String desc;

    public static String descOf(Integer code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .map(OrderStatusEnum::getDesc)
                .findFirst()
                .orElse("未知状态");
    }
}
