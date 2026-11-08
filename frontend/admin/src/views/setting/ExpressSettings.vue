<template>
  <div class="express-settings">
    <div class="page-header">
      <h2>快递设置</h2>
      <p>配置快递鸟、快递100等物流服务商的接口参数</p>
    </div>

    <el-card class="settings-card">
      <el-tabs v-model="activeTab" type="border-card" class="express-tabs">
        <!-- 快递鸟 -->
        <el-tab-pane label="快递鸟" name="kdniao">
          <div class="tab-content">
            <div class="platform-header">
              <el-icon class="platform-icon"><Van /></el-icon>
              <div class="platform-info">
                <h3>快递鸟配置</h3>
                <p>配置快递鸟物流查询接口参数</p>
              </div>
              <el-switch
                v-model="kdniaoConfig.enabled"
                @change="handleKdniaoToggle"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>

            <el-form
              ref="kdniaoFormRef"
              :model="kdniaoConfig"
              :rules="kdniaoRules"
              label-width="120px"
              class="settings-form"
              :disabled="!kdniaoConfig.enabled"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户ID" prop="ebussinessid">
                    <el-input
                      v-model="kdniaoConfig.ebussinessid"
                      placeholder="请输入快递鸟用户ID"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="API Key" prop="appkey">
                    <el-input
                      v-model="kdniaoConfig.appkey"
                      placeholder="请输入快递鸟API Key"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="接口指令" prop="instructCode">
                    <el-input-number
                      v-model="kdniaoConfig.instructCode"
                      :min="0"
                      placeholder="请输入接口指令"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item>
                <el-button type="primary" @click="saveKdniaoConfig" :loading="kdniaoLoading">
                  保存配置
                </el-button>
                <el-button @click="testKdniaoConnection" :loading="kdniaoTestLoading">
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 快递100 -->
        <el-tab-pane label="快递100" name="kd100">
          <div class="tab-content">
            <div class="platform-header">
              <el-icon class="platform-icon"><Van /></el-icon>
              <div class="platform-info">
                <h3>快递100配置</h3>
                <p>配置快递100物流查询接口参数</p>
              </div>
              <el-switch
                v-model="kd100Config.enabled"
                @change="handleKd100Toggle"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>

            <el-form
              ref="kd100FormRef"
              :model="kd100Config"
              :rules="kd100Rules"
              label-width="120px"
              class="settings-form"
              :disabled="!kd100Config.enabled"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Customer" prop="customer">
                    <el-input
                      v-model="kd100Config.customer"
                      placeholder="请输入快递100 Customer"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="Key" prop="key">
                    <el-input
                      v-model="kd100Config.key"
                      placeholder="请输入快递100 Key"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="接口地址" prop="apiUrl">
                    <el-input
                      v-model="kd100Config.apiUrl"
                      placeholder="https://poll.kuaidi100.com/poll/query.do"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item>
                <el-button type="primary" @click="saveKd100Config" :loading="kd100Loading">
                  保存配置
                </el-button>
                <el-button @click="testKd100Connection" :loading="kd100TestLoading">
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Van } from '@element-plus/icons-vue'
import request from '@/utils/request'

