package com.retail.security.interceptor;

import com.retail.common.constant.MessageConstant;
import com.retail.config.properties.ToolProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Agent Tools 回调拦截器：校验 Dify 平台回调时携带的 X-Tool-Key
 */
@Component
@RequiredArgsConstructor
public class ToolKeyInterceptor implements HandlerInterceptor {

    private final ToolProperties toolProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = request.getHeader("X-Tool-Key");
        if (key != null && key.equals(toolProperties.getKey())) {
            return true;
        }
        JwtTokenUserInterceptor.writeUnauthorized(response, MessageConstant.TOOL_KEY_INVALID);
        return false;
    }
}
