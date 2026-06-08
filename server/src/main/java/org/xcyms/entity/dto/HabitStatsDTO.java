package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 习惯统计 DTO。
 */
@Data
public class HabitStatsDTO {

    @ApiDocProperty("习惯ID")
    private Long habitId;

    @ApiDocProperty("习惯名称")
    private String habitName;

    @ApiDocProperty("当前连续完成天数")
    private Integer currentStreak;

    @ApiDocProperty("历史最长连续完成天数")
    private Integer longestStreak;

    @ApiDocProperty("累计完成打卡次数")
    private Integer totalCheckInCount;

    @ApiDocProperty("近7天完成率")
    private BigDecimal completionRate7d;

    @ApiDocProperty("近30天完成率")
    private BigDecimal completionRate30d;

    @ApiDocProperty("最近一次完成打卡的业务日期")
    private LocalDate lastCheckInDate;
}
