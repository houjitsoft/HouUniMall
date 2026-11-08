<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span class="card-title">客户导入</span></template>

      <el-steps :active="currentStep" align-center class="steps">
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
        <el-button type="primary" @click="downloadTemplate">下载导入模板</el-button>
        <el-button @click="currentStep = 1">下一步</el-button>
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
            <el-button type="primary" @click="handleReset">继续导入</el-button>
            <el-button @click="$router.push('/mall/customer/list')">返回列表</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const currentStep = ref(0)
const uploadFile = ref(null)
const previewData = ref([])
const importing = ref(false)
const importSuccess = ref(0)
const importFail = ref(0)

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
    { name: '张三', phone: '13800138001', email: 'zhangsan@example.com', level: 1, points: 100, remark: '测试客户1' },
    { name: '李四', phone: '13800138002', email: 'lisi@example.com', level: 2, points: 500, remark: '测试客户2' },
    { name: '王五', phone: '13800138003', email: 'wangwu@example.com', level: 3, points: 1000, remark: '测试客户3' }
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
  }, 2000)
}

const handleReset = () => {
  currentStep.value = 0
  uploadFile.value = null
  previewData.value = []
  importSuccess.value = 0
  importFail.value = 0
}

const getLevelText = (level) => {
  const map = { 1: '普通会员', 2: 'VIP会员', 3: 'SVIP会员' }
  return map[level] || '-'
}
</script>

<style scoped>
.page-container { padding: 20px; }
.card-title { font-weight: bold; font-size: 16px; }
.steps { margin: 40px 0; }
.step-content { padding: 40px 20px; }
.template-info { margin: 20px 0; padding: 20px; background: #f5f7fa; border-radius: 4px; }
.template-info h3 { margin-bottom: 15px; }
.template-info ul { padding-left: 20px; }
.template-info li { margin-bottom: 10px; line-height: 1.8; }
.upload-area { margin: 30px 0; }
.preview-table { margin: 20px 0; }
.step-buttons { margin-top: 30px; text-align: center; }
.step-buttons .el-button { margin: 0 10px; }
</style>
