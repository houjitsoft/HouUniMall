<template>
  <div class="menu-lists">
    <el-card class="!border-none" shadow="never">
      <div>
        <el-button type="primary" @click="handleAdd()">
          + 新增
        </el-button>
        <el-button @click="handleExpand">展开/折叠</el-button>
      </div>

      <el-table
        ref="tableRef"
        class="mt-4"
        size="large"
        :data="menuList"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
      >
        <el-table-column label="菜单名称" prop="name" min-width="150" show-overflow-tooltip />
        <el-table-column label="类型" prop="type" min-width="80">
          <template #default="{ row }">
            <div v-if="row.type === 0">目录</div>
            <div v-else-if="row.type === 1">菜单</div>
            <div v-else-if="row.type === 2">按钮</div>
          </template>
        </el-table-column>
        <el-table-column label="图标" prop="icon" min-width="80">
          <template #default="{ row }">
            <div>{{ row.icon || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="权限标识" prop="access" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" min-width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status == 1">正常</el-tag>
            <el-tag v-else type="danger">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" min-width="100" />
        <el-table-column label="更新时间" prop="update_time" min-width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.type !== 2"
              type="primary"
              link
              @click="handleAdd(row.id)"
            >新增</el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row.id)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <ShopMenuEdit
      v-if="showEdit"
      ref="editRef"
      @success="fetchList"
      @close="showEdit = false"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import ShopMenuEdit from './ShopMenuEdit.vue'

/** 加载状态 */
const loading = ref(false)
/** 菜单树数据 */
const menuList = ref([])
/** 表格ref（用于展开/折叠） */
const tableRef = ref(null)
/** 是否全部展开 */
let isExpand = false
/** 编辑弹窗 */
const showEdit = ref(false)
const editRef = ref(null)

/**
 * 获取商家菜单权限树
 */
const fetchList = async () => {
  loading.value = true
  try {
    const data = await request.get('/web/mall/admin/shop/menu/lists', { params: { page_type: 0 } })
    menuList.value = Array.isArray(data) ? data : []
  } catch (e) {
    menuList.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 新增菜单
 * @param {number|undefined} pid - 父级菜单ID（可选）
 */
const handleAdd = async (pid) => {
  showEdit.value = true
  await nextTick()
  if (pid) {
    editRef.value?.setFormData({ pid })
  }
  editRef.value?.open('add')
}

/**
 * 编辑菜单
 * @param {object} row - 当前菜单行
 */
const handleEdit = async (row) => {
  showEdit.value = true
  await nextTick()
  editRef.value?.open('edit')
  editRef.value?.getDetail(row)
}

/**
 * 删除菜单
 * @param {number} id - 菜单ID
 */
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除？', '提示', { type: 'warning' })
    const res = await request.post('/web/mall/admin/shop/menu/delete', { id })
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

/**
 * 展开/折叠所有行
 */
const handleExpand = () => {
  isExpand = !isExpand
  toggleExpand(menuList.value, isExpand)
}

/**
 * 递归展开/折叠节点
 * @param {Array} children - 子节点数组
 * @param {boolean} unfold - 是否展开
 */
const toggleExpand = (children, unfold = true) => {
  for (const key in children) {
    tableRef.value?.toggleRowExpansion(children[key], unfold)
    if (children[key].children) {
      toggleExpand(children[key].children, unfold)
    }
  }
}

onMounted(fetchList)
</script>
