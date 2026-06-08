<script lang="ts" setup>
import { ref, watch } from 'vue'
import { DEFAULT_AVATAR } from '@/utils/constants'

definePage({
  name: 'me',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '我的',
    navigationStyle: 'custom',
  },
})

const user = useAuthStore()
const router = useRouter()
const toast = useToast()
const loading = ref(false)

/**
 * 获取当前登录用户信息。
 * 页面需要用到头像、昵称等展示信息。
 */
const { send: getUserInfo } = useRequest(() => Apis.universal.getUserInfo({}), {
  immediate: false,
})

/**
 * 调用后端退出登录接口。
 */
const { send: logout } = useRequest(() => Apis.universal.logout({}), {
  immediate: false,
})

/**
 * 顶部用户标题。
 */
const profileTitle = computed(() => {
  return user.isLoggedIn ? user.userName : '个人中心'
})

/**
 * 顶部用户说明。
 */
const profileDesc = computed(() => {
  return user.isLoggedIn
    ? '继续把你的习惯、记录和统计放在一个顺手的位置。'
    : '登录后查看你的习惯、记录和统计。'
})

/**
 * 执行退出登录。
 */
async function doLogout() {
  loading.value = true
  try {
    const res = await logout()
    if (res.code !== 200) {
      throw new Error(res.message || '退出登录失败')
    }
    toast.success('退出登录成功')
    user.logout()
  }
  catch (error: any) {
    console.error('退出登录失败:', error)
    toast.error(error.message || '退出登录失败')
  }
  finally {
    loading.value = false
  }
}

/**
 * 跳转到登录页。
 */
function goLogin() {
  if (!user.isLoggedIn) {
    router.push({ name: 'login' })
  }
}

/**
 * 打开我的习惯页。
 */
function goHabitList() {
  router.push({ name: 'habit-list' })
}

/**
 * 打开习惯创建页。
 */
function goCreateHabit() {
  router.push({ name: 'habit-edit' })
}

/**
 * 打开记录页。
 */
function goRecords() {
  router.pushTab({ name: 'records' })
}

/**
 * 打开统计页。
 */
function goStats() {
  router.pushTab({ name: 'stats' })
}

/**
 * 在登录状态变化时自动刷新用户信息。
 */
watch(
  () => user.isLoggedIn,
  async (newVal) => {
    if (!newVal) {
      return
    }

    try {
      const userRes = await getUserInfo()
      if (userRes.code === 200 && userRes.data) {
        user.updateUser(userRes.data)
      }
    }
    catch (error) {
      console.error('获取用户信息失败:', error)
    }
  },
  { immediate: true },
)
</script>

<template>
  <view class="habit-page">
    <view class="habit-shell">
      <habit-page-header
        :tag="user.isLoggedIn ? '个人中心' : '欢迎使用 HabitLink'"
        :title="profileTitle"
        :desc="profileDesc"
        :action-text="user.isLoggedIn ? '我的习惯' : '去登录'"
        :action-disabled="loading"
        @action="user.isLoggedIn ? goHabitList() : goLogin()"
      >
        <template #metrics>
          <view class="grid grid-cols-3 gap-3">
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                账号状态
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ user.isLoggedIn ? '已登录' : '未登录' }}
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                当前版本
              </view>
              <view class="mt-2 text-base font-semibold">
                基础版
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                常用入口
              </view>
              <view class="mt-2 text-2xl font-semibold">
                4
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view class="habit-panel mt-4 p-5">
        <view class="flex items-center gap-4">
          <image
            :src="user.isLoggedIn ? getImageUrl(user.userAvatar) : DEFAULT_AVATAR"
            mode="aspectFill"
            class="h-16 w-16 border-4 border-[#EAF8F6] rounded-full"
          />
          <view class="min-w-0 flex-1">
            <view class="text-base text-[#16332F] font-semibold">
              {{ user.isLoggedIn ? user.userName : '点击登录开始使用' }}
            </view>
            <view class="mt-1 text-sm text-[#54706B] leading-6">
              {{ user.isLoggedIn ? '你的习惯空间已经准备好。' : '登录后开启你的个人习惯空间。' }}
            </view>
          </view>
        </view>
      </view>

      <view class="mt-5">
        <view class="habit-group-header">
          <view class="habit-section-title">
            快捷入口
          </view>
          <view class="text-xs text-[#90A29F]">
            常用功能
          </view>
        </view>

        <view class="habit-grid">
          <view class="habit-feature-card" @tap="goHabitList">
            <view class="h-11 w-11 flex items-center justify-center rounded-[22rpx] bg-[#0891B2] text-white shadow-[0_10px_20px_rgba(8,145,178,0.22)]">
              <view class="i-solar-notebook-bold text-[24rpx]" />
            </view>
            <view class="habit-feature-card__title">
              我的习惯
            </view>
            <view class="habit-feature-card__desc">
              统一管理已创建的习惯
            </view>
          </view>

          <view class="habit-feature-card" @tap="goCreateHabit">
            <view class="h-11 w-11 flex items-center justify-center rounded-[22rpx] bg-[#0F766E] text-white shadow-[0_10px_20px_rgba(15,118,110,0.22)]">
              <view class="i-solar-add-circle-bold text-[24rpx]" />
            </view>
            <view class="habit-feature-card__title">
              新建习惯
            </view>
            <view class="habit-feature-card__desc">
              快速创建新的目标计划
            </view>
          </view>

          <view class="habit-feature-card" @tap="goRecords">
            <view class="h-11 w-11 flex items-center justify-center rounded-[22rpx] bg-[#16A34A] text-white shadow-[0_10px_20px_rgba(22,163,74,0.2)]">
              <view class="i-solar-checklist-bold text-[24rpx]" />
            </view>
            <view class="habit-feature-card__title">
              查看记录
            </view>
            <view class="habit-feature-card__desc">
              回看每一次完成和备注
            </view>
          </view>

          <view class="habit-feature-card" @tap="goStats">
            <view class="h-11 w-11 flex items-center justify-center rounded-[22rpx] bg-[#F59E0B] text-white shadow-[0_10px_20px_rgba(245,158,11,0.2)]">
              <view class="i-solar-widget-4-bold text-[24rpx]" />
            </view>
            <view class="habit-feature-card__title">
              统计分析
            </view>
            <view class="habit-feature-card__desc">
              看见坚持背后的真实变化
            </view>
          </view>
        </view>
      </view>

      <view class="habit-panel mt-5 p-5">
        <view class="flex items-start gap-3">
          <view class="h-11 w-11 flex shrink-0 items-center justify-center rounded-[22rpx] bg-[#FFF7EA] text-[#B7791F]">
            <view class="i-solar-info-circle-bold text-[24rpx]" />
          </view>
          <view class="min-w-0 flex-1">
            <view class="habit-section-title">
              当前版本说明
            </view>
            <view class="habit-section-desc mt-2">
              第一版已经接入了习惯创建、首页打卡、记录查看和统计查看的核心能力。后续可以继续补充提醒、补打卡、趋势图和更完整的个人设置。
            </view>
          </view>
        </view>
      </view>

      <view class="mt-5">
        <view
          v-if="!user.isLoggedIn"
          class="habit-primary-button py-4 text-center text-sm font-semibold"
          @tap="goLogin"
        >
          立即登录
        </view>

        <view
          v-else
          class="habit-dark-button py-4 text-center text-sm font-semibold"
          :class="{ 'opacity-70 pointer-events-none': loading }"
          @tap="doLogout"
        >
          {{ loading ? '正在退出登录...' : '退出当前账号' }}
        </view>
      </view>
    </view>
  </view>
</template>
