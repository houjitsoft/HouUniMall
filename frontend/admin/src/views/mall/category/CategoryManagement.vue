<template>
  <div class="category-management">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon category-icon">
                <el-icon><Menu /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ statistics.totalCategories }}</h3>
                <p>分类总数</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon level-icon">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ statistics.maxLevel }}</h3>
                <p>最大层级</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon products-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ statistics.totalProducts }}</h3>
                <p>关联商品</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon new-icon">
                <el-icon><Plus /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ statistics.newCategories }}</h3>
                <p>本月新增</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <div class="left-operations">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新增分类
        </el-button>
        <el-button @click="expandAll">
          <el-icon><ArrowDown /></el-icon>
          展开全部
        </el-button>
        <el-button @click="collapseAll">
          <el-icon><ArrowUp /></el-icon>
          收起全部
        </el-button>
        <el-button @click="exportCategories">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="right-operations">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索分类名称"
          style="width: 250px"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 分类树形表格 -->
    <el-card>
      <el-table 
        :data="categoryTree" 
        v-loading="loading" 
        stripe
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="false"
        ref="categoryTableRef"
      >
        <el-table-column prop="name" label="分类名称" min-width="200">
          <template #default="scope">
            <div class="category-name">
              <el-image
                v-if="scope.row.icon"
                :src="scope.row.icon"
                style="width: 20px; height: 20px; margin-right: 8px;"
                fit="contain"
              />
              <el-icon v-else class="category-default-icon" style="margin-right: 8px;"><Folder /></el-icon>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="80">
          <template #default="scope">
            <el-tag size="small" :type="getLevelTagType(scope.row.level)">
              {{ scope.row.level }}级
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productCount" label="商品数量" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="isShow" label="显示" width="80">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.isShow" 
              @change="updateCategoryShow(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button text type="primary" size="small" @click="addSubCategory(scope.row)">
              新增子级
            </el-button>
            <el-button text type="primary" size="small" @click="viewCategory(scope.row)">查看</el-button>
            <el-button text type="primary" size="small" @click="editCategory(scope.row)">编辑</el-button>
            <el-button 
              text 
              :type="scope.row.status === 'active' ? 'warning' : 'success'" 
              size="small" 
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
            <el-button text type="danger" size="small" @click="deleteCategory(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingCategory ? '编辑分类' : '新增分类'"
      width="600px"
    >
      <el-form :model="categoryForm" :rules="categoryFormRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryTreeOptions"
            :render-after-expand="false"
            placeholder="请选择上级分类（不选择则为顶级分类）"
            style="width: 100%"
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            clearable
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类图标" prop="icon">
          <el-upload
            class="icon-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleIconSuccess"
            :before-upload="beforeIconUpload"
          >
            <el-image v-if="categoryForm.icon" :src="categoryForm.icon" class="icon" />
            <el-icon v-else class="icon-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="是否显示" prop="isShow">
          <el-switch v-model="categoryForm.isShow" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="submitCategoryForm">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 分类详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="分类详情" width="600px">
      <div v-if="currentCategory" class="category-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分类图标">
            <el-image v-if="currentCategory.icon" :src="currentCategory.icon" style="width: 40px; height: 40px" fit="contain" />
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="分类名称">{{ currentCategory.name }}</el-descriptions-item>
          <el-descriptions-item label="上级分类">{{ getParentName(currentCategory.parentId) }}</el-descriptions-item>
          <el-descriptions-item label="分类层级">{{ currentCategory.level }}级</el-descriptions-item>
          <el-descriptions-item label="分类描述" :span="2">{{ currentCategory.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品数量">{{ currentCategory.productCount }}</el-descriptions-item>
          <el-descriptions-item label="排序">{{ currentCategory.sort }}</el-descriptions-item>
          <el-descriptions-item label="是否显示">
            <el-tag :type="currentCategory.isShow ? 'success' : 'danger'">
              {{ currentCategory.isShow ? '显示' : '隐藏' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentCategory.status === 'active' ? 'success' : 'danger'">
              {{ currentCategory.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentCategory.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentCategory.updateTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Menu, Connection, Box, Plus, Download, Search, ArrowDown, ArrowUp, Folder } from '@element-plus/icons-vue'
import { categoryService } from '@/api/goods/goods.js'

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const editingCategory = ref(null)
const currentCategory = ref(null)
const categoryTableRef = ref()

// 统计数据
const statistics = reactive({
  totalCategories: 89,
  maxLevel: 3,
  totalProducts: 2567,
  newCategories: 8
})

// 分类表单
const categoryFormRef = ref()
const categoryForm = reactive({
  parentId: null,
  name: '',
  icon: '',
  description: '',
  sort: 0,
  isShow: true,
  status: 'active'
})

const categoryFormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 模拟分类树形数据
const categoryTree = ref([
  {
    id: 1,
    name: '电子产品',
    parentId: null,
    level: 1,
    icon: 'https://via.placeholder.com/40x40?text=电子',
    description: '各类电子产品分类',
    productCount: 458,
    status: 'active',
    isShow: true,
    sort: 1,
    createTime: '2024-01-15 10:30:00',
    updateTime: '2024-01-20 14:20:00',
    children: [
      {
        id: 11,
        name: '手机',
        parentId: 1,
        level: 2,
        icon: 'https://via.placeholder.com/40x40?text=手机',
        description: '智能手机及配件',
        productCount: 156,
        status: 'active',
        isShow: true,
        sort: 1,
        createTime: '2024-01-16 09:15:00',
        updateTime: '2024-01-22 11:45:00',
        children: [
          {
            id: 111,
            name: 'iPhone',
            parentId: 11,
            level: 3,
            icon: 'https://via.placeholder.com/40x40?text=IP',
            description: '苹果手机系列',
            productCount: 56,
            status: 'active',
            isShow: true,
            sort: 1,
            createTime: '2024-01-17 14:20:00',
            updateTime: '2024-01-23 16:30:00'
          },
          {
            id: 112,
            name: 'Android',
            parentId: 11,
            level: 3,
            icon: 'https://via.placeholder.com/40x40?text=AN',
            description: '安卓手机系列',
            productCount: 100,
            status: 'active',
            isShow: true,
            sort: 2,
            createTime: '2024-01-17 15:40:00',
            updateTime: '2024-01-23 17:50:00'
          }
        ]
      },
      {
        id: 12,
        name: '电脑',
        parentId: 1,
        level: 2,
        icon: 'https://via.placeholder.com/40x40?text=电脑',
        description: '笔记本电脑和台式机',
        productCount: 89,
        status: 'active',
        isShow: true,
        sort: 2,
        createTime: '2024-01-16 10:25:00',
        updateTime: '2024-01-22 12:35:00'
      }
    ]
  },
  {
    id: 2,
    name: '服装鞋帽',
    parentId: null,
    level: 1,
    icon: 'https://via.placeholder.com/40x40?text=服装',
    description: '各类服装鞋帽产品',
    productCount: 324,
    status: 'active',
    isShow: true,
    sort: 2,
    createTime: '2024-01-15 11:45:00',
    updateTime: '2024-01-20 15:30:00',
    children: [
      {
        id: 21,
        name: '男装',
        parentId: 2,
        level: 2,
        icon: 'https://via.placeholder.com/40x40?text=男装',
        description: '男士服装',
        productCount: 156,
        status: 'active',
        isShow: true,
        sort: 1,
        createTime: '2024-01-16 11:30:00',
        updateTime: '2024-01-22 13:20:00'
      },
      {
        id: 22,
        name: '女装',
        parentId: 2,
        level: 2,
        icon: 'https://via.placeholder.com/40x40?text=女装',
        description: '女士服装',
        productCount: 168,
        status: 'active',
        isShow: true,
        sort: 2,
        createTime: '2024-01-16 12:15:00',
        updateTime: '2024-01-22 14:45:00'
      }
    ]
  }
])

// 分类树选项（用于选择上级分类）
const categoryTreeOptions = computed(() => {
  const buildOptions = (categories, level = 0) => {
    return categories.map(cat => ({
      id: cat.id,
      name: `${'  '.repeat(level)}${cat.name}`,
      children: cat.children ? buildOptions(cat.children, level + 1) : []
    }))
  }
  return buildOptions(categoryTree.value)
})

// 方法定义
const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await categoryService.getCategoryTree()
    console.log('分类接口返回:', res)
    
    // 接口返回 code: 1 表示成功
    if (res && (res.code === 1 || res.code === 200)) {
      // 转换后端数据结构为前端需要的格式
      const transformData = (items) => {
        return items.map(item => ({
          id: item.catId,
          name: item.catName,
          parentId: item.catParentId,
          level: item.catLevel || (item.catParentId === 0 ? 1 : 2),
          icon: item.catPic || '',
          description: '',
          productCount: 0,
          status: item.catShowStatus === 1 ? 'active' : 'inactive',
          isShow: item.catShowStatus === 1,
          sort: item.catDisplayorder || 0,
          createTime: item.createTime || '-',
          updateTime: item.updateTime || '-',
          children: item.childMenu ? transformData(item.childMenu) : []
        }))
      }
      
      categoryTree.value = transformData(res.data || [])
      
      // 计算统计数据
      let total = 0, maxLv = 0, products = 0
      const traverse = (cats, lv) => {
        cats.forEach(c => {
          total++
          if (lv > maxLv) maxLv = lv
          if (c.productCount) products += c.productCount
          if (c.children && c.children.length) traverse(c.children, lv + 1)
        })
      }
      traverse(categoryTree.value, 1)
      statistics.totalCategories = total
      statistics.maxLevel = maxLv
      statistics.totalProducts = products
      
      console.log('转换后的分类数据:', categoryTree.value)
    } else {
      ElMessage.error(res.message || '获取分类列表失败')
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchCategories()
}

const getLevelTagType = (level) => {
  const types = ['', 'primary', 'success', 'warning', 'danger']
  return types[level] || 'info'
}

const getParentName = (parentId) => {
  if (!parentId) return '顶级分类'
  
  const findParent = (categories) => {
    for (const cat of categories) {
      if (cat.id === parentId) return cat.name
      if (cat.children) {
        const found = findParent(cat.children)
        if (found) return found
      }
    }
    return null
  }
  
  return findParent(categoryTree.value) || '未知'
}

const expandAll = () => {
  categoryTableRef.value.store.states.expandRows.value = []
  const expandRows = []
  const traverse = (data) => {
    data.forEach(item => {
      expandRows.push(item)
      if (item.children && item.children.length) {
        traverse(item.children)
      }
    })
  }
  traverse(categoryTree.value)
  categoryTableRef.value.store.states.expandRows.value = expandRows
}

const collapseAll = () => {
  categoryTableRef.value.store.states.expandRows.value = []
}

const addSubCategory = (parent) => {
  categoryForm.parentId = parent.id
  showCreateDialog.value = true
}

const viewCategory = (category) => {
  currentCategory.value = category
  showDetailDialog.value = true
}

const editCategory = (category) => {
  editingCategory.value = category
  Object.assign(categoryForm, {
    parentId: category.parentId,
    name: category.name,
    icon: category.icon,
    description: category.description,
    sort: category.sort,
    isShow: category.isShow,
    status: category.status
  })
  showCreateDialog.value = true
}

const deleteCategory = async (category) => {
  if (category.children && category.children.length > 0) {
    ElMessage.warning('该分类下有子分类，无法删除')
    return
  }
  
  if (category.productCount > 0) {
    ElMessage.warning('该分类下有商品，无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // API调用逻辑
    ElMessage.success('删除成功')
    fetchCategories()
  } catch {
    ElMessage.info('已取消删除')
  }
}

const toggleStatus = async (category) => {
  const action = category.status === 'active' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该分类吗？`, '提示')
    
    // API调用逻辑
    category.status = category.status === 'active' ? 'inactive' : 'active'
    ElMessage.success(`${action}成功`)
  } catch {
    ElMessage.info(`已取消${action}`)
  }
}

const updateCategoryShow = async (category) => {
  try {
    // API调用逻辑
    ElMessage.success('更新成功')
  } catch (error) {
    // 回滚状态
    category.isShow = !category.isShow
    ElMessage.error('更新失败')
  }
}

const submitCategoryForm = async () => {
  try {
    await categoryFormRef.value.validate()
    
    // API调用逻辑
    if (editingCategory.value) {
      ElMessage.success('更新成功')
    } else {
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    fetchCategories()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const resetForm = () => {
  Object.assign(categoryForm, {
    parentId: null,
    name: '',
    icon: '',
    description: '',
    sort: 0,
    isShow: true,
    status: 'active'
  })
  editingCategory.value = null
}

const handleIconSuccess = (response) => {
  categoryForm.icon = response.url
  ElMessage.success('图标上传成功')
}

const beforeIconUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt1M = file.size / 1024 / 1024 < 1

  if (!isJPG) {
    ElMessage.error('图标只能是 JPG/PNG 格式!')
  }
  if (!isLt1M) {
    ElMessage.error('图标大小不能超过 1MB!')
  }
  return isJPG && isLt1M
}

const exportCategories = () => {
  ElMessage.success('导出功能开发中...')
}

// 生命周期
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stats-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.category-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.level-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.products-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.new-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

.stats-info h3 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stats-info p {
  margin: 5px 0 0 0;
  color: #666;
  font-size: 14px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.left-operations {
  display: flex;
  gap: 10px;
}

.category-name {
  display: flex;
  align-items: center;
}

.category-default-icon {
  color: #409EFF;
}

.icon-uploader .icon {
  width: 60px;
  height: 60px;
  display: block;
  object-fit: contain;
}

.icon-uploader .icon-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 60px;
  height: 60px;
  line-height: 60px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: .3s;
}

.icon-uploader .icon-uploader-icon:hover {
  border-color: #409EFF;
}

.category-detail {
  max-height: 400px;
  overflow-y: auto;
}
</style>
