package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.time.LocalDate;

/**
 * 打卡提交 DTO。
 */
@Data
public class CheckInDTO {

    @ApiDocProperty(value = "习惯ID", required = true, example = "1")
    private Long habitId;

    @ApiDocProperty(value = "打卡对应的业务日期", required = true, example = "2026-06-08")
    private LocalDate recordDate;

    @ApiDocProperty(value = "用户备注", example = "完成了20分钟阅读")
    private String note;

    @ApiDocProperty(value = "是否补打卡", example = "0")
    private Integer isMakeup;
}
