package org.xcyms.common.annotation;

import java.lang.annotation.*;

/**
 * 接口字段文档注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDocProperty {
    /**
     * 字段名称/描述
     */
    String value() default "";

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 示例值
     */
    String example() default "";
}