package org.jeecg.modules.rule.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.entity.domain.CompoundConditionInfo;
import org.jeecg.modules.rule.entity.domain.ConditionRule;
import org.jeecg.modules.rule.entity.domain.GradeAssessmentInfo;
import org.jeecg.modules.rule.entity.domain.RuleLogicBlock;
import org.jeecg.modules.rule.entity.enums.EvaluationType;
import org.jeecg.modules.rule.utils.GradeLevel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @project_name: 后端平台项目
 * @description: 个人评等规则接口实现 (抽象产品)
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@Slf4j
@Service
public class IndividualEvaluationRuleFactoryImpl implements IndividualEvaluationRuleFactory {

    @Resource
    private CommonAPI commonAPI;
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

    @Override
    public String evaluate(PersonnelProfile personnelProfile, String subjectGradesJson) {
        // 为保持向后兼容性，默认调用定性评等
        return evaluate(personnelProfile, subjectGradesJson, EvaluationType.QUALITATIVE_GRADING);
    }

    @Override
    public String evaluate(PersonnelProfile personnelProfile, String subjectGradesJson, EvaluationType evaluationType) {
        validateInput(personnelProfile, subjectGradesJson);

        switch (evaluationType) {
            case QUALITATIVE_GRADING:
                return evaluateQualitative(personnelProfile, subjectGradesJson);
            case COMPOUND_CONDITION_GRADING:
                return evaluateCompoundCondition(personnelProfile, subjectGradesJson);
            case BMI_GRADING:
                return evaluateBmi(personnelProfile, subjectGradesJson);
            default:
                log.warn("在个人评等工厂中，未知的评等计算方式：{}", evaluationType);
                return "-";
        }
    }

    /**
     * [新] 检查成绩是否满足单条“复合条件评等”规则
     */
    private boolean checkSingleCompoundRule(CompoundConditionInfo rule, Map<String, String> scoresMap) {
        if (CollUtil.isEmpty(rule.getConditions())) {
            return true;
        }
        log.info("正在检查复合条件 '{}' 的规则。", rule.getGrade());
        for (ConditionRule condition : rule.getConditions()) {
            String subjectName = condition.getSubject();
            String requiredValue = condition.getValue();
            String actualValueStr = scoresMap.get(subjectName);
            if (actualValueStr == null) {
                log.info("复合条件 '{}' 的规则没有成绩。", rule.getGrade());
                return false;
            }
            // 判断是等级比较还是分数比较
            if (StringUtils.hasText(condition.getEvaluationGrade())) {
                // 等级比较：要求实际等级必须大于或等于规则要求的等级
                Integer requiredGradeValue = GRADE_HIERARCHY.get(requiredValue);
                Integer actualGradeValue = GRADE_HIERARCHY.get(actualValueStr);
                if (requiredGradeValue == null || actualGradeValue == null || actualGradeValue < requiredGradeValue) {
                    log.info("复合条件 '{}' 的规则等级评定未通过检查。", rule.getGrade());
                    return false;
                }
            } else {
                try {
                    BigDecimal requiredScore = new BigDecimal(requiredValue);
                    BigDecimal actualScore = new BigDecimal(actualValueStr);
                    if (actualScore.compareTo(requiredScore) < 0) {
                        log.info("复合条件 '{}' 的规则分数评定未通过检查。", rule.getGrade());
                        return false;
                    }
                } catch (NumberFormatException e) {
                    log.error("在比较科目 '{}' 的分数时发生错误：无法将 '{}' 或 '{}' 解析为数字。", subjectName, actualValueStr, requiredValue);
                    return false;
                }
            }
        }
        log.warn("复合条件 '{}' 的规则已通过检查。", rule.getGrade());
        return true;
    }


