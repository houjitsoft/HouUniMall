<template>
  <div class="recon-center-container">
    <!-- 顶部标题 + 步骤条 -->
    <div class="page-header">
      <div class="title-wrap">
        <h2 class="page-title">
          <el-icon class="title-icon"><Finished /></el-icon>
          对账中心
        </h2>
        <div class="page-subtitle">
          按真实业务流程：选渠道日期 → 拉取账单 → 触发核对 → 差异处理 → 对账报告
        </div>
      </div>
      <el-tag v-if="stepCtx.channel" type="success" effect="plain">
        当前渠道：{{ channelName(stepCtx.channel) }} · {{ stepCtx.billDate || '-' }}
      </el-tag>
    </div>

    <el-card shadow="never" class="step-card">
      <el-steps :active="activeStep" align-center finish-status="success" class="steps-bar">
        <el-step title="选择渠道与日期" description="对账范围" />
        <el-step title="拉取渠道账单" description="T+1 账单文件" />
        <el-step title="触发对账核对" description="差异识别" />
        <el-step title="差异处理" description="补单/冲正/关闭" />
        <el-step title="对账报告" description="月度报表" />
      </el-steps>

      <!-- ============ 第一步：选择渠道与日期 ============ -->
      <div v-if="activeStep === 0" class="step-panel">
        <div class="panel-title">① 选择对账渠道与日期</div>
        <div class="panel-desc">选择需对账的支付渠道与账单日期（T+1 日 11 点后可拉取 D 日账单）。</div>

        <el-form :model="stepCtx" label-width="110px" class="integration-form">
          <el-form-item label="对账渠道">
            <el-radio-group v-model="stepCtx.channel">
              <el-radio-button v-for="c in channels" :key="c.code" :value="c.code">
                {{ c.name }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="账单日期">
            <el-date-picker v-model="stepCtx.billDate" type="date" value-format="YYYY-MM-DD"
              placeholder="选择对账日期" :disabled-date="disableFuture" style="width: 240px;" />
          </el-form-item>
          <el-form-item label="账单类型">
            <el-select v-model="stepCtx.billType" style="width: 200px;">
              <el-option label="交易账单" value="TRADE" />
              <el-option label="退款账单" value="REFUND" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="panel-actions">
          <el-button type="primary" :disabled="!stepCtx.channel || !stepCtx.billDate" @click="nextStep">
            下一步：拉取渠道账单
          </el-button>
        </div>
      </div>

      <!-- ============ 第二步：拉取渠道账单 ============ -->
      <div v-if="activeStep === 1" class="step-panel">
        <div class="panel-title">② 拉取渠道账单</div>
        <div class="panel-desc">向 {{ channelName(stepCtx.channel) }} 拉取 {{ stepCtx.billDate }} 交易账单，落库为对账文件。</div>

        <el-alert type="info" :closable="false" show-icon class="pull-tip"
          title="说明：当前为自研对账（模拟模式）——基于本地交易生成渠道账单；接入真实渠道 SDK 后此步会真实拉取。" />

        <div class="pull-box">
          <el-button type="primary" size="large" :loading="pulling" @click="doPullBill">
            <el-icon><Download /></el-icon>拉取 {{ channelName(stepCtx.channel) }} 账单
          </el-button>
          <span v-if="pullResult" class="pull-result">
            已拉取：{{ channelName(pullResult.channelCode) }} {{ formatDate(pullResult.billDate) }}
            （{{ pullResult.billType }} · 状态 {{ pullResult.status === 1 ? '已拉取' : '待处理' }}）
          </span>
        </div>

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :disabled="!pullResult" @click="nextStep">
            下一步：触发对账核对
          </el-button>
        </div>
      </div>

      <!-- ============ 第三步：触发对账核对 ============ -->
      <div v-if="activeStep === 2" class="step-panel">
        <div class="panel-title">③ 触发对账核对</div>
        <div class="panel-desc">比对本地订单与渠道账单，自动识别金额/状态/单边差异。</div>

        <div class="pull-box">
          <el-button type="primary" size="large" :loading="checking" @click="doDiffCheck">
            <el-icon><Histogram /></el-icon>开始对账核对
          </el-button>
          <span v-if="checkResult !== null" class="pull-result">
            核对完成，发现差异 <b class="diff-num">{{ checkResult }}</b> 笔
          </span>
        </div>

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :disabled="checkResult === null || checkResult === 0" @click="nextStep">
            下一步：差异处理
          </el-button>
          <el-button v-if="checkResult === 0" type="info" @click="activeStep = 4">无差异，直接看报告</el-button>
        </div>
      </div>

      <!-- ============ 第四步：差异处理 ============ -->
      <div v-if="activeStep === 3" class="step-panel">
        <div class="panel-title">④ 差异处理</div>
        <div class="panel-desc">对差异记录逐条处理：补单（以渠道为准）、冲正（以本地为准）或人工关闭。</div>

        <el-table :data="diffList" v-loading="diffLoading" stripe style="width: 100%;">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="localOrderNo" label="本地订单号" min-width="150">
            <template #default="{ row }">{{ row.localOrderNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="channelOrderNo" label="渠道订单号" min-width="160">
            <template #default="{ row }">{{ row.channelOrderNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="diffType" label="差异类型" width="120">
            <template #default="{ row }">
              <el-tag size="small" :type="diffTypeTag[row.diffType] || 'info'">
                {{ diffTypeText[row.diffType] || row.diffType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="差异金额" width="120" align="right">
            <template #default="{ row }">¥ {{ yuan(row.diffAmount) }}</template>
          </el-table-column>
          <el-table-column label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.handleStatus === 0 ? 'warning' : 'success'">
                {{ row.handleStatus === 0 ? '待处理' : '已处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" size="small" :disabled="row.handleStatus !== 0" @click="handleDiff(row, 1)">补单</el-button>
              <el-button link type="primary" size="small" :disabled="row.handleStatus !== 0" @click="handleDiff(row, 2)">冲正</el-button>
              <el-button link type="info" size="small" :disabled="row.handleStatus !== 0" @click="handleDiff(row, 3)">关闭</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!diffLoading && !diffList.length" description="暂无差异记录" :image-size="80" />

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步：对账报告</el-button>
        </div>
      </div>

      <!-- ============ 第五步：对账报告 ============ -->
      <div v-if="activeStep === 4" class="step-panel">
        <div class="panel-title">⑤ 对账报告</div>
        <div class="panel-desc">生成并查看月度对账报表，统计交易总数/金额/成功/差异。</div>

        <div class="pull-box">
          <el-form :inline="true">
            <el-form-item label="报表月份">
              <el-date-picker v-model="reportMonth" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width: 160px;" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="reportGenerating" @click="doReportGen">
                <el-icon><Document /></el-icon>生成月报
              </el-button>
              <el-button @click="loadReports"><el-icon><Refresh /></el-icon>刷新</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table :data="reportList" v-loading="reportLoading" stripe style="width: 100%;">
          <el-table-column prop="billMonth" label="报表月份" width="110" />
          <el-table-column prop="channelCode" label="渠道" width="110">
            <template #default="{ row }">{{ channelName(row.channelCode) }}</template>
          </el-table-column>
          <el-table-column prop="totalCount" label="交易总数" width="100" align="right" />
          <el-table-column label="交易总额" width="130" align="right">
            <template #default="{ row }">¥ {{ yuan(row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column prop="successCount" label="成功笔数" width="100" align="right" />
          <el-table-column prop="diffCount" label="差异笔数" width="100" align="right">
            <template #default="{ row }">
              <span :class="{ 'diff-num': row.diffCount > 0 }">{{ row.diffCount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="差异金额" width="120" align="right">
            <template #default="{ row }">
              <span :class="{ 'diff-num': row.diffAmount > 0 }">¥ {{ yuan(row.diffAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="genTime" label="生成时间" min-width="160">
            <template #default="{ row }">{{ row.genTime || '-' }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!reportLoading && !reportList.length" description="暂无对账报告" :image-size="80" />

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="activeStep = 0">重新开始对账</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 对账中心（按真实业务流程的分步向导）
 *
 * ① 选择对账渠道与日期 → ② 拉取渠道账单 → ③ 触发对账核对 → ④ 差异处理 → ⑤ 对账报告
 *
 * 数据源（自研对账，真实接口）：
 *  - /web/pay/recon/pullChannelBill  拉取渠道账单
 *  - /web/pay/recon/diffCheck        对账核对
 *  - /web/pay/recon/diffList         差异列表
 *  - /web/pay/recon/diffHandle       差异处理
 *  - /web/pay/recon/reportGen        月报生成
 *  - /web/pay/recon/reportList       报表分页
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Finished, Download, Histogram, Refresh, Document } from '@element-plus/icons-vue'
import { reconApi, reconChannels } from '@/api/payment/paymentBizW4'

const channels = reconChannels
const activeStep = ref(0)
const nextStep = () => { activeStep.value = Math.min(4, activeStep.value + 1) }
const prevStep = () => { activeStep.value = Math.max(0, activeStep.value - 1) }

const stepCtx = reactive({ channel: 'WX', billDate: '', billType: 'TRADE' })

const channelName = (code) => {
  const c = channels.find((x) => x.code === code)
  return c ? c.name : code
}
const formatDate = (d) => {
  if (!d) return '-'
  if (typeof d === 'string' && d.length === 10) return d
  const date = new Date(d)
  return isNaN(date) ? String(d) : date.toISOString().slice(0, 10)
}
const disableFuture = (date) => date.getTime() > Date.now()

// ================= ② 拉取账单 =================
const pulling = ref(false)
const pullResult = ref(null)
const doPullBill = async () => {
  pulling.value = true
  pullResult.value = null
  try {
    const res = await reconApi.pullChannelBill({ channel: stepCtx.channel, billDate: stepCtx.billDate, billType: stepCtx.billType })
    if (res && res.code === 200) {
      pullResult.value = res.data
      ElMessage.success(`已拉取 ${channelName(stepCtx.channel)} ${stepCtx.billDate} 账单`)
    } else {
      ElMessage.error(res?.message || '拉取账单失败')
    }
  } catch (e) {
    ElMessage.error('拉取账单失败: ' + (e.message || '网络错误'))
  } finally {
    pulling.value = false
  }
}

// ================= ③ 触发核对 =================
const checking = ref(false)
const checkResult = ref(null)
const doDiffCheck = async () => {
  checking.value = true
  checkResult.value = null
  try {
    const res = await reconApi.diffCheck({ billDate: stepCtx.billDate, channel: stepCtx.channel })
    if (res && res.code === 200) {
      checkResult.value = res.data || 0
      if (checkResult.value > 0) {
        ElMessage.warning(`对账完成，发现 ${checkResult.value} 笔差异`)
      } else {
        ElMessage.success('对账完成，无差异')
      }
    } else {
      ElMessage.error(res?.message || '对账核对失败')
    }
  } catch (e) {
    ElMessage.error('对账核对失败: ' + (e.message || '网络错误'))
  } finally {
    checking.value = false
  }
}

// ================= ④ 差异处理 =================
const diffLoading = ref(false)
const diffList = ref([])
const diffTypeText = { AMOUNT_DIFF: '金额差异', STATUS_DIFF: '状态差异', LOCAL_ONLY: '本地多出', CHANNEL_ONLY: '渠道多出' }
const diffTypeTag = { AMOUNT_DIFF: 'danger', STATUS_DIFF: 'warning', LOCAL_ONLY: 'primary', CHANNEL_ONLY: 'info' }

const loadDiffs = async () => {
  diffLoading.value = true
  try {
    const res = await reconApi.diffList({ page: 1, size: 50, channel: stepCtx.channel })
    if (res && res.code === 200) {
      diffList.value = res.data?.records || res.data?.list || []
    } else {
      diffList.value = []
    }
  } catch (e) {
    console.error('查询差异失败:', e)
    diffList.value = []
  } finally {
    diffLoading.value = false
  }
}

const handleDiff = async (row, status) => {
  const map = { 1: '补单（以渠道为准）', 2: '冲正（以本地为准）', 3: '人工关闭' }
  try {
    const res = await reconApi.diffHandle({ diffId: row.id, handleStatus: status, handleRemark: map[status] })
    if (res && res.code === 200) {
      ElMessage.success(`差异 #${row.id} 已${map[status]}`)
      loadDiffs()
    } else {
      ElMessage.error(res?.message || '处理失败')
    }
  } catch (e) {
    ElMessage.error('处理失败: ' + (e.message || '网络错误'))
  }
}

// ================= ⑤ 对账报告 =================
const reportMonth = ref('')
const reportGenerating = ref(false)
const reportLoading = ref(false)
const reportList = ref([])

const doReportGen = async () => {
  if (!reportMonth.value) { ElMessage.warning('请选择报表月份'); return }
  reportGenerating.value = true
  try {
    const res = await reconApi.reportGen({ mchNo: stepCtx.mchNo, channel: stepCtx.channel, reportMonth: reportMonth.value })
    if (res && res.code === 200) {
      ElMessage.success('月度对账报表已生成')
      loadReports()
    } else {
      ElMessage.error(res?.message || '生成报表失败')
    }
  } catch (e) {
    ElMessage.error('生成报表失败: ' + (e.message || '网络错误'))
  } finally {
    reportGenerating.value = false
  }
}

const loadReports = async () => {
  reportLoading.value = true
  try {
    const res = await reconApi.reportList({ page: 1, size: 50, channel: stepCtx.channel, billMonth: reportMonth.value || undefined })
    if (res && res.code === 200) {
      reportList.value = res.data?.records || res.data?.list || []
    } else {
      reportList.value = []
    }
  } catch (e) {
    console.error('查询报表失败:', e)
    reportList.value = []
  } finally {
    reportLoading.value = false
  }
}

const yuan = (fen) => {
  if (fen === null || fen === undefined) return '0.00'
  return (Number(fen) / 100).toFixed(2)
}

// 初始化：默认昨日日期
onMounted(() => {
  const d = new Date()
  d.setDate(d.getDate() - 1)
  stepCtx.billDate = d.toISOString().slice(0, 10)
  reportMonth.value = d.toISOString().slice(0, 7)
  loadReports()
})
</script>

<style scoped>
.recon-center-container { padding: 16px; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.title-wrap .page-title { margin: 0; font-size: 20px; font-weight: 600; display: flex; align-items: center; color: #303133; }
.title-icon { margin-right: 8px; color: #67c23a; font-size: 22px; }
.page-subtitle { margin-top: 4px; font-size: 13px; color: #909399; }

.step-card { border-radius: 8px; }
.step-card :deep(.el-card__body) { padding: 18px 20px; }
.steps-bar { margin-bottom: 20px; padding: 8px 0; }

.step-panel { min-height: 300px; }
.panel-title { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 6px; }
.panel-desc { font-size: 13px; color: #909399; margin-bottom: 14px; }

.integration-form { max-width: 720px; }
.pull-tip { margin-bottom: 14px; }
.pull-box { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; flex-wrap: wrap; }
.pull-result { font-size: 13px; color: #67c23a; }
.diff-num { color: #f56c6c; font-weight: 600; }

.panel-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 18px; padding-top: 14px; border-top: 1px solid #f0f2f5; }
</style>
