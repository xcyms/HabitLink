<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { Modal, message } from 'ant-design-vue'
import { getMyNoticePageApi, publishNoticeApi, readAllNoticesApi, readNoticeApi } from '../../api/notice.ts'
import type * as API from '../../types'
import { useUserStore } from '../../store/user.ts'

type ReadFilter = 'ALL' | 'UNREAD' | 'READ'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo.roles?.includes('ADMIN'))

const loading = ref(false)
const publishVisible = ref(false)
const publishLoading = ref(false)
const detailVisible = ref(false)

const noticeList = ref<API.Notice[]>([])
const currentNotice = ref<API.Notice | null>(null)

const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const readFilter = ref<ReadFilter>('ALL')

/**
 * 搜索条件。
 */
const searchForm = ref({
  title: '',
  type: undefined as string | undefined,
})

/**
 * 发布通知表单。
 */
const publishForm = ref({
  title: '',
  content: '',
  type: 'info',
  targetType: 'ALL',
  targetUserId: undefined as number | undefined,
})

/**
 * 加载通知分页数据。
 */
async function loadNotices(page = current.value, size = pageSize.value) {
  loading.value = true
  try {
    const res = await getMyNoticePageApi(
      { current: Number(page), size: Number(size) },
      {
        title: searchForm.value.title || undefined,
        type: searchForm.value.type,
        isRead: readFilter.value === 'ALL' ? undefined : readFilter.value === 'READ',
      },
    )
    noticeList.value = res.records
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载通知列表失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 执行搜索并回到第一页。
 */
function handleSearch() {
  current.value = 1
  loadNotices()
}

/**
 * 重置筛选条件。
 */
function handleReset() {
  searchForm.value = {
    title: '',
    type: undefined,
  }
  readFilter.value = 'ALL'
  current.value = 1
  loadNotices()
}

/**
 * 切换阅读状态筛选。
 */
function handleFilterChange(value: ReadFilter) {
  readFilter.value = value
  current.value = 1
  loadNotices()
}

/**
 * 表格分页切换。
 */
function handleTableChange(pagination: TablePaginationConfig) {
  loadNotices(pagination.current, pagination.pageSize)
}

/**
 * 获取通知摘要。
 */
function getNoticePreview(content?: string, max = 56) {
  if (!content)
    return '暂无通知内容'
  return content.length > max ? `${content.slice(0, max)}...` : content
}

/**
 * 打开通知详情，并在首次查看时标记已读。
 */
async function handleRead(record: API.Notice) {
  if (!record.id) {
    currentNotice.value = record
    detailVisible.value = true
    return
  }

  if (!record.isRead) {
    await readNoticeApi(record.id)
    record.isRead = true
  }
  currentNotice.value = record
  detailVisible.value = true
}

/**
 * 全部标记已读。
 */
async function handleReadAll() {
  await readAllNoticesApi()
  noticeList.value = noticeList.value.map(item => ({ ...item, isRead: true }))
  message.success('已全部标记为已读')
  if (readFilter.value === 'UNREAD')
    loadNotices(1)
}

/**
 * 发布通知。
 */
async function handlePublish() {
  if (!publishForm.value.title || !publishForm.value.content) {
    message.warning('请填写通知标题和通知内容')
    return
  }

  publishLoading.value = true
  try {
    await publishNoticeApi(publishForm.value)
    message.success('通知发布成功')
    publishVisible.value = false
    publishForm.value = {
      title: '',
      content: '',
      type: 'info',
      targetType: 'ALL',
      targetUserId: undefined,
    }
    loadNotices(1)
  }
  catch (error) {
    console.error('发布通知失败:', error)
  }
  finally {
    publishLoading.value = false
  }
}

/**
 * 使用模态框快速预览内容。
 */
function handleQuickPreview(record: API.Notice) {
  Modal.info({
    title: record.title,
    content: record.content || '暂无通知内容',
    maskClosable: true,
  })
}

const unreadCount = computed(() => noticeList.value.filter(item => !item.isRead).length)
const readCount = computed(() => noticeList.value.filter(item => item.isRead).length)
const announcementCount = computed(() => noticeList.value.filter(item => item.type === 'announcement').length)
const directCount = computed(() => noticeList.value.filter(item => item.targetType === 'USER').length)

const summaryCards = computed(() => [
  {
    title: '当前结果',
    value: `${total.value}`,
    hint: '匹配当前筛选条件的通知数量',
    icon: 'i-fa6-solid:inbox',
    tone: 'slate',
  },
  {
    title: '未读通知',
    value: `${unreadCount.value}`,
    hint: '当前页中尚未处理的通知条数',
    icon: 'i-fa6-solid:envelope-open-text',
    tone: 'rose',
  },
  {
    title: '已读通知',
    value: `${readCount.value}`,
    hint: '当前页中已查看的通知条数',
    icon: 'i-fa6-solid:circle-check',
    tone: 'emerald',
  },
  {
    title: '定向通知',
    value: `${directCount.value}`,
    hint: announcementCount.value ? `其中公告类 ${announcementCount.value} 条` : '当前页暂无公告类通知',
    icon: 'i-fa6-solid:bullseye',
    tone: 'amber',
  },
])

onMounted(() => {
  loadNotices()
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-3 md:px-0 py-6 space-y-6">
    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-6 md:py-7 border-b border-slate-200/70 dark:border-slate-800">
        <div class="flex flex-col xl:flex-row xl:items-start xl:justify-between gap-5">
          <div class="space-y-3">
            <div>
              <h2 class="text-2xl font-bold text-slate-900 dark:text-slate-100">
                通知公告
              </h2>
              <p class="mt-2 max-w-3xl text-sm md:text-base text-slate-500 dark:text-slate-400">
                统一查看站内通知、公告与定向消息，支持按阅读状态快速筛选，并集中处理未读项。
              </p>
            </div>

            <div class="flex flex-wrap gap-2">
              <button
                class="rounded-full border px-3.5 py-1.5 text-[13px] transition-colors"
                :class="readFilter === 'ALL'
                  ? 'border-slate-900 bg-slate-900 text-white dark:border-slate-100 dark:bg-slate-100 dark:text-slate-900'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleFilterChange('ALL')"
              >
                全部通知
              </button>
              <button
                class="rounded-full border px-3.5 py-1.5 text-[13px] transition-colors"
                :class="readFilter === 'UNREAD'
                  ? 'border-rose-600 bg-rose-600 text-white dark:border-rose-500 dark:bg-rose-500'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleFilterChange('UNREAD')"
              >
                仅看未读
              </button>
              <button
                class="rounded-full border px-3.5 py-1.5 text-[13px] transition-colors"
                :class="readFilter === 'READ'
                  ? 'border-emerald-600 bg-emerald-600 text-white dark:border-emerald-500 dark:bg-emerald-500'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleFilterChange('READ')"
              >
                仅看已读
              </button>
            </div>
          </div>

          <div class="flex flex-wrap gap-2.5 xl:justify-end">
            <a-button class="inline-flex h-9 items-center justify-center rounded-xl px-3.5 text-[13px]" @click="handleReadAll">
              全部已读
            </a-button>
            <a-button class="inline-flex h-9 items-center justify-center rounded-xl px-3.5 text-[13px]" @click="handleReset">
              重置条件
            </a-button>
            <a-button type="primary" :loading="loading" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleSearch">
              <template #icon>
                <span class="i-fa6-solid:magnifying-glass text-[12px] leading-none" />
              </template>
              搜索通知
            </a-button>
            <a-button v-if="isAdmin" type="primary" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="publishVisible = true">
              <template #icon>
                <span class="i-fa6-solid:paper-plane text-[12px] leading-none" />
              </template>
              发布通知
            </a-button>
          </div>
        </div>
      </div>

      <div class="p-5 md:p-7">
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
          <div
            v-for="item in summaryCards"
            :key="item.title"
            class="rounded-2xl border px-4 py-4 transition-colors"
            :class="{
              'border-slate-200 bg-slate-50/80 dark:border-slate-800 dark:bg-slate-800/40': item.tone === 'slate',
              'border-rose-200 bg-rose-50/80 dark:border-rose-900/70 dark:bg-rose-950/30': item.tone === 'rose',
              'border-emerald-200 bg-emerald-50/80 dark:border-emerald-900/70 dark:bg-emerald-950/30': item.tone === 'emerald',
              'border-amber-200 bg-amber-50/80 dark:border-amber-900/70 dark:bg-amber-950/30': item.tone === 'amber',
            }"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <div class="text-[13px] text-slate-500 dark:text-slate-400">
                  {{ item.title }}
                </div>
                <div class="mt-3 text-2xl font-semibold text-slate-900 dark:text-slate-100">
                  {{ item.value }}
                </div>
              </div>
              <div
                class="flex h-10 w-10 items-center justify-center rounded-2xl text-lg"
                :class="{
                  'bg-slate-900 text-white dark:bg-slate-100 dark:text-slate-900': item.tone === 'slate',
                  'bg-rose-600 text-white dark:bg-rose-500': item.tone === 'rose',
                  'bg-emerald-600 text-white dark:bg-emerald-500': item.tone === 'emerald',
                  'bg-amber-500 text-white dark:bg-amber-400 dark:text-slate-900': item.tone === 'amber',
                }"
              >
                <span :class="item.icon" />
              </div>
            </div>
            <p class="mt-3 text-xs leading-5 text-slate-500 dark:text-slate-400">
              {{ item.hint }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 p-5 md:p-6">
      <a-form :model="searchForm" layout="vertical">
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_240px_auto] xl:items-end">
          <a-form-item label="通知标题" class="mb-0">
            <a-input
              v-model:value="searchForm.title"
              allow-clear
              placeholder="请输入通知标题关键词"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="通知类型" class="mb-0">
            <DictSelect
              v-model:value="searchForm.type"
              allow-clear
              dict-type="sys_notice_type"
              placeholder="全部类型"
            />
          </a-form-item>

          <div class="flex flex-wrap gap-2 xl:justify-end">
            <a-button class="inline-flex h-9 items-center justify-center rounded-xl px-3.5 text-[13px]" @click="handleReset">
              清空
            </a-button>
            <a-button type="primary" class="inline-flex h-9 items-center justify-center rounded-xl px-3.5 text-[13px]" @click="handleSearch">
              立即查询
            </a-button>
          </div>
        </div>
      </a-form>
    </div>

    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <a-table
        :data-source="noticeList"
        :loading="loading"
        :pagination="{
          current,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (value) => `共 ${value} 条记录`,
        }"
        row-key="id"
        :scroll="{ x: 980 }"
        @change="handleTableChange"
      >
        <a-table-column key="title" title="通知内容" :custom-cell="() => ({ class: 'align-top' })">
          <template #default="{ record }">
            <div class="min-w-0 py-0.5">
              <div class="flex items-center gap-2">
                <span v-if="!record.isRead" class="h-2 w-2 rounded-full bg-rose-500 shrink-0" />
                <span class="truncate text-[14px]" :class="record.isRead ? 'text-slate-700 dark:text-slate-200' : 'font-medium text-slate-900 dark:text-slate-100'">
                  {{ record.title }}
                </span>
              </div>
              <p class="mt-1 text-xs leading-5 text-slate-500 dark:text-slate-400">
                {{ getNoticePreview(record.content) }}
              </p>
            </div>
          </template>
        </a-table-column>

        <a-table-column key="type" title="类型" data-index="type" width="140" align="center">
          <template #default="{ text }">
            <DictTag dict-type="sys_notice_type" :value="text" />
          </template>
        </a-table-column>

        <a-table-column key="targetType" title="发送范围" data-index="targetType" width="140" align="center">
          <template #default="{ record }">
            <a-tag :color="record.targetType === 'USER' ? 'blue' : 'default'">
              {{ record.targetType === 'USER' ? '指定用户' : '全体用户' }}
            </a-tag>
          </template>
        </a-table-column>

        <a-table-column key="isRead" title="阅读状态" width="110" align="center">
          <template #default="{ record }">
            <a-tag :color="record.isRead ? 'default' : 'red'">
              {{ record.isRead ? '已读' : '未读' }}
            </a-tag>
          </template>
        </a-table-column>

        <a-table-column key="createTime" title="发布时间" data-index="createTime" width="180" />

        <a-table-column key="action" title="操作" align="center" width="160" fixed="right">
          <template #default="{ record }">
            <div class="flex items-center justify-center gap-1">
              <a-button type="link" class="!px-1.5 text-[13px]" @click="handleRead(record)">
                查看详情
              </a-button>
              <a-button type="link" class="!px-1.5 text-[13px]" @click="handleQuickPreview(record)">
                预览
              </a-button>
            </div>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <a-drawer
      v-model:open="detailVisible"
      :title="currentNotice?.title || '通知详情'"
      placement="right"
      width="520"
    >
      <div v-if="currentNotice" class="space-y-5">
        <div class="rounded-2xl border border-slate-200/80 bg-slate-50/80 px-4 py-4 dark:border-slate-800 dark:bg-slate-900/60">
          <div class="flex flex-wrap items-center gap-2">
            <DictTag dict-type="sys_notice_type" :value="currentNotice.type" />
            <a-tag :color="currentNotice.isRead ? 'default' : 'red'">
              {{ currentNotice.isRead ? '已读' : '未读' }}
            </a-tag>
            <a-tag :color="currentNotice.targetType === 'USER' ? 'blue' : 'default'">
              {{ currentNotice.targetType === 'USER' ? '指定用户' : '全体用户' }}
            </a-tag>
          </div>
          <div class="mt-3 text-xs text-slate-500 dark:text-slate-400">
            发布时间：{{ currentNotice.createTime || '-' }}
          </div>
        </div>

        <div class="rounded-2xl border border-slate-200/80 bg-white px-4 py-4 dark:border-slate-800 dark:bg-slate-900/80">
          <div class="text-sm font-medium text-slate-900 dark:text-slate-100">
            通知正文
          </div>
          <div class="mt-3 whitespace-pre-wrap break-words text-sm leading-7 text-slate-600 dark:text-slate-300">
            {{ currentNotice.content || '暂无通知内容' }}
          </div>
        </div>
      </div>
    </a-drawer>

    <a-modal
      v-model:open="publishVisible"
      title="发布通知"
      :confirm-loading="publishLoading"
      @ok="handlePublish"
    >
      <a-form layout="vertical" class="mt-4">
        <a-form-item label="通知标题" required>
          <a-input v-model:value="publishForm.title" placeholder="请输入通知标题" />
        </a-form-item>

        <a-form-item label="通知类型">
          <DictSelect
            v-model:value="publishForm.type"
            dict-type="sys_notice_type"
          />
        </a-form-item>

        <a-form-item label="发送范围">
          <a-radio-group v-model:value="publishForm.targetType">
            <a-radio value="ALL">
              全体用户
            </a-radio>
            <a-radio value="USER">
              指定用户
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item v-if="publishForm.targetType === 'USER'" label="用户 ID" required>
          <a-input-number v-model:value="publishForm.targetUserId" class="w-full" placeholder="请输入用户 ID" />
        </a-form-item>

        <a-form-item label="通知内容" required>
          <a-textarea v-model:value="publishForm.content" :rows="5" placeholder="请输入通知内容" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
:deep(.ant-table-thead > tr > th) {
  font-size: 13px;
}

:global(html:not(.dark)) :deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
}

:global(html.dark) :deep(.ant-table-thead > tr > th) {
  background-color: #1d1d1d;
}
</style>
