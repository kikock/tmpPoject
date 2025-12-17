package org.jeecg.modules.rule.entity.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.modules.rule.entity.domain.LessonScoreVo;

import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 课目(组)成绩计算参数VO
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "个人成绩计算参数VO")
public class UserScoreParamVo {

    @ApiModelProperty(value = "用户id")
    private String userId;
    @ApiModelProperty(value = "人员类型(1.一类人员,2.二类人员,3.三类人员,4.文职人员,5.新兵")
    private String soldierCategory;
    @ApiModelProperty(value = "计划id")
    private String planId;
    @ApiModelProperty(value = "是否启用通用规则")
    private boolean enableCommonRule;
    @ApiModelProperty(value = "参与课目成绩列表")
    private List<LessonScoreVo> lessonScoreVos;

}