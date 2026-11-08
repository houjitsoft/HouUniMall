<template>
  <div class="jpush-manage">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 配置管理 -->
      <el-tab-pane label="推送配置" name="config">
        <el-form :model="configForm" label-width="140px" style="max-width: 700px" v-loading="configLoading">
          <el-alert title="极光推送配置说明" type="info" :closable="false" show-icon style="margin-bottom: 20px">
            <template #default>
              请在 <el-link type="primary" href="https://www.jiguang.cn/push" target="_blank">极光推送官网</el-link>
              创建应用后，获取 AppKey 和 Master Secret 填入下方。
            </template>
          </el-alert>

          <el-form-item label="AppKey">
            <el-input v-model="configForm.appKey" placeholder="极光应用 AppKey" clearable />
          </el-form-item>
          <el-form-item label="Master Secret">
            <el-input v-model="configForm.masterSecret" placeholder="极光应用 Master Secret" show-password clearable />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-switch v-model="configForm.status" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="默认推送标题">
            <el-input v-model="configForm.defaultTitle" placeholder="推送通知的默认标题" />
          </el-form-item>
          <el-form-item label="推送平台">
            <el-radio-group v-model="configForm.channel">
              <el-radio value="all">全平台</el-radio>
              <el-radio value="android">仅 Android</el-radio>
              <el-radio value="ios">仅 iOS</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-divider content-position="left">iOS 推送设置（APNs）</el-divider>
          <el-form-item label="启用 APNs">
            <el-switch v-model="configForm.apnsEnabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <template v-if="configForm.apnsEnabled === 1">
            <el-form-item label="APNs 环境">
              <el-radio-group v-model="configForm.apnsProduction">
                <el-radio :value="0">开发环境</el-radio>
                <el-radio :value="1">生产环境</el-radio>
              </el-radio-group>
            </el-form-item>
          </template>

          <el-form-item>
            <el-button type="primary" @click="saveConfig" :loading="saveLoading">保存配置</el-button>
            <el-button @click="testConnection" :loading="testLoading">测试连接</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 发送推送 -->
      <el-tab-pane label="发送推送" name="send">
        <el-form :model="pushForm" label-width="120px" style="max-width: 700px" :rules="pushRules" ref="pushFormRef">
          <el-form-item label="推送类型">
            <el-radio-group v-model="pushForm.pushType">
              <el-radio value="broadcast">广播（全部用户）</el-radio>
              <el-radio value="tag">标签推送</el-radio>
              <el-radio value="single">单推（别名）</el-radio>
              <el-radio value="group">群推（多别名）</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item v-if="pushForm.pushType !== 'broadcast'" label="推送目标" prop="targetValue">
            <el-input
              v-model="pushForm.targetValue"
              :placeholder="pushForm.pushType === 'tag' ? '输入标签，多个用逗号分隔' : '输入别名/Registration ID，多个用逗号分隔'"
              type="textarea" :rows="2"
            />
          </el-form-item>

          <el-form-item label="推送标题" prop="title">
            <el-input v-model="pushForm.title" placeholder="推送通知标题" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="推送内容" prop="content">
            <el-input v-model="pushForm.content" placeholder="推送通知内容" type="textarea" :rows="4" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="消息类型">
            <el-radio-group v-model="pushForm.msgType">
              <el-radio value="notification">通知（系统通知栏）</el-radio>
              <el-radio value="message">自定义消息（透传）</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="推送平台">
            <el-radio-group v-model="pushForm.platform">
              <el-radio value="all">全平台</el-radio>
              <el-radio value="android">Android</el-radio>
              <el-radio value="ios">iOS</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="sendPush" :loading="sendLoading">
              <el-icon><Promotion /></el-icon> 立即发送
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 推送历史 -->
      <el-tab-pane label="推送历史" name="history">
        <div class="history-toolbar">
          <el-select v-model="historyFilter.pushType" placeholder="推送类型" clearable style="width: 140px" @change="loadHistory">
            <el-option label="广播" value="broadcast" />
            <el-option label="单推" value="single" />
            <el-option label="群推" value="group" />
            <el-option label="标签推送" value="tag" />
          </el-select>
          <el-select v-model="historyFilter.sendStatus" placeholder="发送状态" clearable style="width: 140px" @change="loadHistory">
            <el-option label="待发送" :value="0" />
            <el-option label="发送成功" :value="1" />
            <el-option label="发送失败" :value="2" />
            <el-option label="部分成功" :value="3" />
          </el-select>
          <el-button @click="loadHistory"><el-icon><Refresh /></el-icon> 刷新</el-button>
        </div>

        <el-table :data="historyList" v-loading="historyLoading" border stripe>
          <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
          <el-table-column prop="pushType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="pushTypeTag(row.pushType)">{{ pushTypeLabel(row.pushType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="platform" label="平台" width="80" />
          <el-table-column prop="sendStatus" label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.sendStatus)">{{ statusLabel(row.sendStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetCount" label="目标数" width="80" align="center" />
          <el-table-column prop="successCount" label="成功" width="70" align="center" />
          <el-table-column prop="failCount" label="失败" width="70" align="center" />
          <el-table-column prop="createTime" label="时间" width="170" />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="showHistoryDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="historyFilter.page"
          v-model:page-size="historyFilter.size"
          :total="historyTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          style="margin-top: 16px; justify-content: flex-end"
          @size-change="loadHistory"
          @current-change="loadHistory"
        />
      </el-tab-pane>

      <!-- 统计监控 -->
      <el-tab-pane label="统计监控" name="stats">
        <el-row :gutter="20" style="margin-bottom: 24px">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-value">{{ stats.total || 0 }}</div>
              <div class="stat-label">累计推送</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-value" style="color: #409eff">{{ stats.todayCount || 0 }}</div>
              <div class="stat-label">今日推送</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-value" style="color: #67c23a">{{ stats.successCount || 0 }}</div>
              <div class="stat-label">成功送达</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-value" style="color: #e6a23c">{{ stats.successRate || 0 }}%</div>
              <div class="stat-label">送达率</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>推送概况</span>
              <el-button @click="loadStatistics" size="small"><el-icon><Refresh /></el-icon> 刷新</el-button>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="失败次数">
              <span style="color: #f56c6c">{{ stats.failCount || 0 }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="送达率">
              <el-progress :percentage="stats.successRate || 0" :stroke-width="16" :text-inside="true" />
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 历史详情弹窗 -->
    <el-dialog v-model="detailVisible" title="推送详情" width="600px" destroy-on-close>
      <el-descriptions :column="1" border v-if="currentDetail">
        <el-descriptions-item label="标题">{{ currentDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentDetail.content }}</el-descriptions-item>
        <el-descriptions-item label="推送类型">{{ pushTypeLabel(currentDetail.pushType) }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ currentDetail.targetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标值">
          <span style="word-break: break-all">{{ currentDetail.targetValue || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="推送平台">{{ currentDetail.platform }}</el-descriptions-item>
        <el-descriptions-item label="消息类型">{{ currentDetail.msgType === 'notification' ? '通知' : '自定义消息' }}</el-descriptions-item>
        <el-descriptions-item label="发送状态">
          <el-tag :type="statusTag(currentDetail.sendStatus)">{{ statusLabel(currentDetail.sendStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="目标数">{{ currentDetail.targetCount }}</el-descriptions-item>
        <el-descriptions-item label="成功数">{{ currentDetail.successCount }}</el-descriptions-item>
        <el-descriptions-item label="失败数">{{ currentDetail.failCount }}</el-descriptions-item>
        <el-descriptions-item label="点击数">{{ currentDetail.clickCount }}</el-descriptions-item>
        <el-descriptions-item label="极光SendID">{{ currentDetail.jpushSendId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentDetail.errorMsg">
          <span style="color: #f56c6c">{{ currentDetail.errorMsg }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ currentDetail.sendTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentDetail.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Promotion, Refresh } from '@element-plus/icons-vue'
import { jpushApi } from '@/api/setting/message'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const orgId = computed(() => authStore.userInfo?.orgId || authStore.userInfo?.org_id || '1')

const activeTab = ref('config')

// ========== 配置 ==========
const configLoading = ref(false)
const saveLoading = ref(false)
const testLoading = ref(false)
const configForm = reactive({
  appKey: '',
  masterSecret: '',
  status: 0,
  defaultTitle: '',
  channel: 'all',
  apnsEnabled: 0,
  apnsProduction: 0
})

const loadConfig = async () => {
  configLoading.value = true
  try {
    const res = await jpushApi.getConfig(orgId.value)
    if (res.data?.code === 200 && res.data?.data) {
      const d = res.data.data
      Object.assign(configForm, {
        appKey: d.appKey || '',
        masterSecret: d.masterSecret || '',
        status: d.status ?? 0,
        defaultTitle: d.defaultTitle || '',
        channel: d.channel || 'all',
        apnsEnabled: d.apnsEnabled ?? 0,
        apnsProduction: d.apnsProduction ?? 0
      })
    }
  } catch (e) {
    console.error('加载极光配置失败', e)
  } finally {
    configLoading.value = false
  }
}

const saveConfig = async () => {
  if (!configForm.appKey) return ElMessage.warning('请输入 AppKey')
  if (!configForm.masterSecret) return ElMessage.warning('请输入 Master Secret')
  saveLoading.value = true
  try {
    const res = await jpushApi.saveConfig({ ...configForm, orgId: orgId.value })
    if (res.data?.code === 200) {
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.data?.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

const testConnection = async () => {
  testLoading.value = true
  try {
    const res = await jpushApi.testConnection({ orgId: orgId.value })
    const data = res.data?.data
    if (data?.success) {
      ElMessage.success('连接测试成功！')
    } else {
      ElMessage.error(data?.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    testLoading.value = false
  }
}

// ========== 发送推送 ==========
const sendLoading = ref(false)
const pushFormRef = ref(null)
const pushForm = reactive({
  pushType: 'broadcast',
  targetValue: '',
  title: '',
  content: '',
  msgType: 'notification',
  platform: 'all'
})
const pushRules = {
  title: [{ required: true, message: '请输入推送标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入推送内容', trigger: 'blur' }]
}

const sendPush = async () => {
  if (!pushFormRef.value) return
  await pushFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (pushForm.pushType !== 'broadcast' && !pushForm.targetValue) {
      return ElMessage.warning('请输入推送目标')
    }
    try {
      await ElMessageBox.confirm(
        `确认发送${pushForm.pushType === 'broadcast' ? '广播' : '推送'}？标题：${pushForm.title}`,
        '发送确认', { type: 'warning' }
      )
    } catch { return }

    sendLoading.value = true
    try {
      const res = await jpushApi.sendPush({
        orgId: orgId.value,
        ...pushForm
      })
      const data = res.data?.data
      if (data?.success) {
        ElMessage.success('推送发送成功')
        pushForm.title = ''
        pushForm.content = ''
        pushForm.targetValue = ''
      } else {
        ElMessage.error(data?.errorMessage || '推送发送失败')
      }
    } catch (e) {
      ElMessage.error('推送发送失败')
    } finally {
      sendLoading.value = false
    }
  })
}

// ========== 推送历史 ==========
const historyLoading = ref(false)
const historyList = ref([])
const historyTotal = ref(0)
const historyFilter = reactive({ page: 1, size: 20, pushType: '', sendStatus: null })
const detailVisible = ref(false)
const currentDetail = ref(null)

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const res = await jpushApi.getHistoryList({
      orgId: orgId.value,
      page: historyFilter.page,
      size: historyFilter.size,
      pushType: historyFilter.pushType || undefined,
      sendStatus: historyFilter.sendStatus
    })
    if (res.data?.code === 200) {
      historyList.value = res.data.data?.list || []
      historyTotal.value = res.data.data?.total || 0
    }
  } catch (e) {
    console.error('加载推送历史失败', e)
  } finally {
    historyLoading.value = false
  }
}

const showHistoryDetail = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const pushTypeLabel = (t) => ({ broadcast: '广播', single: '单推', group: '群推', tag: '标签' }[t] || t)
const pushTypeTag = (t) => ({ broadcast: '', single: 'success', group: 'warning', tag: 'info' }[t] || '')
const statusLabel = (s) => ({ 0: '待发送', 1: '成功', 2: '失败', 3: '部分成功' }[s] || '未知')
const statusTag = (s) => ({ 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }[s] || '')

// ========== 统计 ==========
const stats = ref({})
const loadStatistics = async () => {
  try {
    const res = await jpushApi.getStatistics(orgId.value)
    if (res.data?.code === 200) {
      stats.value = res.data.data || {}
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

// ========== 生命周期 ==========
onMounted(() => {
  loadConfig()
  loadHistory()
  loadStatistics()
})
</script>

<style scoped>
.jpush-manage {
  padding: 0;
}
.history-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.stat-card {
  text-align: center;
  cursor: default;
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}
</style>
