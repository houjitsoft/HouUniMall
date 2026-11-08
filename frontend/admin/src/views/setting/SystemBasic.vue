<template>
  <div class="system-basic">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>基础配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="系统设置" name="system">
          <div class="tab-content">
            <el-form :model="systemForm" label-width="120px">
              <el-form-item label="系统名称">
                <el-input v-model="systemForm.systemName" placeholder="请输入系统名称" />
              </el-form-item>
              
              <el-form-item label="系统Logo">
                <el-upload
                  v-model:file-list="systemForm.logoFileList"
                  class="avatar-uploader"
                  action=""
                  :auto-upload="false"
                  :show-file-list="false"
                >
                  <img v-if="systemForm.logoUrl" :src="systemForm.logoUrl" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
              
              <el-form-item label="系统描述">
                <el-input
                  v-model="systemForm.systemDescription"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入系统描述"
                />
              </el-form-item>
              
              <el-form-item label="系统模式">
                <el-radio-group v-model="systemForm.systemMode">
                  <el-radio label="production">生产模式</el-radio>
                  <el-radio label="development">开发模式</el-radio>
                  <el-radio label="test">测试模式</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="维护模式">
                <el-switch v-model="systemForm.maintenanceMode" />
              </el-form-item>
              
              <el-form-item v-if="systemForm.maintenanceMode" label="维护提示">
                <el-input
                  v-model="systemForm.maintenanceMessage"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入维护提示信息"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="网络设置" name="network">
          <div class="tab-content">
            <el-form :model="networkForm" label-width="120px">
              <el-form-item label="域名">
                <el-input v-model="networkForm.domain" placeholder="请输入系统域名" />
              </el-form-item>
              
              <el-form-item label="端口">
                <el-input-number v-model="networkForm.port" :min="1" :max="65535" />
              </el-form-item>
              
              <el-form-item label="SSL证书">
                <el-switch v-model="networkForm.sslEnabled" />
              </el-form-item>
              
              <el-form-item v-if="networkForm.sslEnabled" label="证书文件">
                <el-input v-model="networkForm.certFile" placeholder="请输入证书文件路径" />
              </el-form-item>
              
              <el-form-item v-if="networkForm.sslEnabled" label="私钥文件">
                <el-input v-model="networkForm.keyFile" placeholder="请输入私钥文件路径" />
              </el-form-item>
              
              <el-form-item label="允许跨域">
                <el-switch v-model="networkForm.corsEnabled" />
              </el-form-item>
              
              <el-form-item v-if="networkForm.corsEnabled" label="跨域域名">
                <el-input v-model="networkForm.corsDomains" placeholder="请输入允许跨域的域名，多个用逗号分隔" />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="文件设置" name="file">
          <div class="tab-content">
            <el-form :model="fileForm" label-width="120px">
              <el-form-item label="存储路径">
                <el-input v-model="fileForm.storagePath" placeholder="请输入文件存储路径" />
              </el-form-item>
              
              <el-form-item label="上传限制">
                <el-input-number v-model="fileForm.uploadLimit" :min="1" :max="1024" />
                <span class="form-tip">MB</span>
              </el-form-item>
              
              <el-form-item label="允许格式">
                <el-input v-model="fileForm.allowedTypes" placeholder="请输入允许的文件格式，多个用逗号分隔" />
              </el-form-item>
              
              <el-form-item label="临时文件清理">
                <el-switch v-model="fileForm.autoCleanup" />
              </el-form-item>
              
              <el-form-item v-if="fileForm.autoCleanup" label="清理周期">
                <el-input-number v-model="fileForm.cleanupDays" :min="1" :max="365" />
                <span class="form-tip">天</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div class="form-actions">
        <el-button type="primary" @click="saveSettings" :loading="loading">保存配置</el-button>
        <el-button @click="resetSettings" :disabled="loading">重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { systemBasicService } from '@/api/setting/setting.js'

const activeTab = ref('system')
const loading = ref(false)

// 系统设置表单
const systemForm = reactive({
  systemName: '',
  logoFileList: [],
  logoUrl: '',
  systemDescription: '',
  systemMode: 'production',
  maintenanceMode: false,
  maintenanceMessage: ''
})

// 网络设置表单
const networkForm = reactive({
  domain: '',
  port: 8080,
  sslEnabled: false,
  certFile: '',
  keyFile: '',
  corsEnabled: false,
  corsDomains: ''
})

// 文件设置表单
const fileForm = reactive({
  storagePath: '',
  uploadLimit: 10,
  allowedTypes: '',
  autoCleanup: false,
  cleanupDays: 7
})

// 原始数据备份，用于重置
const originalSystemForm = reactive({})
const originalNetworkForm = reactive({})
const originalFileForm = reactive({})

