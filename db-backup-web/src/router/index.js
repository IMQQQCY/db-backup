import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '概览' } },
      { path: 'datasource', component: () => import('../views/DataSource.vue'), meta: { title: '数据源管理' } },
      { path: 'task', component: () => import('../views/BackupTask.vue'), meta: { title: '备份任务' } },
      { path: 'history', component: () => import('../views/BackupHistory.vue'), meta: { title: '备份历史' } },
      { path: 'nfs', component: () => import('../views/NfsConfig.vue'), meta: { title: 'NFS 配置' } },
      { path: 'mail', component: () => import('../views/MailConfig.vue'), meta: { title: '邮件配置' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
