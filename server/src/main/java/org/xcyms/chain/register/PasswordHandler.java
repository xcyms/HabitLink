package org.xcyms.chain.register;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.utils.PasswordUtils;


/**
 * <p>
 * 密码校验处理器
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
public class PasswordHandler extends AbstractRegisterHandler {

    @Override
    protected ApiResult<String> doHandle(UserDTO userDTO) {
        String password = userDTO.getPassword();

        if (StringUtils.isBlank(password)) {
            return ApiResult.error("密码不能为空");
        }

        if (PasswordUtils.checkComplexity(password)) {
            return ApiResult.error("密码复杂度不足：需至少8位，包含大小写字母、数字及特殊字符");
        }

        return ApiResult.success();
    }

    @Override
    public String getHandlerName() {
        return "PasswordHandler";
    }
}