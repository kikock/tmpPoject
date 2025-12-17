package org.jeecg.modules.rule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.rule.entity.domain.RawRule;
import org.jeecg.modules.rule.handler.RawRuleHandler;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 课目评等规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@TableName(value = "mts_lesson_grading_rules", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "课目评等规则")
public class LessonGradingRules implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 规则名称
     */
    @Excel(name = "规则名称", width = 15)
    @ApiModelProperty(value = "规则名称")
    private String ruleName;
    /**
     * 画像id
     */
    @Excel(name = "画像id", width = 15, dictTable = "mts_user_profiles", dicText = "name", dicCode = "id")
    @Dict(dictTable = "mts_user_profiles", dicText = "name", dicCode = "id")
    @ApiModelProperty(value = "画像id")
    private String userProfileId;
    /**
     * 计算方式
     */
    @Excel(name = "计算方式", width = 15, dicCode = "calculation_method")
    @Dict(dicCode = "calculation_method")
    @ApiModelProperty(value = "计算方式")
    private String calculationMethod;
    /**
     * 最小年龄
     */
    @Excel(name = "最小年龄", width = 15)
    @ApiModelProperty(value = "最小年龄")
    private Integer minAge;
    /**
     * 最大年龄
     */
    @Excel(name = "最大年龄", width = 15)
    @ApiModelProperty(value = "最大年龄")
    private Integer maxAge;
    /**
     * 计量单位
     */
    @Excel(name = "计量单位", width = 15, dicCode = "lesson_unit")
    @Dict(dicCode = "lesson_unit")
    @ApiModelProperty(value = "计量单位")
    private String unit;
    /**
     * 最小年龄
     */
    @Excel(name = "年龄段序号", width = 15)
    @ApiModelProperty(value = "年龄段序号")
    private Integer ageScoreIndex;

    @Excel(name = "年龄范围", width = 15)
    @ApiModelProperty(value = "年龄范围")
    private String ageRange;

    /**
     * 最小年龄
     */
    @Excel(name = "等级制", width = 15)
    @ApiModelProperty(value = "等级制")
    @Dict(dicCode = "evaluation_grade")
    private String evaluationGrade;
    /**
     * 评等规则
     */
    @Excel(name = "评等规则", width = 15)
    @ApiModelProperty(value = "评等规则")
    @JsonProperty("rulesJson")
    @TableField(value = "rulesJson", typeHandler = RawRuleHandler.class)
    private List<RawRule> rulesJson;
}