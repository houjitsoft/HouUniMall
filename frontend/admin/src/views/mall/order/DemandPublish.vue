<template>
  <el-card shadow="never">
    <el-form :inline="true">
      <el-form-item label="需求单号">
        <el-input placeholder="请输入需求单号" clearable />
      </el-form-item>
      <el-form-item label="发布状态">
        <el-select placeholder="全部" clearable>
          <el-option label="待发布" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="已下架" value="offline" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary">查询</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
    
    <div style="margin: 16px 0">
      <el-button type="primary" :icon="Plus">发布需求</el-button>
    </div>
    
    <el-table :data="[]" stripe>
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary" size="small">编辑</el-button>
          <el-button link type="primary" size="small">发布</el-button>
          <el-button link type="danger" size="small">下架</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="demandNo" label="需求单号" width="180" />
      <el-table-column prop="title" label="需求标题" min-width="200" />
      <el-table-column prop="category" label="商品类别" width="120" />
      <el-table-column prop="quantity" label="需求数量" width="100" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="createTime" label="发布时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
</script>
