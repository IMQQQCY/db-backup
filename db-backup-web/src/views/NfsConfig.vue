<template>
  <div class="page-wrapper">
    <div class="page-card">
      <div class="page-card-header">
        <div class="page-card-title">
          <div class="title-dot"></div>
          <span>NFS 配置</span>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon style="margin-right: 4px"><Plus /></el-icon>新增配置
        </el-button>
      </div>

      <div class="search-bar">
        <el-input v-model="queryForm.keyword" placeholder="名称" clearable style="width: 200px" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" class="modern-table">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="server" label="服务器" />
        <el-table-column prop="remotePath" label="远程路径" />
        <el-table-column prop="localMountPoint" label="本地挂载点" />
        <el-table-column prop="options" label="挂载选项" />
        <el-table-column prop="mounted" label="挂载状态" width="100">
          <template #default="scope">
            <span class="status-badge status-success" v-if="scope.row.mounted">
              <span class="status-dot"></span> 已挂载
            </span>
            <span class="status-badge status-unmounted" v-else>
              <span class="status-dot"></span> 未挂载
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="scope">
            <el-button size="small" type="success" v-if="!scope.row.mounted" @click="handleMount(scope.row)">挂载</el-button>
            <el-button size="small" type="warning" v-if="scope.row.mounted" @click="handleUnmount(scope.row)">卸载</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑NFS配置' : '新增NFS配置'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="服务器地址" prop="server">
          <el-input v-model="form.server" placeholder="如: 192.168.1.100" />
        </el-form-item>
        <el-form-item label="远程路径" prop="remotePath">
          <el-input v-model="form.remotePath" placeholder="如: /data/backup" />
        </el-form-item>
        <el-form-item label="本地挂载点" prop="localMountPoint">
          <el-input v-model="form.localMountPoint" placeholder="如: /mnt/nfs" />
        </el-form-item>
        <el-form-item label="挂载选项">
          <el-input v-model="form.options" placeholder="如: rw,soft,intr" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageNfs, saveNfs, updateNfs, deleteNfs, mountNfs, unmountNfs } from '../api/nfs.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryForm = ref({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({ name: '', server: '', remotePath: '', localMountPoint: '', options: '', remark: '' })

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  server: [{ required: true, message: '请输入服务器地址', trigger: 'blur' }],
  remotePath: [{ required: true, message: '请输入远程路径', trigger: 'blur' }],
  localMountPoint: [{ required: true, message: '请输入本地挂载点', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageNfs(queryForm.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  form.value = { name: '', server: '', remotePath: '', localMountPoint: '', options: '', remark: '' }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该配置?', '提示', { type: 'warning' }).then(async () => {
    await deleteNfs(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleMount = async (row) => {
  await mountNfs(row.id)
  ElMessage.success('挂载成功')
  loadData()
}

const handleUnmount = async (row) => {
  await unmountNfs(row.id)
  ElMessage.success('卸载成功')
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await updateNfs(form.value)
  } else {
    await saveNfs(form.value)
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
.status-unmounted {
  background: rgba(127, 140, 155, 0.1);
  color: #7f8c9b;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}
</style>
