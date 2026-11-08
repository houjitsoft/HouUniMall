<template>
  <div class="security-ip">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>IP安全设置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="IP白名单" name="whitelist">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="showAddWhitelistDialog = true">
                <el-icon><Plus /></el-icon>
                添加IP
              </el-button>
              <el-button type="danger" @click="deleteSelectedWhitelist" :disabled="selectedWhitelist.length === 0">
                删除选中
              </el-button>
            </div>
            
            <el-table 
              :data="whitelist" 
              @selection-change="handleWhitelistSelectionChange"
              style="width: 100%"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="ip" label="IP地址" width="200" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="editWhitelist(row)">
                    编辑
                  </el-button>
                  <el-button size="small" type="danger" link @click="deleteWhitelist(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="IP黑名单" name="blacklist">
          <div class="tab-content">
            <div class="toolbar">
              <el-button type="primary" @click="showAddBlacklistDialog = true">
                <el-icon><Plus /></el-icon>
                添加IP
              </el-button>
              <el-button type="danger" @click="deleteSelectedBlacklist" :disabled="selectedBlacklist.length === 0">
                删除选中
              </el-button>
            </div>
            
            <el-table 
              :data="blacklist" 
              @selection-change="handleBlacklistSelectionChange"
              style="width: 100%"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="ip" label="IP地址" width="200" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column prop="blockCount" label="拦截次数" width="100" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="editBlacklist(row)">
                    编辑
                  </el-button>
                  <el-button size="small" type="danger" link @click="deleteBlacklist(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="访问限制" name="access">
          <div class="tab-content">
            <el-form :model="accessForm" label-width="120px">
              <el-form-item label="启用IP限制">
                <el-switch v-model="accessForm.enableIPRestriction" />
              </el-form-item>
              
              <el-form-item label="限制策略">
                <el-radio-group v-model="accessForm.restrictionStrategy">
                  <el-radio label="whitelist">仅允许白名单IP访问</el-radio>
                  <el-radio label="blacklist">禁止黑名单IP访问</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="异常处理">
                <el-select v-model="accessForm.blockAction">
                  <el-option label="返回403错误" value="403" />
                  <el-option label="跳转到提示页面" value="redirect" />
                  <el-option label="记录日志并允许访问" value="log" />
                </el-select>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveAccessSettings">保存设置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 添加白名单对话框 -->
    <el-dialog v-model="showAddWhitelistDialog" title="添加IP白名单" width="500px">
      <el-form :model="whitelistForm" :rules="whitelistRules" ref="whitelistFormRef">
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="whitelistForm.ip" placeholder="请输入IP地址或IP段" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="whitelistForm.description" placeholder="请输入描述信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddWhitelistDialog = false">取消</el-button>
        <el-button type="primary" @click="addWhitelist">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 添加黑名单对话框 -->
    <el-dialog v-model="showAddBlacklistDialog" title="添加IP黑名单" width="500px">
      <el-form :model="blacklistForm" :rules="blacklistRules" ref="blacklistFormRef">
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="blacklistForm.ip" placeholder="请输入IP地址或IP段" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="blacklistForm.description" placeholder="请输入描述信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddBlacklistDialog = false">取消</el-button>
        <el-button type="primary" @click="addBlacklist">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('whitelist')

// 白名单相关
const showAddWhitelistDialog = ref(false)
const selectedWhitelist = ref([])
const whitelistFormRef = ref()
const whitelistForm = reactive({
  ip: '',
  description: ''
})

const whitelistRules = {
  ip: [
    { required: true, message: '请输入IP地址', trigger: 'blur' }
  ]
}

const whitelist = ref([
  {
    id: 1,
    ip: '192.168.1.100',
    description: '管理员办公网段',
    createTime: '2024-01-08 10:00:00'
  },
  {
    id: 2,
    ip: '10.0.0.0/8',
    description: '内网访问',
    createTime: '2024-01-08 10:00:00'
  }
])

// 黑名单相关
const showAddBlacklistDialog = ref(false)
const selectedBlacklist = ref([])
const blacklistFormRef = ref()
const blacklistForm = reactive({
  ip: '',
  description: ''
})

const blacklistRules = {
  ip: [
    { required: true, message: '请输入IP地址', trigger: 'blur' }
  ]
}

const blacklist = ref([
  {
    id: 1,
    ip: '192.168.1.200',
    description: '恶意扫描IP',
    createTime: '2024-01-08 10:00:00',
    blockCount: 128
  }
])

// 访问限制相关
const accessForm = reactive({
  enableIPRestriction: true,
  restrictionStrategy: 'whitelist',
  blockAction: '403'
})

// 白名单事件处理
const handleWhitelistSelectionChange = (selection) => {
  selectedWhitelist.value = selection
}

const editWhitelist = (row) => {
  ElMessage.info('编辑功能开发中...')
}

const deleteWhitelist = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除IP ${row.ip} 吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}

const deleteSelectedWhitelist = async () => {
  if (selectedWhitelist.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedWhitelist.value.length} 个IP吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}

const addWhitelist = async () => {
  if (!whitelistFormRef.value) return
  
  try {
    await whitelistFormRef.value.validate()
    ElMessage.success('添加成功')
    showAddWhitelistDialog.value = false
    whitelistFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

// 黑名单事件处理
const handleBlacklistSelectionChange = (selection) => {
  selectedBlacklist.value = selection
}

const editBlacklist = (row) => {
  ElMessage.info('编辑功能开发中...')
}

const deleteBlacklist = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除IP ${row.ip} 吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}

const deleteSelectedBlacklist = async () => {
  if (selectedBlacklist.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedBlacklist.value.length} 个IP吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}

const addBlacklist = async () => {
  if (!blacklistFormRef.value) return
  
  try {
    await blacklistFormRef.value.validate()
    ElMessage.success('添加成功')
    showAddBlacklistDialog.value = false
    blacklistFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

// 访问限制设置
const saveAccessSettings = () => {
  ElMessage.success('访问限制设置保存成功')
}
</script>

<style scoped>
.security-ip {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar {
  margin-bottom: 20px;
}

.tab-content {
  padding: 20px 0;
}
</style>
