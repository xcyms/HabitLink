package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 首页总览 DTO。
 */
@Data
public class TodayOverviewDTO {

    @ApiDocProperty("当前日期")
    private LocalDate date;

    @ApiDocProperty("计划习惯数量")
    private Integer plannedCount;

    @ApiDocProperty("已完成习惯数量")
    private Integer completedCount;

    @ApiDocProperty("完成率百分比")
    private BigDecimal completionRate;

    @ApiDocProperty("今日习惯卡片列表")
    private List<TodayHabitDTO> habits;
}
