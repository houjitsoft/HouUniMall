<template>
  <div
    ref="entryRef"
    class="operation-guide-entry"
    :class="{ 'is-open': visible }"
    role="region"
    :aria-label="t('operationGuide.aria', { page: pageTitle })"
    :style="{ top: `${offsetTop}px` }"
  >
    <el-button
      class="guide-trigger"
      type="primary"
      plain
      circle
      :aria-label="t('operationGuide.action')"
      :title="t('operationGuide.action')"
      @click="visible = true"
    >
      <el-icon class="guide-trigger-icon"><QuestionFilled /></el-icon>
    </el-button>
  </div>

  <el-drawer
    v-model="visible"
    :title="t('operationGuide.title', { page: pageTitle })"
    direction="rtl"
    :size="drawerSize"
    append-to-body
  >
    <div class="guide-content">
      <section class="guide-goal">
        <span class="section-label">{{ t('operationGuide.featureLabel') }}</span>
        <p>{{ localizedGuide.feature || localizedGuide.goal }}</p>
      </section>

      <section v-if="localizedGuide.referenceTable?.rows?.length" class="guide-reference">
        <h3>{{ localizedGuide.referenceTable.title || t('operationGuide.reference') }}</h3>
        <div class="guide-table-wrap">
          <table class="guide-table">
            <thead>
              <tr>
                <th v-for="column in localizedGuide.referenceTable.columns" :key="column">{{ column }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in localizedGuide.referenceTable.rows" :key="`${row.priority}-${index}`">
                <td>
                  <el-tag
                    v-if="row.priorityLevel"
                    :type="priorityTagType(row.priorityLevel)"
                    size="small"
                    effect="plain"
                  >{{ row.priority }}</el-tag>
                  <span v-else>{{ row.priority }}</span>
                </td>
                <td>{{ row.platforms }}</td>
                <td>{{ row.reason }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <h3>{{ t('operationGuide.steps') }}</h3>
        <ol class="guide-steps">
          <li v-for="(step, index) in localizedGuide.steps" :key="step">
            <span class="step-number">{{ index + 1 }}</span>
            <span>{{ step }}</span>
          </li>
        </ol>
      </section>

      <el-alert :title="t('operationGuide.notice')" :description="localizedGuide.tip" type="warning" :closable="false" show-icon />

      <el-button class="manual-button" :icon="Reading" @click="openManual">{{ t('operationGuide.manual') }}</el-button>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { QuestionFilled, Reading } from '@element-plus/icons-vue'
// GEO 模块（views/GEO）为商业域，不随开源包分发；缺失时不渲染操作指引。
const getPageOperationGuide = () => null

const props = defineProps({
  pageTitle: { type: String, default: '当前页面' },
  routePath: { type: String, required: true },
  mobile: { type: Boolean, default: false }
})

const EDGE_SAFE = 80
const STRIP_WIDTH = 64
const GAP = 10
/** 相对 main 根查询；只取块级容器，避免卡片内 p/h 把整条右侧占满 */
const CANDIDATE_SELECTOR = [
  ':scope > .geo-page .el-card',
  ':scope > .geo-page .el-alert',
  ':scope > .geo-page .el-table',
  ':scope > .geo-page .el-empty',
  ':scope .geo-kpis .el-card',
  ':scope .geo-main > .el-card',
  ':scope .geo-main > .el-row > .el-col > .el-card',
  ':scope .collision-probe'
].join(',')

const router = useRouter()
const { t, te } = useI18n()
const visible = ref(false)
const entryRef = ref(null)
const offsetTop = ref(Math.round(window.innerHeight * 0.12))
const GUIDE_I18N_KEYS = {
  '/geo/goals/conversion': 'conversionGoals'
}
const baseGuide = computed(() => getPageOperationGuide(props.routePath))
const localizedGuide = computed(() => {
  const key = GUIDE_I18N_KEYS[props.routePath]
  const prefix = `operationGuide.pages.${key}`
  if (!key || !te(`${prefix}.feature`)) return baseGuide.value
  return {
    ...baseGuide.value,
    feature: t(`${prefix}.feature`),
    goal: t(`${prefix}.feature`),
    steps: [1, 2, 3, 4].map((index) => t(`${prefix}.step${index}`)),
    tip: t(`${prefix}.tip`)
  }
})
const drawerSize = computed(() => (props.mobile ? '100%' : '460px'))

const PRIORITY_TAG_TYPES = {
  high: 'danger',
  medium: 'warning',
  low: 'info'
}

function priorityTagType(level) {
  return PRIORITY_TAG_TYPES[level] || 'info'
}

let rafId = 0
let mainEl = null
let resizeObserver = null
let mutationObserver = null

function defaultTop() {
  return Math.round(window.innerHeight * 0.12)
}

function buttonSize() {
  const el = entryRef.value
  if (!el) return { width: 44, height: 44 }
  const rect = el.getBoundingClientRect()
  return {
    width: Math.max(36, rect.width || 44),
    height: Math.max(36, rect.height || 44)
  }
}

function clampTop(top, height) {
  const min = EDGE_SAFE
  const max = Math.max(min, window.innerHeight - EDGE_SAFE - height)
  return Math.min(max, Math.max(min, top))
}

function mergeIntervals(intervals) {
  if (!intervals.length) return []
  const sorted = [...intervals].sort((a, b) => a[0] - b[0])
  const merged = [[sorted[0][0], sorted[0][1]]]
  for (let i = 1; i < sorted.length; i++) {
    const [start, end] = sorted[i]
    const last = merged[merged.length - 1]
    if (start <= last[1] + GAP) last[1] = Math.max(last[1], end)
    else merged.push([start, end])
  }
  return merged
}

function collectOccupiedIntervals() {
  const root = document.querySelector('.app-main') || document.querySelector('main')
  if (!root) return []
  const stripLeft = window.innerWidth - STRIP_WIDTH
  const nodes = root.querySelectorAll(CANDIDATE_SELECTOR)
  const intervals = []
  for (const el of nodes) {
    if (el.classList?.contains('operation-guide-entry') || entryRef.value?.contains(el)) continue
    const style = window.getComputedStyle(el)
    if (style.display === 'none' || style.visibility === 'hidden' || Number(style.opacity) === 0) continue
    const rect = el.getBoundingClientRect()
    if (rect.width < 8 || rect.height < 8) continue
    if (rect.bottom <= EDGE_SAFE || rect.top >= window.innerHeight - EDGE_SAFE) continue
    // 只关心真正侵入右侧贴边条带的块
    if (rect.right < stripLeft) continue
    intervals.push([
      Math.max(0, rect.top - GAP),
      Math.min(window.innerHeight, rect.bottom + GAP)
    ])
  }
  return mergeIntervals(intervals)
}

function findGaps(occupied, minY, maxY, need) {
  const gaps = []
  let cursor = minY
  for (const [start, end] of occupied) {
    if (start > cursor) {
      const gapStart = cursor
      const gapEnd = Math.min(start, maxY + need)
      if (gapEnd - gapStart >= need) gaps.push([gapStart, gapEnd])
    }
    cursor = Math.max(cursor, end)
    if (cursor >= maxY + need) break
  }
  if (cursor < maxY + need) {
    const gapEnd = maxY + need
    if (gapEnd - cursor >= need) gaps.push([cursor, gapEnd])
  }
  return gaps
}

function placeInGap(gap, preferred, height) {
  const [gapStart, gapEnd] = gap
  const maxTop = gapEnd - height
  return clampTop(Math.min(maxTop, Math.max(gapStart, preferred)), height)
}

function overlapAmount(top, height, occupied) {
  let total = 0
  const bottom = top + height
  for (const [start, end] of occupied) {
    const a = Math.max(top, start)
    const b = Math.min(bottom, end)
    if (b > a) total += b - a
  }
  return total
}

function resolveClearTop() {
  const preferred = defaultTop()
  const { height } = buttonSize()
  const minY = EDGE_SAFE
  const maxTop = Math.max(minY, window.innerHeight - EDGE_SAFE - height)
  if (props.mobile) return maxTop
  const occupied = collectOccupiedIntervals()

  if (overlapAmount(preferred, height, occupied) === 0) return preferred

  const gaps = findGaps(occupied, minY, maxTop, height)
  if (gaps.length) {
    const below = gaps.filter(([, end]) => end > preferred)
    const pool = below.length ? below : gaps
    let best = pool[0]
    let bestDist = Infinity
    for (const gap of pool) {
      const placed = placeInGap(gap, preferred, height)
      const dist = Math.abs(placed - preferred)
      const bias = placed >= preferred ? 0 : 8
      if (dist + bias < bestDist) {
        bestDist = dist + bias
        best = gap
      }
    }
    return placeInGap(best, preferred, height)
  }

  // 无完整空隙：扫描最小重叠位置（向下优先）
  let bestTop = preferred
  let bestScore = Infinity
  for (let top = minY; top <= maxTop; top += 2) {
    const overlap = overlapAmount(top, height, occupied)
    const dist = Math.abs(top - preferred)
    const downBias = top >= preferred ? 0 : 12
    const score = overlap * 1000 + dist + downBias
    if (score < bestScore) {
      bestScore = score
      bestTop = top
    }
  }
  return bestTop
}

function adjustPosition() {
  if (!entryRef.value) return
  offsetTop.value = resolveClearTop()
}

function scheduleAdjust() {
  if (rafId) cancelAnimationFrame(rafId)
  rafId = requestAnimationFrame(() => {
    rafId = 0
    adjustPosition()
  })
}

function bindScrollTarget() {
  const next = document.querySelector('.app-main') || document.querySelector('main')
  if (mainEl === next) return
  if (mainEl) mainEl.removeEventListener('scroll', scheduleAdjust)
  mainEl = next
  if (mainEl) mainEl.addEventListener('scroll', scheduleAdjust, { passive: true })
}

function bindObservers() {
  resizeObserver?.disconnect()
  mutationObserver?.disconnect()
  const root = document.querySelector('.app-main')
  if (!root) return
  if (typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(scheduleAdjust)
    resizeObserver.observe(root)
  }
  if (typeof MutationObserver !== 'undefined') {
    mutationObserver = new MutationObserver(scheduleAdjust)
    mutationObserver.observe(root, { childList: true, subtree: true, attributes: true, attributeFilter: ['style', 'class'] })
  }
}

function openManual() {
  visible.value = false
  router.push('/geo/help/manual').catch(() => {})
}

watch(
  () => props.routePath,
  async () => {
    offsetTop.value = defaultTop()
    await nextTick()
    bindScrollTarget()
    bindObservers()
    scheduleAdjust()
    setTimeout(scheduleAdjust, 120)
    setTimeout(scheduleAdjust, 400)
  }
)

onMounted(async () => {
  offsetTop.value = defaultTop()
  await nextTick()
  bindScrollTarget()
  bindObservers()
  scheduleAdjust()
  window.addEventListener('resize', scheduleAdjust, { passive: true })
  window.addEventListener('scroll', scheduleAdjust, { passive: true })
  setTimeout(scheduleAdjust, 120)
  setTimeout(scheduleAdjust, 400)
})

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId)
  window.removeEventListener('resize', scheduleAdjust)
  window.removeEventListener('scroll', scheduleAdjust)
  if (mainEl) mainEl.removeEventListener('scroll', scheduleAdjust)
  resizeObserver?.disconnect()
  mutationObserver?.disconnect()
})
</script>

