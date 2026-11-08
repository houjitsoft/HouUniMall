import { ref, onMounted } from 'vue'
import { aiApi } from '@/api/ai/ai'

/**
 * 从 paas/ai 服务拉取已启用模型（hou_ai_model）
 * 供 AiChat / AiGeneration 等页面选择 modelId
 */
export function usePaasAiModelOptions(options = { autoLoad: true }) {
  const modelOptions = ref([])
  const loading = ref(false)
  const defaultModelId = ref(null)

  const load = async () => {
    loading.value = true
    try {
      const res = await aiApi.getEnabledModels()
      if (res.code === 1 && Array.isArray(res.data)) {
        modelOptions.value = res.data.map(m => ({
          label: m.name,
          value: m.id,
          type: m.type
        }))
        defaultModelId.value = res.data[0]?.id ?? null
      } else {
        modelOptions.value = []
        defaultModelId.value = null
      }
    } catch (error) {
      console.error('加载 AI 模型失败:', error)
      modelOptions.value = []
      defaultModelId.value = null
    } finally {
      loading.value = false
    }
  }

  if (options.autoLoad !== false) {
    onMounted(load)
  }

  return {
    modelOptions,
    loading,
    load,
    defaultModelId
  }
}
