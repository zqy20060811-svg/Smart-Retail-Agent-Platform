package com.retail.security.ratelimit;

import com.retail.common.constant.MessageConstant;
import com.retail.common.constant.RedisConstant;
import com.retail.common.context.BaseContext;
import com.retail.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 限流切面：Redis INCR + 过期时间实现固定窗口
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        Long currentId = BaseContext.getCurrentId();
        String identity = currentId == null ? "anonymous" : String.valueOf(currentId);
        String redisKey = RedisConstant.AI_RATE_LIMIT_PREFIX + rateLimit.key() + ":" + identity;

        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            // 首次访问设置窗口过期时间
            stringRedisTemplate.expire(redisKey, rateLimit.window(), TimeUnit.SECONDS);
        }
        if (count != null && count > rateLimit.max()) {
            log.warn("接口限流触发: key={}, user={}, count={}", redisKey, identity, count);
            throw new BusinessException(MessageConstant.AI_RATE_LIMIT);
        }
        return joinPoint.proceed();
    }
}
