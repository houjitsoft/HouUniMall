<template>
  <div class="page-editor-container">
    <div class="editor-header">
      <el-page-header @back="handleBack" title=" ">
        <template #content>
          <span class="header-title">{{ title }}</span>
          <el-tag size="small" class="ml-2">{{ getPageData.length }}个组件</el-tag>
        </template>
      </el-page-header>
    </div>

    <div class="editor-body">
      <el-card
        shadow="never"
        class="editor-left"
        :body-style="{ 'padding-right': 0 }"
      >
        <component-select
          @addwidgetName="addwidgetName"
          @update:title="updatetitle"
          @resetdragIndex="resetdragIndex"
          :title="title"
        />
      </el-card>

      <index-preview
        class="editor-center"
        ref="previewRef"
        v-model="selectWidgetIndex"
        @delwidget="delwidget"
        @copywidget="copywidget"
        @addwidget="addwidget"
        @updatePageData="updatePageData"
        :pageData="getPageData"
        :pageMeta="getPageMeta"
        :websiteStyle="Number(menus[pagesTypeEnum.HOME].type) === 1 ? websiteStyle : {}"
        :sharedRendering="Number(menus[pagesTypeEnum.HOME].type) === 1"
      />

      <attr-setting
        class="editor-right"
        :widget="getSelectWidget"
        @update:content="updateContent"
      />
    </div>

    <div class="editor-footer">
      <el-button type="primary" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script setup>
// ⚠️ 所有 import 必须集中在 <script setup> 顶部、任何可执行语句之前。
// ESM 规定 import 只能出现在模块顶层；一旦被 const 等语句打断，构建期
// 会被当作未使用而摇掉，运行时报 ref/computed is not defined（页面空白）。
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ComponentSelect from '../components/editor/component-select.vue'
import IndexPreview from '../components/editor/index-preview.vue'
import AttrSetting from '../components/editor/attr-setting.vue'
import widgets from '../components/widgets'
import { getPageDetail, savePage, addPage, getMallStyle } from '@/api/mall/decoration'
import { getNonDuplicateID } from '@/utils/decoration'
import { unwrapDecorate, splitDecorateWidgets, stringifyDecorateSave } from '@/utils/decoratePayload'

const route = useRoute()
const router = useRouter()

/**
 * 组件props定义
 */
const props = defineProps({
  pageId: {
    type: [Number, String],
    default: ''
  }
})

/**
 * 定义组件事件
 */
const emit = defineEmits(['back'])

/**
 * 预览组件引用
 * @type {import('vue').Ref<Object>}
 */
const previewRef = ref(null)
const websiteStyle = ref({})

/**
 * 页面标题
 * @type {import('vue').Ref<string>}
 */
const title = ref('默认页面')

/**
 * 当前拖拽的组件名称
 * @type {import('vue').Ref<string>}
 */
const widgetName = ref('')

/**
 * 当前选中的组件索引
 * @type {import('vue').Ref<number>}
 */
const selectWidgetIndex = ref(-1)

/**
 * 页面类型枚举
 * @type {Object<string, string>}
 */
const pagesTypeEnum = {
  HOME: '1'
}

/**
 * 根据组件名称列表生成页面组件数据
 * @param {Array<string>} widgetNames - 组件名称数组
 * @returns {Array<Object>} 组件数据数组
 */
const generatePageData = (widgetNames) => {
  return widgetNames.map((widgetName) => {
    return {
      id: getNonDuplicateID(),
      ...(widgets[widgetName]?.options() || {})
    }
  })
}

/**
 * 页面菜单数据
 * @type {Object}
 */
const menus = reactive({
  [pagesTypeEnum.HOME]: {
    id: '',
    type: '',
    name: '',
    pageMeta: generatePageData(['page-meta']),
    pageData: generatePageData([])
  }
})

/**
 * 当前激活的菜单
 * @type {import('vue').Ref<string>}
 */
const activeMenu = ref('1')

/**
 * 计算当前页面的组件数据
 * @type {import('vue').ComputedRef<Array<Object>>}
 */
const getPageData = computed(() => {
  return menus[activeMenu.value]?.pageData ?? []
})

/**
 * 计算当前选中的组件
 * @type {import('vue').ComputedRef<Object>}
 */
const getSelectWidget = computed(() => {
  if (selectWidgetIndex.value === -1) {
    return menus[activeMenu.value]?.pageMeta?.[0] ?? ''
  }
  return menus[activeMenu.value]?.pageData[selectWidgetIndex.value] ?? ''
})

/**
 * 计算当前页面的元数据
 * @type {import('vue').ComputedRef<Object>}
 */
const getPageMeta = computed(() => {
  return menus[activeMenu.value]?.pageMeta ?? null
})

/**
 * 处理添加组件名称
 * @param {string} name - 组件名称
 */
const addwidgetName = (name) => {
  widgetName.value = name
}

/**
 * 更新页面标题
 * @param {string} value - 新的标题
 */
const updatetitle = (value) => {
  title.value = value
}

/**
 * 重置拖拽索引
 */
const resetdragIndex = () => {
  previewRef.value?.resetdragIndex()
}

/**
 * 添加组件到页面
 * @param {number} index - 插入位置的索引
 * @param {string} position - 插入位置（top/bottom）
 */
const addwidget = (index, position) => {
  let insertIndex = index
  if (index >= 0) {
    if (position === 'bottom') {
      insertIndex++
    }
    menus[activeMenu.value].pageData.splice(insertIndex, 0, ...generatePageData([widgetName.value]))
  }
  if (index === -1) {
    menus[activeMenu.value].pageData.push(...generatePageData([widgetName.value]))
    insertIndex = menus[activeMenu.value].pageData.length - 1
  }
}

