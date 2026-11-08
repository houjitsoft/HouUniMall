<template>
  <el-drawer
    :model-value="modelValue"
    size="600px"
    destroy-on-close
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <template #header="{ close, titleId, titleClass }">
      <div class="chat-drawer__hd">
        <span :id="titleId" :class="titleClass">试用对话 · {{ agent?.name || '' }}</span>
        <el-popover
          placement="bottom-end"
          :width="380"
          trigger="hover"
          :show-after="120"
          popper-class="chat-hd-pop"
        >
          <template #reference>
            <el-icon class="chat-drawer__hd-info"><InfoFilled /></el-icon>
          </template>
          <div class="chat-hd-pop__body">
            <p class="chat-hd-pop__lead">
              按这个智能体「配置」里保存的模型和提示词当场跑一次，返回的就是前台同一个功能拿到的回复。
            </p>
            <ul class="chat-hd-pop__list">
              <li><b>验证提示词：</b>改完提示词点「新会话」再问一遍，两次回复一对比就知道改对没有。</li>
              <li><b>要等多久：</b>提示词模式通常 1～2 分钟；工作流模式要串行跑多个节点，通常 2～4 分钟。
                第一句话几秒内就会出来，下面出现「模型正在想…」就是在跑，不是卡住了。</li>
              <li><b>验证工作流：</b>工作流模式的智能体，回复下方会带「工作流 + 执行痕迹」，能看到每个节点的耗时与成败。</li>
              <li><b>万一超时：</b>单个模型节点超过 3 分钟没返回会判失败并写明「模型调用超时」，
                这时多半是模型网关不通，不是配置错了。</li>
            </ul>
          </div>
        </el-popover>
        <div class="chat-drawer__hd-spacer" />
        <el-button link class="chat-drawer__hd-close" @click="close">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </template>
    <div class="chat-drawer">
      <div ref="scrollBox" class="chat-drawer__body">
        <el-empty v-if="!messages.length" description="发一句话试试，比如「帮我写一条夏季连衣裙的标题」" :image-size="70" />
        <div
          v-for="(m, i) in messages"
          :key="i"
          class="chat-msg"
          :class="m.role === 'user' ? 'chat-msg--user' : 'chat-msg--ai'"
        >
          <div class="chat-msg__bubble">
            <template v-if="m.role === 'ai' && !m.content">
              <span class="chat-msg__waiting">
                <el-icon class="is-loading"><Loading /></el-icon>
                模型正在想… 已等 {{ elapsed }} 秒
              </span>
            </template>
            <template v-else>
              <!-- 契约型输出（提示词里写死了 JSON 键名，前台按字段解析）：交给公共组件展开，
                   这里只负责「什么时候用结构化视图」——流式过程中仍按原文上屏 -->
              <ContractView v-if="m.structured?.length && !m.streaming" :text="m.content" />
              <div v-else class="chat-msg__text">{{ m.content }}<span v-if="m.streaming" class="chat-msg__caret" /></div>
              <div v-if="m.role === 'ai' && (m.runNo || m.costText)" class="chat-msg__meta">
                <el-tag v-if="m.runNo" size="small" type="info" effect="plain">工作流 {{ m.runNo }}</el-tag>
                <span class="chat-msg__cost">{{ m.costText }}</span>
              </div>
              <el-collapse v-if="m.role === 'ai' && m.traces?.length" class="chat-msg__traces">
                <el-collapse-item :title="`执行痕迹（${m.traces.length} 个节点）`">
                  <div v-for="(t, ti) in m.traces" :key="ti" class="trace-row">
                    <span class="trace-row__name">{{ t.name || t.nodeId }}</span>
                    <el-tag size="small" :type="t.status === 'success' ? 'success' : 'danger'" effect="plain">
                      {{ t.status === 'success' ? '成功' : '失败' }}
                    </el-tag>
                    <span class="trace-row__cost">{{ ((Number(t.costMs) || 0) / 1000).toFixed(1) }}s</span>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </template>
          </div>
        </div>
      </div>

      <div class="chat-drawer__footer">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          resize="none"
          placeholder="输入一句话，回车发送（Ctrl+Enter 换行）"
          @keydown.enter.exact.prevent="send"
        />
        <div class="chat-drawer__actions">
          <el-button size="small" :disabled="sending" @click="newSession">新会话</el-button>
          <span class="chat-drawer__hint">{{ sessionHint }}</span>
          <div class="chat-drawer__spacer" />
          <el-button size="small" type="primary" :loading="sending" :disabled="!input.trim()" @click="send">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
