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
 * 习惯统计汇总实体。
 */
@Getter
@Setter
@TableName("hl_habit_stats")
public class HabitStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "统计ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "所属用户ID", required = true, example = "1001")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty(value = "关联习惯ID", required = true, example = "1")
    @TableField("habit_id")
    private Long habitId;

    @ApiDocProperty(value = "当前连续完成天数", example = "6")
    @TableField("current_streak")
    private Integer currentStreak;

    @ApiDocProperty(value = "历史最长连续完成天数", example = "18")
    @TableField("longest_streak")
    private Integer longestStreak;

    @ApiDocProperty(value = "累计完成打卡次数", example = "28")
    @TableField("total_check_in_count")
    private Integer totalCheckInCount;

    @ApiDocProperty(value = "最近一次完成打卡的业务日期", example = "2026-06-08")
    @TableField("last_check_in_date")
    private LocalDate lastCheckInDate;

    @ApiDocProperty(value = "最近一次统计计算时间")
    @TableField("last_computed_at")
    private LocalDateTime lastComputedAt;

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
