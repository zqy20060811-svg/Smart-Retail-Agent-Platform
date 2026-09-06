package com.retail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.retail.ai.AgentToolService;
import com.retail.ai.DifyClient;
import com.retail.common.constant.MessageConstant;
import com.retail.common.constant.RedisConstant;
import com.retail.common.exception.BusinessException;
import com.retail.common.result.PageResult;
import com.retail.dto.AiChatDTO;
import com.retail.entity.AiChatMessage;
import com.retail.entity.AiChatSession;
import com.retail.entity.Orders;
import com.retail.entity.Product;
import com.retail.entity.Promotion;
import com.retail.service.AiChatMessageService;
import com.retail.service.AiChatService;
import com.retail.service.AiChatSessionService;
import com.retail.vo.AiReplyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI 客服服务
 *  - 会话列表/消息持久化：MySQL
 *  - 短期上下文：Redis（最近 N 条，TTL 30 分钟）
 *  - 回复来源：Dify Agent（真实）或本地知识库 mock（降级）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiChatSessionService sessionService;
    private final AiChatMessageService messageService;
    private final DifyClient difyClient;
    private final AgentToolService agentToolService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public AiReplyVO chat(Long userId, AiChatDTO chatDTO) {
        // 1. 获取或创建会话
        AiChatSession session;
        if (chatDTO.getSessionId() != null) {
            session = sessionService.getById(chatDTO.getSessionId());
            if (session == null || !userId.equals(session.getUserId())) {
                throw new BusinessException("会话不存在");
            }
        } else {
            session = new AiChatSession();
            session.setUserId(userId);
            session.setTitle(chatDTO.getContent().length() > 12
                    ? chatDTO.getContent().substring(0, 12) : chatDTO.getContent());
            sessionService.save(session);
        }

        // 2. 落库用户消息 + 写 Redis 上下文
        saveMessage(session.getId(), "user", chatDTO.getContent(), null);
        writeContext(userId, "user: " + chatDTO.getContent());

        // 3. 调用 Dify，失败/未配置则 mock 降级
        String reply;
        String source;
        String[] difyResult = difyClient.chat(userId, chatDTO.getContent(), session.getDifyConversationId());
        if (difyResult != null && StringUtils.hasText(difyResult[0])) {
            reply = difyResult[0];
            source = "dify";
            if (StringUtils.hasText(difyResult[1])) {
                session.setDifyConversationId(difyResult[1]);
                sessionService.updateById(session);
            }
        } else {
            reply = mockReply(userId, chatDTO.getContent());
            source = "mock";
        }

        // 4. 落库助手消息 + 写上下文
        saveMessage(session.getId(), "assistant", reply, null);
        writeContext(userId, "assistant: " + reply);

        return AiReplyVO.builder()
                .sessionId(session.getId())
                .reply(reply)
                .source(source)
                .build();
    }

    @Override
    public PageResult<AiChatSession> sessions(Long userId, Integer page, Integer pageSize) {
        Page<AiChatSession> p = new Page<>(page == null ? 1 : page, pageSize == null ? 10 : pageSize);
        sessionService.page(p, new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, userId)
                .orderByDesc(AiChatSession::getUpdateTime));
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public List<AiChatMessage> messages(Long userId, Long sessionId) {
        AiChatSession session = sessionService.getById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new BusinessException("会话不存在");
        }
        return messageService.list(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByAsc(AiChatMessage::getId));
    }

    private void saveMessage(Long sessionId, String role, String content, String toolName) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setToolName(toolName);
        messageService.save(message);
    }

    /**
     * Redis 维护最近上下文：List 结构，超出长度裁剪，每次访问刷新 TTL
     */
    private void writeContext(Long userId, String line) {
        String key = RedisConstant.AI_CONTEXT_PREFIX + userId;
        try {
            stringRedisTemplate.opsForList().rightPush(key, line);
            stringRedisTemplate.opsForList().trim(key, -RedisConstant.AI_CONTEXT_MAX_SIZE, -1);
            stringRedisTemplate.expire(key, RedisConstant.AI_CONTEXT_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("AI 上下文写入 Redis 失败（不影响主流程）: {}", e.getMessage());
        }
    }

    /**
     * 本地知识库 mock 回复：简单意图识别 + 调用 Agent Tools 封装的查询能力
     */
    private String mockReply(Long userId, String query) {
        String q = query.toLowerCase();
        try {
            if (q.contains("订单") || q.contains("order") || q.contains("做好") || q.contains("出餐") || q.contains("到哪")) {
                List<Orders> orders = agentToolService.queryOrder(userId, null, null);
                if (orders.isEmpty()) {
                    return "您目前还没有订单哦，可以先去商品页挑选喜欢的商品下单~";
                }
                String detail = orders.stream().limit(5)
                        .map(o -> "订单号 " + o.getOrderNo() + "，金额 ¥" + o.getAmount()
                                + "，状态：" + com.retail.common.enums.OrderStatusEnum.descOf(o.getStatus()))
                        .collect(Collectors.joining("\n"));
                return "为您查到最近的订单信息：\n" + detail;
            }
            if (q.contains("优惠") || q.contains("活动") || q.contains("折扣") || q.contains("券")) {
                List<Promotion> promotions = agentToolService.queryDiscount(null);
                if (promotions.isEmpty()) {
                    return "当前暂无进行中的优惠活动，上新后会第一时间通知您~";
                }
                String detail = promotions.stream()
                        .map(p -> "【" + p.getTitle() + "】" + p.getContent())
                        .collect(Collectors.joining("\n"));
                return "当前正在进行的优惠活动：\n" + detail;
            }
            if (q.contains("商品") || q.contains("菜单") || q.contains("有什么") || q.contains("推荐")
                    || q.contains("价格") || q.contains("奶茶") || q.contains("柠檬")) {
                List<Product> products = agentToolService.searchProduct(null);
                String detail = products.stream().limit(8)
                        .map(p -> p.getName() + " ¥" + p.getPrice())
                        .collect(Collectors.joining("、"));
                return "为您推荐以下热销商品：" + detail + "。告诉我商品名可以查详情哦~";
            }
            return "您好！我是智能零售客服，可以帮您：\n"
                    + "1. 查询订单状态（如「我的订单到哪了」）\n"
                    + "2. 搜索商品（如「有什么奶茶」）\n"
                    + "3. 查询优惠活动（如「最近有什么优惠」）\n"
                    + "（当前为本地模拟回复，配置 Dify 后将接入大模型 Agent）";
        } catch (Exception e) {
            log.error("mock 回复生成失败", e);
            return MessageConstant.AI_SERVICE_ERROR;
        }
    }
}
