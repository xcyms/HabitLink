<script lang="ts" setup>
import { useManualTheme } from '@/composables/useManualTheme'

const router = useRouter()
const route = useRoute()
const { activeTabbar, getTabbarItemValue, setTabbarItemActive, tabbarList } = useTabbar()
const { theme, isDark, themeVars } = useManualTheme()

const activeColor = computed(() => (themeVars.value as any).colorTheme || '#4D7FFF')

function getIconClass(item: any) {
  const iconMap: Record<string, { active: string, inactive: string }> = {
    'home-2': { active: 'i-solar-home-2-bold', inactive: 'i-solar-home-2-linear' },
    'album': { active: 'i-solar-album-bold', inactive: 'i-solar-album-linear' },
    'bell-bing': { active: 'i-solar-bell-bing-bold', inactive: 'i-solar-bell-bing-linear' },
    'user-circle': { active: 'i-solar-user-circle-bold', inactive: 'i-solar-user-circle-linear' },
  }
  const config = iconMap[item.icon]
  return item.active ? config.active : config.inactive
}

function handleTabbarChange({ value }: { value: string }) {
  setTabbarItemActive(value)
  router.pushTab({ name: value })
}

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
      tabbarItemTitleFontSize: '13px',
    }"
  >
    <div class="page-wraper" :class="{ 'wot-theme-dark': isDark }">
      <slot />
      <wd-gap safe-area-bottom height="var(--wot-tabbar-height, 50px)" />
      <wd-tabbar
        :model-value="activeTabbar.name" bordered safe-area-inset-bottom fixed
        :active-color="activeColor"
        @change="handleTabbarChange"
      >
        <wd-tabbar-item
          v-for="(item, index) in tabbarList" :key="index" :name="item.name"
          :value="getTabbarItemValue(item.name)" :title="item.title"
          custom-class="!text-[13px]"
        >
          <template #icon>
            <div
              class="text-[16px] transition-all duration-300 ease-in-out" :class="[
                getIconClass(item),
                item.active ? 'scale-110' : 'scale-100 opacity-40',
              ]"
              :style="{
                color: item.active ? activeColor : '#666',
              }"
            />
          </template>
        </wd-tabbar-item>
      </wd-tabbar>
    </div>
  </wd-config-provider>
</template>

<style scoped lang="scss">
</style>
