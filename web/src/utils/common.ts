export function getImageUrl(url: string | undefined) {
  if (!url)
    return ''

  // 完整的远程地址直接返回。
  if (url.startsWith('http'))
    return url

  // 否则拼接图片访问前缀。
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${url}`
}

export function formatSize(bytes: number) {
  if (bytes === 0)
    return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${Number.parseFloat((bytes / k ** i).toFixed(2))} ${sizes[i]}`
}

export function validatePassword(password: string) {
  if (password.length < 8)
    return '密码长度至少为 8 位'
  if (!/[a-z]/.test(password))
    return '需包含至少一个小写字母'
  if (!/[A-Z]/.test(password))
    return '需包含至少一个大写字母'
  if (!/\d/.test(password))
    return '需包含至少一个数字'
  if (!/[@$!%*?&._-]/.test(password))
    return '需包含至少一个特殊字符（@$!%*?&._-）'
  return null
}
