package com.retail.websocket;

import com.retail.common.constant.JwtClaimsConstant;
import com.retail.config.properties.JwtProperties;
import com.retail.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单状态 WebSocket 推送
 * 连接地址：ws://localhost:8080/ws/order/{token}
 * 管理端更新订单状态后，调用 {@link #sendToUser(Long, String)} 实时推送给对应用户
 */
@Slf4j
@Component
@ServerEndpoint("/ws/order/{token}")
public class WebSocketServer {

    /** userId -> 会话 */
    private static final Map<Long, Session> SESSION_POOL = new ConcurrentHashMap<>();

    private static JwtProperties jwtProperties;

    @Autowired
    public void setJwtProperties(JwtProperties properties) {
        WebSocketServer.jwtProperties = properties;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            SESSION_POOL.put(userId, session);
            log.info("WebSocket 建立连接, userId={}, 当前在线={}", userId, SESSION_POOL.size());
        } catch (Exception e) {
            log.warn("WebSocket 鉴权失败: {}", e.getMessage());
            try {
                session.close();
            } catch (Exception ignored) {
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        SESSION_POOL.values().removeIf(s -> s.getId().equals(session.getId()));
        log.info("WebSocket 断开, 当前在线={}", SESSION_POOL.size());
    }

    /**
     * 向指定用户推送消息
     */
    public static void sendToUser(Long userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("WebSocket 推送消息: userId={}, msg={}", userId, message);
            } catch (Exception e) {
                log.error("WebSocket 推送失败: userId={}", userId, e);
            }
        }
    }
}
