package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xcyms.entity.User;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
