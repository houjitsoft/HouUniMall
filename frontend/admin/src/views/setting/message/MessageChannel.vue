<template>
  <div class="message-channel">
    <!-- 渠道列表 -->
    <el-table :data="channelList" v-loading="loading" border stripe>
      <el-table-column prop="channelCode" label="渠道编码" width="120" />
      <el-table-column prop="channelName" label="渠道名称" width="150" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="配置状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="getConfigStatus(row)" type="success" size="small">已配置</el-tag>
          <el-tag v-else type="warning" size="small">未配置</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleConfig(row)">配置</el-button>
          <el-button type="success" link @click="handleTest(row)" :disabled="!row.config">测试</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 配置弹窗 -->
    <el-dialog v-model="configVisible" :title="`${currentChannel?.channelName}配置`" width="600px" destroy-on-close>
      <el-form :model="configForm" label-width="120px">
        <template v-if="currentChannel?.channelCode === 'EMAIL'">
          <el-form-item label="SMTP服务器">
            <el-input v-model="configForm.host" placeholder="如: smtp.qq.com" />
          </el-form-item>
          <el-form-item label="SMTP端口">
            <el-input v-model="configForm.port" placeholder="如: 465" />
          </el-form-item>
          <el-form-item label="发件人邮箱">
            <el-input v-model="configForm.username" placeholder="发件人邮箱地址" />
          </el-form-item>
          <el-form-item label="发件人名称">
            <el-input v-model="configForm.fromName" placeholder="发件人显示名称" />
          </el-form-item>
          <el-form-item label="邮箱密码/授权码">
            <el-input v-model="configForm.password" type="password" show-password />
          </el-form-item>
          <el-form-item label="启用SSL">
            <el-switch v-model="configForm.ssl" />
          </el-form-item>
        </template>

        <template v-else-if="currentChannel?.channelCode === 'SMS'">
          <el-alert
            title="短信服务配置"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          >
            <template #default>
              短信渠道使用系统短信配置，请前往
              <el-link type="primary" @click="goToSmsConfig">短信配置</el-link>
              页面进行设置。
            </template>
          </el-alert>
          <el-form-item label="当前配置状态">
            <el-tag v-if="smsConfigured" type="success">已配置</el-tag>
            <el-tag v-else type="warning">未配置</el-tag>
          </el-form-item>
          <el-form-item v-if="smsConfigured" label="当前服务商">
            <span>{{ currentSmsProvider }}</span>
          </el-form-item>
        </template>

        <!-- 钉钉、企业微信、微信公众号、飞书统一引导到第三方集成设置 -->
        <template v-else-if="['DINGTALK', 'WECOM', 'WECHAT', 'FEISHU'].includes(currentChannel?.channelCode)">
          <el-alert
            :title="getThirdPartyTitle(currentChannel?.channelCode)"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          >
            <template #default>
              {{ currentChannel?.channelName }}渠道使用系统第三方集成配置，请前往
              <el-link type="primary" @click="goToThirdPartyConfig">第三方集成设置</el-link>
              页面进行配置。
            </template>
          </el-alert>
          <el-form-item label="当前配置状态">
            <el-tag v-if="thirdPartyConfigured[currentChannel?.channelCode]" type="success">已配置</el-tag>
            <el-tag v-else type="warning">未配置</el-tag>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="配置说明">
            <el-alert title="站内信无需额外配置" type="info" :closable="false" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 测试弹窗 -->
    <el-dialog v-model="testVisible" title="发送测试消息" width="500px">
      <el-form :model="testForm" label-width="100px">
        <el-form-item label="接收人">
          <el-input v-model="testForm.receiver" :placeholder="getReceiverPlaceholder()" />
        </el-form-item>
        <el-form-item label="消息标题">
          <el-input v-model="testForm.title" placeholder="测试消息标题" />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input v-model="testForm.content" type="textarea" :rows="3" placeholder="测试消息内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testVisible = false">取消</el-button>
        <el-button type="primary" @click="sendTestMessage" :loading="testing">发送测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { channelApi } from '@/api/setting/message'
