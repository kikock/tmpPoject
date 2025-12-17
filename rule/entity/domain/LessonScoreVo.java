package org.jeecg.modules.rule.entity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;

import java.io.Serializable;
import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 适配用户的课目规则集合
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "适配用户的课目规则集合")
public class LessonScoreVo implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "课目id")
    private String lessonId;
    @ApiModelProperty(value = "课目大类")
    private String lessonCategory;
    @ApiModelProperty(value =  "BMI或者PBF")
    private String bmiOrPbf;
    @ApiModelProperty(value =  "根据原始成绩计算出的最终得分, 如: 75")
    private Integer score;
    @ApiModelProperty(value =  "根据原始成绩计算出的最终等级, 如: 良好")
    @Dict(dicCode = "grade", dicText = "seven_level_grading")
    private String grade;
    @ApiModelProperty(value =  "传入的原始成绩，如: 40")
    private String rawPerformance;
    @ApiModelProperty(value =  "带单位的完整成绩, 如: 40个")
    private String performanceWithUnit;
    @ApiModelProperty(value =  "消息")
    private String msg;
    @ApiModelProperty(value =  "评分规则计算方式")
    @Dict(dicCode = "calculation_method")
    private String calculationMethod;
    @ApiModelProperty(value =  "科目组下级课目成绩")
    private List<LessonScoreVo> lessonScoreVos;
    
    
}