// src/types/page.d.ts

/**
 * 图片数据
 */
export interface ImageItem {
  userName: boolean
  id: number | string
  userId: number | string
  albumId: number | string
  url: string
  thumbnailUrl?: string
  name: string
  size: number
  type: string
  status: {
    code: number
    desc: string
  }
  createTime: string
  make?: string
  model?: string
  exposureTime?: string
  fNumber?: string
  iso?: string
  focalLength?: string
  lensModel?: string
  lat?: string
  lng?: string
  takeTime?: string
}

/**
 * 相册数据
 */
export interface AlbumItem {
  id: string | number
  userId: string | number
  name: string
  description: string
  cover: string
  imageCount: number
  videoCount: number
  type: number
  createTime: string
  updateTime: string
  _error?: boolean
}

/**
 * 消息数据
 */
export interface MessageItem {
  id: string | number
  type: 'info' | 'success' | 'warning' | 'album_share' | 'error'
  title: string
  content: string
  createTime: string
  readFlag: {
    code: number
    desc: string
  }
  userId: string | number
  icon: string
  iconBg: string
  iconColor: string
}

export interface VideoItem {
  id: number
  userId: number
  albumId: number
  url: string
  coverUrl?: string
  name: string
  size: number
  type: string
  duration?: number
  status: {
    code: number
    desc: string
  }
  createTime: string
}
