package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录详情 DTO。
 */
@Data
public class CheckInRecordDTO {

    @ApiDocProperty("记录ID")
    private Long id;

    @ApiDocProperty("习惯ID")
    private Long habitId;

    @ApiDocProperty("习惯名称")
    private String habitName;

    @ApiDocProperty("业务日期")
    private LocalDate recordDate;

    @ApiDocProperty("实际提交时间")
    private LocalDateTime checkInTime;

    @ApiDocProperty("打卡状态")
    private Integer status;

    @ApiDocProperty("用户备注")
    private String note;

    @ApiDocProperty("是否补打卡记录")
    private Integer isMakeup;

    @ApiDocProperty("打卡来源")
    private String source;
}
