<template>
  <div class="order-detail-container" v-loading="loading">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" content="订单详情" />
    
    <!-- 订单状态流程 -->
    <el-card class="status-card">
      <el-steps :active="activeStep" align-center finish-status="success">
        <el-step title="提交订单" description="订单已提交" />
        <el-step title="支付订单" description="等待支付" />
        <el-step title="商家发货" description="等待发货" />
        <el-step title="确认收货" description="配送中" />
        <el-step title="完成评价" description="订单完成" />
      </el-steps>
    </el-card>
    
    <!-- 订单基本信息 -->
    <el-card class="info-card">
      <template #header>
        <span class="card-title">订单信息</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单号">{{ orderInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(orderInfo.status)">
            {{ getStatusText(orderInfo.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ orderInfo.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ orderInfo.paymentMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ orderInfo.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ orderInfo.shipTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 收货信息 -->
    <el-card class="info-card">
      <template #header>
        <span class="card-title">收货信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="收货人">{{ orderInfo.receiverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ orderInfo.receiverPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">
          {{ orderInfo.receiverAddress || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 商品信息 -->
    <el-card class="info-card">
      <template #header>
        <span class="card-title">商品信息</span>
      </template>
      <el-table :data="orderInfo.goodsList || []" border>
        <el-table-column label="商品" min-width="300">
          <template #default="{ row }">
            <div class="goods-info">
              <el-image 
                :src="row.goodsImage || placeholderGoods" 
                style="width: 60px; height: 60px; border-radius: 4px"
                fit="cover"
              />
              <div class="goods-text">
                <div class="goods-name">{{ row.goodsName }}</div>
                <div class="goods-spec">{{ row.goodsSpec }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" align="center" />
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }">
            ¥{{ (row.price * row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>
      
      <div class="order-total">
        <div class="total-item">
          <span class="total-label">商品总额：</span>
          <span class="total-value">¥{{ orderInfo.goodsAmount || 0 }}</span>
        </div>
        <div class="total-item">
          <span class="total-label">优惠金额：</span>
          <span class="total-value discount">-¥{{ orderInfo.discountAmount || 0 }}</span>
        </div>
        <div class="total-item">
          <span class="total-label">运费：</span>
          <span class="total-value">¥{{ orderInfo.shippingFee || 0 }}</span>
        </div>
        <div class="total-item main-total">
          <span class="total-label">应付金额：</span>
          <span class="total-value">¥{{ orderInfo.totalAmount || 0 }}</span>
        </div>
      </div>
    </el-card>
    
    <!-- 物流信息 -->
    <el-card v-if="orderInfo.logisticsInfo" class="info-card">
      <template #header>
        <span class="card-title">物流信息</span>
      </template>
      <div class="logistics-header">
        <div class="logistics-item">
          <span class="label">物流公司：</span>
          <span class="value">{{ orderInfo.logisticsInfo.company }}</span>
        </div>
        <div class="logistics-item">
          <span class="label">物流单号：</span>
          <span class="value">{{ orderInfo.logisticsInfo.trackingNo }}</span>
          <el-button link type="primary" size="small" @click="copyTrackingNo">复制</el-button>
        </div>
      </div>
      <el-timeline v-if="orderInfo.logisticsInfo.traces && orderInfo.logisticsInfo.traces.length > 0">
        <el-timeline-item 
          v-for="(trace, index) in orderInfo.logisticsInfo.traces" 
          :key="index"
          :timestamp="trace.time"
          :type="index === 0 ? 'primary' : ''"
        >
          {{ trace.content }}
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无物流信息" :image-size="100" />
    </el-card>
    
    <!-- 订单备注 -->
    <el-card v-if="orderInfo.remark" class="info-card">
      <template #header>
        <span class="card-title">订单备注</span>
      </template>
      <div class="remark-content">{{ orderInfo.remark }}</div>
    </el-card>
    
    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button @click="goBack">返回列表</el-button>
      <el-button 
        v-if="orderInfo.status === 1" 
        type="success" 
        icon="Promotion"
        @click="handleShip"
      >
        发货
      </el-button>
      <el-button 
        v-if="orderInfo.status === 0" 
        type="warning" 
        icon="Close"
        @click="handleCancel"
      >
        取消订单
      </el-button>
      <el-button 
        type="primary" 
        icon="Print" 
        @click="handlePrint"
      >
        打印订单
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail } from '@/api/order/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orderInfo = ref({
  goodsList: []
})
const activeStep = ref(0)

// 获取订单详情
const fetchOrderDetail = async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.error('缺少订单ID参数')
    goBack()
    return
  }
  
  loading.value = true
  try {
    const response = await getOrderDetail(orderId)
    if (response && response.code === 1) {
      orderInfo.value = response.data || {}
      // 根据订单状态设置步骤
      updateActiveStep()
    } else {
      ElMessage.error(response.message || '获取订单详情失败')
      // 使用模拟数据
      useMockData()
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    // 使用模拟数据
    useMockData()
  } finally {
    loading.value = false
  }
}

// 使用模拟数据
const useMockData = () => {
  const orderId = route.query.orderId
  const orderNo = route.query.orderNo || 'ORD202401150001'
  
  orderInfo.value = {
    id: orderId,
    orderNo: orderNo,
    status: 2,
    createTime: '2024-01-15 10:30:00',
    paymentMethod: '微信支付',
    payTime: '2024-01-15 10:35:00',
    shipTime: '2024-01-15 14:00:00',
    receiverName: '张三',
    receiverPhone: '138****5678',
    receiverAddress: '北京市朝阳区xx路xx号xx小区xx楼xx单元',
    goodsList: [
      {
        goodsId: 1,
        goodsName: '苹果iPhone 15 Pro Max',
        goodsSpec: '深空黑色 256GB',
        goodsImage: placeholderGoods,
        price: 9999.00,
        quantity: 1
      }
    ],
    goodsAmount: 9999.00,
    discountAmount: 100.00,
    shippingFee: 10.00,
    totalAmount: 9909.00,
    logisticsInfo: {
      company: '顺丰速运',
      trackingNo: 'SF1234567890',
      traces: [
        { time: '2024-01-16 10:00:00', content: '【北京市】快件已送达，感谢使用顺丰' },
        { time: '2024-01-16 08:00:00', content: '【北京市】快件正在派送中，派送员：张师傅，电话：138****1234' },
        { time: '2024-01-15 20:00:00', content: '【北京市】快件已到达【北京朝阳区营业点】' },
        { time: '2024-01-15 14:00:00', content: '【北京市】快件已发出' }
      ]
    },
    remark: '请尽快发货，谢谢！'
  }
  updateActiveStep()
}

const updateActiveStep = () => {
  const status = orderInfo.value.status
  switch (status) {
    case 0: activeStep.value = 0; break // 待付款
    case 1: activeStep.value = 2; break // 待发货
    case 2: activeStep.value = 3; break // 已发货
    case 3: activeStep.value = 4; break // 已完成
    case 4: activeStep.value = 0; break // 已取消
    default: activeStep.value = 0
  }
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

const goBack = () => {
  router.push('/mall/order/list')
}

const handleShip = () => {
  ElMessageBox.prompt('请输入物流单号', '发货', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '请输入物流单号'
  }).then(({ value }) => {
    // TODO: 调用发货接口
    ElMessage.success('发货成功')
    orderInfo.value.status = 2
    updateActiveStep()
  }).catch(() => {
    ElMessage.info('已取消发货')
  })
}

const handleCancel = () => {
  ElMessageBox.confirm(
    '确定要取消该订单吗？',
    '确认取消',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // TODO: 调用取消订单接口
    ElMessage.success('订单已取消')
    orderInfo.value.status = 4
    updateActiveStep()
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

const handlePrint = () => {
  window.print()
}

const copyTrackingNo = () => {
  const trackingNo = orderInfo.value.logisticsInfo?.trackingNo
  if (trackingNo) {
    navigator.clipboard.writeText(trackingNo).then(() => {
      ElMessage.success('物流单号已复制')
    }).catch(() => {
      ElMessage.error('复制失败，请手动复制')
    })
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.status-card, .info-card {
  margin-bottom: 20px;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.goods-text {
  flex: 1;
}

.goods-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.goods-spec {
  color: #909399;
  font-size: 12px;
}

.order-total {
  text-align: right;
  padding: 20px;
  border-top: 2px solid #EBEEF5;
  margin-top: 20px;
  background-color: #f5f7fa;
}

.total-item {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.total-label {
  margin-right: 20px;
  color: #606266;
}

.total-value {
  min-width: 100px;
  font-weight: 500;
  color: #303133;
}

.total-value.discount {
  color: #67c23a;
}

.main-total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #DCDFE6;
}

.main-total .total-label {
  font-size: 16px;
  font-weight: bold;
}

.main-total .total-value {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.logistics-header {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.logistics-item {
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.logistics-item:last-child {
  margin-bottom: 0;
}

.logistics-item .label {
  color: #606266;
  margin-right: 8px;
}

.logistics-item .value {
  color: #303133;
  font-weight: 500;
  margin-right: 8px;
}

.remark-content {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0;
  margin-top: 20px;
  border-top: 1px solid #EBEEF5;
}

/* 打印样式 */
@media print {
  .action-buttons {
    display: none;
  }
  
  .el-page-header {
    display: none;
  }
}
</style>
