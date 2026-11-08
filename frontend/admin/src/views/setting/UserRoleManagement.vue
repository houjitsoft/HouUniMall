<template>
  <div class="user-role-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>用户角色管理</h2>
      </div>
      <div class="header-right">
        <el-icon class="header-icon"><FullScreen /></el-icon>
        <div class="user-info">
          <el-avatar size="small">admin</el-avatar>
          <span>admin</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <el-icon class="header-icon"><Setting /></el-icon>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tab-container">
      <div class="tabs">
        <div class="tab-item" :class="{ active: activeTab === 'workbench' }" @click="activeTab = 'workbench'">
          工作台
          <el-icon class="close-icon"><Close /></el-icon>
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'menu' }" @click="activeTab = 'menu'">
          菜单
          <el-icon class="close-icon"><Close /></el-icon>
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'role' }" @click="activeTab = 'role'">
          角色
          <el-icon class="close-icon"><Close /></el-icon>
        </div>
        <div class="tab-item active" :class="{ active: activeTab === 'user-role' }" @click="activeTab = 'user-role'">
          <div class="tab-dot"></div>
          用户角色
          <el-icon class="close-icon"><Close /></el-icon>
        </div>
        <el-icon class="more-icon"><ArrowDown /></el-icon>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="assignUserRole">
        <el-icon><Plus /></el-icon>
        分配用户角色
      </el-button>
    </div>

    <!-- 用户列表 -->
    <div class="table-container">
      <el-table 
        :data="userList" 
        v-loading="loading"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#fafafa', color: '#333' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="200">
          <template #default="scope">
            <el-tag 
              v-for="role in scope.row.roles" 
              :key="role.id" 
              size="small" 
              type="primary"
              style="margin-right: 4px;"
            >
              {{ role.name }}
            </el-tag>
            <span v-if="!scope.row.roles || scope.row.roles.length === 0" class="no-role">无角色</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="editUserRoles(scope.row)"
            >
              编辑角色
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              link
              @click="viewUserPermissions(scope.row)"
            >
              查看权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <div class="pagination-info">
        共 {{ totalCount }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
      </div>
      <div class="pagination-controls">
        <div class="pagination-nav">
          <el-button 
            :disabled="currentPage <= 1" 
            @click="handlePrevPage"
            size="small"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <span class="page-number">{{ currentPage }}</span>
          <el-button 
            :disabled="currentPage >= totalPages" 
            @click="handleNextPage"
            size="small"
          >
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="page-jump">
          <span>跳转到</span>
          <el-input-number 
            v-model="jumpPage" 
            :min="1" 
            :max="totalPages" 
            size="small"
            style="width: 60px;"
          />
          <el-button size="small" @click="handleJumpPage">确定</el-button>
        </div>
        <el-select v-model="pageSize" @change="handleSizeChange" size="small">
          <el-option label="15条/页" :value="15" />
          <el-option label="20条/页" :value="20" />
          <el-option label="50条/页" :value="50" />
          <el-option label="100条/页" :value="100" />
        </el-select>
      </div>
    </div>

    <!-- 编辑用户角色对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑用户角色" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="user-info-section">
          <h4>用户信息</h4>
          <p><strong>用户名：</strong>{{ currentUser.username }}</p>
          <p><strong>真实姓名：</strong>{{ currentUser.realName }}</p>
          <p><strong>邮箱：</strong>{{ currentUser.email }}</p>
        </div>
        
        <div class="role-selection-section">
          <h4>角色选择</h4>
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox 
              v-for="role in allRoles" 
              :key="role.id" 
              :label="role.id"
              :value="role.id"
            >
              {{ role.name }} - {{ role.desc }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserRoles">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看用户权限对话框 -->
    <el-dialog 
      v-model="permissionDialogVisible" 
      title="用户权限" 
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="dialog-content">
        <div class="user-info-section">
          <h4>用户信息</h4>
          <p><strong>用户名：</strong>{{ currentUser.username }}</p>
          <p><strong>真实姓名：</strong>{{ currentUser.realName }}</p>
        </div>
        
        <div class="permission-section">
          <h4>权限菜单</h4>
          <el-tree
            :data="userMenus"
            :props="treeProps"
            node-key="id"
            :default-expand-all="true"
            :show-checkbox="false"
          />
        </div>
        
        <div class="permission-list-section">
          <h4>权限列表</h4>
          <el-tag 
            v-for="permission in userPermissions" 
            :key="permission" 
            size="small" 
            type="info"
            style="margin-right: 8px; margin-bottom: 4px;"
          >
            {{ permission }}
          </el-tag>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="permissionDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed, nextTick } from 'vue'
import { 
  FullScreen, ArrowDown, Setting, Close, Plus, ArrowLeft, ArrowRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { refreshMenusAfterPermissionChange } from '@/utils/userMenusCache'

/**
 * 用户角色管理页面
 * 
 * @author Amos
 * @since 2025-01-27
 */

// 响应式数据
const loading = ref(false)
const activeTab = ref('user-role')
const currentPage = ref(1)
const pageSize = ref(15)
const jumpPage = ref(1)

// 用户数据
const userList = ref([])
const totalCount = ref(0)

// 计算属性
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

// 辅助函数
const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 对话框状态
const editDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const currentUser = ref({})

// 角色数据
const allRoles = ref([])
const selectedRoleIds = ref([])

// 权限数据
const userMenus = ref([])
const userPermissions = ref([])

// 树形组件属性
const treeProps = {
  children: 'children',
  label: 'name'
}

// API调用
const getUserList = async () => {
  try {
    loading.value = true
    console.log('开始获取用户列表，参数:', { page: currentPage.value, limit: pageSize.value })

    const response = await request.post('/web/permission/user/list', {
      page: currentPage.value,
      limit: pageSize.value
    })

    console.log('用户列表响应:', response)

    if (response.code === 1) {
      userList.value = response.data.records || []
      totalCount.value = response.data.total || 0
      console.log('用户列表数据已设置:', { userList: userList.value, totalCount: totalCount.value })

      // 为每个用户获取角色信息
      for (let user of userList.value) {
        await getUserRoles(user)
      }

      // 强制刷新Vue组件
      await nextTick()
      console.log('Vue组件已刷新，当前userList:', userList.value)
    } else {
      console.warn('用户列表响应码不正确:', response.code, response.message)
      ElMessage.error(response.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const getUserRoles = async (user) => {
  try {
    const response = await request.post('/web/permission/user/roles', {
      userId: user.id
    })
    
    if (response.code === 1) {
      const roleIds = response.data || []
      // 根据角色ID获取角色详情
      const roles = allRoles.value.filter(role => roleIds.includes(role.id))
      user.roles = roles
    } else {
      user.roles = []
    }
  } catch (error) {
    console.error('获取用户角色失败:', error)
    user.roles = []
  }
}

const getAllRoles = async () => {
  try {
    const response = await request.get('/web/permission/role/all')
    if (response.code === 1) {
      allRoles.value = response.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 事件处理
const assignUserRole = () => {
  ElMessage.info('请选择用户进行角色分配')
}

const editUserRoles = async (user) => {
  currentUser.value = user
  try {
    // 获取用户当前角色
    const response = await request.post('/web/permission/user/roles', {
      userId: user.id
    })
    
    if (response.code === 1) {
      selectedRoleIds.value = response.data || []
      editDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取用户角色失败')
    }
  } catch (error) {
    console.error('获取用户角色失败:', error)
    ElMessage.error('获取用户角色失败')
  }
}

const saveUserRoles = async () => {
  try {
    const response = await request.post('/web/permission/user/roles/set', {
      userId: currentUser.value.id,
      roleIds: selectedRoleIds.value
    })
    
    if (response.code === 1) {
      ElMessage.success('用户角色设置成功')
      editDialogVisible.value = false
      getUserList() // 重新获取用户列表
      refreshMenusAfterPermissionChange().catch(() => {})
    } else {
      ElMessage.error(response.message || '用户角色设置失败')
    }
  } catch (error) {
    console.error('设置用户角色失败:', error)
    ElMessage.error('用户角色设置失败')
  }
}

const viewUserPermissions = async (user) => {
  currentUser.value = user
  try {
    // 获取用户权限菜单
    const menuResponse = await request.post('/web/permission/user/menus', {
      userId: user.id
    })
    
    // 获取用户权限列表
    const permissionResponse = await request.post('/web/permission/user/permissions', {
      userId: user.id
    })
    
    if (menuResponse.code === 1) {
      userMenus.value = menuResponse.data || []
    } else {
      userMenus.value = []
    }
    
    if (permissionResponse.code === 1) {
      userPermissions.value = permissionResponse.data || []
    } else {
      userPermissions.value = []
    }
    
    permissionDialogVisible.value = true
  } catch (error) {
    console.error('获取用户权限失败:', error)
    ElMessage.error('获取用户权限失败')
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getUserList()
}

const handlePrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    getUserList()
  }
}

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    getUserList()
  }
}

const handleJumpPage = () => {
  const page = parseInt(jumpPage.value)
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    getUserList()
  } else {
    ElMessage.warning('请输入有效的页码')
  }
}

onMounted(() => {
getAllRoles()
  getUserList()
})
</script>

<style scoped>
.user-role-management {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 0;
}

.page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  font-size: 16px;
  color: #666;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  font-size: 14px;
}

.tab-container {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.tabs {
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 8px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  position: relative;
}

.tab-item.active {
  color: #409eff;
  border-bottom-color: #409eff;
}

.tab-dot {
  width: 6px;
  height: 6px;
  background-color: #409eff;
  border-radius: 50%;
}

.close-icon {
  font-size: 12px;
  opacity: 0.6;
}

.more-icon {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.action-bar {
  background-color: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
}

.table-container {
  background-color: #fff;
  margin: 0 24px 16px 24px;
  border-radius: 4px;
  overflow: hidden;
}

.no-role {
  color: #999;
  font-size: 12px;
}

.pagination-container {
  background-color: #fff;
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-info {
  color: #666;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.pagination-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-number {
  padding: 0 12px;
  color: #333;
  font-weight: 500;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
}

.dialog-content {
  padding: 16px 0;
}

.user-info-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.user-info-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 14px;
}

.user-info-section p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.role-selection-section h4,
.permission-section h4,
.permission-list-section h4 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 14px;
}

.role-selection-section {
  margin-bottom: 24px;
}

.permission-section {
  margin-bottom: 24px;
}

.permission-list-section {
  margin-bottom: 24px;
}

/* 表格样式优化 */
:deep(.el-table) {
  border: none;
}

:deep(.el-table th) {
  background-color: #fafafa;
  color: #333;
  font-weight: 500;
  border-bottom: 1px solid #e8e8e8;
}

:deep(.el-table td) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-table tr:hover > td) {
  background-color: #f5f7fa;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafafa;
}

/* 按钮样式 */
:deep(.el-button--primary.is-link) {
  color: #409eff;
  padding: 0;
  font-size: 12px;
}

:deep(.el-button--success.is-link) {
  color: #67c23a;
  padding: 0;
  font-size: 12px;
}

/* 选择器样式 */
:deep(.el-select) {
  width: 100px;
}

:deep(.el-input__inner) {
  text-align: center;
}

/* 复选框样式 */
:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

:deep(.el-checkbox) {
  margin-right: 0;
}

/* 树形组件样式 */
:deep(.el-tree) {
  background-color: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
}
</style>
