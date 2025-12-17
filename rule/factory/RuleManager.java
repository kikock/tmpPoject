package org.jeecg.modules.rule.factory;

import lombok.extern.log4j.Log4j2;
import org.jeecg.modules.rule.entity.LessonGradingRules;
import org.jeecg.modules.rule.entity.LessonScoringRules;
import org.jeecg.modules.rule.entity.PersonnelDept;
import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.entity.enums.EvaluationType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @project_name: 后端平台项目
 * @description:  规则管理器，根据类型动态选择和调用相应的工厂。
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@Service
@Log4j2
public class RuleManager {
    @Resource
    private GradeRuleFactory gradeRuleFactory;

    /**
     *
     * @param rules          个人评定规则
     * @param rawPerformance 个人成绩
     * @param evaluationType 评等评分计算方式
     * @return 最终结果字符串
     */
    public String calculate(PersonnelProfile rules, String rawPerformance, EvaluationType evaluationType) {
        if (evaluationType == null) {
            throw new IllegalArgumentException("评等计算方式不能为空。");
        }
        switch (evaluationType) {
            case TOTAL_SCORE_GRADING:
                // 个人总分评等（定性评分）
                IndividualScoringRuleFactory scoringRule = gradeRuleFactory.createIndividualScoringRule();
                return scoringRule.score(rules, rawPerformance);
            case QUALITATIVE_GRADING:
                // 个人定性评等
                IndividualEvaluationRuleFactory evaluationRule = gradeRuleFactory.createIndividualEvaluationRule();
                return evaluationRule.evaluate(rules, rawPerformance);
            case BMI_GRADING:
            case COMPOUND_CONDITION_GRADING:
                IndividualEvaluationRuleFactory compoundEvaluationRule = gradeRuleFactory.createIndividualEvaluationRule();
                return compoundEvaluationRule.evaluate(rules, rawPerformance, evaluationType);
            default:
                throw new IllegalArgumentException("未知的评等计算方式：" + evaluationType);
        }
    }

    /**
     *
     * @param rules          课目评分规则对象
     * @param rawPerformance 原始成绩/个人id
     * @param unit           计量单位
     * @return 最终结果字符串
     */
    public String calculate(LessonScoringRules rules, String rawPerformance, String unit) {
        // 调用科目评分工厂，并使用它创建的规则实例进行评分
        LessonScoringRuleFactory lessonScoringRule = gradeRuleFactory.createLessonScoringRule();
        return lessonScoringRule.score(rules, rawPerformance, unit);
    }

    /**
     *
     * @param rules          课目评等 规则对象
     * @param rawPerformance 原始成绩/个人id
     * @param unit           计量单位
     * @return 最终结果字符串
     */
    public String calculate(LessonGradingRules rules, String rawPerformance, String unit) {
        // 调用个人评定工厂，并使用它创建的规则实例进行评定
        LessonEvaluationRuleFactory lessonEvaluationRule = gradeRuleFactory.createLessonEvaluationRule();
        return lessonEvaluationRule.evaluate(rules, rawPerformance, unit);
    }

    /**
     *
     * @param rules          单位评定规则
     * @param rawPerformance 单位成绩数据
     * @return 最终结果字符串
     */
    public String evaluate(PersonnelDept rules, String rawPerformance) {
        // 调用个人评定工厂，并使用它创建的规则实例进行评定
        PersonnelDeptRuleFactory personnelDeptRuleFactory = gradeRuleFactory.createPersonnelDeptRule();
        return personnelDeptRuleFactory.evaluate(rules, rawPerformance);
    }
}