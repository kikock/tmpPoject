package org.jeecg.modules.rule.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @project_name: 后端平台项目
 * @description: 批量添加高原补偿规则参数
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
public class MultiPlateauRuleParam {

    @NotNull(message = "课目id不能为空")
    private String lessonId;

    @NotNull(message = "高原规则明细列表不能为空")
    private List<PlateauRuleParam> plateauRuleParams;

}