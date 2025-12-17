package org.jeecg.modules.rule.annotation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.modules.rule.entity.enums.UnitType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * @project_name: 后端平台项目
 * @description: 数值单位的实现-时间单位
 * @author: kikock
 * @create_date: 2025-09-22 14:41
 **/
@Component
@Slf4j
public class TimeConverter implements UnitConverter<Duration> {

    @Resource
    private CommonAPI commonAPI;

    @Override
    public Duration parse(Object value) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("时间转换器只支持字符串类型输入");
        }
        String time = (String) value;
        String timeString = time.trim();
        try {
            // [优化] 使用 BigDecimal 来处理秒，以支持毫秒精度
            BigDecimal totalSeconds;
            if (timeString.contains(":")) {
                String[] parts = timeString.split(":");
                if (parts.length == 2) {
                    // 格式一：处理 mm:ss.SSS
                    BigDecimal minutes = new BigDecimal(parts[0]);
                    BigDecimal seconds = new BigDecimal(parts[1]);
                    totalSeconds = minutes.multiply(BigDecimal.valueOf(60)).add(seconds);
                } else if (parts.length == 3) {
                    // 格式二：处理 HH:mm:ss.SSS
                    BigDecimal hours = new BigDecimal(parts[0]);
                    BigDecimal minutes = new BigDecimal(parts[1]);
                    BigDecimal seconds = new BigDecimal(parts[2]);
                    totalSeconds = hours.multiply(BigDecimal.valueOf(3600))
                            .add(minutes.multiply(BigDecimal.valueOf(60)))
                            .add(seconds);
                } else {
                    throw new IllegalArgumentException("不支持的时间格式: " + timeString);
                }
            } else if (timeString.contains("°") || timeString.contains("'") || timeString.contains("\"")) {
                // 格式三：处理 h°m's.SSS" 或 m's.SSS"
                String cleanedString = timeString
                        .replace("°", ":")
                        .replace("'", ":")
                        .replace("\"", "");

                String[] parts = cleanedString.split(":");
                totalSeconds = BigDecimal.ZERO;
                if (parts.length == 3) {
                    totalSeconds = totalSeconds.add(new BigDecimal(parts[0]).multiply(BigDecimal.valueOf(3600)));
                    totalSeconds = totalSeconds.add(new BigDecimal(parts[1]).multiply(BigDecimal.valueOf(60)));
                    totalSeconds = totalSeconds.add(new BigDecimal(parts[2]));
                } else if (parts.length == 2) {
                    totalSeconds = totalSeconds.add(new BigDecimal(parts[0]).multiply(BigDecimal.valueOf(60)));
                    totalSeconds = totalSeconds.add(new BigDecimal(parts[1]));
                } else if (parts.length == 1) {
                    totalSeconds = totalSeconds.add(new BigDecimal(parts[0]));
                } else {
                    throw new IllegalArgumentException("不支持的时间格式: " + timeString);
                }
            } else {
                // 格式四：直接作为秒数（可带小数）处理
                totalSeconds = new BigDecimal(timeString);
            }
            // [重构] 将总秒数（BigDecimal）转换为 Duration 对象
            long secondsPart = totalSeconds.longValue();
            long nanoPart = totalSeconds.subtract(new BigDecimal(secondsPart))
                    .multiply(new BigDecimal("1000000000"))
                    .longValue();
            return Duration.ofSeconds(secondsPart, nanoPart);
        } catch (Exception e) {
            log.error("时间转换器解析时间字符串{}时出错,直接返回0秒", value);
            return Duration.ZERO;
        }
    }

    @Override
    public BigDecimal convert(Object value) {
        // [修复] 调用 parse 方法得到 Duration，然后精确地转换为总秒数的 BigDecimal，以保留毫秒精度
        Duration duration = parse(value);
        if (duration == null) {
            return BigDecimal.ZERO;
        }
        // 将秒和纳秒部分都转换为 BigDecimal 进行精确计算
        BigDecimal seconds = BigDecimal.valueOf(duration.getSeconds());
        BigDecimal nanos = new BigDecimal(duration.getNano()).divide(new BigDecimal("1000000000"));
        return seconds.add(nanos);
    }

    @Override
    public String formatData(Duration value, String unit) {
        if (value == null) {
            return "";
        }
        // [重构] 将 Duration 精确转换为 "mm:ss.S" 格式的字符串
        long totalMinutes = value.toMinutes();
        long secondsPart = value.getSeconds() % 60;
        int nanoPart = value.getNano();

        // 将纳秒转换为一位小数的毫秒，并进行四舍五入
        long millis = Math.round((double) nanoPart / 100_000_000.0);

        String secondsString;
        if (millis > 0) {
            // 如果毫秒部分进位到10（即1秒），则秒数+1
            if (millis == 10) {
                secondsPart += 1;
                secondsString = String.format("%02d", secondsPart);
            } else {
                secondsString = String.format("%02d.%d", secondsPart, millis);
            }
        } else {
            secondsString = String.format("%02d", secondsPart);
        }

        return String.format("%02d:%s", totalMinutes, secondsString);
    }

    @Override
    public String format(Duration value, String unit) {
        if (value == null) {
            return "";
        }
        // [重构] 使用 Duration 提供的方法来格式化显示字符串毫秒级
        long hours = value.toHours();
        long minutes = value.toMinutes() % 60;
        long seconds = value.getSeconds() % 60;
        int nano = value.getNano();
        if (hours > 0) {
            return String.format("%d时%d分%d秒", hours, minutes, seconds);
        } else {
            if (nano > 0) {
                BigDecimal totalSeconds = BigDecimal.valueOf(seconds).add(new BigDecimal(nano).divide(new BigDecimal("1000000000"), 3, RoundingMode.HALF_UP));
                return String.format("%d分%s秒", minutes, totalSeconds.stripTrailingZeros().toPlainString());
            }
            return String.format("%d分%d秒", minutes, seconds);
        }
    }

    @Override
    public boolean supports(String unit) {
        String one = commonAPI.translateDict("lesson_unit", unit);
        return StringUtils.isNotBlank(one) && unit.split("-")[0].equals(UnitType.TIME.name());
    }

    @Override
    public UnitType getUnitType() {
        return UnitType.TIME;
    }

    public static void main(String[] args) {


        TimeConverter converter = new TimeConverter();
        Duration parse = converter.parse("98.5");
        String format = converter.formatData(parse, "TIME");
        Duration parse2 = converter.parse("95:5.5");
        String format2 = converter.format(parse2, "TIME");
        String format3 = converter.formatData(parse2, "TIME");
        System.out.println("Input '98.5' is converted to: " + format);
        System.out.println("Input '98.5' is converted to: " + format2);
        System.out.println("Input '98.5' is converted to: " + format3);
    }
}