import request from '@/utils/request'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const configVisible = ref(false)
const testVisible = ref(false)
const currentChannel = ref(null)
const configForm = reactive({})
const testForm = reactive({
  receiver: '',
  title: '测试消息',
  content: '这是一条测试消息'
})

const channelList = ref([])
const smsConfigured = ref(false)
const currentSmsProvider = ref('')
const thirdPartyConfigured = reactive({
  DINGTALK: false,
  WECOM: false,
  WECHAT: false,
  FEISHU: false
})

/**
 * 跳转到短信配置页面
 */
const goToSmsConfig = () => {
  configVisible.value = false
  router.push('/setting/sms')
}

/**
 * 跳转到第三方集成设置页面
 */
const goToThirdPartyConfig = () => {
  configVisible.value = false
  router.push('/setting/thridparty')
}

/**
 * 获取第三方配置标题
 */
const getThirdPartyTitle = (channelCode) => {
  const titles = {
    'DINGTALK': '钉钉服务配置',
    'WECOM': '企业微信服务配置',
    'WECHAT': '微信公众号服务配置',
    'FEISHU': '飞书服务配置'
  }
  return titles[channelCode] || '服务配置'
}

/**
 * 获取渠道配置状态
 */
const getConfigStatus = (row) => {
  // 如果是短信渠道，返回短信配置状态
  if (row.channelCode === 'SMS') {
    return smsConfigured.value
  }
  // 如果是第三方渠道，返回第三方配置状态
  if (['DINGTALK', 'WECOM', 'WECHAT', 'FEISHU'].includes(row.channelCode)) {
    return thirdPartyConfigured[row.channelCode] || false
  }
  // 其他渠道（如EMAIL）检查row.config
  return !!row.config
}

/**
 * 获取短信配置状态
 */
const fetchSmsConfig = async () => {
  try {
    const res = await request.get('/web/sms/list')
    if (res.code === 1 || res.code === 200) {
      const list = res.data?.list || res.data || []
      // 查找已启用的短信配置
      const enabledConfig = list.find(item => item.smsStatus === '01')
      smsConfigured.value = !!enabledConfig
      if (enabledConfig) {
        const providerMap = {
          'tencentCloud': '腾讯云',
          'aliyun': '阿里云',
          'huawei': '华为云',
          'unimall': 'unimall',
          'chuanlan': '创蓝'
        }
        currentSmsProvider.value = providerMap[enabledConfig.smsType] || enabledConfig.smsType
      }
    }
  } catch (error) {
    console.error('获取短信配置失败:', error)
  }
}

/**
 * 获取第三方集成配置状态
 */
const fetchThirdPartyConfig = async (channelCode) => {
  try {
    // 将渠道代码映射到平台名称
    const platformMap = {
      'DINGTALK': 'dingtalk',
      'WECOM': 'wechat-work',
      'WECHAT': 'wechat-work', // 微信公众号可能也用企业微信配置
      'FEISHU': 'feishu'
    }
    
    const platform = platformMap[channelCode]
    if (!platform) {
      thirdPartyConfigured[channelCode] = false
      return
    }
    
    // 调用API查询第三方配置
    const res = await request.get(`/web/setting/third-party/${platform}`)
    if (res.code === 200 || res.code === 1 || res.code === 0) {
      const config = res.data
      // 判断是否已配置：enabled为true且有配置数据
      if (config && config.enabled) {
        // 检查是否有具体的配置数据
        let hasConfig = false
        if (platform === 'dingtalk' && config.dingtalk) {
          hasConfig = !!(config.dingtalk.appKey && config.dingtalk.appSecret)
        } else if (platform === 'wechat-work' && config.wechatWork) {
          hasConfig = !!(config.wechatWork.corpId && config.wechatWork.secret)
        } else if (platform === 'feishu' && config.feishu) {
          hasConfig = !!(config.feishu.appId && config.feishu.appSecret)
        }
        thirdPartyConfigured[channelCode] = hasConfig
      } else {
        thirdPartyConfigured[channelCode] = false
      }
    } else {
      thirdPartyConfigured[channelCode] = false
    }
  } catch (error) {
    console.error('获取第三方配置失败:', error)
    thirdPartyConfigured[channelCode] = false
  }
}