// 加载系统基础配置
const loadSystemBasicConfig = async () => {
  try {
    loading.value = true
    const response = await systemBasicService.getSystemBasicConfig()
    if (response.code === 1 && response.data) {
      Object.assign(systemForm, response.data.system || {})
      Object.assign(originalSystemForm, response.data.system || {})
    }
  } catch (error) {
    console.error('加载系统基础配置失败:', error)
    ElMessage.error('加载系统基础配置失败')
  } finally {
    loading.value = false
  }
}

// 加载网络配置
const loadNetworkConfig = async () => {
  try {
    loading.value = true
    const response = await systemBasicService.getNetworkConfig()
    if (response.code === 1 && response.data) {
      Object.assign(networkForm, response.data)
      Object.assign(originalNetworkForm, response.data)
    }
  } catch (error) {
    console.error('加载网络配置失败:', error)
    ElMessage.error('加载网络配置失败')
  } finally {
    loading.value = false
  }
}

// 加载文件配置
const loadFileConfig = async () => {
  try {
    loading.value = true
    const response = await systemBasicService.getFileConfig()
    if (response.code === 1 && response.data) {
      Object.assign(fileForm, response.data)
      Object.assign(originalFileForm, response.data)
    }
  } catch (error) {
    console.error('加载文件配置失败:', error)
    ElMessage.error('加载文件配置失败')
  } finally {
    loading.value = false
  }
}

// 保存系统设置
const saveSystemSettings = async () => {
  try {
    loading.value = true
    const response = await systemBasicService.saveSystemBasicConfig(systemForm)
    if (response.code === 1) {
      ElMessage.success('系统设置保存成功')
      Object.assign(originalSystemForm, { ...systemForm })
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存系统设置失败:', error)
    ElMessage.error('保存系统设置失败')
  } finally {
    loading.value = false
  }
}

// 保存网络设置
const saveNetworkSettings = async () => {
  try {
    loading.value = true
    const response = await systemBasicService.saveNetworkConfig(networkForm)
    if (response.code === 1) {
      ElMessage.success('网络设置保存成功')
      Object.assign(originalNetworkForm, { ...networkForm })
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存网络设置失败:', error)
    ElMessage.error('保存网络设置失败')
  } finally {
    loading.value = false
  }
}

// 保存文件设置
const saveFileSettings = async () => {
  try {
    loading.value = true
    const response = await systemBasicService.saveFileConfig(fileForm)
    if (response.code === 1) {
      ElMessage.success('文件设置保存成功')
      Object.assign(originalFileForm, { ...fileForm })
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存文件设置失败:', error)
    ElMessage.error('保存文件设置失败')
  } finally {
    loading.value = false
  }
}

// 保存所有设置
const saveSettings = async () => {
  try {
    loading.value = true
    const loadingInstance = ElLoading.service({
      lock: true,
      text: '正在保存配置...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 根据当前激活的标签页保存对应的配置
    switch (activeTab.value) {
      case 'system':
        await saveSystemSettings()
        break
      case 'network':
        await saveNetworkSettings()
        break
      case 'file':
        await saveFileSettings()
        break
      default:
        // 保存所有配置
        await Promise.all([
          saveSystemSettings(),
          saveNetworkSettings(),
          saveFileSettings()
        ])
    }

    loadingInstance.close()
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  }
}

// 重置设置
const resetSettings = () => {
  switch (activeTab.value) {
    case 'system':
      Object.assign(systemForm, { ...originalSystemForm })
      ElMessage.info('系统设置已重置')
      break
    case 'network':
      Object.assign(networkForm, { ...originalNetworkForm })
      ElMessage.info('网络设置已重置')
      break
    case 'file':
      Object.assign(fileForm, { ...originalFileForm })
      ElMessage.info('文件设置已重置')
      break
    default:
      Object.assign(systemForm, { ...originalSystemForm })
      Object.assign(networkForm, { ...originalNetworkForm })
      Object.assign(fileForm, { ...originalFileForm })
      ElMessage.info('所有设置已重置')
  }
}

// 监听标签页切换，加载对应配置
const handleTabChange = (tabName) => {
  switch (tabName) {
    case 'system':
      if (!originalSystemForm.systemName) {
        loadSystemBasicConfig()
      }
      break
    case 'network':
      if (!originalNetworkForm.domain) {
        loadNetworkConfig()
      }
      break
    case 'file':
      if (!originalFileForm.storagePath) {
        loadFileConfig()
      }
      break
  }
}

// 组件挂载时加载默认配置
onMounted(() => {
  loadSystemBasicConfig()
})
</script>

<style scoped>
.system-basic {
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

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>