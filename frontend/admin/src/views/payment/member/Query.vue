<template>
  <div class="member-query-container">
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="会员编号">
          <el-input v-model="searchForm.memberNo" placeholder="请输入会员编号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="会员名称">
          <el-input v-model="searchForm.memberName" placeholder="请输入会员名称" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="会员类型">
          <el-select v-model="searchForm.memberType" placeholder="请选择" clearable style="width: 130px;">
            <el-option label="企业会员" value="enterprise" />
            <el-option label="个人会员" value="personal" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="正常" value="active" />
            <el-option label="冻结" value="frozen" />
            <el-option label="注销" value="closed" />
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
          <span class="card-title">会员信息列表</span>
          <el-button @click="handleExport"><el-icon><Download /></el-icon>导出</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%;">
        <el-table-column prop="memberNo" label="会员编号" width="160" />
        <el-table-column prop="memberType" label="会员类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.memberType === 'enterprise' ? 'primary' : 'success'">{{ row.memberType === 'enterprise' ? '企业' : '个人' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberName" label="会员名称" min-width="180" />
        <el-table-column prop="mobile" label="联系电话" width="140" />
        <el-table-column prop="balance" label="账户余额" width="120" align="right">
          <template #default="{ row }">¥{{ row.balance || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="账户状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="accountStatusType[row.status]">{{ accountStatusText[row.status] || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="开户时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size" :page-sizes="[10, 20, 50, 100]" :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" background @size-change="fetchList" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailDialogVisible" title="会员详情" width="700px">
      <el-descriptions v-if="currentRow" :column="2" border>
        <el-descriptions-item label="会员编号">{{ currentRow.memberNo }}</el-descriptions-item>
        <el-descriptions-item label="会员类型">{{ currentRow.memberType === 'enterprise' ? '企业会员' : '个人会员' }}</el-descriptions-item>
        <el-descriptions-item label="会员名称">{{ currentRow.memberName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.mobile || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账户余额">¥{{ currentRow.balance || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="账户状态">
          <el-tag size="small" :type="accountStatusType[currentRow.status]">{{ accountStatusText[currentRow.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开户时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="最近活跃">{{ currentRow.lastActiveTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 会员信息查询页面
 * 统一查询企业/个人会员的基础信息与账户状态
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { payApi } from '@/api/payment/pay'

const accountStatusText = { active: '正常', frozen: '冻结', closed: '注销' }
const accountStatusType = { active: 'success', frozen: 'warning', closed: 'info' }

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentRow = ref(null)
const tableData = ref([])

const searchForm = reactive({ memberNo: '', memberName: '', memberType: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.page, pageSize: pagination.size, memberNo: searchForm.memberNo || undefined, memberName: searchForm.memberName || undefined, memberType: searchForm.memberType || undefined, status: searchForm.status || undefined }
    const res = await payApi.queryMember(params)
    if (res && res.code === 200) {
      const data = res.data != null ? res.data : res
      let records = data.records || data.list || []
      // 兼容聚合查询返回单条会员对象的情况
      if (!records.length && data && typeof data === 'object' && !Array.isArray(data)) {
        const member = data.member || data.memberInfo || data
        if (member && typeof member === 'object') records = [member]
      }
      tableData.value = records.map(mapMemberRow)
      pagination.total = data.total || records.length || 0
    }
  } catch (e) { console.error('查询会员信息失败:', e) } finally { loading.value = false }
}

// 会员行映射：兼容会员聚合查询(e.g. memberNo/memberName/memberType/mobile)字段
const mapMemberRow = (m) => {
  const inner = m.member || m.memberInfo || m
  return {
    memberNo: inner.memberNo || m.memberNo || m.memberId || m.bizUserId || '',
    memberType: inner.memberType || m.memberType || (m.bizUserId ? 'enterprise' : 'personal'),
    memberName: inner.memberName || m.memberName || inner.companyName || m.companyName || inner.name || m.name || '',
    mobile: inner.mobile || m.mobile || inner.phone || m.phone || '',
    balance: inner.balance ?? m.balance ?? inner.userMoney ?? m.userMoney ?? 0,
    status: inner.status ?? m.status ?? (m.userStatus != null ? String(m.userStatus) : 'active'),
    createTime: inner.createTime || m.createTime || m.ts || '',
    lastActiveTime: inner.lastActiveTime || m.lastActiveTime || '',
    _raw: m
  }
}

const handleSearch = () => { pagination.page = 1; fetchList() }
const handleReset = () => { Object.assign(searchForm, { memberNo: '', memberName: '', memberType: '', status: '' }); handleSearch() }
const handleDetail = (row) => { currentRow.value = row; detailDialogVisible.value = true }
const handleExport = () => { ElMessage.info('导出功能开发中') }

onMounted(() => { fetchList() })
</script>

<style scoped>
.member-query-container { padding: 16px; }
.search-card { margin-bottom: 12px; border-radius: 8px; }
.search-card :deep(.el-card__body) { padding: 14px 16px 0 16px; }
.search-form :deep(.el-form-item) { margin-bottom: 14px; }
.table-card { border-radius: 8px; }
.table-card :deep(.el-card__header) { padding: 12px 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 15px; font-weight: 600; color: #303133; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
