<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="订款单号">
        <el-input placeholder="请输入订款单号" clearable />
      </el-form-item>
      <el-form-item label="客户名称">
        <el-input placeholder="请输入客户名称" clearable />
      </el-form-item>
      <el-form-item label="支付状态">
        <el-select placeholder="全部" clearable>
          <el-option label="待支付" value="pending" />
          <el-option label="已支付" value="paid" />
          <el-option label="部分支付" value="partial" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <div style="margin: 16px 0">
      <el-button type="primary" :icon="Plus">新增订款</el-button>
    </div>
    
    <el-table :data="[]" stripe>
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary" size="small">查看</el-button>
          <el-button link type="primary" size="small">确认</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="depositNo" label="订款单号" width="180" />
      <el-table-column prop="customerName" label="客户名称" width="150" />
      <el-table-column prop="depositAmount" label="订款金额 (元)" width="120" />
      <el-table-column prop="paidAmount" label="已付金额 (元)" width="120" />
      <el-table-column prop="paymentMethod" label="支付方式" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
</script>
