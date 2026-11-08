<template>
  <div class="website-sms">
    <div class="sms-container">
      <!-- 内容区 -->
      <div class="sms-content">
        <!-- 服务配置 -->
        <div class="config-panel">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>{{ t('setting.sms.website.title') }}</span>
                <div class="header-buttons">
                  <el-button @click="openSettingDialog">
                    <el-icon><Tools /></el-icon>
                    {{ t('setting.sms.website.settings') }}
                  </el-button>
                  <el-button type="primary" @click="addSmsConfig">
                    <el-icon><Plus /></el-icon>
                    {{ t('setting.sms.website.addConfig') }}
                  </el-button>
                </div>
              </div>
            </template>
            
            <!-- 短信配置列表 -->
            <el-table :data="smsConfigList" v-loading="loading" style="width: 100%">
        <el-table-column prop="smsType" :label="t('setting.sms.website.provider')" width="120">
          <template #default="{ row }">
            <el-tag :type="getProviderTagType(row.smsType)">
              {{ getProviderName(row.smsType) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="smsSign" :label="t('setting.sms.website.signature')" width="150" />
        
        <el-table-column prop="smsStatus" :label="t('setting.sms.website.status')" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.smsStatus"
              :active-value="'01'"
              :inactive-value="'00'"
              @change="toggleStatus(row)"
              :loading="row.statusLoading"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="smsRemark" :label="t('setting.sms.website.remark')" />
        
        <el-table-column prop="smsCreateddate" :label="t('setting.sms.website.createdAt')" width="180">
          <template #default="{ row }">
            {{ formatDate(row.smsCreateddate) }}
          </template>
        </el-table-column>
        
        <el-table-column :label="t('setting.sms.website.action')" width="200" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-button type="primary" size="small" @click="editSmsConfig(row)">
                <el-icon><Edit /></el-icon>
                {{ t('setting.sms.website.edit') }}
              </el-button>
              <el-button type="success" size="small" @click="testSMS(row)">
                <el-icon><Message /></el-icon>
                {{ t('setting.sms.website.test') }}
              </el-button>
              <el-button type="danger" size="small" @click="deleteSmsConfig(row)">
                <el-icon><Delete /></el-icon>
                {{ t('setting.sms.website.delete') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
          </el-card>
        </div>
      </div>
    </div>
    
    <!-- 短信设置对话框 -->
    <el-dialog
      v-model="settingDialogVisible"
      :title="t('setting.sms.website.settings')"
      width="600px"
      @close="closeSettingDialog"
    >
      <el-form :model="smsSettingForm" label-width="150px" ref="smsSettingFormRef">
        <el-form-item :label="t('setting.sms.website.codeExpire')" required>
          <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
            <el-input-number
              v-model="smsSettingForm.codeExpireMinutes"
              :min="1"
              :max="1440"
              :step="1"
              style="width: 200px;"
            />
            <span style="color: #606266;">{{ t('setting.sms.website.minute') }}</span>
          </div>
          <div class="form-tip">{{ t('setting.sms.website.codeExpireTip') }}</div>
        </el-form-item>
        
        <el-form-item :label="t('setting.sms.website.recordExpire')" required>
          <div style="display: flex; align-items: center; gap: 12px; width: 100%;">
            <el-input-number
              v-model="smsSettingForm.recordExpireDays"
              :min="1"
              :max="365"
              :step="1"
              style="width: 200px;"
            />
            <span style="color: #606266;">{{ t('setting.sms.website.day') }}</span>
          </div>
          <div class="form-tip">{{ t('setting.sms.website.recordExpireTip') }}</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeSettingDialog">{{ t('setting.sms.website.cancel') }}</el-button>
        <el-button @click="resetSmsSetting">{{ t('setting.sms.website.reset') }}</el-button>
        <el-button type="primary" @click="saveSmsSetting" :loading="savingSetting">
          {{ t('setting.sms.website.saveSettings') }}
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editDialogTitle"
      width="800px"
      @close="closeEditDialog"
      class="sms-config-dialog"
    >
      <el-form :model="editForm" label-width="140px" ref="editFormRef">
        <el-form-item :label="t('setting.sms.website.providerType')" required>
          <el-select v-model="editForm.smsType" :placeholder="t('setting.sms.website.providerPlaceholder')" style="width: 100%">
            <el-option :label="t('setting.sms.website.providerTencent')" value="tencentCloud" />
            <el-option :label="t('setting.sms.website.providerAliyun')" value="aliyun" />
            <el-option :label="t('setting.sms.website.providerunimall')" value="unimall" />
          </el-select>
        </el-form-item>
        
        <el-form-item :label="t('setting.sms.common.signature')" required>
          <el-input v-model="editForm.smsSign" :placeholder="t('setting.sms.common.signaturePlaceholder')" />
        </el-form-item>
        
        <el-form-item label="SecretId/KeyId" required>
          <el-input v-model="editForm.smsSecretId" :placeholder="t('setting.sms.website.secretIdPlaceholder')" />
        </el-form-item>
        
        <el-form-item label="SecretKey/KeySecret" required>
          <el-input v-model="editForm.smsSecretKey" type="password" :placeholder="t('setting.sms.website.secretKeyPlaceholder')" show-password />
        </el-form-item>
        
        <!-- 应用ID：仅腾讯云需要 -->
        <el-form-item v-if="editForm.smsType === 'tencentCloud'" :label="t('setting.sms.website.appId')" required>
          <el-input v-model="editForm.smsSdkAppId" :placeholder="t('setting.sms.website.appIdPlaceholder')" />
          <div class="form-tip">{{ t('setting.sms.website.appIdTip') }}</div>
        </el-form-item>
        
        <!-- 模板ID：腾讯云和阿里云需要，unimall不需要 -->
        <el-form-item v-if="editForm.smsType === 'tencentCloud' || editForm.smsType === 'aliyun'" :label="t('setting.sms.common.templateId')" required>
          <div style="display: flex; align-items: center; gap: 8px; width: 100%;">
            <el-input 
              v-model="editForm.smsGeneral" 
              :placeholder="editForm.smsType === 'tencentCloud' ? t('setting.sms.website.templateIdTencentPlaceholder') : t('setting.sms.website.templateIdAliyunPlaceholder')" 
              style="flex: 1;"
            />
            <el-link 
              type="primary" 
              :underline="false" 
              @click="goToMessageTemplate"
              style="white-space: nowrap;"
            >
              <el-icon><Link /></el-icon>
              {{ t('setting.sms.website.goSettings') }}
            </el-link>
          </div>
          <div class="form-tip">
            {{ editForm.smsType === 'tencentCloud' ? t('setting.sms.website.templateIdTencent') : t('setting.sms.website.templateIdAliyun') }}
          </div>
        </el-form-item>
        
        <el-form-item :label="t('setting.sms.website.remark')">
          <el-input v-model="editForm.smsRemark" type="textarea" :placeholder="t('setting.sms.website.remarkPlaceholder')" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeEditDialog">{{ t('setting.sms.website.cancel') }}</el-button>
        <el-button type="primary" @click="saveSmsConfig" :loading="saving">
          {{ t('setting.sms.website.save') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Message, Link, Setting, Tools } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const savingSetting = ref(false)
const smsConfigList = ref([])
const settingDialogVisible = ref(false)
const smsSettingFormRef = ref()

// 编辑对话框
const editDialogVisible = ref(false)
const editDialogTitle = ref('')
const editFormRef = ref()
const editForm = reactive({
  id: null,
  smsType: '',
  smsSign: '',
  smsSecretId: '',
  smsSecretKey: '',
  smsSdkAppId: '',
  smsGeneral: '',
  smsRemark: ''
})

// 短信设置表单
const smsSettingForm = reactive({
  codeExpireMinutes: 30, // 验证码有效期（分钟），默认30分钟
  recordExpireDays: 90   // 记录有效期（天），默认90天
})

// 获取服务商名称
const getProviderName = (type) => {
  const nameMap = {
    'tencentCloud': t('setting.sms.website.providerTencent'),
    'aliyun': t('setting.sms.website.providerAliyun'),
    'unimall': t('setting.sms.website.providerunimall')
  }
  return nameMap[type] || type || t('setting.sms.website.unknown')
}

// 获取服务商标签类型
const getProviderTagType = (type) => {
  const typeMap = {
    'tencentCloud': 'primary',
    'aliyun': 'success',
    'unimall': 'warning'
  }
  return typeMap[type] || ''
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 加载短信配置列表
const loadSmsConfigList = async () => {
  try {
    loading.value = true
    console.log('加载短信配置列表')
    
    // 使用POST请求获取所有短信配置列表（根据API规范，该接口只支持POST方法）
    const response = await request.post('/web/base/sms/selectSMSInfoList', {})
    
    if (response.code === 1 && response.data) {
      smsConfigList.value = response.data
      console.log('短信配置列表:', response.data)
    } else {
      console.warn('获取短信配置列表失败:', response.message)
      smsConfigList.value = []
    }
    
  } catch (error) {
    console.error('加载短信配置列表失败:', error)
    ElMessage.error(t('setting.sms.website.loadFailed') + error.message)
    smsConfigList.value = []
  } finally {
    loading.value = false
  }
}

// 添加短信配置
const addSmsConfig = () => {
  editDialogTitle.value = t('setting.sms.website.addTitle')
  editForm.id = null
  editForm.smsType = ''
  editForm.smsSign = ''
  editForm.smsSecretId = ''
  editForm.smsSecretKey = ''
  editForm.smsSdkAppId = ''
  editForm.smsGeneral = ''
  editForm.smsRemark = ''
  editDialogVisible.value = true
}

// 编辑短信配置
const editSmsConfig = (row) => {
  editDialogTitle.value = t('setting.sms.website.editTitle')
  editForm.id = row.id
  editForm.smsType = row.smsType
  editForm.smsSign = row.smsSign
  editForm.smsSecretId = row.smsSecretId
  editForm.smsSecretKey = row.smsSecretKey
  editForm.smsSdkAppId = row.smsSdkAppId
  editForm.smsGeneral = row.smsGeneral
  editForm.smsRemark = row.smsRemark
  editForm.smsStatus = row.smsStatus || '00' // 保存原有状态
  editDialogVisible.value = true
}

// 保存短信配置
const saveSmsConfig = async () => {
  try {
    // 表单验证
    if (!editForm.smsType) {
      ElMessage.error(t('setting.sms.website.selectProvider'))
      return
    }
    if (!editForm.smsSign) {
      ElMessage.error(t('setting.sms.website.inputSignature'))
      return
    }
    if (!editForm.smsSecretId) {
      ElMessage.error(t('setting.sms.website.inputSecretId'))
      return
    }
    if (!editForm.smsSecretKey) {
      ElMessage.error(t('setting.sms.website.inputSecretKey'))
      return
    }
    // 应用ID：仅腾讯云需要
    if (editForm.smsType === 'tencentCloud' && !editForm.smsSdkAppId) {
      ElMessage.error(t('setting.sms.website.inputAppId'))
      return
    }
    // 模板ID：腾讯云和阿里云需要
    if ((editForm.smsType === 'tencentCloud' || editForm.smsType === 'aliyun') && !editForm.smsGeneral) {
      ElMessage.error(t('setting.sms.website.inputTemplateId'))
      return
    }
    saving.value = true
    
    const formData = { ...editForm }
    delete formData.smsRegion
    const configData = {
      ...formData,
      // 如果是新增，默认禁用；如果是编辑，保持原有状态
      smsStatus: editForm.id ? (editForm.smsStatus || '00') : '00',
      smsIsactive: '01'
    }
    
    console.log('保存短信配置:', configData)
    
    const response = await request.post('/web/base/sms/updateSmsConfigMap', configData)
    
    if (response.code === 1) {
      ElMessage.success(t('setting.sms.website.saveSuccess'))
      closeEditDialog()
      await loadSmsConfigList()
    } else {
      ElMessage.error(t('setting.sms.website.saveFailed') + response.message)
    }
  } catch (error) {
    console.error('保存短信配置失败:', error)
    ElMessage.error(t('setting.sms.website.saveFailed') + error.message)
  } finally {
    saving.value = false
  }
}

// 切换状态
const toggleStatus = async (row) => {
  try {
    row.statusLoading = true
    
    const configData = {
      ...row,
      smsStatus: row.smsStatus
    }
    
    console.log('切换状态:', configData)
    
    const response = await request.post('/web/base/sms/updateSmsConfigMap', configData)
    
    if (response.code === 1) {
      ElMessage.success(t('setting.sms.website.statusUpdateSuccess'))
      await loadSmsConfigList()
    } else {
      ElMessage.error(t('setting.sms.website.statusUpdateFailed') + response.message)
      // 恢复原状态
      row.smsStatus = row.smsStatus === '01' ? '00' : '01'
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error(t('setting.sms.website.statusUpdateFailed') + error.message)
    // 恢复原状态
    row.smsStatus = row.smsStatus === '01' ? '00' : '01'
  } finally {
    row.statusLoading = false
  }
}

// 测试短信发送
const testSMS = async (row) => {
  try {
    const { value: phone } = await ElMessageBox.prompt(t('setting.sms.website.testPhonePrompt'), t('setting.sms.website.testSendTitle'), {
      confirmButtonText: t('setting.sms.website.send'),
      cancelButtonText: t('setting.sms.website.cancel'),
      inputPattern: /^1[3-9]\d{9}$/,
      inputErrorMessage: t('setting.sms.website.inputPhoneError')
    })
    
    if (!phone) return
    
    console.log(`测试${row.smsType}短信发送到:`, phone)
    
    const response = await request.post('/web/auth/smscode', {
      userMobile: phone
    })
    
    if (response.code === 1) {
      ElMessage.success(t('setting.sms.website.testSentPrefix') + phone + t('setting.sms.website.testSentCode') + response.data)
    } else {
      ElMessage.error(t('setting.sms.website.testSendFailed') + response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('测试短信发送失败:', error)
      ElMessage.error(t('setting.sms.website.testSendFailed') + error.message)
    }
  }
}

// 删除短信配置
const deleteSmsConfig = async (row) => {
  try {
    await ElMessageBox.confirm(
      t('setting.sms.website.deleteConfirmPrefix') + getProviderName(row.smsType) + t('setting.sms.website.deleteConfirmSuffix'),
      t('setting.sms.website.deleteTitle'),
      {
        confirmButtonText: t('setting.sms.website.confirm'),
        cancelButtonText: t('setting.sms.website.cancel'),
        type: 'warning'
      }
    )
    
    const configData = {
      ...row,
      smsIsactive: '00' // 软删除
    }
    
    const response = await request.post('/web/base/sms/updateSmsConfigMap', configData)
    
    if (response.code === 1) {
      ElMessage.success(t('setting.sms.website.deleteSuccess'))
      await loadSmsConfigList()
    } else {
      ElMessage.error(t('setting.sms.website.deleteFailed') + response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除短信配置失败:', error)
      ElMessage.error(t('setting.sms.website.deleteFailed') + error.message)
    }
  }
}

const editDraftStorageKey = 'website-sms-edit-draft'

// 跳转到消息模板设置页面，保留当前编辑弹窗和表单状态
const goToMessageTemplate = () => {
  const templateUrl = '/setting/message/temlate'
  const openedWindow = window.open(templateUrl, '_blank')
  if (openedWindow) return

  sessionStorage.setItem(editDraftStorageKey, JSON.stringify({
    title: editDialogTitle.value,
    form: { ...editForm }
  }))
  ElMessage.warning(t('setting.sms.website.popupBlocked'))
  router.push(templateUrl)
}

const restoreEditDraft = () => {
  const rawDraft = sessionStorage.getItem(editDraftStorageKey)
  if (!rawDraft) return

  try {
    const draft = JSON.parse(rawDraft)
    if (!draft || typeof draft !== 'object' || !draft.form || typeof draft.form !== 'object') return
    Object.assign(editForm, draft.form)
    if (draft.title) editDialogTitle.value = draft.title
    editDialogVisible.value = true
  } catch (error) {
    console.warn('恢复短信配置草稿失败:', error)
  } finally {
    sessionStorage.removeItem(editDraftStorageKey)
  }
}

// 关闭编辑对话框
const closeEditDialog = () => {
  editDialogVisible.value = false
  sessionStorage.removeItem(editDraftStorageKey)
  if (editFormRef.value) {
    editFormRef.value.resetFields()
  }
}

// 打开短信设置对话框
const openSettingDialog = () => {
  settingDialogVisible.value = true
  // 打开对话框时重新加载设置
  loadSmsSetting()
}

// 关闭短信设置对话框
const closeSettingDialog = () => {
  settingDialogVisible.value = false
  if (smsSettingFormRef.value) {
    smsSettingFormRef.value.resetFields()
  }
}

// 加载短信设置
const loadSmsSetting = async () => {
  try {
    const response = await request.get('/web/base/sms/getSmsSetting')
    if (response.code === 1 && response.data) {
      smsSettingForm.codeExpireMinutes = response.data.codeExpireMinutes || 30
      smsSettingForm.recordExpireDays = response.data.recordExpireDays || 90
    } else {
      // 使用默认值
      smsSettingForm.codeExpireMinutes = 30
      smsSettingForm.recordExpireDays = 90
    }
  } catch (error) {
    console.error('加载短信设置失败:', error)
    // 使用默认值
    smsSettingForm.codeExpireMinutes = 30
    smsSettingForm.recordExpireDays = 90
  }
}

// 保存短信设置
const saveSmsSetting = async () => {
  try {
    // 表单验证
    if (!smsSettingForm.codeExpireMinutes || smsSettingForm.codeExpireMinutes < 1) {
      ElMessage.error(t('setting.sms.website.codeExpireMinError'))
      return
    }
    if (!smsSettingForm.recordExpireDays || smsSettingForm.recordExpireDays < 1) {
      ElMessage.error(t('setting.sms.website.recordExpireMinError'))
      return
    }
    
    savingSetting.value = true
    
    // 调用后端接口保存配置
    const response = await request.post('/web/base/sms/saveSmsSetting', {
      codeExpireMinutes: smsSettingForm.codeExpireMinutes,
      recordExpireDays: smsSettingForm.recordExpireDays
    })
    
    if (response.code === 1) {
      ElMessage.success(t('setting.sms.website.settingsSaveSuccess'))
      closeSettingDialog()
    } else {
      ElMessage.error(t('setting.sms.website.saveFailed') + response.message)
    }
  } catch (error) {
    console.error('保存短信设置失败:', error)
    ElMessage.error(t('setting.sms.website.saveFailed') + error.message)
  } finally {
    savingSetting.value = false
  }
}

// 重置短信设置
const resetSmsSetting = () => {
  smsSettingForm.codeExpireMinutes = 30
  smsSettingForm.recordExpireDays = 90
  ElMessage.info(t('setting.sms.website.resetDone'))
}

onMounted(() => {
  console.log('WebsiteSMS 组件已挂载')
  loadSmsConfigList()
  loadSmsSetting()
  restoreEditDraft()
})
</script>

<style scoped>
.website-sms {
  padding: 20px;
}

.sms-container {
  min-height: calc(100vh - 200px);
}

.sms-content {
  width: 100%;
}

.config-panel,
.setting-panel {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sms-tabs {
  margin-top: 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-card {
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 0;
  margin-top: 4px;
  line-height: 1.5;
  word-break: break-word;
}

.sms-config-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.sms-config-dialog :deep(.el-form-item__label) {
  word-break: break-word;
  white-space: normal;
  line-height: 1.5;
  padding-right: 12px;
}

.sms-config-dialog :deep(.el-input),
.sms-config-dialog :deep(.el-select) {
  width: 100%;
}

.sms-config-dialog :deep(.el-form-item__content) {
  word-break: break-word;
}
</style>
