// src/utils/constants.ts

/**
 * 动画时长常量
 */
export const ANIMATION_DURATION = {
  fast: 150, // 快速动画（按钮点击等）
  normal: 300, // 正常动画（页面切换等）
  slow: 500, // 慢速动画（复杂过渡等）
} as const

/**
 * 缓动函数
 */
export const EASING = {
  ease: 'cubic-bezier(0.25, 0.1, 0.25, 1)',
  easeIn: 'cubic-bezier(0.42, 0, 1, 1)',
  easeOut: 'cubic-bezier(0, 0, 0.58, 1)',
  easeInOut: 'cubic-bezier(0.42, 0, 0.58, 1)',
} as const

/**
 * 缓存键名
 */
export const CACHE_KEYS = {
  SEARCH_ALBUMS: 'cache_search_albums',
  HOME_IMAGES: 'cache_home_images',
  USER_INFO: 'cache_user_info',
  MESSAGES: 'cache_messages',
} as const

/**
 * 缓存有效期（毫秒）
 */
export const CACHE_TTL = {
  SHORT: 1 * 60 * 1000, // 1分钟
  MEDIUM: 5 * 60 * 1000, // 5分钟
  LONG: 30 * 60 * 1000, // 30分钟
  DAY: 24 * 60 * 60 * 1000, // 1天
} as const

/**
 * 分页配置
 */
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 50,
} as const

/**
 * 图片占位符
 */
export const PLACEHOLDER_IMAGE = '/static/image-error.png'

/**
 * 默认头像
 */
export const DEFAULT_AVATAR = '/static/default-avatar.png'
