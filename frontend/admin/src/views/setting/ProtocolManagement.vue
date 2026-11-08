<template>
  <div class="protocol-management">
    <div class="page-header">
      <h2>协议管理</h2>
      <p>管理个人入驻、企业入驻、供应商入驻和合作协议</p>
    </div>

    <el-button type="primary" icon="Plus" @click="showAddDialog" style="margin-bottom: 20px">
      新增协议
    </el-button>

    <div class="protocol-grid">
      <el-card 
        v-for="protocol in protocolList" 
        :key="protocol.id"
        class="protocol-card"
        shadow="hover"
      >
        <template #header>
          <div class="card-header">
            <span class="protocol-title">{{ protocol.name }}</span>
            <div class="card-actions">
              <el-button link type="primary" size="small" @click="editProtocol(protocol)">
                编辑
              </el-button>
              <el-button link type="danger" size="small" @click="deleteProtocol(protocol)">
                删除
              </el-button>
            </div>
          </div>
        </template>

        <div class="protocol-content">
          <div class="info-row">
            <span class="label">链接：</span>
            <div class="file-link">
              <a :href="protocol.fileUrl" target="_blank" class="link-text">
                {{ protocol.fileName || protocol.fileUrl }}
              </a>
              <el-button link type="primary" size="small" @click="copyLink(protocol.fileUrl)">
                复制
              </el-button>
            </div>
          </div>

          <div class="info-row">
            <span class="label">协议：</span>
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="(response) => handleUploadSuccess(response, protocol)"
              :before-upload="beforeUpload"
              accept=".pdf"
            >
              <el-button type="primary" size="small">点击上传</el-button>
            </el-upload>
            <span class="upload-tip">只能上传后缀是.pdf的文件 文件大小不得超过2M</span>
            <el-link type="primary" @click="viewSample">查看示例模板</el-link>
          </div>

          <div class="info-row">
            <span class="label">更新时间：</span>
            <span class="value">{{ protocol.updateTime }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑协议对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="protocolForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="协议名称" prop="name">
          <el-input v-model="protocolForm.name" placeholder="请输入协议名称" />
        </el-form-item>
        
        <el-form-item label="协议类型" prop="type">
          <el-select v-model="protocolForm.type" placeholder="请选择协议类型" style="width: 100%">
            <el-option label="个人入驻" value="PERSONAL" />
            <el-option label="企业入驻" value="ENTERPRISE" />
            <el-option label="供应商入驻" value="SUPPLIER" />
            <el-option label="合作协议" value="COOPERATION" />
          </el-select>
        </el-form-item>

        <el-form-item label="协议文件" prop="fileUrl">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleFormUploadSuccess"
            :before-upload="beforeUpload"
            accept=".pdf"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
          <div v-if="protocolForm.fileUrl" class="file-info">
            <el-link :href="protocolForm.fileUrl" target="_blank" type="primary">
              {{ protocolForm.fileName || '查看文件' }}
            </el-link>
          </div>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="protocolForm.sort" :min="0" :max="999" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="protocolForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProtocol">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getToken } from '@/utils/auth'

// 上传配置
const uploadUrl = computed(() => import.meta.env.VITE_APP_BASE_API + '/web/common/upload')
const uploadHeaders = computed(() => ({
  'Authorization': 'Bearer ' + getToken()
}))

// 协议列表
const protocolList = ref([
  {
    id: 1,
    name: '个人入驻',
    type: 'PERSONAL',
    fileUrl: '384f645ff4dd44ed96822049bacd49fc.pdf',
    fileName: '384f645ff4dd44ed96822049bacd49fc.pdf',
    updateTime: '2025-10-11 14:26:15',
    sort: 1,
    status: 1
  },
  {
    id: 2,
    name: '企业入驻',
    type: 'ENTERPRISE',
    fileUrl: '5e9144c655554e4286cc76673e680552.pdf',
    fileName: '5e9144c655554e4286cc76673e680552.pdf',
    updateTime: '2025-11-13 16:00:16',
    sort: 2,
    status: 1
  },
  {
    id: 3,
    name: '供应商入驻',
    type: 'SUPPLIER',
    fileUrl: '2a023ecc103240f39d0be203163732b7.pdf',
    fileName: '2a023ecc103240f39d0be203163732b7.pdf',
    updateTime: '2025-01-07 18:34:28',
    sort: 3,
    status: 1
  },
  {
    id: 4,
    name: '合作协议',
    type: 'COOPERATION',
    fileUrl: '7d8499b00329423dada8999df33197c4.pdf',
    fileName: '7d8499b00329423dada8999df33197c4.pdf',
    updateTime: '2025-01-07 18:34:28',
    sort: 4,
    status: 1
  }
])

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = computed(() => protocolForm.id ? '编辑协议' : '新增协议')
const formRef = ref()

