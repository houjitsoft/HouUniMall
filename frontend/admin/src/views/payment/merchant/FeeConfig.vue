<template>
  <div class="fee-config-page">
    <el-card shadow="never">
      <template #header><span>商户费率配置</span></template>
      <el-form :inline="true" style="margin-bottom:12px">
        <el-form-item label="商户号"><el-input v-model="mchNo" placeholder="输入商户号" style="width:200px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadDetail">查询</el-button></el-form-item>
      </el-form>
      <el-form v-if="loaded" :model="form" label-width="140px" style="max-width:600px">
        <el-form-item label="微信支付费率(‰)"><el-input-number v-model="form.wxRate" :min="0" :max="100" :precision="1" /></el-form-item>
        <el-form-item label="支付宝费率(‰)"><el-input-number v-model="form.aliRate" :min="0" :max="100" :precision="1" /></el-form-item>
        <el-form-item label="银联费率(‰)"><el-input-number v-model="form.unionRate" :min="0" :max="100" :precision="1" /></el-form-item>
        <el-form-item label="云闪付费率(‰)"><el-input-number v-model="form.ysfRate" :min="0" :max="100" :precision="1" /></el-form-item>
        <el-form-item label="结算周期">
          <el-select v-model="form.settleCycle" style="width:200px">
            <el-option label="T+0（实时）" value="T0" /><el-option label="T+1（次日）" value="T1" /><el-option label="D+0（工作日）" value="D0" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave">保存费率</el-button></el-form-item>
      </el-form>
      <el-empty v-else description="请输入商户号查询费率配置" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const mchNo = ref('')
const loaded = ref(false)
const form = reactive({ wxRate: 6, aliRate: 6, unionRate: 5, ysfRate: 3.8, settleCycle: 'T1' })

const loadDetail = async () => {
  if (!mchNo.value) { ElMessage.warning('请输入商户号'); return }
  try {
    const res = await request.post('/web/pay/merchant/detail', { mchNo: mchNo.value })
    if (res.code === 200 && res.data) {
      const d = res.data
      form.wxRate = d.wxRate || 6; form.aliRate = d.aliRate || 6
      form.unionRate = d.unionRate || 5; form.ysfRate = d.ysfRate || 3.8
      form.settleCycle = d.settleCycle || 'T1'
      loaded.value = true
    } else { ElMessage.warning('未找到商户'); loaded.value = false }
  } catch (e) { ElMessage.error('查询失败') }
}

const handleSave = async () => {
  const res = await request.post('/web/pay/merchant/configFee', { mchNo: mchNo.value, ...form })
  if (res.code === 200) { ElMessage.success('费率配置保存成功') } else { ElMessage.error(res.message || '保存失败') }
}
</script>

<style scoped>
.fee-config-page { padding: 16px; }
</style>
