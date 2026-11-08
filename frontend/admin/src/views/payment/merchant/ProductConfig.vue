<template>
  <div class="product-config-page">
    <el-card shadow="never">
      <template #header><span>商户产品开通</span></template>
      <el-form :inline="true" style="margin-bottom:12px">
        <el-form-item label="商户号"><el-input v-model="mchNo" placeholder="输入商户号" style="width:200px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="loadProducts">查询</el-button></el-form-item>
      </el-form>
      <el-table v-if="loaded" :data="products" stripe>
        <el-table-column prop="productCode" label="产品编码" width="140" />
        <el-table-column prop="productName" label="产品名称" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '已开通' : '未开通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link :type="row.status === 1 ? 'warning' : 'primary'" size="small" @click="toggleProduct(row)">
              {{ row.status === 1 ? '停用' : '开通' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="请输入商户号查询产品开通情况" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const mchNo = ref('')
const loaded = ref(false)
const products = ref([
  { productCode: 'WXPAY', productName: '微信支付', status: 0 },
  { productCode: 'ALIPAY', productName: '支付宝', status: 0 },
  { productCode: 'UNIONPAY', productName: '银联支付', status: 0 },
  { productCode: 'YSF', productName: '云闪付', status: 0 },
  { productCode: 'QUICK', productName: '银行卡快捷', status: 0 },
  { productCode: 'BALANCE', productName: '余额支付', status: 0 }
])

const loadProducts = async () => {
  if (!mchNo.value) { ElMessage.warning('请输入商户号'); return }
  try {
    const res = await request.post('/web/pay/merchant/detail', { mchNo: mchNo.value })
    if (res.code === 200 && res.data) {
      // 如果后端返回了产品列表则用后端数据，否则用默认
      if (res.data.products) { products.value = res.data.products }
      loaded.value = true
    } else { ElMessage.warning('未找到商户') }
  } catch (e) { ElMessage.error('查询失败') }
}

const toggleProduct = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await request.post('/web/pay/merchant/openProduct', { mchNo: mchNo.value, productCode: row.productCode, status: newStatus })
  if (res.code === 200) { row.status = newStatus; ElMessage.success(newStatus === 1 ? '开通成功' : '停用成功') }
  else { ElMessage.error(res.message || '操作失败') }
}
</script>

<style scoped>
.product-config-page { padding: 16px; }
</style>
