export interface MenuItem {
  key?: string
  label: string
  icon?: string
  type?: 'group'
  hidden?: boolean
  roles?: string[]
  children?: MenuItem[]
}

export const menuItems: MenuItem[] = [
  {
    label: '首页',
    type: 'group',
    children: [
      { key: '/', label: '工作台', icon: 'i-fa6-solid:house' },
    ],
  },
  {
    label: '管理',
    type: 'group',
    roles: ['ADMIN'],
    children: [
      { key: '/users', label: '用户管理', icon: 'i-fa6-solid:user' },
      { key: '/roles', label: '角色管理', icon: 'i-fa6-solid:user-shield' },
      { key: '/dicts', label: '字典管理', icon: 'i-fa6-solid:book' },
      { key: '/files', label: '文件管理', icon: 'i-fa6-solid:folder' },
      { key: '/notices', label: '通知公告', icon: 'i-fa6-solid:bullhorn' },
      { key: '/log', label: '操作日志', icon: 'i-fa6-solid:clock-rotate-left' },
      { key: '/monitor', label: '系统监控', icon: 'i-fa6-solid:desktop' },
      { key: '/settings', label: '系统设置', icon: 'i-fa6-solid:wrench' },
    ],
  },
  {
    key: '/profile',
    label: '个人中心',
    hidden: true,
  },
  {
    key: '/dicts/data',
    label: '字典数据管理',
    hidden: true,
  },
]
