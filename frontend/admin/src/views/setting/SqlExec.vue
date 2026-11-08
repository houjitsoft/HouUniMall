<template>
  <div class="sql-exec-page">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>查询执行</span>
        </div>
      </template>
      <DatabaseExecute />
    </el-card>
  </div>
</template>

<script setup>
import DatabaseExecute from './DatabaseExecute.vue'
</script>

<style scoped>
.sql-exec-page {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
