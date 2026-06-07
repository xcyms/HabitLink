import request from '../utils/request.ts'
import type * as API from '../types'

/**
 * 分页查询操作日志
 */
export function getLogPageApi(params: { current: number, size: number }, data?: any): Promise<API.PageResult<any>> {
  return request({
    url: '/log/page',
    method: 'post',
    params,
    data,
  })
}
