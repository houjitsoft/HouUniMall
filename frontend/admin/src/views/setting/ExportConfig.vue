<template>
  <div class="export-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>导出配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="数据导出" name="data">
          <div class="tab-content">
            <el-form :model="exportForm" label-width="120px">
              <el-form-item label="导出格式">
                <el-radio-group v-model="exportForm.format">
                  <el-radio label="json">JSON</el-radio>
                  <el-radio label="csv">CSV</el-radio>
                  <el-radio label="excel">Excel</el-radio>
                  <el-radio label="xml">XML</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="导出范围">
                <el-radio-group v-model="exportForm.range">
                  <el-radio label="all">全部数据</el-radio>
                  <el-radio label="selected">选中数据</el-radio>
                  <el-radio label="filtered">筛选后数据</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="编码格式">
                <el-select v-model="exportForm.encoding">
                  <el-option label="UTF-8" value="utf-8" />
                  <el-option label="GBK" value="gbk" />
                  <el-option label="GB2312" value="gb2312" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="压缩选项">
                <el-switch v-model="exportForm.compress" />
                <span class="form-tip">导出文件是否压缩</span>
              </el-form-item>
              
              <el-form-item v-if="exportForm.compress" label="压缩格式">
                <el-radio-group v-model="exportForm.compressFormat">
                  <el-radio label="zip">ZIP</el-radio>
                  <el-radio label="rar">RAR</el-radio>
                  <el-radio label="7z">7Z</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="exportData">导出数据</el-button>
                <el-button @click="exportTemplate">导出模板</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="配置导出" name="config">
          <div class="tab-content">
            <el-alert
              title="注意：导出的配置文件包含系统敏感信息，请妥善保管"
              type="warning"
              show-icon
              style="margin-bottom: 20px;"
            />
            
            <el-form :model="configExportForm" label-width="120px">
              <el-form-item label="导出内容">
                <el-checkbox-group v-model="configExportForm.content">
                  <el-checkbox label="system">系统配置</el-checkbox>
                  <el-checkbox label="database">数据库配置</el-checkbox>
                  <el-checkbox label="security">安全配置</el-checkbox>
                  <el-checkbox label="mail">邮件配置</el-checkbox>
                  <el-checkbox label="storage">存储配置</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="加密导出">
                <el-switch v-model="configExportForm.encrypt" />
                <span class="form-tip">是否对配置文件进行加密</span>
              </el-form-item>
              
              <el-form-item v-if="configExportForm.encrypt" label="加密密码">
                <el-input v-model="configExportForm.password" type="password" placeholder="请输入加密密码" />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="exportConfig">导出配置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="导出记录" name="history">
          <div class="tab-content">
            <div class="toolbar">
              <el-button @click="refreshHistory">刷新</el-button>
              <el-button type="danger" @click="clearHistory">清空记录</el-button>
            </div>
            
            <el-table :data="exportHistory" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="fileName" label="文件名" min-width="200" />
              <el-table-column prop="type" label="类型" width="120" />
              <el-table-column prop="size" label="大小" width="100" />
              <el-table-column prop="createTime" label="导出时间" width="180" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'success' ? 'success' : 'danger'">
                    {{ row.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="downloadFile(row)">
                    下载
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('data')

// 数据导出表单
const exportForm = reactive({
  format: 'excel',
  range: 'all',
  encoding: 'utf-8',
  compress: false,
  compressFormat: 'zip'
})

// 配置导出表单
const configExportForm = reactive({
  content: ['system', 'database', 'security'],
  encrypt: false,
  password: ''
})

// 导出历史记录
const exportHistory = ref([
  {
    id: 1,
    fileName: 'user_data_20240108.xlsx',
    type: '数据导出',
    size: '2.5MB',
    createTime: '2024-01-08 10:30:00',
    status: 'success'
  },
  {
    id: 2,
    fileName: 'system_config_20240108.json',
    type: '配置导出',
    size: '150KB',
    createTime: '2024-01-08 09:15:00',
    status: 'success'
  }
])

// 数据导出
const exportData = () => {
  ElMessage.success('开始导出数据...')
}

// 导出模板
const exportTemplate = () => {
  ElMessage.success('正在导出模板...')
}

// 导出配置
const exportConfig = () => {
  if (configExportForm.encrypt && !configExportForm.password) {
    ElMessage.error('请输入加密密码')
    return
  }
  ElMessage.success('开始导出配置...')
}

// 刷新历史记录
const refreshHistory = () => {
  ElMessage.success('刷新导出记录')
}

// 清空历史记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有导出记录吗？', '确认清空', {
      type: 'warning'
    })
    exportHistory.value = []
    ElMessage.success('导出记录已清空')
  } catch {
    ElMessage.info('已取消清空')
  }
}

// 下载文件
const downloadFile = (row) => {
  ElMessage.success(`正在下载 ${row.fileName}...`)
}

// 删除记录
const deleteRecord = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除导出记录 ${row.fileName} 吗？`, '确认删除', {
      type: 'warning'
    })
    ElMessage.success('删除成功')
  } catch {
    ElMessage.info('已取消删除')
  }
}
</script>

<style scoped>
.export-config {
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
</style>