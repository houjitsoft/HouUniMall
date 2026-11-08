<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">积分管理</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增规则</el-button>
        </div>
      </template>

      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover"><div class="stat-item"><div class="stat-value">{{ stats.totalPoints }}</div><div class="stat-label">总积分发放</div></div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover"><div class="stat-item"><div class="stat-value">{{ stats.usedPoints }}</div><div class="stat-label">已使用积分</div></div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover"><div class="stat-item"><div class="stat-value">{{ stats.activeRules }}</div><div class="stat-label">有效规则</div></div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover"><div class="stat-item"><div class="stat-value">{{ stats.todayPoints }}</div><div class="stat-label">今日发放</div></div></el-card>
        </el-col>
      </el-row>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.ruleName" placeholder="请输入" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="searchForm.ruleType" placeholder="请选择" clearable style="width: 150px;">
            <el-option label="签到" :value="1" />
            <el-option label="消费" :value="2" />
            <el-option label="评价" :value="3" />
            <el-option label="分享" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="ruleId" label="规则ID" width="80" />
        <el-table-column prop="ruleName" label="规则名称" min-width="150" />
        <el-table-column label="规则类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getRuleTypeTag(row.ruleType)">{{ getRuleTypeText(row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分值" width="100" />
        <el-table-column prop="dailyLimit" label="每日上限" width="100" />
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

      <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :total="pagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData" @current-change="fetchData" class="pagination" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择" style="width: 100%;">
            <el-option label="签到" :value="1" />
            <el-option label="消费" :value="2" />
            <el-option label="评价" :value="3" />
            <el-option label="分享" :value="4" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="积分值" prop="points">
              <el-input-number v-model="form.points" :min="1" :max="99999" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每日上限" prop="dailyLimit">
              <el-input-number v-model="form.dailyLimit" :min="0" :max="99999" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="规则说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入规则说明" />
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

const stats = reactive({ totalPoints: 0, usedPoints: 0, activeRules: 0, todayPoints: 0 })
const searchForm = reactive({ ruleName: '', ruleType: null })
const form = reactive({ ruleId: null, ruleName: '', ruleType: 1, points: 10, dailyLimit: 100, description: '' })
const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  points: [{ required: true, message: '请输入积分值', trigger: 'blur' }]
}
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])

const fetchStats = async () => {
  try {
    const res = await request.post('/web/marketing/points/stats', {})
    if (res.code === 200) Object.assign(stats, res.data)
  } catch (error) {}
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/marketing/points/query', { page: pagination.page, size: pagination.size, ...searchForm })
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
const handleReset = () => { Object.assign(searchForm, { ruleName: '', ruleType: null }); fetchData() }
const handleAdd = () => { dialogTitle.value = '新增规则'; Object.assign(form, { ruleId: null, ruleName: '', ruleType: 1, points: 10, dailyLimit: 100, description: '' }); dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑规则'; Object.assign(form, { ...row }); dialogVisible.value = true }

const handleStatusChange = async (row) => {
  try {
    const res = await request.post('/web/marketing/points/status', { ruleId: row.ruleId, status: row.status })
    if (res.code === 200) ElMessage.success('状态更新成功')
    else row.status = row.status === 1 ? 0 : 1
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    const res = await request.post('/web/marketing/points/delete', { ruleId: row.ruleId })
    if (res.code === 200) { ElMessage.success('删除成功'); fetchData() }
  } catch (error) {}
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const url = form.ruleId ? '/web/marketing/points/update' : '/web/marketing/points/add'
        const res = await request.post(url, form)
        if (res.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; fetchData(); fetchStats() }
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const getRuleTypeTag = (type) => ({ 1: '', 2: 'success', 3: 'warning', 4: 'danger' }[type] || '')
const getRuleTypeText = (type) => ({ 1: '签到', 2: '消费', 3: '评价', 4: '分享' }[type] || '-')

onMounted(() => { fetchStats(); fetchData() })
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.stats-row { margin-bottom: 20px; }
.stat-item { text-align: center; padding: 20px 0; }
.stat-value { font-size: 32px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 10px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
