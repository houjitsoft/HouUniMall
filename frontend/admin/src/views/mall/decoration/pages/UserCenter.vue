<template>
  <div class="page-editor-container">
    <div class="editor-body">
      <preview
        class="editor-center"
        v-model="selectWidgetIndex"
        @updatePageData="updatePageData"
        :pageData="getPageData"
        :pageMeta="getPageMeta"
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
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import Preview from '../components/editor/preview.vue'
import AttrSetting from '../components/editor/attr-setting.vue'
import widgets from '../components/widgets'
import { getPageDetail, savePage } from '@/api/mall/decoration'
import { getNonDuplicateID } from '@/utils/decoration'
import { unwrapDecorate, splitDecorateWidgets } from '@/utils/decoratePayload'

/**
 * 页面类型枚举
 * @type {Object<string, string>}
 */
const pagesTypeEnum = {
  USER: '2'
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
 * 更新页面数据
 * @param {Array<Object>} value - 新的页面数据
 */
const updatePageData = (value) => {
  menus[activeMenu.value].pageData = [...value]
}

/**
 * 页面菜单数据
 * @type {Object}
 */
const menus = reactive({
  [pagesTypeEnum.USER]: {
    id: '',
    type: 2,
    name: '个人中心',
    pageData: generatePageData([
      'user-info',
      'user-order',
      'my-service',
      'user-banner'
    ])
  }
})

/**
 * 当前激活的菜单
 * @type {import('vue').Ref<string>}
 */
const activeMenu = ref(pagesTypeEnum.USER)

/**
 * 当前选中的组件索引
 * @type {import('vue').Ref<number>}
 */
const selectWidgetIndex = ref(-1)

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
 * 获取页面详情数据
 */
const getData = async () => {
  try {
    const payload = unwrapDecorate(await getPageDetail({ type: 2 }))
    if (!payload) return
    const { pageData } = splitDecorateWidgets(payload.data)
    const target = menus[pagesTypeEnum.USER]
    target.id = payload.id || target.id
    target.type = payload.type || 2
    target.name = payload.name || '个人中心'
    if (pageData.length) {
      target.pageData = pageData
    }
  } catch (error) {
    console.error('获取个人中心装修失败', error)
    ElMessage.error('获取个人中心装修失败')
  }
}

/**
 * 保存页面数据
 */
const handleSave = async () => {
  try {
    const page = menus[pagesTypeEnum.USER]
    const payload = {
      type: 2,
      name: page.name || '个人中心',
      data: JSON.stringify(page.pageData || [])
    }
    if (page.id) payload.id = page.id
    await savePage(payload)
    ElMessage.success('保存成功')
    getData()
  } catch (error) {
    console.error('保存个人中心装修失败', error)
    ElMessage.error('保存失败')
  }
}

/**
 * 更新组件内容
 * @param {Object} content - 更新的内容
 */
const updateContent = (content) => {
  const page = menus[activeMenu.value]
  if (selectWidgetIndex.value < 0 || !page?.pageData?.[selectWidgetIndex.value]) return
  page.pageData[selectWidgetIndex.value].content = content
}

watch(
  activeMenu,
  () => {
    selectWidgetIndex.value = getPageData.value.findIndex((item) => !item.disabled)
    getData()
  },
  { immediate: true }
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

.editor-body {
  display: flex;
  flex: 1;
  justify-content: space-between;
  overflow: hidden;
  padding: 8px;
  gap: 8px;
}

.editor-center {
  flex: 1;
  overflow-y: auto;
}

.editor-right {
  width: 400px;
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
</style>
