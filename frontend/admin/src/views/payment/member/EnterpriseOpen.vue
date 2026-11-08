<template>
  <div class="enterprise-open-container">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip-alert"
      title="平台自研入网：走 /web/pay/merchant/*（本地审核 status 0→2）。通企付 member/regEnterprise 需渠道密钥，见页面底部 stub 说明。"
    />

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="企业名称">
          <el-input v-model="searchForm.companyName" placeholder="企业名称" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="信用代码">
          <el-input v-model="searchForm.uniCredit" placeholder="统一社会信用代码" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="入网状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px;">
            <el-option v-for="(t, k) in statusText" :key="k" :label="t" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">企业入网列表（mchType=1）</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>新增企业入网</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="companyName" label="企业名称" min-width="180" />
        <el-table-column prop="uniCredit" label="统一社会信用代码" min-width="180" />
        <el-table-column prop="legalName" label="法定代表人" width="120" />
        <el-table-column prop="mchNo" label="商户号" min-width="150" />
        <el-table-column prop="status" label="入网/审核状态" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status] || 'info'">{{ statusText[row.status] ?? row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0 || row.status === 1" link type="success" size="small" @click="audit(row, 2)">通过</el-button>
            <el-button v-if="row.status === 0 || row.status === 1" link type="danger" size="small" @click="audit(row, 5)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="新增企业入网" width="720px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="130px">
        <el-form-item label="企业名称" prop="companyName">
          <el-input v-model="createForm.companyName" placeholder="企业全称" />
        </el-form-item>
        <el-form-item label="统一信用代码" prop="uniCredit">
          <el-input v-model="createForm.uniCredit" placeholder="18位统一社会信用代码" />
        </el-form-item>
        <el-form-item label="法定代表人" prop="legalName">
          <el-input v-model="createForm.legalName" />
        </el-form-item>
        <el-form-item label="法人证件号" prop="legalIdNo">
          <el-input v-model="createForm.legalIdNo" />
        </el-form-item>
        <el-form-item label="法人手机" prop="legalPhone">
          <el-input v-model="createForm.legalPhone" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="createForm.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="createForm.contactPhone" />
        </el-form-item>
        <el-form-item label="企业地址">
          <el-input v-model="createForm.address" />
        </el-form-item>
        <el-form-item label="结算银行">
          <el-input v-model="createForm.settleBankName" />
        </el-form-item>
        <el-form-item label="结算账号">
          <el-input v-model="createForm.settleAccountNo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">提交入网</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="企业入网详情" width="720px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="企业名称">{{ currentRow.companyName }}</el-descriptions-item>
        <el-descriptions-item label="信用代码">{{ currentRow.uniCredit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="法定代表人">{{ currentRow.legalName }}</el-descriptions-item>
        <el-descriptions-item label="商户号">{{ currentRow.mchNo }}</el-descriptions-item>
        <el-descriptions-item label="法人手机">{{ currentRow.legalPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入网状态">
          <el-tag size="small" :type="statusTagType[currentRow.status] || 'info'">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRow.approvedTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态备注" :span="2">{{ currentRow.statusRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-card shadow="never" class="stub-card">
      <template #header><span class="card-title">通企付 / regbind stub（需密钥，未启用）</span></template>
      <p class="stub-text">
        渠道接口 <code>/web/pay/member/regEnterprise</code>、<code>/web/pay/mch/reg</code> 依赖通企付 appId/密钥与签约商户号。
        请在支付配置写入真实密钥后再接通；当前页面<strong>不调用</strong>这些接口，避免假成功。
        所需配置项：<code>tqf.appId</code>、<code>tqf.appSecret</code>（或等价 ChannelCredential）、入网回调 URL。
      </p>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 企业入网 — 平台自研商户 API（可运营）
 * 列表/进件/审核：/web/pay/merchant/{list,reg,changeStatus}
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { merchantPlatformApi } from '@/api/payment/paymentBizW4'
import { isSuccessCode } from '@/utils/errorCode'

const statusText = { 0: '待审核', 1: '审核中', 2: '已生效', 3: '已冻结', 4: '已注销', 5: '审核失败' }
const statusTagType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'info', 5: 'danger' }

const loading = ref(false)
const submitLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const createFormRef = ref(null)

const searchForm = reactive({ companyName: '', uniCredit: '', status: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const createForm = reactive({
  companyName: '', uniCredit: '', legalName: '', legalIdNo: '', legalPhone: '',
  contactName: '', contactPhone: '', address: '', settleBankName: '', settleAccountNo: ''
})

const createRules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  uniCredit: [{ required: true, message: '请输入统一社会信用代码', trigger: 'blur' }],
  legalName: [{ required: true, message: '请输入法定代表人', trigger: 'blur' }],
  legalIdNo: [{ required: true, message: '请输入法人证件号', trigger: 'blur' }],
  legalPhone: [{ required: true, message: '请输入法人手机', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await merchantPlatformApi.list({
      pageNo: pagination.page,
      pageSize: pagination.size,
      mchType: 1,
      companyName: searchForm.companyName || undefined,
      uniCredit: searchForm.uniCredit || undefined,
      status: searchForm.status === undefined || searchForm.status === null || searchForm.status === ''
        ? undefined
        : Number(searchForm.status)
    })
    if (res && isSuccessCode(res.code)) {
      tableData.value = res.data?.records || res.data?.list || []
      pagination.total = res.data?.total || 0
    } else {
      tableData.value = []
    }
  } catch (e) {
    console.error('查询企业入网列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => {
  Object.assign(searchForm, { companyName: '', uniCredit: '', status: undefined })
  handleSearch()
}
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }

const handleCreate = () => {
  Object.assign(createForm, {
    companyName: '', uniCredit: '', legalName: '', legalIdNo: '', legalPhone: '',
    contactName: '', contactPhone: '', address: '', settleBankName: '', settleAccountNo: ''
  })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = {
      mchType: 1,
      companyName: createForm.companyName,
      uniCredit: createForm.uniCredit,
      legalName: createForm.legalName,
      legalIdNo: createForm.legalIdNo,
      legalPhone: createForm.legalPhone,
      contactName: createForm.contactName || createForm.legalName,
      contactPhone: createForm.contactPhone || createForm.legalPhone,
      address: createForm.address,
      settleBankName: createForm.settleBankName,
      settleAccountNo: createForm.settleAccountNo
    }
    const res = await merchantPlatformApi.reg(payload)
    if (res && isSuccessCode(res.code)) {
      ElMessage.success(`企业入网已提交，商户号 ${res.data?.mchNo || ''}，状态：待审核`)
      createDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res?.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('入网请求异常')
  } finally {
    submitLoading.value = false
  }
}

const audit = async (row, status) => {
  const action = status === 2 ? '审核通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确认对商户 ${row.mchNo} 执行「${action}」？`, '入网审核', { type: 'warning' })
  } catch { return }
  try {
    const res = await merchantPlatformApi.changeStatus({
      mchNo: row.mchNo,
      status,
      remark: action
    })
    if (res && isSuccessCode(res.code)) {
      ElMessage.success(`${action}成功`)
      fetchList()
    } else {
      ElMessage.error(res?.message || `${action}失败`)
    }
  } catch (e) {
    ElMessage.error(`${action}异常`)
  }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.enterprise-open-container { padding: 16px; }
.tip-alert { margin-bottom: 12px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card, .stub-card { border-radius: 8px; margin-bottom: 12px; }
.table-card :deep(.el-card__header), .stub-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
.stub-text { font-size: 13px; color: #606266; line-height: 1.6; margin: 0; }
.stub-text code { background: #f5f7fa; padding: 1px 6px; border-radius: 3px; }
</style>
