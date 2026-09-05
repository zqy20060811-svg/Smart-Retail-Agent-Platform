package com.retail.common.constant;

/**
 * Redis key 常量与缓存策略说明
 *
 * 缓存保护机制：
 *  - 穿透：查询为空时缓存空值 ""，短 TTL，避免恶意请求打到 MySQL
 *  - 击穿：热点 key 重建时使用 SETNX 互斥锁，只放一个请求回源
 *  - 雪崩：TTL 基础值上加随机偏移，避免大量 key 同时过期
 */
public class RedisConstant {

    /** 商品详情缓存前缀，完整 key = retail:product:{id} */
    public static final String PRODUCT_PREFIX = "retail:product:";

    /** 空值占位（防缓存穿透） */
    public static final String EMPTY_CACHE = "";

    /** 商品缓存基础 TTL（秒） */
    public static final long PRODUCT_TTL = 30 * 60L;

    /** 空值缓存 TTL（秒），较短 */
    public static final long EMPTY_TTL = 2 * 60L;

    /** 商品重建互斥锁前缀 */
    public static final String PRODUCT_LOCK_PREFIX = "retail:lock:product:";

    /** 分类列表缓存 */
    public static final String CATEGORY_LIST = "retail:category:list";
    public static final long CATEGORY_TTL = 60 * 60L;

    /** AI 会话上下文（最近对话）前缀，Hash 结构 */
    public static final String AI_CONTEXT_PREFIX = "retail:ai:context:";
    public static final long AI_CONTEXT_TTL = 30 * 60L;
    /** 上下文保留的最大消息条数 */
    public static final int AI_CONTEXT_MAX_SIZE = 10;

    /** AI 限流 key 前缀 */
    public static final String AI_RATE_LIMIT_PREFIX = "retail:ratelimit:ai:";

    private RedisConstant() {
    }
}
