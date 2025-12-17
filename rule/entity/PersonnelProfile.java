package org.jeecg.modules.rule.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.rule.entity.domain.RuleLogicBlock;
import org.jeecg.modules.rule.handler.RuleLogicBlockListHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @project_name: 后端平台项目
 * @description: 个人成绩规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@TableName(value = "mts_personnel_profile", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "个人成绩规则")
public class PersonnelProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ExcelIgnore
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 创建人
     */
    @ExcelIgnore
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**
     * 创建日期
     */
    @ExcelIgnore
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
    /**
     * 更新人
     */
    @ExcelIgnore
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    /**
     * 更新日期
     */
    @ExcelIgnore
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
    /**
     * 所属部门
     */
    @ExcelIgnore
    @ApiModelProperty(value = "所属部门")
    private String sysOrgCode;
    /**
     * 规则名称
     */
    @Excel(name = "规则名称", width = 15)
    @ApiModelProperty(value = "规则名称")
    private String name;
    /**
     * 规则描述
     */
    @Excel(name = "规则描述", width = 15)
    @ApiModelProperty(value = "规则描述")
    private String description;
    /**
     * 规则类型
     */
    @Excel(name = "规则类型", width = 15)
    @ApiModelProperty(value = "规则类型")
    private String ruleType;
    /**
     * 启用状态
     */
    @Excel(name = "启用状态", width = 15)
    @ApiModelProperty(value = "启用状态")
    private String isEnabled;
    /**
     * 具体规则逻辑
     */
    @ExcelIgnore // 复杂类型建议忽略或自定义转换器
    @ApiModelProperty(value = "具体规则逻辑")
    @TableField(value = "rule_logic", typeHandler = RuleLogicBlockListHandler.class)
    private List<RuleLogicBlock> ruleLogic;
    /**
     * 适用条件
     */
    @Excel(name = "适用条件", width = 15)
    @ApiModelProperty(value = "适用条件")
    private String ruleUsers;
    /**
     * 基础体能课目总数
     */
    @Excel(name = "基础体能课目总数", width = 15)
    @ApiModelProperty(value = "基础体能课目总数")
    private Integer baseScoreNum;
    /**
     * 战斗技能课目总数
     */
    @Excel(name = "战斗技能课目总数", width = 15)
    @ApiModelProperty(value = "战斗技能课目总数")
    private Integer combatSkillNum;
    /**
     * 实用技能课目总数
     */
    @Excel(name = "实用技能课目总数", width = 15)
    @ApiModelProperty(value = "实用技能课目总数")
    private Integer practicalSkillNum;
    /**
     * 基础知识课目总数
     */
    @Excel(name = "基础知识课目总数", width = 15)
    @ApiModelProperty(value = "基础知识课目总数")
    private Integer baseKnowledgeNum;
    /**
     * 人员类型
     */
    @Excel(name = "人员类型", width = 15)
    @ApiModelProperty(value = "人员类型")
    private Integer soldierCategory;
}