<script setup lang="ts">
import { message } from 'ant-design-vue'
import { createDictApi, updateDictApi } from '../../../api/dict.ts'
import type { Dict } from '../../../types'

interface Props {
  visible: boolean
  dictData?: Dict | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}>()

const drawerVisible = computed({
  get: () => props.visible,
  set: val => emit('update:visible', val),
})

const drawerLoading = ref(false)
const form = ref({
  id: undefined as number | undefined,
  dictName: '',
  dictType: '',
  status: 0,
  remark: '',
})

// 监听编辑数据变化
watch(() => props.dictData, (val) => {
  if (val) {
    form.value = {
      id: val.id,
      dictName: val.dictName,
      dictType: val.dictType,
      status: val.status ?? 0,
      remark: val.remark || '',
    }
  }
  else {
    form.value = {
      id: undefined,
      dictName: '',
      dictType: '',
      status: 0,
      remark: '',
    }
  }
}, { immediate: true })

const isEdit = computed(() => !!form.value.id)
const title = computed(() => isEdit.value ? '编辑字典类型' : '新增字典类型')

// 保存字典类型
async function handleSave() {
  if (!form.value.dictName) {
    message.error('请输入字典名称')
    return
  }
  if (!form.value.dictType) {
    message.error('请输入字典类型')
    return
  }

  try {
    drawerLoading.value = true
    if (isEdit.value) {
      await updateDictApi(form.value)
      message.success('更新成功')
    }
    else {
      await createDictApi(form.value)
      message.success('创建成功')
    }
    drawerVisible.value = false
    emit('success')
  }
  catch (error) {
    console.error('保存字典类型失败:', error)
  }
  finally {
    drawerLoading.value = false
  }
}

function handleCancel() {
  drawerVisible.value = false
}
</script>

<template>
  <a-drawer
    v-model:open="drawerVisible"
    :title="title"
    width="500"
    :closable="false"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item label="字典名称" required>
        <a-input v-model:value="form.dictName" placeholder="请输入字典名称，如：用户状态" />
      </a-form-item>
      <a-form-item label="字典类型" required>
        <a-input v-model:value="form.dictType" placeholder="请输入字典类型，如：sys_user_status" />
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
        <a-button @click="handleCancel">
          取消
        </a-button>
        <a-button type="primary" :loading="drawerLoading" @click="handleSave">
          保存
        </a-button>
      </a-space>
    </template>
  </a-drawer>
</template>
