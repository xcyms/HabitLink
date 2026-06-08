<script setup lang="ts">
import type * as API from '../../types'
import { getHabitListApi } from '../../api/habit.ts'
import { getHabitStatsDetailApi, getTodayOverviewApi } from '../../api/habitStats.ts'

const loading = ref(false)
const habits = ref<API.Habit[]>([])
const currentHabitId = ref<number>()
const stats = ref<API.HabitStats | null>(null)
const overview = ref<API.TodayOverview | null>(null)

/**
 * 加载今日总览。
 */
async function loadOverview() {
  try {
    overview.value = await getTodayOverviewApi()
  }
  catch (error) {
    console.error('加载今日总览失败:', error)
  }
}

/**
 * 加载习惯列表。
 */
async function loadHabits() {
  try {
    habits.value = await getHabitListApi()
    if (!currentHabitId.value && habits.value.length > 0) {
      currentHabitId.value = habits.value[0]?.id
    }
  }
  catch (error) {
    console.error('加载习惯列表失败:', error)
  }
}

/**
 * 加载当前习惯的统计详情。
 */
async function loadStats() {
  if (!currentHabitId.value) {
    stats.value = null
    return
  }

  loading.value = true
  try {
    stats.value = await getHabitStatsDetailApi(currentHabitId.value)
  }
  catch (error) {
    console.error('加载习惯统计详情失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 切换查看的习惯。
 */
async function handleHabitChange(value: any) {
  if (value === undefined || Array.isArray(value) || value === null) {
    return
  }
  const nextValue = typeof value === 'object' ? value.value : value
  if (nextValue === undefined || nextValue === null) {
    return
  }
  currentHabitId.value = Number(nextValue)
  await loadStats()
}

onMounted(async () => {
  await loadOverview()
  await loadHabits()
  await loadStats()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 px-3 md:px-0 space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="rounded-3xl border border-slate-200/80 bg-white p-5">
        <div class="text-sm text-slate-500">
          今日计划数
        </div>
        <div class="mt-3 text-3xl font-bold text-slate-900">
          {{ overview?.plannedCount ?? 0 }}
        </div>
      </div>
      <div class="rounded-3xl border border-slate-200/80 bg-white p-5">
        <div class="text-sm text-slate-500">
          今日完成数
        </div>
        <div class="mt-3 text-3xl font-bold text-slate-900">
          {{ overview?.completedCount ?? 0 }}
        </div>
      </div>
      <div class="rounded-3xl border border-slate-200/80 bg-white p-5">
        <div class="text-sm text-slate-500">
          今日完成率
        </div>
        <div class="mt-3 text-3xl font-bold text-slate-900">
          {{ overview?.completionRate ?? 0 }}%
        </div>
      </div>
    </div>

    <div class="rounded-3xl border border-slate-200/80 bg-white overflow-hidden">
      <div class="px-6 py-6 border-b border-slate-200/70">
        <h2 class="text-2xl font-bold text-slate-900">
          习惯统计
        </h2>
        <p class="mt-2 text-sm text-slate-500">
          当前页面用于查看单个习惯的基础统计数据，后续可以继续扩展趋势图、分类统计和运营看板。
        </p>
      </div>

      <div class="px-6 py-5 border-b border-slate-200/70 bg-slate-50/60">
        <a-form layout="inline">
          <a-form-item label="选择习惯">
            <a-select
              v-model:value="currentHabitId"
              placeholder="请选择习惯"
              style="width: 280px"
              @change="handleHabitChange"
            >
              <a-select-option
                v-for="habit in habits"
                :key="habit.id"
                :value="habit.id"
              >
                {{ habit.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
      </div>

      <div class="p-6">
        <div v-if="stats" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <div class="rounded-2xl border border-slate-200/70 p-4">
            <div class="text-sm text-slate-500">
              当前连续天数
            </div>
            <div class="mt-3 text-3xl font-bold text-slate-900">
              {{ stats.currentStreak }}
            </div>
          </div>
          <div class="rounded-2xl border border-slate-200/70 p-4">
            <div class="text-sm text-slate-500">
              最长连续天数
            </div>
            <div class="mt-3 text-3xl font-bold text-slate-900">
              {{ stats.longestStreak }}
            </div>
          </div>
          <div class="rounded-2xl border border-slate-200/70 p-4">
            <div class="text-sm text-slate-500">
              累计打卡次数
            </div>
            <div class="mt-3 text-3xl font-bold text-slate-900">
              {{ stats.totalCheckInCount }}
            </div>
          </div>
          <div class="rounded-2xl border border-slate-200/70 p-4">
            <div class="text-sm text-slate-500">
              最近一次完成
            </div>
            <div class="mt-3 text-base font-semibold text-slate-900">
              {{ stats.lastCheckInDate || '暂无记录' }}
            </div>
          </div>
        </div>

        <div v-if="stats" class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="rounded-2xl border border-slate-200/70 p-4">
            <div class="text-sm text-slate-500">
              近 7 天完成率
            </div>
            <div class="mt-3 text-2xl font-bold text-[#2563eb]">
              {{ stats.completionRate7d }}%
            </div>
          </div>
          <div class="rounded-2xl border border-slate-200/70 p-4">
            <div class="text-sm text-slate-500">
              近 30 天完成率
            </div>
            <div class="mt-3 text-2xl font-bold text-[#0f766e]">
              {{ stats.completionRate30d }}%
            </div>
          </div>
        </div>

        <div v-if="!stats && !loading" class="py-16 text-center text-sm text-slate-400">
          暂无统计数据
        </div>
      </div>
    </div>
  </div>
</template>
