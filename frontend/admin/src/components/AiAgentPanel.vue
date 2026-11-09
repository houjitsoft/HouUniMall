<template>
  <span class="ai-agent-trigger">
    <el-button
      :size="size"
      :type="type"
      :plain="link ? false : plain"
      :link="link"
      :disabled="disabled"
      :loading="loading"
      @click="run"
    >
      <el-icon v-if="!loading" class="ai-agent-trigger__icon"><MagicStick /></el-icon>
      {{ loading ? '生成中…' : label }}
    </el-button>
  </span>

  <el-drawer
    v-model="visible"
    :title="title || 'AI 智能体结果'"
    :size="size_"
    destroy-on-close
    class="ai-agent-drawer"
  >
    <div v-if="error" class="ai-agent-error">
      <el-alert :title="error" type="warning" :closable="false" show-icon />
      <p class="ai-agent-error__tip">
        智能体不可用时不影响原有功能，可稍后重试或到「AI 中台 · 智能体管理」检查配置。
      </p>
    </div>

    <template v-else-if="content">
      <div class="ai-agent-meta">
        <el-tag size="small" type="info">{{ agentName || code }}</el-tag>
        <span v-if="model" class="ai-agent-meta__model">{{ model }}</span>
        <el-tag v-if="runNo" size="small" type="warning" effect="plain">工作流 {{ runNo }}</el-tag>
        <span class="ai-agent-meta__cost">{{ costText }}</span>
        <div class="ai-agent-meta__spacer" />
        <el-button link type="primary" size="small" @click="copyResult">复制结果</el-button>
      </div>

      <!-- 契约型输出（键名写死的 JSON）交给公共组件展开成中文字段表；
           普通文本回复仍按原文呈现 -->
      <ContractView v-if="contractRows.length" :text="content" />
      <pre v-else class="ai-agent-pre ai-agent-pre--text">{{ content }}</pre>

      <!-- 工作流模式下回显节点执行轨迹，便于定位是哪一步慢 / 哪一步失败 -->
      <el-collapse v-if="traces.length" class="ai-agent-traces">
        <el-collapse-item name="traces">
          <template #title>
            <span>工作流执行轨迹 · {{ traces.length }} 个节点 · 合计 {{ traceCost }} ms</span>
          </template>
          <div v-for="(item, idx) in traces" :key="idx" class="ai-agent-trace">
            <el-tag size="small" :type="item.status === 'success' ? 'success' : 'danger'" effect="plain">
              {{ item.type }}
            </el-tag>
            <span class="ai-agent-trace__name">{{ item.name }}</span>
            <span class="ai-agent-trace__cost">{{ item.costMs }} ms</span>
          </div>
        </el-collapse-item>
      </el-collapse>
    </template>

    <el-skeleton v-else :rows="6" animated />
  </el-drawer>
</template>

<script setup>
/**
 * AI 智能体操作按钮 + 结果抽屉（管理后台通用）
 *
 * 用法：<AiAgentPanel code="bi_insight" label="AI 解读" :payload="buildPayload" />
 *   - payload 是函数，点击时才取当前页面数据，返回字符串或对象（对象会转成文本喂给模型）
 *   - 智能体未启用 / 调用失败只提示，不抛异常，不影响所在页面原有功能
 */
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ContractView from '@/components/ContractView.vue'
import { buildStructured } from '@/utils/aiContractOutput.js'
import { MagicStick } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { agentApi } from '@/api/ai/agent'

const props = defineProps({
  code: { type: String, required: true },
  label: { type: String, default: 'AI 生成' },
  title: { type: String, default: '' },
  payload: { type: Function, default: () => '' },
  disabled: { type: Boolean, default: false },
  size: { type: String, default: 'default' },
  type: { type: String, default: 'primary' },
  plain: { type: Boolean, default: true },
  /** 表格操作列里用文字按钮，避免撑宽行 */
  link: { type: Boolean, default: false },
  drawerSize: { type: String, default: '520px' }
})

const emit = defineEmits(['done', 'fail'])

