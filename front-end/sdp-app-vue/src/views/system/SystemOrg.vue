<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SysOrganization } from '@/api/types'
import { createOrg, deleteOrg, fetchOrgTree, updateOrg } from '@/api/admin'

const loading = ref(false)
const treeData = ref<SysOrganization[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)

const form = reactive<SysOrganization>({
  parentId: 0,
  orgCode: '',
  orgName: '',
  sortOrder: 0,
  status: 1,
})

async function load() {
  loading.value = true
  try {
    treeData.value = await fetchOrgTree()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number) {
  editingId.value = null
  form.parentId = parentId
  form.orgCode = ''
  form.orgName = ''
  form.sortOrder = 0
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row: SysOrganization) {
  editingId.value = row.id ?? null
  form.parentId = row.parentId ?? 0
  form.orgCode = row.orgCode
  form.orgName = row.orgName
  form.sortOrder = row.sortOrder ?? 0
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function submit() {
  try {
    if (editingId.value != null) {
      await updateOrg(editingId.value, { ...form, id: editingId.value })
      ElMessage.success('已保存')
    } else {
      await createOrg({ ...form })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function remove(row: SysOrganization) {
  try {
    await ElMessageBox.confirm(`确定删除组织「${row.orgName}」？`, '确认', { type: 'warning' })
    await deleteOrg(row.id!)
    ElMessage.success('已删除')
    await load()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">组织架构</span>
          <el-button type="primary" @click="openCreate(0)">新增根组织</el-button>
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
        <el-table-column prop="orgCode" label="编码" min-width="140" />
        <el-table-column prop="orgName" label="名称" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="88" />
        <el-table-column prop="status" label="状态" width="88">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openCreate(row.id!)">新增子级</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑组织' : '新增组织'" width="480px" destroy-on-close>
      <el-form label-width="88px">
        <el-form-item label="上级 ID">
          <el-input-number v-model="form.parentId" :min="0" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model.trim="form.orgCode" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model.trim="form.orgName" maxlength="128" show-word-limit />
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
  </div>
</template>

<style lang="scss" scoped>
.w-full {
  width: 100%;
}
</style>
