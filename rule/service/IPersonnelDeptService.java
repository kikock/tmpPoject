package org.jeecg.modules.rule.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.rule.entity.PersonnelDept;
import org.jeecg.modules.rule.entity.domain.DeptScoreParamVo;
import org.jeecg.modules.rule.entity.domain.PlanDeptScoreVo;
import org.jeecg.modules.rule.entity.excel.PersonnelDeptExcelData;

import java.util.List;
import java.util.Map;


/**
 * @project_name: 后端平台项目
 * @description: 单位总评规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
public interface IPersonnelDeptService extends IService<PersonnelDept> {

    PlanDeptScoreVo getDeptScore(DeptScoreParamVo deptScoreParamVo);

    Map<Integer, String> validateData(PersonnelDeptExcelData row);

    Map<Integer, String> saveBatchImport(List<PersonnelDeptExcelData> cachedDataList);
}
