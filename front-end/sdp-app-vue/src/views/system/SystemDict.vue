<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SysDictItem, SysDictType } from '@/api/types'
import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  fetchDictItemsByType,
  fetchDictTypes,
  updateDictItem,
  updateDictType,
} from '@/api/admin'

const loading = ref(false)
const dictTypes = ref<SysDictType[]>([])
const dictItems = ref<SysDictItem[]>([])

const typeDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const editingTypeId = ref<number | null>(null)
const editingItemId = ref<number | null>(null)

const searchForm = reactive({
  dictCode: '',
  dictName: '',
})

const typeForm = reactive<SysDictType>({
  dictCode: '',
  dictName: '',
  description: '',
  sortOrder: 0,
  status: 1,
})

const itemForm = reactive<SysDictItem>({
  dictTypeId: 0,
  dictCode: '',
  itemLabel: '',
  itemValue: '',
  sortOrder: 0,
  status: 1,
  description: '',
})

const selectedTypeId = ref<number | null>(null)
const selectedType = ref<SysDictType | null>(null)

const selectedItems = ref<number[]>([])

async function loadDictTypes() {
  loading.value = true
  try {
    let types = await fetchDictTypes()
    if (searchForm.dictCode) {
      types = types.filter(t => t.dictCode.includes(searchForm.dictCode))
    }
    if (searchForm.dictName) {
      types = types.filter(t => t.dictName.includes(searchForm.dictName))
    }
    dictTypes.value = types
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载字典类型失败')
  } finally {
    loading.value = false
  }
}

