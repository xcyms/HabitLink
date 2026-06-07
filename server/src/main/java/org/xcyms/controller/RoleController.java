package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.Role;
import org.xcyms.entity.dto.RoleDTO;
import org.xcyms.entity.dto.UserDTO;
import org.xcyms.service.IRoleService;

import java.util.List;

@ApiDoc("角色管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/role")
@Slf4j
public class RoleController {

    private final IRoleService roleService;

    @ApiDoc(value = "获取角色列表", notes = "获取所有角色列表", order = 1)
    @SaCheckRole("ADMIN")
    @GetMapping("/list")
    public ApiResult<List<Role>> getList() {
        return ApiResult.success(roleService.list());
    }

    @ApiDoc(value = "分页查询角色", notes = "管理员分页查看角色列表", order = 2)
    @SaCheckRole("ADMIN")
    @PostMapping("/page")
    public ApiResult<IPage<RoleDTO>> getPage(Page<Role> page, @RequestBody RoleDTO roleDTO) {
        return ApiResult.success(roleService.getPage(page, roleDTO));
    }

    @ApiDoc(value = "获取角色详情", notes = "根据 ID 获取角色详细信息", order = 3)
    @SaCheckRole("ADMIN")
    @GetMapping("/{id}")
    public ApiResult<Role> get(@PathVariable Long id) {
        return ApiResult.success(roleService.getById(id));
    }

    @ApiDoc(value = "获取角色成员", notes = "查看指定角色下的用户列表", order = 4)
    @SaCheckRole("ADMIN")
    @GetMapping("/{id}/users")
    public ApiResult<List<UserDTO>> getUsersByRole(@PathVariable Long id) {
        return ApiResult.success(roleService.getUsersByRoleId(id));
    }

    @Log("创建角色")
    @ApiDoc(value = "创建角色", notes = "新增角色", order = 5)
    @SaCheckRole("ADMIN")
    @PostMapping
    public ApiResult<String> create(@RequestBody Role role) {
        return roleService.create(role);
    }

    @Log("更新角色")
    @ApiDoc(value = "更新角色", notes = "更新角色信息", order = 6)
    @SaCheckRole("ADMIN")
    @PutMapping
    public ApiResult<String> update(@RequestBody Role role) {
        return roleService.update(role);
    }

    @Log("删除角色")
    @ApiDoc(value = "删除角色", notes = "根据 ID 删除角色", order = 7)
    @SaCheckRole("ADMIN")
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable Long id) {
        return roleService.delete(id);
    }

    @ApiDoc(value = "分配用户角色", notes = "为指定用户分配角色", order = 8)
    @SaCheckRole("ADMIN")
    @PostMapping("/assign/{userId}")
    public ApiResult<String> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        try {
            roleService.assignRolesToUser(userId, roleIds);
            return ApiResult.success("角色分配成功");
        }
        catch (Exception e) {
            log.error("角色分配失败", e);
            return ApiResult.error("角色分配失败: " + e.getMessage());
        }
    }
}
