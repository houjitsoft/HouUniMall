<template>
  <el-card shadow="never">
    <div style="margin-bottom: 16px">
      <el-button type="primary" :icon="Plus">新增等级</el-button>
    </div>
    
    <el-table :data="[]" stripe>
      <el-table-column label="操作" width="180">
        <template #default>
          <el-button link type="primary" size="small">编辑</el-button>
          <el-button link type="primary" size="small">查看权益</el-button>
          <el-button link type="danger" size="small">删除</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="levelName" label="等级名称" width="150" />
      <el-table-column prop="levelCode" label="等级编码" width="120" />
      <el-table-column prop="salesRequirement" label="销售额要求 (元)" width="150" />
      <el-table-column prop="commissionRate" label="佣金比例 (%)" width="120" />
      <el-table-column prop="privileges" label="特殊权益" min-width="250" />
      <el-table-column prop="storeCount" label="店铺数量" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'info'">
            {{ row.status === 'enabled' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
  </el-card>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
</script>
