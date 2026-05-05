<template>
  <div class="page-wrapper">
    <div class="page-card">
      <div class="page-card-header">
        <div class="page-card-title">
          <div class="title-dot"></div>
          <span>备份历史</span>
        </div>
      </div>

      <div class="search-bar">
        <el-input v-model="queryForm.taskId" placeholder="任务ID" clearable style="width: 200px" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" class="modern-table">
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="backupType" label="备份类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.backupType === 'FULL'" type="primary">全量</el-tag>
            <el-tag v-else type="warning">部分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <span class="status-badge status-success" v-if="scope.row.status === 'SUCCESS'">
              <span class="status-dot"></span> 成功
            </span>
            <span class="status-badge status-failed" v-else-if="scope.row.status === 'FAILED'">
              <span class="status-dot"></span> 失败
            </span>
            <span class="status-badge status-running" v-else>
              <span class="status-dot pulse"></span> 进行中
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="filePath" label="文件路径" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="scope">
            {{ formatSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @change="loadData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageHistory, deleteHistory } from '../api/history.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryForm = ref({ pageNum: 1, pageSize: 10, taskId: '' })

const formatSize = (size) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / (1024 * 1024)).toFixed(2) + ' MB'
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryForm.value }
    if (params.taskId) params.taskId = Number(params.taskId)
    else params.taskId = null
    const res = await pageHistory(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该记录?', '提示', { type: 'warning' }).then(async () => {
    await deleteHistory(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(loadData)
</script>

<style scoped>
.page-wrapper {
  animation: fadeIn 0.4s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.page-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
.page-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.title-dot {
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.modern-table {
  --el-table-border-color: transparent;
}
.modern-table :deep(.el-table__row) {
  transition: background 0.2s ease;
}
.modern-table :deep(.el-table__row:hover td) {
  background: #f8f9fe !important;
}
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}
.status-success {
  background: rgba(86, 171, 47, 0.1);
  color: #56ab2f;
}
.status-failed {
  background: rgba(235, 51, 73, 0.1);
  color: #eb3349;
}
.status-running {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
.status-dot.pulse {
  animation: pulse 1.5s ease infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
