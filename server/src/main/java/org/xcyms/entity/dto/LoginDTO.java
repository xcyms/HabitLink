package org.xcyms.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

/**
 * <p>
 *     登录DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月11日 15:33
 */
@Data
public class LoginDTO {

    @ApiDocProperty(value = "用户名", required = true, example = "admin")
    @NotNull(message = "用户名不能为空")
    private String username;

    @ApiDocProperty(value = "密码", required = true, example = "123456")
    @NotNull(message = "密码不能为空")
    private String password;

    @ApiDocProperty(value = "验证码", example = "1234")
    private String code;

    @ApiDocProperty(value = "登录类型", example = "password")
    private String loginType;
}