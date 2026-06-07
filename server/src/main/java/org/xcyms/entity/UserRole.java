package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;

import java.io.Serializable;

/**
 * <p>
 * 用户角色表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Getter
@Setter
@TableName("sys_user_role")
public class UserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty("用户ID")
    @TableId("user_id")
    private Long userId;

    @ApiDocProperty("角色ID")
    @TableField("role_id")
    private Long roleId;
}