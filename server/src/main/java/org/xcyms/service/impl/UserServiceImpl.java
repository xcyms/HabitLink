package org.xcyms.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xcyms.chain.register.RegisterChain;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.entity.User;
import org.xcyms.entity.UserRole;
import org.xcyms.entity.dto.LoginDTO;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.mapper.RoleMapper;
import org.xcyms.mapper.UserMapper;
import org.xcyms.mapper.UserRoleMapper;
import org.xcyms.service.IRoleService;
import org.xcyms.service.IUserService;
import org.xcyms.strategy.login.LoginStrategyFactory;
import org.xcyms.utils.PasswordUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final ModelMapper mapper;
    private final IRoleService roleService;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final RegisterChain registerChain;
    private final LoginStrategyFactory loginStrategyFactory;

    @Override
    @CacheEvict(value = Constant.Cache.STATS, key = "'dashboard'")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> register(UserDTO userDTO) {
        ApiResult<String> validateResult = registerChain.handle(userDTO);
        if (validateResult.getCode() != 200) {
            return validateResult;
        }

        String secretPassword = SaSecureUtil.md5BySalt(userDTO.getPassword(), Constant.SALT);
        userDTO.setPassword(secretPassword);
        userDTO.setNickname("用户" + RandomUtil.randomNumbers(4));

        User user = mapper.map(userDTO, User.class);
        baseMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(Constant.Role.USER);
        userRoleMapper.insert(userRole);

        return ApiResult.success("注册成功");
    }

    @Override
    public ApiResult<String> login(LoginDTO loginDto) {
        return loginStrategyFactory.getStrategy(loginDto.getLoginType()).login(loginDto);
    }

    @Override
    public ApiResult<UserDTO> getUserInfo(Long loginId) {
        User user = baseMapper.selectById(loginId);
        UserDTO userDto = mapper.map(user, UserDTO.class);
        userDto.setPassword(null);
        userDto.setEmail(null);
        userDto.setPhone(null);
        userDto.setRoles(roleService.getRoleKeysByUserId(loginId));
        return ApiResult.success(userDto);
    }

    @Override
    public ApiResult<UserDTO> getUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return ApiResult.error("用户名不能为空");
        }
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return ApiResult.error("未找到该用户");
        }
        UserDTO userDto = mapper.map(user, UserDTO.class);
        userDto.setRoles(roleService.getRoleKeysByUserId(user.getId()));
        return ApiResult.success(userDto);
    }

    @Override
    public ApiResult<Page<UserDTO>> getPage(Page<User> page, UserDTO userDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userDTO != null) {
            wrapper.like(StringUtils.isNotBlank(userDTO.getUsername()), User::getUsername, userDTO.getUsername())
                    .like(StringUtils.isNotBlank(userDTO.getNickname()), User::getNickname, userDTO.getNickname())
                    .eq(userDTO.getStatus() != null, User::getStatus, userDTO.getStatus());
        }

        Page<User> userPage = this.page(page, wrapper);
        List<Long> userIds = userPage.getRecords().stream()
                .map(User::getId)
                .toList();
        Map<Long, List<String>> roleKeysByUserId = buildRoleKeysByUserId(userIds);

        List<UserDTO> dtos = userPage.getRecords().stream().map(user -> {
            UserDTO dto = mapper.map(user, UserDTO.class);
            dto.setRoles(roleKeysByUserId.getOrDefault(user.getId(), List.of()));
            return dto;
        }).collect(Collectors.toList());

        Page<UserDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), userPage.getTotal());
        resultPage.setRecords(dtos);
        return ApiResult.success(resultPage);
    }

    /**
     * 按页批量加载角色，避免用户分页时逐条查询角色带来的 N+1 问题。
     */
    private Map<Long, List<String>> buildRoleKeysByUserId(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        return roleMapper.getRoleKeysByUserIds(userIds).stream()
                .filter(item -> item.get("userId") != null && item.get("roleKey") != null)
                .collect(Collectors.groupingBy(
                        item -> ((Number) item.get("userId")).longValue(),
                        Collectors.mapping(item -> String.valueOf(item.get("roleKey")), Collectors.toList())
                ));
    }

    @Override
    public ApiResult<String> updateProfile(UserDTO userDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.error("用户不存在");
        }
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        baseMapper.updateById(user);
        return ApiResult.success("资料更新成功");
    }

    @Override
    public ApiResult<String> updatePassword(UserDTO userDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return ApiResult.error("用户不存在");
        }
        if (PasswordUtils.checkComplexity(userDTO.getPassword())) {
            return ApiResult.error("密码复杂度不足：需至少8位，包含大小写字母、数字及特殊字符");
        }

        String oldSecretPassword = SaSecureUtil.md5BySalt(userDTO.getOldPassword(), Constant.SALT);
        if (!user.getPassword().equals(oldSecretPassword)) {
            return ApiResult.error("原密码错误");
        }

        String newSecretPassword = SaSecureUtil.md5BySalt(userDTO.getPassword(), Constant.SALT);
        user.setPassword(newSecretPassword);
        baseMapper.updateById(user);
        return ApiResult.success("密码修改成功");
    }

    @Override
    public ApiResult<String> updateUserByAdmin(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            return ApiResult.error("用户ID不能为空");
        }
        User user = baseMapper.selectById(userDTO.getId());
        if (user == null) {
            return ApiResult.error("用户不存在");
        }
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }
        baseMapper.updateById(user);
        return ApiResult.success("用户信息更新成功");
    }

    @Override
    public ApiResult<String> resetPasswordByAdmin(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            return ApiResult.error("用户ID不能为空");
        }
        if (StringUtils.isBlank(userDTO.getPassword())) {
            return ApiResult.error("新密码不能为空");
        }
        if (PasswordUtils.checkComplexity(userDTO.getPassword())) {
            return ApiResult.error("密码复杂度不足：需至少8位，包含大小写字母、数字及特殊字符");
        }

        User user = baseMapper.selectById(userDTO.getId());
        if (user == null) {
            return ApiResult.error("用户不存在");
        }

        user.setPassword(SaSecureUtil.md5BySalt(userDTO.getPassword(), Constant.SALT));
        baseMapper.updateById(user);
        return ApiResult.success("密码已重置");
    }
}
