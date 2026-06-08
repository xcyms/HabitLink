package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;
import org.xcyms.common.enums.YesNoEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 习惯执行规则实体。
 */
@Getter
@Setter
@TableName("hl_habit_schedule_rule")
public class HabitScheduleRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "规则ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "关联习惯ID", required = true, example = "1")
    @TableField("habit_id")
    private Long habitId;

    @ApiDocProperty(value = "规则类型", required = true, example = "DAILY")
    @TableField("rule_type")
    private String ruleType;

    @ApiDocProperty(value = "规则内容", example = "{\"days\":[1,3,5]}")
    @TableField("rule_value")
    private String ruleValue;

    @ApiDocProperty(value = "时区", example = "Asia/Shanghai")
    @TableField("timezone")
    private String timezone;

    @ApiDocProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiDocProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiDocProperty(value = "逻辑删除标记", example = "0")
    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;
}
