<template>
  <div class="checkout-page">
    <header class="page-header">
      <div>
        <h1>购买插件</h1>
        <p>下单并完成支付后，系统会<b>自动签发授权、换票并安装</b>，本页展示完整进度</p>
      </div>
      <div class="header-actions">
        <el-button @click="goOrders">我的订单</el-button>
        <el-button @click="goLicenses">授权管理</el-button>
      </div>
    </header>

    <el-alert type="info" :closable="false" class="scope-tip">
      <template #title>
        当前站点实例：<strong>{{ siteInstanceId || '（未能识别域名）' }}</strong>
        授权将绑定到该实例，购买完成后在「授权管理」即可看到。
      </template>
    </el-alert>

    <!-- 第一步：确认购买内容 -->
    <el-card v-if="stage === 'form'" shadow="never">
      <template #header>确认购买内容</template>
      <el-form label-width="110px">
        <el-form-item label="插件">
          <el-select
            v-if="!lockedPluginCode"
            v-model="form.pluginCode"
            filterable
            placeholder="请选择要购买的插件"
            style="width: 100%"
            @change="onPluginChange"
          >
            <el-option
              v-for="item in purchasable"
              :key="item.code"
              :label="`${item.name}（${item.code}）`"
              :value="item.code"
            />
          </el-select>
          <span v-else class="locked-plugin">{{ pluginName }}（{{ form.pluginCode }}）</span>
        </el-form-item>

        <el-form-item v-if="plans.length" label="价格套餐">
          <el-select v-model="form.planCode" placeholder="请选择套餐" style="width: 100%">
            <el-option
              v-for="plan in plans"
              :key="plan.code"
              :label="`${plan.name || plan.code}｜${planPriceText(plan)}${planTermText(plan)}`"
              :value="plan.code"
            />
          </el-select>
          <div class="plan-tip">
            套餐决定应付金额与官方维护期。后端只认下单时选中的套餐，未选中的套餐价格不会被采用。
          </div>
        </el-form-item>

        <el-form-item label="授权类型">
          <span class="auth-type">付费（永久使用 + 首次购买含 12 个月官方维护）</span>
        </el-form-item>

        <el-form-item v-if="form.licenseType === 'PAID'" label="授权权益">
          <span>永久使用已交付版本，首次购买含 12 个月官方维护。维护到期后当前版本继续运行；续费恢复升级、安全补丁、技术支持与官方构建服务。</span>
        </el-form-item>

        <el-form-item label="应付金额">
          <span class="amount">{{ amountText }}</span>
        </el-form-item>

        <el-form-item v-if="payable" label="支付方式">
          <el-radio-group v-model="form.payMethod">
            <el-radio value="WXPAY">微信支付</el-radio>
            <el-radio value="ALIPAY">支付宝</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="购买协议" required>
          <AgreementChecklist ref="purchaseAgreementRef" scene="plugin_purchase" empty-message="请阅读并勾选同意《商业插件购买与维护协议》后再下单" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" :disabled="!canSubmit" @click="submitPurchase">
            {{ !canSubmit ? '暂不可购买' : payable ? '下单并支付' : '免费开通' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 第二步：支付 -->
    <el-card v-else-if="stage === 'pay'" shadow="never">
      <template #header>请完成支付</template>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="插件">{{ pluginName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ Number(order.amount ?? 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付截止">{{ order.expireTime || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="payUrl" class="pay-block">
        <p class="pay-tip">请在新窗口打开支付链接完成付款：</p>
        <el-button type="primary" @click="openPayUrl">打开支付链接</el-button>
        <p class="pay-url">{{ payUrl }}</p>
      </div>
      <el-empty v-else description="收银台未返回支付链接，请查看下方原始支付参数" :image-size="60" />

      <div class="polling">
        <template v-if="sandboxPayment">
          <el-button type="success" :loading="sandboxConfirming" @click="confirmSandboxPayment('SUCCESS')">
            模拟支付成功
          </el-button>
          <el-button type="warning" plain :loading="sandboxConfirming" @click="confirmSandboxPayment('FAILED')">
            模拟支付失败
          </el-button>
        </template>
        <el-button :loading="polling" @click="pollNow">我已支付，查询状态</el-button>
        <el-button link @click="cancelOrder">取消订单</el-button>
        <span class="polling-tip">{{ pollingTip }}</span>
      </div>
    </el-card>

    <!-- 第三步：一页完成（支付成功 → 换票 → 安装 → 健康检查） -->
    <el-card v-else shadow="never">
      <template #header>购买与安装</template>
      <PluginPurchasePipeline
        :phase="pipeline.phase"
        :percent="pipeline.percent"
        :tip="pipeline.tip"
        :ticket-detail="pipeline.ticketDetail"
        :install-detail="pipeline.installDetail"
        :health-ok="pipeline.healthOk"
        :health-detail="pipeline.healthDetail"
        :license-key="order.licenseKey"
        :instance-id="order.instanceId || siteInstanceId"
        :running="pipeline.running"
        @retry="runPostPurchasePipeline"
        @licenses="goLicenses"
        @manage="goAddons"
      >
        <template v-if="isStandaloneService(form.pluginCode)" #extra>
          <el-button @click="downloadPurchased('source')">下载源码包</el-button>
          <el-button @click="downloadPurchased('runtime')">下载运行包</el-button>
        </template>
      </PluginPurchasePipeline>
    </el-card>

    <el-collapse v-if="rawPayData" class="raw-block">
      <el-collapse-item title="原始支付参数（排障用）" name="raw">
        <pre>{{ rawPayData }}</pre>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup>
import { isStandaloneService, installServiceAndWait, downloadServiceArtifact, getServiceDelivery } from '@/api/plugin-center/service'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelPluginOrder,
  confirmSandboxPluginPayment,
  createPluginOrder,
  getMarketDetail,
  getMarketList,
  payPluginOrder,
  syncPluginOrder
} from '@/api/plugin-center/pluginMarket'
import { installAndWait, precheckPlugin } from '@/api/plugin-center/pluginUpdate'
import { getSiteInstanceId } from '@/utils/site'
import { describeInstallProgress } from '@/utils/pluginInstallProgress'
import { formatInstallFailure, explainArtifactError } from '@/utils/pluginInstallFeedback'
import { refreshPluginApplications } from '@/utils/pluginApplicationRefresh'
import AgreementChecklist from '@/components/AgreementChecklist.vue'
import PluginPurchasePipeline from './components/PluginPurchasePipeline.vue'

const route = useRoute()
const router = useRouter()

const siteInstanceId = getSiteInstanceId()

/** form=确认购买 pay=待支付 done=完成 */
const stage = ref('form')
const submitting = ref(false)
const polling = ref(false)
const sandboxConfirming = ref(false)
const pollingTip = ref('')

const marketList = ref([])
const pluginDetail = ref(null)
const order = ref({})

const form = ref({ pluginCode: '', licenseType: 'PAID', payMethod: 'WXPAY', planCode: '' })
const purchaseAgreementRef = ref(null)

const pipeline = reactive({
  phase: 'paid',
  percent: 10,
  tip: '',
  ticketDetail: '',
  installDetail: '',
  healthOk: null,
  healthDetail: '',
  running: false,
})

const lockedPluginCode = computed(() => Boolean(route.query.code))
const pluginName = computed(() => pluginDetail.value?.plugin?.name || form.value.pluginCode || '—')

const purchasable = computed(() =>
  (marketList.value || []).filter((item) => String(item?.licenseType || '').toUpperCase() === 'PAID')
)

/**
 * 该插件已启用的价格套餐（后端 /market/detail 返回）。
 *
 * 以前前端只认商品表 price_cents，也从不传 planCode；插件一旦按套餐售卖，
 * 下单会被后端以「必须指定 planCode」拒绝，而页面根本不知道有哪些套餐可选。
 */
const plans = computed(() => (Array.isArray(pluginDetail.value?.plans) ? pluginDetail.value.plans : []))
const selectedPlan = computed(() => plans.value.find((plan) => plan.code === form.value.planCode) || null)

const planPriceText = (plan) => {
  const value = Number(plan?.price ?? 0)
  if (!Number.isFinite(value) || value <= 0) return '免费'
  return `¥${value.toFixed(2)}`
}

const planTermText = (plan) => {
  const days = Number(plan?.validDays ?? 0)
  if (!Number.isFinite(days) || days <= 0) return '（按官方维护政策）'
  return `（${days} 天）`
}

const payable = computed(() => {
  if (selectedPlan.value) return Number(selectedPlan.value.price ?? 0) > 0
  if (plans.value.length) return false
  return Number(pluginDetail.value?.plugin?.priceCents ?? 0) > 0
})
const canSubmit = computed(() => payable.value)

const amountText = computed(() => {
  if (selectedPlan.value) return planPriceText(selectedPlan.value)
  if (plans.value.length && !selectedPlan.value) return '请先选择套餐'
  const cents = pluginDetail.value?.plugin?.priceCents ?? 0
  if (Number(cents) <= 0) return '未定价'
  return `¥${(Number(cents) / 100).toFixed(2)}`
})

const payUrl = computed(() => {
  const data = order.value?.payData
  if (!data) return ''
  for (const key of ['codeUrl', 'qrCode', 'qr_code', 'payUrl', 'url', 'mwebUrl']) {
    if (typeof data[key] === 'string' && data[key]) return data[key]
  }
  return ''
})

const sandboxPayment = computed(() => String(order.value?.payData?.mode || '').toUpperCase() === 'MOCK')

const rawPayData = computed(() => {
  const data = order.value?.payData
  return data ? JSON.stringify(data, null, 2) : ''
})

let timer = null

const goOrders = () => router.push('/platform/plugins/orders')
const goLicenses = () => router.push('/platform/plugins/licenses')
const goAddons = () => router.push('/platform/plugins')

const targetVersion = () =>
  order.value.version || pluginDetail.value?.plugin?.latestVersion || pluginDetail.value?.plugin?.version || ''

const loadMarket = async () => {
  try {
    const data = await getMarketList({ instanceId: siteInstanceId })
    marketList.value = data?.list ?? []
  } catch {
    marketList.value = []
  }
}

const loadPlugin = async (code) => {
  pluginDetail.value = null
  form.value.planCode = ''
  if (!code) return
  try {
    const data = await getMarketDetail(code, siteInstanceId)
    pluginDetail.value = data
    if (data?.plugin?.licenseType) form.value.licenseType = data.plugin.licenseType
    // 套餐单一律默认选第一个：套餐是必填项，留空只会让用户在点按钮时才碰壁
    const available = Array.isArray(data?.plans) ? data.plans : []
    if (available.length && !form.value.planCode) {
      form.value.planCode = available[0].code
    }
  } catch (error) {
    ElMessage.error(error?.message || '插件详情加载失败')
  }
}

const onPluginChange = (code) => loadPlugin(code)

const openPayUrl = () => {
  if (payUrl.value) window.open(payUrl.value, '_blank')
}

const submitPurchase = async () => {
  if (!form.value.pluginCode) {
    ElMessage.warning('请选择要购买的插件')
    return
  }
  if (!canSubmit.value) {
    ElMessage.warning(plans.value.length ? '请选择价格套餐后再下单' : '该插件暂未配置有效价格，暂不可购买')
    return
  }
  const agreementCheck = purchaseAgreementRef.value?.validate?.()
  if (agreementCheck && !agreementCheck.ok) {
    ElMessage.warning(agreementCheck.message || '请先同意购买协议')
    return
  }
  const agreements = purchaseAgreementRef.value?.getConsents?.()
  if (!agreements || agreements.length === 0) {
    ElMessage.warning('请阅读并勾选同意《商业插件购买与维护协议》后再下单')
    return
  }
  submitting.value = true
  try {
    order.value = await createPluginOrder({
      pluginCode: form.value.pluginCode,
      instanceId: siteInstanceId,
      licenseType: form.value.licenseType,
      version: pluginDetail.value?.plugin?.latestVersion || pluginDetail.value?.plugin?.version,
      // 套餐与期限必须一起带上：后端按 planCode 定价、按 validDays 折算维护期。
      // 只传金额不传套餐是无效的——金额永远由服务端算，客户端传了也不采信。
      planCode: form.value.planCode || undefined,
      validDays: selectedPlan.value?.validDays || undefined,
      agreements
    })
    await requestPay()
  } catch (error) {
    ElMessage.error(error?.message || '下单失败')
  } finally {
    submitting.value = false
  }
}

const requestPay = async () => {
  if (!payable.value) {
    order.value = await payPluginOrder({ orderNo: order.value.orderNo, payMethod: form.value.payMethod })
  } else {
    order.value = await payPluginOrder({
      orderNo: order.value.orderNo,
      payMethod: form.value.payMethod,
      terminal: 'PC',
      returnUrl: window.location.href
    })
  }

  if (order.value?.status === 1) {
    finish()
    return
  }
  stage.value = 'pay'
  startPolling()
}

const startPolling = () => {
  stopPolling()
  timer = setInterval(() => pollNow(), 4000)
}

const stopPolling = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

/**
 * 把「支付结果查询」的技术错误翻成人话。
 *
 * <p>典型场景：收银台服务（order）没部署 / 没注册到注册中心时，Feign 会抛出
 * 「Load balancer does not contain an instance for the service order」这类英文栈信息。
 * 用户已经付过款了，看到这种话只会以为是自己买错了。必须说清：订单在、钱的事按渠道走，
 * 只是「查不到支付结果」，可稍后到插件订单里同步。</p>
 */
const friendlyPollError = (error) => {
  const raw = String(error?.message || '')
  if (/load balancer|service order|feign|503|502/i.test(raw)) {
    return '支付结果查询服务暂不可用（收银台服务未部署或未注册），订单已创建。可稍后在「插件订单」里同步状态，或联系平台确认收款。'
  }
  return raw || '查询失败'
}

const pollNow = async () => {
  if (polling.value || !order.value?.orderNo) return
  polling.value = true
  try {
    const latest = await syncPluginOrder(order.value.orderNo)
    order.value = latest
    if (latest?.status === 1) {
      finish()
      return
    }
    if (latest?.status === 2) {
      stopPolling()
      ElMessage.warning('订单已关闭，请重新下单')
      stage.value = 'form'
      return
    }
    if (latest?.status === 4) {
      stopPolling()
      pollingTip.value = '支付失败，可重新发起支付或取消订单'
      ElMessage.warning(pollingTip.value)
      return
    }
    pollingTip.value = `尚未检测到支付结果，最近查询 ${new Date().toLocaleTimeString()}`
  } catch (error) {
    pollingTip.value = friendlyPollError(error)
  } finally {
    polling.value = false
  }
}

const confirmSandboxPayment = async (result) => {
  if (sandboxConfirming.value || !sandboxPayment.value || !order.value?.orderNo) return
  sandboxConfirming.value = true
  try {
    await confirmSandboxPluginPayment(order.value.orderNo, result)
    pollingTip.value = result === 'SUCCESS'
      ? '模拟支付已确认，正在同步订单…'
      : '模拟支付失败已提交，正在同步订单…'
    await pollNow()
  } catch (error) {
    pollingTip.value = error?.message || '模拟支付失败'
    ElMessage.error(pollingTip.value)
  } finally {
    sandboxConfirming.value = false
  }
}

const applyProgress = (status) => {
  const info = describeInstallProgress(status)
  pipeline.percent = Math.max(pipeline.percent, 35 + Math.round(info.percent * 0.6))
  pipeline.installDetail = info.label
  pipeline.tip = `安装阶段：${info.label}`
  if (info.healthOk !== null) {
    pipeline.healthOk = info.healthOk
    pipeline.healthDetail = info.healthDetail
  }
}

const runPostPurchasePipeline = async () => {
  if (pipeline.running) return
  pipeline.running = true
  pipeline.phase = 'paid'
  pipeline.percent = 15
  pipeline.tip = '支付成功，授权已自动签发'
  pipeline.healthOk = null
  pipeline.healthDetail = ''
  const code = form.value.pluginCode
  const version = targetVersion()
  try {
    // 1) 自动换票：独立服务走 delivery；内嵌走 precheck（安装链路内会再次换票）
    pipeline.phase = 'ticket'
    pipeline.percent = 28
    pipeline.tip = '正在换取制品下载令牌…'
    if (isStandaloneService(code)) {
      const delivery = await getServiceDelivery(code, version, 'runtime')
      const runtime = delivery?.runtime
      const hasTicket = Boolean(runtime?.token || runtime?.url || runtime?.cloudUrl)
      if (!hasTicket) throw new Error('换票失败：未返回制品下载令牌')
      pipeline.ticketDetail = runtime?.cloudUrl ? '对象存储预签名已签发' : '相对 raw 令牌已签发'
    } else {
      const pre = await precheckPlugin(code, version).catch(() => null)
      pipeline.ticketDetail = pre?.pass === false
        ? `预检提示：${pre.detail || '不兼容'}`
        : '授权就绪，安装时自动换票'
    }
    pipeline.percent = 40

    // 2) 安装进度
    pipeline.phase = 'install'
    pipeline.tip = '正在安装…'
    const installer = isStandaloneService(code) ? installServiceAndWait : installAndWait
    const { result, finalStatus } = await installer(code, version, applyProgress)
    const state = String(finalStatus?.state ?? result?.state ?? '').toUpperCase()

    // 3) 健康检查结果
    pipeline.phase = 'health'
    if (state === 'SUCCESS') {
      pipeline.percent = 100
      pipeline.healthOk = true
      pipeline.healthDetail = '健康检查通过'
      pipeline.installDetail = '安装完成'
      pipeline.phase = 'done'
      pipeline.tip = '购买、换票、安装与健康检查均已完成'
      ElMessage.success('插件已安装并通过健康检查')
      await refreshPluginApplications()
    } else if (state === 'ROLLED_BACK') {
      pipeline.healthOk = false
      pipeline.healthDetail = formatInstallFailure({ ...(finalStatus || result || {}), state: 'ROLLED_BACK' })
      pipeline.phase = 'failed'
      pipeline.tip = pipeline.healthDetail
    } else {
      pipeline.healthOk = false
      pipeline.healthDetail = formatInstallFailure(finalStatus || result || { state: 'FAILED' })
      pipeline.phase = 'failed'
      pipeline.tip = pipeline.healthDetail
    }
  } catch (error) {
    pipeline.phase = 'failed'
    pipeline.healthOk = false
    // 沙箱演示环境下，购买/支付/发证全部成功，但换取制品会被 3002 拦下。
    // 这时必须说清「是沙箱限制，不是商品没上架、也不是你没买成功」，
    // 否则用户会反复重下订单。explainArtifactError 负责把它翻译成人话。
    // request.js 把完整响应挂在 error.response.data 上，业务码从那里取
    const businessCode = error?.response?.data?.code ?? error?.code
    if (businessCode) {
      pipeline.healthDetail = `购买与授权均已成功；${explainArtifactError(businessCode, error?.response?.data?.message || error?.message)}`
      pipeline.installDetail = '未执行（制品获取被限制）'
    } else {
      pipeline.healthDetail = formatInstallFailure(error?.installStatus || { errorMsg: error?.message, state: 'FAILED' })
    }
    pipeline.tip = pipeline.healthDetail
  } finally {
    pipeline.running = false
  }
}

const downloadPurchased = async (kind) => {
  try {
    await downloadServiceArtifact(form.value.pluginCode, targetVersion(), kind)
  } catch (error) {
    ElMessage.error(error.message || '下载失败')
  }
}

const finish = () => {
  if (stage.value === 'done') return
  stopPolling()
  stage.value = 'done'
  ElMessage.success('支付成功，授权已自动签发并绑定到本站点')
  void runPostPurchasePipeline()
}

const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('取消后本次订单将关闭且不可支付，确定取消吗？', '取消订单', {
      type: 'warning',
      confirmButtonText: '确定取消',
      cancelButtonText: '继续支付'
    })
  } catch {
    return
  }
  try {
    await cancelPluginOrder(order.value.orderNo)
    stopPolling()
    stage.value = 'form'
    ElMessage.success('订单已取消')
  } catch (error) {
    ElMessage.error(error?.message || '取消失败')
  }
}

