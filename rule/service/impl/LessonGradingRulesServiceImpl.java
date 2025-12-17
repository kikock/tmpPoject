package org.jeecg.modules.rule.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.jeecg.modules.rule.entity.LessonGradingRules;
import org.jeecg.modules.rule.mapper.LessonGradingRulesMapper;
import org.jeecg.modules.rule.service.ILessonGradingRulesService;
import org.springframework.stereotype.Service;


/**
 * @project_name: 后端平台项目
 * @description: 课目评等规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Service
@Log4j2
public class LessonGradingRulesServiceImpl extends ServiceImpl<LessonGradingRulesMapper, LessonGradingRules> implements ILessonGradingRulesService {

}
