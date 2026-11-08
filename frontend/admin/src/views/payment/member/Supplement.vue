<template>
  <div class="member-supplement-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="会员编号">
          <el-input v-model="searchForm.memberNo" placeholder="请输入会员编号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="会员名称">
          <el-input v-model="searchForm.memberName" placeholder="请输入会员名称" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="补录状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="待补录" value="pending" />
            <el-option label="已提交" value="submitted" />
            <el-option label="已完成" value="finished" />
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
          <span class="card-title">资料补录列表</span>
          <el-button type="primary" @click="handleCreate"><el-icon><Plus /></el-icon>新建补录单</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="memberNo" label="会员编号" width="160" />
        <el-table-column prop="memberName" label="会员名称" min-width="180" />
        <el-table-column prop="missingItems" label="缺失项" min-width="180" />
        <el-table-column prop="status" label="补录状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType[row.status]">{{ statusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button link type="success" size="small" v-if="row.status === 'pending'" @click="handleSupplement(row)">补录</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 补录对话框 -->
    <el-dialog v-model="supplementDialogVisible" :title="isCreate ? '新建补录单' : '资料补录'" width="650px" :close-on-click-modal="false">
      <el-form :model="supplementForm" :rules="supplementRules" ref="supplementFormRef" label-width="110px">
        <el-form-item label="会员编号" prop="memberNo">
          <el-input v-model="supplementForm.memberNo" placeholder="请输入会员编号" :disabled="!isCreate" />
        </el-form-item>
        <el-form-item label="补录类型" prop="supplementType">
          <el-checkbox-group v-model="supplementForm.supplementType">
            <el-checkbox label="营业执照" value="license" />
            <el-checkbox label="法人证件" value="idPhoto" />
            <el-checkbox label="银行账户" value="bankAccount" />
            <el-checkbox label="经营地址" value="address" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="营业执照号" v-if="supplementForm.supplementType.includes('license')">
          <el-input v-model="supplementForm.licenseNo" placeholder="请输入营业执照编号" />
        </el-form-item>
        <el-form-item label="银行账号" v-if="supplementForm.supplementType.includes('bankAccount')">
          <el-input v-model="supplementForm.bankAccount" placeholder="请输入银行账号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="supplementForm.remark" type="textarea" :rows="2" placeholder="补录说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="supplementDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitSupplement">提交补录</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailDialogVisible" title="补录详情" width="600px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="会员编号">{{ currentRow.memberNo }}</el-descriptions-item>
        <el-descriptions-item label="会员名称">{{ currentRow.memberName }}</el-descriptions-item>
        <el-descriptions-item label="缺失项">{{ currentRow.missingItems }}</el-descriptions-item>
        <el-descriptions-item label="补录状态">
          <el-tag size="small" :type="statusTagType[currentRow.status]">{{ statusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRow.operator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 会员资料补录页面
 * 补录完善存量会员的合规资料
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const statusText = { pending: '待补录', submitted: '已提交', finished: '已完成' }
const statusTagType = { pending: 'warning', submitted: 'primary', finished: 'success' }

const loading = ref(false)
const submitLoading = ref(false)
const supplementDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isCreate = ref(false)
const currentRow = ref(null)
const tableData = ref([])
const supplementFormRef = ref(null)

const searchForm = reactive({ memberNo: '', memberName: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const supplementForm = reactive({ memberNo: '', supplementType: [], licenseNo: '', bankAccount: '', remark: '' })

const supplementRules = {
  memberNo: [{ required: true, message: '请输入会员编号', trigger: 'blur' }],
  supplementType: [{ required: true, message: '请选择补录类型', trigger: 'change', type: 'array', min: 1 }]
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, memberNo: searchForm.memberNo || undefined, memberName: searchForm.memberName || undefined, status: searchForm.status || undefined }
    const res = await payApi.queryMember({ ...params, queryType: 'supplement' })
    if (res && res.code === 200) { tableData.value = res.data?.records || res.data?.list || []; pagination.total = res.data?.total || 0 }
  } catch (e) { console.error('查询补录列表失败:', e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { memberNo: '', memberName: '', status: '' }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }

const handleCreate = () => {
  isCreate.value = true
  Object.assign(supplementForm, { memberNo: '', supplementType: [], licenseNo: '', bankAccount: '', remark: '' })
  supplementDialogVisible.value = true
}

const handleSupplement = (row) => {
  isCreate.value = false
  Object.assign(supplementForm, { memberNo: row.memberNo, supplementType: [], licenseNo: '', bankAccount: '', remark: '' })
  supplementDialogVisible.value = true
}

const submitSupplement = async () => {
  const valid = await supplementFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const res = await payApi.regSupplement(supplementForm)
    if (res && res.code === 200) { ElMessage.success('补录信息已提交'); supplementDialogVisible.value = false; fetchList() }
    else { ElMessage.error(res?.msg || '提交失败') }
  } catch (e) { ElMessage.error('补录请求异常') } finally { submitLoading.value = false }
}

onMounted(() => { fetchList() })
</script>

<style scoped>
.member-supplement-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
