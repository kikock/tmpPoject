package org.jeecg.modules.rule.factory;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;
import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.entity.domain.RuleLogicBlock;
import org.jeecg.modules.rule.entity.domain.ScoreAssessmentInfo;
import org.jeecg.modules.rule.entity.enums.EvaluationType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 个人定性评分规则的默认实现
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@Service
@Log4j2
public class IndividualScoringRuleFactoryImpl implements IndividualScoringRuleFactory {

    /**
     * 根据个人定性评分规则和传入的总分，计算最终评定等级。
     * <p>
     * 核心逻辑：
     * 1. 解析传入的JSON字符串以获取总分。
     * 2. 从规则对象中提取评分标准列表 (List<ScoreAssessmentInfo>)。
     * 3. 将评分标准按最低分数要求（minScore）降序排序。
     * 4. 遍历标准，找到第一个总分大于或等于其最低分数要求的等级。
     *
     * @param personnelProfile 个人评分规则对象，包含具体的评分逻辑。
     * @param totalScoreJson   代表总分的JSON字符串（例如 "580.5"）。
     * @return 计算后的评定等级字符串（如 "优秀", "良好"），如果无匹配则返回 "未评级"。
     */
    @Override
    public String score(PersonnelProfile personnelProfile, String totalScoreJson) {
        // 1. 输入校验
        if (personnelProfile == null || CollectionUtils.isEmpty(personnelProfile.getRuleLogic())) {
            throw new IllegalArgumentException("个人评分规则或其逻辑块不能为空。");
        }
        if (!StringUtils.hasText(totalScoreJson)) {
            throw new IllegalArgumentException("总分数据不能为空。");
        }
        // 2. 解析总分
        BigDecimal totalScore;
        try {
            totalScore = new BigDecimal(totalScoreJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("总分数据格式不正确，无法解析为数字。", e);
        }
        // 3. 规则块
        for (RuleLogicBlock ruleBlock : personnelProfile.getRuleLogic()) {
            // 确保只处理启用的、类型为 "total_score" 的规则块
            if (ruleBlock.isEnabled() && EvaluationType.TOTAL_SCORE_GRADING.getCode().equals(ruleBlock.getType())) {
                JSONArray assessmentInfoJson = ruleBlock.getAssessment_info();
                if (assessmentInfoJson == null || assessmentInfoJson.isEmpty()) {
                    continue; // 如果规则块内容为空，则跳过
                }
                List<ScoreAssessmentInfo> assessments = JSONUtil.toList(assessmentInfoJson.toString(), ScoreAssessmentInfo.class);
                if (CollectionUtils.isEmpty(assessments)) {
                    continue; // 如果解析后列表为空，则跳过
                }
                // [新增] “一票否决”逻辑：先判断总分是否达到及格线
                ScoreAssessmentInfo passRule = assessments.stream()
                        .filter(a -> "pass".equalsIgnoreCase(a.getGrade()))
                        .findFirst()
                        .orElse(null);

                if (passRule != null && passRule.getMinScore() != null) {
                    if (totalScore.compareTo(passRule.getMinScore()) < 0) {
                        // 如果总分低于及格线，直接返回“不及格”
                        return "fail";
                    }
                }
                // 按 minScore 降序排序，确保从最高等级开始匹配
                assessments.sort(Comparator.comparing(ScoreAssessmentInfo::getMinScore).reversed());
                // 4. 匹配计算
                for (ScoreAssessmentInfo assessment : assessments) {
                    if (assessment.getMinScore() != null && totalScore.compareTo(assessment.getMinScore()) >= 0) {
                        // 找到并返回第一个匹配到的等级
                        return assessment.getGrade();
                    }
                }
            }
        }
        log.info("在规则 '" + personnelProfile.getName() + "' 中未找到有效的定性评分规则或无匹配等级,评级为'-'。");
        return "-";
    }
}