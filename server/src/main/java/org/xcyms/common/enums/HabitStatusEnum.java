package org.xcyms.common.enums;

import lombok.Getter;

/**
 * 习惯状态枚举。
 */
@Getter
public enum HabitStatusEnum implements BaseEnum {

    /**
     * 启用中的习惯，会参与每日计划。
     */
    ACTIVE(1, "ACTIVE"),

    /**
     * 已暂停的习惯，不参与当前计划。
     */
    PAUSED(2, "PAUSED");

    private final Integer code;
    private final String desc;

    HabitStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
