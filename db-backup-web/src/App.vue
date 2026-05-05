<template>
  <router-view />
</template>

<script setup>
</script>

<style>
:root {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --sidebar-bg: linear-gradient(180deg, #1a1c2e 0%, #2d2f45 100%);
  --card-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  --card-hover-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  --body-bg: #f5f7fb;
  --text-primary: #2c3e50;
  --text-secondary: #7f8c9b;
  --accent-blue: #667eea;
  --accent-purple: #764ba2;
  --accent-green: #56ab2f;
  --accent-orange: #f7971e;
  --accent-red: #eb3349;
  --border-radius: 12px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', sans-serif;
  background: var(--body-bg);
  color: var(--text-primary);
  -webkit-font-smoothing: antialiased;
}

/* 全局美化滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.5);
}

/* Element Plus 全局覆盖 */
.el-card {
  border-radius: var(--border-radius) !important;
  border: none !important;
  box-shadow: var(--card-shadow) !important;
  transition: all 0.3s ease !important;
}
.el-card:hover {
  box-shadow: var(--card-hover-shadow) !important;
  transform: translateY(-2px);
}
.el-table {
  border-radius: 8px;
  overflow: hidden;
}
.el-table th.el-table__cell {
  background: #f8f9fe !important;
  color: var(--text-primary);
  font-weight: 600;
}
.el-button--primary {
  background: var(--accent-blue) !important;
  border-color: var(--accent-blue) !important;
  border-radius: 8px !important;
}
.el-button--primary:hover {
  background: #5a6fd6 !important;
  border-color: #5a6fd6 !important;
}
.el-tag {
  border-radius: 6px !important;
}
.el-dialog {
  border-radius: 16px !important;
}
.el-pagination {
  --el-pagination-button-bg-color: transparent;
}
.el-input__wrapper, .el-select__wrapper {
  border-radius: 8px !important;
}
</style>
