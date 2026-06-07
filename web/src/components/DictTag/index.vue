<script setup lang="ts">
import { useDictStore } from '../../store/dict.ts'

interface Props {
  dictType: string
  value?: string | number
}

const props = defineProps<Props>()

const dictStore = useDictStore()

// 根据 value 获取字典项
const dictItem = computed(() => {
  if (props.value === undefined || props.value === null)
    return undefined
  const dictData = dictStore.dictCache.get(props.dictType)
  if (!dictData)
    return undefined
  return dictData.find(item => item.dictValue === String(props.value))
})

// 获取标签颜色
const tagColor = computed(() => {
  const value = String(props.value)
  // 根据常见状态值设置颜色
  if (value === '0' || value === 'true' || value === 'success' || value === 'normal') {
    return 'success'
  }
  if (value === '1' || value === 'false' || value === 'error' || value === 'disabled') {
    return 'error'
  }
  if (value === 'warning' || value === 'pending') {
    return 'warning'
  }
  if (value === 'processing') {
    return 'processing'
  }
  return 'default'
})

// 获取显示文本
const displayText = computed(() => {
  return dictItem.value?.dictLabel || String(props.value)
})

// 组件挂载时预加载字典数据
onMounted(() => {
  if (props.dictType) {
    dictStore.getDictData(props.dictType)
  }
})
</script>

<template>
  <a-tag :color="tagColor">
    {{ displayText }}
  </a-tag>
</template>
