package com.retail.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.config.properties.LlmProperties;
import com.retail.entity.Orders;
import com.retail.entity.Product;
import com.retail.entity.Promotion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 直连大模型 Agent 客户端（OpenAI 兼容 Function Calling，如 DeepSeek）
 *
 * 工作方式：把 AgentToolService 的三个查询能力注册为 functions，
 * 模型自主决定调用哪个工具 → 本地直接执行（不走 HTTP 回调）→ 结果回传 →
 * 模型继续推理，直到给出最终回答，最多迭代 {@link #MAX_ITERATIONS} 轮。
 *
 * 未启用（retail.llm.enabled=false 或 api-key 为空）或调用失败时返回 null，
 * 由 AiChatService 降级为 Dify / 本地 mock 回复。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmAgentClient {

    private static final int MAX_ITERATIONS = 6;
    private static final String SYSTEM_PROMPT = """
            你是线下奶茶门店「智能零售」的客服助手，必须通过调用工具获取真实数据后作答，禁止编造订单、商品、优惠信息。

            规则：
            1. 顾客问订单进度（如"我的奶茶做好了吗"）→ 调用 query_orders，按状态码解释：2=待接单，3=制作中，5=已完成可取餐，6=已取消。
            2. 顾客问商品/菜单/推荐 → 调用 search_products，keyword 从问题提取；泛泛地问（如"有什么好喝的"）传空字符串。
            3. 顾客问优惠/活动 → 调用 query_discounts，查全部时 keyword 传空字符串。
            4. 本店是线下点单、到店取餐，没有外卖配送环节，回答中不要出现派送、快递、骑手等说法。
            5. 用中文简洁亲切地回答，金额用 ¥ 表示，先给结论再列要点。顾客闲聊时正常对话即可，不必强行调用工具。
            """;

    private final LlmProperties llmProperties;
    private final AgentToolService agentToolService;
    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = buildRestTemplate();

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(120_000);
        return new RestTemplate(factory);
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(llmProperties.getEnabled())
                && StringUtils.hasText(llmProperties.getBaseUrl())
                && StringUtils.hasText(llmProperties.getApiKey());
    }

    /**
     * 调用大模型 Agent 生成回复
     *
     * @param userId  当前登录用户ID（工具执行时注入，模型无法越权查他人订单）
     * @param query   用户提问
     * @param history 最近对话上下文（"user: xxx" / "assistant: xxx" 行，可为空）
     * @return 最终回答；未启用或失败返回 null
     */
    public String chat(Long userId, String query, List<String> history) {
        if (!isEnabled()) {
            return null;
        }
        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message("system", SYSTEM_PROMPT));
            if (history != null) {
                for (String line : history) {
                    if (StringUtils.hasText(line)) {
                        if (line.startsWith("assistant: ")) {
                            messages.add(message("assistant", line.substring("assistant: ".length())));
                        } else if (line.startsWith("user: ")) {
                            messages.add(message("user", line.substring("user: ".length())));
                        }
                    }
                }
            }
            messages.add(message("user", query));

            for (int i = 0; i < MAX_ITERATIONS; i++) {
                JsonNode assistant = callChatCompletions(messages);
                JsonNode toolCalls = assistant.path("tool_calls");
                if (!toolCalls.isArray() || toolCalls.isEmpty()) {
                    String content = assistant.path("content").asText("").trim();
                    return StringUtils.hasText(content) ? content : null;
                }

                // 追加带 tool_calls 的 assistant 消息，执行工具并回传结果
                messages.add(objectMapper.convertValue(assistant, Map.class));
                for (JsonNode toolCall : toolCalls) {
                    String toolCallId = toolCall.path("id").asText();
                    String name = toolCall.path("function").path("name").asText();
                    String arguments = toolCall.path("function").path("arguments").asText("{}");
                    String result = executeTool(userId, name, arguments);
                    log.info("LLM Agent 调用工具: {} 参数: {} -> {} 字符", name, arguments, result.length());

                    Map<String, Object> toolMessage = new LinkedHashMap<>();
                    toolMessage.put("role", "tool");
                    toolMessage.put("tool_call_id", toolCallId);
                    toolMessage.put("content", result);
                    messages.add(toolMessage);
                }
            }
            log.warn("LLM Agent 超出最大迭代次数 {}，放弃本次回复", MAX_ITERATIONS);
            return null;
        } catch (Exception e) {
            log.error("直连大模型 Agent 调用失败，将降级: {}", e.getMessage());
            return null;
        }
    }

    private JsonNode callChatCompletions(List<Map<String, Object>> messages) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", llmProperties.getModel());
        body.put("messages", messages);
        body.put("tools", toolDefinitions());
        body.put("tool_choice", "auto");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(llmProperties.getApiKey());

        String url = llmProperties.getBaseUrl().replaceAll("/$", "") + "/chat/completions";
        String resp = restTemplate.postForObject(url, new HttpEntity<>(body, headers), String.class);
        JsonNode root = objectMapper.readTree(resp);
        JsonNode message = root.path("choices").path(0).path("message");
        if (message.isMissingNode()) {
            throw new IllegalStateException("大模型响应缺少 choices[0].message: "
                    + root.path("error").path("message").asText(resp));
        }
        return message;
    }

    /** 本地执行工具调用，返回精简 JSON（省 token） */
    private String executeTool(Long userId, String name, String arguments) {
        Map<String, Object> view = new LinkedHashMap<>();
        try {
            JsonNode args = objectMapper.readTree(arguments == null || arguments.isBlank() ? "{}" : arguments);
            switch (name) {
                case "query_orders" -> {
                    Integer status = args.hasNonNull("status") ? args.get("status").asInt() : null;
                    String orderNo = args.hasNonNull("order_no") ? args.get("order_no").asText() : null;
                    List<Orders> orders = agentToolService.queryOrder(userId, orderNo, status);
                    view.put("orders", orders.stream().map(o -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("order_no", o.getOrderNo());
                        m.put("amount", o.getAmount());
                        m.put("status", o.getStatus());
                        m.put("remark", o.getRemark());
                        m.put("create_time", String.valueOf(o.getCreateTime()));
                        return m;
                    }).toList());
                }
                case "search_products" -> {
                    String keyword = args.hasNonNull("keyword") ? args.get("keyword").asText("") : "";
                    List<Product> products = agentToolService.searchProduct(keyword);
                    view.put("products", products.stream().map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("name", p.getName());
                        m.put("price", p.getPrice());
                        m.put("sales", p.getSales());
                        return m;
                    }).toList());
                }
                case "query_discounts" -> {
                    String keyword = args.hasNonNull("keyword") ? args.get("keyword").asText("") : "";
                    List<Promotion> promotions = agentToolService.queryDiscount(keyword);
                    view.put("promotions", promotions.stream().map(p -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("title", p.getTitle());
                        m.put("type", p.getType() == 1 ? "满减" : p.getType() == 2 ? "折扣" : "赠品");
                        m.put("content", p.getContent());
                        return m;
                    }).toList());
                }
                default -> view.put("error", "未知工具: " + name);
            }
        } catch (Exception e) {
            log.error("工具 {} 执行失败: {}", name, e.getMessage());
            view.put("error", "工具执行失败: " + e.getMessage());
        }
        try {
            return objectMapper.writeValueAsString(view);
        } catch (Exception e) {
            return "{\"error\":\"结果序列化失败\"}";
        }
    }

    /** 三个工具的 OpenAI Function 定义（userId 由服务端注入，不暴露给模型） */
    private List<Map<String, Object>> toolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        tools.add(tool("query_orders",
                "查询当前顾客的订单列表（最新在前，最多10条），用于回答订单进度/状态问题",
                params(Map.of(
                        "status", Map.of("type", "integer",
                                "description", "订单状态筛选：2待接单 3制作中 5已完成 6已取消，不传查全部"),
                        "order_no", Map.of("type", "string", "description", "订单号，仅顾客明确给出时传")))));

        tools.add(tool("search_products", "按关键词搜索门店在售商品（奶茶/果茶/咖啡/小食），返回名称、价格、销量",
                params(Map.of("keyword", Map.of("type", "string",
                        "description", "商品关键词，如 奶茶、咖啡；顾客泛泛地问时传空字符串")))));

        tools.add(tool("query_discounts", "查询门店当前进行中的优惠活动（满减/折扣/赠品）",
                params(Map.of("keyword", Map.of("type", "string",
                        "description", "活动关键词，如 满减、第二杯；查全部时传空字符串")))));

        return tools;
    }

    private Map<String, Object> tool(String name, String description, Map<String, Object> parameters) {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", parameters);
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("type", "function");
        wrapper.put("function", function);
        return wrapper;
    }

    private Map<String, Object> params(Map<String, Object> properties) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("type", "object");
        p.put("properties", properties);
        return p;
    }

    private Map<String, Object> message(String role, String content) {
        Map<String, Object> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }
}
