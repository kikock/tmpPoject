package org.jeecg.modules.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @project_name: 后端平台项目
 * @description: excel下拉数据注解
 * @author: kikock
 * @create_date: 2025-12-15 09:55
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSelect {
    /**
     * 下拉列表的选项
     */
    String[] source() default {};

    /**
     * 如果选项需要从数据字典中动态获取，请填写字典编码
     */
    String dictCode() default "";
}
