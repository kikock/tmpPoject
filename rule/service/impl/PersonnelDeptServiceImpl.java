package org.jeecg.modules.rule.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.excel.progress.ProgressService;
import org.jeecg.modules.rule.entity.PersonnelDept;
import org.jeecg.modules.rule.entity.PersonnelProfile;
import org.jeecg.modules.rule.entity.domain.DeptScoreParamVo;
import org.jeecg.modules.rule.entity.domain.PlanDeptScoreVo;
import org.jeecg.modules.rule.entity.domain.RuleLogicBlock;
import org.jeecg.modules.rule.entity.enums.EvaluationType;
import org.jeecg.modules.rule.entity.enums.LevelScoreEnum;
import org.jeecg.modules.rule.entity.excel.PersonnelDeptExcelData;
import org.jeecg.modules.rule.factory.RuleManager;
import org.jeecg.modules.rule.mapper.PersonnelDeptMapper;
import org.jeecg.modules.rule.service.IPersonnelDeptService;
import org.jeecg.modules.rule.utils.GradeLevel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @project_name: 后端平台项目
 * @description: 单位总评规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Service
@Log4j2
public class PersonnelDeptServiceImpl extends ServiceImpl<PersonnelDeptMapper, PersonnelDept> implements IPersonnelDeptService {


    @Resource
    private RuleManager ruleManager;
    @Resource
    private ProgressService progressService;

    @Override
    public PlanDeptScoreVo getDeptScore(DeptScoreParamVo deptScoreParamVo) {
        // 1. 参数校验
        if (StrUtil.isBlank(deptScoreParamVo.getId())) {
            throw new JeecgBootException("单位评定规则ID不能为空");
        }
        List<GradeLevel> gradeLevels = deptScoreParamVo.getGradeLevels();
        if (CollUtil.isEmpty(gradeLevels)) {
            throw new JeecgBootException("单位评定数据不能为空");
        }
        String id = deptScoreParamVo.getId();
        // 2. 获取单位总评规则
        PersonnelDept personnelDept = this.getById(id);
        if (personnelDept == null) {
            throw new JeecgBootException("未找到对应的单位总评规则");
        }
        // 7. 构造并返回结果
        PlanDeptScoreVo result = new PlanDeptScoreVo();
        String subjectScoresJson = JSONObject.toJSONString(gradeLevels);
        PersonnelProfile profile = new PersonnelProfile();
        profile.setName(personnelDept.getName());
        profile.setRuleLogic(personnelDept.getRuleLogic());
        String finalGrade = ruleManager.calculate(profile, subjectScoresJson, EvaluationType.COMPOUND_CONDITION_GRADING);
        result.setGrade(finalGrade);
        return result;
    }

    @Override
    public Map<Integer, String> validateData(PersonnelDeptExcelData row) {
        // 步骤 1: 创建导入行校验数据结果消息
        Map<Integer, String> errorMap = new HashMap<>();
        Integer rowIndex = row.getRowIndex();
        // 校验：当评等形式为“二级制”时，评定等级必须为“及格”
        if (StrUtil.equals("二级制", row.getRuleType()) && !StrUtil.equals("及格", row.getGrade())) {
            errorMap.put(rowIndex, "评等形式为二级制时，评定等级只能为及格");
        }
        return errorMap;
    }

