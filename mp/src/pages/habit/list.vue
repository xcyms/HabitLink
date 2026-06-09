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
 * 进入登录页。
 */
function goLogin() {
  router.push({ name: 'login' })
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
        :tag="authStore.isLoggedIn ? '习惯空间' : '习惯管理'"
        :title="authStore.isLoggedIn ? '把每条习惯安排清楚' : '登录后统一管理你的习惯'"
        :desc="authStore.isLoggedIn ? '这里专注管理习惯本身：查看状态、调整节奏、暂停或继续，不把页面做得太杂。' : '登录后可以查看自己的习惯状态，按需要继续、暂停或重新开始。'"
        :action-text="authStore.isLoggedIn ? '新建' : '去登录'"
        :show-back="true"
        @action="authStore.isLoggedIn ? goCreateHabit() : goLogin()"
      >
        <template #metrics>
          <view class="habit-metrics-grid">
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                全部习惯
              </view>
              <view class="habit-metric-card__value">
                {{ habitSummary.total }}
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                进行中
              </view>
              <view class="habit-metric-card__value">
                {{ habitSummary.activeCount }}
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                已暂停
              </view>
              <view class="habit-metric-card__value">
                {{ habitSummary.pausedCount }}
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view v-if="!authStore.isLoggedIn" class="habit-empty-state mt-4">
        <view class="mx-auto h-16 w-16 flex items-center justify-center rounded-[28rpx] bg-[#EEF1ED] text-[#728D87]">
          <view class="i-solar-lock-keyhole-bold text-[30rpx]" />
        </view>
        <view class="mt-4 text-base text-[#324340] font-semibold">
          请先登录
        </view>
        <view class="mt-2 text-sm text-[#6F807B] leading-6">
          登录后可以统一整理自己的习惯状态，决定哪些继续、哪些暂停，以及什么时候重新开始。
        </view>
        <view class="habit-primary-button mx-auto mt-5 w-40 py-3.5 text-center text-sm font-semibold" @tap="goLogin">
          去登录查看
        </view>
      </view>

      <view v-else-if="!hasHabits && !loading" class="habit-empty-state mt-4">
        <view class="mx-auto h-16 w-16 flex items-center justify-center rounded-[28rpx] bg-[#EEF1ED] text-[#728D87]">
          <view class="i-solar-notebook-bookmark-bold text-[30rpx]" />
        </view>
        <view class="mt-4 text-base text-[#324340] font-semibold">
          还没有创建习惯
        </view>
        <view class="mt-2 text-sm text-[#6F807B] leading-6">
          先从一条最容易做到的小习惯开始，把管理页面保持得轻一点、清楚一点。
        </view>
        <view class="habit-primary-button mx-auto mt-5 w-36 py-3.5 text-center text-sm font-semibold" @tap="goCreateHabit">
          立即创建
        </view>
      </view>

      <template v-else>
        <view class="habit-panel mt-4 p-5">
          <view class="habit-group-header !mb-4 !px-0">
            <view>
              <view class="habit-section-title">
                当前管理概览
              </view>
              <view class="habit-section-desc mt-2">
                先看总量和状态分布，再决定今天要调整哪一条。
              </view>
            </view>
          </view>
          <view class="habit-grid">
            <view class="habit-value-card !p-5">
              <view class="habit-value-card__eyebrow">
                全部习惯
              </view>
              <view class="habit-value-card__title !mt-3">
                {{ habitSummary.total }}
              </view>
            </view>
            <view class="habit-value-card !p-5">
              <view class="habit-value-card__eyebrow">
                进行中
              </view>
              <view class="habit-value-card__title !mt-3">
                {{ habitSummary.activeCount }}
              </view>
            </view>
            <view class="habit-value-card habit-grid__span-2 !p-5">
              <view class="habit-value-card__eyebrow">
                管理提示
              </view>
              <view class="habit-value-card__desc !mt-3">
                这里先只处理习惯本身的新增、编辑、暂停和删除；记录与统计请从底部导航单独进入查看。
              </view>
            </view>
          </view>
        </view>

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
                    <view class="h-10 w-10 flex shrink-0 items-center justify-center rounded-[20rpx]" :style="{ backgroundColor: `${getHabitColor(habit)}14`, color: getHabitColor(habit) }">
                      <view :class="`i-solar-${habit.icon || 'star-fall'}-bold text-[22rpx]`" />
                    </view>
                    <view class="min-w-0 flex-1">
                      <view class="truncate text-lg text-[#324340] font-semibold">
                        {{ habit.name }}
                      </view>
                      <view class="mt-1 text-xs text-[#6F807B]">
                        正在坚持中
                      </view>
                    </view>
                  </view>

                  <view v-if="habit.description" class="mt-3 text-sm text-[#6F807B] leading-6">
                    {{ habit.description }}
                  </view>

                  <view class="grid grid-cols-2 mt-4 gap-3">
                    <view class="rounded-[22rpx] bg-[#F3F5F2] px-4 py-3">
                      <view class="text-[20rpx] text-[#A1ADA7]">
                        执行规则
                      </view>
                      <view class="mt-2 text-sm text-[#324340] font-medium">
                        {{ getRuleText(habit) }}
                      </view>
                    </view>
                    <view class="rounded-[22rpx] bg-[#F3F5F2] px-4 py-3">
                      <view class="text-[20rpx] text-[#A1ADA7]">
                        开始日期
                      </view>
                      <view class="mt-2 text-sm text-[#324340] font-medium">
                        {{ habit.startDate }}
                      </view>
                    </view>
                    <view class="rounded-[22rpx] bg-[#F3F5F2] px-4 py-3">
                      <view class="text-[20rpx] text-[#A1ADA7]">
                        提醒状态
                      </view>
                      <view class="mt-2 text-sm text-[#324340] font-medium">
                        {{ habit.reminderEnabled === 1 && habit.reminderTime ? `每天 ${habit.reminderTime}` : '当前未开启' }}
                      </view>
                    </view>
                    <view class="rounded-[22rpx] bg-[#F3F5F2] px-4 py-3">
                      <view class="text-[20rpx] text-[#A1ADA7]">
                        补打卡
                      </view>
                      <view class="mt-2 text-sm text-[#324340] font-medium">
                        {{ habit.allowMakeup === 1 ? `${habit.makeupLimitDays} 天内可补打` : '不允许补打卡' }}
                      </view>
                    </view>
                  </view>
                </view>
                <view class="habit-pill habit-pill--success !bg-[#EEF4EF] !text-[#6E9383]">
                  进行中
                </view>
              </view>

              <view class="habit-list-card__footer">
                <view class="rounded-[24rpx] bg-[#F5F6F3] px-4 py-4 text-[22rpx] text-[#6F807B] leading-7 mb-4">
                  {{ habit.allowMakeup === 1 ? `当前允许在 ${habit.makeupLimitDays} 天内补打卡，节奏断了也可以重新接上。` : '当前不支持补打卡，更适合想保持明确边界的习惯。' }}
                </view>

                <view class="grid grid-cols-3 gap-3">
                  <view class="rounded-[24rpx] bg-[#EEF1ED] px-3 py-3 text-center text-sm text-[#728D87]" @tap="goEditHabit(habit)">
                    编辑
                  </view>
                  <view class="rounded-[24rpx] bg-[#F3F5F2] px-3 py-3 text-center text-sm text-[#6F807B]" @tap="toggleHabitStatus(habit)">
                    暂停
                  </view>
                  <view class="rounded-[24rpx] bg-[#FBF1F1] px-3 py-3 text-center text-sm text-[#C46D6D]" @tap="deleteHabit(habit)">
                    删除
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
            <view class="h-2 w-full bg-[#F8D694]" />
            <view class="habit-list-card__body">
              <view class="flex items-start justify-between gap-3">
                <view class="min-w-0 flex-1">
                  <view class="flex items-center gap-3">
                    <view class="h-10 w-10 flex shrink-0 items-center justify-center rounded-[20rpx] bg-[#F3F5F2] text-[#8A9A87]">
                      <view :class="`i-solar-${habit.icon || 'star-fall'}-bold text-[22rpx]`" />
                    </view>
                    <view class="truncate text-lg text-[#324340] font-semibold">
                      {{ habit.name }}
                    </view>
                  </view>
                  <view class="mt-2 text-sm text-[#6F807B] leading-6">
                    {{ habit.description || '这条习惯目前处于暂停状态，随时可以继续。' }}
                  </view>
                </view>
                <view class="habit-pill habit-pill--warning !bg-[#F3F1EB] !text-[#9B8663]">
                  已暂停
                </view>
              </view>

              <view class="habit-list-card__footer">
                <view class="rounded-[24rpx] bg-[#F5F6F3] px-4 py-4 text-[22rpx] text-[#6F807B] leading-7">
                  当前规则：{{ getRuleText(habit) }}。这条习惯暂时停在这里，等你准备好再继续。
                </view>

                <view class="grid grid-cols-3 gap-3">
                  <view class="rounded-[24rpx] bg-[#EEF1ED] px-4 py-3 text-center text-sm text-[#728D87]" @tap="toggleHabitStatus(habit)">
                    恢复习惯
                  </view>
                  <view class="rounded-[24rpx] bg-[#F3F5F2] px-4 py-3 text-center text-sm text-[#6F807B]" @tap="goEditHabit(habit)">
                    编辑
                  </view>
                  <view class="rounded-[24rpx] bg-[#FBF1F1] px-4 py-3 text-center text-sm text-[#C46D6D]" @tap="deleteHabit(habit)">
                    删除
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
