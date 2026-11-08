<template>
  <div class="category-page">
    <!-- 温馨提示 -->
    <el-card shadow="never" style="margin-bottom:16px">
      <el-alert
        type="warning"
        title="温馨提示：1. PC 首页左侧分类取自本页「显示中」的三级树（一级图标 + 二级名用 | 分隔，悬停展开三级）；2. 请为一级分类上传图标，否则前台显示灰色占位图标；3. 点击分类名称前符号可展开下级。"
        :closable="false"
        show-icon
      />
    </el-card>

    <el-card shadow="never">
      <!-- 操作按钮区 -->
      <div style="display:flex;gap:8px;margin-bottom:16px">
        <el-button type="primary" @click="handleAdd">新增分类</el-button>
        <el-button plain type="primary" @click="toggleExpand">
          {{ expandKeys.length ? '收起' : '展开' }}
        </el-button>
      </div>

      <!-- 分类树表格 -->
      <el-table
        v-loading="loading"
        :data="treeList"
        row-key="id"
        :expand-row-keys="expandKeys"
        :tree-props="{ children: 'sons' }"
        size="large"
        style="width:100%"
      >
        <el-table-column width="55" />
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column label="分类图片" min-width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              fit="contain"
              style="width:60px;height:45px"
              :preview-src-list="[row.image]"
              preview-teleported
            />
            <div v-else style="width:60px;height:45px;background:#f5f7fa;display:flex;align-items:center;justify-content:center;color:#c0c4cc;font-size:12px">无图</div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" min-width="80" />
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.isShow"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggleShow($event, row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDel(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <goods-category-edit
      v-if="showEdit"
      ref="editRef"
      @success="onEditSuccess"
      @close="showEdit = false"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  mallCategoryList, mallCategoryDel, mallCategoryStatus
} from '@/api/mall/goodsAdmin.js'
import GoodsCategoryEdit from './GoodsCategoryEdit.vue'

const loading = ref(false)
const treeList = ref([])
const expandKeys = ref([])
const showEdit = ref(false)
const editRef = ref(null)

/**
 * 获取分类树列表
 */
const fetchTree = async () => {
  loading.value = true
  try {
    const res = await mallCategoryList()
    treeList.value = (res && res.code === 1 && res.data) ? res.data : []
    expandKeys.value = []
  } catch (e) {
    treeList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 展开/收起所有分类节点
 */
const toggleExpand = () => {
  if (expandKeys.value.length) {
    expandKeys.value = []
  } else {
    const keys = []
    const collect = (list) => {
      list.forEach(item => {
        keys.push(String(item.id))
        if (item.sons?.length) collect(item.sons)
      })
    }
    collect(treeList.value)
    expandKeys.value = keys
  }
}

/**
 * 打开新增弹窗
 */
const handleAdd = async () => {
  showEdit.value = true
  await nextTick()
  editRef.value?.open('add')
}

/**
 * 打开编辑弹窗
 * @param {Object} row - 当前分类行数据
 */
const handleEdit = async (row) => {
  showEdit.value = true
  await nextTick()
  editRef.value?.open('edit', row)
}

/**
 * 编辑/新增成功回调
 */
const onEditSuccess = () => {
  fetchTree()
}

/**
 * 切换分类显示状态
 * @param {number} val - 新状态值 0|1
 * @param {Object} row - 当前行数据
 */
const handleToggleShow = async (val, row) => {
  try {
    const res = await mallCategoryStatus({ id: row.id, isShow: val })
    if (res && res.code === 1) {
      row.isShow = val
    } else {
      ElMessage.error(res?.message || '更新失败')
    }
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

/**
 * 删除分类
 * @param {Object} row - 当前行数据
 */
const handleDel = async (row) => {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '提示', { type: 'warning' })
  const res = await mallCategoryDel({ id: row.id })
  if (res && res.code === 1) {
    ElMessage.success('已删除')
    fetchTree()
  } else {
    ElMessage.error(res?.message || '删除失败')
  }
}

onMounted(fetchTree)
</script>

<style scoped>
.category-page { padding: 20px; }
</style>
