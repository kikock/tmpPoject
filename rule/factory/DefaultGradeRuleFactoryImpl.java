package org.jeecg.modules.rule.factory;


import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @project_name: 后端平台项目
 * @description: 评等规则工厂实现 实现 GradeRuleFactory 接口中所有方法的创建逻辑
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@Component
public class DefaultGradeRuleFactoryImpl implements GradeRuleFactory {

    @Resource
    private LessonScoringRuleFactory lessonScoringRuleFactory;
    @Resource
    private LessonEvaluationRuleFactory lessonEvaluationRuleFactory;
    @Resource
    private IndividualScoringRuleFactory individualScoringRuleFactory;
    @Resource
    private IndividualEvaluationRuleFactory individualEvaluationRuleFactory;
    @Resource
    private PersonnelDeptRuleFactory personnelDeptRuleFactory;


    @Override
    public LessonEvaluationRuleFactory createLessonEvaluationRule() {
        return lessonEvaluationRuleFactory;
    }

    @Override
    public LessonScoringRuleFactory createLessonScoringRule() {
        return lessonScoringRuleFactory;
    }

    @Override
    public IndividualScoringRuleFactory createIndividualScoringRule() {
        return individualScoringRuleFactory;
    }

    @Override
    public IndividualEvaluationRuleFactory createIndividualEvaluationRule() {
        return individualEvaluationRuleFactory;
    }

    @Override
    public PersonnelDeptRuleFactory createPersonnelDeptRule() {
        return personnelDeptRuleFactory;
    }
}
