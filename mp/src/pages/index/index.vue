<script setup lang="ts">
import type { TodayHabitDTO, TodayOverviewDTO } from '@/types/type'

definePage({
  name: 'index',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '首页',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const authStore = useAuthStore()
const router = useRouter()
const toast = useToast()

/**
 * 首页总览数据。
 * 先给出稳定的默认结构，避免模板层出现过多空值判断。
 */
const overview = ref<TodayOverviewDTO>({
  date: '',
  plannedCount: 0,
  completedCount: 0,
  completionRate: 0,
  habits: [],
})

/**
 * 页面加载状态。
 */
const loading = ref(false)

/**
 * 是否已经创建习惯。
 */
const hasHabits = computed(() => overview.value.habits.length > 0)

/**
 * 今日待完成习惯。
 */
const pendingHabits = computed(() => overview.value.habits.filter(item => item.todayChecked !== 1))

/**
 * 今日已完成习惯。
 */
const completedHabits = computed(() => overview.value.habits.filter(item => item.todayChecked === 1))

/**
 * 今日完成率。
 * 统一在前端限制到 0 到 100 之间。
 */
const completionRate = computed(() => {
  const value = Number(overview.value.completionRate || 0)
  return Math.max(0, Math.min(value, 100))
})

/**
 * 顶部主标题。
 */
const heroTitle = computed(() => {
  if (!authStore.isLoggedIn) {
    return '把习惯慢慢养成'
  }
  if (overview.value.plannedCount === 0) {
    return '今天先安排一个小开始'
  }
  if (pendingHabits.value.length === 0) {
    return '今天的节律已经收住了'
  }
  return '把今天过得稳一点'
})

/**
 * 顶部副标题。
 */
const heroSubtitle = computed(() => {
  if (!authStore.isLoggedIn) {
    return '从一个很小的动作开始，节奏会一点点回到你手里。'
  }
  return completionText.value
})

/**
 * 首页完成率说明。
 */
const completionText = computed(() => {
  const rate = completionRate.value
  if (rate >= 100) {
    return '今天已经全部完成，适合轻松收尾，继续保持。'
  }
  if (rate >= 70) {
    return '整体状态很稳，再推进一两项，今天会更完整。'
  }
  if (rate > 0) {
    return '已经有了不错的起势，继续把剩下的事情做完。'
  }
  return '先完成第一项，今天的节奏就会被带起来。'
})

/**
 * 空状态说明文案。
 */
const emptyHint = computed(() => {
  if (loading.value) {
    return '正在同步今天的习惯安排...'
  }
  return '从创建第一条习惯开始，先让每天有一个清晰的起点。'
})

/**
 * 加载首页总览。
 */
async function loadOverview() {
  if (!authStore.isLoggedIn) {
    overview.value = {
      date: '',
      plannedCount: 0,
      completedCount: 0,
      completionRate: 0,
      habits: [],
    }
    uni.stopPullDownRefresh()
    return
  }

  loading.value = true
  try {
    const res = await Apis.habitStats.todayOverview({})
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '加载首页数据失败')
    }
    overview.value = res.data
  }
  catch (error: any) {
    console.error('加载首页数据失败:', error)
    toast.error(error.message || '加载首页数据失败')
  }
  finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

/**
 * 执行今日打卡。
 */
async function handleCheckIn(habit: TodayHabitDTO) {
  try {
    const res = await Apis.checkIn.submit({
      data: {
        habitId: habit.habitId,
        recordDate: overview.value.date,
        note: '',
        isMakeup: 0,
      },
    })
    if (res.code !== 200) {
      throw new Error(res.message || '打卡失败')
    }
    toast.success('打卡成功')
    await loadOverview()
  }
  catch (error: any) {
    console.error('提交打卡失败:', error)
    toast.error(error.message || '打卡失败')
  }
}

/**
 * 进入登录页。
 */
function goLogin() {
  router.push({ name: 'login' })
}

/**
 * 进入创建习惯页。
 */
function goCreateHabit() {
  router.push({ name: 'habit-edit' })
}

/**
 * 查看某条习惯的记录。
 */
function goHabitRecords(habit: TodayHabitDTO) {
  router.push({
    name: 'records',
    query: {
      habitId: String(habit.habitId),
      habitName: habit.habitName,
    },
  })
}

/**
 * 读取习惯颜色。
 * 如果后端没有返回颜色，则回退到统一主色。
 */
function getHabitColor(habit: TodayHabitDTO) {
  return habit.color || '#0F766E'
}

onShow(() => {
  loadOverview()
})

onPullDownRefresh(() => {
  loadOverview()
})
</script>

<template>
  <view class="habit-page">
    <view class="habit-shell">
      <habit-page-header
        :tag="authStore.isLoggedIn ? '今日节律' : 'HabitLink 习惯养成'"
        :title="heroTitle"
        :desc="heroSubtitle"
        :action-text="authStore.isLoggedIn ? '新建习惯' : '去登录'"
        @action="authStore.isLoggedIn ? goCreateHabit() : goLogin()"
      >
        <template #metrics>
          <view class="grid grid-cols-3 gap-3">
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                今日日期
              </view>
              <view class="mt-2 text-base font-semibold">
                {{ overview.date || '登录后展示' }}
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                今日完成率
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ completionRate }}%
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                今日安排
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ overview.plannedCount }}
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view v-if="!authStore.isLoggedIn" class="habit-panel mt-4 p-5">
        <view class="flex items-start gap-4">
          <view class="h-14 w-14 shrink-0 flex items-center justify-center rounded-[24rpx] bg-[#E6F7F4] text-[#0F766E]">
            <view class="i-solar-star-fall-bold text-[30rpx]" />
          </view>
          <view class="min-w-0 flex-1">
            <view class="habit-section-title">
              先搭一条属于自己的日常轨道
            </view>
            <view class="mt-2 habit-section-desc">
              登录后就可以创建习惯、每天打卡、查看记录和统计，先从一条最容易做到的小习惯开始。
            </view>
          </view>
        </view>

        <view class="habit-grid mt-5">
          <view class="habit-feature-card">
            <view class="text-sm text-[#16332F] font-semibold">创建习惯</view>
            <view class="habit-feature-card__desc">从最容易开始的一条目标先起步。</view>
          </view>
          <view class="habit-feature-card">
            <view class="text-sm text-[#16332F] font-semibold">每日打卡</view>
            <view class="habit-feature-card__desc">让行动有记录，让节奏能被看见。</view>
          </view>
          <view class="habit-feature-card habit-grid__span-2">
            <view class="text-sm text-[#16332F] font-semibold">统计反馈</view>
            <view class="habit-feature-card__desc">坚持多久、完成多少、哪里在波动，都能慢慢沉淀下来。</view>
          </view>
        </view>

        <view class="habit-primary-button mt-5 py-4 text-center text-sm font-semibold" @tap="goLogin">
          立即登录
        </view>
      </view>

      <template v-else>
        <view class="habit-grid mt-4">
          <view class="habit-overview-card habit-grid__span-2">
            <view class="flex items-start justify-between gap-4">
              <view class="min-w-0 flex-1">
                <view class="habit-overview-card__label">
                  今日总览
                </view>
                <view class="habit-overview-card__value">
                  {{ overview.completedCount }}
                  <text class="text-[28rpx] text-[#90A29F] font-medium">/ {{ overview.plannedCount }}</text>
                </view>
                <view class="habit-overview-card__subvalue">
                  {{ completionText }}
                </view>
              </view>
              <view class="h-20 w-20 shrink-0 flex items-center justify-center rounded-[26rpx] bg-[#E6F7F4] text-[#0F766E]">
                <view class="i-solar-widget-4-bold text-[34rpx]" />
              </view>
            </view>
            <view class="mt-4 habit-progress-track">
              <view class="habit-progress-fill transition-all duration-500" :style="{ width: `${completionRate}%` }" />
            </view>
          </view>

          <view class="habit-overview-card habit-overview-card--accent">
            <view class="habit-overview-card__label">
              待完成
            </view>
            <view class="habit-overview-card__value">
              {{ pendingHabits.length }}
            </view>
          </view>
          <view class="habit-overview-card habit-overview-card--accent">
            <view class="habit-overview-card__label">
              已完成
            </view>
            <view class="habit-overview-card__value">
              {{ completedHabits.length }}
            </view>
          </view>
        </view>

        <view v-if="!hasHabits" class="habit-empty-state mt-4">
          <view class="mx-auto h-16 w-16 flex items-center justify-center rounded-[28rpx] bg-[#EAF8F6] text-[#0F766E]">
            <view class="i-solar-clipboard-add-bold text-[32rpx]" />
          </view>
          <view class="mt-4 text-lg text-[#16332F] font-semibold">
            还没有习惯
          </view>
          <view class="mt-2 text-sm text-[#54706B] leading-6">
            {{ emptyHint }}
          </view>
          <view class="habit-primary-button mx-auto mt-6 w-40 py-3.5 text-center text-sm font-semibold" @tap="goCreateHabit">
            新建习惯
          </view>
        </view>

        <view v-if="pendingHabits.length > 0" class="mt-5">
          <view class="habit-group-header">
            <view class="habit-section-title">
              正在进行
            </view>
            <view class="habit-pill habit-pill--muted">
              {{ pendingHabits.length }} 项待完成
            </view>
          </view>

          <view
            v-for="habit in pendingHabits"
            :key="habit.habitId"
            class="habit-list-card mb-4 overflow-hidden"
          >
            <view class="h-2 w-full" :style="{ backgroundColor: getHabitColor(habit) }" />
            <view class="habit-list-card__body">
              <view class="flex items-start justify-between gap-3">
                <view class="min-w-0 flex-1">
                  <view class="flex items-center gap-3">
                    <view class="h-11 w-11 shrink-0 rounded-[20rpx]" :style="{ backgroundColor: `${getHabitColor(habit)}18` }" />
                    <view class="min-w-0 flex-1">
                      <view class="truncate text-base text-[#16332F] font-semibold">
                        {{ habit.habitName }}
                      </view>
                      <view class="mt-1 text-xs text-[#54706B]">
                        今天还差这一项
                      </view>
                    </view>
                  </view>

                  <view class="mt-4 flex flex-wrap gap-2">
                    <text class="habit-pill habit-pill--muted">{{ habit.ruleText || '规则待补充' }}</text>
                    <text v-if="habit.reminderTime" class="habit-pill habit-pill--secondary">提醒 {{ habit.reminderTime }}</text>
                    <text class="habit-pill habit-pill--primary">连续 {{ habit.currentStreak }} 天</text>
                  </view>
                </view>
                <view
                  class="shrink-0 rounded-[22rpx] px-4 py-3 text-sm text-white font-semibold"
                  :style="{ backgroundColor: getHabitColor(habit) }"
                  @tap="handleCheckIn(habit)"
                >
                  打卡
                </view>
              </view>

              <view class="habit-list-card__footer">
                <view
                  class="flex items-center justify-between rounded-[24rpx] bg-[#F2F8F7] px-4 py-3 text-sm text-[#54706B]"
                  @tap="goHabitRecords(habit)"
                >
                  <text>查看这条习惯的历史记录</text>
                  <text class="text-[#0F766E] font-semibold">去查看</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view v-if="completedHabits.length > 0" class="mt-5">
          <view class="habit-group-header">
            <view class="habit-section-title">
              已经完成
            </view>
            <view class="habit-pill habit-pill--success">
              {{ completedHabits.length }} 项已完成
            </view>
          </view>

          <view
            v-for="habit in completedHabits"
            :key="habit.habitId"
            class="habit-list-card mb-4"
          >
            <view class="habit-list-card__body">
              <view class="flex items-start justify-between gap-3">
                <view class="min-w-0 flex-1">
                  <view class="flex items-center gap-3">
                    <view class="h-11 w-11 flex items-center justify-center rounded-[20rpx] bg-[#E8F7EC] text-[#15803D]">
                      <view class="i-solar-check-circle-bold text-[24rpx]" />
                    </view>
                    <view class="min-w-0 flex-1">
                      <view class="truncate text-base text-[#16332F] font-semibold">
                        {{ habit.habitName }}
                      </view>
                      <view class="mt-1 text-xs text-[#54706B]">
                        今天已经完成
                      </view>
                    </view>
                  </view>
                  <view class="mt-4 flex flex-wrap gap-2">
                    <text class="habit-pill habit-pill--muted">{{ habit.ruleText || '规则待补充' }}</text>
                    <text class="habit-pill habit-pill--success">连续 {{ habit.currentStreak }} 天</text>
                  </view>
                </view>
                <view class="habit-pill habit-pill--success">
                  已完成
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="mt-5">
          <view class="habit-primary-button py-4 text-center text-sm font-semibold" @tap="goCreateHabit">
            新建习惯
          </view>
        </view>
      </template>
    </view>
  </view>
</template>
