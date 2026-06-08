<script setup lang="ts">
import type { HabitDTO } from '@/types/type'
import dayjs from 'dayjs'

definePage({
  name: 'habit-edit',
  layout: 'default',
  style: {
    navigationBarTitleText: '习惯编辑',
    navigationStyle: 'custom',
  },
})

const route = useRoute()
const router = useRouter()
const toast = useToast()

/**
 * 每周规则可选项。
 * 数字与后端规则工具中的星期定义保持一致，1 表示周一，7 表示周日。
 */
const weekOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
]

/**
 * 当前页面是否为编辑状态。
 */
const isEdit = computed(() => Boolean(route.query?.id))

/**
 * 习惯表单。
 * 第一版先覆盖最核心的字段。
 */
const form = reactive<HabitDTO>({
  name: '',
  description: '',
  category: 'STUDY',
  icon: 'book',
  color: '#2563EB',
  status: 1,
  startDate: dayjs().format('YYYY-MM-DD'),
  endDate: null,
  allowMakeup: 1,
  makeupLimitDays: 3,
  reminderEnabled: 1,
  reminderTime: '21:00',
  sortOrder: 1,
  ruleType: 'DAILY',
  ruleDays: [],
})

/**
 * 规则文案预览。
 * 让用户在提交前快速确认当前设置。
 */
const rulePreviewText = computed(() => {
  if (form.ruleType === 'DAILY') {
    return '每天执行'
  }

  if (form.ruleDays.length === 0) {
    return '请选择每周执行日'
  }

  const labels = weekOptions
    .filter(item => form.ruleDays.includes(item.value))
    .map(item => item.label)

  return `每周 ${labels.join('、')}`
})

/**
 * 加载习惯详情并回填表单。
 */
async function loadDetail() {
  const id = Number(route.query?.id || 0)
  if (!id) {
    return
  }

  try {
    const res = await Apis.habit.detail({
      params: {
        id,
      },
    })
    if (res.code !== 200 || !res.data) {
      throw new Error(res.message || '加载习惯详情失败')
    }
    Object.assign(form, res.data)
    form.ruleDays = Array.isArray(res.data.ruleDays) ? [...res.data.ruleDays] : []
  }
  catch (error: any) {
    console.error('加载习惯详情失败:', error)
    toast.error(error.message || '加载习惯详情失败')
  }
}

/**
 * 切换规则类型。
 * 切换为每天时清空按周执行日，避免提交冗余数据。
 */
function changeRuleType(ruleType: 'DAILY' | 'WEEKLY') {
  form.ruleType = ruleType
  if (ruleType === 'DAILY') {
    form.ruleDays = []
  }
}

/**
 * 切换某个星期的选中状态。
 */
function toggleRuleDay(day: number) {
  if (form.ruleType !== 'WEEKLY') {
    return
  }

  if (form.ruleDays.includes(day)) {
    form.ruleDays = form.ruleDays.filter(item => item !== day)
    return
  }

  form.ruleDays = [...form.ruleDays, day].sort((left, right) => left - right)
}

/**
 * 切换补打卡开关。
 * 关闭时重置补打卡天数，减少脏数据。
 */
function changeAllowMakeup(value: number) {
  form.allowMakeup = value
  if (value === 0) {
    form.makeupLimitDays = 0
  }
  else if (form.makeupLimitDays <= 0) {
    form.makeupLimitDays = 3
  }
}

/**
 * 切换提醒开关。
 * 关闭提醒时保留默认时间格式，便于后续再次开启。
 */
function changeReminderEnabled(value: number) {
  form.reminderEnabled = value
  if (value === 1 && !form.reminderTime) {
    form.reminderTime = '21:00'
  }
}

/**
 * 清空结束日期。
 */
function clearEndDate() {
  form.endDate = null
}

/**
 * 处理开始日期选择。
 */
function handleStartDateChange(event: any) {
  form.startDate = event.detail.value
  if (form.endDate && dayjs(form.endDate).isBefore(dayjs(form.startDate))) {
    form.endDate = form.startDate
  }
}

/**
 * 处理结束日期选择。
 */
function handleEndDateChange(event: any) {
  form.endDate = event.detail.value
}

/**
 * 处理提醒时间选择。
 */
function handleReminderTimeChange(event: any) {
  form.reminderTime = event.detail.value
}

/**
 * 执行表单校验。
 */
function validateForm() {
  if (!form.name.trim()) {
    toast.warning('请输入习惯名称')
    return false
  }

  if (!form.startDate) {
    toast.warning('请选择开始日期')
    return false
  }

  if (form.endDate && dayjs(form.endDate).isBefore(dayjs(form.startDate))) {
    toast.warning('结束日期不能早于开始日期')
    return false
  }

  if (form.ruleType === 'WEEKLY' && form.ruleDays.length === 0) {
    toast.warning('请选择每周执行日')
    return false
  }

  if (form.allowMakeup === 1 && form.makeupLimitDays < 1) {
    toast.warning('补打卡天数至少为 1 天')
    return false
  }

  if (form.reminderEnabled === 1 && !form.reminderTime) {
    toast.warning('请选择提醒时间')
    return false
  }

  return true
}

