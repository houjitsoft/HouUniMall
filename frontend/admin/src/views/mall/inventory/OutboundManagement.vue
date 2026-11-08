<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <h3 style="margin: 0 0 8px 0">出库管理</h3>
          <el-text type="info">管理商品出库记录</el-text>
        </div>
        <el-button type="primary" icon="Plus" @click="handleCreate">创建出库单</el-button>
      </div>
    </el-card>

    <!-- 筛选条件 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="出库单号">
          <el-input v-model="searchForm.outboundNo" placeholder="请输入出库单号" clearable />
        </el-form-item>
        <el-form-item label="出库类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="销售出库" value="sales" />
            <el-option label="调拨出库" value="transfer" />
            <el-option label="报损出库" value="loss" />
            <el-option label="其他出库" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已审核" value="approved" />
            <el-option label="已出库" value="completed" />
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

    <!-- 出库单列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">出库单列表</span>
      </template>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="outboundNo" label="出库单号" width="180" />
        <el-table-column label="出库类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'sales'" type="primary">销售出库</el-tag>
            <el-tag v-else-if="row.type === 'transfer'">调拨出库</el-tag>
            <el-tag v-else-if="row.type === 'loss'" type="danger">报损出库</el-tag>
            <el-tag v-else type="info">其他出库</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="出库状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 'approved'" type="primary">已审核</el-tag>
            <el-tag v-else-if="row.status === 'completed'" type="success">已出库</el-tag>
            <el-tag v-else type="info">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联单号" width="180" />
        <el-table-column prop="goodsCount" label="商品数量" width="120" align="right" />
        <el-table-column prop="totalQuantity" label="出库总数" width="120" align="right" />
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
              @click="handleOutbound(row)"
            >
              确认出库
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
  outboundNo: '',
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
        outboundNo: 'OUT20240112001',
        type: 'sales',
        status: 'completed',
        orderNo: 'ORD20240112001',
        goodsCount: 5,
        totalQuantity: 12,
        operator: '张三',
        createTime: '2024-01-12 10:30:00',
        remark: '正常销售出库'
      },
      {
        outboundNo: 'OUT20240112002',
        type: 'transfer',
        status: 'approved',
        orderNo: 'TRS20240112001',
        goodsCount: 3,
        totalQuantity: 8,
        operator: '李四',
        createTime: '2024-01-12 14:15:00',
        remark: '调拨至北京仓库'
      },
      {
        outboundNo: 'OUT20240112003',
        type: 'loss',
        status: 'pending',
        orderNo: '-',
        goodsCount: 2,
        totalQuantity: 3,
        operator: '王五',
        createTime: '2024-01-12 16:45:00',
        remark: '商品损坏报损'
      }
    ]
    pagination.total = 45
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
    outboundNo: '',
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

// 创建出库单
const handleCreate = () => {
  ElMessage.info('创建出库单功能开发中')
}

// 查看详情
const handleView = (row) => {
  ElMessage.info(`查看出库单详情：${row.outboundNo}`)
}

// 审核
const handleApprove = (row) => {
  ElMessageBox.confirm(
    `确定要审核通过出库单"${row.outboundNo}"吗？`,
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

// 确认出库
const handleOutbound = (row) => {
  ElMessageBox.confirm(
    `确定要确认出库"${row.outboundNo}"吗？确认后将扣减库存。`,
    '确认出库',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('出库成功，库存已扣减')
    fetchData()
  })
}

// 取消
const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定要取消出库单"${row.outboundNo}"吗？`,
    '确认取消',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('出库单已取消')
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
</style>
