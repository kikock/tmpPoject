package org.jeecg.modules.rule.strategy;


import org.jeecg.modules.rule.entity.domain.RawRule;

import java.util.List;

/**
 * @description: 规则计算策略接口
 * @author: kikock
 * @create_date: 2025-10-12
 **/
public interface CalculationStrategy {

    /**
     * 执行计算的核心方法。
     *
     * @param rawRules       详细的规则条目列表 (例如，包含成绩和分数的对应关系)
     * @param rawPerformance 用户的原始成绩
     * @param unit           计量单位
     * @return 计算出的结果 (分数或等级)
     */
    String execute(List<RawRule> rawRules, String rawPerformance, String unit);

    /**
     * 获取此策略支持的计算方式的字典值。
     *
     * @return 计算方式的字典值 (例如: "1", "2")
     */
    String getSupportedMethod();
}