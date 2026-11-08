<template>
  <div class="tabbar-editor">
    <div class="editor-body">
      <!-- 左侧手机预览 -->
      <div class="phone-preview-wrapper">
        <div class="preview-column">
          <div class="phone-preview">
            <div class="phone-notch" />
            <div class="phone-screen">
              <div class="phone-status">9:41</div>
              <div class="phone-body">底部导航预览</div>
            </div>
            <div class="tabbar" :style="{ background: form.background || '#f7f5f0' }">
              <div
                class="tabbar-item"
                v-for="(item, index) in visibleList"
                :key="item.key"
                :class="{ active: previewIndex === index }"
                @click="previewIndex = index"
              >
                <template v-if="form.showIcon">
                  <img
                    v-if="isImageIcon(item)"
                    class="tabbar-icon"
                    :src="previewIcon(item, index)"
                    alt=""
                  />
                  <i
                    v-else-if="previewIcon(item, index)"
                    class="iconfont"
                    :class="previewIcon(item, index)"
                    :style="{ color: previewColor(index) }"
                  />
                </template>
                <div class="tabbar-name" :style="{ color: previewColor(index) }">
                  {{ item.navTitle || '导航' }}
                </div>
              </div>
              <div v-if="!visibleList.length" class="tabbar-empty">暂无导航项</div>
            </div>
          </div>
          <div class="preview-tip">
            这是配置预览；H5 实际样式以站点版式（标品 / tea）为准
          </div>
        </div>
      </div>

      <!-- 右侧设置面板 -->
      <el-card class="settings-panel" shadow="never">
        <el-scrollbar class="settings-scroll">
          <div class="settings-title">
            <span class="title-bar" />
            底部导航设置
            <span class="title-tip">2 ~ 5 个导航项，改完保存即对 H5 生效</span>
          </div>

          <el-tabs v-model="activeTab">
            <!-- ============ 导航项 ============ -->
            <el-tab-pane label="导航项" name="content">
              <draggable
                v-model="form.list"
                item-key="key"
                handle=".drag-handle"
                animation="300"
              >
                <template #item="{ element, index }">
                  <div class="nav-item">
                    <div class="nav-item-header">
                      <el-icon class="drag-handle"><Rank /></el-icon>
                      <span class="nav-item-title">导航 {{ index + 1 }}</span>
                      <el-tag v-if="element.show === false" size="small" type="info">已隐藏</el-tag>
                      <el-switch
                        v-model="element.show"
                        size="small"
                        inline-prompt
                        active-text="显示"
                        inactive-text="隐藏"
                        class="nav-show-switch"
                      />
                      <el-button
                        v-if="form.list.length > min"
                        type="danger"
                        link
                        size="small"
                        @click="handleDelete(index)"
                      >
                        删除
                      </el-button>
                    </div>

                    <el-form label-width="76px" label-position="left">
                      <el-form-item label="名称">
                        <el-input v-model="element.navTitle" maxlength="6" show-word-limit placeholder="如：首页" />
                      </el-form-item>

                      <el-form-item label="跳转地址">
                        <el-input v-model="element.navUrl" placeholder="/pages/index/index" />
                      </el-form-item>

                      <el-form-item label="图标">
                        <div class="icon-config">
                          <el-radio-group v-model="element.iconType" size="small">
                            <el-radio-button value="none">无</el-radio-button>
                            <el-radio-button value="font">字体图标</el-radio-button>
                            <el-radio-button value="image">图片</el-radio-button>
                          </el-radio-group>

                          <div v-if="element.iconType === 'font'" class="icon-value-row">
                            <span class="icon-preview">
                              <i v-if="element.icon" class="iconfont" :class="element.icon" />
                            </span>
                            <el-select
                              v-model="element.icon"
                              filterable
                              allow-create
                              default-first-option
                              placeholder="选择或输入图标类名"
                              class="icon-select"
                            >
                              <el-option
                                v-for="ic in ICONS"
                                :key="ic.value"
                                :label="`${ic.label} · ${ic.value}`"
                                :value="ic.value"
                              />
                            </el-select>
                          </div>

                          <div v-else-if="element.iconType === 'image'" class="icon-value-row">
                            <el-image
                              v-if="element.icon"
                              :src="element.icon"
                              class="icon-image"
                              fit="contain"
                            />
                            <div v-else class="icon-placeholder">无图</div>
                            <el-input
                              v-model="element.icon"
                              placeholder="图片地址，如 /static/nav/home.png 或 https://…"
                              class="icon-select"
                            />
                          </div>

                          <div v-else class="icon-hint">
                            不显示图标 —— 纯文字导航（当前茶站版式用的就是这种）
                          </div>
                        </div>
                      </el-form-item>

                      <el-form-item v-if="element.iconType === 'font'" label="选中图标">
                        <el-select
                          v-model="element.selectedIcon"
                          filterable
                          allow-create
                          clearable
                          default-first-option
                          placeholder="不填则与默认图标一致"
                          class="icon-select"
                        >
                          <el-option
                            v-for="ic in ICONS"
                            :key="ic.value"
                            :label="`${ic.label} · ${ic.value}`"
                            :value="ic.value"
                          />
                        </el-select>
                      </el-form-item>
                    </el-form>
                  </div>
                </template>
              </draggable>

              <div v-if="form.list.length < max" class="add-row">
                <el-button type="primary" plain @click="handleAdd">
                  <el-icon><Plus /></el-icon>
                  添加导航
                </el-button>
              </div>
            </el-tab-pane>

            <!-- ============ 样式设置 ============ -->
            <el-tab-pane label="样式设置" name="styles">
              <el-form label-width="96px" label-position="left">
                <el-form-item label="显示图标">
                  <el-switch v-model="form.showIcon" />
                  <span class="form-tip">
                    关闭 = 纯文字导航；标品版式建议打开，茶站版式默认关闭
                  </span>
                </el-form-item>

                <el-form-item label="选中颜色">
                  <el-input v-model="form.selectedColor" class="color-input" placeholder="#B8976A">
                    <template #append>
                      <el-color-picker v-model="form.selectedColor" />
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="未选中颜色">
                  <el-input v-model="form.defaultColor" class="color-input" placeholder="#8A8578">
                    <template #append>
                      <el-color-picker v-model="form.defaultColor" />
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="栏底色">
                  <el-input v-model="form.background" class="color-input" placeholder="留空则跟随站点主题">
                    <template #append>
                      <el-color-picker v-model="form.background" />
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label-width="0">
                  <el-button link type="primary" @click="resetStyle">恢复默认配色</el-button>
                </el-form-item>
              </el-form>

              <el-alert type="info" :closable="false" show-icon class="mt-4">
                <template #title>说明</template>
                这里改的是全站底部导航（平台级）。保存后 H5 下一次刷新生效；
                已装修过的店铺如有自己的底部导航，仍以店铺配置为准。
              </el-alert>
            </el-tab-pane>
          </el-tabs>
        </el-scrollbar>
      </el-card>
    </div>

    <div class="editor-footer">
      <el-button @click="getData">重置</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Rank } from '@element-plus/icons-vue'
