package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xcyms.entity.SysNotice;
import org.xcyms.entity.SysNoticeRead;
import org.xcyms.entity.dto.NoticeDTO;
import org.xcyms.mapper.SysNoticeMapper;
import org.xcyms.mapper.SysNoticeReadMapper;
import org.xcyms.observer.notice.NoticeObserver;
import org.xcyms.service.ISysNoticeService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    private static final String TARGET_TYPE_ALL = "ALL";
    private static final String TARGET_TYPE_USER = "USER";

    private final SysNoticeReadMapper noticeReadMapper;
    private final ModelMapper modelMapper;
    private final List<NoticeObserver> noticeObservers;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(NoticeDTO noticeDTO) {
        noticeDTO.setUserId(StpUtil.getLoginIdAsLong());
        noticeDTO.setStatus(1);
        this.save(modelMapper.map(noticeDTO, SysNotice.class));

        noticeObservers.forEach(observer -> {
            try {
                observer.onNoticePublished(noticeDTO);
            } catch (Exception e) {
                log.error("通知观察者执行失败: {}", observer.getObserverName(), e);
            }
        });
    }

    @Override
    public IPage<NoticeDTO> getMyNoticePage(Page<SysNotice> page, NoticeDTO noticeDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SysNotice> visibleNotices = listVisibleNotices(userId);
        Set<Long> readNoticeIds = getReadNoticeIds(userId);

        List<NoticeDTO> filteredRecords = visibleNotices.stream()
                .map(notice -> toNoticeDTO(notice, readNoticeIds))
                .filter(dto -> matchesReadFilter(dto, noticeDTO))
                .collect(Collectors.toList());

        long total = filteredRecords.size();
        long current = page.getCurrent();
        long size = page.getSize();
        long fromIndex = Math.max(0, (current - 1) * size);
        long toIndex = Math.min(total, fromIndex + size);

        List<NoticeDTO> pagedRecords = fromIndex >= total
                ? Collections.emptyList()
                : filteredRecords.subList((int) fromIndex, (int) toIndex);

        Page<NoticeDTO> dtoPage = new Page<>(current, size, total);
        dtoPage.setRecords(pagedRecords);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void readNotice(Long noticeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysNotice notice = this.getById(noticeId);
        if (!canAccessNotice(notice, userId)) {
            return;
        }

        Long count = noticeReadMapper.selectCount(new LambdaQueryWrapper<SysNoticeRead>()
                .eq(SysNoticeRead::getNoticeId, noticeId)
                .eq(SysNoticeRead::getUserId, userId));

        if (count == 0) {
            SysNoticeRead read = new SysNoticeRead();
            read.setNoticeId(noticeId);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            noticeReadMapper.insert(read);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void readAllNotices() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SysNotice> notices = listVisibleNotices(userId);
        Set<Long> readNoticeIds = getReadNoticeIds(userId);

        for (SysNotice notice : notices) {
            if (!readNoticeIds.contains(notice.getId())) {
                SysNoticeRead read = new SysNoticeRead();
                read.setNoticeId(notice.getId());
                read.setUserId(userId);
                read.setReadTime(LocalDateTime.now());
                noticeReadMapper.insert(read);
            }
        }
    }

    @Override
    public long getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<SysNotice> visibleNotices = listVisibleNotices(userId);
        Set<Long> readNoticeIds = getReadNoticeIds(userId);

        return visibleNotices.stream()
                .filter(notice -> !readNoticeIds.contains(notice.getId()))
                .count();
    }

    /**
     * 统一查询当前用户可见的通知集合，供分页、已读和未读统计复用。
     */
    private List<SysNotice> listVisibleNotices(Long userId) {
        QueryWrapper<SysNotice> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
                .and(w -> w.eq("target_type", TARGET_TYPE_ALL)
                        .or(inner -> inner.eq("target_type", TARGET_TYPE_USER).eq("target_user_id", userId)))
                .orderByDesc("create_time");
        return this.list(wrapper);
    }

    private Set<Long> getReadNoticeIds(Long userId) {
        return noticeReadMapper.selectList(new LambdaQueryWrapper<SysNoticeRead>()
                        .eq(SysNoticeRead::getUserId, userId)
                        .select(SysNoticeRead::getNoticeId))
                .stream()
                .map(SysNoticeRead::getNoticeId)
                .collect(Collectors.toSet());
    }

    private NoticeDTO toNoticeDTO(SysNotice notice, Set<Long> readNoticeIds) {
        NoticeDTO dto = modelMapper.map(notice, NoticeDTO.class);
        dto.setIsRead(readNoticeIds.contains(notice.getId()));
        return dto;
    }

    private boolean matchesReadFilter(NoticeDTO dto, NoticeDTO query) {
        if (query == null || query.getIsRead() == null) {
            return true;
        }
        return query.getIsRead().equals(dto.getIsRead());
    }

    private boolean canAccessNotice(SysNotice notice, Long userId) {
        if (notice == null || notice.getStatus() == null || notice.getStatus() != 1) {
            return false;
        }
        if (TARGET_TYPE_ALL.equals(notice.getTargetType())) {
            return true;
        }
        return TARGET_TYPE_USER.equals(notice.getTargetType()) && userId.equals(notice.getTargetUserId());
    }
}
