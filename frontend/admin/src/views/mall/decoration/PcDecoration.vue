<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">电脑端装修</span>
          <el-button type="primary" @click="handlePreview">预览</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="card">
        <!-- 页面列表 -->
        <el-tab-pane label="页面管理" name="pages">
          <div style="margin-bottom: 16px">
            <el-button type="primary" :icon="Plus" @click="handleAddPage">新建页面</el-button>
          </div>

          <el-table :data="pageList" stripe>
            <el-table-column prop="pageName" label="页面名称" min-width="150" />
            <el-table-column prop="pageType" label="页面类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.pageType === 'home' ? 'primary' : 'info'">
                  {{ row.pageType === 'home' ? '首页' : '自定义页面' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isDefault" label="默认页面" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault" type="warning">默认</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" />
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="primary" size="small" @click="handleDesign(row)">装修</el-button>
                <el-button link type="success" size="small" @click="handlePublish(row)">发布</el-button>
                <el-button link type="warning" size="small" @click="handleCopy(row)">复制</el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>
        </el-tab-pane>

        <!-- 组件库 -->
        <el-tab-pane label="组件库" name="components">
          <el-row :gutter="16">
            <el-col :span="6" v-for="comp in componentList" :key="comp.id">
              <el-card shadow="hover" class="component-card">
                <div class="component-icon">{{ comp.icon }}</div>
                <div class="component-name">{{ comp.name }}</div>
                <div class="component-desc">{{ comp.description }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 全局设置 -->
        <el-tab-pane label="全局设置" name="global">
          <el-form :model="globalSettings" label-width="140px">
            <el-form-item label="顶部背景色">
              <el-color-picker v-model="globalSettings.headerBgColor" />
            </el-form-item>
            <el-form-item label="导航背景色">
              <el-color-picker v-model="globalSettings.navBgColor" />
            </el-form-item>
            <el-form-item label="主题色">
              <el-color-picker v-model="globalSettings.primaryColor" />
            </el-form-item>
            <el-form-item label="页面背景色">
              <el-color-picker v-model="globalSettings.pageBgColor" />
            </el-form-item>
            <el-form-item label="页面宽度">
              <el-radio-group v-model="globalSettings.pageWidth">
                <el-radio label="1200">1200px</el-radio>
                <el-radio label="1400">1400px</el-radio>
                <el-radio label="1600">1600px</el-radio>
                <el-radio label="100%">全屏</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveGlobalSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const activeTab = ref('pages')

const pageList = ref([])

const componentList = ref([
  { id: 1, name: '轮播图', icon: '🖼️', description: '图片轮播展示' },
  { id: 2, name: '商品列表', icon: '📦', description: '商品列表展示' },
  { id: 3, name: '分类导航', icon: '🗂️', description: '商品分类导航' },
  { id: 4, name: '广告图', icon: '📢', description: '广告图片展示' },
  { id: 5, name: '公告栏', icon: '📣', description: '公告信息展示' },
  { id: 6, name: '品牌墙', icon: '🏆', description: '品牌展示' },
  { id: 7, name: '自定义文本', icon: '📝', description: '自定义文本内容' },
  { id: 8, name: '视频播放', icon: '🎬', description: '视频播放器' }
])

const globalSettings = ref({
  headerBgColor: '#ffffff',
  navBgColor: '#333333',
  primaryColor: '#409EFF',
  pageBgColor: '#f5f5f5',
  pageWidth: '1200'
})

const handleAddPage = () => {
  ElMessage.info('打开新建页面弹窗')
}

const handleEdit = (row) => {
  ElMessage.info('编辑页面信息')
}

const handleDesign = (row) => {
  ElMessage.info('进入装修编辑器')
}

const handlePublish = (row) => {
  ElMessage.success('页面发布成功')
}

const handleCopy = (row) => {
  ElMessage.success('页面复制成功')
}

const handleDelete = (row) => {
  ElMessage.success('页面删除成功')
}

const handlePreview = () => {
  ElMessage.info('打开预览窗口')
}

const saveGlobalSettings = () => {
  ElMessage.success('全局设置保存成功')
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.component-card {
  margin-bottom: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.component-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.component-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.component-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.component-desc {
  font-size: 12px;
  color: #909399;
}
</style>
