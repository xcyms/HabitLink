package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.SysNotice;
import org.xcyms.entity.dto.NoticeDTO;
import org.xcyms.service.ISysNoticeService;

/**
 * 系统通知控制器
 */
@ApiDoc("系统通知")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class SysNoticeController {

    private final ISysNoticeService noticeService;

    @Log("发布系统通知")
    @ApiDoc(value = "发布通知", notes = "管理员发布系统公告或站内信")
    @SaCheckRole("ADMIN")
    @PostMapping("/publish")
    public ApiResult<Void> publish(@RequestBody NoticeDTO noticeDTO) {
        noticeService.publish(noticeDTO);
        return ApiResult.success();
    }

    @ApiDoc(value = "获取我的通知列表", notes = "分页查询当前用户的通知记录")
    @PostMapping("/my-page")
    public ApiResult<Page<NoticeDTO>> getMyPage(Page<SysNotice> page, @RequestBody NoticeDTO noticeDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        noticeDTO.setUserId(userId);
        return ApiResult.success((Page<NoticeDTO>) noticeService.getMyNoticePage(page, noticeDTO));
    }

    @ApiDoc(value = "标记通知为已读")
    @GetMapping("/read/{id}")
    public ApiResult<Void> read(@PathVariable Long id) {
        noticeService.readNotice(id);
        return ApiResult.success();
    }

    /**
     * 将当前用户可见的通知全部标记为已读
     */
    @ApiDoc(value = "全部标记为已读")
    @PostMapping("/read-all")
    public ApiResult<Void> readAll() {
        noticeService.readAllNotices();
        return ApiResult.success();
    }

    @ApiDoc(value = "获取未读通知数量")
    @GetMapping("/unread-count")
    public ApiResult<Long> getUnreadCount() {
        return ApiResult.success(noticeService.getUnreadCount());
    }
}
