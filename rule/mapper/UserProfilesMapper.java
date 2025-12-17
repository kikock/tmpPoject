package org.jeecg.modules.rule.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.rule.entity.LessonRule;
import org.jeecg.modules.rule.entity.domain.UserProfiles;


/**
 * @project_name: 后端平台项目
 * @description: 用户画像
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
public interface UserProfilesMapper extends BaseMapper<UserProfiles> {

    IPage<LessonRule> pageLessonRuleList(Page<LessonRule> page, @Param("ew") QueryWrapper<LessonRule> queryWrapper);

    LessonRule getByLessonId(@Param("id") String id);

    LessonRule getByLessonCode(@Param("code") String code);
}