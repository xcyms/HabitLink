<script setup lang="ts">
import type { UploadFile as AntdUploadFile } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { deleteBatchFilesApi, deleteFileApi, downloadFileApi, getFileListApi, uploadFileApi } from '../../api/file.ts'
import { getMyConfigsApi } from '../../api/config.ts'
import type * as API from '../../types'
import { formatSize, getImageUrl } from '../../utils/common.ts'

const loading = ref(false)
const fileList = ref<API.PageResult<API.File>>({
  records: [],
  total: 0,
  size: 0,
  current: 0,
})
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)

const searchForm = ref({
  name: '',
  type: '',
})

const uploadLoading = ref(false)
const antdFileList = ref<AntdUploadFile[]>([])
const configs = ref<API.SysConfig[]>([])

const allowedExtensions = computed(() => {
  const config = configs.value.find(c => c.configKey === 'allowed_extensions')
  return config ? config.configValue.split(',').map(s => s.trim().toLowerCase()) : []
})

const maxFileSize = computed(() => {
  const config = configs.value.find(c => c.configKey === 'max_file_size')
  return config ? Number.parseInt(config.configValue) : 2 * 1024 * 1024
})

const maxFileSizeText = computed(() => formatSize(Number(maxFileSize.value)))
const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp']

const previewVisible = ref(false)
const previewImage = ref('')

const selectedKeys = ref<number[]>([])
const detailVisible = ref(false)
const currentFile = ref<API.File | null>(null)

function handlePreview(url: string) {
  previewImage.value = getImageUrl(url)
  previewVisible.value = true
}

function openDetail(record: API.File) {
  currentFile.value = record
  detailVisible.value = true
}

async function loadFiles(page = current.value, size = pageSize.value) {
  loading.value = true
  try {
    const res = await getFileListApi(
      { current: Number(page), size: Number(size) },
      { name: searchForm.value.name, type: searchForm.value.type },
    )
    fileList.value = res
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载文件列表失败:', error)
  }
  finally {
    loading.value = false
  }
}

async function loadAllowedExtensions() {
  try {
    configs.value = await getMyConfigsApi()
  }
  catch (error) {
    console.error('加载文件上传配置失败:', error)
  }
}

function handleSearch() {
  current.value = 1
  loadFiles()
}

function handleReset() {
  searchForm.value = {
    name: '',
    type: '',
  }
  current.value = 1
  loadFiles()
}

function handleTableChange(pagination: any) {
  loadFiles(pagination.current, pagination.pageSize)
}

function beforeUpload(file: AntdUploadFile) {
  const isLtMaxSize = file.size! <= Number(maxFileSize.value)
  if (!isLtMaxSize) {
    message.error(`文件大小不能超过 ${maxFileSizeText.value}`)
    return false
  }

  const ext = (file.name?.split('.').pop() || '').toLowerCase()
  if (!allowedExtensions.value.includes(ext)) {
    message.error(`不支持的文件类型: ${ext}`)
    return false
  }

  return true
}

async function customRequest(options: any) {
  const { file, onSuccess, onError } = options
  uploadLoading.value = true
  try {
    const res = await uploadFileApi(file as File)
    message.success('上传成功')
    loadFiles()
    onSuccess?.(res)
  }
  catch (error: any) {
    message.error(error.message || '上传失败')
    onError?.(error)
  }
  finally {
    uploadLoading.value = false
  }
}

async function handleDownload(record: API.File) {
  try {
    const response = await downloadFileApi(record.id!)
    const blob = response.data instanceof Blob ? response.data : new Blob([response.data])

    let fileName = record.name || 'download'
    const contentDisposition = response.headers['content-disposition']
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename\*?=['"]?(?:UTF-8'')?([^'";]+)['"]?/)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1])
      }
    }

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }
  catch (error: any) {
    if (error.message !== '下载失败') {
      console.error('下载文件失败:', error)
    }
  }
}