<style scoped>
.operation-guide-entry {
  position: fixed;
  right: 14px;
  z-index: 900;
  transition: top 0.18s ease;
}

.guide-trigger {
  flex-shrink: 0;
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
  width: 44px !important;
  height: 44px !important;
  min-width: 44px !important;
  padding: 0 !important;
  border-radius: 50% !important;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 4px 16px rgba(31, 45, 61, 0.16);
}
.guide-trigger-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.guide-content { display: flex; flex-direction: column; gap: 24px; }
.guide-goal { padding: 16px; border-radius: 8px; background: #ecf5ff; color: #303133; }
.section-label { color: #409eff; font-size: 13px; font-weight: 600; }
.guide-goal p { margin: 8px 0 0; line-height: 1.7; }
.guide-content h3 { margin: 0 0 14px; font-size: 16px; color: #303133; }
.guide-steps { margin: 0; padding: 0; list-style: none; display: grid; gap: 16px; }
.guide-steps li { display: flex; align-items: flex-start; gap: 12px; color: #303133; line-height: 1.65; }
.step-number { width: 26px; height: 26px; flex: 0 0 26px; display: inline-flex; align-items: center; justify-content: center; border-radius: 50%; background: #409eff; color: #fff; font-size: 13px; font-weight: 700; }
.manual-button { width: 100%; }
.guide-reference { padding: 14px; border-radius: 8px; background: #f8fafc; border: 1px solid #ebeef5; }
.guide-table-wrap { overflow-x: auto; }
.guide-table { width: 100%; border-collapse: collapse; font-size: 13px; line-height: 1.55; }
.guide-table th,
.guide-table td { padding: 10px 12px; border: 1px solid #ebeef5; text-align: left; vertical-align: top; }
.guide-table th { background: #f5f7fa; color: #606266; font-weight: 600; white-space: nowrap; }
.guide-table td:first-child { white-space: nowrap; }

@media (max-width: 768px) {
  .operation-guide-entry { right: 10px; }
  .guide-trigger {
    width: 40px !important;
    height: 40px !important;
    min-width: 40px !important;
  }
}
</style>
