package org.jeecg.modules.rule.factory;


import org.jeecg.modules.rule.entity.LessonGradingRules;

/**
 * @project_name: 后端平台项目
 * @description: 科目评等规则接口 (抽象产品)
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public interface LessonEvaluationRuleFactory {
    String evaluate(LessonGradingRules lessonGradingRules, String rawPerformance, String unit);
}
