package org.jeecg.modules.rule.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.rule.entity.LessonPlateauRules;
import org.jeecg.modules.rule.entity.LessonRule;
import org.jeecg.modules.rule.entity.domain.*;
import org.jeecg.modules.rule.entity.param.LessonScoreParamVo;
import org.jeecg.modules.rule.entity.param.UserScoreParamVo;
import org.jeecg.modules.rule.param.MultiPlateauRuleParam;
import org.jeecg.modules.rule.service.ILessonPlateauRulesService;
import org.jeecg.modules.rule.service.IPersonnelDeptService;
import org.jeecg.modules.rule.service.IPersonnelProfileService;
import org.jeecg.modules.rule.service.IUserProfilesService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @project_name: 后端平台项目
 * @description: 课目规则接口
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@RestController
@RequestMapping("/lessonRule")
@Log4j2
public class LessonRuleController {


    @Resource
    private IUserProfilesService userProfilesService;

    @Resource
    private ILessonPlateauRulesService lessonPlateauRulesService;

    @Resource
    private IPersonnelProfileService personnelProfileService;

    @Resource
    private IPersonnelDeptService personnelDeptService;

    /**
     * 分页课目规则列表
     *
     * @param param 课目参数
     * @return 分页课目列表
     */
    @GetMapping({"/list"})
    @AutoDict
    public Result<IPage<LessonRule>> queryPageList(LessonRule param, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<LessonRule> queryWrapper = QueryGenerator.initQueryWrapper(param, req.getParameterMap());
        Page<LessonRule> page = new Page<>(pageNo, pageSize);
        IPage<LessonRule> pageList = userProfilesService.pageLessonRuleList(page, queryWrapper);
        //循环 查询海拔区间 值用逗号分割
        pageList.getRecords().forEach(lesson -> {
            List<LessonPlateauRules> list = lessonPlateauRulesService.list(new QueryWrapper<LessonPlateauRules>().eq("lesson_id", lesson.getId()));
//            最小区间-最大区间
            List<String> texts = list.stream().map(item -> item.getMinAltitude() + "-" + item.getMaxAltitude()).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(texts)) {
                lesson.setPlateauText(CollUtil.join(texts, ","));
            }
        });
        return Result.ok(pageList);
    }

    /**
     * 分页课目配置高原规则列表
     *
     * @param param 高原配置参数
     * @return 高原规则列表
     */
    @GetMapping({"/plateau/list"})
    @AutoDict
    public Result<IPage<LessonPlateauRules>> queryPagePlateauList(LessonPlateauRules param, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<LessonPlateauRules> queryWrapper = QueryGenerator.initQueryWrapper(param, req.getParameterMap());
        Page<LessonPlateauRules> page = new Page<>(pageNo, pageSize);
        IPage<LessonPlateauRules> pageList = lessonPlateauRulesService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * 分页课目配置高原规则列表
     *
     * @param param 高原配置参数
     * @return 高原规则列表
     */
    @GetMapping({"/plateau/allList"})
    @AutoDict
    public Result<List<LessonPlateauRules>> queryPagePlateauList(LessonPlateauRules param, HttpServletRequest req) {
        QueryWrapper<LessonPlateauRules> queryWrapper = QueryGenerator.initQueryWrapper(param, req.getParameterMap());
        List<LessonPlateauRules> list = lessonPlateauRulesService.list(queryWrapper);
        return Result.ok(list);
    }

    /**
     * 批量添加或更新高原补偿规则
     *
     * @param param 参数
     * @return 批量添加或更新结果
     */
    @PostMapping(value = "/plateau/saveOrUpdate")
    public Result<List<LessonPlateauRules>> add(@RequestBody MultiPlateauRuleParam param) {
        return Result.OK(lessonPlateauRulesService.saveOrUpdateBatchMultiple(param));
    }


    /**
     * 通过id查询课目
     *
     * @param id 课目id
     * @return 课目信息
     */
    @GetMapping(value = "/queryById")
    @AutoDict
    public Result<LessonRule> queryById(@RequestParam(name = "id") String id) {
        LessonRule lesson = userProfilesService.getByLessonId(id);
        if (lesson == null) {
            return Result.error("未找到课目数据");
        }
        return Result.OK(lesson);
    }

    /**
     * 根据课目ID查询用户画像评分/评等规则
     *
     * @param id 画像id
     * @return 用户画像评分/评等规则详情
     */
    @GetMapping(value = "/getLessonRuleDetail")
    @AutoDict
    public Result<LessonRuleDetailVo> getLessonRuleDetail(@RequestParam(name = "id") String id) {
        try {
            LessonRuleDetailVo ruleDetail = userProfilesService.getLessonRuleDetail(id);
            if (ruleDetail == null) {
                return Result.error("未找到课目规则数据");
            }
            return Result.OK(ruleDetail);
        } catch (Exception e) {
            log.error("查询用户画像评分规则失败，规则id: {}", id, e);
            return Result.error("查询失败");
        }
    }

    /**
     * 添加课目规则
     *
     * @param lessonRuleDetailVo 规则vo
     * @return 保存成功
     */
    @AutoLog(value = "添加课目规则", operateType = 1)
    @PostMapping(value = "/saveLessonRuleDetail")
    @AutoDict
    public Result<Boolean> saveLessonRuleDetail(@RequestBody LessonRuleDetailVo lessonRuleDetailVo) {
        return Result.OK("保存课目评分规则成功", userProfilesService.saveLessonRuleDetail(lessonRuleDetailVo));
    }

    /**
     * 根据课目ID删除规则
     *
     * @return 保存成功
     */
    @AutoLog(value = "删除课目规则", operateType = 4)
    @DeleteMapping({"/deleteOne"})
    public Result<Boolean> delete(@RequestParam(name = "id") String id) {
        boolean res = userProfilesService.deleteUserProfileById(id);
        return Result.OK(res);
    }

    /**
     * 课目(组)成绩接口
     *
     * @param param 包含用户ID、课目组ID和各子课目成绩的参数对象
     * @return 课目组的最终成绩
     */
    @AutoLog(value = "计算课目成绩", operateType = 1)
    @PostMapping("/lessonGroupScore")
    public Result<LessonScoreVo> getLessonScore(@RequestBody LessonScoreParamVo param) {
        // 调用 service 层的方法来执行实际的计算逻辑
        LessonScoreVo lessonScoreVo = userProfilesService.getLessonScore(param);
        return Result.OK(lessonScoreVo);
    }

    /**
     * 个人计划成绩接口
     *
     * @return 保存成功
     */
    @AutoLog(value = "计算个人总评成绩", operateType = 1)
    @GetMapping("/getUserScore")
    public Result<PlanUserScoreVo> getUserScore(@RequestBody UserScoreParamVo param) {
        PlanUserScoreVo planUserScoreVo = personnelProfileService.getUserScore(param);
        return Result.OK(planUserScoreVo);
    }

    /**
     * 单位成绩评定接口
     *
     */
    @AutoLog(value = "计算单位总评成绩", operateType = 1)
    @PostMapping("/getDeptScore")
    public Result<PlanDeptScoreVo> getDeptScore(@RequestBody DeptScoreParamVo param) {
        PlanDeptScoreVo planDeptScoreVo = personnelDeptService.getDeptScore(param);
        return Result.OK(planDeptScoreVo);
    }
}
