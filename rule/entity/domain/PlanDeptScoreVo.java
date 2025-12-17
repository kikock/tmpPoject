package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.io.Serializable;


/**
 * @project_name: 后端平台项目
 * @description: 适配用户的课目规则集合
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "适配用户的课目规则集合")
public class PlanDeptScoreVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "根据原始成绩计算出的最终等级, 如: 良好")
    @Dict(dicCode = "grade", dicText = "seven_level_grading")
    private String grade;

    @ApiModelProperty(value = "得分")
    private String score;
}