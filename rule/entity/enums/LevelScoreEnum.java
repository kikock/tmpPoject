package org.jeecg.modules.rule.entity.enums;

import lombok.Getter;

/**
 * @project_name: 后端平台项目
 * @description: 等级枚举
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Getter
public enum LevelScoreEnum {
    SPECIAL_ONE("special_one", 6),
    SPECIAL_TWO("special_two", 5),
    SPECIAL_THREE("special_three", 4),
    EXCELLENT("excellent", 3),
    GOOD("good", 2),
    PASS("pass", 1),
    FAIL("fail", 0);

    /**
     * code
     */
    private final String code;

    /**
     * 类型的分数
     */
    private final Integer score;

    LevelScoreEnum(String code, Integer score) {
        this.code = code;
        this.score = score;
    }

    /**
     * 根据code查找对应的枚举
     *
     * @param code 编码
     * @return 对应的枚举，或null
     */
    public static LevelScoreEnum fromCode(String code) {
        for (LevelScoreEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return LevelScoreEnum.FAIL;
    }
}