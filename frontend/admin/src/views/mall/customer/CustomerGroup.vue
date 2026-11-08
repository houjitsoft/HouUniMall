<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">客户分组</span>
          <el-button type="primary" @click="handleAdd">新增分组</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户信息">
          <el-input 
            v-model="searchForm.userInfo" 
            placeholder="用户编号/昵称/手机号码" 
            clearable 
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="searchForm.registerTimeRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD"
            style="width: 340px;"
          />
        </el-form-item>
        <el-form-item label="注册来源">
          <el-select 
            v-model="searchForm.registerSource" 
            placeholder="请选择" 
            clearable
            style="width: 160px;"
          >
            <el-option label="微信小程序" value="wechat_mini" />
            <el-option label="微信公众号" value="wechat_mp" />
            <el-option label="H5" value="h5" />
            <el-option label="APP" value="app" />
            <el-option label="PC网站" value="pc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="分组ID" width="80" />
        <el-table-column prop="name" label="分组名称" min-width="150" />
        <el-table-column prop="customerCount" label="客户数量" width="100" />
        <el-table-column label="分组条件" min-width="250">
          <template #default="{ row }">
            <el-tag v-for="(condition, index) in row.conditions" :key="index" class="condition-tag">
              {{ condition }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="分组描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="primary" size="small" @click="handleViewCustomers(row)">查看客户</el-button>
            <el-button text type="warning" size="small" @click="handleSync(row)">同步数据</el-button>
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
      <el-form :model="form" label-width="100px">
        <el-form-item label="分组名称">
          <el-input v-model="form.name" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入分组描述" />
        </el-form-item>
        <el-form-item label="分组条件">
          <div class="condition-builder">
            <el-form :inline="true" v-for="(condition, index) in form.conditions" :key="index" class="condition-item">
              <el-form-item>
                <el-select v-model="condition.field" placeholder="选择字段">
                  <el-option label="客户等级" value="level" />
                  <el-option label="消费金额" value="amount" />
                  <el-option label="订单数" value="orderCount" />
                  <el-option label="注册时间" value="registerTime" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-select v-model="condition.operator" placeholder="选择条件">
                  <el-option label="等于" value="eq" />
                  <el-option label="大于" value="gt" />
                  <el-option label="小于" value="lt" />
                  <el-option label="包含" value="in" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-input v-model="condition.value" placeholder="输入值" style="width: 150px;" />
              </el-form-item>
              <el-form-item>
                <el-button text type="danger" @click="removeCondition(index)">删除</el-button>
              </el-form-item>
            </el-form>
            <el-button type="primary" text @click="addCondition">+ 添加条件</el-button>
          </div>
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
const dialogTitle = ref('新增分组')

const searchForm = reactive({
  userInfo: '',
  registerTimeRange: [],
  registerSource: ''
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  conditions: [{ field: '', operator: '', value: '' }]
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { 
    id: 1, 
    name: 'VIP客户组', 
    customerCount: 256, 
    conditions: ['客户等级=VIP', '消费金额>10000'], 
    description: '高价值VIP客户群体', 
    createTime: '2024-01-01 10:00:00' 
  },
  { 
    id: 2, 
    name: '活跃用户组', 
    customerCount: 512, 
    conditions: ['订单数>5', '注册时间<30天'], 
    description: '近期活跃的新用户', 
    createTime: '2024-01-02 11:00:00' 
  },
  { 
    id: 3, 
    name: '潜在客户组', 
    customerCount: 128, 
    conditions: ['订单数=0', '注册时间<7天'], 
    description: '注册但未下单的潜在客户', 
    createTime: '2024-01-03 12:00:00' 
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
  searchForm.userInfo = ''
  searchForm.registerTimeRange = []
  searchForm.registerSource = ''
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增分组'
  Object.assign(form, { 
    id: null, 
    name: '', 
    description: '', 
    conditions: [{ field: '', operator: '', value: '' }] 
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分组'
  Object.assign(form, { 
    ...row, 
    conditions: [{ field: 'level', operator: 'eq', value: 'VIP' }] 
  })
  dialogVisible.value = true
}

const handleViewCustomers = (row) => {
  ElMessage.info('查看分组客户：' + row.name)
}

const handleSync = (row) => {
  ElMessage.success('数据同步成功')
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该分组？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

const addCondition = () => {
  form.conditions.push({ field: '', operator: '', value: '' })
}

const removeCondition = (index) => {
  form.conditions.splice(index, 1)
}

const handleSubmit = () => {
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.search-form { 
  margin-bottom: 20px;
}
.search-form :deep(.el-form-item) {
  margin-bottom: 15px;
}
.search-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}
.pagination { margin-top: 20px; justify-content: flex-end; }
.condition-tag { margin-right: 10px; }
.condition-builder { padding: 10px; background: #f5f7fa; border-radius: 4px; }
.condition-item { margin-bottom: 10px; }
</style>
