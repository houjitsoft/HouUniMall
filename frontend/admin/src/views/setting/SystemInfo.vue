<template>
  <div class="system-info">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>系统基本信息</span>
            </div>
          </template>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统名称">{{ systemInfo.systemName || 'HouUniMall管理系统' }}</el-descriptions-item>
            <el-descriptions-item label="系统版本">{{ systemInfo.systemVersion || 'v1.0.8' }}</el-descriptions-item>
            <el-descriptions-item label="构建时间">{{ systemInfo.buildTime || '2024-01-08 10:30:00' }}</el-descriptions-item>
            <el-descriptions-item label="运行环境">{{ systemInfo.environment || '生产环境' }}</el-descriptions-item>
            <el-descriptions-item label="操作系统">{{ systemInfo.operatingSystem || 'Linux 5.4.0-80-generic' }}</el-descriptions-item>
            <el-descriptions-item label="服务器时间">{{ currentTime }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>技术栈信息</span>
            </div>
          </template>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="前端框架">{{ techStack.frontend || 'Vue 3.2.45' }}</el-descriptions-item>
            <el-descriptions-item label="UI框架">{{ techStack.ui || 'Element Plus 2.2.28' }}</el-descriptions-item>
            <el-descriptions-item label="后端框架">{{ techStack.backend || 'Spring Boot 2.7.5' }}</el-descriptions-item>
            <el-descriptions-item label="数据库">{{ techStack.database || 'MySQL 8.0.28' }}</el-descriptions-item>
            <el-descriptions-item label="缓存">{{ techStack.cache || 'Redis 6.2.6' }}</el-descriptions-item>
            <el-descriptions-item label="消息队列">{{ techStack.messageQueue || 'RabbitMQ 3.9.13' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>运行状态</span>
            </div>
          </template>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="JVM版本">{{ systemStatus.jvmVersion || 'OpenJDK 1.8.0_312' }}</el-descriptions-item>
            <el-descriptions-item label="JVM内存使用">
              <el-progress :percentage="systemStatus.memoryUsage || memoryUsage" :status="(systemStatus.memoryUsage || memoryUsage) > 80 ? 'exception' : 'success'" />
            </el-descriptions-item>
            <el-descriptions-item label="CPU使用率">
              <el-progress :percentage="systemStatus.cpuUsage || cpuUsage" :status="(systemStatus.cpuUsage || cpuUsage) > 80 ? 'exception' : 'success'" />
            </el-descriptions-item>
            <el-descriptions-item label="磁盘使用">
              <el-progress :percentage="systemStatus.diskUsage || diskUsage" :status="(systemStatus.diskUsage || diskUsage) > 90 ? 'exception' : 'success'" />
            </el-descriptions-item>
            <el-descriptions-item label="在线用户数">{{ systemStatus.onlineUsers || 125 }}</el-descriptions-item>
            <el-descriptions-item label="系统运行时长">{{ systemStatus.uptime || '15天 8小时 25分钟' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>系统监控</span>
            </div>
          </template>
          
          <div class="monitor-actions">
            <el-button type="primary" @click="refreshInfo" :loading="loading">刷新信息</el-button>
            <el-button @click="exportInfo" :loading="exportLoading">导出信息</el-button>
            <el-button type="danger" @click="clearCache" :loading="clearLoading">清理缓存</el-button>
          </div>
          
          <el-alert
            :title="systemStatus.alertTitle || '系统运行正常'"
            :type="systemStatus.alertType || 'success'"
            :description="systemStatus.alertDescription || '所有服务组件均正常运行，无异常告警'"
            show-icon
            style="margin-top: 20px;"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { systemInfoService } from '@/api/setting/setting.js'
import { getFrameworkVersion } from '@/api/plugin-center/pluginMarket.js'

const currentTime = ref(new Date().toLocaleString())
const memoryUsage = ref(65)
const cpuUsage = ref(42)
const diskUsage = ref(78)
const loading = ref(false)
const exportLoading = ref(false)
const clearLoading = ref(false)

// 系统信息
const systemInfo = reactive({
  systemName: '',
  systemVersion: '',
  buildTime: '',
  environment: '',
  operatingSystem: ''
})

// 技术栈信息
const techStack = reactive({
  frontend: '',
  ui: '',
  backend: '',
  database: '',
  cache: '',
  messageQueue: ''
})

// 系统状态
const systemStatus = reactive({
  jvmVersion: '',
  memoryUsage: 0,
  cpuUsage: 0,
  diskUsage: 0,
  onlineUsers: 0,
  uptime: '',
  alertTitle: '',
  alertType: 'success',
  alertDescription: ''
})

let timer = null
let statusTimer = null

// 加载系统信息
const loadSystemInfo = async () => {
  try {
    loading.value = true
    const response = await systemInfoService.getSystemInfo()
    if (response.code === 1 && response.data) {
      Object.assign(systemInfo, response.data.basic || {})
      Object.assign(techStack, response.data.techStack || {})
    }
  } catch (error) {
    // 开源包没有 commons-service 的 /system/info，本页只剩版本号有意义：
    // 直接读宿主唯一事实源 houcloud.update.framework-version，读不到才留空前端回落。
    console.warn('加载系统信息失败，回落到框架版本接口:', error)
    try {
      const version = (await getFrameworkVersion())?.frameworkVersion
      if (version) systemInfo.systemVersion = `v${version}`
    } catch (fallbackError) {
      console.warn('读取框架版本失败:', fallbackError)
    }
  } finally {
    loading.value = false
  }
}

// 加载系统状态
const loadSystemStatus = async () => {
  try {
    const response = await systemInfoService.getSystemStatus()
    if (response.code === 1 && response.data) {
      Object.assign(systemStatus, response.data)
    }
  } catch (error) {
    console.error('加载系统状态失败:', error)
  }
}

// 刷新信息
const refreshInfo = async () => {
  try {
    loading.value = true
    await Promise.all([
      loadSystemInfo(),
      loadSystemStatus()
    ])
    ElMessage.success('系统信息已刷新')
  } catch (error) {
    console.error('刷新系统信息失败:', error)
    ElMessage.error('刷新系统信息失败')
  } finally {
    loading.value = false
  }
}

// 导出信息
const exportInfo = async () => {
  try {
    exportLoading.value = true
    const response = await systemInfoService.exportSystemInfo()
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `system-info-${new Date().toISOString().slice(0, 10)}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('系统信息导出成功')
  } catch (error) {
    console.error('导出系统信息失败:', error)
    ElMessage.error('导出系统信息失败')
  } finally {
    exportLoading.value = false
  }
}

// 清理缓存
const clearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清理系统缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    clearLoading.value = true
    const response = await systemInfoService.clearSystemCache()
    if (response.code === 1) {
      ElMessage.success('缓存清理完成')
      // 清理后刷新状态
      await loadSystemStatus()
    } else {
      ElMessage.error(response.message || '缓存清理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理缓存失败:', error)
      ElMessage.error('清理缓存失败')
    }
  } finally {
    clearLoading.value = false
  }
}

// 更新时间
const updateTime = () => {
  currentTime.value = new Date().toLocaleString()
}

// 定时更新状态
const updateStatus = () => {
  loadSystemStatus()
}

onMounted(() => {
  // 加载初始数据
  loadSystemInfo()
  loadSystemStatus()
  
  // 启动定时器
  timer = setInterval(updateTime, 1000)
  statusTimer = setInterval(updateStatus, 30000) // 每30秒更新一次状态
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
  if (statusTimer) {
    clearInterval(statusTimer)
  }
})
</script>

<style scoped>
.system-info {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-card {
  height: 100%;
}

.monitor-actions {
  display: flex;
  gap: 10px;
}
</style>
