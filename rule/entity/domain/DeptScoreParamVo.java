package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.modules.rule.utils.GradeLevel;

import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 课目(组)成绩计算参数VO
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "课目(组)成绩计算参数VO")
public class DeptScoreParamVo {

    @ApiModelProperty(value = "单位评定规则id")
    private String id;

    @ApiModelProperty(value = "单位评定参数")
    private List<GradeLevel> gradeLevels;

}