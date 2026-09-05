package com.retail.config;

import com.retail.security.interceptor.JwtTokenAdminInterceptor;
import com.retail.security.interceptor.JwtTokenUserInterceptor;
import com.retail.security.interceptor.ToolKeyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册拦截器、放行登录/注册路径、开启跨域
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtTokenUserInterceptor jwtTokenUserInterceptor;
    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    private final ToolKeyInterceptor toolKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端：登录/注册放行
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/api/user/**")
                .excludePathPatterns(
                        "/api/user/auth/login",
                        "/api/user/auth/register"
                );

        // 管理端：登录放行
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");

        // Dify Agent Tools 回调：X-Tool-Key 校验
        registry.addInterceptor(toolKeyInterceptor)
                .addPathPatterns("/api/ai/tools/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
