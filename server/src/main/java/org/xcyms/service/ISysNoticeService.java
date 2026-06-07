package org.xcyms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.entity.SysNotice;
import org.xcyms.entity.dto.NoticeDTO;

/**
 * 系统通知服务接口
 */
public interface ISysNoticeService extends IService<SysNotice> {
    /**
     * 发布通知
     */
    void publish(NoticeDTO noticeDTO);

    /**
     * 分页查询当前用户可见的通知
     */
    IPage<NoticeDTO> getMyNoticePage(Page<SysNotice> page, NoticeDTO noticeDTO);

    /**
     * 标记单条通知为已读
     */
    void readNotice(Long noticeId);

    /**
     * 将当前用户的所有通知标记为已读
     */
    void readAllNotices();

    /**
     * 获取当前用户的未读通知数量
     */
    long getUnreadCount();
}
