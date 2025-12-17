package org.jeecg.modules.rule.entity.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @project_name: 后端平台项目
 * @description: 定性评分规则详情
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "定性评分规则详情")
public class ScoreAssessmentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "等级名称")
    private String grade;

    @ApiModelProperty(value = "最低分数要求")
    @JsonProperty("min_score")
    private BigDecimal minScore;

    @ApiModelProperty(value = "排序字段")
    private Integer sort;
}