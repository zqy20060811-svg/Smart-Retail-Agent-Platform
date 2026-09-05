package com.retail.ai;

import com.retail.config.properties.DifyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify Agent 客户端
 * 对接 Dify 服务端 API：POST {base-url}/v1/chat-messages
 *
 * 未启用（retail.dify.enabled=false 或 base-url 为空）时 chat() 返回 null，
 * 由 AiChatService 降级为本地 mock 回复。
 *
 * TODO: SSE 流式回复（response_mode=streaming），当前使用 blocking 模式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DifyClient {

    private final DifyProperties difyProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean isEnabled() {
        return Boolean.TRUE.equals(difyProperties.getEnabled())
                && StringUtils.hasText(difyProperties.getBaseUrl())
                && StringUtils.hasText(difyProperties.getApiKey());
    }

    /**
     * 调用 Dify 对话接口
     *
     * @param userId         业务用户ID（Dify 的 user 字段）
     * @param query          用户提问
     * @param conversationId Dify 会话ID，首轮为空
     * @return [answer, conversationId]，未启用或调用失败返回 null
     */
    @SuppressWarnings("unchecked")
    public String[] chat(Long userId, String query, String conversationId) {
        if (!isEnabled()) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(difyProperties.getApiKey());

            Map<String, Object> body = new HashMap<>();
            body.put("inputs", new HashMap<>());
            body.put("query", query);
            body.put("response_mode", "blocking");
            body.put("user", String.valueOf(userId));
            if (StringUtils.hasText(conversationId)) {
                body.put("conversation_id", conversationId);
            }

            String url = difyProperties.getBaseUrl().replaceAll("/$", "") + "/v1/chat-messages";
            Map<String, Object> resp = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
            if (resp == null) {
                return null;
            }
            String answer = (String) resp.get("answer");
            String cid = (String) resp.get("conversation_id");
            return new String[]{answer, cid};
        } catch (Exception e) {
            log.error("Dify 调用失败，将降级 mock: {}", e.getMessage());
            return null;
        }
    }
}
