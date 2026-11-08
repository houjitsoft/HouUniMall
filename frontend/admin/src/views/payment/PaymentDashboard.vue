<template>
  <div class="payment-dashboard">
    <!-- 顶部欢迎条 -->
    <div class="page-header">
      <div class="title-wrap">
        <h2 class="page-title">
          <el-icon class="title-icon"><CreditCard /></el-icon>
          支付中心工作台
        </h2>
        <div class="page-subtitle">
          自有支付平台 · 统一中台 · {{ todayText }}
        </div>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="refreshOverview">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 概览指标区 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card stat-amount" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日交易金额</div>
              <div class="stat-value">{{ formatValue(stats.todayAmount, 'money') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card stat-count" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日交易笔数</div>
              <div class="stat-value">{{ formatValue(stats.todayCount, 'number') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card stat-refund" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><RefreshLeft /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日退款金额</div>
              <div class="stat-value">{{ formatValue(stats.todayRefund, 'money') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card stat-rate" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">支付成功率</div>
              <div class="stat-value">{{ formatValue(stats.successRate, 'percent') }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 功能菜单卡片区 -->
    <div class="modules-wrap">
      <el-card
        v-for="group in moduleGroups"
        :key="group.key"
        class="module-group"
        shadow="never"
      >
        <template #header>
          <div class="group-header">
            <span class="group-icon" :style="{ background: group.color }">
              <el-icon><component :is="group.icon" /></el-icon>
            </span>
            <span class="group-title">{{ group.title }}</span>
            <span class="group-desc">{{ group.desc }}</span>
          </div>
        </template>
        <div class="items-grid">
          <div
            v-for="item in group.items"
            :key="item.label"
            class="item-card"
            :class="{ 'is-coming': item.comingSoon }"
            @click="handleClick(item)"
          >
            <div class="item-icon">
              <el-icon><component :is="item.icon || group.icon" /></el-icon>
            </div>
            <div class="item-body">
              <div class="item-label">
                {{ item.label }}
                <el-tag
                  v-if="item.comingSoon"
                  size="small"
                  type="info"
                  effect="plain"
                  class="coming-tag"
                >规划中</el-tag>
              </div>
              <div class="item-desc">{{ item.desc }}</div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
/**
 * 支付中心工作台（Dashboard）
 * - 顶部概览指标：调用 /web/payment/dashboard/overview（未就绪时占位）
 * - 下方分组卡片：展示交易/退款/会员/提现/分账/商户/对账/账户/支付配置
 *   已落地功能直接 router.push，规划中功能仅弹出提示
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CreditCard,
  Refresh,
  Money,
  Tickets,
  RefreshLeft,
  TrendCharts,
  Document,
  User,
  Wallet,
  Share,
  OfficeBuilding,
  Finished,
  Setting,
  List,
  Search,
  CircleClose,
  Plus,
  Download,
  Upload,
  Histogram,
  Coin,
  Warning,
  Notebook
} from '@element-plus/icons-vue'
import { getPaymentOverview } from '@/api/payment/dashboard'

const router = useRouter()
const loading = ref(false)

/**
 * 概览指标数据。null 表示占位 "--"
 */
const stats = reactive({
  todayAmount: null,
  todayCount: null,
  todayRefund: null,
  successRate: null
})

const todayText = computed(() => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
})

/**
 * 格式化指标显示
 * @param {number|null} val 原始值
 * @param {'money'|'number'|'percent'} type 类型
 * @returns {string}
 */
function formatValue(val, type) {
  if (val === null || val === undefined || isNaN(val)) return '--'
  if (type === 'money') {
    return '¥ ' + Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
  if (type === 'number') {
    return Number(val).toLocaleString('zh-CN')
  }
  if (type === 'percent') {
    return Number(val).toFixed(2) + ' %'
  }
  return String(val)
}

/**
 * 分组菜单配置：按用户需求覆盖 9 组
 * 每个子项均已完成路由注册，占位页使用通用 PaymentStubPage 骨架
 */
const moduleGroups = [
  {
    key: 'transaction',
    title: '交易管理',
    desc: '支付下单、订单查询与交易记录',
    icon: CreditCard,
    color: '#409EFF',
    items: [
      { label: '统一下单', icon: Plus, desc: '对接多渠道统一下单能力', path: '/payment/trade/unified-order' },
      { label: '订单查询', icon: Search, desc: '查询支付订单列表', path: '/payment/orders' },
      { label: '关闭订单', icon: CircleClose, desc: '主动关闭未支付的订单', path: '/payment/trade/close-order' },
      { label: '支付管理', icon: CreditCard, desc: '支付综合面板与统计', path: '/payment/manage' },
      { label: '支付统计', icon: Histogram, desc: '交易趋势/渠道分布/商户排行', path: '/payment/statistics' },
      { label: '交易记录', icon: Document, desc: '查看完整交易流水记录', path: '/payment/transaction' }
    ]
  },
  {
    key: 'refund',
    title: '退款管理',
    desc: '退款申请、查询与记录',
    icon: RefreshLeft,
    color: '#F56C6C',
    items: [
      { label: '退款申请', icon: Plus, desc: '发起退款申请单', path: '/payment/refund/apply' },
      { label: '退款查询', icon: Search, desc: '查询退款单状态与明细', path: '/payment/refund' },
      { label: '退款记录', icon: List, desc: '退款历史记录', path: '/payment/refund/log' }
    ]
  },
  {
    key: 'member',
    title: '会员管理',
    desc: '企业/个人会员开户与资料',
    icon: User,
    color: '#67C23A',
    items: [
      { label: '企业会员开户', icon: OfficeBuilding, desc: '企业商户会员开户入驻', path: '/payment/member/enterprise' },
      { label: '个人会员开户', icon: User, desc: '个人会员开户入驻', path: '/payment/member/personal' },
      { label: '会员信息查询', icon: Search, desc: '查询会员基础信息与状态', path: '/payment/member/query' },
      { label: '会员资料补录', icon: Notebook, desc: '补录完善会员资料', path: '/payment/member/supplement' }
    ]
  },
  {
    key: 'withdraw',
    title: '提现管理',
    desc: '账户提现申请与记录',
    icon: Wallet,
    color: '#E6A23C',
    items: [
      { label: '提现申请', icon: Plus, desc: '发起账户余额提现', path: '/payment/withdraw' },
      { label: '提现记录', icon: List, desc: '提现历史与审核状态', path: '/payment/withdraw/log' }
    ]
  },
  {
    key: 'split',
    title: '分账管理',
    desc: '分账中心：平台→角色→支付→发起→查询/撤销',
    icon: Share,
    color: '#909399',
    items: [
      { label: '分账中心', icon: Share, desc: '选平台·设角色·整合支付·发起分账一站式流程', path: '/payment/split/center' },
      { label: '批量分账', icon: Upload, desc: '批量提交分账请求', path: '/payment/split/batch' },
      { label: '分账查询', icon: Search, desc: '查询分账明细与结果', path: '/payment/split/query' },
      { label: '分账撤销', icon: CircleClose, desc: '撤销已发起的分账', path: '/payment/split/cancel' }
    ]
  },
  {
    key: 'merchant',
    title: '商户管理',
    desc: '商户进件与商户档案',
    icon: OfficeBuilding,
    color: '#409EFF',
    items: [
      { label: '商户进件', icon: Plus, desc: '提交商户入驻申请', path: '/payment/merchant/apply' },
      { label: '商户列表', icon: List, desc: '查看已入驻商户', path: '/payment/merchant/list' }
    ]
  },
  {
    key: 'reconciliation',
    title: '对账管理',
    desc: '对账中心：渠道账单→核对→差异→报告',
    icon: Finished,
    color: '#67C23A',
    items: [
      { label: '对账中心', icon: Histogram, desc: '选渠道·拉账单·核对·差异·月报一站式流程', path: '/payment/reconciliation/center' },
      { label: '对账文件下载', icon: Download, desc: '下载渠道对账单', path: '/payment/reconciliation/download' },
      { label: '账单导入', icon: Upload, desc: '导入外部账单进行对账', path: '/payment/reconciliation/import' },
      { label: '对账记录', icon: Histogram, desc: '查看对账结果', path: '/payment/reconciliation' }
    ]
  },
  {
    key: 'account',
    title: '账户管理',
    desc: '账户余额、明细与充值',
    icon: Coin,
    color: '#E6A23C',
    items: [
      { label: '余额查询', icon: Wallet, desc: '查看账户可用余额', path: '/payment/account' },
      { label: '账户明细', icon: Document, desc: '查看账户变动明细', path: '/payment/account/detail' },
      { label: '充值管理', icon: Plus, desc: '账户充值与记录', path: '/payment/recharge' }
    ]
  },
  {
    key: 'config',
    title: '支付配置',
    desc: '渠道配置与风控策略',
    icon: Setting,
    color: '#909399',
    items: [
      { label: '支付配置', icon: Setting, desc: '配置支付渠道与参数', path: '/payment/config' },
      { label: '风控中心', icon: Warning, desc: '规则·评估·日志·审核·黑白名单流程', path: '/payment/risk/center' },
      { label: '风控管理', icon: Warning, desc: '风控规则与风险事件', path: '/payment/risk' }
    ]
  }
]

/**
 * 卡片点击处理
 * @param {{label:string, path?:string, comingSoon?:boolean}} item
 */
function handleClick(item) {
  if (item.comingSoon || !item.path) {
    ElMessage.info(`「${item.label}」功能规划中，敬请期待`)
    return
  }
  router.push(item.path).catch((err) => {
    if (err && err.name !== 'NavigationDuplicated') {
      console.error('[PaymentDashboard] 路由跳转失败:', err)
    }
  })
}

/**
 * 拉取概览指标数据
 */
async function refreshOverview() {
  loading.value = true
  try {
    const res = await getPaymentOverview()
    // 本项目成功码常见为 1 / 200 / 0
    if (res && res.code != null) {
      const c = Number(res.code)
      if (c !== 200 && c !== 0 && c !== 1) {
        ElMessage.error(res.message || '工作台概览加载失败')
        return
      }
    }
    const data = res && (res.data != null ? res.data : res)
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      stats.todayAmount = data.todayAmount ?? 0
      stats.todayCount = data.todayCount ?? 0
      stats.todayRefund = data.todayRefund ?? 0
      stats.successRate = data.successRate ?? 0
    }
  } catch (error) {
    console.error('[PaymentDashboard] 概览接口失败:', error)
    ElMessage.error(error?.message || '工作台概览加载失败，请确认已登录且 payment 服务可用')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshOverview()
})
</script>

<style scoped>
.payment-dashboard {
  padding: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title-wrap .page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  color: #303133;
}

.title-wrap .title-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 22px;
}

.page-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

/* 指标卡片 */
.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  font-size: 26px;
  color: #fff;
}

.stat-amount .stat-icon {
  background: linear-gradient(135deg, #409eff, #5ab1ff);
}

.stat-count .stat-icon {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-refund .stat-icon {
  background: linear-gradient(135deg, #f56c6c, #f89898);
}

.stat-rate .stat-icon {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

/* 功能分组 */
.modules-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-group {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.module-group :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f2f6fc;
}

.module-group :deep(.el-card__body) {
  padding: 12px 16px 16px;
}

.group-header {
  display: flex;
  align-items: center;
}

.group-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  color: #fff;
  font-size: 16px;
}

.group-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.group-desc {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
}

.item-card {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  background: #fafbfc;
  transition: all 0.2s ease;
}

.item-card:hover {
  border-color: #409eff;
  background: #ecf5ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.12);
}

.item-card.is-coming {
  background: #f5f7fa;
}

.item-card.is-coming:hover {
  border-color: #c0c4cc;
  background: #f0f2f5;
  box-shadow: none;
}

.item-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: #fff;
  color: #409eff;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  border: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.item-card.is-coming .item-icon {
  color: #909399;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-label {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 2px;
  display: flex;
  align-items: center;
}

.coming-tag {
  margin-left: 6px;
  transform: scale(0.85);
  transform-origin: left center;
}

.item-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-actions .el-button + .el-button {
  margin-left: 8px;
}
</style>
