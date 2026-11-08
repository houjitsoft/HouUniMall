<template>
  <div class="workflow-dashboard">
    <div class="page-header">
      <h2>工作流概览</h2>
      <p>工作流管理系统的总览和快速操作</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="32" color="#409eff">
            <Document />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalProcesses }}</div>
          <div class="stat-label">流程总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="32" color="#67c23a">
            <Clock />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.pendingTasks }}</div>
          <div class="stat-label">待办任务</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="32" color="#e6a23c">
            <TrendCharts />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.completedToday }}</div>
          <div class="stat-label">今日完成</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="32" color="#f56c6c">
            <Warning />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.overdueTasks }}</div>
          <div class="stat-label">逾期任务</div>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h3>快速操作</h3>
      <div class="action-grid">
        <el-card class="action-card" @click="navigateTo('/workflow/processes')">
          <div class="action-content">
            <el-icon :size="24" color="#409eff">
              <Setting />
            </el-icon>
            <span>流程管理</span>
          </div>
        </el-card>
        
        <el-card class="action-card" @click="navigateTo('/workflow/applications')">
          <div class="action-content">
            <el-icon :size="24" color="#67c23a">
              <Document />
            </el-icon>
            <span>我的申请</span>
          </div>
        </el-card>
        
        <el-card class="action-card" @click="navigateTo('/workflow/tasks')">
          <div class="action-content">
            <el-icon :size="24" color="#e6a23c">
              <Clock />
            </el-icon>
            <span>待办任务</span>
          </div>
        </el-card>
        
        <el-card class="action-card" @click="navigateTo('/workflow/statistics')">
          <div class="action-content">
            <el-icon :size="24" color="#909399">
              <TrendCharts />
            </el-icon>
            <span>流程统计</span>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities">
      <h3>最近活动</h3>
      <el-card>
        <div class="activity-list">
          <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <div class="activity-icon">
              <el-icon :size="16" :color="getActivityColor(activity.type)">
                <component :is="getActivityIcon(activity.type)" />
              </el-icon>
            </div>
            <div class="activity-content">
              <div class="activity-text">{{ activity.description }}</div>
              <div class="activity-time">{{ formatTime(activity.time) }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Document, 
  Clock, 
  TrendCharts, 
  Warning, 
  Setting,
  Check,
  Close,
  InfoFilled
} from '@element-plus/icons-vue'

const router = useRouter()

// 统计数据
const stats = ref({
  totalProcesses: 21,
  pendingTasks: 5,
  completedToday: 12,
  overdueTasks: 2
})

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'approve',
    description: '审批了张三的请假申请',
    time: new Date(Date.now() - 10 * 60 * 1000) // 10分钟前
  },
  {
    id: 2,
    type: 'reject',
    description: '拒绝了李四的报销申请',
    time: new Date(Date.now() - 30 * 60 * 1000) // 30分钟前
  },
  {
    id: 3,
    type: 'submit',
    description: '提交了新的采购申请',
    time: new Date(Date.now() - 60 * 60 * 1000) // 1小时前
  },
  {
    id: 4,
    type: 'complete',
    description: '完成了项目立项流程',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000) // 2小时前
  }
])

// 导航到指定页面
const navigateTo = (path) => {
  router.push(path)
}

// 获取活动图标
const getActivityIcon = (type) => {
  const iconMap = {
    approve: Check,
    reject: Close,
    submit: Document,
    complete: TrendCharts
  }
  return iconMap[type] || InfoFilled
}

// 获取活动颜色
const getActivityColor = (type) => {
  const colorMap = {
    approve: '#67c23a',
    reject: '#f56c6c',
    submit: '#409eff',
    complete: '#909399'
  }
  return colorMap[type] || '#909399'
}

// 格式化时间
const formatTime = (time) => {
  const now = new Date()
  const diff = now - time
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return time.toLocaleDateString()
  }
}

onMounted(() => {
  // 加载统计数据
})
</script>

<style scoped>
.workflow-dashboard {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-icon {
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.quick-actions {
  margin-bottom: 32px;
}

.quick-actions h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.action-card {
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.action-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
}

.action-content span {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.recent-activities h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.activity-list {
  padding: 0;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
