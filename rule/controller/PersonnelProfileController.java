package org.jeecg.modules.rule.controller;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.excel.ExcelTools;
import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.service.IPersonnelProfileService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @project_name: 后端平台项目
 * @description: 个人评定规则
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@RestController
@RequestMapping("/personnelProfile")
@Log4j2
public class PersonnelProfileController extends JeecgController<PersonnelProfile, IPersonnelProfileService> {

    @Resource
    private IPersonnelProfileService personnelProfileService;

    @Resource
    private ExcelTools excelTools;

    @AutoLog(value = "添加个人评定规则", operateType = 2)
    @PostMapping(value = "/add")
    public Result<Boolean> add(@RequestBody @Valid PersonnelProfile param) {
        boolean res = service.save(param);
        return Result.OK(res);
    }

    @AutoLog(value = "编辑个人评定规则", operateType = 3)
    @PutMapping(value = "/edit")
    public Result<Boolean> edit(@RequestBody @Valid PersonnelProfile param) {
        boolean res = service.saveOrUpdate(param);
        return Result.OK(res);
    }

    //根据 ruleUsers 人员类型查询
    @GetMapping(value = "/getRuleType")
    public Result<PersonnelProfile> getRuleType(@RequestParam(name = "type", required = true) String type) {
        List<PersonnelProfile> res = service.list(new QueryWrapper<PersonnelProfile>().eq("soldier_category", type));
        if (CollUtil.isNotEmpty(res)) {
            return Result.OK(res.get(0));
        }
        return Result.OK();
    }

    @PostMapping(value = "/saveUserRuleDetail")
    @AutoDict
    public Result<Boolean> saveUserRuleDetail(@RequestBody @Valid PersonnelProfile param) {
        List<PersonnelProfile> res = service.list(new QueryWrapper<PersonnelProfile>().eq("soldier_category", param.getSoldierCategory()));
        if (CollUtil.isNotEmpty(res)) {
            param.setId(res.get(0).getId());
        }
        boolean b = service.saveOrUpdate(param);
        return Result.OK("个人评定规则成功", b);
    }

}
