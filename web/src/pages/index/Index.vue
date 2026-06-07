<script setup lang="ts">
import * as echarts from 'echarts'
import { useDark, useResizeObserver } from '@vueuse/core'
import { getDashboardStatsApi } from '../../api/stats.ts'
import { useUserStore } from '../../store/user.ts'
import { formatSize } from '../../utils/common.ts'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const stats = ref<any>(null)
const isDark = useDark()

/**
 * 图表实例引用
 */
const trendChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | undefined
let typeChart: echarts.ECharts | undefined

/**
 * 加载首页统计数据
 */
async function loadStats() {
  loading.value = true
  try {
    stats.value = await getDashboardStatsApi()
    nextTick(initCharts)
  }
  catch (error) {
    console.error('加载统计数据失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 根据主题返回图表配色
 */
function getChartColors() {
  const dark = isDark.value
  return {
    text: dark ? '#94a3b8' : '#64748b',
    grid: dark ? '#334155' : '#e2e8f0',
    bg: dark ? '#1e293b' : '#ffffff',
    textLight: dark ? '#f1f5f9' : '#1e293b',
  }
}

/**
 * 初始化首页图表
 */
function initCharts() {
  if (!stats.value)
    return

  trendChart?.dispose()
  typeChart?.dispose()

  const c = getChartColors()

  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: c.bg,
        borderColor: c.grid,
        textStyle: { color: c.textLight },
      },
      legend: { data: ['文件上传', '活跃用户'], bottom: 0, textStyle: { color: c.text } },
      grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: stats.value.dateLabels,
        axisLine: { lineStyle: { color: c.grid } },
        axisLabel: { color: c.text },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: c.grid } },
        axisLabel: { color: c.text },
      },
      series: [
        {
          name: '文件上传',
          type: 'line',
          smooth: true,
          data: stats.value.fileUploadTrend,
          itemStyle: { color: '#3b82f6' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59,130,246,0.3)' },
              { offset: 1, color: 'rgba(59,130,246,0)' },
            ]),
          },
        },
        {
          name: '活跃用户',
          type: 'bar',
          barWidth: '20%',
          data: stats.value.userActiveTrend,
          itemStyle: { color: '#22c55e' },
        },
      ],
    })
  }

  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
        backgroundColor: c.bg,
        borderColor: c.grid,
        textStyle: { color: c.textLight },
      },
      legend: { orient: 'horizontal', bottom: '0%', left: 'center', textStyle: { color: c.text } },
      series: [{
        name: '文件类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: c.bg, borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold', color: c.textLight } },
        labelLine: { show: false },
        data: stats.value.fileTypeDistribution,
      }],
    })
  }
}

watch(isDark, initCharts)

useResizeObserver([trendChartRef, typeChartRef], () => {
  trendChart?.resize()
  typeChart?.resize()
})

onMounted(loadStats)
onUnmounted(() => {
  trendChart?.dispose()
  typeChart?.dispose()
})

/**
 * 首页统计卡片
 */
const statCards = computed(() => [
  { title: '用户总数', value: stats.value?.userCount || 0, hint: '当前系统用户规模', icon: 'i-fa6-solid:users', gradient: 'from-blue-500 to-cyan-400' },
  { title: '文件总数', value: stats.value?.fileCount || 0, hint: '系统文件资源数量', icon: 'i-fa6-solid:file-lines', gradient: 'from-rose-500 to-orange-400' },
  { title: '存储占用', value: formatSize(stats.value?.totalStorageSize || 0), hint: '当前文件占用空间', icon: 'i-fa6-solid:database', gradient: 'from-orange-500 to-amber-400' },
  { title: '操作日志', value: stats.value?.logCount || 0, hint: '累计行为记录', icon: 'i-fa6-solid:clock-rotate-left', gradient: 'from-emerald-500 to-teal-400' },
])

/**
 * 快捷入口
 */
const quickActions = [
  { title: '用户管理', desc: '查看用户、角色和状态', icon: 'i-fa6-solid:user-group', to: '/users' },
  { title: '文件管理', desc: '上传、预览与批量操作', icon: 'i-fa6-solid:folder-open', to: '/files' },
  { title: '通知公告', desc: '查看未读并发布通知', icon: 'i-fa6-solid:bullhorn', to: '/notices' },
  { title: '系统设置', desc: '调整上传与系统参数', icon: 'i-fa6-solid:sliders', to: '/settings' },
]

/**
 * 待处理摘要
 */
const todoItems = computed(() => [
  {
    title: '今日上传',
    value: `${stats.value?.fileUploadTrend?.[stats.value?.fileUploadTrend?.length - 1] || 0}`,
    desc: '今天新增的文件上传数',
    to: '/files',
  },
  {
    title: '存储巡检',
    value: stats.value?.totalStorageSize ? `${formatSize(stats.value.totalStorageSize)}` : '0 B',
    desc: '关注文件存储增长趋势',
    to: '/files',
  },
  {
    title: '活跃变化',
    value: `${stats.value?.userActiveTrend?.[stats.value?.userActiveTrend?.length - 1] || 0}`,
    desc: '今日活跃用户数',
    to: '/log',
  },
])

function go(path: string) {
  router.push(path)
}
</script>

