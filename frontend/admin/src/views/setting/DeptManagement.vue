<template>
  <div class="dept-management">
    <div class="page-header">
      <h2>部门管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加部门
      </el-button>
    </div>

    <!-- 搜索条件 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="所属公司">
          <el-select v-model="searchForm.orgId" placeholder="请选择公司" clearable>
            <el-option
              v-for="org in orgList"
              :key="org.id"
              :label="org.orgName"
              :value="org.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称">
          <el-input v-model="searchForm.deptName" placeholder="请输入部门名称" clearable />
        </el-form-item>
        <el-form-item label="部门类型">
          <el-select v-model="searchForm.deptType" placeholder="请选择部门类型" clearable>
            <el-option label="部门" value="department" />
            <el-option label="小组" value="team" />
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

    <!-- 部门列表 -->
    <el-card>
      <el-table
        :data="deptList"
        :loading="loading"
        stripe
        border
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      >
        <el-table-column prop="departmentCode" label="部门编码" width="120" />
        <el-table-column prop="departmentName" label="部门名称" min-width="150" />
        <el-table-column prop="parentName" label="上级部门" width="150" />
        <el-table-column prop="departmentType" label="部门类型" width="100">
          <template #default="scope">
            <el-tag :type="getDeptTypeTagType(scope.row.departmentType)">
              {{ scope.row.departmentType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="负责人" width="120" />
        <el-table-column prop="departmentLevel" label="层级" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="departmentStatus" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.departmentStatus === '正常' ? 'success' : 'danger'">
              {{ scope.row.departmentStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="success" @click="handleAddChild(scope.row)">
              添加子部门
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

    <!-- 添加/编辑部门对话框 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑部门' : '添加部门'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="所属公司" prop="orgId">
          <el-select v-model="formData.orgId" placeholder="请选择公司" style="width: 100%">
            <el-option
              v-for="org in orgList"
              :key="org.id"
              :label="org.orgName"
              :value="org.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="formData.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="deptTree"
            :props="treeProps"
            placeholder="请选择上级部门"
            check-strictly
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="部门类型" prop="deptType">
          <el-select v-model="formData.deptType" placeholder="请选择部门类型" style="width: 100%">
            <el-option label="部门" value="department" />
            <el-option label="小组" value="team" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="部门负责人" prop="managerName">
          <el-input v-model="formData.managerName" placeholder="请输入部门负责人姓名" />
        </el-form-item>
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        
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
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { departmentService, organizationService, departmentApi, organizationApi } from '../../api/setting/organization'

// 数据
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const deptList = ref([])
const orgList = ref([])
const deptTree = ref([])

const searchForm = reactive({
  orgId: '',
  deptName: '',
  deptType: '',
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
  deptCode: '',
  deptName: '',
  parentId: null,
  deptType: 'department',
  managerName: '',
  level: 1,
  sortOrder: 0,
  status: 1,
  remark: ''
})

const treeProps = {
  children: 'children',
  label: 'deptName',
  value: 'id'
}

const rules = {
  orgId: [{ required: true, message: '请选择所属公司', trigger: 'change' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptType: [{ required: true, message: '请选择部门类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 部门类型标签样式
const getDeptTypeTagType = (deptType) => {
  const typeMap = {
    '管理部门': 'primary',
    '业务部门': 'success', 
    '支持部门': 'info',
    '技术部门': 'warning'
  }
  return typeMap[deptType] || 'info'
}

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
    loading.value = true
    // 构建查询参数
    const params = {
      current: pagination.current,
      size: pagination.size,
      departmentName: searchForm.deptName,
      departmentCode: searchForm.deptCode,
      parentId: searchForm.parentId
    }
    
    const response = await departmentApi.getDepartmentPage(params)

    if (response.code === 1 || response.code === 200) {
      deptList.value = response.records || response.data?.records || []
      pagination.total = response.total ?? response.data?.total ?? 0
    } else {
      ElMessage.error(response.message || '加载部门列表失败')
      deptList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载部门列表失败:', error)
    ElMessage.error('加载部门列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const loadDeptTree = async () => {
  try {
    // 构建部门树形结构，用于上级部门选择
    deptTree.value = [
      {
        id: 1,
        deptName: '技术部',
        children: [
          { id: 2, deptName: '前端组' },
          { id: 3, deptName: '后端组' }
        ]
      },
      { id: 4, deptName: '市场部' },
      { id: 5, deptName: '人事部' }
    ]
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadDeptList()
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

const handleAddChild = (parentData) => {
  isEdit.value = false
  resetForm()
  formData.parentId = parentData.id
  formData.orgId = parentData.orgId
  showDialog.value = true
}

const handleEdit = (data) => {
  isEdit.value = true
  Object.assign(formData, data)
  showDialog.value = true
}

const handleDelete = async (data) => {
  try {
    // 检查是否有子部门
    const hasChildren = deptList.value.some(dept => dept.parentId === data.id)
    if (hasChildren) {
      ElMessage.error('该部门下还有子部门，请先删除子部门')
      return
    }
    
    await ElMessageBox.confirm(
      `确定要删除部门 "${data.departmentName}" 吗？删除后将无法恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 删除部门
    const response = await departmentApi.deleteDepartment(data.id)
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success('删除成功')
      loadDeptList() // 重新加载列表
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
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
    } else if (key === 'deptType') {
      formData[key] = 'department'
    } else if (key === 'level') {
      formData[key] = 1
    } else if (key === 'parentId') {
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
    if (!formData.deptCode) {
      ElMessage.error('请输入部门编码')
      return
    }
    if (!formData.deptName) {
      ElMessage.error('请输入部门名称')
      return
    }
    if (!formData.deptType) {
      ElMessage.error('请选择部门类型')
      return
    }
    
    // 检查部门编码是否重复
    const existingDept = deptList.value.find(dept => 
      dept.departmentCode === formData.deptCode && dept.id !== formData.id
    )
    if (existingDept) {
      ElMessage.error('部门编码已存在，请使用其他编码')
      return
    }
    
    // 计算部门层级
    if (formData.parentId) {
      const parentDept = deptList.value.find(dept => dept.id === formData.parentId)
      if (parentDept) {
        formData.level = parentDept.level + 1
      }
    } else {
      formData.level = 1
    }
    
    let response
    if (isEdit.value) {
      // 更新部门
      response = await departmentApi.updateDepartment(formData.id, formData)
    } else {
      // 新增部门
      response = await departmentApi.addDepartment(formData)
    }
    
    if (response.code === 1 || response.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      showDialog.value = false
      resetForm()
      loadDeptList() // 重新加载列表
    } else {
      ElMessage.error(response.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('保存部门失败:', error)
    ElMessage.error('保存失败: ' + error.message)
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadDeptList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadDeptList()
}

onMounted(async () => {
  await loadOrgList()
  await loadDeptList()
  await loadDeptTree()
})
</script>

<style scoped>
.dept-management {
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
</style>