/**
 * 删除组件
 * @param {number} index - 要删除的组件索引
 */
const delwidget = (index) => {
  menus[activeMenu.value].pageData.splice(index, 1)
}

/**
 * 复制组件
 * @param {number} index - 组件索引
 * @param {string} componentName - 组件名称
 */
const copywidget = (index, componentName) => {
  menus[activeMenu.value].pageData.splice(index, 0, ...generatePageData([componentName]))
}

/**
 * 更新页面数据（拖拽排序等）
 * @param {Array<Object>} value - 新的页面数据
 */
const updatePageData = (value) => {
  menus[activeMenu.value].pageData = [...value]
}

/**
 * 更新组件内容
 * @param {Object} content - 更新的内容
 */
const updateContent = (content) => {
  const menu = menus[activeMenu.value]
  if (!menu) return
  if (selectWidgetIndex.value === -1) {
    if (!menu.pageMeta?.length) {
      menu.pageMeta = generatePageData(['page-meta'])
    }
    menu.pageMeta[0].content = content
    return
  }
  if (menu.pageData?.[selectWidgetIndex.value]) {
    menu.pageData[selectWidgetIndex.value].content = content
  }
}

/**
 * 获取页面详情数据
 */
const resolvePageId = () => props.pageId || route.query.id

const getData = async () => {
  const pageId = resolvePageId()
  if (!pageId) return
  try {
    const payload = unwrapDecorate(await getPageDetail({ id: pageId }))
    if (!payload) return
    const { pageMeta, pageData } = splitDecorateWidgets(payload.data, ['page-meta'])
    menus[pagesTypeEnum.HOME].pageMeta = pageMeta.length ? pageMeta : generatePageData(['page-meta'])
    menus[pagesTypeEnum.HOME].pageData = pageData
    menus[pagesTypeEnum.HOME].id = payload.id || pageId
    menus[pagesTypeEnum.HOME].type = payload.type || 1
    title.value = payload.name || '默认页面'
    if (Number(menus[pagesTypeEnum.HOME].type) === 1) {
      try {
        const style = await getMallStyle()
        websiteStyle.value = style?.data || {}
      } catch {
        websiteStyle.value = {}
      }
    }
  } catch (error) {
    console.error('获取页面详情失败', error)
    ElMessage.error('获取页面详情失败')
  }
}

/**
 * 保存页面数据
 */
const handleSave = async () => {
  try {
    const page = menus[pagesTypeEnum.HOME]
    const data = {
      id: page.id || resolvePageId(),
      type: page.type || 1,
      name: title.value,
      data: stringifyDecorateSave(page.pageMeta, page.pageData)
    }
    if (data.id) {
      await savePage(data)
    } else {
      const created = unwrapDecorate(await addPage(data))
      const newId = created?.id ?? created
      if (newId) {
        page.id = newId
        router.replace({ path: route.path, query: { ...route.query, id: newId } })
      }
    }
    ElMessage.success('保存成功')
    getData()
  } catch (error) {
    console.error('保存页面失败', error)
    ElMessage.error('保存失败')
  }
}

/**
 * 返回上一页
 */
const handleBack = () => {
  router.push('/mall/decoration/pages/pages')
  emit('back')
}

onMounted(() => {
  if (resolvePageId()) {
    getData()
  }
})

watch(
  () => [props.pageId, route.query.id],
  () => {
    if (resolvePageId()) {
      getData()
    }
  }
)
</script>

<style scoped>
.page-editor-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 116px);
  min-width: 1100px;
}

@media (max-width: 991px) {
  .page-editor-container {
    min-width: 0;
    height: auto;
    min-height: 0;
  }

  .editor-header {
    padding: 10px 12px;
  }

  .editor-body {
    flex-direction: column;
    overflow: visible;
    height: auto;
  }

    .editor-left,
    .editor-center,
    .editor-right {
        width: 100% !important;
        max-width: 100%;
        flex: none;
        /* 窄屏取消画布最小宽度，允许堆叠布局 */
        min-width: 0 !important;
        overflow: visible;
    }

  .editor-left {
    order: 1;
  }

  .editor-center {
    order: 2;
    min-height: 240px;
  }

  .editor-right {
    order: 3;
  }

  .editor-footer {
    position: sticky;
    bottom: 0;
    z-index: 10;
  }
}

.editor-header {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  margin-right: 8px;
}

.editor-body {
    display: flex;
    flex: 1;
    justify-content: space-between;
    /* 超窄窗口时允许横向滚动，避免把画布挤扁/裁切 */
    overflow-x: auto;
    overflow-y: hidden;
    padding: 8px;
    gap: 8px;
}

.editor-left {
    /* 组件库面板需要 3 列 72px 卡片 + 间距，280px 在出现纵向滚动条时会挤到横向溢出，
     * 与 DataConfig 左栏统一加宽到 320px（纵向滚动条保留，横向不允许出现）。 */
    width: 320px;
    flex: none;
    overflow-y: auto;
    overflow-x: hidden;
}

.editor-center {
    flex: 1 1 0;
    /* 手机预览固定 375px，容器必须容得下，否则画布会被裁一朵白边 */
    min-width: 375px;
    min-height: 0;
    overflow: auto;
}

.editor-right {
    width: 340px;
    flex: none;
    overflow-y: auto;
    background: #fff;
    border-radius: 4px;
}

.editor-footer {
  height: 60px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.ml-2 {
  margin-left: 8px;
}
</style>
