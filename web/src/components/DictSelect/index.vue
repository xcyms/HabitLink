<script setup lang="ts">
import { useDictStore } from '../../store/dict.ts'
import type { DictData } from '../../types'

interface Props {
  dictType: string
  value?: string | number
  placeholder?: string
  allowClear?: boolean
  disabled?: boolean
  mode?: 'default' | 'multiple' | 'tags'
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择',
  allowClear: true,
  disabled: false,
  mode: undefined,
})

const emit = defineEmits<{
  (e: 'update:value', value: string | number | undefined): void
  (e: 'change', value: string | number | undefined, option: DictData | undefined): void
}>()

const dictStore = useDictStore()
const loading = ref(false)

// 字典数据列表
const dictDataList = computed(() => {
  const data = dictStore.dictCache.get(props.dictType)
  return data ? data.filter(item => item.status === 0) : []
})

// 加载字典数据
async function loadDictData() {
  if (!props.dictType)
    return
  loading.value = true
  try {
    await dictStore.getDictData(props.dictType)
  }
  catch (error) {
    console.error('加载字典数据失败:', error)
  }
  finally {
    loading.value = false
  }
}

// 选择改变
function handleChange(value: unknown) {
  const strValue = value === undefined || value === null ? undefined : String(value)
  const option = dictDataList.value.find(item => item.dictValue === strValue)
  // 如果原值是数字类型，转换回数字
  const emitValue = typeof props.value === 'number' && strValue !== undefined ? Number(strValue) : strValue
  emit('update:value', emitValue)
  emit('change', emitValue, option)
}

// 监听 dictType 变化
watch(() => props.dictType, () => {
  loadDictData()
}, { immediate: true })

// 根据 value 获取 label
function getLabelByValue(value: string | number | undefined): string {
  if (value === undefined || value === null)
    return ''
  const item = dictDataList.value.find(d => d.dictValue === String(value))
  return item?.dictLabel || String(value)
}

defineExpose({
  getLabelByValue,
  refresh: loadDictData,
})
</script>

<template>
  <a-select
    :value="String(value ?? '')"
    :placeholder="placeholder"
    :allow-clear="allowClear"
    :disabled="disabled"
    :loading="loading"
    :mode="mode as any"
    style="width: 100%"
    @change="handleChange as any"
  >
    <a-select-option
      v-for="item in dictDataList"
      :key="item.dictValue"
      :value="item.dictValue"
    >
      {{ item.dictLabel }}
    </a-select-option>
  </a-select>
</template>
