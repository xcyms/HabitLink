package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.time.LocalDate;
import java.util.List;

/**
 * 习惯 DTO。
 * 用于创建、编辑、详情和列表等场景。
 */
@Data
public class HabitDTO {

    @ApiDocProperty("习惯ID")
    private Long id;

    @ApiDocProperty(value = "习惯名称", required = true, example = "每天阅读")
    private String name;

    @ApiDocProperty(value = "习惯描述", example = "睡前阅读20分钟")
    private String description;

    @ApiDocProperty(value = "习惯分类", example = "STUDY")
    private String category;

    @ApiDocProperty(value = "习惯图标标识", example = "book")
    private String icon;

    @ApiDocProperty(value = "习惯主题色", example = "#3B82F6")
    private String color;

    @ApiDocProperty(value = "习惯状态", example = "1")
    private Integer status;

    @ApiDocProperty(value = "开始日期", required = true, example = "2026-06-08")
    private LocalDate startDate;

    @ApiDocProperty(value = "结束日期", example = "2026-12-31")
    private LocalDate endDate;

    @ApiDocProperty(value = "是否允许补打卡", example = "1")
    private Integer allowMakeup;

    @ApiDocProperty(value = "允许补打卡天数", example = "3")
    private Integer makeupLimitDays;

    @ApiDocProperty(value = "是否开启提醒", example = "1")
    private Integer reminderEnabled;

    @ApiDocProperty(value = "提醒时间", example = "21:00")
    private String reminderTime;

    @ApiDocProperty(value = "排序值", example = "1")
    private Integer sortOrder;

    @ApiDocProperty(value = "规则类型", example = "DAILY")
    private String ruleType;

    @ApiDocProperty(value = "按周执行时的星期列表", example = "[1,3,5]")
    private List<Integer> ruleDays;
}
