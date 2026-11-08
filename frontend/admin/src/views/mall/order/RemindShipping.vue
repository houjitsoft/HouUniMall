<template>
  <el-card shadow="never">
    <el-form :inline="true" class="search-form">
      <el-form-item label="订单编号">
        <el-input placeholder="请输入订单编号" clearable />
      </el-form-item>
      <el-form-item label="买家名称">
        <el-input placeholder="请输入买家名称" clearable />
      </el-form-item>
      <el-form-item label="下单时间">
        <el-date-picker type="daterange" range-separator="-" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="[]" stripe style="margin-top: 16px">
      <el-table-column label="操作" width="180" fixed="left">
        <template #default>
          <el-button link type="primary" size="small">提醒发货</el-button>
          <el-button link type="primary" size="small">查看详情</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="orderNo" label="订单编号" width="180" />
      <el-table-column prop="buyerName" label="买家名称" width="150" />
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="orderAmount" label="订单金额 (元)" width="120" />
      <el-table-column prop="paymentTime" label="支付时间" width="180" />
      <el-table-column prop="waitDays" label="等待天数" width="100" />
      <el-table-column prop="remindCount" label="提醒次数" width="100" />
      <el-table-column prop="lastRemindTime" label="最后提醒时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无待发货订单</div>
  </el-card>
</template>

<script setup>
</script>

<style scoped lang="scss">
.search-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style>
