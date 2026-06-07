package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 系统/用户配置表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Getter
@Setter
@TableName("sys_config")
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiDocProperty("配置ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID (NULL表示系统默认配置)
     */
    @ApiDocProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 配置键 (如: max_file_size, upload_path, allowed_extensions)
     */
    @ApiDocProperty("配置键")
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @ApiDocProperty("配置值")
    @TableField("config_value")
    private String configValue;

    /**
     * 配置名称
     */
    @ApiDocProperty("配置名称")
    @TableField("config_name")
    private String configName;

    /**
     * 备注
     */
    @ApiDocProperty("备注")
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @ApiDocProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiDocProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}