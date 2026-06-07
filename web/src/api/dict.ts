import request from '../utils/request.ts'
import type * as API from '../types'

/**
 * 字典类型接口
 */

/**
 * 分页查询字典类型
 */
export function getDictPageApi(params: { current: number, size: number }, data?: API.Dict): Promise<API.PageResult<API.Dict>> {
  return request({
    url: '/dict/page',
    method: 'post',
    params,
    data,
  })
}

/**
 * 获取所有字典类型列表
 */
export function getDictListApi(): Promise<API.Dict[]> {
  return request({
    url: '/dict/list',
    method: 'get',
  })
}

/**
 * 获取字典类型详情
 */
export function getDictApi(id: number): Promise<API.Dict> {
  return request({
    url: `/dict/${id}`,
    method: 'get',
  })
}

/**
 * 新增字典类型
 */
export function createDictApi(data: API.Dict): Promise<string> {
  return request({
    url: '/dict',
    method: 'post',
    data,
  })
}

/**
 * 更新字典类型
 */
export function updateDictApi(data: API.Dict): Promise<string> {
  return request({
    url: '/dict',
    method: 'put',
    data,
  })
}

/**
 * 删除字典类型
 */
export function deleteDictApi(id: number): Promise<string> {
  return request({
    url: `/dict/${id}`,
    method: 'delete',
  })
}

/**
 * 字典数据接口
 */

/**
 * 分页查询字典数据
 */
export function getDictDataPageApi(params: { current: number, size: number }, data?: API.DictData): Promise<API.PageResult<API.DictData>> {
  return request({
    url: '/dict/data/page',
    method: 'post',
    params,
    data,
  })
}

/**
 * 根据字典类型获取字典数据
 */
export function getDictDataByTypeApi(dictType: string): Promise<API.DictData[]> {
  return request({
    url: `/dict/data/${dictType}`,
    method: 'get',
  })
}

/**
 * 获取字典数据详情
 */
export function getDictDataApi(id: number): Promise<API.DictData> {
  return request({
    url: `/dict/data/detail/${id}`,
    method: 'get',
  })
}

/**
 * 新增字典数据
 */
export function createDictDataApi(data: API.DictData): Promise<string> {
  return request({
    url: '/dict/data',
    method: 'post',
    data,
  })
}

/**
 * 更新字典数据
 */
export function updateDictDataApi(data: API.DictData): Promise<string> {
  return request({
    url: '/dict/data',
    method: 'put',
    data,
  })
}

/**
 * 删除字典数据
 */
export function deleteDictDataApi(id: number): Promise<string> {
  return request({
    url: `/dict/data/${id}`,
    method: 'delete',
  })
}
