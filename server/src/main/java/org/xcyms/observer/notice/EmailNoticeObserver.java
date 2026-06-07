package org.xcyms.observer.notice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xcyms.entity.dto.NoticeDTO;

/**
 * <p>
 * 邮件通知观察者
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
public class EmailNoticeObserver implements NoticeObserver {

    @Override
    public void onNoticePublished(NoticeDTO noticeDTO) {
        if ("ALL".equals(noticeDTO.getTargetType()) || "USER".equals(noticeDTO.getTargetType())) {
            log.info("发送邮件通知: title={}, targetType={}", noticeDTO.getTitle(), noticeDTO.getTargetType());
        }
    }

    @Override
    public String getObserverName() {
        return "邮件通知";
    }
}
