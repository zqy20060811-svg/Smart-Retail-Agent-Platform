package com.retail.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 直连大模型 Agent 配置（retail.llm.*）
 * OpenAI 兼容接口 + Function Calling，无需外部 Agent 平台。
 * api-key 为空时视为未启用，AI 客服自动降级为本地 mock 回复。
 */
@Data
@Component
@ConfigurationProperties(prefix = "retail.llm")
public class LlmProperties {

    /** 是否启用直连大模型（还需 api-key 非空才真正生效） */
    private Boolean enabled = false;
    /** OpenAI 兼容服务地址，代码会自动追加 /chat/completions */
    private String baseUrl = "https://api.deepseek.com";
    /** API Key，如 DeepSeek 的 sk-xxx，可用环境变量 ${DEEPSEEK_API_KEY:} 注入 */
    private String apiKey;
    /** 模型名：deepseek-chat / glm-4-flash / qwen-plus 等 */
    private String model = "deepseek-chat";
}
