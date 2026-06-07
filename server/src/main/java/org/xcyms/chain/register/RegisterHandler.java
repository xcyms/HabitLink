package org.xcyms.chain.register;

import org.xcyms.common.ApiResult;
import org.xcyms.entity.dto.UserDTO;

/**
 * <p>
 * 注册校验处理器接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
public interface RegisterHandler {

    /**
     * 处理注册校验
     *
     * @param userDTO 用户信息
     * @return org.xcyms.common.ApiResult<java.lang.String>
     * @author liu-xu
     * @date 2026/05/11
     */
    ApiResult<String> handle(UserDTO userDTO);

    /**
     * 设置下一个处理器
     *
     * @param next 下一个处理器
     * @author liu-xu
     * @date 2026/05/11
     */
    void setNext(RegisterHandler next);

    /**
     * 获取下一个处理器
     *
     * @return RegisterHandler
     */
    RegisterHandler getNext();

    /**
     * 获取处理器名称
     *
     * @return java.lang.String
     * @author liu-xu
     * @date 2026/05/11
     */
    String getHandlerName();
}