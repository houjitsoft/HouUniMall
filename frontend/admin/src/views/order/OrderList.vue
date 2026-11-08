<template>
  <div class="order-list-container">
    <el-card class="order-list-card">
      <template #header>
        <div class="card-header">
          <span>{{ t('menu.orderList') }}</span>
          <el-button type="primary" @click="handleCreateOrder">{{ t('menu.createOrder') }}</el-button>
        </div>
      </template>
      
      <el-table :data="orderList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" :label="t('order.orderNo')" width="180" />
        <el-table-column prop="customer" :label="t('order.customer')" width="120" />
        <el-table-column prop="amount" :label="t('order.amount')" width="120" />
        <el-table-column prop="status" :label="t('order.status')" width="120" />
        <el-table-column :label="t('actions.title')" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">{{ t('actions.view') }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">{{ t('actions.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const orderList = ref([
  {
    id: 1,
    orderNo: 'ORD20230001',
    customer: '张三',
    amount: 999,
    status: '已支付'
  },
  {
    id: 2,
    orderNo: 'ORD20230002',
    customer: '李四',
    amount: 299,
    status: '待支付'
  }
])

const handleCreateOrder = () => {
  router.push('/orders/create')
}

const handleView = (row) => {
  router.push(`/orders/view/${row.id}`)
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `${this.t('messages.confirmDelete')} ${row.orderNo}?`,
    this.t('messages.warning'),
    {
      confirmButtonText: this.t('actions.confirm'),
      cancelButtonText: this.t('actions.cancel'),
      type: 'warning'
    }
  ).then(() => {
    // 执行删除操作
    ElMessage({
      type: 'success',
      message: this.t('messages.deleteSuccess')
    })
  }).catch(() => {
    ElMessage({
      type: 'info',
      message: this.t('messages.deleteCanceled')
    })
  })
}
</script>

<style scoped>
.order-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
