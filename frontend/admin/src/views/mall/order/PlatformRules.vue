<template>
  <el-card shadow="never">
    <div style="margin-bottom: 16px">
      <el-button type="primary" :icon="Plus">新增规范</el-button>
    </div>

    <el-table :data="[]" stripe>
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary" size="small">编辑</el-button>
          <el-button link type="primary" size="small">查看</el-button>
          <el-button link type="danger" size="small">删除</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="ruleNo" label="规范编号" width="150" />
      <el-table-column prop="title" label="规范标题" min-width="250" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '生效' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
</script>
