package com.retail.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置（retail.jwt.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "retail.jwt")
public class JwtProperties {

    private String adminSecretKey;
    private String userSecretKey;
    private Long ttl;
}
