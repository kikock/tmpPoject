package org.jeecg.modules.rule.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.entity.domain.LessonScoreVo;
import org.jeecg.modules.rule.entity.domain.PlanUserScoreVo;
import org.jeecg.modules.rule.entity.enums.EvaluationType;
import org.jeecg.modules.rule.entity.enums.LevelScoreEnum;
import org.jeecg.modules.rule.entity.param.UserScoreParamVo;
import org.jeecg.modules.rule.factory.RuleManager;
import org.jeecg.modules.rule.mapper.PersonnelProfileMapper;
import org.jeecg.modules.rule.service.IPersonnelProfileService;
import org.jeecg.modules.rule.utils.GradeLevel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @project_name: 后端平台项目
 * @description: 个人成绩规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Service
@Log4j2
public class PersonnelProfileServiceImpl extends ServiceImpl<PersonnelProfileMapper, PersonnelProfile> implements IPersonnelProfileService {

    @Resource
    private RuleManager ruleManager;
    @Resource
    private CommonAPI commonAPI;

    @Override
    public PlanUserScoreVo getUserScore(UserScoreParamVo param) {
        // =================================================================
        // 步骤 1: 前置校验与准备
        // =================================================================
        String soldierCategory = param.getSoldierCategory();
        PlanUserScoreVo resultVo = new PlanUserScoreVo();
        resultVo.setPlan(param.getPlanId());
        resultVo.setUserId(param.getUserId());
        // 1.1 校验人员类别，它是查找规则的依据
        if (soldierCategory == null) {
            throw new JeecgBootException("人员类别不能为空！");
        }
        String ruleName = "新兵规则";
        // 1.2 准备规则名称，用于后续可能出现的错误提示
        try {
            String category = commonAPI.translateDict("soldier_category", soldierCategory);
            ruleName = StringUtils.hasText(category) ? category + "规则" : "新兵规则";
        } catch (Exception e) {
            if (soldierCategory.equals("5")) {
                ruleName = "新兵规则";
            } else {
                throw new JeecgBootException("人员类型不存在！");
            }
        }
        // 1.3 校验成绩列表，确保有成绩可供计算
        List<LessonScoreVo> lessonScoreVos = param.getLessonScoreVos();
        if (CollUtil.isEmpty(lessonScoreVos)) {
            throw new JeecgBootException("人员成绩不能为空！");
        }
        // 1.4 加载该人员类别对应的个人总评规则
        PersonnelProfile bodyShapeProfile = this.baseMapper.selectOne(new QueryWrapper<PersonnelProfile>().eq("soldier_category", soldierCategory));
        if (bodyShapeProfile == null) {
            throw new JeecgBootException("未配置" + ruleName);
        }

        // =================================================================
        // 步骤 2: 成绩分类 (将成绩分为体型课目和军事课目)
        // =================================================================
        Map<Boolean, List<LessonScoreVo>> partitionedScores = lessonScoreVos.stream()
                .collect(Collectors.partitioningBy(score -> "BMI".equalsIgnoreCase(score.getBmiOrPbf()) || "PBF".equalsIgnoreCase(score.getBmiOrPbf())));
        // 体型课目成绩列表
        List<LessonScoreVo> bodyShapeScores = partitionedScores.get(true);
        // 其他所有军事课目成绩列表
        List<LessonScoreVo> militaryScores = partitionedScores.get(false);

        // =================================================================
        // 步骤 3: “全优”前置判断 (识别军事素质顶尖人员)
        // =================================================================
        boolean isAllMilitaryExcellent = !militaryScores.isEmpty() && militaryScores.stream()
                .allMatch(score -> score != null && (LevelScoreEnum.fromCode(score.getGrade()).getScore() > 2 || (score.getScore() != null && score.getScore() == 100)));

        // =================================================================
        // 步骤 4: 体型评定与“一票否决”
        // =================================================================
        boolean isProcessBodyShape = param.isEnableCommonRule();
        log.info("是否使用通用规则(体型课目规则):{}", isProcessBodyShape);
        if (isProcessBodyShape) {
            // 4.1 将体型成绩转换为规则引擎所需的格式
            List<GradeLevel> bodyShapeGrades = bodyShapeScores.stream()
                    .map(score -> new GradeLevel(score.getLessonId(), score.getGrade()))
                    .collect(Collectors.toList());

            // 4.2 如果启用了体型规则但未提供体型成绩，则直接判定为不合格
            if (CollUtil.isEmpty(bodyShapeGrades)) {
                log.error("未找到体型课目成绩,直接判定为不及格");
                resultVo.setGrade("fail");
                return resultVo;

            }
            // 4.3 调用规则引擎进行体型评定
            String bmiGrade = ruleManager.calculate(bodyShapeProfile, JSONObject.toJSONString(bodyShapeGrades), EvaluationType.BMI_GRADING);
            // 4.4 执行“一票否决”：如果体型不合格, 且军事课目未达到全优, 则总评直接为不合格
            if ("fail".equalsIgnoreCase(bmiGrade) && !isAllMilitaryExcellent) {
                log.error("体型不合格, 且军事课目未达到全优, 则总评直接为不合格");
                resultVo.setGrade("fail");
                return resultVo;
            }
        }

        // =================================================================
        // 步骤 5: 军事课目总评 (根据不同人员类型执行不同评定逻辑)
        // =================================================================
        // 5.1 按课目大类对军事课目进行分组
        Map<String, List<LessonScoreVo>> militaryScoresByCategory = militaryScores.stream()
                .collect(Collectors.groupingBy(LessonScoreVo::getLessonCategory));
        // 5.2 安全地获取各个大类的成绩列表和所需计算的课目数量
        List<LessonScoreVo> grade1 = militaryScoresByCategory.getOrDefault("1", Collections.emptyList());
        List<LessonScoreVo> grade2 = militaryScoresByCategory.getOrDefault("2", Collections.emptyList());
        Integer baseScoreNum = bodyShapeProfile.getBaseScoreNum();
        List<LessonScoreVo> grade3 = militaryScoresByCategory.getOrDefault("3", Collections.emptyList());
        Integer combatSkillNum = bodyShapeProfile.getCombatSkillNum();
        List<LessonScoreVo> grade4 = militaryScoresByCategory.getOrDefault("4", Collections.emptyList());
        Integer practicalSkillNum = bodyShapeProfile.getPracticalSkillNum();

        // 5.3 分支逻辑：一类、二类人员
        if ("1".equals(soldierCategory) || "2".equals(soldierCategory)) {
            // 5.3.1 计算基础体能总分 (取最优的N项)
            String totalScore2 = getTotalScoreGrade(grade2, bodyShapeProfile, baseScoreNum, false);
            // 5.3.2 评定战斗技能等级 (取最优的N项)
            String finalGrade3 = getQualitativeGrade(grade3, bodyShapeProfile, combatSkillNum);
            // 5.3.3 计算实用技能总分 (取最优的N项)
            String totalScore4 = getTotalScoreGrade(grade4, bodyShapeProfile, practicalSkillNum, false);
            // 5.3.4 将上述三项结果组合，进行最终的“综合条件评等”
            List<GradeLevel> subjectScores = Arrays.asList(
                    new GradeLevel("baseScore", totalScore2),
                    new GradeLevel("combatSkill", finalGrade3),
                    new GradeLevel("practicalSkill", totalScore4)
            );
            try {
                String finalGrade = ruleManager.calculate(bodyShapeProfile, JSONObject.toJSONString(subjectScores), EvaluationType.COMPOUND_CONDITION_GRADING);
                resultVo.setGrade(finalGrade);
                log.info("个人评定成功，用户ID: {}, 规则名称: {}, 评定结果: {}", param.getUserId(), ruleName, resultVo.toString());
                return resultVo;
            } catch (Exception e) {
                log.error("评等时发生异常，用户ID: {}, 规则名称: {}", param.getUserId(), ruleName, e);
            }
        }

        // 5.4 分支逻辑：三类、文职、新兵
        if ("3".equals(soldierCategory) || "4".equals(soldierCategory) || "5".equals(soldierCategory)) {
            String ruleType = bodyShapeProfile.getRuleType();
            try {
                if ("1".equals(ruleType)) {
                    // 5.4.1 “总分评等”：计算基础体能总分，并根据总分评定等级
                    String finalGrade = getTotalScoreGrade(grade2, bodyShapeProfile, baseScoreNum, true);
                    resultVo.setGrade(finalGrade);
                    return resultVo;
                } else if ("2".equals(ruleType)) {
                    // 5.4.2 “定性评等”：根据基础体能的“各等级数量”进行评定
                    String finalGrade = getQualitativeGrade(grade2, bodyShapeProfile, baseScoreNum);
                    resultVo.setGrade(finalGrade);
                } else {
                    log.error("评定类型错误，用户ID: {}, 规则名称: {}", param.getUserId(), ruleName);
                    resultVo.setGrade("fail");
                }
            } catch (Exception e) {
                log.error("评定发生异常，用户ID: {}, 规则名称: {}", param.getUserId(), ruleName, e);
                // 如果总分评等失败，则降级到下一种评等方式，并先将结果置为fail
                resultVo.setGrade("fail");
            }
            log.info("个人评定成功，用户ID: {}, 规则名称: {}, 评定结果: {}", param.getUserId(), ruleName, resultVo.toString());
        }
        return resultVo;
    }

