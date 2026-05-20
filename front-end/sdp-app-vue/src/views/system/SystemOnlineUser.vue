<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { OnlineUserVO } from '@/api/types'
import { fetchOnlineUsersPage, kickOnlineSession } from '@/api/admin'

const loading = ref(false)
const records = ref<OnlineUserVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filters = reactive({
  application: 'system',
  userCode: '',
})

async function load() {
  loading.value = true
  try {
    const res = await fetchOnlineUsersPage({
      application: filters.application.trim() || undefined,
      userCode: filters.userCode.trim() || undefined,
      page: page.value,
      size: pageSize.value,
    })
    records.value = res.records ?? []
    total.value = Number(res.total ?? 0)
    if (res.current) page.value = Number(res.current)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  void load()
}

function onClear() {
  filters.application = ''
  filters.userCode = ''
  page.value = 1
  void load()
}

function onPageChange(p: number) {
  page.value = p
  void load()
}

async function kick(row: OnlineUserVO) {
  if (!row.tokenValue) {
    ElMessage.warning('缺少会话标识，无法下线')
    return
  }
  const name = row.displayName?.trim() || row.username || '该用户'
  try {
    await ElMessageBox.confirm(`确定将「${name}」从当前会话下线？`, '下线确认', { type: 'warning' })
    await kickOnlineSession(row.tokenValue)
    ElMessage.success('已下线')
    await load()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '下线失败')
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <span class="sdp-panel-title">在线用户管理</span>
      </template>

      <div class="toolbar">
        <div class="toolbar-filters">
          <span class="toolbar-label">应用</span>
          <el-select v-model="filters.application" placeholder="请选择" clearable class="toolbar-app">
            <el-option label="system" value="system" />
          </el-select>
          <span class="toolbar-label">在线用户编码</span>
          <el-input v-model.trim="filters.userCode" placeholder="请输入" clearable class="toolbar-code" maxlength="64" />
        </div>
        <div class="toolbar-actions">
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onClear">清空</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="records" border row-key="tokenValue" class="online-table">
        <el-table-column
          type="index"
          label="序号"
          width="72"
          :index="(index: number) => (page - 1) * pageSize + index + 1"
        />
        <el-table-column prop="displayName" label="在线用户名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="username" label="在线用户工号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="userType" label="用户类型" width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.userType ?? '—' }}</template>
        </el-table-column>
        <el-table-column prop="application" label="应用" width="100" />
        <el-table-column prop="authType" label="授权类型" width="110" />
        <el-table-column prop="expireTime" label="到期时间" min-width="168" show-overflow-tooltip>
          <template #default="{ row }">{{ row.expireTime?.trim() ? row.expireTime : '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="!row.tokenValue" @click="kick(row)">下线</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="page"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.toolbar-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 12px;
}

.toolbar-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
}

.toolbar-app {
  width: 140px;
}

.toolbar-code {
  width: 200px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.online-table {
  width: 100%;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
