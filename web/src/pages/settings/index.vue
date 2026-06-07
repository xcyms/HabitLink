<script setup lang="ts">
import { message } from 'ant-design-vue'
import { getSystemConfigsApi, updateConfigApi } from '../../api/config.ts'
import type * as API from '../../types'

interface ConfigGroup {
  key: string
  title: string
  desc: string
  icon: string
  items: API.SysConfig[]
}

const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const viewMode = ref<'all' | 'changed'>('all')
const activeGroup = ref('all')
const configs = ref<API.SysConfig[]>([])
const originalValueMap = ref<Record<string, string>>({})

const defaultGroupMeta = {
  title: '其他配置',
  desc: '未归类的配置项，建议修改前先确认用途。',
  icon: 'i-fa6-solid:gears',
}

const groupMeta: Record<string, { title: string, desc: string, icon: string }> = {
  upload: {
    title: '上传策略',
    desc: '文件大小、存储路径、用户目录等上传相关配置。',
    icon: 'i-fa6-solid:cloud-arrow-up',
  },
  storage: {
    title: '存储设置',
    desc: '本地或云端存储方式及其配套参数。',
    icon: 'i-fa6-solid:hard-drive',
  },
  security: {
    title: '安全限制',
    desc: '文件类型、访问范围和风险控制相关配置。',
    icon: 'i-fa6-solid:shield-halved',
  },
  system: {
    title: '系统参数',
    desc: '通用系统级配置与全局运行参数。',
    icon: 'i-fa6-solid:sliders',
  },
  other: defaultGroupMeta,
}

/**
 * 生成配置项的稳定键，用于对比当前值和原始值。
 */
function getConfigIdentity(config: API.SysConfig) {
  return `${config.id ?? 'new'}:${config.configKey}`
}

/**
 * 判断配置项所属分组，方便按主题展示。
 */
function resolveGroupKey(configKey: string) {
  const key = configKey.toLowerCase()

  if (key.includes('upload') || key.includes('file') || key.includes('size'))
    return 'upload'

  if (key.includes('storage') || key.includes('oss') || key.includes('qiniu') || key.includes('local') || key.includes('path'))
    return 'storage'

  if (key.includes('allow') || key.includes('limit') || key.includes('safe') || key.includes('auth') || key.includes('token'))
    return 'security'

  if (key.includes('system') || key.includes('site') || key.includes('theme') || key.includes('cache'))
    return 'system'

  return 'other'
}

/**
 * 判断当前配置值是否已被修改。
 */
function isConfigChanged(config: API.SysConfig) {
  return originalValueMap.value[getConfigIdentity(config)] !== config.configValue
}

/**
 * 根据配置内容决定输入控件，长文本优先用多行输入。
 */
function isTextareaConfig(config: API.SysConfig) {
  const text = `${config.configKey} ${config.configName} ${config.remark || ''}`.toLowerCase()
  return text.includes('extension')
    || text.includes('path')
    || text.includes('remark')
    || text.includes('json')
    || (config.configValue?.length || 0) > 40
}

/**
 * 将接口返回值同步到页面，并记录原始值快照。
 */
function syncConfigs(list: API.SysConfig[]) {
  configs.value = list.map(item => ({ ...item }))
  originalValueMap.value = Object.fromEntries(
    list.map(item => [getConfigIdentity(item), item.configValue ?? '']),
  )
}

/**
 * 加载系统配置。
 */
