<template>
  <div class="ai-prompts" v-loading="pageLoading">
    <div class="page-header">
      <h1>提示词管理</h1>
      <p>管理和配置AI提示词模板</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="category-sidebar">
          <template #header>
            <div class="card-header">
              <span>分类管理</span>
              <el-button type="primary" size="small" @click="showAddCategoryDialog">
                <el-icon><Plus /></el-icon>
                添加分类
              </el-button>
            </div>
          </template>
          
          <el-tree
            v-if="categories.length"
            :data="categories"
            :props="defaultProps"
            @node-click="handleCategorySelect"
            default-expand-all
            highlight-current
          />
          <el-empty v-else description="暂无分类，请先在数据库初始化 hou_ai_prompt_category" :image-size="80" />
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="prompts-main">
          <template #header>
            <div class="card-header">
              <span>{{ selectedCategory?.name || '所有提示词' }}</span>
              <el-button v-if="hasPermission('shell:ai:prompts:add')" type="primary" @click="showAddPromptDialog">
                <el-icon><Plus /></el-icon>
                添加提示词
              </el-button>
            </div>
          </template>
          
          <el-table v-if="filteredPrompts.length" :data="filteredPrompts" style="width: 100%">
            <el-table-column prop="name" label="提示词名称" width="200" />
            <el-table-column prop="category" label="分类" width="150" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="usage" label="使用次数" width="120" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button v-if="hasPermission('shell:ai:prompts:edit')" size="small" @click="editPrompt(scope.row)">编辑</el-button>
                <el-button v-if="hasPermission('shell:ai:prompts:test')" size="small" @click="testPrompt(scope.row)">测试</el-button>
                <el-button v-if="hasPermission('shell:ai:prompts:delete')" size="small" type="danger" @click="deletePrompt(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无提示词，点击右上角添加" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加/编辑提示词对话框 -->
    <el-dialog v-model="promptDialogVisible" :title="promptDialogTitle" width="800px">
      <el-form :model="promptForm" :rules="promptRules" ref="promptFormRef" label-width="100px">
        <el-form-item label="提示词名称" prop="name">
          <el-input v-model="promptForm.name" placeholder="请输入提示词名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="promptForm.categoryId" placeholder="请选择分类">
            <el-option 
              v-for="category in flatCategories" 
              :key="category.id" 
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提示词内容" prop="content">
          <el-input 
            v-model="promptForm.content" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入提示词内容，使用 {{变量名}} 格式定义变量"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="promptForm.description" type="textarea" :rows="3" placeholder="请输入提示词描述" />
        </el-form-item>
        <el-form-item label="标签">
          <el-tag
            v-for="tag in promptForm.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
            style="margin-right: 10px;"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="inputVisible"
            ref="inputRef"
            v-model="inputValue"
            size="small"
            style="width: 90px;"
            @keyup.enter="addTag"
            @blur="addTag"
          />
          <el-button v-else size="small" @click="showInput">+ 添加标签</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePrompt">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加分类对话框 -->
    <el-dialog v-model="categoryDialogVisible" title="添加分类" width="400px">
      <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, computed, nextTick } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi } from '@/api/ai/ai'
import { hasPermission } from '@/utils/permission'

const categories = ref([])
const flatCategories = ref([])
const prompts = ref([])
const pageLoading = ref(false)

const selectedCategory = ref(null)
const defaultProps = {
  children: 'children',
  label: 'name'
}

const promptDialogVisible = ref(false)
const promptDialogTitle = ref('添加提示词')
const promptFormRef = ref(null)
const isEditPrompt = ref(false)

const promptForm = ref({
  id: null,
  name: '',
  categoryId: null,
  content: '',
  description: '',
  tags: []
})

const promptRules = {
  name: [{ required: true, message: '请输入提示词名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入提示词内容', trigger: 'blur' }]
}

const categoryDialogVisible = ref(false)
const categoryFormRef = ref(null)
const categoryForm = ref({
  name: '',
  description: ''
})

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 标签相关
const inputVisible = ref(false)
const inputValue = ref('')
const inputRef = ref(null)

const filteredPrompts = computed(() => {
  if (!selectedCategory.value) {
    return prompts.value
  }
  return prompts.value.filter(p => p.categoryId === selectedCategory.value.id)
})

const categoryNameMap = computed(() => {
  const map = {}
  flatCategories.value.forEach(c => { map[c.id] = c.name })
  return map
})

const buildCategoryTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

const mapPromptRow = (row) => ({
  ...row,
  category: categoryNameMap.value[row.categoryId] || '-',
  usage: row.usageCount ?? 0,
  tags: row.tags ? String(row.tags).split(',').filter(Boolean) : []
})

const loadCategories = async () => {
  try {
    const res = await aiApi.getPromptCategories()
    if (res.code === 1 && res.data) {
      flatCategories.value = res.data
      categories.value = buildCategoryTree(res.data)
    }
  } catch (e) {
    ElMessage.error(e.message || '加载分类失败')
  }
}

const loadPrompts = async () => {
  pageLoading.value = true
  try {
    const params = { page: 1, limit: 200 }
    if (selectedCategory.value?.id) {
      params.categoryId = selectedCategory.value.id
    }
    const res = await aiApi.getPrompts(params)
    if (res.code === 1 && res.data?.list) {
      prompts.value = res.data.list.map(mapPromptRow)
    } else {
      prompts.value = []
    }
  } catch (e) {
    ElMessage.error(e.message || '加载提示词失败')
  } finally {
    pageLoading.value = false
  }
}

const handleCategorySelect = async (data) => {
  selectedCategory.value = data
  await loadPrompts()
}

const showAddPromptDialog = () => {
  promptDialogTitle.value = '添加提示词'
  isEditPrompt.value = false
  promptForm.value = {
    id: null,
    name: '',
    categoryId: selectedCategory.value?.id || null,
    content: '',
    description: '',
    tags: []
  }
  promptDialogVisible.value = true
}

const editPrompt = (prompt) => {
  promptDialogTitle.value = '编辑提示词'
  isEditPrompt.value = true
  promptForm.value = {
    ...prompt,
    tags: Array.isArray(prompt.tags) ? [...prompt.tags] : []
  }
  promptDialogVisible.value = true
}

const savePrompt = async () => {
  if (!promptFormRef.value) return
  
  try {
    await promptFormRef.value.validate()
    const payload = {
      ...promptForm.value,
      tags: Array.isArray(promptForm.value.tags) ? promptForm.value.tags.join(',') : promptForm.value.tags
    }
    const res = isEditPrompt.value
      ? await aiApi.updatePrompt(payload)
      : await aiApi.createPrompt(payload)
    if (res.code === 1) {
      promptDialogVisible.value = false
      ElMessage.success(isEditPrompt.value ? '提示词更新成功' : '提示词添加成功')
      await loadPrompts()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  }
}

const deletePrompt = (prompt) => {
  ElMessageBox.confirm('确定要删除这个提示词吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await aiApi.deletePrompt({ id: prompt.id })
    if (res.code === 1) {
      ElMessage.success('删除成功')
      await loadPrompts()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const testPrompt = async (prompt) => {
  try {
    const res = await aiApi.generateText({ prompt: prompt.content, type: 'prompt_test' })
    if (res.code === 1) {
      ElMessageBox.alert(res.data?.content || res.data?.result || '无返回内容', `测试：${prompt.name}`, {
        confirmButtonText: '确定'
      })
    } else {
      ElMessage.error(res.message || '测试失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '测试失败')
  }
}

const showAddCategoryDialog = () => {
  categoryForm.value = {
    name: '',
    description: ''
  }
  categoryDialogVisible.value = true
}

const saveCategory = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    const res = await aiApi.createPromptCategory({
      name: categoryForm.value.name,
      description: categoryForm.value.description,
      parentId: 0
    })
    if (res.code === 1) {
      categoryDialogVisible.value = false
      ElMessage.success('分类创建成功')
      await loadCategories()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  }
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    inputRef.value?.focus()
  })
}

const addTag = () => {
  if (inputValue.value && !promptForm.value.tags.includes(inputValue.value)) {
    promptForm.value.tags.push(inputValue.value)
  }
  inputVisible.value = false
  inputValue.value = ''
}

const removeTag = (tag) => {
  const index = promptForm.value.tags.indexOf(tag)
  if (index > -1) {
    promptForm.value.tags.splice(index, 1)
  }
}

onMounted(async () => {
  await loadCategories()
  await loadPrompts()
})
</script>

<style scoped>
.ai-prompts {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-header h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
}

.page-header p {
  font-size: 16px;
  color: #909399;
}

.category-sidebar {
  height: 600px;
}

.prompts-main {
  height: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
