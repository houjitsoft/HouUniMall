<template>
  <div class="third-party-settings">
    <div class="page-header">
      <h2>第三方集成设置</h2>
      <p>配置钉钉、企业微信、飞书等第三方平台的集成参数</p>
    </div>

    <el-card class="settings-card">
      <el-tabs v-model="activeTab" type="card" class="settings-tabs">
        <!-- 钉钉设置 -->
        <el-tab-pane label="钉钉设置" name="dingtalk">
          <div class="tab-content">
            <div class="platform-header">
              <el-icon class="platform-icon"><ChatDotRound /></el-icon>
              <div class="platform-info">
                <h3>钉钉集成配置</h3>
                <p>配置钉钉开放平台的AppKey、AppSecret等参数</p>
              </div>
              <el-switch
                v-model="dingtalkConfig.enabled"
                @change="handleDingtalkToggle"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>

            <el-form
              ref="dingtalkFormRef"
              :model="dingtalkConfig"
              :rules="dingtalkRules"
              label-width="120px"
              class="settings-form"
              :disabled="!dingtalkConfig.enabled"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="AppKey" prop="appKey">
                    <el-input
                      v-model="dingtalkConfig.appKey"
                      placeholder="请输入钉钉AppKey"
                      show-password
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="AppSecret" prop="appSecret">
                    <el-input
                      v-model="dingtalkConfig.appSecret"
                      placeholder="请输入钉钉AppSecret"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="AgentId" prop="agentId">
                    <el-input
                      v-model="dingtalkConfig.agentId"
                      placeholder="请输入钉钉AgentId"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回调URL" prop="callbackUrl">
                    <el-input
                      v-model="dingtalkConfig.callbackUrl"
                      placeholder="请输入钉钉回调URL"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="描述">
                <el-input
                  v-model="dingtalkConfig.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入钉钉集成描述"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveDingtalkConfig" :loading="dingtalkLoading">
                  保存配置
                </el-button>
                <el-button @click="testDingtalkConnection" :loading="dingtalkTestLoading">
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 企业微信设置 -->
        <el-tab-pane label="企业微信设置" name="wechat-work">
          <div class="tab-content">
            <div class="platform-header">
              <el-icon class="platform-icon"><ChatLineRound /></el-icon>
              <div class="platform-info">
                <h3>企业微信集成配置</h3>
                <p>配置企业微信开放平台的CorpID、Secret等参数</p>
              </div>
              <el-switch
                v-model="wechatWorkConfig.enabled"
                @change="handleWechatWorkToggle"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>

            <el-form
              ref="wechatWorkFormRef"
              :model="wechatWorkConfig"
              :rules="wechatWorkRules"
              label-width="120px"
              class="settings-form"
              :disabled="!wechatWorkConfig.enabled"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="CorpID" prop="corpId">
                    <el-input
                      v-model="wechatWorkConfig.corpId"
                      placeholder="请输入企业微信CorpID"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="AgentID" prop="agentId">
                    <el-input
                      v-model="wechatWorkConfig.agentId"
                      placeholder="自建应用 AgentID（如厚匠科技）"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="Secret" prop="secret">
                    <el-input
                      v-model="wechatWorkConfig.secret"
                      placeholder="自建应用 Secret（如厚匠科技）"
                      show-password
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="通讯录 Secret">
                    <el-input
                      v-model="wechatWorkConfig.addressBookSecret"
                      placeholder="与主 Secret 相同；勿填客户联系 Secret；不填则使用 Secret"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="客户联系 Secret" prop="contactSecret">
                    <el-input
                      v-model="wechatWorkConfig.contactSecret"
                      placeholder="选填：与上方 Secret 相同即可；须为「可调用接口的应用」中已授权应用的 Secret"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="打卡 Secret">
                    <el-input
                      v-model="wechatWorkConfig.checkinSecret"
                      placeholder="选填：打卡应用 Secret（拉取考勤必填）"
                      show-password
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回调URL" prop="callbackUrl">
                    <el-input
                      v-model="wechatWorkConfig.callbackUrl"
                      placeholder="请输入企业微信回调URL"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="描述">
                <el-input
                  v-model="wechatWorkConfig.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入企业微信集成描述"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveWechatWorkConfig" :loading="wechatWorkLoading">
                  保存配置
                </el-button>
                <el-button @click="testWechatWorkConnection" :loading="wechatWorkTestLoading">
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 飞书设置 -->
        <el-tab-pane label="飞书设置" name="feishu">
          <div class="tab-content">
            <div class="platform-header">
              <el-icon class="platform-icon"><Message /></el-icon>
              <div class="platform-info">
                <h3>飞书集成配置</h3>
                <p>配置飞书开放平台的AppID、AppSecret等参数</p>
              </div>
              <el-switch
                v-model="feishuConfig.enabled"
                @change="handleFeishuToggle"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>

            <el-form
              ref="feishuFormRef"
              :model="feishuConfig"
              :rules="feishuRules"
              label-width="120px"
              class="settings-form"
              :disabled="!feishuConfig.enabled"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="AppID" prop="appId">
                    <el-input
                      v-model="feishuConfig.appId"
                      placeholder="请输入飞书AppID"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="AppSecret" prop="appSecret">
                    <el-input
                      v-model="feishuConfig.appSecret"
                      placeholder="请输入飞书AppSecret"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="EncryptKey" prop="encryptKey">
                    <el-input
                      v-model="feishuConfig.encryptKey"
                      placeholder="请输入飞书EncryptKey"
                      show-password
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回调URL" prop="callbackUrl">
                    <el-input
                      v-model="feishuConfig.callbackUrl"
                      placeholder="请输入飞书回调URL"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="描述">
                <el-input
                  v-model="feishuConfig.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入飞书集成描述"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveFeishuConfig" :loading="feishuLoading">
                  保存配置
                </el-button>
                <el-button @click="testFeishuConnection" :loading="feishuTestLoading">
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 语雀设置 -->
        <el-tab-pane label="语雀设置" name="yuque">
          <div class="tab-content">
            <div class="platform-header">
              <el-icon class="platform-icon"><Document /></el-icon>
              <div class="platform-info">
                <h3>语雀集成配置</h3>
                <p>配置语雀开放平台的AppID、AppSecret等参数</p>
              </div>
              <el-switch
                v-model="yuqueConfig.enabled"
                @change="handleYuqueToggle"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>

            <el-form
              ref="yuqueFormRef"
              :model="yuqueConfig"
              :rules="yuqueRules"
              label-width="120px"
              class="settings-form"
              :disabled="!yuqueConfig.enabled"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="AppID" prop="appId">
                    <el-input
                      v-model="yuqueConfig.appId"
                      placeholder="请输入语雀AppID"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="AppSecret" prop="appSecret">
                    <el-input
                      v-model="yuqueConfig.appSecret"
                      placeholder="请输入语雀AppSecret"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="AccessToken" prop="accessToken">
                    <el-input
                      v-model="yuqueConfig.accessToken"
                      placeholder="请输入语雀AccessToken"
                      show-password
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="回调URL" prop="callbackUrl">
                    <el-input
                      v-model="yuqueConfig.callbackUrl"
                      placeholder="请输入语雀回调URL"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="描述">
                <el-input
                  v-model="yuqueConfig.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入语雀集成描述"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveYuqueConfig" :loading="yuqueLoading">
                  保存配置
                </el-button>
                <el-button @click="testYuqueConnection" :loading="yuqueTestLoading">
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 微信配置 -->
        <el-tab-pane label="微信配置" name="wechat-login">
          <div class="tab-content">
            <div class="platform-header">
              <el-icon class="platform-icon"><ChatLineRound /></el-icon>
              <div class="platform-info">
                <h3>微信配置</h3>
                <p>配置微信开放平台的AppID、AppSecret等参数，用于登录、支付等功能</p>
              </div>
              <el-switch
                v-model="wechatLoginConfig.enabled"
                @change="handleWechatLoginToggle"
                active-text="启用"
                inactive-text="禁用"
              />
            </div>

            <el-form
              ref="wechatLoginFormRef"
              :model="wechatLoginConfig"
              :rules="wechatLoginRules"
              label-width="120px"
              class="settings-form"
              :disabled="!wechatLoginConfig.enabled"
            >
              <el-alert
                title="配置说明"
                type="info"
                :closable="false"
                style="margin-bottom: 20px;"
              >
                <template #default>
                  <div style="line-height: 1.8;">
                    <p><strong>登录配置：</strong></p>
                    <p>1. 访问 <a href="https://open.weixin.qq.com" target="_blank" style="color: #409eff;">微信开放平台</a> 创建网站应用</p>
                    <p>2. 获取AppID和AppSecret</p>
                    <p>3. 配置授权回调域名（如：unimall.houjit.com，不要带 https:// 和路径）</p>
                    <p>4. 填写回调URL（如：https://unimall.houjit.com/wechat-callback，这是系统内置的回调页）</p>
                    <p style="color: #e6a23c;">AppSecret 属敏感凭据，不由前端或库表承载：实际生效值取服务端 Nacos 的 wechat.open.app-id / app-secret / redirect-uri，改完无需重启服务。此处填写用于备案与核对，配置开通请联系运维。</p>
                  </div>
                </template>
              </el-alert>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="AppID" prop="appId">
                    <el-input
                      v-model="wechatLoginConfig.appId"
                      placeholder="请输入微信开放平台AppID"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="AppSecret" prop="appSecret">
                    <el-input
                      v-model="wechatLoginConfig.appSecret"
                      placeholder="请输入微信开放平台AppSecret"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="回调URL" prop="callbackUrl">
                    <el-input
                      v-model="wechatLoginConfig.callbackUrl"
                      placeholder="请输入授权回调URL（如：https://unimall.houjit.com/wechat-callback）"
                    />
                    <div style="font-size: 12px; color: #909399; margin-top: 5px;">
                      此URL需要在微信开放平台中配置为授权回调域名
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>

                  <el-form-item label="描述">
                <el-input
                  v-model="wechatLoginConfig.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入微信配置描述"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveWechatLoginConfig" :loading="wechatLoginLoading">
                  保存配置
                </el-button>
                <el-button @click="testWechatLoginConnection" :loading="wechatLoginTestLoading">
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
import { ChatDotRound, ChatLineRound, Message, Document } from '@element-plus/icons-vue'
import { getThirdPartyConfigsBatch, saveThirdPartyConfig, testThirdPartyConnection } from '@/api/setting/setting'

