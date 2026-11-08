<template>
  <div class="risk-center-container">
    <!-- 顶部标题 + 步骤条 -->
    <div class="page-header">
      <div class="title-wrap">
        <h2 class="page-title">
          <el-icon class="title-icon"><Warning /></el-icon>
          风控中心
        </h2>
        <div class="page-subtitle">
          按真实业务流程：配置规则 → 实时评估 → 风险日志 → 人工审核 → 黑白名单
        </div>
      </div>
      <el-tag type="warning" effect="plain">支付风控 · 引擎评估</el-tag>
    </div>

    <el-card shadow="never" class="step-card">
      <el-steps :active="activeStep" align-center finish-status="success" class="steps-bar">
        <el-step title="配置风控规则" description="金额/频次/动作" />
        <el-step title="实时风控评估" description="模拟交易检测" />
        <el-step title="风险日志" description="命中与拦截记录" />
        <el-step title="人工审核" description="待审核事件处置" />
        <el-step title="黑白名单" description="名单维护" />
      </el-steps>

      <!-- ============ 第一步：配置风控规则 ============ -->
      <div v-if="activeStep === 0" class="step-panel">
        <div class="panel-title">① 配置风控规则</div>
        <div class="panel-desc">创建交易金额/频次等触发规则，并设定处置动作（放行/拦截/告警/人工审核）。</div>

        <el-button type="primary" size="small" @click="openRuleDialog" style="margin-bottom: 10px;">
          <el-icon><Plus /></el-icon>新增规则
        </el-button>

        <el-table :data="ruleList" v-loading="ruleLoading" stripe style="width: 100%;">
          <el-table-column prop="ruleNo" label="规则编码" min-width="130" />
          <el-table-column prop="ruleName" label="规则名称" min-width="140" />
          <el-table-column label="规则类型" width="100">
            <template #default="{ row }">{{ ruleTypeText[row.ruleType] || row.ruleType }}</template>
          </el-table-column>
          <el-table-column label="触发字段/条件" min-width="150">
            <template #default="{ row }">
              {{ row.triggerField }} {{ operatorText[row.operator] || row.operator }} {{ row.thresholdValue }}
              <span v-if="row.timeWindow"> · {{ row.timeWindow }}s</span>
            </template>
          </el-table-column>
          <el-table-column label="处置动作" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="actionTag[row.action] || 'info'">{{ actionText[row.action] || row.action }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="生效范围" width="100">
            <template #default="{ row }">{{ scopeText[row.scope] || row.scope }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openRuleDialog(row)">编辑</el-button>
              <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleRule(row)">
                {{ row.status === 1 ? '停用' : '启用' }}
              </el-button>
              <el-button link type="danger" size="small" @click="deleteRule(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="panel-actions">
          <el-button type="primary" @click="nextStep">下一步：实时风控评估</el-button>
        </div>
      </div>

      <!-- ============ 第二步：实时风控评估 ============ -->
      <div v-if="activeStep === 1" class="step-panel">
        <div class="panel-title">② 实时风控评估</div>
        <div class="panel-desc">模拟一笔交易，调用风控引擎实时评估（黑名单/规则命中/白名单放行），输出处置结论并留痕。</div>

        <el-form :model="checkForm" label-width="110px" class="integration-form">
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="业务类型"><el-select v-model="checkForm.bizType" style="width:100%;"><el-option label="统一收银台" :value="1" /><el-option label="商城订单" :value="2" /><el-option label="转账" :value="3" /></el-select></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="业务单号"><el-input v-model="checkForm.bizNo" placeholder="业务单号" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="商户号"><el-input v-model="checkForm.mchNo" placeholder="商户号" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="用户ID"><el-input v-model="checkForm.userId" placeholder="用户ID" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="用户IP"><el-input v-model="checkForm.userIp" placeholder="用户IP" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="交易金额(元)"><el-input-number v-model="checkForm.amount" :min="0" :precision="2" style="width:100%;" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="支付方式"><el-select v-model="checkForm.payMethod" clearable style="width:100%;"><el-option label="微信" value="WECHAT" /><el-option label="支付宝" value="ALIPAY" /><el-option label="银行卡" value="BANKCARD" /></el-select></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="银行卡号"><el-input v-model="checkForm.bankCardNo" placeholder="银行卡号" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="手机号"><el-input v-model="checkForm.phone" placeholder="手机号" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="证件号"><el-input v-model="checkForm.idCardNo" placeholder="身份证号" /></el-form-item></el-col>
          </el-row>
        </el-form>

        <el-button type="primary" :loading="checking" @click="doCheck" class="calc-btn">
          <el-icon><Histogram /></el-icon>执行风控评估
        </el-button>

        <div v-if="checkResult" class="check-result" :class="{ pass: checkResult.pass, reject: !checkResult.pass }">
          <div class="result-head">
            <el-tag :type="checkResult.pass ? 'success' : 'danger'" size="large">
              {{ checkResult.pass ? '✅ 放行' : '❌ 拦截' }}
            </el-tag>
            <span class="result-action">处置动作：{{ actionText[checkResult.action] || checkResult.action }}</span>
            <span class="result-action">风险等级：{{ riskLevelText[checkResult.riskLevel] || checkResult.riskLevel }}</span>
          </div>
          <div v-if="checkResult.rejectReason" class="result-reason">{{ checkResult.rejectReason }}</div>
          <div class="result-reason">评估流水号：{{ checkResult.logNo }}</div>
          <el-table v-if="checkResult.hitRules?.length" :data="checkResult.hitRules" stripe size="small" style="margin-top:8px; width:100%;">
            <el-table-column prop="ruleNo" label="规则" min-width="140" />
            <el-table-column prop="ruleName" label="规则名称" min-width="140" />
            <el-table-column label="触发" min-width="180">
              <template #default="{ row }">{{ row.triggerField }} {{ operatorText[row.operator] || row.operator }} {{ row.thresholdValue }}，实际 {{ row.actualValue }}</template>
            </el-table-column>
            <el-table-column label="动作" width="100">
              <template #default="{ row }">{{ actionText[row.action] }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步：风险日志</el-button>
        </div>
      </div>

      <!-- ============ 第三步：风险日志 ============ -->
      <div v-if="activeStep === 2" class="step-panel">
        <div class="panel-title">③ 风险日志</div>
        <div class="panel-desc">查看所有风控评估留痕，按处置动作/风险等级/审核状态过滤。</div>

        <el-form :inline="true" class="merchant-select">
          <el-form-item label="处置动作">
            <el-select v-model="logQuery.action" clearable placeholder="全部" style="width:130px;">
              <el-option v-for="(t, k) in actionText" :key="k" :label="t" :value="Number(k)" />
            </el-select>
          </el-form-item>
          <el-form-item label="审核状态">
            <el-select v-model="logQuery.auditStatus" clearable placeholder="全部" style="width:130px;">
              <el-option label="未审核" :value="0" />
              <el-option label="通过" :value="1" />
              <el-option label="拒绝" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="用户ID"><el-input v-model="logQuery.userId" clearable placeholder="用户ID" style="width:150px;" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadLogs">查询</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="logList" v-loading="logLoading" stripe style="width:100%;">
          <el-table-column prop="logNo" label="流水号" min-width="160" />
          <el-table-column prop="bizNo" label="业务单号" min-width="130">
            <template #default="{ row }">{{ row.bizNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="userId" label="用户" width="110">
            <template #default="{ row }">{{ row.userId || '-' }}</template>
          </el-table-column>
          <el-table-column label="金额" width="110" align="right">
            <template #default="{ row }">¥ {{ yuan(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="处置" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="actionTag[row.action] || 'info'">{{ actionText[row.action] || row.action }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="riskLevelTag[row.riskLevel] || 'info'">{{ riskLevelText[row.riskLevel] || row.riskLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="auditStatusType(row.auditStatus)">{{ auditStatusText(row.auditStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="160">
            <template #default="{ row }">{{ row.createTime || '-' }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!logLoading && !logList.length" description="暂无风险日志" :image-size="80" />

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步：人工审核</el-button>
        </div>
      </div>

      <!-- ============ 第四步：人工审核 ============ -->
      <div v-if="activeStep === 3" class="step-panel">
        <div class="panel-title">④ 人工审核</div>
        <div class="panel-desc">对处置动作=人工审核（action=4）的风控事件进行人工审核：通过或拒绝，写入审核结论与操作人。</div>

        <el-table :data="auditList" v-loading="auditLoading" stripe style="width:100%;">
          <el-table-column prop="logNo" label="流水号" min-width="160" />
          <el-table-column prop="bizNo" label="业务单号" min-width="130">
            <template #default="{ row }">{{ row.bizNo || '-' }}</template>
          </el-table-column>
          <el-table-column prop="userId" label="用户" width="110">
            <template #default="{ row }">{{ row.userId || '-' }}</template>
          </el-table-column>
          <el-table-column label="金额" width="110" align="right">
            <template #default="{ row }">¥ {{ yuan(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="拒绝原因" min-width="180">
            <template #default="{ row }">{{ row.rejectReason || '-' }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" min-width="160">
            <template #default="{ row }">{{ row.createTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="success" size="small" :disabled="row.auditStatus > 0" @click="doAudit(row, 1)">通过</el-button>
              <el-button link type="danger" size="small" :disabled="row.auditStatus > 0" @click="doAudit(row, 2)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!auditLoading && !auditList.length" description="暂无待审核风控事件" :image-size="80" />

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="nextStep">下一步：黑白名单</el-button>
        </div>
      </div>

      <!-- ============ 第五步：黑白名单 ============ -->
      <div v-if="activeStep === 4" class="step-panel">
        <div class="panel-title">⑤ 黑白名单</div>
        <div class="panel-desc">维护用户/商户/IP/银行卡/手机/身份证 的黑白名单，命中白名单直接放行、命中黑名单直接拦截。</div>

        <el-form :inline="true" class="merchant-select">
          <el-form-item label="名单类型">
            <el-select v-model="blackQuery.listType" style="width:130px;">
              <el-option label="黑名单" :value="1" />
              <el-option label="白名单" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标类型">
            <el-select v-model="blackQuery.targetType" clearable placeholder="全部" style="width:140px;">
              <el-option label="用户" :value="1" />
              <el-option label="商户" :value="2" />
              <el-option label="IP" :value="3" />
              <el-option label="银行卡" :value="4" />
              <el-option label="手机" :value="5" />
              <el-option label="身份证" :value="6" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadBlacklist">查询</el-button>
            <el-button type="success" @click="openBlackDialog"><el-icon><Plus /></el-icon>新增名单</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="blackList" v-loading="blackLoading" stripe style="width:100%;">
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.listType === 1 ? 'danger' : 'success'">{{ row.listType === 1 ? '黑名单' : '白名单' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="目标" width="100">
            <template #default="{ row }">{{ blackTargetText[row.targetType] || row.targetType }}</template>
          </el-table-column>
          <el-table-column prop="targetValue" label="目标值" min-width="170" />
          <el-table-column label="风险等级" width="90">
            <template #default="{ row }">{{ riskLevelText[row.riskLevel] || row.riskLevel }}</template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="140">
            <template #default="{ row }">{{ row.reason || '-' }}</template>
          </el-table-column>
          <el-table-column prop="expireTime" label="过期时间" min-width="160">
            <template #default="{ row }">{{ row.expireTime ? String(row.expireTime).slice(0, 10) : '永久' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="removeBlack(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!blackLoading && !blackList.length" description="暂无名单记录" :image-size="80" />

        <div class="panel-actions">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="activeStep = 0">完成，重新开始</el-button>
        </div>
      </div>
    </el-card>

    <!-- ================= 新增规则对话框 ================= -->
    <el-dialog v-model="ruleDialogVisible" :title="editRule ? '编辑规则' : '新增风控规则'" width="620px" :close-on-click-modal="false">
      <el-form :model="ruleForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="规则名称"><el-input v-model="ruleForm.ruleName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规则编码"><el-input v-model="ruleForm.ruleNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规则类型"><el-select v-model="ruleForm.ruleType" style="width:100%;"><el-option label="交易金额" :value="1" /><el-option label="交易频次" :value="2" /><el-option label="自定义" :value="4" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="触发字段"><el-select v-model="ruleForm.triggerField" style="width:100%;"><el-option label="金额 amount" value="amount" /><el-option label="频次 count" value="count" /><el-option label="自定义 custom" value="custom" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="比较操作符"><el-select v-model="ruleForm.operator" style="width:100%;"><el-option label=">" value=">" /><el-option label=">=" value=">=" /><el-option label="==" value="==" /><el-option label="<" value="<" /><el-option label="<=" value="<=" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="阈值"><el-input-number v-model="ruleForm.thresholdValue" :min="0" style="width:100%;" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="时间窗口(s)"><el-input-number v-model="ruleForm.timeWindow" :min="0" style="width:100%;" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="处置动作"><el-select v-model="ruleForm.action" style="width:100%;"><el-option label="放行" :value="1" /><el-option label="拦截" :value="2" /><el-option label="告警" :value="3" /><el-option label="人工审核" :value="4" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="生效范围"><el-select v-model="ruleForm.scope" style="width:100%;"><el-option label="全局" :value="1" /><el-option label="特定商户" :value="2" /><el-option label="特定用户" :value="3" /><el-option label="支付方式" :value="4" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="范围值"><el-input v-model="ruleForm.scopeValue" placeholder="scope=2/3/4 时的值" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="优先级"><el-input-number v-model="ruleForm.priority" :min="1" :max="999" style="width:100%;" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="ruleForm.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSaving" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

    <!-- ================= 新增名单对话框 ================= -->
    <el-dialog v-model="blackDialogVisible" title="新增黑白名单" width="520px" :close-on-click-modal="false">
      <el-form :model="blackForm" label-width="110px">
        <el-form-item label="名单类型">
          <el-select v-model="blackForm.listType" style="width:100%;">
            <el-option label="黑名单" :value="1" />
            <el-option label="白名单" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="blackForm.targetType" style="width:100%;">
            <el-option label="用户" :value="1" />
            <el-option label="商户" :value="2" />
            <el-option label="IP" :value="3" />
            <el-option label="银行卡" :value="4" />
            <el-option label="手机" :value="5" />
            <el-option label="身份证" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标值">
          <el-input v-model="blackForm.targetValue" placeholder="如：用户ID / 商户号 / IP / 卡号 / 手机号" />
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="blackForm.riskLevel" style="width:100%;">
            <el-option label="低" :value="1" /><el-option label="中" :value="2" /><el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="blackForm.reason" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blackDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="blackSaving" @click="saveBlack">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 风控中心（按真实业务流程的分步向导）
 *
 * ① 配置风控规则 → ② 实时风控评估 → ③ 风险日志 → ④ 人工审核 → ⑤ 黑白名单
 *
 * 枚举对齐后端：
 *  - ruleType: 1金额 2频次 4自定义；action: 1放行 2拦截 3告警 4人工审核；scope: 1全局 2商户 3用户 4支付方式
 *  - 黑名单 targetType: 1用户 2商户 3IP 4银行卡 5手机 6身份证
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, Plus, Histogram } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeStep = ref(0)
const nextStep = () => { activeStep.value = Math.min(4, activeStep.value + 1) }
const prevStep = () => { activeStep.value = Math.max(0, activeStep.value - 1) }

