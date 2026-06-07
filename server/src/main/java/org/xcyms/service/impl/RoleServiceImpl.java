package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Role;
import org.xcyms.entity.UserRole;
import org.xcyms.entity.dto.RoleDTO;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.mapper.RoleMapper;
import org.xcyms.mapper.UserMapper;
import org.xcyms.mapper.UserRoleMapper;
import org.xcyms.service.IRoleService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    private final UserRoleMapper userRoleMapper;
    private final UserMapper userMapper;
    private final ModelMapper modelMapper;

    @Override
    @Cacheable(value = "roleKeys", key = "#loginId")
    public List<String> getRoleKeysByUserId(Long loginId) {
        return this.baseMapper.getRoleKeysByUserId(loginId);
    }

    @Override
    public IPage<RoleDTO> getPage(Page<Role> page, RoleDTO roleDTO) {
        IPage<Role> rolePage = this.page(page, new QueryWrapper<Role>()
                .lambda()
                .like(StringUtils.isNotBlank(roleDTO.getRoleName()), Role::getRoleName, roleDTO.getRoleName())
                .like(StringUtils.isNotBlank(roleDTO.getRoleKey()), Role::getRoleKey, roleDTO.getRoleKey())
                .orderByDesc(Role::getCreateTime, Role::getId));

        Map<Long, Long> userCountMap = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>())
                .stream()
                .collect(Collectors.groupingBy(UserRole::getRoleId, Collectors.counting()));

        return rolePage.convert(role -> {
            RoleDTO dto = modelMapper.map(role, RoleDTO.class);
            dto.setUserCount(userCountMap.getOrDefault(role.getId(), 0L));
            return dto;
        });
    }

    @Override
    @CacheEvict(value = "roleKeys", allEntries = true)
    public ApiResult<String> create(Role role) {
        if (this.save(role)) {
            return ApiResult.success("创建成功");
        }
        return ApiResult.error("创建失败");
    }

    @Override
    @CacheEvict(value = "roleKeys", allEntries = true)
    public ApiResult<String> update(Role role) {
        if (this.updateById(role)) {
            return ApiResult.success("更新成功");
        }
        return ApiResult.error("更新失败");
    }

    @Override
    @CacheEvict(value = "roleKeys", allEntries = true)
    public ApiResult<String> delete(Long id) {
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getRoleId, id));
        if (userRoles != null && !userRoles.isEmpty()) {
            return ApiResult.error("该角色下有用户，无法删除");
        }
        if (this.removeById(id)) {
            return ApiResult.success("删除成功");
        }
        return ApiResult.error("删除失败");
    }

    @Override
    @CacheEvict(value = "roleKeys", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));

        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }

        if (StpUtil.isLogin() && StpUtil.getLoginIdAsLong() == userId) {
            StpUtil.logout();
        }
    }

    @Override
    public List<UserDTO> getUsersByRoleId(Long roleId) {
        List<Long> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getRoleId, roleId))
                .stream()
                .map(UserRole::getUserId)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return List.of();
        }

        return userMapper.selectBatchIds(userIds).stream().map(user -> {
            UserDTO dto = modelMapper.map(user, UserDTO.class);
            dto.setRoles(getRoleKeysByUserId(user.getId()));
            return dto;
        }).collect(Collectors.toList());
    }
}