    /**
     * 使用加权评分机制，检查实际成绩是否满足评等规则。
     *
     * @param rule          单条评等规则，定义了各项成绩的数量要求。
     * @param gradeCounts   实际统计出的各等级成绩数量 (e.g., {"excellent": 2, "pass": 1})。
     * @param subjectGrades 原始成绩列表，用于获取总数。
     * @return 如果满足所有条件，返回 true，否则 false。
     */
    private boolean checkQualitativeMatchesTwo(GradeAssessmentInfo rule, Map<String, Long> gradeCounts, List<GradeLevel> subjectGrades) {
        // 1. 校验总科目数 (保留原有逻辑)
        if (rule.getTotalSubjects() != null && subjectGrades.size() != rule.getTotalSubjects()) {
            return false;
        }

        Map<String, Integer> requiredCounts = rule.getRequiredCounts();
        if (CollUtil.isEmpty(requiredCounts)) {
            return false;
        }

        // --- 方案B：加权评分逻辑 ---
        // 2. 确定规则的“底线”：找出规则中要求的最低等级。
        int minAllowedValue = Integer.MAX_VALUE;
        for (String requiredGrade : requiredCounts.keySet()) {
            minAllowedValue = Math.min(minAllowedValue, GRADE_HIERARCHY.get(requiredGrade));
        }

        // 3. 检查实际成绩中是否存在低于“底线”的等级。
        long actualScore = 0L;
        for (Map.Entry<String, Long> actualEntry : gradeCounts.entrySet()) {
            String actualGrade = actualEntry.getKey();
            long count = actualEntry.getValue();
            Integer actualValue = GRADE_HIERARCHY.get(actualGrade);

            if (actualValue == null) {
                log.warn("成绩中存在未知的等级: {}", actualGrade);
                continue;
            }

            // 如果实际成绩的等级低于规则要求的最低等级，则直接不匹配。
            if (actualValue < minAllowedValue) {
                return false;
            }
            // 累加实际分数
            actualScore += actualValue * count;
        }

        // 4. 计算规则要求的最低分数线。
        long requiredScore = 0L;
        for (Map.Entry<String, Integer> requiredEntry : requiredCounts.entrySet()) {
            String requiredGrade = requiredEntry.getKey();
            int count = requiredEntry.getValue();
            Integer requiredValue = GRADE_HIERARCHY.get(requiredGrade);

            if (requiredValue == null) {
                log.error("规则 '{}' 中定义的门槛 '{}' 是一个未知的等级。", rule.getGrade(), requiredGrade);
                return false;
            }
            requiredScore += (long) requiredValue * count;
        }

        // 5. 最终比较：实际分数必须大于或等于要求的分数。
        return actualScore >= requiredScore;
    }

    /**
     *
     * @param rule          单条评等规则。
     * @param gradeCounts   实际统计出的各等级成绩数量 (e.g., {"excellent": 2, "pass": 3})。
     * @param subjectGrades 原始成绩列表，用于获取总数。
     * @return 如果满足所有门槛条件，返回 true，否则 false。
     */
    private boolean checkQualitativeMatches(GradeAssessmentInfo rule, Map<String, Long> gradeCounts, List<GradeLevel> subjectGrades) {
        // 1. 校验总科目数
        if (rule.getTotalSubjects() != null && subjectGrades.size() != rule.getTotalSubjects()) {
            return false;
        }

        Map<String, Integer> requiredCounts = rule.getRequiredCounts();
        if (CollUtil.isEmpty(requiredCounts)) {
            // 如果规则没有定义任何门槛，则认为不适用或匹配失败
            return false;
        }
        // 2. 遍历规则中定义的每一个“门槛-数量”要求
        for (Map.Entry<String, Integer> requiredEntry : requiredCounts.entrySet()) {
            String thresholdGrade = requiredEntry.getKey();
            int minRequiredCount = requiredEntry.getValue();
            Integer thresholdValue = GRADE_HIERARCHY.get(thresholdGrade);
            if (thresholdValue == null) {
                log.error("规则 '{}' 中定义的门槛 '{}' 是一个未知的等级。", rule.getGrade(), thresholdGrade);
                return false;
            }
            // 3. 计算实际成绩中，大于等于当前门槛的科目总数
            long cumulativeCount = 0;
            for (Map.Entry<String, Long> actualEntry : gradeCounts.entrySet()) {
                String actualGrade = actualEntry.getKey();
                Integer actualValue = GRADE_HIERARCHY.get(actualGrade);
                if (actualValue != null && actualValue >= thresholdValue) {
                    cumulativeCount += actualEntry.getValue();
                }
            }
            if (cumulativeCount < minRequiredCount) {
                return false;
            }
        }
        return true;
    }

