package org.jeecg.modules.rule.entity.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @project_name: 后端平台项目
 * @description: 区间成绩
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
public class LeveData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("label")
    private String label;
    @JsonProperty("value")
    private String value;
}