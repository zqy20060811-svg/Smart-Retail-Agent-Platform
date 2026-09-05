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
 * 管理端 JWT 拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            BaseContext.setCurrentId(adminId);
            return true;
        } catch (Exception e) {
            log.debug("管理员 token 校验失败: {}", e.getMessage());
            JwtTokenUserInterceptor.writeUnauthorized(response, MessageConstant.ADMIN_NOT_LOGIN);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }
}