    /**
     * [私有] 处理“定性评等”（累计数量模型）
     */
    private String evaluateQualitative(PersonnelProfile personnelProfile, String subjectGradesJson) {
        List<GradeLevel> subjectGrades = parseGrades(subjectGradesJson);
        if (CollUtil.isEmpty(subjectGrades)) {
            return "-";
        }

        // 增加“一票否决”逻辑：只要有一门不及格，直接返回“不及格”
        boolean hasFail = subjectGrades.stream().anyMatch(grade -> "fail".equalsIgnoreCase(grade.getName()));
        if (hasFail) {
            return "fail";
        }

        Map<String, Long> gradeCounts = subjectGrades.stream()
                .map(GradeLevel::getName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        String finalGrade = "fail";
        for (RuleLogicBlock ruleBlock : personnelProfile.getRuleLogic()) {
            if (ruleBlock.isEnabled() && EvaluationType.QUALITATIVE_GRADING.getCode().equals(ruleBlock.getType())) {
                JSONArray assessmentInfo = ruleBlock.getAssessment_info();
                if (assessmentInfo == null || assessmentInfo.isEmpty()) {
                    continue;
                }
                List<GradeAssessmentInfo> assessments = JSONUtil.toList(assessmentInfo.toString(), GradeAssessmentInfo.class);
                if (CollUtil.isEmpty(assessments)) {
                    continue;
                }
//                根据等级排序前先设置等级顺序
                assessments.forEach(assessment -> {
                    if (assessment.getSort() == null) {
                        assessment.setSort(GRADE_HIERARCHY.get(assessment.getGrade()));
                    }
                });
                assessments.sort(Comparator.comparing(GradeAssessmentInfo::getSort));
                for (GradeAssessmentInfo rule : assessments) {
                    if (checkQualitativeMatchesTwo(rule, gradeCounts, subjectGrades)) {
                        log.debug("规则匹配到等级：{}", rule.getGrade());
                        finalGrade = rule.getGrade();
                    }
                }
                break;
            }
        }
        return finalGrade;
    }

    /**
     * [私有] 处理“复合条件评等”
     */
    private String evaluateCompoundCondition(PersonnelProfile personnelProfile, String subjectGradesJson) {
        List<GradeLevel> subjectGrades = parseGrades(subjectGradesJson);
        Map<String, String> scoresMap = subjectGrades.stream()
                .collect(Collectors.toMap(GradeLevel::getName, GradeLevel::getValue, (v1, v2) -> v1));

        // [新增] “一票否决”逻辑
        // 1. 找到"pass"规则，以获取所有科目的及格分数线
        Map<String, String> passThresholds = new HashMap<>();
        personnelProfile.getRuleLogic().stream()
                .filter(rb -> rb.isEnabled() && EvaluationType.COMPOUND_CONDITION_GRADING.getCode().equals(rb.getType()))
                .findFirst()
                .ifPresent(ruleBlock -> {
                    List<CompoundConditionInfo> assessments = JSONUtil.toList(ruleBlock.getAssessment_info().toString(), CompoundConditionInfo.class);
                    assessments.stream()
                            .filter(rule -> "pass".equalsIgnoreCase(rule.getGrade()))
                            .findFirst()
                            .ifPresent(passRule -> {
                                if (CollUtil.isNotEmpty(passRule.getConditions())) {
                                    passRule.getConditions().forEach(condition ->
                                            passThresholds.put(condition.getSubject(), condition.getValue()));
                                }
                            });
                });

        // 2. 遍历所有成绩，执行否决判断
        for (GradeLevel grade : subjectGrades) {
            // 2.1 等级否决：如果任何一科的评定等级是"fail"
            if ("fail".equalsIgnoreCase(grade.getValue())) {
                return "fail";
            }
            // 2.2 分数否决：如果任何一科的分数低于其"pass"规则中定义的分数线
            String passThreshold = passThresholds.get(grade.getName());
            if (passThreshold != null) {
                try {
                    // 只有当及格线和实际成绩都是数字时才进行比较
                    BigDecimal passScore = new BigDecimal(passThreshold);
                    BigDecimal actualScore = new BigDecimal(grade.getValue());
                    if (actualScore.compareTo(passScore) < 0) {
                        return "fail";
                    }
                } catch (NumberFormatException e) {
                    // 如果及格线或成绩不是数字（例如是"pass"这样的等级），则忽略分数比较，由后续的等级比较处理
                }
            }
        }
        // 如果通过了一票否决，则继续执行正常的从低到高评级逻辑
        for (RuleLogicBlock ruleBlock : personnelProfile.getRuleLogic()) {
            if (ruleBlock.isEnabled() && EvaluationType.COMPOUND_CONDITION_GRADING.getCode().equals(ruleBlock.getType())) {
                JSONArray assessmentInfo = ruleBlock.getAssessment_info();
                if (assessmentInfo == null || assessmentInfo.isEmpty()) {
                    continue;
                }
                List<CompoundConditionInfo> assessments = JSONUtil.toList(assessmentInfo.toString(), CompoundConditionInfo.class);
                if (CollUtil.isEmpty(assessments)) {
                    continue;
                }
                // [修正] 改为降序排序，从最高等级开始匹配
                assessments.forEach(assessment -> {
                    if (assessment.getSort() == null) {
                        assessment.setSort(GRADE_HIERARCHY.get(assessment.getGrade()));
                    }
                });
                assessments.sort(Comparator.comparing(CompoundConditionInfo::getSort).reversed());
                for (CompoundConditionInfo rule : assessments) {
                    if (checkSingleCompoundRule(rule, scoresMap)) {
                        log.info("复合条件评定匹配的规则为：{}", rule);
                        return rule.getGrade();
                    }
                }
            }
        }
        //都不匹配，返回fail不及格
        return "fail";
    }

    /**
     * [私有] 转换等级编码为显示名称
     *
     * @param gradeCode 计算得出的等级编码 (e.g., "good")
     * @param gradeMap  从字典查询出的映射 (e.g., {"good": "良好"})
     * @return 如果找到，返回显示名称 (e.g., "良好")；否则返回原始编码。
     */
    private String translateGrade(String gradeCode, Map<String, String> gradeMap) {
        if (CollUtil.isEmpty(gradeMap) || !StringUtils.hasText(gradeCode)) {
            return gradeCode;
        }
        return gradeMap.getOrDefault(gradeCode, gradeCode);
    }

    /**
     * [私有] 处理“BMI评等”
     */
    private String evaluateBmi(PersonnelProfile personnelProfile, String subjectGradesJson) {
        List<GradeLevel> subjectGrades = parseGrades(subjectGradesJson);
        Map<String, String> scoresMap = subjectGrades.stream()
                .collect(Collectors.toMap(GradeLevel::getName, GradeLevel::getValue, (v1, v2) -> v1));

        // 假设 BMI 的科目代码固定为 "C001", PBF 的科目代码固定为 "C002"
        final String BMI_CODE = "BMI";
        final String PBF_CODE = "PBF";
        String bmiResult = scoresMap.get(BMI_CODE);
        String pbfResult = scoresMap.get(PBF_CODE);
        // 1. 检查BMI
        if ("pass".equalsIgnoreCase(bmiResult)) {
            return "pass";
        }
        // 2. BMI不合格，再检查PBF
        if ("pass".equalsIgnoreCase(pbfResult)) {
            return "pass";
        }
        // 3. 两者都不合格
        return "fail";
    }

    /**
     * [私有] 解析成绩JSON字符串
     */
    private List<GradeLevel> parseGrades(String subjectGradesJson) {
        try {
            return JSONUtil.toList(subjectGradesJson, GradeLevel.class);
        } catch (Exception e) {
            throw new JeecgBootException("单项科目成绩JSON格式不正确。", e);
        }
    }


    /**
     * 校验输入参数
     *
     * @param personnelProfile  个人档案
     * @param subjectGradesJson 单项科目成绩JSON字符串
     */
    private void validateInput(PersonnelProfile personnelProfile, String subjectGradesJson) {
        if (personnelProfile == null || CollUtil.isEmpty(personnelProfile.getRuleLogic())) {
            throw new JeecgBootException("个人评等规则不能为空。");
        }
        if (!StringUtils.hasText(subjectGradesJson)) {
            throw new JeecgBootException("单项科目成绩列表不能为空。");
        }
    }
}