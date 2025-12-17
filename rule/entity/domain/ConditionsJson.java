package org.jeecg.modules.rule.entity.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;

import java.io.Serializable;



/**
 * @project_name: 后端平台项目
 * @description: 用户画像条件
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@Accessors(chain = true)
@ApiModel(description = "用户画像条件")
public class ConditionsJson implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 人员类别
     */
    @JsonProperty("soldierCategory")
    @ApiModelProperty(value = "人员类别")
    @Dict(dicCode = "soldier_category")
    private String soldierCategory;

    /**
     * 是否新兵
     */
    @JsonProperty("newSoldier")
    @ApiModelProperty(value = "是否新兵")
    @Dict(dicCode = "yn")
    private String newSoldier;

    /**
     * 性别
     */
    @JsonProperty("gender")
    @ApiModelProperty(value = "性别")
    @Dict(dicCode = "sex")
    private String gender;

    @JsonProperty("assessmentMethod")
    @ApiModelProperty(value = "考核制度")
    @Dict(dicCode = "assessment_method")
    private String assessmentMethod;
    /**
     * 海拔
     */
    @JsonProperty("altitude")
    @ApiModelProperty(value = "海拔区间")
    private String altitude;
    /**
     * 海拔  大于1500 小于等于1500
     */
    @JsonProperty("altitudeType")
    @ApiModelProperty(value = "海拔")
    @Dict(dicCode = "altitude_range")
    private String altitudeType;
    /**
     * 考核评定等级(二级,四级,七级)
     */
    @JsonProperty("evaluationGrade")
    @ApiModelProperty(value = "考核评定等级")
    @Dict(dicCode = "evaluation_grade")
    private String evaluationGrade;
    /**
     * 计算方式
     */
    @JsonProperty("calculationMethod")
    @ApiModelProperty(value = "计算方式")
    @Dict(dicCode = "calculation_method")
    private String calculationMethod;

}