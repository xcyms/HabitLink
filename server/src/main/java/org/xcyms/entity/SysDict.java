package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典类型表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Getter
@Setter
@TableName("sys_dict")
public class SysDict implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiDocProperty("字典ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典名称
     */
    @ApiDocProperty("字典名称")
    @TableField("dict_name")
    private String dictName;

    /**
     * 字典类型
     */
    @ApiDocProperty("字典类型")
    @TableField("dict_type")
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    @ApiDocProperty("状态")
    @TableField("status")
    private Integer status;

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
