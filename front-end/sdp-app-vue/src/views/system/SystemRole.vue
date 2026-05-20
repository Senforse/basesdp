<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SysMenu, SysRole } from '@/api/types'
import {
  assignRoleMenus,
  createRole,
  deleteRole,
  fetchMenuAdminTree,
  fetchRoleMenuIds,
  fetchRoles,
  updateRole,
} from '@/api/admin'

const loading = ref(false)
const roles = ref<SysRole[]>([])
const dialogVisible = ref(false)
const assignVisible = ref(false)
const editingId = ref<number | null>(null)
const assigningRoleId = ref<number | null>(null)

const menuTree = ref<SysMenu[]>([])
const menuTreeRef = ref<{
  getCheckedKeys: (leafOnly?: boolean) => Array<number | string>
  getHalfCheckedKeys: () => Array<number | string>
  setCheckedKeys: (keys: Array<number | string>, leafOnly?: boolean) => void
}>()

const form = reactive<SysRole>({
  roleCode: '',
  roleName: '',
  sortOrder: 0,
  status: 1,
})

async function load() {
  loading.value = true
  try {
    roles.value = await fetchRoles()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.roleCode = ''
  form.roleName = ''
  form.sortOrder = 0
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row: SysRole) {
  editingId.value = row.id ?? null
  form.roleCode = row.roleCode
  form.roleName = row.roleName
  form.sortOrder = row.sortOrder ?? 0
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function submit() {
  try {
    if (editingId.value != null) {
      await updateRole(editingId.value, { ...form, id: editingId.value })
      ElMessage.success('已保存')
    } else {
      await createRole({ ...form })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function remove(row: SysRole) {
  try {
    await ElMessageBox.confirm(`确定删除角色「${row.roleName}」？`, '确认', { type: 'warning' })
    await deleteRole(row.id!)
    ElMessage.success('已删除')
    await load()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

async function openAssign(row: SysRole) {
  assigningRoleId.value = row.id!
  assignVisible.value = true
  try {
    menuTree.value = await fetchMenuAdminTree()
    const ids = await fetchRoleMenuIds(row.id!)
    await nextTick()
    menuTreeRef.value?.setCheckedKeys(ids, false)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载菜单失败')
  }
}

async function saveAssign() {
  const tree = menuTreeRef.value
  if (!tree || assigningRoleId.value == null) return
  try {
    const checked = tree.getCheckedKeys(false) as number[]
    const half = tree.getHalfCheckedKeys() as number[]
    const merged = [...new Set([...half, ...checked])]
    await assignRoleMenus(assigningRoleId.value, merged)
    ElMessage.success('菜单权限已更新')
    assignVisible.value = false
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '保存失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">角色管理</span>
          <el-button type="primary" @click="openCreate">新增角色</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="roles" border row-key="id">
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column prop="roleCode" label="编码" min-width="140" />
        <el-table-column prop="roleName" label="名称" min-width="140" />
        <el-table-column prop="sortOrder" label="排序" width="88" />
        <el-table-column label="状态" width="88">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAssign(row)">分配菜单</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="480px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="编码" required>
          <el-input v-model.trim="form.roleCode" maxlength="64" :disabled="editingId != null" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model.trim="form.roleName" maxlength="128" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" class="w-full" />
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

    <el-dialog v-model="assignVisible" title="分配菜单" width="520px" destroy-on-close @closed="menuTree = []">
      <el-scrollbar max-height="420px">
        <el-tree
          ref="menuTreeRef"
          :data="menuTree"
          show-checkbox
          node-key="id"
          default-expand-all
          :props="{ label: 'menuName', children: 'children' }"
        />
      </el-scrollbar>
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
