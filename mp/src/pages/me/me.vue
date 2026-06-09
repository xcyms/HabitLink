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

const careCards = [
  {
    eyebrow: '今天适合做什么',
    title: '把目标拆到足够轻',
    desc: '比起一次做很多，先把今天最容易开始的一步做完，更容易守住节奏。',
  },
  {
    eyebrow: '提醒自己',
    title: '允许偶尔波动',
    desc: '没有哪条习惯会永远完美连续，允许自己起伏，才更容易长期坚持。',
  },
]

const companionRules = [
  '先做可完成的小动作，再逐步加量。',
  '把提醒放在固定时刻，让行动更容易开始。',
  '每周只关注一点点进步，不急着立刻变得完美。',
]

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
        :action-text="user.isLoggedIn ? '管理习惯' : '去登录'"
        :action-disabled="loading"
        @action="user.isLoggedIn ? goHabitList() : goLogin()"
      >
        <template #metrics>
          <view class="habit-metrics-grid">
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                账号状态
              </view>
              <view class="habit-metric-card__value">
                {{ user.isLoggedIn ? '已登录' : '未登录' }}
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                陪伴模式
              </view>
              <view class="habit-metric-card__value">
                轻陪伴
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                当前版本
              </view>
              <view class="habit-metric-card__value">
                v1.0
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view class="habit-profile-card mt-4">
        <view class="relative z-10 flex items-center gap-4">
          <image
            :src="user.isLoggedIn ? getImageUrl(user.userAvatar) : DEFAULT_AVATAR"
            mode="aspectFill"
            class="h-16 w-16 border-4 border-white rounded-full"
          />
          <view class="min-w-0 flex-1">
            <view class="text-[30rpx] text-[#324340] font-semibold">
              {{ user.isLoggedIn ? user.userName : '点击登录开始使用' }}
            </view>
            <view class="mt-2 text-[24rpx] text-[#6F807B] leading-7">
              {{ user.isLoggedIn ? '你的个人习惯空间已经准备好，接下来只需要继续把日常的小动作做下去。' : '登录后记录你的习惯、提醒与变化，让每天都更有节奏感。' }}
            </view>
          </view>
        </view>
        <view class="relative z-10 mt-5 flex flex-wrap gap-2">
          <view class="habit-pill habit-pill--primary">
            {{ user.isLoggedIn ? '已连接账号' : '游客状态' }}
          </view>
          <view class="habit-pill habit-pill--secondary">
            适合长期养成
          </view>
        </view>
      </view>

      <view class="mt-5">
        <view class="habit-group-header">
          <view class="habit-section-title">
            本周陪伴建议
          </view>
        </view>

        <view class="habit-grid">
          <view
            v-for="item in careCards"
            :key="item.title"
            class="habit-value-card"
          >
            <view class="habit-value-card__eyebrow">
              {{ item.eyebrow }}
            </view>
            <view class="habit-value-card__title">
              {{ item.title }}
            </view>
            <view class="habit-value-card__desc">
              {{ item.desc }}
            </view>
          </view>
        </view>
      </view>

      <view class="habit-panel mt-5 p-5">
        <view class="flex items-start gap-3">
          <view class="h-11 w-11 flex shrink-0 items-center justify-center rounded-[22rpx] bg-[#EEF1ED] text-[#728D87]">
            <view class="i-solar-heart-angle-bold text-[24rpx]" />
          </view>
          <view class="min-w-0 flex-1">
            <view class="habit-section-title">
              HabitLink 的陪伴方式
            </view>
            <view class="habit-section-desc mt-2">
              我们不强调一次做很多，而是帮助你把行动拆小、把提醒固定、把变化慢慢看清楚。
            </view>
          </view>
        </view>
        <view class="mt-5 flex flex-col gap-3">
          <view
            v-for="item in companionRules"
            :key="item"
            class="flex items-start gap-3 rounded-[24rpx] bg-[#F3F5F2] px-4 py-4"
          >
            <view class="mt-[6rpx] h-2.5 w-2.5 shrink-0 rounded-full bg-[#728D87]" />
            <view class="text-[24rpx] text-[#6F807B] leading-7">
              {{ item }}
            </view>
          </view>
        </view>
      </view>

      <view class="habit-panel mt-5 p-5">
        <view class="flex items-start gap-3">
          <view class="h-11 w-11 flex shrink-0 items-center justify-center rounded-[22rpx] bg-[#EEF8F5] text-[#19A38C]">
            <view class="i-solar-shield-check-bold text-[24rpx]" />
          </view>
          <view class="min-w-0 flex-1">
            <view class="habit-section-title">
              账号与使用说明
            </view>
            <view class="habit-section-desc mt-2">
              这里会逐步补充更完整的资料设置、提醒偏好和隐私说明。当前版本先聚焦习惯创建、打卡、记录与统计。
            </view>
          </view>
        </view>
        <view class="mt-5 flex flex-col gap-3">
          <view class="habit-value-card !p-4">
            <view class="habit-value-card__eyebrow">
              当前身份
            </view>
            <view class="habit-value-card__desc !mt-2">
              {{ user.isLoggedIn ? `你正在以 ${user.userName} 的身份使用 HabitLink。` : '你还没有登录，登录后可以保存自己的习惯与记录。' }}
            </view>
          </view>
          <view class="habit-value-card !p-4">
            <view class="habit-value-card__eyebrow">
              数据同步
            </view>
            <view class="habit-value-card__desc !mt-2">
              习惯、打卡记录和统计数据会围绕你的账号持续保存，方便你长期回看自己的变化。
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
        <view
          v-if="user.isLoggedIn"
          class="habit-light-button mt-3 py-4 text-center text-sm font-semibold"
          @tap="goCreateHabit"
        >
          开始新的习惯
        </view>
      </view>
    </view>
  </view>
</template>
