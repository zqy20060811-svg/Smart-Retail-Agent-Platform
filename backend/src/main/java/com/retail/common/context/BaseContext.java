package com.retail.common.context;

/**
 * 基于 ThreadLocal 的当前登录人上下文，由 JWT 拦截器写入
 */
public class BaseContext {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void remove() {
        CURRENT_ID.remove();
    }
}
