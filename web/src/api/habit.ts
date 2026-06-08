import type * as API from '../types'
import request from '../utils/request.ts'

/**
 * 分页查询习惯。
 */
export function getHabitPageApi(
  params: { current: number, size: number },
  data?: { name?: string, category?: string, status?: number, userId?: number },
): Promise<API.PageResult<API.Habit>> {
  return request({
    url: '/habit/page',
    method: 'post',
    params,
    data,
  })
}

/**
 * 获取当前用户习惯列表。
 */
export function getHabitListApi(): Promise<API.Habit[]> {
  return request({
    url: '/habit/list',
    method: 'get',
  })
}

/**
 * 获取习惯详情。
 */
export function getHabitDetailApi(id: number): Promise<API.Habit> {
  return request({
    url: '/habit/detail',
    method: 'get',
    params: { id },
  })
}

/**
 * 创建习惯。
 */
export function createHabitApi(data: API.Habit): Promise<string> {
  return request({
    url: '/habit/create',
    method: 'post',
    data,
  })
}

/**
 * 更新习惯。
 */
export function updateHabitApi(data: API.Habit): Promise<string> {
  return request({
    url: '/habit/update',
    method: 'post',
    data,
  })
}

/**
 * 删除习惯。
 */
export function deleteHabitApi(id: number): Promise<string> {
  return request({
    url: '/habit/delete',
    method: 'post',
    params: { id },
  })
}

/**
 * 暂停习惯。
 */
export function pauseHabitApi(id: number): Promise<string> {
  return request({
    url: '/habit/pause',
    method: 'post',
    params: { id },
  })
}

/**
 * 恢复习惯。
 */
export function resumeHabitApi(id: number): Promise<string> {
  return request({
    url: '/habit/resume',
    method: 'post',
    params: { id },
  })
}
