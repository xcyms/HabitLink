<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { getLogPageApi } from '../../api/log.ts'
import type * as API from '../../types.ts'

interface LogRecord {
  id: number
  userId?: number
  username?: string
  operation?: string
  method?: string
  url?: string
  params?: string
  result?: string
  duration?: number
  ip?: string
  status?: number
  errorMsg?: string
  createTime?: string
}

const loading = ref(false)
const detailVisible = ref(false)
const currentLog = ref<LogRecord | null>(null)
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const logList = ref<API.PageResult<LogRecord>>({
  records: [],
  total: 0,
  size: 0,
  current: 0,
})

/**
 * 搜索表单。
 */
const searchForm = ref({
  username: '',
  operation: '',
  status: undefined as number | undefined,
})

/**
 * 查询操作日志分页数据。
 */
async function loadLogs(page = current.value, size = pageSize.value) {
  loading.value = true
  try {
    const res = await getLogPageApi(
      { current: Number(page), size: Number(size) },
      {
        username: searchForm.value.username,
        operation: searchForm.value.operation,
        status: searchForm.value.status,
      },
    )
    logList.value = res
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载日志列表失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 触发搜索并回到第一页。
 */
function handleSearch() {
  current.value = 1
  loadLogs()
}

/**
 * 重置搜索条件。
 */
function handleReset() {
  searchForm.value = {
    username: '',
    operation: '',
    status: undefined,
  }
  current.value = 1
  loadLogs()
}

/**
 * 快速切换日志状态筛选。
 */
function handleQuickStatus(status?: number) {
  searchForm.value.status = status
  current.value = 1
  loadLogs()
}

/**
 * 表格分页切换。
 */
function handleTableChange(pagination: TablePaginationConfig) {
  loadLogs(pagination.current, pagination.pageSize)
}

/**
 * 打开日志详情抽屉。
 */
function showDetail(record: LogRecord) {
  currentLog.value = record
  detailVisible.value = true
}

/**
 * 统一展示操作者名称。
 */
function getDisplayUsername(record: LogRecord) {
  return record.username || '匿名用户'
}

/**
 * 判断是否为失败日志。
 */
function isFailure(record: LogRecord) {
  return record.status === 0
}

/**
 * 判断慢请求等级。
 */
function getDurationTone(duration?: number) {
  if ((duration || 0) >= 1000)
    return 'red'
  if ((duration || 0) >= 500)
    return 'orange'
  return 'green'
}

/**
 * 截断较长文本，避免表格拥挤。
 */
function ellipsisText(text?: string, max = 48) {
  if (!text)
    return '-'
  return text.length > max ? `${text.slice(0, max)}...` : text
}

const records = computed(() => logList.value.records || [])

const successCount = computed(() => records.value.filter(item => item.status === 1).length)
const failureCount = computed(() => records.value.filter(item => item.status === 0).length)
const slowCount = computed(() => records.value.filter(item => (item.duration || 0) >= 500).length)
const avgDuration = computed(() => {
  if (!records.value.length)
    return 0
  const totalDuration = records.value.reduce((sum, item) => sum + (Number(item.duration) || 0), 0)
  return Math.round(totalDuration / records.value.length)
})

const summaryCards = computed(() => [
  {
    title: '当前结果数',
    value: `${total.value}`,
    hint: '匹配当前筛选条件的日志总量',
    icon: 'i-fa6-solid:table-list',
    tone: 'slate',
  },
  {
    title: '成功请求',
    value: `${successCount.value}`,
    hint: '当前页内执行成功的操作记录',
    icon: 'i-fa6-solid:circle-check',
    tone: 'emerald',
  },
  {
    title: '失败请求',
    value: `${failureCount.value}`,
    hint: '建议优先查看失败日志和错误信息',
    icon: 'i-fa6-solid:circle-exclamation',
    tone: 'rose',
  },
  {
    title: '平均耗时',
    value: `${avgDuration.value} ms`,
    hint: slowCount.value ? `其中 ${slowCount.value} 条超过 500 ms` : '当前页暂无明显慢请求',
    icon: 'i-fa6-solid:gauge-high',
    tone: 'amber',
  },
])

onMounted(() => {
  loadLogs()
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
                操作日志
              </h2>
              <p class="text-slate-500 dark:text-slate-400 mt-2 text-sm md:text-base max-w-3xl">
                聚焦系统操作记录、执行状态和请求耗时，帮助你更快筛出失败请求、慢请求和异常行为。
              </p>
            </div>

            <div class="flex flex-wrap gap-2">
              <button
                class="rounded-full border px-3.5 py-1.5 text-[13px] transition-colors"
                :class="searchForm.status === undefined
                  ? 'border-slate-900 bg-slate-900 text-white dark:border-slate-100 dark:bg-slate-100 dark:text-slate-900'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleQuickStatus(undefined)"
              >
                全部状态
              </button>
              <button
                class="rounded-full border px-3.5 py-1.5 text-[13px] transition-colors"
                :class="searchForm.status === 1
                  ? 'border-emerald-600 bg-emerald-600 text-white dark:border-emerald-500 dark:bg-emerald-500'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleQuickStatus(1)"
              >
                仅看成功
              </button>
              <button
                class="rounded-full border px-3.5 py-1.5 text-[13px] transition-colors"
                :class="searchForm.status === 0
                  ? 'border-rose-600 bg-rose-600 text-white dark:border-rose-500 dark:bg-rose-500'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleQuickStatus(0)"
              >
                仅看失败
              </button>
            </div>
          </div>

          <div class="flex flex-wrap gap-2.5 xl:justify-end">
            <a-button class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleReset">
              重置条件
            </a-button>
            <a-button type="primary" :loading="loading" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleSearch">
              <template #icon>
                <span class="i-fa6-solid:magnifying-glass text-[12px] leading-none" />
              </template>
              搜索日志
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
      <div class="px-5 md:px-7 py-5 border-b border-slate-200/70 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/30">
        <a-form layout="vertical" :model="searchForm">
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
            <a-form-item label="操作人" class="mb-0">
              <a-input
                v-model:value="searchForm.username"
                placeholder="请输入操作人"
                allow-clear
                @press-enter="handleSearch"
              />
            </a-form-item>
            <a-form-item label="操作描述" class="mb-0">
              <a-input
                v-model:value="searchForm.operation"
                placeholder="请输入操作描述"
                allow-clear
                @press-enter="handleSearch"
              />
            </a-form-item>
            <a-form-item label="状态" class="mb-0">
              <DictSelect
                v-model:value="searchForm.status"
                dict-type="sys_operation_status"
                placeholder="请选择状态"
                @change="handleSearch"
              />
            </a-form-item>
            <div class="flex items-end">
              <div class="w-full rounded-2xl border border-dashed border-slate-200 dark:border-slate-700 px-4 py-3 text-sm text-slate-500 dark:text-slate-400">
                当前第 {{ current }} 页，每页 {{ pageSize }} 条
              </div>
            </div>
          </div>
        </a-form>
      </div>

      <div class="px-5 md:px-7 py-4 border-b border-slate-200/70 dark:border-slate-800 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div>
          <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
            日志列表
          </h3>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
            优先关注失败状态和高耗时记录，点开详情可查看请求参数、响应结果和错误信息。
          </p>
        </div>
        <div class="text-sm text-slate-500 dark:text-slate-400">
          当前页共 {{ records.length }} 条
        </div>
      </div>

      <div class="overflow-hidden">
        <a-table
          :data-source="records"
          :loading="loading"
          :scroll="{ x: 1280 }"
          :pagination="{
            current,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (allTotal) => `共 ${allTotal} 条记录`,
          }"
          row-key="id"
          @change="handleTableChange"
        >
          <a-table-column key="id" title="ID" data-index="id" width="88" />
          <a-table-column key="username" title="操作人" width="140">
            <template #default="{ record }">
              <div class="font-medium text-slate-800 dark:text-slate-100">
                {{ getDisplayUsername(record) }}
              </div>
            </template>
          </a-table-column>
          <a-table-column key="operation" title="操作描述" width="180">
            <template #default="{ record }">
              <div class="text-slate-700 dark:text-slate-200">
                {{ record.operation || '-' }}
              </div>
            </template>
          </a-table-column>
          <a-table-column key="method" title="方法" data-index="method" width="112">
            <template #default="{ text }">
              <div class="w-full overflow-hidden">
                <span class="inline-flex max-w-full items-center justify-center rounded-full bg-blue-50 px-2.5 py-1 text-[12px] font-medium leading-none text-blue-700 dark:bg-blue-500/15 dark:text-blue-300">
                  <span class="truncate">
                    {{ text || 'GET' }}
                  </span>
                </span>
              </div>
            </template>
          </a-table-column>
          <a-table-column key="url" title="请求路径" width="260">
            <template #default="{ record }">
              <div class="font-mono text-xs text-slate-600 dark:text-slate-300 break-all">
                {{ ellipsisText(record.url, 56) }}
              </div>
            </template>
          </a-table-column>
          <a-table-column key="ip" title="IP 地址" data-index="ip" width="150" />
          <a-table-column key="duration" title="耗时" width="110">
            <template #default="{ record }">
              <a-tag :color="getDurationTone(record.duration)">
                {{ record.duration || 0 }} ms
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column key="status" title="状态" width="110">
            <template #default="{ record }">
              <DictTag dict-type="sys_operation_status" :value="record.status" />
            </template>
          </a-table-column>
          <a-table-column key="createTime" title="操作时间" data-index="createTime" width="190" />
          <a-table-column key="action" title="操作" align="center" width="96" fixed="right">
            <template #default="{ record }">
              <a-button type="link" size="small" class="px-1.5" @click="showDetail(record)">
                详情
              </a-button>
            </template>
          </a-table-column>
        </a-table>
      </div>
    </div>

    <a-drawer
      v-model:open="detailVisible"
      title="日志详情"
      placement="right"
      :width="640"
    >
      <div v-if="currentLog" class="space-y-6">
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div class="rounded-[18px] border border-slate-200/80 bg-slate-50/75 p-3.5 dark:border-slate-700/80 dark:bg-slate-900/60">
            <div class="text-[12px] text-slate-500 dark:text-slate-400">
              操作人
            </div>
            <div class="mt-2 text-[15px] font-semibold text-slate-900 dark:text-slate-100 break-all">
              {{ getDisplayUsername(currentLog) }}
            </div>
          </div>
          <div class="rounded-[18px] border border-slate-200/80 bg-slate-50/75 p-3.5 dark:border-slate-700/80 dark:bg-slate-900/60">
            <div class="text-[12px] text-slate-500 dark:text-slate-400">
              执行状态
            </div>
            <div class="mt-2 min-w-0">
              <DictTag dict-type="sys_operation_status" :value="currentLog.status" />
            </div>
          </div>
          <div class="rounded-[18px] border border-slate-200/80 bg-slate-50/75 p-3.5 dark:border-slate-700/80 dark:bg-slate-900/60">
            <div class="text-[12px] text-slate-500 dark:text-slate-400">
              请求方法
            </div>
            <div class="mt-2 overflow-hidden">
              <span class="inline-flex max-w-full items-center justify-center rounded-full bg-blue-50 px-2.5 py-1 text-[12px] font-medium leading-none text-blue-700 dark:bg-blue-500/15 dark:text-blue-300">
                <span class="truncate">
                  {{ currentLog.method || '-' }}
                </span>
              </span>
            </div>
          </div>
          <div class="rounded-[18px] border border-slate-200/80 bg-slate-50/75 p-3.5 dark:border-slate-700/80 dark:bg-slate-900/60">
            <div class="text-[12px] text-slate-500 dark:text-slate-400">
              执行耗时
            </div>
            <div class="mt-2 text-[15px] font-semibold text-slate-900 dark:text-slate-100">
              {{ currentLog.duration || 0 }} ms
            </div>
          </div>
        </div>

        <a-descriptions bordered :column="1" size="small">
          <a-descriptions-item label="操作描述">
            {{ currentLog.operation || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="请求路径">
            <span class="font-mono text-xs break-all">{{ currentLog.url || '-' }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="IP 地址">
            {{ currentLog.ip || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="操作时间">
            {{ currentLog.createTime || '-' }}
          </a-descriptions-item>
        </a-descriptions>

        <div class="space-y-2">
          <h4 class="text-sm font-bold text-slate-700 dark:text-slate-200">
            请求参数
          </h4>
          <div class="max-h-[220px] overflow-auto rounded-2xl border border-slate-200/80 bg-slate-50 px-3.5 py-3.5 font-mono text-xs leading-6 text-slate-700 whitespace-pre-wrap break-all dark:border-slate-700/80 dark:bg-slate-900/70 dark:text-slate-200">
            {{ currentLog.params || '暂无请求参数' }}
          </div>
        </div>

        <div v-if="!isFailure(currentLog)" class="space-y-2">
          <h4 class="text-sm font-bold text-slate-700 dark:text-slate-200">
            响应结果
          </h4>
          <div class="max-h-[220px] overflow-auto rounded-2xl border border-slate-200/80 bg-slate-50 px-3.5 py-3.5 font-mono text-xs leading-6 text-slate-700 whitespace-pre-wrap break-all dark:border-slate-700/80 dark:bg-slate-900/70 dark:text-slate-200">
            {{ currentLog.result || '暂无响应结果' }}
          </div>
        </div>

        <div v-else class="space-y-2">
          <h4 class="text-sm font-bold text-rose-600 dark:text-rose-400">
            错误信息
          </h4>
          <div class="max-h-[220px] overflow-auto rounded-2xl border border-rose-200 bg-rose-50 px-3.5 py-3.5 font-mono text-xs leading-6 text-rose-700 whitespace-pre-wrap break-all dark:border-rose-800/70 dark:bg-rose-950/30 dark:text-rose-200">
            {{ currentLog.errorMsg || '暂无错误信息' }}
          </div>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<style scoped>
:deep(.ant-input),
:deep(.ant-input-affix-wrapper),
:deep(.ant-select-selector),
:deep(.ant-input-number),
:deep(.ant-picker) {
  border-radius: 12px;
}

:deep(.ant-form-item-label > label) {
  color: #475569;
  font-size: 13px;
}

:deep(.ant-table-thead > tr > th) {
  font-size: 13px;
  font-weight: 600;
}

:global(html:not(.dark)) :deep(.ant-table-thead > tr > th) {
  background-color: #f8fafc;
}

:global(html.dark) :deep(.ant-table-thead > tr > th) {
  background-color: #111827;
}

:deep(.ant-btn .ant-btn-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
