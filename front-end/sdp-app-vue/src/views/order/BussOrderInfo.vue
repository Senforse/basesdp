<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createBussOrderInfo, deleteBussOrderInfo, fetchBussOrderInfoList, updateBussOrderInfo, type BussOrderInfo } from '@/api/generated/bussOrderInfo'

const loading = ref(false)
const list = ref<BussOrderInfo[]>([])
const dialogVisible = ref(false)
const editingId = ref<string | number | null>(null)
const addPerm = 'business:buss_order_info:add'
const updatePerm = 'business:buss_order_info:update'
const deletePerm = 'business:buss_order_info:delete'

const form = reactive<BussOrderInfo>({
  id: undefined,
  creator: '',
  createTime: '',
  updater: '',
  updateTime: '',
  orderNo: '',
})

async function load() {
  loading.value = true
  try {
    list.value = await fetchBussOrderInfoList()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    id: undefined,
    creator: '',
    createTime: '',
    updater: '',
    updateTime: '',
    orderNo: '',
  })
  dialogVisible.value = true
}

function openEdit(row: BussOrderInfo) {
  editingId.value = (row.id as string | number) ?? null
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function submit() {
  try {
    if (editingId.value != null) {
      await updateBussOrderInfo(editingId.value, { ...form })
      ElMessage.success('已保存')
    } else {
      await createBussOrderInfo({ ...form })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function remove(row: BussOrderInfo) {
  try {
    await ElMessageBox.confirm('确定删除该记录？', '确认', { type: 'warning' })
    await deleteBussOrderInfo(row.id as string | number)
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
          <span class="sdp-panel-title">BussOrderInfo 管理</span>
          <el-button v-perm="[addPerm]" type="primary" @click="openCreate">新增</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="list" border row-key="id">
        <el-table-column prop="id" label="主键" min-width="140" />
        <el-table-column prop="creator" label="创建者" min-width="140" />
        <el-table-column prop="createTime" label="创建时间" min-width="140" />
        <el-table-column prop="updater" label="更新者" min-width="140" />
        <el-table-column prop="updateTime" label="更新时间" min-width="140" />
        <el-table-column prop="orderNo" label="订单号" min-width="140" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="[updatePerm]" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-perm="[deletePerm]" link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑' : '新增'" width="520px" destroy-on-close>
      <el-form label-width="100px">
        
        
        <el-form-item label="创建者">
          <el-input v-model.trim="form.creator" />
        </el-form-item>
        
        
        <el-form-item label="创建时间">
          <el-date-picker v-model="form.createTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
        </el-form-item>
        
        
        <el-form-item label="更新者">
          <el-input v-model.trim="form.updater" />
        </el-form-item>
        
        
        <el-form-item label="更新时间">
          <el-date-picker v-model="form.updateTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
        </el-form-item>
        
        
        <el-form-item label="订单号">
          <el-input v-model.trim="form.orderNo" />
        </el-form-item>
        
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="[editingId ? updatePerm : addPerm]" type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.w-full {
  width: 100%;
}
</style>
