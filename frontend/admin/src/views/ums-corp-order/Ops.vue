<template>
  <div class="ums-page">
    <el-card shadow="never" class="mb">
      <el-form :inline="true" :model="search">
        <el-form-item label="交易码">
          <el-select v-model="search.transCode" clearable placeholder="全部" style="width: 160px">
            <el-option v-for="c in CODES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card shadow="never">
      <el-table :data="list" v-loading="loading" stripe empty-text="暂无数据">
        <el-table-column prop="trans_code" label="交易码" width="100" />
        <el-table-column prop="src_req_id" label="请求流水" min-width="180" show-overflow-tooltip />
        <el-table-column prop="src_req_date" label="请求日期" width="110" />
        <el-table-column prop="order_no" label="关联订单" min-width="140" />
        <el-table-column prop="pay_amt" label="金额(分)" width="100" />
        <el-table-column prop="status" label="状态" width="90" />
        <el-table-column prop="resp_code" label="返回码" width="110" />
        <el-table-column prop="resp_msg" label="返回信息" min-width="160" show-overflow-tooltip />
        <el-table-column prop="create_time" label="时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="queryRemote(row)">查银商</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :total="page.total"
          layout="total, prev, pager, next"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { umsCorpApi } from '@/api/ums-corp-order'

const CODES = ['202101', '202102', '202103', '202104', '202105', '202106', '202107', '202108', '202109', '202110']
const loading = ref(false)
const list = ref([])
const search = reactive({ transCode: '' })
const page = reactive({ pageNum: 1, pageSize: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const res = await umsCorpApi.opPage({ ...search, pageNum: page.pageNum, pageSize: page.pageSize })
    const data = res?.data || res || {}
    list.value = data.records || []
    page.total = data.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

async function queryRemote(row) {
  try {
    const res = await umsCorpApi.opQuery({
      reqDate: row.src_req_date,
      reqJournalNo: row.src_req_id
    })
    ElMessage.success(`银商状态：${res?.data?.status ?? '-'} / ${res?.data?.respMsg || res?.data?.respCode || ''}`)
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  }
}

onMounted(load)
</script>

<style scoped>
.ums-page { padding: 16px; }
.mb { margin-bottom: 12px; }
.pager { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
