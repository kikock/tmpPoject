package org.jeecg.modules.rule.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
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
 * @description: 课目评分规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@TableName(value = "mts_lesson_scoring_rules", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "课目评分规则")
public class LessonScoringRules implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ExcelIgnore
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
    @ApiModelProperty(value = "计算方式")
    @Dict(dicCode = "calculation_method")
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
     * 最低分
     */
    @Excel(name = "最低分", width = 15)
    @ApiModelProperty(value = "最低分")
    private Integer minScore;
    /**
     * 最高分
     */
    @Excel(name = "最高分", width = 15)
    @ApiModelProperty(value = "最高分")
    private Integer maxScore;
    /**
     * 步进值
     */
    @Excel(name = "步进值", width = 15)
    @ApiModelProperty(value = "步进值")
    private Integer stepValue;
    /**
     * 计量单位
     */
    @Excel(name = "计量单位", width = 15, dicCode = "lesson_unit")
    @ApiModelProperty(value = "计量单位")
    @Dict(dicCode = "lesson_unit")
    private String unit;
    @Excel(name = "年龄范围", width = 15)
    @ApiModelProperty(value = "年龄范围")
    private String ageRange;
    @Excel(name = "成绩范围", width = 15)
    @ApiModelProperty(value = "成绩范围")
    private String scoreRange;
    /**
     * 最小年龄
     */
    @Excel(name = "年龄段序号", width = 15)
    @ApiModelProperty(value = "年龄段序号")
    private String ageScoreIndex;
    /**
     * 评分规则
     */
    @Excel(name = "评分规则", width = 15)
    @ApiModelProperty(value = "评分规则")
    @JsonProperty("rulesJson")
    @TableField(value = "rulesJson", typeHandler = RawRuleHandler.class)
    private List<RawRule> rulesJson;
}