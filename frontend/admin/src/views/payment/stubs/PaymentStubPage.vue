<template>
  <!--
    支付中心占位骨架页
    用于快速搭建"规划中"功能的数据骨架，包含：
      - 面包屑/标题条
      - 搜索栏（按 searchFields 渲染）
      - 工具栏（新增 / 导出）
      - 表格（按 columns 渲染）+ 空数据提示
    真实业务接口接入前仅展示骨架，所有操作按钮均为占位。
  -->
  <div class="payment-stub-page">
    <div class="page-header">
      <div class="title-wrap">
        <h2 class="page-title">
          <el-icon class="title-icon" v-if="icon"><component :is="icon" /></el-icon>
          {{ title }}
        </h2>
        <div class="page-subtitle" v-if="desc">{{ desc }}</div>
      </div>
      <el-tag type="info" effect="plain" size="small">规划中 · 数据骨架</el-tag>
    </div>

    <!-- 搜索栏 -->
    <el-card v-if="searchFields && searchFields.length" shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item
          v-for="f in searchFields"
          :key="f.key"
          :label="f.label"
        >
          <el-date-picker
            v-if="f.type === 'daterange'"
            v-model="searchForm[f.key]"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px;"
          />
          <el-select
            v-else-if="f.type === 'select'"
            v-model="searchForm[f.key]"
            :placeholder="'请选择' + f.label"
            clearable
            style="width: 160px;"
          >
            <el-option
              v-for="opt in (f.options || [])"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-input
            v-else
            v-model="searchForm[f.key]"
            :placeholder="'请输入' + f.label"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleMockAction('search')">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ tableTitle || title + ' 列表' }}</span>
          <div class="header-actions">
            <el-button
              v-if="showCreate"
              type="primary"
              @click="handleMockAction('create')"
            >
              <el-icon><Plus /></el-icon>
              {{ createText }}
            </el-button>
            <el-button
              v-if="showImport"
              @click="handleMockAction('import')"
            >
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button
              v-if="showExport"
              @click="handleMockAction('export')"
            >
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="[]" stripe style="width: 100%;" empty-text="暂无数据（功能规划中）">
        <el-table-column
          v-for="col in columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :align="col.align || 'left'"
        />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default>
            <el-button link type="primary" size="small" @click="handleMockAction('detail')">查看</el-button>
            <el-button link type="primary" size="small" @click="handleMockAction('edit')">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleMockAction('delete')">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页占位 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="0"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 支付中心通用占位骨架页
 * Props：
 *  - title      页面标题（必填）
 *  - desc       页面副标题描述
 *  - icon       Element Plus icon 组件
 *  - tableTitle 列表区标题（默认 title + ' 列表'）
 *  - searchFields 搜索字段数组：{ key, label, type?, options? }
 *  - columns    表格列数组：{ prop, label, width?, minWidth?, align? }
 *  - showCreate/showImport/showExport 控制工具栏按钮显示
 *  - createText 新增按钮文案（如"开户""进件""发起分账"）
 */
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, Upload, Download } from '@element-plus/icons-vue'

const props = defineProps({
  title: { type: String, required: true },
  desc: { type: String, default: '' },
  icon: { type: [Object, Function, String], default: null },
  tableTitle: { type: String, default: '' },
  searchFields: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  showCreate: { type: Boolean, default: true },
  showImport: { type: Boolean, default: false },
  showExport: { type: Boolean, default: true },
  createText: { type: String, default: '新增' }
})

const searchForm = reactive({})
props.searchFields.forEach((f) => {
  searchForm[f.key] = f.type === 'daterange' ? [] : ''
})

const page = reactive({ current: 1, size: 10 })

function handleReset() {
  props.searchFields.forEach((f) => {
    searchForm[f.key] = f.type === 'daterange' ? [] : ''
  })
  ElMessage.info('已重置查询条件（骨架页）')
}

function handleMockAction(type) {
  const textMap = {
    search: '搜索',
    create: props.createText,
    import: '导入',
    export: '导出',
    detail: '查看详情',
    edit: '编辑',
    delete: '删除'
  }
  ElMessage.info(`「${props.title} · ${textMap[type] || type}」功能建设中`)
}
</script>

<style scoped>
.payment-stub-page {
  padding: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title-wrap .page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  color: #303133;
}

.title-wrap .title-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 22px;
}

.page-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.search-card {
  margin-bottom: 12px;
  border-radius: 8px;
}

.search-card :deep(.el-card__body) {
  padding: 14px 16px 0 16px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.table-card {
  border-radius: 8px;
}

.table-card :deep(.el-card__header) {
  padding: 12px 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.header-actions .el-button + .el-button {
  margin-left: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
