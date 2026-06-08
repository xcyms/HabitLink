<script setup lang="ts">
import { reactive, ref } from 'vue'

definePage({
  name: 'login',
  layout: 'default',
  style: {
    navigationBarTitleText: '登录',
    navigationStyle: 'custom',
  },
})

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()
const systemInfo = useSystemInfo()

const isLogin = ref(true)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

function toggleMode() {
  isLogin.value = !isLogin.value
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
}

function validateForm() {
  if (!form.username) {
    toast.warning('请输入用户名')
    return false
  }
  if (!form.password) {
    toast.warning('请输入密码')
    return false
  }
  if (!isLogin.value) {
    if (form.password.length < 6) {
      toast.warning('密码长度至少6位')
      return false
    }
    if (form.password !== form.confirmPassword) {
      toast.warning('两次密码输入不一致')
      return false
    }
  }
  return true
}

async function handleSubmit() {
  if (!validateForm())
    return

  loading.value = true
  try {
    if (isLogin.value) {
      const res = await Apis.universal.login({
        data: {
          username: form.username,
          password: form.password,
        },
      })

      if (res.code === 200 && res.data) {
        toast.success('登录成功')
        authStore.login({ username: form.username }, res.data || '')
        router.pushTab({ name: 'index' })
      }
      else {
        throw new Error(res.message || '登录失败')
      }
    }
    else {
      const res = await Apis.universal.register({
        data: {
          username: form.username,
          password: form.password,
        },
      })

      if (res.code === 200) {
        toast.success('注册成功，请登录')
        isLogin.value = true
      }
      else {
        throw new Error(res.message || '注册失败')
      }
    }
  }
  catch (error: any) {
    console.error(error)
    toast.error(error.message || (isLogin.value ? '登录失败' : '注册失败'))
    if (isLogin.value && authStore.token) {
      authStore.logout()
    }
  }
  finally {
    loading.value = false
  }
}

function handleBack() {
  router.back()
}
</script>

<template>
  <view class="relative min-h-screen w-full overflow-hidden from-gray-50 to-white bg-gradient-to-b">
    <!-- 背景装饰 -->
    <view class="absolute left-0 top-0 h-64 w-64 translate-y-1/2 rounded-full bg-blue-100/40 blur-2xl -translate-x-1/2" />
    <view class="absolute bottom-0 right-0 h-48 w-48 translate-x-1/3 translate-y-1/3 rounded-full bg-blue-50/60 blur-2xl" />
    <view class="absolute left-1/4 top-1/3 h-24 w-24 rounded-full bg-blue-100/30 blur-xl" />

    <!-- 顶部返回 -->
    <view class="fixed left-0 top-0 z-50 w-full" :style="{ paddingTop: `${systemInfo?.statusBarHeight.value}px` }">
      <view class="h-11 w-11 flex items-center justify-center" @click="handleBack">
        <wd-icon name="home" size="large" class="dark:text-white" />
      </view>
    </view>

    <view class="flex flex-col px-6 pt-28">
      <!-- Logo 区域 -->
      <view class="mb-8 flex items-center gap-3">
        <view class="h-14 w-14 flex items-center justify-center rounded-2xl bg-blue-500">
          <div class="i-solar-home-2-bold text-3xl text-white" />
        </view>
        <view>
          <view class="text-xl text-gray-900 font-bold">
            Universal
          </view>
          <view class="text-sm text-gray-500">
            Starter Kit
          </view>
        </view>
      </view>

      <!-- 标题 -->
      <view class="mb-6">
        <view class="text-2xl text-gray-900 font-bold">
          {{ isLogin ? '欢迎回来' : '创建账户' }}
        </view>
        <view class="mt-1 text-sm text-gray-500">
          {{ isLogin ? '登录以继续使用服务' : '填写以下信息注册账户' }}
        </view>
      </view>

      <!-- 表单卡片 -->
      <view class="rounded-2xl bg-white p-6 shadow-blue-900/10 shadow-lg">
        <!-- 表单 -->
        <view class="space-y-4">
          <view>
            <view class="mb-1.5 text-xs text-gray-600 font-medium">
              用户名
            </view>
            <input
              v-model="form.username"
              class="h-11 border border-gray-200 rounded-lg bg-gray-50 px-3 text-sm text-gray-900 focus:border-blue-500 focus:bg-white focus:outline-none placeholder-gray-400"
              type="text"
              placeholder="请输入用户名"
            >
          </view>

          <view>
            <view class="mb-1.5 text-xs text-gray-600 font-medium">
              密码
            </view>
            <input
              v-model="form.password"
              class="h-11 border border-gray-200 rounded-lg bg-gray-50 px-3 text-sm text-gray-900 focus:border-blue-500 focus:bg-white focus:outline-none placeholder-gray-400"
              :password="true"
              type="text"
              placeholder="请输入密码"
            >
          </view>

          <view v-if="!isLogin">
            <view class="mb-1.5 text-xs text-gray-600 font-medium">
              确认密码
            </view>
            <input
              v-model="form.confirmPassword"
              class="h-11 w-full border border-gray-200 rounded-lg bg-gray-50 px-3 text-sm text-gray-900 focus:border-blue-500 focus:bg-white focus:outline-none placeholder-gray-400"
              :password="true"
              type="text"
              placeholder="请再次输入密码"
            >
          </view>
        </view>

        <!-- 按钮 -->
        <view class="mt-6">
          <view
            class="h-11 flex cursor-pointer items-center justify-center rounded-lg bg-blue-500 text-white font-medium transition-all hover:bg-blue-600"
            @click="handleSubmit"
          >
            <view v-if="loading" class="h-4 w-4 animate-spin border-2 border-white/30 border-t-white rounded-full" />
            <text v-else class="text-sm">
              {{ isLogin ? '登录' : '注册' }}
            </text>
          </view>
        </view>
      </view>

      <!-- 切换 -->
      <view class="mt-6 flex justify-center">
        <view class="flex items-center gap-1" @click="toggleMode">
          <text class="text-sm text-gray-500">
            {{ isLogin ? '没有账号？' : '已有账号？' }}
          </text>
          <text class="text-sm text-blue-500 font-medium">
            {{ isLogin ? '立即注册' : '立即登录' }}
          </text>
        </view>
      </view>

      <!-- 底部装饰 -->
      <view class="mt-auto pb-8 pt-12 text-center">
        <view class="text-xs text-gray-400">
          登录即表示同意我们的服务条款
        </view>
      </view>
    </view>
  </view>
</template>
