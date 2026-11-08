<template>
  <el-card class="template-card" shadow="never">
    <template #header>
      <div class="card-header">
        <div>
          <span class="title">{{ pageTitle }}</span>
          <p class="subtitle">
            为{{ platformLabel }}页面挑选展示模板。模板只决定版式、配色与栅格列数，不影响页面里已配置的商品；
            选完后{{ renderTarget }}前台会按该模板渲染对应版式。改动对买家侧立即生效。
            <b>未设置模板的页面默认使用「{{ defaultTemplateName }}」。</b>
          </p>
        </div>
      </div>
    </template>

    <div class="step-row">
      <div class="step-label">
        <b>1</b>
        <span>选择页面</span>
      </div>
      <el-select
        v-model="selectedPageId"
        placeholder="请选择要套用模板的页面"
        class="page-select"
        filterable
        :loading="pageLoading"
        @change="handlePageChange"
      >
        <el-option
          v-for="p in pageOptions"
          :key="p.pageId"
          :label="p.pageName"
          :value="p.pageId"
        >
          <span>{{ p.pageName }}</span>
          <span v-if="Number(p.pageIsIndex) === 1" class="opt-tag">前台首页</span>
        </el-option>
      </el-select>
      <el-tag v-if="selectedPageId" :type="currentCode ? 'success' : 'info'" size="small">
        当前：{{ currentCode ? (codeOf(currentCode) || currentCode) : '不套用模板' }}
      </el-tag>
    </div>

    <div class="step-row step-templates">
      <div class="step-label">
        <b>2</b>
        <span>选择模板</span>
      </div>
      <div v-loading="tplLoading" class="tpl-grid">
        <div
          class="tpl-item tpl-item-none"
          :class="{ 'is-active': currentCode === '' }"
          @click="pick('')"
        >
          <div class="tpl-preview tpl-preview-none">
            <span>不套用</span>
          </div>
          <div class="tpl-meta">
            <b>不套用模板</b>
            <p>保持页面自身的原始版式，不走任何模板</p>
          </div>
        </div>

        <div
          v-for="t in templates"
          :key="t.code"
          class="tpl-item"
          :class="{ 'is-active': currentCode === t.code, 'is-disabled': !selectedPageId }"
          @click="selectedPageId && pick(t.code)"
        >
          <div class="tpl-preview" :style="previewStyle(t)">
            <span v-if="t.isDefault" class="tpl-default-tag">默认模板</span>
            <iframe
              v-if="previewUrl(t)"
              class="pv-iframe"
              :src="previewUrl(t)"
              scrolling="no"
              frameborder="0"
              loading="lazy"
              :title="t.name + ' 实时预览'"
            ></iframe>
            <span class="pv-live-tag">实时预览</span>
          </div>
          <div class="tpl-meta">
            <b>{{ t.name }}<em v-if="t.isDefault" class="tpl-default-inline">默认</em></b>
            <p>{{ t.summary }}</p>
            <p class="tpl-storage">code={{ t.code }} ｜ 实现 {{ storage.implement }} ｜ 持久化 {{ storage.persist }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedPageId" class="step-actions">
      <el-button type="primary" :loading="saving" :disabled="!dirty" @click="save">
        {{ dirty ? '保存并生效' : '已保存' }}
      </el-button>
      <el-button :disabled="!dirty" @click="reset">撤销修改</el-button>
      <span v-if="saved" class="saved-hint">已保存，前台即时生效</span>
    </div>
    <el-alert
      v-else
      class="pick-hint"
      type="info"
      :closable="false"
      title="请先在上方选择一个页面，再挑选模板"
    />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getDecoratePageTemplates,
  getDecoratePageTemplate,
  applyDecoratePageTemplate,
  getHomeList
} from '@/api/mall/decoration'

const route = useRoute()

/**
 * 当前装修端：pc | mobile，由路由 meta.platform 决定。
 */
const isPc = computed(() => (route.meta?.platform || route.query?.platform) === 'pc')
const pageTitle = computed(() => (isPc.value ? 'PC 模板选择' : '模板选择'))
const platformLabel = computed(() => (isPc.value ? 'PC 端' : '移动端'))
const renderTarget = computed(() => (isPc.value ? 'PC 商城前台' : 'uniapp / 小程序'))

/**
 * 模板库加载状态
 * @type {import('vue').Ref<boolean>}
 */
const tplLoading = ref(false)

/**
 * 页面列表加载状态
 * @type {import('vue').Ref<boolean>}
 */
const pageLoading = ref(false)

/**
 * 保存中状态
 * @type {import('vue').Ref<boolean>}
 */
