<script setup lang="ts">
const router = useRouter()
</script>

<template>
  <div class="flex h-full items-center justify-center px-6 py-10">
    <a-result status="500" title="500" sub-title="服务器暂时出现异常，请稍后重试，或返回首页继续其他操作。">
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
  label: 服务器错误
</route>
