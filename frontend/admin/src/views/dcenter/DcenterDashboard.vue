<template>
  <div class="dcenter-dashboard">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>数据中心</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="data-card">
            <div class="data-title">数据总量</div>
            <div class="data-value">1,234,567</div>
            <div class="data-unit">条记录</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="data-card">
            <div class="data-title">存储空间</div>
            <div class="data-value">256</div>
            <div class="data-unit">GB</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="data-card">
            <div class="data-title">备份状态</div>
            <div class="data-value">正常</div>
            <div class="data-unit">运行中</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
// 数据中心仪表盘
</script>

<style scoped>
.dcenter-dashboard {
  padding: 20px;
}

.data-card {
  text-align: center;
  padding: 30px 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.data-title {
  font-size: 16px;
  color: #666;
  margin-bottom: 15px;
}

.data-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.data-unit {
  font-size: 14px;
  color: #999;
}
</style>
