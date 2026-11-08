<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="预占单号">
        <el-input placeholder="请输入预占单号" clearable />
      </el-form-item>
      <el-form-item label="预占状态">
        <el-select placeholder="全部" clearable>
          <el-option label="预占中" value="occupied" />
          <el-option label="已释放" value="released" />
          <el-option label="已转订单" value="converted" />
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
          <el-button link type="primary" size="small">释放</el-button>
          <el-button link type="primary" size="small">转订单</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="occupyNo" label="预占单号" width="180" />
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="occupyQty" label="预占数量" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="expireTime" label="过期时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
</script>
