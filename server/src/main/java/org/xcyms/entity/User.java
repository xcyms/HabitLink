package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;
import org.xcyms.common.enums.YesNoEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Getter
@Setter
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "用户ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "用户名", required = true, example = "admin")
    @TableField("username")
    private String username;

    @ApiDocProperty(value = "密码", required = true, example = "******")
    @TableField("password")
    private String password;

    @ApiDocProperty(value = "昵称", example = "管理员")
    @TableField("nickname")
    private String nickname;

    @ApiDocProperty(value = "头像地址")
    @TableField("avatar")
    private String avatar;

    @ApiDocProperty(value = "邮箱", example = "admin@everkeep.com")
    @TableField("email")
    private String email;

    @ApiDocProperty(value = "手机号", example = "13800138000")
    @TableField("phone")
    private String phone;

    @ApiDocProperty(value = "状态 (0-启用, 1-禁用)")
    @TableField("status")
    private Integer status;

    @ApiDocProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiDocProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiDocProperty(value = "删除标志 (YES-已删除, NO-未删除)")
    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}