<script setup lang="ts">
import { BellOutlined, CheckOutlined, RightOutlined } from '@ant-design/icons-vue'
import { message, notification } from 'ant-design-vue'
import { getMyNoticePageApi, getUnreadCountApi, readAllNoticesApi, readNoticeApi } from '../../api/notice.ts'
import type * as API from '../../types'
import { useUserStore } from '../../store/user.ts'

const userStore = useUserStore()

const unreadCount = ref(0)
const loading = ref(false)
const visible = ref(false)
const noticeList = ref<API.Notice[]>([])

let socket: WebSocket | null = null

/**
 * 建立通知 WebSocket 连接。
 */
function connectWebSocket() {
  if (!userStore.userInfo.id)
    return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = import.meta.env.VITE_API_BASE_URL.replace('http://', '').replace('https://', '')
  const wsUrl = `${protocol}//${host}/ws/notice/${userStore.userInfo.id}`

  socket = new WebSocket(wsUrl)

  socket.onmessage = (event) => {
    if (event.data.startsWith('NEW_NOTICE:')) {
      const title = event.data.replace('NEW_NOTICE:', '')
      notification.info({
        message: '收到新通知',
        description: title,
        placement: 'bottomRight',
      })
      refreshUnreadCount()
      if (visible.value)
        loadNotices()
    }
  }

  socket.onclose = () => {
    setTimeout(() => {
      if (userStore.isLoggedIn)
        connectWebSocket()
    }, 5000)
  }
}

/**
 * 刷新未读数量。
 */
