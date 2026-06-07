<script setup lang="ts">
import { message } from 'ant-design-vue'
import { loginApi, registerApi } from '../../api/user.ts'
import { useUserStore } from '../../store/user.ts'
import { validatePassword } from '../../utils/common.ts'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)

const formState = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  remember: true,
})

const loginPasswordRules = [
  { required: true, message: '请输入密码' },
]

const registerPasswordRules = [
  { required: true, message: '请输入密码' },
  {
    async validator(_: unknown, value: string) {
      if (!value)
        return
      const errorMsg = validatePassword(value)
      if (errorMsg)
        throw new Error(errorMsg)
    },
    trigger: 'change' as const,
  },
]

const confirmPasswordRules = [
  { required: true, message: '请再次输入密码' },
  {
    async validator(_: unknown, value: string) {
      if (!value)
        return
      if (formState.password && formState.password !== value)
        throw new Error('两次输入的密码不一致')
    },
    trigger: 'change' as const,
  },
]

/**
 * 切换登录与注册模式，并清空敏感字段。
 */
function toggleMode() {
  isLogin.value = !isLogin.value
  formState.username = ''
  formState.password = ''
  formState.confirmPassword = ''
}

/**
 * 忘记密码提示。
 */
function handleForgotPassword() {
  message.info('请联系管理员重置密码，后续可在此处接入找回流程。')
}

/**
 * 提交登录或注册表单。
 */