/**
 * 智能体「试用对话」抽屉。
 *
 * <p>走流式（SSE）：模型实测整段 60～70 秒、首字 2～3 秒，同步接口的表现就是
 * 「点了没反应 → 45 秒超时」。这里按 event 增量上屏，同时把已等秒数显示出来，
 * 让人知道后台真的在跑，而不是把请求丢进黑洞。
 *
 * <p>只传 scene，不传提示词与模型：后端会自动回退到智能体自身配置，
 * 这样这里看到的效果和前台调用完全一致（改配置 → 开新会话 → 重问即可对比）。
 */
import { ref, computed, nextTick, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, InfoFilled, Close } from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth.js'
import { buildStructured } from '@/utils/aiContractOutput.js'
import ContractView from '@/components/ContractView.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  agent: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['update:modelValue'])

const messages = ref([])
const input = ref('')
const sending = ref(false)
const elapsed = ref(0)
const scrollBox = ref(null)
let sessionNo = ''
let ticker = null
let abortController = null

const sessionHint = computed(() => (sessionNo ? '同一会话内会带上文，换话题请点「新会话」' : ''))

const stopTicker = () => {
  if (ticker) {
    clearInterval(ticker)
    ticker = null
  }
}
const startTicker = () => {
  stopTicker()
  elapsed.value = 0
  const startedAt = Date.now()
  ticker = setInterval(() => {
    elapsed.value = Math.floor((Date.now() - startedAt) / 1000)
  }, 500)
}

const newSession = () => {
  if (sending.value) return
  sessionNo = ''
  messages.value = []
  input.value = ''
}

const scrollToBottom = () => {
  nextTick(() => {
    const el = scrollBox.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

/** 解析一个 SSE 事件块（event: 事件名 / data: 多行数据） */
const parseEventBlock = (block) => {
  let event = 'message'
  const dataLines = []
  for (const rawLine of block.split('\n')) {
    // 有的代理会把 \n 改写成 \r\n，行尾的 \r 不能带进业务文本
    const line = rawLine.replace(/\r$/, '')
    if (line.startsWith('event:')) event = line.slice(6).trim()
    else if (line.startsWith('data:')) dataLines.push(line.slice(5).replace(/^ /, ''))
    else if (line === 'data') dataLines.push('')
  }
  return { event, data: dataLines.join('\n') }
}

const safeJson = (text) => {
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

const copyText = async (text) => {
  const value = String(text || '')
  try {
    await navigator.clipboard.writeText(value)
    ElMessage.success('已复制')
  } catch {
    const ta = document.createElement('textarea')
    ta.value = value
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    ElMessage.success('已复制')
  }
}

const send = async () => {
  const text = input.value.trim()
  if (!text || sending.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  const answer = { role: 'ai', content: '', streaming: true, runNo: '', traces: [], costText: '', structured: [], showRaw: false }
  messages.value.push(answer)
  sending.value = true
  startTicker()
  scrollToBottom()

  const startedAt = Date.now()
  abortController = new AbortController()
  // 断网/服务端卡死时别无限挂着：10 分钟兜底（正常长文案 60～90 秒就结束）
  const hardTimeout = setTimeout(() => abortController?.abort(), 600000)

  try {
    const body = { scene: props.agent?.code, content: text }
    if (sessionNo) body.sessionNo = sessionNo
    const token = getToken()
    const res = await fetch('/web/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token.replace(/^Bearer\s+/i, '')}` } : {})
      },
      body: JSON.stringify(body),
      signal: abortController.signal
    })
    if (!res.ok) {
      throw new Error(`服务返回 ${res.status}${res.status === 401 || res.status === 403 ? '（登录状态可能已过期，请刷新页面重登）' : ''}`)
    }

    const contentType = res.headers.get('content-type') || ''
    if (!contentType.includes('text/event-stream') || !res.body) {
      // 后端还是老版本的同步接口：退回一次性读取，至少不白屏
      const payload = safeJson(await res.text()) || {}
      const data = payload.data || {}
      answer.content = data.content || payload.message || '（没有返回内容）'
      sessionNo = data.sessionNo || sessionNo
      answer.streaming = false
      return
    }

    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let finished = false

    while (!finished) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      let idx
      while ((idx = buffer.indexOf('\n\n')) >= 0) {
        const block = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 2)
        if (!block.trim()) continue
        const { event, data } = parseEventBlock(block)
        if (event === 'delta') {
          answer.content += data
          scrollToBottom()
        } else if (event === 'done') {
          const payload = safeJson(data) || {}
          sessionNo = payload.sessionNo || sessionNo
          answer.runNo = payload.runNo || ''
          answer.traces = Array.isArray(payload.traces) ? payload.traces : []
          if (!answer.content && payload.content) answer.content = payload.content
          const cost = answer.traces.reduce((sum, t) => sum + (Number(t.costMs) || 0), 0)
          answer.costText = cost ? `共 ${(cost / 1000).toFixed(1)}s` : ''
          finished = true
        } else if (event === 'error') {
          const payload = safeJson(data) || {}
          answer.content = answer.content || `调用失败：${payload.message || '生成失败'}`
          finished = true
        }
      }
    }
    if (!answer.content) {
      answer.content = '（模型没有返回内容，可能是模型未绑定或网关不可用，可到「AI 设置」检查）'
    }
  } catch (e) {
    const aborted = e?.name === 'AbortError'
    answer.content = answer.content
      || (aborted ? '已超过 10 分钟仍未返回，已中断本次请求' : `调用失败：${e.message || '请求异常'}`)
    if (!aborted) ElMessage.error(e.message || '调用失败')
  } finally {
    clearTimeout(hardTimeout)
    answer.streaming = false
    sending.value = false
    stopTicker()
    answer.costText = answer.costText || `用时 ${((Date.now() - startedAt) / 1000).toFixed(1)}s`
    // 跑完再判：契约型输出摊成字段表，自然语言回复保持原文
    answer.structured = buildStructured(answer.content)
    scrollToBottom()
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) newSession()
    else {
      stopTicker()
      abortController?.abort()
    }
  }
)

