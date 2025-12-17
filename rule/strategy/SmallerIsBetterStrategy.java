package org.jeecg.modules.rule.strategy;

import cn.hutool.core.collection.CollUtil;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.annotation.RawDataConverter;
import org.jeecg.modules.rule.annotation.UnitConverter;
import org.jeecg.modules.rule.entity.domain.RawRule;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @description: "越小越好" 计算策略
 * @author: kikock
 * @create_date: 2025-10-12
 **/
@Component
public class SmallerIsBetterStrategy implements CalculationStrategy {
    @Resource
    private RawDataConverter rawDataConverter;

    @Override
    public String getSupportedMethod() {
        // 返回此策略支持的计算方式字典值
        return "2";
    }

    @Override
    public String execute(List<RawRule> rawRules, String rawPerformance, String unit) {
        if (CollUtil.isEmpty(rawRules) || rawPerformance == null) {
            return "-";
        }
        BigDecimal userPerformance = convertToBigDecimal(rawPerformance, unit);

        // 1. 筛选出用户成绩达标的所有规则 (userPerformance <= rule.getPerformance())
        // 2. 在满足条件的规则中，找到最优的规则（分数/评等最高）
        // 修正逻辑：在“越小越好”的场景中，最优规则是要求最严格的规则，即 performance 值最小的规则。
        Optional<RawRule> bestMatch = rawRules.stream()
                .filter(rule -> userPerformance.compareTo(convertToBigDecimal(rule.getPerformance(), unit)) <= 0)
                .min(Comparator.comparing(rule -> convertToBigDecimal(rule.getPerformance(), unit)
                ));

        // 3. 返回匹配到的规则的得分或等级名称
        return bestMatch.map(rule -> {
            // 优先返回等级名称，如果没有等级名称则返回分数
            return rule.getEvaluationCode() != null ? String.valueOf(rule.getEvaluationCode()) : rule.getScore();
        }).orElse(null);
    }

    /**
     * 统一转换原始成绩为BigDecimal，不进行任何逻辑适配。
     *
     * @param rawPerformance 原始成绩字符串
     * @param unit           单位代码
     * @return 转换后的BigDecimal
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