const protocolForm = reactive({
  id: null,
  name: '',
  type: '',
  fileUrl: '',
  fileName: '',
  sort: 0,
  status: 1
})

const formRules = {
  name: [
    { required: true, message: '请输入协议名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择协议类型', trigger: 'change' }
  ],
  fileUrl: [
    { required: true, message: '请上传协议文件', trigger: 'change' }
  ]
}

// 加载协议列表
const loadProtocols = async () => {
  try {
    const res = await request.post('/web/base/protocol/list', {})
    if (res.code === 200) {
      protocolList.value = res.data || []
    }
  } catch (error) {
    console.error('加载协议列表失败:', error)
  }
}

// 显示新增对话框
const showAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

// 编辑协议
const editProtocol = (protocol) => {
  Object.assign(protocolForm, {
    id: protocol.id,
    name: protocol.name,
    type: protocol.type,
    fileUrl: protocol.fileUrl,
    fileName: protocol.fileName,
    sort: protocol.sort || 0,
    status: protocol.status || 1
  })
  dialogVisible.value = true
}

// 保存协议
const saveProtocol = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await request.post('/web/base/protocol/save', protocolForm)
        if (res.code === 200) {
          ElMessage.success(protocolForm.id ? '编辑成功' : '添加成功')
          dialogVisible.value = false
          loadProtocols()
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } catch (error) {
        console.error('保存协议失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除协议
const deleteProtocol = (protocol) => {
  ElMessageBox.confirm('确定要删除该协议吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.post('/web/base/protocol/delete', { id: protocol.id })
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadProtocols()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除协议失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 文件上传成功（卡片中）
const handleUploadSuccess = async (response, protocol) => {
  if (response.code === 200) {
    try {
      const res = await request.post('/web/base/protocol/save', {
        id: protocol.id,
        fileUrl: response.data.url,
        fileName: response.data.fileName
      })
      if (res.code === 200) {
        ElMessage.success('上传成功')
        loadProtocols()
      }
    } catch (error) {
      ElMessage.error('更新失败')
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 文件上传成功（表单中）
const handleFormUploadSuccess = (response) => {
  if (response.code === 200) {
    protocolForm.fileUrl = response.data.url
    protocolForm.fileName = response.data.fileName
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传前验证
const beforeUpload = (file) => {
  const isPDF = file.type === 'application/pdf'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isPDF) {
    ElMessage.error('只能上传 PDF 格式的文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('文件大小不能超过 2MB!')
    return false
  }
  return true
}

// 复制链接
const copyLink = (url) => {
  if (!url) {
    ElMessage.warning('链接为空')
    return
  }
  
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('复制成功')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 查看示例模板
const viewSample = () => {
  ElMessage.info('示例模板功能待实现')
}

// 重置表单
const resetForm = () => {
  Object.assign(protocolForm, {
    id: null,
    name: '',
    type: '',
    fileUrl: '',
    fileName: '',
    sort: 0,
    status: 1
  })
  formRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  loadProtocols()
})
</script>

<style scoped lang="scss">
.protocol-management {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      font-weight: 500;
      margin: 0 0 8px 0;
      color: #303133;
    }

    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .protocol-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-top: 20px;

    .protocol-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .protocol-title {
          font-size: 16px;
          font-weight: 500;
          color: #303133;
        }

        .card-actions {
          display: flex;
          gap: 8px;
        }
      }

      .protocol-content {
        .info-row {
          margin-bottom: 16px;
          display: flex;
          align-items: flex-start;

          &:last-child {
            margin-bottom: 0;
          }

          .label {
            flex-shrink: 0;
            width: 80px;
            color: #606266;
            font-size: 14px;
          }

          .value {
            flex: 1;
            color: #303133;
            font-size: 14px;
          }

          .file-link {
            flex: 1;
            display: flex;
            align-items: center;
            gap: 8px;

            .link-text {
              flex: 1;
              color: #409eff;
              text-decoration: none;
              word-break: break-all;
              font-size: 14px;

              &:hover {
                text-decoration: underline;
              }
            }
          }

          .upload-tip {
            margin-left: 12px;
            color: #909399;
            font-size: 13px;
          }
        }
      }
    }
  }

  .file-info {
    margin-top: 8px;
  }
}
</style>
