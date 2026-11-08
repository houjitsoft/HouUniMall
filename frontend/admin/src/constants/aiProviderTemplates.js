/** AI 服务商预设模板（新增配置时可一键填充） */
export const CUSTOM_TEMPLATE_ID = '__custom__'

/** Normalize provider model identifiers for case-insensitive catalog matching. */
export function normalizeModelName(value) {
  return String(value || '').trim().toLowerCase()
}

function normalizeEndpoint(value) {
  return String(value || '').trim().replace(/\/+$/, '').toLowerCase()
}

function modelConfigKey(item) {
  const endpoint = normalizeEndpoint(item?.apiEndpoint)
  return `${item?.providerCode || ''}::${endpoint}::${normalizeModelName(item?.modelName)}`
}

/** 已下线的外部供应商配置不再进入 GEO 模型目录。 */
export function isRetiredProviderConfig(item) {
  const descriptor = [item?.providerCode, item?.providerName, item?.modelName]
    .filter(Boolean)
    .join(' ')
  return /dify|dataset-service-api/i.test(descriptor)
}

export const AI_PROVIDER_TEMPLATES = [
  {
    id: 'openai-gpt56-sol-hezu-bus',
    name: 'GPT-5.6-Sol（HezuBus）',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.6-Sol',
    modelVersion: 'latest',
    description: 'HezuBus OpenAI 兼容接口 GPT-5.6-Sol 模型',
    apiEndpoint: 'https://hezubus.cc/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 21,
    color: '#10a37f'
  },
  {
    id: 'hezu-gpt56-sol',
    name: 'GPT5.6-sol (Hezu)',
    providerCode: 'openai',
    providerName: 'Hezu',
    modelName: 'GPT5.6-sol',
    modelVersion: 'latest',
    description: 'Hezu OpenAI 兼容接口：GPT5.6-sol（https://hezu.ink/v1）',
    apiEndpoint: 'https://hezu.ink/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 25,
    color: '#6366f1'
  },
  {
    id: 'openai-gpt56-sol',
    name: 'OpenAI GPT-5.6 Sol',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.6-sol',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.6 Sol 通用模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 20,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt56-terra',
    name: 'OpenAI GPT-5.6 Terra',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.6-terra',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.6 Terra 通用模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 19,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt56-luna',
    name: 'OpenAI GPT-5.6 Luna',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.6-luna',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.6 Luna 通用模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 18,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt55',
    name: 'OpenAI GPT-5.5',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.5',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.5 旗舰文本与推理模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 17,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt55-pro',
    name: 'OpenAI GPT-5.5 Pro',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.5-pro',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.5 高质量深度推理模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 16,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt54',
    name: 'OpenAI GPT-5.4',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.4',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.4 通用文本与推理模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 15,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt54-mini',
    name: 'OpenAI GPT-5.4 Mini',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.4-mini',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.4 轻量低成本模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 14,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt54-nano',
    name: 'OpenAI GPT-5.4 Nano',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-5.4-nano',
    modelVersion: 'latest',
    description: 'OpenAI GPT-5.4 高吞吐量轻型模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 128000,
    temperature: 1,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 13,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt4-turbo',
    name: 'OpenAI GPT-4 Turbo',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-4-turbo',
    modelVersion: '0125',
    description: 'OpenAI 旗舰对话模型，支持函数调用与流式输出',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 4096,
    temperature: 0.7,
    topP: 1,
    timeout: 60,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.03,
    priority: 10,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt35',
    name: 'OpenAI GPT-3.5',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-3.5-turbo',
    modelVersion: '0125',
    description: '高性价比通用对话模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.0015,
    costPer1kOutput: 0.002,
    priority: 5,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt4o',
    name: 'OpenAI GPT-4o',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-4o',
    modelVersion: '2024-11-20',
    description: 'OpenAI 多模态旗舰模型，文本与视觉理解',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.005,
    costPer1kOutput: 0.015,
    priority: 10,
    color: '#10a37f'
  },
  {
    id: 'openai-gpt4o-mini',
    name: 'OpenAI GPT-4o Mini',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'gpt-4o-mini',
    modelVersion: '2024-07-18',
    description: 'OpenAI 高性价比多模态小模型',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.00015,
    costPer1kOutput: 0.0006,
    priority: 8,
    color: '#10a37f'
  },
  {
    id: 'openai-o1-mini',
    name: 'OpenAI o1-mini',
    providerCode: 'openai',
    providerName: 'OpenAI',
    modelName: 'o1-mini',
    modelVersion: 'latest',
    description: 'OpenAI 推理增强模型，适合复杂逻辑与数学',
    apiEndpoint: 'https://api.openai.com/v1',
    maxTokens: 8192,
    temperature: 1,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.003,
    costPer1kOutput: 0.012,
    priority: 9,
    color: '#10a37f'
  },
  {
    id: 'qwen-max',
    name: '通义千问 Max',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen-max',
    modelVersion: 'latest',
    description: '阿里云旗舰大模型，中文理解能力强',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 6000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.02,
    costPer1kOutput: 0.06,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'qwen-plus',
    name: '通义千问 Plus',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen-plus',
    modelVersion: 'latest',
    description: '均衡性能与成本的通用模型',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 6000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.012,
    priority: 6,
    color: '#ff6a00'
  },
  {
    id: 'qwen-turbo',
    name: '通义千问 Turbo',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen-turbo',
    modelVersion: 'latest',
    description: '通义千问高速轻量模型，低延迟对话',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 6000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.002,
    costPer1kOutput: 0.006,
    priority: 5,
    color: '#ff6a00'
  },
  {
    id: 'qwen2.5-max',
    name: '通义千问 2.5 Max',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen2.5-max',
    modelVersion: 'latest',
    description: 'Qwen 2.5 旗舰文本模型',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 8000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.036,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'qwen2.5-plus',
    name: '通义千问 2.5 Plus',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen2.5-plus',
    modelVersion: 'latest',
    description: 'Qwen 2.5 均衡性能模型',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 8000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.012,
    priority: 7,
    color: '#ff6a00'
  },
  {
    id: 'qwen-long',
    name: '通义千问 Long',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen-long',
    modelVersion: 'latest',
    description: '通义千问长文本模型，支持超长上下文',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 30000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.012,
    priority: 7,
    color: '#ff6a00'
  },
  {
    id: 'qwen-coder-plus',
    name: '通义千问 Coder Plus',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen-coder-plus',
    modelVersion: 'latest',
    description: '通义千问代码专用模型，代码生成与补全',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 8000,
    temperature: 0.3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.006,
    costPer1kOutput: 0.018,
    priority: 7,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.6-plus',
    name: '通义千问 3.6 Plus',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.6-plus-2026-04-02',
    modelVersion: '2026-04-02',
    description: '阿里云百炼平台 Qwen 3.6 Plus 模型，通过专属 API 端点调用',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.008,
    costPer1kOutput: 0.024,
    priority: 9,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.7-max-preview',
    name: '通义千问 3.7 Max 预览版',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.7-max-preview',
    modelVersion: 'preview',
    description: '阿里云百炼平台 Qwen 3.7 Max 预览版，最新旗舰模型',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 10000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.036,
    priority: 10,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.7-max-2026-06-08',
    name: '通义千问 3.7 Max (2026-06-08)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.7-max-2026-06-08',
    modelVersion: '2026-06-08',
    description: '阿里云百炼平台 Qwen 3.7 Max 稳定版，2026年6月8日发布',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 10000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.036,
    priority: 10,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.7-max-2026-05-17',
    name: '通义千问 3.7 Max (2026-05-17)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.7-max-2026-05-17',
    modelVersion: '2026-05-17',
    description: '阿里云百炼平台 Qwen 3.7 Max 稳定版，2026年5月17日发布',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 10000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.036,
    priority: 10,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.6-35b-a3b',
    name: '通义千问 3.6 (35B-A3B)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.6-35b-a3b',
    modelVersion: '35b-a3b',
    description: '阿里云百炼平台 Qwen 3.6 混合专家模型，35B总参数量，3B激活',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.006,
    costPer1kOutput: 0.018,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.5-ocr',
    name: '通义千问 3.5 OCR',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.5-ocr',
    modelVersion: 'ocr',
    description: '阿里云百炼平台 Qwen 3.5 OCR 专用模型，擅长文字识别与文档理解',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 4000,
    temperature: 0.3,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.012,
    priority: 7,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.6-flash-2026-04-16',
    name: '通义千问 3.6 Flash (2026-04-16)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.6-flash-2026-04-16',
    modelVersion: '2026-04-16',
    description: '阿里云百炼平台 Qwen 3.6 Flash 轻量模型，快速响应，高性价比',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 6000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 60,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.002,
    costPer1kOutput: 0.006,
    priority: 6,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.7-plus',
    name: '通义千问 3.7 Plus',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.7-plus',
    modelVersion: 'latest',
    description: '阿里云百炼平台 Qwen 3.7 Plus 均衡模型，性能与成本的最佳平衡',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.008,
    costPer1kOutput: 0.024,
    priority: 9,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.6-27b',
    name: '通义千问 3.6 (27B)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.6-27b',
    modelVersion: '27b',
    description: '阿里云百炼平台 Qwen 3.6 中等规模模型，27B参数量',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.006,
    costPer1kOutput: 0.018,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'deepseek-v4-pro',
    name: 'DeepSeek V4 Pro（百炼）',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'deepseek-v4-pro',
    modelVersion: 'pro',
    description: '阿里云百炼平台 DeepSeek V4 Pro 专业版，性能强劲',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.012,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.6-flash',
    name: '通义千问 3.6 Flash',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.6-flash',
    modelVersion: 'latest',
    description: '阿里云百炼平台 Qwen 3.6 Flash 轻量模型，快速响应',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 6000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 60,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.002,
    costPer1kOutput: 0.006,
    priority: 6,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.7-plus-2026-05-26',
    name: '通义千问 3.7 Plus (2026-05-26)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.7-plus-2026-05-26',
    modelVersion: '2026-05-26',
    description: '阿里云百炼平台 Qwen 3.7 Plus 稳定版，2026年5月26日发布',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.008,
    costPer1kOutput: 0.024,
    priority: 9,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.7-max-2026-05-20',
    name: '通义千问 3.7 Max (2026-05-20)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.7-max-2026-05-20',
    modelVersion: '2026-05-20',
    description: '阿里云百炼平台 Qwen 3.7 Max 稳定版，2026年5月20日发布',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 10000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.036,
    priority: 10,
    color: '#ff6a00'
  },
  {
    id: 'kimi-k2.6',
    name: 'Kimi K2.6（百炼）',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'kimi-k2.6',
    modelVersion: 'k2.6',
    description: '阿里云百炼平台 Kimi K2.6 模型，月之暗面长文本专家',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 32000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.008,
    costPer1kOutput: 0.024,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'kimi-k2.7-code',
    name: 'Kimi K2.7 Code（百炼）',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'kimi-k2.7-code',
    modelVersion: 'k2.7-code',
    description: '阿里云百炼平台 Kimi K2.7 Code 编程专用模型，代码生成与理解',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 16000,
    temperature: 0.5,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.03,
    priority: 9,
    color: '#ff6a00'
  },
  {
    id: 'glm-5.2',
    name: 'GLM-5.2（百炼）',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'glm-5.2',
    modelVersion: 'latest',
    description: '阿里云百炼平台 GLM-5.2 模型，智谱新一代大模型',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 10000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.012,
    priority: 9,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.7-max',
    name: '通义千问 3.7 Max',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.7-max',
    modelVersion: 'latest',
    description: '阿里云百炼平台 Qwen 3.7 Max 旗舰模型，自动跟随最新版本',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 10000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.036,
    priority: 10,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.6-max-preview',
    name: '通义千问 3.6 Max 预览版',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.6-max-preview',
    modelVersion: 'preview',
    description: '阿里云百炼平台 Qwen 3.6 Max 预览版，体验最新能力',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 10000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.03,
    priority: 9,
    color: '#ff6a00'
  },
  {
    id: 'qwen3.5-plus-2026-04-20',
    name: '通义千问 3.5 Plus (2026-04-20)',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'qwen3.5-plus-2026-04-20',
    modelVersion: '2026-04-20',
    description: '阿里云百炼平台 Qwen 3.5 Plus 稳定版，2026年4月20日发布',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.006,
    costPer1kOutput: 0.018,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'glm-5.1',
    name: 'GLM-5.1（百炼）',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'glm-5.1',
    modelVersion: 'latest',
    description: '阿里云百炼平台 GLM-5.1 模型，智谱第五代大模型',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 8000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 120,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.01,
    priority: 9,
    color: '#ff6a00'
  },
  {
    id: 'deepseek-v4-flash',
    name: 'DeepSeek V4 Flash（百炼）',
    providerCode: 'qwen',
    providerName: '阿里云百炼',
    modelName: 'deepseek-v4-flash',
    modelVersion: 'flash',
    description: '阿里云百炼平台 DeepSeek V4 Flash 模型，轻量高效',
    apiEndpoint: 'https://llm-d61h5rzgd6kiqsfr.cn-beijing.maas.aliyuncs.com',
    maxTokens: 6000,
    temperature: 0.7,
    topP: 0.8,
    timeout: 60,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.001,
    costPer1kOutput: 0.003,
    priority: 5,
    color: '#ff6a00'
  },
  {
    id: 'deepseek-chat',
    name: 'DeepSeek Chat',
    providerCode: 'deepseek',
    providerName: 'DeepSeek',
    modelName: 'deepseek-chat',
    modelVersion: 'latest',
    description: 'DeepSeek 官方对话模型',
    apiEndpoint: 'https://api.deepseek.com/v1',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.001,
    costPer1kOutput: 0.002,
    priority: 8,
    color: '#4d6bfe'
  },
  {
    id: 'deepseek-reasoner',
    name: 'DeepSeek Reasoner',
    providerCode: 'deepseek',
    providerName: 'DeepSeek',
    modelName: 'deepseek-reasoner',
    modelVersion: 'latest',
    description: 'DeepSeek 推理模型，适合复杂问题求解',
    apiEndpoint: 'https://api.deepseek.com/v1',
    maxTokens: 8192,
    temperature: 1,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.016,
    priority: 9,
    color: '#4d6bfe'
  },
  {
    id: 'kimi-moonshot-8k',
    name: 'Kimi Moonshot 8K',
    providerCode: 'kimi',
    providerName: '月之暗面 Kimi',
    modelName: 'moonshot-v1-8k',
    modelVersion: 'v1',
    description: 'Kimi 官方 API 8K 上下文模型',
    apiEndpoint: 'https://api.moonshot.cn/v1',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.012,
    priority: 7,
    color: '#6366f1'
  },
  {
    id: 'kimi-moonshot-32k',
    name: 'Kimi Moonshot 32K',
    providerCode: 'kimi',
    providerName: '月之暗面 Kimi',
    modelName: 'moonshot-v1-32k',
    modelVersion: 'v1',
    description: 'Kimi 官方 API 32K 长上下文模型',
    apiEndpoint: 'https://api.moonshot.cn/v1',
    maxTokens: 32768,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.024,
    costPer1kOutput: 0.024,
    priority: 8,
    color: '#6366f1'
  },
  {
    id: 'kimi-moonshot-128k',
    name: 'Kimi Moonshot 128K',
    providerCode: 'kimi',
    providerName: '月之暗面 Kimi',
    modelName: 'moonshot-v1-128k',
    modelVersion: 'v1',
    description: 'Kimi 官方 API 128K 超长上下文模型',
    apiEndpoint: 'https://api.moonshot.cn/v1',
    maxTokens: 128000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.06,
    costPer1kOutput: 0.06,
    priority: 9,
    color: '#6366f1'
  },
  {
    id: 'qwen-vl-max',
    name: '通义千问 VL Max',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen-vl-max',
    modelVersion: 'latest',
    description: '通义千问视觉理解旗舰模型，支持图像问答、OCR、多图理解',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 6000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0.02,
    costPer1kOutput: 0.06,
    priority: 8,
    color: '#ff6a00'
  },
  {
    id: 'qwen-vl-plus',
    name: '通义千问 VL Plus',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'qwen-vl-plus',
    modelVersion: 'latest',
    description: '通义千问视觉理解均衡模型，图文多模态对话',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 6000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0.008,
    costPer1kOutput: 0.024,
    priority: 7,
    color: '#ff6a00'
  },
  {
    id: 'wanx2.1-t2i',
    name: '通义万相 · 文生图',
    providerCode: 'qwen',
    providerName: '阿里云通义万相',
    modelName: 'wanx2.1-t2i-plus',
    modelVersion: '2.1',
    description: '通义万相文生图模型，根据文本描述生成高质量图像',
    apiEndpoint: 'https://dashscope.aliyuncs.com/api/v1',
    maxTokens: 0,
    temperature: 0.7,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 6,
    color: '#ff6a00',
    configJson: JSON.stringify({
      requestSchema: 'wanx2.1-t2i',
      imageCapabilities: {
        supportsTextToImage: true,
        supportsReferenceImage: false,
        maxReferenceImages: 0,
        supportedAspectRatios: ['1:1', '16:9', '9:16']
      }
    })
  },
  {
    id: 'wanx2.1-imageedit',
    name: '通义万相 · 角色参考图',
    providerCode: 'qwen',
    providerName: '阿里云通义万相',
    modelName: 'wanx2.1-imageedit',
    modelVersion: '2.1',
    description: '通义万相角色参考图模型，基于参考图保持角色特征并生成新图像',
    apiEndpoint: 'https://dashscope.aliyuncs.com/api/v1',
    maxTokens: 0,
    temperature: 0.7,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 6,
    color: '#ff6a00',
    configJson: JSON.stringify({
      requestSchema: 'wanx2.1-imageedit',
      defaultFunction: 'control_cartoon_feature',
      imageCapabilities: {
        supportsTextToImage: false,
        supportsReferenceImage: true,
        maxReferenceImages: 1,
        supportedAspectRatios: ['1:1', '16:9', '9:16']
      }
    })
  },
  {
    id: 'wanx2.1-i2v',
    name: '通义万相 · 图生视频',
    providerCode: 'qwen',
    providerName: '阿里云通义万相',
    modelName: 'wanx2.1-i2v',
    modelVersion: '2.1',
    description: '通义万相图生视频模型，由静态图像生成短视频',
    apiEndpoint: 'https://dashscope.aliyuncs.com/api/v1',
    maxTokens: 0,
    temperature: 0.7,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 6,
    color: '#ff6a00'
  },
  {
    id: 'qwen-text-embedding-v3',
    name: '通义文本 Embedding V3',
    providerCode: 'qwen',
    providerName: '阿里云通义千问',
    modelName: 'text-embedding-v3',
    modelVersion: 'v3',
    description: '通义文本向量化模型，用于语义检索与重排',
    apiEndpoint: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    maxTokens: 8192,
    temperature: 0,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.0007,
    costPer1kOutput: 0,
    priority: 5,
    color: '#ff6a00'
  },
  {
    id: 'paraformer-v2',
    name: 'Paraformer 语音识别 V2',
    providerCode: 'qwen',
    providerName: '阿里云通义听悟',
    modelName: 'paraformer-v2',
    modelVersion: 'v2',
    description: '通义 Paraformer 自动语音识别模型',
    apiEndpoint: 'https://dashscope.aliyuncs.com/api/v1',
    maxTokens: 0,
    temperature: 0,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 5,
    color: '#ff6a00'
  },
  {
    id: 'cosyvoice-v1',
    name: 'CosyVoice 语音合成',
    providerCode: 'qwen',
    providerName: '阿里云通义听悟',
    modelName: 'cosyvoice-v1',
    modelVersion: 'v1',
    description: '通义 CosyVoice 语音和音频合成模型',
    apiEndpoint: 'https://dashscope.aliyuncs.com/api/v1',
    maxTokens: 0,
    temperature: 0.7,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 5,
    color: '#ff6a00'
  },
  {
    id: 'ernie-3.5-8k',
    name: '文心一言 3.5',
    providerCode: 'ernie',
    providerName: '百度文心一言',
    modelName: 'ernie-3.5-8k',
    modelVersion: 'latest',
    description: '百度文心 3.5 通用对话模型',
    apiEndpoint: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop',
    maxTokens: 8000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.008,
    costPer1kOutput: 0.008,
    priority: 6,
    color: '#2932e1'
  },
  {
    id: 'ernie-speed-128k',
    name: '文心一言 Speed 128K',
    providerCode: 'ernie',
    providerName: '百度文心一言',
    modelName: 'ernie-speed-128k',
    modelVersion: 'latest',
    description: '百度文心高速长上下文模型',
    apiEndpoint: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop',
    maxTokens: 128000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.004,
    priority: 7,
    color: '#2932e1'
  },
  {
    id: 'ernie-4',
    name: '文心一言 4.0',
    providerCode: 'ernie',
    providerName: '百度文心一言',
    modelName: 'ernie-4.0-8k',
    modelVersion: 'latest',
    description: '百度旗舰大模型',
    apiEndpoint: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop',
    maxTokens: 8000,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.012,
    costPer1kOutput: 0.012,
    priority: 7,
    color: '#2932e1'
  },
  {
    id: 'hunyuan-lite',
    name: '腾讯混元 Lite',
    providerCode: 'hunyuan',
    providerName: '腾讯混元',
    modelName: 'hunyuan-lite',
    modelVersion: 'latest',
    description: '腾讯混元轻量模型，高性价比',
    apiEndpoint: 'https://hunyuan.tencentcloudapi.com',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.004,
    costPer1kOutput: 0.004,
    priority: 5,
    color: '#006eff'
  },
  {
    id: 'hunyuan-standard',
    name: '腾讯混元 Standard',
    providerCode: 'hunyuan',
    providerName: '腾讯混元',
    modelName: 'hunyuan-standard',
    modelVersion: 'latest',
    description: '腾讯混元标准版通用模型',
    apiEndpoint: 'https://hunyuan.tencentcloudapi.com',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.008,
    costPer1kOutput: 0.008,
    priority: 6,
    color: '#006eff'
  },
  {
    id: 'hunyuan-pro',
    name: '腾讯混元 Pro',
    providerCode: 'hunyuan',
    providerName: '腾讯混元',
    modelName: 'hunyuan-pro',
    modelVersion: 'latest',
    description: '腾讯混元大模型专业版',
    apiEndpoint: 'https://hunyuan.tencentcloudapi.com',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.01,
    priority: 6,
    color: '#006eff'
  },
  {
    id: 'chatglm-4-plus',
    name: '智谱 GLM-4 Plus',
    providerCode: 'chatglm',
    providerName: '智谱AI',
    modelName: 'glm-4-plus',
    modelVersion: 'latest',
    description: '智谱 GLM-4 Plus 增强版对话模型',
    apiEndpoint: 'https://open.bigmodel.cn/api/paas/v4',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.01,
    priority: 8,
    color: '#1a73e8'
  },
  {
    id: 'chatglm-4-air',
    name: '智谱 GLM-4 Air',
    providerCode: 'chatglm',
    providerName: '智谱AI',
    modelName: 'glm-4-air',
    modelVersion: 'latest',
    description: '智谱 GLM-4 Air 轻量高速模型',
    apiEndpoint: 'https://open.bigmodel.cn/api/paas/v4',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.001,
    costPer1kOutput: 0.001,
    priority: 6,
    color: '#1a73e8'
  },
  {
    id: 'chatglm-4-flash',
    name: '智谱 GLM-4 Flash',
    providerCode: 'chatglm',
    providerName: '智谱AI',
    modelName: 'glm-4-flash',
    modelVersion: 'latest',
    description: '智谱 GLM-4 Flash 极速响应模型',
    apiEndpoint: 'https://open.bigmodel.cn/api/paas/v4',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.0001,
    costPer1kOutput: 0.0001,
    priority: 5,
    color: '#1a73e8'
  },
  {
    id: 'chatglm-4v',
    name: '智谱 GLM-4V',
    providerCode: 'chatglm',
    providerName: '智谱AI',
    modelName: 'glm-4v',
    modelVersion: 'latest',
    description: '智谱 GLM-4V 视觉多模态模型',
    apiEndpoint: 'https://open.bigmodel.cn/api/paas/v4',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.01,
    priority: 7,
    color: '#1a73e8'
  },
  {
    id: 'chatglm-4',
    name: '智谱 GLM-4',
    providerCode: 'chatglm',
    providerName: '智谱AI',
    modelName: 'glm-4',
    modelVersion: 'latest',
    description: '智谱 AI 第四代对话模型',
    apiEndpoint: 'https://open.bigmodel.cn/api/paas/v4',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 0,
    costPer1kInput: 0.01,
    costPer1kOutput: 0.01,
    priority: 7,
    color: '#1a73e8'
  },
  {
    id: 'gemini-1.5-pro',
    name: 'Google Gemini 1.5 Pro',
    providerCode: 'gemini',
    providerName: 'Google Gemini',
    modelName: 'gemini-1.5-pro',
    modelVersion: 'latest',
    description: 'Google Gemini 1.5 Pro 长上下文多模态模型',
    apiEndpoint: 'https://generativelanguage.googleapis.com/v1beta',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.0035,
    costPer1kOutput: 0.0105,
    priority: 8,
    color: '#4285f4'
  },
  {
    id: 'gemini-2.0-flash',
    name: 'Google Gemini 2.0 Flash',
    providerCode: 'gemini',
    providerName: 'Google Gemini',
    modelName: 'gemini-2.0-flash',
    modelVersion: 'latest',
    description: 'Google Gemini 2.0 Flash 高速多模态模型',
    apiEndpoint: 'https://generativelanguage.googleapis.com/v1beta',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.0001,
    costPer1kOutput: 0.0004,
    priority: 9,
    color: '#4285f4'
  },
  {
    id: 'gemini-pro',
    name: 'Google Gemini Pro',
    providerCode: 'gemini',
    providerName: 'Google Gemini',
    modelName: 'gemini-pro',
    modelVersion: 'latest',
    description: 'Google 多模态大模型',
    apiEndpoint: 'https://generativelanguage.googleapis.com/v1beta',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0.005,
    costPer1kOutput: 0.015,
    priority: 6,
    color: '#4285f4'
  },
  {
    id: 'claude-3-5-sonnet',
    name: 'Claude 3.5 Sonnet',
    providerCode: 'claude',
    providerName: 'Anthropic Claude',
    modelName: 'claude-3-5-sonnet-20241022',
    modelVersion: '20241022',
    description: 'Anthropic Claude 3.5 Sonnet 高性能模型',
    apiEndpoint: 'https://api.anthropic.com/v1',
    maxTokens: 8192,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.003,
    costPer1kOutput: 0.015,
    priority: 10,
    color: '#d97757'
  },
  {
    id: 'claude-3-opus',
    name: 'Claude 3 Opus',
    providerCode: 'claude',
    providerName: 'Anthropic Claude',
    modelName: 'claude-3-opus-20240229',
    modelVersion: '20240229',
    description: 'Anthropic Claude 3 Opus 旗舰模型',
    apiEndpoint: 'https://api.anthropic.com/v1',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.015,
    costPer1kOutput: 0.075,
    priority: 9,
    color: '#d97757'
  },
  {
    id: 'claude-3-haiku',
    name: 'Claude 3 Haiku',
    providerCode: 'claude',
    providerName: 'Anthropic Claude',
    modelName: 'claude-3-haiku-20240307',
    modelVersion: '20240307',
    description: 'Anthropic Claude 3 Haiku 轻量高速模型',
    apiEndpoint: 'https://api.anthropic.com/v1',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.00025,
    costPer1kOutput: 0.00125,
    priority: 7,
    color: '#d97757'
  },
  {
    id: 'claude-3-sonnet',
    name: 'Claude 3 Sonnet',
    providerCode: 'claude',
    providerName: 'Anthropic Claude',
    modelName: 'claude-3-sonnet-20240229',
    modelVersion: 'latest',
    description: 'Anthropic 均衡性能对话模型',
    apiEndpoint: 'https://api.anthropic.com/v1',
    maxTokens: 4096,
    temperature: 0.7,
    supportStream: 1,
    supportFunction: 1,
    supportVision: 1,
    costPer1kInput: 0.003,
    costPer1kOutput: 0.015,
    priority: 8,
    color: '#d97757'
  },
  {
    id: 'dify-workflow',
    name: 'Dify 工作流（文档识别）',
    providerCode: 'dify',
    providerName: 'Dify 文档识别',
    modelName: 'workflow',
    modelVersion: 'latest',
    description: 'Dify 工作流应用，用于小匠帮你传文档智能识别与自动入库',
    apiEndpoint: 'http://dify.shangliansoft.cn/v1',
    maxTokens: 4096,
    temperature: 0.3,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 20,
    color: '#7c3aed',
    configJson: JSON.stringify({
      appType: 'workflow',
      fileInputKey: 'file',
      descriptionInputKey: 'description',
      outputKey: 'result'
    })
  },
  {
    id: 'dify-chat',
    name: 'Dify 对话（文档识别）',
    providerCode: 'dify',
    providerName: 'Dify 文档识别',
    modelName: 'chat',
    modelVersion: 'latest',
    description: 'Dify 对话/Agent 应用，上传文件后由 AI 识别业务类型',
    apiEndpoint: 'http://dify.shangliansoft.cn/v1',
    maxTokens: 4096,
    temperature: 0.3,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 19,
    color: '#7c3aed',
    configJson: JSON.stringify({ appType: 'chat' })
  },
  {
    id: 'volcengine-digital-human',
    name: '火山引擎智能数字人',
    providerCode: 'volcengine-avatar',
    providerName: '火山引擎数字人',
    modelName: 'volcengine-digital-human',
    modelVersion: 'latest',
    category: 'avatar',
    description: '火山引擎智能数字人口播，支持形象、音色、唇形同步和异步成片',
    apiEndpoint: '',
    maxTokens: 0,
    temperature: 0,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    priority: 20,
    color: '#111827',
    configJson: JSON.stringify({ avatarId: '', voiceId: '', callbackUrl: 'https://geo.shangliansoft.cn/geo/public/video-callbacks/volcengine-avatar', projectId: '' })
  },
  {
    id: 'tencent-digital-human',
    name: '腾讯云智能数智人',
    providerCode: 'tencent-avatar',
    providerName: '腾讯云数字人',
    modelName: 'tencent-digital-human',
    modelVersion: 'latest',
    category: 'avatar',
    description: '腾讯云智能数智人口播供应商配置',
    apiEndpoint: '',
    maxTokens: 0,
    temperature: 0,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    priority: 10,
    color: '#006eff',
    configJson: JSON.stringify({ region: '', avatarId: '', voiceId: '', callbackUrl: '', projectId: '' })
  },
  {
    id: 'aliyun-digital-human',
    name: '阿里云数字人',
    providerCode: 'aliyun-avatar',
    providerName: '阿里云数字人',
    modelName: 'aliyun-digital-human',
    modelVersion: 'latest',
    category: 'avatar',
    description: '阿里云数字人口播供应商配置，与通义万相视频生成凭据分开管理',
    apiEndpoint: '',
    maxTokens: 0,
    temperature: 0,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    priority: 10,
    color: '#ff6a00',
    configJson: JSON.stringify({ region: '', avatarId: '', voiceId: '', callbackUrl: '', projectId: '', templateId: '', audioVariableName: 'slide_audio' })
  },
  {
    id: 'remotion-render',
    name: 'Remotion 成片渲染',
    providerCode: 'remotion-render',
    providerName: 'Remotion / Hyperframes',
    modelName: 'remotion-render',
    modelVersion: 'latest',
    category: 'video',
    description: '短视频成片合成渲染服务（remotion-render / hyperframes 别名互通），需配置可访问的渲染 Endpoint',
    apiEndpoint: '',
    maxTokens: 0,
    temperature: 0,
    supportStream: 0,
    supportFunction: 0,
    supportVision: 1,
    priority: 15,
    color: '#0f766e',
    configJson: JSON.stringify({ compositionId: 'GeoShortVideo', webhookUrl: '', outputFormat: 'mp4' })
  },
  {
    id: CUSTOM_TEMPLATE_ID,
    name: '自定义模板',
    providerCode: 'custom',
    providerName: '自定义服务商',
    modelName: '',
    description: '手动填写服务商、模型与 API 参数，适用于私有部署或小众模型',
    apiEndpoint: '',
    maxTokens: 2000,
    temperature: 0.7,
    topP: 1,
    timeout: 60,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 1,
    color: '#909399',
    isCustom: true
  }
]