    private String getQualitativeGrade(List<LessonScoreVo> militaryScores, PersonnelProfile bodyShapeProfile, Integer num) {
        // [优化] 根据 combatSkillNum 对成绩进行排序和取优
        List<GradeLevel> subjectScores;
        if (num != null && num > 0 && CollUtil.isNotEmpty(militaryScores)) {
            subjectScores = militaryScores.stream()
                    // [优化] 过滤掉等级为空的成绩，并按等级值从高到低排序
                    .filter(scoreVo -> StringUtils.hasText(scoreVo.getGrade()))
                    // 按等级对应的枚举分数从大到小排序
                    .sorted(Comparator.comparing((LessonScoreVo scoreVo) -> LevelScoreEnum.fromCode(scoreVo.getGrade()).getScore()).reversed())
                    // 取前 num 条数据
                    .limit(num)
                    .map(score -> new GradeLevel(score.getGrade(), String.valueOf(score.getScore())))
                    .collect(Collectors.toList());
        } else {
            subjectScores = Collections.emptyList();
        }
        // 2. 调用规则引擎，进行定性评等
        return ruleManager.calculate(bodyShapeProfile, JSONObject.toJSONString(subjectScores), EvaluationType.QUALITATIVE_GRADING);
    }

    private String getTotalScoreGrade(List<LessonScoreVo> lessonScoreVos, PersonnelProfile bodyShapeProfile, Integer num, boolean isCalculate) {
        BigDecimal totalScore;
        if (num != null && num > 0 && CollUtil.isNotEmpty(lessonScoreVos)) {
            totalScore = lessonScoreVos.stream()
                    // 过滤掉分数为null的
                    .filter(scoreVo -> scoreVo.getScore() != null)
                    // 按分数从大到小排序
                    .sorted(Comparator.comparing(LessonScoreVo::getScore).reversed())
                    // 取前 num 条数据
                    .limit(num)
                    // 提取分数
                    .map(LessonScoreVo::getScore)
                    .map(BigDecimal::new)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            totalScore = BigDecimal.ZERO;
        }
        if (isCalculate) {
            // 1. 调用规则引擎，根据总分进行评等
            return ruleManager.calculate(bodyShapeProfile, totalScore.toString(), EvaluationType.TOTAL_SCORE_GRADING);
        }
        // 直接返回总分
        return totalScore.toString();
    }
}
