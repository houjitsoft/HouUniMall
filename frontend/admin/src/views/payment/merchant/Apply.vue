<template>
  <div class="merchant-apply-container">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip-alert"
      title="商户进件走平台自研 /web/pay/merchant/{reg,list,changeStatus}。通企付 /web/pay/mch/* 需密钥，本页不调用。"
    />

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商户名称">
          <el-input v-model="searchForm.companyName" placeholder="商户名称" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="信用代码">
          <el-input v-model="searchForm.uniCredit" placeholder="统一社会信用代码" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="商户类型">
          <el-select v-model="searchForm.mchType" clearable placeholder="全部" style="width: 130px;">
            <el-option label="企业" :value="1" />
            <el-option label="个体" :value="2" />
            <el-option label="个人" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="进件状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 130px;">
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
          <span class="card-title">商户进件列表</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>新建进件单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="companyName" label="商户名称" min-width="180" />
        <el-table-column prop="uniCredit" label="统一社会信用代码" min-width="170" />
        <el-table-column prop="mchType" label="类型" width="90">
          <template #default="{ row }">{{ mchTypeText[row.mchType] || row.mchType }}</template>
        </el-table-column>
        <el-table-column prop="mchNo" label="商户号" min-width="140" />
        <el-table-column prop="contactName" label="联系人" width="110" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="status" label="进件状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status] || 'info'">{{ statusText[row.status] ?? row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" min-width="160" />
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

    <el-dialog v-model="createDialogVisible" title="新建进件单" width="720px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="130px">
        <el-form-item label="商户类型" prop="mchType">
          <el-select v-model="createForm.mchType" style="width: 100%;">
            <el-option label="企业" :value="1" />
            <el-option label="个体工商户" :value="2" />
            <el-option label="个人" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="商户名称" prop="companyName">
          <el-input v-model="createForm.companyName" placeholder="商户全称/姓名" />
        </el-form-item>
        <el-form-item v-if="createForm.mchType !== 3" label="统一信用代码" prop="uniCredit">
          <el-input v-model="createForm.uniCredit" />
        </el-form-item>
        <el-form-item label="法人/负责人" prop="legalName">
          <el-input v-model="createForm.legalName" />
        </el-form-item>
        <el-form-item label="证件号" prop="legalIdNo">
          <el-input v-model="createForm.legalIdNo" />
        </el-form-item>
        <el-form-item label="手机号" prop="legalPhone">
          <el-input v-model="createForm.legalPhone" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="createForm.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="createForm.contactPhone" />
        </el-form-item>
        <el-form-item label="经营地址">
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
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">提交进件</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="进件详情" width="720px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="商户名称">{{ currentRow.companyName }}</el-descriptions-item>
        <el-descriptions-item label="商户号">{{ currentRow.mchNo }}</el-descriptions-item>
        <el-descriptions-item label="信用代码">{{ currentRow.uniCredit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ mchTypeText[currentRow.mchType] }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentRow.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="进件状态">
          <el-tag size="small" :type="statusTagType[currentRow.status] || 'info'">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentRow.approvedTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.statusRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-card shadow="never" class="stub-card">
      <template #header><span class="card-title">通企付 mch/reg stub</span></template>
      <p class="stub-text">
        旧路径 <code>/web/pay/mch/reg</code>、<code>/web/pay/mch/list</code> 为通企付适配，需渠道密钥后才可启用。
        配置项：<code>tqf.appId</code>、<code>tqf.appSecret</code>、渠道商户号。勿在未配置时伪造密钥。
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { merchantPlatformApi } from '@/api/payment/paymentBizW4'
import { isSuccessCode } from '@/utils/errorCode'

const statusText = { 0: '待审核', 1: '审核中', 2: '已生效', 3: '已冻结', 4: '已注销', 5: '审核失败' }
const statusTagType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info', 4: 'info', 5: 'danger' }
const mchTypeText = { 1: '企业', 2: '个体', 3: '个人' }

const loading = ref(false)
const submitLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const createFormRef = ref(null)

const searchForm = reactive({ companyName: '', uniCredit: '', mchType: undefined, status: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const createForm = reactive({
  mchType: 1, companyName: '', uniCredit: '', legalName: '', legalIdNo: '', legalPhone: '',
  contactName: '', contactPhone: '', address: '', settleBankName: '', settleAccountNo: ''
})

const createRules = {
  mchType: [{ required: true, message: '请选择商户类型', trigger: 'change' }],
  companyName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  legalName: [{ required: true, message: '请输入法人/负责人', trigger: 'blur' }],
  legalIdNo: [{ required: true, message: '请输入证件号', trigger: 'blur' }],
  legalPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await merchantPlatformApi.list({
      pageNo: pagination.page,
      pageSize: pagination.size,
      companyName: searchForm.companyName || undefined,
      uniCredit: searchForm.uniCredit || undefined,
      mchType: searchForm.mchType,
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
    console.error('查询进件列表失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => {
  Object.assign(searchForm, { companyName: '', uniCredit: '', mchType: undefined, status: undefined })
  handleSearch()
}
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleCreate = () => {
  Object.assign(createForm, {
    mchType: 1, companyName: '', uniCredit: '', legalName: '', legalIdNo: '', legalPhone: '',
    contactName: '', contactPhone: '', address: '', settleBankName: '', settleAccountNo: ''
  })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (createForm.mchType !== 3 && !createForm.uniCredit) {
    ElMessage.warning('企业/个体请填写统一社会信用代码')
    return
  }
  submitLoading.value = true
  try {
    const res = await merchantPlatformApi.reg({
      mchType: createForm.mchType,
      companyName: createForm.companyName,
      uniCredit: createForm.uniCredit || undefined,
      legalName: createForm.legalName,
      legalIdNo: createForm.legalIdNo,
      legalPhone: createForm.legalPhone,
      contactName: createForm.contactName || createForm.legalName,
      contactPhone: createForm.contactPhone || createForm.legalPhone,
      address: createForm.address,
      settleBankName: createForm.settleBankName,
      settleAccountNo: createForm.settleAccountNo
    })
    if (res && isSuccessCode(res.code)) {
      ElMessage.success(`进件已提交，商户号 ${res.data?.mchNo || ''}，待审核`)
      createDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res?.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('进件请求异常')
  } finally {
    submitLoading.value = false
  }
}

const audit = async (row, status) => {
  const action = status === 2 ? '审核通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确认对 ${row.mchNo} ${action}？`, '进件审核', { type: 'warning' })
  } catch { return }
  try {
    const res = await merchantPlatformApi.changeStatus({ mchNo: row.mchNo, status, remark: action })
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
.merchant-apply-container { padding: 16px; }
.tip-alert { margin-bottom: 12px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card, .stub-card { border-radius: 8px; margin-bottom: 12px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
.stub-text { font-size: 13px; color: #606266; line-height: 1.6; margin: 0; }
.stub-text code { background: #f5f7fa; padding: 1px 6px; border-radius: 3px; }
</style>
