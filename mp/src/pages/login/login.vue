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
const { statusBarHeight, menuButtonRight, safeAreaInsetsBottom } = useSystemInfo()

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
  const pages = getCurrentPages()
  if (pages.length > 1) {
    router.back()
  }
  else {
    router.pushTab({ name: 'index' })
  }
}

const navStyle = computed(() => ({
  paddingTop: `${statusBarHeight.value + 10}px`,
  paddingRight: `${Math.max(menuButtonRight.value, 24)}px`,
}))

const capsulePlaceholderStyle = computed(() => ({
  width: `${Math.max(menuButtonRight.value, 92)}px`,
}))

const pageStyle = computed(() => ({
  paddingBottom: `${safeAreaInsetsBottom.value + 28}px`,
}))
</script>

<template>
  <view class="habit-page min-h-screen">
    <view class="habit-shell habit-shell--default" :style="pageStyle">
      <view class="flex items-center justify-between gap-3" :style="navStyle">
        <view class="h-11 w-11 flex items-center justify-center border border-white/80 rounded-[24rpx] bg-white/78 text-[#324340] shadow-[0_12px_28px_rgba(95,121,116,0.10)] backdrop-blur" @tap="handleBack">
          <view class="i-solar-alt-arrow-left-linear text-[28rpx]" />
        </view>
        <view class="border border-white/80 rounded-full bg-white/66 px-4 py-2 text-[22rpx] text-[#5F7974] font-semibold tracking-[0.2rpx] shadow-[0_8px_20px_rgba(95,121,116,0.08)] backdrop-blur">
          HabitLink
        </view>
        <view class="shrink-0" :style="capsulePlaceholderStyle" />
      </view>

      <view class="relative mt-4 overflow-hidden border border-white/80 rounded-[40rpx] bg-white/72 px-5 pb-6 pt-5 shadow-[0_22px_60px_rgba(95,121,116,0.10)] backdrop-blur">
        <view class="absolute h-36 w-36 rounded-full bg-[#E4ECE7]/80 blur-2xl -right-8 -top-8" />
        <view class="absolute bottom-0 h-32 w-32 rounded-full bg-[#EEF1ED]/85 blur-2xl -left-10" />

        <view class="relative z-10">
          <view class="inline-flex items-center gap-2 rounded-full bg-[#EEF1ED] px-3 py-2 text-[20rpx] text-[#5F7974] font-semibold">
            <view class="i-solar-heart-angle-bold text-[22rpx]" />
            {{ isLogin ? '欢迎回来' : '开始建立你的节律' }}
          </view>
          <view class="mt-4 text-[56rpx] text-[#324340] font-semibold leading-[1.16] tracking-[-1rpx]">
            {{ isLogin ? '继续今天的习惯节奏' : '创建你的习惯空间' }}
          </view>
          <view class="mt-3 text-[24rpx] text-[#6F807B] leading-7">
            {{ isLogin ? '保留你的打卡记录、提醒节奏和最近变化，在这里接着往下走。' : '从一条足够轻的小目标开始，把提醒、记录和反馈都放进同一个地方。' }}
          </view>

          <view class="grid grid-cols-3 mt-5 gap-3">
            <view class="rounded-[24rpx] bg-white/78 px-3 py-4 shadow-[0_10px_24px_rgba(95,121,116,0.05)]">
              <view class="text-[20rpx] text-[#A1ADA7]">
                同步内容
              </view>
              <view class="mt-2 text-[30rpx] text-[#324340] font-semibold">
                习惯
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/78 px-3 py-4 shadow-[0_10px_24px_rgba(95,121,116,0.05)]">
              <view class="text-[20rpx] text-[#A1ADA7]">
                记录状态
              </view>
              <view class="mt-2 text-[30rpx] text-[#324340] font-semibold">
                打卡
              </view>
            </view>
            <view class="rounded-[24rpx] bg-white/78 px-3 py-4 shadow-[0_10px_24px_rgba(95,121,116,0.05)]">
              <view class="text-[20rpx] text-[#A1ADA7]">
                长期反馈
              </view>
              <view class="mt-2 text-[30rpx] text-[#324340] font-semibold">
                统计
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="habit-panel mt-5 overflow-hidden p-5">
        <view class="grid grid-cols-2 gap-3 rounded-[28rpx] bg-[#F1F4F1] p-2">
          <view
            class="rounded-[22rpx] py-3 text-center text-sm font-semibold transition-all"
            :class="isLogin ? 'bg-white text-[#324340] shadow-[0_8px_22px_rgba(95,121,116,0.08)]' : 'text-[#82918D]'"
            @tap="isLogin = true"
          >
            登录
          </view>
          <view
            class="rounded-[22rpx] py-3 text-center text-sm font-semibold transition-all"
            :class="!isLogin ? 'bg-white text-[#324340] shadow-[0_8px_22px_rgba(95,121,116,0.08)]' : 'text-[#82918D]'"
            @tap="isLogin = false"
          >
            注册
          </view>
        </view>

        <view class="mt-5">
          <view class="mb-2 text-[22rpx] text-[#6F807B] font-medium">
            用户名
          </view>
          <input
            v-model="form.username"
            class="h-12 border border-[#E1E6E1] rounded-[24rpx] bg-[#F8F9F6] px-4 text-sm text-[#324340]"
            type="text"
            placeholder="请输入用户名"
            placeholder-class="text-[#A4AFA9]"
          >
        </view>

        <view class="mt-4">
          <view class="mb-2 text-[22rpx] text-[#6F807B] font-medium">
            密码
          </view>
          <input
            v-model="form.password"
            class="h-12 border border-[#E1E6E1] rounded-[24rpx] bg-[#F8F9F6] px-4 text-sm text-[#324340]"
            :password="true"
            type="text"
            placeholder="请输入密码"
            placeholder-class="text-[#A4AFA9]"
          >
        </view>

        <view v-if="!isLogin" class="mt-4">
          <view class="mb-2 text-[22rpx] text-[#6F807B] font-medium">
            确认密码
          </view>
          <input
            v-model="form.confirmPassword"
            class="h-12 border border-[#E1E6E1] rounded-[24rpx] bg-[#F8F9F6] px-4 text-sm text-[#324340]"
            :password="true"
            type="text"
            placeholder="请再次输入密码"
            placeholder-class="text-[#A4AFA9]"
          >
        </view>

        <view class="mt-5 rounded-[26rpx] bg-[#F1F4F1] px-4 py-4 text-[22rpx] text-[#6F807B] leading-7">
          {{ isLogin ? '登录后可继续查看你的习惯安排、打卡记录和统计变化。' : '注册后即可开始创建习惯，并逐步沉淀每天的执行反馈。' }}
        </view>

        <view
          class="habit-primary-button mt-6 py-4 text-center text-sm font-semibold"
          :class="{ 'opacity-70 pointer-events-none': loading }"
          @tap="handleSubmit"
        >
          {{ loading ? '提交中...' : (isLogin ? '登录并继续' : '注册并开始') }}
        </view>

        <view class="mt-4 text-center text-[22rpx] text-[#6F807B] leading-7" @tap="toggleMode">
          {{ isLogin ? '还没有账号？切换到注册' : '已经有账号了？切换到登录' }}
        </view>
      </view>

      <view class="grid grid-cols-2 mt-5 gap-4">
        <view class="habit-value-card !bg-white/94 !p-5">
          <view class="h-11 w-11 flex items-center justify-center rounded-[22rpx] bg-[#EEF1ED] text-[#728D87]">
            <view class="i-solar-shield-check-bold text-[24rpx]" />
          </view>
          <view class="habit-value-card__title !mt-4 !text-[30rpx]">
            数据持续沉淀
          </view>
          <view class="habit-value-card__desc !mt-2">
            习惯、记录和统计会围绕账号持续保存，不用每次重新开始。
          </view>
        </view>

        <view class="habit-value-card !bg-white/94 !p-5">
          <view class="h-11 w-11 flex items-center justify-center rounded-[22rpx] bg-[#F1F4F1] text-[#8EA49D]">
            <view class="i-solar-notification-lines-remove-bold text-[24rpx]" />
          </view>
          <view class="habit-value-card__title !mt-4 !text-[30rpx]">
            提醒更容易接住
          </view>
          <view class="habit-value-card__desc !mt-2">
            登录后可以保留提醒时间和执行节奏，让行动更容易延续。
          </view>
        </view>
      </view>
    </view>
  </view>
</template>
