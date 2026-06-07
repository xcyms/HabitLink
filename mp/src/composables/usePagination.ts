// src/composables/usePagination.ts
import type { PaginationResponse } from '@/types/common'
import { ref } from 'vue'

interface PaginationOptions<T> {
  fetchFn: (page: number, ...args: any[]) => Promise<PaginationResponse<T>>
  pageSize?: number
  immediate?: boolean
}

interface PaginationResult<T> {
  list: Ref<T[]>
  loading: Ref<boolean>
  hasMore: Ref<boolean>
  page: Ref<number>
  error: Ref<Error | null>
  loadMore: () => Promise<void>
  refresh: () => Promise<void>
  reset: () => void
}

/**
 * 分页逻辑 composable
 * 统一管理列表的分页加载逻辑
 */
export function useListPagination<T>(
  options: PaginationOptions<T>,
): PaginationResult<T> {
  const { fetchFn, immediate = false } = options

  const list = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const hasMore = ref(true)
  const page = ref(1)
  const error = ref<Error | null>(null)
  const isLoadingMore = ref(false)

  /**
   * 加载数据
   */
  const load = async (isLoadMore = false) => {
    // 防止重复请求
    if (loading.value)
      return

    loading.value = true
    error.value = null

    try {
      const response = await fetchFn(page.value)

      if (response) {
        const newData = response.data || []

        if (isLoadMore) {
          list.value.push(...newData)
        }
        else {
          list.value = newData
        }
        // 判断是否还有更多数据
        hasMore.value = Number(response.current_page) < Number(response.last_page)
      }
      else {
        hasMore.value = false
      }
    }
    catch (err) {
      error.value = err instanceof Error ? err : new Error(String(err))
      console.error('Pagination load error:', err)
    }
    finally {
      loading.value = false
      isLoadingMore.value = false
    }
  }

  /**
   * 加载更多
   */
  const loadMore = async () => {
    if (!hasMore.value || loading.value || isLoadingMore.value) {
      return
    }

    isLoadingMore.value = true
    page.value++
    await load(true)
  }

  /**
   * 刷新数据
   */
  const refresh = async () => {
    page.value = 1
    hasMore.value = true
    list.value = []
    await load(false)
  }

  /**
   * 重置状态
   */
  const reset = () => {
    list.value = []
    page.value = 1
    hasMore.value = true
    loading.value = false
    error.value = null
    isLoadingMore.value = false
  }

  // 立即加载
  if (immediate) {
    load(false)
  }

  return {
    list,
    loading,
    hasMore,
    page,
    error,
    loadMore,
    refresh,
    reset,
  }
}
