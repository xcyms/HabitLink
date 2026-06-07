
package org.xcyms.chain.register;

import org.xcyms.common.ApiResult;
import org.xcyms.entity.dto.UserDTO;

/**
 * <p>
 * 注册处理器抽象基类
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
public abstract class AbstractRegisterHandler implements RegisterHandler {

    protected RegisterHandler next;

    @Override
    public void setNext(RegisterHandler next) {
        this.next = next;
    }

    @Override
    public RegisterHandler getNext() {
        return this.next;
    }

    @Override
    public ApiResult<String> handle(UserDTO userDTO) {
        ApiResult<String> result = doHandle(userDTO);

        if (result.getCode() != 200) {
            return result; // 当前处理器校验失败，返回错误结果
        }

        // 当前处理器校验通过，传递给下一个处理器
        if (next != null) {
            return next.handle(userDTO);
        }

        return result; // 没有下一个处理器，返回当前结果
    }

    /**
     * 具体的处理逻辑，由子类实现
     */
    protected abstract ApiResult<String> doHandle(UserDTO userDTO);
}