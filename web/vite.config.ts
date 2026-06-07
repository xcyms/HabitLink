import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'
import { defineConfig } from 'vite'
import Pages from 'vite-plugin-pages'
import Layouts from 'vite-plugin-vue-layouts-next'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    Pages(),
    Layouts(),
    UnoCSS(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia', '@vueuse/core'], // 自动导入 vue, vue-router 等 API
      dts: 'src/auto-import.d.ts', // 生成类型声明文件
    }),
    Components({
      resolvers: [AntDesignVueResolver({ importStyle: false })], // 自动按需引入 Ant Design Vue 组件
      dts: 'src/components.d.ts', // 生成类型声明文件
    }),
  ],
})