async function handleDownloadBatch() {
  if (selectedKeys.value.length === 0) {
    message.warning('请先选择要下载的文件')
    return
  }

  const selectedFiles = fileList.value.records.filter(file => selectedKeys.value.includes(file.id!))
  for (const file of selectedFiles) {
    // 顺序下载，避免浏览器同时弹出太多下载请求
    await handleDownload(file)
  }
  message.success(`已发起 ${selectedFiles.length} 个文件下载`)
}

async function handleDelete(id: number) {
  try {
    await deleteFileApi(id)
    message.success('删除成功')
    loadFiles()
  }
  catch (error) {
    console.error('删除文件失败:', error)
  }
}

async function handleDeleteBatch() {
  if (selectedKeys.value.length === 0) {
    message.warning('请先选择要删除的文件')
    return
  }

  try {
    await deleteBatchFilesApi(selectedKeys.value)
    message.success('批量删除成功')
    selectedKeys.value = []
    loadFiles()
  }
  catch (error) {
    console.error('批量删除文件失败:', error)
  }
}

async function handleCopyLink(record: API.File) {
  const url = getImageUrl(record.url)
  try {
    await navigator.clipboard.writeText(url)
    message.success('链接已复制')
  }
  catch (error) {
    console.error('复制链接失败:', error)
    message.error('复制失败')
  }
}

function handleSelectChange(keys: number[]) {
  selectedKeys.value = keys
}

function handleActionMenuClick(record: API.File, key: string) {
  if (key === 'detail') {
    openDetail(record)
    return
  }
  if (key === 'copy') {
    handleCopyLink(record)
    return
  }
  if (key === 'delete' && record.id) {
    handleDelete(record.id)
  }
}

