<template>
  <div class="personal-open-container">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="tip-alert"
      title="平台自研个人入网：mchType=3 → /web/pay/merchant/*。通企付 member/regPerson 需密钥，本页不调用。"
    />

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.companyName" placeholder="姓名/户名" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.legalPhone" placeholder="法人/本人手机" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="入网状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;">
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
          <span class="card-title">个人入网列表（mchType=3）</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>新增个人入网</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="legalName" label="姓名" width="120" />
        <el-table-column prop="legalIdNo" label="证件号" min-width="180" />
        <el-table-column prop="legalPhone" label="手机号" width="140" />
        <el-table-column prop="mchNo" label="商户号" min-width="150" />
        <el-table-column prop="status" label="入网状态" width="110">
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

    <el-dialog v-model="createDialogVisible" title="新增个人入网" width="600px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="110px">
        <el-form-item label="真实姓名" prop="legalName">
          <el-input v-model="createForm.legalName" />
        </el-form-item>
        <el-form-item label="证件号" prop="legalIdNo">
          <el-input v-model="createForm.legalIdNo" />
        </el-form-item>
        <el-form-item label="手机号" prop="legalPhone">
          <el-input v-model="createForm.legalPhone" />
        </el-form-item>
        <el-form-item label="结算银行">
          <el-input v-model="createForm.settleBankName" />
        </el-form-item>
        <el-form-item label="银行卡号">
          <el-input v-model="createForm.settleAccountNo" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitCreate">提交入网</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="个人入网详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="姓名">{{ currentRow.legalName }}</el-descriptions-item>
        <el-descriptions-item label="证件号">{{ currentRow.legalIdNo }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentRow.legalPhone }}</el-descriptions-item>
        <el-descriptions-item label="商户号">{{ currentRow.mchNo }}</el-descriptions-item>
        <el-descriptions-item label="入网状态">
          <el-tag size="small" :type="statusTagType[currentRow.status] || 'info'">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-card shadow="never" class="stub-card">
      <template #header><span class="card-title">通企付个人开户 stub</span></template>
      <p class="stub-text">
        <code>/web/pay/member/regPerson</code> 依赖通企付密钥，未配置前保持 stub。配置项同企业：<code>tqf.appId</code>/<code>tqf.appSecret</code>。
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

const loading = ref(false)
const submitLoading = ref(false)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const createFormRef = ref(null)

const searchForm = reactive({ companyName: '', legalPhone: '', status: undefined })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const createForm = reactive({ legalName: '', legalIdNo: '', legalPhone: '', settleBankName: '', settleAccountNo: '' })

const createRules = {
  legalName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  legalIdNo: [{ required: true, message: '请输入证件号', trigger: 'blur' }],
  legalPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await merchantPlatformApi.list({
      pageNo: pagination.page,
      pageSize: pagination.size,
      mchType: 3,
      companyName: searchForm.companyName || undefined,
      legalPhone: searchForm.legalPhone || undefined,
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
    console.error('查询个人入网失败:', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => {
  Object.assign(searchForm, { companyName: '', legalPhone: '', status: undefined })
  handleSearch()
}
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleCreate = () => {
  Object.assign(createForm, { legalName: '', legalIdNo: '', legalPhone: '', settleBankName: '', settleAccountNo: '' })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const res = await merchantPlatformApi.reg({
      mchType: 3,
      companyName: createForm.legalName,
      legalName: createForm.legalName,
      legalIdNo: createForm.legalIdNo,
      legalPhone: createForm.legalPhone,
      contactName: createForm.legalName,
      contactPhone: createForm.legalPhone,
      settleBankName: createForm.settleBankName,
      settleAccountNo: createForm.settleAccountNo,
      settleType: 2
    })
    if (res && isSuccessCode(res.code)) {
      ElMessage.success(`个人入网已提交，商户号 ${res.data?.mchNo || ''}，待审核`)
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
    await ElMessageBox.confirm(`确认对 ${row.mchNo} ${action}？`, '入网审核', { type: 'warning' })
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
.personal-open-container { padding: 16px; }
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
