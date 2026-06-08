<script setup lang="ts">
import { Modal, message } from 'ant-design-vue'
import type * as API from '../../types'
import { deleteHabitApi, getHabitPageApi, pauseHabitApi, resumeHabitApi } from '../../api/habit.ts'

const loading = ref(false)
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const habitList = ref<API.PageResult<API.Habit>>({
  records: [],
  total: 0,
  size: 0,
  current: 0,
})

/**
 * 搜索条件。
 */
const searchForm = ref({
  name: '',
  category: '',
  status: undefined as number | undefined,
})

/**
 * 加载习惯分页数据。
 */
async function loadHabits(page = current.value, size = pageSize.value) {
  loading.value = true
  try {
    const res = await getHabitPageApi(
      { current: Number(page), size: Number(size) },
      {
        name: searchForm.value.name,
        category: searchForm.value.category,
        status: searchForm.value.status,
      },
    )
    habitList.value = res
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载习惯列表失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 执行搜索。
 */
function handleSearch() {
  current.value = 1
  loadHabits()
}

/**
 * 重置搜索条件。
 */
function handleReset() {
  searchForm.value = {
    name: '',
    category: '',
    status: undefined,
  }
  current.value = 1
  loadHabits()
}

/**
 * 处理分页变化。
 */
function handleTableChange(pagination: any) {
  loadHabits(pagination.current, pagination.pageSize)
}

/**
 * 删除习惯。
 */
function handleDelete(record: API.Habit) {
  Modal.confirm({
    title: '删除习惯',
    content: `确认删除习惯「${record.name}」吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await deleteHabitApi(record.id as number)
      message.success('删除成功')
      loadHabits()
    },
  })
}

/**
 * 切换习惯状态。
 */
async function handleToggleStatus(record: API.Habit) {
  try {
    if (record.status === 1) {
      await pauseHabitApi(record.id as number)
      message.success('已暂停习惯')
    }
    else {
      await resumeHabitApi(record.id as number)
      message.success('已恢复习惯')
    }
    loadHabits()
  }
  catch (error) {
    console.error('切换习惯状态失败:', error)
  }
}

onMounted(() => {
  loadHabits()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 px-3 md:px-0 space-y-6">
    <div class="rounded-3xl border border-slate-200/80 bg-white overflow-hidden">
      <div class="px-6 py-6 border-b border-slate-200/70">
        <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
          <div>
            <h2 class="text-2xl font-bold text-slate-900">
              习惯管理
            </h2>
            <p class="mt-2 text-sm text-slate-500 max-w-3xl">
              在后台查看用户创建的习惯，关注分类分布、提醒配置和启用状态，为后续运营分析做准备。
            </p>
          </div>
          <div class="flex items-center gap-3">
            <a-button @click="handleReset">
              重置
            </a-button>
            <a-button type="primary" :loading="loading" @click="handleSearch">
              搜索
            </a-button>
          </div>
        </div>
      </div>

      <div class="px-6 py-5 border-b border-slate-200/70 bg-slate-50/60">
        <a-form layout="inline" :model="searchForm">
          <a-form-item label="习惯名称">
            <a-input v-model:value="searchForm.name" placeholder="请输入习惯名称" allow-clear @press-enter="handleSearch" />
          </a-form-item>
          <a-form-item label="习惯分类">
            <a-input v-model:value="searchForm.category" placeholder="请输入分类编码" allow-clear @press-enter="handleSearch" />
          </a-form-item>
          <a-form-item label="状态">
            <a-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              style="width: 160px"
              allow-clear
            >
              <a-select-option :value="1">
                启用
              </a-select-option>
              <a-select-option :value="2">
                暂停
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-form>
      </div>

      <div class="overflow-hidden">
        <a-table
          :data-source="habitList.records"
          :loading="loading"
          row-key="id"
          :pagination="{
            current,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (allTotal) => `共 ${allTotal} 条记录`,
          }"
          @change="handleTableChange"
        >
          <a-table-column title="习惯ID" data-index="id" width="90" />
          <a-table-column title="习惯名称" data-index="name" />
          <a-table-column title="分类" data-index="category" width="140" />
          <a-table-column title="提醒时间" data-index="reminderTime" width="140">
            <template #default="{ text }">
              {{ text || '-' }}
            </template>
          </a-table-column>
          <a-table-column title="补打卡" width="120">
            <template #default="{ record }">
              {{ record.allowMakeup === 1 ? '允许' : '不允许' }}
            </template>
          </a-table-column>
          <a-table-column title="状态" width="120">
            <template #default="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'orange'">
                {{ record.status === 1 ? '启用' : '暂停' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="开始日期" data-index="startDate" width="140" />
          <a-table-column title="操作" width="180" fixed="right">
            <template #default="{ record }">
              <a-space :size="4">
                <a-button type="link" @click="handleToggleStatus(record)">
                  {{ record.status === 1 ? '暂停' : '恢复' }}
                </a-button>
                <a-button type="link" danger @click="handleDelete(record)">
                  删除
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </a-table>
      </div>
    </div>
  </div>
</template>
