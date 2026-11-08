<template>
  <div class="storage-file-panel">
    <div class="file-toolbar">
      <el-input
        v-model="query.fileName"
        placeholder="按文件名搜索"
        clearable
        style="width: 220px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.fileType" placeholder="文件类型" clearable style="width: 140px">
        <el-option label="图片" :value="1" />
        <el-option label="视频" :value="2" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <div class="file-toolbar__right">
        <el-upload
          :show-file-list="false"
          :http-request="handleUpload"
          :disabled="uploading"
          multiple
        >
          <el-button type="success" :loading="uploading">上传文件</el-button>
        </el-upload>
        <el-button
          type="danger"
          plain
          :disabled="!selectedIds.length"
          :loading="deleting"
          @click="handleBatchDelete"
        >
          删除选中 ({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <el-alert
      type="warning"
      :closable="false"
      show-icon
      title="上传说明"
      description="文件将上传到当前启用的对象存储，并登记到系统素材库。启用 MinIO 时返回的是「公网访问域名 (Endpoint)」下的链接，请确认浏览器可直接打开。删除仅移除素材库记录，不会自动删除云端对象。"
      style="margin-bottom: 16px"
    />

    <el-table
      v-loading="loading"
      :data="files"
      stripe
      border
      @selection-change="onSelectionChange"
    >
      <el-table-column type="selection" width="48" />
      <el-table-column label="预览" width="88">
        <template #default="{ row }">
          <el-image
            v-if="row.fileType === 1 && row.fileUrl"
            :src="row.fileUrl"
            fit="cover"
            class="file-thumb"
            :preview-src-list="[row.fileUrl]"
            preview-teleported
          />
          <div v-else-if="row.fileType === 2" class="file-thumb file-thumb--video">视频</div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="fileType" label="类型" width="90">
        <template #default="{ row }">
          <el-tag :type="row.fileType === 2 ? 'warning' : 'success'" size="small">
            {{ row.fileType === 2 ? '视频' : '图片' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fileUrl" label="访问地址" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link v-if="row.fileUrl" :href="row.fileUrl" target="_blank" type="primary">
            {{ row.fileUrl }}
          </el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="fileCreateTime" label="上传时间" width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="copyUrl(row.fileUrl)">复制链接</el-button>
          <el-button type="danger" link size="small" @click="handleDelete([row.id])">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="file-pager">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :current-page="query.page"
        :page-size="query.limit"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteStorageFiles,
  queryStorageFiles,
  registerStorageFile,
  uploadStorageFile
} from '@/api/setting/storage'

const loading = ref(false)
const uploading = ref(false)
const deleting = ref(false)
const files = ref([])
const total = ref(0)
const selectedIds = ref([])

const query = reactive({
  page: 1,
  limit: 20,
  fileName: '',
  fileType: null
})

const VIDEO_EXT = /\.(mp4|mov|avi|wmv|flv|mkv|webm)$/i

const resolveFileType = (fileName) => (VIDEO_EXT.test(fileName || '') ? 2 : 1)

const loadFiles = async () => {
  loading.value = true
  try {
    const res = await queryStorageFiles({
      page: query.page,
      limit: query.limit,
      fileName: query.fileName || undefined,
      fileType: query.fileType ?? undefined
    })
    if (res && (res.code === 200 || res.code === 1)) {
      const pageData = res.data || {}
      files.value = pageData.list || []
      total.value = pageData.totalCount || 0
    } else {
      ElMessage.warning(res?.message || '加载文件列表失败')
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
    ElMessage.error('加载文件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadFiles()
}

const resetQuery = () => {
  query.page = 1
  query.fileName = ''
  query.fileType = null
  loadFiles()
}

const onPageChange = (page) => {
  query.page = page
  loadFiles()
}

const onSelectionChange = (rows) => {
  selectedIds.value = rows.map((row) => row.id).filter(Boolean)
}

const handleUpload = async ({ file }) => {
  uploading.value = true
  try {
    const uploadRes = await uploadStorageFile(file)
    if (!(uploadRes && (uploadRes.code === 200 || uploadRes.code === 1) && uploadRes.data)) {
      throw new Error(uploadRes?.message || '上传失败')
    }
    const fileUrl = typeof uploadRes.data === 'string' ? uploadRes.data : uploadRes.data.url
    const registerRes = await registerStorageFile({
      fileName: file.name,
      fileUrl,
      fileType: resolveFileType(file.name)
    })
    if (registerRes && (registerRes.code === 200 || registerRes.code === 1)) {
      ElMessage.success(`「${file.name}」上传成功`)
      await loadFiles()
    } else {
      ElMessage.warning(registerRes?.message || '文件已上传但登记素材库失败')
    }
  } catch (error) {
    console.error('上传文件失败:', error)
    ElMessage.error(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

const handleDelete = async (fileIds) => {
  if (!fileIds.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${fileIds.length} 个文件记录吗？`, '确认删除', {
      type: 'warning'
    })
    deleting.value = true
    const res = await deleteStorageFiles(fileIds)
    if (res && (res.code === 200 || res.code === 1)) {
      ElMessage.success('删除成功')
      selectedIds.value = []
      await loadFiles()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文件失败:', error)
    }
  } finally {
    deleting.value = false
  }
}

const handleBatchDelete = () => handleDelete([...selectedIds.value])

const copyUrl = async (url) => {
  if (!url) return
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

onMounted(() => {
  loadFiles()
})

defineExpose({ refresh: loadFiles })
</script>

<style scoped>
.storage-file-panel {
  padding-top: 4px;
}
.file-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
}
.file-toolbar__right {
  margin-left: auto;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.file-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}
.file-thumb--video {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
}
.file-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
@media (max-width: 768px) {
  .file-toolbar__right {
    margin-left: 0;
    width: 100%;
  }
}
</style>
