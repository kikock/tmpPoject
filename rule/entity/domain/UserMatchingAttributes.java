package org.jeecg.modules.rule.entity.domain;

import lombok.Data;

import java.io.Serializable;


/**
 * @project_name: 后端平台项目
 * @description: 用于规则匹配的用户属性集合 DTO
 * @author: kikock
 * @create_date: 2025-12-20 14:38
 **/
@Data
public class UserMatchingAttributes implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 年龄 (周岁)
     */
    private Integer age;

    /**
     * 性别 (需要与字典 'sex' 的值对应)
     */
    private String gender;

    /**
     * 人员类别 (需要与字典 'soldier_category' 的值对应)
     */
    private String soldierCategory;

    /**
     * 是否新兵
     */
    private String newSoldier;

    /**
     * 用户所在单位的海拔高度 (单位：米)
     */
    private Integer altitude;
}