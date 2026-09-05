package com.retail.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建订单请求（骨架版：直接按商品+数量下单，支付/购物车流程后续扩展）
 */
@Data
public class OrderCreateDTO implements Serializable {

    private String phone;
    private String address;
    private String remark;

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO implements Serializable {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        @NotNull(message = "购买数量不能为空")
        private Integer number;
    }
}