async function refreshUnreadCount() {
  try {
    unreadCount.value = await getUnreadCountApi()
  }
  catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

/**
 * 加载通知中心列表。
 */
async function loadNotices() {
  loading.value = true
  try {
    const res = await getMyNoticePageApi({ current: 1, size: 6 }, {})
    noticeList.value = res.records
  }
  catch (error) {
    console.error('加载通知失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 返回通知内容摘要。
 */
function getNoticePreview(content?: string, max = 42) {
  if (!content)
    return '暂无通知内容'
  return content.length > max ? `${content.slice(0, max)}...` : content
}

/**
 * 读取单条通知。
 */
async function handleRead(item: API.Notice) {
  if (!item.id) {
    message.info(`已查看通知：${item.title}`)
    return
  }

  if (!item.isRead) {
    await readNoticeApi(item.id)
    item.isRead = true
    refreshUnreadCount()
  }

  message.info(`已查看通知：${item.title}`)
}

/**
 * 全部标记已读。
 */
async function handleReadAll() {
  await readAllNoticesApi()
  noticeList.value = noticeList.value.map(item => ({ ...item, isRead: true }))
  refreshUnreadCount()
  message.success('已全部标记为已读')
}

const unreadList = computed(() => noticeList.value.filter(item => !item.isRead))
const latestNotice = computed(() => noticeList.value[0] || null)

watch(() => visible.value, (value) => {
  if (value)
    loadNotices()
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    refreshUnreadCount()
    connectWebSocket()
  }
})

onUnmounted(() => {
  socket?.close()
})
</script>

<template>
  <a-popover
    v-model:open="visible"
    trigger="click"
    placement="bottomRight"
    overlay-class-name="notice-popover"
  >
    <template #content>
      <div class="w-[380px] overflow-hidden rounded-[24px] bg-white dark:bg-slate-950">
        <div class="border-b border-slate-200/80 bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] px-4 py-4 dark:border-slate-800 dark:bg-[linear-gradient(180deg,#0f172a_0%,#020617_100%)]">
          <div class="flex items-start justify-between gap-4">
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <div class="flex h-9 w-9 items-center justify-center rounded-2xl bg-sky-100 text-sky-600 dark:bg-sky-500/10 dark:text-sky-300">
                  <BellOutlined class="text-[15px]" />
                </div>
                <div>
                  <div class="text-sm font-semibold text-slate-900 dark:text-slate-100">
                    消息盒子
                  </div>
                  <div class="text-xs text-slate-500 dark:text-slate-400">
                    汇总最近站内通知和待处理消息
                  </div>
                </div>
              </div>
            </div>

            <a-button
              type="text"
              size="small"
              class="!h-8 !rounded-xl !px-2.5 !text-[12px] !text-slate-500 hover:!bg-slate-100 hover:!text-slate-900 dark:!text-slate-400 dark:hover:!bg-slate-800 dark:hover:!text-slate-100"
              @click="handleReadAll"
            >
              <template #icon>
                <CheckOutlined />
              </template>
              全部已读
            </a-button>
          </div>

          <div class="mt-4 grid grid-cols-2 gap-3">
            <div class="rounded-2xl border border-rose-200 bg-rose-50/90 px-3.5 py-3 dark:border-rose-900/60 dark:bg-rose-950/30">
              <div class="text-[12px] text-rose-600 dark:text-rose-300">
                未读消息
              </div>
              <div class="mt-2 text-2xl font-semibold text-rose-700 dark:text-rose-200">
                {{ unreadCount }}
              </div>
            </div>

            <button
              type="button"
              class="rounded-2xl border border-slate-200 bg-white px-3.5 py-3 text-left transition-colors hover:border-slate-300 hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-900/70 dark:hover:border-slate-600 dark:hover:bg-slate-900"
              @click="visible = false; $router.push('/notices')"
            >
              <div class="text-[12px] text-slate-500 dark:text-slate-400">
                查看全部
              </div>
              <div class="mt-2 flex items-center justify-between text-sm font-medium text-slate-900 dark:text-slate-100">
                <span>进入通知中心</span>
                <RightOutlined class="text-[12px] text-slate-400" />
              </div>
            </button>
          </div>

          <div
            v-if="latestNotice"
            class="mt-4 rounded-2xl border border-slate-200/80 bg-white/90 px-3.5 py-3 dark:border-slate-800 dark:bg-slate-900/80"
          >
            <div class="flex items-center gap-2">
              <span class="rounded-full bg-sky-100 px-2 py-0.5 text-[10px] font-medium text-sky-700 dark:bg-sky-500/10 dark:text-sky-300">
                最新消息
              </span>
              <span class="truncate text-xs text-slate-400 dark:text-slate-500">
                {{ latestNotice.createTime || '-' }}
              </span>
            </div>
            <div class="mt-2 truncate text-sm font-medium text-slate-900 dark:text-slate-100">
              {{ latestNotice.title }}
            </div>
            <p class="mt-1 line-clamp-2 text-xs leading-5 text-slate-500 dark:text-slate-400">
              {{ getNoticePreview(latestNotice.content, 72) }}
            </p>
          </div>
        </div>

        <div class="max-h-[360px] overflow-auto px-3 py-3">
          <a-list
            :loading="loading"
            :data-source="noticeList"
            :locale="{ emptyText: '暂无通知' }"
            size="small"
          >
            <template #renderItem="{ item }">
              <a-list-item class="!border-none !px-0 !py-0">
                <button
                  type="button"
                  class="w-full rounded-2xl border border-transparent px-3 py-3 text-left transition-all hover:border-slate-200 hover:bg-slate-50 dark:hover:border-slate-800 dark:hover:bg-slate-900/70"
                  @click="handleRead(item)"
                >
                  <div class="flex items-start gap-3">
                    <div class="mt-1 flex h-8 w-8 shrink-0 items-center justify-center rounded-2xl" :class="item.isRead ? 'bg-slate-100 text-slate-400 dark:bg-slate-900 dark:text-slate-500' : 'bg-rose-100 text-rose-600 dark:bg-rose-500/10 dark:text-rose-300'">
                      <BellOutlined class="text-[13px]" />
                    </div>

                    <div class="min-w-0 flex-1">
                      <div class="flex items-center gap-2">
                        <div class="truncate text-sm" :class="item.isRead ? 'text-slate-700 dark:text-slate-300' : 'font-medium text-slate-900 dark:text-slate-100'">
                          {{ item.title }}
                        </div>
                        <span
                          v-if="!item.isRead"
                          class="rounded-full bg-rose-100 px-2 py-0.5 text-[10px] font-medium text-rose-600 dark:bg-rose-500/10 dark:text-rose-300"
                        >
                          未读
                        </span>
                      </div>
                      <p class="mt-1 line-clamp-2 text-xs leading-5 text-slate-500 dark:text-slate-400">
                        {{ getNoticePreview(item.content) }}
                      </p>
                      <div class="mt-2 text-[11px] text-slate-400 dark:text-slate-500">
                        {{ item.createTime || '-' }}
                      </div>
                    </div>
                  </div>
                </button>
              </a-list-item>
            </template>
          </a-list>
        </div>

        <div v-if="unreadList.length" class="border-t border-slate-200/80 px-4 py-2.5 text-[11px] text-slate-500 dark:border-slate-800 dark:text-slate-400">
          当前仍有 {{ unreadList.length }} 条未读消息，建议优先处理最新通知。
        </div>
      </div>
    </template>

    <div class="flex h-10 w-10 cursor-pointer items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 shadow-sm shadow-slate-100/80 transition-colors hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:shadow-none dark:hover:border-slate-500 dark:hover:text-slate-100">
      <a-badge :count="unreadCount" :offset="[2, 0]" size="small" :show-zero="false">
        <BellOutlined class="text-[15px]" />
      </a-badge>
    </div>
  </a-popover>
</template>

<style>
.notice-popover .ant-popover-inner-content {
  padding: 0;
}
</style>
