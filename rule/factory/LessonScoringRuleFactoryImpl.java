package org.jeecg.modules.rule.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.entity.LessonScoringRules;
import org.jeecg.modules.rule.entity.domain.RawRule;
import org.jeecg.modules.rule.strategy.CalculationStrategy;
import org.jeecg.modules.rule.strategy.CalculationStrategyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;



/**
 * @project_name: 后端平台项目
 * @description: 科目评分规则实现
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@Service
@Log4j2
public class LessonScoringRuleFactoryImpl implements LessonScoringRuleFactory {

    @Resource
    private CalculationStrategyFactory strategyFactory;

    @Override
    public String score(LessonScoringRules lessonScoringRules, String rawPerformance, String unit) {
        // 1. 基本参数校验，确保关键数据不为空
        if (ObjUtil.isNull(lessonScoringRules) || StrUtil.isBlank(lessonScoringRules.getCalculationMethod()) || ObjUtil.isNull(lessonScoringRules.getRulesJson())) {
            throw new JeecgBootException("计算方式或评分规则 JSON 不能为空。");
        }
        if (StrUtil.isBlank(lessonScoringRules.getUnit()) && StrUtil.isBlank(unit)) {
            throw new JeecgBootException("计量单位不能为空。");
        }
        if (StrUtil.isBlank(rawPerformance)) {
            throw new JeecgBootException("成绩不能为空。");
        }
        unit = StrUtil.isBlank(unit) ? lessonScoringRules.getUnit() : unit;
        lessonScoringRules.setUnit(unit);
        List<RawRule> rulesJson = lessonScoringRules.getRulesJson();
        if (CollUtil.isEmpty(rulesJson)) {
            // 如果没有规则，返回最低分或0分
            return "-";
        }
        // 1. 从工厂获取策略
        CalculationStrategy strategy = strategyFactory.getStrategy(lessonScoringRules.getCalculationMethod());
        // 2. 执行策略
        // 3. 如果策略没有返回任何结果（即不满足任何得分标准），则返回默认的最低分
        String result = strategy.execute(rulesJson, rawPerformance, unit);
        if (StrUtil.isBlank(result)) {
            return "0";
        }
        return result;
    }
}
