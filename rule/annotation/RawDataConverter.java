package org.jeecg.modules.rule.annotation;


import org.jeecg.modules.rule.entity.enums.UnitType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @project_name: 后端平台项目
 * @description: 原始成绩值转换器
 * @author: kikock
 * @create_date: 2025-12-15 09:55
 */
@Component
public class RawDataConverter {

    private final Map<String, UnitConverter> convertersByUnit;
    private final Map<UnitType, UnitConverter> convertersByType;

    public RawDataConverter(List<UnitConverter> converters) {
        this.convertersByUnit = converters.stream()
                .collect(Collectors.toMap(
                        c -> c.getUnitType().toString(),
                        Function.identity()
                ));
        this.convertersByType = converters.stream()
                .collect(Collectors.toMap(
                        UnitConverter::getUnitType,
                        Function.identity()
                ));
    }

    public UnitConverter getConverterByUnit(String unit) {
        return convertersByUnit.values().stream()
                .filter(c -> c.supports(unit))
                .findFirst()
                .orElse(null);
    }

    public UnitConverter getConverterByType(UnitType type) {
        return convertersByType.get(type);
    }

    /**
     * 将原始成绩字符串安全地格式化为人类可读的显示字符串。
     * <p>
     * 此方法封装了“解析 -> 格式化”的完整流程，确保类型安全。
     *
     * @param rawPerformance 原始成绩字符串
     * @param unit           单位代码
     * @return 格式化后的显示字符串
     */
    @SuppressWarnings("unchecked")
    public String formatPerformance(String rawPerformance, String unit) {
        UnitConverter converter = getConverterByUnit(unit);
        if (converter == null || rawPerformance == null) {
            // 如果找不到转换器或输入为空，返回原始值
            return rawPerformance;
        }
        Object parsedValue = converter.parse(rawPerformance);
        return converter.format((Comparable) parsedValue, unit);
    }

    /**
     * 将原始成绩字符串转换保存数据
     * <p>
     * 此方法封装了“解析 -> 格式化”的完整流程，确保类型安全。
     *
     * @param rawPerformance 原始成绩字符串
     * @param unit           单位代码
     * @return 格式化后的显示字符串
     */
    @SuppressWarnings("unchecked")
    public String formatData(String rawPerformance, String unit) {
        UnitConverter converter = getConverterByUnit(unit);
        if (converter == null || rawPerformance == null) {
            // 如果找不到转换器或输入为空，返回原始值
            return rawPerformance;
        }
        Object parsedValue = converter.parse(rawPerformance);
        return converter.formatData((Comparable) parsedValue, unit);
    }
}