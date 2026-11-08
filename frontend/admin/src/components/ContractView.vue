<template>
  <!-- 契约型输出：把 JSON 摊成「中文字段 → 值」，原文可折叠。
       聊天面板（试用对话）和操作抽屉（AI 预审等）共用这一份，避免两处各写一遍
       出现「一处中文、一处英文路径」的不一致。 -->
  <div class="contract-view">
    <div
      v-for="(r, ri) in rows"
      :key="ri"
      class="contract-view__row"
      :class="[`contract-view__row--${r.type}`, `contract-view__row--d${r.depth}`]"
    >
      <span class="contract-view__k">{{ r.label }}</span>
      <div class="contract-view__v">
        <template v-if="r.type === 'pairs'">
          <div v-for="(p, pi) in r.pairs" :key="pi" class="contract-view__pair">
            <span class="contract-view__pk">{{ p.k }}</span>
            <span class="contract-view__pv" :class="{ 'is-warn': isWarn(p.v) }">{{ p.v }}</span>
          </div>
        </template>
        <template v-else-if="r.type === 'group'">
          <span class="contract-view__count">{{ r.count }} 项</span>
        </template>
        <template v-else-if="r.type === 'empty'">
          <span class="contract-view__empty">—</span>
        </template>
        <template v-else>
          <span :class="{ 'is-warn': r.type === 'text' && isWarn(r.value) }">{{ r.value }}</span>
        </template>
      </div>
    </div>

    <div class="contract-view__foot">
      <span class="contract-view__hint">这份输出是给程序解析的 JSON，已按字段展开</span>
      <div class="contract-view__spacer" />
      <el-button link size="small" @click="showRaw = !showRaw">
        {{ showRaw ? '收起原文' : '看原文' }}
      </el-button>
      <el-button link size="small" @click="copy">复制 JSON</el-button>
    </div>
    <pre v-if="showRaw" class="contract-view__raw">{{ pretty }}</pre>
  </div>
</template>

<script setup>
/**
 * 智能体契约输出的展示组件。
 *
 * <p>输入是模型返回的整段文本：能解析成契约 JSON 就渲染字段表，解析不了就
 * 由父级决定继续显示原文（本组件只在能展开时挂载）。
 */
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { buildStructured, prettyJson } from '@/utils/aiContractOutput.js'

const props = defineProps({
  /** 模型返回的原始文本 */
  text: { type: String, default: '' }
})

const showRaw = ref(false)
const rows = computed(() => buildStructured(props.text))
const pretty = computed(() => prettyJson(props.text))

/**
 * 风险类取值标红，让人一眼看到「高危 / 不通过」这类结论。
 *
 * <p>只认枚举短值（high / reject / false / 否…），不做全文关键词匹配 ——
 * 否则「存在 2 处高风险表述」这种总结文案会因为含「风险」二字整段变红，
 * 反而看不出真正的结论在哪。
 */
const WARN_VALUES = /^(high|urgent|blocker|major|fail|reject|false|否|不合格|不通过)$/i
const isWarn = (val) => {
  const s = String(val || '').trim()
  return s.length <= 8 && WARN_VALUES.test(s)
}

const copy = async () => {
  try {
    await navigator.clipboard.writeText(pretty.value)
    ElMessage.success('已复制')
  } catch {
    const ta = document.createElement('textarea')
    ta.value = pretty.value
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    ElMessage.success('已复制')
  }
}
</script>

<style scoped>
.contract-view {
  white-space: normal;
  min-width: 240px;
  background: #fff;
  border: 1px solid #e8ebf0;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #1f2329;
}
.contract-view__row {
  display: flex;
  gap: 10px;
  padding: 3px 0;
  border-bottom: 1px dashed #e8ebf0;
}
.contract-view__row:last-of-type {
  border-bottom: none;
}
.contract-view__row--pairs {
  align-items: flex-start;
}
.contract-view__k {
  flex: 0 0 96px;
  color: #8a9099;
  font-size: 12px;
  line-height: 1.6;
}
.contract-view__v {
  flex: 1;
  min-width: 0;
  word-break: break-word;
}
.contract-view__row--group .contract-view__k {
  color: #185fa5;
  font-weight: 600;
}
.contract-view__row--d1 {
  padding-left: 14px;
}
.contract-view__row--d2 {
  padding-left: 28px;
}
.contract-view__count {
  color: #8a9099;
  font-size: 12px;
}
.contract-view__empty {
  color: #c0c4cc;
}
.contract-view__pair {
  display: flex;
  gap: 8px;
  padding: 1px 0;
}
.contract-view__pk {
  flex: 0 0 72px;
  color: #a8aeb8;
  font-size: 12px;
}
.contract-view__pv {
  flex: 1;
  min-width: 0;
}
.is-warn {
  color: #c0392b;
  font-weight: 600;
}
.contract-view__foot {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px -10px -8px;
  padding: 6px 10px 0;
  border-top: 1px solid #e8ebf0;
}
.contract-view__hint {
  font-size: 12px;
  color: #a8aeb8;
}
.contract-view__spacer {
  flex: 1;
}
.contract-view__raw {
  margin: 8px 0 0;
  padding: 8px 10px;
  background: #fafbfc;
  border: 1px solid #e8ebf0;
  border-radius: 6px;
  font-family: Menlo, Consolas, monospace;
  font-size: 12px;
  line-height: 1.6;
  color: #4b5563;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 220px;
  overflow: auto;
}
</style>
