package org.jeecg.modules.rule.entity.enums;

import lombok.Getter;

/**
 * @project_name: 后端平台项目
 * @description: 评定类型枚举
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Getter
public enum EvaluationType {
    TOTAL_SCORE_GRADING("total_score", "总分评等"),
    QUALITATIVE_GRADING("qualitative", "定性评等"),
    BMI_GRADING("bmi", "BMI评等"),
    COMPOUND_CONDITION_GRADING("compound_condition", "综合评等");

    /**
     * 类型的编码，用于内部逻辑判断
     */
    private final String code;

    /**
     * 类型的描述，用于显示
     */
    private final String description;

    EvaluationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code查找对应的枚举
     *
     * @param code 编码
     * @return 对应的枚举，或null
     */
    public static EvaluationType fromCode(String code) {
        for (EvaluationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}