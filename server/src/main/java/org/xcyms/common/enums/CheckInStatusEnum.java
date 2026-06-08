package org.xcyms.common.enums;

import lombok.Getter;

/**
 * 打卡状态枚举。
 */
@Getter
public enum CheckInStatusEnum implements BaseEnum {

    /**
     * 目标业务日期已完成。
     */
    COMPLETED(1, "COMPLETED");

    private final Integer code;
    private final String desc;

    CheckInStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
