package org.xcyms.strategy.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 存储策略工厂
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
public class StorageStrategyFactory {

    private static final String DEFAULT_STRATEGY = "local";

    private final Map<String, StorageStrategy> strategies;

    public StorageStrategyFactory(List<StorageStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(StorageStrategy::getStorageType, s -> s));
        log.info("存储策略工厂初始化完成，已加载策略: {}", strategies.keySet());
    }

    /**
     * 获取存储策略
     *
     * @param type 存储类型
     * @return org.xcyms.strategy.storage.StorageStrategy
     * @author liu-xu
     * @date 2026/05/11
     */
    public StorageStrategy getStrategy(String type) {
        if (StringUtils.isBlank(type)) {
            log.debug("存储类型为空，使用默认策略: {}", DEFAULT_STRATEGY);
            return strategies.get(DEFAULT_STRATEGY);
        }

        StorageStrategy strategy = strategies.get(type);
        if (strategy == null) {
            log.warn("未找到存储策略: {}，使用默认策略: {}", type, DEFAULT_STRATEGY);
            return strategies.get(DEFAULT_STRATEGY);
        }

        log.debug("使用存储策略: {}", type);
        return strategy;
    }
}