    @Override
    public Map<Integer, String> saveBatchImport(List<PersonnelDeptExcelData> cachedDataList) {
        // 步骤 1: 创建导入数据保存结果消息
        Map<Integer, String> errorMap = new HashMap<>();

        // 步骤 2: 按规则名称分组
        Map<String, List<PersonnelDeptExcelData>> groupedByRuleName = cachedDataList.stream()
                .filter(row -> StrUtil.isNotBlank(row.getRuleName()))
                .collect(Collectors.groupingBy(PersonnelDeptExcelData::getRuleName));

        // 步骤 3: 遍历每个分组，进行校验和数据转换
        for (Map.Entry<String, List<PersonnelDeptExcelData>> entry : groupedByRuleName.entrySet()) {
            String ruleName = entry.getKey();
            List<PersonnelDeptExcelData> groupData = entry.getValue();
            // 3.1 校验：一个规则名称只能存在一个评等形式
            long distinctRuleTypes = groupData.stream().map(PersonnelDeptExcelData::getRuleType).distinct().count();
            if (distinctRuleTypes > 1) {
                String errorMsg = "规则名称【" + ruleName + "】存在多种评等形式，请保持一致。";
                groupData.forEach(row -> errorMap.put(row.getRowIndex(), errorMsg));
                continue;
            }
            // 3.2 校验通过，开始转换数据
            try {
                PersonnelDept personnelDept = new PersonnelDept();
                personnelDept.setName(ruleName);
                // 使用分组中第一个的评等形式
                personnelDept.setRuleType(groupData.get(0).getRuleType());
                // 3.3 将组内多行数据转换为规则逻辑
                /*
[{"type":"compound_condition","evaluationGrade":"quaternary_rading","enabled":true,"assessment_info":[{"grade":"excellent","sort":3,"conditions":[{"subject":"participation_rate","name":"参考率","value":60},{"subject":"excellent_rate","name":"优秀率","value":75},{"subject":"good_rate","name":"良好率","value":14},{"subject":"pass_rate","name":"及格率","value":100},{"subject":"military_commander_grade","name":"军事主官成绩","value":"excellent","evaluationGrade":"quaternary_rading"},{"subject":"political_commissar_grade","name":"政治主管成绩","value":"excellent","evaluationGrade":"quaternary_rading"}]},{"grade":"good","sort":2,"conditions":[{"subject":"participation_rate","name":"参考率","value":51},{"subject":"excellent_rate","name":"优秀率","value":60},{"subject":"good_rate","name":"良好率","value":45},{"subject":"pass_rate","name":"及格率","value":45},{"subject":"military_commander_grade","name":"军事主官成绩","value":"good","evaluationGrade":"quaternary_rading"},{"subject":"political_commissar_grade","name":"政治主管成绩","value":"good","evaluationGrade":"quaternary_rading"}]},{"grade":"pass","sort":1,"conditions":[{"subject":"participation_rate","name":"参考率","value":42},{"subject":"excellent_rate","name":"优秀率","value":50},{"subject":"good_rate","name":"良好率","value":19},{"subject":"pass_rate","name":"及格率","value":3},{"subject":"military_commander_grade","name":"军事主官成绩","value":"pass","evaluationGrade":"quaternary_rading"},{"subject":"political_commissar_grade","name":"政治主管成绩","value":"pass","evaluationGrade":"quaternary_rading"}]}]},{"type":"bmi","evaluationGrade":"binary_grading","enabled":true,"assessment_info":null}]
                */
                List<RuleLogicBlock> ruleLogicBlocks = new ArrayList<>();
                // 根据要求，默认添加一个BMI规则块
                RuleLogicBlock bmiBlock = new RuleLogicBlock();
                bmiBlock.setType(EvaluationType.BMI_GRADING.getCode());
                bmiBlock.setEvaluationGrade("binary_grading");
                bmiBlock.setEnabled(true);
                bmiBlock.setAssessment_info(null);
                ruleLogicBlocks.add(bmiBlock);

                // 3.3.2 创建并添加综合评等规则块
                RuleLogicBlock compoundBlock = new RuleLogicBlock();
                compoundBlock.setType(EvaluationType.COMPOUND_CONDITION_GRADING.getCode());
                compoundBlock.setEnabled(true);
                // 根据评等形式设置 evaluationGrade
                String ruleType = groupData.get(0).getRuleType();
                compoundBlock.setEvaluationGrade("二级制".equals(ruleType) ? "binary_grading" : "quaternary_rading");
                // 遍历 groupData，构建 assessment_info

                List<JSONObject> assessmentInfoList = groupData.stream().map(row -> {
                    JSONObject assessmentItem = new JSONObject();
                    assessmentItem.put("grade", row.getGrade());
                    assessmentItem.put("sort", LevelScoreEnum.fromCode(row.getGrade()).getScore());
                    List<JSONObject> conditions = new ArrayList<>();
                    conditions.add(buildCondition("participation_rate", "参考率", row.getParticipationRate()));
                    conditions.add(buildCondition("excellent_rate", "优秀率", row.getExcellentRate()));
                    conditions.add(buildCondition("good_rate", "良好率", row.getGoodRate()));
                    conditions.add(buildCondition("pass_rate", "及格率", row.getPassRate()));
                    conditions.add(buildCondition("military_commander_grade", "军事主官成绩", row.getMilitaryChiefScore(), "quaternary_rading"));
                    conditions.add(buildCondition("political_commissar_grade", "政治主管成绩", row.getParticipationRate(), "quaternary_rading"));
                    assessmentItem.put("conditions", conditions);
                    return assessmentItem;
                }).collect(Collectors.toList());
                compoundBlock.setAssessment_info(new JSONArray(assessmentInfoList));
                ruleLogicBlocks.add(compoundBlock);
                personnelDept.setRuleLogic(ruleLogicBlocks);
                // 3.4 检查数据库中是否已存在同名规则
                QueryWrapper<PersonnelDept> queryWrapper = new QueryWrapper<PersonnelDept>().eq("name", ruleName);
                PersonnelDept existingDept = this.getOne(queryWrapper);
                if (existingDept != null) {
                    personnelDept.setId(existingDept.getId());
                }
                this.saveOrUpdate(personnelDept);
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                groupData.forEach(row -> errorMap.put(row.getRowIndex(), "规则保存失败: " + errorMessage));
            }
        }
        return errorMap;
    }

