package org.jeecg.modules.rule.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;
import org.jeecg.modules.rule.entity.PersonnelDept;
import org.jeecg.modules.rule.entity.domain.CompoundConditionInfo;
import org.jeecg.modules.rule.entity.domain.ConditionRule;
import org.jeecg.modules.rule.entity.domain.RuleLogicBlock;
import org.jeecg.modules.rule.utils.GradeLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @project_name: 后端平台项目
 * @description: 单位总评规则实现类 (具体产品)
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@Service
@Log4j2
public class PersonnelDeptRuleFactoryImpl implements PersonnelDeptRuleFactory {

    /**
     * 定义等级的层级关系和权重。
     * Key: 等级中文名或英文编码。
     * Value: 等级对应的分值，用于内部比较。
     */
    private static final Map<String, Integer> GRADE_HIERARCHY = new HashMap<>();

    static {
        GRADE_HIERARCHY.put("特一级", 6);
        GRADE_HIERARCHY.put("special_one", 6);
        GRADE_HIERARCHY.put("特二级", 5);
        GRADE_HIERARCHY.put("special_two", 5);
        GRADE_HIERARCHY.put("特三级", 4);
        GRADE_HIERARCHY.put("special_three", 4);
        GRADE_HIERARCHY.put("优秀", 3);
        GRADE_HIERARCHY.put("excellent", 3);
        GRADE_HIERARCHY.put("良好", 2);
        GRADE_HIERARCHY.put("good", 2);
        GRADE_HIERARCHY.put("及格", 1);
        GRADE_HIERARCHY.put("pass", 1);
        GRADE_HIERARCHY.put("不及格", 0);
        GRADE_HIERARCHY.put("fail", 0);
    }

    /**
     * 根据单位总评规则和传入的原始成绩，计算最终评定等级。
     *
     * @param personnelDept  单位总评规则对象。
     * @param rawPerformance 原始成绩数据，通常为JSON字符串。
     * @return 计算后的评定等级字符串。
     */
    @Override
    public String evaluate(PersonnelDept personnelDept, String rawPerformance) {
        // 1. 解析单位的各项成绩指标
        List<GradeLevel> deptGrades = JSONUtil.toList(rawPerformance, GradeLevel.class);
        if (CollUtil.isEmpty(deptGrades)) {
            return "未评级";
        }
        Map<String, String> scoresMap = deptGrades.stream()
                .collect(Collectors.toMap(GradeLevel::getName, GradeLevel::getValue, (v1, v2) -> v1));
        String finalGrade = "未评级";
        for (RuleLogicBlock ruleBlock : personnelDept.getRuleLogic()) {
            // 假设单位总评也使用 "compound_condition" 类型
            if (ruleBlock.isEnabled() && "compound_condition".equals(ruleBlock.getType())) {
                List<CompoundConditionInfo> assessments = JSONUtil.toList(ruleBlock.getAssessment_info().toString(), CompoundConditionInfo.class);
                if (CollUtil.isEmpty(assessments)) {
                    continue; // 如果此规则块没有内容，则跳过
                }
                assessments.sort(Comparator.comparingInt(CompoundConditionInfo::getSort));
                // 3. 从低到高遍历规则，进行匹配
                for (CompoundConditionInfo rule : assessments) {
                    if (checkIfDeptMatches(scoresMap, rule.getConditions())) {
                        finalGrade = rule.getGrade();
                    }
                }
                // 找到并处理完第一个启用的复合条件规则块后即可退出
                break;
            }
        }
        return finalGrade;
    }

    /**
     * 检查单位成绩是否满足一组特定的条件。
     *
     * @param scoresMap  单位的各项成绩指标
     * @param conditions 一个等级所要求的所有条件
     * @return 如果所有条件都满足，则返回 true
     */
    private boolean checkIfDeptMatches(Map<String, String> scoresMap, List<ConditionRule> conditions) {
        if (CollUtil.isEmpty(conditions)) {
            return true;
        }
        for (ConditionRule condition : conditions) {
            String subject = condition.getSubject();
            String requiredValue = condition.getValue();
            String actualValue = scoresMap.get(subject);
            if (actualValue == null) {
                return false;
            }
            // 如果条件中定义了 evaluationGrade，说明是等级比较
            if (StrUtil.isNotEmpty(condition.getEvaluationGrade())) {
                Integer requiredGradeScore = GRADE_HIERARCHY.get(requiredValue.toLowerCase());
                Integer actualGradeScore = GRADE_HIERARCHY.get(actualValue.toLowerCase());
                if (requiredGradeScore == null || actualGradeScore == null || actualGradeScore < requiredGradeScore) {
                    return false;
                }
            } else {
                try {
                    BigDecimal required = new BigDecimal(requiredValue);
                    BigDecimal actual = new BigDecimal(actualValue);
                    if (actual.compareTo(required) < 0) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }
}