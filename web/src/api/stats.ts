import request from '../utils/request.ts'

/**
 * 获取首页统计数据
 */
export function getDashboardStatsApi(): Promise<any> {
  return request({
    url: '/stats/dashboard',
    method: 'get',
  })
}
