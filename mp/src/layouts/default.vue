<script lang="ts" setup>
import { useManualTheme } from '@/composables/useManualTheme'

const { theme, isDark, themeVars } = useManualTheme()
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
  <wd-config-provider :theme="theme" :theme-vars="themeVars">
    <div class="page-wraper" :class="{ 'wot-theme-dark': isDark }">
      <slot />
    </div>
  </wd-config-provider>
</template>
