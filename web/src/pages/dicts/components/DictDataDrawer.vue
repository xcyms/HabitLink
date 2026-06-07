<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { createDictDataApi, deleteDictDataApi, getDictDataPageApi, updateDictDataApi } from '../../../api/dict.ts'
import type { Dict, DictData } from '../../../types'

interface Props {
  visible: boolean
  dict?: Dict | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
}>()

const drawerVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val),
})

const loading = ref(false)
const formDrawerVisible = ref(false)
const formLoading = ref(false)
const currentData = ref<DictData | null>(null)
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const dataList = ref<DictData[]>([])

/**
 * 数据筛选表单。
 */
const searchForm = ref({
  dictLabel: '',
  status: undefined as number | undefined,
})

const form = ref({
  id: undefined as number | undefined,
  dictId: 0,
  dictLabel: '',
  dictValue: '',
  dictType: '',
  dictSort: 0,
  status: 0,
  remark: '',
})

/**
 * 加载字典数据列表。
 */
async function loadData(page = current.value, size = pageSize.value) {
  if (!props.dict)
    return

  loading.value = true
  try {
    const res = await getDictDataPageApi(
      { current: Number(page), size: Number(size) },
      {
        dictId: Number(props.dict.id),
        dictType: props.dict.dictType,
        dictLabel: searchForm.value.dictLabel,
        status: searchForm.value.status,
      },
    )
    dataList.value = res.records
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载字典数据列表失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 监听字典切换并重置筛选条件。
 */
watch(() => props.dict, () => {
  searchForm.value = {
    dictLabel: '',
    status: undefined,
  }
  current.value = 1
  loadData()
}, { immediate: true })

/**
 * 搜索数据项。
 */
function handleSearch() {
  current.value = 1
  loadData()
}

/**
 * 重置数据项筛选条件。
 */
function handleReset() {
  searchForm.value = {
    dictLabel: '',
    status: undefined,
  }
  current.value = 1
  loadData()
}

/**
 * 快捷切换数据项状态。
 */
function handleQuickStatus(status?: number) {
  searchForm.value.status = status
  current.value = 1
  loadData()
}

/**
 * 表格分页切换。
 */
function handleTableChange(pagination: TablePaginationConfig) {
  loadData(pagination.current, pagination.pageSize)
}

/**
 * 打开新增数据项表单。
 */
function handleCreate() {
  if (!props.dict)
    return

  currentData.value = null
  form.value = {
    id: undefined,
    dictId: Number(props.dict.id),
    dictLabel: '',
    dictValue: '',
    dictType: props.dict.dictType,
    dictSort: 0,
    status: 0,
    remark: '',
  }
  formDrawerVisible.value = true
}

/**
 * 打开编辑数据项表单。
 */
function handleEdit(record: DictData) {
  currentData.value = record
  form.value = {
    id: record.id,
    dictId: Number(record.dictId),
    dictLabel: record.dictLabel || '',
    dictValue: record.dictValue || '',
    dictType: record.dictType || '',
    dictSort: record.dictSort ?? 0,
    status: record.status ?? 0,
    remark: record.remark || '',
  }
  formDrawerVisible.value = true
}

/**
 * 保存字典数据项。
 */
async function handleSave() {
  if (!form.value.dictLabel) {
    message.error('请输入字典标签')
    return
  }
  if (!form.value.dictValue) {
    message.error('请输入字典键值')
    return
  }

  try {
    formLoading.value = true
    if (form.value.id) {
      await updateDictDataApi(form.value)
      message.success('更新成功')
    }
    else {
      await createDictDataApi(form.value)
      message.success('创建成功')
    }
    formDrawerVisible.value = false
    loadData()
  }
  catch (error) {
    console.error('保存字典数据失败:', error)
  }
  finally {
    formLoading.value = false
  }
}

/**
 * 删除字典数据项。
 */
async function handleDelete(id: number) {
  try {
    await deleteDictDataApi(id)
    message.success('删除成功')
    loadData()
  }
  catch (error) {
    console.error('删除字典数据失败:', error)
  }
}

/**
 * 关闭表单抽屉。
 */
function handleFormCancel() {
  formDrawerVisible.value = false
}

const enabledCount = computed(() => dataList.value.filter(item => item.status === 0).length)
const disabledCount = computed(() => dataList.value.filter(item => item.status === 1).length)

const summaryCards = computed(() => [
  {
    title: '数据项总数',
    value: `${total.value}`,
    hint: '当前字典类型下的全部数据项数量',
    icon: 'i-fa6-solid:list-check',
    tone: 'slate',
  },
  {
    title: '启用项',
    value: `${enabledCount.value}`,
    hint: '当前页可正常参与业务映射的数据项',
    icon: 'i-fa6-solid:circle-check',
    tone: 'emerald',
  },
  {
    title: '停用项',
    value: `${disabledCount.value}`,
    hint: '已停用的数据项建议确认是否仍被引用',
    icon: 'i-fa6-solid:ban',
    tone: 'rose',
  },
  {
    title: '当前页数量',
    value: `${dataList.value.length}`,
    hint: '便于快速核对本页维护规模',
    icon: 'i-fa6-solid:layer-group',
    tone: 'amber',
  },
])
</script>

<template>
  <a-drawer
    v-model:open="drawerVisible"
    :title="`字典数据管理 - ${dict?.dictName || ''}`"
    :width="960"
    :closable="false"
  >
    <div class="space-y-5">
      <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
        <div class="px-5 py-5 border-b border-slate-200/70 dark:border-slate-800 flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
          <div class="space-y-3">
            <div>
              <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
                {{ dict?.dictName || '字典数据管理' }}
              </h3>
              <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
                维护当前字典类型下的标签、键值、状态和排序，避免同一类型中的数据项重复或失序。
              </p>
            </div>

            <div class="flex flex-wrap items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
              <span>字典类型</span>
              <code class="rounded-lg bg-slate-100 px-2 py-1 text-xs text-blue-600 break-all dark:bg-slate-800 dark:text-blue-300">
                {{ dict?.dictType || '-' }}
              </code>
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

          <div class="flex flex-wrap gap-2.5 lg:justify-end">
            <a-button class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleReset">
              重置条件
            </a-button>
            <a-button type="primary" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleSearch">
              <template #icon>
                <span class="i-fa6-solid:magnifying-glass text-[12px] leading-none" />
              </template>
              搜索数据
            </a-button>
            <a-button type="primary" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" @click="handleCreate">
              <template #icon>
                <span class="i-fa6-solid:plus text-[12px] leading-none" />
              </template>
              新增数据
            </a-button>
          </div>
        </div>

        <div class="p-5">
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
        <div class="px-5 py-5 border-b border-slate-200/70 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/30">
          <a-form layout="vertical" :model="searchForm">
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
              <a-form-item label="字典标签" class="mb-0">
                <a-input
                  v-model:value="searchForm.dictLabel"
                  placeholder="请输入字典标签"
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

        <div class="px-5 py-4 border-b border-slate-200/70 dark:border-slate-800 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
          <div>
            <h4 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
              数据项列表
            </h4>
            <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
              推荐先维护标签与键值的唯一性，再调整排序和状态，避免前端枚举展示出现重复或错序。
            </p>
          </div>
          <div class="text-sm text-slate-500 dark:text-slate-400">
            当前页共 {{ dataList.length }} 条
          </div>
        </div>

        <div class="overflow-hidden">
          <a-table
            :data-source="dataList"
            :loading="loading"
            :scroll="{ x: 980 }"
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
            <a-table-column key="id" title="ID" data-index="id" width="72" />
            <a-table-column key="dictLabel" title="字典标签" width="180">
              <template #default="{ record }">
                <div class="font-medium text-slate-800 dark:text-slate-100">
                  {{ record.dictLabel || '-' }}
                </div>
              </template>
            </a-table-column>
            <a-table-column key="dictValue" title="字典键值" width="180">
              <template #default="{ record }">
                <code class="inline-flex max-w-full rounded-lg bg-slate-100 px-2 py-1 text-xs text-blue-600 break-all dark:bg-slate-800 dark:text-blue-300">
                  {{ record.dictValue || '-' }}
                </code>
              </template>
            </a-table-column>
            <a-table-column key="dictSort" title="排序" data-index="dictSort" width="88" />
            <a-table-column key="status" title="状态" width="100">
              <template #default="{ record }">
                <DictTag dict-type="sys_common_status" :value="record.status" />
              </template>
            </a-table-column>
            <a-table-column key="remark" title="备注" width="220">
              <template #default="{ record }">
                <div class="text-slate-500 dark:text-slate-400">
                  {{ record.remark || '-' }}
                </div>
              </template>
            </a-table-column>
            <a-table-column key="action" title="操作" width="140" fixed="right">
              <template #default="{ record }">
                <div class="flex items-center gap-1">
                  <a-button type="link" size="small" class="px-1.5" @click="handleEdit(record)">
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="确定要删除该数据项吗？"
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
    </div>

    <a-drawer
      v-model:open="formDrawerVisible"
      :title="currentData ? '编辑字典数据' : '新增字典数据'"
      :width="520"
      :closable="false"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item label="字典类型">
          <a-input v-model:value="form.dictType" disabled />
        </a-form-item>
        <a-form-item label="字典标签" required>
          <a-input v-model:value="form.dictLabel" placeholder="请输入字典标签，例如：正常" />
        </a-form-item>
        <a-form-item label="字典键值" required>
          <a-input v-model:value="form.dictValue" placeholder="请输入字典键值，例如：0" />
        </a-form-item>
        <a-form-item label="显示排序">
          <a-input-number v-model:value="form.dictSort" :min="0" class="!w-full" />
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="0">
              正常
            </a-radio>
            <a-radio :value="1">
              停用
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="4" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="handleFormCancel">
            取消
          </a-button>
          <a-button type="primary" :loading="formLoading" @click="handleSave">
            保存
          </a-button>
        </a-space>
      </template>
    </a-drawer>
  </a-drawer>
</template>

<style scoped>
:deep(.ant-input),
:deep(.ant-input-affix-wrapper),
:deep(.ant-select-selector),
:deep(.ant-input-number),
:deep(.ant-input-number-input-wrap),
:deep(.ant-input-number-group-wrapper) {
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
