<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">特色馆装修</span>
          <el-button type="primary" @click="handleAddSpecial">创建特色馆</el-button>
        </div>
      </template>

      <el-form :inline="true">
        <el-form-item label="特色馆名称">
          <el-input placeholder="请输入特色馆名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select placeholder="全部状态" clearable>
            <el-option label="已发布" value="1" />
            <el-option label="草稿" value="0" />
            <el-option label="已关闭" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">查询</el-button>
          <el-button>重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="specialList" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="specialName" label="特色馆名称" min-width="200" />
        <el-table-column prop="specialType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.specialType === 'brand' ? 'primary' : 'success'">
              {{ row.specialType === 'brand' ? '品牌馆' : '主题馆' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coverImage" label="封面图" width="100">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverImage"
              :src="row.coverImage" 
              style="width: 60px; height: 60px"
              fit="cover"
              :preview-src-list="[row.coverImage]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="goodsCount" label="商品数量" width="100" />
        <el-table-column prop="visitCount" label="访问量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'danger'">
              {{ row.status === 1 ? '已发布' : row.status === 0 ? '草稿' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleDecorate(row)">装修</el-button>
            <el-button link type="success" size="small" @click="handleGoods(row)">商品管理</el-button>
            <el-button link type="warning" size="small" @click="handlePublish(row)">
              {{ row.status === 1 ? '下架' : '发布' }}
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; text-align: center; color: #909399">暂无数据</div>

      <!-- 装修对话框 -->
      <el-dialog v-model="decorateVisible" title="特色馆装修" width="90%" :close-on-click-modal="false">
        <el-row :gutter="20">
          <!-- 左侧：组件库 -->
          <el-col :span="6">
            <el-card shadow="never" header="组件库">
              <el-collapse accordion>
                <el-collapse-item title="基础组件" name="1">
                  <div class="component-item" v-for="comp in basicComponents" :key="comp.id">
                    <span class="comp-icon">{{ comp.icon }}</span>
                    <span class="comp-name">{{ comp.name }}</span>
                  </div>
                </el-collapse-item>
                <el-collapse-item title="商品组件" name="2">
                  <div class="component-item" v-for="comp in goodsComponents" :key="comp.id">
                    <span class="comp-icon">{{ comp.icon }}</span>
                    <span class="comp-name">{{ comp.name }}</span>
                  </div>
                </el-collapse-item>
                <el-collapse-item title="营销组件" name="3">
                  <div class="component-item" v-for="comp in marketingComponents" :key="comp.id">
                    <span class="comp-icon">{{ comp.icon }}</span>
                    <span class="comp-name">{{ comp.name }}</span>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </el-card>
          </el-col>

          <!-- 中间：预览区域 -->
          <el-col :span="12">
            <el-card shadow="never" header="页面预览">
              <div class="preview-area">
                <div class="preview-content">
                  <el-empty description="拖拽组件到此处开始装修" />
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：属性设置 -->
          <el-col :span="6">
            <el-card shadow="never" header="属性设置">
              <el-form label-width="80px">
                <el-form-item label="页面标题">
                  <el-input placeholder="请输入标题" />
                </el-form-item>
                <el-form-item label="页面背景">
                  <el-color-picker />
                </el-form-item>
                <el-form-item label="主题色">
                  <el-color-picker />
                </el-form-item>
                <el-form-item label="显示头部">
                  <el-switch />
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>

        <template #footer>
          <el-button @click="decorateVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveDecorate">保存</el-button>
          <el-button type="success" @click="handlePublishDecorate">保存并发布</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const specialList = ref([])
const decorateVisible = ref(false)

const basicComponents = ref([
  { id: 1, name: '轮播图', icon: '🖼️' },
  { id: 2, name: '图片热区', icon: '🌄' },
  { id: 3, name: '视频', icon: '📹' },
  { id: 4, name: '富文本', icon: '📝' },
  { id: 5, name: '分割线', icon: '➖' },
  { id: 6, name: '空白占位', icon: '⬜' }
])

const goodsComponents = ref([
  { id: 1, name: '商品列表', icon: '📦' },
  { id: 2, name: '商品分组', icon: '📂' },
  { id: 3, name: '商品搜索', icon: '🔍' },
  { id: 4, name: '商品分类', icon: '🗂️' }
])

const marketingComponents = ref([
  { id: 1, name: '限时秒杀', icon: '⏰' },
  { id: 2, name: '优惠券', icon: '🎫' },
  { id: 3, name: '满减活动', icon: '💰' },
  { id: 4, name: '倒计时', icon: '⏱️' }
])

const handleAddSpecial = () => {
  ElMessage.info('打开创建特色馆弹窗')
}

const handleEdit = (row) => {
  ElMessage.info('编辑特色馆信息')
}

const handleDecorate = (row) => {
  decorateVisible.value = true
}

const handleGoods = (row) => {
  ElMessage.info('管理特色馆商品')
}

const handlePublish = (row) => {
  ElMessage.success(row.status === 1 ? '下架成功' : '发布成功')
}

const handleDelete = (row) => {
  ElMessage.success('删除成功')
}

const handleSaveDecorate = () => {
  ElMessage.success('保存成功')
  decorateVisible.value = false
}

const handlePublishDecorate = () => {
  ElMessage.success('保存并发布成功')
  decorateVisible.value = false
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

.component-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: move;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}

.component-item:hover {
  background: #e5e5e5;
  transform: translateX(4px);
}

.comp-icon {
  font-size: 20px;
  margin-right: 8px;
}

.comp-name {
  font-size: 14px;
}

.preview-area {
  min-height: 600px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  background: #fafafa;
}

.preview-content {
  padding: 20px;
  min-height: 560px;
}
</style>
