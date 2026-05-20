import { defineStore } from 'pinia'
import http from '@/api/http'

/** 首页（工作台）常驻入口，与静态路由 `/dashboard/home` 一致 */
export const dashboardMenuRoot: MenuItem = {
  path: '/dashboard',
  meta: { title: '工作台', icon: 'Odometer' },
  children: [{ path: '/dashboard/home', component: 'DashboardHome', meta: { title: '首页', icon: 'House' } }],
}

/** 与后端菜单 component 字段一致 */
export const dynamicRouteMap = {
  DashboardHome: () => import('@/views/dashboard/HomeDashboard.vue'),
  ProductionOrder: () => import('@/views/production/Order.vue'),
  ProductionSchedule: () => import('@/views/production/Schedule.vue'),
  QualityCheck: () => import('@/views/quality/Check.vue'),
  SystemOrg: () => import('@/views/system/SystemOrg.vue'),
  SystemMenu: () => import('@/views/system/SystemMenu.vue'),
  SystemRole: () => import('@/views/system/SystemRole.vue'),
  SystemUser: () => import('@/views/system/SystemUser.vue'),
  SystemOnlineUser: () => import('@/views/system/SystemOnlineUser.vue'),
  SystemCodegen: () => import('@/views/system/SystemCodegen.vue'),
}

type RouteLoader = () => Promise<unknown>
const viewModules = import.meta.glob('@/views/**/*.vue') as Record<string, RouteLoader>
const viewComponentMap = new Map<string, RouteLoader>()
const viewComponentPaths = new Map<string, string[]>()
const duplicatedKeys = new Set<string>()
const printedWarnings = new Set<string>()

Object.entries(viewModules).forEach(([path, loader]) => {
  const matched = path.match(/\/([^/]+)\.vue$/)
  if (!matched) return
  const key = matched[1]
  const paths = viewComponentPaths.get(key) ?? []
  paths.push(path)
  viewComponentPaths.set(key, paths)
  if (viewComponentMap.has(key)) {
    duplicatedKeys.add(key)
    return
  }
  viewComponentMap.set(key, loader)
})

if (duplicatedKeys.size) {
  duplicatedKeys.forEach((key) => {
    const paths = viewComponentPaths.get(key) ?? []
    console.warn(`[dynamic-route] 发现重复组件名 "${key}"，请改名或在 dynamicRouteMap 显式映射。冲突文件：\n- ${paths.join('\n- ')}`)
  })
}

export function validateMenuComponentResolvable(component?: string): { ok: boolean; message?: string } {
  const hint = inspectMenuComponent(component)
  return { ok: hint.ok, message: hint.ok ? undefined : hint.message }
}

export type MenuComponentHintStatus = 'empty' | 'resolved' | 'duplicated' | 'not_found'

export interface MenuComponentHint {
  ok: boolean
  status: MenuComponentHintStatus
  via: 'explicit-map' | 'auto-scan' | 'none'
  message: string
  paths: string[]
}

