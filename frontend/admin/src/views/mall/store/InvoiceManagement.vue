<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="发票号码">
        <el-input placeholder="请输入发票号码" clearable />
      </el-form-item>
      <el-form-item label="店铺名称">
        <el-input placeholder="请输入店铺名称" clearable />
      </el-form-item>
      <el-form-item label="发票类型">
        <el-select placeholder="全部类型" clearable>
          <el-option label="增值税专用发票" value="vat_special" />
          <el-option label="增值税普通发票" value="vat_normal" />
          <el-option label="电子发票" value="electronic" />
        </el-select>
      </el-form-item>
      <el-form-item label="开票状态">
        <el-select placeholder="全部状态" clearable>
          <el-option label="待开票" value="pending" />
          <el-option label="已开票" value="issued" />
          <el-option label="已作废" value="cancelled" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <div style="margin: 16px 0">
      <el-button type="primary" :icon="Plus">开具发票</el-button>
    </div>
    
    <el-table :data="[]" stripe>
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary" size="small">查看</el-button>
          <el-button link type="primary" size="small">打印</el-button>
          <el-button link type="danger" size="small">作废</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="invoiceNo" label="发票号码" width="180" />
      <el-table-column prop="storeName" label="店铺名称" width="150" />
      <el-table-column prop="invoiceType" label="发票类型" width="150" />
      <el-table-column prop="buyerName" label="购买方名称" min-width="200" />
      <el-table-column prop="invoiceAmount" label="开票金额 (元)" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'issued' ? 'success' : row.status === 'pending' ? 'warning' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="开票时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
</script>
