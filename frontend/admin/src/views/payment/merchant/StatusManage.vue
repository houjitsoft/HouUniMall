<template>
  <div class="status-manage-page">
    <el-card shadow="never">
      <template #header><span>商户状态管理</span></template>
      <el-form :inline="true" :model="search" style="margin-bottom:12px">
        <el-form-item label="商户号"><el-input v-model="search.mchNo" placeholder="商户号" style="width:160px" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="search.status" clearable placeholder="全部" style="width:120px">
            <el-option label="正常" value="1" /><el-option label="冻结" value="2" /><el-option label="注销" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="loadData">查询</el-button></el-form-item>
      </el-form>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="mchNo" label="商户号" width="150" />
        <el-table-column prop="mchName" label="商户名称" min-width="160" />
        <el-table-column prop="status" label="当前状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'danger' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : row.status === 2 ? '冻结' : '注销' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入驻时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="warning" size="small" @click="changeStatus(row, 2)">冻结</el-button>
            <el-button v-if="row.status === 2" link type="success" size="small" @click="changeStatus(row, 1)">解冻</el-button>
            <el-popconfirm title="确认注销此商户?" @confirm="changeStatus(row, 3)">
              <template #reference><el-button link type="danger" size="small">注销</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:12px;display:flex;justify-content:flex-end;">
        <el-pagination v-model:current-page="page.current" v-model:page-size="page.size" :total="page.total"
          :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" background @change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const loading = ref(false)
const tableData = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const search = reactive({ mchNo: '', status: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.post('/web/pay/merchant/list', { ...search, page: page.current, size: page.size })
    if (res.code === 200 && res.data) {
      tableData.value = res.data.records || res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const changeStatus = async (row, status) => {
  const res = await request.post('/web/pay/merchant/changeStatus', { mchNo: row.mchNo, status })
  if (res.code === 200) { ElMessage.success('状态变更成功'); loadData() } else { ElMessage.error(res.message || '操作失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.status-manage-page { padding: 16px; }
</style>