async function handleSubmit() {
  try {
    loading.value = true
    if (isLogin.value) {
      const token = await loginApi({
        username: formState.username.trim(),
        password: formState.password,
      })
      userStore.login({
        username: formState.username.trim(),
        token,
      })
      message.success('登录成功')
      router.push('/')
    }
    else {
      await registerApi({
        username: formState.username.trim(),
        password: formState.password,
        confirmPassword: formState.confirmPassword,
      })
      message.success('注册成功，请使用新账号登录')
      isLogin.value = true
      formState.password = ''
      formState.confirmPassword = ''
    }
  }
  catch (error) {
    // 请求错误已在 request.ts 拦截器中统一处理。
    console.error('登录或注册失败:', error)
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[linear-gradient(180deg,#f8fafc_0%,#eef4ff_100%)] text-slate-900">
    <div class="flex min-h-screen flex-col">
      <div class="flex flex-1 overflow-hidden bg-white">
        <section class="relative hidden flex-1 overflow-hidden lg:flex">
          <div class="absolute inset-0 bg-[linear-gradient(145deg,#e0f2fe_0%,#eff6ff_42%,#f8fafc_100%)]" />
          <div class="absolute inset-0 bg-[radial-gradient(circle_at_18%_20%,rgba(14,165,233,0.18),transparent_26%),radial-gradient(circle_at_82%_16%,rgba(59,130,246,0.16),transparent_24%),radial-gradient(circle_at_58%_76%,rgba(14,116,144,0.10),transparent_24%)]" />
          <div class="absolute left-14 top-12 flex items-center gap-3">
            <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-900 text-sky-300 shadow-lg shadow-slate-300/60">
              <div class="i-fa6-solid:gem text-xl" />
            </div>
            <div>
              <div class="text-base font-semibold tracking-wide text-slate-900">
                Universal Starter
              </div>
              <div class="text-xs text-slate-500">
                Admin Workspace
              </div>
            </div>
          </div>

          <div class="relative z-10 flex w-full flex-col justify-between px-14 py-16 xl:px-18">
            <div class="max-w-3xl pt-16 space-y-8">
              <div class="inline-flex items-center gap-2 rounded-full border border-sky-200 bg-white/75 px-4 py-2 text-xs text-sky-700 shadow-sm backdrop-blur">
                <span class="i-fa6-solid:wand-magic-sparkles text-[11px]" />
                面向中后台场景的开箱即用工作台
              </div>

              <div class="space-y-5">
                <h1 class="max-w-4xl text-4xl font-semibold leading-tight tracking-tight text-slate-900 xl:text-6xl">
                  更轻盈的后台入口，更稳定的业务工作台。
                </h1>
                <p class="max-w-2xl text-base leading-8 text-slate-600 xl:text-lg">
                  集成用户、角色、字典、通知、日志、监控和系统设置等常用能力，帮助团队以统一界面完成日常管理、运维协作和配置维护。
                </p>
              </div>

              <div class="grid grid-cols-1 gap-4 xl:grid-cols-3">
                <div class="rounded-[28px] border border-white/70 bg-white/78 px-5 py-5 shadow-sm backdrop-blur">
                  <div class="text-xs text-slate-400">
                    模块能力
                  </div>
                  <div class="mt-3 text-2xl font-semibold text-slate-900">
                    开箱即用
                  </div>
                  <p class="mt-2 text-sm leading-6 text-slate-600">
                    常见后台模块预置完成，减少重复搭建成本，更快进入业务开发。
                  </p>
                </div>
                <div class="rounded-[28px] border border-white/70 bg-white/78 px-5 py-5 shadow-sm backdrop-blur">
                  <div class="text-xs text-slate-400">
                    运维视角
                  </div>
                  <div class="mt-3 text-2xl font-semibold text-slate-900">
                    信息聚合
                  </div>
                  <p class="mt-2 text-sm leading-6 text-slate-600">
                    通知、日志、监控和配置集中归拢，减少后台日常切换负担。
                  </p>
                </div>
                <div class="rounded-[28px] border border-white/70 bg-white/78 px-5 py-5 shadow-sm backdrop-blur">
                  <div class="text-xs text-slate-400">
                    权限协作
                  </div>
                  <div class="mt-3 text-2xl font-semibold text-slate-900">
                    管理清晰
                  </div>
                  <p class="mt-2 text-sm leading-6 text-slate-600">
                    角色、用户和系统设置彼此衔接，适合持续演进的后台项目。
                  </p>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between text-xs text-slate-500">
              <span>© 2026 Universal Starter</span>
              <span>Built for practical admin systems</span>
            </div>
          </div>
        </section>

        <section class="flex w-full items-center justify-center border-l border-slate-200/80 bg-white px-5 py-10 sm:px-8 lg:w-[460px] xl:w-[520px] xl:px-10">
          <div class="w-full max-w-md">
            <div class="mb-8 text-center lg:hidden">
              <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-slate-900 text-sky-300 shadow-lg shadow-slate-300/50">
                <div class="i-fa6-solid:gem text-2xl" />
              </div>
              <h2 class="mt-4 text-2xl font-semibold text-slate-900">
                Universal Starter
              </h2>
              <p class="mt-2 text-sm text-slate-500">
                统一的后台登录入口
              </p>
            </div>

            <div class="space-y-2">
              <div class="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 text-[12px] text-slate-500">
                <span class="i-fa6-solid:key text-[11px] text-sky-600" />
                安全访问
              </div>
              <h2 class="text-3xl font-semibold tracking-tight text-slate-900">
                {{ isLogin ? '欢迎回来' : '创建账号' }}
              </h2>
              <p class="text-sm leading-6 text-slate-500">
                {{ isLogin ? '输入账号信息后继续进入后台工作台。' : '填写基础信息后即可创建新的后台账号。' }}
              </p>
            </div>

            <a-form layout="vertical" :model="formState" autocomplete="off" class="mt-8" @finish="handleSubmit">
              <a-form-item
                name="username"
                class="mb-5"
                :rules="[{ required: true, message: '请输入用户名' }]"
              >
                <template #label>
                  <span class="text-sm font-medium text-slate-700">用户名</span>
                </template>
                <a-input
                  v-model:value.trim="formState.username"
                  size="large"
                  placeholder="请输入用户名"
                  class="!rounded-2xl !py-2.5 !text-sm"
                >
                  <template #prefix>
                    <div class="i-ant-design:user-outlined mr-1 text-slate-400" />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item
                name="password"
                class="mb-5"
                :rules="isLogin ? loginPasswordRules : registerPasswordRules"
              >
                <template #label>
                  <span class="text-sm font-medium text-slate-700">密码</span>
                </template>
                <a-input-password
                  v-model:value="formState.password"
                  size="large"
                  placeholder="请输入密码"
                  class="!rounded-2xl !py-2.5 !text-sm"
                >
                  <template #prefix>
                    <div class="i-ant-design:lock-outlined mr-1 text-slate-400" />
                  </template>
                </a-input-password>
                <div v-if="!isLogin" class="mt-2 flex items-center gap-1 text-xs text-slate-400">
                  <div class="i-ant-design:info-circle-outlined" />
                  至少 8 位，包含大小写字母、数字和特殊字符。
                </div>
              </a-form-item>

              <a-form-item
                v-if="!isLogin"
                name="confirmPassword"
                class="mb-6"
                :rules="confirmPasswordRules"
              >
                <template #label>
                  <span class="text-sm font-medium text-slate-700">确认密码</span>
                </template>
                <a-input-password
                  v-model:value="formState.confirmPassword"
                  size="large"
                  placeholder="请再次输入密码"
                  class="!rounded-2xl !py-2.5 !text-sm"
                >
                  <template #prefix>
                    <div class="i-ant-design:lock-outlined mr-1 text-slate-400" />
                  </template>
                </a-input-password>
              </a-form-item>

              <div v-if="isLogin" class="mb-7 flex items-center justify-between">
                <a-checkbox v-model:checked="formState.remember" class="text-slate-500">
                  记住我
                </a-checkbox>
                <button type="button" class="text-sm font-medium text-sky-600 transition-colors hover:text-sky-500" @click="handleForgotPassword">
                  忘记密码
                </button>
              </div>

              <a-button
                type="primary"
                html-type="submit"
                block
                size="large"
                :loading="loading"
                class="!h-12 !rounded-2xl !border-none !bg-slate-900 !text-base !font-medium shadow-lg shadow-slate-200 hover:!bg-slate-800"
              >
                {{ isLogin ? '登录系统' : '创建账号' }}
              </a-button>
            </a-form>

            <div class="mt-7 border-t border-slate-100 pt-5 text-center text-sm text-slate-500">
              {{ isLogin ? '还没有账号？' : '已有账号？' }}
              <button type="button" class="ml-1 font-semibold text-sky-600 transition-colors hover:text-sky-500" @click="toggleMode">
                {{ isLogin ? '立即注册' : '立即登录' }}
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: false
</route>
