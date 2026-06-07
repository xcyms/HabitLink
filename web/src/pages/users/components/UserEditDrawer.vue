<script setup lang="ts">
import { message } from 'ant-design-vue'
import { updateUserByAdminApi } from '../../../api/user.ts'
import type { User } from '../../../types'

interface Props {
  visible: boolean
  user?: User | null
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

const loading = ref(false)
const form = ref<User>({
  id: 0,
  username: '',
  nickname: '',
  email: '',
  phone: '',
  status: 0,
  roles: [],
})

watch(() => props.user, (val) => {
  if (!val)
    return
  form.value = {
    id: val.id || 0,
    username: val.username || '',
    nickname: val.nickname || '',
    email: val.email || '',
    phone: val.phone || '',
    status: val.status ?? 0,
    roles: val.roles || [],
  }
}, { immediate: true })

async function handleSave() {
  if (!form.value.nickname) {
    message.error('请输入用户昵称')
    return
  }

  try {
    loading.value = true
    await updateUserByAdminApi(form.value)
    message.success('用户信息更新成功')
    drawerVisible.value = false
    emit('success')
  }
  catch (error) {
    console.error('更新用户信息失败:', error)
  }
  finally {
    loading.value = false
  }
}

function handleCancel() {
  drawerVisible.value = false
}
</script>

<template>
  <a-drawer
    v-model:open="drawerVisible"
    title="编辑用户信息"
    width="500"
    :closable="false"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item label="用户名">
        <a-input v-model:value="form.username" disabled />
      </a-form-item>
      <a-form-item label="用户昵称" required>
        <a-input v-model:value="form.nickname" placeholder="请输入用户昵称" />
      </a-form-item>
      <a-form-item label="邮箱">
        <a-input v-model:value="form.email" placeholder="请输入邮箱" />
      </a-form-item>
      <a-form-item label="手机号">
        <a-input v-model:value="form.phone" placeholder="请输入手机号" />
      </a-form-item>
      <a-form-item label="用户状态">
        <DictSelect
          v-model:value="form.status"
          dict-type="sys_common_status"
        />
      </a-form-item>
    </a-form>
    <template #footer>
      <a-space>
        <a-button @click="handleCancel">
          取消
        </a-button>
        <a-button type="primary" :loading="loading" @click="handleSave">
          保存
        </a-button>
      </a-space>
    </template>
  </a-drawer>
</template>
