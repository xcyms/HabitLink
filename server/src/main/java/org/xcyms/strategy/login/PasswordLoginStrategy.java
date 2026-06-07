package org.xcyms.strategy.login;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.mapper.UserMapper;

/**
 * <p>
 * 密码登录策略
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final UserMapper userMapper;

    @Override
    public ApiResult<String> login(LoginDTO dto) {
        log.debug("使用密码登录策略，用户名: {}", dto.getUsername());

        // 1. 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));

        if (user == null) {
            log.warn("用户不存在: {}", dto.getUsername());
            return ApiResult.error("用户不存在");
        }

        // 2. 校验密码
        String secretPassword = SaSecureUtil.md5BySalt(dto.getPassword(), Constant.SALT);
        if (!user.getPassword().equals(secretPassword)) {
            log.warn("密码错误，用户名: {}", dto.getUsername());
            return ApiResult.error("密码错误");
        }

        // 3. 执行 Sa-Token 登录
        StpUtil.login(user.getId());
        StpUtil.getSession().set("username", user.getUsername());

        // 4. 返回 Token 信息
        log.info("登录成功，用户ID: {}", user.getId());
        return ApiResult.success(StpUtil.getTokenInfo().getTokenValue());
    }

    @Override
    public String getLoginType() {
        return "password";
    }
}