<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="aside">
      <div class="logo">
        <div class="logo-icon">
          <el-icon size="24" color="#fff"><DataLine /></el-icon>
        </div>
        <span class="logo-text">DB Backup</span>
      </div>
      <div class="menu-wrapper">
        <el-menu
          :default-active="activeMenu"
          router
          class="el-menu-vertical"
          background-color="transparent"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#fff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>概览</span>
          </el-menu-item>
          <el-menu-item index="/datasource">
            <el-icon><Coin /></el-icon>
            <span>数据源管理</span>
          </el-menu-item>
          <el-menu-item index="/task">
            <el-icon><Timer /></el-icon>
            <span>备份任务</span>
          </el-menu-item>
          <el-menu-item index="/history">
            <el-icon><Document /></el-icon>
            <span>备份历史</span>
          </el-menu-item>
          <el-menu-item index="/nfs">
            <el-icon><FolderOpened /></el-icon>
            <span>NFS 配置</span>
          </el-menu-item>
          <el-menu-item index="/mail">
            <el-icon><Message /></el-icon>
            <span>邮件配置</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="aside-footer">
        <div class="version-badge">v1.0.0</div>
      </div>
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <h2 class="page-title">{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <div class="header-time">{{ currentTime }}</div>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '')
const currentTime = ref('')

let timer = null
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', weekday: 'short' })
    + ' ' + now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 60000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}
.aside {
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}
.aside::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(ellipse at top left, rgba(102, 126, 234, 0.15) 0%, transparent 50%);
  pointer-events: none;
}
.logo {
  height: 72px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}
.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--primary-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  margin-left: 12px;
  color: #fff;
  letter-spacing: -0.5px;
}
.menu-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
  position: relative;
  z-index: 1;
}
.el-menu-vertical {
  border-right: none;
}
.el-menu-vertical .el-menu-item {
  border-radius: 10px;
  margin-bottom: 4px;
  height: 48px;
  line-height: 48px;
  transition: all 0.25s ease;
}
.el-menu-vertical .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.08) !important;
}
.el-menu-vertical .el-menu-item.is-active {
  background: var(--primary-gradient) !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
  color: #fff !important;
}
.aside-footer {
  padding: 16px 24px;
  position: relative;
  z-index: 1;
}
.version-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.5);
  font-size: 12px;
}
.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-time {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 6px 14px;
  background: #f0f2f7;
  border-radius: 20px;
}
.main {
  background: var(--body-bg);
  padding: 24px 32px;
  overflow-y: auto;
  flex: 1;
}
</style>
