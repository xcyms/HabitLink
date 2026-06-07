package org.xcyms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.entity.dto.UserDTO;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
public interface IUserService extends IService<User> {

    ApiResult<String> register(UserDTO userDTO);

    ApiResult<String> login(LoginDTO loginDto);

    ApiResult<UserDTO> getUserInfo(Long id);

    ApiResult<UserDTO> getUserByUsername(String username);

    ApiResult<Page<UserDTO>> getPage(Page<User> page, UserDTO userDTO);

    ApiResult<String> updateProfile(UserDTO userDTO);

    ApiResult<String> updatePassword(UserDTO userDTO);

    ApiResult<String> updateUserByAdmin(UserDTO userDTO);

    ApiResult<String> resetPasswordByAdmin(UserDTO userDTO);
}