async function loadConfigs() {
  loading.value = true
  try {
    const res = await getSystemConfigsApi()
    syncConfigs(res)
  }
  catch (error) {
    console.error('加载配置失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 保存单个配置项，并在成功后刷新该项的基线值。
 */
async function handleSave(config: API.SysConfig) {
  try {
    await updateConfigApi(config)
    originalValueMap.value[getConfigIdentity(config)] = config.configValue ?? ''
    message.success(`配置 [${config.configName}] 已更新`)
  }
  catch (error) {
    console.error('更新配置失败:', error)
  }
}

/**
 * 批量保存已修改的配置项，减少逐条操作成本。
 */
async function handleSaveChanged() {
  const changedItems = configs.value.filter(isConfigChanged)
  if (!changedItems.length) {
    message.info('当前没有待保存的修改')
    return
  }

  saving.value = true
  try {
    await Promise.all(changedItems.map(item => updateConfigApi(item)))
    changedItems.forEach((item) => {
      originalValueMap.value[getConfigIdentity(item)] = item.configValue ?? ''
    })
    message.success(`已保存 ${changedItems.length} 项配置`)
  }
  catch (error) {
    console.error('批量保存配置失败:', error)
  }
  finally {
    saving.value = false
  }
}

/**
 * 撤销单个配置项的本地修改。
 */
function handleReset(config: API.SysConfig) {
  config.configValue = originalValueMap.value[getConfigIdentity(config)] ?? ''
}

/**
 * 撤销全部本地修改。
 */
function handleResetChanged() {
  configs.value.forEach((config) => {
    if (isConfigChanged(config))
      handleReset(config)
  })
  message.success('已撤销未保存的修改')
}

const changedCount = computed(() => configs.value.filter(isConfigChanged).length)

const filteredConfigs = computed(() => {
  const search = keyword.value.trim().toLowerCase()

  return configs.value.filter((item) => {
    const matchKeyword = !search || [item.configName, item.configKey, item.remark, item.configValue]
      .filter(Boolean)
      .some(text => `${text}`.toLowerCase().includes(search))

    const matchMode = viewMode.value === 'all' || isConfigChanged(item)
    const groupKey = resolveGroupKey(item.configKey)
    const matchGroup = activeGroup.value === 'all' || activeGroup.value === groupKey

    return matchKeyword && matchMode && matchGroup
  })
})

const groupedConfigs = computed<ConfigGroup[]>(() => {
  const buckets = new Map<string, API.SysConfig[]>()

  filteredConfigs.value.forEach((item) => {
    const groupKey = resolveGroupKey(item.configKey)
    const list = buckets.get(groupKey) || []
    list.push(item)
    buckets.set(groupKey, list)
  })

  return Array.from(buckets.entries()).map(([key, items]) => ({
    key,
    items,
    title: groupMeta[key]?.title || defaultGroupMeta.title,
    desc: groupMeta[key]?.desc || defaultGroupMeta.desc,
    icon: groupMeta[key]?.icon || defaultGroupMeta.icon,
  }))
})

const groupTabs = computed(() => {
  const counts = configs.value.reduce<Record<string, number>>((acc, item) => {
    const key = resolveGroupKey(item.configKey)
    acc[key] = (acc[key] || 0) + 1
    return acc
  }, {})

  const tabs = Object.entries(groupMeta).map(([key, meta]) => ({
    key,
    title: meta.title,
    count: counts[key] || 0,
  }))

  return [
    {
      key: 'all',
      title: '全部配置',
      count: configs.value.length,
    },
    ...tabs,
  ]
})

onMounted(() => {
  loadConfigs()
})
</script>

<template>
  <div class="max-w-6xl mx-auto py-6 px-3 md:px-0 space-y-6">
    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-6 md:py-7 border-b border-slate-200/70 dark:border-slate-800">
        <div class="flex flex-col xl:flex-row xl:items-start xl:justify-between gap-5">
          <div class="space-y-3">
            <div>
              <h2 class="text-2xl font-bold text-slate-900 dark:text-slate-100">
                系统设置
              </h2>
              <p class="text-slate-500 dark:text-slate-400 mt-2 text-sm md:text-base max-w-3xl">
                把系统默认配置集中管理，支持快速筛选、分组查看和批量保存，减少在长列表里逐条查找的成本。
              </p>
            </div>

            <div class="flex flex-wrap gap-3">
              <div class="min-w-[140px] rounded-2xl bg-slate-100 dark:bg-slate-800 px-4 py-3">
                <div class="text-xs text-slate-500 dark:text-slate-400">
                  配置总数
                </div>
                <div class="mt-1 text-2xl font-semibold text-slate-900 dark:text-slate-100">
                  {{ configs.length }}
                </div>
              </div>
              <div class="min-w-[140px] rounded-2xl bg-amber-50 dark:bg-amber-500/10 px-4 py-3">
                <div class="text-xs text-amber-700 dark:text-amber-300">
                  待保存修改
                </div>
                <div class="mt-1 text-2xl font-semibold text-amber-600 dark:text-amber-300">
                  {{ changedCount }}
                </div>
              </div>
              <div class="min-w-[140px] rounded-2xl bg-emerald-50 dark:bg-emerald-500/10 px-4 py-3">
                <div class="text-xs text-emerald-700 dark:text-emerald-300">
                  当前分组
                </div>
                <div class="mt-1 text-lg font-semibold text-emerald-600 dark:text-emerald-300">
                  {{ groupTabs.find(item => item.key === activeGroup)?.title || '全部配置' }}
                </div>
              </div>
            </div>
          </div>

          <div class="flex flex-wrap gap-2.5 xl:justify-end">
            <a-button :loading="loading" class="toolbar-btn" @click="loadConfigs">
              <template #icon>
                <span class="i-fa6-solid:rotate text-[12px] leading-none" />
              </template>
              刷新配置
            </a-button>
            <a-button class="toolbar-btn" :disabled="changedCount === 0" @click="handleResetChanged">
              撤销修改
            </a-button>
            <a-button type="primary" class="toolbar-btn" :loading="saving" :disabled="changedCount === 0" @click="handleSaveChanged">
              保存全部修改
            </a-button>
          </div>
        </div>
      </div>

      <div class="px-5 md:px-7 py-5 border-b border-slate-200/70 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/40">
        <div class="flex flex-col gap-4">
          <div class="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_260px] gap-4">
            <a-input
              v-model:value="keyword"
              size="large"
              placeholder="搜索配置名称、配置键、说明或当前值"
              allow-clear
              class="min-w-0"
            >
              <template #prefix>
                <span class="i-fa6-solid:magnifying-glass text-slate-400" />
              </template>
            </a-input>

            <a-radio-group v-model:value="viewMode" size="large" button-style="solid" class="w-full min-w-0">
              <a-radio-button value="all" class="!text-center md:!min-w-[120px]">
                全部配置
              </a-radio-button>
              <a-radio-button value="changed" class="!text-center md:!min-w-[120px]">
                仅看已修改
              </a-radio-button>
            </a-radio-group>
          </div>

          <div class="-mx-1 overflow-x-auto pb-1">
            <div class="flex min-w-max gap-2 px-1">
              <button
                v-for="tab in groupTabs"
                :key="tab.key"
                class="shrink-0 whitespace-nowrap px-4 py-2 rounded-full text-sm border transition-colors"
                :class="activeGroup === tab.key
                  ? 'bg-slate-900 text-white border-slate-900 dark:bg-slate-100 dark:text-slate-900 dark:border-slate-100'
                  : 'bg-white text-slate-600 border-slate-200 hover:border-slate-300 dark:bg-slate-900 dark:text-slate-300 dark:border-slate-700 dark:hover:border-slate-500'"
                @click="activeGroup = tab.key"
              >
                {{ tab.title }} · {{ tab.count }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loading && configs.length === 0" class="py-24 text-center">
        <a-spin tip="正在获取系统配置..." />
      </div>

      <div v-else-if="groupedConfigs.length" class="p-5 md:p-7 space-y-6">
        <section
          v-for="group in groupedConfigs"
          :key="group.key"
          class="rounded-3xl border border-slate-200/70 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-950/20 overflow-hidden"
        >
          <div class="px-5 py-4 border-b border-slate-200/70 dark:border-slate-800 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
            <div class="flex items-start gap-3">
              <div class="w-11 h-11 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-700 dark:text-slate-200">
                <span :class="group.icon" class="text-lg" />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
                  {{ group.title }}
                </h3>
                <p class="text-sm text-slate-500 dark:text-slate-400 mt-1">
                  {{ group.desc }}
                </p>
              </div>
            </div>
            <div class="text-sm text-slate-500 dark:text-slate-400">
              共 {{ group.items.length }} 项
            </div>
          </div>

          <div class="p-4 md:p-5 grid grid-cols-1 2xl:grid-cols-2 gap-4">
            <div
              v-for="item in group.items"
              :key="item.id || item.configKey"
              class="rounded-2xl border p-4 transition-all"
              :class="isConfigChanged(item)
                ? 'border-amber-300 bg-amber-50/60 dark:border-amber-500/40 dark:bg-amber-500/10'
                : 'border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900'"
            >
              <div class="flex items-start justify-between gap-4">
                <div class="min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <h4 class="text-base font-semibold text-slate-900 dark:text-slate-100">
                      {{ item.configName }}
                    </h4>
                    <span
                      v-if="isConfigChanged(item)"
                      class="inline-flex items-center rounded-full bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300 px-2 py-0.5 text-xs font-medium"
                    >
                      已修改
                    </span>
                  </div>
                  <code class="inline-flex mt-2 text-xs text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-1 rounded-lg break-all">
                    {{ item.configKey }}
                  </code>
                </div>

                <a-button type="text" size="small" class="compact-btn compact-btn-text" :disabled="!isConfigChanged(item)" @click="handleReset(item)">
                  撤销
                </a-button>
              </div>

              <p class="text-sm text-slate-500 dark:text-slate-400 mt-3 min-h-[40px]">
                {{ item.remark || '暂无详细说明，建议修改前先确认该配置的实际用途。' }}
              </p>

              <div class="mt-4">
                <a-textarea
                  v-if="isTextareaConfig(item)"
                  v-model:value="item.configValue"
                  :auto-size="{ minRows: 3, maxRows: 6 }"
                  placeholder="请输入配置值"
                  class="font-mono text-sm"
                />
                <a-input
                  v-else
                  v-model:value="item.configValue"
                  size="large"
                  placeholder="请输入配置值"
                  class="font-mono text-sm"
                />
              </div>

              <div class="mt-4 flex items-center justify-between gap-3">
                <div class="text-xs" :class="isConfigChanged(item) ? 'text-amber-700 dark:text-amber-300' : 'text-slate-400 dark:text-slate-500'">
                  {{ isConfigChanged(item) ? '当前值与已生效配置不同，尚未保存。' : '当前值已与服务端配置保持一致。' }}
                </div>
                <a-button type="primary" class="compact-btn" :disabled="!isConfigChanged(item)" @click="handleSave(item)">
                  保存
                </a-button>
              </div>
            </div>
          </div>
        </section>
      </div>

      <div v-else class="py-24">
        <a-empty description="当前筛选条件下没有配置项" />
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-input),
:deep(.ant-input-affix-wrapper),
:deep(.ant-input-textarea textarea) {
  border-radius: 14px;
}

:deep(.ant-radio-group) {
  display: flex;
}

:deep(.ant-radio-button-wrapper) {
  flex: 1;
}

:deep(.toolbar-btn) {
  height: 36px;
  padding-inline: 14px;
  border-radius: 12px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

:deep(.toolbar-btn .ant-btn-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

:deep(.compact-btn) {
  height: 34px;
  padding-inline: 14px;
  border-radius: 10px;
  font-size: 13px;
}

:deep(.compact-btn.compact-btn-text) {
  padding-inline: 8px;
}
</style>
