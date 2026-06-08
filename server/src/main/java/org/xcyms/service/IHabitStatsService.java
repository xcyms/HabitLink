package org.xcyms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.HabitStats;
import org.xcyms.entity.dto.HabitStatsDTO;
import org.xcyms.entity.dto.TodayOverviewDTO;

/**
 * 首页总览与习惯统计服务接口。
 */
public interface IHabitStatsService extends IService<HabitStats> {

    /**
     * 构建当前用户的今日首页总览。
     *
     * @return 今日总览 DTO
     */
    ApiResult<TodayOverviewDTO> getTodayOverview();

    /**
     * 获取当前用户某个习惯的统计详情。
     *
     * @param habitId 习惯ID
     * @return 习惯统计 DTO
     */
    ApiResult<HabitStatsDTO> getHabitStats(Long habitId);
}
