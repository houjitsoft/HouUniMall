<template>
  <div class="permission-config">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>权限配置</span>
          <el-button type="primary" @click="addPermission">
            <el-icon><Plus /></el-icon>
            添加权限
          </el-button>
        </div>
      </template>
      
      <!-- 权限配置表单 -->
      <el-form
        ref="permissionFormRef"
        :model="permissionForm"
        :rules="permissionRules"
        label-width="120px"
        class="permission-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务名称" prop="serviceName">
              <el-select v-model="permissionForm.serviceName" placeholder="请选择服务名称">
                <el-option label="认证服务" value="auth" />
                <el-option label="用户服务" value="userms" />
                <el-option label="订单服务" value="order" />
                <el-option label="商品服务" value="goods" />
                <el-option label="支付服务" value="payment" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="模块名称" prop="moduleName">
              <el-input v-model="permissionForm.moduleName" placeholder="请输入模块名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="功能名称" prop="functionName">
              <el-input v-model="permissionForm.functionName" placeholder="请输入功能名称" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="操作类型" prop="operationType">
              <el-select v-model="permissionForm.operationType" placeholder="请选择操作类型">
                <el-option label="查看" value="read" />
                <el-option label="编辑" value="write" />
                <el-option label="删除" value="delete" />
                <el-option label="执行" value="execute" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资源类型" prop="resourceType">
              <el-select v-model="permissionForm.resourceType" placeholder="请选择资源类型">
                <el-option label="菜单" :value="1" />
                <el-option label="页面" :value="2" />
                <el-option label="按钮" :value="3" />
                <el-option label="API接口" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="资源名称" prop="resourceName">
              <el-input v-model="permissionForm.resourceName" placeholder="请输入资源名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资源URL" prop="resourceUrl">
              <el-input v-model="permissionForm.resourceUrl" placeholder="请输入资源URL" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="资源图标" prop="resourceIcon">
              <el-input v-model="permissionForm.resourceIcon" placeholder="请输入资源图标" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="父资源ID" prop="parentResourceId">
              <el-input v-model="permissionForm.parentResourceId" placeholder="请输入父资源ID" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="permissionForm.sortOrder" :min="1" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="资源描述" prop="resourceDescription">
          <el-input
            v-model="permissionForm.resourceDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入资源描述"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitPermission">提交</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="previewResourceCode">预览资源编码</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 资源编码预览 -->
      <div v-if="resourceCodePreview" class="resource-code-preview">
        <h4>资源编码预览</h4>
        <el-alert
          :title="resourceCodePreview"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
      
      <!-- 权限配置列表 -->
      <div class="permission-list">
        <h3>权限配置列表</h3>
        <el-table :data="permissionList" stripe>
          <el-table-column prop="resourceCode" label="资源编码" width="200" />
          <el-table-column prop="resourceName" label="资源名称" width="150" />
          <el-table-column prop="resourceType" label="资源类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getResourceTypeTagType(row.resourceType)">
                {{ getResourceTypeDescription(row.resourceType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="serviceName" label="服务名称" width="100" />
          <el-table-column prop="moduleName" label="模块名称" width="100" />
          <el-table-column prop="functionName" label="功能名称" width="100" />
          <el-table-column prop="operationType" label="操作类型" width="100" />
          <el-table-column prop="resourceUrl" label="资源URL" width="200" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" @click="editPermission(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deletePermission(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { PermissionUtils } from '@/utils/permission'

/**
 * 权限配置页面
 * 
 * @author Amos
 * @since 2025-01-07
 */
export default {
  name: 'PermissionConfig',
  
  setup() {
    const permissionFormRef = ref()
    
    // 权限表单
    const permissionForm = reactive({
      serviceName: '',
      moduleName: '',
      functionName: '',
      operationType: '',
      resourceType: 1,
      resourceName: '',
      resourceUrl: '',
      resourceIcon: '',
      parentResourceId: '',
      sortOrder: 1,
      resourceDescription: ''
    })
    
    // 表单验证规则
    const permissionRules = {
      serviceName: [
        { required: true, message: '请选择服务名称', trigger: 'change' }
      ],
      moduleName: [
        { required: true, message: '请输入模块名称', trigger: 'blur' }
      ],
      functionName: [
        { required: true, message: '请输入功能名称', trigger: 'blur' }
      ],
      operationType: [
        { required: true, message: '请选择操作类型', trigger: 'change' }
      ],
      resourceType: [
        { required: true, message: '请选择资源类型', trigger: 'change' }
      ],
      resourceName: [
        { required: true, message: '请输入资源名称', trigger: 'blur' }
      ]
    }
    
    // 资源编码预览
    const resourceCodePreview = ref('')
    
    // 权限配置列表
    const permissionList = ref([
      {
        resourceCode: 'auth:permission:list:read',
        resourceName: '权限列表',
        resourceType: 2,
        serviceName: 'auth',
        moduleName: 'permission',
        functionName: 'list',
        operationType: 'read',
        resourceUrl: '/permission/list',
        sortOrder: 1
      },
      {
        resourceCode: 'auth:permission:add:write',
        resourceName: '添加权限',
        resourceType: 3,
        serviceName: 'auth',
        moduleName: 'permission',
        functionName: 'add',
        operationType: 'write',
        resourceUrl: '/permission/add',
        sortOrder: 2
      }
    ])
    
    // 获取资源类型标签类型
    const getResourceTypeTagType = (resourceType) => {
      const typeMap = {
        1: 'success',
        2: 'primary',
        3: 'warning',
        4: 'danger'
      }
      return typeMap[resourceType] || 'info'
    }
    
    // 获取资源类型描述
    const getResourceTypeDescription = (resourceType) => {
      return PermissionUtils.getResourceTypeDescription(resourceType)
    }
    
    // 预览资源编码
    const previewResourceCode = () => {
      if (!permissionForm.serviceName || !permissionForm.moduleName || 
          !permissionForm.functionName || !permissionForm.operationType) {
        ElMessage.warning('请先填写必要的字段')
        return
      }
      
      resourceCodePreview.value = PermissionUtils.buildResourceCode(
        permissionForm.serviceName,
        permissionForm.moduleName,
        permissionForm.functionName,
        permissionForm.operationType
      )
    }
    
    // 提交权限配置
    const submitPermission = async () => {
      try {
        await permissionFormRef.value.validate()
        
        // 构建资源编码
        const resourceCode = PermissionUtils.buildResourceCode(
          permissionForm.serviceName,
          permissionForm.moduleName,
          permissionForm.functionName,
          permissionForm.operationType
        )
        
        // 这里应该调用API保存权限配置
        // await savePermissionConfig({ ...permissionForm, resourceCode })
        
        ElMessage.success('权限配置保存成功')
        resetForm()
        
      } catch (error) {
        console.error('提交权限配置失败:', error)
        ElMessage.error('提交权限配置失败')
      }
    }
    
    // 重置表单
    const resetForm = () => {
      permissionFormRef.value?.resetFields()
      resourceCodePreview.value = ''
    }
    
    // 添加权限
    const addPermission = () => {
      resetForm()
    }
    
    // 编辑权限
    const editPermission = (row) => {
      Object.assign(permissionForm, row)
      previewResourceCode()
    }
    
    // 删除权限
    const deletePermission = (row) => {
      ElMessageBox.confirm('确定要删除这个权限配置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 这里应该调用API删除权限配置
        // await deletePermissionConfig(row.id)
        ElMessage.success('删除成功')
      }).catch(() => {
        ElMessage.info('已取消删除')
      })
    }
    
    // 初始化
    onMounted(() => {
      // 初始化权限配置列表
      // await loadPermissionConfigList()
    })
    
    return {
      permissionFormRef,
      permissionForm,
      permissionRules,
      resourceCodePreview,
      permissionList,
      getResourceTypeTagType,
      getResourceTypeDescription,
      previewResourceCode,
      submitPermission,
      resetForm,
      addPermission,
      editPermission,
      deletePermission
    }
  }
}
</script>

<style scoped>
.permission-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.permission-form {
  margin-bottom: 30px;
}

.resource-code-preview {
  margin-bottom: 30px;
}

.resource-code-preview h4 {
  margin-bottom: 10px;
  color: #303133;
}

.permission-list h3 {
  margin-bottom: 15px;
  color: #303133;
}
</style>
