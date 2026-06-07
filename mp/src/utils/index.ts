/**
 * 获取当前页面路径
 * @returns 当前页面路径
 */
export function getCurrentPath() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  return currentPage.route || ''
}

/**
 * 生成随机 UUID
 * @returns UUID 字符串
 */
export function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

export function getImageUrl(url: string | undefined) {
  if (!url)
    return ''
  // 如果是完整的 http 地址则直接返回
  if (url.startsWith('http'))
    return url
  // 否则拼接公共图片访问前缀
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${url}`
}

/**
 * 格式化视频时长
 * @param seconds 秒数
 * @returns 格式化后的时长字符串 (HH:mm:ss 或 mm:ss)
 */
export function formatDuration(seconds: number | undefined): string {
  if (seconds === undefined || seconds === null || Number.isNaN(seconds))
    return ''
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)

  const parts = []
  if (h > 0) {
    parts.push(h.toString().padStart(2, '0'))
  }
  parts.push(m.toString().padStart(2, '0'))
  parts.push(s.toString().padStart(2, '0'))

  return parts.join(':')
}

export function formatSize(bytes: number) {
  if (bytes === 0)
    return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${Number.parseFloat((bytes / k ** i).toFixed(2))} ${sizes[i]}`
}
