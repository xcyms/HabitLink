<script setup lang="ts">
import {
  BarsOutlined,
  BgColorsOutlined,
  BulbOutlined,
  CompressOutlined,
  ExpandOutlined,
  MenuFoldOutlined,
  PoweroffOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { useFullscreen } from '@vueuse/core'
import NoticeCenter from '../components/NoticeCenter/index.vue'
import { isDark, toggleDark } from '../composables/useTheme.ts'
import { getUserInfoApi, logoutApi } from '../api/user.ts'
import { type MenuItem, menuItems } from '../router/menu.ts'
import { useUserStore } from '../store/user.ts'
import { getImageUrl } from '../utils/common.ts'

interface BreadcrumbItem {
  path: string
  label: string
}

const { isFullscreen, toggle: toggleFullscreen } = useFullscreen()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)
const isMobile = ref(false)
const selectedKeys = ref<string[]>([route.path])
const openKeys = ref<string[]>([])

/**
 * 检测是否为移动端布局。
 */
function checkMobile() {
  isMobile.value = window.innerWidth < 992
  if (isMobile.value)
    collapsed.value = true
}

/**
 * 过滤当前用户可见菜单。
 */
const filteredMenuItems = computed(() => {
  const userRoles = userStore.userInfo.roles || []

  const filterItems = (items: MenuItem[]): MenuItem[] => {
    return items
      .filter((item) => {
        if (!item.roles?.length)
          return true
        return item.roles.some(role => userRoles.includes(role))
      })
      .map(item => ({
        ...item,
        children: item.children ? filterItems(item.children) : undefined,
      }))
  }

  return filterItems(menuItems)
})

/**
 * 构建当前路由面包屑。
 */
const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const result: BreadcrumbItem[] = [{ path: '/', label: '首页' }]

  if (route.path === '/')
    return result

  const findLabel = (path: string, items: MenuItem[]): string | null => {
    for (const item of items) {
      if (item.key === path)
        return item.label
      if (item.children) {
        const childLabel = findLabel(path, item.children)
        if (childLabel)
          return childLabel
      }
    }
    return null
  }

  const label = findLabel(route.path, filteredMenuItems.value)
  if (label) {
    result.push({ path: route.path, label })
  }
  else {
    const lastMatched = route.matched[route.matched.length - 1]
    const fallbackLabel = (lastMatched?.meta?.label || lastMatched?.name || route.path.split('/').pop() || '未知页面') as string
    result.push({ path: route.path, label: fallbackLabel })
  }

  return result
})

/**
 * 当前页面主标题。
 */
const currentPageTitle = computed(() => breadcrumbs.value[breadcrumbs.value.length - 1]?.label || '首页')

/**
 * 同步子菜单展开项。
 */
function syncOpenKeys(path: string) {
  const nextOpenKeys: string[] = []

  filteredMenuItems.value.forEach((item) => {
    item.children?.forEach((child) => {
      if (child.children?.some(grandChild => grandChild.key === path))
        nextOpenKeys.push(`sub-${child.key || child.label}`)
    })
  })

  if (!isMobile.value)
    openKeys.value = nextOpenKeys
}

/**
 * 菜单点击跳转。
 */
function handleMenuClick(e: { key: string | number }) {
  const key = String(e.key)
  if (key.startsWith('/') && !key.startsWith('sub-')) {
    router.push(key)
    if (isMobile.value)
      collapsed.value = true
  }
}

/**
 * 用户菜单点击事件。
 */
function handleUserMenuClick({ key }: { key: string }) {
  if (key === 'logout') {
    handleLogout()
    return
  }

  if (key === 'profile')
    router.push('/profile')
}

/**
 * 退出登录。
 */
async function handleLogout() {
  try {
    await logoutApi()
  }
  catch (error) {
    console.error('退出登录失败:', error)
  }

  userStore.logout()
  router.replace('/login')
}

/**
 * 校验登录状态并同步用户资料。
 */
async function checkUserStatus() {
  if (!userStore.isLoggedIn) {
    router.replace('/login')
    return
  }

  if (!userStore.userInfo.id) {
    try {
      const res = await getUserInfoApi()
      userStore.login({
        ...userStore.userInfo,
        ...res,
      })
    }
    catch (error) {
      // request.ts 已处理 401，这里仅保留日志。
      console.error('获取用户信息失败:', error)
    }
  }
}

/**
 * 返回 BackTop 绑定的滚动容器。
 */
function getBackTopTarget() {
  return document.querySelector('.app-content-scroll') || window
}

watch(() => route.path, (path) => {
  selectedKeys.value = [path]
  syncOpenKeys(path)
}, { immediate: true })

watch(() => router.currentRoute.value.path, () => {
  checkUserStatus()
}, { immediate: true })

