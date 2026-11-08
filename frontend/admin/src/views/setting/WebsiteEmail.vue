<template>
  <div class="website-email">
    <el-card class="box-card" v-loading="settingsLoading">
      <template #header>
        <div class="card-header">
          <span>邮件设置</span>
        </div>
      </template>
      
      <el-form :model="emailForm" label-width="120px">
        <el-form-item label="邮件服务商">
          <el-select v-model="emailForm.provider" placeholder="请选择邮件服务商">
            <el-option label="SMTP" value="smtp"></el-option>
            <el-option label="阿里云邮件推送" value="aliyun"></el-option>
            <el-option label="腾讯云邮件推送" value="tencent"></el-option>
            <el-option label="SendGrid" value="sendgrid"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="SMTP服务器">
          <el-input v-model="emailForm.smtpHost" placeholder="请输入SMTP服务器地址"></el-input>
        </el-form-item>
        
        <el-form-item label="端口">
          <el-input-number v-model="emailForm.smtpPort" :min="1" :max="65535" controls-position="right"></el-input-number>
        </el-form-item>
        
        <el-form-item label="加密方式">
          <el-radio-group v-model="emailForm.encryption">
            <el-radio value="none">无加密</el-radio>
            <el-radio value="ssl">SSL</el-radio>
            <el-radio value="tls">TLS</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="发件人邮箱">
          <el-input v-model="emailForm.fromEmail" placeholder="请输入发件人邮箱"></el-input>
        </el-form-item>
        
        <el-form-item label="发件人名称">
          <el-input v-model="emailForm.fromName" placeholder="请输入发件人名称"></el-input>
        </el-form-item>
        
        <el-form-item label="用户名">
          <el-input v-model="emailForm.username" placeholder="请输入邮箱用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="密码">
          <el-input v-model="emailForm.password" type="password" placeholder="请输入邮箱密码或授权码" show-password></el-input>
        </el-form-item>
        
        <el-form-item label="测试邮箱">
          <el-input v-model="emailForm.testEmail" placeholder="请输入测试邮箱地址"></el-input>
          <el-button type="primary" :loading="testLoading" @click="sendTestEmail" style="margin-left: 10px;">发送测试邮件</el-button>
        </el-form-item>
        
        <el-form-item label="邮件模板">
          <el-switch v-model="emailForm.templateEnabled"></el-switch>
          <span class="form-tip">启用邮件模板功能</span>
        </el-form-item>
        
        <el-form-item label="邮件队列">
          <el-switch v-model="emailForm.queueEnabled"></el-switch>
          <span class="form-tip">启用邮件队列处理（异步发送，不阻塞业务）</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="saveLoading" @click="saveEmailConfig">保存设置</el-button>
          <el-button @click="resetEmailConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 邮件模板管理 -->
    <el-card class="box-card" style="margin-top: 20px;" v-if="emailForm.templateEnabled">
      <template #header>
        <div class="card-header">
          <span>邮件模板管理</span>
          <el-button type="primary" @click="showAddTemplateDialog">新增模板</el-button>
        </div>
      </template>
      
      <el-table :data="emailTemplates" v-loading="templateLoading" style="width: 100%">
        <el-table-column prop="templateName" label="模板名称" width="150"></el-table-column>
        <el-table-column prop="titleTemplate" label="邮件主题" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="bizType" label="模板类型" width="120">
          <template #default="scope">
            <el-tag>{{ getTemplateTypeName(scope.row.bizType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editTemplate(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTemplate(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增/编辑模板对话框 -->
    <el-dialog v-model="templateDialogVisible" :title="templateDialogTitle" width="800px">
      <el-form :model="templateForm" label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input v-model="templateForm.name" placeholder="请输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="模板类型" required>
          <el-select v-model="templateForm.type" placeholder="请选择模板类型">
            <el-option label="注册验证" value="AUTH"></el-option>
            <el-option label="密码重置" value="RESET"></el-option>
            <el-option label="通知邮件" value="NOTIFY"></el-option>
            <el-option label="营销邮件" value="MARKETING"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="邮件主题" required>
          <el-input v-model="templateForm.subject" placeholder="请输入邮件主题，支持变量如 ${userName}"></el-input>
        </el-form-item>
        <el-form-item label="邮件内容" required>
          <el-input 
            v-model="templateForm.content" 
            type="textarea" 
            :rows="10"
            placeholder="请输入邮件内容，支持HTML格式及变量如 ${content}"
          ></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="templateForm.status" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="templateSaveLoading" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import emailApi, { templateApi } from '@/api/setting/email'

const settingsLoading = ref(false)
const saveLoading = ref(false)
const testLoading = ref(false)
const templateLoading = ref(false)
const templateSaveLoading = ref(false)

const emailForm = reactive({
  provider: 'smtp',
  smtpHost: 'smtp.qq.com',
  smtpPort: 587,
  encryption: 'tls',
  fromEmail: '',
  fromName: 'UniMall系统',
  username: '',
  password: '',
  testEmail: '',
  templateEnabled: true,
  queueEnabled: false
})

const emailTemplates = ref([])

const templateDialogVisible = ref(false)
const templateDialogTitle = ref('新增模板')
const isEditTemplate = ref(false)

const templateForm = reactive({
  id: null,
  templateCode: '',
  name: '',
  type: 'NOTIFY',
  subject: '',
  content: '',
  status: 1
})

/**
 * 格式化 Unix 时间戳为本地时间字符串。
 * @param {number} ts 秒级时间戳
 * @returns {string} 格式化后的时间
 */
const formatTime = (ts) => {
  if (!ts) return '-'
  return new Date(ts * 1000).toLocaleString()
}

/**
 * 获取模板类型显示名称。
 * @param {string} type 业务类型编码
 * @returns {string} 中文名称
 */
const getTemplateTypeName = (type) => {
  const typeMap = {
    AUTH: '注册验证',
    RESET: '密码重置',
    NOTIFY: '通知邮件',
    MARKETING: '营销邮件',
    TASK: '任务通知',
    PROJECT: '项目通知'
  }
  return typeMap[type] || type
}

/**
 * 从后端加载邮件 SMTP 配置。
 */
const loadSettings = async () => {
  settingsLoading.value = true
  try {
    const res = await emailApi.getSettings()
    if (res.code === 200 || res.code === 1) {
      const data = res.data || {}
      Object.assign(emailForm, {
        provider: data.provider || 'smtp',
        smtpHost: data.smtpHost || 'smtp.qq.com',
        smtpPort: data.smtpPort || 587,
        encryption: data.encryption || 'tls',
        fromEmail: data.fromEmail || '',
        fromName: data.fromName || 'UniMall系统',
        username: data.username || '',
        password: data.password || '',
        templateEnabled: data.templateEnabled !== false,
        queueEnabled: !!data.queueEnabled
      })
    } else {
      ElMessage.error(res.message || res.msg || '加载邮件配置失败')
    }
  } catch (e) {
    ElMessage.error('加载邮件配置失败')
  } finally {
    settingsLoading.value = false
  }
}

/**
 * 加载 EMAIL 渠道的消息模板列表。
 */
const loadTemplates = async () => {
  if (!emailForm.templateEnabled) {
    emailTemplates.value = []
    return
  }
  templateLoading.value = true
  try {
    const res = await templateApi.getList({ channel: 'EMAIL', page: 1, size: 100 })
    if (res.code === 200 || res.code === 1) {
      emailTemplates.value = res.data?.list || []
    }
  } catch (e) {
    ElMessage.error('加载邮件模板失败')
  } finally {
    templateLoading.value = false
  }
}

/**
 * 发送测试邮件。
 */
const sendTestEmail = async () => {
  if (!emailForm.testEmail) {
    ElMessage.warning('请输入测试邮箱地址')
    return
  }
  testLoading.value = true
  try {
    const res = await emailApi.sendTest(emailForm.testEmail)
    if (res.code === 200 || res.code === 1) {
      ElMessage.success(res.message || res.msg || '测试邮件发送成功')
    } else {
      ElMessage.error(res.message || res.msg || '测试邮件发送失败')
    }
  } catch (e) {
    ElMessage.error('测试邮件发送失败')
  } finally {
    testLoading.value = false
  }
}

/**
 * 保存邮件 SMTP 配置及开关。
 */
const saveEmailConfig = async () => {
  if (!emailForm.username) {
    ElMessage.warning('请填写邮箱用户名')
    return
  }
  saveLoading.value = true
  try {
    const res = await emailApi.saveSettings({ ...emailForm })
    if (res.code === 200 || res.code === 1) {
      ElMessage.success('邮件设置保存成功')
      await loadSettings()
      if (emailForm.templateEnabled) {
        await loadTemplates()
      }
    } else {
      ElMessage.error(res.message || res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

/**
 * 重置表单为服务端配置。
 */
const resetEmailConfig = async () => {
  await loadSettings()
  ElMessage.info('已恢复为已保存的配置')
}

/**
 * 打开新增模板对话框。
 */
const showAddTemplateDialog = () => {
  templateDialogTitle.value = '新增模板'
  isEditTemplate.value = false
  resetTemplateForm()
  templateDialogVisible.value = true
}

/**
 * 打开编辑模板对话框。
 * @param {object} row 模板行数据
 */
const editTemplate = async (row) => {
  templateDialogTitle.value = '编辑模板'
  isEditTemplate.value = true
  try {
    const res = await templateApi.getById(row.id)
    const data = (res.code === 200 || res.code === 1) ? res.data : row
    Object.assign(templateForm, {
      id: data.id,
      templateCode: data.templateCode,
      name: data.templateName,
      type: data.bizType || 'NOTIFY',
      subject: data.titleTemplate,
      content: data.contentTemplate,
      status: data.status ?? 1
    })
    templateDialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载模板详情失败')
  }
}

/**
 * 删除邮件模板。
 * @param {object} row 模板行数据
 */
const deleteTemplate = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await templateApi.delete(row.id)
    if (res.code === 200 || res.code === 1) {
      ElMessage.success('删除成功')
      await loadTemplates()
    } else {
      ElMessage.error(res.message || res.msg || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

/**
 * 构建模板 API 请求体。
 * @returns {object} 后端 MessageTemplate 字段
 */
const buildTemplatePayload = () => {
  const code = templateForm.templateCode || `EMAIL_${Date.now()}`
  return {
    id: templateForm.id,
    templateCode: code,
    templateName: templateForm.name,
    bizType: templateForm.type,
    titleTemplate: templateForm.subject,
    contentTemplate: templateForm.content,
    channels: 'EMAIL',
    emailTemplateCode: code,
    status: templateForm.status
  }
}

/**
 * 保存邮件模板（新增或更新）。
 */
const saveTemplate = async () => {
  if (!templateForm.name || !templateForm.type || !templateForm.subject || !templateForm.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  templateSaveLoading.value = true
  try {
    const payload = buildTemplatePayload()
    const res = isEditTemplate.value
      ? await templateApi.update(payload)
      : await templateApi.create(payload)
    if (res.code === 200 || res.code === 1) {
      ElMessage.success(isEditTemplate.value ? '编辑成功' : '新增成功')
      templateDialogVisible.value = false
      await loadTemplates()
    } else {
      ElMessage.error(res.message || res.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    templateSaveLoading.value = false
  }
}

/**
 * 重置模板表单。
 */
const resetTemplateForm = () => {
  Object.assign(templateForm, {
    id: null,
    templateCode: '',
    name: '',
    type: 'NOTIFY',
    subject: '',
    content: '',
    status: 1
  })
}

watch(() => emailForm.templateEnabled, (enabled) => {
  if (enabled) {
    loadTemplates()
  } else {
    emailTemplates.value = []
  }
})

onMounted(async () => {
  await loadSettings()
  if (emailForm.templateEnabled) {
    await loadTemplates()
  }
})
</script>

<style scoped>
.website-email {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}
</style>