// 枚举
const ruleTypeText = { 1: '交易金额', 2: '交易频次', 4: '自定义' }
const operatorText = { '>': '>', '>=': '>=', '==': '==', '<': '<', '<=': '<=' }
const actionText = { 1: '放行', 2: '拦截', 3: '告警', 4: '人工审核' }
const actionTag = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'primary' }
const scopeText = { 1: '全局', 2: '商户', 3: '用户', 4: '支付方式' }
const riskLevelText = { 0: '低', 1: '低', 2: '中', 3: '高' }
const riskLevelTag = { 0: 'info', 1: 'info', 2: 'warning', 3: 'danger' }
const blackTargetText = { 1: '用户', 2: '商户', 3: 'IP', 4: '银行卡', 5: '手机', 6: '身份证' }

// 审计状态文本（独立对象避免模板直接访问未定义）
const auditStatusTextMap = { 0: '未审核', 1: '通过', 2: '拒绝' }
const auditStatusTextTypeMap = { 0: 'warning', 1: 'success', 2: 'danger' }
const auditStatusText = (s) => auditStatusTextMap[s] ?? auditStatusTextMap[Number(s)] ?? '未审核'
const auditStatusType = (s) => auditStatusTextTypeMap[s] ?? auditStatusTextTypeMap[Number(s)] ?? 'warning'

