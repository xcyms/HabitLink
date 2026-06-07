package org.xcyms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 是否枚举
 */
@Getter
public enum YesNoEnum implements BaseEnum {

    YES(1, "是"),
    NO(0, "否");

    @EnumValue
    private final Integer code;
    private final String desc;

    YesNoEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}