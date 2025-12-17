package org.jeecg.modules.rule.entity.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 课目(组)成绩计算参数VO
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "课目(组)成绩计算参数VO")
public class LessonScoreParamVo {

    @ApiModelProperty(value =  "用户ID")
    private String userId;

    @ApiModelProperty(value =  "年龄")
    private Integer age;

    @ApiModelProperty(value =  "课目(组)ID")
    private String lessonId;

    @ApiModelProperty(value =  "原始成绩")
    private String rawPerformance;

    @ApiModelProperty(value =  "该课目组下，所有子课目的成绩列表")
    private List<SubjectPerformance> subjectPerformance;

    @Data
    public static class SubjectPerformance {
        @ApiModelProperty(value =  "子课目ID")
        private String lessonId;
        @ApiModelProperty(value =  "该子课目的原始成绩")
        private String rawPerformance;
    }
}