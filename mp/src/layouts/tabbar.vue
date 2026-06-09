<script lang="ts" setup>
import { useManualTheme } from '@/composables/useManualTheme'

const router = useRouter()
const route = useRoute()
const { safeAreaInsetsBottom } = useSystemInfo()
const { activeTabbar, setTabbarItemActive, tabbarList } = useTabbar()
const { theme, isDark, themeVars } = useManualTheme()

/**
 * 底部导航激活色。
 * 这里固定为 HabitLink 新版主色，避免继续出现旧版蓝色调。
 */
const activeColor = '#728D87'

/**
 * 根据导航项返回对应图标。
 * 第二个和第三个图标使用更清晰的记录与概览语义。
 */
function getIconClass(item: any) {
  const iconMap: Record<string, { active: string, inactive: string }> = {
    'home-2': { active: 'i-solar-home-2-bold', inactive: 'i-solar-home-2-linear' },
    'checklist-minimalistic': { active: 'i-solar-checklist-minimalistic-bold', inactive: 'i-solar-checklist-minimalistic-linear' },
    'widget-4': { active: 'i-solar-widget-4-bold', inactive: 'i-solar-widget-4-linear' },
    'user-circle': { active: 'i-solar-user-circle-bold', inactive: 'i-solar-user-circle-linear' },
  }
  const config = iconMap[item.icon] || iconMap['home-2']
  return item.active ? config.active : config.inactive
}

function handleTabbarChange({ value }: { value: string }) {
  setTabbarItemActive(value)
  router.pushTab({ name: value })
}

/**
 * 页面底部占位高度。
 * 为固定底栏和安全区预留空间，避免页面内容被遮挡。
 */
const tabbarSpacerHeight = computed(() => `${104 + safeAreaInsetsBottom.value}px`)

onMounted(() => {
  // #ifdef APP
  uni.hideTabBar()
  // #endif
  nextTick(() => {
    if (route.name && route.name !== activeTabbar.value.name) {
      setTabbarItemActive(route.name)
    }
  })
})
</script>

<script lang="ts">
export default {
  options: {
    addGlobalClass: true,
    virtualHost: true,
    styleIsolation: 'shared',
  },
}
</script>

<template>
  <wd-config-provider
    :theme="theme"
    :theme-vars="{
      ...themeVars,
      colorTheme: activeColor,
      tabbarItemTitleFontSize: '13px',
    }"
  >
    <div class="page-wraper" :class="{ 'wot-theme-dark': isDark }">
      <slot />
      <view :style="{ height: tabbarSpacerHeight }" />

      <view
        class="habit-fixed-tabbar pointer-events-none fixed bottom-0 left-0 right-0 z-50"
        :style="{ paddingBottom: `${safeAreaInsetsBottom}px` }"
      >
        <view class="habit-float-tabbar pointer-events-auto">
          <view
            v-for="(item, index) in tabbarList"
            :key="index"
            class="habit-float-tabbar__item"
            :class="{ 'habit-float-tabbar__item--active': item.active }"
            @tap="handleTabbarChange({ value: item.name })"
          >
            <view
              class="habit-float-tabbar__icon"
              :class="{ 'habit-float-tabbar__icon--active': item.active }"
              :style="{ color: item.active ? activeColor : '#90A29F' }"
            >
              <div :class="getIconClass(item)" />
            </view>
            <view
              class="habit-float-tabbar__title"
              :class="{ 'habit-float-tabbar__title--active': item.active }"
            >
              {{ item.title }}
            </view>
          </view>
        </view>
      </view>
    </div>
  </wd-config-provider>
</template>
