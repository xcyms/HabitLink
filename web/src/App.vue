<script setup lang="ts">
import { ConfigProvider } from 'ant-design-vue'
import { themeConfig } from './composables/useTheme.ts'

const env = import.meta.env.MODE
const isDev = env === 'development'

// eslint-disable-next-line no-console
console.log(
  `%c ${import.meta.env.VITE_APP_TITLE} %c Running in ${env} mode %c`,
  `background: ${isDev ? '#42b883' : '#35495e'}; padding: 1px; border-radius: 3px 0 0 3px; color: #fff`,
  `background: ${isDev ? '#35495e' : '#42b883'}; padding: 1px; border-radius: 0 3px 3px 0; color: #fff`,
  'background: transparent',
)
// eslint-disable-next-line no-console
console.log(`%cAPI URL:%c ${import.meta.env.VITE_API_BASE_URL}`, 'color: #999', 'color: #42b883; font-weight: bold')
</script>

<template>
  <ConfigProvider :theme="themeConfig">
    <!-- 路由出口 -->
    <router-view />
  </ConfigProvider>
</template>

<style>
/* 全局样式 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

/* 简单的暗黑模式背景适配，具体由 Layout 和 ConfigProvider 接管 */
html.dark {
  color-scheme: dark;
  background: #141414; /* Ant Design Vue Dark 背景色 */
  color: rgba(255, 255, 255, 0.85);
}
</style>
