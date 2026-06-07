<script setup lang="ts">
const router = useRouter()
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-[radial-gradient(circle_at_top,_rgba(15,23,42,0.08),_transparent_35%),linear-gradient(180deg,#f8fafc_0%,#eef2ff_100%)] px-6 text-center">
    <div class="mb-8">
      <h1 class="relative text-9xl font-black tracking-widest text-slate-200">
        404
        <span class="absolute inset-0 mt-2 flex items-center justify-center text-3xl font-bold tracking-normal text-slate-800">
          页面未找到
        </span>
      </h1>
    </div>

    <p class="mx-auto mb-10 max-w-md text-lg leading-8 text-slate-500">
      抱歉，你访问的页面不存在、已被移动，或者当前访问地址不正确。你可以返回首页，或退回上一页继续操作。
    </p>

    <div class="flex flex-col gap-4 sm:flex-row">
      <a-button type="primary" size="large" class="!h-12 !rounded-2xl !px-8 !text-base !font-medium" @click="router.push('/')">
        返回首页
      </a-button>
      <a-button size="large" class="!h-12 !rounded-2xl !px-8 !text-base !font-medium" @click="router.back()">
        返回上一页
      </a-button>
    </div>

    <div class="mt-16 rounded-full border border-slate-200 bg-white/70 p-6 shadow-sm">
      <div class="i-fa6-solid:compass text-5xl text-slate-300" />
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: false
</route>
