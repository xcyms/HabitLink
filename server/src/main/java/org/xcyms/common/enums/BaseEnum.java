package org.xcyms.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 枚举基类接口，用于统一 Jackson 序列化/反序列化逻辑
 */
public interface BaseEnum {
    /**
     * 获取枚举值（存储到数据库的值）
     */
    @JsonValue
    Integer getCode();

    /**
     * 获取枚举描述（展示给前端的值）
     */
    String getDesc();
}
