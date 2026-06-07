package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 字典数据表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Getter
@Setter
@TableName("sys_dict_data")
public class SysDictData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ApiDocProperty("字典数据ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型ID
     */
    @ApiDocProperty("字典类型ID")
    @TableField("dict_id")
    private Long dictId;

    /**
     * 字典标签
     */
    @ApiDocProperty("字典标签")
    @TableField("dict_label")
    private String dictLabel;

    /**
     * 字典键值
     */
    @ApiDocProperty("字典键值")
    @TableField("dict_value")
    private String dictValue;

    /**
     * 字典类型
     */
    @ApiDocProperty("字典类型")
    @TableField("dict_type")
    private String dictType;

    /**
     * 显示排序
     */
    @ApiDocProperty("显示排序")
    @TableField("dict_sort")
    private Integer dictSort;

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
