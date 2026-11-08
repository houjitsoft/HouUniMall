<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">促销活动</span>
          <el-button type="primary" @click="handleAdd">新增活动</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.name" placeholder="请输入活动名称" clearable />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable>
            <el-option label="限时折扣" value="discount" />
            <el-option label="满减活动" value="fullcut" />
            <el-option label="秒杀活动" value="seckill" />
            <el-option label="拼团活动" value="group" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="未开始" value="0" />
            <el-option label="进行中" value="1" />
            <el-option label="已结束" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="活动ID" width="80" />
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column label="活动类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="优惠力度" width="100">
          <template #default="{ row }">
            {{ row.type === 'discount' ? row.discount + '折' : '满' + row.fullAmount + '减' + row.cutAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="endTime" label="结束时间" width="160" />
        <el-table-column label="活动状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="参与人数" width="100" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" text type="success" size="small" @click="handleStart(row)">启动</el-button>
            <el-button v-if="row.status === 1" text type="warning" size="small" @click="handleStop(row)">停止</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="活动名称">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="form.type" placeholder="请选择" @change="handleTypeChange">
            <el-option label="限时折扣" value="discount" />
            <el-option label="满减活动" value="fullcut" />
            <el-option label="秒杀活动" value="seckill" />
            <el-option label="拼团活动" value="group" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.type === 'discount'" label="折扣力度">
          <el-input-number v-model="form.discount" :min="1" :max="10" :step="0.1" />
          <span style="margin-left: 10px;">折</span>
        </el-form-item>
        <el-form-item v-if="form.type === 'fullcut'" label="满减条件">
          <el-input-number v-model="form.fullAmount" :min="0" placeholder="满" />
          <span style="margin: 0 10px;">元减</span>
          <el-input-number v-model="form.cutAmount" :min="0" placeholder="减" />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="活动商品">
          <el-button @click="handleSelectGoods">选择商品</el-button>
          <div v-if="form.goodsCount > 0" style="margin-top: 10px;">
            已选择 {{ form.goodsCount }} 件商品
          </div>
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增活动')

const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

const form = reactive({
  id: null,
  name: '',
  type: '',
  discount: 8.5,
  fullAmount: 100,
  cutAmount: 20,
  timeRange: [],
  goodsCount: 0,
  description: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { 
    id: 1, 
    name: '春季大促', 
    type: 'discount', 
    discount: 8.5, 
    startTime: '2024-03-01 00:00:00', 
    endTime: '2024-03-31 23:59:59',
    status: 1,
    participantCount: 1256
  },
  { 
    id: 2, 
    name: '满200减50', 
    type: 'fullcut', 
    fullAmount: 200, 
    cutAmount: 50,
    startTime: '2024-03-15 00:00:00', 
    endTime: '2024-03-20 23:59:59',
    status: 0,
    participantCount: 0
  },
  { 
    id: 3, 
    name: '限时秒杀', 
    type: 'seckill',
    discount: 5,
    startTime: '2024-03-10 10:00:00', 
    endTime: '2024-03-10 12:00:00',
    status: 2,
    participantCount: 856
  }
])

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = tableData.value.length
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, { name: '', type: '', status: '' })
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增活动'
  Object.assign(form, { 
    id: null, 
    name: '', 
    type: '', 
    discount: 8.5, 
    fullAmount: 100, 
    cutAmount: 20,
    timeRange: [],
    goodsCount: 0,
    description: '' 
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑活动'
  Object.assign(form, { 
    ...row, 
    timeRange: [row.startTime, row.endTime],
    goodsCount: 10
  })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看活动详情：' + row.name)
}

const handleStart = (row) => {
  ElMessageBox.confirm('确认启动该活动？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    row.status = 1
    ElMessage.success('活动已启动')
  })
}

const handleStop = (row) => {
  ElMessageBox.confirm('确认停止该活动？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = 2
    ElMessage.success('活动已停止')
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该活动？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleTypeChange = () => {
  // 切换类型时重置相关字段
  form.discount = 8.5
  form.fullAmount = 100
  form.cutAmount = 20
}

const handleSelectGoods = () => {
  ElMessage.info('打开商品选择器')
  form.goodsCount = 5
}

const handleSubmit = () => {
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

const getTypeColor = (type) => {
  const map = { discount: 'success', fullcut: 'primary', seckill: 'danger', group: 'warning' }
  return map[type] || ''
}

const getTypeText = (type) => {
  const map = { discount: '限时折扣', fullcut: '满减活动', seckill: '秒杀活动', group: '拼团活动' }
  return map[type] || '-'
}

const getStatusColor = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] || '-'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
