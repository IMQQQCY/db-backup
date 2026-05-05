<template>
  <div class="page-wrapper">
    <div class="page-card">
      <div class="page-card-header">
        <div class="page-card-title">
          <div class="title-dot"></div>
          <span>备份任务</span>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon style="margin-right: 4px"><Plus /></el-icon>新增任务
        </el-button>
      </div>

      <div class="search-bar">
        <el-input v-model="queryForm.keyword" placeholder="任务名称" clearable style="width: 240px" @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" class="modern-table">
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="backupType" label="备份类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.backupType === 'FULL'" type="primary">全量</el-tag>
            <el-tag v-else type="warning">部分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="backupContent" label="备份内容" width="110">
          <template #default="scope">
            <el-tag v-if="scope.row.backupContent === 'STRUCTURE'" type="info">仅结构</el-tag>
            <el-tag v-else>结构+数据</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storageType" label="存储类型" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.storageType === 'LOCAL'">本地</el-tag>
            <el-tag v-else type="success">NFS</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cronExpression" label="Cron表达式" width="140" />
        <el-table-column prop="retainDays" label="保留天数" width="90" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.enabled"
              @change="(val) => handleToggle(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="small" type="success" @click="handleExecute(scope.row)">立即执行</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新增任务'" width="750px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="数据源" prop="dataSourceId">
          <el-select v-model="form.dataSourceId" placeholder="请选择数据源" style="width: 100%">
            <el-option v-for="item in dataSourceList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备份类型" prop="backupType">
          <el-radio-group v-model="form.backupType" @change="handleBackupTypeChange">
            <el-radio label="FULL">全量备份</el-radio>
            <el-radio label="PARTIAL">部分表备份</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备份内容" prop="backupContent">
          <el-radio-group v-model="form.backupContent">
            <el-radio label="STRUCTURE_DATA">结构 + 数据</el-radio>
            <el-radio label="STRUCTURE">仅结构</el-radio>
          </el-radio-group>
          <div style="font-size: 12px; color: #909399; margin-top: 4px" v-if="form.backupContent === 'STRUCTURE'">仅备份表结构SQL，不包含数据，适合快速备份DDL</div>
        </el-form-item>
        <el-form-item label="选择表" v-if="form.backupType === 'PARTIAL'">
          <div style="width: 100%">
            <div style="margin-bottom: 8px; display: flex; align-items: center; gap: 10px;">
              <el-input v-model="tableSearchKeyword" placeholder="搜索表名" clearable style="width: 200px" @input="handleTableSearch" />
              <el-select v-model="tableOrderBy" style="width: 130px" @change="loadTablePage">
                <el-option label="按名称排序" value="name" />
                <el-option label="按大小排序" value="size" />
              </el-select>
              <el-select v-model="tableOrderDir" style="width: 100px" @change="loadTablePage">
                <el-option label="升序" value="asc" />
                <el-option label="降序" value="desc" />
              </el-select>
              <el-button size="small" @click="loadTablePage" :loading="loadingTables">刷新</el-button>
              <span style="font-size: 12px; color: #909399">已选 {{ selectedTables.length }} 张表</span>
            </div>
            <el-table :data="tableInfoList" stripe v-loading="loadingTables" size="small" max-height="300"
                      @selection-change="handleTableSelectionChange" ref="tableSelectRef" row-key="tableName">
              <el-table-column type="selection" width="50" :reserve-selection="true" />
              <el-table-column prop="tableName" label="表名" />
              <el-table-column prop="dataSizeStr" label="大小" width="120" />
            </el-table>
            <el-pagination
              v-model:current-page="tablePageNum"
              v-model:page-size="tablePageSize"
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next"
              :total="tableTotal"
              @change="loadTablePage"
              size="small"
              style="margin-top: 8px; justify-content: flex-end"
            />
            <div v-if="!loadingTables && tableInfoList.length === 0" style="color: #909399; font-size: 12px; margin-top: 8px">请先选择数据源</div>
          </div>
        </el-form-item>
        <el-form-item label="存储类型" prop="storageType">
          <el-radio-group v-model="form.storageType">
            <el-radio label="LOCAL">本地存储</el-radio>
            <el-radio label="NFS">NFS存储</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="NFS配置" v-if="form.storageType === 'NFS'" prop="nfsConfigId">
          <el-select v-model="form.nfsConfigId" placeholder="请选择NFS配置" style="width: 100%">
            <el-option v-for="item in nfsList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="存储路径" prop="storagePath">
          <el-input v-model="form.storagePath" placeholder="本地绝对路径或NFS子目录" />
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cronExpression">
          <div style="width: 100%">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-input v-model="form.cronExpression" placeholder="如: 0 0 2 * * ? (每天凌晨2点)" style="flex: 1" />
              <el-button @click="handleTestCron" :loading="cronTesting">测试</el-button>
            </div>
            <div style="font-size: 12px; color: #909399; margin-top: 4px">格式: 秒 分 时 日 月 周</div>
            <div class="cron-presets">
              <span class="cron-preset-label">常用模板：</span>
              <el-tag v-for="item in cronPresets" :key="item.value" size="small" class="cron-preset-tag"
                      @click="applyCronPreset(item.value)" :title="item.value">
                {{ item.label }}
              </el-tag>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="保留天数" prop="retainDays">
          <el-input-number v-model="form.retainDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
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

    <!-- Cron测试结果弹窗 -->
    <el-dialog v-model="cronDialogVisible" title="Cron 表达式测试" width="460px" append-to-body>
      <div class="cron-test-result">
        <div class="cron-expression-display">
          <span class="cron-label">当前表达式：</span>
          <code class="cron-code">{{ cronTestExpression }}</code>
        </div>
        <div class="cron-next-title">最近 {{ cronNextList.length }} 次执行时间：</div>
        <div class="cron-time-list">
          <div v-for="(time, index) in cronNextList" :key="index" class="cron-time-item">
            <span class="cron-time-index">{{ index + 1 }}</span>
            <span class="cron-time-value">{{ time }}</span>
          </div>
        </div>
        <div v-if="cronNextList.length === 0 && !cronTesting" class="cron-empty">无法解析执行时间</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageTask, saveTask, updateTask, deleteTask, toggleTask, executeTask, cronNextTimes } from '../api/task.js'
