package org.xcyms.observer.notice;

import org.xcyms.entity.dto.NoticeDTO;

/**
 * <p>
 * 通知观察者接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
public interface NoticeObserver {

    /**
     * 通知发布时触发
     *
     * @param noticeDTO 通知实体
     * @author liu-xu
     * @date 2026/05/11
     */
    void onNoticePublished(NoticeDTO noticeDTO);

    /**
     * 获取观察者名称
     *
     * @return java.lang.String
     * @author liu-xu
     * @date 2026/05/11
     */
    String getObserverName();
}
