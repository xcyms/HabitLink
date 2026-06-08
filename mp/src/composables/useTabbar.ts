export interface TabbarItem {
  name: string
  value: number | null
  active: boolean
  title: string
  icon: string
}

/**
 * 底部导航配置。
 * 第二个和第三个图标已经替换成更贴近任务记录与数据概览的图形。
 */
const tabbarItems = ref<TabbarItem[]>([
  { name: 'index', value: null, active: true, title: '首页', icon: 'home-2' },
  { name: 'records', value: null, active: false, title: '记录', icon: 'checklist-minimalistic' },
  { name: 'stats', value: null, active: false, title: '统计', icon: 'widget-4' },
  { name: 'me', value: null, active: false, title: '我的', icon: 'user-circle' },
])

export function useTabbar() {
  const tabbarList = computed(() => tabbarItems.value)

  const activeTabbar = computed(() => {
    const item = tabbarItems.value.find(item => item.active)
    return item || tabbarItems.value[0]
  })

  const getTabbarItemValue = (name: string) => {
    const item = tabbarItems.value.find(item => item.name === name)
    return item && item.value ? item.value : null
  }

  const setTabbarItem = (name: string, value: number) => {
    const tabbarItem = tabbarItems.value.find(item => item.name === name)
    if (tabbarItem) {
      tabbarItem.value = value
    }
  }

  /**
   * 设置当前激活的底部导航项。
   */
  const setTabbarItemActive = (name: string) => {
    tabbarItems.value.forEach((item) => {
      item.active = item.name === name
    })
  }

  return {
    tabbarList,
    activeTabbar,
    getTabbarItemValue,
    setTabbarItem,
    setTabbarItemActive,
  }
}