import { listDataSource, getTableList, getTableInfoPage } from '../api/datasource.js'
import { listNfs } from '../api/nfs.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryForm = ref({ pageNum: 1, pageSize: 10, keyword: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = ref({
  name: '', dataSourceId: null, backupType: 'FULL', backupContent: 'STRUCTURE_DATA', tableList: '',
  storageType: 'LOCAL', storagePath: '', nfsConfigId: null,
  cronExpression: '', retainDays: 30, enabled: true, remark: ''
})
const dataSourceList = ref([])
const nfsList = ref([])
const allTables = ref([])
const selectedTables = ref([])
const selectAllTables = ref(false)
const isIndeterminate = ref(false)
const loadingTables = ref(false)
const tableInfoList = ref([])
const tableTotal = ref(0)
const tablePageNum = ref(1)
const tablePageSize = ref(20)
const tableSearchKeyword = ref('')
const tableOrderBy = ref('name')
const tableOrderDir = ref('asc')
const tableSelectRef = ref()

// Cron 测试相关
const cronTesting = ref(false)
const cronDialogVisible = ref(false)
const cronTestExpression = ref('')
const cronNextList = ref([])
const cronPresets = [
  { label: '每天凌晨2点', value: '0 0 2 * * ?' },
  { label: '每天凌晨3点', value: '0 0 3 * * ?' },
  { label: '每6小时', value: '0 0 */6 * * ?' },
  { label: '每12小时', value: '0 0 */12 * * ?' },
  { label: '每周一凌晨2点', value: '0 0 2 ? * MON' },
  { label: '每周日凌晨1点', value: '0 0 1 ? * SUN' },
  { label: '每月 1 日凌晨2点', value: '0 0 2 1 * ?' },
  { label: '工作日凌晨2点', value: '0 0 2 ? * MON-FRI' },
  { label: '每30分钟', value: '0 */30 * * * ?' },
  { label: '每天上午8点', value: '0 0 8 * * ?' },
]

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  dataSourceId: [{ required: true, message: '请选择数据源', trigger: 'change' }],
  backupType: [{ required: true, message: '请选择备份类型', trigger: 'change' }],
  storageType: [{ required: true, message: '请选择存储类型', trigger: 'change' }],
  storagePath: [{ required: true, message: '请输入存储路径', trigger: 'blur' }],
  cronExpression: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }],
  retainDays: [{ required: true, message: '请输入保留天数', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageTask(queryForm.value)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const dsRes = await listDataSource()
  dataSourceList.value = dsRes.data
  const nfsRes = await listNfs()
  nfsList.value = nfsRes.data
}

const handleAdd = () => {
  isEdit.value = false
  form.value = {
    name: '', dataSourceId: null, backupType: 'FULL', backupContent: 'STRUCTURE_DATA', tableList: '',
    storageType: 'LOCAL', storagePath: '', nfsConfigId: null,
    cronExpression: '', retainDays: 30, enabled: true, remark: ''
  }
  allTables.value = []
  selectedTables.value = []
  selectAllTables.value = false
  isIndeterminate.value = false
  tableInfoList.value = []
  tableTotal.value = 0
  tablePageNum.value = 1
  tableSearchKeyword.value = ''
  tableOrderBy.value = 'name'
  tableOrderDir.value = 'asc'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  if (row.backupType === 'PARTIAL' && row.tableList) {
    selectedTables.value = row.tableList.split(',').map(t => t.trim()).filter(t => t)
  } else {
    selectedTables.value = []
  }
  tablePageNum.value = 1
  tableSearchKeyword.value = ''
  tableOrderBy.value = 'name'
  tableOrderDir.value = 'asc'
  if (row.dataSourceId && row.backupType === 'PARTIAL') {
    loadTablePage()
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该任务?', '提示', { type: 'warning' }).then(async () => {
    await deleteTask(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleToggle = async (row, val) => {
  await toggleTask(row.id, val)
  ElMessage.success('状态更新成功')
}

const handleExecute = (row) => {
  ElMessageBox.confirm('确认立即执行备份?', '提示', { type: 'info' }).then(async () => {
    await executeTask(row.id)
    ElMessage.success('备份任务已启动')
  })
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (form.value.backupType === 'PARTIAL') {
    form.value.tableList = selectedTables.value.join(',')
  } else {
    form.value.tableList = ''
  }
  if (isEdit.value) {
    await updateTask(form.value)
  } else {
    await saveTask(form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const loadTableList = async () => {
  if (!form.value.dataSourceId) {
    ElMessage.warning('请先选择数据源')
    return
  }
  loadingTables.value = true
  try {
    const res = await getTableList(form.value.dataSourceId)
    allTables.value = res.data || []
  } catch (e) {
    allTables.value = []
  } finally {
    loadingTables.value = false
  }
}

const loadTablePage = async () => {
  if (!form.value.dataSourceId) {
    ElMessage.warning('请先选择数据源')
    return
  }
  loadingTables.value = true
  try {
    const res = await getTableInfoPage(form.value.dataSourceId, {
      pageNum: tablePageNum.value,
      pageSize: tablePageSize.value,
      keyword: tableSearchKeyword.value,
      orderBy: tableOrderBy.value,
      orderDir: tableOrderDir.value
    })
    tableInfoList.value = res.data.records || []
    tableTotal.value = res.data.total || 0
    // 回显已选的行
    await nextTick()
    if (tableSelectRef.value) {
      tableInfoList.value.forEach(row => {
        if (selectedTables.value.includes(row.tableName)) {
          tableSelectRef.value.toggleRowSelection(row, true)
        }
      })
    }
  } catch (e) {
    tableInfoList.value = []
    tableTotal.value = 0
  } finally {
    loadingTables.value = false
  }
}

let searchTimer = null
const handleTableSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    tablePageNum.value = 1
    loadTablePage()
  }, 300)
}

const handleTableSelectionChange = (rows) => {
  // 当前页被选中的表名
  const currentPageNames = tableInfoList.value.map(r => r.tableName)
  const currentSelectedNames = rows.map(r => r.tableName)
  // 保留非当前页的已选项 + 当前页新选中的
  const otherSelected = selectedTables.value.filter(n => !currentPageNames.includes(n))
  selectedTables.value = [...otherSelected, ...currentSelectedNames]
}

const handleBackupTypeChange = (val) => {
  if (val === 'PARTIAL' && form.value.dataSourceId) {
    loadTablePage()
  }
}

const handleTestCron = async () => {
  if (!form.value.cronExpression) {
    ElMessage.warning('请先输入Cron表达式')
    return
  }
  cronTesting.value = true
  cronTestExpression.value = form.value.cronExpression
  try {
    const res = await cronNextTimes(form.value.cronExpression, 5)
    if (res.code === 200) {
      cronNextList.value = res.data || []
      cronDialogVisible.value = true
    } else {
      ElMessage.error(res.msg || 'Cron表达式解析失败')
    }
  } catch (e) {
    ElMessage.error('Cron表达式解析失败')
  } finally {
    cronTesting.value = false
  }
}

const applyCronPreset = (value) => {
  form.value.cronExpression = value
}

const handleSelectAll = (val) => {
  selectedTables.value = val ? [...allTables.value] : []
  isIndeterminate.value = false
}

const handleTableSelectChange = (val) => {
  const count = val.length
  selectAllTables.value = count === allTables.value.length
  isIndeterminate.value = count > 0 && count < allTables.value.length
}

watch(() => form.value.dataSourceId, (newVal) => {
  if (newVal && form.value.backupType === 'PARTIAL') {
    tablePageNum.value = 1
    tableSearchKeyword.value = ''
    loadTablePage()
  }
})

onMounted(() => {
  loadData()
  loadOptions()
})
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

/* Cron 模板样式 */
.cron-presets {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}
.cron-preset-label {
  font-size: 12px;
  color: var(--text-secondary);
}
.cron-preset-tag {
  cursor: pointer;
  transition: all 0.2s ease;
}
.cron-preset-tag:hover {
  background: var(--accent-blue) !important;
  color: #fff !important;
  border-color: var(--accent-blue) !important;
}

/* Cron 测试弹窗样式 */
.cron-test-result {
  padding: 8px 0;
}
.cron-expression-display {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f8f9fe;
  border-radius: 10px;
}
.cron-label {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}
.cron-code {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 14px;
  font-weight: 600;
  color: var(--accent-blue);
}
.cron-next-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.cron-time-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cron-time-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #fafbfd;
  border-radius: 8px;
  transition: background 0.2s;
}
.cron-time-item:hover {
  background: #f0f2ff;
}
.cron-time-index {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--primary-gradient);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cron-time-value {
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  font-size: 14px;
  color: var(--text-primary);
}
.cron-empty {
  text-align: center;
  color: var(--text-secondary);
  padding: 20px;
}
</style>
