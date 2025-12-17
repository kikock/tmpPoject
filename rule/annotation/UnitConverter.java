package org.jeecg.modules.rule.annotation;


import org.jeecg.modules.rule.entity.enums.UnitType;

import java.math.BigDecimal;

/**
 * @project_name: 后端平台项目
 * @description: 单位转换接口
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public interface UnitConverter<T extends Comparable<T>> {
    /**
     * 将原始值解析为与转换器关联的泛型类型 T。
     *
     * @param value 原始值 (通常是 String)
     * @return 转换后的泛型类型对象
     */
    T parse(Object value);

    /**
     * 将原始值转换为统一的、可比较的类型
     *
     * @param value 原始值 (可以是 String, Double 等)
     * @return 转换后的统一类型对象
     */
    BigDecimal convert(Object value);

    /**
     * 转为为数据库保存数据
     *
     * @param value 原始值 (可以是 String, Double 等)
     * @return 转换后的统一类型对象
     */
    String formatData(T value, String unit);

    /**
     * 将泛型类型 T 的值格式化为人类可读的字符串。
     *
     * @param value 经过 parse 方法转换后的对象
     * @param unit  原始单位，用于决定格式
     * @return 格式化后的显示字符串
     */
    String format(T value, String unit);

    /**
     * 判断当前转换器是否支持指定的单位
     *
     * @param unit 计量单位字符串
     * @return 如果支持则返回 true
     */
    boolean supports(String unit);

    /**
     * 获取当前转换器支持的单位类型
     *
     * @return UnitType 枚举
     */
    UnitType getUnitType();
}
