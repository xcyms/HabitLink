<script setup lang="ts">
import { Modal, message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { getUserConfigsApi, updateConfigApi } from '../../api/config.ts'
import { assignUserRolesApi, getRoleListApi } from '../../api/role.ts'
import { getUserListApi, resetPasswordByAdminApi, updateUserByAdminApi } from '../../api/user.ts'
import type * as API from '../../types'
import { getImageUrl, validatePassword } from '../../utils/common.ts'
import UserEditDrawer from './components/UserEditDrawer.vue'

const loading = ref(false)
const userList = ref<API.PageResult<API.User>>({
  records: [],
  total: 0,
  size: 0,
  current: 0,
})
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const currentUser = ref<API.User | null>(null)
const userConfigs = ref<API.SysConfig[]>([])

const roleDrawerVisible = ref(false)
const roleDrawerLoading = ref(false)
const assignUser = ref<API.User | null>(null)
const availableRoles = ref<API.Role[]>([])
const selectedRoleIds = ref<number[]>([])
const checkboxMap = ref<Record<number, boolean>>({})

const editDrawerVisible = ref(false)
const editUser = ref<API.User | null>(null)

const resetPasswordVisible = ref(false)
const resetPasswordLoading = ref(false)
const resetPasswordForm = ref({
  password: '',
  confirmPassword: '',
})
const resetPasswordFormRef = ref()
const resetPasswordUser = ref<API.User | null>(null)

const resetPasswordRules: Record<string, Rule[]> = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: async (_rule: any, value: string) => {
        if (!value)
          return Promise.resolve()
        const errorMsg = validatePassword(value)
        if (errorMsg)
          return Promise.reject(new Error(errorMsg))
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      validator: async (_rule: any, value: string) => {
        if (!value)
          return Promise.reject(new Error('请再次输入密码'))
        if (value !== resetPasswordForm.value.password)
          return Promise.reject(new Error('两次输入的密码不一致'))
        return Promise.resolve()
      },
      trigger: 'change',
    },
  ],
}

const searchForm = ref({
  username: '',
  nickname: '',
  status: undefined as number | undefined,
})

