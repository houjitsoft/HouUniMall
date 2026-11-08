<template>
  <div class="message-template-container">
    <!-- 左侧业务类型菜单 -->
    <div class="left-menu">
      <div class="menu-header">
        <span>业务分类</span>
        <el-button type="primary" link size="small" @click="handleCreate">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      <el-menu
        :default-active="currentBizType"
        @select="handleMenuSelect"
        class="biz-menu"
      >
        <el-menu-item index="ALL">
          <el-icon><Grid /></el-icon>
          <span>全部模板</span>
          <el-badge :value="templateStats.total" :max="99" class="menu-badge" />
        </el-menu-item>
        <el-menu-item 
          v-for="item in bizTypeList" 
          :key="item.value" 
          :index="item.value"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
          <el-badge :value="templateStats[item.value] || 0" :max="99" class="menu-badge" />
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧模板列表 -->
    <div class="right-content">
      <!-- 搜索区域 -->
      <el-row :gutter="16" class="search-bar">
        <el-col :span="5">
          <el-input v-model="searchForm.keyword" placeholder="模板名称/编码" clearable @keyup.enter="handleSearch">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.channel" placeholder="消息类型" clearable>
            <el-option 
              v-for="ch in channelOptions" 
              :key="ch.value" 
              :label="ch.label" 
              :value="ch.value"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="11">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleCreate">
            <el-icon><Plus /></el-icon>新建模板
          </el-button>
        </el-col>
      </el-row>

      <!-- 当前分类标题和批量操作 -->
      <div class="category-title">
        <div class="title-left">
          <el-icon><component :is="getCurrentBizIcon()" /></el-icon>
          <span>{{ getCurrentBizName() }}</span>
          <span class="template-count">（{{ pagination.total }} 个模板）</span>
        </div>
        <div class="batch-actions" v-if="selectedIds.length > 0">
          <span class="selected-count">已选择 {{ selectedIds.length }} 项</span>
          <el-button type="success" size="small" @click="handleBatchEnable">批量启用</el-button>
          <el-button type="warning" size="small" @click="handleBatchDisable">批量禁用</el-button>
        </div>
      </div>

      <!-- 模板列表 -->
      <el-table 
        ref="tableRef"
        :data="templateList" 
        v-loading="loading" 
        border 
        stripe 
        style="width: 100%"
        @row-dblclick="handleEdit"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="templateCode" label="模板编码" width="200" show-overflow-tooltip />
        <el-table-column prop="templateName" label="模板名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="channels" label="支持渠道" width="220">
          <template #default="{ row }">
            <el-tag 
              v-for="ch in parseChannels(row.channels)" 
              :key="ch" 
              size="small" 
              :type="getChannelTagType(ch)"
              style="margin-right: 4px; margin-bottom: 2px"
            >
              {{ getChannelName(ch) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link size="small" @click="handlePreview(row)">预览</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        class="pagination"
        @size-change="fetchTemplateList"
        @current-change="fetchTemplateList"
      />
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑模板' : '新建模板'" 
      width="750px"
      destroy-on-close
    >
      <el-form :model="templateForm" :rules="rules" ref="formRef" label-width="130px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板编码" prop="templateCode">
              <el-input 
                v-model="templateForm.templateCode" 
                :disabled="isEdit" 
                placeholder="如 PROJECT_DUE_REMIND"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="templateForm.templateName" placeholder="模板显示名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="业务类型" prop="bizType">
          <el-select v-model="templateForm.bizType" placeholder="选择业务类型" style="width: 100%">
            <el-option 
              v-for="item in bizTypeList" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span style="margin-left: 8px">{{ item.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="标题模板" prop="titleTemplate">
          <el-input 
            v-model="templateForm.titleTemplate" 
            placeholder="如：项目【${projectName}】即将到期"
          >
            <template #append>
              <el-tooltip content="使用 ${变量名} 作为占位符" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="内容模板" prop="contentTemplate">
          <el-input 
            v-model="templateForm.contentTemplate" 
            type="textarea" 
            :rows="4"
            placeholder="使用 ${变量名} 作为占位符，如：您负责的项目【${projectName}】将于${dueDate}到期"
          />
        </el-form-item>

        <el-form-item label="支持渠道">
          <el-checkbox-group v-model="templateForm.channelList">
            <el-checkbox v-for="ch in channelOptions" :key="ch.value" :label="ch.value">
              {{ ch.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 各渠道模板CODE配置 -->
        <el-divider content-position="left">各渠道模板CODE配置</el-divider>
        <div v-if="templateForm.channelList.includes('INBOX')" style="margin-bottom: 16px">
          <el-form-item label="站内信模板CODE" :prop="templateForm.channelList.includes('INBOX') ? 'inboxTemplateCode' : ''">
            <el-input 
              v-model="templateForm.inboxTemplateCode" 
              placeholder="请输入站内信模板CODE"
              clearable
            />
          </el-form-item>
        </div>
        <div v-if="templateForm.channelList.includes('EMAIL')" style="margin-bottom: 16px">
          <el-form-item label="邮件模板CODE" :prop="templateForm.channelList.includes('EMAIL') ? 'emailTemplateCode' : ''">
            <el-input 
              v-model="templateForm.emailTemplateCode" 
              placeholder="请输入邮件模板CODE"
              clearable
            />
          </el-form-item>
        </div>
        <div v-if="templateForm.channelList.includes('SMS')" style="margin-bottom: 16px">
          <el-form-item label="短信模板CODE" :prop="templateForm.channelList.includes('SMS') ? 'smsTemplateCode' : ''">
            <el-input 
              v-model="templateForm.smsTemplateCode" 
              placeholder="请输入短信模板CODE（如：SMS_VERIFICATION_CODE）"
              clearable
            />
          </el-form-item>
        </div>
        <div v-if="templateForm.channelList.includes('DINGTALK')" style="margin-bottom: 16px">
          <el-form-item label="钉钉模板CODE" :prop="templateForm.channelList.includes('DINGTALK') ? 'dingtalkTemplateCode' : ''">
            <el-input 
              v-model="templateForm.dingtalkTemplateCode" 
              placeholder="请输入钉钉模板CODE"
              clearable
            />
          </el-form-item>
        </div>
        <div v-if="templateForm.channelList.includes('WECOM')" style="margin-bottom: 16px">
          <el-form-item label="企业微信模板CODE" :prop="templateForm.channelList.includes('WECOM') ? 'wecomTemplateCode' : ''">
            <el-input 
              v-model="templateForm.wecomTemplateCode" 
              placeholder="请输入企业微信模板CODE"
              clearable
            />
          </el-form-item>
        </div>
        <div v-if="templateForm.channelList.includes('WECHAT')" style="margin-bottom: 16px">
          <el-form-item label="微信公众号模板CODE" :prop="templateForm.channelList.includes('WECHAT') ? 'wechatTemplateCode' : ''">
            <el-input 
              v-model="templateForm.wechatTemplateCode" 
              placeholder="请输入微信公众号模板CODE"
              clearable
            />
          </el-form-item>
        </div>
        <div v-if="templateForm.channelList.includes('FEISHU')" style="margin-bottom: 16px">
          <el-form-item label="飞书模板CODE" :prop="templateForm.channelList.includes('FEISHU') ? 'feishuTemplateCode' : ''">
            <el-input 
              v-model="templateForm.feishuTemplateCode" 
              placeholder="请输入飞书模板CODE"
              clearable
            />
          </el-form-item>
        </div>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch 
                v-model="templateForm.status" 
                :active-value="1" 
                :inactive-value="0"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input 
            v-model="templateForm.remark" 
            type="textarea" 
            :rows="2" 
            placeholder="描述模板用途和变量说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" title="模板预览" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="模板编码">{{ previewData.templateCode }}</el-descriptions-item>
        <el-descriptions-item label="模板名称">{{ previewData.templateName }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">
          <el-tag>{{ getBizTypeName(previewData.bizType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支持渠道">
          <el-tag 
            v-for="ch in parseChannels(previewData.channels)" 
            :key="ch" 
            size="small"
            :type="getChannelTagType(ch)"
            style="margin-right: 4px"
          >
            {{ getChannelName(ch) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标题模板">
          <code>{{ previewData.titleTemplate }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="内容模板">
          <div class="preview-content">{{ previewData.contentTemplate }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ previewData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Grid, Folder, Document, List, 
  Setting, ShoppingCart, Money, Box, Bell, User, Calendar, InfoFilled, Lock
} from '@element-plus/icons-vue'
import { templateApi } from '@/api/setting/message'

// 状态
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentBizType = ref('ALL')
const selectedIds = ref([])
const tableRef = ref(null)

// 业务类型定义
const bizTypeList = [
  { value: 'PROJECT', label: '项目管理', icon: 'Folder' },
  { value: 'TASK', label: '任务管理', icon: 'Document' },
  { value: 'WORKFLOW', label: '工作流审批', icon: 'List' },
  { value: 'ORDER', label: '订单交易', icon: 'ShoppingCart' },
  { value: 'FINANCE', label: '财务相关', icon: 'Money' },
  { value: 'INVENTORY', label: '库存物料', icon: 'Box' },
  { value: 'SYSTEM', label: '系统通知', icon: 'Setting' },
  { value: 'USER', label: '用户交互', icon: 'User' },
  { value: 'AUTH', label: '用户认证', icon: 'Lock' },
  { value: 'SCHEDULE', label: '日程提醒', icon: 'Calendar' },
  { value: 'MEETING', label: '会议管理', icon: 'Bell' }
]

// 渠道选项
const channelOptions = [
  { value: 'INBOX', label: '站内信' },
  { value: 'EMAIL', label: '邮件' },
  { value: 'SMS', label: '短信' },
  { value: 'DINGTALK', label: '钉钉' },
  { value: 'WECOM', label: '企微' },
  { value: 'WECHAT', label: '微信' },
  { value: 'FEISHU', label: '飞书' }
]

// 模板统计
const templateStats = reactive({
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  channel: null,
  status: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 表单数据
const templateForm = reactive({
  id: null,
  templateCode: '',
  templateName: '',
  bizType: '',
  titleTemplate: '',
  contentTemplate: '',
  channelList: ['INBOX'],
  inboxTemplateCode: '',
  emailTemplateCode: '',
  smsTemplateCode: '',
  dingtalkTemplateCode: '',
  wecomTemplateCode: '',
  wechatTemplateCode: '',
  feishuTemplateCode: '',
  status: 1,
  remark: ''
})

// 预览数据
const previewData = ref({})

// 模板列表
const templateList = ref([])

// 表单校验规则
const rules = {
  templateCode: [
    { required: true, message: '请输入模板编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ],
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  bizType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  titleTemplate: [{ required: true, message: '请输入标题模板', trigger: 'blur' }],
  contentTemplate: [{ required: true, message: '请输入内容模板', trigger: 'blur' }],
  inboxTemplateCode: [
    { 
      validator: (rule, value, callback) => {
        if (templateForm.channelList.includes('INBOX') && !value) {
          callback(new Error('请填写站内信模板CODE'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  emailTemplateCode: [
    { 
      validator: (rule, value, callback) => {
        if (templateForm.channelList.includes('EMAIL') && !value) {
          callback(new Error('请填写邮件模板CODE'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  smsTemplateCode: [
    { 
      validator: (rule, value, callback) => {
        if (templateForm.channelList.includes('SMS') && !value) {
          callback(new Error('请填写短信模板CODE'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  dingtalkTemplateCode: [
    { 
      validator: (rule, value, callback) => {
        if (templateForm.channelList.includes('DINGTALK') && !value) {
          callback(new Error('请填写钉钉模板CODE'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  wecomTemplateCode: [
    { 
      validator: (rule, value, callback) => {
        if (templateForm.channelList.includes('WECOM') && !value) {
          callback(new Error('请填写企业微信模板CODE'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  wechatTemplateCode: [
    { 
      validator: (rule, value, callback) => {
        if (templateForm.channelList.includes('WECHAT') && !value) {
          callback(new Error('请填写微信公众号模板CODE'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  feishuTemplateCode: [
    { 
      validator: (rule, value, callback) => {
        if (templateForm.channelList.includes('FEISHU') && !value) {
          callback(new Error('请填写飞书模板CODE'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 渠道名称映射
const channelMap = {
  INBOX: '站内信',
  EMAIL: '邮件',
  SMS: '短信',
  DINGTALK: '钉钉',
  WECOM: '企微',
  WECHAT: '微信',
  FEISHU: '飞书'
}

// 渠道标签类型
const channelTagTypes = {
  INBOX: '',
  EMAIL: 'success',
  SMS: 'warning',
  DINGTALK: 'primary',
  WECOM: 'success',
  WECHAT: 'success',
  FEISHU: 'info'
}

// 业务类型名称映射
const bizTypeMap = computed(() => {
  const map = { ALL: '全部模板' }
  bizTypeList.forEach(item => {
    map[item.value] = item.label
  })
  return map
})

// 获取业务类型名称
const getBizTypeName = (type) => bizTypeMap.value[type] || type

// 获取渠道名称
const getChannelName = (code) => channelMap[code] || code

// 获取渠道标签类型
const getChannelTagType = (code) => channelTagTypes[code] || 'info'

// 解析渠道字符串
const parseChannels = (channels) => {
  if (!channels) return []
  return channels.split(',').filter(Boolean)
}

// 获取当前业务类型名称
const getCurrentBizName = () => bizTypeMap.value[currentBizType.value] || '全部模板'

// 获取当前业务类型图标
const getCurrentBizIcon = () => {
  if (currentBizType.value === 'ALL') return 'Grid'
  const item = bizTypeList.find(i => i.value === currentBizType.value)
  return item?.icon || 'Document'
}

// 获取模板列表
const fetchTemplateList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    
    // 业务类型筛选
    if (currentBizType.value && currentBizType.value !== 'ALL') {
      params.bizType = currentBizType.value
    }
    
    // 关键词搜索
    if (searchForm.keyword) {
      params.templateName = searchForm.keyword
      params.templateCode = searchForm.keyword
    }
    
    // 消息类型筛选
    if (searchForm.channel) {
      params.channel = searchForm.channel
    }
    
    // 状态筛选
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }

    const res = await templateApi.getList(params)
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      const data = res.data || {}
      templateList.value = data.records || data.list || []
      pagination.total = data.total || 0
    } else {
      ElMessage.error(res.msg || '获取模板列表失败')
    }
  } catch (error) {
    console.error('获取模板列表失败:', error)
    ElMessage.error('获取模板列表失败')
  } finally {
    loading.value = false
  }
}

// 获取模板统计
const fetchTemplateStats = async () => {
  try {
    // 获取全部模板统计各业务类型数量
    const res = await templateApi.getList({ pageNum: 1, pageSize: 1000 })
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      const list = res.data?.records || res.data?.list || []
      templateStats.total = list.length
      
      // 统计各业务类型数量
      bizTypeList.forEach(item => {
        templateStats[item.value] = list.filter(t => t.bizType === item.value).length
      })
    }
  } catch (error) {
    console.error('获取模板统计失败:', error)
  }
}

// 菜单选择
const handleMenuSelect = (index) => {
  currentBizType.value = index
  pagination.page = 1
  fetchTemplateList()
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchTemplateList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.channel = null
  searchForm.status = null
  handleSearch()
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(row => row.id)
}

// 全选
const handleSelectAll = () => {
  if (tableRef.value) {
    tableRef.value.toggleAllSelection()
  }
}

// 反选
const handleInvertSelection = () => {
  if (tableRef.value) {
    const allIds = templateList.value.map(row => row.id)
    const currentSelected = new Set(selectedIds.value)
    const newSelected = allIds.filter(id => !currentSelected.has(id))
    
    // 清空当前选择
    tableRef.value.clearSelection()
    
    // 选择之前未选中的项
    templateList.value.forEach(row => {
      if (newSelected.includes(row.id)) {
        tableRef.value.toggleRowSelection(row, true)
      }
    })
  }
}

// 批量启用
const handleBatchEnable = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要操作的模板')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要批量启用 ${selectedIds.value.length} 个模板吗？`,
      '批量启用确认',
      { type: 'warning' }
    )
    
    loading.value = true
    const res = await templateApi.batchUpdateStatus(selectedIds.value, 1)
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      ElMessage.success('批量启用成功')
      selectedIds.value = []
      if (tableRef.value) {
        tableRef.value.clearSelection()
      }
      fetchTemplateList()
      fetchTemplateStats()
    } else {
      ElMessage.error(res.msg || '批量启用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量启用失败:', error)
      ElMessage.error('批量启用失败')
    }
  } finally {
    loading.value = false
  }
}

// 批量禁用
const handleBatchDisable = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要操作的模板')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要批量禁用 ${selectedIds.value.length} 个模板吗？`,
      '批量禁用确认',
      { type: 'warning' }
    )
    
    loading.value = true
    const res = await templateApi.batchUpdateStatus(selectedIds.value, 0)
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      ElMessage.success('批量禁用成功')
      selectedIds.value = []
      if (tableRef.value) {
        tableRef.value.clearSelection()
      }
      fetchTemplateList()
      fetchTemplateStats()
    } else {
      ElMessage.error(res.msg || '批量禁用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量禁用失败:', error)
      ElMessage.error('批量禁用失败')
    }
  } finally {
    loading.value = false
  }
}

// 新建模板
const handleCreate = () => {
  isEdit.value = false
  Object.assign(templateForm, {
    id: null,
    templateCode: '',
    templateName: '',
    bizType: currentBizType.value !== 'ALL' ? currentBizType.value : '',
    titleTemplate: '',
    contentTemplate: '',
    channelList: ['INBOX'],
    inboxTemplateCode: '',
    emailTemplateCode: '',
    smsTemplateCode: '',
    dingtalkTemplateCode: '',
    wecomTemplateCode: '',
    wechatTemplateCode: '',
    feishuTemplateCode: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

// 编辑模板
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(templateForm, {
    ...row,
    channelList: parseChannels(row.channels || ''),
    inboxTemplateCode: row.inboxTemplateCode || '',
    emailTemplateCode: row.emailTemplateCode || '',
    smsTemplateCode: row.smsTemplateCode || '',
    dingtalkTemplateCode: row.dingtalkTemplateCode || '',
    wecomTemplateCode: row.wecomTemplateCode || '',
    wechatTemplateCode: row.wechatTemplateCode || '',
    feishuTemplateCode: row.feishuTemplateCode || ''
  })
  dialogVisible.value = true
}

// 预览模板
const handlePreview = (row) => {
  previewData.value = row
  previewVisible.value = true
}

// 删除模板
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除模板【${row.templateName}】吗？`,
      '删除确认',
      { type: 'warning' }
    )
    
    const res = await templateApi.delete(row.id)
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      ElMessage.success('删除成功')
      fetchTemplateList()
      fetchTemplateStats()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    const res = await templateApi.updateStatus(row.id, row.status)
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
    } else {
      // 恢复状态
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error(res.msg || '状态更新失败')
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  }
}

// 提交表单
const submitForm = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 处理渠道，确保所有字段都正确传递
    const data = {
      id: templateForm.id,
      templateCode: templateForm.templateCode,
      templateName: templateForm.templateName,
      bizType: templateForm.bizType,
      titleTemplate: templateForm.titleTemplate,
      contentTemplate: templateForm.contentTemplate,
      channels: templateForm.channelList.join(','),
      inboxTemplateCode: templateForm.inboxTemplateCode || null,
      emailTemplateCode: templateForm.emailTemplateCode || null,
      smsTemplateCode: templateForm.smsTemplateCode || null,
      dingtalkTemplateCode: templateForm.dingtalkTemplateCode || null,
      wecomTemplateCode: templateForm.wecomTemplateCode || null,
      wechatTemplateCode: templateForm.wechatTemplateCode || null,
      feishuTemplateCode: templateForm.feishuTemplateCode || null,
      status: templateForm.status,
      remark: templateForm.remark || null,
      orgId: templateForm.orgId || null
    }
    
    let res
    if (isEdit.value) {
      res = await templateApi.update(data)
    } else {
      res = await templateApi.create(data)
    }
    
    if (res.code === 1 || res.code === 200 || res.code === 0) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchTemplateList()
      fetchTemplateStats()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败')
    }
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  fetchTemplateList()
  fetchTemplateStats()
})
</script>

<style scoped>
.message-template-container {
  display: flex;
  height: calc(100vh - 120px);
  min-height: 600px;
}

.left-menu {
  width: 200px;
  border-right: 1px solid #e4e7ed;
  background: #f5f7fa;
  flex-shrink: 0;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.biz-menu {
  border-right: none;
  background: transparent;
}

.biz-menu .el-menu-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-right: 40px !important;
  position: relative;
}

.menu-badge {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
}

.menu-badge :deep(.el-badge__content) {
  font-size: 10px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
}

.right-content {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.search-bar {
  margin-bottom: 16px;
}

.category-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}

.category-title .title-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  font-size: 14px;
  color: #409eff;
  font-weight: normal;
}

.template-count {
  font-size: 14px;
  font-weight: normal;
  color: #909399;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.preview-content {
  white-space: pre-wrap;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-family: monospace;
}
</style>
