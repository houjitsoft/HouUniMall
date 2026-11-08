<template>
  <div class="business-logs">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>业务日志</span>
          <el-tag type="primary">审计库 MySQL</el-tag>
          <el-button type="danger" @click="clearBusinessLogs">清空业务日志</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="业务模块">
          <el-select v-model="searchForm.module" placeholder="请选择业务模块" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="用户管理" value="user"></el-option>
            <el-option label="订单管理" value="order"></el-option>
            <el-option label="商品管理" value="goods"></el-option>
            <el-option label="支付管理" value="payment"></el-option>
            <el-option label="库存管理" value="inventory"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.action" placeholder="请选择操作类型" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="新增" value="create"></el-option>
            <el-option label="修改" value="update"></el-option>
            <el-option label="删除" value="delete"></el-option>
            <el-option label="查询" value="query"></el-option>
            <el-option label="导出" value="export"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable></el-input>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchLogs">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="logList" v-loading="loading" style="width: 100%" max-height="500">
        <el-table-column prop="timestamp" label="时间" width="180"></el-table-column>
        <el-table-column prop="module" label="业务模块" width="120">
          <template #default="scope">
            <el-tag :type="getModuleTagType(scope.row.module)">
              {{ getModuleName(scope.row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="scope">
            <el-tag :type="getActionTagType(scope.row.action)">
              {{ getActionName(scope.row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="120"></el-table-column>
        <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200"></el-table-column>
        <el-table-column prop="result" label="执行结果" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'">
              {{ scope.row.result }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="130"></el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>
    
    <!-- 业务日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="业务日志详情" width="70%">
      <div class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">{{ logDetail.timestamp }}</el-descriptions-item>
          <el-descriptions-item label="业务模块">
            <el-tag :type="getModuleTagType(logDetail.module)">
              {{ getModuleName(logDetail.module) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getActionTagType(logDetail.action)">
              {{ getActionName(logDetail.action) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行结果">
            <el-tag :type="logDetail.result === 'SUCCESS' ? 'success' : 'danger'">
              {{ logDetail.result }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ logDetail.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ logDetail.userName }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ logDetail.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="用户代理">{{ logDetail.userAgent }}</el-descriptions-item>
          <el-descriptions-item label="操作描述" :span="2">{{ logDetail.description }}</el-descriptions-item>
          <el-descriptions-item label="请求参数" :span="2">
            <pre class="json-content">{{ formatJson(logDetail.requestParams) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="响应数据" :span="2">
            <pre class="json-content">{{ formatJson(logDetail.responseData) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { businessLogAPI } from '@/api/log/index.js'
import { unwrapPage, mapBusinessRow, unwrap } from '@/utils/logApi.js'

const searchForm = reactive({
  module: '',
  action: '',
  userId: '',
  dateRange: []
})

const logList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const detailVisible = ref(false)
const logDetail = ref({})

/**
 * 根据当前搜索表单构建请求参数
 * 返回包含 username、module、action、startTime、endTime 等字段的对象
 * @returns {Object} 查询条件对象
 */
const buildParams = () => {
  const params = {}
  if (searchForm.module)  params.module  = searchForm.module
  if (searchForm.action)  params.action  = searchForm.action
  if (searchForm.userId)  params.username = searchForm.userId
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startTime = searchForm.dateRange[0]
    params.endTime   = searchForm.dateRange[1]
  }
  return params
}

/**
 * 分页加载业务日志列表
 * 使用当前搜索条件和分页参数进行查询
 */
const fetchLogs = async () => {
  loading.value = true
  try {
    const params = buildParams()
    const page = unwrapPage(await businessLogAPI.getPage(params, currentPage.value, pageSize.value))
    logList.value = (page.records || []).map(mapBusinessRow)
    total.value = page.total || 0
  } catch (error) {
    console.error('查询业务日志失败:', error)
    ElMessage.error(error.message || '查询业务日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const getModuleTagType = (module) => {
  const typeMap = {
    'user': 'success',
    'order': 'primary',
    'goods': 'warning',
    'payment': 'danger',
    'inventory': 'info'
  }
  return typeMap[module] || ''
}

const getModuleName = (module) => {
  const nameMap = {
    'user': '用户管理',
    'order': '订单管理',
    'goods': '商品管理',
    'payment': '支付管理',
    'inventory': '库存管理'
  }
  return nameMap[module] || module
}

const getActionTagType = (action) => {
  const typeMap = {
    'create': 'success',
    'update': 'primary',
    'delete': 'danger',
    'query': 'info',
    'export': 'warning'
  }
  return typeMap[action] || ''
}

const getActionName = (action) => {
  const nameMap = {
    'create': '新增',
    'update': '修改',
    'delete': '删除',
    'query': '查询',
    'export': '导出'
  }
  return nameMap[action] || action
}

/**
 * 将对象格式化为 JSON 字符串并缩进，用于详情面板展示
 * @param {Object|string} obj 待格式化的对象
 * @returns {string} 格式化后的 JSON 字符串
 */
const formatJson = (obj) => {
  if (!obj) return ''
  try {
    return typeof obj === 'string' ? obj : JSON.stringify(obj, null, 2)
  } catch {
    return String(obj)
  }
}

/**
 * 点击搜索按鈕，重置页码后重新加载
 */
const searchLogs = () => {
  currentPage.value = 1
  fetchLogs()
}

/**
 * 重置搜索表单并重新加载数据
 */
const resetSearch = () => {
  searchForm.module    = ''
  searchForm.action    = ''
  searchForm.userId    = ''
  searchForm.dateRange = []
  currentPage.value    = 1
  fetchLogs()
}

/**
 * 查看详情，将当前行数据赋值并显示详情对话框
 * @param {Object} row 当前行数据
 */
const viewDetail = (row) => {
  logDetail.value  = row
  detailVisible.value = true
}

/**
 * 清空业务日志，删除超过 30 天的日志记录（二次确认）
 */
const clearBusinessLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有业务日志吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    loading.value = true
    const res = await businessLogAPI.clean(30)
    unwrap(res)
    ElMessage.success('业务日志清空成功')
    fetchLogs()
  } catch (error) {
    if (error !== 'cancel' && error?.message) {
      console.error('清空业务日志失败:', error)
      ElMessage.error('清空失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 分页尺寸变化处理
 * @param {number} val 新的每页条数
 */
const handleSizeChange = (val) => {
  pageSize.value    = val
  currentPage.value = 1
  fetchLogs()
}

/**
 * 当前页码变化处理
 * @param {number} val 新的页码
 */
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.business-logs {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.log-detail {
  padding: 20px;
}

.json-content {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  max-height: 200px;
  overflow-y: auto;
}
</style>
