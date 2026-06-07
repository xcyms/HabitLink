<script setup lang="ts">
definePage({
  name: 'index',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '首页',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const user = useAuthStore()
const router = useRouter()

function goLogin() {
  router.push({ name: 'login' })
}
</script>

<template>
  <view class="w-full bg-gray-50">
    <!-- 顶部蓝色区域 -->
    <view class="relative overflow-hidden from-blue-500 to-blue-600 bg-gradient-to-b px-6 pb-12 pt-14">
      <!-- 装饰 -->
      <view class="absolute h-40 w-40 rounded-full bg-white/10 blur-2xl -right-10 -top-10" />
      <view class="absolute h-32 w-32 rounded-full bg-blue-400/20 blur-xl -bottom-8 -left-8" />

      <!-- 欢迎 -->
      <view class="relative z-10">
        <view class="text-sm text-white/80">
          早上好
        </view>
        <view class="mt-1 text-2xl text-white font-bold">
          {{ user.userName || '游客' }}
        </view>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="relative rounded-t-3xl bg-gray-50 px-6 pb-8 -mt-6">
      <!-- 主卡片 -->
      <view class="mb-6 overflow-hidden rounded-2xl bg-white p-5 shadow-sm">
        <view class="flex items-center gap-3">
          <view class="h-12 w-12 flex items-center justify-center rounded-xl bg-blue-500">
            <div class="i-solar:coffee-bold text-2xl text-white" />
          </view>
          <view class="flex-1">
            <view class="text-gray-900 font-semibold">
              Universal Starter
            </view>
            <view class="text-xs text-gray-500">
              开箱即用的项目模板
            </view>
          </view>
        </view>

        <view class="mt-4 border-t border-gray-100 pt-4">
          <view class="text-sm text-gray-600 leading-relaxed">
            这是一个开箱即用的通用项目模板，助您快速构建卓越应用体验。
          </view>
        </view>
      </view>

      <!-- 功能网格 -->
      <view class="mb-6">
        <view class="mb-3 text-sm text-gray-900 font-semibold">
          快捷功能
        </view>

        <view class="grid grid-cols-4 gap-3">
          <view class="flex flex-col items-center gap-2" @click="router.push({ name: 'index' })">
            <view hover-class="scale-95 bg-blue-100" :hover-stay-time="100" class="h-12 w-12 flex items-center justify-center rounded-xl bg-blue-50 transition-all">
              <div class="i-solar:home-2-linear text-lg text-blue-500" />
            </view>
            <view class="text-xs text-gray-600">
              首页
            </view>
          </view>

          <view class="flex flex-col items-center gap-2">
            <view hover-class="scale-95 bg-orange-100" :hover-stay-time="100" class="h-12 w-12 flex items-center justify-center rounded-xl bg-orange-50 transition-all">
              <div class="i-solar:user-linear text-lg text-orange-500" />
            </view>
            <view class="text-xs text-gray-600">
              我的
            </view>
          </view>

          <view class="flex flex-col items-center gap-2">
            <view hover-class="scale-95 bg-green-100" :hover-stay-time="100" class="h-12 w-12 flex items-center justify-center rounded-xl bg-green-50 transition-all">
              <div class="i-solar:settings-linear text-lg text-green-500" />
            </view>
            <view class="text-xs text-gray-600">
              设置
            </view>
          </view>

          <view class="flex flex-col items-center gap-2">
            <view hover-class="scale-95 bg-pink-100" :hover-stay-time="100" class="h-12 w-12 flex items-center justify-center rounded-xl bg-pink-50 transition-all">
              <div class="i-solar:bell-linear text-lg text-pink-500" />
            </view>
            <view class="text-xs text-gray-600">
              消息
            </view>
          </view>
        </view>
      </view>

      <!-- 登录按钮 -->
      <view v-if="!user.isLoggedIn" class="mt-6">
        <view
          class="h-11 flex cursor-pointer items-center justify-center rounded-lg bg-blue-500 text-white font-medium transition-all hover:bg-blue-600"
          @click="goLogin"
        >
          <text class="text-sm">
            立即登录
          </text>
        </view>
      </view>

      <!-- 已登录 -->
      <view v-else class="mt-6 rounded-xl bg-white p-4 shadow-sm">
        <view class="flex items-center gap-3">
          <view class="h-10 w-10 flex items-center justify-center rounded-full bg-blue-500">
            <div class="i-solar:user-rounded-bold text-lg text-white" />
          </view>
          <view class="flex-1">
            <view class="text-xs text-gray-500">
              已登录
            </view>
            <view class="text-gray-900 font-semibold">
              {{ user.userName }}
            </view>
          </view>
          <view class="i-solar:exit-right-linear text-lg text-gray-400" />
        </view>
      </view>
    </view>
  </view>
</template>
