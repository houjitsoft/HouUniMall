<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="公司名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入公司名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="全部" value="" />
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleAdd">
            <el-icon><Plus /></el-icon>添加物流公司
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 物流公司列表 -->
    <el-card shadow="never">
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="name" label="物流公司" width="150">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 10px">
              <el-image
                v-if="row.logo"
                :src="row.logo"
                fit="contain"
                style="width: 40px; height: 30px"
              />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="公司代码" width="120" />
        <el-table-column prop="website" label="官网地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <a :href="row.website" target="_blank" class="link">{{ row.website }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="客服电话" width="130" />
        <el-table-column label="API配置" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.hasApi ? 'success' : 'info'" size="small">
              {{ row.hasApi ? '已配置' : '未配置' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="电子面单" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.hasSheet ? 'success' : 'info'" size="small">
              {{ row.hasSheet ? '支持' : '不支持' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="monthlyCount" label="本月发货量" width="110" align="right">
          <template #default="{ row }">
            <span class="number-highlight">{{ row.monthlyCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="signRate" label="签收率" width="100">
          <template #default="{ row }">
            <el-progress
              :percentage="row.signRate"
              :color="getSignRateColor(row.signRate)"
              :stroke-width="12"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleApiConfig(row)">
              <el-icon><Setting /></el-icon>API配置
            </el-button>
            <el-button link type="primary" size="small" @click="handleTest(row)">
              <el-icon><Connection /></el-icon>测试
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
    >
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物流公司" required>
              <el-input v-model="form.name" placeholder="请输入物流公司名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司代码" required>
              <el-input v-model="form.code" placeholder="请输入公司代码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="LOGO">
          <el-input v-model="form.logo" placeholder="请输入LOGO地址">
            <template #append>
              <el-button @click="handleUploadLogo">上传</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="官网地址">
              <el-input v-model="form.website" placeholder="https://" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客服电话">
              <el-input v-model="form.phone" placeholder="请输入客服电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电子面单">
              <el-switch v-model="form.hasSheet" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- API配置对话框 -->
    <el-dialog
      v-model="apiDialogVisible"
      title="API配置"
      width="600px"
    >
      <el-alert
        title="配置物流公司API接口，用于自动获取物流轨迹信息"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />
      <el-form :model="apiForm" label-width="120px">
        <el-form-item label="API地址" required>
          <el-input v-model="apiForm.apiUrl" placeholder="https://api.example.com" />
        </el-form-item>
        <el-form-item label="App Key" required>
          <el-input v-model="apiForm.appKey" placeholder="请输入App Key" />
        </el-form-item>
        <el-form-item label="App Secret" required>
          <el-input
            v-model="apiForm.appSecret"
            type="password"
            placeholder="请输入App Secret"
            show-password
          />
        </el-form-item>
        <el-form-item label="请求方式">
          <el-radio-group v-model="apiForm.method">
            <el-radio value="GET">GET</el-radio>
            <el-radio value="POST">POST</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="超时时间">
          <el-input-number v-model="apiForm.timeout" :min="1" :max="60" />
          <span style="margin-left: 10px; font-size: 12px; color: #909399">秒</span>
        </el-form-item>
        <el-form-item label="自动更新">
          <el-switch v-model="apiForm.autoUpdate" />
          <span style="margin-left: 10px; font-size: 12px; color: #909399">
            开启后将自动同步物流轨迹
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="apiDialogVisible = false">取消</el-button>
        <el-button @click="handleTestApi">测试连接</el-button>
        <el-button type="primary" @click="handleSaveApi">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, RefreshRight, Plus, Edit, Setting, Connection, Delete
} from '@element-plus/icons-vue'

const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表格数据
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加物流公司')
const form = reactive({
  id: null,
  name: '',
  code: '',
  logo: '',
  website: '',
  phone: '',
  hasSheet: false,
  sort: 0,
  remark: '',
  enabled: true
})

// API配置对话框
const apiDialogVisible = ref(false)
const apiForm = reactive({
  id: null,
  apiUrl: '',
  appKey: '',
  appSecret: '',
  method: 'POST',
  timeout: 30,
  autoUpdate: true
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // TODO: 调用接口获取数据
    // 模拟数据
    tableData.value = [
      {
        id: 1,
        name: '顺丰速运',
        code: 'shunfeng',
        logo: getPlaceholderDataUri(40, 30),
        website: 'https://www.sf-express.com',
        phone: '95338',
        hasApi: true,
        hasSheet: true,
        monthlyCount: 1256,
        signRate: 98,
        enabled: true,
        sort: 1
      },
      {
        id: 2,
        name: '中通快递',
        code: 'zhongtong',
        logo: getPlaceholderDataUri(40, 30),
        website: 'https://www.zto.com',
        phone: '95311',
        hasApi: true,
        hasSheet: true,
        monthlyCount: 986,
        signRate: 96,
        enabled: true,
        sort: 2
      },
      {
        id: 3,
        name: '圆通速递',
        code: 'yuantong',
        logo: getPlaceholderDataUri(40, 30),
        website: 'https://www.yto.net.cn',
        phone: '95554',
        hasApi: true,
        hasSheet: true,
        monthlyCount: 745,
        signRate: 94,
        enabled: true,
        sort: 3
      },
      {
        id: 4,
        name: '韵达快递',
        code: 'yunda',
        logo: getPlaceholderDataUri(40, 30),
        website: 'https://www.yunda56.com',
        phone: '95546',
        hasApi: false,
        hasSheet: true,
        monthlyCount: 352,
        signRate: 93,
        enabled: true,
        sort: 4
      },
      {
        id: 5,
        name: '申通快递',
        code: 'shentong',
        logo: getPlaceholderDataUri(40, 30),
        website: 'https://www.sto.cn',
        phone: '95543',
        hasApi: false,
        hasSheet: false,
        monthlyCount: 218,
        signRate: 91,
        enabled: false,
        sort: 5
      }
    ]
    pagination.total = 5
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    name: '',
    status: ''
  })
  handleSearch()
}

// 添加
const handleAdd = () => {
  dialogTitle.value = '添加物流公司'
  Object.assign(form, {
    id: null,
    name: '',
    code: '',
    logo: '',
    website: '',
    phone: '',
    hasSheet: false,
    sort: 0,
    remark: '',
    enabled: true
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑物流公司'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 保存
const handleSave = () => {
  // TODO: 调用接口保存
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

// 切换状态
const handleToggleStatus = (row) => {
  ElMessage.success(row.enabled ? '已启用' : '已禁用')
}

// API配置
const handleApiConfig = (row) => {
  apiForm.id = row.id
  apiDialogVisible.value = true
}

// 测试API
const handleTestApi = () => {
  ElMessage.info('正在测试连接...')
  setTimeout(() => {
    ElMessage.success('连接测试成功')
  }, 1000)
}

// 保存API配置
const handleSaveApi = () => {
  ElMessage.success('API配置已保存')
  apiDialogVisible.value = false
}

// 测试连接
const handleTest = (row) => {
  ElMessage.info(`正在测试 ${row.name} 连接...`)
  setTimeout(() => {
    ElMessage.success('连接测试成功')
  }, 1000)
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除物流公司"${row.name}"吗？`, '确认删除', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 上传LOGO
const handleUploadLogo = () => {
  ElMessage.info('打开上传对话框')
}

// 获取签收率颜色
const getSignRateColor = (signRate) => {
  if (signRate >= 95) return '#67c23a'
  if (signRate >= 90) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.link {
  color: #409eff;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.number-highlight {
  color: #409eff;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