// ================= ① 风控规则 =================
const ruleLoading = ref(false)
const ruleList = ref([])
const ruleDialogVisible = ref(false)
const ruleSaving = ref(false)
const editRule = ref(null)
const ruleForm = reactive({
  ruleName: '', ruleNo: '', ruleType: 1, triggerField: 'amount', operator: '>',
  thresholdValue: 0, timeWindow: 0, action: 2, scope: 1, scopeValue: '', priority: 100, remark: ''
})

const loadRules = async () => {
  ruleLoading.value = true
  try {
    const res = await request.post('/web/pay/risk/rule/list', { pageNum: 1, pageSize: 100 })
    if (res && res.code === 200) ruleList.value = res.data?.records || res.data?.list || []
    else ruleList.value = []
  } catch (e) { console.error(e); ruleList.value = [] } finally { ruleLoading.value = false }
}

const openRuleDialog = (row) => {
  editRule.value = row || null
  if (row) {
    Object.assign(ruleForm, {
      ruleName: row.ruleName, ruleNo: row.ruleNo, ruleType: row.ruleType, triggerField: row.triggerField,
      operator: row.operator, thresholdValue: Number(row.thresholdValue || 0), timeWindow: row.timeWindow || 0,
      action: row.action, scope: row.scope || 1, scopeValue: row.scopeValue || '', priority: row.priority || 100, remark: row.remark || ''
    })
  } else {
    Object.assign(ruleForm, {
      ruleName: '', ruleNo: '', ruleType: 1, triggerField: 'amount', operator: '>', thresholdValue: 0,
      timeWindow: 0, action: 2, scope: 1, scopeValue: '', priority: 100, remark: ''
    })
  }
  ruleDialogVisible.value = true
}

