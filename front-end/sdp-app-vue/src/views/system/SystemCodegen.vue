<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createTableAndGenerate, downloadFrontendPageZip } from '@/api/admin'
import type { AutoCodeColumnPayload, AutoCodeGenerateResult } from '@/api/types'

const loading = ref(false)
const result = ref<AutoCodeGenerateResult | null>(null)

const form = reactive({
  tableName: '',
  tableComment: '',
  modulePackage: 'business',
})

const columns = ref<AutoCodeColumnPayload[]>([
  { columnName: 'id', comment: '主键', dbType: 'BIGINT', nullable: false, primaryKey: true },
  { columnName: 'creator', comment: '创建者', dbType: 'VARCHAR', length: 64, nullable: true, primaryKey: false },
  { columnName: 'create_time', comment: '创建时间', dbType: 'DATETIME', nullable: true, primaryKey: false },
  { columnName: 'updater', comment: '更新者', dbType: 'VARCHAR', length: 64, nullable: true, primaryKey: false },
  { columnName: 'update_time', comment: '更新时间', dbType: 'DATETIME', nullable: true, primaryKey: false },
])

const dbTypeOptions = ['BIGINT', 'INT', 'VARCHAR', 'CHAR', 'DECIMAL', 'DATETIME', 'DATE', 'TEXT']

function addColumn() {
  columns.value.push({
    columnName: '',
    comment: '',
    dbType: 'VARCHAR',
    length: 64,
    nullable: true,
    primaryKey: false,
  })
}

function removeColumn(index: number) {
  columns.value.splice(index, 1)
}

async function submit() {
  if (!form.tableName.trim()) {
    ElMessage.warning('请输入表名')
    return
  }
  if (columns.value.length === 0) {
    ElMessage.warning('请至少添加一个字段')
    return
  }
  if (!columns.value.some((c) => c.primaryKey)) {
    ElMessage.warning('请至少指定一个主键字段')
    return
  }

  const invalid = columns.value.find((c) => !c.columnName?.trim() || !c.dbType?.trim())
  if (invalid) {
    ElMessage.warning('字段名和字段类型不能为空')
    return
  }

  loading.value = true
  try {
    result.value = await createTableAndGenerate({
      tableName: form.tableName.trim(),
      tableComment: form.tableComment.trim(),
      modulePackage: form.modulePackage.trim() || 'business',
      columns: columns.value.map((c) => ({
        columnName: c.columnName.trim(),
        comment: c.comment?.trim(),
        dbType: c.dbType,
        length: c.length,
        scale: c.scale,
        nullable: c.nullable ?? true,
        primaryKey: c.primaryKey ?? false,
      })),
    })
    ElMessage.success('建表并生成成功')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '生成失败')
  } finally {
    loading.value = false
  }
}

async function downloadFrontendZip() {
  if (!result.value?.frontendZipToken) {
    ElMessage.warning('请先生成前端页面包')
    return
  }
  try {
    const blob = await downloadFrontendPageZip(result.value.frontendZipToken)
    const fileName = `frontend-page-${form.tableName || 'package'}.zip`
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '下载失败')
  }
}
</script>

<template>
  <div class="sdp-page">
    <el-card class="sdp-panel" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">代码生成</span>
          <div>
            <el-button
              type="success"
              plain
              :disabled="!result?.frontendZipToken"
              class="mr8"
              @click="downloadFrontendZip"
            >
              下载前端页面包
            </el-button>
            <el-button type="primary" :loading="loading" @click="submit">建表并生成 CRUD + 前端页面</el-button>
          </div>
        </div>
      </template>

      <el-form label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="表名" required>
              <el-input v-model.trim="form.tableName" placeholder="例如：biz_customer" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="表描述">
              <el-input v-model.trim="form.tableComment" placeholder="例如：客户信息表" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模块包名">
              <el-input v-model.trim="form.modulePackage" placeholder="默认 business" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" plain @click="addColumn">添加字段</el-button>
      </div>

      <el-table :data="columns" border>
        <el-table-column type="index" label="#" width="56" />
        <el-table-column label="字段名" min-width="150">
          <template #default="{ row }">
            <el-input v-model.trim="row.columnName" placeholder="如 id / create_time" />
          </template>
        </el-table-column>
        <el-table-column label="字段描述" min-width="150">
          <template #default="{ row }">
            <el-input v-model.trim="row.comment" />
          </template>
        </el-table-column>
        <el-table-column label="字段类型" width="120">
          <template #default="{ row }">
            <el-select v-model="row.dbType" class="w-full">
              <el-option v-for="type in dbTypeOptions" :key="type" :value="type" :label="type" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="长度" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.length" :min="1" controls-position="right" class="w-full" />
          </template>
        </el-table-column>
        <el-table-column label="小数位" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.scale" :min="0" controls-position="right" class="w-full" />
          </template>
        </el-table-column>
        <el-table-column label="主键" width="76">
          <template #default="{ row }">
            <el-checkbox v-model="row.primaryKey" />
          </template>
        </el-table-column>
        <el-table-column label="可为空" width="86">
          <template #default="{ row }">
            <el-checkbox v-model="row.nullable" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ $index }">
            <el-button link type="danger" @click="removeColumn($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="result" class="sdp-panel mt12" shadow="never">
      <template #header>
        <div class="sdp-panel-head">
          <span class="sdp-panel-title">生成结果</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Entity">{{ result.entityClassName }}</el-descriptions-item>
        <el-descriptions-item label="Mapper">{{ result.mapperClassName }}</el-descriptions-item>
        <el-descriptions-item label="Service">{{ result.serviceClassName }}</el-descriptions-item>
        <el-descriptions-item label="Controller">{{ result.controllerClassName }}</el-descriptions-item>
      </el-descriptions>
      <div class="mt12">
        <div class="sub-title">建表 SQL</div>
        <el-input :model-value="result.createTableSql" type="textarea" :rows="6" readonly />
      </div>
      <div class="mt12">
        <div class="sub-title">生成文件</div>
        <el-tag v-for="file in result.generatedFiles" :key="file" type="info" class="file-tag">{{ file }}</el-tag>
      </div>
      <div class="mt12">
        <div class="sub-title">前端页面包文件</div>
        <el-tag v-for="file in result.frontendGeneratedFiles" :key="file" type="success" class="file-tag">{{ file }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 12px;
}

.mr8 {
  margin-right: 8px;
}

.w-full {
  width: 100%;
}

.mt12 {
  margin-top: 12px;
}

.sub-title {
  margin-bottom: 8px;
  font-weight: 600;
}

.file-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}
</style>
