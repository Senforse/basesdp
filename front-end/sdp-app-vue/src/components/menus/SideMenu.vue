<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SideMenuNode from './SideMenuNode.vue'
import type { MenuItem } from '@/stores/menu'

withDefaults(
  defineProps<{
    menuData: MenuItem[]
    collapse?: boolean
  }>(),
  {
    menuData: () => [],
    collapse: false,
  },
)

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<template>
  <el-menu
    class="sdp-side-menu"
    :default-active="activeMenu"
    :collapse="collapse"
    router
  >
    <SideMenuNode :items="menuData" />
  </el-menu>
</template>

<style lang="scss" scoped>
.sdp-side-menu:not(.el-menu--collapse) {
  width: 220px;
}
.sdp-side-menu.el-menu--collapse {
  width: 64px;
}
</style>
