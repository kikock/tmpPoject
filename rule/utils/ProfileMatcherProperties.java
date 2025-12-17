package org.jeecg.modules.rule.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @project_name: 后端平台项目
 * @description: 规则匹配字段配置
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Component
@ConfigurationProperties(prefix = "profile.matcher")
@Data
public class ProfileMatcherProperties {

    /**
     * 通过 @Value 注解从配置文件中注入字段与操作符的映射关系。
     * 例如，在 application.yml 中配置:
     * profile:
     * matcher:
     * rules:
     * soldierCategory: "="
     * newSoldier: "="
     * gender: "="
     * altitudeRange: "range"
     */
    private Map<String, String> rules;

}