const saving = ref(false)

/**
 * 模板列表
 * @type {import('vue').Ref<Array<Object>>}
 */
const templates = ref([])

/**
 * 页面下拉选项
 * @type {import('vue').Ref<Array<Object>>}
 */
const pageOptions = ref([])

/**
 * 当前选中的页面 id
 * @type {import('vue').Ref<number|string>}
 */
const selectedPageId = ref('')

/**
 * 已保存的模板 code（服务端真值）
 * @type {import('vue').Ref<string>}
 */
const savedCode = ref('')

/**
 * 待保存的模板 code
 * @type {import('vue').Ref<string>}
 */
const pendingCode = ref('')

/**
 * 当前生效（待保存）的 code
 * @type {import('vue').ComputedRef<string>}
 */
const currentCode = computed(() => pendingCode.value)

/**
 * 是否有未保存改动
 * @type {import('vue').ComputedRef<boolean>}
 */
const dirty = computed(() => pendingCode.value !== savedCode.value)

/**
 * 保存成功提示
 * @type {import('vue').Ref<boolean>}
 */
const saved = ref(false)

/**
 * 默认模板名（服务端 isDefault 标记，兜底「奢侈品简单风格」）
 * 页面未设置模板（库中 NULL）时前台按它渲染。
 * @type {import('vue').ComputedRef<string>}
 */
const defaultTemplateName = computed(() => templates.value.find((t) => t.isDefault)?.name || '奢侈品简单风格')

/**
 * 按 code 取模板名
 * @param {string} code 模板 code
 * @returns {string} 模板名
 */
const codeOf = (code) => {
  const hit = templates.value.find((t) => t.code === code)
  return hit ? hit.name : ''
}

/**
 * 模板卡预览配色
 * @param {Object} t 模板
 * @returns {Object} 行内样式
 */
const previewStyle = (t) => ({
  borderTopColor: t.themeColor,
  background: t.code === 'tea' ? '#f4f1ea' : t.style === 'banner-first' ? '#1f2733' : '#f7f9fa'
})

/**
 * 模板存储位置说明（与 uniapp/src/components/decoration-rules.mjs 的 templateStorage 对齐）。
 * 实现文件：uniapp 前台把 code 翻译成真实样式的地方；
 * 持久化：apply 接口写入的库表字段，前台 /web/mall/index/data 读回。
 */
const storage = {
  implement: 'uniapp/src/components/decoration-rules.mjs',
  persist: 'decorate_page.page_template_code'
}

/**
 * 模板实时预览地址：同源加载 H5 首页并带 tpl 预览参数，
 * 卡片里渲染的就是买家侧真实页面（uniapp hash 路由，query 放在 # 内）。
 * @param {Object} t 模板
 * @returns {string} iframe src，仅 H5 可达（同源 /h5/）时有效
 */
const previewUrl = (t) => {
  if (typeof window === 'undefined') return ''
  return `${window.location.origin}/h5/#/pages/index/index?tpl=${encodeURIComponent(t.code)}`
}

/**
 * 加载模板库
 */
const loadTemplates = async () => {
  tplLoading.value = true
  try {
    const res = await getDecoratePageTemplates()
    templates.value = res?.data ?? []
  } catch (error) {
    console.error('加载模板库失败', error)
    ElMessage.error('加载模板库失败')
  } finally {
    tplLoading.value = false
  }
}

/**
 * 加载页面下拉列表
 * 读的是 H5 首页同一张表（hou_base_decorate_page 的移动端首页微页面），
 * 不是老的 hou_base_modularizationd
 */
const loadPages = async () => {
  pageLoading.value = true
  try {
    const res = await getHomeList({ page: 1, limit: 200, pagePlatform: isPc.value ? 'pc' : 'mobile' })
    const data = res?.data ?? res
    const lists = data?.lists ?? data?.list ?? (Array.isArray(data) ? data : [])
    pageOptions.value = lists.map((p) => ({
      pageId: p.id,
      pageName: p.name,
      pageIsIndex: p.is_home ?? 0,
      templateCode: p.page_template_code ?? ''
    }))
  } catch (error) {
    console.error('加载页面列表失败', error)
    ElMessage.error('加载页面列表失败')
  } finally {
    pageLoading.value = false
  }
}

/**
 * 切换页面时读回该页面已保存的模板
 * @param {number|string} pageId 页面 id
 */
