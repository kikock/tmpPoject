package org.jeecg.modules.rule.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 计算策略工厂，用于获取具体的计算策略实例
 * @author: kikock
 * @create_date: 2025-10-12
 **/
@Component
public class CalculationStrategyFactory {

    // Spring会自动将所有实现了CalculationStrategy接口的Bean注入到这个List中
    private final List<CalculationStrategy> strategies;

    private Map<String, CalculationStrategy> strategyMap;

    @Autowired
    public CalculationStrategyFactory(List<CalculationStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 在依赖注入完成后，初始化策略映射表
     */
    @PostConstruct
    public void init() {
        // 使用Java 8 Stream API将List转换为Map
        // Key是策略支持的计算方式(e.g., "1"), Value是策略实例本身
        strategyMap = strategies.stream()
                .collect(Collectors.toMap(CalculationStrategy::getSupportedMethod, Function.identity()));
    }

    /**
     * 根据计算方式的字典值获取对应的策略实例
     *
     * @param calculationMethod 计算方式的字典值 (e.g., "1", "2")
     * @return 对应的策略实例
     * @throws IllegalArgumentException 如果找不到对应的策略
     */
    public CalculationStrategy getStrategy(String calculationMethod) {
        CalculationStrategy strategy = strategyMap.get(calculationMethod);
        if (strategy == null) {
            throw new IllegalArgumentException("未知的计算方式: " + calculationMethod);
        }
        return strategy;
    }
}