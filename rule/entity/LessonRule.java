package org.jeecg.modules.rule.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.Date;

/**
 * @project_name: 后端平台项目
 * @description: 课目(组)规则信息
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@TableName("v_lesson_rule")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "课目(组)规则信息")
public class LessonRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ExcelIgnore
    @ApiModelProperty(value = "主键")
    private String id;
    /**
     * 专业
     */
    @Excel(name = "专业", width = 15, dictTable = "mts_specialty", dicText = "name", dicCode = "id")
    @Dict(dictTable = "mts_specialty", dicText = "name", dicCode = "id")
    @ApiModelProperty(value = "专业")
    private String mtsSpecialty;
    /**
     * 课目(组)名称
     */
    @Excel(name = "课目(组)名称", width = 15)
    @ApiModelProperty(value = "课目(组)名称")
    private String name;
    /**
     * 课目(组)编号
     */
    @Excel(name = "课目(组)编号", width = 15)
    @ApiModelProperty(value = "课目(组)编号")
    private String code;
    /**
     * 分类
     */
    @Excel(name = "分类", width = 15, dicCode = "main_category")
    @Dict(dicCode = "main_category")
    @ApiModelProperty(value = "分类")
    private String mainCategory;
    /**
     * 类型
     */
    @Excel(name = "类型", width = 15, dicCode = "lesson_type")
    @Dict(dicCode = "lesson_type")
    @ApiModelProperty(value = "类型")
    private String type;

    /**
     * 课目组选择课目
     */
    @Excel(name = "课目", width = 15, dictTable = "mts_lesson", dicText = "name", dicCode = "id")
    @Dict(dictTable = "mts_lesson", dicText = "name", dicCode = "id")
    @ApiModelProperty(value = "课目")
    private String lessonIds;
    /**
     * 课目组选择课目数量
     */
    @Excel(name = "课目取值方式", width = 15)
    @ApiModelProperty(value = "课目取值方式")
    @Dict(dicCode = "capture_method")
    private Integer captureMethod;

    /**
     * 计量单位
     */
    @Excel(name = "计量单位", width = 15, dicCode = "lesson_unit")
    @Dict(dicCode = "lesson_unit")
    @ApiModelProperty(value = "计量单位")
    private String unit;
    /**
     * 已配置规则数量
     */
    @Excel(name = "已配置规则数量", width = 15)
    @ApiModelProperty(value = "已配置规则数量")
    private Integer profileCount;

    /**
     * 高原补偿数量
     */
    @Excel(name = "高原补偿数量", width = 15)
    @ApiModelProperty(value = "高原补偿数量")
    private Integer plateauCount;
    @ApiModelProperty(value = "高原区间内容")
    @TableField(exist = false)
    private String plateauText;

    /**
     * 最后更新时间
     */
    @Excel(name = "最后更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "最后更新时间")
    private Date lastUpdateTime;
}