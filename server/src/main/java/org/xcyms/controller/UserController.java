package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.service.IUserService;

@ApiDoc("用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;

    @Log("用户注册")
    @ApiDoc(value = "用户注册", notes = "新用户注册账号", order = 1)
    @PostMapping("/register")
    @SaIgnore
    public ApiResult<String> register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }

    @Log("用户登录")
    @ApiDoc(value = "用户登录", notes = "使用用户名和密码登录", order = 2)
    @PostMapping("/login")
    @SaIgnore
    public ApiResult<String> login(@RequestBody LoginDTO loginDto) {
        return userService.login(loginDto);
    }

    @Log("退出登录")
    @ApiDoc(value = "退出登录", order = 3)
    @GetMapping("/logout")
    public ApiResult<String> logout() {
        StpUtil.logout();
        return ApiResult.success("退出成功");
    }

    @ApiDoc(value = "获取个人信息", notes = "获取当前登录用户的详细信息", order = 4)
    @GetMapping("/info")
    public ApiResult<UserDTO> getUserInfo() {
        return userService.getUserInfo(StpUtil.getLoginIdAsLong());
    }

    @ApiDoc("通过用户名搜索用户")
    @GetMapping("/search")
    public ApiResult<UserDTO> search(@RequestParam String username) {
        return userService.getUserByUsername(username);
    }

    @ApiDoc(value = "分页查询用户", notes = "管理员查看用户列表", order = 5)
    @SaCheckRole("ADMIN")
    @PostMapping("/page")
    public ApiResult<Page<UserDTO>> getPage(Page<User> page, @RequestBody UserDTO userDTO) {
        page.addOrder(OrderItem.desc("id"));
        return userService.getPage(page, userDTO);
    }

    @Log("更新个人资料")
    @ApiDoc("更新个人资料")
    @PostMapping("/update")
    public ApiResult<String> updateProfile(@RequestBody UserDTO userDTO) {
        return userService.updateProfile(userDTO);
    }

    @Log("修改密码")
    @ApiDoc("修改密码")
    @PostMapping("/password")
    public ApiResult<String> updatePassword(@RequestBody UserDTO userDTO) {
        return userService.updatePassword(userDTO);
    }

    @Log("管理员更新用户信息")
    @ApiDoc(value = "管理员更新用户信息", notes = "管理员可以更新任意用户的基本信息")
    @SaCheckRole("ADMIN")
    @PostMapping("/admin/update")
    public ApiResult<String> updateUserByAdmin(@RequestBody UserDTO userDTO) {
        return userService.updateUserByAdmin(userDTO);
    }

    @Log("管理员重置密码")
    @ApiDoc(value = "管理员重置密码", notes = "管理员为指定用户重置登录密码")
    @SaCheckRole("ADMIN")
    @PostMapping("/admin/reset-password")
    public ApiResult<String> resetPasswordByAdmin(@RequestBody UserDTO userDTO) {
        return userService.resetPasswordByAdmin(userDTO);
    }
}
