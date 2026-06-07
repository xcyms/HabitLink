package org.xcyms.chain.register;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.User;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.mapper.UserMapper;

/**
 * <p>
 * 用户名校验处理器
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameHandler extends AbstractRegisterHandler {

    private final UserMapper userMapper;

    @Override
    protected ApiResult<String> doHandle(UserDTO userDTO) {
        String username = userDTO.getUsername();

        if (StringUtils.isBlank(username)) {
            return ApiResult.error("用户名不能为空");
        }

        if (username.length() < 3 || username.length() > 20) {
            return ApiResult.error("用户名长度需在3-20个字符之间");
        }

        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (count > 0) {
            return ApiResult.error("用户名已存在");
        }

        return ApiResult.success();
    }

    @Override
    public String getHandlerName() {
        return "UsernameHandler";
    }
}