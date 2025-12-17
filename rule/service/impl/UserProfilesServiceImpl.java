package org.jeecg.modules.rule.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.rule.annotation.RawDataConverter;
import org.jeecg.modules.rule.annotation.TimeConverter;
import org.jeecg.modules.rule.entity.*;
import org.jeecg.modules.rule.entity.domain.*;
import org.jeecg.modules.rule.entity.enums.EvaluationType;
import org.jeecg.modules.rule.entity.excel.LessonRulesExcelData;
import org.jeecg.modules.rule.entity.param.LessonScoreParamVo;
import org.jeecg.modules.rule.factory.RuleManager;
import org.jeecg.modules.rule.handler.ExcelSelectSheetWriteHandler;
import org.jeecg.modules.rule.mapper.UserProfilesMapper;
import org.jeecg.modules.rule.service.ILessonGradingRulesService;
import org.jeecg.modules.rule.service.ILessonPlateauRulesService;
import org.jeecg.modules.rule.service.ILessonScoringRulesService;
import org.jeecg.modules.rule.service.IUserProfilesService;
import org.jeecg.modules.rule.strategy.CalculationStrategyFactory;
import org.jeecg.modules.rule.utils.GradeLevel;
import org.jeecg.modules.rule.utils.MatchingCondition;
import org.jeecg.modules.rule.utils.ProfileMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @project_name: 后端平台项目
 * @description: 用户画像
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Service
@Log4j2
public class UserProfilesServiceImpl extends ServiceImpl<UserProfilesMapper, UserProfiles> implements IUserProfilesService {
    private static final Map<String, Integer> gradeToScoreMap;
    @Resource
    private UserProfilesMapper userProfilesMapper;
    @Resource
    private CommonAPI commonAPI;
    @Resource
    private ILessonGradingRulesService lessonGradingRulesService;
    @Resource
    private ILessonScoringRulesService lessonScoringRulesService;
    @Resource
    private ILessonPlateauRulesService lessonPlateauRulesService;
    @Resource
    private CalculationStrategyFactory strategyFactory;
    @Resource
    private ProfileMatcher profileMatcher;
    @Resource
    private RuleManager ruleManager;
    @Resource
    private RawDataConverter rawDataConverter;


