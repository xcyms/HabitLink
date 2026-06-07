// src/types/common.d.ts

/**
 * 排序选项
 */
export interface SortOption {
  name: string
  value: string
  subname: string
  icon?: string
}

/**
 * 分页参数
 */
export interface PaginationParams {
  page: number
  pageSize: number
  order?: string
}

/**
 * 分页响应
 */
export interface PaginationResponse<T> {
  data: T[]
  current_page: number
  last_page: number
  total: number
  per_page: number
}

/**
 * API 响应基础结构
 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 加载状态
 */
export type LoadingState = 'idle' | 'loading' | 'success' | 'error'

/**
 * 缓存配置
 */
export interface CacheConfig {
  key: string
  ttl: number
  storage: 'memory' | 'local' | 'session'
}
