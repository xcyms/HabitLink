<script setup lang="ts">
import type { SystemMonitorInfo } from '../../api/system.ts'
import { getSystemMonitorInfoApi } from '../../api/system.ts'

const loading = ref(false)
const monitorInfo = ref<SystemMonitorInfo>()

/**
 * 加载系统监控信息。
 */
async function loadMonitorInfo() {
  try {
    loading.value = true
    monitorInfo.value = await getSystemMonitorInfoApi()
  }
  catch (error) {
    console.error('获取系统监控信息失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 格式化字节大小。
 */
function formatBytes(bytes?: number): string {
  if (!bytes || bytes === 0)
    return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return `${Number.parseFloat((bytes / k ** i).toFixed(2))} ${sizes[i]}`
}

/**
 * 格式化运行时长。
 */
function formatUptime(uptime?: number): string {
  if (!uptime)
    return '0 秒'

  const seconds = Math.floor(uptime / 1000)
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  if (days > 0)
    return `${days} 天 ${hours} 小时 ${minutes} 分钟`
  if (hours > 0)
    return `${hours} 小时 ${minutes} 分钟 ${secs} 秒`
  if (minutes > 0)
    return `${minutes} 分钟 ${secs} 秒`
  return `${secs} 秒`
}

/**
 * 格式化日期时间。
 */
function formatDate(timestamp?: number): string {
  if (!timestamp)
    return '未知'

  const date = new Date(Number(timestamp))
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 统一限制百分比显示范围。
 */
function clampRate(rate?: number) {
  return Math.min(Math.max(rate || 0, 0), 100)
}

/**
 * 根据资源占用率返回风险色阶。
 */
function getRateTone(rate?: number) {
  const value = clampRate(rate)
  if (value >= 90)
    return 'rose'
  if (value >= 75)
    return 'amber'
  return 'emerald'
}

const memoryUsageRate = computed(() => clampRate(monitorInfo.value?.memoryInfo?.memoryUsageRate))
const swapUsageRate = computed(() => clampRate(monitorInfo.value?.memoryInfo?.swapUsageRate))
const diskInfos = computed(() => monitorInfo.value?.diskInfos || [])
const topDiskRate = computed(() => {
  if (!diskInfos.value.length)
    return 0
  return Math.max(...diskInfos.value.map(item => item.usageRate || 0))
})

const riskItems = computed(() => {
  const items = []

  if (memoryUsageRate.value >= 90) {
    items.push({
      title: '物理内存占用过高',
      desc: `当前内存使用率 ${memoryUsageRate.value.toFixed(1)}%，建议排查高占用进程或调整 JVM 配置。`,
      tone: 'rose',
    })
  }
  else if (memoryUsageRate.value >= 75) {
    items.push({
      title: '物理内存接近预警线',
      desc: `当前内存使用率 ${memoryUsageRate.value.toFixed(1)}%，建议关注后续增长趋势。`,
      tone: 'amber',
    })
  }

  if (swapUsageRate.value >= 60) {
    items.push({
      title: '交换空间使用偏高',
      desc: `当前交换空间使用率 ${swapUsageRate.value.toFixed(1)}%，可能影响系统响应速度。`,
      tone: 'amber',
    })
  }

  if (topDiskRate.value >= 90) {
    items.push({
      title: '磁盘空间不足',
      desc: `最高磁盘使用率 ${topDiskRate.value.toFixed(1)}%，建议及时清理空间。`,
      tone: 'rose',
    })
  }
  else if (topDiskRate.value >= 80) {
    items.push({
      title: '磁盘空间接近上限',
      desc: `最高磁盘使用率 ${topDiskRate.value.toFixed(1)}%，建议提前扩容或清理。`,
      tone: 'amber',
    })
  }

  if (!items.length) {
    items.push({
      title: '当前运行状态稳定',
      desc: '核心资源占用均处于可控范围内，暂未发现明显风险项。',
      tone: 'emerald',
    })
  }

  return items
})

const summaryCards = computed(() => [
  {
    title: '运行时长',
    value: formatUptime(monitorInfo.value?.runtimeInfo?.uptime),
    hint: '服务连续运行时间',
    icon: 'i-fa6-solid:clock',
    tone: 'slate',
  },
  {
    title: '物理内存',
    value: `${memoryUsageRate.value.toFixed(1)}%`,
    hint: `${formatBytes(monitorInfo.value?.memoryInfo?.usedMemory)} / ${formatBytes(monitorInfo.value?.memoryInfo?.totalMemory)}`,
    icon: 'i-fa6-solid:memory',
    tone: getRateTone(memoryUsageRate.value),
  },
  {
    title: '最高磁盘占用',
    value: `${topDiskRate.value.toFixed(1)}%`,
    hint: diskInfos.value.length ? `共 ${diskInfos.value.length} 个磁盘分区` : '暂无磁盘分区数据',
    icon: 'i-fa6-solid:hard-drive',
    tone: getRateTone(topDiskRate.value),
  },
  {
    title: '线程数量',
    value: `${monitorInfo.value?.runtimeInfo?.threadCount || 0}`,
    hint: `峰值 ${monitorInfo.value?.runtimeInfo?.peakThreadCount || 0}`,
    icon: 'i-fa6-solid:code-branch',
    tone: 'amber',
  },
])

onMounted(() => {
  loadMonitorInfo()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 px-3 md:px-0 space-y-6">
    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-6 md:py-7 border-b border-slate-200/70 dark:border-slate-800">
        <div class="flex flex-col xl:flex-row xl:items-start xl:justify-between gap-5">
          <div class="space-y-3">
            <div>
              <h2 class="text-2xl font-bold text-slate-900 dark:text-slate-100">
                系统运行监控
              </h2>
              <p class="text-slate-500 dark:text-slate-400 mt-2 text-sm md:text-base max-w-3xl">
                聚合系统、JVM、内存、磁盘和线程等关键指标，帮助快速识别资源瓶颈和运行风险。
              </p>
            </div>

            <div class="flex flex-wrap gap-2">
              <div class="rounded-full border border-slate-200 bg-slate-50 px-3.5 py-1.5 text-[13px] text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
                主机: {{ monitorInfo?.systemInfo?.hostName || '未知' }}
              </div>
              <div class="rounded-full border border-slate-200 bg-slate-50 px-3.5 py-1.5 text-[13px] text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
                处理器: {{ monitorInfo?.jvmInfo?.availableProcessors || 0 }} 核
              </div>
              <div class="rounded-full border border-slate-200 bg-slate-50 px-3.5 py-1.5 text-[13px] text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
                Java: {{ monitorInfo?.systemInfo?.javaVersion || '未知' }}
              </div>
            </div>
          </div>

          <div class="flex flex-wrap gap-2.5 xl:justify-end">
            <a-button type="primary" :loading="loading" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="loadMonitorInfo">
              <template #icon>
                <span class="i-fa6-solid:rotate text-[12px] leading-none" />
              </template>
              刷新监控
            </a-button>
          </div>
        </div>
      </div>

      <div class="p-5 md:p-7">
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
          <div
            v-for="card in summaryCards"
            :key="card.title"
            class="rounded-2xl border border-slate-200/70 dark:border-slate-800 p-4 bg-slate-50/70 dark:bg-slate-950/30"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <div class="text-sm text-slate-500 dark:text-slate-400">
                  {{ card.title }}
                </div>
                <div class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-100">
                  {{ card.value }}
                </div>
              </div>
              <div
                class="w-10 h-10 rounded-2xl flex items-center justify-center"
                :class="{
                  'bg-slate-200/80 text-slate-700 dark:bg-slate-800 dark:text-slate-200': card.tone === 'slate',
                  'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300': card.tone === 'emerald',
                  'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300': card.tone === 'rose',
                  'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300': card.tone === 'amber',
                }"
              >
                <span :class="card.icon" class="text-lg" />
              </div>
            </div>
            <p class="mt-3 text-xs text-slate-500 dark:text-slate-400">
              {{ card.hint }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800">
        <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
          风险提示
        </h3>
        <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
          自动汇总当前监控数据中的高风险或需要关注的资源状态。
        </p>
      </div>
      <div class="p-5 md:p-7">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div
            v-for="item in riskItems"
            :key="item.title"
            class="rounded-2xl border p-4"
            :class="{
              'border-emerald-200 bg-emerald-50/70 dark:border-emerald-500/25 dark:bg-emerald-500/10': item.tone === 'emerald',
              'border-amber-200 bg-amber-50/70 dark:border-amber-500/25 dark:bg-amber-500/10': item.tone === 'amber',
              'border-rose-200 bg-rose-50/70 dark:border-rose-500/25 dark:bg-rose-500/10': item.tone === 'rose',
            }"
          >
            <div class="flex items-start gap-3">
              <div
                class="mt-0.5 h-9 w-9 rounded-2xl flex items-center justify-center"
                :class="{
                  'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300': item.tone === 'emerald',
                  'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300': item.tone === 'amber',
                  'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300': item.tone === 'rose',
                }"
              >
                <span class="i-fa6-solid:triangle-exclamation text-sm" />
              </div>
              <div>
                <div class="font-semibold text-slate-900 dark:text-slate-100">
                  {{ item.title }}
                </div>
                <p class="mt-1 text-sm text-slate-600 dark:text-slate-300">
                  {{ item.desc }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <section class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
        <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800">
          <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
            系统信息
          </h3>
        </div>
        <div class="p-5 md:p-7 space-y-3">
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">操作系统</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.systemInfo?.osName }} {{ monitorInfo?.systemInfo?.osVersion }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">系统架构</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.systemInfo?.osArch }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">主机名</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.systemInfo?.hostName }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">IP 地址</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.systemInfo?.hostAddress }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">Java 版本</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.systemInfo?.javaVersion }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">Java 厂商</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.systemInfo?.javaVendor }}</span>
          </div>
        </div>
      </section>

      <section class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
        <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800">
          <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
            JVM 信息
          </h3>
        </div>
        <div class="p-5 md:p-7 space-y-3">
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">JVM 名称</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.jvmInfo?.jvmName }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">JVM 版本</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.jvmInfo?.jvmVersion }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">处理器数量</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.jvmInfo?.availableProcessors }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">总内存</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ formatBytes(monitorInfo?.jvmInfo?.totalMemory) }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">可用内存</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ formatBytes(monitorInfo?.jvmInfo?.freeMemory) }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">最大内存</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ formatBytes(monitorInfo?.jvmInfo?.maxMemory) }}</span>
          </div>
        </div>
      </section>

      <section class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
        <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800">
          <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
            内存使用情况
          </h3>
        </div>
        <div class="p-5 md:p-7 space-y-5">
          <div>
            <div class="flex items-center justify-between gap-4 mb-2">
              <span class="text-slate-500 dark:text-slate-400">物理内存</span>
              <span class="font-medium text-slate-900 dark:text-slate-100 text-right">
                {{ formatBytes(monitorInfo?.memoryInfo?.usedMemory) }} / {{ formatBytes(monitorInfo?.memoryInfo?.totalMemory) }}
              </span>
            </div>
            <div class="h-2.5 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
              <div
                class="h-full rounded-full transition-all duration-300"
                :class="{
                  'bg-emerald-500': getRateTone(memoryUsageRate) === 'emerald',
                  'bg-amber-500': getRateTone(memoryUsageRate) === 'amber',
                  'bg-rose-500': getRateTone(memoryUsageRate) === 'rose',
                }"
                :style="{ width: `${memoryUsageRate}%` }"
              />
            </div>
            <div class="mt-2 text-right text-sm text-slate-500 dark:text-slate-400">
              {{ memoryUsageRate.toFixed(1) }}%
            </div>
          </div>

          <div v-if="monitorInfo?.memoryInfo?.totalSwap && monitorInfo.memoryInfo.totalSwap > 0">
            <div class="flex items-center justify-between gap-4 mb-2">
              <span class="text-slate-500 dark:text-slate-400">交换空间</span>
              <span class="font-medium text-slate-900 dark:text-slate-100 text-right">
                {{ formatBytes(monitorInfo.memoryInfo.usedSwap) }} / {{ formatBytes(monitorInfo.memoryInfo.totalSwap) }}
              </span>
            </div>
            <div class="h-2.5 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
              <div
                class="h-full rounded-full transition-all duration-300"
                :class="{
                  'bg-emerald-500': getRateTone(swapUsageRate) === 'emerald',
                  'bg-amber-500': getRateTone(swapUsageRate) === 'amber',
                  'bg-rose-500': getRateTone(swapUsageRate) === 'rose',
                }"
                :style="{ width: `${swapUsageRate}%` }"
              />
            </div>
            <div class="mt-2 text-right text-sm text-slate-500 dark:text-slate-400">
              {{ swapUsageRate.toFixed(1) }}%
            </div>
          </div>
        </div>
      </section>

      <section class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
        <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800">
          <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
            运行时信息
          </h3>
        </div>
        <div class="p-5 md:p-7 space-y-3">
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">运行时长</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ formatUptime(monitorInfo?.runtimeInfo?.uptime) }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">启动时间</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ formatDate(monitorInfo?.runtimeInfo?.startTime) }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">线程数量</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.runtimeInfo?.threadCount }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">峰值线程</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.runtimeInfo?.peakThreadCount }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">累计启动线程</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ monitorInfo?.runtimeInfo?.totalStartedThreadCount }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">堆内存已用</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ formatBytes(monitorInfo?.runtimeInfo?.heapMemoryUsed) }}</span>
          </div>
          <div class="flex items-center justify-between gap-4">
            <span class="text-slate-500 dark:text-slate-400">非堆内存已用</span>
            <span class="font-medium text-slate-900 dark:text-slate-100 text-right">{{ formatBytes(monitorInfo?.runtimeInfo?.nonHeapMemoryUsed) }}</span>
          </div>
        </div>
      </section>
    </div>

    <section class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800">
        <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
          磁盘使用情况
        </h3>
      </div>
      <div class="p-5 md:p-7">
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          <div
            v-for="disk in diskInfos"
            :key="disk.fileSystem"
            class="rounded-2xl border border-slate-200/70 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/30 p-4"
          >
            <div class="flex items-start justify-between gap-4 mb-3">
              <div>
                <div class="font-semibold text-slate-900 dark:text-slate-100 break-all">
                  {{ disk.fileSystem }}
                </div>
                <div class="mt-1 text-xs text-slate-500 dark:text-slate-400 break-all">
                  挂载点: {{ disk.mountPoint }}
                </div>
              </div>
              <span
                class="rounded-full px-2.5 py-1 text-xs font-medium"
                :class="disk.usageRate > 90
                  ? 'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300'
                  : disk.usageRate > 80
                    ? 'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300'
                    : 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300'"
              >
                {{ disk.usageRate.toFixed(1) }}%
              </span>
            </div>

            <div class="space-y-2 text-sm">
              <div class="flex items-center justify-between gap-4">
                <span class="text-slate-500 dark:text-slate-400">总空间</span>
                <span class="text-slate-900 dark:text-slate-100">{{ formatBytes(disk.totalSpace) }}</span>
              </div>
              <div class="flex items-center justify-between gap-4">
                <span class="text-slate-500 dark:text-slate-400">已使用</span>
                <span class="text-slate-900 dark:text-slate-100">{{ formatBytes(disk.usedSpace) }}</span>
              </div>
              <div class="flex items-center justify-between gap-4">
                <span class="text-slate-500 dark:text-slate-400">可用空间</span>
                <span class="text-slate-900 dark:text-slate-100">{{ formatBytes(disk.freeSpace) }}</span>
              </div>
            </div>

            <div class="mt-3 h-2.5 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
              <div
                class="h-full rounded-full transition-all duration-300"
                :class="disk.usageRate > 90 ? 'bg-rose-500' : disk.usageRate > 80 ? 'bg-amber-500' : 'bg-emerald-500'"
                :style="{ width: `${clampRate(disk.usageRate)}%` }"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800">
        <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
          系统属性
        </h3>
      </div>
      <div class="p-5 md:p-7">
        <div class="max-h-[360px] overflow-auto rounded-2xl border border-slate-200/70 dark:border-slate-800">
          <table class="w-full text-sm">
            <tbody>
              <tr
                v-for="(value, key) in monitorInfo?.jvmInfo?.systemProperties"
                :key="key"
                class="border-b border-slate-200/70 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-950/40 transition-colors"
              >
                <td class="py-2.5 px-3 font-medium text-slate-700 dark:text-slate-300 align-top w-[260px] break-all">
                  {{ key }}
                </td>
                <td class="py-2.5 px-3 text-slate-500 dark:text-slate-400 break-all">
                  {{ value }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </div>
</template>