const handlePageChange = async (pageId) => {
  saved.value = false
  if (!pageId) {
    savedCode.value = ''
    pendingCode.value = ''
    return
  }
  try {
    const res = await getDecoratePageTemplate({ pageId })
    const code = res?.data ?? ''
    savedCode.value = typeof code === 'string' ? code : ''
    pendingCode.value = savedCode.value
  } catch (error) {
    console.error('读取当前模板失败', error)
    ElMessage.error('读取当前模板失败')
  }
}

/**
 * 选中模板（仅改本地待保存值，不立即写库）
 * @param {string} code 模板 code，空串表示不套用
 */
const pick = (code) => {
  if (!selectedPageId.value) return
  pendingCode.value = code
  saved.value = false
}

/**
 * 撤销未保存改动
 */
const reset = () => {
  pendingCode.value = savedCode.value
  saved.value = false
}

/**
 * 保存并生效
 */
const save = async () => {
  if (!dirty.value) return
  saving.value = true
  try {
    const res = await applyDecoratePageTemplate({
      pageId: selectedPageId.value,
      templateCode: pendingCode.value
    })
    const applied = res?.data ?? pendingCode.value
    savedCode.value = typeof applied === 'string' ? applied : ''
    pendingCode.value = savedCode.value
    saved.value = true
    ElMessage.success('已保存，前台即时生效')
  } catch (error) {
    console.error('保存模板失败', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadTemplates()
  loadPages()
})
</script>

<style scoped>
.template-card {
  min-height: calc(100vh - 116px);
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
}

.card-header .subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
  font-weight: 400;
  max-width: 760px;
  line-height: 1.7;
}

.step-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.step-templates {
  align-items: flex-start;
  margin-bottom: 24px;
}

.step-label {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 110px;
  flex-shrink: 0;
  font-size: 14px;
  color: #303133;
}

.step-label b {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #19c7a5;
  color: #07111e;
  font-size: 12px;
}

.page-select {
  width: 320px;
}

.opt-tag {
  float: right;
  color: #19c7a5;
  font-size: 12px;
}

.tpl-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  flex: 1;
  min-height: 180px;
}

.tpl-item {
  width: 200px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  transition: all 0.2s;
}

.tpl-item:hover {
  border-color: #19c7a5;
}

.tpl-item.is-active {
  border-color: #19c7a5;
  box-shadow: 0 0 0 2px rgba(25, 199, 165, 0.18);
}

.tpl-item.is-disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.tpl-preview {
  position: relative;
  height: 110px;
  padding: 0;
  border-top: 3px solid #19c7a5;
  box-sizing: border-box;
  overflow: hidden;
  background: #fff;
}

/* 真实网页预览：按 375px 设计宽缩放到卡片宽（200px），只露首屏 */
.pv-iframe {
  width: 375px;
  height: 800px;
  border: 0;
  transform: scale(0.5333);
  transform-origin: 0 0;
  pointer-events: none;
  background: #fff;
}

.pv-live-tag {
  position: absolute;
  top: 4px;
  right: 4px;
  z-index: 1;
  padding: 1px 6px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  pointer-events: none;
}

.tpl-default-tag {
  position: absolute;
  top: 4px;
  left: 4px;
  z-index: 2;
  padding: 1px 6px;
  border-radius: 8px;
  background: #b0805a;
  color: #fff;
  font-size: 10px;
  line-height: 16px;
  pointer-events: none;
}

.tpl-default-inline {
  display: inline-block;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 4px;
  background: #f4f1ea;
  color: #b0805a;
  font-size: 11px;
  font-style: normal;
  line-height: 16px;
}

.tpl-storage {
  margin: 4px 0 0;
  font-size: 10px;
  color: #b0b6bf;
  word-break: break-all;
}

.tpl-preview-none {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 13px;
  border-top-color: #c0c4cc;
}

.pv-bar {
  height: 10px;
  border-radius: 5px;
  margin-bottom: 8px;
}

.pv-cols {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
}

.pv-cols i {
  flex: 1;
  height: 36px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 4px;
}

.pv-blocks {
  display: flex;
  gap: 6px;
}

.pv-blocks i {
  flex: 1;
  height: 24px;
  background: rgba(255, 255, 255, 0.55);
  border-radius: 4px;
}

.tpl-meta {
  padding: 10px 12px 12px;
}

.tpl-meta b {
  font-size: 14px;
  color: #303133;
}

.tpl-meta p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.step-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.saved-hint {
  font-size: 13px;
  color: #19c7a5;
}

.pick-hint {
  margin-top: 8px;
}

@media (max-width: 991px) {
  .step-row {
    flex-direction: column;
    align-items: stretch;
  }

  .step-label {
    width: auto;
  }

  .page-select {
    width: 100%;
  }

  .tpl-item {
    width: 100%;
  }
}
</style>