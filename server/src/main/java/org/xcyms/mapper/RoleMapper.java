package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xcyms.entity.Role;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色表Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT r.role_key FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> getRoleKeysByUserId(Long userId);

    @Select({
            "<script>",
            "SELECT ur.user_id AS userId, r.role_key AS roleKey ",
            "FROM sys_role r ",
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id ",
            "WHERE ur.user_id IN ",
            "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>",
            "#{userId}",
            "</foreach>",
            "</script>"
    })
    List<Map<String, Object>> getRoleKeysByUserIds(@Param("userIds") List<Long> userIds);
}