async function loadDictItems(typeId: number) {
  loading.value = true
  try {
    dictItems.value = await fetchDictItemsByType(typeId)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载字典项失败')
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchForm.dictCode = ''
  searchForm.dictName = ''
  loadDictTypes()
}

function openCreateType() {
  editingTypeId.value = null
  typeForm.dictCode = ''
  typeForm.dictName = ''
  typeForm.description = ''
  typeForm.sortOrder = 0
  typeForm.status = 1
  typeDialogVisible.value = true
}

function openEditType(row: SysDictType) {
  editingTypeId.value = row.id ?? null
  typeForm.dictCode = row.dictCode
  typeForm.dictName = row.dictName
  typeForm.description = row.description ?? ''
  typeForm.sortOrder = row.sortOrder ?? 0
  typeForm.status = row.status ?? 1
  typeDialogVisible.value = true
}

async function submitType() {
  try {
    if (editingTypeId.value != null) {
      await updateDictType(editingTypeId.value, { ...typeForm, id: editingTypeId.value })
      ElMessage.success('已保存')
    } else {
      await createDictType({ ...typeForm })
      ElMessage.success('已创建')
    }
    typeDialogVisible.value = false
    await loadDictTypes()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function removeType(row: SysDictType) {
  try {
    await ElMessageBox.confirm(`确定删除字典类型「${row.dictName}」？`, '确认', { type: 'warning' })
    await deleteDictType(row.id!)
    ElMessage.success('已删除')
    await loadDictTypes()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

function openItemDialog(row?: SysDictItem) {
  if (!selectedTypeId.value || !selectedType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  
  if (row) {
    editingItemId.value = row.id ?? null
    itemForm.dictTypeId = row.dictTypeId
    itemForm.dictCode = row.dictCode
    itemForm.itemLabel = row.itemLabel
    itemForm.itemValue = row.itemValue
    itemForm.sortOrder = row.sortOrder ?? 0
    itemForm.status = row.status ?? 1
    itemForm.description = row.description ?? ''
  } else {
    editingItemId.value = null
    itemForm.dictTypeId = selectedTypeId.value
    itemForm.dictCode = selectedType.value.dictCode
    itemForm.itemLabel = ''
    itemForm.itemValue = ''
    itemForm.sortOrder = 0
    itemForm.status = 1
    itemForm.description = ''
  }
  itemDialogVisible.value = true
}

async function submitItem() {
  try {
    if (editingItemId.value != null) {
      await updateDictItem(editingItemId.value, { ...itemForm, id: editingItemId.value })
      ElMessage.success('已保存')
    } else {
      await createDictItem({ ...itemForm })
      ElMessage.success('已创建')
    }
    itemDialogVisible.value = false
    if (selectedTypeId.value != null) {
      loadDictItems(selectedTypeId.value)
    }
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function removeItem(row: SysDictItem) {
  try {
    await ElMessageBox.confirm(`确定删除字典项「${row.itemLabel}」？`, '确认', { type: 'warning' })
    await deleteDictItem(row.id!)
    ElMessage.success('已删除')
    if (selectedTypeId.value != null) {
      loadDictItems(selectedTypeId.value)
    }
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

async function batchRemoveItems() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要删除的字典项')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedItems.value.length} 条字典项？`, '确认', { type: 'warning' })
    for (const id of selectedItems.value) {
      await deleteDictItem(id)
    }
    ElMessage.success('已批量删除')
    selectedItems.value = []
    if (selectedTypeId.value != null) {
      loadDictItems(selectedTypeId.value)
    }
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(e instanceof Error ? e.message : '删除失败')
  }
}

function selectType(row: SysDictType) {
  selectedTypeId.value = row.id!
  selectedType.value = row
  selectedItems.value = []
  loadDictItems(row.id!)
}

loadDictTypes()
</script>

<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">数据字典类型</span>
          <el-button type="primary" @click="openCreateType">+新增</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.dictCode" placeholder="字典编码" class="search-input" @keyup.enter="loadDictTypes" />
        <el-input v-model="searchForm.dictName" placeholder="字典名称" class="search-input" @keyup.enter="loadDictTypes" />
        <el-button type="primary" @click="loadDictTypes">查询</el-button>
        <el-button @click="resetSearch">清空</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="dictTypes"
        border
        row-key="id"
        :highlight-current-row="true"
        :current-row-key="selectedTypeId"
        @row-click="selectType"
      >
        <el-table-column prop="id" label="序号" width="80" />
        <el-table-column prop="dictCode" label="字典编码" min-width="140" />
        <el-table-column prop="dictName" label="字典名称" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column prop="sortOrder" label="排序" width="88" />
        <el-table-column label="状态" width="88">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditType(row)">修改</el-button>
            <el-button link type="danger" @click="removeType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="sdp-panel mt-20" shadow="never" v-if="selectedType">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">{{ selectedType.dictName }} - 字典项列表</span>
          <div>
            <el-button type="primary" @click="openItemDialog()">+新增</el-button>
            <el-button :disabled="selectedItems.length === 0" @click="batchRemoveItems">批量删除</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="dictItems"
        border
        row-key="id"
        :selection-change="(val: number[]) => selectedItems = val"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="序号" width="80" />
        <el-table-column prop="itemLabel" label="字典标签" min-width="140" />
        <el-table-column prop="itemValue" label="字典键值" min-width="140" />
        <el-table-column prop="sortOrder" label="字典排序" width="100" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column label="状态" width="88">
          <template #default="{ row }">{{ row.status === 1 ? '正常' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openItemDialog(row)">修改</el-button>
            <el-button link type="danger" @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="dictItems.length > 0">
        <span class="total">共 {{ dictItems.length }} 条</span>
        <el-pagination
          :page-size="10"
          :total="dictItems.length"
          layout="prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <el-card class="sdp-panel mt-20" shadow="never" v-else>
      <el-empty description="请先选择字典类型" />
    </el-card>

    <el-dialog v-model="typeDialogVisible" :title="editingTypeId ? '修改字典类型' : '新增字典类型'" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="字典编码" required>
          <el-input v-model.trim="typeForm.dictCode" maxlength="64" :disabled="editingTypeId != null" />
        </el-form-item>
        <el-form-item label="字典名称" required>
          <el-input v-model.trim="typeForm.dictName" maxlength="128" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model.trim="typeForm.description" maxlength="256" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="typeForm.sortOrder" :min="0" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitType">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialogVisible" :title="editingItemId ? '修改字典项' : '新增字典项'" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="字典标签" required>
          <el-input v-model.trim="itemForm.itemLabel" maxlength="128" />
        </el-form-item>
        <el-form-item label="字典键值" required>
          <el-input v-model.trim="itemForm.itemValue" maxlength="128" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model.trim="itemForm.description" maxlength="256" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="itemForm.sortOrder" :min="0" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="itemForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.search-input {
  width: 200px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeeef;
}

.total {
  color: #606266;
}

.w-full {
  width: 100%;
}

.mt-20 {
  margin-top: 20px;
}
</style>