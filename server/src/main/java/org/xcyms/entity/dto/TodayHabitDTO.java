package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.time.LocalDate;

/**
 * 首页习惯卡片 DTO。
 */
@Data
public class TodayHabitDTO {

    @ApiDocProperty("习惯ID")
    private Long habitId;

    @ApiDocProperty("习惯名称")
    private String habitName;

    @ApiDocProperty("图标标识")
    private String icon;

    @ApiDocProperty("主题色")
    private String color;

    @ApiDocProperty("习惯分类")
    private String category;

    @ApiDocProperty("提醒时间")
    private String reminderTime;

    @ApiDocProperty("当前连续完成天数")
    private Integer currentStreak;

    @ApiDocProperty("今日是否已完成")
    private Integer todayChecked;

    @ApiDocProperty("目标业务日期")
    private LocalDate todayDate;

    @ApiDocProperty("可读规则文案")
    private String ruleText;
}
