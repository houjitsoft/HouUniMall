<template>
  <div class="page-container">
    <!-- 物流公司配置 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">物流公司配置</span>
          <el-button type="primary" @click="handleAddCompany">
            <el-icon><Plus /></el-icon>添加物流公司
          </el-button>
        </div>
      </template>
      <el-table :data="companyList" border stripe v-loading="loading">
        <el-table-column prop="name" label="物流公司" width="150" />
        <el-table-column prop="code" label="公司代码" width="120" />
        <el-table-column label="LOGO" width="100" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.logo"
              :src="row.logo"
              fit="contain"
              style="width: 60px; height: 40px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="apiUrl" label="API地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="appKey" label="App Key" width="150" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleTest(row)">
              <el-icon><Connection /></el-icon>测试
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 运费模板 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">运费模板</span>
          <el-button type="primary" @click="handleAddTemplate">
            <el-icon><Plus /></el-icon>添加模板
          </el-button>
        </div>
      </template>
      <el-table :data="templateList" border stripe v-loading="loading">
        <el-table-column prop="name" label="模板名称" width="150" />
        <el-table-column label="计费方式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.chargeType === 'weight' ? 'success' : 'warning'">
              {{ row.chargeType === 'weight' ? '按重量' : '按件数' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="basePrice" label="首重/首件" width="120" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.basePrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="baseUnit" label="首重单位" width="100" align="center" />
        <el-table-column prop="addPrice" label="续重/续件" width="120" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.addPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="addUnit" label="续重单位" width="100" align="center" />
        <el-table-column label="包邮条件" width="150">
          <template #default="{ row }">
            <span v-if="row.freeShipping">
              满{{ row.freeAmount }}元包邮
            </span>
            <span v-else>不包邮</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditTemplate(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDeleteTemplate(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 物流规则 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">物流规则配置</span>
      </template>
      <el-form :model="ruleForm" label-width="140px" style="max-width: 800px">
        <el-form-item label="自动发货">
          <el-switch v-model="ruleForm.autoShip" />
          <span class="form-tip">开启后，订单支付成功将自动发货</span>
        </el-form-item>
        <el-form-item label="发货超时提醒">
          <el-input-number
            v-model="ruleForm.shipTimeout"
            :min="1"
            :max="72"
            style="width: 150px"
          />
          <span class="form-tip">小时（超时未发货将提醒商家）</span>
        </el-form-item>
        <el-form-item label="自动确认收货">
          <el-switch v-model="ruleForm.autoConfirm" />
          <span class="form-tip">开启后，发货后指定天数自动确认收货</span>
        </el-form-item>
        <el-form-item label="自动收货天数" v-if="ruleForm.autoConfirm">
          <el-input-number
            v-model="ruleForm.confirmDays"
            :min="1"
            :max="30"
            style="width: 150px"
          />
          <span class="form-tip">天（发货后多少天自动确认收货）</span>
        </el-form-item>
        <el-form-item label="物流跟踪更新">
          <el-radio-group v-model="ruleForm.trackUpdate">
            <el-radio value="realtime">实时更新</el-radio>
            <el-radio value="hourly">每小时更新</el-radio>
            <el-radio value="daily">每天更新</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="异常订单处理">
          <el-checkbox-group v-model="ruleForm.exceptionHandle">
            <el-checkbox value="notify">短信通知客户</el-checkbox>
            <el-checkbox value="alert">系统预警</el-checkbox>
            <el-checkbox value="auto">自动转人工处理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="电子面单">
          <el-switch v-model="ruleForm.electronicSheet" />
          <span class="form-tip">启用电子面单打印功能</span>
        </el-form-item>
        <el-form-item label="默认物流公司">
          <el-select v-model="ruleForm.defaultCompany" placeholder="请选择" style="width: 200px">
            <el-option label="顺丰速运" value="shunfeng" />
            <el-option label="中通快递" value="zhongtong" />
            <el-option label="圆通速递" value="yuantong" />
            <el-option label="韵达快递" value="yunda" />
            <el-option label="申通快递" value="shentong" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveRules">
            <el-icon><Select /></el-icon>保存配置
          </el-button>
          <el-button @click="handleResetRules">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 添加/编辑物流公司对话框 -->
    <el-dialog
      v-model="companyDialogVisible"
      :title="companyDialogTitle"
      width="600px"
    >
      <el-form :model="companyForm" label-width="100px">
        <el-form-item label="物流公司" required>
          <el-input v-model="companyForm.name" placeholder="请输入物流公司名称" />
        </el-form-item>
        <el-form-item label="公司代码" required>
          <el-input v-model="companyForm.code" placeholder="请输入公司代码" />
        </el-form-item>
        <el-form-item label="LOGO">
          <el-input v-model="companyForm.logo" placeholder="请输入LOGO地址" />
        </el-form-item>
        <el-form-item label="API地址" required>
          <el-input v-model="companyForm.apiUrl" placeholder="请输入API地址" />
        </el-form-item>
        <el-form-item label="App Key">
          <el-input v-model="companyForm.appKey" placeholder="请输入App Key" />
        </el-form-item>
        <el-form-item label="App Secret">
          <el-input v-model="companyForm.appSecret" type="password" placeholder="请输入App Secret" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="companyForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="companyForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="companyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCompany">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Connection, Select, RefreshRight
} from '@element-plus/icons-vue'

const loading = ref(false)

// 物流公司列表
const companyList = ref([
  {
    id: 1,
    name: '顺丰速运',
    code: 'shunfeng',
    logo: 'https://via.placeholder.com/60x40',
    enabled: true,
    apiUrl: 'https://api.sf-express.com',
    appKey: 'SF***************',
    sort: 1
  },
  {
    id: 2,
    name: '中通快递',
    code: 'zhongtong',
    logo: 'https://via.placeholder.com/60x40',
    enabled: true,
    apiUrl: 'https://api.zto.com',
    appKey: 'ZTO***************',
    sort: 2
  },
  {
    id: 3,
    name: '圆通速递',
    code: 'yuantong',
    logo: 'https://via.placeholder.com/60x40',
    enabled: true,
    apiUrl: 'https://api.yto.net.cn',
    appKey: 'YTO***************',
    sort: 3
  }
])

// 运费模板列表
const templateList = ref([
  {
    id: 1,
    name: '标准快递',
    chargeType: 'weight',
    basePrice: '8.00',
    baseUnit: '1kg',
    addPrice: '2.00',
    addUnit: '1kg',
    freeShipping: true,
    freeAmount: 99,
    enabled: true
  },
  {
    id: 2,
    name: '同城配送',
    chargeType: 'count',
    basePrice: '5.00',
    baseUnit: '1件',
    addPrice: '1.00',
    addUnit: '1件',
    freeShipping: false,
    freeAmount: 0,
    enabled: true
  }
])

// 物流规则表单
const ruleForm = reactive({
  autoShip: false,
  shipTimeout: 24,
  autoConfirm: true,
  confirmDays: 7,
  trackUpdate: 'hourly',
  exceptionHandle: ['notify', 'alert'],
  electronicSheet: true,
  defaultCompany: 'shunfeng'
})

// 物流公司对话框
const companyDialogVisible = ref(false)
const companyDialogTitle = ref('添加物流公司')
const companyForm = reactive({
  id: null,
  name: '',
  code: '',
  logo: '',
  apiUrl: '',
  appKey: '',
  appSecret: '',
  sort: 0,
  enabled: true
})

// 添加物流公司
const handleAddCompany = () => {
  companyDialogTitle.value = '添加物流公司'
  Object.assign(companyForm, {
    id: null,
    name: '',
    code: '',
    logo: '',
    apiUrl: '',
    appKey: '',
    appSecret: '',
    sort: 0,
    enabled: true
  })
  companyDialogVisible.value = true
}

// 编辑物流公司
const handleEdit = (row) => {
  companyDialogTitle.value = '编辑物流公司'
  Object.assign(companyForm, { ...row })
  companyDialogVisible.value = true
}

// 保存物流公司
const handleSaveCompany = () => {
  // TODO: 调用接口保存
  ElMessage.success('保存成功')
  companyDialogVisible.value = false
}

// 切换状态
const handleToggleStatus = (row) => {
  ElMessage.success(row.enabled ? '已启用' : '已禁用')
}

// 测试连接
const handleTest = (row) => {
  ElMessage.info(`正在测试 ${row.name} 连接...`)
  setTimeout(() => {
    ElMessage.success('连接测试成功')
  }, 1000)
}

// 删除物流公司
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除物流公司"${row.name}"吗？`, '确认删除', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

// 添加模板
const handleAddTemplate = () => {
  ElMessage.info('打开添加模板对话框')
}

// 编辑模板
const handleEditTemplate = (row) => {
  ElMessage.info(`编辑模板: ${row.name}`)
}

// 删除模板
const handleDeleteTemplate = (row) => {
  ElMessageBox.confirm(`确定要删除模板"${row.name}"吗？`, '确认删除', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

// 保存规则
const handleSaveRules = () => {
  // TODO: 调用接口保存
  ElMessage.success('规则配置已保存')
}

// 重置规则
const handleResetRules = () => {
  Object.assign(ruleForm, {
    autoShip: false,
    shipTimeout: 24,
    autoConfirm: true,
    confirmDays: 7,
    trackUpdate: 'hourly',
    exceptionHandle: ['notify', 'alert'],
    electronicSheet: true,
    defaultCompany: 'shunfeng'
  })
  ElMessage.success('已重置为默认配置')
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
