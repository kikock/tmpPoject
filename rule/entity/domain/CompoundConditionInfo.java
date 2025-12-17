package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 复合条件评等规则详情
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(value = "复合条件评等规则详情", description = "复合条件评等规则详情")
public class CompoundConditionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "最终评定的等级名称")
    private String grade;

    @ApiModelProperty(value = "排序字段")
    private Integer sort;

    @ApiModelProperty(value = "必须全部满足的条件列表")
    private List<ConditionRule> conditions;
}