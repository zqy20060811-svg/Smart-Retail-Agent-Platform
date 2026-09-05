package com.retail.common.constant;

/**
 * 提示信息常量
 */
public class MessageConstant {

    public static final String LOGIN_FAILED = "用户名或密码错误";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String ADMIN_NOT_LOGIN = "管理员未登录";
    public static final String TOOL_KEY_INVALID = "工具调用密钥无效";
    public static final String USER_EXISTS = "用户名已存在";
    public static final String PRODUCT_NOT_FOUND = "商品不存在或已下架";
    public static final String ORDER_NOT_FOUND = "订单不存在";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String AI_RATE_LIMIT = "提问过于频繁，请稍后再试";
    public static final String AI_SERVICE_ERROR = "智能客服暂时不可用，请稍后再试";

    private MessageConstant() {
    }
}
