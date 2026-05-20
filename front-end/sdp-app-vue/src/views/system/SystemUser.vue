<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SysOrganization, SysRole, SysUser } from '@/api/types'
import {
  assignUserRoles,
  createUser,
  deleteUser,
  fetchOrgTree,
  fetchRoles,
  fetchUserRoleIds,
  fetchUsers,
  updateUser,
} from '@/api/admin'

const loading = ref(false)
const users = ref<SysUser[]>([])
const roleOptions = ref<SysRole[]>([])
const orgTree = ref<SysOrganization[]>([])

const dialogVisible = ref(false)
const assignVisible = ref(false)
const editingId = ref<number | null>(null)
const assigningUserId = ref<number | null>(null)
const selectedRoleIds = ref<number[]>([])

const form = reactive<SysUser>({
  username: '',
  password: '',
  displayName: '',
  status: 1,
  deptId: undefined,
})

async function load() {
  loading.value = true
  try {
    users.value = await fetchUsers()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadMeta() {
  try {
    const [roles, orgs] = await Promise.all([fetchRoles(), fetchOrgTree()])
    roleOptions.value = roles
    orgTree.value = orgs
  } catch {
    /* ignore */
  }
}

function openCreate() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.displayName = ''
  form.status = 1
  form.deptId = undefined
  dialogVisible.value = true
}

function openEdit(row: SysUser) {
  editingId.value = row.id ?? null
  form.username = row.username
  form.password = ''
  form.displayName = row.displayName ?? ''
  form.status = row.status ?? 1
  form.deptId = row.deptId ?? undefined
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value == null && !form.password?.trim()) {
    ElMessage.warning('请输入密码')
    return
  }
  try {
    if (editingId.value != null) {
      const payload: SysUser = {
        username: form.username,
        displayName: form.displayName,
        status: form.status,
        deptId: form.deptId,
      }
      if (form.password?.trim()) {
        payload.password = form.password
      }
      await updateUser(editingId.value, payload)
      ElMessage.success('已保存')
    } else {
      await createUser({
        username: form.username,
        password: form.password,
        displayName: form.displayName,
        status: form.status,
        deptId: form.deptId,
      })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function remove(row: SysUser) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '确认', { type: 'warning' })
    await deleteUser(row.id!)
    ElMessage.success('已删除')
    await load()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

async function openAssign(row: SysUser) {
  assigningUserId.value = row.id!
  assignVisible.value = true
  try {
    selectedRoleIds.value = await fetchUserRoleIds(row.id!)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载角色失败')
  }
}

async function saveAssign() {
  if (assigningUserId.value == null) return
  try {
    await assignUserRoles(assigningUserId.value, selectedRoleIds.value)
    ElMessage.success('角色已更新')
    assignVisible.value = false
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  }
}

onMounted(async () => {
  await loadMeta()
  await load()
})
</script>

<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">用户管理</span>
          <el-button type="primary" @click="openCreate">新增用户</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="users" border row-key="id">
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="displayName" label="显示名" min-width="120" />
        <el-table-column prop="deptId" label="组织 ID" width="100" />
        <el-table-column label="状态" width="88">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAssign(row)">分配角色</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="480px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="用户名" required>
          <el-input v-model.trim="form.username" maxlength="64" :disabled="editingId != null" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="editingId ? '新密码' : '密码'" :required="editingId == null">
          <el-input
            v-model.trim="form.password"
            type="password"
            maxlength="128"
            show-password
            autocomplete="new-password"
            :placeholder="editingId ? '留空则不修改' : '必填'"
          />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model.trim="form.displayName" maxlength="128" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-tree-select
            v-model="form.deptId"
            class="w-full"
            :data="orgTree"
            check-strictly
            clearable
            :props="{ value: 'id', label: 'orgName', children: 'children' }"
            placeholder="可选"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="分配角色" width="440px" destroy-on-close>
      <el-select v-model="selectedRoleIds" multiple class="w-full" placeholder="选择角色">
        <el-option v-for="r in roleOptions" :key="r.id!" :label="r.roleName" :value="r.id!" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.w-full {
  width: 100%;
}
</style>
