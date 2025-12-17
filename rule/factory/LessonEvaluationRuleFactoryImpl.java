package org.jeecg.modules.rule.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.entity.LessonGradingRules;
import org.jeecg.modules.rule.entity.domain.RawRule;
import org.jeecg.modules.rule.entity.enums.LevelScoreEnum;
import org.jeecg.modules.rule.strategy.CalculationStrategy;
import org.jeecg.modules.rule.strategy.CalculationStrategyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;



/**
 * @project_name: 后端平台项目
 * @description: 科目评等规则实现
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@Service
@Log4j2
public class LessonEvaluationRuleFactoryImpl implements LessonEvaluationRuleFactory {
    @Resource
    private CalculationStrategyFactory strategyFactory;

    @Override
    public String evaluate(LessonGradingRules gradingRules, String rawPerformance, String unit) {
        // 1. 基本参数校验，确保关键数据不为空
        if (ObjUtil.isNull(gradingRules) || StrUtil.isBlank(gradingRules.getCalculationMethod()) || ObjUtil.isNull(gradingRules.getRulesJson())) {
            throw new JeecgBootException("计算方式或评定规则 JSON 不能为空。");
        }
        if (StrUtil.isBlank(gradingRules.getUnit()) && StrUtil.isBlank(unit)) {
            throw new JeecgBootException("计量单位不能为空。");
        }
        if (StrUtil.isBlank(rawPerformance)) {
            throw new JeecgBootException("成绩不能为空。");
        }
        unit = StrUtil.isBlank(unit) ? gradingRules.getUnit() : unit;
        gradingRules.setUnit(unit);
        List<RawRule> rawRules = gradingRules.getRulesJson();
        if (CollUtil.isEmpty(rawRules)) {
            return "-";
        }
        // 1. 从工厂获取策略
        CalculationStrategy strategy = strategyFactory.getStrategy(gradingRules.getCalculationMethod());
        // 2. 执行策略
        String result = strategy.execute(rawRules, rawPerformance, unit);
        if (StrUtil.isBlank(result)) {
            return LevelScoreEnum.FAIL.getCode();
        }
        return result;
    }
}
