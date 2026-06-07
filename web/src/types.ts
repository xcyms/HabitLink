export interface Response<T> {
  code: number
  message: string
  data: T
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
}

export interface SysConfig {
  userId: number | undefined
  id?: number
  configKey: string
  configValue: string
  configName: string
  remark?: string
}

export interface UploadFile {
  id: string
  file: File
  name: string
  size: number
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
  url?: string
  preview?: string
}

export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  roles: string[]
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface Role {
  id?: number
  roleName: string
  roleKey: string
  createTime?: string
  updateTime?: string
}

export interface File {
  id?: number
  userId?: number
  businessId?: number
  businessType?: string
  url?: string
  name?: string
  size?: number
  type?: string
  sorted?: number
  createTime?: string
  updateTime?: string
}

export interface ConfigOption {
  label: string
  value: string
}

export interface Dict {
  id?: number
  dictName: string
  dictType: string
  status?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface DictData {
  id?: number
  dictId?: number
  dictLabel?: string
  dictValue?: string
  dictType?: string
  dictSort?: number
  status?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface Notice {
  id?: number
  title: string
  content?: string
  type?: string
  isRead?: boolean
  targetType?: string
  targetUserId?: number
  createTime?: string
  updateTime?: string
}