onMounted(() => {
  checkMobile()
  syncOpenKeys(route.path)
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<template>
  <a-layout class="h-screen overflow-hidden bg-slate-100 dark:bg-[#09090b]">
    <div
      v-if="isMobile && !collapsed"
      class="fixed inset-0 z-[1090] bg-slate-950/35 backdrop-blur-[1px]"
      @click="collapsed = true"
    />

    <a-layout-sider
      v-model:collapsed="collapsed"
      collapsible
      :trigger="null"
      :width="244"
      :collapsed-width="84"
      :theme="isDark ? 'dark' : 'light'"
      class="app-sider border-r border-slate-200/80 bg-white shadow-none transition-all duration-300 dark:border-slate-800 dark:bg-[#111113]"
      :class="{ 'app-sider-mobile': isMobile }"
    >
      <div class="flex h-full flex-col overflow-hidden">
        <div class="px-4 pb-3 pt-4">
          <div
            class="flex h-14 items-center rounded-2xl border border-slate-200/80 bg-slate-50 px-3 transition-colors dark:border-slate-800 dark:bg-slate-900/80"
            :class="collapsed ? 'justify-center' : 'gap-3'"
          >
            <div class="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-900 text-sky-300 dark:bg-slate-100 dark:text-slate-900">
              <span class="i-fa6-solid:gem text-[16px]" />
            </div>
            <div v-if="!collapsed" class="min-w-0">
              <div class="truncate text-sm font-semibold text-slate-900 dark:text-slate-100">
                HabitLink
              </div>
              <div class="truncate text-[11px] text-slate-500 dark:text-slate-400">
                管理员工作区
              </div>
            </div>
          </div>
        </div>

        <div class="flex-1 overflow-y-auto px-3 pb-4">
          <a-menu
            v-model:selected-keys="selectedKeys"
            v-model:open-keys="openKeys"
            :theme="isDark ? 'dark' : 'light'"
            mode="inline"
            class="app-menu border-none bg-transparent"
            @click="handleMenuClick"
          >
            <template v-for="item in filteredMenuItems" :key="item.label">
              <a-menu-item-group v-if="item.type === 'group' && !item.hidden" :title="item.label">
                <template v-for="subItem in item.children" :key="subItem.key || subItem.label">
                  <template v-if="!subItem.hidden">
                    <a-sub-menu v-if="subItem.children" :key="`sub-${subItem.key || subItem.label}`" :title="subItem.label">
                      <template #icon>
                        <span v-if="subItem.icon" class="menu-icon" :class="subItem.icon" />
                      </template>
                      <a-menu-item v-for="child in subItem.children" :key="child.key!">
                        {{ child.label }}
                      </a-menu-item>
                    </a-sub-menu>

                    <a-menu-item v-else :key="subItem.key!">
                      <template #icon>
                        <span v-if="subItem.icon" class="menu-icon" :class="subItem.icon" />
                      </template>
                      {{ subItem.label }}
                    </a-menu-item>
                  </template>
                </template>
              </a-menu-item-group>

              <a-menu-item v-else-if="!item.hidden && item.type !== 'group'" :key="item.key!">
                <template #icon>
                  <span v-if="item.icon" class="menu-icon" :class="item.icon" />
                </template>
                {{ item.label }}
              </a-menu-item>
            </template>
          </a-menu>
        </div>
      </div>
    </a-layout-sider>

    <a-layout class="min-w-0">
      <a-layout-header class="!h-[72px] !bg-white !px-3 !leading-normal border-b border-slate-200/80 shadow-none dark:!bg-[#111113] dark:border-slate-800 sm:!px-4 lg:!px-5">
        <div class="flex h-full items-center justify-between gap-4">
          <div class="flex min-w-0 items-center gap-3">
            <button
              type="button"
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 leading-none shadow-sm shadow-slate-100/80 transition-colors hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:shadow-none dark:hover:border-slate-500 dark:hover:text-slate-100"
              @click="collapsed = !collapsed"
            >
              <BarsOutlined v-if="collapsed" class="text-[15px]" />
              <MenuFoldOutlined v-else class="text-[15px]" />
            </button>

            <div class="min-w-0">
              <div class="truncate text-lg font-semibold text-slate-900 dark:text-slate-100">
                {{ currentPageTitle }}
              </div>
              <a-breadcrumb class="app-breadcrumb mt-1 min-w-0 text-[12px] text-slate-500 dark:text-slate-400">
                <a-breadcrumb-item v-for="(bc, index) in breadcrumbs" :key="bc.path">
                  <router-link
                    v-if="index < breadcrumbs.length - 1"
                    :to="bc.path"
                    class="truncate text-slate-500 transition-colors hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100"
                  >
                    {{ bc.label }}
                  </router-link>
                  <span v-else class="truncate text-slate-500 dark:text-slate-400">
                    {{ bc.label }}
                  </span>
                </a-breadcrumb-item>
              </a-breadcrumb>
            </div>
          </div>

          <div class="flex shrink-0 items-center gap-2">
            <button
              v-if="!isMobile"
              type="button"
              class="flex h-10 w-10 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 leading-none shadow-sm shadow-slate-100/80 transition-colors hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:shadow-none dark:hover:border-slate-500 dark:hover:text-slate-100"
              @click="toggleFullscreen"
            >
              <CompressOutlined v-if="isFullscreen" class="text-[15px]" />
              <ExpandOutlined v-else class="text-[15px]" />
            </button>

            <button
              type="button"
              class="flex h-10 w-10 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-500 leading-none shadow-sm shadow-slate-100/80 transition-colors hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300 dark:shadow-none dark:hover:border-slate-500 dark:hover:text-slate-100"
              @click="toggleDark()"
            >
              <BulbOutlined v-if="isDark" class="text-[15px]" />
              <BgColorsOutlined v-else class="text-[15px]" />
            </button>

            <NoticeCenter />

            <a-dropdown placement="bottomRight">
              <div class="flex h-10 cursor-pointer items-center gap-2 rounded-2xl border border-slate-200 bg-white px-1.5 pr-3 shadow-sm shadow-slate-100/80 transition-colors hover:border-slate-300 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:shadow-none dark:hover:border-slate-500 dark:hover:bg-slate-800">
                <a-avatar v-if="userStore.avatar" :size="30" :src="getImageUrl(userStore.avatar)" />
                <a-avatar v-else :size="30" class="!bg-slate-900 dark:!bg-slate-100 dark:!text-slate-900">
                  {{ (userStore.name || 'U').slice(0, 1).toUpperCase() }}
                </a-avatar>
                <div class="hidden min-w-0 sm:block">
                  <div class="max-w-[128px] truncate text-[13px] font-medium text-slate-900 dark:text-slate-100">
                    {{ userStore.name }}
                  </div>
                  <div class="max-w-[128px] truncate text-[11px] text-slate-400 dark:text-slate-500">
                    当前在线
                  </div>
                </div>
                <span class="hidden i-fa6-solid:chevron-down text-[10px] text-slate-400 dark:text-slate-500 sm:block" />
              </div>

              <template #overlay>
                <a-menu class="w-36" @click="({ key }) => handleUserMenuClick({ key: String(key) })">
                  <a-menu-item key="profile">
                    <div class="flex items-center gap-2 text-sm">
                      <UserOutlined />
                      个人中心
                    </div>
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout" danger>
                    <div class="flex items-center gap-2 text-sm">
                      <PoweroffOutlined />
                      退出登录
                    </div>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </div>
      </a-layout-header>

      <a-layout-content class="app-content-scroll overflow-auto bg-slate-100 px-3 py-3 transition-colors duration-300 dark:bg-[#09090b] sm:px-4 sm:py-4">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>

      <a-layout-footer class="border-t border-slate-200/80 bg-white px-4 py-3 text-center text-[13px] text-slate-400 dark:border-slate-800 dark:bg-[#111113] dark:text-slate-500">
        Universal Starter © 2026 Created by Lewis
      </a-layout-footer>
    </a-layout>

    <a-back-top :target="getBackTopTarget as () => HTMLElement" />
  </a-layout>
</template>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.18s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.menu-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

:deep(.app-breadcrumb .ant-breadcrumb-link),
:deep(.app-breadcrumb .ant-breadcrumb-separator) {
  display: inline-flex;
  align-items: center;
}

:deep(.app-menu.ant-menu) {
  font-size: 13px;
}

:deep(.app-menu .ant-menu-item-group-title) {
  padding: 10px 12px 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #94a3b8;
}

:deep(.app-menu .ant-menu-item),
:deep(.app-menu .ant-menu-submenu-title) {
  height: 42px;
  margin: 4px 0;
  border-radius: 14px;
  line-height: 42px;
}

:deep(.app-menu .ant-menu-item-selected) {
  font-weight: 600;
}

:deep(.app-menu.ant-menu-light .ant-menu-item-selected) {
  background: #e2f2ff;
  color: #0f172a;
}

:deep(.app-menu.ant-menu-dark .ant-menu-item-group-title) {
  color: #64748b !important;
}

:deep(.app-menu.ant-menu-dark .ant-menu-item),
:deep(.app-menu.ant-menu-dark .ant-menu-submenu-title) {
  color: rgba(255, 255, 255, 0.82) !important;
}

:deep(.app-menu.ant-menu-dark .ant-menu-item-selected) {
  background: rgba(56, 189, 248, 0.18) !important;
  color: #fff !important;
}

:deep(.ant-layout-sider-trigger) {
  display: none;
}

:deep(.ant-layout-header) {
  line-height: normal !important;
}

@media (max-width: 991px) {
  :deep(.app-sider.app-sider-mobile) {
    position: fixed !important;
    inset: 0 auto 0 0;
    z-index: 1100;
    height: 100vh;
    box-shadow: 8px 0 30px rgba(15, 23, 42, 0.16);
  }

  :deep(.app-sider.app-sider-mobile.ant-layout-sider-collapsed) {
    transform: translateX(-244px);
  }

  :deep(.app-sider.app-sider-mobile) {
    transition: transform 0.28s ease !important;
  }
}
</style>
