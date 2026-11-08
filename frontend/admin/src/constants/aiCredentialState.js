export function buildCredentialPayload(form) {
  const payload = { ...form }
  delete payload.apiKeyConfigured
  delete payload.apiSecretConfigured

  if (payload.id && form.apiKeyConfigured && !payload.apiKey) {
    delete payload.apiKey
  }
  if (payload.id && form.apiSecretConfigured && !payload.apiSecret) {
    delete payload.apiSecret
  }
  return payload
}

export function getSecretKeyPlaceholder(isAvatar, isConfigured) {
  if (isConfigured) {
    return isAvatar
      ? '已配置，留空则保持原 Secret Key'
      : '已配置，留空则保持原密钥'
  }
  return isAvatar ? '请输入供应商 Secret Key' : '部分服务商需要（可选）'
}
