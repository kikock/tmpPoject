package org.jeecg.modules.rule.entity.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.excel.annotation.ExcelLength;
import org.jeecg.modules.excel.annotation.ExcelNumber;
import org.jeecg.modules.excel.annotation.ExcelPattern;
import org.jeecg.modules.excel.annotation.ExcelRequired;
import org.jeecg.modules.rule.annotation.ExcelSelect;

/**
 *
 * @project_name: 后端平台项目
 * @description: 课目规则导入数据
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@ApiModel(description = "课目规则导入数据")
public class LessonRulesExcelData {

    /*导入数据序号*/
    @ExcelIgnore
    private Integer rowIndex;
    /*导入数据校验是否通过状态*/
    @ExcelIgnore
    private boolean status;
    /**
     * 对应 "课目" 列
     */
    @ApiModelProperty(value = "课目")
    @ExcelProperty(value = {"课目"})
    @ColumnWidth(15)
    private String lessonCode;

    /**
     * 对应 "适用人员" -> "人员类别"
     * [核心] 使用字符串数组来定义多级表头
     */
    @ExcelProperty(value = {"适用人员", "人员类别"})
    @ApiModelProperty(value = "人员类别")
    @Dict(dicCode = "soldier_category")
    @ExcelSelect(source = {"一类", "二类", "三类", "文职人员"})
    @ColumnWidth(20)
    private String soldierCategory;
    /**
     * 对应 "适用人员" -> "是否新兵"
     */

    @ExcelProperty(value = {"适用人员", "是否新兵"})
    @ApiModelProperty(value = "是否新兵")
    @Dict(dicCode = "yn")
    @ExcelSelect(source = {"是", "否"})
    @ColumnWidth(20)
    private String newSoldier;

    /**
     * 对应 "适用人员" -> "性别"
     */
    @ExcelProperty(value = {"适用人员", "性别"})
    @Dict(dicCode = "sex")
    @ExcelSelect(source = {"男", "女"})
    @ColumnWidth(15)
    private String gender;

    /**
     * 对应 "适用人员" -> "考核制度"
     */
    @ApiModelProperty(value = "考核制度")
    @ExcelProperty(value = {"适用人员", "考核制度"})
    @Dict(dicCode = "assessment_method")
    @ExcelSelect(source = {"评分", "评等"})
    @ColumnWidth(20)
    private String assessmentMethod;

    /**
     * 对应 "适用人员" -> "等级制"
     */
    @ExcelProperty(value = {"适用人员", "等级制"})
    @Dict(dicCode = "evaluation_grade")
    @ExcelSelect(source = {"二级制", "四级制"})
    @ColumnWidth(20)
    private String evaluationGrade;

    /**
     * 对应 "适用人员" -> "海拔区间"
     */
    @ExcelProperty(value = {"适用人员", "海拔区间"})
    @ExcelPattern(regexp = "^\\d+-\\d+$")
    @ColumnWidth(20)
    private String altitudeRange;

    /**
     * 对应 "规则计算方式" 列
     */
    @ApiModelProperty(value = "规则计算方式")
    @ExcelProperty(value = "规则计算方式")
    @Dict(dicCode = "calculation_method")
    @ExcelSelect(source = {"越大越好", "越小越好", "区间选择"})
    @ColumnWidth(20)
    private String calculationMethod;

    @ExcelProperty(value = "年龄段")
    @ExcelRequired
    @ExcelPattern(regexp = "^\\d+-\\d+$")
    @ColumnWidth(15)
    private String ageRange;

    @ExcelProperty(value = {"规则详情", "评分"})
    @ExcelNumber(min = 0, max = 100)
    private String score;

    @Dict(dicCode = "seven_level_grading")
    @ExcelProperty(value = {"规则详情", "评等"})
    @ExcelLength(length = 20)
    @ExcelSelect(source = {"优秀", "良好", "及格"})
    private String grade;

    @ExcelProperty(value = {"规则详情", "成绩"})
    private String result;

    @ExcelProperty(value = {"规则详情", "区间"})
    @ExcelPattern(regexp = "^(\\d+-\\d+)(\\s*,\\s*\\d+-\\d+)*$")
    private String range;

}