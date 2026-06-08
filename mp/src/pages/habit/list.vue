<script setup lang="ts">
import type { HabitDTO } from '@/types/type'

definePage({
  name: 'habit-list',
  layout: 'default',
  style: {
    navigationBarTitleText: '我的习惯',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const router = useRouter()
const toast = useToast()
const authStore = useAuthStore()

/**
 * 页面加载状态。
 */
const loading = ref(false)

/**
 * 当前登录用户的习惯列表。
 */
const habits = ref<HabitDTO[]>([])

/**
 * 是否存在习惯数据。
 */
const hasHabits = computed(() => habits.value.length > 0)

/**
 * 进行中的习惯。
 */
const activeHabits = computed(() => habits.value.filter(item => item.status === 1))

/**
 * 已暂停的习惯。
 */
const pausedHabits = computed(() => habits.value.filter(item => item.status !== 1))

/**
 * 习惯总览数据。
 */
const habitSummary = computed(() => ({
  total: habits.value.length,
  activeCount: activeHabits.value.length,
  pausedCount: pausedHabits.value.length,
}))

/**
 * 加载习惯列表。
 */
async function loadHabits() {
  if (!authStore.isLoggedIn) {
    habits.value = []
    uni.stopPullDownRefresh()
    return
  }

  loading.value = true
  try {
    const res = await Apis.habit.list({})
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '加载习惯列表失败')
    }
    habits.value = res.data
  }
  catch (error: any) {
    console.error('加载习惯列表失败:', error)
    toast.error(error.message || '加载习惯列表失败')
  }
  finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

/**
 * 获取规则文案。
 * 便于列表中快速理解习惯的执行频率。
 */
function getRuleText(habit: HabitDTO) {
  if (habit.ruleType === 'DAILY') {
    return '每天执行'
  }

  const weekMap: Record<number, string> = {
    1: '周一',
    2: '周二',
    3: '周三',
    4: '周四',
    5: '周五',
    6: '周六',
    7: '周日',
  }

  if (!habit.ruleDays || habit.ruleDays.length === 0) {
    return '每周执行'
  }

  return `每周 ${habit.ruleDays.map(item => weekMap[item] || item).join('、')}`
}

/**
 * 获取习惯主色。
 */
function getHabitColor(habit: HabitDTO) {
  return habit.color || '#0F766E'
}

/**
 * 进入编辑页。
 */
function goEditHabit(habit: HabitDTO) {
  if (!habit.id) {
    return
  }

  router.push({
    name: 'habit-edit',
    query: {
      id: String(habit.id),
    },
  })
}

/**
 * 查看某条习惯的记录。
 */
function goRecords(habit: HabitDTO) {
  if (!habit.id) {
    return
  }

  router.push({
    name: 'records',
    query: {
      habitId: String(habit.id),
      habitName: habit.name,
    },
  })
}

/**
 * 查看某条习惯的统计。
 */
function goStats(habit: HabitDTO) {
  if (!habit.id) {
    return
  }

  router.push({
    name: 'stats',
    query: {
      habitId: String(habit.id),
    },
  })
}

/**
 * 创建习惯。
 */
function goCreateHabit() {
  router.push({ name: 'habit-edit' })
}

/**
 * 暂停或恢复习惯。
 */
async function toggleHabitStatus(habit: HabitDTO) {
  if (!habit.id) {
    return
  }

  try {
    const isActive = habit.status === 1
    const res = isActive
      ? await Apis.habit.pause({
          params: {
            id: habit.id,
          },
        })
      : await Apis.habit.resume({
          params: {
            id: habit.id,
          },
        })

    if (res.code !== 200) {
      throw new Error(res.message || '更新习惯状态失败')
    }

    toast.success(isActive ? '已暂停习惯' : '已恢复习惯')
    await loadHabits()
  }
  catch (error: any) {
    console.error('更新习惯状态失败:', error)
    toast.error(error.message || '更新习惯状态失败')
  }
}

/**
 * 删除习惯。
 */
function deleteHabit(habit: HabitDTO) {
  if (!habit.id) {
    return
  }

  uni.showModal({
    title: '删除习惯',
    content: `确认删除习惯“${habit.name}”吗？`,
    confirmText: '确认删除',
    cancelText: '取消',
    success: async (result) => {
      if (!result.confirm) {
        return
      }

      try {
        const res = await Apis.habit.delete({
          params: {
            id: habit.id as number,
          },
        })

        if (res.code !== 200) {
          throw new Error(res.message || '删除习惯失败')
        }

        toast.success('删除习惯成功')
        await loadHabits()
      }
      catch (error: any) {
        console.error('删除习惯失败:', error)
        toast.error(error.message || '删除习惯失败')
      }
    },
  })
}

onShow(() => {
  loadHabits()
})

onPullDownRefresh(() => {
  loadHabits()
})
</script>

<template>
  <view class="habit-page">
    <view class="habit-shell">
      <habit-page-header
        tag="个人习惯管理"
        title="我的习惯"
        desc="这里统一管理自己的习惯，保持节奏清楚、状态清楚、每一条习惯的去向也清楚。"
        action-text="新建"
        @action="goCreateHabit"
      >
        <template #metrics>
          <view class="grid grid-cols-3 gap-3">
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                全部习惯
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ habitSummary.total }}
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                进行中
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ habitSummary.activeCount }}
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                已暂停
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ habitSummary.pausedCount }}
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view v-if="!authStore.isLoggedIn" class="habit-empty-state mt-4">
        <view class="mx-auto h-16 w-16 flex items-center justify-center rounded-[28rpx] bg-[#EAF8F6] text-[#0F766E]">
          <view class="i-solar-lock-keyhole-bold text-[30rpx]" />
        </view>
        <view class="mt-4 text-base text-[#16332F] font-semibold">
          请先登录
        </view>
        <view class="mt-2 text-sm text-[#54706B] leading-6">
          登录后才能查看和管理你的个人习惯。
        </view>
      </view>

      <view v-else-if="!hasHabits && !loading" class="habit-empty-state mt-4">
        <view class="mx-auto h-16 w-16 flex items-center justify-center rounded-[28rpx] bg-[#EAF8F6] text-[#0F766E]">
          <view class="i-solar-notebook-bookmark-bold text-[30rpx]" />
        </view>
        <view class="mt-4 text-base text-[#16332F] font-semibold">
          还没有创建习惯
        </view>
        <view class="mt-2 text-sm text-[#54706B] leading-6">
          先创建第一条习惯，再开始每天的节律管理。
        </view>
        <view class="habit-primary-button mx-auto mt-5 w-36 py-3.5 text-center text-sm font-semibold" @tap="goCreateHabit">
          立即创建
        </view>
      </view>

      <template v-else>
        <view v-if="activeHabits.length > 0" class="mt-5">
          <view class="habit-group-header">
            <view class="habit-section-title">
              进行中
            </view>
            <view class="habit-pill habit-pill--primary">
              {{ activeHabits.length }} 条
            </view>
          </view>

          <view
            v-for="habit in activeHabits"
            :key="habit.id"
            class="habit-list-card mb-4 overflow-hidden"
          >
            <view class="h-2 w-full" :style="{ backgroundColor: getHabitColor(habit) }" />
            <view class="habit-list-card__body">
              <view class="flex items-start justify-between gap-3">
                <view class="min-w-0 flex-1">
                  <view class="flex items-center gap-3">
                    <view class="h-10 w-10 shrink-0 rounded-[20rpx]" :style="{ backgroundColor: `${getHabitColor(habit)}18` }" />
                    <view class="min-w-0 flex-1">
                      <view class="truncate text-lg text-[#16332F] font-semibold">
                        {{ habit.name }}
                      </view>
                      <view class="mt-1 text-xs text-[#54706B]">
                        正在坚持中
                      </view>
                    </view>
                  </view>

                  <view v-if="habit.description" class="mt-3 text-sm text-[#54706B] leading-6">
                    {{ habit.description }}
                  </view>

                  <view class="mt-4 flex flex-wrap gap-2">
                    <text class="habit-pill habit-pill--muted">
                      {{ getRuleText(habit) }}
                    </text>
                    <text class="habit-pill habit-pill--primary">
                      开始于 {{ habit.startDate }}
                    </text>
                    <text v-if="habit.reminderEnabled === 1 && habit.reminderTime" class="habit-pill habit-pill--secondary">
                      提醒 {{ habit.reminderTime }}
                    </text>
                    <text class="habit-pill habit-pill--muted">
                      {{ habit.allowMakeup === 1 ? `允许补打卡 ${habit.makeupLimitDays} 天` : '不允许补打卡' }}
                    </text>
                  </view>
                </view>
                <view class="habit-pill habit-pill--success">
                  进行中
                </view>
              </view>

              <view class="habit-list-card__footer">
                <view class="grid grid-cols-3 gap-3">
                  <view class="rounded-[24rpx] bg-[#EAF8F6] px-3 py-3 text-center text-sm text-[#0F766E]" @tap="goEditHabit(habit)">
                    编辑
                  </view>
                  <view class="rounded-[24rpx] bg-[#EEF8FB] px-3 py-3 text-center text-sm text-[#0B718B]" @tap="goRecords(habit)">
                    记录
                  </view>
                  <view class="rounded-[24rpx] bg-[#FFF6E8] px-3 py-3 text-center text-sm text-[#B7791F]" @tap="goStats(habit)">
                    统计
                  </view>
                </view>

                <view class="mt-3 flex gap-3">
                  <view class="flex-1 rounded-[24rpx] bg-[#F4FBFA] px-4 py-3 text-center text-sm text-[#0F766E]" @tap="toggleHabitStatus(habit)">
                    暂停习惯
                  </view>
                  <view class="flex-1 rounded-[24rpx] bg-[#FFF1F1] px-4 py-3 text-center text-sm text-[#DC2626]" @tap="deleteHabit(habit)">
                    删除习惯
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view v-if="pausedHabits.length > 0" class="mt-5">
          <view class="habit-group-header">
            <view class="habit-section-title">
              已暂停
            </view>
            <view class="habit-pill habit-pill--warning">
              {{ pausedHabits.length }} 条
            </view>
          </view>

          <view
            v-for="habit in pausedHabits"
            :key="habit.id"
            class="habit-list-card mb-4 overflow-hidden"
          >
            <view class="h-2 w-full bg-[#F2D7A7]" />
            <view class="habit-list-card__body">
              <view class="flex items-start justify-between gap-3">
                <view class="min-w-0 flex-1">
                  <view class="truncate text-lg text-[#16332F] font-semibold">
                    {{ habit.name }}
                  </view>
                  <view class="mt-2 text-sm text-[#54706B] leading-6">
                    {{ habit.description || '这条习惯目前处于暂停状态，随时可以继续。' }}
                  </view>
                  <view class="mt-4 flex flex-wrap gap-2">
                    <text class="habit-pill habit-pill--muted">
                      {{ getRuleText(habit) }}
                    </text>
                    <text class="habit-pill habit-pill--warning">
                      已暂停
                    </text>
                  </view>
                </view>
                <view class="habit-pill habit-pill--warning">
                  已暂停
                </view>
              </view>

              <view class="habit-list-card__footer">
                <view class="flex gap-3">
                  <view class="flex-1 rounded-[24rpx] bg-[#EAF8F6] px-4 py-3 text-center text-sm text-[#0F766E]" @tap="toggleHabitStatus(habit)">
                    恢复习惯
                  </view>
                  <view class="flex-1 rounded-[24rpx] bg-[#FFF1F1] px-4 py-3 text-center text-sm text-[#DC2626]" @tap="deleteHabit(habit)">
                    删除习惯
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </template>
    </view>
  </view>
</template>
