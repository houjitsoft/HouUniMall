<template>
  <div class="system-backup">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>备份恢复</span>
          <el-button type="primary" @click="createBackup">创建备份</el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="backup-card">
            <template #header>
              <span>数据库备份</span>
            </template>
            <el-form :model="dbBackupForm" label-width="120px">
              <el-form-item label="备份类型">
                <el-radio-group v-model="dbBackupForm.type">
                  <el-radio value="full">完整备份</el-radio>
                  <el-radio value="incremental">增量备份</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="压缩备份">
                <el-switch v-model="dbBackupForm.compression"></el-switch>
              </el-form-item>
              <el-form-item label="备份说明">
                <el-input v-model="dbBackupForm.description" type="textarea" placeholder="请输入备份说明"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="backupDatabase">开始备份</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="backup-card">
            <template #header>
              <span>文件备份</span>
            </template>
            <el-form :model="fileBackupForm" label-width="120px">
              <el-form-item label="备份路径">
                <el-input v-model="fileBackupForm.path" placeholder="请输入备份路径"></el-input>
              </el-form-item>
              <el-form-item label="包含子目录">
                <el-switch v-model="fileBackupForm.includeSubdirs"></el-switch>
              </el-form-item>
              <el-form-item label="排除文件">
                <el-input v-model="fileBackupForm.excludeFiles" placeholder="请输入排除的文件类型，用逗号分隔"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="backupFiles">开始备份</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
      
      <el-divider></el-divider>
      
      <h3>备份记录</h3>
      <el-table :data="backupList" style="width: 100%">
        <el-table-column prop="name" label="备份名称" width="200"></el-table-column>
        <el-table-column prop="type" label="备份类型" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'database' ? 'primary' : 'success'">
              {{ scope.row.type === 'database' ? '数据库' : '文件' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="120"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="downloadBackup(scope.row)">下载</el-button>
            <el-button size="small" type="success" @click="restoreBackup(scope.row)">恢复</el-button>
            <el-button size="small" type="danger" @click="deleteBackup(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const dbBackupForm = reactive({
  type: 'full',
  compression: true,
  description: ''
})

const fileBackupForm = reactive({
  path: '/var/www/html',
  includeSubdirs: true,
  excludeFiles: '*.log,*.tmp'
})

const backupList = ref([
  {
    id: 1,
    name: '数据库完整备份_20240108',
    type: 'database',
    size: '125MB',
    createTime: '2024-01-08 14:30:00',
    status: 'success'
  },
  {
    id: 2,
    name: '文件备份_20240108',
    type: 'file',
    size: '256MB',
    createTime: '2024-01-08 14:25:00',
    status: 'success'
  },
  {
    id: 3,
    name: '数据库增量备份_20240107',
    type: 'database',
    size: '45MB',
    createTime: '2024-01-07 18:00:00',
    status: 'success'
  }
])

const createBackup = () => {
  ElMessage.info('创建备份功能开发中...')
}

const backupDatabase = () => {
  ElMessage.success('数据库备份已开始，请稍后查看备份记录')
}

const backupFiles = () => {
  ElMessage.success('文件备份已开始，请稍后查看备份记录')
}

const downloadBackup = (row) => {
  ElMessage.success(`开始下载备份文件：${row.name}`)
}

const restoreBackup = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要恢复备份 ${row.name} 吗？此操作将覆盖当前数据！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('备份恢复已开始，请稍后查看结果')
  } catch {
    ElMessage.info('已取消恢复')
  }
}

const deleteBackup = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除备份 ${row.name} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const index = backupList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      backupList.value.splice(index, 1)
      ElMessage.success('备份删除成功')
    }
  } catch {
    ElMessage.info('已取消删除')
  }
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.system-backup {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.backup-card {
  margin-bottom: 20px;
}
</style>