export default {
  name: 'ExpressSettings',
  components: {
    Van
  },
  setup() {
    const activeTab = ref('kdniao')
    
    // 快递鸟配置
    const kdniaoConfig = reactive({
      enabled: false,
      ebussinessid: '',
      appkey: '',
      instructCode: 8003
    })

    // 快递100配置
    const kd100Config = reactive({
      enabled: false,
      customer: '',
      key: '',
      apiUrl: 'https://poll.kuaidi100.com/poll/query.do'
    })

    // 加载状态
    const kdniaoLoading = ref(false)
    const kd100Loading = ref(false)
    const kdniaoTestLoading = ref(false)
    const kd100TestLoading = ref(false)

    // 表单引用
    const kdniaoFormRef = ref(null)
    const kd100FormRef = ref(null)

    // 表单验证规则
    const kdniaoRules = {
      ebussinessid: [{ required: true, message: '请输入快递鸟用户ID', trigger: 'blur' }],
      appkey: [{ required: true, message: '请输入快递鸟API Key', trigger: 'blur' }],
      instructCode: [{ required: true, message: '请输入接口指令', trigger: 'blur' }]
    }

    const kd100Rules = {
      customer: [{ required: true, message: '请输入快递100 Customer', trigger: 'blur' }],
      key: [{ required: true, message: '请输入快递100 Key', trigger: 'blur' }],
      apiUrl: [{ required: true, message: '请输入接口地址', trigger: 'blur' }]
    }

    // 快递鸟配置加载
    const loadKdniaoConfig = async () => {
      try {
        const res = await request.post('/web/base/kdniaoConfig/queryConfig', {})
        if (res.code === 200 && res.data) {
          kdniaoConfig.ebussinessid = res.data.ebussinessid || ''
          kdniaoConfig.appkey = res.data.appkey || ''
          kdniaoConfig.instructCode = res.data.instructCode || 8003
          kdniaoConfig.enabled = !!(res.data.ebussinessid && res.data.appkey)
        }
      } catch (error) {
        console.error('加载快递鸟配置失败:', error)
      }
    }

    // 保存快递鸟配置
    const saveKdniaoConfig = async () => {
      if (!kdniaoFormRef.value) return
      
      try {
        await kdniaoFormRef.value.validate()
        kdniaoLoading.value = true
        
        const res = await request.post('/web/base/kdniaoConfig/updateKdiniaoConfig', {
          ebussinessid: kdniaoConfig.ebussinessid,
          appkey: kdniaoConfig.appkey,
          instructCode: kdniaoConfig.instructCode
        })
        
        if (res.code === 200) {
          ElMessage.success('快递鸟配置保存成功')
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存快递鸟配置失败:', error)
        ElMessage.error('保存失败')
      } finally {
        kdniaoLoading.value = false
      }
    }

    // 测试快递鸟连接
    const testKdniaoConnection = async () => {
      try {
        kdniaoTestLoading.value = true
        ElMessage.info('功能开发中，请直接保存配置')
      } finally {
        kdniaoTestLoading.value = false
      }
    }

    // 快递100配置加载
    const loadKd100Config = async () => {
      try {
        const res = await request.post('/web/base/kd100Config/queryConfig', {})
        if (res.code === 200 && res.data) {
          kd100Config.customer = res.data.customer || ''
          kd100Config.key = res.data.key || ''
          kd100Config.apiUrl = res.data.apiUrl || 'https://poll.kuaidi100.com/poll/query.do'
          kd100Config.enabled = !!(res.data.customer && res.data.key)
        }
      } catch (error) {
        console.error('加载快递100配置失败:', error)
      }
    }

    // 保存快递100配置
    const saveKd100Config = async () => {
      if (!kd100FormRef.value) return
      
      try {
        await kd100FormRef.value.validate()
        kd100Loading.value = true
        
        const res = await request.post('/web/base/kd100Config/updateConfig', {
          customer: kd100Config.customer,
          key: kd100Config.key,
          apiUrl: kd100Config.apiUrl
        })
        
        if (res.code === 200) {
          ElMessage.success('快递100配置保存成功')
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存快递100配置失败:', error)
        ElMessage.error('保存失败')
      } finally {
        kd100Loading.value = false
      }
    }

    // 测试快递100连接
    const testKd100Connection = async () => {
      try {
        kd100TestLoading.value = true
        ElMessage.info('功能开发中，请直接保存配置')
      } finally {
        kd100TestLoading.value = false
      }
    }

    // 切换快递鸟启用状态
    const handleKdniaoToggle = (value) => {
      if (value) {
        ElMessageBox.confirm('确定要启用快递鸟集成吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          kdniaoConfig.enabled = true
        }).catch(() => {
          kdniaoConfig.enabled = false
        })
      }
    }

    // 切换快递100启用状态
    const handleKd100Toggle = (value) => {
      if (value) {
        ElMessageBox.confirm('确定要启用快递100集成吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          kd100Config.enabled = true
        }).catch(() => {
          kd100Config.enabled = false
        })
      }
    }

    onMounted(() => {
      loadKdniaoConfig()
      loadKd100Config()
    })

    return {
      activeTab,
      kdniaoConfig,
      kd100Config,
      kdniaoLoading,
      kd100Loading,
      kdniaoTestLoading,
      kd100TestLoading,
      kdniaoFormRef,
      kd100FormRef,
      kdniaoRules,
      kd100Rules,
      saveKdniaoConfig,
      saveKd100Config,
      testKdniaoConnection,
      testKd100Connection,
      handleKdniaoToggle,
      handleKd100Toggle
    }
  }
}
</script>

<style scoped>
.express-settings {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.settings-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.express-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px 0;
}

.platform-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.platform-icon {
  font-size: 32px;
  color: #409eff;
  margin-right: 16px;
}

.platform-info {
  flex: 1;
}

.platform-info h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.platform-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.settings-form {
  max-width: 800px;
}

.settings-form .el-form-item {
  margin-bottom: 20px;
}

.settings-form .el-button {
  margin-right: 12px;
}
</style>