export const PLATFORM_TABS = [
  { name: 'all', label: '全部' },
  { name: 'openai', label: 'OpenAI', color: '#10a37f' },
  { name: 'qwen', label: '阿里云', color: '#ff6a00' },
  { name: 'ernie', label: '百度', color: '#2932e1' },
  { name: 'hunyuan', label: '腾讯', color: '#006eff' },
  { name: 'chatglm', label: '智谱AI', color: '#1a73e8' },
  { name: 'gemini', label: 'Google', color: '#4285f4' },
  { name: 'claude', label: 'Claude', color: '#d97757' },
  { name: 'custom', label: '自定义', color: '#909399' },
  { name: 'other', label: '其他', color: '#606266' }
]

const MAIN_PROVIDERS = ['openai', 'qwen', 'ernie', 'hunyuan', 'chatglm', 'gemini', 'claude', 'custom']

export function filterByPlatform(list, platform) {
  if (platform === 'all') return list
  if (platform === 'other') {
    return list.filter(item => !MAIN_PROVIDERS.includes(item.providerCode))
  }
  return list.filter(item => item.providerCode === platform)
}

export function getProviderColor(code) {
  const tab = PLATFORM_TABS.find(t => t.name === code)
  return tab?.color || '#409eff'
}

export function matchTemplateByConfig(config) {
  if (!config) return CUSTOM_TEMPLATE_ID
  if (config.providerCode === 'custom') return CUSTOM_TEMPLATE_ID
  const sameModel = AI_PROVIDER_TEMPLATES.filter(
    t => !t.isCustom && t.providerCode === config.providerCode
      && normalizeModelName(t.modelName) === normalizeModelName(config.modelName)
  )
  const exactKey = modelConfigKey(config)
  const matched = sameModel.find(t => modelConfigKey(t) === exactKey)
    || (normalizeEndpoint(config.apiEndpoint)
      ? sameModel.find(t => normalizeEndpoint(t.apiEndpoint) === normalizeEndpoint(config.apiEndpoint))
      : sameModel.find(t => normalizeEndpoint(t.apiEndpoint) === 'https://api.openai.com/v1'))
    || sameModel[0]
  return matched?.id || CUSTOM_TEMPLATE_ID
}

