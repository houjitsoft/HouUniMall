<template>
  <div class="goods-import-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商品导入</span>
        </div>
      </template>

      <!-- 导入说明 -->
      <el-alert
        title="导入说明"
        type="info"
        :closable="false"
        show-icon
        class="import-tips"
      >
        <template #default>
          <ul class="tips-list">
            <li>请先下载导入模板，按模板格式填写商品数据</li>
            <li>支持Excel格式（.xlsx、.xls）</li>
            <li>单次导入数量不超过1000条</li>
            <li>导入过程中请勿关闭页面</li>
          </ul>
        </template>
      </el-alert>

      <!-- 模板下载 -->
      <div class="template-section">
        <h4>第一步：下载导入模板</h4>
        <el-button type="primary" @click="downloadTemplate">
          <el-icon><Download /></el-icon>
          下载导入模板
        </el-button>
      </div>

      <!-- 文件上传 -->
      <div class="upload-section">
        <h4>第二步：上传商品数据</h4>
        <el-upload
          ref="uploadRef"
          class="upload-area"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传 xlsx/xls 文件，且不超过 10MB
            </div>
          </template>
        </el-upload>
      </div>

      <!-- 导入操作 -->
      <div class="action-section">
        <h4>第三步：开始导入</h4>
        <el-button type="success" :loading="importing" :disabled="!selectedFile" @click="handleImport">
          {{ importing ? '导入中...' : '开始导入' }}
        </el-button>
      </div>

      <!-- 导入结果 -->
      <div class="result-section" v-if="importResult">
        <h4>导入结果</h4>
        <el-result
          :icon="importResult.success ? 'success' : 'warning'"
          :title="importResult.success ? '导入成功' : '部分导入成功'"
        >
          <template #sub-title>
            <p>成功导入 {{ importResult.successCount }} 条</p>
            <p v-if="importResult.failCount">失败 {{ importResult.failCount }} 条</p>
          </template>
          <template #extra>
            <el-button v-if="importResult.failCount" type="primary" @click="downloadErrorLog">
              下载错误日志
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 导入历史 -->
      <div class="history-section">
        <h4>导入历史</h4>
        <el-table :data="historyData" v-loading="loading" stripe border>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="fileName" label="文件名" min-width="150" />
          <el-table-column prop="totalCount" label="总数量" width="100" />
          <el-table-column prop="successCount" label="成功" width="100">
            <template #default="{ row }">
              <span class="text-success">{{ row.successCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="failCount" label="失败" width="100">
            <template #default="{ row }">
              <span class="text-danger">{{ row.failCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="120" />
          <el-table-column prop="createTime" label="导入时间" width="160" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'completed' ? 'success' : 'warning'">
                {{ row.status === 'completed' ? '已完成' : '处理中' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'

const uploadRef = ref()
const selectedFile = ref(null)
const importing = ref(false)
const loading = ref(false)
const importResult = ref(null)
const historyData = ref([])

const downloadTemplate = () => {
  ElMessage.info('正在下载模板...')
  // TODO: 实现模板下载
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先删除已选择的文件')
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }
  
  importing.value = true
  try {
    // TODO: 调用导入API
    await new Promise(resolve => setTimeout(resolve, 2000))
    importResult.value = {
      success: true,
      successCount: 100,
      failCount: 0
    }
    ElMessage.success('导入成功')
    fetchHistory()
  } catch (error) {
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

const downloadErrorLog = () => {
  ElMessage.info('正在下载错误日志...')
}

const fetchHistory = async () => {
  loading.value = true
  try {
    // TODO: 获取导入历史
    historyData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.goods-import-container {
  padding: 20px;
}
.card-header {
  font-weight: bold;
}
.import-tips {
  margin-bottom: 20px;
}
.tips-list {
  margin: 0;
  padding-left: 20px;
}
.template-section,
.upload-section,
.action-section,
.result-section,
.history-section {
  margin-bottom: 30px;
}
h4 {
  margin-bottom: 15px;
  color: #333;
}
.upload-area {
  width: 100%;
}
.text-success {
  color: #67c23a;
}
.text-danger {
  color: #f56c6c;
}
</style>
