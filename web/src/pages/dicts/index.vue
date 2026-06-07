<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { deleteDictApi, getDictPageApi } from '../../api/dict.ts'
import type * as API from '../../types'
import DictTypeDrawer from './components/DictTypeDrawer.vue'
import DictDataDrawer from './components/DictDataDrawer.vue'

const loading = ref(false)
const typeDrawerVisible = ref(false)
const dataDrawerVisible = ref(false)
const currentDict = ref<API.Dict | null>(null)
const currentDictForData = ref<API.Dict | null>(null)
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const dictList = ref<API.PageResult<API.Dict>>({
  records: [],
  total: 0,
  size: 0,
  current: 0,
})

/**
 * 搜索表单。
 */
const searchForm = ref({
  dictName: '',
  dictType: '',
  status: undefined as number | undefined,
})

/**
 * 加载字典类型分页数据。
 */
async function loadDicts(page = current.value, size = pageSize.value) {
  loading.value = true
  try {
    const res = await getDictPageApi(
      { current: Number(page), size: Number(size) },
      {
        dictName: searchForm.value.dictName,
        dictType: searchForm.value.dictType,
        status: searchForm.value.status,
      },
    )
    dictList.value = res
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载字典类型列表失败:', error)
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
  loadDicts()
}

/**
 * 重置筛选条件。
 */
function handleReset() {
  searchForm.value = {
    dictName: '',
    dictType: '',
    status: undefined,
  }
  current.value = 1
  loadDicts()
}

/**
 * 快捷切换字典状态。
 */
function handleQuickStatus(status?: number) {
  searchForm.value.status = status
  current.value = 1
  loadDicts()
}

/**
 * 表格分页切换。
 */
function handleTableChange(pagination: TablePaginationConfig) {
  loadDicts(pagination.current, pagination.pageSize)
}

/**
 * 打开新增字典类型抽屉。
 */
function handleCreate() {
  currentDict.value = null
  typeDrawerVisible.value = true
}

/**
 * 打开编辑字典类型抽屉。
 */
function handleEdit(record: API.Dict) {
  currentDict.value = record
  typeDrawerVisible.value = true
}

/**
 * 字典类型保存成功后刷新列表。
 */
function handleTypeDrawerSuccess() {
  loadDicts()
}

/**
 * 删除字典类型。
 */
async function handleDelete(id: number) {
  try {
    await deleteDictApi(id)
    message.success('删除成功')
    loadDicts()
  }
  catch (error) {
    console.error('删除字典类型失败:', error)
  }
}

/**
 * 打开字典数据管理抽屉。
 */
function handleManageData(record: API.Dict) {
  currentDictForData.value = record
  dataDrawerVisible.value = true
}

const records = computed(() => dictList.value.records || [])
const enabledCount = computed(() => records.value.filter(item => item.status === 0).length)
const disabledCount = computed(() => records.value.filter(item => item.status === 1).length)

const summaryCards = computed(() => [
  {
    title: '字典总数',
    value: `${total.value}`,
    hint: '匹配当前筛选条件的字典类型总量',
    icon: 'i-fa6-solid:table-cells-large',
    tone: 'slate',
  },
  {
    title: '启用类型',
    value: `${enabledCount.value}`,
    hint: '当前页内处于正常状态的字典类型',
    icon: 'i-fa6-solid:circle-check',
    tone: 'emerald',
  },
  {
    title: '停用类型',
    value: `${disabledCount.value}`,
    hint: '建议及时核对是否仍有业务引用',
    icon: 'i-fa6-solid:ban',
    tone: 'rose',
  },
  {
    title: '当前页数量',
    value: `${records.value.length}`,
    hint: '本页展示的字典类型记录数',
    icon: 'i-fa6-solid:layer-group',
    tone: 'amber',
  },
])

onMounted(() => {
  loadDicts()
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
                字典管理
              </h2>
              <p class="text-slate-500 dark:text-slate-400 mt-2 text-sm md:text-base max-w-3xl">
                统一维护系统字典类型和字典数据，方便集中管理状态、分类编码和业务枚举项。
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
                :class="searchForm.status === 0
                  ? 'border-emerald-600 bg-emerald-600 text-white dark:border-emerald-500 dark:bg-emerald-500'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleQuickStatus(0)"
              >
                仅看启用
              </button>
              <button
                class="rounded-full border px-3.5 py-1.5 text-[13px] transition-colors"
                :class="searchForm.status === 1
                  ? 'border-rose-600 bg-rose-600 text-white dark:border-rose-500 dark:bg-rose-500'
                  : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:hover:border-slate-500 dark:hover:text-slate-100'"
                @click="handleQuickStatus(1)"
              >
                仅看停用
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
              搜索字典
            </a-button>
            <a-button type="primary" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleCreate">
              <template #icon>
                <span class="i-fa6-solid:plus text-[12px] leading-none" />
              </template>
              新增字典
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
            <a-form-item label="字典名称" class="mb-0">
              <a-input
                v-model:value="searchForm.dictName"
                placeholder="请输入字典名称"
                allow-clear
                @press-enter="handleSearch"
              />
            </a-form-item>
            <a-form-item label="字典类型" class="mb-0">
              <a-input
                v-model:value="searchForm.dictType"
                placeholder="请输入字典类型"
                allow-clear
                @press-enter="handleSearch"
              />
            </a-form-item>
            <a-form-item label="状态" class="mb-0">
              <DictSelect
                v-model:value="searchForm.status"
                dict-type="sys_common_status"
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
            字典类型列表
          </h3>
          <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
            建议优先维护字典类型编码的统一性，再进入数据管理维护枚举项，避免同义字段分散。
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
          :scroll="{ x: 1180 }"
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
          <a-table-column key="id" title="字典ID" data-index="id" width="88" />
          <a-table-column key="dictName" title="字典名称" width="180">
            <template #default="{ record }">
              <div class="font-medium text-slate-800 dark:text-slate-100">
                {{ record.dictName }}
              </div>
            </template>
          </a-table-column>
          <a-table-column key="dictType" title="字典类型" width="240">
            <template #default="{ record }">
              <code class="inline-flex max-w-full rounded-lg bg-slate-100 px-2 py-1 text-xs text-blue-600 break-all dark:bg-slate-800 dark:text-blue-300">
                {{ record.dictType }}
              </code>
            </template>
          </a-table-column>
          <a-table-column key="status" title="状态" width="110">
            <template #default="{ record }">
              <DictTag dict-type="sys_common_status" :value="record.status" />
            </template>
          </a-table-column>
          <a-table-column key="remark" title="备注" width="240">
            <template #default="{ record }">
              <div class="text-slate-500 dark:text-slate-400">
                {{ record.remark || '-' }}
              </div>
            </template>
          </a-table-column>
          <a-table-column key="createTime" title="创建时间" data-index="createTime" width="180" />
          <a-table-column key="action" title="操作" width="220" fixed="right">
            <template #default="{ record }">
              <div class="flex items-center gap-1">
                <a-button type="link" size="small" class="px-1.5" @click="handleManageData(record)">
                  数据管理
                </a-button>
                <a-button type="link" size="small" class="px-1.5" @click="handleEdit(record)">
                  编辑
                </a-button>
                <a-popconfirm
                  title="确定要删除该字典类型吗？"
                  description="删除后将同时删除该字典下的所有数据项。"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDelete(record.id!)"
                >
                  <a-button type="link" danger size="small" class="px-1.5">
                    删除
                  </a-button>
                </a-popconfirm>
              </div>
            </template>
          </a-table-column>
        </a-table>
      </div>
    </div>

    <DictTypeDrawer
      v-model:visible="typeDrawerVisible"
      :dict-data="currentDict"
      @success="handleTypeDrawerSuccess"
    />

    <DictDataDrawer
      v-model:visible="dataDrawerVisible"
      :dict="currentDictForData"
    />
  </div>
</template>

<style scoped>
:deep(.ant-input),
:deep(.ant-input-affix-wrapper),
:deep(.ant-select-selector) {
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