export function applyTemplateToForm(template, keepApiKey = false, existingApiKey = '') {
  if (!template) return {}
  const base = {
    providerCode: template.providerCode,
    providerName: template.providerName,
    modelName: template.modelName,
    modelVersion: template.modelVersion || '',
    description: template.description || '',
    apiEndpoint: template.apiEndpoint || '',
    maxTokens: template.maxTokens ?? 2000,
    temperature: template.temperature ?? 0.7,
    topP: template.topP ?? 1,
    timeout: template.timeout ?? 60,
    retryCount: template.retryCount ?? 3,
    supportStream: template.supportStream ?? 1,
    supportFunction: template.supportFunction ?? 0,
    supportVision: template.supportVision ?? 0,
    costPer1kInput: template.costPer1kInput ?? 0,
    costPer1kOutput: template.costPer1kOutput ?? 0,
    priority: template.priority ?? 1
  }
  if (template.configJson) {
    base.configJson = template.configJson
  }
  if (!keepApiKey) {
    base.apiKey = ''
    base.apiSecret = ''
    base.appId = ''
  } else if (existingApiKey) {
    base.apiKey = existingApiKey
  }
  return base
}

/** 新增配置弹窗：按服务商/模型系列分 Tab */
export const TEMPLATE_PROVIDER_TABS = [
  { name: 'all', label: '全部' },
  { name: 'openai', label: 'OpenAI', color: '#10a37f' },
  { name: 'qwen', label: '通义千问', color: '#ff6a00' },
  { name: 'deepseek', label: 'DeepSeek', color: '#4d6bfe' },
  { name: 'kimi', label: 'Kimi', color: '#6366f1' },
  { name: 'glm', label: 'GLM 智谱', color: '#1a73e8' },
  { name: 'ernie', label: '百度文心', color: '#2932e1' },
  { name: 'hunyuan', label: '腾讯混元', color: '#006eff' },
  { name: 'gemini', label: 'Google', color: '#4285f4' },
  { name: 'claude', label: 'Claude', color: '#d97757' },
  { name: 'dify', label: 'Dify', color: '#7c3aed' },
  { name: 'custom', label: '自定义', color: '#909399' }
]

