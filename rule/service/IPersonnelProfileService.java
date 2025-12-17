package org.jeecg.modules.rule.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.entity.domain.PlanUserScoreVo;
import org.jeecg.modules.rule.entity.param.UserScoreParamVo;


/**
 * @project_name: 后端平台项目
 * @description: 个人成绩规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
public interface IPersonnelProfileService extends IService<PersonnelProfile> {

    /**
     * 根据用户ID和计划ID计算总成绩
     *
     * @return 最终的成绩对象
     */
    PlanUserScoreVo getUserScore(UserScoreParamVo param);
}