onUnmounted(() => {
  stopTicker()
  abortController?.abort()
})
</script>

<style scoped>
.chat-drawer__hd {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  color: var(--el-text-color-primary);
}
.chat-drawer__hd-info {
  font-size: 15px;
  color: var(--el-text-color-secondary);
  cursor: help;
  outline: none;
}
.chat-drawer__hd-info:hover {
  color: var(--el-color-primary);
}
.chat-drawer__hd-spacer {
  flex: 1;
}
.chat-drawer__hd-close {
  font-size: 16px;
  color: var(--el-text-color-regular);
}
.chat-drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.chat-drawer__body {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}
.chat-msg {
  display: flex;
  margin-bottom: 12px;
}
.chat-msg--user {
  justify-content: flex-end;
}
.chat-msg__bubble {
  max-width: 86%;
  background: #f4f5f7;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
.chat-msg--user .chat-msg__bubble {
  background: #e6f1fb;
  color: #0c447c;
}
.chat-msg__text {
  white-space: pre-wrap;
}
.chat-msg__waiting {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #8a9099;
}
.chat-msg__caret {
  display: inline-block;
  width: 2px;
  height: 13px;
  margin-left: 1px;
  vertical-align: -2px;
  background: #185fa5;
  animation: caret-blink 1s steps(2, start) infinite;
}
@keyframes caret-blink {
  to {
    visibility: hidden;
  }
}
.chat-msg__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 12px;
  color: #8a9099;
}
.chat-msg__traces {
  margin-top: 6px;
}
.trace-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
  padding: 2px 0;
}
.trace-row__name {
  min-width: 120px;
}
.chat-drawer__footer {
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}
.chat-drawer__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.chat-drawer__hint {
  font-size: 12px;
  color: #a8aeb8;
}
.chat-drawer__spacer {
  flex: 1;
}
</style>

<!-- popover 是 teleport 到 body 的，scoped 样式作用不到，这里单独写一份 -->
<style>
.chat-hd-pop.el-popover.el-popper {
  padding: 12px 14px;
}
.chat-hd-pop__lead {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}
.chat-hd-pop__list {
  margin: 0;
  padding-left: 16px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}
.chat-hd-pop__list li {
  margin-bottom: 4px;
}
.chat-hd-pop__list li:last-child {
  margin-bottom: 0;
}
.chat-hd-pop__list b {
  color: var(--el-text-color-primary);
}
</style>
