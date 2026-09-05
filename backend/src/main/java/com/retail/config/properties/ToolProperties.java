package com.retail.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent Tools 回调密钥配置（retail.tool.*）
 * Dify 平台调用本系统暴露的 Tool 接口时，需在请求头 X-Tool-Key 携带该密钥
 */
@Data
@Component
@ConfigurationProperties(prefix = "retail.tool")
public class ToolProperties {

    private String key;
}
