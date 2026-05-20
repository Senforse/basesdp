<script setup lang="ts">
import * as ElementIcons from '@element-plus/icons-vue'
import SideMenuNode from './SideMenuNode.vue'
import type { MenuItem } from '@/stores/menu'

defineProps<{
  items: MenuItem[]
}>()

function resolveIcon(name?: string) {
  if (!name) return ElementIcons.Menu
  const ic = (ElementIcons as Record<string, typeof ElementIcons.Menu>)[name]
  return ic || ElementIcons.Menu
}
</script>

<template>
  <template v-for="item in items" :key="item.path + (item.meta?.title ?? '')">
    <el-sub-menu v-if="item.children?.length" :index="item.path">
      <template #title>
        <el-icon><component :is="resolveIcon(item.meta?.icon)" /></el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>
      <SideMenuNode :items="item.children" />
    </el-sub-menu>
    <el-menu-item v-else :index="item.path">
      <el-icon><component :is="resolveIcon(item.meta?.icon)" /></el-icon>
      <template #title>{{ item.meta?.title }}</template>
    </el-menu-item>
  </template>
</template>
