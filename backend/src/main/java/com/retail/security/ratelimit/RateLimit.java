package com.retail.security.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解：基于 Redis 固定窗口计数
 * 主要用于 AI 对话接口，防止模型调用被刷
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流业务 key，拼入 Redis key */
    String key() default "ai";

    /** 时间窗口内最大请求次数 */
    int max() default 10;

    /** 时间窗口（秒） */
    int window() default 60;
}
