<template>
  <el-card class="page-list-card" shadow="never">
<template #header>
        <div class="card-header">
          <div>
            <span class="title">{{ pageTitle }}</span>
            <p class="subtitle">管理 {{ platformLabel }} {{ pageTitle }}。设为首页后，买家打开商城首页会加载该页组件。</p>
          </div>
          <el-button type="primary" @click="handleAdd">新增页面</el-button>
        </div>
      </template>
    <div class="page-list-toolbar flex justify-between items-center">
      <el-form ref="formRef" :model="queryParams" class="page-list-search flex items-center" @submit.prevent="handleSearch">
        <el-form-item label="页面名称">
          <el-input
            class="w-280"
            v-model="queryParams.name"
            placeholder="请输入页面名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="hc-table-wrap mt-6">
    <el-table
      size="large"
      v-loading="loading"
      :data="pageList"
    >
      <el-table-column label="页面名称" prop="name" min-width="140">
        <template #default="{ row }">
          <span class="mr-2">{{ row.name }}</span>
          <el-tag type="success" v-if="Number(row.is_home) === 1">首页</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="update_time" min-width="140" />
      <el-table-column label="创建时间" prop="create_time" min-width="140" />
      <template #empty>
        <el-empty description="还没有首页微页面，点击右上角新增，或保存后自动生成默认首页" />
      </template>
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            v-if="Number(row.is_home) !== 1"
            @click="handleSetHome(row.id)"
          >设为首页</el-button>
          <el-button type="primary" link @click="handleEdit(row.id)">装修</el-button>
          <el-button
            type="primary"
            link
            v-if="Number(row.is_home) !== 1"
            @click="handleDel(row.id)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <div class="pagination-container flex justify-end mt-4">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.limit"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getLists"
        @current-change="getLists"
      />
    </div>
  </el-card>

  <!-- 新增页面弹窗 -->
  <el-dialog
    v-model="dialogVisible"
    title="新增页面"
    width="400px"
    :close-on-click-modal="false"
  >
    <el-form :model="addForm" label-width="80px">
      <el-form-item label="页面名称">
        <el-input
          v-model="addForm.name"
          placeholder="请输入页面名称"
          maxlength="20"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmAdd">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHomeList, addPage, deletePage, setHomePage } from '@/api/mall/decoration'
import { unwrapDecorate, unwrapPageList } from '@/utils/decoratePayload'

const router = useRouter()
const route = useRoute()

/**
 * 当前装修端：pc | mobile，由路由 meta.platform 决定。
 * PC 与移动共用本组件，仅标题与提示语不同。
 */
const isPc = computed(() => (route.meta?.platform || route.query?.platform) === 'pc')
const platformLabel = computed(() => (isPc.value ? 'PC 端' : 'H5 / 小程序'))
const pageTitle = computed(() => (isPc.value ? 'PC 首页装修' : '首页装修'))
/** 对应端的页面编辑器路由（PC 与移动端编辑器是两条独立路由） */
const editorPath = computed(() =>
  isPc.value ? '/mall/decoration/pc/index' : '/mall/decoration/pages/index'
)

/**
 * 加载状态
 * @type {import('vue').Ref<boolean>}
 */
/**
 * 定义组件事件
 */
const emit = defineEmits(['editPage'])

/**
 * 加载状态
 * @type {import('vue').Ref<boolean>}
 */
const loading = ref(false)

/**
 * 页面列表数据
 * @type {import('vue').Ref<Array<Object>>}
 */
const pageList = ref([])

/**
 * 总记录数
 * @type {import('vue').Ref<number>}
 */
const total = ref(0)

/**
 * 查询参数
 * @type {{ name: string, page: number, limit: number }}
 */
const queryParams = reactive({
  name: '',
  page: 1,
  limit: 10
})

/**
 * 新增弹窗显示状态
 * @type {import('vue').Ref<boolean>}
 */
const dialogVisible = ref(false)

/**
 * 新增表单数据
 * @type {{ name: string }}
 */
const addForm = reactive({
  name: ''
})

/**
 * 获取页面列表
 */
const getLists = async () => {
  loading.value = true
  try {
    const { lists, count } = unwrapPageList(
      await getHomeList({ ...queryParams, pagePlatform: isPc.value ? 'pc' : 'mobile' })
    )
    pageList.value = lists
    total.value = count
  } catch (error) {
    console.error('获取页面列表失败', error)
    ElMessage.error('获取页面列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索重置页码并查询
 */
const handleSearch = () => {
  queryParams.page = 1
  getLists()
}

/**
 * 打开新增页面弹窗
 */
const handleAdd = () => {
  addForm.name = ''
  dialogVisible.value = true
}

/**
 * 确认新增页面
 */
const confirmAdd = async () => {
  if (!addForm.name.trim()) {
    ElMessage.warning('请输入页面名称')
    return
  }
  try {
    const created = unwrapDecorate(
      await addPage({
        name: addForm.name.trim(),
        type: 1,
        pagePlatform: isPc.value ? 'pc' : 'mobile'
      })
    )
    const newId = created?.id ?? created
    ElMessage.success('新增成功')
    dialogVisible.value = false
    if (newId) {
      router.push(`${editorPath.value}?id=${newId}`)
      return
    }
    getLists()
  } catch (error) {
    console.error('新增页面失败', error)
    ElMessage.error('新增失败')
  }
}

/**
 * 编辑页面（跳转到对应端的页面编辑器）
 * @param {number} id - 页面ID
 */
const handleEdit = (id) => {
  router.push(`${editorPath.value}?id=${id}`)
  emit('editPage', id)
}

/**
 * 删除页面
 * @param {number} id - 页面ID
 */
const handleDel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该页面吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePage({ id })
    ElMessage.success('删除成功')
    getLists()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除页面失败', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 设为首页
 * @param {number} id - 页面ID
 */
const handleSetHome = async (id) => {
  try {
    await setHomePage({ id, pagePlatform: isPc.value ? 'pc' : 'mobile' })
    ElMessage.success('设置成功')
    getLists()
  } catch (error) {
    console.error('设为首页失败', error)
    ElMessage.error('设置失败')
  }
}

onMounted(() => {
  getLists()
})
</script>

<style scoped>
.page-list-card {
  min-height: calc(100vh - 116px);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
}

.card-header .subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
  font-weight: 400;
}

.page-list-toolbar {
  gap: 12px;
}

.page-list-search {
  flex: 1;
  min-width: 0;
  gap: 8px;
}

.flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.items-center {
  align-items: center;
}

.justify-end {
  justify-content: flex-end;
}

.mt-6 {
  margin-top: 24px;
}

.mt-4 {
  margin-top: 16px;
}

.mr-2 {
  margin-right: 8px;
}

.w-280 {
  width: 280px;
}

@media (max-width: 991px) {
  .page-list-card {
    min-height: auto;
  }

  .page-list-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .page-list-toolbar > .el-button {
    width: 100%;
  }

  .page-list-search {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
  }

  .page-list-search :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .page-list-search :deep(.el-form-item__label) {
    justify-content: flex-start;
  }

  .page-list-search :deep(.el-input) {
    width: 100%;
  }

  .search-actions :deep(.el-form-item__content) {
    display: flex;
    gap: 8px;
  }

  .search-actions :deep(.el-button) {
    flex: 1;
  }

  .pagination-container {
    justify-content: center !important;
  }

  .pagination-container :deep(.el-pagination__sizes),
  .pagination-container :deep(.el-pagination__jump) {
    display: none;
  }
}
</style>
