package org.jeecg.modules.rule.utils;

import lombok.Data;

import java.util.List;

/**
 * @project_name: 后端平台项目
 * @description: 匹配条件的数据结构
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
public class MatchingCondition {

    /**
     * 单个匹配条件，由字段名、操作符和值组成。
     */
    @Data
    public static class Condition {
        private String fieldName; // 字段名, 例如: "gender"
        private String operator;  // 操作符, 例如: "=", ">", "<", "in"
        private String value;     // 匹配值, 例如: "男", "2000"

        public Condition(String fieldName, String operator, String value) {
            this.fieldName = fieldName;
            this.operator = operator;
            this.value = value;
        }

        public Condition() {
        }
    }

    /**
     * 区间匹配条件，由字段名、最小值和最大值组成。
     */
    @Data
    public static class RangeCondition {
        private String fieldName; // 字段名, 例如: "altitude"
        private String min;       // 最小值, 例如: "2000"
        private String max;       // 最大值, 例如: "3000"

        public RangeCondition(String fieldName, String min, String max) {
            this.fieldName = fieldName;
            this.min = min;
            this.max = max;
        }

        public RangeCondition() {
        }
    }

    /**
     * 匹配规则，包含一个或多个条件。
     * 默认是 AND 关系。
     */
    @Data
    public static class MatchingRule {
        private String ruleName;
        private List<Condition> conditions;
        private List<RangeCondition> rangeConditions;
    }
}

