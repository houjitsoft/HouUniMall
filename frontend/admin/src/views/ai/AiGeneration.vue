<template>
  <div class="ai-generation">
    <div class="page-header">
      <h1>智能生成</h1>
      <p>使用AI进行智能内容生成</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="generation-types">
          <template #header>
            <span>生成类型</span>
          </template>
          
          <div class="type-list">
            <div 
              v-for="type in generationTypes" 
              :key="type.id"
              class="type-item"
              :class="{ active: selectedType?.id === type.id }"
              @click="selectType(type)"
            >
              <div class="type-icon">
                <el-icon><component :is="type.icon" /></el-icon>
              </div>
              <div class="type-info">
                <h4>{{ type.name }}</h4>
                <p>{{ type.description }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card class="generation-workspace">
          <template #header>
            <span>{{ selectedType?.name || '请选择生成类型' }}</span>
          </template>
          
          <div v-if="selectedType" class="generation-form">
            <el-form label-width="100px" style="margin-bottom: 16px">
              <el-form-item label="AI 模型">
                <el-select
                  v-model="selectedModelId"
                  placeholder="选择模型（默认使用第一个启用模型）"
                  style="width: 100%"
                  :loading="modelsLoading"
                >
                  <el-option
                    v-for="opt in modelOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-form>
            <!-- 代码生成 -->
            <div v-if="selectedType.id === 'code'" class="code-generation">
              <el-form :model="codeForm" label-width="100px">
                <el-form-item label="编程语言">
                  <el-select v-model="codeForm.language" placeholder="请选择编程语言">
                    <el-option label="JavaScript" value="javascript" />
                    <el-option label="Python" value="python" />
                    <el-option label="Java" value="java" />
                    <el-option label="Vue.js" value="vue" />
                    <el-option label="React" value="react" />
                  </el-select>
                </el-form-item>
                <el-form-item label="功能描述">
                  <el-input 
                    v-model="codeForm.description" 
                    type="textarea" 
                    :rows="4" 
                    placeholder="请描述需要实现的功能..."
                  />
                </el-form-item>
                <el-form-item label="特殊要求">
                  <el-input 
                    v-model="codeForm.requirements" 
                    type="textarea" 
                    :rows="2" 
                    placeholder="如有特殊要求请说明..."
                  />
                </el-form-item>
              </el-form>
            </div>

            <!-- 文档生成 -->
            <div v-else-if="selectedType.id === 'document'" class="document-generation">
              <el-form :model="documentForm" label-width="100px">
                <el-form-item label="文档类型">
                  <el-select v-model="documentForm.type" placeholder="请选择文档类型">
                    <el-option label="技术文档" value="technical" />
                    <el-option label="产品文档" value="product" />
                    <el-option label="API文档" value="api" />
                    <el-option label="用户手册" value="manual" />
                  </el-select>
                </el-form-item>
                <el-form-item label="文档标题">
                  <el-input v-model="documentForm.title" placeholder="请输入文档标题" />
                </el-form-item>
                <el-form-item label="内容大纲">
                  <el-input 
                    v-model="documentForm.outline" 
                    type="textarea" 
                    :rows="6" 
                    placeholder="请描述文档的主要内容和大纲..."
                  />
                </el-form-item>
              </el-form>
            </div>

            <!-- 文本工具 -->
            <div v-else-if="['summary', 'translate', 'correct'].includes(selectedType.id)" class="text-tool-generation">
              <el-form :model="textToolForm" label-width="100px">
                <el-form-item v-if="selectedType.id === 'translate'" label="目标语言">
                  <el-input v-model="textToolForm.targetLang" placeholder="例如：英文、日文" />
                </el-form-item>
                <el-form-item label="输入内容">
                  <el-input
                    v-model="textToolForm.content"
                    type="textarea"
                    :rows="8"
                    placeholder="请输入要处理的文本..."
                  />
                </el-form-item>
              </el-form>
            </div>

            <!-- 图像生成 -->
            <div v-else-if="selectedType.id === 'image'" class="image-generation">
              <el-form :model="imageForm" label-width="100px">
                <el-form-item label="图像描述">
                  <el-input 
                    v-model="imageForm.description" 
                    type="textarea" 
                    :rows="4" 
                    placeholder="请详细描述要生成的图像..."
                  />
                </el-form-item>
                <el-form-item label="图像风格">
                  <el-select v-model="imageForm.style" placeholder="请选择图像风格">
                    <el-option label="写实风格" value="realistic" />
                    <el-option label="卡通风格" value="cartoon" />
                    <el-option label="抽象风格" value="abstract" />
                    <el-option label="水彩风格" value="watercolor" />
                  </el-select>
                </el-form-item>
                <el-form-item label="图像尺寸">
                  <el-select v-model="imageForm.size" placeholder="请选择图像尺寸">
                    <el-option label="正方形 (1024x1024)" value="1024x1024" />
                    <el-option label="横向 (1024x1792)" value="1024x1792" />
                    <el-option label="纵向 (1792x1024)" value="1792x1024" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>

            <!-- 生成按钮 -->
            <div class="generation-actions">
              <el-button v-if="hasPermission('shell:ai:generation:run')" type="primary" size="large" @click="startGeneration" :loading="isGenerating">
                <el-icon><MagicStick /></el-icon>
                开始生成
              </el-button>
              <el-button size="large" @click="resetForm">重置表单</el-button>
            </div>
          </div>

          <!-- 生成结果 -->
          <div v-if="generationResult" class="generation-result">
            <el-divider>生成结果</el-divider>
            <div class="result-content">
              <pre v-if="selectedType?.id === 'code'" class="code-result">{{ generationResult }}</pre>
              <div v-else-if="selectedType?.id === 'image'" class="image-result">
                <img :src="generationResult" alt="生成的图像" style="max-width: 100%;" />
              </div>
              <div v-else class="text-result">{{ generationResult }}</div>
            </div>
            <div class="result-actions">
              <el-button type="primary" @click="copyResult">复制结果</el-button>
              <el-button @click="downloadResult">下载结果</el-button>
              <el-button @click="regenerateResult">重新生成</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { ref, onMounted, watch } from 'vue'
import { MagicStick, Document, Picture, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai/ai'
import { usePaasAiModelOptions } from '@/composables/usePaasAiModelOptions'
import { hasPermission } from '@/utils/permission'

const { modelOptions, loading: modelsLoading, defaultModelId } = usePaasAiModelOptions()
const selectedModelId = ref(null)

watch(defaultModelId, (id) => {
  if (id && !selectedModelId.value) selectedModelId.value = id
}, { immediate: true })

const selectedType = ref(null)
const isGenerating = ref(false)
const generationResult = ref('')

const generationTypes = ref([
  {
    id: 'code',
    name: '代码生成',
    description: '根据描述生成代码',
    icon: 'Edit'
  },
  {
    id: 'document',
    name: '文档生成',
    description: '生成各类文档',
    icon: 'Document'
  },
  {
    id: 'summary',
    name: '文本摘要',
    description: '快速提炼长文要点',
    icon: 'Document'
  },
  {
    id: 'translate',
    name: '文本翻译',
    description: '多语言翻译',
    icon: 'Document'
  },
  {
    id: 'correct',
    name: '文本纠错',
    description: '纠错并优化表达',
    icon: 'Edit'
  },
  {
    id: 'image',
    name: '图像生成',
    description: '根据描述生成图像',
    icon: 'Picture'
  }
])

const codeForm = ref({
  language: '',
  description: '',
  requirements: ''
})

const documentForm = ref({
  type: '',
  title: '',
  outline: ''
})

const imageForm = ref({
  description: '',
  style: '',
  size: ''
})

const textToolForm = ref({
  content: '',
  targetLang: '英文'
})

const selectType = (type) => {
  selectedType.value = type
  generationResult.value = ''
}

const startGeneration = async () => {
  if (!selectedType.value) {
    ElMessage.warning('请先选择生成类型')
    return
  }

  isGenerating.value = true
  
  try {
    let prompt = ''
    let type = selectedType.value.id
    if (type === 'code') {
      prompt = `请用${codeForm.value.language || 'JavaScript'}生成代码：${codeForm.value.description}\n要求：${codeForm.value.requirements || '无'}`
    } else if (type === 'document') {
      prompt = `请生成${documentForm.value.type || '技术'}文档《${documentForm.value.title}》，大纲：${documentForm.value.outline}`
    } else if (type === 'image') {
      const res = await aiApi.generateImage({
        prompt: `${imageForm.value.description}，风格：${imageForm.value.style}，尺寸：${imageForm.value.size}`,
        modelId: selectedModelId.value,
        size: imageForm.value.size || '1024x1024'
      })
      if (res.code === 1) {
        generationResult.value = res.data?.url || res.data?.result || '图像生成任务已提交'
        ElMessage.success('生成完成！')
      } else {
        ElMessage.warning(res.message || '图像生成失败')
      }
      return
    } else if (type === 'summary') {
      const res = await aiApi.summarizeText({
        content: textToolForm.value.content,
        modelId: selectedModelId.value
      })
      if (res.code === 1) {
        generationResult.value = res.data?.content || res.data?.result || ''
        ElMessage.success('生成完成！')
      } else {
        ElMessage.error(res.message || '生成失败')
      }
      return
    } else if (type === 'translate') {
      const res = await aiApi.translateText({
        content: textToolForm.value.content,
        targetLang: textToolForm.value.targetLang || '英文',
        modelId: selectedModelId.value
      })
      if (res.code === 1) {
        generationResult.value = res.data?.content || res.data?.result || ''
        ElMessage.success('生成完成！')
      } else {
        ElMessage.error(res.message || '生成失败')
      }
      return
    } else if (type === 'correct') {
      const res = await aiApi.correctText({
        content: textToolForm.value.content,
        modelId: selectedModelId.value
      })
      if (res.code === 1) {
        generationResult.value = res.data?.content || res.data?.result || ''
        ElMessage.success('生成完成！')
      } else {
        ElMessage.error(res.message || '生成失败')
      }
      return
    }
    const res = await aiApi.generateText({ prompt, type, modelId: selectedModelId.value })
    if (res.code === 1) {
      generationResult.value = res.data?.content || res.data?.result || ''
      ElMessage.success('生成完成！')
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成失败：' + (error.message || '未知错误'))
  } finally {
    isGenerating.value = false
  }
}

const resetForm = () => {
  codeForm.value = { language: '', description: '', requirements: '' }
  documentForm.value = { type: '', title: '', outline: '' }
  imageForm.value = { description: '', style: '', size: '' }
  textToolForm.value = { content: '', targetLang: '英文' }
  generationResult.value = ''
}

const copyResult = () => {
  navigator.clipboard.writeText(generationResult.value).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

const downloadResult = () => {
  if (selectedType.value.id === 'image') {
    const link = document.createElement('a')
    link.href = generationResult.value
    link.download = 'generated-image.jpg'
    link.click()
  } else {
    const blob = new Blob([generationResult.value], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `generated-${selectedType.value.id}.txt`
    link.click()
    URL.revokeObjectURL(url)
  }
  ElMessage.success('下载完成')
}

const regenerateResult = () => {
  startGeneration()
}

onMounted(() => {
})
</script>

<style scoped>
.ai-generation {
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

.generation-types {
  height: 600px;
}

.type-list {
  padding: 10px 0;
}

.type-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.type-item:hover {
  background-color: #f0f9ff;
}

.type-item.active {
  background-color: #409EFF;
  color: white;
}

.type-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 18px;
}

.type-item.active .type-icon {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.type-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.type-info p {
  margin: 0;
  font-size: 14px;
  opacity: 0.8;
}

.generation-workspace {
  height: 600px;
  overflow-y: auto;
}

.generation-form {
  margin-bottom: 20px;
}

.generation-actions {
  margin-top: 20px;
  text-align: center;
}

.generation-actions .el-button {
  margin: 0 10px;
}

.generation-result {
  margin-top: 30px;
}

.result-content {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 20px;
  margin-bottom: 20px;
}

.code-result {
  background-color: #2d3748;
  color: #e2e8f0;
  padding: 20px;
  border-radius: 6px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
}

.text-result {
  white-space: pre-wrap;
  line-height: 1.6;
}

.image-result {
  text-align: center;
}

.result-actions {
  text-align: center;
}

.result-actions .el-button {
  margin: 0 10px;
}
</style>
