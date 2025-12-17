package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.modules.rule.entity.LessonGradingRules;

import java.io.Serializable;


/**
 * @project_name: 后端平台项目
 * @description: 适配用户的单个评等规则详情
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(value = "适配用户的单个评等规则详情", description = "适配用户的单个评等规则详情")
public class AdaptedGradingRuleVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "画像信息")
    private UserProfiles userProfiles;

    @ApiModelProperty(value =  "原始评等规则")
    private LessonGradingRules rule;

    @ApiModelProperty(value =  "根据原始成绩计算出的最终等级, 如: 优秀")
    private String grade;

    @ApiModelProperty(value =  "传入的原始成绩，如: 12.5")
    private String rawPerformance;

    @ApiModelProperty(value =  "带单位的完整成绩, 如: 12.5秒")
    private String performanceWithUnit;

    public AdaptedGradingRuleVo() {
    }

    public AdaptedGradingRuleVo(LessonGradingRules rule, UserProfiles userProfiles) {
        this.rule = rule;
        this.userProfiles = userProfiles;
    }
}