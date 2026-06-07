package org.xcyms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xcyms.entity.Config;

/**
 * <p>
 * 系统/用户配置表 Mapper 接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {

}
