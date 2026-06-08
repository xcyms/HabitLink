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
}

defineProps<Props>()

defineEmits<{
  action: []
}>()

const { statusBarHeight } = useSystemInfo()

/**
 * 顶部安全区内边距。
 * 统一所有页面的头部起始位置，避免每页各自偏移导致视觉别扭。
 */
const headerPaddingTop = computed(() => `${statusBarHeight.value + 18}px`)
</script>

<template>
  <view class="habit-hero habit-page-header" :style="{ paddingTop: headerPaddingTop }">
    <view class="relative z-10 px-5 pb-5">
      <view class="flex items-start justify-between gap-4">
        <view class="min-w-0 flex-1">
          <view class="habit-pill bg-white/16 text-white/88">
            {{ tag }}
          </view>
          <view class="mt-4 text-[36px] font-semibold leading-tight">
            {{ title }}
          </view>
          <view class="mt-3 text-sm text-white/78 leading-6">
            {{ desc }}
          </view>
          <slot name="headline-extra" />
        </view>

        <view
          v-if="actionText"
          class="shrink-0 rounded-[24rpx] bg-white/16 px-4 py-3 text-sm text-white font-semibold backdrop-blur"
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
