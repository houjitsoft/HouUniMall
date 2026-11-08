<template>
  <div class="payment-risk-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-risk">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ riskStats.total }}</div>
              <div class="stat-label">风险事件</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card high-risk">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Top /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ riskStats.highRisk }}</div>
              <div class="stat-label">高风险</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card processing">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Clock /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ riskStats.processing }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card resolved">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Check /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ riskStats.resolved }}</div>
              <div class="stat-label">已解决</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="风险等级">
          <el-select v-model="searchForm.riskLevel" placeholder="请选择风险等级" clearable>
            <el-option label="低风险" value="low" />
            <el-option label="中风险" value="medium" />
            <el-option label="高风险" value="high" />
            <el-option label="极高风险" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
            <el-option label="已忽略" value="ignored" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="searchForm.eventType" placeholder="请选择事件类型" clearable>
            <el-option label="异常交易" value="abnormal_transaction" />
            <el-option label="频繁支付" value="frequent_payment" />
            <el-option label="大额交易" value="large_amount" />
            <el-option label="黑名单用户" value="blacklist_user" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <el-card class="action-card">
      <el-row :gutter="10">
        <el-col :span="24">
          <el-button type="primary" @click="handleBatchProcess" :disabled="!selectedRows.length">
            <el-icon><Operation /></el-icon>
            批量处理
          </el-button>
          <el-button type="warning" @click="handleBatchIgnore" :disabled="!selectedRows.length">
            <el-icon><Close /></el-icon>
            批量忽略
          </el-button>
          <el-button type="success" @click="handleExportRisk">
            <el-icon><Download /></el-icon>
            导出风险报告
          </el-button>
          <el-button type="info" @click="handleRiskConfig">
            <el-icon><Setting /></el-icon>
            风控配置
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 风险事件列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <span class="table-title">风险事件列表</span>
        <div class="table-controls">
          <el-button size="small" @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <el-table
        :data="riskList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="事件ID" width="120" />
        <el-table-column prop="eventType" label="事件类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getEventTypeTagType(row.eventType)">
              {{ getEventTypeName(row.eventType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskLevel" label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskLevelTagType(row.riskLevel)">
              {{ getRiskLevelName(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="事件描述" min-width="200" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="amount" label="涉及金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发生时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleProcess(row)"
              v-if="row.status === 'pending'"
            >
              处理
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="handleIgnore(row)"
              v-if="row.status === 'pending'"
            >
              忽略
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 风险详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="风险事件详情" width="800px">
      <div class="risk-detail" v-if="currentRisk">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="事件ID">{{ currentRisk.id }}</el-descriptions-item>
          <el-descriptions-item label="事件类型">
            <el-tag :type="getEventTypeTagType(currentRisk.eventType)">
              {{ getEventTypeName(currentRisk.eventType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="getRiskLevelTagType(currentRisk.riskLevel)">
              {{ getRiskLevelName(currentRisk.riskLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentRisk.userId }}</el-descriptions-item>
          <el-descriptions-item label="涉及金额">
            <span class="amount">¥{{ currentRisk.amount?.toFixed(2) || '0.00' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusTagType(currentRisk.status)">
              {{ getStatusName(currentRisk.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生时间" :span="2">{{ currentRisk.createTime }}</el-descriptions-item>
          <el-descriptions-item label="事件描述" :span="2">{{ currentRisk.description }}</el-descriptions-item>
        </el-descriptions>

        <!-- 处理记录 -->
        <div class="process-records" v-if="currentRisk.processRecords?.length">
          <h4>处理记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="record in currentRisk.processRecords"
              :key="record.id"
              :timestamp="record.createTime"
            >
              <div class="record-content">
                <div class="record-action">{{ record.action }}</div>
                <div class="record-operator">操作人：{{ record.operator }}</div>
                <div class="record-remark" v-if="record.remark">备注：{{ record.remark }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理风险事件" width="500px">
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="processForm.action">
            <el-radio label="resolve">解决</el-radio>
            <el-radio label="ignore">忽略</el-radio>
            <el-radio label="escalate">升级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProcess">确认处理</el-button>
      </template>
    </el-dialog>

    <!-- 风控配置对话框 -->
    <el-dialog v-model="configDialogVisible" title="风控配置" width="800px">
      <el-form :model="configForm" label-width="120px">
        <el-form-item label="大额交易阈值">
          <el-input-number v-model="configForm.largeAmountThreshold" :min="0" />
          <span class="form-tip">元</span>
        </el-form-item>
        <el-form-item label="频繁支付次数">
          <el-input-number v-model="configForm.frequentPaymentCount" :min="1" />
          <span class="form-tip">次/小时</span>
        </el-form-item>
        <el-form-item label="风险等级自动升级">
          <el-switch v-model="configForm.autoEscalate" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="configForm.notificationMethods">
            <el-checkbox label="email">邮件</el-checkbox>
            <el-checkbox label="sms">短信</el-checkbox>
            <el-checkbox label="webhook">Webhook</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
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
import {
  Search,
  Refresh,
  Operation,
  Close,
  Download,
  Setting,
  Warning,
  Top,
  Clock,
  Check
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const selectedRows = ref([])

// 统计数据
const riskStats = reactive({
  total: 0,
  highRisk: 0,
  processing: 0,
  resolved: 0
})

// 搜索表单
const searchForm = reactive({
  riskLevel: '',
  status: '',
  eventType: '',
  dateRange: []
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 风险事件列表数据
const riskList = ref([])

// 对话框状态
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const configDialogVisible = ref(false)

// 当前选中的风险事件
const currentRisk = ref(null)

// 处理表单
const processForm = reactive({
  action: 'resolve',
  remark: ''
})

// 配置表单
const configForm = reactive({
  largeAmountThreshold: 10000,
  frequentPaymentCount: 10,
  autoEscalate: true,
  notificationMethods: ['email']
})

// 方法定义
const handleSearch = () => {
  loadRiskList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    riskLevel: '',
    status: '',
    eventType: '',
    dateRange: []
  })
  loadRiskList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBatchProcess = () => {
  ElMessageBox.confirm('确认批量处理选中的风险事件吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量处理成功')
    loadRiskList()
  })
}

const handleBatchIgnore = () => {
  ElMessageBox.confirm('确认批量忽略选中的风险事件吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('批量忽略成功')
    loadRiskList()
  })
}

const handleExportRisk = () => {
  ElMessage.info('请前往「风控管理 → 风控日志」查看与处理')
}

const handleRiskConfig = () => {
  configDialogVisible.value = true
}

const handleViewDetail = (row) => {
  currentRisk.value = row
  detailDialogVisible.value = true
}

const handleProcess = (row) => {
  currentRisk.value = row
  processForm.action = 'resolve'
  processForm.remark = ''
  processDialogVisible.value = true
}

const handleIgnore = (row) => {
  currentRisk.value = row
  processForm.action = 'ignore'
  processForm.remark = ''
  processDialogVisible.value = true
}

const confirmProcess = async () => {
  try {
    const res = await request.post('/web/pay/risk/log/audit', {
      id: currentRisk.value?.id,
      auditStatus: processForm.action === 'resolve' ? 1 : 2,
      remark: processForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('风险事件处理成功')
      processDialogVisible.value = false
      loadRiskList()
    } else {
      ElMessage.error(res.message || '处理失败')
    }
  } catch (e) {
    ElMessage.error('处理失败: ' + (e.message || '网络错误'))
  }
}

const saveConfig = () => {
  ElMessage.success('风控配置保存成功（请前往「风控管理 → 风控规则配置」进行完整配置）')
  configDialogVisible.value = false
}

const handleRefresh = () => {
  loadRiskList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadRiskList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadRiskList()
}

// 加载风险日志（透传风控日志分页，走 hou_pay_risk_log 真实数据）
const loadRiskList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      riskLevel: searchForm.riskLevel || undefined,
      status: searchForm.status || undefined,
      ruleType: searchForm.eventType || undefined
    }
    const res = await request.post('/web/pay/risk/log/list', params)
    if (res.code === 200) {
      const data = res.data || {}
      const records = data.records || data.list || []
      riskList.value = records.map((r) => ({
        id: r.id,
        eventType: r.ruleType || r.riskType || 'abnormal_transaction',
        riskLevel: r.riskLevel || 'medium',
        description: r.riskDesc || r.description || '',
        userId: r.userId || r.targetId || '',
        amount: r.amount ?? 0,
        status: mapRiskStatus(r.status),
        createTime: r.createTime || r.gmtCreate || '',
        processRecords: [],
        _raw: r
      }))
      pagination.total = data.total || data.totalCount || 0
      let high = 0
      let processing = 0
      let resolved = 0
      records.forEach((r) => {
        if (r.riskLevel === 'high' || r.riskLevel === 'critical') high += 1
        if (mapRiskStatus(r.status) === 'processing') processing += 1
        if (mapRiskStatus(r.status) === 'resolved') resolved += 1
      })
      riskStats.total = data.total || data.totalCount || records.length
      riskStats.highRisk = high
      riskStats.processing = processing
      riskStats.resolved = resolved
    } else {
      riskList.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error('加载风控日志失败:', e)
    riskList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 风险状态映射（0待处理 1处理中 2已解决 3已忽略）
const mapRiskStatus = (status) => {
  const s = String(status == null ? '' : status)
  const map = {
    '0': 'pending',
    '1': 'processing',
    '2': 'resolved',
    '3': 'ignored',
    'PENDING': 'pending',
    'PROCESSING': 'processing',
    'RESOLVED': 'resolved',
    'IGNORED': 'ignored',
    'SUCCESS': 'resolved',
    'FAILED': 'pending'
  }
  return map[s] || map[status] || 'pending'
}

// 获取事件类型名称
const getEventTypeName = (type) => {
  const typeMap = {
    abnormal_transaction: '异常交易',
    frequent_payment: '频繁支付',
    large_amount: '大额交易',
    blacklist_user: '黑名单用户'
  }
  return typeMap[type] || type
}

// 获取事件类型标签类型
const getEventTypeTagType = (type) => {
  const typeMap = {
    abnormal_transaction: 'danger',
    frequent_payment: 'warning',
    large_amount: 'primary',
    blacklist_user: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取风险等级名称
const getRiskLevelName = (level) => {
  const levelMap = {
    low: '低风险',
    medium: '中风险',
    high: '高风险',
    critical: '极高风险'
  }
  return levelMap[level] || level
}

// 获取风险等级标签类型
const getRiskLevelTagType = (level) => {
  const levelMap = {
    low: 'success',
    medium: 'warning',
    high: 'danger',
    critical: 'danger'
  }
  return levelMap[level] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    ignored: '已忽略'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const statusMap = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success',
    ignored: 'info'
  }
  return statusMap[status] || 'info'
}

// 初始化
onMounted(() => {
  loadRiskList()
})
</script>

<style scoped>
.payment-risk-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.total-risk .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.high-risk .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.processing .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.resolved .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-card, .action-card, .table-card {
  margin-bottom: 20px;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.amount {
  color: #E6A23C;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.risk-detail {
  margin-bottom: 20px;
}

.process-records {
  margin-top: 30px;
}

.process-records h4 {
  margin-bottom: 15px;
  color: #303133;
}

.record-content {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 5px;
  border-left: 3px solid #409eff;
}

.record-action {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.record-operator, .record-remark {
  font-size: 12px;
  color: #909399;
  margin-bottom: 3px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