onMounted(async () => {
  const code = route.query.code ? String(route.query.code) : ''
  if (code) {
    form.value.pluginCode = code
    await loadPlugin(code)
  } else {
    await loadMarket()
  }
})

onBeforeUnmount(stopPolling)
</script>

<style scoped>
.checkout-page { padding: 20px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.page-header h1 { margin: 0 0 6px; font-size: 20px; }
.page-header p { margin: 0; color: #909399; font-size: 13px; }
.header-actions, .header-actions > * { flex-shrink: 0; }
.scope-tip { margin-bottom: 16px; }
.amount { font-size: 20px; font-weight: 600; color: #f56c6c; }
.plan-tip { margin-top: 4px; font-size: 12px; color: var(--el-text-color-secondary); line-height: 1.5; }
.locked-plugin { font-weight: 500; }
.pay-block { margin-top: 18px; }
.pay-tip { margin: 0 0 10px; }
.pay-url { margin-top: 10px; color: #909399; font-size: 12px; word-break: break-all; }
.polling { display: flex; align-items: center; gap: 10px; margin-top: 18px; }
.polling-tip { color: #909399; font-size: 12px; }
.raw-block { margin-top: 18px; }
.raw-block pre { margin: 0; font-size: 12px; white-space: pre-wrap; word-break: break-all; }
</style>
