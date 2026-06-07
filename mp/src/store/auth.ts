import type { UserDTO } from '@/types/type'
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    isLoggedIn: false,
    user: null as UserDTO | null,
    token: null as string | null,
    loginTime: null as number | null,
  }),

  getters: {
    /**
     * 检查 token 是否有效（30分钟有效期）
     */
    isTokenValid: (state) => {
      if (!state.token || !state.loginTime)
        return false
      const expiryTime = 30 * 60 * 1000 // 30分钟
      return Date.now() - state.loginTime < expiryTime
    },

    /**
     * 获取用户头像
     */
    userAvatar: (state) => {
      return state.user?.avatar || '/static/default-avatar.png'
    },

    /**
     * 获取用户名称
     */
    userName: (state) => {
      return state.user?.nickname || state.user?.username || '未登录用户'
    },
  },

  actions: {
    /**
     * 登录
     */
    login(user: UserDTO, token: string) {
      this.isLoggedIn = true
      this.user = user
      this.token = token
      this.loginTime = Date.now()

      // 触发全局登录事件
      if (typeof uni !== 'undefined' && uni.$emit) {
        uni.$emit('auth:login', { user, token })
      }
    },

    /**
     * 退出登录
     */
    logout() {
      this.isLoggedIn = false
      this.user = null
      this.token = null
      this.loginTime = null

      // 清除相关缓存
      try {
        if (typeof uni !== 'undefined') {
          uni.removeStorageSync('cache_user_info')
        }
      }
      catch (e) {
        console.error('Clear cache error:', e)
      }

      // 触发全局退出事件
      if (typeof uni !== 'undefined' && uni.$emit) {
        uni.$emit('auth:logout')
      }
    },

    /**
     * 更新用户信息
     */
    updateUser(user: Partial<UserDTO>) {
      if (this.user) {
        this.user = { ...this.user, ...user }
      }
    },

    /**
     * 检查并刷新登录状态
     */
    checkLoginStatus() {
      if (!this.isTokenValid) {
        this.logout()
      }
    },
  },
})
