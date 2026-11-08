<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">限时折扣</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增活动
          </el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.discountName" placeholder="请输入活动名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.discountState" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="未开始" :value="0" />
            <el-option label="已开始" :value="1" />
            <el-option label="已结束" :value="2" />
            <el-option label="已失效" :value="3" />
            <el-option label="预热中" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="discountId" label="活动ID" width="80" />
        <el-table-column prop="discountName" label="活动名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="shopName" label="店铺名称" width="150" show-overflow-tooltip />
        <el-table-column label="活动时间" width="180">
          <template #default="{ row }">
            <div>{{ formatDate(row.discountStartTime) }}</div>
            <div>{{ formatDate(row.discountEndTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="预热时间" width="100">
          <template #default="{ row }">
            {{ row.preStartHour }}小时
          </template>
        </el-table-column>
        <el-table-column label="限购" width="100">
          <template #default="{ row }">
            下限:{{ row.discountLowerLimit }}<br/>
            上限:{{ row.discountUpperLimit }}
          </template>
        </el-table-column>
        <el-table-column label="活动状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.discountState)">
              {{ getStatusText(row.discountState) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountCreateTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleViewGoods(row)">
              <el-icon><Goods /></el-icon> 查看商品
            </el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button
              v-if="row.discountState === 0"
              text
              type="success"
              size="small"
              @click="handleStart(row)"
            >
              <el-icon><VideoPlay /></el-icon> 启动
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动名称" prop="discountName">
              <el-input v-model="form.discountName" placeholder="请输入活动名称" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="店铺名称" prop="shopName">
              <el-input v-model="form.shopName" placeholder="请输入店铺名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="discountStartTime">
              <el-date-picker
                v-model="form.discountStartTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="discountEndTime">
              <el-date-picker
                v-model="form.discountEndTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="预热时间" prop="preStartHour">
              <el-input-number
                v-model="form.preStartHour"
                :min="0"
                :max="72"
                style="width: 100%;"
              />
              <span style="margin-left: 10px;">小时</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="限购天数" prop="discountUpperTime">
              <el-input-number
                v-model="form.discountUpperTime"
                :min="0"
                style="width: 100%;"
              />
              <span style="margin-left: 10px;">天</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="购买下限" prop="discountLowerLimit">
              <el-input-number
                v-model="form.discountLowerLimit"
                :min="1"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买上限" prop="discountUpperLimit">
              <el-input-number
                v-model="form.discountUpperLimit"
                :min="1"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 商品列表对话框 -->
    <el-dialog v-model="goodsDialogVisible" title="活动商品列表" width="1000px">
      <el-button type="primary" @click="handleAddGoods" style="margin-bottom: 15px;">
        <el-icon><Plus /></el-icon> 添加商品
      </el-button>
      <el-table :data="goodsList" border stripe>
        <el-table-column prop="goodsId" label="商品ID" width="80" />
        <el-table-column prop="goodsName" label="商品名称" min-width="200" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.goodsImage"
              :src="row.goodsImage"
              :preview-src-list="[row.goodsImage]"
              fit="cover"
              style="width: 60px; height: 60px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="goodsPrice" label="原价(元)" width="100">
          <template #default="{ row }">
            ¥{{ row.goodsPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="discountPrice" label="折扣价(元)" width="120">
          <template #default="{ row }">
            <span class="discount-price">¥{{ row.discountPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="折扣" width="80">
          <template #default="{ row }">
            {{ row.discountAmount }}折
          </template>
        </el-table-column>
        <el-table-column label="限购" width="120">
          <template #default="{ row }">
            下限:{{ row.goodsLowerLimit }}<br/>
            上限:{{ row.goodsUpperLimit }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.discountGoodsState)">
              {{ getStatusText(row.discountGoodsState) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button text type="danger" size="small" @click="handleRemoveGoods(scope.$index)">
              <el-icon><Delete /></el-icon> 移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="goodsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, VideoPlay, Goods } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const goodsDialogVisible = ref(false)
const dialogTitle = ref('新增活动')
const formRef = ref(null)

const searchForm = reactive({
  discountName: '',
  discountState: null
})

const form = reactive({
  discountId: null,
  discountName: '',
  shopName: '',
  discountStartTime: '',
  discountEndTime: '',
  preStartHour: 0,
  discountUpperTime: 0,
  discountLowerLimit: 1,
  discountUpperLimit: 10
})

const rules = {
  discountName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  discountStartTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  discountEndTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const goodsList = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request.post('/web/activity/discount/query', params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchForm, { discountName: '', discountState: null })
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增活动'
  Object.assign(form, {
    discountId: null,
    discountName: '',
    shopName: '',
    discountStartTime: '',
    discountEndTime: '',
    preStartHour: 0,
    discountUpperTime: 0,
    discountLowerLimit: 1,
    discountUpperLimit: 10
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑活动'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleViewGoods = async (row) => {
  try {
    const res = await request.post('/web/activity/discount/goods/query', { discountId: row.discountId })
    if (res.code === 200) {
      goodsList.value = res.data || []
      goodsDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  }
}

const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm('确认启动该限时折扣活动？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    const res = await request.post('/web/activity/discount/start', { discountId: row.discountId })
    if (res.code === 200) {
      ElMessage.success('启动成功')
      fetchData()
    }
  } catch (error) {
    console.log('取消启动')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该限时折扣活动？删除后不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.post('/web/activity/discount/delete', { discountId: row.discountId })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    console.log('取消删除')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const url = form.discountId ? '/web/activity/discount/update' : '/web/activity/discount/add'
        const res = await request.post(url, form)
        if (res.code === 200) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          fetchData()
        }
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleAddGoods = () => {
  ElMessage.info('添加商品功能开发中...')
}

const handleRemoveGoods = (index) => {
  goodsList.value.splice(index, 1)
}

const formatDate = (date) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning', 4: 'primary' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '未开始', 1: '已开始', 2: '已结束', 3: '已失效', 4: '预热中' }
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
.discount-price { color: #f56c6c; font-weight: bold; }
</style>
