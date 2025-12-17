package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.modules.rule.entity.LessonScoringRules;

import java.io.Serializable;


/**
 * @project_name: 后端平台项目
 * @description: 适配用户的单个评分规则详情
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "适配用户的单个评分规则详情")
public class AdaptedScoringRuleVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "画像信息")
    private UserProfiles userProfiles;

    @ApiModelProperty(value = "原始评分规则")
    private LessonScoringRules rule;

    @ApiModelProperty(value = "根据原始成绩计算出的最终得分, 如: 85")
    private Integer score;

    @ApiModelProperty(value = "传入的原始成绩，如: 50")
    private String rawPerformance;

    @ApiModelProperty(value = "带单位的完整成绩, 如: 50个")
    private String performanceWithUnit;

    public AdaptedScoringRuleVo(LessonScoringRules rule, UserProfiles userProfiles) {
        this.rule = rule;
        this.userProfiles = userProfiles;
    }

    public AdaptedScoringRuleVo() {
    }
}