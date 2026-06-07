import type { AxiosResponse } from 'axios'
import request from '../utils/request.ts'
import type * as API from '../types'

/**
 * 单文件上传
 */
export function uploadFileApi(file: File, businessId?: number, businessType?: string): Promise<API.File> {
  const formData = new FormData()
  formData.append('file', file)
  if (businessId)
    formData.append('businessId', String(businessId))
  if (businessType)
    formData.append('businessType', businessType)

  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/**
 * 获取文件列表
 */
export function getFileListApi(params: { current: number, size: number }, data?: API.File): Promise<API.PageResult<API.File>> {
  return request({
    url: '/file/page',
    method: 'post',
    params,
    data,
  })
}

/**
 * 删除文件
 */
export function deleteFileApi(id: number): Promise<string> {
  return request({
    url: `/file/${id}`,
    method: 'delete',
  })
}

/**
 * 批量删除文件
 */
export function deleteBatchFilesApi(ids: number[]): Promise<string> {
  return request({
    url: '/file/batch',
    method: 'delete',
    data: ids,
  })
}

/**
 * 下载文件
 */
export function downloadFileApi(id: number): Promise<AxiosResponse<Blob>> {
  return request({
    url: `/file/download/${id}`,
    method: 'get',
    responseType: 'blob',
  })
}
