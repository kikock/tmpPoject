package org.jeecg.modules.rule.entity.domain;

import cn.hutool.json.JSONArray;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @project_name: 后端平台项目
 * @description: 评等规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
@Accessors(chain = true)
@ApiModel(description = "评等规则")
public class RawRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("score")
    private String score;
    @JsonProperty("performance")
    private String performance;
    @JsonProperty("minPerformance")
    private String minPerformance;
    @JsonProperty("maxPerformance")
    private String maxPerformance;
    @JsonProperty("evaluationName")
    private String evaluationName;
    @JsonProperty("evaluationCode")
    private String evaluationCode;
    @JsonProperty("evaluationScore")
    private String evaluationScore;
    @JsonProperty("scoreRanges")
    private String scoreRanges;
    @JsonProperty("levelData")
    private JSONArray levelData;

}