<template>
  <div class="page-editor-container">
    <div class="editor-body">
      <el-card
        shadow="never"
        class="editor-left"
        :body-style="{ 'padding-right': 0 }"
      >
        <data-select
          @addwidgetName="addwidgetName"
          @resetdragIndex="resetdragIndex"
        />
      </el-card>

      <data-preview
        class="editor-center"
        ref="previewRef"
        v-model="selectWidgetIndex"
        @delwidget="delwidget"
        @copywidget="copywidget"
        @addwidget="addwidget"
        @updatePageData="updatePageData"
        @updataTitle="updataTitle"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import DataSelect from '../components/editor/data-select.vue'
import DataPreview from '../components/editor/data-preview.vue'
import AttrSetting from '../components/editor/attr-setting.vue'
import widgets from '../components/widgets'
import { getPageDetail, savePage } from '@/api/mall/decoration'
import { getNonDuplicateID } from '@/utils/decoration'
import { unwrapDecorate, splitDecorateWidgets, stringifyDecorateSave } from '@/utils/decoratePayload'

/**
 * 页面类型枚举
 * @type {Object<string, string>}
 */
const pagesTypeEnum = {
  SECKILL: '7',
  TEAM: '8'
}

/**
 * 预览组件引用
 * @type {import('vue').Ref<Object>}
 */
const previewRef = ref(null)

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
 * 处理添加组件名称
 * @param {string} name - 组件名称
 */
const addwidgetName = (name) => {
  widgetName.value = name
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
 * 更新页面数据
 * @param {Array<Object>} value - 新的页面数据
 */
const updatePageData = (value) => {
  menus[activeMenu.value].pageData = [...value]
}

/**
 * 切换标题（秒杀/拼团）
 * @param {number} value - 1表示秒杀，2表示拼团
 */
const updataTitle = (value) => {
  activeMenu.value = value === 1 ? pagesTypeEnum.SECKILL : pagesTypeEnum.TEAM
  getData()
}

/**
 * 页面菜单数据
 * @type {Object}
 */
const menus = reactive({
  [pagesTypeEnum.SECKILL]: {
    id: '',
    type: 7,
    name: '限时秒杀',
    pageMeta: generatePageData(['data-meta']),
    pageData: generatePageData(['banner'])
  },
  [pagesTypeEnum.TEAM]: {
    id: '',
    type: 8,
    name: '超级拼团',
    pageMeta: generatePageData(['data-meta']),
    pageData: generatePageData(['banner'])
  }
})

/**
 * 当前激活的菜单
 * @type {import('vue').Ref<string>}
 */
const activeMenu = ref(pagesTypeEnum.SECKILL)

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
 * 获取页面详情数据
 */
const getData = async () => {
  try {
    const current = menus[activeMenu.value]
    const payload = unwrapDecorate(await getPageDetail({ type: current.type }))
    if (!payload) return
    const { pageMeta, pageData } = splitDecorateWidgets(payload.data, ['data-meta'])
    current.pageMeta = pageMeta.length ? pageMeta : generatePageData(['data-meta'])
    if (pageData.length) {
      current.pageData = pageData
    }
    current.id = payload.id || current.id
    current.type = payload.type || current.type
    if (payload.name) current.name = payload.name
  } catch (error) {
    console.error('获取数据配置失败', error)
    ElMessage.error('获取数据配置失败')
  }
}

/**
 * 保存页面数据
 */
const handleSave = async () => {
  try {
    const current = menus[activeMenu.value]
    const payload = {
      type: current.type,
      name: current.name,
      data: stringifyDecorateSave(current.pageMeta, current.pageData)
    }
    if (current.id) payload.id = current.id
    await savePage(payload)
    ElMessage.success('保存成功')
    getData()
  } catch (error) {
    console.error('保存数据配置失败', error)
    ElMessage.error('保存失败')
  }
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
      menu.pageMeta = generatePageData(['data-meta'])
    }
    menu.pageMeta[0].content = content
    return
  }
  if (menu.pageData?.[selectWidgetIndex.value]) {
    menu.pageData[selectWidgetIndex.value].content = content
  }
}

onMounted(() => {
  getData()
})
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

.editor-left {
  width: 320px;
  flex: none;
  overflow-y: auto;
  overflow-x: hidden;
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
