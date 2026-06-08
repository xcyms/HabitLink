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
 * 习惯打卡记录实体。
 */
@Getter
@Setter
@TableName("hl_check_in_record")
public class CheckInRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty(value = "记录ID", example = "1")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty(value = "所属用户ID", required = true, example = "1001")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty(value = "习惯ID", required = true, example = "1")
    @TableField("habit_id")
    private Long habitId;

    @ApiDocProperty(value = "打卡对应的业务日期", required = true, example = "2026-06-08")
    @TableField("record_date")
    private LocalDate recordDate;

    @ApiDocProperty(value = "实际提交打卡时间")
    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    @ApiDocProperty(value = "打卡状态", example = "1")
    @TableField("status")
    private Integer status;

    @ApiDocProperty(value = "打卡备注", example = "完成了20分钟阅读")
    @TableField("note")
    private String note;

    @ApiDocProperty(value = "打卡来源", example = "MINI_PROGRAM")
    @TableField("source")
    private String source;

    @ApiDocProperty(value = "是否补打卡", example = "0")
    @TableField("is_makeup")
    private Integer isMakeup;

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
