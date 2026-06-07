package org.xcyms.strategy.login;

import org.xcyms.common.ApiResult;
import org.xcyms.entity.dto.LoginDTO;

/**
 * <p>
 * 登录策略接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
public interface LoginStrategy {

    /**
     * 执行登录
     *
     * @param dto 登录信息
     * @return org.xcyms.common.ApiResult<java.lang.String>
     * @author liu-xu
     * @date 2026/05/11
     */
    ApiResult<String> login(LoginDTO dto);

    /**
     * 获取登录类型
     *
     * @return java.lang.String
     * @author liu-xu
     * @date 2026/05/11
     */
    String getLoginType();
}