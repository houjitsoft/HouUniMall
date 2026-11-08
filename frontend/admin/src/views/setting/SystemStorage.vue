<template>
  <div class="system-storage">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>存储管理</span>
          <el-button type="primary" @click="showAddDialog">新增存储</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="存储配置" name="config">
          <div class="tab-content">
            <el-table v-loading="tableLoading" :data="storages" style="width: 100%">
              <el-table-column prop="ossType" label="存储类型" width="150">
                <template #default="{ row }">
                  <el-tag :type="getStorageTypeTag(row.ossType)">
                    {{ getStorageTypeName(row.ossType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="ossEndpoint" label="公网访问域名 (Endpoint)" min-width="220" show-overflow-tooltip />
              <el-table-column prop="ossRegion" label="API地址 / Region" min-width="180" show-overflow-tooltip />
              <el-table-column prop="ossBacketName" label="存储桶" width="120" />
              <el-table-column prop="ossStates" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.ossStates === '01' ? 'success' : 'info'">
                    {{ row.ossStates === '01' ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button
                    size="small"
                    :type="row.ossStates === '01' ? 'warning' : 'success'"
                    link
                    @click="toggleState(row)"
                  >
                    {{ row.ossStates === '01' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" type="primary" link @click="editStorage(row)">
                    编辑
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="存储监控" name="monitor">
          <div class="tab-content">
            <StorageMonitorPanel v-if="activeTab === 'monitor'" ref="monitorPanelRef" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="文件管理" name="files">
          <div class="tab-content">
            <StorageFilePanel v-if="activeTab === 'files'" ref="filePanelRef" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑存储对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="storageFormRef" :model="storageForm" :rules="formRules" label-width="120px">
        <!-- 存储类型 -->
        <el-form-item label="存储类型" prop="ossType">
          <el-select
            v-model="storageForm.ossType"
            placeholder="请选择存储类型"
            style="width: 100%"
            @change="onStorageTypeChange"
          >
            <el-option label="阿里云 OSS" value="aliyun" />
            <el-option label="MinIO" value="minio" />
            <el-option label="腾讯云 COS" value="tencentCloud" />
            <el-option label="西部数码 FSS" value="westFss" />
          </el-select>
        </el-form-item>

        <!-- 通用字段：存储桶 -->
        <el-form-item label="存储桶名称" prop="ossBacketName">
          <el-input v-model="storageForm.ossBacketName" :placeholder="getBucketPlaceholder()" />
        </el-form-item>

        <!-- 通用字段：文件夹 -->
        <el-form-item label="文件夹路径" prop="ossFolder">
          <el-input v-model="storageForm.ossFolder" placeholder="请输入文件上传的子目录，如：file/uploads" />
        </el-form-item>

        <!-- 西部数码 FSS 专属字段 -->
        <template v-if="storageForm.ossType === 'westFss'">
          <el-form-item label="FTP地址" prop="ossRegion">
            <el-input v-model="storageForm.ossRegion" placeholder="如：ftp-my.vhostgo.com" />
          </el-form-item>
          <el-form-item label="API域名" prop="ossEndpoint">
            <el-input v-model="storageForm.ossEndpoint" placeholder="如：https://fss-my.myhostadmin.net" />
          </el-form-item>
          <el-form-item label="FTP用户名" prop="ossAccessKeyId">
            <el-input v-model="storageForm.ossAccessKeyId" placeholder="请输入FTP用户名" />
          </el-form-item>
          <el-form-item label="FTP密码" prop="ossAccessKeySecret">
            <el-input
              v-model="storageForm.ossAccessKeySecret"
              type="password"
              show-password
              :placeholder="isEdit ? '请重新输入FTP密码（必须重新填写，不能含*号）' : '请输入FTP密码'"
            />
            <div v-if="isEdit" style="color: #E6A23C; font-size: 12px; margin-top: 4px;">
              编辑时需重新填写密码，服务器不会保留明文
            </div>
          </el-form-item>
        </template>

        <!-- 其他存储类型通用字段 -->
        <template v-else>
          <el-form-item :label="getRegionLabel()" prop="ossRegion">
            <el-input v-model="storageForm.ossRegion" :placeholder="getRegionPlaceholder()" />
            <div v-if="storageForm.ossType === 'minio'" class="field-hint">
              MinIO S3 API 地址（服务端建连用），如 http://内网IP:9000
            </div>
          </el-form-item>
          <el-form-item :label="getEndpointLabel()" prop="ossEndpoint">
            <el-input v-model="storageForm.ossEndpoint" :placeholder="getEndpointPlaceholder()" />
            <div v-if="storageForm.ossType === 'minio'" class="field-hint">
              浏览器可打开的公网前缀，如 https://minio.你的域名（勿与 API 填成同一 localhost）
            </div>
          </el-form-item>
          <el-form-item label="AccessKeyId" prop="ossAccessKeyId">
            <el-input v-model="storageForm.ossAccessKeyId" placeholder="请输入AccessKeyId（含*号表示未修改）" />
          </el-form-item>
          <el-form-item label="AccessKeySecret" prop="ossAccessKeySecret">
            <el-input
              v-model="storageForm.ossAccessKeySecret"
              type="password"
              show-password
              :placeholder="isEdit ? '请重新输入AccessKeySecret（不能含*号）' : '请输入AccessKeySecret'"
            />
            <div v-if="isEdit" style="color: #E6A23C; font-size: 12px; margin-top: 4px;">
              编辑时需重新填写秘鑰，服务器不会保留明文
            </div>
          </el-form-item>
        </template>

        <!-- 状态 -->
        <el-form-item label="状态">
          <el-switch
            v-model="storageForm.ossStates"
            active-value="01"
            inactive-value="00"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input
            v-model="storageForm.ossRemark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>

      <!-- 西部数码FSS配置说明 -->
      <el-alert
        v-if="storageForm.ossType === 'westFss'"
        title="西部数码 FSS 配置说明"
        type="info"
        :closable="false"
        style="margin: 0 20px 10px;"
      >
        <template #default>
          <ul style="margin: 5px 0; padding-left: 20px; font-size: 13px;">
            <li>FTP地址：西部数码提供的FTP服务地址，如 ftp-my.vhostgo.com</li>
            <li>API域名：REST API 访问域名，如 https://fss-my.myhostadmin.net</li>
            <li>FTP用户名/密码：用于认证鉴权（Authorization: WESTYUN 用户名:签名）</li>
            <li>存储桶名：即创建的Bucket名称</li>
          </ul>
        </template>
      </el-alert>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveStorage">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StorageMonitorPanel from '@/components/setting/StorageMonitorPanel.vue'
import StorageFilePanel from '@/components/setting/StorageFilePanel.vue'
import {
  queryAllOssConfig,
  saveOssConfig,
  updateOssStates
} from '@/api/setting/storage'

const activeTab = ref('config')
const dialogVisible = ref(false)
const dialogTitle = ref('新增存储配置')
const isEdit = ref(false)
const tableLoading = ref(false)
const saveLoading = ref(false)
const storageFormRef = ref(null)
const monitorPanelRef = ref(null)
const filePanelRef = ref(null)

/** 存储配置列表 */
const storages = ref([])

/** 存储表单 */
const storageForm = reactive({
  ossId: null,
  ossType: 'aliyun',
  ossBacketName: '',
  ossFolder: '',
  ossRegion: '',
  ossEndpoint: '',
  ossAccessKeyId: '',
  ossAccessKeySecret: '',
  ossStates: '01',
  ossRemark: ''
})

/** 表单校验规则 */
const formRules = {
  ossType: [{ required: true, message: '请选择存储类型', trigger: 'change' }],
  ossBacketName: [{ required: true, message: '请输入存储桶名称', trigger: 'blur' }],
  ossRegion: [{ required: true, message: '请输入FTP地址/集群地址', trigger: 'blur' }],
  ossEndpoint: [{ required: true, message: '请输入API域名/访问域名', trigger: 'blur' }],
  ossAccessKeyId: [{ required: true, message: '请输入用户名/AccessKeyId', trigger: 'blur' }],
  ossAccessKeySecret: [{ required: true, message: '请输入密码/AccessKeySecret', trigger: 'blur' }]
}

/**
 * 获取存储类型名称
 * @param {string} type 存储类型标识
 * @returns {string} 存储类型中文名
 */
const getStorageTypeName = (type) => {
  const typeMap = {
    aliyun: '阿里云OSS',
    minio: 'MinIO',
    tencentCloud: '腾讯云COS',
    westFss: '西部数码FSS'
  }
  return typeMap[type] || type
}

/**
 * 获取存储类型标签样式
 * @param {string} type 存储类型标识
 * @returns {string} Element Plus Tag 类型
 */
const getStorageTypeTag = (type) => {
  if (!type) return 'info'
  const tagMap = {
    aliyun: 'success',
    minio: 'warning',
    tencentCloud: 'primary',
    westFss: 'danger'
  }
  return tagMap[type] || 'info'
}

/**
 * 根据存储类型返回存储桶输入提示
 * @returns {string}
 */
const getBucketPlaceholder = () => {
  const map = {
    aliyun: '如：my-bucket',
    minio: '如：my-bucket',
    tencentCloud: '如：my-bucket-1255349518',
    westFss: '如：houjit（西部数码后台创建的桶名）'
  }
  return map[storageForm.ossType] || '请输入存储桶名称'
}

const getRegionLabel = () => {
  if (storageForm.ossType === 'minio') return 'API 地址 (Region)'
  return '集群/Region'
}

const getEndpointLabel = () => {
  if (storageForm.ossType === 'minio') return '公网访问域名 (Endpoint)'
  return '访问域名'
}

/**
 * 根据存储类型返回Region输入提示
 * @returns {string}
 */
const getRegionPlaceholder = () => {
  const map = {
    aliyun: '如：oss-cn-shanghai',
    minio: '如：http://127.0.0.1:9000',
    tencentCloud: '如：ap-shanghai'
  }
  return map[storageForm.ossType] || '请输入集群地址/Region'
}

/**
 * 根据存储类型返回Endpoint输入提示
 * @returns {string}
 */
const getEndpointPlaceholder = () => {
  const map = {
    aliyun: '如：https://oss-cn-shanghai.aliyuncs.com',
    minio: '如：https://minio.example.com',
    tencentCloud: '如：https://my-bucket.cos.ap-shanghai.myqcloud.com'
  }
  return map[storageForm.ossType] || '请输入访问域名'
}

/**
 * 加载存储配置列表
 */
const loadStorages = async () => {
  tableLoading.value = true
  try {
    const res = await queryAllOssConfig()
    if (res && (res.code === 200 || res.code === 1)) {
      storages.value = res.data || []
    } else {
      ElMessage.warning(res?.message || '获取存储配置失败')
    }
  } catch (error) {
    console.error('加载存储配置失败:', error)
    ElMessage.error('加载存储配置失败，请检查网络连接')
  } finally {
    tableLoading.value = false
  }
}

/**
 * 显示新增对话框
 */
const showAddDialog = () => {
  activeTab.value = 'config'
  dialogTitle.value = '新增存储配置'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

/**
 * 编辑存储配置
 * @param {Object} row 当前行数据
 */
const editStorage = (row) => {
  dialogTitle.value = '编辑存储配置'
  isEdit.value = true
  Object.assign(storageForm, {
    ossId: row.ossId,
    ossType: row.ossType,
    ossBacketName: row.ossBacketName,
    ossFolder: row.ossFolder || '',
    ossRegion: row.ossRegion || '',
    ossEndpoint: row.ossEndpoint || '',
    ossAccessKeyId: row.ossAccessKeyId || '',
    ossAccessKeySecret: row.ossAccessKeySecret || '',
    ossStates: row.ossStates || '00',
    ossRemark: row.ossRemark || ''
  })
  dialogVisible.value = true
}

/**
 * 切换存储启用/禁用状态
 * @param {Object} row 当前行数据
 */
const toggleState = async (row) => {
  const newState = row.ossStates === '01' ? '00' : '01'
  const action = newState === '01' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}存储类型「${getStorageTypeName(row.ossType)}」吗？`,
      '确认操作',
      { type: 'warning' }
    )
    const res = await updateOssStates(row.ossId, newState)
      if (res && (res.code === 200 || res.code === 1)) {
        ElMessage.success(`${action}成功`)
        await loadStorages()
        monitorPanelRef.value?.refresh?.()
      } else {
      ElMessage.error(res?.message || `${action}失败`)
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('切换状态失败:', e)
    }
  }
}

/**
 * 保存存储配置
 */
const saveStorage = async () => {
  if (!storageFormRef.value) return
  await storageFormRef.value.validate(async (valid) => {
    if (!valid) return
    saveLoading.value = true
    try {
      const res = await saveOssConfig({ ...storageForm })
      if (res && (res.code === 200 || res.code === 1)) {
        ElMessage.success(isEdit.value ? '存储配置更新成功' : '存储配置添加成功')
        dialogVisible.value = false
        await loadStorages()
        monitorPanelRef.value?.refresh?.()
      } else {
        ElMessage.error(res?.message || '保存失败')
      }
    } catch (error) {
      console.error('保存存储配置失败:', error)
      ElMessage.error(error?.message || '保存失败，请重试')
    } finally {
      saveLoading.value = false
    }
  })
}

/**
 * 切换存储类型时设置默认值
 * @param {string} value 存储类型
 */
const onStorageTypeChange = (value) => {
  storageForm.ossRegion = ''
  storageForm.ossEndpoint = ''
  storageForm.ossAccessKeyId = ''
  storageForm.ossAccessKeySecret = ''
  switch (value) {
    case 'aliyun':
      storageForm.ossEndpoint = 'https://oss-cn-hangzhou.aliyuncs.com'
      storageForm.ossRegion = 'oss-cn-hangzhou'
      break
    case 'tencentCloud':
      storageForm.ossEndpoint = 'https://cos.ap-beijing.myqcloud.com'
      storageForm.ossRegion = 'ap-beijing'
      break
    case 'minio':
      storageForm.ossRegion = 'http://127.0.0.1:9000'
      storageForm.ossEndpoint = 'https://minio.example.com'
      break
    case 'westFss':
      storageForm.ossRegion = 'ftp-my.vhostgo.com'
      storageForm.ossEndpoint = 'https://fss-my.myhostadmin.net'
      break
    default:
      break
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(storageForm, {
    ossId: null,
    ossType: 'aliyun',
    ossBacketName: '',
    ossFolder: '',
    ossRegion: '',
    ossEndpoint: '',
    ossAccessKeyId: '',
    ossAccessKeySecret: '',
    ossStates: '01',
    ossRemark: ''
  })
  if (storageFormRef.value) {
    storageFormRef.value.clearValidate()
  }
}

onMounted(() => {
  loadStorages()
})
</script>

<style scoped>
.system-storage {
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

.field-hint {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
  margin-top: 4px;
}
</style>