import Draggable from 'vuedraggable'
// H5 底部栏用的字体图标（与 uniapp static/css/iconfont 同源），后台做图标预览要能看到真图形
import '@/assets/iconfont-nav/iconfont.css'
import { getBottomNavDetail, saveBottomNav } from '@/api/mall/decoration'
import { unwrapDecorate } from '@/utils/decoratePayload'

/**
 * 底部导航编辑器
 *
 * 数据源与 H5 底部栏完全一致：hou_base_modularizationd 里 page_is_index = 3 那条的 page_data。
 * （旧版页面走 /web/mall/decorate/tabbar/*，读的是 hou_decorate_tabbar，H5 根本不读那张表，
 *  改完不生效 —— 这就是"后台改了菜单、前台纹丝不动"的原因，故整体切到 base 接口。）
 *
 * page_data 结构：
 * { color, defaultColor, background, showIcon,
 *   items: [{ navTitle, navUrl, icon, selectedIcon, show, navIndex }] }
 */

const DEFAULT_STYLE = {
  selectedColor: '#B8976A',
  defaultColor: '#8A8578',
  background: '#F7F5F0',
  showIcon: false
}

/** 可选字体图标（uniapp static/css/iconfont/iconfont.css 里的常用集合） */
const ICONS = [
  { label: '首页', value: 'icon-home-s1' },
  { label: '首页·实心', value: 'icon-home-s2' },
  { label: '首页·线性', value: 'icon-home-s3' },
  { label: '首页·描边', value: 'icon-home-s4' },
  { label: '分类', value: 'icon-class-s1' },
  { label: '分类·实心', value: 'icon-class-s2' },
  { label: '分类·线性', value: 'icon-class-s3' },
  { label: '分类·描边', value: 'icon-class-s4' },
  { label: '购物车', value: 'icon-cart-s1' },
  { label: '购物车·实心', value: 'icon-cart-s2' },
  { label: '购物车·线性', value: 'icon-cart-s3' },
  { label: '购物车·描边', value: 'icon-cart-s4' },
  { label: '我的', value: 'icon-user-s1' },
  { label: '我的·实心', value: 'icon-user-s2' },
  { label: '我的·线性', value: 'icon-user-s3' },
  { label: '我的·描边', value: 'icon-user-s4' },
  { label: '会员', value: 'icon-member-s1' },
  { label: '会员·实心', value: 'icon-member-s2' },
  { label: '会员·线性', value: 'icon-member-s3' },
  { label: '会员·描边', value: 'icon-member-s4' },
  { label: '内容', value: 'icon-content-s1' },
  { label: '内容·实心', value: 'icon-content-s2' },
  { label: '内容·线性', value: 'icon-content-s3' },
  { label: '内容·描边', value: 'icon-content-s4' },
  { label: '活动', value: 'icon-activity-s1' },
  { label: '活动·实心', value: 'icon-activity-s2' },
  { label: '活动·线性', value: 'icon-activity-s3' },
  { label: '活动·描边', value: 'icon-activity-s4' },
  { label: '搜索', value: 'icon-sousuo' },
  { label: '扫一扫', value: 'icon-saoyisao' },
  { label: '消息', value: 'icon-tongzhi1' },
  { label: '订单', value: 'icon-dingdanzhuangtai' }
]

