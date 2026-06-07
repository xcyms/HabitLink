<script setup lang="ts">
import type { UploadChangeParam } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useUserStore } from '../../store/user.ts'
import { logoutApi, updatePasswordApi, updateProfileApi } from '../../api/user.ts'
import { getImageUrl, validatePassword } from '../../utils/common.ts'

const userStore = useUserStore()
const router = useRouter()
const activeKey = ref('1')
const loading = ref(false)
const uploadUrl = `${import.meta.env.VITE_API_BASE_URL}/file/upload`

/**
 * 个人资料表单。
 */
const profileForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
})

/**
 * 修改密码表单。
 */
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

/**
 * 初始化资料表单。
 */
function syncProfileForm() {
  const userInfo = userStore.userInfo as typeof userStore.userInfo & {
    nickname?: string
    email?: string
    phone?: string
  }

  profileForm.nickname = userInfo.nickname || ''
  profileForm.email = userInfo.email || ''
  profileForm.phone = userInfo.phone || ''
  profileForm.avatar = userInfo.avatar || ''
}

/**
 * 提交个人资料更新。
 */
async function handleUpdateProfile() {
  loading.value = true
  try {
    await updateProfileApi(profileForm)
    userStore.login({
      ...userStore.userInfo,
      ...profileForm,
    })
    message.success('个人资料更新成功')
  }
  catch (error) {
    console.error('更新个人资料失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 提交密码修改并退出当前登录。
 */
async function handleUpdatePassword() {
  loading.value = true
  try {
    await updatePasswordApi({
      oldPassword: passwordForm.oldPassword,
      password: passwordForm.newPassword,
    })
    message.success('密码修改成功，请重新登录')
    await logoutApi()
    userStore.logout()
    router.replace('/login')
  }
  catch (error) {
    console.error('更新密码失败:', error)
  }
  finally {
    loading.value = false
  }
}

/**
 * 处理头像上传结果。
 */
function handleAvatarChange(info: UploadChangeParam) {
  if (info.file.status === 'uploading') {
    loading.value = true
    return
  }

  if (info.file.status === 'done') {
    loading.value = false
    const response = info.file.response as { code: number, message?: string, data?: { url?: string } }
    if (response?.code === 200 && response.data?.url) {
      profileForm.avatar = response.data.url
      handleUpdateProfile()
    }
    else {
      message.error(response?.message || '头像上传失败')
    }
  }
  else if (info.file.status === 'error') {
    loading.value = false
    message.error('网络错误，头像上传失败')
  }
}

const profileSummary = computed(() => [
  {
    title: '当前账号',
    value: userStore.userInfo.username || '游客',
    hint: '登录用户名',
    icon: 'i-fa6-solid:user',
    tone: 'slate',
  },
  {
    title: '显示名称',
    value: profileForm.nickname || '未设置',
    hint: '用于前台或管理页显示',
    icon: 'i-fa6-solid:id-card',
    tone: 'emerald',
  },
  {
    title: '联系方式',
    value: profileForm.phone || '未设置',
    hint: profileForm.email || '建议至少补充一种联系方式',
    icon: 'i-fa6-solid:phone',
    tone: 'amber',
  },
  {
    title: '账号状态',
    value: userStore.token ? '已登录' : '未登录',
    hint: '当前会话凭证状态',
    icon: 'i-fa6-solid:shield-halved',
    tone: 'rose',
  },
])

onMounted(() => {
  syncProfileForm()
})
</script>

<template>
  <div class="max-w-6xl mx-auto py-6 px-3 md:px-0 space-y-6">
    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-6 md:py-7 border-b border-slate-200/70 dark:border-slate-800">
        <div class="flex flex-col xl:flex-row xl:items-start xl:justify-between gap-5">
          <div class="flex items-start gap-4">
            <a-avatar
              :size="72"
              :src="getImageUrl(profileForm.avatar || userStore.avatar)"
              class="shrink-0 ring-4 ring-slate-100 dark:ring-slate-800"
            />
            <div class="space-y-2">
              <div>
                <h2 class="text-2xl font-bold text-slate-900 dark:text-slate-100">
                  个人中心
                </h2>
                <p class="text-slate-500 dark:text-slate-400 mt-2 text-sm md:text-base max-w-3xl">
                  统一管理你的资料、头像和账号安全设置，重要账户信息集中在这里维护。
                </p>
              </div>
              <div class="flex flex-wrap gap-2">
                <div class="rounded-full border border-slate-200 bg-slate-50 px-3.5 py-1.5 text-[13px] text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
                  用户名: {{ userStore.userInfo.username || '游客' }}
                </div>
                <div class="rounded-full border border-slate-200 bg-slate-50 px-3.5 py-1.5 text-[13px] text-slate-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">
                  邮箱: {{ profileForm.email || '未设置' }}
                </div>
              </div>
            </div>
          </div>

          <div class="flex flex-wrap gap-2.5 xl:justify-end">
            <a-upload
              name="file"
              :action="uploadUrl"
              :data="{ businessType: 'avatar' }"
              :show-upload-list="false"
              :headers="{ Authorization: `Bearer ${userStore.token}` }"
              accept="image/*"
              @change="handleAvatarChange"
            >
              <a-button class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-3.5 text-[13px]" :loading="loading">
                <template #icon>
                  <span class="i-fa6-solid:upload text-[12px] leading-none" />
                </template>
                更换头像
              </a-button>
            </a-upload>
          </div>
        </div>
      </div>

      <div class="p-5 md:p-7">
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
          <div
            v-for="card in profileSummary"
            :key="card.title"
            class="rounded-2xl border border-slate-200/70 dark:border-slate-800 p-4 bg-slate-50/70 dark:bg-slate-950/30"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <div class="text-sm text-slate-500 dark:text-slate-400">
                  {{ card.title }}
                </div>
                <div class="mt-2 text-xl font-semibold text-slate-900 dark:text-slate-100 break-all">
                  {{ card.value }}
                </div>
              </div>
              <div
                class="w-10 h-10 rounded-2xl flex items-center justify-center"
                :class="{
                  'bg-slate-200/80 text-slate-700 dark:bg-slate-800 dark:text-slate-200': card.tone === 'slate',
                  'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300': card.tone === 'emerald',
                  'bg-amber-100 text-amber-700 dark:bg-amber-500/15 dark:text-amber-300': card.tone === 'amber',
                  'bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300': card.tone === 'rose',
                }"
              >
                <span :class="card.icon" class="text-lg" />
              </div>
            </div>
            <p class="mt-3 text-xs text-slate-500 dark:text-slate-400 break-all">
              {{ card.hint }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="rounded-3xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 overflow-hidden">
      <div class="px-5 md:px-7 py-5">
        <a-tabs v-model:active-key="activeKey">
          <a-tab-pane key="1" tab="基本资料">
            <div class="grid grid-cols-1 xl:grid-cols-[320px_minmax(0,1fr)] gap-6 pt-5">
              <div class="rounded-3xl border border-slate-200/70 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/30 p-5">
                <div class="flex flex-col items-center text-center">
                  <a-avatar
                    :size="108"
                    :src="getImageUrl(profileForm.avatar || userStore.avatar)"
                    class="ring-4 ring-white dark:ring-slate-900 shadow-sm"
                  />
                  <h3 class="mt-4 text-lg font-semibold text-slate-900 dark:text-slate-100">
                    {{ profileForm.nickname || userStore.userInfo.username || '未设置昵称' }}
                  </h3>
                  <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    {{ userStore.userInfo.username || '游客账号' }}
                  </p>
                </div>

                <div class="mt-6 space-y-3 text-sm">
                  <div class="flex items-center justify-between gap-4">
                    <span class="text-slate-500 dark:text-slate-400">邮箱</span>
                    <span class="text-slate-900 dark:text-slate-100 text-right break-all">{{ profileForm.email || '未设置' }}</span>
                  </div>
                  <div class="flex items-center justify-between gap-4">
                    <span class="text-slate-500 dark:text-slate-400">手机号</span>
                    <span class="text-slate-900 dark:text-slate-100 text-right">{{ profileForm.phone || '未设置' }}</span>
                  </div>
                  <div class="flex items-center justify-between gap-4">
                    <span class="text-slate-500 dark:text-slate-400">头像状态</span>
                    <span class="text-slate-900 dark:text-slate-100 text-right">{{ profileForm.avatar ? '已设置' : '默认头像' }}</span>
                  </div>
                </div>
              </div>

              <div class="rounded-3xl border border-slate-200/70 dark:border-slate-800 p-5 md:p-6">
                <div class="mb-5">
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
                    编辑基本资料
                  </h3>
                  <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    修改昵称、邮箱和手机号后，将同步更新当前登录态中的个人信息。
                  </p>
                </div>

                <a-form layout="vertical" :model="profileForm" class="max-w-2xl">
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <a-form-item label="显示名称" class="mb-0">
                      <a-input v-model:value="profileForm.nickname" placeholder="请输入显示名称" />
                    </a-form-item>
                    <a-form-item label="邮箱" class="mb-0">
                      <a-input v-model:value="profileForm.email" placeholder="请输入邮箱" />
                    </a-form-item>
                    <a-form-item label="手机号" class="mb-0 md:col-span-2">
                      <a-input v-model:value="profileForm.phone" placeholder="请输入手机号" />
                    </a-form-item>
                  </div>

                  <div class="mt-6 flex flex-wrap gap-2.5">
                    <a-button type="primary" :loading="loading" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-4 text-[13px]" @click="handleUpdateProfile">
                      保存资料
                    </a-button>
                  </div>
                </a-form>
              </div>
            </div>
          </a-tab-pane>

          <a-tab-pane key="2" tab="头像设置">
            <div class="grid grid-cols-1 xl:grid-cols-[340px_minmax(0,1fr)] gap-6 pt-5">
              <div class="rounded-3xl border border-slate-200/70 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/30 p-6 flex flex-col items-center text-center">
                <a-avatar
                  :size="132"
                  :src="getImageUrl(profileForm.avatar || userStore.avatar)"
                  class="ring-4 ring-white dark:ring-slate-900 shadow-sm"
                />
                <h3 class="mt-5 text-lg font-semibold text-slate-900 dark:text-slate-100">
                  当前头像预览
                </h3>
                <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
                  上传成功后会自动保存到当前账户资料中。
                </p>
              </div>

              <div class="rounded-3xl border border-slate-200/70 dark:border-slate-800 p-5 md:p-6">
                <div class="mb-5">
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
                    上传新头像
                  </h3>
                  <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    建议使用清晰的 JPG 或 PNG 图片，上传后会立即替换当前头像。
                  </p>
                </div>

                <a-upload
                  name="file"
                  :action="uploadUrl"
                  :data="{ businessType: 'avatar' }"
                  :show-upload-list="false"
                  :headers="{ Authorization: `Bearer ${userStore.token}` }"
                  accept="image/*"
                  @change="handleAvatarChange"
                >
                  <a-button type="primary" :loading="loading" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-4 text-[13px]">
                    <template #icon>
                      <span class="i-fa6-solid:image text-[12px] leading-none" />
                    </template>
                    选择并上传头像
                  </a-button>
                </a-upload>

                <div class="mt-5 rounded-2xl border border-dashed border-slate-200 dark:border-slate-700 px-4 py-4 text-sm text-slate-500 dark:text-slate-400 space-y-2">
                  <div>支持格式：JPG、PNG、GIF 等常见图片格式。</div>
                  <div>建议使用清晰的正方形头像，显示效果会更稳定。</div>
                  <div>上传成功后将自动更新当前个人资料。</div>
                </div>
              </div>
            </div>
          </a-tab-pane>

          <a-tab-pane key="3" tab="安全设置">
            <div class="grid grid-cols-1 xl:grid-cols-[320px_minmax(0,1fr)] gap-6 pt-5">
              <div class="rounded-3xl border border-slate-200/70 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-950/30 p-5">
                <div class="flex items-center gap-3">
                  <div class="w-11 h-11 rounded-2xl bg-rose-100 text-rose-700 dark:bg-rose-500/15 dark:text-rose-300 flex items-center justify-center">
                    <span class="i-fa6-solid:shield-halved text-lg" />
                  </div>
                  <div>
                    <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
                      账户安全
                    </h3>
                    <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                      修改密码后，当前登录态会立即失效。
                    </p>
                  </div>
                </div>

                <div class="mt-6 space-y-3 text-sm text-slate-500 dark:text-slate-400">
                  <div>建议使用包含大小写字母、数字和特殊字符的高强度密码。</div>
                  <div>请勿将新密码设置为与历史密码过于相近的内容。</div>
                  <div>修改成功后系统会要求重新登录以确认新凭证生效。</div>
                </div>
              </div>

              <div class="rounded-3xl border border-slate-200/70 dark:border-slate-800 p-5 md:p-6">
                <div class="mb-5">
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-slate-100">
                    修改密码
                  </h3>
                  <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    请先输入当前密码，再设置新的登录密码。确认无误后提交保存。
                  </p>
                </div>

                <a-form layout="vertical" :model="passwordForm" class="max-w-2xl" @finish="handleUpdatePassword">
                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <a-form-item
                      label="当前密码"
                      name="oldPassword"
                      class="mb-0 md:col-span-2"
                      :rules="[{ required: true, message: '请输入当前密码' }]"
                    >
                      <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入当前密码" />
                    </a-form-item>
                    <a-form-item
                      label="新密码"
                      name="newPassword"
                      class="mb-0"
                      :rules="[
                        { required: true, message: '请输入新密码' },
                        {
                          async validator(_, value) {
                            if (!value)
                              return
                            const errorMsg = validatePassword(value)
                            if (errorMsg)
                              throw new Error(errorMsg)
                          },
                          trigger: 'change',
                        },
                      ]"
                    >
                      <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
                    </a-form-item>
                    <a-form-item
                      label="确认新密码"
                      name="confirmPassword"
                      class="mb-0"
                      :rules="[
                        { required: true, message: '请再次输入新密码' },
                        {
                          async validator(_, value) {
                            if (!value)
                              return
                            if (passwordForm.newPassword && passwordForm.newPassword !== value)
                              throw new Error('两次输入的密码不一致')
                          },
                          trigger: 'change',
                        },
                      ]"
                    >
                      <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
                    </a-form-item>
                  </div>

                  <div class="mt-6 flex flex-wrap gap-2.5">
                    <a-button type="primary" danger :loading="loading" class="inline-flex h-9 items-center justify-center gap-1.5 rounded-xl px-4 text-[13px]" html-type="submit">
                      修改密码
                    </a-button>
                  </div>
                </a-form>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.ant-tabs-nav-wrap) {
  padding-bottom: 8px;
}

:deep(.ant-input),
:deep(.ant-input-affix-wrapper) {
  border-radius: 12px;
}

:deep(.ant-form-item-label > label) {
  color: #475569;
  font-size: 13px;
}

:deep(.ant-btn .ant-btn-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
