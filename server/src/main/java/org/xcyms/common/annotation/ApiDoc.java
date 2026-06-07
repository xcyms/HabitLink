package org.xcyms.common.annotation;

import java.lang.annotation.*;

/**
 * 接口文档注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDoc {
    /**
     * 接口/模块名称
     */
    String value() default "";

    /**
     * 详细描述
     */
    String notes() default "";

    /**
     * 排序
     */
    int order() default 0;
}