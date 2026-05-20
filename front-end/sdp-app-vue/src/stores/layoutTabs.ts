import { defineStore } from 'pinia'

export interface LayoutTab {
  path: string
  title: string
}

export const useLayoutTabsStore = defineStore('layoutTabs', {
  state: () => ({
    tabs: [] as LayoutTab[],
  }),
  actions: {
    clear() {
      this.tabs = []
    },
    addTab(path: string, title: string) {
      const t = title?.trim() || path
      const hit = this.tabs.find((x) => x.path === path)
      if (hit) {
        hit.title = t
        return
      }
      this.tabs.push({ path, title: t })
    },
    removeTab(path: string) {
      const i = this.tabs.findIndex((x) => x.path === path)
      if (i !== -1) this.tabs.splice(i, 1)
    },
  },
})
