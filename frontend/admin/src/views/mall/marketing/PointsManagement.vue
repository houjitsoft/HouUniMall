<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">积分管理</span>
          <el-button type="primary" @click="handleAdd">新增积分规则</el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">1,258,000</div>
              <div class="stat-label">总发放积分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">856,000</div>
              <div class="stat-label">已使用积分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">402,000</div>
              <div class="stat-label">剩余积分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <div class="stat-item">
              <div class="stat-value">3,256</div>
              <div class="stat-label">参与人数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="规则名称">
          <el-input v-model="searchForm.name" placeholder="请输入规则名称" clearable />
        </el-form-item>
        <el-form-item label="规则状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="规则ID" width="80" />
        <el-table-column prop="name" label="规则名称" min-width="150" />
        <el-table-column label="触发条件" min-width="150">
          <template #default="{ row }">
            {{ getConditionText(row.condition) }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="获得积分" width="100">
          <template #default="{ row }">
            <el-tag type="warning">+{{ row.points }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="limitTimes" label="每日上限" width="100">
          <template #default="{ row }">
            {{ row.limitTimes > 0 ? row.limitTimes + '次' : '不限' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="primary" size="small" @click="handleViewRecord(row)">查看记录</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="form.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="触发条件">
          <el-select v-model="form.condition" placeholder="请选择">
            <el-option label="注册送积分" value="register" />
            <el-option label="签到送积分" value="signin" />
            <el-option label="消费送积分" value="order" />
            <el-option label="评价送积分" value="comment" />
            <el-option label="分享送积分" value="share" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.condition === 'order'" label="消费比例">
          <el-input-number v-model="form.ratio" :min="0" :max="100" :step="0.1" />
          <span style="margin-left: 10px;">%（消费1元获得积分）</span>
        </el-form-item>
        <el-form-item v-else label="获得积分">
          <el-input-number v-model="form.points" :min="1" />
        </el-form-item>
        <el-form-item label="每日上限">
          <el-input-number v-model="form.limitTimes" :min="0" />
          <span style="margin-left: 10px;">次（0表示不限制）</span>
        </el-form-item>
        <el-form-item label="规则状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="规则说明">
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
const dialogTitle = ref('新增积分规则')

const searchForm = reactive({
  name: '',
  status: ''
})

const form = reactive({
  id: null,
  name: '',
  condition: '',
  points: 10,
  ratio: 1,
  limitTimes: 0,
  status: 1,
  description: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { id: 1, name: '新用户注册', condition: 'register', points: 100, limitTimes: 1, status: 1, createTime: '2024-01-01 10:00:00' },
  { id: 2, name: '每日签到', condition: 'signin', points: 10, limitTimes: 1, status: 1, createTime: '2024-01-02 11:00:00' },
  { id: 3, name: '消费赠送', condition: 'order', points: 0, ratio: 1, limitTimes: 0, status: 1, createTime: '2024-01-03 12:00:00' },
  { id: 4, name: '商品评价', condition: 'comment', points: 20, limitTimes: 5, status: 1, createTime: '2024-01-04 13:00:00' },
  { id: 5, name: '分享好友', condition: 'share', points: 50, limitTimes: 3, status: 0, createTime: '2024-01-05 14:00:00' }
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
  Object.assign(searchForm, { name: '', status: '' })
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增积分规则'
  Object.assign(form, { 
    id: null, 
    name: '', 
    condition: '', 
    points: 10, 
    ratio: 1,
    limitTimes: 0,
    status: 1,
    description: '' 
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑积分规则'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleViewRecord = (row) => {
  ElMessage.info('查看积分记录：' + row.name)
}

const handleStatusChange = (row) => {
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该积分规则？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

const handleSubmit = () => {
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

const getConditionText = (condition) => {
  const map = { 
    register: '新用户注册', 
    signin: '每日签到', 
    order: '消费赠送（1元=1积分）', 
    comment: '商品评价', 
    share: '分享好友' 
  }
  return map[condition] || '-'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.stats-cards { margin-bottom: 30px; }
.stat-item { text-align: center; padding: 20px 0; }
.stat-value { font-size: 32px; font-weight: bold; color: #409EFF; margin-bottom: 10px; }
.stat-label { font-size: 14px; color: #666; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }
</style>