    //    正则匹配时间格式 mm:ss
    private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{1,2}:\\d{2}$");

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("special_one", 6);
        map.put("special_two", 5);
        map.put("special_three", 4);
        map.put("excellent", 3);
        map.put("good", 2);
        map.put("pass", 1);
        map.put("fail", 0);
        gradeToScoreMap = Collections.unmodifiableMap(map);
    }


    @Override
    public IPage<LessonRule> pageLessonRuleList(Page<LessonRule> page, QueryWrapper<LessonRule> queryWrapper) {
        return userProfilesMapper.pageLessonRuleList(page, queryWrapper);
    }

    @Override
    public LessonRule getByLessonId(String id) {
        return userProfilesMapper.getByLessonId(id);
    }

    @Override
    public LessonRuleDetailVo getLessonRuleDetail(String id) {
        // 查询用户画像信息
        UserProfiles userProfiles = this.getOne(new QueryWrapper<UserProfiles>().eq("id", id));
        if (userProfiles == null) {
            return null;
        }
        // 查询评分规则
        List<LessonGradingRules> lessonGradingRulesList = lessonGradingRulesService.list(new QueryWrapper<LessonGradingRules>().eq("user_profile_id", id));
        // 查询等级规则
        List<LessonScoringRules> lessonScoringRulesList = lessonScoringRulesService.list(new QueryWrapper<LessonScoringRules>().eq("user_profile_id", id));
        // 构造返回VO对象
        LessonRuleDetailVo detailVo = new LessonRuleDetailVo();
        detailVo.setUserProfiles(userProfiles);
        detailVo.setLessonGradingRules(lessonGradingRulesList);
        detailVo.setLessonScoringRules(lessonScoringRulesList);
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveLessonRuleDetail(LessonRuleDetailVo lessonRuleDetailVo) {
        // 前置校验
        validateLessonRuleDetail(lessonRuleDetailVo);
        // 校验通过后，可以安全地获取数据并执行业务逻辑
        UserProfiles userProfiles = lessonRuleDetailVo.getUserProfiles();
        boolean isUpdate = true;
        if (StringUtils.isBlank(userProfiles.getId())) {
            //新增规则--名称
            String name = generateNewRuleName(userProfiles.getLessonId());
            userProfiles.setName(name);
            isUpdate = false;
        }
        if (StrUtil.equals(userProfiles.getConditionsJson().getAltitudeType(), "0")) {
            userProfiles.getConditionsJson().setAltitude("0-1500");
        }
        this.saveOrUpdate(userProfiles);
        String userProfileId = userProfiles.getId();
        //处理并保存评分规则
        saveScoringRules(userProfileId, lessonRuleDetailVo.getLessonScoringRules(), isUpdate);
        //处理并保存等级规则
        saveGradingRules(userProfileId, lessonRuleDetailVo.getLessonGradingRules(), isUpdate);
        return true;
    }

    @Override
    @Transactional // 2. 在方法上添加此注解
    public boolean deleteUserProfileById(String id) {
        UserProfiles userProfiles = this.getById(id);
        // 1. 删除关联的 评分规则
        lessonScoringRulesService.remove(new QueryWrapper<LessonScoringRules>().eq("user_profile_id", id));
        // 2. 删除关联的 评等规则
        lessonGradingRulesService.remove(new QueryWrapper<LessonGradingRules>().eq("user_profile_id", id));
        // 3. 删除用户画像主体
        boolean b = this.removeById(id);
        if (b) {
//            删除成功刷新规则名称
            reorderRuleNames(userProfiles.getLessonId());
        }
        return b;
    }

    @Override
    public String generateNewRuleName(String lessonId) {
        // 1. 生成当天日期的前缀
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String prefix = "基础规则_" + today.format(formatter) + "#";
        // 2. 查询当前lessonId下，使用相同前缀的已有规则
        List<UserProfiles> existingRules = this.list(new QueryWrapper<UserProfiles>()
                .eq("lesson_id", lessonId));
        // 3. 解析并找到最大序号
        int maxSeq = 0;
        if (CollUtil.isNotEmpty(existingRules)) {
            for (UserProfiles rule : existingRules) {
                String name = rule.getName();
                try {
                    String seqStr = name.substring(name.lastIndexOf("#") + 1);
                    int currentSeq = Integer.parseInt(seqStr);
                    if (currentSeq > maxSeq) {
                        maxSeq = currentSeq;
                    }
                } catch (Exception e) {
                    // 忽略解析失败的名称
                }
            }
        }
        // 4. 返回新名称
        return prefix + (maxSeq + 1);
    }

    @Override
    public void reorderRuleNames(String lessonId) {
        // 1. 按创建时间查询所有符合命名规则的记录
        List<UserProfiles> rulesToReorder = this.list(new QueryWrapper<UserProfiles>()
                .eq("lesson_id", lessonId)
                .like("name", "基础规则_")
                .like("name", "#")
                .orderByAsc("create_time"));

        if (CollUtil.isEmpty(rulesToReorder)) {
            return;
        }
        List<UserProfiles> updates = new ArrayList<>();
        int currentSeq = 1;
        for (UserProfiles rule : rulesToReorder) {
            String oldName = rule.getName();
            String prefix = oldName.substring(0, oldName.lastIndexOf("#") + 1);
            String newName = prefix + currentSeq;
            if (!oldName.equals(newName)) {
                rule.setName(newName);
                updates.add(rule);
            }
            currentSeq++;
        }

        if (CollUtil.isNotEmpty(updates)) {
            this.updateBatchById(updates);
        }
    }

    @Override
    public List<RuleDescriptionVo> getRuleDescriptions(String userProfileId) {
        UserProfiles userProfile = this.getById(userProfileId);
        if (userProfile == null) {
            throw new JeecgBootException("未找到指定的规则。");
        }
        List<RuleDescriptionVo> descriptions = new ArrayList<>();
        String assessmentMethod = userProfile.getConditionsJson().getAssessmentMethod();

        if ("score".equals(assessmentMethod)) {
            List<LessonScoringRules> scoringRules = lessonScoringRulesService.list(new QueryWrapper<LessonScoringRules>().eq("user_profile_id", userProfileId));
            for (LessonScoringRules rule : scoringRules) {
                descriptions.add(convertToDescription(userProfile, rule));
            }
        } else if ("grade".equals(assessmentMethod)) {
            List<LessonGradingRules> gradingRules = lessonGradingRulesService.list(new QueryWrapper<LessonGradingRules>().eq("user_profile_id", userProfileId));
            for (LessonGradingRules rule : gradingRules) {
                descriptions.add(convertToDescription(userProfile, rule));
            }
        }
        return descriptions;
    }

    @Override
    public RuleDescriptionVo getRuleDescription(String userProfileId, String ruleId) {
        UserProfiles userProfile = this.getById(userProfileId);
        if (userProfile == null) {
            throw new JeecgBootException("未找到指定的规则。");
        }

        String assessmentMethod = userProfile.getConditionsJson().getAssessmentMethod();
        Object rule = null;

        if ("score".equals(assessmentMethod)) {
            rule = lessonScoringRulesService.getById(ruleId);
        } else if ("grade".equals(assessmentMethod)) {
            rule = lessonGradingRulesService.getById(ruleId);
        }

        if (rule == null) {
            throw new IllegalArgumentException("未找到指定的年龄段规则。");
        }

        return convertToDescription(userProfile, rule);
    }

    @Override
    public UserMatchingAttributes getUserMatchingAttributes(String idOrIdCard) {
        //TODO 根据人员身份证或id 获取人员画像
        return null;
    }

    @Override
    public AdaptedLessonRulesVo getAdaptedRulesForUser(UserMatchingAttributes userAttributes, String lessonId) {
        // 1. 获取该课目下所有启用的规则画像作为候选
        List<UserProfiles> candidateProfiles = this.list(new QueryWrapper<UserProfiles>()
                .eq("lesson_id", lessonId)
                .orderByAsc("create_time"));

        if (CollUtil.isEmpty(candidateProfiles)) {
            return new AdaptedLessonRulesVo();
        }
        // 3. 将用户属性转换为用于匹配的 ConditionsJson 对象
        ConditionsJson userProfileToMatch = new ConditionsJson();
        userProfileToMatch.setSoldierCategory(userAttributes.getSoldierCategory());
        userProfileToMatch.setNewSoldier(userAttributes.getNewSoldier());
        userProfileToMatch.setGender(userAttributes.getGender());
        userProfileToMatch.setAltitude(String.valueOf(userAttributes.getAltitude()));
        List<UserProfiles> userProfilesList = new ArrayList<>();
        for (UserProfiles candidate : candidateProfiles) {
            ConditionsJson ruleConditions = candidate.getConditionsJson();
            MatchingCondition.MatchingRule matchingRule = profileMatcher.createMatchingRuleFromConditionsJson(ruleConditions);
            if (profileMatcher.matches(userProfileToMatch, matchingRule)) {
                userProfilesList.add(candidate);
            }
        }
        AdaptedLessonRulesVo result = new AdaptedLessonRulesVo();
        if (CollUtil.isEmpty(userProfilesList)) {
            return result;
        }
        for (UserProfiles userProfile : userProfilesList) {
            String assessmentMethod = userProfile.getConditionsJson().getAssessmentMethod();
            Integer userAge = userAttributes.getAge();
            if ("score".equals(assessmentMethod)) {
                List<LessonScoringRules> scoringRules = lessonScoringRulesService.list(new QueryWrapper<LessonScoringRules>()
                        .eq("user_profile_id", userProfile.getId())
                        .le("min_age", userAge)
                        .ge("max_age", userAge));
                if (CollUtil.isNotEmpty(scoringRules)) {
                    // 理论上年龄区间不重叠，只会找到一个
                    result.setScoringRule(new AdaptedScoringRuleVo(scoringRules.get(0), userProfile));
                }
            } else if ("grade".equals(assessmentMethod)) {
                List<LessonGradingRules> gradingRules = lessonGradingRulesService.list(new QueryWrapper<LessonGradingRules>()
                        .eq("user_profile_id", userProfile.getId())
                        .le("min_age", userAge)
                        .ge("max_age", userAge));
                if (CollUtil.isNotEmpty(gradingRules)) {
                    result.setGradingRule(new AdaptedGradingRuleVo(gradingRules.get(0), userProfile));
                }
            }
        }
        return result;
    }

    @Override
    public AdaptedLessonRulesVo calculateAdaptedRules(AdaptedLessonRulesVo adaptedRules, String rawPerformance) {
        if (adaptedRules == null || rawPerformance == null) {
            return adaptedRules;
        }

        AdaptedScoringRuleVo scoringRuleVo = adaptedRules.getScoringRule();
        AdaptedGradingRuleVo gradingRuleVo = adaptedRules.getGradingRule();

        if (scoringRuleVo != null && scoringRuleVo.getRule() != null) {
            LessonScoringRules rule = scoringRuleVo.getRule();
            // 调用 RuleManager 进行计算
            String scoreResult = ruleManager.calculate(rule, rawPerformance, rule.getUnit());
            // 填充原始成绩
            scoringRuleVo.setRawPerformance(rawPerformance);
            // 填充格式化后的计算结果
            scoringRuleVo.setPerformanceWithUnit(rawDataConverter.formatPerformance(rawPerformance, rule.getUnit()));
            // 填充计算结果
            try {
                if ("-".equals(scoreResult)) {
                    scoreResult = "-1";
                }
                scoringRuleVo.setScore(Integer.parseInt(scoreResult));
            } catch (NumberFormatException e) {
                // 如果返回的不是数字（例如 "-”），则不设置分数，保持为null
            }
        }
        if (gradingRuleVo != null && gradingRuleVo.getRule() != null) {
            LessonGradingRules rule = gradingRuleVo.getRule();
            // 调用 RuleManager 进行计算
            String gradeResult = ruleManager.calculate(rule, rawPerformance, rule.getUnit());
            // 填充原始成绩
            gradingRuleVo.setRawPerformance(rawPerformance);
            // 填充格式化后的计算结果
            gradingRuleVo.setPerformanceWithUnit(rawDataConverter.formatPerformance(rawPerformance, rule.getUnit()));
            // 填充计算结果
            gradingRuleVo.setGrade(gradeResult);
        }
        return adaptedRules;
    }

    @Override
    public LessonScoreVo getLessonScore(LessonScoreParamVo param) {
        // 步骤 1: 校验输入参数
        if (param == null || StrUtil.isBlank(param.getUserId()) || StrUtil.isBlank(param.getLessonId())) {
            throw new IllegalArgumentException("用户ID和课目ID均不能为空。");
        }
        // 步骤 2: 查询课目信息，以确定是单个课目还是课目组
        LessonRule lessonRule = this.getByLessonId(param.getLessonId());
        if (lessonRule == null) {
            throw new JeecgBootException("未找到ID为 " + param.getLessonId() + " 的课目。");
        }
        // 步骤 3: 根据课目类型进行逻辑分发
        if ("group".equals(lessonRule.getType())) {
            // 场景A: 计算综合课目成绩
            if (CollUtil.isEmpty(param.getSubjectPerformance())) {
                throw new IllegalArgumentException("计算综合课目成绩时，必须提供子课目成绩列表。");
            }
            //校验传入的子课目列表是否与课目组定义的完全匹配
            String definedLessonIdsStr = lessonRule.getLessonIds();
            if (StrUtil.isBlank(definedLessonIdsStr)) {
                throw new IllegalArgumentException("综合课目【" + lessonRule.getName() + "】未配置任何子课目。");
            }
            Set<String> definedIds = StrUtil.split(definedLessonIdsStr, ',').stream()
                    .map(String::trim)
                    .collect(Collectors.toSet());
            Set<String> providedIds = param.getSubjectPerformance().stream()
                    .map(LessonScoreParamVo.SubjectPerformance::getLessonId)
                    .collect(Collectors.toSet());
            if (!definedIds.equals(providedIds)) {
                throw new IllegalArgumentException("提供的课目成绩与课目组【" + lessonRule.getName() + "】定义的子课目不匹配。");
            }
            return calculateGroupLessonScore(param);
        } else {
            // 场景B: 计算单个课目成绩
            return calculateSingleLessonScore(param);
        }
    }

    /**
     * 私有辅助方法：计算单个课目的成绩
     */
    public LessonScoreVo calculateSingleLessonScore(LessonScoreParamVo param) {
        // [优化] 总是返回一个有效的 LessonScoreVo 对象
        //获取课目信息
        String userId = param.getUserId();
        String rawPerformance = param.getRawPerformance();
        String lessonId = param.getLessonId();
        LessonRule lessonRule = this.getByLessonId(lessonId);
        LessonScoreVo lessonScoreVo = new LessonScoreVo();
        lessonScoreVo.setLessonId(param.getLessonId());
        lessonScoreVo.setRawPerformance(rawPerformance);
        // 填充格式化后的计算结果
        lessonScoreVo.setPerformanceWithUnit(rawDataConverter.formatPerformance(rawPerformance, lessonRule.getUnit()));
        lessonScoreVo.setGrade("-");
        lessonScoreVo.setScore(-1);
        if (StrUtil.isBlank(rawPerformance)) {
            lessonScoreVo.setMsg("请输入原始成绩");
            return lessonScoreVo;
        }
        UserMatchingAttributes userMatchingAttributes = getUserMatchingAttributes(userId);
        if (Objects.isNull(userMatchingAttributes)) {
            lessonScoreVo.setMsg("未找到匹配的用户信息");
            return lessonScoreVo;
        }
        if (Objects.nonNull(param.getAge())) {
            userMatchingAttributes.setAge(param.getAge());
        }
        log.info("匹配到用户信息【{}】开始匹配【{}】规则", userMatchingAttributes, lessonRule.getName());
        AdaptedLessonRulesVo adaptedRules = getAdaptedRulesForUser(userMatchingAttributes, lessonId);

        if (adaptedRules == null || (adaptedRules.getScoringRule() == null && adaptedRules.getGradingRule() == null)) {
            lessonScoreVo.setMsg("未找到匹配的规则");
            log.error("未找到匹配【" + lessonRule.getName() + "】规则");
            return lessonScoreVo;
        }
        // 处理原始成绩  1.处理成绩取值方式 2. 处理高原规则
        String processedPerformance = processRawPerformance(rawPerformance, lessonRule, userMatchingAttributes.getAltitude());
        // 3. 计算成绩
        AdaptedLessonRulesVo resultRules = calculateAdaptedRules(adaptedRules, processedPerformance);
        //返回评等规则----成绩
        if (resultRules.getGradingRule() != null) {
            lessonScoreVo.setGrade(resultRules.getGradingRule().getGrade());
            // 存在评分规则
            if (resultRules.getGradingRule().getUserProfiles() != null && resultRules.getGradingRule().getUserProfiles().getConditionsJson() != null) {
                String calculationMethod = resultRules.getGradingRule().getUserProfiles().getConditionsJson().getCalculationMethod();
                lessonScoreVo.setCalculationMethod(calculationMethod);
            }
        }
        //返回评分规则----成绩
        if (resultRules.getScoringRule() != null) {
            lessonScoreVo.setScore(resultRules.getScoringRule().getScore());
            // 存在评分规则
            if (resultRules.getScoringRule().getUserProfiles() != null && resultRules.getScoringRule().getUserProfiles().getConditionsJson() != null) {
                String calculationMethod = resultRules.getScoringRule().getUserProfiles().getConditionsJson().getCalculationMethod();
                lessonScoreVo.setCalculationMethod(calculationMethod);
            }
        }
        log.info("课目【{}】原始成绩:{},高原规则处理后成绩:{}", lessonRule.getName(), lessonScoreVo.getRawPerformance(), processedPerformance);
        log.info("课目【{}】评分结果:{},评等结果:{}", lessonRule.getName(), lessonScoreVo.getScore(), lessonScoreVo.getGrade());
        return lessonScoreVo;
    }

    /**
     * [新增] 根据取值方式处理原始成绩（向上/向下取整、四舍五入）,海拔规则处理。
     *
     * @param rawPerformance 原始成绩字符串
     * @param lessonRule     课目信息
     * @param altitude       海拔高度           单位，用于判断是否为时间格式
     * @return 处理后的成绩字符串
     */
    private String processRawPerformance(String rawPerformance, LessonRule lessonRule, Integer altitude) {
        String compensatedPerformance = rawPerformance;
        Integer captureMethod = lessonRule.getCaptureMethod();
        String unit = lessonRule.getUnit();
        // 步骤1: 应用高原补偿规则 //BMI,PBF成绩跳过高原规则
        // 步骤1: 应用高原补偿规则
        // 当课目代码不是 "PBF" 或 "BMI" 时，才执行高原补偿逻辑
        if (!"PBF".equals(lessonRule.getCode()) && !"BMI".equals(lessonRule.getCode())) {
            if (altitude != null && altitude > 1500) {
                log.info("高原海拔规则原始成绩:{}", rawPerformance);
                // [优化] 直接在查询中匹配海拔区间
                QueryWrapper<LessonPlateauRules> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("lesson_id", lessonRule.getId())
                        .le("min_altitude", altitude)
                        .ge("max_altitude", altitude);
                LessonPlateauRules plateauRule = lessonPlateauRulesService.getOne(queryWrapper);
                // 如果找到匹配的规则，则进行补偿计算
                compensatedPerformance = applyAltitudeCompensation(rawPerformance, plateauRule, altitude, unit);
            }
        }
        // 仅当 captureMethod 有效，且单位不是时间格式时，才进行取整操作
        if (captureMethod != null && (unit == null || !unit.contains("TIME"))) {
            try {
                BigDecimal performanceDecimal = new BigDecimal(compensatedPerformance);
                BigDecimal roundedPerformance;
                switch (captureMethod) {
                    // 向上取整
                    case 1:
                        roundedPerformance = performanceDecimal.setScale(0, RoundingMode.CEILING);
                        break;
                    // 向下取整
                    case 2:
                        roundedPerformance = performanceDecimal.setScale(0, RoundingMode.FLOOR);
                        break;
                    // 四舍五入
                    case 3:
                        roundedPerformance = performanceDecimal.setScale(0, RoundingMode.HALF_UP);
                        break;
                    default:
                        return compensatedPerformance;
                }
                return roundedPerformance.toPlainString();
            } catch (NumberFormatException e) {
                // 如果 rawPerformance 不是有效的数字，则忽略取整操作，使用原始值
                log.error("原始成绩不是有效的数字：" + e + "直接使用原始值");
                return compensatedPerformance;
            }
        }
        log.info("高原海拔规则最终成绩:{}", compensatedPerformance);
        // 如果不满足条件，直接返回原始值
        return compensatedPerformance;
    }

    /**
     * [新增] 应用高原补偿计算。
     *
     * @param rawPerformance 原始成绩
     * @param rule           匹配到的高原补偿规则
     * @param userAltitude   用户当前海拔
     * @param unit           单位
     * @return 补偿后的成绩
     */
    private String applyAltitudeCompensation(String rawPerformance, LessonPlateauRules rule, Integer userAltitude, String unit) {
        if (rule == null) {
            return rawPerformance;
        }
        try {
            // 计算补偿倍数 = (用户海拔 - 规则起始海拔) / 高度每增加值 (向下取整)
            int multiplier = (userAltitude - rule.getMinAltitude()) / rule.getChangeNum();
            if (multiplier <= 0) {
                return rawPerformance;
            }
            // 总补偿值
            int totalCompensation = multiplier * rule.getChangeValue();

            // 判断是增加还是减少
            boolean isAdding = "up".equals(rule.getChangeMethod());
            // 根据单位判断是处理时间还是数字
            if (unit != null && unit.contains("TIME")) {
                // 时间格式处理
                TimeConverter converter = new TimeConverter();
                BigDecimal rawSeconds = converter.convert(rawPerformance);
                long totalSeconds = rawSeconds.longValue();
                long compensatedSeconds = isAdding ? totalSeconds + totalCompensation : totalSeconds - totalCompensation;
                // 成绩不能为负
                if (compensatedSeconds < 0) {
                    compensatedSeconds = 0;
                }
                long minutes = compensatedSeconds / 60;
                long seconds = compensatedSeconds % 60;
                return StrUtil.padPre(String.valueOf(minutes), 2, '0') + ":" + StrUtil.padPre(String.valueOf(seconds), 2, '0');
            } else {
                // 数字格式处理
                BigDecimal performanceDecimal = new BigDecimal(rawPerformance);
                BigDecimal compensationDecimal = new BigDecimal(totalCompensation);
                BigDecimal compensatedValue = isAdding ? performanceDecimal.add(compensationDecimal) : performanceDecimal.subtract(compensationDecimal);
                if (compensatedValue.compareTo(BigDecimal.ZERO) < 0) {
                    // 成绩不能为负
                    compensatedValue = BigDecimal.ZERO;
                }
                return compensatedValue.toPlainString();
            }
        } catch (Exception e) {
            log.error("应用高原补偿规则时出错，将返回原始成绩。错误: " + e.getMessage());
            // 发生任何异常都返回原始值，保证流程不中断
            return rawPerformance;
        }
    }

    /**
     * 私有辅助方法：计算综合课目（课目组）的成绩
     */
    private LessonScoreVo calculateGroupLessonScore(LessonScoreParamVo param) {
        LessonScoreVo lessonScoreVo = new LessonScoreVo();
        lessonScoreVo.setLessonId(param.getLessonId());
        //科目组一般没有成绩根据课目成绩进行处理
        lessonScoreVo.setRawPerformance(param.getRawPerformance());
        lessonScoreVo.setGrade("-");
        lessonScoreVo.setScore(-1);
        // 步骤 1: 遍历子课目列表，为每个子课目计算成绩
        List<LessonScoreVo> subLessonScores = param.getSubjectPerformance().stream()
                .map((subject) -> {
                    LessonScoreParamVo param2 = new LessonScoreParamVo();
                    param2.setRawPerformance(subject.getRawPerformance());
                    param2.setLessonId(subject.getLessonId());
                    param2.setUserId(param.getUserId());
                    return calculateSingleLessonScore(param2);
                })
                .collect(Collectors.toList());
        //返回课目成绩
        lessonScoreVo.setLessonScoreVos(subLessonScores);
        UserMatchingAttributes userMatchingAttributes = getUserMatchingAttributes(param.getUserId());
        if (Objects.isNull(userMatchingAttributes)) {
            lessonScoreVo.setMsg("未找到匹配的用户信息");
            return lessonScoreVo;
        }
        if (Objects.nonNull(param.getAge())) {
            userMatchingAttributes.setAge(param.getAge());
        }
        //2.获取课目组规则
        AdaptedLessonRulesVo adaptedRules = getAdaptedRulesForUser(userMatchingAttributes, param.getLessonId());
        if (adaptedRules == null || adaptedRules.getGradingRule() == null) {
            lessonScoreVo.setMsg("未找到匹配的综合课目总评规则，仅返回各子课目成绩。");
            return lessonScoreVo;
        }
        AdaptedGradingRuleVo gradingRuleVo = adaptedRules.getGradingRule();
        if (gradingRuleVo.getRule() != null) {
            // 步骤 3: 构建用于总评计算的成绩单 (List<GradeLevel>)
            List<GradeLevel> finalScoresForGrading = subLessonScores.stream()
                    .map(scoreVo -> {
                        // [修改] 直接使用 getGrade() 的值
                        return new GradeLevel(scoreVo.getLessonId(), scoreVo.getGrade());
                    })
                    .collect(Collectors.toList());

            LessonGradingRules rule = gradingRuleVo.getRule();
            PersonnelProfile compoundProfile = new PersonnelProfile();
            compoundProfile.setName("科目组临时评等规则");
            compoundProfile.setRuleType("2");
            List<RuleLogicBlock> ruleLogicBlocks = convertGradingRuleToLogicBlock(rule);

            compoundProfile.setRuleLogic(ruleLogicBlocks);
            String subjectScoresJson = JSONUtil.parseArray(finalScoresForGrading).toString();
            String result = ruleManager.calculate(compoundProfile, subjectScoresJson, EvaluationType.COMPOUND_CONDITION_GRADING);
            lessonScoreVo.setGrade(result);
        }

        log.info("计算课目成绩完成，结果为：{}", lessonScoreVo);
        return lessonScoreVo;
    }

    /**
     * 将从数据库获取的 LessonGradingRules 对象转换为规则引擎所需的 RuleLogicBlock 对象。
     * 主要用于处理“复合条件评等”的规则格式转换。
     *
     * @param gradingRule 包含原始规则的 LessonGradingRules 对象
     * @return 配置好的 List<RuleLogicBlock> 对象
     */
    @Override
    public List<RuleLogicBlock> convertGradingRuleToLogicBlock(LessonGradingRules gradingRule) {
        List<RuleLogicBlock> ruleBlocks = new ArrayList<>();
        RuleLogicBlock ruleBlock = new RuleLogicBlock();
        ruleBlock.setEnabled(true);
        ruleBlock.setType(EvaluationType.COMPOUND_CONDITION_GRADING.getCode());
        ruleBlock.setEvaluationGrade(gradingRule.getEvaluationGrade());
        List<RawRule> rulesJson = gradingRule.getRulesJson();

        // [优化] 使用 for 循环重写规则转换逻辑，使其更清晰
        List<CompoundConditionInfo> conditionInfoArrayList = new ArrayList<>();
        for (RawRule rawRule : rulesJson) {
            CompoundConditionInfo conditionInfo = new CompoundConditionInfo();
            conditionInfo.setGrade(rawRule.getEvaluationCode());
            conditionInfo.setSort(Integer.valueOf(rawRule.getEvaluationScore()));
            List<ConditionRule> conditions = new ArrayList<>();
            if (rawRule.getLevelData() != null) {
                for (Object levelObj : rawRule.getLevelData()) {
                    cn.hutool.json.JSONObject levelJson = (cn.hutool.json.JSONObject) levelObj;
                    ConditionRule conditionRule = new ConditionRule();
                    conditionRule.setSubject(levelJson.getStr("id"));
                    conditionRule.setName(levelJson.getStr("label"));
                    conditionRule.setValue(levelJson.getStr("value"));
                    conditionRule.setEvaluationGrade("quaternary_rading");
                    conditions.add(conditionRule);
                }
            }
            conditionInfo.setConditions(conditions);

            conditionInfoArrayList.add(conditionInfo);
        }
        ruleBlock.setAssessment_info(JSONUtil.parseArray(conditionInfoArrayList));
        ruleBlocks.add(ruleBlock);
        return ruleBlocks;
    }

    @Override
    public void getTemplate(HttpServletResponse response) throws IOException {
        try {
            // 1. 设置下载响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String encodedFileName = URLEncoder.encode("单位总评规则导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=UTF-8''" + encodedFileName + ".xlsx");
            // 2. 创建一个空数据
            List<LessonRulesExcelData> dataList = new ArrayList<>();
            // 3. 使用 EasyExcel 写入模板，并注册自定义的下拉框处理器
            EasyExcel.write(response.getOutputStream(), LessonRulesExcelData.class)
                    .registerWriteHandler(new ExcelSelectSheetWriteHandler(LessonRulesExcelData.class))
                    .sheet("单位总评规则").doWrite(dataList);
        } catch (UnsupportedEncodingException e) {
            throw new JeecgBootException("导出Excel模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<Integer, String> saveBatchImport(List<LessonRulesExcelData> cachedDataList) {
        // 步骤 1: 创建导入数据保存结果消息
        Map<Integer, String> errorMap = new HashMap<>();
        // [优化] 获取所有校验失败行的 lessonCode，并存入 Set 以提高过滤效率
        Set<String> errorLessonCodes = cachedDataList.stream()
                .filter(row -> !row.isStatus())
                .map(LessonRulesExcelData::getLessonCode)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        // [修复] 过滤掉包含错误行的整个规则组，然后按 课目+适用人员 分组
        log.info("开始保存数据共有有效数据 {} 条", errorLessonCodes.size());
        Map<String, List<LessonRulesExcelData>> groupedByRule = cachedDataList.stream()
                .filter(row -> StrUtil.isNotBlank(row.getLessonCode()) && !errorLessonCodes.contains(row.getLessonCode()))
                .collect(Collectors.groupingBy(row -> row.getLessonCode() + "-" + row.getSoldierCategory() + "-" + row.getNewSoldier() + "-" + row.getGender() + "-" + row.getAssessmentMethod() + "-" + row.getEvaluationGrade() + "-" + row.getAltitudeRange()));
        log.info("开始处理导入数据，共有 {} 组规则", groupedByRule.size());
        //遍历每个分组，将其转换为 LessonRuleDetailVo 并保存
        for (Map.Entry<String, List<LessonRulesExcelData>> entry : groupedByRule.entrySet()) {
            List<LessonRulesExcelData> groupData = entry.getValue();
            if (CollUtil.isEmpty(groupData)) {
                continue;
            }
            try {
                String key = entry.getKey();
                //根据课目code  获取课目id
                String lessonCode = groupData.get(0).getLessonCode();
                LessonRule lessonRule = userProfilesMapper.getByLessonCode(lessonCode);
                // 步骤 2.1: 创建 LessonRuleDetailVo
                LessonRuleDetailVo detailVo = new LessonRuleDetailVo();
                UserProfiles userProfiles = new UserProfiles();
                // 步骤 2.2: 从分组的第一行数据填充 UserProfiles 的公共信息
                LessonRulesExcelData firstRow = groupData.get(0);
                userProfiles.setLessonId(lessonRule.getId());
                userProfiles.setIsEnabled(1);
                ConditionsJson conditions = new ConditionsJson();
                conditions.setSoldierCategory(firstRow.getSoldierCategory());
                conditions.setNewSoldier(firstRow.getNewSoldier());
                conditions.setGender(firstRow.getGender());
                conditions.setEvaluationGrade(firstRow.getEvaluationGrade());
                // 海拔区间处理逻辑
                String altitudeRangeStr = firstRow.getAltitudeRange();
                String altitudeType = "0";
                conditions.setAltitude("0-1500");
                if (StrUtil.isNotBlank(altitudeRangeStr) && altitudeRangeStr.contains("-")) {
                    try {
                        String[] parts = altitudeRangeStr.split("-");
                        if (parts.length == 2) {
                            int minAltitude = Integer.parseInt(parts[0].trim());
                            int maxAltitude = Integer.parseInt(parts[1].trim());
                            // 判断两边数字是否都大于1500，如果是，则设置AltitudeType为1
                            if (minAltitude > 1500 && maxAltitude > 1500) {
                                altitudeType = "1";
                                conditions.setAltitude(minAltitude + "-" + maxAltitude);
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析海拔区间失败: {}, 错误信息: {}", altitudeRangeStr, e.getMessage());
                    }
                }
                conditions.setAltitudeType(altitudeType);
                conditions.setAssessmentMethod(firstRow.getAssessmentMethod());
                conditions.setCalculationMethod(firstRow.getCalculationMethod());
                userProfiles.setConditionsJson(conditions);
                detailVo.setUserProfiles(userProfiles);
                //根据年龄段再次分组年龄段详情规则
                Map<String, List<LessonRulesExcelData>> groupedByAge = groupData.stream()
                        .filter(row -> StrUtil.isNotBlank(row.getAgeRange()))
                        .collect(Collectors.groupingBy(LessonRulesExcelData::getAgeRange));
                // 遍历每个年龄段分组，创建对应的规则
//                评分规则
                if ("score".equals(conditions.getAssessmentMethod())) {
                    List<LessonScoringRules> scoringRulesList = new ArrayList<>();
                    for (Map.Entry<String, List<LessonRulesExcelData>> ageEntry : groupedByAge.entrySet()) {
                        String ageRange = ageEntry.getKey();
                        List<LessonRulesExcelData> ageGroupData = ageEntry.getValue();
                        LessonScoringRules rule = new LessonScoringRules();
                        //年龄段
                        rule.setAgeRange(ageRange);
                        String[] parts = ageRange.split("-");
                        if (parts.length == 2) {
                            //最小年龄,最大年龄
                            rule.setMinAge(Integer.parseInt(parts[0].trim()));
                            rule.setMaxAge(Integer.parseInt(parts[1].trim()));
                        }
                        //计量单位
                        rule.setUnit(lessonRule.getUnit());
                        //计算方式
                        rule.setCalculationMethod(firstRow.getCalculationMethod());
                        //根据ageGroupData数量和 score 值   计算最低分,最高分,步长
                        List<Integer> scores = ageGroupData.stream()
                                .map(row -> Integer.parseInt(row.getScore()))
                                .sorted()
                                .collect(Collectors.toList());

                        if (CollUtil.isNotEmpty(scores)) {
                            rule.setMinScore(scores.get(0));
                            rule.setMaxScore(scores.get(scores.size() - 1));
                            // 只有当分数超过一个时，步长才有意义
                            if (scores.size() > 1) {
                                // 假设步长是固定的，取前两个分数的差值
                                rule.setStepValue(scores.get(1) - scores.get(0));
                            }
                            // 根据业务需求设置为 5
                            rule.setStepValue(5);

                        }
                        //评分规则
                        List<RawRule> rawRules = ageGroupData.stream().map(excelRow -> {
                            RawRule rawRule = new RawRule();
                            rawRule.setScore(excelRow.getScore());
                            // 根据计算方式，决定将 excelRow.getResult() 设置为 performance 还是 scoreRanges
                            if ("3".equals(firstRow.getCalculationMethod())) {
                                // 计算方式为“区间适配”，设置成绩区间
                                rawRule.setScoreRanges(excelRow.getRange());
                            } else {
                                // 其他计算方式，设置为单个成绩
                                String performance = rawDataConverter.formatData(excelRow.getResult(), lessonRule.getUnit());
                                rawRule.setPerformance(performance);
                            }
                            return rawRule;
                        }).collect(Collectors.toList());
                        rule.setRulesJson(rawRules);
                        scoringRulesList.add(rule);
                    }
                    detailVo.setLessonScoringRules(scoringRulesList);
                }
                // 评等规则
                if ("grade".equals(conditions.getAssessmentMethod())) {
                    List<LessonGradingRules> gradingRulesList = new ArrayList<>();
                    for (Map.Entry<String, List<LessonRulesExcelData>> ageEntry : groupedByAge.entrySet()) {
                        String ageRange = ageEntry.getKey();
                        List<LessonRulesExcelData> ageGroupData = ageEntry.getValue();
                        LessonGradingRules rule = new LessonGradingRules();
                        //年龄段
                        rule.setAgeRange(ageRange);
                        String[] parts = ageRange.split("-");
                        if (parts.length == 2) {
                            //最小年龄,最大年龄
                            rule.setMinAge(Integer.parseInt(parts[0].trim()));
                            rule.setMaxAge(Integer.parseInt(parts[1].trim()));
                        }
                        //计量单位
                        rule.setUnit(lessonRule.getUnit());
                        //计算方式
                        rule.setCalculationMethod(firstRow.getCalculationMethod());
                        //等级制
                        rule.setEvaluationGrade(firstRow.getEvaluationGrade());
                        //评等规则
                        List<RawRule> rawRules = ageGroupData.stream().map(excelRow -> {
                            RawRule rawRule = new RawRule();
                            String text = commonAPI.translateDict("seven_level_grading", excelRow.getGrade());
                            rawRule.setEvaluationCode(excelRow.getGrade());
                            rawRule.setEvaluationName(text);
                            rawRule.setEvaluationScore(String.valueOf(gradeToScoreMap.getOrDefault(excelRow.getGrade(), 0)));
                            rawRule.setScore(String.valueOf(gradeToScoreMap.getOrDefault(excelRow.getGrade(), 0)));
                            if ("3".equals(firstRow.getCalculationMethod())) {
                                // 计算方式为“区间适配”，设置成绩区间
                                rawRule.setScoreRanges(excelRow.getRange());
                            } else {
                                // 其他计算方式，设置为单个成绩
                                String performance = rawDataConverter.formatData(excelRow.getResult(), lessonRule.getUnit());
                                rawRule.setPerformance(performance);
                            }
                            return rawRule;
                        }).collect(Collectors.toList());
                        rule.setRulesJson(rawRules);
                        gradingRulesList.add(rule);
                    }
                    detailVo.setLessonGradingRules(gradingRulesList);
                }
                Boolean b = this.saveLessonRuleDetail(detailVo);
                if (!b) {
                    errorMap.put(1, "保存失败");
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                log.error("导入规则失败:{}", errorMessage);
                errorMap.put(0, "导入规则保存失败:" + errorMessage);
            }
        }
        // 步骤 5: 返回包含所有错误信息的 Map
        return errorMap;
    }

    @Override
    public Map<Integer, String> validateData(LessonRulesExcelData row) {
        // 步骤 1: 创建导入行校验数据结果消息
        Map<Integer, String> errorMap = new HashMap<>();
        Integer rowIndex = row.getRowIndex();
        validateRange(row.getAltitudeRange(), "海拔区间", rowIndex, errorMap);
        validateRange(row.getAgeRange(), "年龄段", rowIndex, errorMap);
        if (StrUtil.isNotBlank(row.getRange())) {
            // 先校验整体格式
            if (!row.getRange().matches("^(\\d+(\\.\\d{1,2})?-\\d+(\\.\\d{1,2})?)(\\s*,\\s*\\d+(\\.\\d{1,2})?-\\d+(\\.\\d{1,2})?)*$")) {
                errorMap.put(rowIndex, "区间格式错误,格式应为 '数字-数字' 或 '数字-数字,数字-数字', 且数字最多包含两位小数");
            } else {
                // 如果整体格式正确，则对每一个子区间进行逻辑校验
                String[] ranges = row.getRange().split(",");
                for (String rangePart : ranges) {
                    validateRange(rangePart.trim(), "区间", rowIndex, errorMap);
                }
            }
        }
        return errorMap;
    }

    /**
     * [新增] 私有辅助方法，用于校验 "数字-数字" 格式的字符串
     *
     * @param rangeStr  待校验的字符串
     * @param fieldName 字段名，用于错误提示
     * @param rowIndex  行号
     * @param errorMap  错误信息收集器
     */
    private void validateRange(String rangeStr, String fieldName, Integer rowIndex, Map<Integer, String> errorMap) {
        // 如果为空，则不校验
        if (StrUtil.isBlank(rangeStr)) return;
        // 步骤 1: 根据字段名选择校验规则和错误信息
        String regex;
        String formatDescription;
        if ("区间".equals(fieldName)) {
            // 成绩区间，支持最多两位小数
            regex = "^\\d+(\\.\\d{1,2})?-\\d+(\\.\\d{1,2})?$";
            formatDescription = "'数字-数字'，且数字最多可包含两位小数";
        } else {
            // 其他区间（如年龄、海拔），只支持整数
            regex = "^\\d+-\\d+$";
            formatDescription = "'整数-整数'";
        }

        // 步骤 2: 执行正则表达式校验
        if (!rangeStr.matches(regex)) {
            errorMap.put(rowIndex, String.format("%s格式错误, 应为 %s", fieldName, formatDescription));
            return;
        }

        // 步骤 3: 执行统一的数值逻辑校验（统一使用 BigDecimal）
        try {
            String[] parts = rangeStr.split("-");
            BigDecimal start = new BigDecimal(parts[0]);
            BigDecimal end = new BigDecimal(parts[1]);

            if (start.compareTo(BigDecimal.ZERO) < 0 || end.compareTo(BigDecimal.ZERO) < 0) {
                errorMap.put(rowIndex, String.format("%s中的数字必须大于或等于0", fieldName));
            }
            if (start.compareTo(end) > 0) {
                errorMap.put(rowIndex, String.format("%s的起始值不能大于结束值", fieldName));
            }
        } catch (NumberFormatException e) {
            errorMap.put(rowIndex, String.format("%s中的数字过大或格式不正确", fieldName));
        }
    }

    /**
     * 通用的转换方法，将规则对象转换为文字描述VO
     */
    private RuleDescriptionVo convertToDescription(UserProfiles userProfile, Object rule) {
        RuleDescriptionVo vo = new RuleDescriptionVo();
        vo.setName(userProfile.getName());

        ConditionsJson conditions = userProfile.getConditionsJson();
        String assessmentMethodText = commonAPI.translateDict("assessment_method", conditions.getAssessmentMethod());
        String calculationMethodText = commonAPI.translateDict("calculation_method", conditions.getCalculationMethod());

        // 提取公共属性
        Integer minAge, maxAge;
        String unit;
        List<RawRule> rawRules;
        if (rule instanceof LessonScoringRules) {
            LessonScoringRules r = (LessonScoringRules) rule;
            minAge = r.getMinAge();
            maxAge = r.getMaxAge();
            unit = r.getUnit();
            rawRules = r.getRulesJson();
        } else {
            LessonGradingRules r = (LessonGradingRules) rule;
            minAge = r.getMinAge();
            maxAge = r.getMaxAge();
            unit = r.getUnit();
            rawRules = r.getRulesJson();
        }
        String unitText = StringUtils.isNotBlank(unit) ? commonAPI.translateDict("lesson_unit", unit) : "";
        maxAge = (rule instanceof LessonScoringRules) ? ((LessonScoringRules) rule).getMaxAge() : ((LessonGradingRules) rule).getMaxAge();
        vo.setAgeRangeText("年龄段 [" + minAge + ", " + maxAge + "]");
        StringBuilder sb = new StringBuilder();
        sb.append(assessmentMethodText).append(", ").append(calculationMethodText).append("。 ");

        if (CollUtil.isNotEmpty(rawRules)) {
            for (RawRule rawRule : rawRules) {
                if ("1".equals(conditions.getCalculationMethod()) || "2".equals(conditions.getCalculationMethod())) {
                    String operator = "1".equals(conditions.getCalculationMethod()) ? " >= " : " <= ";
                    if ("grade".equals(conditions.getAssessmentMethod())) {
                        // 评等制：使用等级名称
                        sb.append(rawRule.getEvaluationName()).append(":").append(operator).append(rawRule.getPerformance()).append(unitText).append("; ");
                    } else {
                        // 评分制：使用分数
                        sb.append(rawRule.getScore()).append("分:").append(operator).append(rawRule.getPerformance()).append(unitText).append("; ");
                    }
                } else if ("3".equals(conditions.getCalculationMethod())) {
                    // 区间适配

                    if ("grade".equals(conditions.getAssessmentMethod())) {
                        // 评等制：使用等级名称
                        String formattedRanges = rawRule.getScoreRanges()
                                .replace("[[", "[")
                                .replace("]]", "]")
                                .replace("],[", "], [");
                        sb.append(rawRule.getEvaluationName()).append(": ").append(formattedRanges).append(unitText).append("; ");
                    } else {
                        // 评分制：使用分数
                        String formattedRanges = rawRule.getScoreRanges()
                                .replace("[[", "[")
                                .replace("]]", "]")
                                .replace("],[", "], [");
                        sb.append(rawRule.getScore()).append("分: ").append(formattedRanges).append(unitText).append("; ");
                    }
                }
            }
        }

        vo.setDescriptionText(sb.toString().trim());
        return vo;
    }

    /**
     * 专门用于校验 LessonRuleDetailVo 数据的私有方法.
     * 如果校验失败，此方法会抛出异常，中断执行.
     *
     * @param lessonRuleDetailVo 待校验的VO对象
     */
    private void validateLessonRuleDetail(LessonRuleDetailVo lessonRuleDetailVo) {
        if (lessonRuleDetailVo == null) {
            throw new IllegalArgumentException("请求数据不能为空。");
        }
        UserProfiles currentUserProfile = lessonRuleDetailVo.getUserProfiles();
        if (currentUserProfile == null) {
            throw new IllegalArgumentException("条件规则不能为空。");
        }
        // 1. 获取当前规则的关键信息,并进行校验
        String lessonId = currentUserProfile.getLessonId();
        ConditionsJson conditions = currentUserProfile.getConditionsJson();
        String currentId = currentUserProfile.getId();
        // 2. 根据 lessonId 查询所有已存在的规则
        List<UserProfiles> existingProfiles = this.list(new QueryWrapper<UserProfiles>().eq("lesson_id", lessonId));
        for (UserProfiles existingProfile : existingProfiles) {
            // 如果是更新操作，则跳过与自身的比较
            if (currentId != null && currentId.equals(existingProfile.getId())) {
                continue;
            }
            ConditionsJson existingConditions = existingProfile.getConditionsJson();
            // 特殊校验：当其他条件相同时，检查海拔区间是否重叠
            if (ObjectUtil.equal(conditions.getSoldierCategory(), existingConditions.getSoldierCategory()) &&
                    ObjectUtil.equal(conditions.getNewSoldier(), existingConditions.getNewSoldier()) &&
                    ObjectUtil.equal(conditions.getGender(), existingConditions.getGender()) &&
                    ObjectUtil.equal(conditions.getAssessmentMethod(), existingConditions.getAssessmentMethod()) &&
                    ObjectUtil.equal(conditions.getAltitudeType(), existingConditions.getAltitudeType()) &&
                    "1".equals(conditions.getAltitudeType())) {

                String currentAltitudeRange = conditions.getAltitude();
                String existingAltitudeRange = existingConditions.getAltitude();

                if (areRangesOverlapping(currentAltitudeRange, existingAltitudeRange)) {
                    throw new IllegalArgumentException("海拔区间存在重叠：规则【" + existingProfile.getName() + "】的区间 (" + existingAltitudeRange + ") 与当前区间 (" + currentAltitudeRange + ") 重叠。");
                }
            }

            // ConditionsJson 是否相同
            if (ObjectUtil.equal(conditions, existingConditions)) {
                throw new IllegalArgumentException("已存在相同的条件规则【" + existingProfile.getName() + "】，请勿重复添加。");
            }
        }


        // 2. 开始进行评分规则校验/评等规则校验
        if (conditions != null && conditions.getAssessmentMethod() != null) {
            String assessmentMethod = conditions.getAssessmentMethod();
            if ("score".equals(assessmentMethod)) {
                validateScoringRules(lessonRuleDetailVo.getLessonScoringRules());
            } else if ("grade".equals(assessmentMethod)) {
                validateGradingRules(lessonRuleDetailVo.getLessonGradingRules());
            } else {
                throw new IllegalArgumentException("评估方法不存在,请联系管理员。");
            }
        } else {
            throw new JeecgBootException("评估方法不能为空。");
        }
    }

    /**
     * 检查两个表示范围的字符串是否重叠。
     * 范围格式为 "min-max"。
     *
     * @param rangeStr1 第一个范围字符串
     * @param rangeStr2 第二个范围字符串
     * @return 如果重叠则返回 true，否则返回 false
     */
    private boolean areRangesOverlapping(String rangeStr1, String rangeStr2) {
        // 如果任一范围为空或格式不正确，则认为不重叠
        if (StringUtils.isBlank(rangeStr1) || StringUtils.isBlank(rangeStr2) || !rangeStr1.contains("-") || !rangeStr2.contains("-")) {
            return false;
        }

        try {
            String[] parts1 = rangeStr1.split("-");
            int start1 = Integer.parseInt(parts1[0]);
            int end1 = Integer.parseInt(parts1[1]);

            String[] parts2 = rangeStr2.split("-");
            int start2 = Integer.parseInt(parts2[0]);
            int end2 = Integer.parseInt(parts2[1]);

            // 确保每个范围的 start <= end
            if (start1 > end1 || start2 > end2) {
                // 格式错误的范围不参与比较
                return false;
            }

            // 检查重叠的逻辑：两个区间的起始点中较大的那个，是否小于等于两个区间终点中较小的那个
            return Math.max(start1, start2) <= Math.min(end1, end2);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // 如果解析失败，则认为不重叠，让后续的校验处理格式问题
            return false;
        }
    }

    /**
     * 校验评分规则的私有辅助方法。
     *
     * @param scoringRules 待校验的评分规则列表
     */
    private void validateScoringRules(List<LessonScoringRules> scoringRules) {
        validateCommonRules(scoringRules, "评分规则");
    }

    /**
     * 将成绩（performance）字符串转换为 BigDecimal。
     * 如果字符串是 "mm:ss" 格式，则将其转换为总秒数。
     * 否则，直接将其作为普通数值处理。
     *
     * @param performanceStr 代表成绩的字符串
     * @return 用于比较的 BigDecimal 值
     * @throws IllegalArgumentException 如果格式不正确
     */
    private BigDecimal convertPerformanceToBigDecimal(String performanceStr) {
        if (performanceStr == null || performanceStr.trim().isEmpty()) {
            throw new IllegalArgumentException("成绩值不能为空。");
        }

        if (TIME_PATTERN.matcher(performanceStr).matches()) {
            try {
                String[] parts = performanceStr.split(":");
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);

                if (seconds >= 60) {
                    throw new IllegalArgumentException("时间格式错误，秒数必须小于60: " + performanceStr);
                }

                long totalSeconds = (long) minutes * 60 + seconds;
                return new BigDecimal(totalSeconds);
            } catch (NumberFormatException e) {
                // 正则表达式已匹配，但解析失败（例如数字过大），这是一种边缘情况
                throw new IllegalArgumentException("无法解析时间格式: " + performanceStr, e);
            }
        } else {
            try {
                // 如果不是时间格式，则尝试作为普通数字解析
                return new BigDecimal(performanceStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("成绩格式无法识别，既不是有效数字也不是 'mm:ss' 时间格式: " + performanceStr, e);
            }
        }
    }

    /**
     * 校验分数和成绩的单调性---处理越大越好和越小越好的校验
     */
    private void validateMonotonicity(List<RawRule> rawRules, String calculationMethod, String identifier) {
//        rawRules.sort(Comparator.comparing(RawRule::getScore, Comparator.reverseOrder()));
        for (int i = 1; i < rawRules.size(); i++) {
            RawRule prev = rawRules.get(i - 1);
            RawRule curr = rawRules.get(i);
            BigDecimal prevScore = new BigDecimal(prev.getScore());
            BigDecimal currScore = new BigDecimal(curr.getScore());
            if (currScore.compareTo(prevScore) >= 0) {
                throw new IllegalArgumentException("分数必须严格降序。");
            }
            // 使用辅助方法转换成绩，支持数字和时间格式
            BigDecimal prevPerf = convertPerformanceToBigDecimal(prev.getPerformance());
            BigDecimal currPerf = convertPerformanceToBigDecimal(curr.getPerformance());

            if ("1".equals(calculationMethod)) {
                if (currPerf.compareTo(prevPerf) >= 0) {
                    throw new IllegalArgumentException("计算方式为“越大越好”，成绩应严格降序。");
                }
            } else {
                if (currPerf.compareTo(prevPerf) <= 0) {
                    throw new IllegalArgumentException(" 计算方式为“越小越好”，成绩应严格升序。");
                }
            }
        }
    }

    /**
     * 校验成绩区间不重叠
     */
    private void validateScoreRangesIntersection(List<RawRule> rawRules, String identifier, String unit) {
        List<List<BigDecimal>> allRanges = new ArrayList<>();
        for (RawRule rawRule : rawRules) {
            String scoreRangesStr = rawRule.getScoreRanges();
            if (StringUtils.isBlank(scoreRangesStr)) {
                throw new IllegalArgumentException(identifier + " 的【" + rawRule.getEvaluationName() + "】等级未配置成绩区间。");
            }
            // [修复] 解析 "10-11,12-13" 格式的字符串
            String[] rangeParts = scoreRangesStr.split(",");
            List<List<String>> currentRangesStrList = new ArrayList<>();
            for (String part : rangeParts) {
                currentRangesStrList.add(CollUtil.newArrayList(part.split("-")));
            }
            validateAndAddRanges(currentRangesStrList, allRanges, identifier, rawRule.getEvaluationName(), unit);
        }
        // 步骤 3: 对收集到的所有区间进行统一校验  只有一个成绩区间 不用校验是否重叠
        if (allRanges.size() >= 2) {
            allRanges.sort(Comparator.comparing(range -> range.get(0)));
            for (int i = 1; i < allRanges.size(); i++) {
                List<BigDecimal> prev = allRanges.get(i - 1);
                List<BigDecimal> curr = allRanges.get(i);
                if (curr.get(0).compareTo(prev.get(1)) <= 0) {
                    // [优化] 如果是时间单位，错误提示也使用时间格式
                    String prevRangeStr = "[" + formatValue(prev.get(0), unit) + ", " + formatValue(prev.get(1), unit) + "]";
                    String currRangeStr = "[" + formatValue(curr.get(0), unit) + ", " + formatValue(curr.get(1), unit) + "]";
                    throw new IllegalArgumentException(
                            identifier + " 内，不同等级/分数的成绩区间存在重叠：" +
                                    prevRangeStr + " 与 " + currRangeStr + " 重叠。"
                    );
                }
            }
        }
    }

    /**
     * 校验单个区间的有效性（min <= max）并将其添加到总列表中。
     */
    private void validateAndAddRanges(List<List<String>> rangesToCheckStr, List<List<BigDecimal>> allRanges, String identifier, String evaluationName, String unit) {
        for (List<String> rangeStr : rangesToCheckStr) {
            if (rangeStr == null || rangeStr.size() != 2 || StringUtils.isBlank(rangeStr.get(0)) || StringUtils.isBlank(rangeStr.get(1))) {
                throw new IllegalArgumentException(identifier + " 的【" + evaluationName + "】等级中，存在格式不正确的成绩区间。");
            }
            BigDecimal start = convertPerformanceToBigDecimal(rangeStr.get(0));
            BigDecimal end = convertPerformanceToBigDecimal(rangeStr.get(1));
            // 校验：左边的值不能大于右边的值
            if (start.compareTo(end) > 0) {
                throw new IllegalArgumentException(identifier + " 的【" + evaluationName + "】等级中，成绩区间 [" + rangeStr.get(0) + ", " + rangeStr.get(1) + "] 的起始值不能大于结束值。");
            }
            allRanges.add(CollUtil.newArrayList(start, end));
        }
    }

    /**
     * 根据单位格式化用于显示的值。
     * 如果是时间单位，将秒数转为 mm:ss 格式。
     */
    private String formatValue(BigDecimal value, String unit) {
        if (value == null) return "null";
        if (unit != null && unit.contains("TIME")) {
            long totalSeconds = value.longValue();
            long minutes = totalSeconds / 60;
            long remainingSeconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, remainingSeconds);
        } else {
            return value.stripTrailingZeros().toPlainString();
        }
    }

    /**
     * 校验评等规则的私有辅助方法。
     *
     * @param gradingRules 待校验的评等规则列表
     */
    private void validateGradingRules(List<LessonGradingRules> gradingRules) {
        validateCommonRules(gradingRules, "评等规则");
    }

    /**
     * 通用的规则校验方法，适用于评分和评等规则。
     *
     * @param rules        规则列表，可以是 List<LessonScoringRules> 或 List<LessonGradingRules>
     * @param ruleTypeName 规则类型名称，用于错误提示 (例如 "评分规则", "评等规则")
     */
    private void validateCommonRules(List<?> rules, String ruleTypeName) {
        if (CollUtil.isEmpty(rules)) {
            throw new IllegalArgumentException(ruleTypeName + "不能为空。");
        }

        // 1. 校验年龄段不重叠
        if (rules.size() >= 2) {
            rules.sort(Comparator.comparingInt(rule -> {
                if (rule instanceof LessonScoringRules) {
                    return ((LessonScoringRules) rule).getMinAge();
                } else if (rule instanceof LessonGradingRules) {
                    return ((LessonGradingRules) rule).getMinAge();
                }
                return 0;
            }));

            for (int i = 1; i < rules.size(); i++) {
                Object prevObj = rules.get(i - 1);
                Object currObj = rules.get(i);
                Integer prevMaxAge = (prevObj instanceof LessonScoringRules) ? ((LessonScoringRules) prevObj).getMaxAge() : ((LessonGradingRules) prevObj).getMaxAge();
                Integer currMinAge = (currObj instanceof LessonScoringRules) ? ((LessonScoringRules) currObj).getMinAge() : ((LessonGradingRules) currObj).getMinAge();
                Integer currMaxAge = (currObj instanceof LessonScoringRules) ? ((LessonScoringRules) currObj).getMaxAge() : ((LessonGradingRules) currObj).getMaxAge();
                if (currMinAge != null && prevMaxAge != null && currMinAge <= prevMaxAge) {
                    // [修复] 动态获取 prevMinAge，避免类型转换错误
                    Integer prevMinAge = (prevObj instanceof LessonScoringRules) ? ((LessonScoringRules) prevObj).getMinAge() : ((LessonGradingRules) prevObj).getMinAge();
                    throw new IllegalArgumentException("下面规则年龄段区间存在重叠：[" + prevMinAge + ", " + prevMaxAge + "] 与 [" + currMinAge + ", " + currMaxAge + "] 重叠。");
                }
            }
        }

        // 2. 遍历每个年龄段规则，校验其内部的 rulesJson
        for (Object rule : rules) {
            Integer minAge, maxAge;
            String calculationMethod;
            String unit;
            List<RawRule> rawRules;

            if (rule instanceof LessonScoringRules) {
                LessonScoringRules r = (LessonScoringRules) rule;
                minAge = r.getMinAge();
                maxAge = r.getMaxAge();
                calculationMethod = r.getCalculationMethod();
                rawRules = r.getRulesJson();
                unit = r.getUnit();
            } else {
                LessonGradingRules r = (LessonGradingRules) rule;
                minAge = r.getMinAge();
                maxAge = r.getMaxAge();
                calculationMethod = r.getCalculationMethod();
                rawRules = r.getRulesJson();
                unit = r.getUnit();
            }

            String identifier = "年龄段[" + minAge + ", " + maxAge + "]";
            if (CollUtil.isEmpty(rawRules)) {
                throw new IllegalArgumentException(identifier + " 内未配置详细规则。");
            }

            if ("1".equals(calculationMethod) || "2".equals(calculationMethod)) {
                validateMonotonicity(rawRules, calculationMethod, identifier);
            } else if ("3".equals(calculationMethod)) {
                //增加判断  unit 为 TIME-003 时  数据成绩格式为 ["03:04","04:05"] 或者 [["03:04","04:05"]["03:04","04:05"]]
                //以下方法处理的的时数字类型数据 [1,2]或者 [[1,2],[2,3]
                validateScoreRangesIntersection(rawRules, identifier, unit);
            } else {

//                throw new IllegalArgumentException("未知的计算方式：" + calculationMethod);
            }
        }
    }

    /**
     * 用于保存评分规则的私有辅助方法。
     * 提高可读性并分离关注点。
     */
    private void saveScoringRules(String userProfileId, List<LessonScoringRules> scoringRules, boolean isUpdate) {

        if (isUpdate) {
            // 步骤 1: 根据 userProfileId 删除所有已存在的评分规则。
            QueryWrapper<LessonScoringRules> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_profile_id", userProfileId);
            lessonScoringRulesService.remove(queryWrapper);
        }
        // 步骤 2: 如果传入的新规则列表不为空，则批量保存它们。
        if (CollUtil.isNotEmpty(scoringRules)) {
            // 关键步骤: 将每个新规则与父实体的ID关联起来
            scoringRules.forEach(rule -> {
                if (!isUpdate) rule.setId(null);
                rule.setUserProfileId(userProfileId);
            });
            // 批量保存新的规则列表。
            lessonScoringRulesService.saveOrUpdateBatch(scoringRules);
        }
    }

    /**
     * 用于保存等级规则的私有辅助方法。
     */
    private void saveGradingRules(String userProfileId, List<LessonGradingRules> gradingRules, boolean isUpdate) {
        if (isUpdate) {
            // 步骤 1: 根据 userProfileId 删除所有已存在的评分规则。
            QueryWrapper<LessonGradingRules> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_profile_id", userProfileId);
            lessonGradingRulesService.remove(queryWrapper);
        }
        // 步骤 2: 如果传入的新规则列表不为空，则批量保存它们。
        if (CollUtil.isNotEmpty(gradingRules)) {
            // 关键步骤: 将每个新规则与父实体的ID关联起来
            gradingRules.forEach(rule -> {
                if (!isUpdate) rule.setId(null); // 新增时清空ID
                rule.setUserProfileId(userProfileId);
            });

            // 批量保存新的规则列表。
            lessonGradingRulesService.saveOrUpdateBatch(gradingRules);
        }
    }
}