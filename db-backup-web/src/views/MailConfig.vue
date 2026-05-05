<template>
  <div class="page-wrapper">
    <div class="page-card">
      <div class="page-card-header">
        <div class="page-card-title">
          <div class="title-dot"></div>
          <span>邮件配置</span>
        </div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="130px" style="max-width: 600px">
        <el-form-item label="SMTP服务器" prop="smtpHost">
          <el-input v-model="form.smtpHost" placeholder="如: smtp.qq.com" />
        </el-form-item>
        <el-form-item label="SMTP端口" prop="smtpPort">
          <el-input-number v-model="form.smtpPort" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="邮箱账号" prop="username">
          <el-input v-model="form.username" placeholder="如: xxx@qq.com" />
        </el-form-item>
        <el-form-item label="邮箱密码/授权码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="启用SSL">
          <el-switch v-model="form.enableSsl" />
        </el-form-item>
        <el-form-item label="发件人地址" prop="fromAddress">
          <el-input v-model="form.fromAddress" placeholder="如: xxx@qq.com" />
        </el-form-item>
        <el-form-item label="收件人地址" prop="toAddresses">
          <el-input v-model="form.toAddresses" placeholder="多个用逗号分隔" />
        </el-form-item>
        <el-form-item label="启用通知">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存配置</el-button>
          <el-button @click="handleTest">发送测试邮件</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMailConfig, saveMailConfig, testMail } from '../api/mail.js'

const formRef = ref()
const form = ref({
  smtpHost: '', smtpPort: 587, username: '', password: '',
  enableSsl: true, fromAddress: '', toAddresses: '', enabled: false
})

const rules = {
  smtpHost: [{ required: true, message: '请输入SMTP服务器', trigger: 'blur' }],
  smtpPort: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  username: [{ required: true, message: '请输入邮箱账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  fromAddress: [{ required: true, message: '请输入发件人地址', trigger: 'blur' }],
  toAddresses: [{ required: true, message: '请输入收件人地址', trigger: 'blur' }]
}

const loadData = async () => {
  const res = await getMailConfig()
  if (res.data) {
    form.value = res.data
  }
}

const handleSave = async () => {
  await formRef.value.validate()
  await saveMailConfig(form.value)
  ElMessage.success('保存成功')
}

const handleTest = async () => {
  await formRef.value.validate()
  const res = await testMail(form.value)
  if (res.data) {
    ElMessage.success('测试邮件发送成功')
  } else {
    ElMessage.error('测试邮件发送失败')
  }
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
  margin-bottom: 24px;
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
</style>
