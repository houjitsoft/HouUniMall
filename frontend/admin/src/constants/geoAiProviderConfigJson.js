export function parseProviderOptions(value) {
  if (!value) return {}
  try {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch {
    return {}
  }
}

export function updateProviderOptions(currentValue, updates = {}) {
  return JSON.stringify({ ...parseProviderOptions(currentValue), ...updates })
}
