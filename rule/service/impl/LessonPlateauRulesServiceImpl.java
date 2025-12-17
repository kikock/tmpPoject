package org.jeecg.modules.rule.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.entity.LessonPlateauRules;
import org.jeecg.modules.rule.mapper.LessonPlateauRulesMapper;
import org.jeecg.modules.rule.param.MultiPlateauRuleParam;
import org.jeecg.modules.rule.param.PlateauRuleParam;
import org.jeecg.modules.rule.service.ILessonPlateauRulesService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;


/**
 * @project_name: 后端平台项目
 * @description: 高原补偿规则
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Service
@Log4j2
public class LessonPlateauRulesServiceImpl extends ServiceImpl<LessonPlateauRulesMapper, LessonPlateauRules> implements ILessonPlateauRulesService {



    @Lazy
    @Resource
    private ILessonPlateauRulesService self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<LessonPlateauRules> saveOrUpdateBatchMultiple(MultiPlateauRuleParam param) {
        String lessonId = param.getLessonId();
//  TODO      校验课目是否存在
//        if (Objects.isNull(lesson)) {
//            // 如果不存在，抛出异常或进行相应的错误处理
//            throw new JeecgBootException("高原规则适用课目不存在,请确认课目是否创建!");
//        }
        List<PlateauRuleParam> plateauRuleParams = param.getPlateauRuleParams();
//        校验海拔区间不能重复
        validateAltitudeRanges(plateauRuleParams, lessonId);

        // 1. 首先，删除该课目 ID 下所有现有的规则
        self.remove(new LambdaQueryWrapper<LessonPlateauRules>()
                .eq(LessonPlateauRules::getLessonId, lessonId));
        // 2. 然后，将新的规则保存到数据库中
        List<LessonPlateauRules> lessonPlateauRulesList = BeanUtil.copyToList(plateauRuleParams, LessonPlateauRules.class);
        if (CollUtil.isEmpty(lessonPlateauRulesList)) {
            return lessonPlateauRulesList;
        }
        boolean success = self.saveOrUpdateBatch(lessonPlateauRulesList);
        if (success) {
            return lessonPlateauRulesList;
        } else {
            throw new JeecgBootException("高原规则保存失败!");
        }
    }

    /**
     * 校验 PlateauRuleParam 对象列表中的海拔区间是否重叠，并为每个规则设置名称。
     * 同时，校验每个规则中最低海拔是否小于或等于最高海拔。
     *
     * @param params   需要校验和设置名称的 PlateauRuleParam 对象列表。
     * @param lessonId 课目id
     */
    private void validateAltitudeRanges(List<PlateauRuleParam> params, String lessonId) {
        if (params == null || params.isEmpty()) {
            return;
        }
        // 先按最小海拔排序，便于后续处理
        params.sort(Comparator.comparing(PlateauRuleParam::getMinAltitude));
        // 首先，校验每个单独的规则是否具有有效的海拔区间
        for (PlateauRuleParam param : params) {
            // 添加最小海拔值校验（最小值为1500）
            if (param.getMinAltitude() < 1501) {
                String errorMessage = String.format("最低海拔不能小于1500米，当前设置为%d米!", param.getMinAltitude());
                throw new JeecgBootException(errorMessage);
            }

            if (param.getMinAltitude() > param.getMaxAltitude()) {
                String errorMessage = String.format("海拔区间设置有误，最低海拔(%d)不能大于最高海拔(%d)!",
                        param.getMinAltitude(), param.getMaxAltitude());
                throw new JeecgBootException(errorMessage);
            }
            if (param.getChangeNum() <= 0 || param.getChangeValue() <= 0) {
                throw new JeecgBootException("“高度每增加”的值和“标准增加/减少”的变动值都必须大于0！");
            }
        }

        // 按最低海拔对列表进行排序，以简化重叠检查
        params.sort(Comparator.comparing(PlateauRuleParam::getMinAltitude));

        for (int i = 0; i < params.size(); i++) {
            PlateauRuleParam current = params.get(i);
            // 检查与排序列表中下一个元素的重叠情况（如果存在）
            if (i < params.size() - 1) {
                PlateauRuleParam next = params.get(i + 1);
                if (next.getMinAltitude() <= current.getMaxAltitude()) {
                    String errorMessage = String.format("海拔区间存在重复，冲突区间为: [%d, %d] 和 [%d, %d]",
                            current.getMinAltitude(), current.getMaxAltitude(),
                            next.getMinAltitude(), next.getMaxAltitude());
                    throw new JeecgBootException(errorMessage);
                }
            }

            // 为当前对象设置规则名称
            String ruleName = String.format("高原补偿规则(%d-%d)", current.getMinAltitude(), current.getMaxAltitude());
            current.setRuleName(ruleName);
            current.setLessonId(lessonId);
        }
    }
}