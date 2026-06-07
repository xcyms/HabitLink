package org.xcyms.strategy.login;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 登录策略工厂
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
public class LoginStrategyFactory {

    private static final String DEFAULT_STRATEGY = "password";

    private final Map<String, LoginStrategy> strategies;

    public LoginStrategyFactory(List<LoginStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(LoginStrategy::getLoginType, s -> s));
        log.info("登录策略工厂初始化完成，已加载策略: {}", strategies.keySet());
    }

    /**
     * 获取登录策略
     *
     * @param type 登录类型
     * @return org.xcyms.strategy.login.LoginStrategy
     * @author liu-xu
     * @date 2026/05/11
     */
    public LoginStrategy getStrategy(String type) {
        if (StringUtils.isBlank(type)) {
            log.debug("登录类型为空，使用默认策略: {}", DEFAULT_STRATEGY);
            return strategies.get(DEFAULT_STRATEGY);
        }

        LoginStrategy strategy = strategies.get(type);
        if (strategy == null) {
            log.warn("未找到登录策略: {}，使用默认策略: {}", type, DEFAULT_STRATEGY);
            return strategies.get(DEFAULT_STRATEGY);
        }

        log.debug("使用登录策略: {}", type);
        return strategy;
    }
}