package org.xcyms.common.enums;

import lombok.Getter;

/**
 * 习惯执行规则类型枚举。
 */
@Getter
public enum HabitRuleTypeEnum {

    /**
     * 每天执行。
     */
    DAILY("DAILY"),

    /**
     * 按指定星期执行。
     */
    WEEKLY("WEEKLY");

    private final String code;

    HabitRuleTypeEnum(String code) {
        this.code = code;
    }
}
