<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { createRoleApi, deleteRoleApi, getRolePageApi, getRoleUsersApi, updateRoleApi } from '../../api/role.ts'
import type * as API from '../../types'

interface RoleRecord extends API.Role {
  userCount?: number
}

const loading = ref(false)
const drawerVisible = ref(false)
const drawerLoading = ref(false)
const memberVisible = ref(false)
const memberLoading = ref(false)

const total = ref(0)
const current = ref(1)
const pageSize = ref(10)

const roleList = ref<API.PageResult<RoleRecord>>({
  records: [],
  total: 0,
  size: 0,
  current: 0,
})

const currentRole = ref<RoleRecord | null>(null)
const memberRole = ref<RoleRecord | null>(null)
const currentMembers = ref<API.User[]>([])

/**
 * 角色搜索条件。
 */
const searchForm = ref({
  roleName: '',
  roleKey: '',
})

/**
 * 角色表单。
 */
const form = ref({
  id: undefined as number | undefined,
  roleName: '',
  roleKey: '',
})

/**
 * 加载角色分页数据。
 */
async function loadRoles(page = current.value, size = pageSize.value) {
  loading.value = true
  try {
    const res = await getRolePageApi(
      { current: Number(page), size: Number(size) },
      {
        roleName: searchForm.value.roleName,
        roleKey: searchForm.value.roleKey,
      },
    )
    roleList.value = res as API.PageResult<RoleRecord>
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载角色列表失败:', error)
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
  loadRoles()
}

/**
 * 重置搜索条件。
 */
function handleReset() {
  searchForm.value = {
    roleName: '',
    roleKey: '',
  }
  current.value = 1
  loadRoles()
}

/**
 * 表格分页切换。
 */
function handleTableChange(pagination: TablePaginationConfig) {
  loadRoles(pagination.current, pagination.pageSize)
}

/**
 * 打开新增角色抽屉。
 */
function handleCreate() {
  currentRole.value = null
  form.value = {
    id: undefined,
    roleName: '',
    roleKey: '',
  }
  drawerVisible.value = true
}

/**
 * 打开编辑角色抽屉。
 */
function handleEdit(record: RoleRecord) {
  currentRole.value = record
  form.value = {
    id: record.id,
    roleName: record.roleName,
    roleKey: record.roleKey,
  }
  drawerVisible.value = true
}

/**
 * 基于现有角色快速复制。
 */
function handleClone(record: RoleRecord) {
  currentRole.value = null
  form.value = {
    id: undefined,
    roleName: `${record.roleName}-副本`,
    roleKey: `${record.roleKey}_COPY`,
  }
  drawerVisible.value = true
}

/**
 * 打开角色成员抽屉并加载成员。
 */
async function handleOpenMembers(record: RoleRecord) {
  memberRole.value = record
  memberVisible.value = true
  memberLoading.value = true
  try {
    currentMembers.value = await getRoleUsersApi(record.id!)
  }
  catch (error) {
    console.error('加载角色成员失败:', error)
  }
  finally {
    memberLoading.value = false
  }
}

/**
 * 保存角色。
 */
async function handleSave() {
  try {
    drawerLoading.value = true
    if (form.value.id) {
      await updateRoleApi(form.value)
      message.success('角色更新成功')
    }
    else {
      await createRoleApi(form.value)
      message.success('角色创建成功')
    }
    drawerVisible.value = false
    loadRoles()
  }
  catch (error) {
    console.error('保存角色失败:', error)
  }
  finally {
    drawerLoading.value = false
  }
}

/**
 * 删除角色。
 */
async function handleDelete(id: number) {
  try {
    await deleteRoleApi(id)
    message.success('角色删除成功')
    loadRoles()
  }
  catch (error) {
    console.error('删除角色失败:', error)
  }
}

/**
 * 统一处理更多操作菜单。
 */
function handleActionMenuClick(record: RoleRecord, key: string) {
  if (key === 'members') {
    handleOpenMembers(record)
    return
  }
  if (key === 'clone') {
    handleClone(record)
    return
  }
  if (key === 'delete' && record.id) {
    handleDelete(record.id)
  }
}

/**
 * 计算角色内成员总数。
 */
const memberTotal = computed(() => roleList.value.records.reduce((sum, item) => sum + (Number(item.userCount) || 0), 0))

/**
 * 统计当前页为空角色数量。
 */
const emptyRoleCount = computed(() => roleList.value.records.filter(item => !Number(item.userCount)).length)

/**
 * 统计当前页高频使用角色数量。
 */
const activeRoleCount = computed(() => roleList.value.records.filter(item => Number(item.userCount) >= 5).length)

/**
 * 首页摘要卡片。
 */
const summaryCards = computed(() => [
  {
    title: '角色总数',
    value: `${total.value}`,
    hint: '匹配当前筛选条件的角色数量',
    icon: 'i-fa6-solid:user-shield',
    tone: 'slate',
  },
  {
    title: '成员覆盖',
    value: `${memberTotal.value}`,
    hint: '当前页角色累计关联成员数',
    icon: 'i-fa6-solid:users',
    tone: 'blue',
  },
  {
    title: '活跃角色',
    value: `${activeRoleCount.value}`,
    hint: '当前页中成员数不少于 5 的角色',
    icon: 'i-fa6-solid:bolt',
    tone: 'emerald',
  },
  {
    title: '待补成员',
    value: `${emptyRoleCount.value}`,
    hint: '当前页暂无成员的角色，建议检查是否仍在使用',
    icon: 'i-fa6-solid:user-slash',
    tone: 'amber',
  },
])

/**
 * 返回角色成员数量标签样式。
 */
function getMemberCountTagColor(count?: number) {
  if (!Number(count))
    return 'default'
  if (Number(count) >= 5)
    return 'processing'
  return 'blue'
}

/**
 * 返回成员邮箱或占位文本。
 */
function getUserEmail(user: API.User) {
  return user.email || '未填写邮箱'
}

/**
 * 返回成员手机号或占位文本。
 */
function getUserPhone(user: API.User) {
  return user.phone || '未填写手机号'
}

onMounted(() => {
  loadRoles()
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
                角色管理
              </h2>
              <p class="mt-2 max-w-3xl text-sm md:text-base text-slate-500 dark:text-slate-400">
                统一维护系统角色、标识编码与成员覆盖情况，帮助你更快识别空角色、活跃角色以及需要继续收敛的权限分组。
              </p>
            </div>

            <div class="flex flex-wrap gap-2">
              <div class="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-[13px] text-slate-600 dark:border-slate-700 dark:bg-slate-800/70 dark:text-slate-300">
                <span class="i-fa6-solid:users-viewfinder text-[12px] text-slate-400 dark:text-slate-500" />
                当前页角色 {{ roleList.records.length }}
              </div>
              <div class="inline-flex items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1.5 text-[13px] text-emerald-700 dark:border-emerald-900/80 dark:bg-emerald-950/40 dark:text-emerald-300">
                <span class="i-fa6-solid:bolt text-[12px]" />
                活跃角色 {{ activeRoleCount }}
              </div>
              <div class="inline-flex items-center gap-2 rounded-full border border-amber-200 bg-amber-50 px-3 py-1.5 text-[13px] text-amber-700 dark:border-amber-900/80 dark:bg-amber-950/40 dark:text-amber-300">
                <span class="i-fa6-solid:user-slash text-[12px]" />
                空角色 {{ emptyRoleCount }}
              </div>
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
              搜索角色
            </a-button>
            <a-button type="primary" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleCreate">
              <template #icon>
                <span class="i-fa6-solid:plus text-[12px] leading-none" />
              </template>
              新增角色
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
              'border-sky-200 bg-sky-50/80 dark:border-sky-900/70 dark:bg-sky-950/30': item.tone === 'blue',
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
                  'bg-sky-600 text-white dark:bg-sky-500': item.tone === 'blue',
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
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] xl:items-end">
          <a-form-item label="角色名称" class="mb-0">
            <a-input
              v-model:value="searchForm.roleName"
              allow-clear
              placeholder="请输入角色名称"
              @press-enter="handleSearch"
            />
          </a-form-item>

          <a-form-item label="角色标识" class="mb-0">
            <a-input
              v-model:value="searchForm.roleKey"
              allow-clear
              placeholder="请输入角色标识"
              @press-enter="handleSearch"
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
        :data-source="roleList.records"
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
        <a-table-column key="id" title="角色 ID" data-index="id" width="96" />

        <a-table-column key="roleName" title="角色信息" data-index="roleName" :custom-cell="() => ({ class: 'align-top' })">
          <template #default="{ record }">
            <div class="min-w-0 py-0.5">
              <div class="truncate text-[14px] font-medium text-slate-900 dark:text-slate-100">
                {{ record.roleName }}
              </div>
              <div class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                创建时间：{{ record.createTime || '-' }}
              </div>
            </div>
          </template>
        </a-table-column>

        <a-table-column key="roleKey" title="角色标识" data-index="roleKey" width="220">
          <template #default="{ text }">
            <div class="min-w-0">
              <code class="block truncate rounded-lg bg-slate-100 px-2.5 py-1.5 text-[12px] text-sky-700 dark:bg-slate-800 dark:text-sky-300">
                {{ text }}
              </code>
            </div>
          </template>
        </a-table-column>

        <a-table-column key="userCount" title="成员数量" data-index="userCount" width="120" align="center">
          <template #default="{ text }">
            <a-tag :color="getMemberCountTagColor(text)">
              {{ text || 0 }}
            </a-tag>
          </template>
        </a-table-column>

        <a-table-column key="updateTime" title="更新时间" data-index="updateTime" width="180" />

        <a-table-column key="action" title="操作" align="center" width="210" fixed="right">
          <template #default="{ record }">
            <div class="flex items-center justify-center gap-1">
              <a-button type="link" class="!px-1.5 text-[13px]" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button type="link" class="!px-1.5 text-[13px]" @click="handleOpenMembers(record)">
                成员
              </a-button>
              <a-dropdown>
                <a-button type="link" class="inline-flex items-center gap-1 !px-1.5 text-[13px]">
                  更多
                  <span class="i-fa6-solid:chevron-down text-[10px]" />
                </a-button>
                <template #overlay>
                  <a-menu @click="({ key }) => handleActionMenuClick(record, String(key))">
                    <a-menu-item key="clone">
                      复制角色
                    </a-menu-item>
                    <a-menu-item key="members">
                      查看成员
                    </a-menu-item>
                    <a-menu-item key="delete" danger>
                      删除角色
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <a-drawer
      v-model:open="drawerVisible"
      :title="currentRole ? '编辑角色' : '新增角色'"
      placement="right"
      width="520"
    >
      <div class="space-y-5">
        <div class="rounded-2xl border border-slate-200/80 bg-slate-50/80 px-4 py-4 dark:border-slate-800 dark:bg-slate-900/60">
          <div class="text-sm font-medium text-slate-900 dark:text-slate-100">
            {{ currentRole ? '调整角色基础信息' : '创建新的权限角色' }}
          </div>
          <p class="mt-2 text-xs leading-5 text-slate-500 dark:text-slate-400">
            角色标识建议使用大写字母和下划线，便于和权限编码、业务配置保持统一。
          </p>
        </div>

        <a-form :model="form" layout="vertical" @finish="handleSave">
          <a-form-item v-if="form.id" label="角色 ID">
            <a-input v-model:value="form.id" disabled />
          </a-form-item>

          <a-form-item
            label="角色名称"
            name="roleName"
            :rules="[{ required: true, message: '请输入角色名称' }]"
          >
            <a-input v-model:value="form.roleName" placeholder="例如：运营管理员" />
          </a-form-item>

          <a-form-item
            label="角色标识"
            name="roleKey"
            :rules="[{ required: true, message: '请输入角色标识' }]"
          >
            <a-input v-model:value="form.roleKey" placeholder="例如：OPS_ADMIN" />
          </a-form-item>

          <a-form-item class="mb-0 pt-2">
            <a-button type="primary" html-type="submit" :loading="drawerLoading" block>
              {{ currentRole ? '保存修改' : '创建角色' }}
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </a-drawer>

    <a-drawer
      v-model:open="memberVisible"
      :title="`角色成员 - ${memberRole?.roleName || ''}`"
      placement="right"
      width="560"
    >
      <div class="space-y-4">
        <div class="rounded-2xl border border-slate-200/80 bg-slate-50/80 px-4 py-4 dark:border-slate-800 dark:bg-slate-900/60">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <div class="text-sm font-medium text-slate-900 dark:text-slate-100">
                成员覆盖概览
              </div>
              <p class="mt-1 text-xs leading-5 text-slate-500 dark:text-slate-400">
                当前角色下共有 {{ currentMembers.length }} 名成员，可用于快速核对角色是否仍在实际使用。
              </p>
            </div>
            <a-tag :color="getMemberCountTagColor(currentMembers.length)">
              {{ currentMembers.length }} 人
            </a-tag>
          </div>
        </div>

        <div v-if="memberLoading" class="py-20 text-center">
          <a-spin tip="正在加载角色成员..." />
        </div>

        <a-empty v-else-if="currentMembers.length === 0" description="该角色下暂无成员" />

        <div v-else class="space-y-3">
          <div
            v-for="user in currentMembers"
            :key="user.id"
            class="rounded-2xl border border-slate-200/80 bg-white px-4 py-4 dark:border-slate-800 dark:bg-slate-900/80"
          >
            <div class="flex items-start justify-between gap-4">
              <div class="min-w-0">
                <div class="flex items-center gap-2">
                  <div class="max-w-[180px] truncate text-sm font-medium text-slate-900 dark:text-slate-100">
                    {{ user.nickname || user.username }}
                  </div>
                  <a-tag v-if="user.status === 0" color="success">
                    正常
                  </a-tag>
                  <a-tag v-else-if="user.status === 1" color="default">
                    停用
                  </a-tag>
                </div>
                <div class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                  @{{ user.username }}
                </div>
              </div>

              <div class="shrink-0 text-right text-xs leading-5 text-slate-500 dark:text-slate-400">
                <div>{{ getUserEmail(user) }}</div>
                <div>{{ getUserPhone(user) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-drawer>
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
