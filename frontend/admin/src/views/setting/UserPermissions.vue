<template>
  <div class="user-permissions">
    <PermissionTabs />
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户权限管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.mobile" placeholder="请输入手机号" clearable></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="请选择角色" clearable>
            <el-option 
              v-for="role in roleList" 
              :key="role.id" 
              :label="role.name" 
              :value="role.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUsers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="userList" v-loading="loading" style="width: 100%" stripe>
        <el-table-column prop="username" label="用户名" width="150"></el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="mobile" label="手机号" width="130"></el-table-column>
        <el-table-column prop="roles" label="角色" width="150">
          <template #default="scope">
            <el-tag 
              v-for="role in scope.row.roles" 
              :key="role" 
              size="small" 
              style="margin-right: 4px;"
            >
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="small" @click="editPermissions(scope.row)">权限设置</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="updateUserStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" type="info" @click="resetPassword(scope.row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 15, 20, 50]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 权限设置对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="权限设置" width="800px">
      <div class="permission-content">
        <div class="user-info">
          <h4>用户信息</h4>
          <p>用户名：{{ currentUser.username }}</p>
          <p>真实姓名：{{ currentUser.realName }}</p>
        </div>
        
        <div class="permission-tree">
          <h4>权限配置</h4>
          <el-tree
            ref="permissionTree"
            :data="permissionTreeData"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedPermissions"
            :props="treeProps"
          ></el-tree>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import PermissionTabs from '@/components/setting/PermissionTabs.vue'

// 响应式数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(15)
const totalCount = ref(0)

const searchForm = reactive({
  username: '',
  realName: '',
  email: '',
  mobile: '',
  status: null,
  roleId: null
})

const userList = ref([])
const roleList = ref([])

const permissionDialogVisible = ref(false)
const currentUser = ref({})
const checkedPermissions = ref([])

const permissionTreeData = ref([
  {
    id: 1,
    label: '系统管理',
    children: [
      { id: 11, label: '用户管理' },
      { id: 12, label: '角色管理' },
      { id: 13, label: '权限管理' }
    ]
  },
  {
    id: 2,
    label: '业务管理',
    children: [
      { id: 21, label: '订单管理' },
      { id: 22, label: '商品管理' },
      { id: 23, label: '库存管理' }
    ]
  },
  {
    id: 3,
    label: '系统设置',
    children: [
      { id: 31, label: '基础设置' },
      { id: 32, label: '安全设置' },
      { id: 33, label: '日志管理' }
    ]
  }
])

const treeProps = {
  children: 'children',
  label: 'label'
}

// 计算属性
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

// API调用
const getUserList = async () => {
  try {
    loading.value = true
    const response = await request.post('/web/permission/user/list', {
      pageNo: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    })
    
    if ([1, 200].includes(response.code)) {
      userList.value = response.data.records || response.data.list || []
      totalCount.value = response.data.total ?? response.data.totalCount ?? 0
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const getRoleList = async () => {
  try {
    const response = await request.get('/web/permission/role/all')
    if ([1, 200].includes(response.code)) {
      roleList.value = response.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const getMenuList = async () => {
  try {
    const response = await request.post('/web/permission/menu/list')
    if ([1, 200].includes(response.code)) {
      permissionTreeData.value = response.data || []
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
  }
}

// 事件处理
const searchUsers = () => {
  currentPage.value = 1
  getUserList()
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.email = ''
  searchForm.mobile = ''
  searchForm.status = null
  searchForm.roleId = null
  currentPage.value = 1
  getUserList()
}

const editPermissions = async (user) => {
  currentUser.value = user
  try {
    const response = await request.get(`/web/permission/user/detail/${user.id}`)
    if ([1, 200].includes(response.code)) {
      // 这里可以根据用户角色获取对应的菜单权限
      checkedPermissions.value = [11, 21, 31] // 临时数据
      permissionDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户 ${user.username} 的密码吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 生成随机密码
    const newPassword = Math.random().toString(36).slice(-8)
    
    const response = await request.post('/web/permission/user/reset-password', {
      id: user.id,
      newPassword: newPassword
    })
    
    if ([1, 200].includes(response.code)) {
      ElMessage.success(`密码重置成功，新密码：${newPassword}`)
    } else {
      ElMessage.error(response.message || '密码重置失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('密码重置失败')
    }
  }
}

const updateUserStatus = async (user) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    const statusText = newStatus === 1 ? '启用' : '禁用'
    
    await ElMessageBox.confirm(`确定要${statusText}用户 ${user.username} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.post('/web/permission/user/update-status', {
      id: user.id,
      status: newStatus
    })
    
    if ([1, 200].includes(response.code)) {
      ElMessage.success(`${statusText}成功`)
      getUserList()
    } else {
      ElMessage.error(response.message || `${statusText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新用户状态失败:', error)
      ElMessage.error('更新用户状态失败')
    }
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.post('/web/permission/user/delete', {
      id: user.id
    })
    
    if ([1, 200].includes(response.code)) {
      ElMessage.success('删除成功')
      getUserList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const savePermissions = () => {
  const checkedKeys = permissionTree.value.getCheckedKeys()
  ElMessage.success('权限设置保存成功')
  permissionDialogVisible.value = false
}

const permissionTree = ref()

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getUserList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getUserList()
}

onMounted(() => {
getUserList()
  getRoleList()
  getMenuList()
})
</script>

<style scoped>
.user-permissions {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.permission-content {
  display: flex;
  gap: 30px;
}

.user-info {
  flex: 1;
}

.user-info h4 {
  margin-bottom: 15px;
  color: #303133;
}

.user-info p {
  margin: 8px 0;
  color: #606266;
}

.permission-tree {
  flex: 2;
}

.permission-tree h4 {
  margin-bottom: 15px;
  color: #303133;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
