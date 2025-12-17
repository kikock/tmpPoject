package org.jeecg.modules.rule.factory;



/**
 * @project_name: 后端平台项目
 * @description: 评等规则工厂接口 定义了创建评等规则实例的方法
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
interface GradeRuleFactory {
    //    创建科目评等规则实例
    LessonEvaluationRuleFactory createLessonEvaluationRule();

    //    创建科目评分规则实例
    LessonScoringRuleFactory createLessonScoringRule();

    //    创建个人评等规则实例
    IndividualEvaluationRuleFactory createIndividualEvaluationRule();

    //    创建个人评分规则实例
    IndividualScoringRuleFactory createIndividualScoringRule();

    //创建单位评定规则实例
    PersonnelDeptRuleFactory createPersonnelDeptRule();
}