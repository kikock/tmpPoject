package org.jeecg.modules.rule.entity.domain;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @project_name: 后端平台项目
 * @description: 定性评等规则详情
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "定性评等规则详情")
public class GradeAssessmentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "等级名称")
    private String grade;

    @ApiModelProperty(value = "排序字段")
    private Integer sort;

    @ApiModelProperty(value = "必需的成绩数量, Key: 等级名称, Value: 数量")
    @JSONField(name = "required_counts")
    private Map<String, Integer> requiredCounts;

    @ApiModelProperty(value = "参与评定的总课目数")
    @JSONField(name = "total_subjects")
    private Integer totalSubjects;
}