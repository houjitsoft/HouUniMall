<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="预置单号">
        <el-input placeholder="请输入预置单号" clearable />
      </el-form-item>
      <el-form-item label="预置状态">
        <el-select placeholder="全部" clearable>
          <el-option label="待审核" value="pending" />
          <el-option label="已通过" value="approved" />
          <el-option label="已拒绝" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="[]" stripe style="margin-top: 16px">
      <el-table-column label="操作" width="120">
        <template #default>
          <el-button link type="primary" size="small">审核</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="presetNo" label="预置单号" width="180" />
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="presetQty" label="预置数量" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
</script>