export function matchesTemplateSearch(tpl, keyword) {
  if (!keyword?.trim()) return true
  const kw = keyword.trim().toLowerCase()
  if (tpl.isCustom) {
    return ['自定义', 'custom', '自由配置'].some(s => s.includes(kw))
      || kw.includes('自定义') || kw.includes('custom')
  }
  const fields = [
    tpl.name,
    tpl.providerName,
    tpl.providerCode,
    tpl.modelName,
    tpl.modelVersion,
    tpl.description
  ]
  return fields.some(f => (f || '').toString().toLowerCase().includes(kw))
}

export function filterTemplatesByProviderTab(templates, tabName) {
  if (!templates?.length) return []
  if (tabName === 'all') return templates
  if (tabName === 'custom') return templates.filter(t => t.isCustom)
  if (tabName === 'deepseek') {
    return templates.filter(t => !t.isCustom && (
      t.providerCode === 'deepseek' || /deepseek/i.test(t.modelName || '')
    ))
  }
  if (tabName === 'kimi') {
    return templates.filter(t => !t.isCustom && (
      t.providerCode === 'kimi' || /kimi|moonshot/i.test(t.modelName || '')
    ))
  }
  if (tabName === 'glm') {
    return templates.filter(t => !t.isCustom && (
      t.providerCode === 'chatglm' || /^glm/i.test(t.modelName || '')
    ))
  }
  if (tabName === 'qwen') {
    return templates.filter(t => !t.isCustom && t.providerCode === 'qwen'
      && !/deepseek|kimi|glm|moonshot/i.test(t.modelName || ''))
  }
  return templates.filter(t => !t.isCustom && t.providerCode === tabName)
}

