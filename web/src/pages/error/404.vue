<script setup lang="ts">
const router = useRouter()
</script>

<template>
  <div class="flex h-full items-center justify-center px-6 py-10">
    <a-result status="404" title="404" sub-title="你访问的页面不存在，可能已被移除或链接地址有误。">
      <template #extra>
        <a-button type="primary" @click="router.push('/')">
          返回首页
        </a-button>
      </template>
    </a-result>
  </div>
</template>

<route lang="yaml">
meta:
  layout: default
  label: 404
</route>
