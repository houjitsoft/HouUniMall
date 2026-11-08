<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="店铺名称">
        <el-input placeholder="请输入店铺名称" clearable />
      </el-form-item>
      <el-form-item label="质保金状态">
        <el-select placeholder="全部状态" clearable>
          <el-option label="未缴纳" value="unpaid" />
          <el-option label="已缴纳" value="paid" />
          <el-option label="部分缴纳" value="partial" />
          <el-option label="已退还" value="refunded" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-card shadow="never" style="margin: 16px 0; background: #f5f7fa">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-statistic title="质保金总额" :value="0" suffix="元" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已缴纳店铺" :value="0" suffix="家" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="待缴纳店铺" :value="0" suffix="家" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="退还中店铺" :value="0" suffix="家" />
        </el-col>
      </el-row>
    </el-card>
    
    <el-table :data="[]" stripe>
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary" size="small">查看明细</el-button>
          <el-button link type="primary" size="small">退还</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="storeName" label="店铺名称" min-width="200" />
      <el-table-column prop="merchantName" label="商家名称" width="150" />
      <el-table-column prop="requiredAmount" label="应缴金额 (元)" width="120" />
      <el-table-column prop="paidAmount" label="已缴金额 (元)" width="120" />
      <el-table-column prop="unpaidAmount" label="待缴金额 (元)" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'paid' ? 'success' : row.status === 'partial' ? 'warning' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payTime" label="缴纳时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
</script>

<style scoped lang="scss">
:deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
}
:deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: 600;
}
</style>