/** 缺省导航（仅在库里没有配置时使用） */
const DEFAULT_TABS = [
  { navTitle: '首页', navUrl: '/pages/index/index', icon: 'icon-home-s1' },
  { navTitle: '分类', navUrl: '/pages/search/search', icon: 'icon-class-s1' },
  { navTitle: '购物车', navUrl: '/pages/cart/cart', icon: 'icon-cart-s1' },
  { navTitle: '我的', navUrl: '/pages/personal/personal', icon: 'icon-user-s1' }
]

const max = 5
const min = 2

const activeTab = ref('content')
const previewIndex = ref(0)
const saving = ref(false)
const pageId = ref(null)

const form = reactive({
  ...DEFAULT_STYLE,
  list: []
})

let keySeed = 0
const nextKey = () => `nav-${Date.now()}-${keySeed++}`

const isImageValue = (v) =>
  /^(https?:)?\/\//i.test(v) || String(v || '').indexOf('/') === 0 || String(v || '').indexOf('data:') === 0

const isImageIcon = (item) => item.iconType === 'image' || isImageValue(item.icon)

function toEditorItem(raw = {}) {
  const icon = raw.icon || ''
  let iconType = 'none'
  if (icon) iconType = isImageValue(icon) ? 'image' : 'font'
  return {
    key: nextKey(),
    navTitle: raw.navTitle || raw.name || '',
    navUrl: raw.navUrl || (raw.link && raw.link.path) || '',
    icon,
    selectedIcon: raw.selectedIcon || '',
    iconType,
    show: raw.show !== false
  }
}