const saveRule = async () => {
  if (!ruleForm.ruleName) { ElMessage.warning('请输入规则名称'); return }
  ruleSaving.value = true
  try {
    const api = editRule.value ? '/web/pay/risk/rule/update' : '/web/pay/risk/rule/create'
    const data = { ...ruleForm }
    if (editRule.value) data.id = editRule.value.id
    const res = await request.post(api, data)
    if (res && res.code === 200) {
      ElMessage.success('规则保存成功')
      ruleDialogVisible.value = false
      loadRules()
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.message || '网络错误'))
  } finally {
    ruleSaving.value = false
  }
}

const toggleRule = async (row) => {
  try {
    const res = await request.post('/web/pay/risk/rule/changeStatus', { ruleNo: row.ruleNo, status: row.status === 1 ? 0 : 1 })
    if (res && res.code === 200) { ElMessage.success('状态已变更'); loadRules() }
    else ElMessage.error(res?.message || '操作失败')
  } catch (e) { ElMessage.error('操作失败: ' + (e.message || '网络错误')) }
}

const deleteRule = async (row) => {
  try { await ElMessageBox.confirm(`确认删除规则「${row.ruleName}」？`, '删除确认', { type: 'warning' }) } catch { return }
  try {
    const res = await request.post('/web/pay/risk/rule/delete', { ruleNo: row.ruleNo })
    if (res && res.code === 200) { ElMessage.success('删除成功'); loadRules() }
    else ElMessage.error(res?.message || '删除失败')
  } catch (e) { ElMessage.error('删除失败: ' + (e.message || '网络错误')) }
}

