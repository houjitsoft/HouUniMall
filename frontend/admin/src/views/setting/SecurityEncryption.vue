<template>
  <div class="security-encryption">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>数据加密设置</span>
        </div>
      </template>
      
      <el-form :model="encryptionForm" label-width="150px">
        <el-form-item label="数据加密算法">
          <el-select v-model="encryptionForm.algorithm" placeholder="请选择加密算法">
            <el-option label="AES-256" value="AES-256"></el-option>
            <el-option label="AES-128" value="AES-128"></el-option>
            <el-option label="RSA-2048" value="RSA-2048"></el-option>
            <el-option label="RSA-4096" value="RSA-4096"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="密码加密">
          <el-switch v-model="encryptionForm.passwordEncryption"></el-switch>
          <span class="form-tip">启用密码加密存储</span>
        </el-form-item>
        
        <el-form-item label="敏感数据加密">
          <el-switch v-model="encryptionForm.sensitiveDataEncryption"></el-switch>
          <span class="form-tip">加密存储敏感数据</span>
        </el-form-item>
        
        <el-form-item label="数据库连接加密">
          <el-switch v-model="encryptionForm.dbConnectionEncryption"></el-switch>
          <span class="form-tip">加密数据库连接</span>
        </el-form-item>
        
        <el-form-item label="文件存储加密">
          <el-switch v-model="encryptionForm.fileStorageEncryption"></el-switch>
          <span class="form-tip">加密存储文件</span>
        </el-form-item>
        
        <el-form-item label="密钥轮换周期">
          <el-input-number v-model="encryptionForm.keyRotationDays" :min="30" :max="365" controls-position="right" />
          <span class="form-tip">（天）</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveEncryptionConfig">保存设置</el-button>
          <el-button @click="resetEncryptionConfig">重置</el-button>
          <el-button type="warning" @click="generateNewKey">生成新密钥</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 加密状态 -->
    <el-card class="box-card" style="margin-top: 20px;">
      <template #header>
        <span>加密状态</span>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-item">
            <div class="status-icon success">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="status-info">
              <h4>密码加密</h4>
              <p>已启用</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="status-item">
            <div class="status-icon success">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="status-info">
              <h4>敏感数据</h4>
              <p>已加密</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="status-item">
            <div class="status-icon success">
              <el-icon><Database /></el-icon>
            </div>
            <div class="status-info">
              <h4>数据库连接</h4>
              <p>已加密</p>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="status-item">
            <div class="status-icon success">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="status-info">
              <h4>文件存储</h4>
              <p>已加密</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Folder } from '@element-plus/icons-vue'

const encryptionForm = reactive({
  algorithm: 'AES-256',
  passwordEncryption: true,
  sensitiveDataEncryption: true,
  dbConnectionEncryption: true,
  fileStorageEncryption: true,
  keyRotationDays: 90
})

const saveEncryptionConfig = () => {
  ElMessage.success('加密设置保存成功')
}

const resetEncryptionConfig = () => {
  encryptionForm.algorithm = 'AES-256'
  encryptionForm.passwordEncryption = true
  encryptionForm.sensitiveDataEncryption = true
  encryptionForm.dbConnectionEncryption = true
  encryptionForm.fileStorageEncryption = true
  encryptionForm.keyRotationDays = 90
  ElMessage.info('设置已重置')
}

const generateNewKey = async () => {
  try {
    await ElMessageBox.confirm('确定要生成新的加密密钥吗？这将影响所有加密数据！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('新密钥生成成功')
  } catch {
    ElMessage.info('已取消生成')
  }
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.security-encryption {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.status-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  text-align: center;
}

.status-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.status-icon.success {
  background-color: #67c23a;
}

.status-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.status-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>
