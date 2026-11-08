<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">满减满折</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增活动</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="活动名称">
          <el-input v-model="searchForm.name" placeholder="请输入" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="满减" value="reduce" />
            <el-option label="满折" value="discount" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'reduce' ? 'success' : 'warning'">
              {{ row.type === 'reduce' ? '满减' : '满折' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="condition" label="满足条件" width="120" />
        <el-table-column prop="value" label="优惠值" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="reduce">满减</el-radio>
            <el-radio label="discount">满折</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="满足条件" prop="condition">
          <el-input-number v-model="form.condition" :min="0" :precision="2" />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>
        <el-form-item :label="form.type === 'reduce' ? '减免金额' : '折扣'" prop="value">
          <el-input-number v-model="form.value" :min="0" :precision="2" />
          <span style="margin-left: 10px;">{{ form.type === 'reduce' ? '元' : '折' }}</span>
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
const dialogTitle = ref('新增活动')
const formRef = ref(null)

const searchForm = reactive({ name: '', type: null })
const form = reactive({ id: null, name: '', type: 'reduce', condition: 0, value: 0 })
const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  condition: [{ required: true, message: '请输入满足条件', trigger: 'blur' }],
  value: [{ required: true, message: '请输入优惠值', trigger: 'blur' }]
}

const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/activity/fulldiscount/query', { page: pagination.page, size: pagination.size, ...searchForm })
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
const handleReset = () => { Object.assign(searchForm, { name: '', type: null }); fetchData() }
const handleAdd = () => { dialogTitle.value = '新增活动'; Object.assign(form, { id: null, name: '', type: 'reduce', condition: 0, value: 0 }); dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑活动'; Object.assign(form, { ...row }); dialogVisible.value = true }

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await request.post('/web/activity/fulldiscount/delete', { id: row.id })
    if (res.code === 200) { ElMessage.success('删除成功'); fetchData() }
  } catch (error) {}
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const url = form.id ? '/web/activity/fulldiscount/update' : '/web/activity/fulldiscount/add'
        const res = await request.post(url, form)
        if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; fetchData() }
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
