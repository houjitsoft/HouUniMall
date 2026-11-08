<template>
  <div class="ums-page">
    <el-row :gutter="16">
      <el-col :md="12" :xs="24">
        <el-card shadow="never">
          <template #header><span class="title">企业订单划付 (202104)</span></template>
          <el-form :model="transferForm" label-width="100px">
            <el-form-item label="划付金额(元)" required>
              <el-input-number v-model="transferForm.payAmtYuan" :min="0.01" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="附言">
              <el-input v-model="transferForm.ps" maxlength="150" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="transferLoading" @click="doTransfer">发起划付</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :md="12" :xs="24">
        <el-card shadow="never">
          <template #header><span class="title">企业订单分账 (202105)</span></template>
          <el-form :model="splitForm" label-width="100px">
            <el-form-item label="分账金额(元)" required>
              <el-input-number v-model="splitForm.payAmtYuan" :min="0.01" :precision="2" style="width: 100%" />
            </el-form-item>
            <el-form-item label="收款卡号" required>
              <el-input v-model="splitForm.cardNo" placeholder="明文卡号将做 SHA-256；也可直接填 64 位摘要" />
            </el-form-item>
            <el-form-item label="分账附言" required>
              <el-input v-model="splitForm.ps" maxlength="150" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="splitLoading" @click="doSplit">发起分账</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
    <el-alert
      class="tip"
      type="info"
      :closable="false"
      title="说明"
      description="划付结算到签约预留账户；分账可指定预先在银商维护的结算账户，单笔分账比例受银商规则限制（其他账户通常不超过 30%）。"
    />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { umsCorpApi } from '@/api/ums-corp-order'

const transferLoading = ref(false)
const splitLoading = ref(false)
const transferForm = reactive({ payAmtYuan: 0.01, ps: '' })
const splitForm = reactive({ payAmtYuan: 0.01, cardNo: '', ps: '' })

async function doTransfer() {
  transferLoading.value = true
  try {
    const res = await umsCorpApi.transfer({
      payAmt: Math.round(Number(transferForm.payAmtYuan) * 100),
      ps: transferForm.ps || undefined
    })
    if (res?.code != null && ![0,1,200].includes(Number(res.code))) {
      ElMessage.error(res.message || '划付失败')
      return
    }
    ElMessage.success(`划付已提交：${res?.data?.respMsg || res?.data?.respCode || 'OK'}`)
  } catch (e) {
    ElMessage.error(e?.message || '划付失败')
  } finally {
    transferLoading.value = false
  }
}

async function doSplit() {
  splitLoading.value = true
  try {
    const res = await umsCorpApi.split({
      payAmt: Math.round(Number(splitForm.payAmtYuan) * 100),
      cardNo: splitForm.cardNo,
      ps: splitForm.ps,
      payType: '0'
    })
    if (res?.code != null && ![0,1,200].includes(Number(res.code))) {
      ElMessage.error(res.message || '分账失败')
      return
    }
    ElMessage.success(`分账已提交：${res?.data?.respMsg || res?.data?.respCode || 'OK'}`)
  } catch (e) {
    ElMessage.error(e?.message || '分账失败')
  } finally {
    splitLoading.value = false
  }
}
</script>

<style scoped>
.ums-page { padding: 16px; }
.title { font-weight: 600; }
.tip { margin-top: 16px; }
</style>