const visible = ref(false)
const loading = ref(false)
const content = ref('')
const model = ref('')
const runNo = ref('')
const traces = ref([])

const traceCost = computed(() => traces.value.reduce((sum, t) => sum + (Number(t.costMs) || 0), 0))
const agentName = ref('')
const error = ref('')
const costMs = ref(0)
const size_ = computed(() => props.drawerSize)

const contractRows = computed(() => buildStructured(content.value))

const costText = computed(() => (costMs.value ? `耗时 ${(costMs.value / 1000).toFixed(1)}s` : ''))

const toPromptText = (value) => {
  if (value === null || value === undefined || value === '') return ''
  if (typeof value === 'string') return value
  try {
    return JSON.stringify(value, null, 1)
  } catch {
    return String(value)
  }
}

const copyResult = async () => {
  try {
    await navigator.clipboard.writeText(content.value)
    ElMessage.success('已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选择文本')
  }
}

const run = async () => {
  if (loading.value) return
  const promptText = toPromptText(props.payload())
  if (!String(promptText).trim()) {
    ElMessage.warning('当前页面没有可提交给智能体的数据')
    return
  }

  loading.value = true
  content.value = ''
  error.value = ''
  costMs.value = 0
  visible.value = true
  const started = Date.now()

  try {
    const cfgRes = await agentApi.getConfig(props.code)
    const cfg = cfgRes?.data || {}
    if (!cfg.enabled) {
      error.value = cfg.reason || '该智能体未启用或未绑定模型，请到「AI 中台 · 智能体管理」检查'
      emit('fail', error.value)
      return
    }
    agentName.value = cfg.name || ''

    const res = await request({
      url: '/web/ai/chat',
      method: 'post',
      // 长输出场景后端要 60s 以上，这里给足等待时间，避免前端先断
      timeout: 330000,
      data: {
        content: promptText,
        // 每次独立会话，避免上一次的分析上下文影响本次判断
        sessionNo: `${props.code}-${Date.now()}`,
        scene: props.code,
        systemPrompt: cfg.prompt || ''
      }
    })

    const data = res?.data || {}
    runNo.value = ''
    traces.value = []
    if (data.success) {
      content.value = data.content || ''
      model.value = data.model || cfg.modelCode || ''
      runNo.value = data.runNo || ''
      traces.value = Array.isArray(data.traces) ? data.traces : []
      costMs.value = Date.now() - started
      emit('done', data)
    } else {
      error.value = data.message || res?.message || '智能体调用失败'
      emit('fail', error.value)
    }
  } catch (e) {
    error.value = e?.message || '调用异常，请稍后重试'
    emit('fail', error.value)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-agent-trigger {
  display: inline-flex;
}

.ai-agent-trigger__icon {
  margin-right: 4px;
}

.ai-agent-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.ai-agent-traces {
  margin-top: 12px;
}

.ai-agent-trace {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
}

.ai-agent-trace__name {
  flex: 1;
  color: var(--el-text-color-regular);
}

.ai-agent-trace__cost {
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
}

.ai-agent-meta__model {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.ai-agent-meta__spacer {
  flex: 1;
}

.ai-agent-json__row {
  display: flex;
  gap: 12px;
  padding: 7px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
  font-size: 13px;
  line-height: 1.6;
}

.ai-agent-json__key {
  flex: 0 0 132px;
  color: var(--el-text-color-secondary);
  word-break: break-all;
}

.ai-agent-json__val {
  flex: 1;
  color: var(--el-text-color-primary);
  word-break: break-word;
  white-space: pre-wrap;
}

.ai-agent-json__val.is-warn {
  color: var(--el-color-danger);
}

.ai-agent-raw {
  margin-top: 16px;
}

.ai-agent-pre {
  margin: 0;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 420px;
  overflow: auto;
}

.ai-agent-pre--text {
  font-size: 13px;
  line-height: 1.9;
  color: var(--el-text-color-primary);
}

.ai-agent-error__tip {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}
</style>
