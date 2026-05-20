import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { resolveRouteComponent, useMenuStore } from '@/stores/menu'
import { usePermissionStore } from '@/stores/permission'

const staticRoutes = [
  { path: '/', redirect: '/dashboard/home' },
  { path: '/login', component: () => import('@/views/Login.vue') },
  {
    path: '/dashboard',
    component: BasicLayout,
    children: [
      {
        path: 'home',
        component: () => import('@/views/dashboard/HomeDashboard.vue'),
      },
    ],
  },
  {
    path: '/production',
    component: BasicLayout,
    children: [
      {
        path: 'order',
        component: () => import('@/views/production/Order.vue'),
      },
      {
        path: 'schedule',
        component: () => import('@/views/production/Schedule.vue'),
      },
    ],
  },
  {
    path: '/quality',
    component: BasicLayout,
    children: [
      {
        path: 'check',
        component: () => import('@/views/quality/Check.vue'),
      },
    ],
  },
  {
    path: '/system',
    component: BasicLayout,
    children: [
      { path: 'org', component: () => import('@/views/system/SystemOrg.vue') },
      { path: 'menu', component: () => import('@/views/system/SystemMenu.vue') },
      { path: 'role', component: () => import('@/views/system/SystemRole.vue') },
      { path: 'user', component: () => import('@/views/system/SystemUser.vue') },
      { path: 'online-user', component: () => import('@/views/system/SystemOnlineUser.vue') },
      { path: 'codegen', component: () => import('@/views/system/SystemCodegen.vue') },
    ],
  },
  { path: '/404', component: () => import('@/views/404.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/404' },
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
})

function hasConcreteMatch(path: string): boolean {
  return router.resolve(path).matched.some((record) => record.path !== '/:pathMatch(.*)*')
}

function toDynamicRouteName(path: string): string {
  return `dynamic:${path.replace(/[^a-zA-Z0-9]/g, '_')}`
}

function registerDynamicRoutesFromMenu() {
  const menuStore = useMenuStore()
  let added = false
  for (const routeItem of menuStore.flatRoutes) {
    if (!routeItem.path || !routeItem.component) continue
    if (hasConcreteMatch(routeItem.path)) continue
    const routeComponent = resolveRouteComponent(routeItem.component)
    if (!routeComponent) continue
    const routeName = toDynamicRouteName(routeItem.path)
    if (router.hasRoute(routeName)) continue
    router.addRoute({
      path: routeItem.path,
      name: routeName,
      component: BasicLayout,
      children: [
        {
          path: '',
          name: `${routeName}:view`,
          component: routeComponent,
        },
      ],
    })
    added = true
  }
  return added
}

router.beforeEach(async (to) => {
  const token = localStorage.getItem('token')
  const menuStore = useMenuStore()
  const permissionStore = usePermissionStore()
  if (to.path === '/login') return true
  if (!token) return '/login'
  if (!menuStore.loaded) {
    try {
      await menuStore.loadFromApi()
    } catch {
      menuStore.setMenu([])
      menuStore.loaded = true
    }
  }
  if (!permissionStore.loaded) {
    try {
      await permissionStore.loadFromApi()
    } catch {
      permissionStore.clear()
    }
  }
  const added = registerDynamicRoutesFromMenu()
  if (!hasConcreteMatch(to.path) && added) {
    return to.fullPath
  }
  return true
})

export default router
