package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @project_name: 后端平台项目
 * @description: 规则文字描述VO
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "规则文字描述VO")
public class RuleDescriptionVo {
    @ApiModelProperty(value = "规则名称")
    private String name;

    @ApiModelProperty(value = "年龄段描述", example = "年龄段 [18, 25]")
    private String ageRangeText;

    @ApiModelProperty(value = "规则详情的文字描述")
    private String descriptionText;
}