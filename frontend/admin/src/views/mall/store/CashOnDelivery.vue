<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="订单编号">
        <el-input placeholder="请输入订单编号" clearable />
      </el-form-item>
      <el-form-item label="支付状态">
        <el-select placeholder="全部状态" clearable>
          <el-option label="待支付" value="pending" />
          <el-option label="已支付" value="paid" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
      </el-form-item>
      <el-form-item label="收货状态">
        <el-select placeholder="全部状态" clearable>
          <el-option label="未收货" value="pending" />
          <el-option label="已收货" value="received" />
          <el-option label="已拒收" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="[]" stripe style="margin-top: 16px">
      <el-table-column label="操作" width="150">
        <template #default>
          <el-button link type="primary" size="small">查看详情</el-button>
          <el-button link type="primary" size="small">确认收货</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="orderNo" label="订单编号" width="180" />
      <el-table-column prop="storeName" label="店铺名称" width="150" />
      <el-table-column prop="buyerName" label="买家名称" width="150" />
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="orderAmount" label="订单金额 (元)" width="120" />
      <el-table-column prop="paymentStatus" label="支付状态" width="100" />
      <el-table-column prop="receiveStatus" label="收货状态" width="100" />
      <el-table-column prop="createTime" label="下单时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
</script>
