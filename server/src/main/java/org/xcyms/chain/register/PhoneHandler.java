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
 * 手机号校验处理器
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhoneHandler extends AbstractRegisterHandler {

    private final UserMapper userMapper;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    protected ApiResult<String> doHandle(UserDTO userDTO) {
        String phone = userDTO.getPhone();

        if (StringUtils.isNotBlank(phone)) {
            if (!PHONE_PATTERN.matcher(phone).matches()) {
                return ApiResult.error("手机号格式不正确");
            }

            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, phone));
            if (count > 0) {
                return ApiResult.error("该手机号已被注册");
            }
        }

        return ApiResult.success();
    }

    @Override
    public String getHandlerName() {
        return "PhoneHandler";
    }
}