<template>
  <div class="commission-record">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>佣金记录</span>
          <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleBatchSettle">
            批量结算（{{ selectedIds.length }}）
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="分销商ID">
          <el-input v-model="query.distributorId" placeholder="按分销商筛选" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待结算" :value="0" />
            <el-option label="已结算" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="46" />
        <el-table-column prop="recordNo" label="佣金单号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="orderNo" label="订单号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="goodsName" label="商品" min-width="140" show-overflow-tooltip />
        <el-table-column prop="orderAmount" label="订单金额" width="110" align="right" />
        <el-table-column prop="commissionAmount" label="佣金金额" width="110" align="right" />
        <el-table-column prop="commissionRate" label="佣金比例" width="100" align="right" />
        <el-table-column prop="level" label="层级" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settleTime" label="结算时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" :disabled="row.status !== 0" @click="handleSettle(row)">结算</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无佣金记录" />
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="query[pageKey]"
        :page-size="query[sizeKey]"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="onCurrentChange"
        @size-change="onSizeChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { commissionRecordApi } from '../api/distribution'
import { runAction, useTable } from '../utils/useTable'

// commission 读 page/size
const {
  loading, list, total, query, pageKey, sizeKey,
  load, search, reset, onCurrentChange, onSizeChange
} = useTable(commissionRecordApi.list, {
  defaultQuery: { distributorId: null, status: null },
  pageKeys: ['page', 'size']
})

const selectedIds = ref([])

function statusText(status) {
  if (status === 0) return '待结算'
  if (status === 1) return '已结算'
  if (status === 2) return '已取消'
  return status === null || status === undefined ? '-' : `状态${status}`
}

function statusTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'default'
}

function onSelectionChange(rows) {
  selectedIds.value = rows.map(row => row.id)
}

async function handleSettle(row) {
  await ElMessageBox.confirm(`确认结算佣金单「${row.recordNo}」？`, '结算确认', { type: 'warning' })
  await runAction(() => commissionRecordApi.settle({ id: row.id }), load, '结算成功')
}

async function handleBatchSettle() {
  await ElMessageBox.confirm(`确认批量结算选中的 ${selectedIds.value.length} 条佣金记录？`, '批量结算', { type: 'warning' })
  await runAction(() => commissionRecordApi.batchSettle(selectedIds.value), load, '批量结算已提交')
}

function handleReset() {
  reset({ distributorId: null, status: null })
}

onMounted(load)
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.search-form { margin-bottom: 8px; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
