<script setup lang="ts">
import { useManualThemeStore } from '@/store/manualThemeStore'

onLaunch(() => {
  const themeStore = useManualThemeStore()
  themeStore.initTheme()
})
</script>

<style lang="scss">
page {
  background-color: #f8f9fa;
  transition: background-color 0.3s;
}

.wot-theme-dark page {
  background-color: #000 !important;
}

.page-wraper {
  min-height: calc(100vh - var(--window-top));
  box-sizing: border-box;
  background: var(--wot-color-bg, #f8f9fa);
}

.wot-theme-dark.page-wraper {
  background: var(--wot-dark-background, #000) !important;
}
</style>
