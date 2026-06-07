package org.xcyms.chain.register;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.dto.UserDTO;

import java.util.List;

/**
 * <p>
 * 注册责任链配置类
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterChain {

    private final List<RegisterHandler> handlers;

    public ApiResult<String> handle(UserDTO userDTO) {
        if (handlers == null || handlers.isEmpty()) {
            log.warn("没有注册处理器可用");
            return ApiResult.success("注册成功");
        }

        // 构建责任链：将所有处理器按顺序链接起来
        for (int i = 0; i < handlers.size() - 1; i++) {
            handlers.get(i).setNext(handlers.get(i + 1));
        }

        // 从第一个处理器开始执行，整个责任链会自动传递
        RegisterHandler firstHandler = handlers.get(0);
        log.debug("开始执行注册责任链，共 {} 个处理器", handlers.size());

        ApiResult<String> result = firstHandler.handle(userDTO);

        if (result.getCode() != 200) {
            log.warn("注册校验失败: {}", result.getMessage());
        } else {
            log.info("注册校验全部通过");
        }

        return result;
    }
}