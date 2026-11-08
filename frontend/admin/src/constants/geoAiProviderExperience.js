export const PROVIDER_APPLICATION_URLS = Object.freeze({
  openai: 'https://platform.openai.com/api-keys',
  qwen: 'https://bailian.console.aliyun.com/',
  deepseek: 'https://platform.deepseek.com/api_keys',
  kimi: 'https://platform.moonshot.cn/console/api-keys',
  chatglm: 'https://open.bigmodel.cn/usercenter/apikeys',
  ernie: 'https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application',
  wenxin: 'https://console.bce.baidu.com/qianfan/ais/console/applicationConsole/application',
  hunyuan: 'https://console.cloud.tencent.com/hunyuan/api-key',
  gemini: 'https://aistudio.google.com/apikey',
  google: 'https://aistudio.google.com/apikey',
  claude: 'https://console.anthropic.com/settings/keys',
  volcengine: 'https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey',
  kling: 'https://app.klingai.com/global/dev',
  minimax: 'https://platform.minimaxi.com/user-center/basic-information/interface-key',
  'volcengine-avatar': 'https://www.volcengine.com/product/avatar',
  'tencent-avatar': 'https://console.cloud.tencent.com/ivh',
  'aliyun-avatar': 'https://avatar.console.aliyun.com/',
  // Remotion Lambda / 自建渲染；Hyperframes 走 HeyGen API Key
  'remotion-render': 'https://www.remotion.dev/docs/lambda',
  hyperframes: 'https://app.heygen.com/settings?nav=API'
})

export function getProviderApplicationUrl(providerCode) {
  return PROVIDER_APPLICATION_URLS[providerCode] || ''
}

export function focusTemplateCard(container, templateId) {
  if (!container || !templateId) return false
  const card = [...container.querySelectorAll('[data-template-id]')]
    .find(item => item.dataset.templateId === String(templateId))
  if (!card) return false
  card.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' })
  return true
}