const visibleList = computed(() => form.list.filter((item) => item.show !== false))

const previewIcon = (item, index) => {
  if (index === previewIndex.value && item.selectedIcon) return item.selectedIcon
  return item.icon
}

const previewColor = (index) => {
  return index === previewIndex.value
    ? (form.selectedColor || DEFAULT_STYLE.selectedColor)
    : (form.defaultColor || DEFAULT_STYLE.defaultColor)
}

const handleAdd = () => {
  if (form.list.length >= max) {
    ElMessage.warning(`最多添加${max}个导航`)
    return
  }
  form.list.push(toEditorItem({ navTitle: '', navUrl: '/pages/index/index' }))
}

const handleDelete = (index) => {
  if (form.list.length <= min) {
    ElMessage.warning(`最少保留${min}个导航`)
    return
  }
  form.list.splice(index, 1)
  if (previewIndex.value >= form.list.length) {
    previewIndex.value = Math.max(form.list.length - 1, 0)
  }
}

const resetStyle = () => {
  form.selectedColor = DEFAULT_STYLE.selectedColor
  form.defaultColor = DEFAULT_STYLE.defaultColor
  form.background = DEFAULT_STYLE.background
  form.showIcon = DEFAULT_STYLE.showIcon
}

/** 读取真实配置 */
const getData = async () => {
  try {
    const data = unwrapDecorate(await getBottomNavDetail())
    if (!data) {
      form.list = DEFAULT_TABS.map((item) => toEditorItem(item))
      return
    }
    pageId.value = data.pageId || null
    let cfg = {}
    if (data.pagedata) {
      try {
        cfg = typeof data.pagedata === 'string' ? JSON.parse(data.pagedata) : data.pagedata
      } catch (e) {
        console.error('底部导航配置解析失败', e)
      }
    }
    form.selectedColor = cfg.color || DEFAULT_STYLE.selectedColor
    form.defaultColor = cfg.defaultColor || DEFAULT_STYLE.defaultColor
    form.background = cfg.background || DEFAULT_STYLE.background
    form.showIcon = typeof cfg.showIcon === 'boolean' ? cfg.showIcon : DEFAULT_STYLE.showIcon
    const items = Array.isArray(cfg.items) ? cfg.items : []
    form.list = items.length ? items.map(toEditorItem) : DEFAULT_TABS.map((item) => toEditorItem(item))
    previewIndex.value = 0
  } catch (error) {
    console.error('获取底部导航失败', error)
    ElMessage.error('获取底部导航失败')
  }
}

/** 保存 */
const handleSave = async () => {
  if (form.list.length < min) {
    ElMessage.warning(`至少添加${min}个导航`)
    return
  }
  if (form.list.length > max) {
    ElMessage.warning(`最多添加${max}个导航`)
    return
  }
  if (form.list.some((item) => !String(item.navTitle || '').trim())) {
    ElMessage.warning('请填写导航名称')
    return
  }
  if (form.list.some((item) => !String(item.navUrl || '').trim())) {
    ElMessage.warning('请填写跳转地址')
    return
  }
  const payload = {
    color: form.selectedColor || DEFAULT_STYLE.selectedColor,
    defaultColor: form.defaultColor || DEFAULT_STYLE.defaultColor,
    background: form.background || '',
    showIcon: !!form.showIcon,
    items: form.list.map((item, index) => {
      const icon = item.iconType === 'none' ? '' : (item.icon || '')
      return {
        navTitle: String(item.navTitle || '').trim(),
        navUrlSign: String(item.navTitle || '').trim(),
        navUrl: String(item.navUrl || '').trim(),
        icon,
        selectedIcon: item.iconType === 'font' ? (item.selectedIcon || '') : '',
        show: item.show !== false,
        navIndex: index
      }
    })
  }
  saving.value = true
  try {
    // 注意键名：base 的 ModularizationdDTO 用 @JsonProperty("pagedata") 绑定，
    // 传 pageData 会被静默忽略并写入 NULL（把线上底栏清空），这里必须用 pagedata。
    const res = await saveBottomNav({
      pageId: pageId.value,
      pagedata: JSON.stringify(payload)
    })
    if (res && res.code !== undefined && res.code !== 1 && res.code !== 200) {
      ElMessage.error(res.message || '保存失败')
      return
    }
    ElMessage.success('保存成功，H5 刷新后生效')
    await getData()
  } catch (error) {
    console.error('保存底部导航失败', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  getData()
})
</script>

<style scoped>
.tabbar-editor {
  min-width: 800px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 116px);
}

