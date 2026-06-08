import type * as API from '../types'
import request from '../utils/request.ts'

/**
 * 获取今日总览。
 */
export function getTodayOverviewApi(): Promise<API.TodayOverview> {
  return request({
    url: '/habit-stats/today-overview',
    method: 'get',
  })
}

/**
 * 获取单个习惯统计详情。
 */
export function getHabitStatsDetailApi(habitId: number): Promise<API.HabitStats> {
  return request({
    url: '/habit-stats/detail',
    method: 'get',
    params: { habitId },
  })
}
