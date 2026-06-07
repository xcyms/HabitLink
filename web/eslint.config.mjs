// eslint.config.js
import antfu from '@antfu/eslint-config'

export default antfu({
  // 启用 Vue 支持
  vue: true,

  // 启用 TypeScript 支持
  typescript: true,

  // 设置解析器配置（用于 import/no-unresolved）
  settings: {
    'import/resolver': {
      node: {
        extensions: ['.js', '.jsx', '.ts', '.tsx', '.vue', '.json'],
        paths: ['./src'],
      },
      // 如果使用路径别名
      alias: {
        map: [
          ['@', './src'],
          ['~', './src'],
        ],
        extensions: ['.js', '.jsx', '.ts', '.tsx', '.vue', '.json'],
      },
    },
  },

  // 规则配置
  rules: {
    'import/no-unresolved': ['error', {
      ignore: ['^virtual:', '\\.vue$'], // 忽略虚拟模块和 .vue 文件
      commonjs: true,
      amd: true,
    }],
  },
})
