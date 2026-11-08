<template>
  <div class="return-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>退换货管理</span>
          <el-button type="primary" @click="handleExport">导出记录</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderId" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="买家姓名">
          <el-input v-model="searchForm.buyerUserName" placeholder="请输入买家姓名" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.returnType" placeholder="请选择类型" clearable>
            <el-option label="退款" value="1" />
            <el-option label="退货" value="2" />
            <el-option label="换货" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.returnState" placeholder="请选择状态" clearable>
            <el-option label="待审核" value="1" />
            <el-option label="审核通过" value="2" />
            <el-option label="审核不通过" value="3" />
            <el-option label="已收货" value="4" />
            <el-option label="已完成" value="5" />
            <el-option label="已关闭" value="8" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 退换货列表 -->
      <el-table :data="returnList" style="width: 100%" v-loading="loading">
        <el-table-column prop="returnCode" label="售后单号" width="180" />
        <el-table-column prop="orderId" label="订单号" width="180" />
        <el-table-column prop="buyerUserAccount" label="买家账号" width="120" />
        <el-table-column prop="buyerUserName" label="买家姓名" width="120" />
        <el-table-column prop="returnType" label="类型" width="80">
          <template #default="scope">
            {{ getReturnTypeText(scope.row.returnType) }}
          </template>
        </el-table-column>
        <el-table-column prop="returnReason" label="原因" width="150" />
        <el-table-column prop="returnCash" label="退款金额" width="100">
          <template #default="scope">
            ¥{{ formatAmount(scope.row.returnCash) }}
          </template>
        </el-table-column>
        <el-table-column prop="returnState" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getReturnStateTagType(scope.row.returnState)">
              {{ getReturnStateText(scope.row.returnState) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returnAddTime" label="申请时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.returnAddTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button 
              size="small" 
              type="primary" 
              @click="handleProcess(scope.row)" 
              v-if="scope.row.returnState === 1"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 退换货详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="退换货详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentReturn" class="return-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="售后单号">{{ currentReturn.returnCode }}</el-descriptions-item>
          <el-descriptions-item label="订单号">{{ currentReturn.orderId }}</el-descriptions-item>
          <el-descriptions-item label="买家账号">{{ currentReturn.buyerUserAccount }}</el-descriptions-item>
          <el-descriptions-item label="买家姓名">{{ currentReturn.buyerUserName }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ getReturnTypeText(currentReturn.returnType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getReturnStateTagType(currentReturn.returnState)">
              {{ getReturnStateText(currentReturn.returnState) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ formatAmount(currentReturn.returnCash) }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(currentReturn.returnAddTime) }}</el-descriptions-item>
          <el-descriptions-item label="退货原因" :span="2">{{ currentReturn.returnReason }}</el-descriptions-item>
          <el-descriptions-item label="退货备注" :span="2">{{ currentReturn.returnMessage || '无' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentReturn.returnRealName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentReturn.returnMobile }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentReturn.returnAddr }}</el-descriptions-item>
        </el-descriptions>

        <div class="return-goods" v-if="returnGoodsList.length > 0">
          <h4>退货商品</h4>
          <el-table :data="returnGoodsList" border>
            <el-table-column prop="goodsName" label="商品名称" />
            <el-table-column prop="orderSpecChineseValue" label="规格" />
            <el-table-column prop="goodsPrice" label="单价" width="100">
              <template #default="scope">
                ¥{{ formatAmount(scope.row.goodsPrice) }}
              </template>
            </el-table-column>
            <el-table-column prop="orderGoodsNum" label="数量" width="80" />
            <el-table-column prop="orderGoodsAmount" label="小计" width="100">
              <template #default="scope">
                ¥{{ formatAmount(scope.row.orderGoodsAmount) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="return-voucher" v-if="currentReturn.returnVoucher">
          <h4>退货凭证</h4>
          <el-image 
            v-for="(img, index) in currentReturn.returnVoucher.split(',')" 
            :key="index"
            :src="img" 
            :preview-src-list="currentReturn.returnVoucher.split(',')"
            class="voucher-image"
            fit="cover"
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 处理退换货对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="处理退换货申请"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="processForm" 
        :rules="processRules" 
        ref="processFormRef" 
        label-width="120px"
      >
        <el-form-item label="处理结果" prop="result">
          <el-radio-group v-model="processForm.result">
            <el-radio :label="2">审核通过</el-radio>
            <el-radio :label="3">审核不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          label="处理备注" 
          prop="message"
          v-if="processForm.result === 3"
        >
          <el-input 
            v-model="processForm.message" 
            type="textarea" 
            placeholder="请输入审核不通过的原因"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../../api/order/order'

// 响应式数据
const loading = ref(false)
const exportLoading = ref(false)
const dialogVisible = ref(false)
const processDialogVisible = ref(false)
const currentReturn = ref(null)
const returnGoodsList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  orderId: '',
  buyerUserName: '',
  returnType: '',
  returnState: '',
  pageNum: 1,
  pageSize: 10
})

// 退换货列表
const returnList = ref([])

// 处理表单
const processForm = reactive({
  result: 2,
  message: ''
})

const processRules = {
  message: [
    { required: true, message: '请输入审核不通过的原因', trigger: 'blur' }
  ]
}

// 表单引用
const processFormRef = ref(null)

// 生命周期
onMounted(() => {
  fetchReturnList()
})

// 获取退换货列表
const fetchReturnList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await orderApi.getReturnList(params)
    
    if (response.code === 200 || response.success) {
      const data = response.data || response
      returnList.value = data.records || data.list || []
      total.value = data.total || 0
    } else {
      ElMessage.error(response.message || '获取退换货列表失败')
      returnList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取退换货列表失败:', error)
    ElMessage.error('获取退换货列表失败: ' + (error.message || '网络错误'))
    returnList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取退货类型文本
const getReturnTypeText = (type) => {
  const typeMap = {
    1: '退款',
    2: '退货',
    3: '换货'
  }
  return typeMap[type] || '未知'
}

// 获取退货状态标签类型
const getReturnStateTagType = (state) => {
  const stateMap = {
    1: 'warning',
    2: 'primary',
    3: 'danger',
    4: 'info',
    5: 'success',
    8: 'info'
  }
  return stateMap[state] || 'info'
}

// 获取退货状态文本
const getReturnStateText = (state) => {
  const stateMap = {
    1: '待审核',
    2: '审核通过',
    3: '审核不通过',
    4: '已收货',
    5: '已完成',
    8: '已关闭'
  }
  return stateMap[state] || '未知'
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

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchReturnList()
}

// 重置搜索
const handleReset = () => {
  Object.assign(searchForm, {
    orderId: '',
    buyerUserName: '',
    returnType: '',
    returnState: '',
    pageNum: 1,
    pageSize: 10
  })
  currentPage.value = 1
  fetchReturnList()
}

// 查看退换货详情
const handleView = async (row) => {
  try {
    // 这里应该调用获取退换货详情的接口
    // 暂时使用行数据作为详情
    currentReturn.value = row
    
    // 模拟退货商品数据
    returnGoodsList.value = [
      {
        goodsName: 'iPhone 15 Pro',
        orderSpecChineseValue: '256GB, 黑色',
        goodsPrice: 8999,
        orderGoodsNum: 1,
        orderGoodsAmount: 8999
      }
    ]
    
    dialogVisible.value = true
  } catch (error) {
    console.error('获取退换货详情失败:', error)
    ElMessage.error('获取退换货详情失败: ' + (error.message || '网络错误'))
  }
}

// 处理退换货
const handleProcess = (row) => {
  currentReturn.value = row
  processForm.result = 2
  processForm.message = ''
  processDialogVisible.value = true
}

// 提交处理结果
const submitProcess = async () => {
  if (!processFormRef.value) return
  
  await processFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const params = {
          returnCode: currentReturn.value.returnCode,
          returnState: processForm.result,
          returnShopMessage: processForm.message
        }
        
        const response = await orderApi.handleReturn(params)
        if (response.code === 200 || response.success) {
          ElMessage.success('处理成功')
          processDialogVisible.value = false
          fetchReturnList() // 刷新列表
        } else {
          ElMessage.error(response.message || '处理失败')
        }
      } catch (error) {
        console.error('处理失败:', error)
        ElMessage.error('处理失败: ' + (error.message || '网络错误'))
      }
    }
  })
}

// 导出记录
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  searchForm.pageSize = val
  fetchReturnList()
}

// 当前页改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  searchForm.pageNum = val
  fetchReturnList()
}
</script>

<style scoped>
.return-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.return-detail {
  padding: 20px 0;
}

.return-goods {
  margin: 20px 0;
}

.return-goods h4 {
  margin-bottom: 15px;
  color: #303133;
}

.return-voucher {
  margin: 20px 0;
}

.return-voucher h4 {
  margin-bottom: 15px;
  color: #303133;
}

.voucher-image {
  width: 100px;
  height: 100px;
  margin-right: 10px;
  border-radius: 4px;
}

.dialog-footer {
  text-align: right;
}
</style>
