<template>
  <div class="reg-status-container">
    <el-card shadow="never">
      <template #header>
        <span class="card-title">入网审核状态查询</span>
      </template>
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="商户号 mchNo">
          <el-input v-model="mchNo" placeholder="如 E9740..." clearable style="width: 240px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="query">查询</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="!result && !loading"
        type="info"
        :closable="false"
        title="输入平台商户号查询入网→审核状态（自研 merchant/detail）。通企付 regStatus 需密钥，未配置时不调用。"
        style="margin-bottom: 12px;"
      />

      <el-descriptions v-if="result" :column="2" border>
        <el-descriptions-item label="商户号">{{ result.merchant?.mchNo || result.mchNo }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ result.merchant?.companyName || result.companyName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ mchTypeText[result.merchant?.mchType ?? result.mchType] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType[result.merchant?.status ?? result.status] || 'info'">
            {{ statusText[result.merchant?.status ?? result.status] ?? (result.merchant?.status ?? result.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ result.merchant?.createTime || result.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ result.merchant?.approvedTime || result.approvedTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态备注" :span="2">{{ result.merchant?.statusRemark || result.statusRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { merchantPlatformApi } from '@/api/payment/paymentBizW4'
import { isSuccessCode } from '@/utils/errorCode'

const mchNo = ref('')
const loading = ref(false)
const result = ref(null)

const statusText = { 0: '待审核', 1: '审核中', 2: '已生效', 3: '已冻结', 4: '已注销', 5: '审核失败' }
const statusTagType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'info', 5: 'danger' }
const mchTypeText = { 1: '企业', 2: '个体工商户', 3: '个人' }

const query = async () => {
  if (!mchNo.value) {
    ElMessage.warning('请输入商户号')
    return
  }
  loading.value = true
  result.value = null
  try {
    const res = await merchantPlatformApi.detail({ mchNo: mchNo.value.trim() })
    if (res && isSuccessCode(res.code) && res.data) {
      result.value = res.data
    } else {
      ElMessage.error(res?.message || '未查到该商户')
    }
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reg-status-container { padding: 16px; }
.card-title { font-size: 15px; font-weight: 600; }
</style>
