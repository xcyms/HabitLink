package org.xcyms.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告 DTO
 */
@Data
public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer status;
    private Long userId;
    private String targetType;
    private Long targetUserId;
    private LocalDateTime createTime;
    
    // 扩展字段
    private Boolean isRead;
}
