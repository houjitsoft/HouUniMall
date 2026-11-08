<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">店铺装修</span>
          <el-button type="primary" @click="handleSave">保存装修</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane label="首页装修" name="home">
          <el-form :model="decorationForm" label-width="120px">
            <el-form-item label="店铺Banner">
              <el-upload
                class="upload-demo"
                action="#"
                :limit="3"
                list-type="picture-card"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="tips">建议尺寸：1920*600px，最多3张</div>
            </el-form-item>
            
            <el-form-item label="店铺Logo">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
              >
                <img v-if="decorationForm.logo" :src="decorationForm.logo" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="tips">建议尺寸：200*200px</div>
            </el-form-item>

            <el-form-item label="店铺名称">
              <el-input v-model="decorationForm.name" placeholder="请输入店铺名称" />
            </el-form-item>

            <el-form-item label="店铺简介">
              <el-input
                v-model="decorationForm.description"
                type="textarea"
                :rows="3"
                placeholder="请输入店铺简介"
              />
            </el-form-item>

            <el-form-item label="主题颜色">
              <el-color-picker v-model="decorationForm.themeColor" />
            </el-form-item>

            <el-form-item label="显示客服">
              <el-switch v-model="decorationForm.showService" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="商品分类" name="category">
          <el-tree :data="categoryData" :props="{ label: 'name', children: 'children' }" default-expand-all>
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
                <span>
                  <el-button text type="primary" size="small" @click="handleEditCategory(data)">编辑</el-button>
                  <el-button text type="danger" size="small" @click="handleDeleteCategory(data)">删除</el-button>
                </span>
              </span>
            </template>
          </el-tree>
          <el-button type="primary" class="mt-20" @click="handleAddCategory">添加分类</el-button>
        </el-tab-pane>

        <el-tab-pane label="推荐商品" name="recommend">
          <el-button type="primary" @click="handleSelectGoods">选择商品</el-button>
          <el-table :data="recommendGoods" class="mt-20" border>
            <el-table-column prop="id" label="商品ID" width="100" />
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="price" label="价格" width="100" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button text type="danger" size="small" @click="handleRemoveGoods(row)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const activeTab = ref('home')

const decorationForm = reactive({
  logo: '',
  name: '我的店铺',
  description: '欢迎光临我的店铺',
  themeColor: '#409EFF',
  showService: true
})

const categoryData = ref([
  {
    name: '数码产品',
    children: [
      { name: '手机' },
      { name: '电脑' }
    ]
  },
  {
    name: '服装鞋帽',
    children: [
      { name: '男装' },
      { name: '女装' }
    ]
  }
])

const recommendGoods = ref([
  { id: 1, name: 'iPhone 15 Pro', price: 7999 },
  { id: 2, name: 'MacBook Pro', price: 12999 }
])

const handleSave = () => {
  ElMessage.success('保存成功')
}

const handleEditCategory = (data) => {
  ElMessage.info('编辑分类：' + data.name)
}

const handleDeleteCategory = (data) => {
  ElMessage.success('删除分类：' + data.name)
}

const handleAddCategory = () => {
  ElMessage.info('添加分类')
}

const handleSelectGoods = () => {
  ElMessage.info('选择商品')
}

const handleRemoveGoods = (row) => {
  const index = recommendGoods.value.findIndex(item => item.id === row.id)
  if (index > -1) {
    recommendGoods.value.splice(index, 1)
    ElMessage.success('移除成功')
  }
}
</script>

<style scoped>
.page-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-weight: bold; font-size: 16px; }
.tabs { margin-top: 20px; }
.tips { color: #999; font-size: 12px; margin-top: 5px; }
.mt-20 { margin-top: 20px; }
.avatar-uploader .avatar { width: 178px; height: 178px; display: block; }
.avatar-uploader .el-upload { border: 1px dashed var(--el-border-color); border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; transition: var(--el-transition-duration-fast); }
.avatar-uploader .el-upload:hover { border-color: var(--el-color-primary); }
.el-icon.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 178px; height: 178px; text-align: center; }
.custom-tree-node { flex: 1; display: flex; align-items: center; justify-content: space-between; font-size: 14px; padding-right: 8px; }
</style>
