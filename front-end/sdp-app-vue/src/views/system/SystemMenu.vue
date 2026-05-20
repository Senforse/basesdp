<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SysMenu } from '@/api/types'
import { createMenu, deleteMenu, fetchMenuAdminTree, updateMenu } from '@/api/admin'
import { inspectMenuComponent, validateMenuComponentResolvable } from '@/stores/menu'

const loading = ref(false)
const treeData = ref<SysMenu[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const menuEditPerm = 'system:menu:edit'

const form = reactive<SysMenu>({
  parentId: 0,
  menuName: '',
  path: '',
  component: '',
  perms: '',
  menuType: 1,
  icon: '',
  sortOrder: 0,
  visible: 1,
  status: 1,
})

const componentHint = computed(() => {
  if (form.menuType !== 1) return null
  return inspectMenuComponent(form.component ?? undefined)
})

async function load() {
  loading.value = true
  try {
    treeData.value = await fetchMenuAdminTree()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number) {
  editingId.value = null
  form.parentId = parentId
  form.menuName = ''
  form.path = ''
  form.component = ''
  form.perms = ''
  form.menuType = 1
  form.icon = ''
  form.sortOrder = 0
  form.visible = 1
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row: SysMenu) {
  editingId.value = row.id ?? null
  form.parentId = row.parentId ?? 0
  form.menuName = row.menuName
  form.path = row.path ?? ''
  form.component = row.component ?? ''
  form.perms = row.perms ?? ''
  form.menuType = row.menuType ?? 1
  form.icon = row.icon ?? ''
  form.sortOrder = row.sortOrder ?? 0
  form.visible = row.visible ?? 1
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function submit() {
  if (form.menuType === 1) {
    if (!form.path?.trim()) {
      ElMessage.error('菜单类型为“菜单”时，路由路径不能为空')
      return
    }
    if (!form.path.startsWith('/')) {
      ElMessage.error('路由路径必须以 / 开头，例如 /order/buss-order-info')
      return
    }
    const checked = validateMenuComponentResolvable(form.component ?? undefined)
    if (!checked.ok) {
      ElMessage.error(checked.message ?? 'component 配置无效')
      return
    }
  }
  try {
    if (editingId.value != null) {
      await updateMenu(editingId.value, { ...form, id: editingId.value })
      ElMessage.success('已保存')
    } else {
      await createMenu({ ...form })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function remove(row: SysMenu) {
  try {
    await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」？`, '确认', { type: 'warning' })
    await deleteMenu(row.id!)
    ElMessage.success('已删除')
    await load()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

function menuTypeLabel(t?: number) {
  if (t === 0) return '目录'
  if (t === 1) return '菜单'
  if (t === 2) return '按钮'
  return String(t ?? '')
}

onMounted(load)
</script>

<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">菜单管理</span>
          <el-button v-perm="[menuEditPerm]" type="primary" @click="openCreate(0)">新增顶级</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        row-key="id"
        :data="treeData"
        border
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="menuName" label="名称" min-width="160" />
        <el-table-column prop="path" label="路由路径" min-width="140" />
        <el-table-column prop="component" label="前端组件" min-width="140" />
        <el-table-column prop="perms" label="权限标识" min-width="160" />
        <el-table-column label="类型" width="88">
          <template #default="{ row }">{{ menuTypeLabel(row.menuType) }}</template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="72" />
        <el-table-column label="可见" width="72">
          <template #default="{ row }">{{ row.visible === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="72">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="[menuEditPerm]" link type="primary" @click="openCreate(row.id!)">新增子级</el-button>
            <el-button v-perm="[menuEditPerm]" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-perm="[menuEditPerm]" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜单' : '新增菜单'" width="520px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="上级菜单 ID">
          <el-input-number v-model="form.parentId" :min="0" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model.trim="form.menuName" maxlength="128" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model.trim="form.path" maxlength="256" placeholder="目录/菜单路由，可为空" />
        </el-form-item>
        <el-form-item label="组件名">
          <el-input v-model.trim="form.component" maxlength="128" placeholder="如 SystemOrg，按钮可为空" />
          <div v-if="componentHint" class="component-hint">
            <el-text :type="componentHint.ok ? 'success' : componentHint.status === 'empty' ? 'info' : 'danger'" size="small">
              {{ componentHint.message }}
            </el-text>
            <div v-if="componentHint.paths.length" class="component-hint-paths">
              <div v-for="p in componentHint.paths" :key="p" class="component-hint-path">{{ p }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model.trim="form.perms" maxlength="256" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.menuType" class="w-full">
            <el-option :value="0" label="目录" />
            <el-option :value="1" label="菜单" />
            <el-option :value="2" label="按钮" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model.trim="form.icon" maxlength="64" placeholder="Element Plus 图标名" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="显示">
          <el-radio-group v-model="form.visible">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
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
        <el-button v-perm="[menuEditPerm]" type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.w-full {
  width: 100%;
}

.component-hint {
  margin-top: 6px;
  width: 100%;
}

.component-hint-paths {
  margin-top: 4px;
  padding: 6px 8px;
  border-radius: 6px;
  background: var(--surface-color-alt);
  border: 1px solid var(--panel-border);
  font-size: 12px;
  line-height: 1.45;
  color: var(--text-muted);
  word-break: break-all;
}

.component-hint-path + .component-hint-path {
  margin-top: 2px;
}
</style>
