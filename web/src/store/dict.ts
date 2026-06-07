import { defineStore } from 'pinia'
import { getDictDataByTypeApi } from '../api/dict.ts'
import type { DictData } from '../types'

export const useDictStore = defineStore('dict', () => {
  // 字典缓存 Map<dictType, DictData[]>
  const dictCache = ref<Map<string, DictData[]>>(new Map())

  // 正在加载中的请求 Map<dictType, Promise>
  const loadingMap = ref<Map<string, Promise<DictData[]>>>(new Map())

  /**
   * 获取字典数据（带缓存）
   */
  async function getDictData(dictType: string): Promise<DictData[]> {
    if (!dictType)
      return []

    // 先检查缓存
    const cached = dictCache.value.get(dictType)
    if (cached) {
      return cached
    }

    // 检查是否正在加载中
    const loading = loadingMap.value.get(dictType)
    if (loading) {
      return loading
    }

    // 发起新请求
    const promise = getDictDataByTypeApi(dictType)
      .then((res) => {
        dictCache.value.set(dictType, res)
        loadingMap.value.delete(dictType)
        return res
      })
      .catch((error) => {
        loadingMap.value.delete(dictType)
        throw error
      })

    loadingMap.value.set(dictType, promise)
    return promise
  }

  /**
   * 根据字典类型和键值获取标签
   */
  function getDictLabel(dictType: string, value: string | number | undefined): string {
    if (value === undefined || value === null)
      return ''
    const dictData = dictCache.value.get(dictType)
    if (!dictData)
      return String(value)
    const item = dictData.find(d => d.dictValue === String(value))
    return item?.dictLabel || String(value)
  }

  /**
   * 清除指定字典类型的缓存
   */
  function clearCache(dictType?: string) {
    if (dictType) {
      dictCache.value.delete(dictType)
    }
    else {
      dictCache.value.clear()
    }
  }

  /**
   * 刷新字典数据
   */
  async function refreshDict(dictType: string): Promise<DictData[]> {
    dictCache.value.delete(dictType)
    return getDictData(dictType)
  }

  return {
    dictCache,
    getDictData,
    getDictLabel,
    clearCache,
    refreshDict,
  }
})
