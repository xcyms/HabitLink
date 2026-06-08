import type * as API from '../types'
import request from '../utils/request.ts'

/**
 * 查询某个习惯的打卡记录。
 */
export function getCheckInListApi(habitId: number): Promise<API.CheckInRecord[]> {
  return request({
    url: '/check-in/list',
    method: 'get',
    params: { habitId },
  })
}
