<template>
  <div class="split-center-container">
    <!-- 顶部标题 + 步骤条 -->
    <div class="page-header">
      <div class="title-wrap">
        <h2 class="page-title">
          <el-icon class="title-icon"><Share /></el-icon>
          分账中心
        </h2>
        <div class="page-subtitle">
          按真实业务流程：选平台 → 设角色 → 整合支付 → 发起分账 → 查询/撤销
        </div>
      </div>
      <el-tag v-if="selectedPlatform" type="success" effect="plain">
        当前平台：{{ selectedPlatform.name }}（{{ selectedPlatform.chanNo }}）
      </el-tag>
    </div>

    <el-card shadow="never" class="step-card">
      <el-steps :active="activeStep" align-center finish-status="success" class="steps-bar">
        <el-step title="选择分账平台" description="通企付/收银宝/云商通" />
        <el-step title="设置分账角色" description="分账接收方绑定" />
        <el-step title="整合对应支付" description="商户+支付渠道" />
        <el-step title="发起分账" description="规则引擎+提交" />
        <el-step title="查询/撤销" description="分账记录管理" />
      </el-steps>

      <!-- ============ 第一步：选择分账平台 ============ -->
      <div v-if="activeStep === 0" class="step-panel">
        <div class="panel-title">① 选择分账平台</div>
        <div class="panel-desc">
          选择分账服务商平台，不同平台的单笔/批量能力、抽佣模板、通知方式不同。
        </div>
        <div class="platform-grid">
          <div
            v-for="p in platforms"
            :key="p.chanNo"
            class="platform-card"
            :class="{ active: selectedPlatform?.chanNo === p.chanNo }"
            @click="selectPlatform(p)"
          >
            <div class="platform-head">
              <span class="platform-name">{{ p.name }}</span>
              <el-tag size="small" type="info" effect="plain">{{ p.provider }}</el-tag>
            </div>
            <div class="platform-desc">{{ p.desc }}</div>
            <div class="platform-tags">
              <el-tag size="small" :type="p.multiReceivers ? 'success' : 'warning'" effect="plain">
                {{ p.multiReceivers ? '支持多接收方' : '单接收方' }}
              </el-tag>
              <el-tag size="small" type="primary" effect="plain" v-if="p.supportsCoupon">支持抽佣</el-tag>
              <el-tag size="small" type="info" effect="plain" v-if="p.template">支持分账模板</el-tag>
            </div>
            <div class="platform-foot">
              <el-tag size="small" type="warning" effect="plain">chanNo={{ p.chanNo }}</el-tag>
            </div>
          </div>
        </div>
        <div class="panel-actions">
          <el-button type="primary" :disabled="!selectedPlatform" @click="nextStep">下一步：设置分账角色</el-button>
        </div>
      </div>

      <!-- ============ 第二步：设置分账角色（接收方） ============ -->
      <div v-if="activeStep === 1" class="step-panel">
        <div class="panel-title">② 设置分账角色（分账接收方）</div>
        <div class="panel-desc">
          为当前商户配置分账接收方（个人/商户/平台），并设置分账比例（万分比）、优先级、保底/封顶与兜底方。
        </div>

        <el-form :inline="true" class="merchant-select">
          <el-form-item label="分账商户">
            <el-select
              v-model="receiverQuery.mchNo"
              filterable
              placeholder="选择分账商户号"
              style="width: 320px;"
              @change="loadReceivers"
            >
              <el-option
                v-for="m in merchantOptions"
                :key="m.mchNo"
                :label="`${m.mchShortName || m.companyName || m.mchNo}（${m.mchNo}）`"
                :value="m.mchNo"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadReceivers">
              <el-icon><Search /></el-icon>查询角色
            </el-button>
            <el-button @click="openReceiverDialog">
              <el-icon><Plus /></el-icon>新增接收方
            </el-button>
          </el-form-item>
        </el-form>

        <el-table :data="receiverList" v-loading="receiverLoading" stripe style="width: 100%;">
          <el-table-column prop="receiverNo" label="接收方编号" min-width="170" />
          <el-table-column prop="receiverName" label="接收方名称" min-width="150" />
          <el-table-column prop="receiverType" label="角色类型" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="receiverTypeTag[row.receiverType] || 'info'">
                {{ receiverTypeText[row.receiverType] || row.receiverType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="accountType" label="账户类型" width="100">
            <template #default="{ row }">
              {{ row.accountType === 'BANK' ? '对公/银行卡' : '内部账户' }}
            </template>
          </el-table-column>
          <el-table-column prop="accountNo" label="账户号" min-width="150" />
          <el-table-column prop="shareRatio" label="比例(万分比)" width="110" align="right">
            <template #default="{ row }">{{ row.shareRatio }} / 10000</template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="80" align="right" />
          <el-table-column label="兜底方" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isFallback === 1" size="small" type="warning">兜底</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="unbindReceiver(row)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步：整合对应支付</el-button>
        </div>
      </div>

      <!-- ============ 第三步：整合对应支付 ============ -->
      <div v-if="activeStep === 2" class="step-panel">
        <div class="panel-title">③ 整合对应支付</div>
        <div class="panel-desc">
          把分账平台与商户的收款支付渠道打通：选择商户与对应支付渠道，填写平台接入参数（应用ID/回调URL），确认整合配置。
        </div>

        <el-form :model="integration" label-width="120px" class="integration-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="分账商户号">
                <el-input v-model="integration.mchNo" placeholder="分账出金/入金商户号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商户应用ID">
                <el-input v-model="integration.appId" placeholder="通企付应用ID（appId）" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="分账平台">
                <el-input :value="selectedPlatform ? `${selectedPlatform.name}（${selectedPlatform.chanNo}）` : ''" disabled />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="关联支付渠道">
                <el-select v-model="integration.channelCode" filterable clearable placeholder="选择该商户的收款支付渠道" style="width: 100%;">
                  <el-option
                    v-for="c in channelOptions"
                    :key="c.channelCode"
                    :label="`${c.channelName}（${c.channelCode}）`"
                    :value="c.channelCode"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="分账结果回调URL">
                <el-input v-model="integration.notifyUrl" placeholder="https://xxx/notify/division（分账结果异步通知）" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div class="integration-preview" v-if="integration.mchNo && integration.appId && integration.channelCode">
          <div class="preview-title">整合配置预览</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="分账平台">{{ selectedPlatform?.name }}（{{ selectedPlatform?.chanNo }}）</el-descriptions-item>
            <el-descriptions-item label="关联支付渠道">{{ channelName(integration.channelCode) }}</el-descriptions-item>
            <el-descriptions-item label="商户号">{{ integration.mchNo }}</el-descriptions-item>
            <el-descriptions-item label="应用ID">{{ integration.appId }}</el-descriptions-item>
            <el-descriptions-item label="回调URL" :span="2">{{ integration.notifyUrl || '未配置' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步：发起分账</el-button>
        </div>
      </div>

      <!-- ============ 第四步：发起分账 ============ -->
      <div v-if="activeStep === 3" class="step-panel">
        <div class="panel-title">④ 发起分账</div>
        <div class="panel-desc">
          输入原收款订单，按已配置的角色用规则引擎测算每位接收方分得金额，确认后向 {{ selectedPlatform?.name }} 发起分账。
        </div>

        <el-form :model="divisionForm" label-width="120px" class="integration-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="原收款订单号">
                <el-input v-model="divisionForm.outTradeNo" placeholder="统一下单商户订单号（mchOrderNo）" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="本次分账金额(元)">
                <el-input-number v-model="divisionForm.amount" :min="0.01" :precision="2" :step="10" style="width: 100%;" placeholder="单位：元" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商户分账订单号">
                <el-input v-model="divisionForm.mchAssureOrderNo" placeholder="留空自动生成" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="抽佣金额(元)" v-if="selectedPlatform?.supportsCoupon">
                <el-input-number v-model="divisionForm.couponAmount" :min="0" :precision="2" :step="1" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <el-button type="primary" :loading="calcLoading" @click="runRuleEngine" class="calc-btn">
          <el-icon><Histogram /></el-icon>规则引擎测算分账明细
        </el-button>

        <el-table v-if="calcResult.length" :data="calcResult" stripe style="width: 100%; margin-top: 12px;">
          <el-table-column prop="receiverNo" label="接收方编号" min-width="170" />
          <el-table-column prop="receiverName" label="接收方" min-width="150" />
          <el-table-column prop="receiverType" label="角色" width="110">
            <template #default="{ row }">
              {{ receiverTypeText[row.receiverType] || row.receiverType }}
            </template>
          </el-table-column>
          <el-table-column prop="shareRatio" label="比例(万分比)" width="100" align="right" />
          <el-table-column label="分得金额" width="140" align="right">
            <template #default="{ row }">
              <span class="amount">¥ {{ yuan(row.shareAmount) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="兜底方" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isFallback === 1" size="small" type="warning">兜底</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" :loading="submitLoading" :disabled="!divisionForm.outTradeNo || !divisionForm.amount" @click="submitDivision">
            <el-icon><Upload /></el-icon>确认发起分账
          </el-button>
        </div>
      </div>

      <!-- ============ 第五步：查询/撤销 ============ -->
      <div v-if="activeStep === 4" class="step-panel">
        <div class="panel-title">⑤ 分账查询 / 撤销</div>
        <div class="panel-desc">
          按商户/订单/分账订单号查询分账结果，对未完成分账可发起撤销。
        </div>

        <el-form :inline="true" class="merchant-select">
          <el-form-item label="分账订单号">
            <el-input v-model="queryForm.mchAssureOrderNo" placeholder="商户分账订单号" clearable style="width: 190px;" />
          </el-form-item>
          <el-form-item label="商户号">
            <el-input v-model="queryForm.mchNo" placeholder="分账商户号" clearable style="width: 190px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadDivisionRecords">
              <el-icon><Search /></el-icon>查询
            </el-button>
            <el-button @click="loadDivisionRecords">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </el-form-item>
        </el-form>

        <el-table :data="divisionRecords" v-loading="queryLoading" stripe style="width: 100%;">
          <el-table-column prop="mchAssureOrderNo" label="商户分账订单号" min-width="180" />
          <el-table-column prop="mchOrderNo" label="原订单号" min-width="170" />
          <el-table-column prop="batchNo" label="平台分账批次号" min-width="170" />
          <el-table-column label="分账金额" width="130" align="right">
            <template #default="{ row }">¥ {{ yuan(row.amount ?? row.divisionAmount) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="divisionStateTag(row.state)">{{ divisionStateText(row.state) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发起时间" min-width="160">
            <template #default="{ row }">{{ row.createTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button
                link
                type="danger"
                size="small"
                :disabled="row.state === 4 || row.state === 5"
                @click="openCancelDialog(row)"
              >撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!queryLoading && !divisionRecords.length" description="暂无分账记录" :image-size="80" />

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="activeStep = 0">重新开始新分账</el-button>
        </div>
      </div>
    </el-card>

    <!-- ================= 新增接收方对话框 ================= -->
    <el-dialog v-model="receiverDialogVisible" title="新增分账接收方（分账角色）" width="680px" :close-on-click-modal="false">
      <el-form :model="receiverForm" :rules="receiverRules" ref="receiverFormRef" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="接收方名称" prop="receiverName">
              <el-input v-model="receiverForm.receiverName" placeholder="如：张三 / XX科技有限公司" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色类型" prop="receiverType">
              <el-select v-model="receiverForm.receiverType" style="width: 100%;">
                <el-option label="个人" value="PERSON" />
                <el-option label="商户" value="MERCHANT" />
                <el-option label="平台" value="PLATFORM" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户类型">
              <el-select v-model="receiverForm.accountType" style="width: 100%;">
                <el-option label="对公/银行卡" value="BANK" />
                <el-option label="平台内部账户" value="INNER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账户号" prop="accountNo">
              <el-input v-model="receiverForm.accountNo" placeholder="银行卡号/商户号/会员号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户行">
              <el-input v-model="receiverForm.accountBank" placeholder="开户行（BANK 类型）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系手机">
              <el-input v-model="receiverForm.phone" placeholder="联系手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分账比例(万分比)" prop="shareRatio">
              <el-input-number v-model="receiverForm.shareRatio" :min="0" :max="10000" :step="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-input-number v-model="receiverForm.priority" :min="1" :max="999" :step="10" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低分账(分)">
              <el-input-number v-model="receiverForm.minShareAmount" :min="0" :step="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高分账(分)">
              <el-input-number v-model="receiverForm.maxShareAmount" :min="0" :step="100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="兜底接收方">
              <el-switch v-model="receiverForm.isFallback" :active-value="1" :inactive-value="0" active-text="是（分完后余额归其）" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="receiverForm.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="receiverDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindLoading" @click="submitReceiver">绑定接收方</el-button>
      </template>
    </el-dialog>

    <!-- ================= 撤销分账对话框 ================= -->
    <el-dialog v-model="cancelDialogVisible" title="分账撤销" width="520px" :close-on-click-modal="false">
      <el-form :model="cancelForm" label-width="110px">
        <el-form-item label="原分账订单号">
          <el-input :value="cancelForm.mchAssureOrderNo" disabled />
        </el-form-item>
        <el-form-item label="撤销金额(元)">
          <el-input-number v-model="cancelForm.refundAmount" :min="0.01" :precision="2" :step="10" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="撤销原因">
          <el-select v-model="cancelForm.reason" style="width: 100%;">
            <el-option label="重复分账" value="重复分账" />
            <el-option label="订单退款" value="订单退款" />
            <el-option label="操作错误" value="操作错误" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="cancelForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelLoading" @click="confirmCancel">确认撤销</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 分账中心（按真实业务流程的分步向导）
 *
 * ① 选择分账平台：通企付分账渠道（allinpay 收银宝 / yunst2isv 特色结算 / yunst2yhzh 银行账户）
 * ② 设置分账角色：绑定分账接收方（个人/商户/平台），比例/优先级/保底/兜底
 * ③ 整合对应支付：商户 + 关联支付渠道 + 应用ID/回调URL
 * ④ 发起分账：规则引擎测算 → 批量分账提交（BatchDivisionRequest）
 * ⑤ 查询/撤销：分账记录查询 + 撤销
 *
 * 数据源：
 *  - /web/pay/division/biz/receiver/*   接收方真实接口
 *  - /web/pay/division/biz/ruleEngine/calc  规则引擎真实接口
 *  - /web/pay/merchant/list             商户列表
 *  - /web/pay/config/public             启用支付渠道
 *  - /web/pay/division/batch|query|refund  通企付分账
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Share, Upload, Histogram } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { splitCenterApi, divisionBizApi } from '@/api/payment/paymentBizW4'
import { isSuccessCode } from '@/utils/errorCode'

// ================= 步骤状态 =================
const activeStep = ref(0)
const nextStep = () => { activeStep.value = Math.min(4, activeStep.value + 1) }
const prevStep = () => { activeStep.value = Math.max(0, activeStep.value - 1) }

// ================= ① 分账平台 =================
const platforms = splitCenterApi.platforms()
const selectedPlatform = ref(null)
const selectPlatform = (p) => {
  selectedPlatform.value = p
  ElMessage.success(`已选择分账平台：${p.name}`)
}

// ================= 商户 / 渠道 =================
const merchantOptions = ref([])
const channelOptions = ref([])
const integration = reactive({ mchNo: '', appId: '', channelCode: '', notifyUrl: '' })

const loadMerchants = async () => {
  try {
    const res = await splitCenterApi.merchantList({ pageNo: 1, pageSize: 100 })
    if (res && isSuccessCode(res.code)) {
      merchantOptions.value = res.data?.records || res.data?.list || []
    }
  } catch (e) { console.error('加载商户失败:', e) }
}

const loadChannels = async () => {
  try {
    const res = await splitCenterApi.channelPublic({})
    if (res && isSuccessCode(res.code)) {
      channelOptions.value = res.data || []
    }
  } catch (e) { console.error('加载支付渠道失败:', e) }
}

const channelName = (code) => {
  const c = channelOptions.value.find((x) => x.channelCode === code)
  return c ? c.channelName : code
}

// ================= ② 分账角色（接收方） =================
const receiverLoading = ref(false)
const receiverList = ref([])
const receiverQuery = reactive({ mchNo: '' })
const receiverDialogVisible = ref(false)
const receiverFormRef = ref(null)
const bindLoading = ref(false)

const receiverTypeText = { PERSON: '个人', MERCHANT: '商户', PLATFORM: '平台' }
const receiverTypeTag = { PERSON: 'success', MERCHANT: 'primary', PLATFORM: 'warning' }

const receiverForm = reactive({
  mchNo: '',
  receiverName: '',
  receiverType: 'PERSON',
  accountType: 'BANK',
  accountNo: '',
  accountBank: '',
  phone: '',
  shareRatio: 0,
  priority: 100,
  minShareAmount: 0,
  maxShareAmount: 0,
  isFallback: 0,
  remark: ''
})

const receiverRules = {
  receiverName: [{ required: true, message: '请输入接收方名称', trigger: 'blur' }],
  receiverType: [{ required: true, message: '请选择角色类型', trigger: 'change' }],
  accountNo: [{ required: true, message: '请输入账户号', trigger: 'blur' }],
  shareRatio: [{ required: true, message: '请设置分账比例(万分比)', trigger: 'blur' }]
}

const loadReceivers = async () => {
  if (!receiverQuery.mchNo) { ElMessage.warning('请先选择分账商户号'); return }
  receiverLoading.value = true
  try {
    const res = await divisionBizApi.receiverList({ mchNo: receiverQuery.mchNo })
    if (res && isSuccessCode(res.code)) {
      receiverList.value = res.data || []
    } else {
      receiverList.value = []
    }
  } catch (e) {
    console.error('查询接收方失败:', e)
    receiverList.value = []
  } finally {
    receiverLoading.value = false
  }
}

const openReceiverDialog = () => {
  if (!receiverQuery.mchNo) { ElMessage.warning('请先选择分账商户号'); return }
  Object.assign(receiverForm, {
    mchNo: receiverQuery.mchNo,
    receiverName: '', receiverType: 'PERSON', accountType: 'BANK',
    accountNo: '', accountBank: '', phone: '',
    shareRatio: 0, priority: 100, minShareAmount: 0, maxShareAmount: 0, isFallback: 0, remark: ''
  })
  receiverDialogVisible.value = true
}

const submitReceiver = async () => {
  const valid = await receiverFormRef.value?.validate().catch(() => false)
  if (!valid) return
  bindLoading.value = true
  try {
    const res = await divisionBizApi.receiverBind({ ...receiverForm })
    if (res && isSuccessCode(res.code)) {
      ElMessage.success('分账接收方绑定成功')
      receiverDialogVisible.value = false
      loadReceivers()
    } else {
      ElMessage.error(res?.message || '绑定失败')
    }
  } catch (e) {
    ElMessage.error('绑定失败: ' + (e.message || '网络错误'))
  } finally {
    bindLoading.value = false
  }
}

const unbindReceiver = async (row) => {
  try {
    await ElMessageBox.confirm(`确认解绑接收方「${row.receiverName}」？解绑后该角色将不可参与分账。`, '解绑确认', { type: 'warning' })
  } catch { return }
  try {
    const res = await divisionBizApi.receiverUnbind({ receiverNo: row.receiverNo })
    if (res && isSuccessCode(res.code) && res.data) {
      ElMessage.success('已解绑')
      loadReceivers()
    } else {
      ElMessage.error(res?.message || '解绑失败')
    }
  } catch (e) {
    ElMessage.error('解绑失败: ' + (e.message || '网络错误'))
  }
}

// ================= ④ 发起分账 =================
const divisionForm = reactive({ outTradeNo: '', amount: null, mchAssureOrderNo: '', couponAmount: 0 })
const calcLoading = ref(false)
const calcResult = ref([])
const submitLoading = ref(false)

const runRuleEngine = async () => {
  if (!receiverQuery.mchNo) { ElMessage.warning('请先在「设置分账角色」选择分账商户'); return }
  if (!divisionForm.amount) { ElMessage.warning('请输入本次分账金额'); return }
  calcLoading.value = true
  calcResult.value = []
  try {
    const res = await divisionBizApi.ruleEngineCalc({
      mchNo: receiverQuery.mchNo,
      totalAmount: Math.round(divisionForm.amount * 100) // 分
    })
    if (res && isSuccessCode(res.code)) {
      calcResult.value = res.data || []
      if (!calcResult.value.length) ElMessage.info('该商户暂无可用接收方，请先绑定分账角色')
    } else {
      ElMessage.error(res?.message || '规则引擎测算失败')
    }
  } catch (e) {
    ElMessage.error('规则引擎测算失败: ' + (e.message || '网络错误'))
  } finally {
    calcLoading.value = false
  }
}

const submitDivision = async () => {
  if (!selectedPlatform.value) { ElMessage.warning('请先选择分账平台'); return }
  if (!divisionForm.outTradeNo || !divisionForm.amount) { ElMessage.warning('请输入原订单号与分账金额'); return }
  if (!calcResult.value.length) {
    await runRuleEngine()
    if (!calcResult.value.length) return
  }
  // 组装 BatchDivisionInfoDTO 分账列表
  const details = calcResult.value.map((r) => ({
    divisionBizMemNo: r.receiverNo,
    amount: Number(r.shareAmount),
    remark: r.receiverName
  }))
  const info = {
    mchAssureOrderNo: divisionForm.mchAssureOrderNo || `MD${Date.now()}`,
    mchOrderNo: divisionForm.outTradeNo,
    orderAmount: Math.round(divisionForm.amount * 100)
  }
  if (selectedPlatform.value.supportsCoupon && divisionForm.couponAmount) {
    info.couponAmount = Math.round(divisionForm.couponAmount * 100)
  }
  info.divisionDetails = details

  submitLoading.value = true
  try {
    const res = await request.post('/web/pay/division/batch', {
      chanNo: selectedPlatform.value.chanNo,
      mchNo: receiverQuery.mchNo || integration.mchNo,
      appId: integration.appId,
      notifyUrl: integration.notifyUrl || undefined,
      batchDivisionInfos: JSON.stringify([info])
    })
    if (res && isSuccessCode(res.code)) {
      ElMessage.success('分账指令已提交')
      // 记录到本会话批次列表（保证第五步有可见记录）
      localSubmitted.value = [{
        mchAssureOrderNo: info.mchAssureOrderNo,
        mchOrderNo: divisionForm.outTradeNo,
        batchNo: res.data?.batchNo || res.data?.assureOrderId || '-',
        amount: Math.round(divisionForm.amount * 100),
        state: res.data?.state ?? 1,
        createTime: new Date().toLocaleString('zh-CN')
      }, ...localSubmitted.value]
      activeStep.value = 4
      loadDivisionRecords()
    } else {
      ElMessage.error(res?.message || '分账提交失败')
    }
  } catch (e) {
    ElMessage.error('分账提交失败: ' + (e.message || '网络错误'))
  } finally {
    submitLoading.value = false
  }
}

// ================= ⑤ 查询 / 撤销 =================
const queryLoading = ref(false)
const divisionRecords = ref([])
const queryForm = reactive({ mchAssureOrderNo: '', mchNo: '' })
const cancelDialogVisible = ref(false)
const cancelLoading = ref(false)
const cancelForm = reactive({ mchAssureOrderNo: '', refundAmount: 0, reason: '重复分账', remark: '' })
// 本会话已提交的分账批次（前端暂存，保证第五步有可见记录；真实状态以平台查询为准）
const localSubmitted = ref([])

const divisionStateText = (s) => {
  const map = { 0: '受理中', 1: '分账中', 2: '成功', 3: '失败', 4: '已撤销', 5: '撤销中' }
  return map[s] ?? map[Number(s)] ?? '未知'
}
const divisionStateTag = (s) => {
  const map = { 0: 'info', 1: 'primary', 2: 'success', 3: 'danger', 4: 'info', 5: 'warning' }
  return map[s] ?? map[Number(s)] ?? 'info'
}

const loadDivisionRecords = async () => {
  // 1) 先展示本会话提交的批次
  divisionRecords.value = [...localSubmitted.value]

  // 2) 若填了商户分账订单号，则按真实接口查单并覆盖/合并
  const orderNo = queryForm.mchAssureOrderNo
  if (!orderNo) {
    if (!divisionRecords.value.length) ElMessage.info('输入「商户分账订单号」可从通企付查询真实分账结果')
    return
  }
  queryLoading.value = true
  try {
    const res = await request.post('/web/pay/division/query', {
      mchNo: queryForm.mchNo || receiverQuery.mchNo || integration.mchNo,
      appId: integration.appId,
      mchAssureOrderNo: orderNo
    })
    if (res && isSuccessCode(res.code) && res.data) {
      const d = res.data
      const row = Array.isArray(d) ? d[0] : d
      if (row && (row.mchAssureOrderNo || row.assureOrderId || row.batchNo)) {
        divisionRecords.value = [row, ...divisionRecords.value.filter((r) => r.mchAssureOrderNo !== (row.mchAssureOrderNo || orderNo))]
      }
    } else {
      ElMessage.error(res?.message || '查询分账结果失败')
    }
  } catch (e) {
    ElMessage.error('查询分账结果失败: ' + (e.message || '网络错误'))
  } finally {
    queryLoading.value = false
  }
}

const openCancelDialog = (row) => {
  Object.assign(cancelForm, {
    mchAssureOrderNo: row.mchAssureOrderNo || row.mchOrderNo || '',
    refundAmount: Number(row.amount || row.divisionAmount || 0) / 100 || 0,
    reason: '重复分账',
    remark: ''
  })
  cancelDialogVisible.value = true
}

const confirmCancel = async () => {
  cancelLoading.value = true
  try {
    // 对齐 RefundAssureOrderRequest：refundMchOrderNo=本次撤销单号（必填），mchOrderNo=原商户分账订单号
    const res = await request.post('/web/pay/division/refund', {
      chanNo: selectedPlatform.value?.chanNo,
      mchNo: queryForm.mchNo || receiverQuery.mchNo || integration.mchNo,
      appId: integration.appId,
      refundMchOrderNo: `DRF${Date.now()}`,
      mchOrderNo: cancelForm.mchAssureOrderNo,
      refundAmount: Math.round((Number(cancelForm.refundAmount || 0)) * 100),
      reason: cancelForm.reason,
      remark: cancelForm.remark
    })
    if (res && isSuccessCode(res.code)) {
      ElMessage.success('分账撤销已提交')
      cancelDialogVisible.value = false
      // 更新本地记录状态为「撤销中」
      localSubmitted.value = localSubmitted.value.map((r) =>
        r.mchAssureOrderNo === cancelForm.mchAssureOrderNo ? { ...r, state: 5 } : r
      )
      loadDivisionRecords()
    } else {
      ElMessage.error(res?.message || '撤销提交失败')
    }
  } catch (e) {
    ElMessage.error('撤销提交失败: ' + (e.message || '网络错误'))
  } finally {
    cancelLoading.value = false
  }
}

// ================= 工具 =================
const yuan = (fen) => {
  if (fen === null || fen === undefined) return '0.00'
  return (Number(fen) / 100).toFixed(2)
}

// ================= 初始化 =================
onMounted(() => {
  loadMerchants()
  loadChannels()
})
</script>

<style scoped>
.split-center-container { padding: 16px; }

.page-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px;
}
.title-wrap .page-title { margin: 0; font-size: 20px; font-weight: 600; display: flex; align-items: center; color: #303133; }
.title-icon { margin-right: 8px; color: #409eff; font-size: 22px; }
.page-subtitle { margin-top: 4px; font-size: 13px; color: #909399; }

.step-card { border-radius: 8px; }
.step-card :deep(.el-card__body) { padding: 18px 20px; }
.steps-bar { margin-bottom: 20px; padding: 8px 0; }

.step-panel { min-height: 320px; }
.panel-title { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 6px; }
.panel-desc { font-size: 13px; color: #909399; margin-bottom: 14px; }

.platform-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 12px; }
.platform-card {
  border: 1px solid #ebeef5; border-radius: 8px; padding: 14px 16px; cursor: pointer;
  transition: all 0.2s; background: #fafbfc;
}
.platform-card:hover { border-color: #409eff; box-shadow: 0 4px 12px rgba(64,158,255,.12); }
.platform-card.active { border-color: #409eff; background: #ecf5ff; box-shadow: 0 4px 12px rgba(64,158,255,.18); }
.platform-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.platform-name { font-size: 16px; font-weight: 600; color: #303133; }
.platform-desc { font-size: 13px; color: #606266; line-height: 1.5; margin-bottom: 8px; min-height: 38px; }
.platform-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.platform-foot { display: flex; justify-content: flex-end; }

.panel-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 18px; padding-top: 14px; border-top: 1px solid #f0f2f5; }

.merchant-select { margin-bottom: 10px; }

.integration-form { max-width: 860px; }
.integration-preview {
  margin-top: 10px; border: 1px solid #e4e7ed; border-radius: 8px; padding: 12px; background: #fafbfc;
}
.preview-title { font-size: 14px; font-weight: 600; margin-bottom: 10px; color: #303133; }

.calc-btn { margin-top: 4px; }
.amount { color: #f56c6c; font-weight: 600; }
</style>