<template>
  <div class="p-4 md:p-6 space-y-6">
    <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold text-slate-800 dark:text-slate-100">
          下午好，{{ userStore.name }}
        </h2>
        <p class="text-slate-500 dark:text-slate-400 mt-1">
          这里是今天的系统概览，常用入口和关键指标都集中在这里。
        </p>
      </div>
      <a-button type="primary" class="flex items-center gap-2 w-fit" :loading="loading" @click="loadStats">
        <template #icon>
          <span class="i-fa6-solid:rotate text-[12px]" />
        </template>
        刷新数据
      </a-button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
      <div
        v-for="card in statCards" :key="card.title"
        class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 p-6 shadow-[0_2px_8px_rgba(0,0,0,0.04)] dark:shadow-none hover:shadow-[0_8px_24px_rgba(0,0,0,0.08)] dark:hover:shadow-[0_8px_24px_rgba(0,0,0,0.3)] transition-all duration-300 border border-slate-100 dark:border-slate-700"
      >
        <div :class="`absolute top-0 left-0 right-0 h-1 bg-gradient-to-r ${card.gradient}`" />
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <p class="text-sm font-medium text-slate-500 dark:text-slate-400 mb-2">
              {{ card.title }}
            </p>
            <h3 class="text-3xl font-bold text-slate-800 dark:text-slate-100 tracking-tight">
              {{ card.value }}
            </h3>
            <p class="text-xs text-slate-400 dark:text-slate-500 mt-3">
              {{ card.hint }}
            </p>
          </div>
          <div :class="`w-11 h-11 rounded-xl bg-gradient-to-br ${card.gradient} flex items-center justify-center`">
            <span :class="`${card.icon} text-lg text-white`" />
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-3 gap-5">
      <div class="xl:col-span-2 rounded-2xl bg-white dark:bg-slate-800/50 border border-slate-100 dark:border-slate-700 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700/50 flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-slate-800 dark:text-slate-100">
              快捷入口
            </h3>
            <p class="text-xs text-slate-500 dark:text-slate-400 mt-1">
              把最常用的管理动作放在首页直接进入。
            </p>
          </div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 p-5">
          <button
            v-for="item in quickActions"
            :key="item.title"
            class="text-left rounded-2xl border border-slate-100 dark:border-slate-700 bg-slate-50/80 dark:bg-slate-900/40 p-5 hover:border-blue-300 dark:hover:border-blue-500 hover:shadow-md transition-all"
            @click="go(item.to)"
          >
            <div class="flex items-start gap-4">
              <div class="w-11 h-11 rounded-xl bg-blue-100 dark:bg-blue-500/10 text-blue-600 dark:text-blue-300 flex items-center justify-center flex-shrink-0">
                <span :class="item.icon" class="text-lg" />
              </div>
              <div>
                <div class="font-semibold text-slate-800 dark:text-slate-100">
                  {{ item.title }}
                </div>
                <div class="text-sm text-slate-500 dark:text-slate-400 mt-1">
                  {{ item.desc }}
                </div>
              </div>
            </div>
          </button>
        </div>
      </div>

      <div class="rounded-2xl bg-white dark:bg-slate-800/50 border border-slate-100 dark:border-slate-700 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <h3 class="text-lg font-semibold text-slate-800 dark:text-slate-100">
            待处理摘要
          </h3>
          <p class="text-xs text-slate-500 dark:text-slate-400 mt-1">
            快速看到今天更值得关注的部分。
          </p>
        </div>
        <div class="p-5 space-y-4">
          <button
            v-for="item in todoItems"
            :key="item.title"
            class="w-full text-left rounded-2xl border border-slate-100 bg-slate-50/80 px-4 py-4 shadow-sm shadow-slate-100/70 transition-all hover:border-slate-200 hover:bg-white hover:shadow-md dark:border-slate-700 dark:bg-slate-900/55 dark:shadow-none dark:hover:border-slate-600 dark:hover:bg-slate-900"
            @click="go(item.to)"
          >
            <div class="flex items-center justify-between gap-4">
              <div>
                <div class="font-medium text-slate-800 dark:text-slate-100">
                  {{ item.title }}
                </div>
                <div class="text-xs text-slate-500 dark:text-slate-400 mt-1">
                  {{ item.desc }}
                </div>
              </div>
              <div class="text-lg font-bold text-slate-700 dark:text-slate-100">
                {{ item.value }}
              </div>
            </div>
          </button>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
      <div class="lg:col-span-2 rounded-2xl bg-white dark:bg-slate-800/50 border border-slate-100 dark:border-slate-700 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <h3 class="text-lg font-semibold text-slate-800 dark:text-slate-100">
            近 7 天业务趋势
          </h3>
        </div>
        <div class="p-4">
          <div ref="trendChartRef" class="h-[320px] w-full" />
        </div>
      </div>
      <div class="rounded-2xl bg-white dark:bg-slate-800/50 border border-slate-100 dark:border-slate-700 overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-100 dark:border-slate-700/50">
          <h3 class="text-lg font-semibold text-slate-800 dark:text-slate-100">
            文件类型分布
          </h3>
        </div>
        <div class="p-4">
          <div ref="typeChartRef" class="h-[320px] w-full" />
        </div>
      </div>
    </div>
  </div>
</template>
