<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">客户列表</span>
          <div class="header-buttons">
            <el-button type="success" @click="showImportDialog">导入客户</el-button>
            <el-button type="primary" @click="handleAdd">新增客户</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户名称">
          <el-input v-model="searchForm.name" placeholder="请输入客户名称" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="searchForm.level" placeholder="请选择" clearable>
            <el-option label="普通会员" value="1" />
            <el-option label="VIP会员" value="2" />
            <el-option label="SVIP会员" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="正常" value="1" />
            <el-option label="冻结" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="客户ID" width="80" />
        <el-table-column prop="name" label="客户名称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="客户等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">{{ getLevelText(row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="消费金额(元)" width="120" sortable />
        <el-table-column prop="orderCount" label="订单数" width="100" sortable />
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button text type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button text type="warning" size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '冻结' : '解冻' }}
            </el-button>
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
        <el-form-item label="客户名称">
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="form.level" placeholder="请选择">
            <el-option label="普通会员" :value="1" />
            <el-option label="VIP会员" :value="2" />
            <el-option label="SVIP会员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分">
          <el-input-number v-model="form.points" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">冻结</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 客户导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="客户导入" width="900px" :close-on-click-modal="false">
      <el-steps :active="currentStep" align-center class="import-steps">
        <el-step title="下载模板" />
        <el-step title="上传文件" />
        <el-step title="数据预览" />
        <el-step title="完成导入" />
      </el-steps>

      <!-- 步骤1：下载模板 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-alert title="请先下载导入模板，按照模板格式填写客户信息" type="info" :closable="false" show-icon />
        <div class="template-info">
          <h3>模板说明：</h3>
          <ul>
            <li>客户名称：必填，不超过50个字符</li>
            <li>手机号：必填，11位数字</li>
            <li>邮箱：选填，需符合邮箱格式</li>
            <li>客户等级：必填，可选值：1-普通会员、2-VIP会员、3-SVIP会员</li>
            <li>积分：选填，数字类型</li>
            <li>备注：选填，不超过200个字符</li>
          </ul>
        </div>
        <div class="step-buttons">
          <el-button type="primary" @click="downloadTemplate">下载导入模板</el-button>
          <el-button @click="currentStep = 1">下一步</el-button>
        </div>
      </div>

      <!-- 步骤2：上传文件 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-alert title="请选择填写好的Excel文件进行上传" type="info" :closable="false" show-icon />
        <el-upload
          class="upload-area"
          drag
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          accept=".xlsx,.xls"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">只能上传 xlsx/xls 文件，且不超过 5MB</div>
          </template>
        </el-upload>
        <div class="step-buttons">
          <el-button @click="currentStep = 0">上一步</el-button>
          <el-button type="primary" :disabled="!uploadFile" @click="handleParse">解析文件</el-button>
        </div>
      </div>

      <!-- 步骤3：数据预览 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-alert :title="`共解析 ${previewData.length} 条数据，请确认后导入`" type="success" :closable="false" show-icon />
        <el-table :data="previewData" border stripe max-height="400" class="preview-table">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="name" label="客户名称" min-width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="150" />
          <el-table-column prop="level" label="客户等级" width="100">
            <template #default="{ row }">
              {{ getLevelText(row.level) }}
            </template>
          </el-table-column>
          <el-table-column prop="points" label="积分" width="100" />
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        </el-table>
        <div class="step-buttons">
          <el-button @click="currentStep = 1">上一步</el-button>
          <el-button type="primary" @click="handleImport" :loading="importing">开始导入</el-button>
        </div>
      </div>

      <!-- 步骤4：完成导入 -->
      <div v-if="currentStep === 3" class="step-content">
        <el-result icon="success" title="导入完成" :sub-title="`成功导入 ${importSuccess} 条数据，失败 ${importFail} 条`">
          <template #extra>
            <el-button type="primary" @click="handleResetImport">继续导入</el-button>
            <el-button @click="closeImportDialog">关闭</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增客户')

