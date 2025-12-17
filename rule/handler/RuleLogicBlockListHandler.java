package org.jeecg.modules.rule.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jeecg.modules.rule.entity.domain.RuleLogicBlock;

import java.util.List;



/**
 * @project_name: 字符串转换
 * @description: 它将 List 序列化为 JSON 字符串以存入数据库，并将 JSON 字符串反序列化回 List<RawRule>。
 * @author: kikock
 * @create_date: 2025-09-22 14:38
 **/
public class RuleLogicBlockListHandler extends ListTypeHandler<RuleLogicBlock> {

    @Override
    protected TypeReference<List<RuleLogicBlock>> specificType() {
        return new TypeReference<List<RuleLogicBlock>>() {
        };
    }
}