// ================= ② 实时评估 =================
const checking = ref(false)
const checkResult = ref(null)
const checkForm = reactive({ bizType: 1, bizNo: '', mchNo: '', userId: '', userIp: '', amount: null, payMethod: '', bankCardNo: '', phone: '', idCardNo: '' })

const doCheck = async () => {
  if (!checkForm.mchNo) { ElMessage.warning('请填写商户号'); return }
  checking.value = true
  checkResult.value = null
  try {
    const data = { ...checkForm }
    if (data.amount) data.amount = Number(data.amount)
    const res = await request.post('/web/pay/risk/check', data)
    if (res && res.code === 200) {
      checkResult.value = res.data
      if (res.data?.pass) ElMessage.success('风控评估通过，放行')
      else ElMessage.warning('风控评估命中，需处置')
    } else {
      ElMessage.error(res?.message || '风控评估失败')
    }
  } catch (e) {
    ElMessage.error('风控评估失败: ' + (e.message || '网络错误'))
  } finally {
    checking.value = false
  }
}

// ================= ③ 风险日志 =================
const logLoading = ref(false)
const logList = ref([])
const logQuery = reactive({ action: '', auditStatus: '', userId: '' })

const loadLogs = async () => {
  logLoading.value = true
  try {
    const params = { pageNum: 1, pageSize: 100 }
    if (logQuery.action !== '') params.action = logQuery.action
    if (logQuery.auditStatus !== '') params.auditStatus = logQuery.auditStatus
    if (logQuery.userId) params.userId = logQuery.userId
    const res = await request.post('/web/pay/risk/log/list', params)
    if (res && res.code === 200) logList.value = res.data?.records || res.data?.list || []
    else logList.value = []
  } catch (e) { console.error(e); logList.value = [] } finally { logLoading.value = false }
}

