<template>
  <div class="monitor-system-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统监控</h2>
      <p>监控服务器和系统资源使用情况</p>
    </div>

    <!-- 系统信息概览 -->
    <div class="overview-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>系统信息</span>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="system-info">
              <div class="info-label">操作系统:</div>
              <div class="info-value">{{ systemInfo.os }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="system-info">
              <div class="info-label">系统架构:</div>
              <div class="info-value">{{ systemInfo.architecture }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="system-info">
              <div class="info-label">运行时间:</div>
              <div class="info-value">{{ systemInfo.uptime }}</div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <div class="system-info">
              <div class="info-label">主机名:</div>
              <div class="info-value">{{ systemInfo.hostname }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="system-info">
              <div class="info-label">内核版本:</div>
              <div class="info-value">{{ systemInfo.kernel }}</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="system-info">
              <div class="info-label">负载均衡:</div>
              <div class="info-value">{{ systemInfo.loadAverage }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 资源使用情况 -->
    <div class="resource-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>CPU使用情况</span>
              </div>
            </template>
            
            <div class="resource-info">
              <el-progress 
                type="circle" 
                :percentage="resourceUsage.cpu" 
                :status="getResourceStatus(resourceUsage.cpu)"
                :width="120"
              />
              <div class="resource-details">
                <div class="detail-item">
                  <span class="detail-label">核心数:</span>
                  <span class="detail-value">{{ cpuInfo.cores }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">频率:</span>
                  <span class="detail-value">{{ cpuInfo.frequency }} GHz</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">使用率:</span>
                  <span class="detail-value">{{ resourceUsage.cpu }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>内存使用情况</span>
              </div>
            </template>
            
            <div class="resource-info">
              <el-progress 
                type="circle" 
                :percentage="resourceUsage.memory" 
                :status="getResourceStatus(resourceUsage.memory)"
                :width="120"
              />
              <div class="resource-details">
                <div class="detail-item">
                  <span class="detail-label">总内存:</span>
                  <span class="detail-value">{{ memoryInfo.total }} GB</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">已使用:</span>
                  <span class="detail-value">{{ memoryInfo.used }} GB</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">使用率:</span>
                  <span class="detail-value">{{ resourceUsage.memory }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>磁盘使用情况</span>
              </div>
            </template>
            
            <div class="resource-info">
              <el-progress 
                type="circle" 
                :percentage="resourceUsage.disk" 
                :status="getResourceStatus(resourceUsage.disk)"
                :width="120"
              />
              <div class="resource-details">
                <div class="detail-item">
                  <span class="detail-label">总容量:</span>
                  <span class="detail-value">{{ diskInfo.total }} GB</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">已使用:</span>
                  <span class="detail-value">{{ diskInfo.used }} GB</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">使用率:</span>
                  <span class="detail-value">{{ resourceUsage.disk }}%</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 网络和进程 -->
    <div class="network-process-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>网络状态</span>
              </div>
            </template>
            
            <div class="network-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="network-metric">
                    <div class="metric-label">接收速率</div>
                    <div class="metric-value">{{ networkStats.rx }} KB/s</div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="network-metric">
                    <div class="metric-label">发送速率</div>
                    <div class="metric-value">{{ networkStats.tx }} KB/s</div>
                  </div>
                </el-col>
              </el-row>
              
              <div class="network-interfaces">
                <div class="interface-item" v-for="iface in networkInterfaces" :key="iface.name">
                  <div class="interface-name">{{ iface.name }}</div>
                  <div class="interface-ip">{{ iface.ip }}</div>
                  <div class="interface-status">
                    <el-tag :type="iface.status === 'up' ? 'success' : 'danger'">
                      {{ iface.status === 'up' ? 'UP' : 'DOWN' }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>进程列表</span>
              </div>
            </template>
            
            <div class="process-list">
              <el-table :data="processList" stripe border style="width: 100%" max-height="300">
                <el-table-column prop="pid" label="PID" width="80" />
                <el-table-column prop="name" label="进程名" min-width="150" />
                <el-table-column prop="cpu" label="CPU%" width="80" />
                <el-table-column prop="memory" label="内存%" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getProcessStatusType(row.status)">
                      {{ getProcessStatusText(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

// 数据
const systemInfo = reactive({
  os: 'Linux Ubuntu 20.04',
  architecture: 'x86_64',
  uptime: '15天 3小时 25分钟',
  hostname: 'server-01',
  kernel: '5.4.0-74-generic',
  loadAverage: '0.15, 0.10, 0.05'
})

const resourceUsage = reactive({
  cpu: 45,
  memory: 65,
  disk: 30
})

const cpuInfo = reactive({
  cores: 8,
  frequency: 3.2
})

const memoryInfo = reactive({
  total: 16,
  used: 10.4
})

const diskInfo = reactive({
  total: 500,
  used: 150
})

const networkStats = reactive({
  rx: 128,
  tx: 64
})

const networkInterfaces = ref([
  { name: 'eth0', ip: '192.168.1.100', status: 'up' },
  { name: 'lo', ip: '127.0.0.1', status: 'up' }
])

const processList = ref([
  { pid: 1234, name: 'nginx', cpu: '2.5', memory: '1.2', status: 'running' },
  { pid: 1235, name: 'mysql', cpu: '5.0', memory: '8.5', status: 'running' },
  { pid: 1236, name: 'redis', cpu: '1.0', memory: '0.8', status: 'sleeping' },
  { pid: 1237, name: 'java', cpu: '15.0', memory: '25.0', status: 'running' }
])

// 方法
const loadSystemInfo = async () => {
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('系统信息加载成功')
  } catch (error) {
    ElMessage.error('加载系统信息失败: ' + error.message)
  }
}

// 状态相关方法
const getResourceStatus = (percentage) => {
  if (percentage < 60) {
    return 'success'
  } else if (percentage < 80) {
    return 'warning'
  } else {
    return 'exception'
  }
}

const getProcessStatusType = (status) => {
  const statusMap = {
    running: 'success',
    sleeping: 'info',
    stopped: 'warning',
    zombie: 'danger'
  }
  return statusMap[status] || 'info'
}

const getProcessStatusText = (status) => {
  const statusMap = {
    running: '运行中',
    sleeping: '睡眠',
    stopped: '停止',
    zombie: '僵尸进程'
  }
  return statusMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  loadSystemInfo()
})
</script>

<style scoped>
.monitor-system-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.system-info {
  display: flex;
  margin-bottom: 15px;
}

.info-label {
  width: 100px;
  font-weight: 500;
  color: #606266;
}

.info-value {
  flex: 1;
  color: #303133;
}

.resource-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.resource-details {
  flex: 1;
  margin-left: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 10px;
}

.detail-label {
  width: 70px;
  color: #606266;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.network-info {
  padding: 10px 0;
}

.network-metric {
  text-align: center;
  margin-bottom: 20px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.network-interfaces {
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.interface-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.interface-name {
  width: 80px;
  font-weight: 500;
  color: #303133;
}

.interface-ip {
  flex: 1;
  color: #606266;
}

.interface-status {
  width: 60px;
}

.process-list {
  padding: 10px 0;
}
</style>
