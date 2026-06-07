package org.xcyms.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息通知 WebSocket 服务端
 */
@Slf4j
@Component
@ServerEndpoint("/ws/notice/{userId}")
public class NoticeWebSocket {

    /**
     * 存放所有在线用户的会话
     */
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSIONS.put(userId, session);
        log.info("WebSocket 连接成功: userId={}, 当前在线人数={}", userId, SESSIONS.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
        log.info("WebSocket 连接关闭: userId={}, 当前在线人数={}", userId, SESSIONS.size());
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误: {}", error.getMessage());
    }

    /**
     * 发送文本消息给指定用户
     */
    public static void sendMessage(Long userId, String message) {
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送消息失败: userId={}, error={}", userId, e.getMessage());
            }
        }
    }

    /**
     * 群发消息 (全体通知)
     */
    public static void broadcast(String message) {
        SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("广播消息失败: userId={}, error={}", userId, e.getMessage());
                }
            }
        });
    }
}