/** 按 Tab + 关键词筛选模板（弹窗用） */
export function filterDialogTemplates(templates, tabName, keyword = '') {
  const byTab = filterTemplatesByProviderTab(templates, tabName)
  if (!keyword?.trim()) return byTab
  return byTab.filter(t => matchesTemplateSearch(t, keyword))
}

export function countTemplatesForTab(templates, tabName, keyword = '') {
  return filterDialogTemplates(templates, tabName, keyword).length
}

/** 模型能力分类（对齐百炼/云控制台分类导航） */
export const MODEL_CATEGORIES = [
  { key: 'all', label: '全部模型' },
  { key: 'text', label: '文本生成' },
  { key: 'vision', label: '视觉模型' },
  { key: 'image', label: '图像生成与处理' },
  { key: '3d', label: '3D 生成' },
  { key: 'ocr', label: '文档处理/OCR' },
  { key: 'video', label: '视频生成' },
  { key: 'avatar', label: '数字人' },
  { key: 'asr', label: '自动语音识别' },
  { key: 'tts', label: '语音和音频合成' },
  { key: 'embedding', label: '向量化和重排' },
  { key: 'risk', label: '风控识别' },
  { key: 'industry', label: '行业大模型' }
]

/** @deprecated 使用 MODEL_CATEGORIES */
export const MODEL_MODALITIES = MODEL_CATEGORIES.filter(c => c.key !== 'all')

