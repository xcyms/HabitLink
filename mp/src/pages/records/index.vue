<script setup lang="ts">
import type { CheckInRecordDTO, HabitDTO } from '@/types/type'

definePage({
  name: 'records',
  layout: 'tabbar',
  style: {
    navigationBarTitleText: '记录',
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
 * 当前查看的习惯名称。
 */
const currentHabitName = ref('')

/**
 * 当前习惯的打卡记录。
 */
const records = ref<CheckInRecordDTO[]>([])

/**
 * 补打卡数量。
 */
const makeupCount = computed(() => records.value.filter(item => item.isMakeup === 1).length)

/**
 * 最近一次打卡日期。
 */
const latestRecordDate = computed(() => records.value[0]?.recordDate || '暂无记录')

/**
 * 加载习惯列表。
 */
async function loadHabits() {
  if (!authStore.isLoggedIn) {
    habits.value = []
    currentHabitId.value = null
    currentHabitName.value = ''
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
    else if (habits.value.length > 0) {
      currentHabitId.value = habits.value[0].id || null
    }

    const currentHabit = habits.value.find(item => item.id === currentHabitId.value)
    currentHabitName.value = currentHabit?.name || String(route.query?.habitName || '')
  }
  catch (error: any) {
    console.error('加载习惯列表失败:', error)
    toast.error(error.message || '加载习惯列表失败')
  }
}

/**
 * 加载当前习惯的打卡记录。
 */
async function loadRecords() {
  if (!authStore.isLoggedIn) {
    records.value = []
    return
  }

  if (!currentHabitId.value) {
    records.value = []
    return
  }

  try {
    const res = await Apis.checkIn.list({
      params: {
        habitId: currentHabitId.value,
      },
    })
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '加载打卡记录失败')
    }
    records.value = res.data
  }
  catch (error: any) {
    console.error('加载打卡记录失败:', error)
    toast.error(error.message || '加载打卡记录失败')
  }
}

/**
 * 切换当前查看的习惯。
 */
async function switchHabit(habit: HabitDTO) {
  currentHabitId.value = habit.id || null
  currentHabitName.value = habit.name
  await loadRecords()
}

/**
 * 进入登录页。
 */
function goLogin() {
  router.push({ name: 'login' })
}

onShow(async () => {
  await loadHabits()
  await loadRecords()
})
</script>

<template>
  <view class="habit-page">
    <view class="habit-shell">
      <habit-page-header
        tag="打卡记录"
        title="回看每一次完成"
        desc="记录不只是留痕，更是在帮你看到自己的连续感和真实节奏。"
      >
        <template #metrics>
          <view class="habit-metrics-grid">
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                当前习惯
              </view>
              <view class="habit-metric-card__value truncate text-[28rpx] !leading-[1.25]">
                {{ currentHabitName || '未选择' }}
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                记录数量
              </view>
              <view class="habit-metric-card__value">
                {{ records.length }}
              </view>
            </view>
            <view class="habit-metric-card">
              <view class="habit-metric-card__label">
                补打卡
              </view>
              <view class="habit-metric-card__value">
                {{ makeupCount }}
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view v-if="!authStore.isLoggedIn" class="habit-empty-state mt-4">
        <view class="mx-auto h-16 w-16 flex items-center justify-center rounded-[28rpx] bg-[#EEF2FF]">
          <view class="i-solar-lock-keyhole-bold text-[30rpx]" />
        </view>
        <view class="mt-4 text-base text-[#16332F] font-semibold">
          暂未展示你的记录
        </view>
        <view class="mt-2 text-sm text-[#54706B] leading-6">
          登录后可以查看更多打卡记录、补打卡信息和每次完成的时间轨迹。
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

      <view v-if="authStore.isLoggedIn" class="mt-4">
        <view class="habit-group-header">
          <view class="habit-section-title">
            {{ currentHabitName || '暂无选中习惯' }}
          </view>
          <view class="habit-pill habit-pill--muted">
            最近：{{ latestRecordDate }}
          </view>
        </view>

        <view v-if="records.length === 0" class="habit-empty-state">
          <view class="text-base text-[#16332F] font-semibold">
            还没有打卡记录
          </view>
          <view class="mt-2 text-sm text-[#54706B]">
            等第一条记录出现后，这里会更像你的节律时间线。
          </view>
        </view>

        <view v-else>
          <view
            v-for="record in records"
            :key="record.id"
            class="habit-list-card mb-4"
          >
            <view class="habit-list-card__body">
              <view class="flex items-start gap-4">
                <view class="mt-2 flex flex-col items-center">
                  <view class="h-4 w-4 shrink-0 rounded-full bg-[#5B7CF6]" />
                  <view class="mt-2 h-full w-[2px] bg-[#EEF1F8]" />
                </view>
                <view class="min-w-0 flex-1">
                  <view class="flex items-center justify-between gap-3">
                    <view class="text-base text-[#16332F] font-semibold">
                      {{ record.recordDate }}
                    </view>
                    <view class="habit-pill" :class="record.isMakeup === 1 ? 'habit-pill--warning' : 'habit-pill--success'">
                      {{ record.isMakeup === 1 ? '补打卡' : '已完成' }}
                    </view>
                  </view>
                  <view class="mt-3 rounded-[22rpx] bg-[#F6F7FB] px-4 py-4">
                    <view class="flex items-center justify-between gap-3">
                      <view class="text-xs text-[#656B85]">
                        提交时间：{{ record.checkInTime }}
                      </view>
                      <view class="text-xs text-[#9AA2B4]">
                        {{ record.isMakeup === 1 ? '来自补打卡' : '当日完成' }}
                      </view>
                    </view>
                    <view v-if="record.note" class="mt-3 rounded-[18rpx] bg-white px-3 py-3 text-xs text-[#656B85] leading-6">
                      备注：{{ record.note }}
                    </view>
                    <view v-else class="mt-3 rounded-[18rpx] bg-white px-3 py-3 text-xs text-[#9AA2B4] leading-6">
                      这次打卡没有填写备注，节奏依然被记录下来了。
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>
