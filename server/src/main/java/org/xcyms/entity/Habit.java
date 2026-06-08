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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 习惯主实体。
 */
@Getter
@Setter
@TableName("hl_habit")
public class Habit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "习惯ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "所属用户ID", required = true, example = "1001")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty(value = "习惯名称", required = true, example = "每天阅读")
    @TableField("name")
    private String name;

    @ApiDocProperty(value = "习惯描述", example = "睡前阅读20分钟")
    @TableField("description")
    private String description;

    @ApiDocProperty(value = "习惯分类编码", example = "STUDY")
    @TableField("category")
    private String category;

    @ApiDocProperty(value = "习惯图标标识", example = "book")
    @TableField("icon")
    private String icon;

    @ApiDocProperty(value = "习惯主题色", example = "#3B82F6")
    @TableField("color")
    private String color;

    @ApiDocProperty(value = "习惯状态", example = "1")
    @TableField("status")
    private Integer status;

    @ApiDocProperty(value = "开始日期", required = true, example = "2026-06-08")
    @TableField("start_date")
    private LocalDate startDate;

    @ApiDocProperty(value = "结束日期", example = "2026-12-31")
    @TableField("end_date")
    private LocalDate endDate;

    @ApiDocProperty(value = "是否允许补打卡", example = "1")
    @TableField("allow_makeup")
    private Integer allowMakeup;

    @ApiDocProperty(value = "允许补打卡天数", example = "3")
    @TableField("makeup_limit_days")
    private Integer makeupLimitDays;

    @ApiDocProperty(value = "是否开启提醒", example = "1")
    @TableField("reminder_enabled")
    private Integer reminderEnabled;

    @ApiDocProperty(value = "提醒时间", example = "21:00")
    @TableField("reminder_time")
    private String reminderTime;

    @ApiDocProperty(value = "排序值", example = "1")
    @TableField("sort_order")
    private Integer sortOrder;

    @ApiDocProperty(value = "最近一次完成打卡的业务日期", example = "2026-06-08")
    @TableField("last_check_in_date")
    private LocalDate lastCheckInDate;

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
