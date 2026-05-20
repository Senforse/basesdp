<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Close, Expand, Fold } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import SideMenu from '@/components/menus/SideMenu.vue'
import { useLayoutTabsStore } from '@/stores/layoutTabs'
import type { MenuItem } from '@/stores/menu'
import { useMenuStore } from '@/stores/menu'
import { usePermissionStore } from '@/stores/permission'
import http from '@/api/http'

const menuStore = useMenuStore()
const permissionStore = usePermissionStore()
const layoutTabs = useLayoutTabsStore()
const router = useRouter()
const route = useRoute()
const isCollapse = ref(false)
const themes = ['dark', 'light', 'ocean'] as const
type ThemeName = (typeof themes)[number]
const theme = ref<ThemeName>('dark')

const themeLabel = computed(() => {
  if (theme.value === 'dark') return '暗黑'
  if (theme.value === 'light') return '亮白'
  return '大海蓝'
})

function applyTheme(nextTheme: ThemeName) {
  theme.value = nextTheme
  document.documentElement.classList.remove('dark')
  document.documentElement.setAttribute('data-theme', nextTheme)
  localStorage.setItem('sdp-theme', nextTheme)
}

function toggleTheme() {
  const idx = themes.indexOf(theme.value)
  const next = themes[(idx + 1) % themes.length]
  applyTheme(next)
}

function titleForPath(path: string, tree: MenuItem[]): string {
  for (const n of tree) {
    if (n.path === path && n.meta?.title) return n.meta.title
    if (n.children?.length) {
      const sub = titleForPath(path, n.children)
      if (sub) return sub
    }
  }
  return ''
}

watch(
  () => route.fullPath,
  (fullPath) => {
    if (!fullPath || fullPath.startsWith('/login')) return
    let title = titleForPath(fullPath, menuStore.menuTree)
    if (!title) {
      const parts = fullPath.split('/').filter(Boolean)
      title = parts.length ? parts[parts.length - 1] : fullPath
    }
    layoutTabs.addTab(fullPath, title)
  },
  { immediate: true },
)

const activeTabPath = computed(() => route.fullPath)

function goTab(tabPath: string) {
  if (tabPath !== route.fullPath) router.push(tabPath)
}

function closeTab(tabPath: string, e: MouseEvent) {
  e.stopPropagation()
  if (layoutTabs.tabs.length <= 1) return
  const idx = layoutTabs.tabs.findIndex((t) => t.path === tabPath)
  if (idx === -1) return
  const wasActive = route.fullPath === tabPath
  layoutTabs.removeTab(tabPath)
  if (wasActive) {
    const next = layoutTabs.tabs[idx - 1] ?? layoutTabs.tabs[idx] ?? layoutTabs.tabs[0]
    if (next) router.push(next.path)
  }
}

const displayName = computed(() => {
  try {
    const raw = localStorage.getItem('user')
    if (!raw) return ''
    const u = JSON.parse(raw) as { displayName?: string; username?: string }
    return u.displayName || u.username || ''
  } catch {
    return ''
  }
})

const logout = async () => {
  try {
    await http.post('/api/auth/logout')
  } catch {
    /* ignore */
  }
  localStorage.removeItem('token')
  localStorage.removeItem('tokenHeaderName')
  localStorage.removeItem('user')
  menuStore.loaded = false
  menuStore.setMenu([])
  permissionStore.clear()
  layoutTabs.clear()
  await router.push('/login')
}

onMounted(() => {
  const cached = localStorage.getItem('sdp-theme') as ThemeName | null
  applyTheme(cached && themes.includes(cached) ? cached : 'dark')
})
</script>