const PROVIDER_GROUP_META = {
  openai: { label: 'OpenAI', color: '#10a37f' },
  qwen: { label: '阿里云 · 通义千问', color: '#ff6a00' },
  deepseek: { label: 'DeepSeek（百炼）', color: '#4d6bfe' },
  kimi: { label: 'Kimi（月之暗面）', color: '#6366f1' },
  glm: { label: '智谱 GLM', color: '#1a73e8' },
  chatglm: { label: '智谱 GLM', color: '#1a73e8' },
  ernie: { label: '百度 · 文心一言', color: '#2932e1' },
  hunyuan: { label: '腾讯 · 混元', color: '#006eff' },
  gemini: { label: 'Google Gemini', color: '#4285f4' },
  claude: { label: 'Anthropic Claude', color: '#d97757' },
  'volcengine-avatar': { label: '火山引擎数字人', color: '#111827' },
  'tencent-avatar': { label: '腾讯云数智人', color: '#006eff' },
  'aliyun-avatar': { label: '阿里云数字人', color: '#ff6a00' },
  'remotion-render': { label: 'Remotion 渲染', color: '#0f766e' },
  custom: { label: '自定义服务商', color: '#909399' },
  other: { label: '其他', color: '#606266' }
}

const PROVIDER_GROUP_ORDER = [
  'openai', 'qwen', 'deepseek', 'kimi', 'glm', 'ernie', 'hunyuan', 'gemini', 'claude',
  'volcengine-avatar', 'tencent-avatar', 'aliyun-avatar', 'remotion-render', 'custom', 'other'
]

/** 推断模型能力分类 */
export function inferModelCategory(item) {
  if (item?.supportVision === 1 || item?.config?.supportVision === 1) return 'vision'
  const name = (item?.modelName || '').toLowerCase()
  const desc = (item?.description || '').toLowerCase()
  const text = `${name} ${desc}`

  if (/digital[ -]?human|avatar|virtual[ -]?human|数字人|数智人/i.test(text)) return 'avatar'
  if (/3d|tripo|mesh|三维|3-d/i.test(text)) return '3d'
  if (/remotion|hyperframes|成片渲染|render.?service/i.test(text)) return 'video'
  if (/video|图生视频|cogvideo|kling|sora|gen-?3|animate|i2v|t2v|视频生成/i.test(text)) return 'video'
  if (/asr|whisper|paraformer|speech.?recog|语音识别|听写|speech-to-text/i.test(text)) return 'asr'
  if (/tts|cosyvoice|speech.?synth|语音合成|text-to-speech|音频合成/i.test(text)) return 'tts'
  if (/ocr|document|doc-|文档处理|文字识别|pdf/i.test(text)) return 'ocr'
  if (/wanx|t2i|i2i|dall-?e|stable.?diff|文生图|图像生成|image.?gen|图生图/i.test(text)) return 'image'
  if (/embed|embedding|rerank|向量|重排|text-embedding/i.test(text)) return 'embedding'
  if (/risk|moderation|content.?safe|风控|内容审核|安全/i.test(text)) return 'risk'
  if (/industry|金融|医疗|法律|行业大模型|vertical/i.test(text)) return 'industry'
  if (/vl-|qwen-vl|glm-4v|gpt-4o|vision|multimodal|视觉理解|图像理解|视觉模型/i.test(text)) return 'vision'
  return 'text'
}