.editor-body {
  display: flex;
  align-items: flex-start;
  flex: 1;
  overflow: hidden;
}

.phone-preview-wrapper {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 30px;
  flex: 1;
}

.preview-column {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.preview-tip {
  margin-top: 14px;
  font-size: 12px;
  color: #909399;
}

.phone-preview {
  width: 375px;
  height: 720px;
  position: relative;
  background: #f3f4f6;
  border: 12px solid #111827;
  border-radius: 36px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.phone-notch {
  position: absolute;
  top: 10px;
  left: 50%;
  z-index: 2;
  width: 108px;
  height: 10px;
  transform: translateX(-50%);
  background: #111827;
  border-radius: 8px;
}

.phone-screen {
  height: calc(100% - 56px);
  display: flex;
  flex-direction: column;
}

.phone-status {
  padding: 18px 20px 0;
  font-size: 12px;
  color: #6b7280;
}

.phone-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 13px;
}

.tabbar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  display: flex;
  align-items: center;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
}

.tabbar-empty {
  flex: 1;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}

.tabbar-icon {
  width: 22px;
  height: 22px;
  object-fit: contain;
}

.tabbar-name {
  margin-top: 3px;
  line-height: 1.2;
  font-size: 11px;
}

.settings-panel {
  width: 560px;
  margin-left: auto;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.settings-scroll {
  height: calc(100% - 20px);
}

.settings-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
}

.title-bar {
  width: 3px;
  height: 14px;
  background-color: var(--el-color-primary);
  margin-right: 8px;
  display: inline-block;
}

.title-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
  font-weight: normal;
}

.nav-item {
  background-color: #f5f7fa;
  padding: 14px 16px 2px;
  border-radius: 8px;
  margin-bottom: 14px;
}

.nav-item-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.drag-handle {
  cursor: move;
  margin-right: 8px;
  color: #909399;
}

.nav-item-title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
}

.nav-show-switch {
  margin-right: 10px;
}

.icon-config {
  width: 100%;
}

.icon-value-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.icon-preview {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  font-size: 18px;
  color: #606266;
  flex: none;
}

.icon-select {
  flex: 1;
}

.icon-image {
  width: 34px;
  height: 34px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
  flex: none;
}

.icon-placeholder {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  font-size: 11px;
  color: #b1b3b8;
  flex: none;
}

.icon-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.color-input {
  width: 220px;
}

.add-row {
  padding: 4px 0 12px;
}

.mt-4 {
  margin-top: 16px;
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

@media (max-width: 991px) {
  .tabbar-editor {
    min-width: 0;
    height: auto;
  }

  .editor-body {
    flex-direction: column;
    overflow: visible;
    height: auto;
  }

  .phone-preview-wrapper {
    padding: 12px;
    width: 100%;
  }

  .phone-preview {
    transform: scale(0.88);
    transform-origin: top center;
    margin: 0 auto;
  }

  .settings-panel {
    width: 100% !important;
    margin-left: 0 !important;
    height: auto !important;
  }

  .settings-scroll {
    height: auto !important;
  }

  .editor-footer {
    position: sticky;
    bottom: 0;
    z-index: 10;
  }
}
</style>

<style>
.el-input-group__append,
.el-input-group__prepend {
  padding: 0;
}
</style>
