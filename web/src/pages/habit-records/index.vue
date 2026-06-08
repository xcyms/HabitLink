<script setup lang="ts">
import type * as API from '../../types'
import { getCheckInListApi } from '../../api/checkIn.ts'
import { getHabitListApi } from '../../api/habit.ts'

const loading = ref(false)
const habits = ref<API.Habit[]>([])
const records = ref<API.CheckInRecord[]>([])
const currentHabitId = ref<number>()

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
 * 加载指定习惯的打卡记录。
 */
async function loadRecords() {
  if (!currentHabitId.value) {
    records.value = []
    return
  }

  loading.value = true
  try {
    records.value = await getCheckInListApi(currentHabitId.value)
  }
  catch (error) {
    console.error('加载打卡记录失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 切换当前查看的习惯。
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
  await loadRecords()
}

onMounted(async () => {
  await loadHabits()
  await loadRecords()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 px-3 md:px-0 space-y-6">
    <div class="rounded-3xl border border-slate-200/80 bg-white overflow-hidden">
      <div class="px-6 py-6 border-b border-slate-200/70">
        <h2 class="text-2xl font-bold text-slate-900">
          打卡记录
        </h2>
        <p class="mt-2 text-sm text-slate-500">
          按习惯查看用户的打卡历史，当前版本主要用于确认记录写入是否正常，后续可以继续补充筛选和导出能力。
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

      <div class="overflow-hidden">
        <a-table
          :data-source="records"
          :loading="loading"
          row-key="id"
          :pagination="false"
        >
          <a-table-column title="记录ID" data-index="id" width="90" />
          <a-table-column title="习惯名称" data-index="habitName" />
          <a-table-column title="业务日期" data-index="recordDate" width="140" />
          <a-table-column title="提交时间" data-index="checkInTime" width="200" />
          <a-table-column title="是否补打卡" width="120">
            <template #default="{ record }">
              {{ record.isMakeup === 1 ? '是' : '否' }}
            </template>
          </a-table-column>
          <a-table-column title="来源" data-index="source" width="140" />
          <a-table-column title="备注" data-index="note">
            <template #default="{ text }">
              {{ text || '-' }}
            </template>
          </a-table-column>
        </a-table>
      </div>
    </div>
  </div>
</template>