// 导入相关变量
const importDialogVisible = ref(false)
const currentStep = ref(0)
const uploadFile = ref(null)
const previewData = ref([])
const importing = ref(false)
const importSuccess = ref(0)
const importFail = ref(0)

const searchForm = reactive({
  name: '',
  phone: '',
  level: '',
  status: ''
})

const form = reactive({
  id: null,
  name: '',
  phone: '',
  email: '',
  level: 1,
  points: 0,
  status: 1,
  remark: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tableData = ref([
  { id: 1, name: '张三', phone: '13800138001', level: 3, totalAmount: 12580, orderCount: 35, points: 2580, status: 1, createTime: '2024-01-01 10:00:00' },
  { id: 2, name: '李四', phone: '13800138002', level: 2, totalAmount: 8560, orderCount: 28, points: 1856, status: 1, createTime: '2024-01-02 11:00:00' },
  { id: 3, name: '王五', phone: '13800138003', level: 1, totalAmount: 3200, orderCount: 12, points: 650, status: 0, createTime: '2024-01-03 12:00:00' }
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
  Object.assign(searchForm, { name: '', phone: '', level: '', status: '' })
  fetchData()
}

const handleAdd = () => {
  dialogTitle.value = '新增客户'
  Object.assign(form, { id: null, name: '', phone: '', email: '', level: 1, points: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑客户'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage.info('查看客户详情：' + row.name)
}

const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '冻结' : '解冻'
  ElMessageBox.confirm(`确认${action}该客户？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该客户？', '提示', {
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

const getLevelType = (level) => {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[level] || ''
}

const getLevelText = (level) => {
  const map = { 1: '普通会员', 2: 'VIP会员', 3: 'SVIP会员' }
  return map[level] || '-'
}

// 导入相关方法
const showImportDialog = () => {
  importDialogVisible.value = true
  currentStep.value = 0
}

const downloadTemplate = () => {
  ElMessage.success('模板下载成功')
}

const handleFileChange = (file) => {
  uploadFile.value = file.raw
  ElMessage.success('文件选择成功')
}

const handleParse = () => {
  // 模拟解析Excel数据
  previewData.value = [
    { name: '张三', phone: '13800138001', email: 'zhangsan@example.com', level: 1, points: 100, remark: '测试客户 1' },
    { name: '李四', phone: '13800138002', email: 'lisi@example.com', level: 2, points: 500, remark: '测试客户 2' },
    { name: '王五', phone: '13800138003', email: 'wangwu@example.com', level: 3, points: 1000, remark: '测试客户 3' }
  ]
  currentStep.value = 2
  ElMessage.success('文件解析成功')
}

const handleImport = async () => {
  importing.value = true
  setTimeout(() => {
    importSuccess.value = previewData.value.length
    importFail.value = 0
    currentStep.value = 3
    importing.value = false
    ElMessage.success('导入完成')
    // 刷新列表
    fetchData()
  }, 2000)
}

const handleResetImport = () => {
  currentStep.value = 0
  uploadFile.value = null
  previewData.value = []
  importSuccess.value = 0
  importFail.value = 0
}

const closeImportDialog = () => {
  importDialogVisible.value = false
  handleResetImport()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.header-buttons { display: flex; gap: 10px; }
.search-form { margin-bottom: 20px; }
.pagination { margin-top: 20px; justify-content: flex-end; }

/* 导入对话框样式 */
.import-steps { margin: 30px 0; }
.step-content { padding: 30px 20px; min-height: 300px; }
.template-info { margin: 20px 0; padding: 20px; background: #f5f7fa; border-radius: 4px; }
.template-info h3 { margin-bottom: 15px; font-size: 14px; color: #303133; }
.template-info ul { padding-left: 20px; }
.template-info li { margin-bottom: 10px; line-height: 1.8; color: #606266; }
.upload-area { margin: 30px 0; }
.preview-table { margin: 20px 0; }
.step-buttons { margin-top: 30px; text-align: center; }
.step-buttons .el-button { margin: 0 10px; }
</style>
