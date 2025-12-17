package org.jeecg.modules.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.rule.entity.LessonGradingRules;
import org.jeecg.modules.rule.entity.LessonRule;
import org.jeecg.modules.rule.entity.domain.*;
import org.jeecg.modules.rule.entity.excel.LessonRulesExcelData;
import org.jeecg.modules.rule.entity.param.LessonScoreParamVo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * @project_name: 后端平台项目
 * @description: 用户画像
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
public interface IUserProfilesService extends IService<UserProfiles> {

    IPage<LessonRule> pageLessonRuleList(Page<LessonRule> page, QueryWrapper<LessonRule> queryWrapper);

    LessonRule getByLessonId(String id);

    LessonRuleDetailVo getLessonRuleDetail(String lessonId);

    Boolean saveLessonRuleDetail(LessonRuleDetailVo lessonRuleDetailVo);

    boolean deleteUserProfileById(String id);

    /**
     * 为指定课目生成一个新的、唯一的顺序规则名称。
     *
     * @param lessonId 课目ID
     * @return 生成的规则名称，例如 "基础规则_20251010_#1"
     */
    String generateNewRuleName(String lessonId);

    /**
     * 在删除规则后，对指定课目下的顺序命名规则进行重新排序。
     *
     * @param lessonId 课目ID
     */
    void reorderRuleNames(String lessonId);

    /**
     * 根据规则ID获取所有年龄段的文字描述
     *
     * @param userProfileId 顶层规则ID
     * @return 描述列表
     */
    List<RuleDescriptionVo> getRuleDescriptions(String userProfileId);

    /**
     * 根据规则ID和年龄段规则ID获取单个年龄段的文字描述
     *
     * @param userProfileId 顶层规则ID
     * @param ruleId        年龄段规则ID
     * @return 单个描述对象
     */
    RuleDescriptionVo getRuleDescription(String userProfileId, String ruleId);

    /**
     * 根据用户ID获取其用于规则匹配的所有关键属性。
     *
     * @param id 用户ID或身份证号码
     * @return 包含用户年龄、性别、人员类别、海拔等信息的属性对象
     */
    UserMatchingAttributes getUserMatchingAttributes(String id);

    /**
     * 根据用户ID和课目ID，获取适配该用户的评分和评等规则集合。
     *
     * @param userAttributes 用户匹配参数
     * @param lessonId       课目ID
     * @return 包含评分和评等规则的集合对象。如果找不到任何适配规则，则返回一个空对象或null。
     */
    AdaptedLessonRulesVo getAdaptedRulesForUser(UserMatchingAttributes userAttributes, String lessonId);

    /**
     * 根据适配的规则和用户的原始成绩，计算最终得分或等级，并填充回对象中。
     *
     * @param adaptedRules   通过 getAdaptedRulesForUser 获取到的适配规则对象
     * @param rawPerformance 用户的原始成绩 (例如 "100"秒 或 "50"个)
     * @return 填充了计算结果的 AdaptedLessonRulesVo 对象
     */
    AdaptedLessonRulesVo calculateAdaptedRules(AdaptedLessonRulesVo adaptedRules, String rawPerformance);

    /**
     * 单课目评分(不含课目组)
     *
     * @param param 课目(组)成绩计算参数VO
     * @return 填充了计算结果的 AdaptedLessonRulesVo 对象
     */
    LessonScoreVo getLessonScore(LessonScoreParamVo param);

    /**
     * 课目组规则翻译
     *
     * @return List<RuleLogicBlock> 对象
     */
    List<RuleLogicBlock> convertGradingRuleToLogicBlock(LessonGradingRules gradingRule);

    /**
     * 规则模板下载
     *
     */
    void getTemplate(HttpServletResponse response) throws IOException;

    /**
     * 模板导入保存数据 ---保存结果放入第一行错误信息
     *
     */
    Map<Integer, String> saveBatchImport(List<LessonRulesExcelData> cachedDataList);

    /**
     * 模板导入保存数据行格式校验
     *
     */
    Map<Integer, String> validateData(LessonRulesExcelData row);
}
