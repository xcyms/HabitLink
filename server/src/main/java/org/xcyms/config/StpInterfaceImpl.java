package org.xcyms.config;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.xcyms.mapper.RoleMapper;
import org.xcyms.service.IRoleService;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展，对接 Sa-Token
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final IRoleService roleService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return roleService.getRoleKeysByUserId(Long.valueOf(loginId.toString()));
    }
}
