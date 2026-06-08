/// <reference types="./globals.d.ts" />

/**
 * 小程序端接口定义表。
 * 这里维护前端直连后端所需的接口路径和请求方法。
 */
export default {
  // 用户相关接口
  'universal.login': ['POST', '/user/login'],
  'universal.register': ['POST', '/user/register'],
  'universal.getUserInfo': ['GET', '/user/info'],
  'universal.logout': ['GET', '/user/logout'],

  // 习惯相关接口
  'habit.create': ['POST', '/habit/create'],
  'habit.update': ['POST', '/habit/update'],
  'habit.delete': ['POST', '/habit/delete'],
  'habit.pause': ['POST', '/habit/pause'],
  'habit.resume': ['POST', '/habit/resume'],
  'habit.detail': ['GET', '/habit/detail'],
  'habit.list': ['GET', '/habit/list'],

  // 打卡相关接口
  'checkIn.submit': ['POST', '/check-in/submit'],
  'checkIn.makeup': ['POST', '/check-in/makeup'],
  'checkIn.list': ['GET', '/check-in/list'],

  // 统计相关接口
  'habitStats.todayOverview': ['GET', '/habit-stats/today-overview'],
  'habitStats.detail': ['GET', '/habit-stats/detail'],
}
