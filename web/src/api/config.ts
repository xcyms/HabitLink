import type * as API from '../types'
import request from '../utils/request.ts'

/**
 * 获取当前用户的生效配置
 */
export function getMyConfigsApi(): Promise<API.SysConfig[]> {
  return request({
    url: '/config/my',
    method: 'get',
  })
}

/**
 * 获取系统默认配置 (管理员)
 */
export function getSystemConfigsApi(): Promise<API.SysConfig[]> {
  return request({
    url: '/config/user/0', // 后端约定 userId 为 0 或 null 时获取系统配置
    method: 'get',
  })
}

/**
 * 获取指定用户的配置 (管理员)
 */
export function getUserConfigsApi(userId: number): Promise<API.SysConfig[]> {
  return request({
    url: `/config/user/${userId}`,
    method: 'get',
  })
}

/**
 * 更新配置 (管理员)
 */
export function updateConfigApi(data: API.SysConfig): Promise<boolean> {
  return request({
    url: '/config/update',
    method: 'post',
    data,
  })
}