// ================= ④ 人工审核 =================
const auditLoading = ref(false)
const auditList = ref([])

const loadAuditList = async () => {
  auditLoading.value = true
  try {
    const res = await request.post('/web/pay/risk/log/list', { pageNum: 1, pageSize: 100, action: 4 })
    if (res && res.code === 200) auditList.value = res.data?.records || res.data?.list || []
    else auditList.value = []
  } catch (e) { console.error(e); auditList.value = [] } finally { auditLoading.value = false }
}

const doAudit = async (row, auditStatus) => {
  try {
    const res = await request.post('/web/pay/risk/log/audit', { logNo: row.logNo, auditStatus, auditResult: auditStatus === 1 ? '人工审核通过' : '人工审核拒绝' })
    if (res && res.code === 200) {
      ElMessage.success('审核完成')
      loadAuditList()
      loadLogs()
    } else {
      ElMessage.error(res?.message || '审核失败')
    }
  } catch (e) {
    ElMessage.error('审核失败: ' + (e.message || '网络错误'))
  }
}

// ================= ⑤ 黑白名单 =================
const blackLoading = ref(false)
const blackList = ref([])
const blackQuery = reactive({ listType: 1, targetType: '', targetValue: '' })
const blackDialogVisible = ref(false)
const blackSaving = ref(false)
const blackForm = reactive({ listType: 1, targetType: 1, targetValue: '', riskLevel: 2, reason: '' })

