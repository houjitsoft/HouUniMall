import { ref, onMounted } from 'vue'
import request from '@/utils/request'

/** 下拉框「自定义模板」选项值 */
export const CUSTOM_MODEL_VALUE = '__custom__'

/**
 * 从 AI 配置中心拉取已启用模型，并追加「自定义模板」选项
 * 供 AiSettings / AiGeneration / AiPrompt 等页面复用
 */
export function useAiModelOptions(options = { autoLoad: true }) {
  const modelOptions = ref([])
  const loading = ref(false)
  const defaultConfigId = ref(null)

  const load = async () => {
    loading.value = true
    try {
      const [enabledRes, defaultRes] = await Promise.all([
        request.post('/web/base/aiConfig/enabled'),
        request.get('/web/base/aiConfig/default').catch(() => null)
      ])

      const list = enabledRes?.data || []
      if (defaultRes?.data?.id) {
        defaultConfigId.value = String(defaultRes.data.id)
      } else {
        const defaultItem = list.find(item => item.isDefault === 1)
        if (defaultItem?.id) {
          defaultConfigId.value = String(defaultItem.id)
        }
      }

      modelOptions.value = [
        ...list.map(item => ({
          label: `${item.providerName} · ${item.modelName}`,
          value: String(item.id),
          providerCode: item.providerCode,
          modelName: item.modelName,
          isDefault: item.isDefault === 1
        })),
        {
          label: '自定义模板',
          value: CUSTOM_MODEL_VALUE,
          description: '手动指定模型名称与参数'
        }
      ]
    } catch (error) {
      console.error('加载 AI 模型选项失败:', error)
      modelOptions.value = [
        { label: '自定义模板', value: CUSTOM_MODEL_VALUE, description: '手动指定模型名称与参数' }
      ]
    } finally {
      loading.value = false
    }
  }

  const resolveInitialModel = () => {
    if (defaultConfigId.value) return defaultConfigId.value
    const first = modelOptions.value.find(o => o.value !== CUSTOM_MODEL_VALUE)
    return first?.value || CUSTOM_MODEL_VALUE
  }

  if (options.autoLoad !== false) {
    onMounted(load)
  }

  return {
    modelOptions,
    loading,
    load,
    defaultConfigId,
    CUSTOM_MODEL_VALUE,
    resolveInitialModel
  }
}
