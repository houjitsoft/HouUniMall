<template>
  <div class="backup-file-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份文件</h2>
      <p>管理系统中的备份文件</p>
    </div>

    <!-- 搜索和操作区域 -->
    <div class="search-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索备份文件名"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.backupType" placeholder="备份类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="全量备份" value="full" />
            <el-option label="增量备份" value="incremental" />
            <el-option label="差异备份" value="differential" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleUpload">
            <el-icon><Upload /></el-icon>
            上传备份
          </el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 文件列表 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="fileList"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="文件名" min-width="250" show-overflow-tooltip />
        <el-table-column prop="backupType" label="备份类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBackupTypeTagType(row.backupType)">
              {{ getBackupTypeText(row.backupType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="文件大小" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button type="warning" size="small" @click="handleRestore(row)">
              恢复
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 上传对话框 -->
    <el-dialog
      v-model="showUploadDialog"
      title="上传备份文件"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="uploadForm" :rules="uploadRules" ref="uploadFormRef" label-width="100px">
        <el-form-item label="备份文件" prop="file">
          <el-upload
            v-model:file-list="uploadForm.fileList"
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                请上传备份文件，支持 .zip, .tar.gz 格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="备份类型" prop="backupType">
          <el-select v-model="uploadForm.backupType" placeholder="请选择备份类型">
            <el-option label="全量备份" value="full" />
            <el-option label="增量备份" value="incremental" />
            <el-option label="差异备份" value="differential" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="uploadForm.description" 
            type="textarea" 
            placeholder="请输入备份文件描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUploadSave" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Upload, UploadFilled 
} from '@element-plus/icons-vue'

// 数据
const loading = ref(false)
const uploading = ref(false)
const showUploadDialog = ref(false)
const uploadFormRef = ref()
const selectedFiles = ref([])

const fileList = ref([
  {
    id: 1,
    name: 'backup_20240101_full.zip',
    backupType: 'full',
    size: '2.5GB',
    createTime: '2024-01-01 02:00:00',
    status: 'available'
  },
  {
    id: 2,
    name: 'backup_20231231_incremental.zip',
    backupType: 'incremental',
    size: '1.2GB',
    createTime: '2023-12-31 02:00:00',
    status: 'available'
  },
  {
    id: 3,
    name: 'backup_20231230_full.zip',
    backupType: 'full',
    size: '2.3GB',
    createTime: '2023-12-30 02:00:00',
    status: 'available'
  }
])

const searchForm = reactive({
  keyword: '',
  backupType: '',
  dateRange: []
})

const uploadForm = reactive({
  fileList: [],
  backupType: 'full',
  description: ''
})

const uploadRules = {
  fileList: [
    { required: true, message: '请上传备份文件', trigger: 'change' }
  ],
  backupType: [
    { required: true, message: '请选择备份类型', trigger: 'change' }
  ]
}

const pagination = reactive({
  current: 1,
  size: 20,
  total: 3
})

// 方法
const loadFileList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('文件列表加载成功')
  } catch (error) {
    ElMessage.error('加载文件列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadFileList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.backupType = ''
  searchForm.dateRange = []
  handleSearch()
}

const handleDownload = (file) => {
  ElMessage.info(`开始下载文件: ${file.name}`)
  // 模拟下载操作
}

const handleRestore = async (file) => {
  try {
    await ElMessageBox.confirm(
      `确定要从备份文件"${file.name}"恢复数据吗？这将覆盖当前数据，请确保已做好相关准备。`,
      '确认恢复',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('开始恢复操作')
    // 跳转到恢复页面
    // router.push('/setting/backup/restore')
  } catch {
    ElMessage.info('已取消恢复')
  }
}

const handleDelete = async (file) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除备份文件"${file.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('删除成功')
    loadFileList()
  } catch {
    ElMessage.info('已取消删除')
  }
}

const handleUpload = () => {
  // 重置表单
  uploadForm.fileList = []
  uploadForm.backupType = 'full'
  uploadForm.description = ''
  showUploadDialog.value = true
}

const handleFileChange = (file, fileList) => {
  uploadForm.fileList = fileList
}

const handleUploadSave = async () => {
  try {
    await uploadFormRef.value.validate()
    uploading.value = true
    
    // 模拟上传操作
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('文件上传成功')
    showUploadDialog.value = false
    loadFileList()
  } catch (error) {
    if (error.message) {
      ElMessage.error('上传失败: ' + error.message)
    }
  } finally {
    uploading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedFiles.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadFileList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadFileList()
}

// 状态相关方法
const getBackupTypeTagType = (type) => {
  const typeMap = {
    full: 'primary',
    incremental: 'success',
    differential: 'warning'
  }
  return typeMap[type] || 'info'
}

const getBackupTypeText = (type) => {
  const typeMap = {
    full: '全量备份',
    incremental: '增量备份',
    differential: '差异备份'
  }
  return typeMap[type] || '未知'
}

const getStatusTagType = (status) => {
  const statusMap = {
    available: 'success',
    unavailable: 'danger',
    pending: 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    available: '可用',
    unavailable: '不可用',
    pending: '待处理'
  }
  return statusMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  loadFileList()
})
</script>

<style scoped>
.backup-file-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-section {
  margin-bottom: 20px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}
</style>
