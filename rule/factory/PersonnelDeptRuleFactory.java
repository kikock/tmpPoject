package org.jeecg.modules.rule.factory;


import org.jeecg.modules.rule.entity.PersonnelDept;


/**
 * @project_name: 后端平台项目
 * @description: 单位总评规则接口 (抽象产品)
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public interface PersonnelDeptRuleFactory {
    /**
     * 根据单位总评规则和传入的原始成绩，计算最终评定等级。
     *
     * @param personnelDept  单位总评规则对象。
     * @param rawPerformance 原始成绩数据，通常为JSON字符串。
     * @return 计算后的评定等级字符串。
     */
    String evaluate(PersonnelDept personnelDept, String rawPerformance);
}