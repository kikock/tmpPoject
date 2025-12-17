package org.jeecg.modules.rule.strategy;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.annotation.RawDataConverter;
import org.jeecg.modules.rule.annotation.UnitConverter;
import org.jeecg.modules.rule.entity.domain.RawRule;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @description: "区间适配" 计算策略
 * @author: kikock
 * @create_date: 2025-10-12
 **/
@Component
@Slf4j
public class RangeStrategy implements CalculationStrategy {

    @Resource
    private RawDataConverter rawDataConverter;

    // ObjectMapper是线程安全的，可以作为成员变量
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSupportedMethod() {
        // 假设 "3" 是 "区间适配" 的字典值
        return "3";
    }

    @Override
    public String execute(List<RawRule> rawRules, String rawPerformance, String unit) {
        if (CollUtil.isEmpty(rawRules) || rawPerformance == null) {
            return "-";
        }

        BigDecimal userPerformance = convertToBigDecimal(rawPerformance, unit);

        // 1. 遍历所有规则，找到第一个用户成绩落在其 scoreRanges 定义的任何一个区间内的规则
        Optional<RawRule> bestMatch = rawRules.stream()
                .filter(rule -> {
                    String scoreRangesJson = rule.getScoreRanges();
                    if (StringUtils.isBlank(scoreRangesJson)) {
                        return false;
                    }
                    try {
                        List<List<String>> ranges = parseRanges(scoreRangesJson);
                        // 检查用户成绩是否落在任何一个区间内
                        for (List<String> range : ranges) {
                            if (range.size() == 2) {
                                BigDecimal min = convertToBigDecimal(range.get(0), unit);
                                BigDecimal max = convertToBigDecimal(range.get(1), unit);
                                // 判断是否在 [min, max] 闭区间内
                                if (userPerformance.compareTo(min) >= 0 && userPerformance.compareTo(max) <= 0) {
                                    return true; // 匹配成功
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("成绩区间数据{}解析失败:{}", scoreRangesJson, e.getMessage());
                        return false;
                    }
                    return false;
                })
                .findFirst();
        // 2. 返回匹配到的规则的得分或等级名称
        // 优先返回等级名称，如果没有等级名称则返回分数
        return bestMatch.map(rule -> rule.getEvaluationCode() != null ? String.valueOf(rule.getEvaluationCode()) : rule.getScore())
                .orElse(null);
    }

    /**
     * [重构] 解析 scoreRanges 字符串，格式为 "min-max" 或 "min1-max1,min2-max2"
     */
    private List<List<String>> parseRanges(String scoreRangesStr) {
        if (StringUtils.isBlank(scoreRangesStr)) {
            return new ArrayList<>();
        }

        List<List<String>> allRanges = new ArrayList<>();
        // 1. 按逗号分割，得到多个 "min-max" 字符串
        String[] rangeParts = scoreRangesStr.split(",");
        for (String part : rangeParts) {
            String trimmedPart = part.trim();
            // 2. 按连字符分割，得到 min 和 max
            String[] minMax = trimmedPart.split("-");
            if (minMax.length == 2) {
                allRanges.add(Arrays.asList(minMax[0].trim(), minMax[1].trim()));
            }
        }
        return allRanges;
    }

    /**
     * 统一转换原始成绩为BigDecimal，不进行任何逻辑适配。
     */
    private BigDecimal convertToBigDecimal(Object rawPerformance, String unit) {
        UnitConverter converter = rawDataConverter.getConverterByUnit(unit);
        if (converter == null) {
            throw new JeecgBootException("未能找到单位 [" + unit + "] 对应的转换器，请检查配置。");
        }
        try {
            return converter.convert(rawPerformance);
        } catch (Exception e) {
            throw new JeecgBootException("原始成绩格式转换失败: " + e.getMessage(), e);
        }
    }
}