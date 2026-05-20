<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const currentPath = computed(() => route.fullPath || route.path || '/')

function goHome() {
  router.push('/dashboard/home')
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="not-found-page">
    <div class="not-found-card">
      <h1 class="code">404</h1>
      <p class="title">页面不存在或路由未配置</p>
      <p class="desc">
        当前地址：<code>{{ currentPath }}</code>
      </p>
      <div class="actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.not-found-page {
  min-height: calc(100vh - 56px);
  display: grid;
  place-items: center;
  padding: 24px;
}

.not-found-card {
  width: min(680px, 100%);
  background: var(--surface-color, #121826);
  border: 1px solid var(--panel-border, rgba(255, 255, 255, 0.1));
  border-radius: 14px;
  padding: 32px 28px;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.22);
}

.code {
  margin: 0;
  font-size: 64px;
  line-height: 1;
  letter-spacing: 2px;
  color: var(--primary-color, #409eff);
}

.title {
  margin: 16px 0 8px;
  font-size: 20px;
  color: var(--text-color, #fff);
}

.desc {
  margin: 0;
  color: var(--text-muted, #a9b4c5);
  word-break: break-all;
}

.actions {
  margin-top: 22px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>