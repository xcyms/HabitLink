import nprogress from 'nprogress'
import { setupLayouts } from 'virtual:generated-layouts'
import generatedRoutes from 'virtual:generated-pages'
import { createRouter, createWebHistory } from 'vue-router'
import 'nprogress/nprogress.css'
import { useUserStore } from '../store/user.ts'

const routes = setupLayouts(generatedRoutes)

// 添加通配符重定向
routes.push({
  path: '/:pathMatch(.*)*',
  redirect: '/error/404',
})

const router = createRouter({
  history: createWebHistory(),
  routes, // 使用自动生成的路由
})

router.beforeEach((to, _from, next) => {
  nprogress.start()

  const userStore = useUserStore()
  const publicPages = ['/login', '/error/404', '/500', '/401']
  const authRequired = !publicPages.includes(to.path)

  if (authRequired && !userStore.isLoggedIn)
    return next('/login')

  if (to.path === '/login' && userStore.isLoggedIn)
    return next('/')

  next()
})

router.afterEach(() => {
  nprogress.done()
})

export default router
