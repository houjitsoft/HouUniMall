<template>
  <div class="role-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      
      <!-- 角色列表 -->
      <el-table :data="roleList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="150" />
        <el-table-column prop="description" label="角色描述" width="300" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'enabled' ? 'success' : 'info'">
              {{ scope.row.status === 'enabled' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userCount" label="用户数量" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="success" @click="handlePermission(scope.row)">权限设置</el-button>
            <el-button size="small" type="warning" @click="handleToggleStatus(scope.row)">
              {{ scope.row.status === 'enabled' ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="roleForm" :rules="rules" ref="roleFormRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input type="textarea" v-model="roleForm.description" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限设置"
      width="800px"
    >
      <div class="permission-tree">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedPermissions"
          :props="{ label: 'name', children: 'children' }"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermissions">保存权限</el-button>
        </span>
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

// 响应式数据
const loading = ref(false)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const roleFormRef = ref(null)
const permissionTreeRef = ref(null)
const currentRole = ref(null)
const checkedPermissions = ref([])

// 角色表单
const roleForm = reactive({
  id: '',
  name: '',
  code: '',
  description: '',
  status: 'enabled'
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 角色数据
const roleList = ref([
  {
    id: 1,
    name: '超级管理员',
    code: 'SUPER_ADMIN',
    description: '系统超级管理员，拥有所有权限',
    status: 'enabled',
    userCount: 1,
    createTime: '2025-01-01 00:00:00'
  },
  {
    id: 2,
    name: '系统管理员',
    code: 'SYSTEM_ADMIN',
    description: '系统管理员，负责系统配置和用户管理',
    status: 'enabled',
    userCount: 3,
    createTime: '2025-01-01 00:00:00'
  },
  {
    id: 3,
    name: '业务管理员',
    code: 'BUSINESS_ADMIN',
    description: '业务管理员，负责业务数据管理',
    status: 'enabled',
    userCount: 5,
    createTime: '2025-01-01 00:00:00'
  },
  {
    id: 4,
    name: '普通用户',
    code: 'NORMAL_USER',
    description: '普通用户，只能查看和操作自己的数据',
    status: 'enabled',
    userCount: 25,
    createTime: '2025-01-01 00:00:00'
  }
])

// 权限树数据
const permissionTree = ref([
  {
    id: 1,
    name: '系统管理',
    children: [
      { id: 11, name: '用户管理' },
      { id: 12, name: '角色管理' },
      { id: 13, name: '权限管理' },
      { id: 14, name: '系统配置' }
    ]
  },
  {
    id: 2,
    name: '商品管理',
    children: [
      { id: 21, name: '商品列表' },
      { id: 22, name: '商品分类' },
      { id: 23, name: '库存管理' },
      { id: 24, name: '价格管理' }
    ]
  },
  {
    id: 3,
    name: '订单管理',
    children: [
      { id: 31, name: '订单列表' },
      { id: 32, name: '订单详情' },
      { id: 33, name: '订单处理' },
      { id: 34, name: '退款管理' }
    ]
  },
  {
    id: 4,
    name: '财务管理',
    children: [
      { id: 41, name: '收入统计' },
      { id: 42, name: '支出管理' },
      { id: 43, name: '财务报表' },
      { id: 44, name: '税务管理' }
    ]
  }
])

// 生命周期
onMounted(() => {
  fetchRoleList()
})

// 获取角色列表
const fetchRoleList = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 新增角色
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  Object.assign(roleForm, {
    id: '',
    name: '',
    code: '',
    description: '',
    status: 'enabled'
  })
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(roleForm, row)
  dialogVisible.value = true
}

// 权限设置
const handlePermission = (row) => {
  currentRole.value = row
  // 模拟获取角色权限
  checkedPermissions.value = [11, 21, 31, 41]
  permissionDialogVisible.value = true
}

// 切换状态
const handleToggleStatus = (row) => {
  const action = row.status === 'enabled' ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${action}这个角色吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    row.status = row.status === 'enabled' ? 'disabled' : 'enabled'
    ElMessage.success(`${action}成功`)
  })
}

// 删除角色
const handleDelete = (row) => {
  if (row.userCount > 0) {
    ElMessage.warning('该角色下还有用户，无法删除')
    return
  }
  
  ElMessageBox.confirm('确定要删除这个角色吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = roleList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      roleList.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!roleFormRef.value) return
  
  try {
    await roleFormRef.value.validate()
    
    if (roleForm.id) {
      // 编辑模式
      const index = roleList.value.findIndex(item => item.id === roleForm.id)
      if (index > -1) {
        Object.assign(roleList.value[index], roleForm)
      }
      ElMessage.success('编辑成功')
    } else {
      // 新增模式
      const newRole = {
        ...roleForm,
        id: Date.now(),
        userCount: 0,
        createTime: new Date().toLocaleString()
      }
      roleList.value.push(newRole)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 保存权限
const savePermissions = () => {
  if (!permissionTreeRef.value) return
  
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
  
  console.log('选中的权限:', checkedKeys)
  console.log('半选中的权限:', halfCheckedKeys)
  
  ElMessage.success('权限保存成功')
  permissionDialogVisible.value = false
}
</script>

<style scoped>
.role-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  text-align: right;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>
