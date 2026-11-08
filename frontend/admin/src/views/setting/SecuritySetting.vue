<template>
  <div class="security-setting">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>安全设置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础安全" name="basic">
          <div class="tab-content">
            <el-form :model="basicForm" label-width="150px">
              <el-form-item label="启用双因素认证">
                <el-switch v-model="basicForm.enable2FA" />
              </el-form-item>
              
              <el-form-item label="会话超时时间">
                <el-input-number v-model="basicForm.sessionTimeout" :min="1" :max="1440" />
                <span class="form-tip">分钟（1-1440）</span>
              </el-form-item>
              
              <el-form-item label="强制HTTPS">
                <el-switch v-model="basicForm.forceHTTPS" />
              </el-form-item>
              
              <el-form-item label="CSRF保护">
                <el-switch v-model="basicForm.enableCSRF" />
              </el-form-item>
              
              <el-form-item label="XSS防护">
                <el-switch v-model="basicForm.enableXSS" />
              </el-form-item>
              
              <el-form-item label="点击劫持防护">
                <el-switch v-model="basicForm.enableClickjacking" />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="验证码设置" name="captcha">
          <div class="tab-content">
            <el-form :model="captchaForm" label-width="150px">
              <el-form-item label="启用验证码">
                <el-switch v-model="captchaForm.enableCaptcha" />
              </el-form-item>
              
              <el-form-item label="验证码类型">
                <el-radio-group v-model="captchaForm.captchaType">
                  <el-radio label="image">图片验证码</el-radio>
                  <el-radio label="math">算术验证码</el-radio>
                  <el-radio label="slide">滑动验证码</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="验证码长度">
                <el-input-number v-model="captchaForm.captchaLength" :min="4" :max="8" />
              </el-form-item>
              
              <el-form-item label="过期时间">
                <el-input-number v-model="captchaForm.expireTime" :min="30" :max="300" />
                <span class="form-tip">秒（30-300）</span>
              </el-form-item>
              
              <el-form-item label="错误次数限制">
                <el-input-number v-model="captchaForm.errorLimit" :min="0" :max="10" />
                <span class="form-tip">0表示无限制</span>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveCaptchaSettings">保存设置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="安全日志" name="log">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="exportLogs">导出日志</el-button>
              <el-button type="danger" @click="clearLogs">清空日志</el-button>
            </div>
            
            <el-table :data="securityLogs" style="width: 100%">
              <el-table-column prop="timestamp" label="时间" width="180" />
              <el-table-column prop="user" label="用户" width="120" />
              <el-table-column prop="action" label="操作" width="150" />
              <el-table-column prop="ip" label="IP地址" width="150" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
                    {{ row.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" />
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('basic')

// 基础安全设置
const basicForm = reactive({
  enable2FA: true,
  sessionTimeout: 30,
  forceHTTPS: true,
  enableCSRF: true,
  enableXSS: true,
  enableClickjacking: true
})

// 验证码设置
const captchaForm = reactive({
  enableCaptcha: true,
  captchaType: 'image',
  captchaLength: 4,
  expireTime: 120,
  errorLimit: 3
})

// 安全日志
const securityLogs = ref([
  {
    id: 1,
    timestamp: '2024-01-08 15:30:45',
    user: 'admin',
    action: '登录',
    ip: '192.168.1.100',
    status: 'success',
    description: '用户登录成功'
  },
  {
    id: 2,
    timestamp: '2024-01-08 15:25:30',
    user: 'user1',
    action: '登录失败',
    ip: '192.168.1.101',
    status: 'fail',
    description: '密码错误'
  },
  {
    id: 3,
    timestamp: '2024-01-08 15:20:15',
    user: 'admin',
    action: '权限变更',
    ip: '192.168.1.100',
    status: 'success',
    description: '修改用户权限'
  }
])

// 基础安全设置保存
const saveBasicSettings = () => {
  ElMessage.success('基础安全设置保存成功')
}

// 验证码设置保存
const saveCaptchaSettings = () => {
  ElMessage.success('验证码设置保存成功')
}

// 导出日志
const exportLogs = () => {
  ElMessage.success('正在导出安全日志...')
}

// 清空日志
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有安全日志吗？', '确认清空', {
      type: 'warning'
    })
    securityLogs.value = []
    ElMessage.success('安全日志已清空')
  } catch {
    ElMessage.info('已取消清空')
  }
}
</script>

<style scoped>
.security-setting {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
}

.form-tip {
  margin-left: 15px;
  color: #909399;
  font-size: 12px;
}
</style>