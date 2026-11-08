<template>
  <div class="order-detail-page">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <div>
            <el-button @click="goBack">返回</el-button>
            <el-button 
              type="primary" 
              @click="handleShip" 
              v-if="orderInfo && orderInfo.orderStatus === ORDER_STATUS.WAIT_SHIP"
            >
              发货
            </el-button>
            <el-button 
              type="success" 
              @click="handleConfirm" 
              v-if="orderInfo && orderInfo.orderStatus === ORDER_STATUS.SHIPPED"
            >
              确认收货
            </el-button>
            <el-button 
              type="danger" 
              @click="handleCancel" 
              v-if="orderInfo && orderInfo.orderStatus === ORDER_STATUS.PENDING"
            >
              取消订单
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="orderInfo" class="order-info">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ orderInfo.orderId }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusTagType(orderInfo.orderStatus)">
              {{ getStatusText(orderInfo.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="买家姓名">{{ orderInfo.buyerUserName }}</el-descriptions-item>
          <el-descriptions-item label="买家ID">{{ orderInfo.buyerUserId }}</el-descriptions-item>
          <el-descriptions-item label="店铺名称">{{ orderInfo.shopName }}</el-descriptions-item>
          <el-descriptions-item label="店铺ID">{{ orderInfo.shopId }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ orderInfo.orderReceiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ orderInfo.orderReceiverContact }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ orderInfo.orderReceiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ orderInfo.paymentName }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDate(orderInfo.paymentTime) }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(orderInfo.orderCreateTime) }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ formatDate(orderInfo.orderShippingTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDate(orderInfo.orderFinishedTime) }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ orderInfo.orderShippingCode || '暂无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 金额信息 -->
        <el-descriptions title="金额信息" :column="4" border class="amount-info">
          <el-descriptions-item label="商品总额">¥{{ formatAmount(orderInfo.orderGoodsAmount) }}</el-descriptions-item>
          <el-descriptions-item label="运费">¥{{ formatAmount(orderInfo.orderShippingFee) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ formatAmount(orderInfo.orderDiscountFee) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span class="total-amount">¥{{ formatAmount(orderInfo.orderPaymentAmount) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 订单留言 -->
        <el-descriptions 
          title="订单留言" 
          :column="1" 
          border 
          class="message-info" 
          v-if="orderInfo.orderMessage"
        >
          <el-descriptions-item label="留言内容">
            {{ orderInfo.orderMessage }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 商品信息 -->
        <div class="goods-info">
          <h3>商品信息</h3>
          <el-table :data="orderGoodsList" border>
            <el-table-column prop="goodsName" label="商品名称" />
            <el-table-column prop="orderSpecChineseValue" label="规格" />
            <el-table-column prop="goodsPrice" label="单价" width="120">
              <template #default="scope">
                ¥{{ formatAmount(scope.row.goodsPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="orderGoodsNum" label="数量" width="80" />
            <el-table-column prop="orderGoodsAmount" label="小计" width="120">
              <template #default="scope">
                ¥{{ formatAmount(scope.row.orderGoodsAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="orderGoodsStatus" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getGoodsStatusTagType(scope.row.orderGoodsStatus)">
                  {{ getGoodsStatusText(scope.row.orderGoodsStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 操作日志 -->
        <div class="operation-log" v-if="operationLogs.length > 0">
          <h3>操作日志</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in operationLogs"
              :key="index"
              :timestamp="formatDate(log.createTime)"
              placement="top"
            >
              <el-card>
                <h4>{{ log.operator }}</h4>
                <p>{{ log.description }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <div v-else class="empty">
        <el-empty description="暂无订单信息" />
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, ORDER_STATUS, ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/api/order/order'

// 路由相关
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const orderInfo = ref(null)
const orderGoodsList = ref([])
const operationLogs = ref([])

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.query.orderId
    if (!orderId) {
      ElMessage.error('订单ID不能为空')
      return
    }

    // 获取订单基本信息
    const orderResponse = await orderApi.getOrderDetail(orderId)
    if (orderResponse.code === 200 || orderResponse.success) {
      orderInfo.value = orderResponse.data || orderResponse
    } else {
      ElMessage.error(orderResponse.message || '获取订单详情失败')
      return
    }

    // 获取订单商品信息
    // 这里假设有一个获取订单商品列表的接口
    // const goodsResponse = await orderApi.getOrderGoodsList({ orderId })
    // if (goodsResponse.code === 200 || goodsResponse.success) {
    //   orderGoodsList.value = goodsResponse.data || goodsResponse
    // } else {
    //   ElMessage.error(goodsResponse.message || '获取订单商品失败')
    // }

    // 模拟订单商品数据
    orderGoodsList.value = [
      {
        goodsName: 'iPhone 15 Pro',
        orderSpecChineseValue: '256GB, 黑色',
        goodsPrice: 8999,
        orderGoodsNum: 1,
        orderGoodsAmount: 8999,
        orderGoodsStatus: 1
      },
      {
        goodsName: 'AirPods Pro',
        orderSpecChineseValue: '第二代',
        goodsPrice: 1999,
        orderGoodsNum: 1,
        orderGoodsAmount: 1999,
        orderGoodsStatus: 1
      }
    ]

    // 模拟操作日志
    operationLogs.value = [
      {
        operator: '系统',
        description: '订单创建',
        createTime: orderInfo.value.orderCreateTime
      },
      {
        operator: '买家',
        description: '完成支付',
        createTime: orderInfo.value.paymentTime
      }
    ]
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return ORDER_STATUS_TAG_TYPE[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  return ORDER_STATUS_TEXT[status] || '未知状态'
}

// 获取商品状态标签类型
const getGoodsStatusTagType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'primary'
  }
  return statusMap[status] || 'info'
}

// 获取商品状态文本
const getGoodsStatusText = (status) => {
  const statusMap = {
    0: '未发货',
    1: '已发货'
  }
  return statusMap[status] || '未知'
}

// 格式化金额
const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 发货
const handleShip = async () => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const response = await orderApi.shipOrder({ orderId: orderInfo.value.orderId })
    if (response.code === 200 || response.success) {
      ElMessage.success('发货成功')
      fetchOrderDetail() // 刷新详情
    } else {
      ElMessage.error(response.message || '发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败: ' + (error.message || '网络错误'))
    }
  }
}

// 确认收货
const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确定要确认收货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const response = await orderApi.confirmOrder({ orderId: orderInfo.value.orderId })
    if (response.code === 200 || response.success) {
      ElMessage.success('确认收货成功')
      fetchOrderDetail() // 刷新详情
    } else {
      ElMessage.error(response.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败: ' + (error.message || '网络错误'))
    }
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await orderApi.cancelOrder({ 
      orderId: orderInfo.value.orderId, 
      orderCancelReason: '管理员取消'
    })
    if (response.code === 200 || response.success) {
      ElMessage.success('订单已取消')
      fetchOrderDetail() // 刷新详情
    } else {
      ElMessage.error(response.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败: ' + (error.message || '网络错误'))
    }
  }
}

// 生命周期
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  padding: 20px 0;
}

.amount-info {
  margin: 20px 0;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.message-info {
  margin: 20px 0;
}

.goods-info {
  margin: 20px 0;
}

.goods-info h3 {
  margin-bottom: 15px;
  color: #303133;
}

.operation-log {
  margin: 20px 0;
}

.operation-log h3 {
  margin-bottom: 15px;
  color: #303133;
}

.empty {
  text-align: center;
  padding: 50px 0;
}
</style>
