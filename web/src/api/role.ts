import request from '../utils/request.ts'
import type * as API from '../types'

export function getRoleListApi(): Promise<API.Role[]> {
  return request({
    url: '/role/list',
    method: 'get',
  })
}

export function getRolePageApi(params: { current: number, size: number }, data?: API.Role): Promise<API.PageResult<API.Role>> {
  return request({
    url: '/role/page',
    method: 'post',
    params,
    data,
  })
}

export function getRoleApi(id: number): Promise<API.Role> {
  return request({
    url: `/role/${id}`,
    method: 'get',
  })
}

export function getRoleUsersApi(id: number): Promise<API.User[]> {
  return request({
    url: `/role/${id}/users`,
    method: 'get',
  })
}

export function createRoleApi(data: API.Role): Promise<string> {
  return request({
    url: '/role',
    method: 'post',
    data,
  })
}

export function updateRoleApi(data: API.Role): Promise<string> {
  return request({
    url: '/role',
    method: 'put',
    data,
  })
}

export function deleteRoleApi(id: number): Promise<string> {
  return request({
    url: `/role/${id}`,
    method: 'delete',
  })
}

export function assignRoleApi(roleIds: number[]): Promise<string> {
  return request({
    url: '/role/assign',
    method: 'post',
    data: roleIds,
  })
}

export function assignUserRolesApi(userId: number, roleIds: number[]): Promise<string> {
  return request({
    url: `/role/assign/${userId}`,
    method: 'post',
    data: roleIds,
  })
}
