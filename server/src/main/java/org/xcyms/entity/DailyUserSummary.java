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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日汇总实体。
 */
@Getter
@Setter
@TableName("hl_daily_user_summary")
public class DailyUserSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "汇总ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "所属用户ID", required = true, example = "1001")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty(value = "汇总日期", required = true, example = "2026-06-08")
    @TableField("summary_date")
    private LocalDate summaryDate;

    @ApiDocProperty(value = "计划习惯数量", example = "4")
    @TableField("planned_count")
    private Integer plannedCount;

    @ApiDocProperty(value = "已完成习惯数量", example = "2")
    @TableField("completed_count")
    private Integer completedCount;

    @ApiDocProperty(value = "完成率百分比", example = "50.00")
    @TableField("completion_rate")
    private BigDecimal completionRate;

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
