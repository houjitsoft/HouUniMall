<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">店铺优惠券</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增优惠券
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="优惠券名称">
          <el-input v-model="searchForm.couponName" placeholder="请输入" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.couponType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="代金券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="couponId" label="券ID" width="80" />
        <el-table-column prop="couponName" label="优惠券名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCouponTypeTag(row.couponType)">
              {{ getCouponTypeText(row.couponType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠内容" width="150">
          <template #default="{ row }">
            <span v-if="row.couponType === 1">满{{ row.minAmount }}减{{ row.discountAmount }}</span>
            <span v-else-if="row.couponType === 2">{{ row.discountRate }}折</span>
            <span v-else>{{ row.discountAmount }}元</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="发放数量" width="100" />
        <el-table-column prop="receivedCount" label="已领取" width="100" />
        <el-table-column prop="usedCount" label="已使用" width="100" />
        <el-table-column label="有效期" width="180">
          <template #default="{ row }">
            <div>{{ formatDate(row.startTime) }}</div>
            <div>{{ formatDate(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon> 删除
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
        @size-change="fetchData"
        @current-change="fetchData"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="优惠券名称" prop="couponName">
          <el-input v-model="form.couponName" placeholder="请输入优惠券名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优惠券类型" prop="couponType">
              <el-select v-model="form.couponType" placeholder="请选择" style="width: 100%;" @change="handleTypeChange">
                <el-option label="满减券" :value="1" />
                <el-option label="折扣券" :value="2" />
                <el-option label="代金券" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发放数量" prop="totalCount">
              <el-input-number v-model="form.totalCount" :min="1" :max="999999" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.couponType === 1">
          <el-col :span="12">
            <el-form-item label="满足金额" prop="minAmount">
              <el-input-number v-model="form.minAmount" :min="0.01" :precision="2" style="width: 100%;" />
              <span style="margin-left: 10px;">元</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="减免金额" prop="discountAmount">
              <el-input-number v-model="form.discountAmount" :min="0.01" :precision="2" style="width: 100%;" />
              <span style="margin-left: 10px;">元</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.couponType === 2">
          <el-col :span="12">
            <el-form-item label="折扣率" prop="discountRate">
              <el-input-number v-model="form.discountRate" :min="0.1" :max="9.9" :precision="1" :step="0.1" style="width: 100%;" />
              <span style="margin-left: 10px;">折</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高优惠" prop="maxDiscountAmount">
              <el-input-number v-model="form.maxDiscountAmount" :min="0" :precision="2" style="width: 100%;" />
              <span style="margin-left: 10px;">元（0为不限）</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.couponType === 3">
          <el-col :span="12">
            <el-form-item label="代金券金额" prop="discountAmount">
              <el-input-number v-model="form.discountAmount" :min="0.01" :precision="2" style="width: 100%;" />
              <span style="margin-left: 10px;">元</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
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
            <el-form-item label="每人限领" prop="limitPerUser">
              <el-input-number v-model="form.limitPerUser" :min="1" :max="999" style="width: 100%;" />
              <span style="margin-left: 10px;">张</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="使用门槛" prop="minAmount">
              <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width: 100%;" />
              <span style="margin-left: 10px;">元（0为无门槛）</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="使用说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入使用说明" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, View } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增优惠券')
const formRef = ref(null)

const searchForm = reactive({
  couponName: '',
  couponType: null,
  status: null
})

const form = reactive({
  couponId: null,
  couponName: '',
  couponType: 1,
  totalCount: 100,
  minAmount: 0,
  discountAmount: 0,
  discountRate: 9.0,
  maxDiscountAmount: 0,
  startTime: '',
  endTime: '',
  limitPerUser: 1,
  description: ''
})

const rules = {
  couponName: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  couponType: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  totalCount: [{ required: true, message: '请输入发放数量', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request.post('/web/marketing/coupon/query', params)
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
  Object.assign(searchForm, { couponName: '', couponType: null, status: null })
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增优惠券'
  Object.assign(form, {
    couponId: null,
    couponName: '',
    couponType: 1,
    totalCount: 100,
    minAmount: 0,
    discountAmount: 0,
    discountRate: 9.0,
    maxDiscountAmount: 0,
    startTime: '',
    endTime: '',
    limitPerUser: 1,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑优惠券'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看优惠券详情：' + row.couponName)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该优惠券？删除后不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.post('/web/marketing/coupon/delete', { couponId: row.couponId })
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch (error) {
    console.log('取消删除')
  }
}

const handleTypeChange = () => {
  // 切换类型时重置相关字段
  form.minAmount = 0
  form.discountAmount = 0
  form.discountRate = 9.0
  form.maxDiscountAmount = 0
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const url = form.couponId ? '/web/marketing/coupon/update' : '/web/marketing/coupon/add'
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

const formatDate = (date) => {
  if (!date) return '-'
  return date.replace('T', ' ').substring(0, 16)
}

const getCouponTypeTag = (type) => {
  const map = { 1: 'success', 2: 'warning', 3: 'primary' }
  return map[type] || ''
}

const getCouponTypeText = (type) => {
  const map = { 1: '满减券', 2: '折扣券', 3: '代金券' }
  return map[type] || '-'
}

const getStatusType = (status) => {
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
