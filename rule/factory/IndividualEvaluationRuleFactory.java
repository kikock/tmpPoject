package org.jeecg.modules.rule.factory;


import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.entity.enums.EvaluationType;


/**
 * @project_name: 后端平台项目
 * @description: 个人评等规则接口 (抽象产品)
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public interface IndividualEvaluationRuleFactory {
    /**
     * 根据个人评等规则和各单项成绩等级，计算最终总评等级。
     *
     * @param personnelProfile  个人评等规则对象。
     * @param subjectGradesJson 包含 List<GradeLevel> 的JSON数组字符串。
     * @return 计算后的总评等级字符串。
     */
    String evaluate(PersonnelProfile personnelProfile, String subjectGradesJson);

    /**
     * 根据个人评等规则和各单项成绩等级，计算最终总评等级。
     *
     * @param personnelProfile  个人评等规则对象。
     * @param subjectGradesJson 包含 List<GradeLevel> 的JSON数组字符串。
     * @return 计算后的总评等级字符串。
     */
    String evaluate(PersonnelProfile personnelProfile, String subjectGradesJson, EvaluationType evaluationType);
}