onMounted(() => {
  loadFiles()
  loadAllowedExtensions()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 px-3 md:px-0">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h2 class="text-xl md:text-2xl font-bold text-gray-800 dark:text-gray-200">
          文件管理
        </h2>
        <p class="text-gray-500 dark:text-gray-400 mt-1 text-sm">
          管理系统文件，支持预览、批量下载、复制链接和详情查看。
        </p>
        <div v-if="allowedExtensions.length > 0 || maxFileSize" class="mt-2 text-xs text-gray-500 dark:text-gray-400 flex flex-wrap gap-4">
          <span v-if="allowedExtensions.length > 0">
            <span class="mr-2">允许类型：</span>
            <span class="inline-flex gap-1 flex-wrap">
              <span
                v-for="ext in allowedExtensions"
                :key="ext"
                class="px-2 py-0.5 bg-gray-100 dark:bg-gray-800 rounded text-gray-700 dark:text-gray-300"
              >
                {{ ext }}
              </span>
            </span>
          </span>
          <span v-if="maxFileSize" class="flex items-center gap-1">
            <span class="i-fa6-solid:file-size text-[12px]" />
            <span>大小上限：</span>
            <span class="px-2 py-0.5 bg-blue-50 dark:bg-blue-900/30 rounded text-blue-600 dark:text-blue-400 font-medium">
              {{ maxFileSizeText }}
            </span>
          </span>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <a-button class="flex items-center gap-1.5" @click="handleReset">
          重置
        </a-button>
        <a-button type="primary" :loading="loading" class="flex items-center gap-1.5" @click="handleSearch">
          <template #icon>
            <span class="i-fa6-solid:magnifying-glass text-[13px]" />
          </template>
          搜索
        </a-button>
        <a-upload
          v-model:file-list="antdFileList"
          :before-upload="beforeUpload"
          :show-upload-list="false"
          :custom-request="customRequest"
          :accept="allowedExtensions.map(ext => `.${ext}`).join(',')"
        >
          <a-button type="primary" :loading="uploadLoading" class="flex items-center gap-1.5">
            <template #icon>
              <span class="i-fa6-solid:upload text-[13px]" />
            </template>
            上传文件
          </a-button>
        </a-upload>
      </div>
    </div>

    <div class="bg-white dark:bg-[#141414] p-4 md:p-6 rounded-xl border border-gray-100 dark:border-gray-800 shadow-sm dark:shadow-none mb-6 transition-colors duration-300">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="文件名">
          <a-input v-model:value="searchForm.name" placeholder="请输入文件名" allow-clear @press-enter="handleSearch" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="searchForm.type" placeholder="请选择文件类型" allow-clear style="width: 150px" @change="handleSearch">
            <a-select-option
              v-for="ext in allowedExtensions"
              :key="ext"
              :value="ext"
            >
              {{ ext.toUpperCase() }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </div>

    <div class="bg-white dark:bg-[#141414] rounded-xl border border-gray-100 dark:border-gray-800 shadow-sm dark:shadow-none overflow-hidden transition-colors duration-300">
      <a-table
        :data-source="fileList.records"
        :loading="loading"
        :pagination="{
          current,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条记录`,
        }"
        row-key="id"
        :row-selection="{
          selectedRowKeys: selectedKeys,
          onChange: (keys: (string | number)[]) => handleSelectChange(keys as number[]),
        }"
        :scroll="{ x: 1100 }"
        class="custom-table"
        @change="handleTableChange"
      >
        <a-table-column key="preview" title="预览" width="96">
          <template #default="{ record }">
            <div
              v-if="imageExtensions.includes(record.type?.toLowerCase() || '')"
              class="w-14 h-14 rounded-lg overflow-hidden bg-gray-100 dark:bg-gray-800 cursor-pointer hover:opacity-80 transition-opacity"
              @click="handlePreview(record.url)"
            >
              <img
                :src="getImageUrl(record.url)"
                alt="预览"
                class="w-full h-full object-cover"
                @error="($event.target as HTMLImageElement).style.display = 'none'"
              >
            </div>
            <div v-else class="w-14 h-14 rounded-lg flex items-center justify-center bg-gray-100 dark:bg-gray-800 text-gray-400">
              <span class="i-fa6-solid:file text-xl" />
            </div>
          </template>
        </a-table-column>

        <a-table-column key="name" title="文件名" data-index="name" width="220">
          <template #default="{ record }">
            <div class="max-w-[200px] truncate font-medium text-gray-800 dark:text-gray-200" :title="record.name">
              {{ record.name }}
            </div>
          </template>
        </a-table-column>
        <a-table-column key="type" title="类型" data-index="type" width="90">
          <template #default="{ record }">
            <a-tag>{{ (record.type || '-').toUpperCase() }}</a-tag>
          </template>
        </a-table-column>
        <a-table-column key="size" title="大小" width="110">
          <template #default="{ record }">
            {{ formatSize(record.size || 0) }}
          </template>
        </a-table-column>
        <a-table-column key="url" title="访问地址" data-index="url" width="320">
          <template #default="{ record }">
            <div class="max-w-[300px] truncate text-gray-500 dark:text-gray-400" :title="getImageUrl(record.url)">
              {{ getImageUrl(record.url) }}
            </div>
          </template>
        </a-table-column>
        <a-table-column key="createTime" title="上传时间" data-index="createTime" width="180" />
        <a-table-column key="action" title="操作" align="center" width="180" fixed="right">
          <template #default="{ record }">
            <a-space :size="4">
              <a-button type="link" size="small" class="flex items-center" @click="handleDownload(record)">
                <template #icon>
                  <span class="i-fa6-solid:download text-[13px] inline-block align-middle" />
                </template>
                下载
              </a-button>
              <a-dropdown>
                <a-button type="link" size="small" class="flex items-center">
                  <template #icon>
                    <span class="i-fa6-solid:ellipsis-vertical text-[13px] inline-block align-middle" />
                  </template>
                  更多
                </a-button>
                <template #overlay>
                  <a-menu @click="({ key }) => handleActionMenuClick(record, String(key))">
                    <a-menu-item key="detail">
                      查看详情
                    </a-menu-item>
                    <a-menu-item key="copy">
                      复制链接
                    </a-menu-item>
                    <a-menu-item key="delete" danger>
                      删除文件
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <div v-if="selectedKeys.length > 0" class="fixed bottom-6 right-6 z-50">
      <div class="bg-gray-800 dark:bg-white text-white dark:text-gray-800 px-5 py-3 rounded-lg shadow-lg flex items-center gap-3">
        <span class="text-sm font-medium">
          已选择 {{ selectedKeys.length }} 个文件
        </span>
        <a-button type="primary" size="small" @click="handleDownloadBatch">
          批量下载
        </a-button>
        <a-button type="primary" danger size="small" @click="handleDeleteBatch">
          批量删除
        </a-button>
        <a-button size="small" @click="selectedKeys = []">
          取消
        </a-button>
      </div>
    </div>

    <a-drawer
      v-model:open="detailVisible"
      title="文件详情"
      placement="right"
      width="520"
    >
      <div v-if="currentFile" class="space-y-6">
        <div class="flex items-start gap-4">
          <div
            v-if="imageExtensions.includes(currentFile.type?.toLowerCase() || '')"
            class="w-24 h-24 rounded-xl overflow-hidden bg-gray-100 dark:bg-gray-800"
          >
            <img :src="getImageUrl(currentFile.url)" alt="文件预览" class="w-full h-full object-cover">
          </div>
          <div v-else class="w-24 h-24 rounded-xl flex items-center justify-center bg-gray-100 dark:bg-gray-800 text-gray-400">
            <span class="i-fa6-solid:file text-3xl" />
          </div>
          <div class="min-w-0 flex-1">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-gray-200 break-all">
              {{ currentFile.name }}
            </h3>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-2">
              {{ (currentFile.type || '-').toUpperCase() }} · {{ formatSize(currentFile.size || 0) }}
            </p>
          </div>
        </div>

        <a-descriptions bordered :column="1" size="small">
          <a-descriptions-item label="文件 ID">
            {{ currentFile.id }}
          </a-descriptions-item>
          <a-descriptions-item label="业务类型">
            {{ currentFile.businessType || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="业务 ID">
            {{ currentFile.businessId || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="上传时间">
            {{ currentFile.createTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="访问地址">
            <div class="break-all text-blue-500">
              {{ getImageUrl(currentFile.url) }}
            </div>
          </a-descriptions-item>
        </a-descriptions>

        <div class="flex gap-3">
          <a-button type="primary" @click="handleDownload(currentFile)">
            下载文件
          </a-button>
          <a-button @click="handleCopyLink(currentFile)">
            复制链接
          </a-button>
        </div>
      </div>
    </a-drawer>

    <a-image
      :preview="{
        visible: previewVisible,
        onVisibleChange: (visible: boolean) => previewVisible = visible,
      }"
      :src="previewImage"
      style="display: none;"
    />
  </div>
</template>

<style scoped>
:deep(.custom-table .ant-table-thead > tr > th) {
  font-size: 13px;
  font-weight: 600;
}

:deep(.custom-table .ant-table-cell) {
  padding: 14px 8px;
  font-size: 13px;
}

:global(html:not(.dark)) :deep(.custom-table .ant-table-thead > tr > th) {
  color: rgba(0, 0, 0, 0.85);
  background-color: #fafafa;
}

:global(html:not(.dark)) :deep(.custom-table .ant-table-tbody > tr:hover > td) {
  background-color: #fafafa;
}

:global(html.dark) :deep(.custom-table .ant-table-thead > tr > th) {
  color: rgba(255, 255, 255, 0.85);
  background-color: #1d1d1d;
}

:global(html.dark) :deep(.custom-table .ant-table-tbody > tr:hover > td) {
  background-color: #1d1d1d;
}
</style>
