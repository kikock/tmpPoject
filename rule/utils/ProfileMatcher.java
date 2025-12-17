package org.jeecg.modules.rule.utils;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.rule.entity.domain.ConditionsJson;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

/**
 * @project_name: 后端平台项目
 * @description: 人员画像匹配器，根据规则校验人员画像是否符合条件。
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Component
@Slf4j
public class ProfileMatcher {


    @Resource
    private ProfileMatcherProperties profileMatcherProperties;

    /**
     * 根据匹配规则，校验人员画像是否符合所有条件。
     *
     * @param profile 待匹配的人员画像
     * @param rule    包含匹配条件的规则
     * @return 如果画像符合所有条件则返回 true，否则返回 false
     */
    public boolean matches(final ConditionsJson profile, final MatchingCondition.MatchingRule rule) {
        if (rule == null || profile == null) {
            log.error("匹配规则或人员画像为空，无法进行匹配。");
            return false;
        }

        // 遍历所有普通条件
        final List<MatchingCondition.Condition> conditions = rule.getConditions();
        if (conditions != null) {
            for (final MatchingCondition.Condition condition : conditions) {
                if (!matchesSingleCondition(profile, condition)) {
                    log.debug("条件未匹配：{}", condition.getFieldName());
                    return false;
                }
            }
        }

        // 遍历所有区间条件
        final List<MatchingCondition.RangeCondition> rangeConditions = rule.getRangeConditions();
        if (rangeConditions != null) {
            for (final MatchingCondition.RangeCondition rangeCondition : rangeConditions) {
                if (!matchesRangeCondition(profile, rangeCondition)) {
                    log.debug("区间条件未匹配：{}", rangeCondition.getFieldName());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 获取所有条件的详细匹配结果。
     *
     * @param profile 待匹配的人员画像
     * @param rule    包含匹配条件的规则
     * @return 包含每个条件匹配结果的Map，键为字段名，值为匹配结果（true/false）
     */
    public Map<String, Boolean> getMatchDetails(final ConditionsJson profile, final MatchingCondition.MatchingRule rule) {
        final Map<String, Boolean> matchDetails = new HashMap<>();

        if (rule == null || profile == null) {
            log.error("匹配规则或人员画像为空，无法获取详细匹配信息。");
            return matchDetails;
        }

        // 遍历所有普通条件并记录结果
        final List<MatchingCondition.Condition> conditions = rule.getConditions();
        if (conditions != null) {
            for (final MatchingCondition.Condition condition : conditions) {
                final boolean isMatched = matchesSingleCondition(profile, condition);
                matchDetails.put(condition.getFieldName(), isMatched);
            }
        }

        // 遍历所有区间条件并记录结果
        final List<MatchingCondition.RangeCondition> rangeConditions = rule.getRangeConditions();
        if (rangeConditions != null) {
            for (final MatchingCondition.RangeCondition rangeCondition : rangeConditions) {
                final boolean isMatched = matchesRangeCondition(profile, rangeCondition);
                matchDetails.put(rangeCondition.getFieldName(), isMatched);
            }
        }
        return matchDetails;
    }

    /**
     * 匹配单个条件
     */
    private boolean matchesSingleCondition(final ConditionsJson profile, final MatchingCondition.Condition condition) {
        try {
            final Field field = ConditionsJson.class.getDeclaredField(condition.getFieldName());
            field.setAccessible(true);
            final Object profileValue = field.get(profile);
            final String valueToMatch = condition.getValue();
            final String operator = condition.getOperator();

            if (profileValue == null || valueToMatch == null) {
                log.error("画像或规则的值为空，跳过匹配。");
                return false;
            }

            switch (operator) {
                case "=":
                    return profileValue.toString().equals(valueToMatch);
                case ">":
                    try {
                        final BigDecimal actualValue = new BigDecimal(profileValue.toString());
                        final BigDecimal requiredValue = new BigDecimal(valueToMatch);
                        return actualValue.compareTo(requiredValue) > 0;
                    } catch (NumberFormatException e) {
                        log.error("无法将值转换为数字进行 '>' 比较: {}", e.getMessage());
                        return false;
                    }
                case "<":
                    try {
                        final BigDecimal actualValue = new BigDecimal(profileValue.toString());
                        final BigDecimal requiredValue = new BigDecimal(valueToMatch);
                        return actualValue.compareTo(requiredValue) < 0;
                    } catch (NumberFormatException e) {
                        log.error("无法将值转换为数字进行 '<' 比较: {}", e.getMessage());
                        return false;
                    }
                case "in":
                    final List<String> valuesInList = Arrays.asList(valueToMatch.split(","));
                    return valuesInList.contains(profileValue.toString());
                default:
                    log.error("不支持的操作符: {}", operator);
                    return false;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("匹配条件字段不存在或访问失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 匹配区间条件
     */
    private boolean matchesRangeCondition(final ConditionsJson profile, final MatchingCondition.RangeCondition condition) {
        try {
            final Field field = ConditionsJson.class.getDeclaredField(condition.getFieldName());
            field.setAccessible(true);
            final Object profileValue = field.get(profile);

            if (!(profileValue instanceof String)) {
                log.error("匹配失败：字段 '{}' 的画像值不是字符串类型，而是 '{}'。",
                        condition.getFieldName(), profileValue != null ? profileValue.getClass().getSimpleName() : "null");
                return false;
            }
            final String actualValueStr = (String) profileValue;
            final String minValueStr = condition.getMin();
            final String maxValueStr = condition.getMax();

            try {
                final BigDecimal actualValue = new BigDecimal(actualValueStr);
                final BigDecimal minValue = new BigDecimal(minValueStr);
                final BigDecimal maxValue = new BigDecimal(maxValueStr);

                // 检查是否在 [min, max] 区间内
                return actualValue.compareTo(minValue) >= 0 && actualValue.compareTo(maxValue) <= 0;

            } catch (NumberFormatException e) {
                log.error("匹配失败：字段 '{}' 的值无法转换为数字类型。画像值='{}', 最小值='{}', 最大值='{}'",
                        condition.getFieldName(), actualValueStr, minValueStr, maxValueStr, e);
                return false;
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("匹配失败：无法通过反射访问字段 '{}'。错误信息：{}",
                    condition.getFieldName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将 ConditionsJson 对象的数据转换为 MatchingRule 规则组。
     * 该方法使用反射动态解析字段，并根据外部配置设置匹配操作符。
     *
     * @param conditionsJson 包含规则数据的 ConditionsJson 对象
     * @return 转换后的 MatchingRule 对象
     */
    public MatchingCondition.MatchingRule createMatchingRuleFromConditionsJson(ConditionsJson conditionsJson) {
        MatchingCondition.MatchingRule matchingRule = new MatchingCondition.MatchingRule();

        // 创建普通条件列表
        List<MatchingCondition.Condition> conditions = new ArrayList<>();
        // 创建区间条件列表
        List<MatchingCondition.RangeCondition> rangeConditions = new ArrayList<>();

        // 1. 使用 Java 反射动态获取所有字段
        Field[] fields = ConditionsJson.class.getDeclaredFields();
        for (Field field : fields) {
            // 确保可以访问私有字段
            field.setAccessible(true);
            try {
                Object value = field.get(conditionsJson);
                // 仅处理有值的字段
                if (value != null) {
                    String fieldName = field.getName();
                    String stringValue = value.toString();
                    // 2. 根据字段名从注入的配置中获取匹配操作符
                    Map<String, String> fieldOperatorMap = profileMatcherProperties.getRules();
                    String operator = fieldOperatorMap.get(fieldName);
                    if ("range".equals(operator)) {
                        // 如果操作符是 "range"，则按区间条件处理
                        if (!stringValue.isEmpty()) {
                            String[] parts = stringValue.split("-");
                            if (parts.length == 2) {
                                rangeConditions.add(new MatchingCondition.RangeCondition(fieldName, parts[0], parts[1]));
                            } else {
                                log.error("区间格式不正确，无法转换为规则: {}", stringValue);
                            }
                        }
                    } else if (operator != null) {
                        // 对于其他在配置中定义的字段，直接创建普通条件
                        conditions.add(new MatchingCondition.Condition(fieldName, operator, stringValue));
                    }
                }
            } catch (IllegalAccessException e) {
                log.error("访问字段时出错: {}", field.getName(), e);
            }
        }

        matchingRule.setConditions(conditions);
        matchingRule.setRangeConditions(rangeConditions);

        return matchingRule;
    }
}