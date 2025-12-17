package org.jeecg.modules.rule.entity.domain;

import cn.hutool.json.JSONArray;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @project_name: 后端平台项目
 * @description: 规则详情
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "规则详情")
public class RuleLogicBlock implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "规则类型 (total_score, qualitative,bmi,compound_condition)")
    private String type;

    @ApiModelProperty(value = "考核评定等级")
    private String evaluationGrade;

    @ApiModelProperty(value = "是否启用")
    private boolean enabled;

    @ApiModelProperty(value = "具体的评定信息列表")
    private JSONArray assessment_info;
}