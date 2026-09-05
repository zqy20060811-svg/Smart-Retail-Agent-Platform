package com.retail.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dify Agent 配置（retail.dify.*）
 * enabled=false 或 base-url 为空时，AI 客服自动降级为本地 mock 回复
 */
@Data
@Component
@ConfigurationProperties(prefix = "retail.dify")
public class DifyProperties {

    /** 是否启用 Dify 真实调用 */
    private Boolean enabled = false;
    /** Dify 服务地址，例如 https://api.dify.ai */
    private String baseUrl;
    /** Dify App API-Key */
    private String apiKey;
}