    /**
     * 辅助方法：构建单个条件对象
     */
    private JSONObject buildCondition(String subject, String name, Object value) {
        JSONObject condition = new JSONObject();
        condition.put("subject", subject);
        condition.put("name", name);
        condition.put("value", value);
        return condition;
    }

    /**
     * 辅助方法：构建带 evaluationGrade 的条件对象
     */
    private JSONObject buildCondition(String subject, String name, Object value, String evaluationGrade) {
        JSONObject condition = new JSONObject();
        condition.put("subject", subject);
        condition.put("name", name);
        condition.put("value", value);
        condition.put("evaluationGrade", evaluationGrade);
        return condition;
    }


    @Override
    public boolean save(PersonnelDept entity) {
        this.checkUniqueness(entity);
        return super.save(entity);
    }

    @Override
    public boolean update(PersonnelDept entity, Wrapper<PersonnelDept> updateWrapper) {
        this.checkUniqueness(entity);
        return super.update(entity, updateWrapper);
    }

    @Override
    public boolean saveOrUpdate(PersonnelDept entity) {
        this.checkUniqueness(entity);
        return super.saveOrUpdate(entity);
    }

    /**
     * 校验规则名称或内容是否重复
     *
     * @param entity 待校验的实体
     */
    private void checkUniqueness(PersonnelDept entity) {
        if (entity == null || StrUtil.isBlank(entity.getName())) {
            return;
        }
        QueryWrapper<PersonnelDept> queryWrapper = new QueryWrapper<>();
        // [修改] 只校验名称不能重复
        queryWrapper.eq("name", entity.getName());

        // 如果是编辑操作，需要排除自身
        if (StrUtil.isNotBlank(entity.getId())) {
            queryWrapper.ne("id", entity.getId());
        }

        if (this.count(queryWrapper) > 0) {
            throw new JeecgBootException("规则名称已存在，请勿重复添加！");
        }
    }
}
