package org.xcyms.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.dto.ConfigDTO;
import org.xcyms.service.IConfigService;

import java.util.List;

/**
 * <p>
 * 系统/用户配置表 前端控制器
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-12
 */
@ApiDoc("系统配置")
@RestController
@RequiredArgsConstructor
@RequestMapping("/config")
public class ConfigController {

    private final IConfigService configService;

    @ApiDoc("获取我的配置列表")
    @GetMapping("/my")
    public ApiResult<List<ConfigDTO>> getMyConfigs() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(configService.getUserConfigs(userId));
    }

    @Log("更新系统配置")
    @ApiDoc(value = "更新配置项", notes = "更新系统或用户的配置项", order = 2)
    @PostMapping("/update")
    public ApiResult<Boolean> update(@RequestBody ConfigDTO configDTO) {
        return configService.updateConfig(configDTO);
    }

    @ApiDoc("获取指定用户的配置列表")
    @SaCheckRole("ADMIN")
    @GetMapping("/user/{userId}")
    public ApiResult<List<ConfigDTO>> getUserConfigs(@PathVariable Long userId) {
        return ApiResult.success(configService.getUserConfigs(userId));
    }
}