/**
 * 提交创建或更新请求。
 */
async function submit() {
  if (!validateForm()) {
    return
  }

  try {
    const payload: HabitDTO = {
      ...form,
      ruleDays: form.ruleType === 'WEEKLY' ? [...form.ruleDays] : [],
      endDate: form.endDate || null,
      makeupLimitDays: form.allowMakeup === 1 ? form.makeupLimitDays : 0,
      reminderTime: form.reminderEnabled === 1 ? form.reminderTime : '',
    }

    const res = isEdit.value
      ? await Apis.habit.update({ data: payload })
      : await Apis.habit.create({ data: payload })

    if (res.code !== 200) {
      throw new Error(res.message || '保存失败')
    }
    toast.success(isEdit.value ? '更新成功' : '创建成功')
    router.back()
  }
  catch (error: any) {
    console.error('保存习惯失败:', error)
    toast.error(error.message || '保存失败')
  }
}

onShow(() => {
  if (isEdit.value) {
    loadDetail()
  }
})
</script>

<template>
  <view class="min-h-screen bg-[#f3f6fb] px-5 pb-10 pt-4">
    <view class="overflow-hidden rounded-[28rpx] bg-white shadow-[0_18px_50px_rgba(15,23,42,0.08)]">
      <view class="relative overflow-hidden from-[#111827] via-[#2563eb] to-[#60a5fa] bg-gradient-to-r p-5 text-white">
        <view class="absolute h-32 w-32 rounded-full bg-white/10 blur-2xl -right-4 -top-6" />
        <view class="absolute h-24 w-24 rounded-full bg-blue-100/15 blur-xl left-6 bottom-0" />
        <view class="relative z-10 text-lg font-semibold">
        {{ isEdit ? '编辑习惯' : '新建习惯' }}
        </view>
        <view class="relative z-10 mt-2 text-sm text-white/80 leading-6">
          用更清楚的规则和提醒，把想坚持的事真正安排进每天。
        </view>
      </view>

      <view class="grid grid-cols-2 gap-3 p-4">
        <view class="rounded-2xl bg-[#f8fafc] px-4 py-4">
          <view class="text-xs text-slate-500">
            当前模式
          </view>
          <view class="mt-2 text-base text-slate-900 font-semibold">
            {{ isEdit ? '编辑已有习惯' : '创建新习惯' }}
          </view>
        </view>
        <view class="rounded-2xl bg-[#f8fafc] px-4 py-4">
          <view class="text-xs text-slate-500">
            执行规则
          </view>
          <view class="mt-2 text-base text-slate-900 font-semibold">
            {{ rulePreviewText }}
          </view>
        </view>
      </view>
    </view>

    <view class="mt-4 rounded-[28rpx] bg-white p-5 shadow-[0_18px_50px_rgba(15,23,42,0.08)]">
      <view class="text-sm text-slate-900 font-semibold">
        基础信息
      </view>

      <view class="mb-2 text-xs text-slate-500">
        习惯名称
      </view>
      <input
        v-model="form.name"
        class="h-12 rounded-2xl bg-[#f8fafc] px-4 text-sm text-slate-900"
        placeholder="例如：每天阅读"
      >

      <view class="mb-2 mt-4 text-xs text-slate-500">
        习惯描述
      </view>
      <textarea
        v-model="form.description"
        class="min-h-24 rounded-2xl bg-[#f8fafc] px-4 py-3 text-sm text-slate-900"
        placeholder="补充说明，例如阅读20分钟"
      />
    </view>

    <view class="mt-4 rounded-[28rpx] bg-white p-5 shadow-[0_18px_50px_rgba(15,23,42,0.08)]">
      <view class="text-sm text-slate-900 font-semibold">
        时间设置
      </view>
      <view class="mb-2 mt-4 text-xs text-slate-500">
        开始日期
      </view>
      <picker mode="date" :value="form.startDate" @change="handleStartDateChange">
        <view class="h-12 flex items-center rounded-2xl bg-[#f8fafc] px-4 text-sm text-slate-900">
          {{ form.startDate || '请选择开始日期' }}
        </view>
      </picker>

      <view class="mb-2 mt-4 text-xs text-slate-500">
        结束日期
      </view>
      <view class="flex items-center gap-3">
        <picker mode="date" :value="form.endDate || ''" class="flex-1" @change="handleEndDateChange">
          <view class="h-12 flex items-center rounded-2xl bg-[#f8fafc] px-4 text-sm text-slate-900">
            {{ form.endDate || '不设置结束日期' }}
          </view>
        </picker>
        <view
          class="rounded-2xl bg-[#eef2ff] px-4 py-3 text-xs text-slate-600"
          @tap="clearEndDate"
        >
          清空
        </view>
      </view>
    </view>

    <view class="mt-4 rounded-[28rpx] bg-white p-5 shadow-[0_18px_50px_rgba(15,23,42,0.08)]">
      <view class="text-sm text-slate-900 font-semibold">
        提醒与频率
      </view>
      <view class="mb-2 mt-4 text-xs text-slate-500">
        提醒时间
      </view>
      <view class="flex gap-3">
        <view
          class="rounded-full px-4 py-2.5 text-sm"
          :class="form.reminderEnabled === 1 ? 'bg-[#0f766e] text-white' : 'bg-[#ecfeff] text-slate-700'"
          @tap="changeReminderEnabled(1)"
        >
          开启提醒
        </view>
        <view
          class="rounded-full px-4 py-2.5 text-sm"
          :class="form.reminderEnabled === 0 ? 'bg-[#0f766e] text-white' : 'bg-[#ecfeff] text-slate-700'"
          @tap="changeReminderEnabled(0)"
        >
          关闭提醒
        </view>
      </view>
      <picker
        v-if="form.reminderEnabled === 1"
        mode="time"
        :value="form.reminderTime || '21:00'"
        @change="handleReminderTimeChange"
      >
        <view class="mt-3 h-12 flex items-center rounded-2xl bg-[#f8fafc] px-4 text-sm text-slate-900">
          {{ form.reminderTime || '请选择提醒时间' }}
        </view>
      </picker>

      <view class="mb-2 mt-4 text-xs text-slate-500">
        规则类型
      </view>
      <view class="flex gap-3">
        <view
          class="rounded-full px-4 py-2.5 text-sm"
          :class="form.ruleType === 'DAILY' ? 'bg-[#2563eb] text-white' : 'bg-[#eef2ff] text-slate-700'"
          @tap="changeRuleType('DAILY')"
        >
          每天
        </view>
        <view
          class="rounded-full px-4 py-2.5 text-sm"
          :class="form.ruleType === 'WEEKLY' ? 'bg-[#2563eb] text-white' : 'bg-[#eef2ff] text-slate-700'"
          @tap="changeRuleType('WEEKLY')"
        >
          每周
        </view>
      </view>

      <view v-if="form.ruleType === 'WEEKLY'" class="mt-3 flex flex-wrap gap-3">
        <view
          v-for="item in weekOptions"
          :key="item.value"
          class="rounded-full px-4 py-2.5 text-sm"
          :class="form.ruleDays.includes(item.value) ? 'bg-[#dbeafe] text-[#1d4ed8]' : 'bg-[#f8fafc] text-slate-600'"
          @tap="toggleRuleDay(item.value)"
        >
          {{ item.label }}
        </view>
      </view>

      <view class="mt-3 rounded-2xl bg-[#f8fafc] px-4 py-3 text-xs text-slate-500">
        当前规则：{{ rulePreviewText }}
      </view>
    </view>

    <view class="mt-4 rounded-[28rpx] bg-white p-5 shadow-[0_18px_50px_rgba(15,23,42,0.08)]">
      <view class="text-sm text-slate-900 font-semibold">
        补打卡设置
      </view>
      <view class="mb-2 mt-4 text-xs text-slate-500">
        是否允许补打卡
      </view>
      <view class="flex gap-3">
        <view
          class="rounded-full px-4 py-2.5 text-sm"
          :class="form.allowMakeup === 1 ? 'bg-[#0f766e] text-white' : 'bg-[#ecfeff] text-slate-700'"
          @tap="changeAllowMakeup(1)"
        >
          允许
        </view>
        <view
          class="rounded-full px-4 py-2.5 text-sm"
          :class="form.allowMakeup === 0 ? 'bg-[#0f766e] text-white' : 'bg-[#ecfeff] text-slate-700'"
          @tap="changeAllowMakeup(0)"
        >
          不允许
        </view>
      </view>

      <view v-if="form.allowMakeup === 1">
        <view class="mb-2 mt-4 text-xs text-slate-500">
          补打卡天数
        </view>
        <input
          v-model="form.makeupLimitDays"
          type="number"
          class="h-12 rounded-2xl bg-[#f8fafc] px-4 text-sm text-slate-900"
          placeholder="请输入允许补打卡的天数"
        >
      </view>
    </view>

    <view
      class="mt-5 rounded-[28rpx] bg-[#2563eb] py-4 text-center text-sm text-white font-semibold shadow-[0_14px_28px_rgba(37,99,235,0.26)]"
      @tap="submit"
    >
      {{ isEdit ? '保存修改' : '创建习惯' }}
    </view>
  </view>
</template>
