package com.retail.security.interceptor;

import com.retail.common.constant.JwtClaimsConstant;
import com.retail.common.constant.MessageConstant;
import com.retail.common.context.BaseContext;
import com.retail.config.properties.JwtProperties;
import com.retail.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户端 JWT 拦截器：校验请求头 token，解析后将 userId 放入 BaseContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception e) {
            log.debug("用户 token 校验失败: {}", e.getMessage());
            writeUnauthorized(response, MessageConstant.USER_NOT_LOGIN);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }

    static void writeUnauthorized(HttpServletResponse response, String msg) {
        try {
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"" + msg + "\"}");
        } catch (Exception ignored) {
        }
    }
}
