<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h3 style="margin: 0 0 8px 0">入库管理</h3>
          <el-text type="info">管理商品入库记录</el-text>
        </div>
        <el-button type="primary" icon="Plus" @click="handleCreate">创建入库单</el-button>
      </div>
    </el-card>

    <!-- 筛选条件 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="入库单号">
          <el-input v-model="searchForm.inboundNo" placeholder="请输入入库单号" clearable />
        </el-form-item>
        <el-form-item label="入库类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="采购入库" value="purchase" />
            <el-option label="调拨入库" value="transfer" />
            <el-option label="退货入库" value="return" />
            <el-option label="其他入库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已审核" value="approved" />
            <el-option label="已入库" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 入库单列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">入库单列表</span>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="inboundNo" label="入库单号" width="180" />
        <el-table-column label="入库类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'purchase'" type="primary">采购入库</el-tag>
            <el-tag v-else-if="row.type === 'transfer'">调拨入库</el-tag>
            <el-tag v-else-if="row.type === 'return'" type="warning">退货入库</el-tag>
            <el-tag v-else type="info">其他入库</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入库状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 'approved'" type="primary">已审核</el-tag>
            <el-tag v-else-if="row.status === 'completed'" type="success">已入库</el-tag>
            <el-tag v-else type="info">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="150" />
        <el-table-column prop="goodsCount" label="商品数量" width="120" align="right" />
        <el-table-column prop="totalQuantity" label="入库总数" width="120" align="right" />
        <el-table-column prop="totalAmount" label="入库金额" width="140" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="success" 
              size="small" 
              @click="handleApprove(row)"
            >
              审核
            </el-button>
            <el-button 
              v-if="row.status === 'approved'" 
              link 
              type="primary" 
              size="small" 
              @click="handleInbound(row)"
            >
              确认入库
            </el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              link 
              type="danger" 
              size="small" 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  inboundNo: '',
  type: '',
  status: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const loading = ref(false)
const tableData = ref([])

// 获取列表数据
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟数据
    tableData.value = [
      {
        inboundNo: 'IN20240112001',
        type: 'purchase',
        status: 'completed',
        supplier: '华为供应商',
        goodsCount: 8,
        totalQuantity: 50,
        totalAmount: '325000.00',
        operator: '张三',
        createTime: '2024-01-12 09:00:00',
        remark: '新品采购入库'
      },
      {
        inboundNo: 'IN20240112002',
        type: 'transfer',
        status: 'approved',
        supplier: '上海仓库',
        goodsCount: 5,
        totalQuantity: 30,
        totalAmount: '156000.00',
        operator: '李四',
        createTime: '2024-01-12 11:30:00',
        remark: '从上海仓库调拨'
      },
      {
        inboundNo: 'IN20240112003',
        type: 'return',
        status: 'pending',
        supplier: '客户退货',
        goodsCount: 2,
        totalQuantity: 5,
        totalAmount: '8500.00',
        operator: '王五',
        createTime: '2024-01-12 15:20:00',
        remark: '客户退货入库'
      }
    ]
    pagination.total = 56
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    inboundNo: '',
    type: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

// 导出
const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

// 创建入库单
const handleCreate = () => {
  ElMessage.info('创建入库单功能开发中')
}

// 查看详情
const handleView = (row) => {
  ElMessage.info(`查看入库单详情：${row.inboundNo}`)
}

// 审核
const handleApprove = (row) => {
  ElMessageBox.confirm(
    `确定要审核通过入库单"${row.inboundNo}"吗？`,
    '确认审核',
    {
      confirmButtonText: '通过',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('审核通过')
    fetchData()
  })
}

// 确认入库
const handleInbound = (row) => {
  ElMessageBox.confirm(
    `确定要确认入库"${row.inboundNo}"吗？确认后将增加库存。`,
    '确认入库',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('入库成功，库存已增加')
    fetchData()
  })
}

// 取消
const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定要取消入库单"${row.inboundNo}"吗？`,
    '确认取消',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('入库单已取消')
    fetchData()
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-weight: 500;
}
</style>
