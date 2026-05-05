<template>
  <div class="page-wrapper">
    <div class="page-card">
      <div class="page-card-header">
        <div class="page-card-title">
          <div class="title-dot"></div>
          <span>数据源管理</span>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon style="margin-right: 4px"><Plus /></el-icon>新增数据源
        </el-button>
      </div>

      <div class="search-bar">
        <el-input v-model="queryForm.keyword" placeholder="名称/主机/数据库" clearable style="width: 260px" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" class="modern-table">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="host" label="主机" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="databaseName" label="数据库" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑数据源' : '新增数据源'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="form.host" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="form.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="数据库名" prop="databaseName">
          <el-input v-model="form.databaseName" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="handleTest">测试连接</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageDataSource, saveDataSource, updateDataSource, deleteDataSource, testConnection } from '../api/datasource.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryForm = ref({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({ name: '', host: '', port: 3306, databaseName: '', username: '', password: '', remark: '' })

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageDataSource(queryForm.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { name: '', host: '', port: 3306, databaseName: '', username: '', password: '', remark: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该数据源?', '提示', { type: 'warning' }).then(async () => {
    await deleteDataSource(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleTest = async () => {
  const res = await testConnection(form.value)
  if (res.data) {
    ElMessage.success('连接成功')
  } else {
    ElMessage.error('连接失败')
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateDataSource(form.value)
  } else {
    await saveDataSource(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
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
</style>
