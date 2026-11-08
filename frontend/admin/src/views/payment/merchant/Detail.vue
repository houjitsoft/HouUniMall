<template>
  <div class="merchant-detail-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商户详情</span>
          <el-button size="small" @click="$router.back()">返回列表</el-button>
        </div>
      </template>
      <el-form :inline="true" style="margin-bottom:16px">
        <el-form-item label="商户号">
          <el-input v-model="queryMchNo" placeholder="输入商户号" style="width:200px" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadDetail">查询</el-button></el-form-item>
      </el-form>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="商户号">{{ detail.mchNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商户名称">{{ detail.mchName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商户类型">{{ detail.mchType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 1 ? 'success' : detail.status === 2 ? 'danger' : 'info'" size="small">
            {{ detail.status === 1 ? '正常' : detail.status === 2 ? '冻结' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="营业执照号" :span="2">{{ detail.licenseNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经营地址" :span="2">{{ detail.address || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="请输入商户号查询" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const queryMchNo = ref('')
const detail = ref(null)

const loadDetail = async () => {
  if (!queryMchNo.value) { ElMessage.warning('请输入商户号'); return }
  try {
    const res = await request.post('/web/pay/merchant/detail', { mchNo: queryMchNo.value })
    if (res.code === 200 && res.data) { detail.value = res.data } else { ElMessage.warning(res.message || '未找到商户'); detail.value = null }
  } catch (e) { ElMessage.error('查询失败'); detail.value = null }
}
</script>

<style scoped>
.merchant-detail-page { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
