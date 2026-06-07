package org.xcyms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xcyms.entity.UserRole;
import org.xcyms.mapper.UserRoleMapper;
import org.xcyms.service.IUserRoleService;

/**
 * <p>
 *     用户角色服务实现类
 * </p>
 *
 * @author liu-xu
 * @date 2026年03月17日 10:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {
}
