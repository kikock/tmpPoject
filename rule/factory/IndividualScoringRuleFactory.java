package org.jeecg.modules.rule.factory;


import org.jeecg.modules.rule.entity.PersonnelProfile;


/**
 * @project_name: 后端平台项目
 * @description: 个人评分规则接口 (抽象产品)
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public interface IndividualScoringRuleFactory {
    /**
     * 根据个人评分规则和传入的总分，计算最终得分或等级。
     *
     * @param personnelProfile 个人评分规则对象。
     * @param totalScoreJson   代表总分的JSON字符串（例如 "580"）。
     * @return 计算后的结果字符串。
     */
    String score(PersonnelProfile personnelProfile, String totalScoreJson);
}
