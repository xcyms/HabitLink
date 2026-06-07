package org.xcyms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.entity.dto.DashboardStatsDTO;
import org.xcyms.service.IStatsService;

/**
 * 统计管理控制器
 */
@ApiDoc("统计管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatsController {

    private final IStatsService statsService;

    @ApiDoc(value = "获取首页统计数据", notes = "获取管理端首页看板所需的各项统计数据")
    @GetMapping("/dashboard")
    public ApiResult<DashboardStatsDTO> getDashboardStats() {
        return ApiResult.success(statsService.getDashboardStats());
    }
}
