// src/composables/usePageCache.ts
import { onMounted, ref } from 'vue'

interface PageCacheOptions<T> {
  key: string
  fetchFn: () => Promise<T>
  ttl?: number
  immediate?: boolean
}

interface PageCacheResult<T> {
  data: Ref<T | null>
  loading: Ref<boolean>
  error: Ref<Error | null>
  refresh: (force?: boolean) => Promise<void>
  clear: () => void
}

interface CacheData<T> {
  data: T
  timestamp: number
}

/**
 * 页面缓存 composable
 * 支持内存缓存和本地存储，自动处理缓存过期
 */
export function usePageCache<T>(
  options: PageCacheOptions<T>,
): PageCacheResult<T> {
  const { key, fetchFn, ttl = 5 * 60 * 1000, immediate = true } = options

  const data = ref<T | null>(null) as Ref<T | null>
  const loading = ref(false)
  const error = ref<Error | null>(null)

  /**
   * 检查缓存是否有效
   */
  const isCacheValid = (cacheData: CacheData<T>): boolean => {
    if (!cacheData || !cacheData.timestamp)
      return false
    const now = Date.now()
    return now - cacheData.timestamp < ttl
  }

  /**
   * 从本地存储读取缓存
   */
  const loadFromCache = (): T | null => {
    try {
      const cached = uni.getStorageSync(key)
      if (cached) {
        const cacheData = JSON.parse(cached) as CacheData<T>
        if (isCacheValid(cacheData)) {
          return cacheData.data
        }
      }
    }
    catch (err) {
      console.error('Load cache error:', err)
    }
    return null
  }

  /**
   * 保存到本地存储
   */
  const saveToCache = (value: T) => {
    try {
      const cacheData: CacheData<T> = {
        data: value,
        timestamp: Date.now(),
      }
      uni.setStorageSync(key, JSON.stringify(cacheData))
    }
    catch (err) {
      console.error('Save cache error:', err)
    }
  }

  /**
   * 获取数据
   */
  const fetchData = async (silent = false) => {
    if (!silent) {
      loading.value = true
    }
    error.value = null

    try {
      const result = await fetchFn()
      data.value = result
      saveToCache(result)
    }
    catch (err) {
      error.value = err instanceof Error ? err : new Error(String(err))
      console.error('Fetch data error:', err)
    }
    finally {
      if (!silent) {
        loading.value = false
      }
    }
  }

  /**
   * 刷新数据
   */
  const refresh = async (force = false) => {
    // 如果不是强制刷新，先尝试从缓存加载
    if (!force) {
      const cached = loadFromCache()
      if (cached) {
        data.value = cached
        // 后台静默更新
        fetchData(true)
        return
      }
    }

    await fetchData(false)
  }

  /**
   * 清除缓存
   */
  const clear = () => {
    try {
      uni.removeStorageSync(key)
      data.value = null
    }
    catch (err) {
      console.error('Clear cache error:', err)
    }
  }

  // 组件挂载时加载数据
  if (immediate) {
    onMounted(() => {
      refresh()
    })
  }

  return {
    data,
    loading,
    error,
    refresh,
    clear,
  }
}
