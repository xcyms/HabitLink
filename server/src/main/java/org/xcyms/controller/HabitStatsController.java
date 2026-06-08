package org.xcyms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xcyms.common.ApiResult;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.entity.dto.HabitStatsDTO;
import org.xcyms.entity.dto.TodayOverviewDTO;
import org.xcyms.service.IHabitStatsService;

/**
 * 首页总览与习惯统计接口控制器。
 */
@ApiDoc("习惯统计")
@RestController
@RequiredArgsConstructor
@RequestMapping("/habit-stats")
public class HabitStatsController {

    private final IHabitStatsService habitStatsService;

    @ApiDoc(value = "获取今日总览", notes = "获取当前登录用户首页所需的今日总览数据", order = 1)
    @GetMapping("/today-overview")
    public ApiResult<TodayOverviewDTO> todayOverview() {
        return habitStatsService.getTodayOverview();
    }

    @ApiDoc(value = "获取习惯统计详情", notes = "获取当前登录用户某个习惯的统计详情", order = 2)
    @GetMapping("/detail")
    public ApiResult<HabitStatsDTO> detail(@RequestParam Long habitId) {
        return habitStatsService.getHabitStats(habitId);
    }
}
