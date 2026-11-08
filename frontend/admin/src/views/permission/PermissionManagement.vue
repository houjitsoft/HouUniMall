<template>
  <div class="permission-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" @click="refreshPermissions">
            <el-icon><Refresh /></el-icon>
            刷新权限
          </el-button>
        </div>
      </template>
      
      <!-- 权限信息展示 -->
      <div class="permission-info">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>当前组织</span>
              </template>
              <div class="info-content">
                <el-tag type="success">{{ currentOrgId }}</el-tag>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>当前服务</span>
              </template>
              <div class="info-content">
                <el-tag type="primary">{{ currentServiceName }}</el-tag>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>权限数量</span>
              </template>
              <div class="info-content">
                <el-tag type="info">{{ userPermissions.length }}</el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 权限菜单展示 -->
      <div class="permission-menus">
        <h3>权限菜单</h3>
        <el-tree
          :data="organizedMenus"
          :props="treeProps"
          node-key="resourceId"
          :expand-on-click-node="false"
          :default-expand-all="true"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <el-icon v-if="data.resourceIcon">
                <component :is="data.resourceIcon" />
              </el-icon>
              <span>{{ data.resourceName }}</span>
              <el-tag size="small" type="info">{{ data.resourceCode }}</el-tag>
            </div>
          </template>
        </el-tree>
      </div>
      
      <!-- 权限列表展示 -->
      <div class="permission-list">
        <h3>权限列表</h3>
        <el-table :data="permissionTableData" stripe>
          <el-table-column prop="resourceCode" label="资源编码" width="300" />
          <el-table-column prop="permissionType" label="权限类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getPermissionTypeTagType(row.permissionType)">
                {{ getPermissionTypeDescription(row.permissionType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="serviceName" label="服务名称" width="120" />
          <el-table-column prop="moduleName" label="模块名称" width="120" />
          <el-table-column prop="functionName" label="功能名称" width="120" />
          <el-table-column prop="operationType" label="操作类型" width="120" />
        </el-table>
      </div>
      
      <!-- 权限测试 -->
      <div class="permission-test">
        <h3>权限测试</h3>
        <el-form :model="testForm" label-width="120px">
          <el-form-item label="资源编码">
            <el-input v-model="testForm.resourceCode" placeholder="请输入资源编码" />
          </el-form-item>
          <el-form-item label="权限类型">
            <el-select v-model="testForm.permissionType" placeholder="请选择权限类型">
              <el-option label="查看" value="READ" />
              <el-option label="编辑" value="WRITE" />
              <el-option label="删除" value="DELETE" />
              <el-option label="执行" value="EXECUTE" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="testPermission">测试权限</el-button>
            <el-button @click="clearTest">清空</el-button>
          </el-form-item>
        </el-form>
        
        <div v-if="testResult !== null" class="test-result">
          <el-alert
            :title="testResult ? '有权限' : '无权限'"
            :type="testResult ? 'success' : 'warning'"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import { PermissionUtils } from '@/utils/permission'

/**
 * 权限管理页面
 * 
 * @author Amos
 * @since 2025-01-07
 */
export default {
  name: 'PermissionManagement',
  
  setup() {
    const permissionStore = usePermissionStore()
    
    // 测试表单
    const testForm = ref({
      resourceCode: '',
      permissionType: 'READ'
    })
    
    // 测试结果
    const testResult = ref(null)
    
    // 树形组件配置
    const treeProps = {
      children: 'children',
      label: 'resourceName'
    }
    
    // 计算属性
    const currentOrgId = computed(() => permissionStore.currentOrgId)
    const currentServiceName = computed(() => permissionStore.currentServiceName)
    const userPermissions = computed(() => permissionStore.userPermissions)
    const organizedMenus = computed(() => permissionStore.organizedMenus)
    
    // 权限表格数据
    const permissionTableData = computed(() => {
      return userPermissions.value.map(permission => {
        const parts = permission.split(':')
        return {
          resourceCode: permission,
          permissionType: parts[parts.length - 1] || 'READ',
          serviceName: parts[0] || '',
          moduleName: parts[1] || '',
          functionName: parts[2] || '',
          operationType: parts[3] || ''
        }
      })
    })
    
    // 获取权限类型标签类型
    const getPermissionTypeTagType = (permissionType) => {
      const typeMap = {
        'READ': 'success',
        'WRITE': 'primary',
        'DELETE': 'danger',
        'EXECUTE': 'warning'
      }
      return typeMap[permissionType] || 'info'
    }
    
    // 获取权限类型描述
    const getPermissionTypeDescription = (permissionType) => {
      return PermissionUtils.getPermissionTypeDescription(permissionType)
    }
    
    // 刷新权限
    const refreshPermissions = async () => {
      try {
        await permissionStore.initializeUserPermissions()
      } catch (error) {
        console.error('刷新权限失败:', error)
      }
    }
    
    // 测试权限
    const testPermission = async () => {
      if (!testForm.value.resourceCode) {
        ElMessage.warning('请输入资源编码')
        return
      }
      
      try {
        const result = await permissionStore.checkPermission(
          testForm.value.resourceCode,
          testForm.value.permissionType
        )
        testResult.value = result
      } catch (error) {
        console.error('权限测试失败:', error)
        testResult.value = false
      }
    }
    
    // 清空测试
    const clearTest = () => {
      testForm.value = {
        resourceCode: '',
        permissionType: 'READ'
      }
      testResult.value = null
    }
    
    // 初始化
    onMounted(async () => {
      try {
        await permissionStore.initializeUserPermissions()
      } catch (error) {
        console.error('初始化权限管理页面失败:', error)
      }
    })
    
    return {
      testForm,
      testResult,
      treeProps,
      currentOrgId,
      currentServiceName,
      userPermissions,
      organizedMenus,
      permissionTableData,
      getPermissionTypeTagType,
      getPermissionTypeDescription,
      refreshPermissions,
      testPermission,
      clearTest
    }
  }
}
</script>

<style scoped>
.permission-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.permission-info {
  margin-bottom: 20px;
}

.info-card {
  height: 120px;
}

.info-content {
  text-align: center;
  padding: 20px 0;
}

.permission-menus,
.permission-list,
.permission-test {
  margin-bottom: 30px;
}

.permission-menus h3,
.permission-list h3,
.permission-test h3 {
  margin-bottom: 15px;
  color: #303133;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-result {
  margin-top: 15px;
}
</style>
