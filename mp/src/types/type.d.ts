// src/types/index.d.ts

export interface UserDTO {
  id?: number
  username?: string
  password?: string
  nickname?: string
  avatar?: string
  email?: string
  phone?: string
  status?: {
    code: number
    desc: string
  }
  createTime?: string
  updateTime?: string
  roles?: string[]
}

export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
