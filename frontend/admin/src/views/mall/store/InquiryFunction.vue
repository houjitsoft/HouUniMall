<template>
  <div class="article-lists">
    <el-card class="!border-none" shadow="never">
      <el-alert
        type="warning"
        title="温馨提示：平台可根据主营类目便捷管理所有商家。主营类目只提供一级分类。"
        :closable="false"
        show-icon
      />
    </el-card>

    <el-card class="!border-none mt-4" shadow="never">
      <div class="flex mb-4">
        <el-button type="primary" @click="handleAdd">
          + 新增类目
        </el-button>
      </div>

      <el-table size="large" v-loading="loading" :data="list">
        <el-table-column width="55" />
        <el-table-column label="分类图标" min-width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              style="width: 60px; height: 45px"
              fit="contain"
              :preview-src-list="[row.image]"
              :preview-teleported="true"
            />
            <div v-else style="width: 60px; height: 45px; background: #f5f5f5; display: flex; align-items: center; justify-content: center; color: #ccc; font-size: 12px">无图</div>
          </template>
        </el-table-column>
        <el-table-column property="name" label="分类名称" max-width="266" />
        <el-table-column property="shop_nums" label="店铺数量" max-width="266" />
        <el-table-column label="排序" prop="sort" min-width="100" />
        <el-table-column label="状态" max-width="220">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleChangeStatus($event, row.id)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="create_time" min-width="120" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 15, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <ShopCategoryEdit
      v-if="showEdit"
      ref="editRef"
      @success="handleSuccess"
      @close="showEdit = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import ShopCategoryEdit from './ShopCategoryEdit.vue'

/** 加载状态 */
const loading = ref(false)
/** 列表数据 */
const list = ref([])
/** 编辑弹窗 */
const showEdit = ref(false)
const editRef = ref(null)

/** 分页信息 */
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })

/**
 * 获取经营类目列表数据
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/web/mall/admin/shop/category/lists', {
      params: { page_no: pagination.pageNo, page_size: pagination.pageSize }
    })
    if (res && res.code === 1 && res.data) {
      list.value = res.data.lists || []
      pagination.total = res.data.count || 0
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 成功回调，刷新列表
 */
const handleSuccess = () => {
  fetchList()
}

/**
 * 新增类目
 */
const handleAdd = async () => {
  showEdit.value = true
  await nextTick()
  editRef.value?.open('add')
}

/**
 * 编辑类目
 * @param {object} row - 当前分类行
 */
const handleEdit = async (row) => {
  showEdit.value = true
  await nextTick()
  editRef.value?.open('edit')
  editRef.value?.getDetail(row)
}

/**
 * 切换类目状态
 * @param {number} status - 状态值 1/0
 * @param {number} id - 分类ID
 */
const handleChangeStatus = async (status, id) => {
  try {
    const res = await request.post('/web/mall/admin/shop/category/status', { id, status })
    if (res && res.code === 1) {
      ElMessage.success('状态已更新')
    } else {
      ElMessage.error(res?.message || '操作失败')
      fetchList()
    }
  } catch (e) {
    fetchList()
  }
}

/**
 * 删除类目
 * @param {number} id - 分类ID
 */
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除？', '提示', { type: 'warning' })
    const res = await request.post('/web/mall/admin/shop/category/del', { id })
    if (res && res.code === 1) {
      ElMessage.success('删除成功')
      fetchList()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (e) {
    // 取消操作
  }
}

onMounted(fetchList)
</script>
