package org.jeecg.modules.rule.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.rule.entity.LessonPlateauRules;
import org.jeecg.modules.rule.param.MultiPlateauRuleParam;

import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 高原补偿规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
public interface ILessonPlateauRulesService extends IService<LessonPlateauRules> {

    List<LessonPlateauRules> saveOrUpdateBatchMultiple(MultiPlateauRuleParam param);
}