export function inspectMenuComponent(component?: string): MenuComponentHint {
  const value = (component ?? '').trim()
  if (!value) {
    return {
      ok: false,
      status: 'empty',
      via: 'none',
      message: 'component 不能为空',
      paths: [],
    }
  }
  const explicit = (dynamicRouteMap as Partial<Record<string, RouteLoader>>)[value]
  if (explicit) {
    return {
      ok: true,
      status: 'resolved',
      via: 'explicit-map',
      message: `已匹配 dynamicRouteMap 显式映射：${value}`,
      paths: [],
    }
  }
  if (duplicatedKeys.has(value)) {
    const paths = viewComponentPaths.get(value) ?? []
    return {
      ok: false,
      status: 'duplicated',
      via: 'auto-scan',
      message: `component="${value}" 存在同名页面冲突，请改名或在 dynamicRouteMap 显式配置。`,
      paths,
    }
  }
  const directPath = (viewComponentPaths.get(value) ?? [])[0]
  if (directPath) {
    return {
      ok: true,
      status: 'resolved',
      via: 'auto-scan',
      message: `已匹配页面文件：${directPath}`,
      paths: [directPath],
    }
  }
  const normalized = value.replace(/^@\/views\//, '').replace(/\.vue$/, '')
  const matched = Object.keys(viewModules).filter((path) => path.endsWith(`/${normalized}.vue`))
  if (matched.length > 1) {
    return {
      ok: false,
      status: 'duplicated',
      via: 'auto-scan',
      message: `component="${value}" 匹配到多个页面，请改名或改为 dynamicRouteMap 显式映射。`,
      paths: matched,
    }
  }
  if (matched.length === 1) {
    return {
      ok: true,
      status: 'resolved',
      via: 'auto-scan',
      message: `已匹配页面文件：${matched[0]}`,
      paths: matched,
    }
  }
  return {
    ok: false,
    status: 'not_found',
    via: 'none',
    message: `component="${value}" 无法解析。请确认页面文件存在，或在 dynamicRouteMap 中手动映射。`,
    paths: [],
  }
}

export function resolveRouteComponent(component?: string): RouteLoader | null {
  if (!component) return null
  const explicit = (dynamicRouteMap as Partial<Record<string, RouteLoader>>)[component]
  if (explicit) return explicit
  if (!duplicatedKeys.has(component) && viewComponentMap.has(component)) {
    return viewComponentMap.get(component) ?? null
  }
  const normalized = component.replace(/^@\/views\//, '').replace(/\.vue$/, '')
  const entry = Object.entries(viewModules).find(([path]) => path.endsWith(`/${normalized}.vue`))
  if (!entry && !printedWarnings.has(component)) {
    printedWarnings.add(component)
    console.warn(`[dynamic-route] component="${component}" 未找到对应页面，请检查菜单配置或页面路径。`)
  }
  return entry ? entry[1] : null
}

export interface MenuItem {
  path: string
  component?: string
  meta?: {
    title: string
    icon?: string
  }
  children?: MenuItem[]
}

/** 登录后进入第一个可路由的叶子菜单 */
export function firstAccessibleLeafPath(tree: MenuItem[]): string | null {
  for (const node of tree) {
    if (node.children?.length) {
      const sub = firstAccessibleLeafPath(node.children)
      if (sub) return sub
    } else if (node.path && node.component && resolveRouteComponent(node.component)) {
      return node.path
    }
  }
  return null
}

export const useMenuStore = defineStore('menu', {
  state: () => ({
    menuTree: [] as MenuItem[],
    flatRoutes: [] as MenuItem[],
    loaded: false,
  }),
  actions: {
    setMenu(menuData: MenuItem[]) {
      this.menuTree = menuData
      this.flatRoutes = menuData
        .flatMap((item: MenuItem) => item.children ?? [item])
        .filter((item: MenuItem) => item.component && Boolean(resolveRouteComponent(item.component)))
    },
    hydrateDefaultMenu() {
      this.setMenu([
        dashboardMenuRoot,
        {
          path: '/production',
          meta: { title: '生产管理', icon: 'FolderOpened' },
          children: [
            { path: '/production/order', component: 'ProductionOrder', meta: { title: '生产工单', icon: 'Document' } },
            { path: '/production/schedule', component: 'ProductionSchedule', meta: { title: '排产计划', icon: 'Calendar' } },
          ],
        },
        {
          path: '/quality',
          meta: { title: '质量管理', icon: 'CircleCheck' },
          children: [{ path: '/quality/check', component: 'QualityCheck', meta: { title: '质量检验', icon: 'Histogram' } }],
        },
        {
          path: '/system',
          meta: { title: '系统管理', icon: 'Setting' },
          children: [
            { path: '/system/org', component: 'SystemOrg', meta: { title: '组织架构', icon: 'OfficeBuilding' } },
            { path: '/system/role', component: 'SystemRole', meta: { title: '角色管理', icon: 'UserFilled' } },
            { path: '/system/menu', component: 'SystemMenu', meta: { title: '菜单管理', icon: 'Menu' } },
            { path: '/system/user', component: 'SystemUser', meta: { title: '用户授权', icon: 'Avatar' } },
            {
              path: '/system/online-user',
              component: 'SystemOnlineUser',
              meta: { title: '在线用户', icon: 'Monitor' },
            },
            {
              path: '/system/codegen',
              component: 'SystemCodegen',
              meta: { title: '代码生成', icon: 'Tickets' },
            },
          ],
        },
      ])
      this.loaded = true
    },
    async loadFromApi() {
      const res = await http.get('/api/auth/menus')
      if (res.data?.code !== 0) {
        this.hydrateDefaultMenu()
        return
      }
      const raw = res.data.data as Array<Record<string, unknown>>
      const convert = (nodes: Array<Record<string, unknown>>): MenuItem[] =>
        (nodes || []).map((n) => ({
          path: String(n.path ?? ''),
          component: n.component ? String(n.component) : undefined,
          meta: {
            title: String((n.meta as { title?: string } | undefined)?.title ?? ''),
            icon: (n.meta as { icon?: string } | undefined)?.icon,
          },
          children: Array.isArray(n.children) && (n.children as unknown[]).length ? convert(n.children as Array<Record<string, unknown>>) : undefined,
        }))
      const fromApi = convert(raw)
      const systemNode = fromApi.find((item) => item.path === '/system')
      if (systemNode) {
        const children = systemNode.children ?? []
        const hasCodegen = children.some((item) => item.path === '/system/codegen')
        if (!hasCodegen) {
          children.push({ path: '/system/codegen', component: 'SystemCodegen', meta: { title: '代码生成', icon: 'Tickets' } })
        }
        systemNode.children = children
      }
      this.setMenu([dashboardMenuRoot, ...fromApi])
      this.loaded = true
    },
  },
})
