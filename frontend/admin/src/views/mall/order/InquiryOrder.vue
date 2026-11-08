<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="询价单号">
        <el-input placeholder="请输入询价单号" clearable />
      </el-form-item>
      <el-form-item label="询价状态">
        <el-select placeholder="全部" clearable>
          <el-option label="待报价" value="pending" />
          <el-option label="已报价" value="quoted" />
          <el-option label="已成交" value="deal" />
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
          <el-button link type="primary" size="small">查看报价</el-button>
          <el-button link type="primary" size="small">成交</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="inquiryNo" label="询价单号" width="180" />
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="inquiryQty" label="询价数量" width="100" />
      <el-table-column prop="quoteCount" label="报价数量" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
</script>
