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
const systemInfo = useSystemInfo()

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

function handleBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    router.back()
  }
  else {
    router.pushTab({ name: 'index' })
  }
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
  <view class="habit-page min-h-screen">
    <view class="habit-shell habit-shell--default">
      <view class="flex items-center justify-between" :style="{ paddingTop: `${systemInfo.statusBarHeight.value + 8}px` }">
        <view class="h-11 w-11 flex items-center justify-center rounded-[24rpx] bg-white text-[#23263A] shadow-sm" @tap="handleBack">
          <view class="i-solar-alt-arrow-left-linear text-[28rpx]" />
        </view>
        <view class="text-[24rpx] text-[#656B85] font-medium">
          HabitLink
        </view>
      </view>

      <view class="habit-hero mt-4 px-5 pb-6 pt-5">
        <view class="relative z-10">
          <view class="habit-pill bg-white/16 text-white/88">
            {{ isEdit ? '编辑已有习惯' : '创建新习惯' }}
          </view>
          <view class="habit-page-header__title mt-4">
            {{ isEdit ? '微调这条习惯的节奏' : '把想坚持的事安排进每天' }}
          </view>
          <view class="habit-page-header__desc mt-3">
            从名字、提醒到规则，都尽量保持简单，让这条习惯更容易被真正执行。
          </view>
        </view>
      </view>

      <view class="habit-metrics-grid mt-5">
        <view class="habit-metric-card">
          <view class="habit-metric-card__label">
            当前模式
          </view>
          <view class="habit-metric-card__value text-[30rpx] !leading-[1.25]">
            {{ isEdit ? '编辑习惯' : '新建习惯' }}
          </view>
        </view>
        <view class="habit-metric-card">
          <view class="habit-metric-card__label">
            执行规则
          </view>
          <view class="habit-metric-card__value text-[30rpx] !leading-[1.25]">
            {{ rulePreviewText }}
          </view>
        </view>
        <view class="habit-metric-card">
          <view class="habit-metric-card__label">
            开始日期
          </view>
          <view class="habit-metric-card__value text-[30rpx] !leading-[1.25]">
            {{ form.startDate }}
          </view>
        </view>
      </view>

      <view class="habit-panel mt-5 p-5">
        <view class="habit-section-title">
          基础信息
        </view>
        <view class="habit-section-desc mt-2">
          先给这条习惯一个容易理解、容易记住的名字。
        </view>

        <view class="mt-5">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            习惯名称
          </view>
          <input
            v-model="form.name"
            class="h-12 border border-[#E8EBF3] rounded-[24rpx] bg-[#F8FAFD] px-4 text-sm text-[#23263A]"
            placeholder="例如：每天阅读 20 分钟"
          >
        </view>

        <view class="mt-4">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            习惯描述
          </view>
          <textarea
            v-model="form.description"
            class="min-h-24 border border-[#E8EBF3] rounded-[24rpx] bg-[#F8FAFD] px-4 py-3 text-sm text-[#23263A]"
            placeholder="补充说明，例如睡前阅读、通勤时学习等"
          />
        </view>
      </view>

      <view class="habit-panel mt-4 p-5">
        <view class="habit-section-title">
          时间设置
        </view>
        <view class="mt-5">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            开始日期
          </view>
          <picker mode="date" :value="form.startDate" @change="handleStartDateChange">
            <view class="h-12 flex items-center border border-[#E8EBF3] rounded-[24rpx] bg-[#F8FAFD] px-4 text-sm text-[#23263A]">
              {{ form.startDate || '请选择开始日期' }}
            </view>
          </picker>
        </view>

        <view class="mt-4">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            结束日期
          </view>
          <view class="flex items-center gap-3">
            <picker mode="date" :value="form.endDate || ''" class="flex-1" @change="handleEndDateChange">
              <view class="h-12 flex items-center border border-[#E8EBF3] rounded-[24rpx] bg-[#F8FAFD] px-4 text-sm text-[#23263A]">
                {{ form.endDate || '不设置结束日期' }}
              </view>
            </picker>
            <view class="habit-light-button px-4 py-3 text-xs" @tap="clearEndDate">
              清空
            </view>
          </view>
        </view>
      </view>

      <view class="habit-panel mt-4 p-5">
        <view class="habit-section-title">
          提醒与规则
        </view>

        <view class="mt-5">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            提醒时间
          </view>
          <view class="flex gap-3">
            <view
              class="habit-pill px-5 py-3"
              :class="form.reminderEnabled === 1 ? 'habit-pill--primary' : 'habit-pill--muted'"
              @tap="changeReminderEnabled(1)"
            >
              开启提醒
            </view>
            <view
              class="habit-pill px-5 py-3"
              :class="form.reminderEnabled === 0 ? 'habit-pill--primary' : 'habit-pill--muted'"
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
            <view class="mt-3 h-12 flex items-center border border-[#E8EBF3] rounded-[24rpx] bg-[#F8FAFD] px-4 text-sm text-[#23263A]">
              {{ form.reminderTime || '请选择提醒时间' }}
            </view>
          </picker>
        </view>

        <view class="mt-5">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            规则类型
          </view>
          <view class="flex gap-3">
            <view
              class="habit-pill px-5 py-3"
              :class="form.ruleType === 'DAILY' ? 'habit-pill--primary' : 'habit-pill--muted'"
              @tap="changeRuleType('DAILY')"
            >
              每天
            </view>
            <view
              class="habit-pill px-5 py-3"
              :class="form.ruleType === 'WEEKLY' ? 'habit-pill--primary' : 'habit-pill--muted'"
              @tap="changeRuleType('WEEKLY')"
            >
              每周
            </view>
          </view>

          <view v-if="form.ruleType === 'WEEKLY'" class="mt-3 flex flex-wrap gap-3">
            <view
              v-for="item in weekOptions"
              :key="item.value"
              class="habit-pill px-5 py-3"
              :class="form.ruleDays.includes(item.value) ? 'habit-pill--secondary' : 'habit-pill--muted'"
              @tap="toggleRuleDay(item.value)"
            >
              {{ item.label }}
            </view>
          </view>

          <view class="mt-4 rounded-[24rpx] bg-[#F6F7FB] px-4 py-4 text-[22rpx] text-[#656B85] leading-7">
            当前规则：{{ rulePreviewText }}
          </view>
        </view>
      </view>

      <view class="habit-panel mt-4 p-5">
        <view class="habit-section-title">
          补打卡设置
        </view>

        <view class="mt-5">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            是否允许补打卡
          </view>
          <view class="flex gap-3">
            <view
              class="habit-pill px-5 py-3"
              :class="form.allowMakeup === 1 ? 'habit-pill--primary' : 'habit-pill--muted'"
              @tap="changeAllowMakeup(1)"
            >
              允许
            </view>
            <view
              class="habit-pill px-5 py-3"
              :class="form.allowMakeup === 0 ? 'habit-pill--primary' : 'habit-pill--muted'"
              @tap="changeAllowMakeup(0)"
            >
              不允许
            </view>
          </view>
        </view>

        <view v-if="form.allowMakeup === 1" class="mt-4">
          <view class="mb-2 text-[22rpx] text-[#656B85] font-medium">
            补打卡天数
          </view>
          <input
            v-model="form.makeupLimitDays"
            type="number"
            class="h-12 border border-[#E8EBF3] rounded-[24rpx] bg-[#F8FAFD] px-4 text-sm text-[#23263A]"
            placeholder="请输入允许补打卡的天数"
          >
        </view>
      </view>

      <view
        class="habit-primary-button mt-5 py-4 text-center text-sm font-semibold"
        @tap="submit"
      >
        {{ isEdit ? '保存修改' : '创建习惯' }}
      </view>
    </view>
  </view>
</template>
