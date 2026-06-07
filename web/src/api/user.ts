import type * as API from '../types'
import request from '../utils/request.ts'

export function loginApi(data: API.LoginRequest): Promise<string> {
  return request({
    url: '/user/login',
    method: 'post',
    data,
  })
}

export function registerApi(data: API.RegisterRequest): Promise<string> {
  return request({
    url: '/user/register',
    method: 'post',
    data,
  })
}

export function logoutApi(): Promise<string> {
  return request({
    url: '/user/logout',
    method: 'get',
  })
}

export function getUserInfoApi(): Promise<API.User> {
  return request({
    url: '/user/info',
    method: 'get',
  })
}

export function updateProfileApi(data: Partial<API.User>): Promise<API.User> {
  return request({
    url: '/user/update',
    method: 'post',
    data,
  })
}

export function updatePasswordApi(data: { oldPassword: string, password: string }): Promise<string> {
  return request({
    url: '/user/password',
    method: 'post',
    data,
  })
}

export function getUserListApi(
  params: { current: number, size: number },
  data?: { username?: string, nickname?: string, status?: number },
): Promise<API.PageResult<API.User>> {
  return request({
    url: '/user/page',
    method: 'post',
    params,
    data,
  })
}

export function updateUserByAdminApi(data: API.User): Promise<string> {
  return request({
    url: '/user/admin/update',
    method: 'post',
    data,
  })
}

export function resetPasswordByAdminApi(data: { id: number, password: string }): Promise<string> {
  return request({
    url: '/user/admin/reset-password',
    method: 'post',
    data,
  })
}
