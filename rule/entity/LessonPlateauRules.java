package org.jeecg.modules.rule.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @project_name: 后端平台项目
 * @description: 高原补偿规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@TableName("mts_lesson_plateau_rules")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "高原补偿规则")
public class LessonPlateauRules implements Serializable {

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
    @org.jeecgframework.poi.excel.annotation.ExcelIgnore
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**
     * 创建日期
     */
    @org.jeecgframework.poi.excel.annotation.ExcelIgnore
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
    /**
     * 更新人
     */
    @org.jeecgframework.poi.excel.annotation.ExcelIgnore
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    /**
     * 更新日期
     */
    @org.jeecgframework.poi.excel.annotation.ExcelIgnore
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
    /**
     * 规则名称
     */
    @Excel(name = "规则名称", width = 15)
    @ApiModelProperty(value = "规则名称")
    private String ruleName;
    /**
     * 课目id
     */
    @Excel(name = "课目", width = 15, dictTable = "mts_lesson", dicText = "name", dicCode = "id")
    @Dict(dictTable = "mts_lesson", dicText = "name", dicCode = "id")
    @ApiModelProperty(value = "课目id")
    private String lessonId;
    /**
     * 最低海拔
     */
    @Excel(name = "最低海拔", width = 15)
    @ApiModelProperty(value = "最低海拔")
    private Integer minAltitude;
    /**
     * 最高海拔
     */
    @Excel(name = "最高海拔", width = 15)
    @ApiModelProperty(value = "最高海拔")
    private Integer maxAltitude;
    /**
     * 海拔增加值
     */
    @Excel(name = "海拔增加值", width = 15)
    @ApiModelProperty(value = "海拔增加值")
    private Integer changeNum;
    /**
     * 变动方式
     */
    @Excel(name = "变动方式", width = 15)
    @ApiModelProperty(value = "变动方式")
    private String changeMethod;
    /**
     * 变动成绩成绩值
     */
    @Excel(name = "变动成绩成绩值", width = 15)
    @ApiModelProperty(value = "变动成绩成绩值")
    private Integer changeValue;
    /**
     * 计量单位
     */
    @Excel(name = "计量单位", width = 15)
    @ApiModelProperty(value = "计量单位")
    private String unit;
}