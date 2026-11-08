import { buildCategoryDefaultAssignments } from './aiProviderTemplates.js'

export const CORE_DEFAULT_MODEL_GROUPS = [
  { key: 'text', label: '文本模型', categories: ['text'] },
  { key: 'vision', label: '视觉理解', categories: ['vision'] },
  { key: 'image', label: '图像生成', categories: ['image'] },
  { key: 'video', label: '视频生成', categories: ['video'] },
  { key: 'speech', label: '语音', categories: ['asr', 'tts'] },
  { key: 'avatar', label: '数字人', categories: ['avatar'] }
]

export function buildCoreDefaultSummary(options = {}) {
  const assignments = buildCategoryDefaultAssignments(options)
  const assignmentByCategory = new Map(assignments.map((item) => [item.key, item]))

  return CORE_DEFAULT_MODEL_GROUPS.map((group) => ({
    ...group,
    items: group.categories.map((category) => {
      const assignment = assignmentByCategory.get(category)
      return {
        category,
        categoryLabel: assignment?.label || category,
        modelName: assignment?.modelName || '',
        providerName: assignment?.providerName || '',
        configured: Boolean(assignment?.configured),
        isDefault: Boolean(assignment?.isDefault)
      }
    })
  }))
}
