// src/composables/useSystemInfo.ts
import { readonly, ref } from 'vue'

interface SystemInfo {
  statusBarHeight: Readonly<Ref<number>>
  menuButtonRight: Readonly<Ref<number>>
  safeAreaInsetsBottom: Readonly<Ref<number>>
  windowWidth: Readonly<Ref<number>>
  windowHeight: Readonly<Ref<number>>
}

// 单例模式，避免重复获取
let systemInfoInstance: SystemInfo | null = null

/**
 * 获取系统信息的 composable
 * 使用单例模式，确保只获取一次系统信息
 */
export function useSystemInfo(): SystemInfo {
  if (systemInfoInstance) {
    return systemInfoInstance
  }

  const statusBarHeight = ref(0)
  const menuButtonRight = ref(0)
  const safeAreaInsetsBottom = ref(0)
  const windowWidth = ref(0)
  const windowHeight = ref(0)

  // 获取系统信息
  uni.getSystemInfo({
    success: (res) => {
      statusBarHeight.value = res.statusBarHeight || 0
      safeAreaInsetsBottom.value = res.safeAreaInsets?.bottom || 0
      windowWidth.value = res.windowWidth || 0
      windowHeight.value = res.windowHeight || 0

      // #ifdef MP-WEIXIN
      const menuButton = uni.getMenuButtonBoundingClientRect()
      if (menuButton) {
        menuButtonRight.value = res.windowWidth - menuButton.left
      }
      // #endif
    },
  })

  systemInfoInstance = {
    statusBarHeight: readonly(statusBarHeight),
    menuButtonRight: readonly(menuButtonRight),
    safeAreaInsetsBottom: readonly(safeAreaInsetsBottom),
    windowWidth: readonly(windowWidth),
    windowHeight: readonly(windowHeight),
  }

  return systemInfoInstance
}
