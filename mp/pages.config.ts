import { defineUniPages } from '@uni-helper/vite-plugin-uni-pages'

/**
 * uni-app 页面配置。
 * 页面本身通过 definePage 自动注册，这里主要维护全局样式和底部导航。
 */
export default defineUniPages({
  pages: [],
  globalStyle: {
    navigationBarBackgroundColor: '@navBgColor',
    navigationBarTextStyle: '@navTxtStyle',
    navigationBarTitleText: 'HabitLink',
    backgroundColor: '@bgColor',
    backgroundTextStyle: '@bgTxtStyle',
    backgroundColorTop: '@bgColorTop',
    backgroundColorBottom: '@bgColorBottom',
    enablePullDownRefresh: false,
    onReachBottomDistance: 50,
    animationType: 'pop-in',
    animationDuration: 300,
  },
  tabBar: {
    custom: true,
    // #ifdef MP-ALIPAY
    customize: true,
    overlay: true,
    // #endif
    height: '0',
    color: '@tabColor',
    selectedColor: '@tabSelectedColor',
    backgroundColor: '@tabBgColor',
    borderStyle: '@tabBorderStyle',
    list: [
      {
        pagePath: 'pages/index/index',
      },
      {
        pagePath: 'pages/records/index',
      },
      {
        pagePath: 'pages/stats/index',
      },
      {
        pagePath: 'pages/me/me',
      },
    ],
  },
})
