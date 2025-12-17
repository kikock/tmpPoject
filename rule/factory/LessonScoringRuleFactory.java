package org.jeecg.modules.rule.factory;


import org.jeecg.modules.rule.entity.LessonScoringRules;

/**
 * @project_name: 后端平台项目
 * @description: 科目评分规则接口 (抽象产品)
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public interface LessonScoringRuleFactory {
    /**
     * 根据传入的规则数据和原始成绩，计算最终分数。
     * 该方法是所有评分计算的统一入口。
     *
     * @param lessonScoringRules 评分规则对象
     * @param rawPerformance     成绩(未格式化)
     * @param unit               单位
     * @return 最终计算出的分数字符串
     */
    String score(LessonScoringRules lessonScoringRules, String rawPerformance, String unit);
}
