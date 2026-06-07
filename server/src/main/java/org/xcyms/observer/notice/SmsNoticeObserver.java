package org.xcyms.observer.notice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xcyms.entity.dto.NoticeDTO;

/**
 * <p>
 * 短信通知观察者
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
public class SmsNoticeObserver implements NoticeObserver {

    @Override
    public void onNoticePublished(NoticeDTO noticeDTO) {
        if ("USER".equals(noticeDTO.getTargetType()) && noticeDTO.getTargetUserId() != null) {
            log.info("发送短信通知给用户: userId={}, title={}", noticeDTO.getTargetUserId(), noticeDTO.getTitle());
        }
    }

    @Override
    public String getObserverName() {
        return "短信通知";
    }
}
