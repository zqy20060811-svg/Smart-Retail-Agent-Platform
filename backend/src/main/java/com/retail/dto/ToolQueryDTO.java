package com.retail.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Dify Agent Tools 回调入参（订单查询/商品搜索/优惠查询通用，字段按需使用）
 */
@Data
public class ToolQueryDTO implements Serializable {

    /** 查询订单时的用户ID（Dify 会话中透传） */
    private Long userId;
    /** 订单号 */
    private String orderNo;
    /** 订单状态 */
    private Integer status;
    /** 商品/优惠搜索关键词 */
    private String keyword;
}
