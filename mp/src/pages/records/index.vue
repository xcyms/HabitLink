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
const toast = useToast()

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
          <view class="grid grid-cols-3 gap-3">
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                当前习惯
              </view>
              <view class="mt-2 truncate text-base font-semibold">
                {{ currentHabitName || '未选择' }}
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                记录数量
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ records.length }}
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/12 px-4 py-4 backdrop-blur">
              <view class="text-xs text-white/70">
                补打卡
              </view>
              <view class="mt-2 text-2xl font-semibold">
                {{ makeupCount }}
              </view>
            </view>
          </view>
        </template>
      </habit-page-header>

      <view v-if="habits.length > 0" class="habit-panel mt-4 p-4">
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
              :class="habit.id === currentHabitId ? 'bg-[#0F766E] text-white' : 'bg-[#F2F8F7] text-[#54706B]'"
              @tap="switchHabit(habit)"
            >
              {{ habit.name }}
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="mt-4">
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
              <view class="flex items-start gap-3">
                <view class="mt-2 h-4 w-4 shrink-0 rounded-full bg-[#0F766E]" />
                <view class="min-w-0 flex-1">
                  <view class="flex items-center justify-between gap-3">
                    <view class="text-base text-[#16332F] font-semibold">
                      {{ record.recordDate }}
                    </view>
                    <view class="habit-pill" :class="record.isMakeup === 1 ? 'habit-pill--warning' : 'habit-pill--success'">
                      {{ record.isMakeup === 1 ? '补打卡' : '已完成' }}
                    </view>
                  </view>
                  <view class="mt-3 rounded-[22rpx] bg-[#F2F8F7] px-4 py-4">
                    <view class="text-xs text-[#54706B]">
                      提交时间：{{ record.checkInTime }}
                    </view>
                    <view v-if="record.note" class="mt-3 rounded-[18rpx] bg-white px-3 py-3 text-xs text-[#54706B] leading-5">
                      备注：{{ record.note }}
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
