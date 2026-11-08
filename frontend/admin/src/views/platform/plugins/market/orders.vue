<template>
  <div class="orders-page">
    <header class="page-header">
      <div>
        <h1>我的订单</h1>
        <p>插件购买记录：待支付可继续付款或取消，已支付表示授权已自动签发</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="goCheckout">购买插件</el-button>
        <el-button :loading="loading" @click="loadOrders">刷新</el-button>
      </div>
    </header>

    <el-table v-loading="loading" :data="orders" border stripe empty-text="暂无订单">
      <el-table-column prop="orderNo" label="订单号" min-width="200" />
      <el-table-column prop="pluginCode" label="插件" min-width="160" />
      <el-table-column prop="licenseType" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.licenseType)">{{ typeLabel(row.licenseType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="110">
        <template #default="{ row }">¥{{ Number(row.amount ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTag(row.status)">{{ row.statusText }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="licenseKey" label="自动签发授权码" min-width="220">
        <template #default="{ row }">
          <span v-if="row.licenseKey">{{ row.licenseKey }}</span>
          <span v-else class="muted">未支付</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" min-width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" link type="primary" @click="goInstall(row.pluginCode)">去安装</el-button>
          <el-button v-else-if="row.status === 0" link type="primary" @click="continuePay(row)">继续支付</el-button>
          <el-button v-if="row.status === 0" link type="danger" @click="handleCancel(row)">取消</el-button>
          <el-button v-else link type="primary" @click="syncRow(row)">刷新状态</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelPluginOrder, getMyOrders, syncPluginOrder } from '@/api/plugin-center/pluginMarket'
import { getSiteInstanceId } from '@/utils/site'

const router = useRouter()
const siteInstanceId = getSiteInstanceId()

const loading = ref(false)
const orders = ref([])

const goCheckout = () => router.push('/platform/plugins/market/checkout')
const goInstall = (code) => router.push(`/platform/plugins/market/detail?code=${encodeURIComponent(code)}`)

const loadOrders = async () => {
  loading.value = true
  try {
    const data = await getMyOrders(siteInstanceId)
    orders.value = Array.isArray(data) ? data : []
  } catch (error) {
    ElMessage.error(error?.message || '加载订单失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}

const continuePay = (row) => {
  router.push(`/platform/plugins/market/checkout?code=${encodeURIComponent(row.pluginCode)}`)
}

const syncRow = async (row) => {
  try {
    await syncPluginOrder(row.orderNo)
    ElMessage.success('已刷新支付状态')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error?.message || '刷新失败')
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该待支付订单吗？', '取消订单', {
      type: 'warning',
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想'
    })
  } catch {
    return
  }
  try {
    await cancelPluginOrder(row.orderNo)
    ElMessage.success('订单已取消')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error?.message || '取消失败')
  }
}

const typeLabel = (type) => ({ FREE: '免费', PAID: '付费', TRIAL: '试用' }[String(type).toUpperCase()] || type || '—')
const typeTag = (type) => ({ FREE: 'info', PAID: 'success', TRIAL: 'warning' }[String(type).toUpperCase()] || 'info')
const statusTag = (status) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[status] || 'info')

onMounted(loadOrders)
</script>

<style scoped>
.orders-page {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 20px;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.muted {
  color: #c0c4cc;
}
</style>
