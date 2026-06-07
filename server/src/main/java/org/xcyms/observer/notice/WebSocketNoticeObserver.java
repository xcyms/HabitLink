package org.xcyms.observer.notice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xcyms.entity.dto.NoticeDTO;
import org.xcyms.websocket.NoticeWebSocket;

/**
 * <p>
 * WebSocket通知观察者
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
public class WebSocketNoticeObserver implements NoticeObserver {

    @Override
    public void onNoticePublished(NoticeDTO noticeDTO) {
        String wsMsg = "NEW_NOTICE:" + noticeDTO.getTitle();
        if ("ALL".equals(noticeDTO.getTargetType())) {
            NoticeWebSocket.broadcast(wsMsg);
            log.info("WebSocket广播通知: {}", noticeDTO.getTitle());
        } else if ("USER".equals(noticeDTO.getTargetType()) && noticeDTO.getTargetUserId() != null) {
            NoticeWebSocket.sendMessage(noticeDTO.getTargetUserId(), wsMsg);
            log.info("WebSocket发送通知给用户: userId={}, title={}", noticeDTO.getTargetUserId(), noticeDTO.getTitle());
        }
    }

    @Override
    public String getObserverName() {
        return "WebSocket通知";
    }
}