async function loadUsers(page = current.value, size = pageSize.value) {
  loading.value = true
  try {
    const res = await getUserListApi(
      { current: Number(page), size: Number(size) },
      {
        username: searchForm.value.username,
        nickname: searchForm.value.nickname,
        status: searchForm.value.status,
      },
    )
    userList.value = res
    total.value = Number(res.total)
    current.value = Number(res.current)
    pageSize.value = Number(res.size)
  }
  catch (error) {
    console.error('加载用户列表失败:', error)
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  current.value = 1
  loadUsers()
}

function handleReset() {
  searchForm.value = {
    username: '',
    nickname: '',
    status: undefined,
  }
  current.value = 1
  loadUsers()
}

function handleTableChange(pagination: any) {
  loadUsers(pagination.current, pagination.pageSize)
}

async function handleOpenConfigs(user: API.User) {
  currentUser.value = user
  drawerVisible.value = true
  drawerLoading.value = true
  try {
    const configs = await getUserConfigsApi(user.id)
    const systemOnlyKeys = ['upload_path']
    userConfigs.value = configs.filter(item => !systemOnlyKeys.includes(item.configKey))
  }
  catch (error) {
    console.error('加载用户配置失败:', error)
  }
  finally {
    drawerLoading.value = false
  }
}

async function handleSaveConfig(config: API.SysConfig) {
  try {
    config.userId = currentUser.value?.id
    await updateConfigApi(config)
    message.success(`用户 [${currentUser.value?.username}] 的配置已更新`)
  }
  catch (error) {
    console.error('更新用户配置失败:', error)
  }
}

async function handleOpenRoleAssign(user: API.User) {
  assignUser.value = user
  roleDrawerVisible.value = true
  roleDrawerLoading.value = true
  try {
    availableRoles.value = await getRoleListApi()
    const currentRoleIds = user.roles?.map((role) => {
      const roleObj = availableRoles.value.find(r => r.roleKey === role)
      return roleObj?.id
    }).filter(Boolean) as number[] || []
    selectedRoleIds.value = currentRoleIds
    checkboxMap.value = {}
    availableRoles.value.forEach((role) => {
      checkboxMap.value[role.id as number] = currentRoleIds.includes(role.id as number)
    })
  }
  catch (error) {
    console.error('加载角色列表失败:', error)
  }
  finally {
    roleDrawerLoading.value = false
  }
}

async function handleSaveRoles() {
  if (!assignUser.value)
    return
  try {
    await assignUserRolesApi(assignUser.value.id, selectedRoleIds.value)
    message.success(`用户 [${assignUser.value.username}] 的角色已更新`)
    roleDrawerVisible.value = false
    loadUsers()
  }
  catch (error) {
    console.error('保存角色失败:', error)
  }
}

function handleRoleChange(roleId: number, checked: boolean) {
  if (checked) {
    if (!selectedRoleIds.value.includes(roleId))
      selectedRoleIds.value.push(roleId)
    checkboxMap.value[roleId] = true
  }
  else {
    selectedRoleIds.value = selectedRoleIds.value.filter(id => id !== roleId)
    checkboxMap.value[roleId] = false
  }
}

function handleEditUser(record: API.User) {
  editUser.value = record
  editDrawerVisible.value = true
}

function handleEditSuccess() {
  loadUsers()
}

function handleToggleStatus(record: API.User) {
  const nextStatus = record.status === 1 ? 0 : 1
  Modal.confirm({
    title: nextStatus === 0 ? '启用用户' : '禁用用户',
    content: `确认要将用户 ${record.nickname || record.username} 设为${nextStatus === 0 ? '启用' : '禁用'}吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      await updateUserByAdminApi({
        ...record,
        status: nextStatus,
      })
      message.success('用户状态已更新')
      loadUsers()
    },
  })
}

function handleResetPassword(record: API.User) {
  resetPasswordUser.value = record
  resetPasswordForm.value = {
    password: '',
    confirmPassword: '',
  }
  resetPasswordVisible.value = true
}

function handleActionMenuClick(record: API.User, key: string) {
  if (key === 'config') {
    handleOpenConfigs(record)
    return
  }
  if (key === 'role') {
    handleOpenRoleAssign(record)
    return
  }
  if (key === 'status') {
    handleToggleStatus(record)
    return
  }
  if (key === 'password')
    handleResetPassword(record)
}

async function handleSubmitResetPassword() {
  if (!resetPasswordUser.value)
    return

  try {
    await resetPasswordFormRef.value?.validate()
  }
  catch (error) {
    message.error('重置密码失败')
    console.error(error)
    return
  }

  resetPasswordLoading.value = true
  try {
    await resetPasswordByAdminApi({
      id: resetPasswordUser.value.id,
      password: resetPasswordForm.value.password,
    })
    message.success('密码重置成功')
    resetPasswordVisible.value = false
  }
  catch (error) {
    console.error('重置密码失败:', error)
  }
  finally {
    resetPasswordLoading.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <div class="max-w-6xl mx-auto py-6 px-3 md:px-0">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h2 class="text-xl md:text-2xl font-bold text-gray-800 dark:text-gray-200">
          用户管理
        </h2>
        <p class="text-gray-500 dark:text-gray-400 mt-1 text-sm">
          查看系统注册用户，并为其配置个性化的存储策略和权限。
        </p>
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
      </div>
    </div>

    <div class="bg-white dark:bg-[#141414] p-4 md:p-6 rounded-xl border border-gray-100 dark:border-gray-800 shadow-sm dark:shadow-none mb-6 transition-colors duration-300">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allow-clear @press-enter="handleSearch" />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="searchForm.nickname" placeholder="请输入用户昵称" allow-clear @press-enter="handleSearch" />
        </a-form-item>
        <a-form-item label="状态">
          <DictSelect
            v-model:value="searchForm.status"
            dict-type="sys_common_status"
            placeholder="请选择状态"
            style="width: 140px"
            allow-clear
            @change="handleSearch"
          />
        </a-form-item>
      </a-form>
    </div>

    <div class="bg-white dark:bg-[#141414] rounded-xl border border-gray-100 dark:border-gray-800 shadow-sm dark:shadow-none overflow-hidden transition-colors duration-300">
      <a-table
        :data-source="userList.records"
        :loading="loading"
        :pagination="{
          current,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条记录`,
        }"
        row-key="id"
        @change="handleTableChange"
      >
        <a-table-column key="id" title="用户ID" data-index="id" width="80" />
        <a-table-column key="user" title="用户信息">
          <template #default="{ record }">
            <div class="flex items-center gap-3">
              <a-avatar :src="getImageUrl(record.avatar)" :size="40" class="flex-shrink-0">
                <template #icon>
                  <div class="i-fa6-solid:user" />
                </template>
              </a-avatar>
              <div class="flex flex-col">
                <span class="font-bold text-gray-800 dark:text-gray-200">{{ record.nickname }}</span>
                <span class="text-xs text-gray-400 dark:text-gray-500">@{{ record.username }}</span>
              </div>
            </div>
          </template>
        </a-table-column>
        <a-table-column key="roles" title="角色" data-index="roles">
          <template #default="{ text }">
            <div class="flex gap-1 flex-wrap">
              <a-tag v-for="role in text" :key="role" :color="role === 'ADMIN' ? 'red' : 'blue'">
                {{ role }}
              </a-tag>
            </div>
          </template>
        </a-table-column>
        <a-table-column key="status" title="状态" data-index="status" width="110">
          <template #default="{ text }">
            <a-tag :color="text === 0 ? 'green' : 'red'">
              {{ text === 0 ? '启用' : '禁用' }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column key="contact" title="联系方式">
          <template #default="{ record }">
            <div class="text-xs space-y-1">
              <div class="flex items-center gap-1.5 text-gray-500 dark:text-gray-400">
                <span class="i-fa6-solid:envelope text-[10px]" /> {{ record.email || '-' }}
              </div>
              <div class="flex items-center gap-1.5 text-gray-400 dark:text-gray-500">
                <span class="i-fa6-solid:phone text-[10px]" /> {{ record.phone || '-' }}
              </div>
            </div>
          </template>
        </a-table-column>
        <a-table-column key="action" title="操作" align="center" width="200">
          <template #default="{ record }">
            <a-space :size="4">
              <a-button type="link" class="flex items-center gap-1.5" @click="handleEditUser(record)">
                <template #icon>
                  <span class="i-fa6-solid:pen-to-square text-[13px] inline-block align-middle" />
                </template>
                编辑
              </a-button>
              <a-dropdown>
                <a-button type="link" class="flex items-center gap-1.5">
                  <template #icon>
                    <span class="i-fa6-solid:ellipsis-vertical text-[13px] inline-block align-middle" />
                  </template>
                  更多
                </a-button>
                <template #overlay>
                  <a-menu @click="({ key }) => handleActionMenuClick(record, String(key))">
                    <a-menu-item key="config">
                      配置管理
                    </a-menu-item>
                    <a-menu-item key="role">
                      角色分配
                    </a-menu-item>
                    <a-menu-item key="status">
                      {{ record.status === 0 ? '禁用用户' : '启用用户' }}
                    </a-menu-item>
                    <a-menu-item key="password">
                      重置密码
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </div>

    <a-drawer
      v-model:open="drawerVisible"
      :title="`配置管理 - ${currentUser?.nickname}`"
      placement="right"
      width="500"
    >
      <div v-if="drawerLoading" class="py-20 text-center">
        <a-spin tip="正在拉取用户个性化配置..." />
      </div>
      <div v-else class="space-y-6">
        <a-alert type="info" show-icon class="mb-4">
          <template #message>
            <div class="text-xs">
              在此修改的配置将仅对该用户生效。
            </div>
          </template>
        </a-alert>

        <div v-for="item in userConfigs" :key="item.configKey" class="p-4 rounded-lg border border-gray-100 dark:border-gray-800 bg-gray-50/30 dark:bg-gray-800/30 space-y-3 transition-colors duration-300">
          <div class="flex items-center justify-between">
            <div class="flex flex-col">
              <span class="font-bold text-gray-700 dark:text-gray-200">{{ item.configName }}</span>
              <code class="text-[10px] text-blue-500">{{ item.configKey }}</code>
            </div>
            <a-button type="primary" size="small" @click="handleSaveConfig(item)">
              应用设置
            </a-button>
          </div>
          <a-input v-model:value="item.configValue" placeholder="请输入配置值" class="font-mono" />
        </div>
      </div>
    </a-drawer>

    <a-drawer
      v-model:open="roleDrawerVisible"
      title="角色分配"
      placement="right"
      width="500"
    >
      <div v-if="roleDrawerLoading" class="py-20 text-center">
        <a-spin tip="正在加载角色列表..." />
      </div>
      <div v-else class="space-y-6">
        <a-alert type="info" show-icon class="mb-4">
          <template #message>
            <div class="text-xs">
              为用户 <b>{{ assignUser?.nickname }}</b> 分配角色。
            </div>
          </template>
        </a-alert>

        <div class="space-y-3">
          <a-checkbox
            v-for="role in availableRoles"
            :key="role.id"
            v-model:checked="checkboxMap[role.id as number]"
            @change="(e: any) => handleRoleChange(role.id as number, e.target.checked)"
          >
            <div class="flex items-center gap-2">
              <span class="font-medium">{{ role.roleName }}</span>
              <code class="text-xs text-gray-500">{{ role.roleKey }}</code>
            </div>
          </a-checkbox>
        </div>

        <div class="flex justify-end gap-3 pt-4 border-t border-gray-100 dark:border-gray-800">
          <a-button @click="roleDrawerVisible = false">
            取消
          </a-button>
          <a-button type="primary" @click="handleSaveRoles">
            保存
          </a-button>
        </div>
      </div>
    </a-drawer>

    <UserEditDrawer
      v-model:visible="editDrawerVisible"
      :user="editUser"
      @success="handleEditSuccess"
    />

    <a-modal
      v-model:open="resetPasswordVisible"
      title="重置密码"
      :confirm-loading="resetPasswordLoading"
      ok-text="确认重置"
      cancel-text="取消"
      @ok="handleSubmitResetPassword"
    >
      <div class="space-y-4 pt-2">
        <a-alert type="warning" show-icon>
          <template #message>
            为用户 <b>{{ resetPasswordUser?.nickname || resetPasswordUser?.username }}</b> 设置新密码。
          </template>
        </a-alert>
        <a-form
          ref="resetPasswordFormRef"
          :model="resetPasswordForm"
          :rules="resetPasswordRules"
          layout="vertical"
        >
          <a-form-item label="新密码" name="password">
            <a-input-password v-model:value="resetPasswordForm.password" placeholder="请输入新密码" />
          </a-form-item>
          <a-form-item label="确认新密码" name="confirmPassword">
            <a-input-password v-model:value="resetPasswordForm.confirmPassword" placeholder="请再次输入新密码" />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>