<template>
  <el-container class="sdp-layout">
    <el-aside class="sdp-aside" :width="isCollapse ? '72px' : '236px'">
      <div class="sdp-brand">
        <span v-if="!isCollapse" class="sdp-brand-text">SDP 控制台</span>
        <span v-else class="sdp-brand-mini">S</span>
      </div>
      <SideMenu :menu-data="menuStore.menuTree" :collapse="isCollapse" />
    </el-aside>
    <el-container class="sdp-main-wrap">
      <el-header class="sdp-header">
        <el-button class="sdp-collapse-btn" text @click="isCollapse = !isCollapse">
          <el-icon :size="20">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </el-button>
        <div class="sdp-header-right">
          <el-button class="sdp-theme-btn" circle size="small" @click="toggleTheme">
            {{ themeLabel.slice(0, 1) }}
          </el-button>
          <span class="sdp-user">{{ displayName }}</span>
          <el-button type="primary" plain size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <div class="sdp-tabs-bar">
        <div class="sdp-tabs-scroll">
          <button
            v-for="tab in layoutTabs.tabs"
            :key="tab.path"
            type="button"
            class="sdp-tab"
            :class="{ 'is-active': tab.path === activeTabPath }"
            @click="goTab(tab.path)"
          >
            <span class="sdp-tab-title">{{ tab.title }}</span>
            <span
              v-if="layoutTabs.tabs.length > 1"
              class="sdp-tab-close"
              role="button"
              tabindex="-1"
              @click="closeTab(tab.path, $event)"
            >
              <el-icon :size="12"><Close /></el-icon>
            </span>
          </button>
        </div>
      </div>

      <el-main class="sdp-main">
        <router-view v-slot="{ Component }">
          <keep-alive :max="12">
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
.sdp-layout {
  height: 100vh;
  background: var(--sdp-bg-deep, #070b14);
}
.sdp-aside {
  display: flex;
  flex-direction: column;
  background: var(--surface-color-alt);
  border-right: 1px solid var(--primary-soft);
  transition: width 0.2s ease;
}
.sdp-brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-color);
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid var(--primary-soft);
}
.sdp-brand-text {
  font-size: 15px;
  background: linear-gradient(90deg, var(--primary-color), var(--brand-accent));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.sdp-brand-mini {
  font-size: 18px;
  color: var(--primary-color);
}
.sdp-main-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.sdp-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px !important;
  padding: 0 16px;
  background: var(--surface-color);
  border-bottom: 1px solid var(--primary-soft);
  backdrop-filter: blur(8px);
}
.sdp-collapse-btn {
  color: var(--text-weak) !important;
}
.sdp-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sdp-theme-btn {
  color: var(--text-color);
  border-color: var(--panel-border);
  background: var(--panel-bg);
}
.sdp-user {
  color: var(--text-muted);
  font-size: 14px;
}

.sdp-tabs-bar {
  flex-shrink: 0;
  padding: 8px 12px 0;
  background: var(--surface-color-alt);
  border-bottom: 1px solid var(--primary-soft);
}
.sdp-tabs-scroll {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: thin;
}
.sdp-tabs-scroll::-webkit-scrollbar {
  height: 4px;
}
.sdp-tabs-scroll::-webkit-scrollbar-thumb {
  background: var(--primary-soft);
  border-radius: 4px;
}
.sdp-tab {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 200px;
  padding: 6px 10px;
  border: 1px solid var(--panel-border);
  border-radius: 8px 8px 0 0;
  background: var(--surface-color);
  color: var(--text-weak);
  font-size: 13px;
  cursor: pointer;
  transition:
    background 0.15s,
    border-color 0.15s,
    color 0.15s;
}
.sdp-tab:hover {
  background: var(--primary-soft);
  border-color: var(--primary-color);
}
.sdp-tab.is-active {
  background: var(--surface-color-alt);
  border-color: var(--primary-color);
  color: var(--text-color);
  box-shadow: inset 0 -2px 0 var(--primary-color);
}
.sdp-tab-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.sdp-tab-close {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
  border-radius: 4px;
  opacity: 0.65;
  color: inherit;
}
.sdp-tab-close:hover {
  opacity: 1;
  background: var(--primary-soft);
}

.sdp-main {
  flex: 1;
  min-height: 0;
  overflow: auto;
  background: var(--sdp-bg-deep, #070b14);
  padding: 16px;
}
</style>
