<template>
  <div class="distribution-order">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>分销订单</span>
          <div class="header-actions">
            <span class="header-tip">订单完成后佣金才可结算；退款/取消请标记「失效」，避免佣金错发</span>
            <el-button :loading="loading" @click="load">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="query.orderNo" placeholder="按订单号搜索" clearable style="width: 190px" />
        </el-form-item>
        <el-form-item label="分销商ID">
          <el-input v-model="query.distributorId" placeholder="按分销商筛选" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="query.orderStatus" placeholder="全部" clearable style="width: 130px">
            <el-option label="待完成" :value="1" />
            <el-option label="已完成" :value="3" />
            <el-option label="已失效" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="170" show-overflow-tooltip />
        <el-table-column prop="distributorId" label="分销商ID" width="110" />
        <el-table-column prop="buyerId" label="买家ID" width="110" />
        <el-table-column label="订单金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column label="佣金金额" width="120" align="right">
          <template #default="{ row }">¥{{ money(row.commissionAmount) }}</template>
        </el-table-column>
        <el-table-column label="订单状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)">{{ statusText(row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" :disabled="Number(row.orderStatus) === 3" @click="changeStatus(row, 3)">
              标记完成
            </el-button>
            <el-button link type="warning" :disabled="Number(row.orderStatus) === 4" @click="changeStatus(row, 4)">
              标记失效
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无分销订单，买家通过分销链接下单后自动登记" />
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
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { distributionOrderApi } from '../api/distribution'
import { runAction, useTable } from '../utils/useTable'

const {
  loading, list, total, query, pageKey, sizeKey,
  load, search, reset, onCurrentChange, onSizeChange
} = useTable(distributionOrderApi.list, {
  defaultQuery: { orderNo: '', distributorId: null, orderStatus: null, shopId: null },
  pageKeys: ['page', 'size']
})

function money(value) {
  return Number(value || 0).toFixed(2)
}

function statusText(status) {
  return { 1: '待完成', 3: '已完成', 4: '已失效' }[Number(status)] || '—'
}

function statusType(status) {
  return { 1: 'warning', 3: 'success', 4: 'info' }[Number(status)] || 'info'
}

async function changeStatus(row, status) {
  const label = status === 3 ? '标记完成' : '标记失效'
  try {
    await ElMessageBox.confirm(
      status === 3
        ? `确认把订单「${row.orderNo || row.id}」标记为已完成？标记后佣金进入可结算。`
        : `确认把订单「${row.orderNo || row.id}」标记为已失效？该单佣金将不再结算。`,
      label,
      { type: 'warning' }
    )
  } catch {
    return
  }
  await runAction(() => distributionOrderApi.updateStatus({ id: row.id, orderStatus: status }), load, label + '成功')
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除分销订单「${row.orderNo || row.id}」？`, '删除分销订单', { type: 'warning' })
  } catch {
    return
  }
  await runAction(() => distributionOrderApi.remove(row.id), load, '已删除')
}

function handleReset() {
  reset({ orderNo: '', distributorId: null, orderStatus: null, shopId: null })
}

onMounted(load)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}
.header-tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.search-form {
  margin-bottom: 8px;
}
.pagination {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
