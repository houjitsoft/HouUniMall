<template>
  <div class="security-scan">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>安全扫描</span>
          <el-button type="primary" @click="startScan">
            <el-icon><Search /></el-icon>
            开始扫描
          </el-button>
        </div>
      </template>
      
      <div class="scan-config">
        <el-form :model="scanConfig" label-width="120px">
          <el-form-item label="扫描类型">
            <el-checkbox-group v-model="scanConfig.types">
              <el-checkbox label="weakPassword">弱密码检测</el-checkbox>
              <el-checkbox label="permission">权限配置检查</el-checkbox>
              <el-checkbox label="vulnerability">漏洞扫描</el-checkbox>
              <el-checkbox label="config">配置安全检查</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          
          <el-form-item label="扫描范围">
            <el-radio-group v-model="scanConfig.scope">
              <el-radio label="all">全系统扫描</el-radio>
              <el-radio label="custom">自定义范围</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item v-if="scanConfig.scope === 'custom'" label="模块选择">
            <el-checkbox-group v-model="scanConfig.modules">
              <el-checkbox label="user">用户管理</el-checkbox>
              <el-checkbox label="permission">权限管理</el-checkbox>
              <el-checkbox label="system">系统配置</el-checkbox>
              <el-checkbox label="database">数据库安全</el-checkbox>
              <el-checkbox label="network">网络安全</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          
          <el-form-item label="定时扫描">
            <el-switch v-model="scanConfig.scheduled" />
          </el-form-item>
          
          <el-form-item v-if="scanConfig.scheduled" label="扫描周期">
            <el-select v-model="scanConfig.scheduleType">
              <el-option label="每天" value="daily" />
              <el-option label="每周" value="weekly" />
              <el-option label="每月" value="monthly" />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="scanConfig.scheduled && scanConfig.scheduleType === 'weekly'" label="扫描时间">
            <el-select v-model="scanConfig.weekDay">
              <el-option label="周一" value="monday" />
              <el-option label="周二" value="tuesday" />
              <el-option label="周三" value="wednesday" />
              <el-option label="周四" value="thursday" />
              <el-option label="周五" value="friday" />
              <el-option label="周六" value="saturday" />
              <el-option label="周日" value="sunday" />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="scanConfig.scheduled" label="执行时间">
            <el-time-picker
              v-model="scanConfig.executeTime"
              format="HH:mm"
              value-format="HH:mm"
              placeholder="选择执行时间"
            />
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>扫描结果</span>
          <div>
            <el-button @click="exportReport">导出报告</el-button>
            <el-button type="danger" @click="clearResults">清空结果</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="scanResults.length > 0">
        <div class="scan-summary">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value">{{ scanResults.length }}</div>
                <div class="summary-label">总问题数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value high-risk">{{ riskCount.high }}</div>
                <div class="summary-label">高风险</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value medium-risk">{{ riskCount.medium }}</div>
                <div class="summary-label">中风险</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <div class="summary-value low-risk">{{ riskCount.low }}</div>
                <div class="summary-label">低风险</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <el-table :data="scanResults" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="type" label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getRiskTagType(row.riskLevel)">
                {{ getRiskText(row.riskLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="description" label="问题描述" />
          <el-table-column prop="suggestion" label="修复建议" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'fixed' ? 'success' : 'warning'">
                {{ row.status === 'fixed' ? '已修复' : '未修复' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="viewDetail(row)">
                查看详情
              </el-button>
              <el-button 
                v-if="row.status !== 'fixed'" 
                size="small" 
                type="success" 
                link 
                @click="markAsFixed(row)"
              >
                标记已修复
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="no-results">
        <el-empty description="暂无扫描结果" />
      </div>
    </el-card>
    
    <!-- 扫描详情对话框 -->
    <el-dialog v-model="detailVisible" title="问题详情" width="600px">
      <div class="scan-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="问题ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="getRiskTagType(detailData.riskLevel)">
              {{ getRiskText(detailData.riskLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属模块">{{ detailData.module }}</el-descriptions-item>
          <el-descriptions-item label="问题描述">{{ detailData.description }}</el-descriptions-item>
          <el-descriptions-item label="影响范围">{{ detailData.impact }}</el-descriptions-item>
          <el-descriptions-item label="修复建议">{{ detailData.suggestion }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 'fixed' ? 'success' : 'warning'">
              {{ detailData.status === 'fixed' ? '已修复' : '未修复' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const scanConfig = reactive({
  types: ['weakPassword', 'permission', 'vulnerability', 'config'],
  scope: 'all',
  modules: [],
  scheduled: false,
  scheduleType: 'daily',
  weekDay: 'monday',
  executeTime: '02:00'
})

const scanResults = ref([
  {
    id: 1,
    riskLevel: 'high',
    module: '用户管理',
    description: '存在弱密码账户',
    impact: '可能导致账户被暴力破解',
    suggestion: '强制用户修改为强密码',
    status: 'unfixed'
  },
  {
    id: 2,
    riskLevel: 'medium',
    module: '权限管理',
    description: '过多用户拥有管理员权限',
    impact: '增加系统安全风险',
    suggestion: '按最小权限原则分配权限',
    status: 'unfixed'
  },
  {
    id: 3,
    riskLevel: 'low',
    module: '系统配置',
    description: '登录失败次数限制未配置',
    impact: '可能遭受暴力破解攻击',
    suggestion: '设置登录失败次数限制',
    status: 'fixed'
  }
])

const detailVisible = ref(false)
const detailData = ref({})

const riskCount = computed(() => {
  const count = { high: 0, medium: 0, low: 0 }
  scanResults.value.forEach(item => {
    if (item.riskLevel === 'high') count.high++
    else if (item.riskLevel === 'medium') count.medium++
    else if (item.riskLevel === 'low') count.low++
  })
  return count
})

const getRiskTagType = (riskLevel) => {
  const typeMap = {
    'high': 'danger',
    'medium': 'warning',
    'low': 'info'
  }
  return typeMap[riskLevel] || 'info'
}

const getRiskText = (riskLevel) => {
  const textMap = {
    'high': '高风险',
    'medium': '中风险',
    'low': '低风险'
  }
  return textMap[riskLevel] || riskLevel
}

const startScan = () => {
  ElMessage.success('开始安全扫描...')
}

const exportReport = () => {
  ElMessage.success('正在导出扫描报告...')
}

const clearResults = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有扫描结果吗？', '确认清空', {
      type: 'warning'
    })
    scanResults.value = []
    ElMessage.success('扫描结果已清空')
  } catch {
    ElMessage.info('已取消清空')
  }
}

const viewDetail = (row) => {
  detailData.value = row
  detailVisible.value = true
}

const markAsFixed = (row) => {
  row.status = 'fixed'
  ElMessage.success('已标记为已修复')
}
</script>

<style scoped>
.security-scan {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scan-summary {
  margin-bottom: 20px;
}

.summary-item {
  text-align: center;
  padding: 20px 0;
  background: #f5f5f5;
  border-radius: 4px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.summary-value.high-risk {
  color: #f56c6c;
}

.summary-value.medium-risk {
  color: #e6a23c;
}

.summary-value.low-risk {
  color: #909399;
}

.summary-label {
  font-size: 14px;
  color: #606266;
  margin-top: 5px;
}

.no-results {
  text-align: center;
  padding: 40px 0;
}

.scan-detail {
  padding: 20px;
}
</style>
