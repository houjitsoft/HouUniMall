<template>
  <div class="security-api">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>API安全设置</span>
        </div>
      </template>
      
      <el-form :model="apiSecurityForm" label-width="150px">
        <el-form-item label="API访问限制">
          <el-input-number v-model="apiSecurityForm.rateLimit" :min="10" :max="1000" controls-position="right" />
          <span class="form-tip">（每分钟请求次数）</span>
        </el-form-item>
        
        <el-form-item label="API密钥认证">
          <el-switch v-model="apiSecurityForm.apiKeyAuth"></el-switch>
          <span class="form-tip">启用API密钥认证</span>
        </el-form-item>
        
        <el-form-item label="JWT Token过期时间">
          <el-input-number v-model="apiSecurityForm.tokenExpire" :min="15" :max="1440" controls-position="right" />
          <span class="form-tip">（分钟）</span>
        </el-form-item>
        
        <el-form-item label="跨域请求">
          <el-switch v-model="apiSecurityForm.corsEnabled"></el-switch>
          <span class="form-tip">允许跨域请求</span>
        </el-form-item>
        
        <el-form-item label="CORS域名">
          <el-input 
            v-model="apiSecurityForm.corsOrigins" 
            type="textarea" 
            :rows="3"
            placeholder="请输入允许的域名，多个域名用逗号分隔"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="API版本控制">
          <el-switch v-model="apiSecurityForm.versionControl"></el-switch>
          <span class="form-tip">启用API版本控制</span>
        </el-form-item>
        
        <el-form-item label="请求日志记录">
          <el-switch v-model="apiSecurityForm.requestLogging"></el-switch>
          <span class="form-tip">记录API请求日志</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveApiSecurity">保存设置</el-button>
          <el-button @click="resetApiSecurity">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- API密钥管理 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>API密钥管理</span>
          <el-button type="primary" @click="showAddKeyDialog">新增密钥</el-button>
        </div>
      </template>
      
      <el-table :data="apiKeys" style="width: 100%">
        <el-table-column prop="name" label="密钥名称" width="150"></el-table-column>
        <el-table-column prop="key" label="密钥" width="200">
          <template #default="scope">
            <span>{{ scope.row.key.substring(0, 8) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="permissions" label="权限" width="200">
          <template #default="scope">
            <el-tag v-for="perm in scope.row.permissions" :key="perm" size="small" style="margin-right: 5px;">
              {{ perm }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editKey(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteKey(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增/编辑密钥对话框 -->
    <el-dialog v-model="keyDialogVisible" :title="keyDialogTitle" width="600px">
      <el-form :model="keyForm" label-width="100px">
        <el-form-item label="密钥名称" required>
          <el-input v-model="keyForm.name" placeholder="请输入密钥名称"></el-input>
        </el-form-item>
        <el-form-item label="权限" required>
          <el-checkbox-group v-model="keyForm.permissions">
            <el-checkbox value="read">读取</el-checkbox>
            <el-checkbox value="write">写入</el-checkbox>
            <el-checkbox value="delete">删除</el-checkbox>
            <el-checkbox value="admin">管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="keyForm.status" :active-value="'active'" :inactive-value="'inactive'"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="keyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveKey">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const apiSecurityForm = reactive({
  rateLimit: 100,
  apiKeyAuth: true,
  tokenExpire: 60,
  corsEnabled: true,
  corsOrigins: 'http://localhost:3000,https://example.com',
  versionControl: true,
  requestLogging: true
})

const apiKeys = ref([
  {
    id: 1,
    name: 'Web应用密钥',
    key: 'ak_1234567890abcdef',
    permissions: ['read', 'write'],
    status: 'active',
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    name: '移动应用密钥',
    key: 'ak_abcdef1234567890',
    permissions: ['read'],
    status: 'active',
    createTime: '2024-01-01 10:00:00'
  }
])

const keyDialogVisible = ref(false)
const keyDialogTitle = ref('新增密钥')
const isEditKey = ref(false)

const keyForm = reactive({
  id: null,
  name: '',
  permissions: [],
  status: 'active'
})

const saveApiSecurity = () => {
  ElMessage.success('API安全设置保存成功')
}

const resetApiSecurity = () => {
  apiSecurityForm.rateLimit = 100
  apiSecurityForm.apiKeyAuth = true
  apiSecurityForm.tokenExpire = 60
  apiSecurityForm.corsEnabled = true
  apiSecurityForm.corsOrigins = 'http://localhost:3000,https://example.com'
  apiSecurityForm.versionControl = true
  apiSecurityForm.requestLogging = true
  ElMessage.info('设置已重置')
}

const showAddKeyDialog = () => {
  keyDialogTitle.value = '新增密钥'
  isEditKey.value = false
  resetKeyForm()
  keyDialogVisible.value = true
}

const editKey = (row) => {
  keyDialogTitle.value = '编辑密钥'
  isEditKey.value = true
  Object.assign(keyForm, row)
  keyDialogVisible.value = true
}

const deleteKey = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个API密钥吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = apiKeys.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      apiKeys.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {
    ElMessage.info('已取消删除')
  }
}

const saveKey = () => {
  if (!keyForm.name || keyForm.permissions.length === 0) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (isEditKey.value) {
    const index = apiKeys.value.findIndex(item => item.id === keyForm.id)
    if (index > -1) {
      apiKeys.value[index] = { ...keyForm }
    }
    ElMessage.success('编辑成功')
  } else {
    keyForm.id = Date.now()
    keyForm.key = 'ak_' + Math.random().toString(36).substr(2, 16)
    keyForm.createTime = new Date().toLocaleString()
    apiKeys.value.push({ ...keyForm })
    ElMessage.success('新增成功')
  }
  
  keyDialogVisible.value = false
}

const resetKeyForm = () => {
  Object.assign(keyForm, {
    id: null,
    name: '',
    permissions: [],
    status: 'active'
  })
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.security-api {
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
