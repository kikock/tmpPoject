package org.jeecg.modules.rule.entity.domain;

import lombok.Data;
import org.jeecg.modules.rule.entity.LessonGradingRules;
import org.jeecg.modules.rule.entity.LessonScoringRules;

import java.util.List;

/**
 * @project_name: 后端平台项目
 * @description: 课目规则详情VO类 包含用户画像信息和相关规则信息
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
public class LessonRuleDetailVo {
    /**
     * 用户画像信息
     */
    private UserProfiles userProfiles;
    /**
     * 课目评分规则
     */
    private List<LessonScoringRules> lessonScoringRules;
    /**
     * 课目评等规则
     */
    private List<LessonGradingRules> lessonGradingRules;


}