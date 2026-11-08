<template>
  <div class="ai-models" v-loading="pageLoading">
    <div class="page-header">
      <h1>AI模型管理</h1>
      <p>管理和配置 hou_ai_model 模型池（对话/生成调用此配置）</p>
    </div>

    <el-card class="models-container" shadow="never">
      <template #header>
        <div class="card-header">
          <span>模型列表</span>
          <el-button v-if="hasPermission('shell:ai:models:add')" type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加模型
          </el-button>
        </div>
      </template>

      <el-table v-if="models.length" :data="models" style="width: 100%">
        <el-table-column prop="name" label="模型名称" width="180" />
        <el-table-column prop="type" label="模型类型" width="120">
          <template #default="scope">
            <el-tag :type="getModelTypeTag(scope.row.type)">
              {{ getModelTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="usage" label="使用次数" width="100" align="right" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button v-if="hasPermission('shell:ai:models:edit')" size="small" @click="editModel(scope.row)">编辑</el-button>
            <el-button v-if="hasPermission('shell:ai:models:copy')" size="small" @click="copyModel(scope.row)">复制</el-button>
            <el-button
              v-if="hasPermission('shell:ai:models:status')"
              size="small"
              :type="scope.row.status === 'active' ? 'warning' : 'success'"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 'active' ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="hasPermission('shell:ai:models:delete')" size="small" type="danger" @click="deleteModel(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无模型，请先添加或检查服务是否已启动" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form :model="modelForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="modelForm.name" placeholder="如 qwen-plus" />
        </el-form-item>
        <el-form-item label="模型类型" prop="type">
          <el-select v-model="modelForm.type" placeholder="请选择模型类型" style="width: 100%">
            <el-option
              v-for="opt in typeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址" prop="apiUrl">
          <el-input v-model="modelForm.apiUrl" placeholder="OpenAI 兼容地址，DashScope 可留空" />
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input v-model="modelForm.apiKey" type="password" placeholder="或配置环境变量 AI_DASHSCOPE_API_KEY" show-password />
        </el-form-item>
        <el-form-item label="温度">
          <el-slider v-model="modelForm.temperature" :min="0" :max="2" :step="0.1" show-input />
        </el-form-item>
        <el-form-item label="最大Token">
          <el-input-number v-model="modelForm.maxTokens" :min="256" :max="8192" controls-position="right" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="modelForm.description" type="textarea" :rows="2" placeholder="模型用途说明" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="modelForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveModel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi } from '@/api/ai/ai'
import { hasPermission } from '@/utils/permission'

const pageLoading = ref(false)
const saving = ref(false)
const models = ref([])
const typeOptions = ref([
  { value: 'chat', label: '对话模型' },
  { value: 'text', label: '文本生成' },
  { value: 'image', label: '图像生成' },
  { value: 'code', label: '代码生成' }
])

const dialogVisible = ref(false)
const dialogTitle = ref('添加模型')
const isEdit = ref(false)
const formRef = ref(null)

const modelForm = ref({
  name: '',
  type: 'chat',
  apiUrl: '',
  apiKey: '',
  description: '',
  temperature: 0.7,
  maxTokens: 2048,
  status: true
})

const rules = {
  name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择模型类型', trigger: 'change' }]
}

const getModelTypeTag = (type) => {
  const tagMap = { text: 'primary', image: 'success', code: 'warning', chat: 'info' }
  return tagMap[type] || 'primary'
}

const getModelTypeLabel = (type) => {
  const found = typeOptions.value.find(t => t.value === type)
  return found?.label || type
}

const showAddDialog = () => {
  dialogTitle.value = '添加模型'
  isEdit.value = false
  modelForm.value = {
    name: '',
    type: 'chat',
    apiUrl: '',
    apiKey: '',
    description: '',
    temperature: 0.7,
    maxTokens: 2048,
    status: true
  }
  dialogVisible.value = true
}

const mapModelRow = (row) => ({
  ...row,
  status: row.status === 1 || row.status === 'active' ? 'active' : 'inactive',
  usage: row.usageCount ?? row.usage ?? 0,
  apiUrl: row.apiUrl || row.api_url,
  apiKey: row.apiKey || row.api_key,
  temperature: row.temperature != null ? Number(row.temperature) : 0.7,
  maxTokens: row.maxTokens ?? row.max_tokens ?? 2048
})

const loadTypes = async () => {
  try {
    const res = await aiApi.getTypes()
    if (res.code === 1 && Array.isArray(res.data) && res.data.length) {
      typeOptions.value = res.data
    }
  } catch {
    // 使用默认类型列表
  }
}

const loadModels = async () => {
  pageLoading.value = true
  try {
    const res = await aiApi.getList({ page: 1, limit: 100 })
    if (res.code === 1 && res.data) {
      models.value = (res.data.list || []).map(mapModelRow)
    }
  } catch (e) {
    ElMessage.error(e.message || '加载模型失败')
  } finally {
    pageLoading.value = false
  }
}

const editModel = (model) => {
  dialogTitle.value = '编辑模型'
  isEdit.value = true
  modelForm.value = {
    ...model,
    status: model.status === 'active' || model.status === 1
  }
  dialogVisible.value = true
}

const saveModel = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saving.value = true
    const payload = {
      ...modelForm.value,
      status: modelForm.value.status ? 1 : 0
    }
    const res = isEdit.value ? await aiApi.update(payload) : await aiApi.create(payload)
    if (res.code === 1) {
      dialogVisible.value = false
      ElMessage.success(isEdit.value ? '模型更新成功' : '模型添加成功')
      await loadModels()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    if (error?.message) ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (model) => {
  const active = model.status === 'active'
  try {
    const res = active ? await aiApi.pause({ id: model.id }) : await aiApi.resume({ id: model.id })
    if (res.code === 1) {
      ElMessage.success(active ? '已禁用' : '已启用')
      await loadModels()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const copyModel = async (model) => {
  try {
    const res = await aiApi.copy({ id: model.id })
    if (res.code === 1) {
      ElMessage.success('复制成功')
      await loadModels()
    } else {
      ElMessage.error(res.message || '复制失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '复制失败')
  }
}

const deleteModel = (model) => {
  ElMessageBox.confirm(`确定删除模型「${model.name}」吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await aiApi.delete({ id: model.id })
    if (res.code === 1) {
      ElMessage.success('删除成功')
      await loadModels()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

onMounted(async () => {
  await loadTypes()
  await loadModels()
})
</script>

<style scoped>
.ai-models {
  padding: 16px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 16px;
  text-align: center;
}

.page-header h1 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 14px;
  color: #909399;
}

.models-container {
  border-radius: 8px;
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