const loadBlacklist = async () => {
  blackLoading.value = true
  try {
    const params = { pageNum: 1, pageSize: 100, listType: blackQuery.listType }
    if (blackQuery.targetType) params.targetType = blackQuery.targetType
    if (blackQuery.targetValue) params.targetValue = blackQuery.targetValue
    const res = await request.post('/web/pay/risk/blacklist/list', params)
    if (res && res.code === 200) blackList.value = res.data?.records || res.data?.list || []
    else blackList.value = []
  } catch (e) { console.error(e); blackList.value = [] } finally { blackLoading.value = false }
}

const openBlackDialog = () => {
  Object.assign(blackForm, { listType: blackQuery.listType || 1, targetType: 1, targetValue: '', riskLevel: 2, reason: '' })
  blackDialogVisible.value = true
}

const saveBlack = async () => {
  if (!blackForm.targetValue) { ElMessage.warning('请输入目标值'); return }
  blackSaving.value = true
  try {
    const res = await request.post('/web/pay/risk/blacklist/add', { ...blackForm })
    if (res && res.code === 200) {
      ElMessage.success('名单已保存')
      blackDialogVisible.value = false
      loadBlacklist()
    } else {
      ElMessage.error(res?.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败: ' + (e.message || '网络错误'))
  } finally {
    blackSaving.value = false
  }
}

const removeBlack = async (row) => {
  try { await ElMessageBox.confirm(`确认移除该${blackTargetText[row.targetType] || '名单'}记录？`, '移除确认', { type: 'warning' }) } catch { return }
  try {
    const res = await request.post('/web/pay/risk/blacklist/remove', { id: row.id })
    if (res && res.code === 200) { ElMessage.success('已移除'); loadBlacklist() }
    else ElMessage.error(res?.message || '移除失败')
  } catch (e) { ElMessage.error('移除失败: ' + (e.message || '网络错误')) }
}

// ================= 工具 =================
const yuan = (fen) => {
  if (fen === null || fen === undefined) return '0.00'
  return (Number(fen) / 100).toFixed(2)
}

// ================= 初始化 =================
onMounted(() => {
  loadRules()
  loadLogs()
  loadAuditList()
  loadBlacklist()
})
</script>

<style scoped>
.risk-center-container { padding: 16px; }

.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.title-wrap .page-title { margin: 0; font-size: 20px; font-weight: 600; display: flex; align-items: center; color: #303133; }
.title-icon { margin-right: 8px; color: #e6a23c; font-size: 22px; }
.page-subtitle { margin-top: 4px; font-size: 13px; color: #909399; }

.step-card { border-radius: 8px; }
.step-card :deep(.el-card__body) { padding: 18px 20px; }
.steps-bar { margin-bottom: 20px; padding: 8px 0; }

.step-panel { min-height: 300px; }
.panel-title { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 6px; }
.panel-desc { font-size: 13px; color: #909399; margin-bottom: 14px; }

.integration-form { max-width: 860px; }
.merchant-select { margin-bottom: 10px; }
.calc-btn { margin-top: 4px; }

.check-result { margin-top: 14px; border-radius: 8px; padding: 14px; border: 1px solid #e4e7ed; }
.check-result.pass { border-color: #67c23a; background: #f0f9eb; }
.check-result.reject { border-color: #f56c6c; background: #fef0f0; }
.result-head { display: flex; align-items: center; gap: 14px; margin-bottom: 8px; }
.result-action { font-size: 13px; color: #303133; }
.result-reason { font-size: 13px; color: #606266; margin-top: 4px; }

.panel-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 18px; padding-top: 14px; border-top: 1px solid #f0f2f5; }
</style>