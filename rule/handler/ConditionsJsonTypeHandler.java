package org.jeecg.modules.rule.handler;


import org.apache.ibatis.type.MappedTypes;
import org.jeecg.modules.rule.entity.domain.ConditionsJson;

/**
 * @project_name: 后端平台项目
 * @description:
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
@MappedTypes(ConditionsJson.class)
public class ConditionsJsonTypeHandler extends JsonTypeHandler<ConditionsJson> {
    public ConditionsJsonTypeHandler() {
        // 无参构造函数调用父类的带参构造函数来设置类型
        super(ConditionsJson.class);
    }
}
