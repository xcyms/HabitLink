package org.xcyms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Role;
import org.xcyms.entity.dto.RoleDTO;
import org.xcyms.entity.dto.UserDTO;

import java.util.List;

/**
 * <p>
 * 角色服务接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
public interface IRoleService extends IService<Role> {

    List<String> getRoleKeysByUserId(Long loginId);

    IPage<RoleDTO> getPage(Page<Role> page, RoleDTO roleDTO);

    ApiResult<String> create(Role role);

    ApiResult<String> update(Role role);

    ApiResult<String> delete(Long id);

    void assignRolesToUser(Long userId, List<Long> roleIds);

    List<UserDTO> getUsersByRoleId(Long roleId);
}
