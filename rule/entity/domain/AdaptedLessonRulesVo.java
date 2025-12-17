package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @project_name: 后端平台项目
 * @description: 适配用户的课目规则集合
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(value = "适配用户的课目规则集合", description = "适配用户的课目规则集合")
public class AdaptedLessonRulesVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "匹配到的评分规则详情")
    private AdaptedScoringRuleVo scoringRule;

    @ApiModelProperty(value = "匹配到的评等规则详情")
    private AdaptedGradingRuleVo gradingRule;
}