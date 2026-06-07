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

import java.util.regex.Pattern;

/**
 * <p>
 * 邮箱校验处理器
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailHandler extends AbstractRegisterHandler {

    private final UserMapper userMapper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    protected ApiResult<String> doHandle(UserDTO userDTO) {
        String email = userDTO.getEmail();

        if (StringUtils.isNotBlank(email)) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return ApiResult.error("邮箱格式不正确");
            }

            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            if (count > 0) {
                return ApiResult.error("该邮箱已被注册");
            }
        }

        return ApiResult.success();
    }

    @Override
    public String getHandlerName() {
        return "EmailHandler";
    }
}