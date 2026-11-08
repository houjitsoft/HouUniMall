<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">千客千价</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增规则
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.name" placeholder="请输入规则名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="用户等级">
          <el-select v-model="searchForm.userLevel" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="普通会员" :value="1" />
            <el-option label="银卡会员" :value="2" />
            <el-option label="金卡会员" :value="3" />
            <el-option label="钻石会员" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column label="用户等级" width="120">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.userLevel)">
              {{ getLevelText(row.userLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="discountRate" label="折扣率" width="100">
          <template #default="{ row }">
            {{ row.discountRate }}折
          </template>
        </el-table-column>
        <el-table-column prop="minQuantity" label="最小购买量" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="用户等级" prop="userLevel">
          <el-select v-model="form.userLevel" placeholder="请选择用户等级" style="width: 100%;">
            <el-option label="普通会员" :value="1" />
            <el-option label="银卡会员" :value="2" />
            <el-option label="金卡会员" :value="3" />
            <el-option label="钻石会员" :value="4" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="折扣率" prop="discountRate">
              <el-input-number v-model="form.discountRate" :min="0.1" :max="10" :precision="1" :step="0.1" />
              <span style="margin-left: 10px;">折</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小购买量" prop="minQuantity">
              <el-input-number v-model="form.minQuantity" :min="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适用商品">
          <el-button @click="handleSelectGoods">选择商品</el-button>
          <el-table :data="form.goodsList" border class="goods-table" max-height="300">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="goodsName" label="商品名称" min-width="150" />
            <el-table-column prop="originalPrice" label="原价" width="100">
              <template #default="{ row }">¥{{ row.originalPrice }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button text type="danger" size="small" @click="handleRemoveGoods(scope.$index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
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
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const formRef = ref(null)

const searchForm = reactive({ name: '', userLevel: null })
const form = reactive({ id: null, ruleName: '', userLevel: 1, discountRate: 9.0, minQuantity: 1, goodsList: [] })
const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  userLevel: [{ required: true, message: '请选择用户等级', trigger: 'change' }],
  discountRate: [{ required: true, message: '请输入折扣率', trigger: 'blur' }],
  minQuantity: [{ required: true, message: '请输入最小购买量', trigger: 'blur' }]
}

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/marketing/thousand-price/query', { page: pagination.page, size: pagination.size, ...searchForm })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchData() }
const handleReset = () => { Object.assign(searchForm, { name: '', userLevel: null }); fetchData() }
const handleAdd = () => { 
  dialogTitle.value = '新增规则'
  Object.assign(form, { id: null, ruleName: '', userLevel: 1, discountRate: 9.0, minQuantity: 1, goodsList: [] })
  dialogVisible.value = true 
}
const handleEdit = (row) => { 
  dialogTitle.value = '编辑规则'
  Object.assign(form, { ...row, goodsList: row.goodsList || [] })
  dialogVisible.value = true 
}

const handleStatusChange = async (row) => {
  try {
    const res = await request.post('/web/marketing/thousand-price/status', { id: row.id, status: row.status })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该规则？', '提示', { type: 'warning' })
    const res = await request.post('/web/marketing/thousand-price/delete', { id: row.id })
    if (res.code === 200) { ElMessage.success('删除成功'); fetchData() }
  } catch (error) {}
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const url = form.id ? '/web/marketing/thousand-price/update' : '/web/marketing/thousand-price/add'
        const res = await request.post(url, form)
        if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; fetchData() }
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleSelectGoods = () => {
  ElMessage.info('商品选择功能开发中')
}

const handleRemoveGoods = (index) => {
  form.goodsList.splice(index, 1)
}

const getLevelType = (level) => {
  const map = { 1: '', 2: 'success', 3: 'warning', 4: 'danger' }
  return map[level] || ''
}

const getLevelText = (level) => {
  const map = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员', 4: '钻石会员' }
  return map[level] || '-'
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
.goods-table { margin-top: 10px; }
</style>
