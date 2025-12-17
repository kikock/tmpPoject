package org.jeecg.modules.rule.entity.domain;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @project_name: 后端平台项目
 * @description: 复合条件评等中的单个条件规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "复合条件评等中的单个条件规则")
public class ConditionRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "科目编码")
    private String subject;

    @ApiModelProperty(value = "科目名称")
    private String name;

    @ApiModelProperty(value = "等级制字典编码 (如果为空，则为分数制)")
    @JSONField(name = "evaluationGrade")
    private String evaluationGrade;

    @ApiModelProperty(value = "要求达到的值 (等级或分数)")
    private String value;

}