/** @deprecated 使用 inferModelCategory */
export function inferModelModality(item) {
  const cat = inferModelCategory(item)
  const legacy = {
    text: 'chat', vision: 'vision', image: 'vision', video: 'video',
    asr: 'audio', tts: 'audio', embedding: 'other', '3d': 'other',
    ocr: 'vision', risk: 'other', industry: 'chat'
  }
  return legacy[cat] || 'chat'
}

/** 列表分组用的服务商 key（百炼托管模型单独成组） */
export function resolveProviderGroupKey(item) {
  if (!item) return 'other'
  if (item.providerCode === 'custom') return 'custom'
  if (item.providerCode === 'deepseek') return 'deepseek'
  if (item.providerCode === 'kimi') return 'kimi'
  const name = (item.modelName || '').toLowerCase()
  if (/deepseek/i.test(name)) return 'deepseek'
  if (/kimi|moonshot/i.test(name)) return 'kimi'
  if (item.providerCode === 'chatglm' || /^glm/i.test(name)) return 'glm'
  if (PROVIDER_GROUP_META[item.providerCode]) return item.providerCode
  return 'other'
}

function matchPlatformFilter(item, platform) {
  if (!platform || platform === 'all') return true
  if (platform === 'other') {
    return !MAIN_PROVIDERS.includes(item.providerCode) && item.groupKey === 'other'
  }
  if (platform === 'qwen') {
    return ['qwen', 'deepseek', 'kimi', 'glm'].includes(item.groupKey) || item.providerCode === 'qwen'
  }
  if (platform === 'chatglm') return item.groupKey === 'glm' || item.providerCode === 'chatglm'
  return item.groupKey === platform || item.providerCode === platform
}

function matchCategoryFilter(item, category) {
  if (!category || category === 'all') return true
  return (item.category || inferModelCategory(item)) === category
}

function mergeCatalogItems(templates, configs) {
  const configMap = new Map()
  configs.forEach((c) => {
    configMap.set(modelConfigKey(c), c)
  })

  const catalogItems = []

  templates.filter(t => !t.isCustom).forEach((tpl) => {
    const mapKey = modelConfigKey(tpl)
    const saved = configMap.get(mapKey)
    const merged = {
      templateId: tpl.id,
      providerCode: tpl.providerCode,
      providerName: tpl.providerName,
      modelName: tpl.modelName,
      modelVersion: tpl.modelVersion || saved?.modelVersion || '',
      description: tpl.description || saved?.description || '',
      apiEndpoint: tpl.apiEndpoint || saved?.apiEndpoint || '',
      color: tpl.color || getProviderColor(tpl.providerCode),
      supportStream: saved?.supportStream ?? tpl.supportStream,
      supportFunction: saved?.supportFunction ?? tpl.supportFunction,
      supportVision: saved?.supportVision ?? tpl.supportVision,
      configured: !!saved,
      config: saved || null
    }
    merged.groupKey = resolveProviderGroupKey(merged)
    merged.category = inferModelCategory(merged)
    catalogItems.push(merged)
    if (saved) configMap.delete(mapKey)
  })

  configMap.forEach((cfg) => {
    const item = {
      templateId: matchTemplateByConfig(cfg) !== CUSTOM_TEMPLATE_ID ? matchTemplateByConfig(cfg) : null,
      providerCode: cfg.providerCode,
      providerName: cfg.providerName,
      modelName: cfg.modelName,
      modelVersion: cfg.modelVersion || '',
      description: cfg.description || '',
      apiEndpoint: cfg.apiEndpoint || '',
      color: getProviderColor(cfg.providerCode),
      supportStream: cfg.supportStream,
      supportFunction: cfg.supportFunction,
      supportVision: cfg.supportVision,
      configured: true,
      config: cfg
    }
    item.groupKey = resolveProviderGroupKey(item)
    item.category = inferModelCategory(item)
    catalogItems.push(item)
  })

  return catalogItems
}

function applyCatalogFilters(items, { keyword, enabledFilter }) {
  let filtered = items

  if (keyword?.trim()) {
    const kw = keyword.trim().toLowerCase()
    filtered = filtered.filter(i =>
      (i.modelName || '').toLowerCase().includes(kw)
      || (i.providerName || '').toLowerCase().includes(kw)
      || (i.description || '').toLowerCase().includes(kw)
    )
  }

  if (enabledFilter != null) {
    filtered = filtered.filter(i => {
      if (!i.configured) return enabledFilter === 0
      return i.config?.isEnabled === enabledFilter
    })
  }

  return filtered
}

/** 左侧分类导航：各分类模型数量 */
export function buildCategoryNav(options) {
  const {
    templates = [],
    configs = [],
    keyword = '',
    enabledFilter = null
  } = options

  const items = applyCatalogFilters(mergeCatalogItems(templates, configs), { keyword, enabledFilter })
  const countMap = { all: items.length }
  MODEL_CATEGORIES.filter(c => c.key !== 'all').forEach(c => { countMap[c.key] = 0 })
  items.forEach((item) => {
    const k = item.category || 'text'
    countMap[k] = (countMap[k] || 0) + 1
  })

  return MODEL_CATEGORIES.map(c => ({
    ...c,
    count: countMap[c.key] ?? 0
  }))
}

/**
 * 合并模板目录与已保存配置，按分类筛选后按服务商分组
 */
export function buildGroupedModelCatalog(options) {
  const {
    templates = [],
    configs = [],
    category = 'all',
    keyword = '',
    enabledFilter = null
  } = options

  let filtered = applyCatalogFilters(mergeCatalogItems(templates, configs), { keyword, enabledFilter })
  filtered = filtered.filter(i => matchCategoryFilter(i, category))

  const groupMap = {}
  filtered.forEach((item) => {
    const gk = item.groupKey
    if (!groupMap[gk]) {
      groupMap[gk] = { key: gk, items: [] }
    }
    groupMap[gk].items.push(item)
  })

  const orderedKeys = [
    ...PROVIDER_GROUP_ORDER.filter(k => groupMap[k]),
    ...Object.keys(groupMap).filter(k => !PROVIDER_GROUP_ORDER.includes(k))
  ]

  return orderedKeys.map((k) => {
    const g = groupMap[k]
    const meta = PROVIDER_GROUP_META[k] || { label: k, color: '#409eff' }
    return {
      key: k,
      label: meta.label,
      color: meta.color,
      total: g.items.length,
      configured: g.items.filter(i => i.configured).length,
      enabled: g.items.filter(i => i.config?.isEnabled === 1).length,
      items: g.items
    }
  })
}

/**
 * 品牌凭证归组 key：端点非空按规范化端点分组，否则按 providerCode 分组
 * 用于「一个 API 端点 = 一把共享密钥 = 一张品牌卡片」的聚合视图
 */
export function resolveCredentialGroupKey(item) {
  if (!item) return 'pc::other'
  const providerCode = item.providerCode || 'other'
  if (/^openai$/i.test(providerCode)) return 'pc::openai'
  // Dify 同一服务器上承载多个不同用途的应用（文档识别 / 知识库），各自独立密钥；
  // 按用途（providerCode）归组而非端点，避免 http/https 端点差异误拆或不同用途误合。
  if (/^dify/i.test(providerCode)) {
    return `pc::${providerCode}`
  }
  const endpoint = (item.apiEndpoint || item.config?.apiEndpoint || '').trim()
  if (endpoint) {
    return `ep::${endpoint.replace(/\/+$/, '').toLowerCase()}`
  }
  return `pc::${providerCode}`
}

