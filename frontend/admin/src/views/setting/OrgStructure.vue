<template>
  <div class="org-structure">
    <div class="page-header">
      <h2>公司信息管理</h2>
      <div class="header-actions">
        <el-button v-if="can('shell:organization:batch-delete')" @click="handleBatchDelete" :disabled="selectedRows.length === 0">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button v-if="can('shell:organization:export')" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
        <el-button v-if="can('shell:organization:add')" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加公司
        </el-button>
      </div>
    </div>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="公司名称">
          <el-input v-model="searchForm.orgName" placeholder="请输入公司名称" clearable />
        </el-form-item>
        <el-form-item label="组织类型">
          <el-select v-model="searchForm.orgType" placeholder="请选择组织类型" clearable>
            <el-option label="公司" value="company" />
            <el-option label="分公司" value="branch" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公司信息列表 -->
    <el-card>
      <el-table
        :data="orgList"
        :loading="loading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="can('shell:organization:batch-delete')" type="selection" width="55" />
        <el-table-column prop="orgCode" label="组织编码" width="120" />
        <el-table-column prop="orgName" label="公司名称" min-width="150" />
        <el-table-column prop="orgType" label="组织类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.orgType === 1 ? 'primary' : 'success'">
              {{ scope.row.orgType === 1 ? '公司' : '部门' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="legalPerson" label="法人代表" width="120" />
        <el-table-column prop="businessLicense" label="营业执照号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="registeredCapital" label="注册资本" width="120">
          <template #default="scope">
            {{ scope.row.registeredCapital ? formatCurrency(scope.row.registeredCapital) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="establishDate" label="成立日期" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="contactEmail" label="联系邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column prop="orgStatus" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.orgStatus === 1 ? 'success' : 'danger'">
              {{ scope.row.orgStatus === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button v-if="can('shell:organization:edit')" size="small" type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button v-if="can('shell:organization:view')" size="small" type="success" @click="handleViewDetail(scope.row)">
              详情
            </el-button>
            <el-button v-if="canStatus(scope.row)"
              size="small"
              :type="Number(scope.row.orgStatus ?? scope.row.status) === 1 ? 'warning' : 'success'"
              @click="handleStatusToggle(scope.row)"
            >
              {{ Number(scope.row.orgStatus ?? scope.row.status) === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="can('shell:organization:delete')"
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑公司对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑公司' : '添加公司'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织编码" prop="orgCode">
              <el-input v-model="formData.orgCode" placeholder="请输入组织编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称" prop="orgName">
              <el-input v-model="formData.orgName" placeholder="请输入公司名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织类型" prop="orgType">
              <el-select v-model="formData.orgType" placeholder="请选择组织类型">
                <el-option label="公司" value="company" />
                <el-option label="分公司" value="branch" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法人代表" prop="legalPerson">
              <el-input v-model="formData.legalPerson" placeholder="请输入法人代表" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="营业执照号" prop="businessLicense">
              <el-input v-model="formData.businessLicense" placeholder="请输入营业执照号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税号" prop="taxNumber">
              <el-input v-model="formData.taxNumber" placeholder="请输入税号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="注册资本" prop="registeredCapital">
              <el-input-number 
                v-model="formData.registeredCapital" 
                :min="0" 
                :precision="2"
                placeholder="请输入注册资本"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成立日期" prop="establishDate">
              <el-date-picker
                v-model="formData.establishDate"
                type="date"
                placeholder="请选择成立日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="经营范围" prop="businessScope">
          <el-input
            v-model="formData.businessScope"
            type="textarea"
            :rows="3"
            placeholder="请输入经营范围"
          />
        </el-form-item>

        <el-form-item label="注册地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入注册地址" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="formData.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="官网地址" prop="website">
              <el-input v-model="formData.website" placeholder="请输入官网地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Logo地址" prop="logoUrl">
              <el-input v-model="formData.logoUrl" placeholder="请输入Logo地址" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button v-if="can(isEdit ? 'shell:organization:edit' : 'shell:organization:add')" type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 公司详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="公司详情"
      width="800px"
    >
      <div v-if="currentOrg" class="org-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="组织编码">
            {{ currentOrg.orgCode }}
          </el-descriptions-item>
          <el-descriptions-item label="公司名称">
            {{ currentOrg.orgName }}
          </el-descriptions-item>
          <el-descriptions-item label="组织类型">
            <el-tag :type="currentOrg.orgType === 'company' ? 'primary' : 'success'">
              {{ currentOrg.orgType === 'company' ? '公司' : '分公司' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="法人代表">
            {{ currentOrg.legalPerson || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="营业执照号">
            {{ currentOrg.businessLicense || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="税号">
            {{ currentOrg.taxNumber || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册资本">
            {{ currentOrg.registeredCapital ? formatCurrency(currentOrg.registeredCapital) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="成立日期">
            {{ currentOrg.establishDate || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="经营范围" :span="2">
            {{ currentOrg.businessScope || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册地址" :span="2">
            {{ currentOrg.address || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentOrg.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系邮箱">
            {{ currentOrg.contactEmail || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="官网地址">
            {{ currentOrg.website || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="Logo地址">
            {{ currentOrg.logoUrl || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentOrg.status === 1 ? 'success' : 'danger'">
              {{ currentOrg.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="排序">
            {{ currentOrg.sortOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentOrg.remark || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentOrg.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ currentOrg.updateTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Delete, Download } from '@element-plus/icons-vue'
import { organizationApi } from '../../api/setting/organization'
import { hasPermission } from '@/utils/permission'

const can = (permission) => hasPermission(permission)
const canStatus = (row) => can(Number(row?.orgStatus ?? row?.status) === 1
  ? 'shell:organization:disable'
  : 'shell:organization:enable')
const ensurePermission = (permission) => {
  if (can(permission)) return true
  ElMessage.warning('当前账号没有该操作权限')
  return false
}

// 数据
const loading = ref(false)
const showDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const orgList = ref([])
const currentOrg = ref(null)
const selectedRows = ref([])

const searchForm = reactive({
  orgName: '',
  orgType: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const formData = reactive({
  id: '',
  orgCode: '',
  orgName: '',
  orgType: 'company',
  parentId: null,
  legalPerson: '',
  businessLicense: '',
  taxNumber: '',
  registeredCapital: null,
  establishDate: '',
  businessScope: '',
  address: '',
  contactPhone: '',
  contactEmail: '',
  website: '',
  logoUrl: '',
  status: 1,
  sortOrder: 0,
  remark: ''
})

const rules = {
  orgCode: [{ required: true, message: '请输入组织编码', trigger: 'blur' }],
  orgName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  orgType: [{ required: true, message: '请选择组织类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 方法
const loadOrgList = async () => {
  try {
    loading.value = true
    
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      orgName: searchForm.orgName,
      orgType: searchForm.orgType,
      orgStatus: searchForm.status
    }

    const response = await organizationApi.getOrganizationPage(params)

    if (response.code === 1 || response.code === 200) {
      orgList.value = response.records || response.data?.records || []
      pagination.total = response.total ?? response.data?.total ?? 0
    } else {
      ElMessage.error(response.message || '加载组织列表失败')
      orgList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载组织列表失败:', error)
    ElMessage.error('加载组织列表失败: ' + error.message)
    orgList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadOrgList()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

const handleAdd = () => {
  if (!ensurePermission('shell:organization:add')) return
  isEdit.value = false
  resetForm()
  showDialog.value = true
}

const handleEdit = (data) => {
  if (!ensurePermission('shell:organization:edit')) return
  isEdit.value = true
  Object.assign(formData, data)
  showDialog.value = true
}

const handleViewDetail = (data) => {
  if (!ensurePermission('shell:organization:view')) return
  currentOrg.value = data
  showDetailDialog.value = true
}

const handleDelete = async (data) => {
  if (!ensurePermission('shell:organization:delete')) return
  try {
    await ElMessageBox.confirm(
      `确定要删除公司 "${data.orgName}" 吗？删除后将无法恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 删除组织
    const response = await organizationApi.deleteOrganization(data.orgId)
    
    if (response.code === 1) {
      ElMessage.success('删除成功')
      loadOrgList() // 重新加载列表
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公司失败:', error)
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const resetForm = () => {
  Object.keys(formData).forEach(key => {
    if (key === 'status') {
      formData[key] = 1
    } else if (key === 'sortOrder') {
      formData[key] = 0
    } else if (key === 'orgType') {
      formData[key] = 'company'
    } else if (key === 'registeredCapital') {
      formData[key] = null
    } else if (key === 'parentId') {
      formData[key] = null
    } else {
      formData[key] = ''
    }
  })
}

const handleSave = async () => {
  try {
    if (!ensurePermission(isEdit.value ? 'shell:organization:edit' : 'shell:organization:add')) return
    // 表单验证
    if (!formData.orgCode) {
      ElMessage.error('请输入组织编码')
      return
    }
    if (!formData.orgName) {
      ElMessage.error('请输入公司名称')
      return
    }
    if (!formData.orgType) {
      ElMessage.error('请选择组织类型')
      return
    }
    
    // 检查组织名称是否重复
    try {
      const checkResponse = await organizationApi.checkOrgNameExists(formData.orgName, formData.id)
      if (checkResponse.data) {
        ElMessage.error('组织名称已存在，请使用其他名称')
        return
      }
    } catch (error) {
      console.warn('检查组织名称失败:', error)
    }
    
    let response
    if (isEdit.value) {
      // 更新组织
      response = await organizationApi.updateOrganization(formData)
    } else {
      // 创建组织
      response = await organizationApi.addOrganization(formData)
    }
    
    if (response.code === 1) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      showDialog.value = false
      resetForm()
      loadOrgList() // 重新加载列表
    } else {
      ElMessage.error(response.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('保存公司失败:', error)
    ElMessage.error('保存失败: ' + error.message)
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadOrgList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadOrgList()
}

const formatCurrency = (amount) => {
  if (!amount) return '-'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleBatchDelete = async () => {
  if (!ensurePermission('shell:organization:batch-delete')) return
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的公司')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 家公司吗？删除后将无法恢复！`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量删除
    const selectedIds = selectedRows.value.map(row => row.orgId)
    const response = await organizationApi.batchDeleteOrganization(selectedIds)
    
    if (response.code === 1) {
      selectedRows.value = []
      ElMessage.success(`成功删除 ${selectedIds.length} 家组织`)
      loadOrgList() // 重新加载列表
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + error.message)
    }
  }
}

const handleExport = () => {
  if (!ensurePermission('shell:organization:export')) return
  if (orgList.value.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  
  // 模拟导出功能
  const exportData = orgList.value.map(org => ({
    组织编码: org.orgCode,
    公司名称: org.orgName,
    组织类型: org.orgType === 'company' ? '公司' : '分公司',
    法人代表: org.legalPerson || '',
    营业执照号: org.businessLicense || '',
    税号: org.taxNumber || '',
    注册资本: org.registeredCapital ? formatCurrency(org.registeredCapital) : '',
    成立日期: org.establishDate || '',
    经营范围: org.businessScope || '',
    注册地址: org.address || '',
    联系电话: org.contactPhone || '',
    联系邮箱: org.contactEmail || '',
    官网地址: org.website || '',
    状态: org.status === 1 ? '启用' : '禁用',
    创建时间: org.createTime
  }))
  
  // 创建CSV内容
  const headers = Object.keys(exportData[0]).join(',')
  const rows = exportData.map(row => Object.values(row).join(','))
  const csvContent = [headers, ...rows].join('\n')
  
  // 下载文件
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `公司信息_${new Date().toLocaleDateString()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('导出成功')
}

const handleStatusToggle = async (data) => {
  const currentStatus = Number(data?.orgStatus ?? data?.status)
  const nextStatus = currentStatus === 1 ? 0 : 1
  const permission = nextStatus === 1 ? 'shell:organization:enable' : 'shell:organization:disable'
  if (!ensurePermission(permission)) return
  try {
    const response = await organizationApi.updateOrganizationStatus(data.orgId, nextStatus)
    if (response.code === 1 || response.code === 200) {
      ElMessage.success(nextStatus === 1 ? '启用成功' : '禁用成功')
      await loadOrgList()
    } else {
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error) {
    ElMessage.error('状态更新失败: ' + error.message)
  }
}

onMounted(async () => {
  await loadOrgList()
})
</script>

<style scoped>
.org-structure {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.org-detail {
  max-height: 500px;
  overflow-y: auto;
}
</style>
