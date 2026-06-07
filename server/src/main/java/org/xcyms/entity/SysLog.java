package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统操作日志表
 * </p>
 *
 * @author liu-xu
 * @since 2026-04-19
 */
@Getter
@Setter
@TableName("sys_log")
public class SysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "日志ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "操作用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty(value = "操作用户名")
    @TableField("username")
    private String username;

    @ApiDocProperty(value = "操作描述")
    @TableField("operation")
    private String operation;

    @ApiDocProperty(value = "请求方法")
    @TableField("method")
    private String method;

    @ApiDocProperty(value = "请求URL")
    @TableField("url")
    private String url;

    @ApiDocProperty(value = "请求参数")
    @TableField("params")
    private String params;

    @ApiDocProperty(value = "响应结果")
    @TableField("result")
    private String result;

    @ApiDocProperty(value = "执行时长(毫秒)")
    @TableField("duration")
    private Long duration;

    @ApiDocProperty(value = "操作IP")
    @TableField("ip")
    private String ip;

    @ApiDocProperty(value = "状态 (0-失败, 1-成功)")
    @TableField("status")
    private Integer status;

    @ApiDocProperty(value = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

    @ApiDocProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
