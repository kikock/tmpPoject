package org.jeecg.modules.rule.utils;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @project_name: 后端平台项目
 * @description: 等级分值
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
public class GradeLevel {
    private String itemName;
    // 等级名称
    private String name;
    // 等级对应的分值
    private String value;
    //判断等级的最低门槛要求所有 n 个课目都达到该等级的分值
    private boolean requireAllCurrentLevel;

    /**
     * 全参数构造函数，所有其他构造函数最终都应调用此构造函数。
     */
    public GradeLevel(String itemName, String name, String value, boolean requireAllCurrentLevel) {
        this.itemName = itemName;
        this.name = name;
        this.value = value;
        this.requireAllCurrentLevel = requireAllCurrentLevel;
    }

    /**
     * GradeLevel 的构造函数，明确指定是否要求所有课目都达到当前等级。
     *
     * @param name                   等级名称（例如：“优秀”、“良好”、“及格”、“不及格”）。
     * @param value                  分配给该等级的数值。
     * @param requireAllCurrentLevel 如果为 true，则该等级的最低门槛将计算为 n * value。
     *                               如果为 false，则将使用 (n-1)*value + 1*nextLowerLevelValue 规则。
     */
    public GradeLevel(String name, String value, boolean requireAllCurrentLevel) {
        this(null, name, value, requireAllCurrentLevel);
    }

    /**
     * GradeLevel 的构造函数，默认不要求所有课目都达到当前等级。
     * 此构造函数保持向后兼容性。
     *
     * @param name  等级名称。
     * @param value 分配给该等级的数值。
     */
    public GradeLevel(String name, String value) {
        this(null, name, value, false);
    }

    /**
     * GradeLevel 的构造函数，默认不要求所有课目都达到当前等级。
     * 此构造函数保持向后兼容性。
     */
    public GradeLevel(String itemName, String name, String value) {
        this(itemName, name, value, false);
    }

    /**
     * 返回一个包含传统评等的 GradeLevel 列表.
     *
     * @return 包含优秀、良好、及格、不及格的 GradeLevel 列表
     */
    public static List<GradeLevel> getTraditionalGradeLevels() {
        return Arrays.asList(
                new GradeLevel("优秀", "3"),
                new GradeLevel("良好", "2"),
                new GradeLevel("及格", "1"),
                new GradeLevel("不及格", "0")
        );
    }

    public boolean requiresAllCurrentLevel() {
        return requireAllCurrentLevel;
    }

    @Override
    public String toString() {
        // 使用 String.format 提高可读性，并处理 itemName 可能为 null 的情况
        if (StringUtils.hasText(itemName)) {
            return String.format("%s (%s: %s分)", itemName, name, value);
        }
        return String.format("%s (%s分)", name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradeLevel that = (GradeLevel) o;
        return requireAllCurrentLevel == that.requireAllCurrentLevel && Objects.equals(itemName, that.itemName) && Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemName, name, value, requireAllCurrentLevel);
    }
}