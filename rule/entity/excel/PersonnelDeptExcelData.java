package org.jeecg.modules.rule.entity.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.rule.annotation.ExcelSelect;

/**
 * @project_name: 后端平台项目
 * @description: 单位总评规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "单位总评规则excel模板")
public class PersonnelDeptExcelData {
    /*导入数据序号*/
    @ExcelIgnore
    private Integer rowIndex;
    /*导入数据校验是否通过状态*/
    @ExcelIgnore
    private boolean status;
    /**
     * 规则名称
     * 这是一个单列表头，直接设置即可
     */
    @ExcelProperty("规则名称")
    @ColumnWidth(15)
    private String ruleName;

    /**
     * 评等形式
     * 这也是一个单列表头
     */
    @ExcelProperty("评等形式")
    @ColumnWidth(15)
    @ExcelSelect(source = {"二级制", "四级制"})
    @Dict(dicCode = "evaluation_grade")
    private String ruleType;
    /**
     * 评定等级
     * EasyExcel 会自动将具有相同一级表头的列进行合并
     */
    @ExcelProperty(value = {"单位评定规则", "评定等级"})
    @ColumnWidth(15)
    @ExcelSelect(source = {"优秀", "良好", "及格"})
    @Dict(dicCode = "seven_level_grading")
    private String grade;
    /**
     * 参考率
     * EasyExcel 会自动将具有相同一级表头的列进行合并
     */
    @ExcelProperty(value = {"单位评定规则", "参考率"})
    @ColumnWidth(15)
    @ApiModelProperty(value = "参考率")
    private String participationRate;

    /**
     * 优秀率
     */
    @ExcelProperty(value = {"单位评定规则", "优秀率"})
    @ColumnWidth(15)
    @ApiModelProperty(value = "优秀率")
    private String excellentRate;

    /**
     * 良好率
     */
    @ExcelProperty(value = {"单位评定规则", "良好率"})
    @ColumnWidth(15)
    @ApiModelProperty(value = "良好率")
    private String goodRate;

    /**
     * 及格率
     */
    @ExcelProperty(value = {"单位评定规则", "及格率"})
    @ColumnWidth(15)
    @ApiModelProperty(value = "及格率")
    private String passRate;

    /**
     * 军事主官成绩
     */
    @ExcelProperty(value = {"单位评定规则", "军事主官成绩"})
    @ColumnWidth(20)
    @ApiModelProperty(value = "军事主官成绩")
    @ExcelSelect(source = {"优秀", "良好", "及格"})
    @Dict(dicCode = "seven_level_grading")
    private String militaryChiefScore;

    /**
     * 政治主官成绩
     */
    @ExcelProperty(value = {"单位评定规则", "政治主官成绩"})
    @ColumnWidth(20)
    @ApiModelProperty(value = "政治主官成绩")
    @ExcelSelect(source = {"优秀", "良好", "及格"})
    @Dict(dicCode = "seven_level_grading")
    private String politicalChiefScore;

    /**
     * 默认说明内容
     */
    @ExcelProperty("说明")
    @ColumnWidth(40)
    @ApiModelProperty(value = "说明")
    private String description;

}