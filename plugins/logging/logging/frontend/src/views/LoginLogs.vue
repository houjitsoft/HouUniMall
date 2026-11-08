<template>
  <div class="login-logs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>登录日志</span>
          <el-tag type="primary">审计库 MySQL</el-tag>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.loginStatus" clearable placeholder="全部">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP">
          <el-input v-model="searchForm.ip" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="logList" max-height="520">
        <el-table-column prop="loginTime" label="时间" width="170" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="loginType" label="方式" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="160" show-overflow-tooltip />
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pager"
        @size-change="fetchLogs"
        @current-change="fetchLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { loginLogAPI } from '@/api/log/index.js'
import { unwrapPage, mapLoginRow } from '@/utils/logApi.js'

const searchForm = reactive({ username: '', loginStatus: '', ip: '', dateRange: [] })
const logList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const buildParams = () => {
  const p = {}
  if (searchForm.username) p.username = searchForm.username
  if (searchForm.loginStatus === 'success') p.loginStatus = 1
  else if (searchForm.loginStatus === 'fail') p.loginStatus = 0
  if (searchForm.ip) p.ip = searchForm.ip
  if (searchForm.dateRange?.length === 2) {
    p.startTime = searchForm.dateRange[0]
    p.endTime = searchForm.dateRange[1]
  }
  return p
}

const fetchLogs = async () => {
  loading.value = true
  try {
    const page = unwrapPage(await loginLogAPI.getPage(buildParams(), currentPage.value, pageSize.value))
    logList.value = (page.records || []).map(mapLoginRow)
    total.value = page.total || 0
  } catch (e) {
    ElMessage.error(e.message || '查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; fetchLogs() }
const handleReset = () => {
  Object.assign(searchForm, { username: '', loginStatus: '', ip: '', dateRange: [] })
  currentPage.value = 1
  fetchLogs()
}

onMounted(fetchLogs)
</script>

<style scoped>
.login-logs { padding: 20px; }
.card-header { display: flex; align-items: center; gap: 12px; }
.search-form { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
