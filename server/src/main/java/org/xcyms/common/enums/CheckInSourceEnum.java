package org.xcyms.common.enums;

import lombok.Getter;

/**
 * 打卡来源枚举。
 */
@Getter
public enum CheckInSourceEnum {

    /**
     * 来自小程序。
     */
    MINI_PROGRAM("MINI_PROGRAM"),

    /**
     * 来自网页或后台工具。
     */
    WEB("WEB"),

    /**
     * 由内部任务或数据迁移生成。
     */
    SYSTEM("SYSTEM");

    private final String code;

    CheckInSourceEnum(String code) {
        this.code = code;
    }
}
