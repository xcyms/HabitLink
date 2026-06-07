package org.xcyms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.entity.Config;
import org.xcyms.entity.dto.ConfigDTO;
import org.xcyms.mapper.ConfigMapper;
import org.xcyms.service.IConfigService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统/用户配置表 服务实现类
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements IConfigService {

    private final ModelMapper modelMapper;

    @Override
    @Cacheable(value = Constant.Cache.CONFIG, key = "'val:' + (#userId == null || #userId == 0 ? 'sys' : #userId) + ':' + #key", unless = "#result == null")
    public String getConfigValue(Long userId, String key) {
        // 归一化 userId: 0 视为 null (系统配置)
        final Long finalUserId = (userId != null && userId == 0) ? null : userId;

        // 1. 优先查用户定制配置
        if (finalUserId != null) {
            Config userConfig = this.getOne(new LambdaQueryWrapper<Config>()
                    .eq(Config::getUserId, finalUserId)
                    .eq(Config::getConfigKey, key));
            if (userConfig != null) {
                return userConfig.getConfigValue();
            }
        }

        // 2. 查系统默认配置 (userId 为 null)
        Config defaultConfig = this.getOne(new LambdaQueryWrapper<Config>()
                .isNull(Config::getUserId)
                .eq(Config::getConfigKey, key));

        return defaultConfig != null ? defaultConfig.getConfigValue() : null;
    }

    @Override
    @Cacheable(value = Constant.Cache.CONFIG, key = "'list:' + (#userId == null ? 'sys' : #userId)", unless = "#result == null")
    public List<ConfigDTO> getUserConfigs(Long userId) {
        // 1. 获取所有系统默认配置
        List<Config> defaultConfigs = this.list(new LambdaQueryWrapper<Config>().isNull(Config::getUserId));

        if (userId == null) {
            return defaultConfigs.stream()
                    .map(c -> modelMapper.map(c, ConfigDTO.class))
                    .collect(Collectors.toList());
        }

        // 2. 获取该用户的定制配置
        List<Config> userConfigs = this.list(new LambdaQueryWrapper<Config>().eq(Config::getUserId, userId));
        Map<String, Config> userConfigMap = userConfigs.stream()
                .collect(Collectors.toMap(Config::getConfigKey, c -> c));

        // 3. 合并：用户配置覆盖默认配置
        return defaultConfigs.stream().map(dc -> {
            if (userConfigMap.containsKey(dc.getConfigKey())) {
                // 将用户配置的Config对象也转换为ConfigDTO对象
                return modelMapper.map(userConfigMap.get(dc.getConfigKey()), ConfigDTO.class);
            }
            return modelMapper.map(dc, ConfigDTO.class);
        }).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = Constant.Cache.CONFIG, allEntries = true)
    public ApiResult<Boolean> updateConfig(ConfigDTO configDTO) {
        Long targetUserId = configDTO.getUserId();
        // 归一化 userId: 0 视为 null (系统配置)
        if (targetUserId != null && targetUserId == 0) {
            targetUserId = null;
        }

        String key = configDTO.getConfigKey();

        // 查找该目标用户(或系统)是否已有该 key 的配置记录
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<Config>()
                .eq(Config::getConfigKey, key);
        if (targetUserId == null) {
            wrapper.isNull(Config::getUserId);
        } else {
            wrapper.eq(Config::getUserId, targetUserId);
        }

        Config existingConfig = this.getOne(wrapper);
        Config configToSave = modelMapper.map(configDTO, Config.class);
        configToSave.setUserId(targetUserId);

        if (existingConfig != null) {
            // 如果已存在该用户的配置记录，则更新它
            configToSave.setId(existingConfig.getId());
            return ApiResult.success(this.updateById(configToSave));
        } else {
            // 如果不存在（即目前使用的是系统默认值），则为该用户新增一条配置记录
            configToSave.setId(null);
            return ApiResult.success(this.save(configToSave));
        }
    }
}