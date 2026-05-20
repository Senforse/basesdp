import { defineStore } from 'pinia'
import { fetchMyPermissions } from '@/api/admin'

function normalize(code: string): string {
  return code.trim()
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    permissions: [] as string[],
    loaded: false,
  }),
  actions: {
    setPermissions(codes: string[]) {
      const unique = new Set<string>()
      for (const code of codes || []) {
        const normalized = normalize(code)
        if (normalized) unique.add(normalized)
      }
      this.permissions = [...unique]
      this.loaded = true
    },
    clear() {
      this.permissions = []
      this.loaded = false
    },
    async loadFromApi() {
      const codes = await fetchMyPermissions()
      this.setPermissions(codes)
    },
    hasPerm(code?: string) {
      if (!code) return true
      if (this.permissions.includes('*')) return true
      return this.permissions.includes(normalize(code))
    },
    hasAnyPerm(codes: string[]) {
      if (!codes?.length) return true
      return codes.some((code) => this.hasPerm(code))
    },
    hasAllPerm(codes: string[]) {
      if (!codes?.length) return true
      return codes.every((code) => this.hasPerm(code))
    },
  },
})
