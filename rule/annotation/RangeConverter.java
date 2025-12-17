package org.jeecg.modules.rule.annotation;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.modules.rule.entity.enums.UnitType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @project_name: 后端平台项目
 * @description: 数值单位的实现----BMI,PBF
 * @author: kikock
 * @create_date: 2025-12-15 09:55
 **/
@Component
public class RangeConverter implements UnitConverter<BigDecimal> {

    @Resource
    private CommonAPI commonAPI;

    @Override
    public BigDecimal parse(Object value) {
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法将值 '" + value + "' 转换为数值类型", e);
        }
    }

    @Override
    public BigDecimal convert(Object value) {
        return parse(value);
    }

    @Override
    public String formatData(BigDecimal value, String unit) {
        if (value == null) {
            return "-";
        }
        return value.toPlainString();
    }

    @Override
    public String format(BigDecimal value, String unit) {
        if (value == null) {
            return "";
        }
        String unitText = commonAPI.translateDict("lesson_unit", unit);
        if (StringUtils.isBlank(unitText)) {
            return value.toPlainString();
        }
        return value.toPlainString() + unitText;
    }

    @Override
    public boolean supports(String unit) {
        String one = commonAPI.translateDict("lesson_unit", unit);
        return StringUtils.isNotBlank(one) && unit.split("-")[0].equals(UnitType.RANGE.name());
    }

    @Override
    public UnitType getUnitType() {
        return UnitType.RANGE;
    }
}