export default {
  name: 'ThirdPartySettings',
  components: {
    ChatDotRound,
    ChatLineRound,
    Message,
    Document
  },
  setup() {
    const activeTab = ref('dingtalk')
    
    // 钉钉配置
    const dingtalkConfig = reactive({
      enabled: false,
      appKey: '',
      appSecret: '',
      agentId: '',
      callbackUrl: '',
      description: ''
    })

    // 企业微信配置
    const wechatWorkConfig = reactive({
      enabled: false,
      corpId: '',
      secret: '',
      addressBookSecret: '',
      contactSecret: '',
      checkinSecret: '',
      agentId: '',
      callbackUrl: '',
      description: ''
    })

    // 飞书配置
    const feishuConfig = reactive({
      enabled: false,
      appId: '',
      appSecret: '',
      encryptKey: '',
      callbackUrl: '',
      description: ''
    })

    // 语雀配置
    const yuqueConfig = reactive({
      enabled: false,
      appId: '',
      appSecret: '',
      accessToken: '',
      callbackUrl: '',
      description: ''
    })

    // 微信配置
    const wechatLoginConfig = reactive({
      enabled: false,
      appId: '',
      appSecret: '',
      callbackUrl: '',
      description: ''
    })

    // 加载状态
    const dingtalkLoading = ref(false)
    const wechatWorkLoading = ref(false)
    const feishuLoading = ref(false)
    const yuqueLoading = ref(false)
    const wechatLoginLoading = ref(false)
    const dingtalkTestLoading = ref(false)
    const wechatWorkTestLoading = ref(false)
    const feishuTestLoading = ref(false)
    const yuqueTestLoading = ref(false)
    const wechatLoginTestLoading = ref(false)

    // 表单引用
    const dingtalkFormRef = ref(null)
    const wechatWorkFormRef = ref(null)
    const feishuFormRef = ref(null)
    const yuqueFormRef = ref(null)
    const wechatLoginFormRef = ref(null)

    // 表单验证规则
    const dingtalkRules = {
      appKey: [{ required: true, message: '请输入钉钉AppKey', trigger: 'blur' }],
      appSecret: [{ required: true, message: '请输入钉钉AppSecret', trigger: 'blur' }],
      agentId: [{ required: true, message: '请输入钉钉AgentId', trigger: 'blur' }]
    }

    const wechatWorkRules = {
      corpId: [{ required: true, message: '请输入企业微信CorpID', trigger: 'blur' }],
      secret: [{ required: true, message: '请输入企业微信Secret', trigger: 'blur' }],
      agentId: [{ required: true, message: '请输入企业微信AgentID', trigger: 'blur' }]
    }

    const feishuRules = {
      appId: [{ required: true, message: '请输入飞书AppID', trigger: 'blur' }],
      appSecret: [{ required: true, message: '请输入飞书AppSecret', trigger: 'blur' }]
    }

    const yuqueRules = {
      appId: [{ required: true, message: '请输入语雀AppID', trigger: 'blur' }],
      appSecret: [{ required: true, message: '请输入语雀AppSecret', trigger: 'blur' }],
      accessToken: [{ required: true, message: '请输入语雀AccessToken', trigger: 'blur' }]
    }

    const wechatLoginRules = {
      appId: [{ required: true, message: '请输入微信开放平台AppID', trigger: 'blur' }],
      appSecret: [{ required: true, message: '请输入微信开放平台AppSecret', trigger: 'blur' }],
      callbackUrl: [{ required: true, message: '请输入授权回调URL', trigger: 'blur' }]
    }

    const applyConfigData = (platform, data) => {
      if (!data) return
      switch (platform) {
        case 'dingtalk':
          dingtalkConfig.enabled = data.enabled || false
          dingtalkConfig.description = data.description || ''
          if (data.dingtalk) Object.assign(dingtalkConfig, data.dingtalk)
          break
        case 'wechat-work':
          wechatWorkConfig.enabled = data.enabled || false
          wechatWorkConfig.description = data.description || ''
          if (data.wechatWork) Object.assign(wechatWorkConfig, data.wechatWork)
          break
        case 'feishu':
          feishuConfig.enabled = data.enabled || false
          feishuConfig.description = data.description || ''
          if (data.feishu) Object.assign(feishuConfig, data.feishu)
          break
        case 'yuque':
          yuqueConfig.enabled = data.enabled || false
          yuqueConfig.description = data.description || ''
          if (data.yuque) Object.assign(yuqueConfig, data.yuque)
          break
        case 'wechat-login':
          wechatLoginConfig.enabled = data.enabled || false
          wechatLoginConfig.description = data.description || ''
          if (data.wechatLogin) Object.assign(wechatLoginConfig, data.wechatLogin)
          break
      }
    }

    // 加载配置数据（一次批量请求，避免 5 次 Feign 往返）
    const loadConfigs = async () => {
      try {
        const res = await getThirdPartyConfigsBatch()
        if ((res.code === 200 || res.code === 1) && res.data) {
          const batch = res.data
          applyConfigData('dingtalk', batch['dingtalk'])
          applyConfigData('wechat-work', batch['wechat-work'])
          applyConfigData('feishu', batch['feishu'])
          applyConfigData('yuque', batch['yuque'])
          applyConfigData('wechat-login', batch['wechat-login'])
        }
      } catch (error) {
        console.error('加载第三方集成配置失败:', error)
      }
    }

    // 保存钉钉配置
    const saveDingtalkConfig = async () => {
      if (!dingtalkFormRef.value) return
      
      try {
        await dingtalkFormRef.value.validate()
        dingtalkLoading.value = true
        
        const configData = {
          platform: 'dingtalk',
          enabled: dingtalkConfig.enabled,
          description: dingtalkConfig.description,
          dingtalk: {
            appKey: dingtalkConfig.appKey,
            appSecret: dingtalkConfig.appSecret,
            agentId: dingtalkConfig.agentId,
            callbackUrl: dingtalkConfig.callbackUrl
          }
        }
        
        const res = await saveThirdPartyConfig('dingtalk', configData)
        if (res.code === 200 || res.code === 1) {
          ElMessage.success('钉钉配置保存成功')
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存钉钉配置失败:', error)
        ElMessage.error('保存失败')
      } finally {
        dingtalkLoading.value = false
      }
    }

    // 保存企业微信配置
    const saveWechatWorkConfig = async () => {
      if (!wechatWorkFormRef.value) return
      
      try {
        await wechatWorkFormRef.value.validate()
        wechatWorkLoading.value = true
        
        const configData = {
          platform: 'wechat-work',
          enabled: wechatWorkConfig.enabled,
          description: wechatWorkConfig.description,
          wechatWork: {
            corpId: wechatWorkConfig.corpId,
            secret: wechatWorkConfig.secret,
            addressBookSecret: wechatWorkConfig.addressBookSecret,
            contactSecret: wechatWorkConfig.contactSecret,
            checkinSecret: wechatWorkConfig.checkinSecret,
            agentId: wechatWorkConfig.agentId,
            callbackUrl: wechatWorkConfig.callbackUrl
          }
        }
        
        const res = await saveThirdPartyConfig('wechat-work', configData)
        if (res.code === 200 || res.code === 1) {
          ElMessage.success('企业微信配置保存成功')
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存企业微信配置失败:', error)
        ElMessage.error('保存失败')
      } finally {
        wechatWorkLoading.value = false
      }
    }

    // 保存飞书配置
    const saveFeishuConfig = async () => {
      if (!feishuFormRef.value) return
      
      try {
        await feishuFormRef.value.validate()
        feishuLoading.value = true
        
        const configData = {
          platform: 'feishu',
          enabled: feishuConfig.enabled,
          description: feishuConfig.description,
          feishu: {
            appId: feishuConfig.appId,
            appSecret: feishuConfig.appSecret,
            encryptKey: feishuConfig.encryptKey,
            callbackUrl: feishuConfig.callbackUrl
          }
        }
        
        const res = await saveThirdPartyConfig('feishu', configData)
        if (res.code === 200 || res.code === 1) {
          ElMessage.success('飞书配置保存成功')
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存飞书配置失败:', error)
        ElMessage.error('保存失败')
      } finally {
        feishuLoading.value = false
      }
    }

    // 测试钉钉连接
    const testDingtalkConnection = async () => {
      try {
        dingtalkTestLoading.value = true
        const configData = {
          platform: 'dingtalk',
          enabled: dingtalkConfig.enabled,
          description: dingtalkConfig.description,
          dingtalk: {
            appKey: dingtalkConfig.appKey,
            appSecret: dingtalkConfig.appSecret,
            agentId: dingtalkConfig.agentId,
            callbackUrl: dingtalkConfig.callbackUrl
          }
        }
        const res = await testThirdPartyConnection('dingtalk', configData)
        if (res.code === 200) {
          ElMessage.success('钉钉连接测试成功')
        } else {
          ElMessage.error(res.message || '连接测试失败')
        }
      } catch (error) {
        console.error('钉钉连接测试失败:', error)
        ElMessage.error('连接测试失败')
      } finally {
        dingtalkTestLoading.value = false
      }
    }

    // 测试企业微信连接
    const testWechatWorkConnection = async () => {
      try {
        wechatWorkTestLoading.value = true
        const configData = {
          platform: 'wechat-work',
          enabled: wechatWorkConfig.enabled,
          description: wechatWorkConfig.description,
          wechatWork: {
            corpId: wechatWorkConfig.corpId,
            secret: wechatWorkConfig.secret,
            addressBookSecret: wechatWorkConfig.addressBookSecret,
            contactSecret: wechatWorkConfig.contactSecret,
            checkinSecret: wechatWorkConfig.checkinSecret,
            agentId: wechatWorkConfig.agentId,
            callbackUrl: wechatWorkConfig.callbackUrl
          }
        }
        const res = await testThirdPartyConnection('wechat-work', configData)
        if (res.code === 200) {
          ElMessage.success('企业微信连接测试成功')
        } else {
          ElMessage.error(res.message || '连接测试失败')
        }
      } catch (error) {
        console.error('企业微信连接测试失败:', error)
        ElMessage.error('连接测试失败')
      } finally {
        wechatWorkTestLoading.value = false
      }
    }

    // 测试飞书连接
    const testFeishuConnection = async () => {
      try {
        feishuTestLoading.value = true
        const configData = {
          platform: 'feishu',
          enabled: feishuConfig.enabled,
          description: feishuConfig.description,
          feishu: {
            appId: feishuConfig.appId,
            appSecret: feishuConfig.appSecret,
            encryptKey: feishuConfig.encryptKey,
            callbackUrl: feishuConfig.callbackUrl
          }
        }
        const res = await testThirdPartyConnection('feishu', configData)
        if (res.code === 200) {
          ElMessage.success('飞书连接测试成功')
        } else {
          ElMessage.error(res.message || '连接测试失败')
        }
      } catch (error) {
        console.error('飞书连接测试失败:', error)
        ElMessage.error('连接测试失败')
      } finally {
        feishuTestLoading.value = false
      }
    }

    // 保存语雀配置
    const saveYuqueConfig = async () => {
      if (!yuqueFormRef.value) return
      
      try {
        await yuqueFormRef.value.validate()
        yuqueLoading.value = true
        
        const configData = {
          platform: 'yuque',
          enabled: yuqueConfig.enabled,
          description: yuqueConfig.description,
          yuque: {
            appId: yuqueConfig.appId,
            appSecret: yuqueConfig.appSecret,
            accessToken: yuqueConfig.accessToken,
            callbackUrl: yuqueConfig.callbackUrl
          }
        }
        
        const res = await saveThirdPartyConfig('yuque', configData)
        if (res.code === 200 || res.code === 1) {
          ElMessage.success('语雀配置保存成功')
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存语雀配置失败:', error)
        ElMessage.error('保存失败')
      } finally {
        yuqueLoading.value = false
      }
    }

    // 测试语雀连接
    const testYuqueConnection = async () => {
      try {
        yuqueTestLoading.value = true
        const configData = {
          platform: 'yuque',
          enabled: yuqueConfig.enabled,
          description: yuqueConfig.description,
          yuque: {
            appId: yuqueConfig.appId,
            appSecret: yuqueConfig.appSecret,
            accessToken: yuqueConfig.accessToken,
            callbackUrl: yuqueConfig.callbackUrl
          }
        }
        const res = await testThirdPartyConnection('yuque', configData)
        if (res.code === 200) {
          ElMessage.success('语雀连接测试成功')
        } else {
          ElMessage.error(res.message || '连接测试失败')
        }
      } catch (error) {
        console.error('语雀连接测试失败:', error)
        ElMessage.error('连接测试失败')
      } finally {
        yuqueTestLoading.value = false
      }
    }

    // 切换钉钉启用状态
    const handleDingtalkToggle = (value) => {
      if (value) {
        ElMessageBox.confirm('确定要启用钉钉集成吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          dingtalkConfig.enabled = true
        }).catch(() => {
          dingtalkConfig.enabled = false
        })
      }
    }

    // 切换企业微信启用状态
    const handleWechatWorkToggle = (value) => {
      if (value) {
        ElMessageBox.confirm('确定要启用企业微信集成吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          wechatWorkConfig.enabled = true
        }).catch(() => {
          wechatWorkConfig.enabled = false
        })
      }
    }

    // 切换飞书启用状态
    const handleFeishuToggle = (value) => {
      if (value) {
        ElMessageBox.confirm('确定要启用飞书集成吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          feishuConfig.enabled = true
        }).catch(() => {
          feishuConfig.enabled = false
        })
      }
    }

    // 切换语雀启用状态
    const handleYuqueToggle = (value) => {
      if (value) {
        ElMessageBox.confirm('确定要启用语雀集成吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          yuqueConfig.enabled = true
        }).catch(() => {
          yuqueConfig.enabled = false
        })
      }
    }

    // 保存微信配置
    const saveWechatLoginConfig = async () => {
      if (!wechatLoginFormRef.value) return
      
      try {
        await wechatLoginFormRef.value.validate()
        wechatLoginLoading.value = true
        
        const configData = {
          platform: 'wechat-login',
          enabled: wechatLoginConfig.enabled,
          description: wechatLoginConfig.description,
          wechatLogin: {
            appId: wechatLoginConfig.appId,
            appSecret: wechatLoginConfig.appSecret,
            callbackUrl: wechatLoginConfig.callbackUrl
          }
        }
        
        const res = await saveThirdPartyConfig('wechat-login', configData)
        if (res.code === 200 || res.code === 1) {
          ElMessage.success('微信配置保存成功')
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } catch (error) {
        console.error('保存微信配置失败:', error)
        ElMessage.error('保存失败')
      } finally {
        wechatLoginLoading.value = false
      }
    }

    // 测试微信配置连接
    const testWechatLoginConnection = async () => {
      try {
        wechatLoginTestLoading.value = true
        const configData = {
          platform: 'wechat-login',
          enabled: wechatLoginConfig.enabled,
          description: wechatLoginConfig.description,
          wechatLogin: {
            appId: wechatLoginConfig.appId,
            appSecret: wechatLoginConfig.appSecret,
            callbackUrl: wechatLoginConfig.callbackUrl
          }
        }
        const res = await testThirdPartyConnection('wechat-login', configData)
        if (res.code === 200 || res.code === 1) {
          ElMessage.success('微信配置连接测试成功')
        } else {
          ElMessage.error(res.message || '连接测试失败')
        }
      } catch (error) {
        console.error('微信配置连接测试失败:', error)
        ElMessage.error('连接测试失败')
      } finally {
        wechatLoginTestLoading.value = false
      }
    }

    // 切换微信配置启用状态
    const handleWechatLoginToggle = (value) => {
      if (value) {
        ElMessageBox.confirm('确定要启用微信配置吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          wechatLoginConfig.enabled = true
        }).catch(() => {
          wechatLoginConfig.enabled = false
        })
      }
    }

    onMounted(() => {
      loadConfigs()
    })

    return {
      activeTab,
      dingtalkConfig,
      wechatWorkConfig,
      feishuConfig,
      yuqueConfig,
      wechatLoginConfig,
      dingtalkLoading,
      wechatWorkLoading,
      feishuLoading,
      yuqueLoading,
      wechatLoginLoading,
      dingtalkTestLoading,
      wechatWorkTestLoading,
      feishuTestLoading,
      yuqueTestLoading,
      wechatLoginTestLoading,
      dingtalkFormRef,
      wechatWorkFormRef,
      feishuFormRef,
      yuqueFormRef,
      wechatLoginFormRef,
      dingtalkRules,
      wechatWorkRules,
      feishuRules,
      yuqueRules,
      wechatLoginRules,
      saveDingtalkConfig,
      saveWechatWorkConfig,
      saveFeishuConfig,
      saveYuqueConfig,
      saveWechatLoginConfig,
      testDingtalkConnection,
      testWechatWorkConnection,
      testFeishuConnection,
      testYuqueConnection,
      testWechatLoginConnection,
      handleDingtalkToggle,
      handleWechatWorkToggle,
      handleFeishuToggle,
      handleYuqueToggle,
      handleWechatLoginToggle
    }
  }
}
</script>

<style scoped>
.third-party-settings {
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

.settings-tabs {
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
  background: #f8f9fa;
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
