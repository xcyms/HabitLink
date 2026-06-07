import { useDark, useToggle } from '@vueuse/core'
import { theme } from 'ant-design-vue'
import { computed } from 'vue'

// 使用 VueUse 的 useDark 管理 html 标签上的 dark 类
// 默认存储在 localStorage 中的 'vueuse-color-scheme' key
export const isDark = useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: '',
})

// 切换函数
export const toggleDark = useToggle(isDark)

// Ant Design Vue 的主题算法配置
export const themeConfig = computed(() => {
  return {
    algorithm: isDark.value ? theme.darkAlgorithm : theme.defaultAlgorithm,
    token: {
      // 可以在这里自定义主题色，例如
      // colorPrimary: '#1677ff',
    },
  }
})
