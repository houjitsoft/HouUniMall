<template>
  <div class="job-management">
    <div class="page-header">
      <h2>岗位管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加岗位
      </el-button>
    </div>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="所属公司">
          <el-select v-model="searchForm.orgId" placeholder="请选择公司" clearable>
            <el-option
              v-for="org in orgList"
              :key="org.orgId || org.id"
              :label="org.orgName"
              :value="org.orgId || org.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="searchForm.deptId" placeholder="请选择部门" clearable>
            <el-option
              v-for="dept in deptList"
              :key="dept.id"
              :label="dept.departmentName || dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.jobName" placeholder="请输入岗位名称" clearable />
        </el-form-item>
        <el-form-item label="岗位级别">
          <el-select v-model="searchForm.jobLevel" placeholder="请选择岗位级别" clearable>
            <el-option label="初级" value="junior" />
            <el-option label="中级" value="middle" />
            <el-option label="高级" value="senior" />
            <el-option label="专家" value="expert" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位类别">
          <el-select v-model="searchForm.jobCategory" placeholder="请选择岗位类别" clearable>
            <el-option label="技术" value="technical" />
            <el-option label="管理" value="management" />
            <el-option label="销售" value="sales" />
            <el-option label="支持" value="support" />
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

    <!-- 岗位列表 -->
    <el-card>
      <el-table
        :data="jobList"
        :loading="loading"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="code" label="岗位编码" width="120" />
        <el-table-column prop="name" label="岗位名称" min-width="150" />
        <el-table-column prop="orgName" label="所属公司" width="120" />
        <el-table-column prop="deptName" label="所属部门" width="120" />
        <el-table-column prop="level" label="岗位级别" width="100">
          <template #default="scope">
            <el-tag :type="getLevelTagType(scope.row.level)">
              {{ getLevelText(scope.row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="jobCategory" label="岗位类别" width="100">
          <template #default="scope">
            <el-tag :type="getCategoryTagType(scope.row.jobCategory)">
              {{ getCategoryText(scope.row.jobCategory) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workLocation" label="工作地点" width="120" />
        <el-table-column prop="workType" label="工作类型" width="100">
          <template #default="scope">
            <el-tag :type="getWorkTypeTagType(scope.row.workType)">
              {{ getWorkTypeText(scope.row.workType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="薪资范围" width="150">
          <template #default="scope">
            <span v-if="scope.row.minSalary && scope.row.maxSalary">
              {{ formatCurrency(scope.row.minSalary) }} - {{ formatCurrency(scope.row.maxSalary) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="人员配置" width="120">
          <template #default="scope">
            <span>{{ scope.row.currentCount }}/{{ scope.row.headcount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="success" @click="handleViewDetail(scope.row)">
              详情
            </el-button>
            <el-button 
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

    <!-- 添加/编辑岗位对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑岗位' : '添加岗位'"
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
            <el-form-item label="所属公司" prop="orgId">
              <el-select v-model="formData.orgId" placeholder="请选择公司" style="width: 100%" @change="handleOrgChange">
                <el-option
                  v-for="org in orgList"
                  :key="org.orgId || org.id"
                  :label="org.orgName"
                  :value="org.orgId || org.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-select v-model="formData.deptId" placeholder="请选择部门" style="width: 100%">
                <el-option
                  v-for="dept in filteredDeptList"
                  :key="dept.id"
                  :label="dept.deptName"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位编码" prop="jobCode">
              <el-input v-model="formData.jobCode" placeholder="请输入岗位编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="jobName">
              <el-input v-model="formData.jobName" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位级别" prop="jobLevel">
              <el-select v-model="formData.jobLevel" placeholder="请选择岗位级别" style="width: 100%">
                <el-option label="初级" value="junior" />
                <el-option label="中级" value="middle" />
                <el-option label="高级" value="senior" />
                <el-option label="专家" value="expert" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位类别" prop="jobCategory">
              <el-select v-model="formData.jobCategory" placeholder="请选择岗位类别" style="width: 100%">
                <el-option label="技术" value="technical" />
                <el-option label="管理" value="management" />
                <el-option label="销售" value="sales" />
                <el-option label="支持" value="support" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="岗位描述" prop="jobDescription">
          <el-input
            v-model="formData.jobDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入岗位描述"
          />
        </el-form-item>

        <el-form-item label="岗位要求" prop="jobRequirements">
          <el-input
            v-model="formData.jobRequirements"
            type="textarea"
            :rows="3"
            placeholder="请输入岗位要求"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低薪资" prop="minSalary">
              <el-input-number 
                v-model="formData.minSalary" 
                :min="0" 
                :precision="2"
                placeholder="请输入最低薪资"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最高薪资" prop="maxSalary">
              <el-input-number 
                v-model="formData.maxSalary" 
                :min="0" 
                :precision="2"
                placeholder="请输入最高薪资"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作地点" prop="workLocation">
              <el-input v-model="formData.workLocation" placeholder="请输入工作地点" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作类型" prop="workType">
              <el-select v-model="formData.workType" placeholder="请选择工作类型" style="width: 100%">
                <el-option label="全职" value="fulltime" />
                <el-option label="兼职" value="parttime" />
                <el-option label="合同工" value="contract" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="编制人数" prop="headcount">
              <el-input-number v-model="formData.headcount" :min="1" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前人数" prop="currentCount">
              <el-input-number v-model="formData.currentCount" :min="0" :max="999" />
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
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 岗位详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="岗位详情"
      width="800px"
    >
      <div v-if="currentJob" class="job-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="岗位编码">
            {{ currentJob.code }}
          </el-descriptions-item>
          <el-descriptions-item label="岗位名称">
            {{ currentJob.name }}
          </el-descriptions-item>
          <el-descriptions-item label="所属公司">
            {{ currentJob.orgName }}
          </el-descriptions-item>
          <el-descriptions-item label="所属部门">
            {{ currentJob.deptName }}
          </el-descriptions-item>
          <el-descriptions-item label="岗位级别">
            <el-tag :type="getLevelTagType(currentJob.level)">
              {{ getLevelText(currentJob.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="岗位类别">
            <el-tag :type="getCategoryTagType(currentJob.jobCategory)">
              {{ getCategoryText(currentJob.jobCategory) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="工作地点">
            {{ currentJob.workLocation || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="工作类型">
            <el-tag :type="getWorkTypeTagType(currentJob.workType)">
              {{ getWorkTypeText(currentJob.workType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="薪资范围">
            <span v-if="currentJob.minSalary && currentJob.maxSalary">
              {{ formatCurrency(currentJob.minSalary) }} - {{ formatCurrency(currentJob.maxSalary) }}
            </span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="人员配置">
            {{ currentJob.currentCount }}/{{ currentJob.headcount }}
          </el-descriptions-item>
          <el-descriptions-item label="岗位描述" :span="2">
            {{ currentJob.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="岗位要求" :span="2">
            {{ currentJob.jobRequirements || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentJob.status === 1 ? 'success' : 'danger'">
              {{ currentJob.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="排序">
            {{ currentJob.sortOrder }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentJob.remark || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentJob.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ currentJob.updateTime }}
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { positionService, organizationService, departmentService, positionApi, organizationApi, departmentApi } from '../../api/setting/organization'

// 数据
const loading = ref(false)
const showDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const jobList = ref([])
const orgList = ref([])
const deptList = ref([])
const currentJob = ref(null)

const searchForm = reactive({
  orgId: '',
  deptId: '',
  jobName: '',
  jobLevel: '',
  jobCategory: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const formData = reactive({
  id: '',
  orgId: '',
  deptId: '',
  jobCode: '',
  jobName: '',
  jobLevel: '',
  jobCategory: '',
  jobDescription: '',
  jobRequirements: '',
  minSalary: null,
  maxSalary: null,
  workLocation: '',
  workType: 'fulltime',
  headcount: 1,
  currentCount: 0,
  status: 1,
  sortOrder: 0,
  remark: ''
})

const rules = {
  orgId: [{ required: true, message: '请选择所属公司', trigger: 'change' }],
  jobCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  jobName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  jobLevel: [{ required: true, message: '请选择岗位级别', trigger: 'change' }],
  jobCategory: [{ required: true, message: '请选择岗位类别', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 计算属性
const filteredDeptList = computed(() => {
  if (!formData.orgId) return deptList.value
  return deptList.value.filter(dept => dept.orgId === formData.orgId)
})

// 方法
const loadOrgList = async () => {
  try {
    const response = await organizationApi.getOrganizationPage({
      pageNum: 1,
      pageSize: 100
    })
    if (response.code === 1 || response.code === 200) {
      orgList.value = response.records || response.data?.records || []
    } else {
      ElMessage.error(response.message || '加载公司列表失败')
      orgList.value = []
    }
  } catch (error) {
    console.error('加载公司列表失败:', error)
    ElMessage.error('加载公司列表失败: ' + error.message)
    orgList.value = []
  }
}

const loadDeptList = async () => {
  try {
    const response = await departmentApi.getDepartmentPage({
      current: 1,
      size: 100
    })
    if (response.code === 1 || response.code === 200) {
      deptList.value = response.records || response.data?.records || []
    } else {
      ElMessage.error(response.message || '加载部门列表失败')
      deptList.value = []
    }
  } catch (error) {
    console.error('加载部门列表失败:', error)
    ElMessage.error('加载部门列表失败: ' + error.message)
    deptList.value = []
  }
}

const loadJobList = async () => {
  try {
    loading.value = true
    // 构建查询参数
    const params = {
      current: pagination.current,
      size: pagination.size,
      name: searchForm.jobName
    }
    
    const response = await positionApi.getPositionPage(params)

    if (response.code === 1 || response.code === 200) {
      jobList.value = response.records || response.data?.records || []
      pagination.total = response.total ?? response.data?.total ?? 0
    } else {
      ElMessage.error(response.message || '加载岗位列表失败')
      jobList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载岗位列表失败:', error)
    ElMessage.error('加载岗位列表失败: ' + error.message)
    jobList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadJobList()
}

const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  showDialog.value = true
}

const handleEdit = (data) => {
  isEdit.value = true
  Object.assign(formData, data)
  showDialog.value = true
}

const handleViewDetail = (data) => {
  currentJob.value = data
  showDetailDialog.value = true
}

const handleDelete = async (data) => {
  try {
    // 检查是否有在职员工
    if (data.currentCount > 0) {
      ElMessage.error('该岗位还有在职员工，请先处理员工后再删除')
      return
    }
    
    await ElMessageBox.confirm(
      `确定要删除岗位 "${data.name}" 吗？删除后将无法恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 删除岗位
    const response = await positionApi.deletePosition(data.id)
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success('删除成功')
      loadJobList() // 重新加载列表
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除岗位失败:', error)
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const handleOrgChange = () => {
  // 当公司改变时，清空部门选择
  formData.deptId = ''
}

const resetForm = () => {
  Object.keys(formData).forEach(key => {
    if (key === 'status') {
      formData[key] = 1
    } else if (key === 'sortOrder') {
      formData[key] = 0
    } else if (key === 'workType') {
      formData[key] = 'fulltime'
    } else if (key === 'headcount') {
      formData[key] = 1
    } else if (key === 'currentCount') {
      formData[key] = 0
    } else if (['minSalary', 'maxSalary'].includes(key)) {
      formData[key] = null
    } else {
      formData[key] = ''
    }
  })
}

const handleSave = async () => {
  try {
    // 表单验证
    if (!formData.orgId) {
      ElMessage.error('请选择所属公司')
      return
    }
    if (!formData.jobCode) {
      ElMessage.error('请输入岗位编码')
      return
    }
    if (!formData.jobName) {
      ElMessage.error('请输入岗位名称')
      return
    }
    if (!formData.jobLevel) {
      ElMessage.error('请选择岗位级别')
      return
    }
    if (!formData.jobCategory) {
      ElMessage.error('请选择岗位类别')
      return
    }
    
    // 检查岗位编码是否重复
    const existingJob = jobList.value.find(job => 
      job.code === formData.jobCode && job.id !== formData.id
    )
    if (existingJob) {
      ElMessage.error('岗位编码已存在，请使用其他编码')
      return
    }
    
    // 验证薪资范围
    if (formData.minSalary && formData.maxSalary && formData.minSalary > formData.maxSalary) {
      ElMessage.error('最低薪资不能大于最高薪资')
      return
    }
    
    // 验证人员配置
    if (formData.currentCount > formData.headcount) {
      ElMessage.error('当前人数不能大于编制人数')
      return
    }
    
    let response
    if (isEdit.value) {
      // 更新岗位
      response = await positionApi.updatePosition(formData.id, formData)
    } else {
      // 新增岗位
      response = await positionApi.addPosition(formData)
    }
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      showDialog.value = false
      resetForm()
      loadJobList() // 重新加载列表
    } else {
      ElMessage.error(response.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('保存岗位失败:', error)
    ElMessage.error('保存失败: ' + error.message)
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadJobList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadJobList()
}

const getLevelTagType = (level) => {
  const typeMap = {
    'L4': 'info',
    'L5': 'primary',
    'L6': 'warning',
    'L7': 'success',
    'L8': 'danger',
    'L9': 'danger',
    'L10': 'danger'
  }
  return typeMap[level] || 'info'
}

const getLevelText = (level) => {
  const textMap = {
    'L4': '初级',
    'L5': '中级',
    'L6': '高级',
    'L7': '专家',
    'L8': '资深',
    'L9': '总监',
    'L10': '高管'
  }
  return textMap[level] || level || '未知'
}

const getCategoryTagType = (category) => {
  const typeMap = {
    'technical': 'primary',
    'management': 'danger',
    'sales': 'success',
    'support': 'info'
  }
  return typeMap[category] || 'info'
}

const getCategoryText = (category) => {
  const textMap = {
    'technical': '技术',
    'management': '管理',
    'sales': '销售',
    'support': '支持'
  }
  return textMap[category] || '未知'
}

const getWorkTypeTagType = (type) => {
  const typeMap = {
    'fulltime': 'primary',
    'parttime': 'success',
    'contract': 'warning'
  }
  return typeMap[type] || 'info'
}

const getWorkTypeText = (type) => {
  const textMap = {
    'fulltime': '全职',
    'parttime': '兼职',
    'contract': '合同工'
  }
  return textMap[type] || '未知'
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

onMounted(async () => {
  await loadOrgList()
  await loadDeptList()
  await loadJobList()
})
</script>

<style scoped>
.job-management {
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

.search-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.job-detail {
  max-height: 500px;
  overflow-y: auto;
}
</style>
