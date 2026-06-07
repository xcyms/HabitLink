package org.xcyms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.Config;
import org.xcyms.entity.dto.ConfigDTO;

import java.util.List;

/**
 * <p>
 * 系统/用户配置表 服务类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
public interface IConfigService extends IService<Config> {

    /**
     * 获取配置值 (支持用户覆盖默认)
     *
     * @param userId 用户ID (可以为null，为null则仅查默认配置)
     * @param key    配置键
     * @return 配置值
     */
    String getConfigValue(Long userId, String key);

    /**
     * 获取用户的所有生效配置 (合并默认配置与个性化配置)
     */
    List<ConfigDTO> getUserConfigs(Long userId);

    ApiResult<Boolean> updateConfig(ConfigDTO configDTO);
}
