<script setup lang="ts">
/**
 * 统一页面头部组件属性。
 * 所有主页面共用这一套骨架，保证头部层级、留白和安全区完全一致。
 */
interface Props {
  tag: string
  title: string
  desc: string
  actionText?: string
  actionDisabled?: boolean
  showBack?: boolean
  backFallbackName?: string
}

const props = withDefaults(defineProps<Props>(), {
  actionText: '',
  actionDisabled: false,
  showBack: false,
  backFallbackName: 'index',
})

defineEmits<{
  action: []
}>()

const { statusBarHeight } = useSystemInfo()
const router = useRouter()

/**
 * 顶部安全区内边距。
 * 统一所有页面的头部起始位置，避免每页各自偏移导致视觉别扭。
 */
const headerStyle = computed(() => ({
  paddingTop: `${statusBarHeight.value + 14}px`,
}))

/**
 * 返回上一页。
 * 没有历史栈时回到指定兜底页，兼顾 tab 页与普通二级页。
 */
function handleBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    router.back()
    return
  }

  router.pushTab({ name: props.backFallbackName })
}
</script>

<template>
  <view class="habit-hero habit-page-header" :style="headerStyle">
    <view class="relative z-10 px-5 pb-5">
      <view
        v-if="showBack"
        class="mb-4 h-11 w-11 flex items-center justify-center rounded-[24rpx] bg-white/20 text-white backdrop-blur"
        @tap="handleBack"
      >
        <view class="i-solar-alt-arrow-left-linear text-[28rpx]" />
      </view>

      <view class="min-w-0">
        <view class="habit-pill bg-white/18 text-white/88">
          {{ tag }}
        </view>
        <view class="habit-page-header__title mt-4">
          {{ title }}
        </view>
        <view class="habit-page-header__desc mt-3">
          {{ desc }}
        </view>
        <slot name="headline-extra" />
      </view>

      <view v-if="actionText" class="mt-5">
        <view
          class="habit-page-header__action inline-flex items-center justify-center rounded-[24rpx] bg-white/18 px-5 py-3 text-sm text-white font-semibold backdrop-blur"
          :class="{ 'opacity-60 pointer-events-none': actionDisabled }"
          @tap="$emit('action')"
        >
          {{ actionText }}
        </view>
      </view>

      <view v-if="$slots.metrics" class="relative z-10 mt-5">
        <slot name="metrics" />
      </view>
    </view>
  </view>
</template>
