<template>
  <div class="import-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>导入配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="数据导入" name="data">
          <div class="tab-content">
            <el-alert
              title="注意：导入操作会覆盖现有数据，请谨慎操作"
              type="warning"
              show-icon
              style="margin-bottom: 20px;"
            />
            
            <el-form :model="importForm" label-width="120px">
              <el-form-item label="选择文件">
                <el-upload
                  v-model:file-list="importForm.fileList"
                  class="upload-demo"
                  action=""
                  :auto-upload="false"
                  :limit="1"
                  accept=".xlsx,.xls,.csv,.json,.xml"
                >
                  <el-button type="primary">选择文件</el-button>
                  <template #tip>
                    <div class="el-upload__tip">
                      支持格式：Excel、CSV、JSON、XML，文件大小不超过100MB
                    </div>
                  </template>
                </el-upload>
              </el-form-item>
              
              <el-form-item label="导入模式">
                <el-radio-group v-model="importForm.mode">
                  <el-radio label="overwrite">覆盖导入</el-radio>
                  <el-radio label="append">追加导入</el-radio>
                  <el-radio label="skip">跳过重复</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="数据验证">
                <el-switch v-model="importForm.validate" />
                <span class="form-tip">导入前是否进行数据验证</span>
              </el-form-item>
              
              <el-form-item v-if="importForm.validate" label="错误处理">
                <el-radio-group v-model="importForm.errorHandling">
                  <el-radio label="stop">遇到错误停止导入</el-radio>
                  <el-radio label="continue">跳过错误继续导入</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="importData" :loading="importing">
                  开始导入
                </el-button>
                <el-button @click="previewData" :disabled="importForm.fileList.length === 0">
                  预览数据
                </el-button>
              </el-form-item>
            </el-form>
            
            <!-- 导入进度 -->
            <div v-if="importing" class="import-progress">
              <el-progress :percentage="importProgress" :status="importStatus" />
              <p>{{ importMessage }}</p>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="配置导入" name="config">
          <div class="tab-content">
            <el-alert
              title="警告：导入配置文件会覆盖当前系统配置，请确保配置文件来源可靠"
              type="error"
              show-icon
              style="margin-bottom: 20px;"
            />
            
            <el-form :model="configImportForm" label-width="120px">
              <el-form-item label="选择配置文件">
                <el-upload
                  v-model:file-list="configImportForm.fileList"
                  class="upload-demo"
                  action=""
                  :auto-upload="false"
                  :limit="1"
                  accept=".json"
                >
                  <el-button type="primary">选择配置文件</el-button>
                  <template #tip>
                    <div class="el-upload__tip">
                      仅支持JSON格式的配置文件
                    </div>
                  </template>
                </el-upload>
              </el-form-item>
              
              <el-form-item label="备份当前配置">
                <el-switch v-model="configImportForm.backup" />
                <span class="form-tip">导入前是否备份当前配置</span>
              </el-form-item>
              
              <el-form-item v-if="configImportForm.backup" label="备份文件名">
                <el-input v-model="configImportForm.backupName" placeholder="请输入备份文件名" />
              </el-form-item>
              
              <el-form-item label="加密配置">
                <el-switch v-model="configImportForm.encrypted" />
                <span class="form-tip">配置文件是否经过加密</span>
              </el-form-item>
              
              <el-form-item v-if="configImportForm.encrypted" label="解密密码">
                <el-input v-model="configImportForm.password" type="password" placeholder="请输入解密密码" />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="importConfig" :loading="configImporting">
                  导入配置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="导入记录" name="history">
          <div class="tab-content">
            <div class="toolbar">
              <el-button @click="refreshHistory">刷新</el-button>
              <el-button type="danger" @click="clearHistory">清空记录</el-button>
            </div>
            
            <el-table :data="importHistory" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="fileName" label="文件名" min-width="200" />
              <el-table-column prop="type" label="类型" width="120" />
              <el-table-column prop="recordCount" label="记录数" width="100" />
              <el-table-column prop="createTime" label="导入时间" width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'success' ? 'success' : (row.status === 'failed' ? 'danger' : 'warning')">
                    {{ getImportStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="结果信息" min-width="200" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="viewDetail(row)">
                    查看详情
                  </el-button>
                  <el-button size="small" type="danger" link @click="deleteRecord(row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 数据预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="数据预览" width="80%">
      <el-table :data="previewDataList" max-height="400">
        <el-table-column
          v-for="column in previewColumns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
        />
      </el-table>
      <template #footer>
        <el-button @click="showPreviewDialog = false">关闭</el-button>
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

const activeTab = ref('data')

// 数据导入相关
const importing = ref(false)
const importProgress = ref(0)
const importStatus = ref('')
const importMessage = ref('')

const importForm = reactive({
  fileList: [],
  mode: 'overwrite',
  validate: true,
  errorHandling: 'stop'
})

// 配置导入相关
const configImporting = ref(false)

const configImportForm = reactive({
  fileList: [],
  backup: true,
  backupName: '',
  encrypted: false,
  password: ''
})

// 导入历史记录
const importHistory = ref([
  {
    id: 1,
    fileName: 'user_data_20240108.xlsx',
    type: '数据导入',
    recordCount: 1250,
    createTime: '2024-01-08 10:30:00',
    status: 'success',
    message: '导入成功'
  },
  {
    id: 2,
    fileName: 'system_config_20240108.json',
    type: '配置导入',
    recordCount: 0,
    createTime: '2024-01-08 09:15:00',
    status: 'failed',
    message: '配置文件格式错误'
  }
])

// 数据预览相关
const showPreviewDialog = ref(false)
const previewDataList = ref([])
const previewColumns = ref([])

// 导入数据
const importData = async () => {
  if (importForm.fileList.length === 0) {
    ElMessage.error('请选择要导入的文件')
    return
  }
  
  importing.value = true
  importStatus.value = ''
  importMessage.value = '正在上传文件...'
  
  try {
    // 模拟导入过程
    for (let i = 0; i <= 100; i += 10) {
      importProgress.value = i
      importMessage.value = `正在导入数据... ${i}%`
      await new Promise(resolve => setTimeout(resolve, 200))
    }
    
    importStatus.value = 'success'
    importMessage.value = '数据导入完成'
    ElMessage.success('数据导入成功')
  } catch (error) {
    importStatus.value = 'exception'
    importMessage.value = '导入失败: ' + error.message
    ElMessage.error('数据导入失败: ' + error.message)
  } finally {
    setTimeout(() => {
      importing.value = false
    }, 2000)
  }
}

// 预览数据
const previewData = () => {
  // 模拟预览数据
  previewColumns.value = [
    { prop: 'id', label: 'ID', width: 80 },
    { prop: 'name', label: '姓名', width: 120 },
    { prop: 'email', label: '邮箱', width: 200 },
    { prop: 'department', label: '部门', width: 120 }
  ]
  
  previewDataList.value = [
    { id: 1, name: '张三', email: 'zhangsan@example.com', department: '技术部' },
    { id: 2, name: '李四', email: 'lisi@example.com', department: '产品部' },
    { id: 3, name: '王五', email: 'wangwu@example.com', department: '设计部' }
  ]
  
  showPreviewDialog.value = true
}

// 导入配置
const importConfig = async () => {
  if (configImportForm.fileList.length === 0) {
    ElMessage.error('请选择要导入的配置文件')
    return
  }
  
  if (configImportForm.encrypted && !configImportForm.password) {
    ElMessage.error('请输入解密密码')
    return
  }
  
  configImporting.value = true
  
  try {
    // 模拟导入配置过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('配置导入成功')
  } catch (error) {
    ElMessage.error('配置导入失败: ' + error.message)
  } finally {
    configImporting.value = false
  }
}

// 刷新历史记录
const refreshHistory = () => {
  ElMessage.success('刷新导入记录')
}

// 清空历史记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有导入记录吗？', '确认清空', {
      type: 'warning'
    })
    importHistory.value = []
    ElMessage.success('导入记录已清空')
  } catch {
    ElMessage.info('已取消清空')
  }
}

// 查看详情
const viewDetail = (row) => {
  ElMessage.info(`查看导入记录详情: ${row.fileName}`)
}

// 删除记录
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除导入记录 ${row.fileName} 吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}

// 获取导入状态文本
const getImportStatusText = (status) => {
  const statusMap = {
    success: '成功',
    failed: '失败',
    processing: '处理中'
  }
  return statusMap[status] || '未知'
}
</script>

<style scoped>
.import-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.import-progress {
  margin-top: 20px;
}
</style>
