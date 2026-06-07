package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统通知公告表
 * </p>
 *
 * @author liu-xu
 * @since 2026-04-19
 */
@Getter
@Setter
@TableName("sys_notice")
public class SysNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "公告ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "公告标题", required = true)
    @TableField("title")
    private String title;

    @ApiDocProperty(value = "公告内容", required = true)
    @TableField("content")
    private String content;

    @ApiDocProperty(value = "类型 (info, success, warning, error)")
    @TableField("type")
    private String type;

    @ApiDocProperty(value = "状态 (0-下线, 1-发布)")
    @TableField("status")
    private Integer status;

    @ApiDocProperty(value = "发布人ID")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty(value = "目标类型 (ALL-全体, USER-指定用户)")
    @TableField("target_type")
    private String targetType;

    @ApiDocProperty(value = "目标用户ID")
    @TableField("target_user_id")
    private Long targetUserId;

    @ApiDocProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiDocProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
