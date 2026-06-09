<script setup lang="ts">
import type { HabitDTO, HabitStatsDTO } from '@/types/type'

definePage({
  name: 'stats',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '统计',
    navigationStyle: 'custom',
  },
})

const route = useRoute()
const router = useRouter()
const toast = useToast()
const authStore = useAuthStore()

/**
 * 当前用户的习惯列表。
 */
const habits = ref<HabitDTO[]>([])

/**
 * 当前查看的习惯 ID。
 */
const currentHabitId = ref<number | null>(null)

/**
 * 当前习惯统计详情。
 */
const stats = ref<HabitStatsDTO | null>(null)

/**
 * 当前选中的习惯名称。
 */
const currentHabitName = computed(() => {
  return habits.value.find(item => item.id === currentHabitId.value)?.name || '当前习惯'
})

/**
 * 当前连续表现评价。
 */
const streakLevelText = computed(() => {
  const streak = Number(stats.value?.currentStreak || 0)
  if (streak >= 30) {
    return '已经进入稳定坚持区间。'
  }
  if (streak >= 14) {
    return '节奏正在明显形成。'
  }
  if (streak >= 7) {
    return '已经走过最难的前期阶段。'
  }
  if (streak > 0) {
    return '开头不错，继续把节奏接住。'
  }
  return '从今天开始重新建立连续感。'
})

/**
 * 根据 30 天完成率生成一句提示。
 */
const completionInsight = computed(() => {
  const rate = Number(stats.value?.completionRate30d || 0)
  if (rate >= 90) {
    return '近 30 天表现非常稳定，已经接近长期习惯化。'
  }
  if (rate >= 70) {
    return '整体完成得不错，再把波动收窄一点会更稳。'
  }
  if (rate >= 40) {
    return '有明显执行基础，下一步重点是提升连续性。'
  }
  return '当前完成率偏低，可以先把目标拆小，降低启动门槛。'
})

/**
 * 近 7 天完成率条宽度。
 */
const completionRate7dWidth = computed(() => `${Math.min(Number(stats.value?.completionRate7d || 0), 100)}%`)

/**
 * 近 30 天完成率条宽度。
 */
const completionRate30dWidth = computed(() => `${Math.min(Number(stats.value?.completionRate30d || 0), 100)}%`)

/**
 * 加载习惯列表。
 */
async function loadHabits() {
  if (!authStore.isLoggedIn) {
    habits.value = []
    currentHabitId.value = null
    stats.value = null
    return
  }

  try {
    const res = await Apis.habit.list({})
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '加载习惯列表失败')
    }
    habits.value = res.data

    const queryHabitId = Number(route.query?.habitId || 0)
    if (queryHabitId) {
      currentHabitId.value = queryHabitId
    }
    else if (!currentHabitId.value && habits.value.length > 0) {
      currentHabitId.value = habits.value[0].id || null
    }
  }
  catch (error: any) {
    console.error('加载习惯列表失败:', error)
    toast.error(error.message || '加载习惯列表失败')
  }
}

/**
 * 加载当前习惯的统计详情。
 */
async function loadStats() {
  if (!authStore.isLoggedIn) {
    stats.value = null
    return
  }

  if (!currentHabitId.value) {
    stats.value = null
    return
  }

  try {
    const res = await Apis.habitStats.detail({
      params: {
        habitId: currentHabitId.value,
      },
    })
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '加载统计数据失败')
    }
    stats.value = res.data
  }
  catch (error: any) {
    console.error('加载统计数据失败:', error)
    toast.error(error.message || '加载统计数据失败')
  }
}

/**
 * 切换统计对应的习惯。
 */
async function switchHabit(habit: HabitDTO) {
  currentHabitId.value = habit.id || null
  await loadStats()
}

/**
 * 进入登录页。
 */
function goLogin() {
  router.push({ name: 'login' })
}

onShow(async () => {
  await loadHabits()
  await loadStats()
})
</script>

