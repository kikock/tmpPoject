package org.jeecg.modules.rule.entity.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.rule.handler.ConditionsJsonTypeHandler;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelIgnore;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * @project_name: 后端平台项目
 * @description: 用户画像
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@TableName("mts_user_profiles")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "用户画像")
public class UserProfiles implements Serializable {
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
     * 画像名称
     */
    @Excel(name = "画像名称", width = 15)
    @ApiModelProperty(value = "画像名称")
    private String name;
    /**
     * 画像规则
     */
    @Excel(name = "画像规则", width = 15)
    @ApiModelProperty(value = "画像规则")
    @JsonProperty("conditionsJson")
    @TableField(value = "conditions_json", typeHandler = ConditionsJsonTypeHandler.class)
    private ConditionsJson conditionsJson;
    /**
     * 启用状态
     */
    @Excel(name = "启用状态", width = 15, dicCode = "valid_status")
    @Dict(dicCode = "valid_status")
    @ApiModelProperty(value = "启用状态")
    private Integer isEnabled;
    /**
     * 课目(组)id
     */
    @Excel(name = "课目(组)id", width = 15)
    @ApiModelProperty(value = "课目(组)id")
    @Dict(dictTable = "mts_lesson", dicText = "name", dicCode = "id")
    private String lessonId;
}