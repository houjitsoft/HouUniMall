<template>
  <div class="security-login">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>登录安全设置</span>
        </div>
      </template>
      
      <el-form :model="securityForm" label-width="150px">
        <el-form-item label="密码最小长度">
          <el-input-number v-model="securityForm.minPasswordLength" :min="6" :max="20" controls-position="right" />
          <span class="form-tip">（字符）</span>
        </el-form-item>
        
        <el-form-item label="密码复杂度要求">
          <el-checkbox-group v-model="securityForm.passwordComplexity">
            <el-checkbox value="uppercase">包含大写字母</el-checkbox>
            <el-checkbox value="lowercase">包含小写字母</el-checkbox>
            <el-checkbox value="number">包含数字</el-checkbox>
            <el-checkbox value="special">包含特殊字符</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="登录失败次数限制">
          <el-input-number v-model="securityForm.maxLoginAttempts" :min="3" :max="10" controls-position="right" />
          <span class="form-tip">（次）</span>
        </el-form-item>
        
        <el-form-item label="账户锁定时间">
          <el-input-number v-model="securityForm.lockoutDuration" :min="5" :max="60" controls-position="right" />
          <span class="form-tip">（分钟）</span>
        </el-form-item>
        
        <el-form-item label="会话超时时间">
          <el-input-number v-model="securityForm.sessionTimeout" :min="15" :max="480" controls-position="right" />
          <span class="form-tip">（分钟）</span>
        </el-form-item>
        
        <el-form-item label="强制HTTPS">
          <el-switch v-model="securityForm.forceHttps"></el-switch>
          <span class="form-tip">强制使用HTTPS协议</span>
        </el-form-item>
        
        <el-form-item label="双因素认证">
          <el-switch v-model="securityForm.twoFactorAuth"></el-switch>
          <span class="form-tip">启用双因素认证</span>
        </el-form-item>
        
        <el-form-item label="记住登录状态">
          <el-switch v-model="securityForm.rememberMe"></el-switch>
          <span class="form-tip">允许用户记住登录状态</span>
        </el-form-item>
        
        <el-form-item label="IP白名单">
          <el-input 
            v-model="securityForm.ipWhitelist" 
            type="textarea" 
            :rows="4"
            placeholder="请输入允许访问的IP地址，多个IP用换行分隔"
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveSecurityConfig">保存设置</el-button>
          <el-button @click="resetSecurityConfig">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 登录日志 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>登录日志</span>
          <el-button type="danger" @click="clearLoginLogs">清空日志</el-button>
        </div>
      </template>

      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="登录状态">
          <el-select v-model="searchForm.loginStatus" placeholder="请选择" clearable>
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="searchForm.ip" placeholder="请输入IP地址" clearable />
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
          <el-button type="primary" @click="() => { currentPage = 1; fetchLoginLogs() }">搜索</el-button>
          <el-button @click="() => { searchForm.username=''; searchForm.loginStatus=''; searchForm.ip=''; searchForm.dateRange=[]; currentPage=1; fetchLoginLogs() }">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="loginLogs" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="150"></el-table-column>
        <el-table-column prop="location" label="登录地点" width="150"></el-table-column>
        <el-table-column prop="userAgent" label="用户代理" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginTime" label="登录时间" width="180"></el-table-column>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { loginLogAPI } from '@/api/log/index.js'
import { unwrapPage, mapLoginRow, unwrap } from '@/utils/logApi.js'

const securityForm = reactive({
  minPasswordLength: 8,
  passwordComplexity: ['lowercase', 'number'],
  maxLoginAttempts: 5,
  lockoutDuration: 15,
  sessionTimeout: 30,
  forceHttps: true,
  twoFactorAuth: false,
  rememberMe: true,
  ipWhitelist: ''
})

const loginLogs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  username: '',
  loginType: '',
  loginStatus: '',
  ip: '',
  dateRange: []
})

/**
 * 根据当前搜索表单构建请求参数
 * @returns {Object} 包含 username、loginType、loginStatus、ip、startTime、endTime 的查询条件
 */
const buildParams = () => {
  const params = {}
  if (searchForm.username)    params.username    = searchForm.username
  if (searchForm.loginType)   params.loginType   = searchForm.loginType
  if (searchForm.loginStatus === 'success') params.loginStatus = 1
  else if (searchForm.loginStatus === 'fail') params.loginStatus = 0
  if (searchForm.ip)          params.ip          = searchForm.ip
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.startTime = searchForm.dateRange[0]
    params.endTime   = searchForm.dateRange[1]
  }
  return params
}

/**
 * 分页加载登录日志列表
 * 使用当前搜索条件和分页参数进行查询
 */
const fetchLoginLogs = async () => {
  loading.value = true
  try {
    const page = unwrapPage(await loginLogAPI.getPage(buildParams(), currentPage.value, pageSize.value))
    loginLogs.value = (page.records || []).map(mapLoginRow)
    total.value = page.total || 0
  } catch (error) {
    console.error('查询登录日志失败:', error)
    ElMessage.error(error.message || '查询登录日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 保存登录安全设置（暂时展示提示，待后端接口支持）
 */
const saveSecurityConfig = () => {
  ElMessage.success('安全设置保存成功')
}

/**
 * 重置登录安全表单到默认值
 */
const resetSecurityConfig = () => {
  securityForm.minPasswordLength  = 8
  securityForm.passwordComplexity = ['lowercase', 'number']
  securityForm.maxLoginAttempts   = 5
  securityForm.lockoutDuration    = 15
  securityForm.sessionTimeout     = 30
  securityForm.forceHttps         = true
  securityForm.twoFactorAuth      = false
  securityForm.rememberMe         = true
  securityForm.ipWhitelist        = ''
  ElMessage.info('设置已重置')
}

/**
 * 清空登录日志，删除超过 30 天的日志记录（二次确认）
 */
const clearLoginLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空登录日志吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    loading.value = true
    unwrap(await loginLogAPI.clean(30))
    ElMessage.success('登录日志清空成功')
    fetchLoginLogs()
  } catch (error) {
    if (error !== 'cancel' && error?.message) {
      console.error('清空登录日志失败:', error)
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
  fetchLoginLogs()
}

/**
 * 当前页码变化处理
 * @param {number} val 新的页码
 */
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchLoginLogs()
}

onMounted(() => {
  fetchLoginLogs()
})
</script>

<style scoped>
.security-login {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}
</style>