/**
 * 按 API 端点（品牌）聚合模板与配置：同端点的多个模型共用一把密钥，聚合为一张卡片。
 * 复用 mergeCatalogItems / applyCatalogFilters / matchCategoryFilter，保持统计口径一致。
 */
export function buildProviderCredentialGroups(options) {
  const {
    templates = [],
    configs = [],
    category = 'all',
    keyword = '',
    enabledFilter = null
  } = options

  let filtered = applyCatalogFilters(mergeCatalogItems(templates, configs), { keyword, enabledFilter })
  filtered = filtered.filter(i => matchCategoryFilter(i, category))

  const groupMap = {}
  filtered.forEach((item) => {
    const gk = resolveCredentialGroupKey(item)
    if (!groupMap[gk]) {
      groupMap[gk] = { key: gk, items: [] }
    }
    groupMap[gk].items.push(item)
  })

  const groups = Object.values(groupMap).map((g) => {
    const rawModels = g.items
    const groupProviderCode = rawModels[0]?.providerCode || ''
    const modelMap = new Map()
    rawModels.forEach((model) => {
      const key = normalizeModelName(model.modelName)
      const existing = modelMap.get(key)
      if (!existing || (!existing.configured && model.configured)) modelMap.set(key, model)
    })
    const models = groupProviderCode === 'openai'
      ? [...modelMap.values()]
      : rawModels
    // label：取组内出现最多的 providerName（同端点多 providerName 时用出现最多者）
    const nameCount = {}
    models.forEach((m) => {
      const n = m.providerName || ''
      if (n) nameCount[n] = (nameCount[n] || 0) + 1
    })
    const baseLabel = Object.keys(nameCount).sort((a, b) => nameCount[b] - nameCount[a])[0]
      || models[0]?.providerName || models[0]?.providerCode || '未知品牌'
    const firstConfigured = models.find(m => m.configured)
    const representativeModel = firstConfigured || models[0]
    const representativeConfig = firstConfigured?.config || null
    const providerCode = representativeModel?.providerCode || rawModels[0]?.providerCode || ''
    const apiEndpoint = representativeConfig?.apiEndpoint || representativeModel?.apiEndpoint || ''
    const color = representativeModel?.color || getProviderColor(providerCode)
    const isAvatar = /avatar/i.test(providerCode)
    // 服务类型标签：由组内模型的能力分类去重得到，用于区分大模型 / 数字人 / 语音等不同服务
    const categoryKeys = [...new Set(models.map(m => m.category).filter(Boolean))]
    const typeLabels = categoryKeys.map(k => (DEFAULT_MODEL_CATEGORY_ROWS.find(r => r.key === k)?.label) || k)
    // 主功能类型（组内模型数量最多的能力分类），用于同名品牌卡片的消歧后缀
    const catCount = {}
    models.forEach((m) => { if (m.category) catCount[m.category] = (catCount[m.category] || 0) + 1 })
    const primaryCatKey = Object.keys(catCount).sort((a, b) => catCount[b] - catCount[a])[0] || ''
    const primaryTypeLabel = (DEFAULT_MODEL_CATEGORY_ROWS.find(r => r.key === primaryCatKey)?.label) || ''
    const apiKeyConfigured = models.some(m => m.config?.apiKey || m.config?.apiKeyConfigured)
    return {
      key: g.key,
      label: baseLabel,
      baseLabel,
      primaryTypeLabel,
      color,
      apiEndpoint,
      providerCode,
      models,
      total: models.length,
      configured: models.filter(m => m.configured).length,
      enabled: models.filter(m => m.config?.isEnabled === 1).length,
      apiKeyConfigured,
      isAvatar,
      categoryKeys,
      typeLabels,
      representativeConfig
    }
  })

  // 同名品牌卡片消歧：同一品牌名出现多张卡片时，追加主功能类型后缀
  // （如「阿里云百炼（文本模型）」「阿里云百炼（语音合成）」），避免用户混淆
  const baseLabelCount = {}
  groups.forEach((g) => { baseLabelCount[g.baseLabel] = (baseLabelCount[g.baseLabel] || 0) + 1 })
  groups.forEach((g) => {
    if (baseLabelCount[g.baseLabel] > 1 && g.primaryTypeLabel) {
      g.label = `${g.baseLabel}（${g.primaryTypeLabel}）`
    }
  })

  // 组顺序：同一品牌名的卡片相邻排列；同名内再按 providerCode 顺序与完整标签
  const orderIndex = (pc) => {
    const idx = PROVIDER_GROUP_ORDER.indexOf(pc)
    return idx === -1 ? PROVIDER_GROUP_ORDER.length : idx
  }
  groups.sort((a, b) => {
    const nameDiff = (a.baseLabel || '').localeCompare(b.baseLabel || '', 'zh')
    if (nameDiff !== 0) return nameDiff
    const d = orderIndex(a.providerCode) - orderIndex(b.providerCode)
    if (d !== 0) return d
    return (a.label || '').localeCompare(b.label || '', 'zh')
  })

  return groups
}

/** 默认模型弹窗展示的能力分类（对齐百炼主分类 + 扩展项） */
export const DEFAULT_MODEL_CATEGORY_ROWS = [
  { key: 'text', label: '文本模型' },
  { key: 'vision', label: '视觉模型' },
  { key: 'asr', label: '语音识别' },
  { key: 'tts', label: '语音合成' },
  { key: 'embedding', label: '向量模型' },
  { key: 'image', label: '图像生成' },
  { key: 'video', label: '视频生成' },
  { key: 'avatar', label: '数字人' },
  { key: 'ocr', label: '文档/OCR' },
  { key: '3d', label: '3D 生成' },
  { key: 'risk', label: '风控识别' },
  { key: 'industry', label: '行业大模型' }
]

function pickBestEnabledForCategory(enabledItems, catKey) {
  const matches = enabledItems.filter(i => matchCategoryFilter(i, catKey))
  if (!matches.length) return null
  // 优先取该功能类型下显式设置的默认（每种类型各管各的），否则取优先级最高的已启用模型
  const explicitDefault = matches.find(m => m.config?.isDefault === 1)
  if (explicitDefault) return explicitDefault
  return matches.sort((a, b) => {
    const priorityDiff = (b.config?.priority ?? 0) - (a.config?.priority ?? 0)
    if (priorityDiff !== 0) return priorityDiff
    return (b.config?.sortOrder ?? 0) - (a.config?.sortOrder ?? 0)
  })[0]
}

/** 解析各能力分类当前首选已启用模型（用于默认模型概览弹窗） */
export function buildCategoryDefaultAssignments(options) {
  const { templates = [], configs = [] } = options
  const items = mergeCatalogItems(templates, configs)
  const enabledItems = items.filter(i => i.configured && i.config?.isEnabled === 1)

  return DEFAULT_MODEL_CATEGORY_ROWS.map((cat) => {
    const item = pickBestEnabledForCategory(enabledItems, cat.key)
    return {
      ...cat,
      modelName: item?.modelName || '',
      providerName: item?.providerName || '',
      configured: !!item,
      isDefault: !!(item && item.config?.isDefault === 1)
    }
  })
}