<template>
  <view class="habit-page">
    <view class="habit-shell">
      <habit-page-header
        tag="习惯统计"
        title="看见坚持的变化"
        desc="先看最核心的连续表现和完成率，把坚持这件事变得更有感知。"
      >
        <template #metrics>
          <view class="habit-metrics-grid">
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                当前连续
              </view>
              <view class="habit-metric-card__value">
                {{ stats?.currentStreak || 0 }}
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                近 7 天
              </view>
              <view class="habit-metric-card__value">
                {{ stats?.completionRate7d || 0 }}<text class="habit-metric-card__suffix">
                  %
                </text>
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                近 30 天
              </view>
              <view class="habit-metric-card__value">
                {{ stats?.completionRate30d || 0 }}<text class="habit-metric-card__suffix">
                  %
                </text>
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view v-if="!authStore.isLoggedIn" class="habit-empty-state mt-4">
        <view class="mx-auto h-16 w-16 flex items-center justify-center rounded-[28rpx] bg-[#EEF2FF]">
          <view class="i-solar-chart-2-bold text-[30rpx]" />
        </view>
        <view class="mt-4 text-base text-[#16332F] font-semibold">
          暂未展示你的统计
        </view>
        <view class="mt-2 text-sm text-[#54706B] leading-6">
          登录后可以查看更多连续天数、近 7 天和近 30 天完成率等习惯变化。
        </view>
        <view class="habit-primary-button mx-auto mt-5 w-40 py-3.5 text-center text-sm font-semibold" @tap="goLogin">
          去登录查看
        </view>
      </view>

      <view v-else-if="habits.length > 0" class="habit-panel mt-4 p-4">
        <view class="habit-group-header">
          <view class="habit-section-title">
            选择习惯
          </view>
          <view class="text-xs text-[#90A29F]">
            共 {{ habits.length }} 项
          </view>
        </view>
        <scroll-view scroll-x class="whitespace-nowrap">
          <view class="flex gap-3">
            <view
              v-for="habit in habits"
              :key="habit.id"
              class="rounded-full px-4 py-2.5 text-sm"
              :class="habit.id === currentHabitId ? 'bg-[#5B7CF6] text-white' : 'bg-[#F6F7FB] text-[#656B85]'"
              @tap="switchHabit(habit)"
            >
              {{ habit.name }}
            </view>
          </view>
        </scroll-view>
      </view>

      <view v-if="authStore.isLoggedIn && stats" class="habit-grid mt-4">
        <view class="habit-overview-card habit-grid__span-2">
          <view class="flex items-start justify-between gap-4">
            <view class="min-w-0 flex-1">
              <view class="habit-pill habit-pill--primary">
                {{ currentHabitName }}
              </view>
              <view class="habit-overview-card__value">
                {{ stats.currentStreak }}
                <text class="text-[28rpx] text-[#90A29F] font-medium">
                  天连续
                </text>
              </view>
              <view class="habit-overview-card__subvalue">
                {{ streakLevelText }}
              </view>
            </view>
            <view class="h-20 w-20 flex shrink-0 items-center justify-center rounded-[26rpx] bg-[#EEF2FF] text-[#5B7CF6]">
              <view class="i-solar-widget-4-bold text-[34rpx]" />
            </view>
          </view>
        </view>

        <view class="habit-overview-card">
          <view class="habit-overview-card__label">
            最长连续
          </view>
          <view class="habit-overview-card__value">
            {{ stats.longestStreak }}
          </view>
        </view>
        <view class="habit-overview-card">
          <view class="habit-overview-card__label">
            累计打卡
          </view>
          <view class="habit-overview-card__value">
            {{ stats.totalCheckInCount }}
          </view>
        </view>
        <view class="habit-overview-card habit-grid__span-2">
          <view class="habit-overview-card__label">
            最近完成
          </view>
          <view class="mt-3 text-lg text-[#16332F] font-semibold">
            {{ stats.lastCheckInDate || '暂无记录' }}
          </view>
        </view>
      </view>

      <view v-if="authStore.isLoggedIn && stats" class="habit-panel mt-5 p-5">
        <view class="habit-group-header">
          <view class="habit-section-title">
            完成率趋势
          </view>
          <view class="text-xs text-[#90A29F]">
            最近表现
          </view>
        </view>

        <view class="habit-overview-card habit-overview-card--accent">
          <view class="flex items-center justify-between text-sm">
            <text class="text-[#656B85]">
              近 7 天完成率
            </text>
            <text class="text-[#5B7CF6] font-semibold">
              {{ stats.completionRate7d }}%
            </text>
          </view>
          <view class="habit-progress-track mt-3 bg-[#E7ECFF]">
            <view class="h-full rounded-full bg-[#5B7CF6] transition-all duration-500" :style="{ width: completionRate7dWidth }" />
          </view>
          <view class="habit-overview-card__subvalue">
            反映最近一周的执行稳定度，适合判断这条习惯是否容易坚持。
          </view>
        </view>

        <view class="habit-overview-card habit-overview-card--accent mt-3">
          <view class="flex items-center justify-between text-sm">
            <text class="text-[#656B85]">
              近 30 天完成率
            </text>
            <text class="text-[#19A38C] font-semibold">
              {{ stats.completionRate30d }}%
            </text>
          </view>
          <view class="habit-progress-track mt-3">
            <view class="h-full rounded-full bg-[#19A38C] transition-all duration-500" :style="{ width: completionRate30dWidth }" />
          </view>
          <view class="habit-overview-card__subvalue">
            更适合观察长期变化，帮助你判断目标是否过重或提醒是否合适。
          </view>
        </view>

        <view class="mt-4 rounded-[24rpx] bg-[#F6F7FB] px-4 py-4 text-sm text-[#656B85] leading-6">
          {{ completionInsight }}
        </view>
      </view>

      <view v-else-if="authStore.isLoggedIn" class="habit-empty-state mt-4">
        <view class="text-base text-[#16332F] font-semibold">
          暂无统计数据
        </view>
        <view class="mt-2 text-sm text-[#54706B]">
          选择一条习惯并开始打卡后，这里就会出现趋势和连续表现。
        </view>
      </view>
    </view>
  </view>
</template>
