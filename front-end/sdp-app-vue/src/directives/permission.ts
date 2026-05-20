import type { App, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

function isAllowed(binding: DirectiveBinding): boolean {
  const permissionStore = usePermissionStore()
  const value = binding.value
  if (typeof value === 'string') {
    return permissionStore.hasPerm(value)
  }
  if (Array.isArray(value)) {
    return permissionStore.hasAnyPerm(value.filter((v): v is string => typeof v === 'string'))
  }
  return true
}

function applyPermission(el: HTMLElement, binding: DirectiveBinding) {
  const allowed = isAllowed(binding)
  el.style.display = allowed ? '' : 'none'
}

export function registerPermissionDirective(app: App) {
  app.directive('perm', {
    mounted(el, binding) {
      applyPermission(el as HTMLElement, binding)
    },
    updated(el, binding) {
      applyPermission(el as HTMLElement, binding)
    },
  })
}
