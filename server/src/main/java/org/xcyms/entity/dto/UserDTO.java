package org.xcyms.entity.dto;

import lombok.Data;
import org.xcyms.common.annotation.ApiDocProperty;

import java.util.List;

/**
 * <p>
 *     用户DTO
 * </p>
 * @author liu-xu
 * @date 2026年01月12日 9:55
 */
@Data
public class UserDTO {

    @ApiDocProperty("用户ID")
    private Long id;

    @ApiDocProperty("用户名")
    private String username;

    @ApiDocProperty("密码 (仅注册/修改时使用)")
    private String password;

    @ApiDocProperty("昵称")
    private String nickname;

    @ApiDocProperty("头像URL")
    private String avatar;

    @ApiDocProperty("邮箱")
    private String email;

    @ApiDocProperty("手机号")
    private String phone;

    @ApiDocProperty("状态 (ENABLE-启用, DISENABLE-禁用)")
    private Integer status;

    @ApiDocProperty("角色列表")
    private List<String> roles;

    @ApiDocProperty("旧密码 (修改密码时使用)")
    private String oldPassword;
}