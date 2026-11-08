<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="商品名称">
        <el-input placeholder="请输入商品名称" clearable />
      </el-form-item>
      <el-form-item label="推荐类型">
        <el-select placeholder="全部" clearable>
          <el-option label="热销推荐" value="hot" />
          <el-option label="新品推荐" value="new" />
          <el-option label="精选推荐" value="featured" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <div style="margin: 16px 0">
      <el-button type="primary" :icon="Plus">添加推荐</el-button>
    </div>
    
    <el-table :data="[]" stripe>
      <el-table-column label="操作" width="150">
        <template #default>
          <el-button link type="primary" size="small">编辑</el-button>
          <el-button link type="danger" size="small">删除</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="goodsName" label="商品名称" min-width="200" />
      <el-table-column prop="recommendType" label="推荐类型" width="120" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
</script>
