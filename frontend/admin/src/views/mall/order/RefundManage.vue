<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="退款单号">
        <el-input placeholder="请输入退款单号" clearable />
      </el-form-item>
      <el-form-item label="订单编号">
        <el-input placeholder="请输入订单编号" clearable />
      </el-form-item>
      <el-form-item label="退款状态">
        <el-select placeholder="全部" clearable>
          <el-option label="待审核" value="pending" />
          <el-option label="退款中" value="processing" />
          <el-option label="已退款" value="refunded" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="[]" stripe style="margin-top: 16px">
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary" size="small">查看详情</el-button>
          <el-button link type="primary" size="small">审核</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="refundNo" label="退款单号" width="180" />
      <el-table-column prop="orderNo" label="订单编号" width="180" />
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="refundAmount" label="退款金额 (元)" width="120" />
      <el-table-column prop="refundReason" label="退款原因" min-width="200" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="申请时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
</script>
