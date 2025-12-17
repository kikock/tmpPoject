package org.jeecg.modules.rule.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @project_name: 后端平台项目
 * @description: 高原补偿规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "高原补偿规则")
public class PlateauRuleParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    private String ruleName;
    /**
     * 课目id
     */
    @ApiModelProperty(value = "课目id")
    private String lessonId;
    /**
     * 最低海拔
     */
    @ApiModelProperty(value = "最低海拔")
    @NotNull(message = "最低海拔不能为空")
    private Integer minAltitude;
    /**
     * 最高海拔
     */
    @ApiModelProperty(value = "最高海拔")
    @NotNull(message = "最高海拔不能为空")
    private Integer maxAltitude;
    /**
     * 海拔增加值
     */
    @ApiModelProperty(value = "海拔增加值")
    @NotNull(message = "海拔增加值不能为空")
    private Integer changeNum;
    /**
     * 变动方式
     */
    @ApiModelProperty(value = "变动方式")
    @NotNull(message = "变动方式不能为空")
    private String changeMethod;
    /**
     * 变动成绩成绩值
     */
    @ApiModelProperty(value = "变动成绩成绩值")
    @NotNull(message = "变动成绩成绩值不能为空")
    private Integer changeValue;
    /**
     * 计量单位
     */
    @ApiModelProperty(value = "计量单位")
    @NotNull(message = "计量单位不能为空")
    private String unit;
}