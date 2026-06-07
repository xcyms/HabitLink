<script setup lang="ts">
const router = useRouter()
</script>

<template>
  <div class="flex h-full items-center justify-center px-6 py-10">
    <a-result status="403" title="401" sub-title="当前账号没有访问该页面的权限，请返回首页或联系管理员处理。">
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
  label: 无权限
</route>
