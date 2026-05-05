<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="24">
      <el-col :span="6" v-for="(item, index) in statCards" :key="index">
        <div class="stat-card" :style="{ '--card-color': item.color, '--card-gradient': item.gradient }">
          <div class="stat-card-bg"></div>
          <div class="stat-card-content">
            <div class="stat-card-icon">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-card-info">
              <div class="stat-card-value">{{ item.value }}</div>
              <div class="stat-card-label">{{ item.label }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近备份记录 -->
    <div class="section-card" style="margin-top: 24px">
      <div class="section-header">
        <div class="section-title">
          <div class="title-dot"></div>
          <span>最近备份记录</span>
        </div>
      </div>
      <el-table :data="recentHistory" class="modern-table">
        <el-table-column prop="taskName" label="任务名称">
          <template #default="scope">
            <span class="task-name">{{ scope.row.taskName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="backupType" label="备份类型" width="120">
          <template #default="scope">
            <span class="type-badge type-full" v-if="scope.row.backupType === 'FULL'">全量备份</span>
            <span class="type-badge type-partial" v-else>部分备份</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
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
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listDataSource } from '../api/datasource.js'
import { pageTask } from '../api/task.js'
import { pageHistory } from '../api/history.js'

const stats = ref({ dataSourceCount: 0, taskCount: 0, historyCount: 0, successCount: 0 })
const recentHistory = ref([])

const statCards = computed(() => [
  {
    label: '数据源',
    value: stats.value.dataSourceCount,
    icon: 'Coin',
    color: '#667eea',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    label: '备份任务',
    value: stats.value.taskCount,
    icon: 'Timer',
    color: '#56ab2f',
    gradient: 'linear-gradient(135deg, #56ab2f 0%, #a8e063 100%)'
  },
  {
    label: '备份历史',
    value: stats.value.historyCount,
    icon: 'Document',
    color: '#f7971e',
    gradient: 'linear-gradient(135deg, #f7971e 0%, #ffd200 100%)'
  },
  {
    label: '成功次数',
    value: stats.value.successCount,
    icon: 'CircleCheck',
    color: '#eb3349',
    gradient: 'linear-gradient(135deg, #eb3349 0%, #f45c43 100%)'
  }
])

const loadStats = async () => {
  try {
    const dsRes = await listDataSource()
    stats.value.dataSourceCount = dsRes.data?.length || 0

    const taskRes = await pageTask({ pageNum: 1, pageSize: 1 })
    stats.value.taskCount = taskRes.data?.total || 0

    const histRes = await pageHistory({ pageNum: 1, pageSize: 10 })
    stats.value.historyCount = histRes.data?.total || 0
    recentHistory.value = histRes.data?.records || []
    stats.value.successCount = recentHistory.value.filter(h => h.status === 'SUCCESS').length
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadStats)
</script>

<style scoped>
.dashboard {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 统计卡片 */
.stat-card {
  position: relative;
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: default;
}
.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.15);
}
.stat-card-bg {
  position: absolute;
  inset: 0;
  background: var(--card-gradient);
  opacity: 0.9;
}
.stat-card-bg::after {
  content: '';
  position: absolute;
  top: -30%;
  right: -20%;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}
.stat-card-bg::before {
  content: '';
  position: absolute;
  bottom: -20%;
  left: -10%;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}
.stat-card-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-card-value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -1px;
}
.stat-card-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 2px;
}

/* Section Card */
.section-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: var(--card-shadow);
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.section-title {
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
  background: var(--primary-gradient);
}

/* 任务名称 */
.task-name {
  font-weight: 500;
  color: var(--text-primary);
}

/* 类型 Badge */
.type-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}
.type-full {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}
.type-partial {
  background: rgba(247, 151, 30, 0.1);
  color: #f7971e;
}

/* 状态 Badge */
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

/* Modern Table */
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
