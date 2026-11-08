<template>
  <div class="permission-management">
    <el-card class="management-card">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon>
          <span>权限管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="refreshPermissions">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button @click="exportPermissions">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 权限信息展示 -->
      <div class="permission-info">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="info-card">
              <template #header>
                <span>总权限数</span>
              </template>
              <div class="info-content">
                <el-tag type="success" size="large">{{ totalPermissions }}</el-tag>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="info-card">
              <template #header>
                <span>角色数量</span>
              </template>
              <div class="info-content">
                <el-tag type="primary" size="large">{{ roleCount }}</el-tag>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="info-card">
              <template #header>
                <span>用户数量</span>
              </template>
              <div class="info-content">
                <el-tag type="info" size="large">{{ userCount }}</el-tag>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="info-card">
              <template #header>
                <span>组织数量</span>
              </template>
              <div class="info-content">
                <el-tag type="warning" size="large">{{ orgCount }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 搜索和过滤 -->
      <div class="search-filters">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索权限名称或编码"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedService" placeholder="选择服务" clearable @change="handleServiceFilter">
              <el-option label="全部服务" value="" />
              <el-option label="主服务(shell)" value="shell" />
              <el-option label="认证服务(auth)" value="auth" />
              <el-option label="商城服务(mall)" value="mall" />
              <el-option label="OA服务(oa)" value="oa" />
              <el-option label="MES服务(mes)" value="mes" />
              <el-option label="CMS服务(cms)" value="cms" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="selectedType" placeholder="选择类型" clearable @change="handleTypeFilter">
              <el-option label="全部类型" value="" />
              <el-option label="菜单" :value="1" />
              <el-option label="子菜单" :value="2" />
              <el-option label="按钮" :value="3" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 权限表格 -->
      <div class="permission-table">
        <el-table :data="filteredPermissions" v-loading="loading" stripe>
          <el-table-column prop="resourceCode" label="资源编码" width="200" />
          <el-table-column prop="resourceName" label="资源名称" width="150" />
          <el-table-column prop="permissionType" label="权限类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getPermissionTypeTagType(row.permissionType)">
                {{ getPermissionTypeText(row.permissionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="serviceName" label="服务名称" width="120">
            <template #default="{ row }">
              <el-tag :type="getServiceTagType(row.serviceName)">
                {{ getServiceDisplayName(row.serviceName) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="moduleName" label="模块名称" width="120" />
          <el-table-column prop="functionName" label="功能名称" width="120" />
          <el-table-column prop="operationType" label="操作类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getOperationTagType(row.operationType)">
                {{ row.operationType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="editPermission(row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="deletePermission(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed } from 'vue'
import { Lock, Refresh, Download, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

/**
 * 权限管理页面
 * 
 * @author Amos
 * @since 2025-01-07
 */

// 统计数据
const totalPermissions = ref(282)
const roleCount = ref(3)
const userCount = ref(0)
const orgCount = ref(0)

// 权限数据 - 基于我们创建的完整权限数据
const permissionsData = ref([
  // 主服务权限
  {
    resourceCode: 'shell:dashboard',
    resourceName: '仪表盘',
    permissionType: 1,
    serviceName: 'shell',
    moduleName: 'dashboard',
    functionName: 'overview',
    operationType: 'READ',
    description: '系统仪表盘主页面'
  },
  {
    resourceCode: 'shell:dashboard:overview',
    resourceName: '数据概览',
    permissionType: 2,
    serviceName: 'shell',
    moduleName: 'dashboard',
    functionName: 'overview',
    operationType: 'READ',
    description: '查看数据概览信息'
  },
  {
    resourceCode: 'shell:dashboard:refresh',
    resourceName: '刷新数据',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'dashboard',
    functionName: 'refresh',
    operationType: 'EXECUTE',
    description: '刷新仪表盘数据'
  },
  {
    resourceCode: 'shell:user',
    resourceName: '用户管理',
    permissionType: 1,
    serviceName: 'shell',
    moduleName: 'user',
    functionName: 'management',
    operationType: 'READ',
    description: '用户管理主页面'
  },
  {
    resourceCode: 'shell:user:add',
    resourceName: '添加用户',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'user',
    functionName: 'add',
    operationType: 'WRITE',
    description: '添加新用户'
  },
  {
    resourceCode: 'shell:user:edit',
    resourceName: '编辑用户',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'user',
    functionName: 'edit',
    operationType: 'WRITE',
    description: '编辑用户信息'
  },
  {
    resourceCode: 'shell:user:delete',
    resourceName: '删除用户',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'user',
    functionName: 'delete',
    operationType: 'DELETE',
    description: '删除用户'
  },
  {
    resourceCode: 'shell:permission',
    resourceName: '权限管理',
    permissionType: 1,
    serviceName: 'shell',
    moduleName: 'permission',
    functionName: 'management',
    operationType: 'READ',
    description: '权限管理主页面'
  },
  {
    resourceCode: 'shell:permission:role',
    resourceName: '角色管理',
    permissionType: 2,
    serviceName: 'shell',
    moduleName: 'permission',
    functionName: 'role',
    operationType: 'READ',
    description: '角色管理页面'
  },
  {
    resourceCode: 'shell:permission:role:add',
    resourceName: '添加角色',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'permission',
    functionName: 'role:add',
    operationType: 'WRITE',
    description: '添加新角色'
  },
  {
    resourceCode: 'shell:organization',
    resourceName: '组织管理',
    permissionType: 1,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'management',
    operationType: 'READ',
    description: '组织管理主页面'
  },
  {
    resourceCode: 'shell:organization:add',
    resourceName: '添加组织',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'add',
    operationType: 'WRITE',
    description: '添加新组织'
  },
  {
    resourceCode: 'shell:organization:view',
    resourceName: '查看组织',
    permissionType: 2,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'view',
    operationType: 'READ',
    description: '查看组织详情和组织树'
  },
  {
    resourceCode: 'shell:organization:edit',
    resourceName: '编辑组织',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'edit',
    operationType: 'WRITE',
    description: '编辑组织信息'
  },
  {
    resourceCode: 'shell:organization:delete',
    resourceName: '删除组织',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'delete',
    operationType: 'DELETE',
    description: '删除单个组织'
  },
  {
    resourceCode: 'shell:organization:batch-delete',
    resourceName: '批量删除组织',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'batch-delete',
    operationType: 'DELETE',
    description: '批量删除组织'
  },
  {
    resourceCode: 'shell:organization:export',
    resourceName: '导出组织',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'export',
    operationType: 'READ',
    description: '导出当前组织范围数据'
  },
  {
    resourceCode: 'shell:organization:enable',
    resourceName: '启用组织',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'enable',
    operationType: 'WRITE',
    description: '启用组织'
  },
  {
    resourceCode: 'shell:organization:disable',
    resourceName: '禁用组织',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'organization',
    functionName: 'disable',
    operationType: 'WRITE',
    description: '禁用组织'
  },
  {
    resourceCode: 'shell:setting',
    resourceName: '系统设置',
    permissionType: 1,
    serviceName: 'shell',
    moduleName: 'setting',
    functionName: 'management',
    operationType: 'READ',
    description: '系统设置主页面'
  },
  {
    resourceCode: 'shell:setting:basic',
    resourceName: '基本设置',
    permissionType: 2,
    serviceName: 'shell',
    moduleName: 'setting',
    functionName: 'basic',
    operationType: 'READ',
    description: '基本设置页面'
  },
  {
    resourceCode: 'shell:setting:save',
    resourceName: '保存设置',
    permissionType: 3,
    serviceName: 'shell',
    moduleName: 'setting',
    functionName: 'save',
    operationType: 'WRITE',
    description: '保存系统设置'
  },
  
  // 认证服务权限
  {
    resourceCode: 'auth:authentication',
    resourceName: '用户认证',
    permissionType: 1,
    serviceName: 'auth',
    moduleName: 'authentication',
    functionName: 'login',
    operationType: 'READ',
    description: '用户认证主页面'
  },
  {
    resourceCode: 'auth:login',
    resourceName: '用户登录',
    permissionType: 2,
    serviceName: 'auth',
    moduleName: 'authentication',
    functionName: 'login',
    operationType: 'EXECUTE',
    description: '用户登录功能'
  },
  {
    resourceCode: 'auth:register',
    resourceName: '用户注册',
    permissionType: 2,
    serviceName: 'auth',
    moduleName: 'authentication',
    functionName: 'register',
    operationType: 'WRITE',
    description: '用户注册功能'
  },
  {
    resourceCode: 'auth:login:phone',
    resourceName: '手机登录',
    permissionType: 3,
    serviceName: 'auth',
    moduleName: 'authentication',
    functionName: 'login:phone',
    operationType: 'EXECUTE',
    description: '手机号登录'
  },
  {
    resourceCode: 'auth:login:email',
    resourceName: '邮箱登录',
    permissionType: 3,
    serviceName: 'auth',
    moduleName: 'authentication',
    functionName: 'login:email',
    operationType: 'EXECUTE',
    description: '邮箱登录'
  },
  {
    resourceCode: 'auth:userinfo',
    resourceName: '用户信息',
    permissionType: 1,
    serviceName: 'auth',
    moduleName: 'userinfo',
    functionName: 'profile',
    operationType: 'READ',
    description: '用户信息管理'
  },
  {
    resourceCode: 'auth:userinfo:profile',
    resourceName: '个人信息',
    permissionType: 2,
    serviceName: 'auth',
    moduleName: 'userinfo',
    functionName: 'profile',
    operationType: 'READ',
    description: '个人信息页面'
  },
  {
    resourceCode: 'auth:userinfo:avatar',
    resourceName: '更新头像',
    permissionType: 3,
    serviceName: 'auth',
    moduleName: 'userinfo',
    functionName: 'avatar',
    operationType: 'WRITE',
    description: '更新用户头像'
  },
  {
    resourceCode: 'auth:company',
    resourceName: '公司登记',
    permissionType: 1,
    serviceName: 'auth',
    moduleName: 'company',
    functionName: 'registration',
    operationType: 'READ',
    description: '公司登记管理'
  },
  {
    resourceCode: 'auth:company:register',
    resourceName: '公司注册',
    permissionType: 2,
    serviceName: 'auth',
    moduleName: 'company',
    functionName: 'register',
    operationType: 'WRITE',
    description: '公司注册功能'
  },
  {
    resourceCode: 'auth:company:submit',
    resourceName: '提交登记',
    permissionType: 3,
    serviceName: 'auth',
    moduleName: 'company',
    functionName: 'submit',
    operationType: 'WRITE',
    description: '提交公司登记申请'
  },
  
  // 业务服务权限
  {
    resourceCode: 'mall:management',
    resourceName: '商城管理',
    permissionType: 1,
    serviceName: 'mall',
    moduleName: 'management',
    functionName: 'overview',
    operationType: 'READ',
    description: '商城管理主页面'
  },
  {
    resourceCode: 'mall:shop',
    resourceName: '店铺管理',
    permissionType: 2,
    serviceName: 'mall',
    moduleName: 'shop',
    functionName: 'management',
    operationType: 'READ',
    description: '店铺管理页面'
  },
  {
    resourceCode: 'mall:shop:add',
    resourceName: '添加店铺',
    permissionType: 3,
    serviceName: 'mall',
    moduleName: 'shop',
    functionName: 'add',
    operationType: 'WRITE',
    description: '添加新店铺'
  },
  {
    resourceCode: 'mall:goods',
    resourceName: '商品管理',
    permissionType: 2,
    serviceName: 'mall',
    moduleName: 'goods',
    functionName: 'management',
    operationType: 'READ',
    description: '商品管理页面'
  },
  {
    resourceCode: 'mall:goods:add',
    resourceName: '添加商品',
    permissionType: 3,
    serviceName: 'mall',
    moduleName: 'goods',
    functionName: 'add',
    operationType: 'WRITE',
    description: '添加新商品'
  },
  {
    resourceCode: 'mall:order',
    resourceName: '订单管理',
    permissionType: 2,
    serviceName: 'mall',
    moduleName: 'order',
    functionName: 'management',
    operationType: 'READ',
    description: '订单管理页面'
  },
  {
    resourceCode: 'mall:order:process',
    resourceName: '处理订单',
    permissionType: 3,
    serviceName: 'mall',
    moduleName: 'order',
    functionName: 'process',
    operationType: 'EXECUTE',
    description: '处理订单'
  },
  
  // 系统服务权限
  {
    resourceCode: 'oa:management',
    resourceName: 'OA办公',
    permissionType: 1,
    serviceName: 'oa',
    moduleName: 'management',
    functionName: 'overview',
    operationType: 'READ',
    description: 'OA办公主页面'
  },
  {
    resourceCode: 'oa:workbench',
    resourceName: '工作台',
    permissionType: 2,
    serviceName: 'oa',
    moduleName: 'workbench',
    functionName: 'dashboard',
    operationType: 'READ',
    description: '工作台页面'
  },
  {
    resourceCode: 'oa:workflow',
    resourceName: '流程管理',
    permissionType: 2,
    serviceName: 'oa',
    moduleName: 'workflow',
    functionName: 'management',
    operationType: 'READ',
    description: '流程管理页面'
  },
  {
    resourceCode: 'oa:workflow:create',
    resourceName: '创建流程',
    permissionType: 3,
    serviceName: 'oa',
    moduleName: 'workflow',
    functionName: 'create',
    operationType: 'WRITE',
    description: '创建新流程'
  },
  {
    resourceCode: 'mes:management',
    resourceName: 'MES制造执行',
    permissionType: 1,
    serviceName: 'mes',
    moduleName: 'management',
    functionName: 'overview',
    operationType: 'READ',
    description: 'MES制造执行主页面'
  },
  {
    resourceCode: 'mes:production',
    resourceName: '生产计划',
    permissionType: 2,
    serviceName: 'mes',
    moduleName: 'production',
    functionName: 'planning',
    operationType: 'READ',
    description: '生产计划管理'
  },
  {
    resourceCode: 'mes:production:create',
    resourceName: '创建计划',
    permissionType: 3,
    serviceName: 'mes',
    moduleName: 'production',
    functionName: 'create',
    operationType: 'WRITE',
    description: '创建生产计划'
  },
  {
    resourceCode: 'cms:management',
    resourceName: 'CMS内容管理',
    permissionType: 1,
    serviceName: 'cms',
    moduleName: 'management',
    functionName: 'overview',
    operationType: 'READ',
    description: 'CMS内容管理主页面'
  },
  {
    resourceCode: 'cms:article',
    resourceName: '文章管理',
    permissionType: 2,
    serviceName: 'cms',
    moduleName: 'article',
    functionName: 'management',
    operationType: 'READ',
    description: '文章管理页面'
  },
  {
    resourceCode: 'cms:article:add',
    resourceName: '添加文章',
    permissionType: 3,
    serviceName: 'cms',
    moduleName: 'article',
    functionName: 'add',
    operationType: 'WRITE',
    description: '添加新文章'
  }
])

const loading = ref(false)

// 搜索和过滤
const searchKeyword = ref('')
const selectedService = ref('')
const selectedType = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 计算属性
const filteredPermissions = computed(() => {
  let filtered = permissionsData.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item => 
      item.resourceName.toLowerCase().includes(keyword) ||
      item.resourceCode.toLowerCase().includes(keyword) ||
      item.description.toLowerCase().includes(keyword)
    )
  }

  // 服务过滤
  if (selectedService.value) {
    filtered = filtered.filter(item => item.serviceName === selectedService.value)
  }

  // 类型过滤
  if (selectedType.value !== '') {
    filtered = filtered.filter(item => item.permissionType === selectedType.value)
  }

  return filtered
})

const totalCount = computed(() => filteredPermissions.value.length)

// 获取权限类型标签类型
const getPermissionTypeTagType = (permissionType) => {
  const typeMap = {
    1: 'success',    // 菜单
    2: 'primary',    // 子菜单
    3: 'warning'     // 按钮
  }
  return typeMap[permissionType] || 'info'
}

// 获取权限类型文本
const getPermissionTypeText = (permissionType) => {
  const typeMap = {
    1: '菜单',
    2: '子菜单',
    3: '按钮'
  }
  return typeMap[permissionType] || permissionType
}

// 获取服务标签类型
const getServiceTagType = (serviceName) => {
  const typeMap = {
    'shell': 'success',
    'auth': 'primary',
    'mall': 'warning',
    'oa': 'info',
    'mes': 'danger',
    'cms': 'success'
  }
  return typeMap[serviceName] || 'info'
}

// 获取服务显示名称
const getServiceDisplayName = (serviceName) => {
  const nameMap = {
    'shell': '主服务',
    'auth': '认证服务',
    'mall': '商城服务',
    'oa': 'OA服务',
    'mes': 'MES服务',
    'cms': 'CMS服务'
  }
  return nameMap[serviceName] || serviceName
}

// 获取操作类型标签类型
const getOperationTagType = (operationType) => {
  const typeMap = {
    'READ': 'success',
    'WRITE': 'primary',
    'DELETE': 'danger',
    'EXECUTE': 'warning'
  }
  return typeMap[operationType] || 'info'
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
}

// 服务过滤处理
const handleServiceFilter = () => {
  currentPage.value = 1
}

// 类型过滤处理
const handleTypeFilter = () => {
  currentPage.value = 1
}

// 重置过滤器
const resetFilters = () => {
  searchKeyword.value = ''
  selectedService.value = ''
  selectedType.value = ''
  currentPage.value = 1
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 刷新权限
const refreshPermissions = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('权限刷新成功')
  }, 1000)
}

// 导出权限
const exportPermissions = () => {
  ElMessage.success('权限导出成功')
}

// 编辑权限
const editPermission = (row) => {
  ElMessage.info(`编辑权限: ${row.resourceName}`)
}

// 删除权限
const deletePermission = (row) => {
  ElMessage.warning(`删除权限: ${row.resourceName}`)
}

onMounted(() => {
  refreshPermissions()
})
</script>

<style scoped>
.permission-management {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.management-card {
  max-width: 1400px;
  margin: 0 auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.permission-info {
  margin-bottom: 20px;
}

.info-card {
  text-align: center;
  border-radius: 8px;
  transition: all 0.3s;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.15);
}

.info-content {
  padding: 15px 0;
}

.info-content .el-tag {
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 20px;
}

.search-filters {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.permission-table {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f8f9fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table tr:hover) {
  background-color: #f5f7fa;
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
}

/* 输入框样式优化 */
:deep(.el-input__inner) {
  border-radius: 4px;
}

:deep(.el-select .el-input__inner) {
  border-radius: 4px;
}

/* 卡片样式优化 */
:deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