/**
 * 获取渠道列表
 */
const fetchChannelList = async () => {
  loading.value = true
  try {
    const res = await channelApi.getList({ pageNum: 1, pageSize: 100 })
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      channelList.value = res.data?.records || res.data?.list || []
    } else {
      ElMessage.error(res.msg || '获取渠道列表失败')
    }
  } catch (error) {
    console.error('获取渠道列表失败:', error)
    ElMessage.error('获取渠道列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理状态变更
 */
const handleStatusChange = async (row) => {
  try {
    const res = await channelApi.updateStatus(row.id, row.status)
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      ElMessage.success(`${row.channelName}已${row.status === 1 ? '启用' : '禁用'}`)
    } else {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error(res.msg || '状态更新失败')
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

/**
 * 打开配置弹窗
 */
const handleConfig = async (row) => {
  currentChannel.value = row
  // 清空表单
  Object.keys(configForm).forEach(key => delete configForm[key])
  // 解析现有配置
  if (row.config) {
    try {
      const parsed = JSON.parse(row.config)
      Object.assign(configForm, parsed)
    } catch (e) {
      console.error('解析配置失败:', e)
    }
  }
  // 设置默认模式
  if (['DINGTALK', 'WECOM', 'FEISHU'].includes(row.channelCode) && !configForm.mode) {
    configForm.mode = 'webhook'
  }
  // 如果是短信渠道，获取短信配置状态
  if (row.channelCode === 'SMS') {
    fetchSmsConfig()
  }
  // 如果是第三方渠道，获取第三方配置状态（确保获取最新状态）
  if (['DINGTALK', 'WECOM', 'WECHAT', 'FEISHU'].includes(row.channelCode)) {
    await fetchThirdPartyConfig(row.channelCode)
  }
  configVisible.value = true
}

/**
 * 保存配置
 */
const saveConfig = async () => {
  saving.value = true
  try {
    const config = JSON.stringify(configForm)
    const data = {
      id: currentChannel.value.id,
      config: config
    }
    const res = await channelApi.update(data)
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      currentChannel.value.config = config
      ElMessage.success('配置保存成功')
      configVisible.value = false
      fetchChannelList()
    } else {
      ElMessage.error(res.msg || '配置保存失败')
    }
  } catch (error) {
    console.error('配置保存失败:', error)
    ElMessage.error('配置保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 打开测试弹窗
 */
const handleTest = (row) => {
  currentChannel.value = row
  testForm.receiver = ''
  testForm.title = '测试消息'
  testForm.content = '这是一条测试消息，用于验证渠道配置是否正确。'
  testVisible.value = true
}

/**
 * 获取接收人占位符
 */
const getReceiverPlaceholder = () => {
  const code = currentChannel.value?.channelCode
  switch (code) {
    case 'EMAIL': return '请输入邮箱地址'
    case 'SMS': return '请输入手机号'
    case 'DINGTALK': return 'Webhook模式无需填写'
    case 'WECOM': return 'Webhook模式无需填写'
    case 'FEISHU': return 'Webhook模式无需填写'
    default: return '请输入接收人ID'
  }
}

/**
 * 发送测试消息
 */
const sendTestMessage = async () => {
  testing.value = true
  try {
    // TODO: 调用测试发送接口
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('测试消息已发送，请检查接收情况')
    testVisible.value = false
  } catch (error) {
    ElMessage.error('测试发送失败')
  } finally {
    testing.value = false
  }
}

/**
 * 初始化第三方配置状态
 */
const initThirdPartyConfigs = async () => {
  // 在获取渠道列表后，检查所有第三方渠道的配置状态
  const thirdPartyChannels = ['DINGTALK', 'WECOM', 'WECHAT', 'FEISHU']
  for (const channelCode of thirdPartyChannels) {
    await fetchThirdPartyConfig(channelCode)
  }
}

onMounted(async () => {
  await fetchChannelList()
  // 初始化第三方配置状态
  await initThirdPartyConfigs()
})
</script>

<style scoped>
.message-channel {
  padding: 10px;
}
</style>
