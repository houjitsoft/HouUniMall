const CORE_METRICS = new Set(['lcp', 'inp', 'cls'])

export function normalizeCwvRun(input = {}) {
  return {
    ...input,
    devices: (input.devices || []).map(device => ({
      ...device,
      lab: device.lab || {},
      field: device.field || {},
      metrics: device.metrics || {},
      recommendations: device.recommendations || [],
      fieldStatus: device.fieldStatus || 'insufficient-sample',
      fieldDataAvailable: device.fieldDataAvailable ?? Object.keys(device.field || {}).length > 0,
    })),
  }
}

export function selectedMetric(device = {}, metric) {
  const field = device.field?.[metric]
  if (CORE_METRICS.has(metric) && field?.percentile != null) {
    return { value: Number(field.percentile), rating: field.rating || device.metrics?.[metric]?.googleRating || 'unavailable', source: 'field' }
  }
  const lab = device.lab?.[metric]
  if (lab != null) {
    return { value: Number(lab), rating: device.metrics?.[metric]?.googleRating || 'unavailable', source: 'lab' }
  }
  return { value: null, rating: 'unavailable', source: 'none' }
}

export function formatCwvMetric(metric, value) {
  if (value == null || Number.isNaN(Number(value))) return '未采集'
  if (metric === 'cls') return Number(value).toFixed(3).replace(/0+$/, '').replace(/\.$/, '')
  return `${Math.round(Number(value)).toLocaleString('zh-CN')} ms`
}

export function metricSource(device, metric) {
  const selected = selectedMetric(device, metric)
  if (selected.source === 'field') return 'CrUX 真实用户数据'
  if (selected.source === 'lab') return 'Lighthouse 实验室数据'
  if (CORE_METRICS.has(metric) && device?.fieldStatus === 'insufficient-sample') return 'CrUX 样本不足'
  return '未采集'
}

export function ratingLabel(rating) {
  return ({ good: '良好', 'needs-improvement': '需要改进', poor: '较差', unavailable: '未采集' })[rating] || '未采集'
}

export function ratingTag(rating) {
  return ({ good: 'success', 'needs-improvement': 'warning', poor: 'danger' })[rating] || 'info'
}

export function deviceLabel(strategy) {
  return strategy === 'desktop' ? '桌面端' : '移动端'
}
