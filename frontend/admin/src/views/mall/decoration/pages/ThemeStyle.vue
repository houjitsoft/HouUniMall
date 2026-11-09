<template>
  <div class="shop-theme" v-loading="loading">
    <el-card shadow="never" class="theme-card">
      <div class="section-block">
        <h3 class="section-title">网站品牌风格</h3>
        <p class="section-desc">先选择网站风格，再进入首页编辑；页面显式背景和组件配置优先。奢侈品简单风格为原创设计，非源站像素复刻。</p>
        <el-radio-group v-model="brandStyle">
          <el-radio-button label="">沿用当前风格</el-radio-button>
          <el-radio-button label="tea">奢侈品简单风格 · 暖象牙 / 墨绿 / 茶褐金</el-radio-button>
        </el-radio-group>
        <div v-if="brandStyle === 'tea'" class="tea-brand-preview">
          <small>奢侈品简单风格 · LUXURY</small><h2>一盏茶，慢下来</h2>
          <p>宽幅首屏 · 分类导航 · 双列商品 · 品牌留白</p>
        </div>
      </div>
    </el-card>
    <template v-if="isMobile">
      <el-card shadow="never" class="theme-card">
        <template #header>
          <div class="card-header">
            <div>
              <span class="title">移动装修</span>
              <p class="subtitle">选中的颜色会立刻作用到 H5 / 小程序等移动端店面。下方预览为当前主题的实时样稿。</p>
            </div>
          </div>
        </template>

        <div class="theme-list">
          <div
            class="theme-item"
            :class="{ active: item.className === theme.current }"
            v-for="(item, index) in theme.list"
            :key="index"
            @click="handleThemeChange(item.className)"
          >
            <div
              :class="['theme-item__name', `theme-item__name--${item.className}`]"
              :style="item.variable"
            />
            <div class="theme-item__title">{{ item.name }}</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="theme-card">
        <template #header>
          <div class="card-header">
            <div>
              <span class="title">DIY 样式</span>
              <p class="subtitle">导航色改完立刻反映在下方样稿。自定义主题时再调主色与深浅辅助色。</p>
            </div>
          </div>
        </template>

        <div class="theme-form-wrap" v-if="currentIndex !== -1">
          <el-form label-width="140px" class="theme-form hc-form-stack" label-position="right">
            <el-form-item label="导航顶部文字颜色">
              <el-radio-group v-model="theme.list[currentIndex].nav_text_color">
                <el-radio-button label="#ffffff">白色</el-radio-button>
                <el-radio-button label="#000000">黑色</el-radio-button>
              </el-radio-group>
              <span class="field-hint">用在顶栏文字、搜索框外的字色</span>
            </el-form-item>

            <el-form-item label="导航顶部背景颜色">
              <el-input
                v-model="theme.list[currentIndex].nav_bg_color"
                class="color-input"
                placeholder="#ffffff"
              >
                <template #append>
                  <el-color-picker v-model="theme.list[currentIndex].nav_bg_color" />
                </template>
              </el-input>
            </el-form-item>

            <template v-if="theme.current === 'custom_theme'">
              <el-form-item label="自定义主题颜色">
                <el-input
                  v-model="theme.list[currentIndex].variable['--theme-color']"
                  class="color-input"
                  placeholder="请输入"
                >
                  <template #append>
                    <el-color-picker
                      v-model="theme.list[currentIndex].variable['--theme-color']"
                      @change="(value) => handleColorChange(currentIndex, 'color', value)"
                    />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="自定义辅助深色主题">
                <el-input
                  v-model="theme.list[currentIndex].variable['--theme-dark-color']"
                  class="color-input"
                  placeholder="请输入"
                >
                  <template #append>
                    <el-color-picker
                      v-model="theme.list[currentIndex].variable['--theme-dark-color']"
                      @change="(value) => handleColorChange(currentIndex, 'dark_color', value)"
                    />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="自定义辅助淡色主题">
                <el-input
                  v-model="theme.list[currentIndex].variable['--theme-light-color']"
                  class="color-input"
                  placeholder="请输入"
                >
                  <template #append>
                    <el-color-picker
                      v-model="theme.list[currentIndex].variable['--theme-light-color']"
                    />
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="自定义辅助禁用主题">
                <el-input
                  v-model="theme.list[currentIndex].variable['--theme-disabled-color']"
                  class="color-input"
                  placeholder="请输入"
                >
                  <template #append>
                    <el-color-picker
                      v-model="theme.list[currentIndex].variable['--theme-disabled-color']"
                    />
                  </template>
                </el-input>
              </el-form-item>
            </template>

            <el-form-item label="页面预览">
              <ThemeLivePreview
                :color="previewColor"
                :dark="previewDark"
                :light="previewLight"
                :nav-bg="previewNavBg"
                :nav-text="previewNavText"
                :name="previewName"
              />
            </el-form-item>
          </el-form>
        </div>
        <el-empty v-else description="暂无可用主题，请刷新重试" />
      </el-card>
    </template>

    <el-card v-if="isPc" shadow="never" class="theme-card">
      <template #header>
        <div class="card-header">
          <div>
            <span class="title">PC装修</span>
            <p class="subtitle">
              决定 PC 首页骨架、内容宽度和顶栏菜单。保存后刷新
              <a href="https://bbc.houjit.com/" target="_blank" rel="noreferrer">bbc.houjit.com</a>
              即可看到整站变化。
            </p>
          </div>
          <div class="header-actions">
            <el-button :icon="View" @click="openMallPreview">预览商城</el-button>
          </div>
        </div>
      </template>

      <el-form label-width="108px" class="theme-form hc-form-stack" label-position="right">
        <!-- 外观布局 -->
        <section class="section-block">
          <div class="section-head">
            <h3 class="section-title">外观布局</h3>
            <p class="section-desc">首页骨架、内容宽度、圆角与默认币种</p>
          </div>

          <el-form-item label="布局风格">
            <div class="layout-cards">
              <button
                v-for="item in layoutOptions"
                :key="item.value"
                type="button"
                class="layout-card"
                :class="{ active: theme.pc_layout === item.value }"
                @click="theme.pc_layout = item.value"
              >
                <div class="layout-mock" :class="`layout-mock--${item.value}`">
                  <span class="mock-nav" />
                  <span class="mock-body">
                    <i v-if="item.value !== 'intl'" class="mock-aside" />
                    <i class="mock-hero" />
                    <i v-if="item.value === 'cn'" class="mock-user" />
                  </span>
                  <span class="mock-floor" />
                </div>
                <div class="layout-card__meta">
                  <strong>{{ item.title }}</strong>
                  <em>{{ item.desc }}</em>
                </div>
                <span v-if="theme.pc_layout === item.value" class="layout-card__check">已选</span>
              </button>
            </div>
          </el-form-item>

          <div class="option-grid">
            <el-form-item label="内容宽度">
              <el-radio-group v-model="theme.pc_container">
                <el-radio-button label="1200">1200px</el-radio-button>
                <el-radio-button label="1280">1280px</el-radio-button>
                <el-radio-button label="1440">1440px</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="圆角">
              <el-radio-group v-model="theme.pc_radius">
                <el-radio-button label="sm">小</el-radio-button>
                <el-radio-button label="md">中</el-radio-button>
                <el-radio-button label="lg">大</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="默认币种">
              <el-radio-group v-model="theme.pc_currency">
                <el-radio-button label="CNY">人民币 ¥</el-radio-button>
                <el-radio-button label="USD">美元 $</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </div>
        </section>

        <!-- 顶部菜单 -->
        <section class="section-block">
          <div class="section-head">
            <div>
              <h3 class="section-title">顶部菜单</h3>
              <p class="section-desc">控制 PC 顶栏导航名称、英文名、路径、图标与显示开关；可拖动或用上下按钮排序</p>
            </div>
            <div class="section-actions">
              <el-button type="primary" plain :icon="Plus" @click="addPcNav">添加菜单</el-button>
              <el-button plain @click="resetPcNav">恢复默认</el-button>
            </div>
          </div>

          <draggable
            v-if="theme.pc_nav?.length"
            v-model="theme.pc_nav"
            :item-key="(item) => item._navId || item.path"
            class="editor-list"
            handle=".nav-drag-handle"
            :animation="180"
            ghost-class="floor-row--ghost"
            @end="onPcNavDragEnd"
          >
            <template #item="{ element: item, index }">
              <div class="editor-row nav-row">
                <button
                  type="button"
                  class="floor-drag-handle nav-drag-handle"
                  :title="`拖动调整第 ${index + 1} 项顺序，也可用上下方向键移动`"
                  :aria-label="`第 ${index + 1} 项排序手柄`"
                  @click.stop
                  @keydown.up.prevent="movePcNav(index, -1)"
                  @keydown.down.prevent="movePcNav(index, 1)"
                >
                  <el-icon><Rank /></el-icon>
                  <span>拖动排序</span>
                </button>
                <button
                  type="button"
                  class="floor-move-button"
                  :disabled="index === 0"
                  :title="`上移第 ${index + 1} 项`"
                  :aria-label="`上移第 ${index + 1} 项`"
                  @click.stop="movePcNav(index, -1)"
                >
                  <el-icon><ArrowUp /></el-icon>
                </button>
                <button
                  type="button"
                  class="floor-move-button"
                  :disabled="index === theme.pc_nav.length - 1"
                  :title="`下移第 ${index + 1} 项`"
                  :aria-label="`下移第 ${index + 1} 项`"
                  @click.stop="movePcNav(index, 1)"
                >
                  <el-icon><ArrowDown /></el-icon>
                </button>
                <div class="row-index">{{ index + 1 }}</div>
                <div class="row-fields">
                  <el-input v-model="item.name" placeholder="中文名称" />
                  <el-input v-model="item.name_en" placeholder="English" />
                  <el-input v-model="item.path" placeholder="/path" />
                  <el-input v-model="item.image" placeholder="图标 URL（可选）" />
                </div>
                <div class="row-actions">
                  <el-switch
                    v-model="item.target"
                    :active-value="1"
                    :inactive-value="0"
                    active-text="新窗"
                    inactive-text="本窗"
                    inline-prompt
                  />
                  <el-switch v-model="item.visible" active-text="显示" inactive-text="隐藏" inline-prompt />
                  <el-button link type="danger" @click="removePcNav(index)">删除</el-button>
                </div>
              </div>
            </template>
          </draggable>
          <el-empty
            v-else
            description="还没有顶部菜单，点击右上角添加；全部隐藏时前台顶栏不显示导航"
            :image-size="64"
          />
        </section>

        <!-- 首页精选 -->
        <section class="section-block">
          <div class="section-head">
            <div>
              <h3 class="section-title">首页精选模块</h3>
              <p class="section-desc">关闭后前台不再展示对应精选区块；下方「PC 楼层」仍由本页配置控制</p>
            </div>
          </div>
          <div class="feature-toggles">
            <label class="feature-toggle" :class="{ on: theme.pc_show_rank }">
              <el-switch v-model="theme.pc_show_rank" />
              <div>
                <strong>精选榜单</strong>
                <span>展示人气 / 热销榜入口</span>
              </div>
            </label>
            <label class="feature-toggle" :class="{ on: theme.pc_show_recommend }">
              <el-switch v-model="theme.pc_show_recommend" />
              <div>
                <strong>精选推荐</strong>
                <span>展示首页推荐货架</span>
              </div>
            </label>
          </div>
        </section>

        <!-- PC 楼层 -->
        <section class="section-block section-block--last">
          <div class="section-head">
            <div>
              <h3 class="section-title">PC 楼层装修</h3>
              <p class="section-desc">
                按住楼层左侧手柄可拖动排序，调整后点击底部「保存配置」生效。可单独隐藏/删除；「恢复默认」会按当前一级分类重新铺齐。
              </p>
            </div>
            <div class="section-actions">
              <el-button type="primary" plain :icon="Plus" @click="addPcFloor">新增促销楼层</el-button>
              <el-button type="success" plain :icon="Plus" @click="addCategoryFloor">添加分类楼层</el-button>
              <el-button plain @click="resetPcFloors">恢复默认</el-button>
            </div>
          </div>

          <draggable
            v-if="theme.pc_floors?.length"
            v-model="theme.pc_floors"
            item-key="_dragId"
            class="editor-list floor-editor-list"
            handle=".floor-drag-handle"
            :animation="180"
            ghost-class="floor-row--ghost"
            chosen-class="floor-row--chosen"
            drag-class="floor-row--dragging"
            @end="onPcFloorDragEnd"
          >
            <template #item="{ element: item, index }">
              <div class="editor-row floor-row">
                <div class="floor-row__top">
                  <button
                    type="button"
                    class="floor-drag-handle"
                    :title="`拖动调整第 ${index + 1} 层顺序，也可用上下方向键移动`"
                    :aria-label="`第 ${index + 1} 层排序手柄`"
                    @click.stop
                    @keydown.up.prevent="movePcFloor(index, -1)"
                    @keydown.down.prevent="movePcFloor(index, 1)"
                  >
                    <el-icon><Rank /></el-icon>
                    <span>拖动排序</span>
                  </button>
                  <button
                    type="button"
                    class="floor-move-button"
                    :disabled="index === 0"
                    :title="`上移第 ${index + 1} 层`"
                    :aria-label="`上移第 ${index + 1} 层`"
                    @click.stop="movePcFloor(index, -1)"
                  >
                    <el-icon><ArrowUp /></el-icon>
                  </button>
                  <button
                    type="button"
                    class="floor-move-button"
                    :disabled="index === theme.pc_floors.length - 1"
                    :title="`下移第 ${index + 1} 层`"
                    :aria-label="`下移第 ${index + 1} 层`"
                    @click.stop="movePcFloor(index, 1)"
                  >
                    <el-icon><ArrowDown /></el-icon>
                  </button>
                  <div class="row-index">{{ index + 1 }}</div>
                  <el-tag size="small" :type="item.source === 'category' ? 'warning' : 'primary'" effect="plain">
                    {{ item.source === 'category' ? '分类楼层' : '促销货架' }}
                  </el-tag>
                  <el-switch v-model="item.visible" active-text="显示" inactive-text="隐藏" inline-prompt />
                  <el-button link type="danger" class="floor-del" @click="removePcFloor(index)">删除</el-button>
                </div>
                <div class="floor-row__fields">
                  <el-input v-model="item.name" placeholder="楼层名称" />
                  <el-input
                    v-if="item.source !== 'category'"
                    v-model="item.subtitle"
                    placeholder="展示副标题"
                  />
                  <el-select
                    v-model="item.source"
                    placeholder="商品来源"
                    class="floor-select"
                    @change="onPcFloorSourceChange(item)"
                  >
                    <el-option-group label="促销活动">
                      <el-option label="热门推荐" value="hotGoods" />
                      <el-option label="新品上架" value="newGoods" />
                      <el-option label="猜你喜欢" value="guessGoods" />
                      <el-option label="人气榜单" value="popularGoods" />
                    </el-option-group>
                    <el-option-group label="分类楼层">
                      <el-option label="一级分类（截图样式）" value="category" />
                    </el-option-group>
                  </el-select>
                  <el-select
                    v-if="item.source === 'category'"
                    v-model="item.categoryId"
                    placeholder="选择一级分类"
                    filterable
                    class="floor-select"
                    @change="(val) => onPcFloorCategoryChange(item, val)"
                  >
                    <el-option
                      v-for="cat in level1Categories"
                      :key="cat.id"
                      :label="cat.name"
                      :value="cat.id"
                    />
                  </el-select>
                  <template v-if="item.source !== 'category'">
                    <el-input v-model="item.bannerImage" placeholder="楼层广告图 URL" />
                    <el-input v-model="item.bannerLink" placeholder="广告跳转地址（可选）" />
                  </template>
                </div>
              </div>
            </template>
          </draggable>
          <el-empty
            v-else
            description="还没有楼层，可点「恢复默认」铺齐促销 + 全部一级分类"
            :image-size="64"
          />
        </section>
      </el-form>
    </el-card>

    <div class="footer-btns">
      <el-button type="primary" size="large" :loading="saving" @click="handleSave">保存配置</el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, ArrowUp, Plus, Rank, View } from '@element-plus/icons-vue'
import Draggable from 'vuedraggable'
import { getMallStyle, setMallStyle } from '@/api/mall/decoration'
import request from '@/utils/request'
import ThemeLivePreview from './ThemeLivePreview.vue'
import { teaTheme } from '@decoration/decoration-rules.mjs'

const route = useRoute()
/** pc | mobile，由路由 meta.platform 决定展示分区 */
const platform = computed(() => {
  const p = route.meta?.platform || route.query?.platform || 'mobile'
  return p === 'pc' ? 'pc' : 'mobile'
})
const isPc = computed(() => platform.value === 'pc')
const isMobile = computed(() => platform.value === 'mobile')

const loading = ref(false)
const saving = ref(false)
let pcFloorDragSeed = 0

const createPcFloorDragId = () => `pc-floor-${Date.now().toString(36)}-${++pcFloorDragSeed}`

const layoutOptions = [
  { value: 'cn', title: '国内站', desc: '分类侧栏 + 楼层货架' },
  { value: 'intl', title: '国际站', desc: '大图首屏 + 标签导航' },
  { value: 'hybrid', title: '双市场', desc: '中西兼顾，默认推荐' },
]

/**
 * 主题数据对象
 * @type {{ current: string, list: Array<Object> }}
 */
const theme = reactive({
  current: 'red_theme',
  list: [],
  pc_template_code: '',
  pc_layout: 'hybrid',
  pc_container: '1280',
  pc_radius: 'md',
  pc_currency: 'CNY',
  pc_show_rank: true,
  pc_show_recommend: true,
  pc_nav: [
    { name: '首页', name_en: 'Home', path: '/', visible: true },
    { name: '商家店铺', name_en: 'Stores', path: '/store/list', visible: true },
    { name: '特色馆', name_en: 'Featured', path: '/feature', visible: true },
    { name: '积分商城', name_en: 'Points', path: '/points', visible: true },
    { name: '限时秒杀', name_en: 'Flash Sale', path: '/activity/seckill', visible: true },
    { name: '限时折扣', name_en: 'Deals', path: '/activity/discount', visible: true },
  ],
  pc_floors: [
    { _dragId: createPcFloorDragId(), name: '热门推荐', subtitle: '大家都在买', source: 'hotGoods', categoryId: 0, visible: true, bannerImage: '', bannerLink: '' },
    { _dragId: createPcFloorDragId(), name: '新品上架', subtitle: '潮流好物抢先看', source: 'newGoods', categoryId: 0, visible: true, bannerImage: '', bannerLink: '' },
    { _dragId: createPcFloorDragId(), name: '猜你喜欢', subtitle: '为你精选好物', source: 'guessGoods', categoryId: 0, visible: true, bannerImage: '', bannerLink: '' },
  ],
})

/**
 * 当前选中主题在列表中的索引
 * @type {import('vue').ComputedRef<number>}
 */
const currentIndex = computed(() => {
  return theme.list.findIndex((item) => item.className === theme.current)
})

/**
 * 「网站品牌风格」两端共用同一控件：
 * - PC 写 mall_style.pc_template_code（前台 frontend/pc 据此切换 tea 版式）
 * - 移动端写 mall_style.current + 追加 teaTheme（H5 据此整站换肤）
 * 统一由 v-model 的 setter 处理，不再依赖 @change 事件兜底。
 */
const brandStyle = computed({
  get: () => isPc.value ? theme.pc_template_code || '' : theme.current === 'tea_theme' ? 'tea' : '',
  set: (value) => {
    if (isPc.value) {
      theme.pc_template_code = value
      return
    }
    if (value === 'tea') {
      if (!theme.list.some(item => item.className === 'tea_theme')) {
        theme.list.push(JSON.parse(JSON.stringify(teaTheme)))
      }
      theme.current = 'tea_theme'
    } else if (theme.current === 'tea_theme') {
      theme.current = theme.list.find(item => item.className !== 'tea_theme')?.className || 'red_theme'
    }
  }
})

const currentTheme = computed(() => theme.list[currentIndex.value] || {})
const previewColor = computed(
  () => currentTheme.value.variable?.['--theme-color'] || currentTheme.value.color || '#E0453C'
)
const previewDark = computed(
  () => currentTheme.value.variable?.['--theme-dark-color'] || currentTheme.value.dark_color || previewColor.value
)
const previewLight = computed(
  () => currentTheme.value.variable?.['--theme-light-color'] || '#FDF0EF'
)
const previewNavBg = computed(() => currentTheme.value.nav_bg_color || '#ffffff')
const previewNavText = computed(() => currentTheme.value.nav_text_color || '#111827')
const previewName = computed(() => currentTheme.value.name || '商城')

/**
 * 处理主题切换
 * @param {string} className - 主题类名
 */
const openMallPreview = () => {
  window.open('https://bbc.houjit.com/', '_blank', 'noopener,noreferrer')
}

const handleThemeChange = (className) => {
  theme.current = className
}

/**
 * 处理自定义颜色变更
 * @param {number} index - 主题索引
 * @param {string} field - 变更的字段名
 * @param {string} value - 颜色值
 */
const handleColorChange = (index, field, value) => {
  theme.list[index][field] = value
}

/** PC 顶栏菜单默认项：与后端 MallStyleDefaults.defaultPcNav 同构 */
const DEFAULT_PC_NAV = [
  { name: '首页', name_en: 'Home', path: '/', image: '', target: 0, visible: true },
  { name: '商家店铺', name_en: 'Stores', path: '/store/list', image: '', target: 0, visible: true },
  { name: '特色馆', name_en: 'Featured', path: '/feature', image: '', target: 0, visible: true },
  { name: '积分商城', name_en: 'Points', path: '/points', image: '', target: 0, visible: true },
  { name: '限时秒杀', name_en: 'Flash Sale', path: '/activity/seckill', image: '', target: 0, visible: true },
  { name: '限时折扣', name_en: 'Deals', path: '/activity/discount', image: '', target: 0, visible: true },
]

let pcNavDragSeed = 0
const createPcNavDragId = () => `pc-nav-${Date.now().toString(36)}-${++pcNavDragSeed}`

/**
 * 顶部菜单归一化：与 pc_floors 同口径，后端返回任意结构/旧结构都能安全渲染。
 * image / target 对应前台 Header.vue 的 navImage / navNewOpen。
 */
const normalizePcNav = (list) => {
  if (!Array.isArray(list)) return []
  return list
    .filter((item) => item && typeof item === 'object')
    .map((item) => ({
      _navId: item._navId || createPcNavDragId(),
      name: String(item.name ?? item.navTitle ?? '').trim(),
      name_en: String(item.name_en ?? '').trim(),
      path: String(item.path ?? item.navUrl ?? '/').trim() || '/',
      image: String(item.image ?? item.navImage ?? '').trim(),
      target: Number(item.target ?? item.navNewOpen ?? 0) === 1 ? 1 : 0,
      visible: item.visible !== false && item.visible !== 0 && item.visible !== '0',
    }))
}

const addPcNav = () => {
  if (!Array.isArray(theme.pc_nav)) theme.pc_nav = []
  theme.pc_nav.push(normalizePcNav([{ name: '新菜单', name_en: 'New', path: '/', visible: true }])[0])
}

const removePcNav = (index) => {
  const name = theme.pc_nav[index]?.name || '该菜单'
  ElMessageBox.confirm(`确定删除「${name}」？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    confirmButtonClass: 'el-button--danger',
  })
    .then(() => {
      theme.pc_nav.splice(index, 1)
    })
    .catch(() => {})
}

const movePcNav = (index, offset) => {
  if (!Array.isArray(theme.pc_nav)) return
  const targetIndex = index + offset
  if (targetIndex < 0 || targetIndex >= theme.pc_nav.length) return
  const [nav] = theme.pc_nav.splice(index, 1)
  theme.pc_nav.splice(targetIndex, 0, nav)
  ElMessage.success(`已移到第 ${targetIndex + 1} 项，点击底部「保存配置」后生效`)
}

const onPcNavDragEnd = ({ oldIndex, newIndex } = {}) => {
  if (!Number.isInteger(oldIndex) || !Number.isInteger(newIndex) || oldIndex === newIndex) return
  ElMessage.success(`已移到第 ${newIndex + 1} 项，点击底部「保存配置」后生效`)
}

const resetPcNav = async () => {
  try {
    await ElMessageBox.confirm(
      '将恢复为 6 条默认顶部菜单，当前自定义菜单会丢失。继续吗？',
      '恢复默认菜单',
      { type: 'warning', confirmButtonText: '恢复默认' }
    )
  } catch {
    return
  }
  theme.pc_nav = normalizePcNav(DEFAULT_PC_NAV)
  ElMessage.success('已恢复默认顶部菜单，点击底部「保存配置」后生效')
}

/** 保存前校验：避免前台渲染空名称、坏路径或重复导航；允许全部隐藏/清空 */
const validatePcNav = () => {
  if (!Array.isArray(theme.pc_nav) || !theme.pc_nav.length) return ''
  const seen = new Set()
  for (let i = 0; i < theme.pc_nav.length; i += 1) {
    const item = theme.pc_nav[i]
    const label = `第 ${i + 1} 项`
    if (!String(item.name || '').trim()) return `${label} 菜单名称不能为空`
    const path = String(item.path || '').trim()
    if (!path) return `${label} 路径不能为空`
    if (!/^\/|^https?:\/\//i.test(path)) return `${label} 路径需以 / 或 http(s):// 开头`
    if (seen.has(path)) return `${label} 路径与前面重复：${path}`
    seen.add(path)
  }
  return ''
}

const level1Categories = ref([])

const normalizeCategoryId = (value) => {
  const n = Number(value)
  return Number.isFinite(n) && n > 0 ? n : 0
}

const createPromoFloor = (name, subtitle, source) => ({
  _dragId: createPcFloorDragId(),
  name,
  subtitle,
  source,
  categoryId: 0,
  visible: true,
  bannerImage: '',
  bannerLink: '',
})

const createCategoryFloor = (cat) => ({
  _dragId: createPcFloorDragId(),
  name: cat.name,
  subtitle: '',
  source: 'category',
  categoryId: cat.id,
  visible: true,
  bannerImage: '',
  bannerLink: '',
})

/** 默认：三层促销货架 + 全部一级分类楼层（均启用） */
const buildDefaultPcFloors = () => [
  createPromoFloor('热门推荐', '大家都在买', 'hotGoods'),
  createPromoFloor('新品上架', '潮流好物抢先看', 'newGoods'),
  createPromoFloor('猜你喜欢', '为你精选好物', 'guessGoods'),
  ...level1Categories.value.map((cat) => createCategoryFloor(cat)),
]

/** 补齐缺失的一级分类楼层，默认启用；不改动已有楼层的显示状态 */
const ensureAllCategoryFloors = () => {
  if (!Array.isArray(theme.pc_floors)) theme.pc_floors = []
  if (!level1Categories.value.length) return 0
  const used = new Set(
    theme.pc_floors
      .filter((item) => item?.source === 'category')
      .map((item) => normalizeCategoryId(item.categoryId))
      .filter(Boolean)
  )
  let added = 0
  for (const cat of level1Categories.value) {
    if (used.has(cat.id)) continue
    theme.pc_floors.push(createCategoryFloor(cat))
    used.add(cat.id)
    added += 1
  }
  return added
}

const loadLevel1Categories = async () => {
  try {
    const res = await request.get('/web/mall/index/category')
    const list = res?.data?.category_lists || res?.data?.lists || res?.data?.list || []
    level1Categories.value = (Array.isArray(list) ? list : [])
      .map((item) => ({
        id: normalizeCategoryId(item.id ?? item.catId),
        name: item.name || item.catName || '',
      }))
      .filter((item) => item.id && item.name)
  } catch (error) {
    console.error('加载一级分类失败', error)
    level1Categories.value = []
  }
}

const onPcFloorSourceChange = (item) => {
  if (!item) return
  if (item.source === 'category') {
    item.bannerImage = item.bannerImage || ''
    item.bannerLink = item.bannerLink || ''
    if (!normalizeCategoryId(item.categoryId) && level1Categories.value.length) {
      onPcFloorCategoryChange(item, level1Categories.value[0].id)
    }
  } else {
    item.categoryId = 0
  }
}

const onPcFloorCategoryChange = (item, categoryId) => {
  if (!item) return
  const id = normalizeCategoryId(categoryId)
  item.categoryId = id
  const hit = level1Categories.value.find((cat) => cat.id === id)
  if (hit?.name) item.name = hit.name
}

const addPcFloor = () => {
  if (!Array.isArray(theme.pc_floors)) theme.pc_floors = []
  theme.pc_floors.push(createPromoFloor('新促销楼层', '', 'hotGoods'))
}

/** 追加一条尚未使用的一级分类楼层 */
const addCategoryFloor = async () => {
  if (!level1Categories.value.length) {
    await loadLevel1Categories()
  }
  if (!level1Categories.value.length) {
    ElMessage.warning('暂无一级分类可添加')
    return
  }
  if (!Array.isArray(theme.pc_floors)) theme.pc_floors = []
  const used = new Set(
    theme.pc_floors
      .filter((item) => item?.source === 'category')
      .map((item) => normalizeCategoryId(item.categoryId))
      .filter(Boolean)
  )
  const next = level1Categories.value.find((cat) => !used.has(cat.id))
  if (!next) {
    ElMessage.info('全部一级分类楼层已添加')
    return
  }
  theme.pc_floors.push(createCategoryFloor(next))
  ElMessage.success(`已添加分类楼层：${next.name}`)
}

const removePcFloor = (index) => {
  const name = theme.pc_floors[index]?.name || '该楼层'
  ElMessageBox.confirm(`确定删除「${name}」？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
    confirmButtonClass: 'el-button--danger',
  })
    .then(() => {
      theme.pc_floors.splice(index, 1)
    })
    .catch(() => {})
}

const notifyPcFloorOrderChanged = (position) => {
  ElMessage.success(`已移到第 ${position + 1} 层，点击底部「保存配置」后生效`)
}

const movePcFloor = (index, offset) => {
  if (!Array.isArray(theme.pc_floors)) return
  const targetIndex = index + offset
  if (targetIndex < 0 || targetIndex >= theme.pc_floors.length) return
  const [floor] = theme.pc_floors.splice(index, 1)
  theme.pc_floors.splice(targetIndex, 0, floor)
  notifyPcFloorOrderChanged(targetIndex)
}

const onPcFloorDragEnd = ({ oldIndex, newIndex } = {}) => {
  if (!Number.isInteger(oldIndex) || !Number.isInteger(newIndex) || oldIndex === newIndex) return
  notifyPcFloorOrderChanged(newIndex)
}

const resetPcFloors = async () => {
  try {
    await ElMessageBox.confirm(
      '将恢复为：三层促销货架 + 全部一级分类楼层（均启用），当前自定义楼层会丢失。继续吗？',
      '恢复默认楼层',
      {
        type: 'warning',
        confirmButtonText: '恢复默认',
      }
    )
  } catch {
    return
  }
  if (!level1Categories.value.length) {
    await loadLevel1Categories()
  }
  theme.pc_floors = buildDefaultPcFloors()
  if (!level1Categories.value.length) {
    ElMessage.warning('一级分类加载失败，已恢复为 3 层促销楼层；分类楼层可稍后点「添加分类楼层」补回')
  } else {
    ElMessage.success(
      `已恢复默认楼层（促销 3 + 一级分类 ${level1Categories.value.length}，均启用）`
    )
  }
}

/**
 * 获取商城主题配置
 */
/** 读取失败时禁止保存，避免把页面默认值写回、覆盖线上真实配置 */
let themeLoadFailed = false

const getThemeConfig = async () => {
  loading.value = true
  themeLoadFailed = false
  try {
    const res = await getMallStyle()
    if (res && res.data) {
      const data = res.data
      Object.keys(data).forEach((key) => {
        theme[key] = data[key]
      })
      if (Array.isArray(theme.pc_floors)) {
        theme.pc_floors = theme.pc_floors.map((item) => ({
          _dragId: item?._dragId || createPcFloorDragId(),
          name: item?.name || '楼层',
          subtitle: item?.subtitle || '',
          source: item?.source || (normalizeCategoryId(item?.categoryId) ? 'category' : 'hotGoods'),
          categoryId: normalizeCategoryId(item?.categoryId),
          visible: item?.visible !== false && item?.visible !== 0 && item?.visible !== '0',
          bannerImage: item?.bannerImage || '',
          bannerLink: item?.bannerLink || '',
        }))
      } else {
        theme.pc_floors = []
      }
    } else {
      themeLoadFailed = true
      ElMessage.warning('服务端暂无主题配置，已载入页面默认值；为避免覆盖线上数据，暂不允许保存')
    }
  } catch (error) {
    themeLoadFailed = true
    console.error('获取主题失败', error)
    ElMessage.error('获取主题失败，当前为页面默认值；为避免覆盖线上数据，暂不允许保存')
  } finally {
    // 无论后端是否返回 pc_nav，都统一归一化，保证拖拽 key 与字段结构一致
    theme.pc_nav = normalizePcNav(theme.pc_nav)
    if (!Array.isArray(theme.pc_floors)) theme.pc_floors = []
    loading.value = false
  }
}

/**
 * 保存商城主题配置
 */
/** 两端字段集合：保存时只覆盖当前端字段，避免 PC 保存把移动端主题色整包写回 */
const PC_STYLE_KEYS = [
  'pc_template_code', 'pc_layout', 'pc_container', 'pc_radius', 'pc_currency',
  'pc_show_rank', 'pc_show_recommend', 'pc_nav', 'pc_floors',
]
const MOBILE_STYLE_KEYS = ['current', 'list']

const handleSave = async () => {
  if (saving.value) return
  if (themeLoadFailed) {
    ElMessage.error('主题配置读取失败，为避免覆盖线上数据已阻止保存，请刷新页面重试')
    return
  }
  if (isPc.value) {
    const navError = validatePcNav()
    if (navError) {
      ElMessage.warning(`顶部菜单：${navError}`)
      return
    }
  }
  saving.value = true
  try {
    if (!level1Categories.value.length) {
      await loadLevel1Categories()
    }
    // 空楼层是合法的「关闭全部楼层」配置，不再强制恢复默认
    if (!Array.isArray(theme.pc_floors)) theme.pc_floors = []
    if (isPc.value && theme.pc_floors.length === 0) {
      ElMessage.info('当前 PC 楼层为空，保存后前台不展示楼层；如需恢复可点「恢复默认」')
    }
    theme.pc_floors = theme.pc_floors.map((item) => ({
      ...item,
      _dragId: item?._dragId || createPcFloorDragId(),
      categoryId: normalizeCategoryId(item.categoryId),
      source: item.source || (normalizeCategoryId(item.categoryId) ? 'category' : 'hotGoods'),
      visible: item?.visible !== false && item?.visible !== 0 && item?.visible !== '0',
    }))
    const serializedTheme = JSON.parse(JSON.stringify(theme))
    serializedTheme.pc_floors = serializedTheme.pc_floors.map(({ _dragId, ...item }) => item)
    serializedTheme.pc_nav = normalizePcNav(serializedTheme.pc_nav).map(({ _navId, ...item }) => item)

    // 跨端合并：先取服务端最新配置，再用本端字段覆盖，避免用页面加载时的旧快照覆盖另一端
    let merged = serializedTheme
    try {
      const latest = await getMallStyle()
      const serverData = latest?.data || {}
      merged = { ...serializedTheme, ...serverData }
      const scopeKeys = isPc.value ? PC_STYLE_KEYS : MOBILE_STYLE_KEYS
      scopeKeys.forEach((key) => { merged[key] = serializedTheme[key] })
    } catch (error) {
      console.warn('保存前读取最新主题失败，按当前页面配置提交', error)
    }

    const payload = { data: merged }
    const res = await setMallStyle(payload)
    if (res && (res.code === 1 || res.code === 200)) {
      ElMessage.success('保存成功，请刷新商城首页查看')
      return
    }
    ElMessage.error((res && (res.message || res.msg)) || '保存失败')
  } catch (error) {
    console.error('设置主题失败', error)
    ElMessage.error(error?.message || '保存失败，请确认已登录 Cloud 后台')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([getThemeConfig(), loadLevel1Categories()])
  if (!Array.isArray(theme.pc_floors) || !theme.pc_floors.length) {
    theme.pc_floors = buildDefaultPcFloors()
  } else {
    const added = ensureAllCategoryFloors()
    if (added > 0) {
      ElMessage.info(`已按默认补齐 ${added} 个一级分类楼层（启用状态，保存后生效）`)
    }
  }
})
</script>

<style>
.el-input-group__append,
.el-input-group__prepend {
  padding: 0;
}
</style>

<style scoped>
.tea-brand-preview { margin-top: 16px; padding: 32px; background: #f4f1ea; color: #221e17; border-top: 3px solid #b0805a; }
.tea-brand-preview small { color: #b0805a; letter-spacing: 4px; }
.tea-brand-preview h2 { font-family: serif; font-weight: 400; }
.tea-brand-preview p { color: #504f49; }
.shop-theme {
  padding: 16px;
  padding-bottom: 88px;
  min-height: 240px;
}

.theme-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.theme-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.theme-card :deep(.el-card__body) {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  font-weight: 400;
  color: #909399;
  line-height: 1.5;
}

.subtitle a {
  color: var(--el-color-primary);
  text-decoration: none;
}

.subtitle a:hover {
  text-decoration: underline;
}

.header-actions {
  display: flex;
  flex-shrink: 0;
  gap: 8px;
}

.section-block {
  padding: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.section-block--last {
  border-bottom: none;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.section-desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.section-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.theme-form {
  padding: 0;
}

.theme-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.theme-form-wrap {
  padding: 4px 20px 20px;
}

.field-hint {
  display: inline-block;
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.color-input {
  width: 220px;
  max-width: 100%;
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px 24px;
}

.layout-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  width: 100%;
}

.layout-card {
  position: relative;
  border: 1px solid #ebeef5;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.15s;
}

.layout-card:hover {
  border-color: #c0c4cc;
}

.layout-card.active {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--el-color-primary) 16%, transparent);
}

.layout-card__meta {
  margin-top: 10px;
}

.layout-card strong {
  display: block;
  font-size: 14px;
  color: #303133;
}

.layout-card em {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  font-style: normal;
  color: #909399;
}

.layout-card__check {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 0 8px;
  height: 22px;
  line-height: 22px;
  border-radius: 999px;
  background: var(--el-color-primary);
  color: #fff;
  font-size: 11px;
}

.layout-mock {
  height: 88px;
  border-radius: 8px;
  background: #f5f7fa;
  padding: 6px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.mock-nav {
  height: 10px;
  border-radius: 4px;
  background: #dcdfe6;
}

.mock-body {
  flex: 1;
  display: grid;
  gap: 6px;
}

.layout-mock--cn .mock-body {
  grid-template-columns: 22px 1fr 22px;
}

.layout-mock--hybrid .mock-body {
  grid-template-columns: 22px 1fr;
}

.layout-mock--intl .mock-body {
  grid-template-columns: 1fr;
}

.mock-aside,
.mock-user {
  background: #c0c4cc;
  border-radius: 3px;
}

.mock-hero {
  background: linear-gradient(90deg, #909399, #dcdfe6);
  border-radius: 3px;
}

.mock-floor {
  height: 10px;
  border-radius: 3px;
  background: #e4e7ed;
}

.feature-toggles {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.feature-toggle {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafbfc;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.feature-toggle.on {
  border-color: color-mix(in srgb, var(--el-color-primary) 45%, #ebeef5);
  background: color-mix(in srgb, var(--el-color-primary) 6%, #fff);
}

.feature-toggle strong {
  display: block;
  font-size: 14px;
  color: #303133;
}

.feature-toggle span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.editor-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.editor-row {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fafbfc;
  padding: 12px;
}

.nav-row {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.row-index {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #eef2f7;
  color: #606266;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.row-fields {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  min-width: 0;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.floor-row__top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.floor-row {
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

.floor-drag-handle {
  width: auto;
  min-width: 28px;
  height: 28px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 0 9px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  color: #909399;
  cursor: grab;
  font-size: 16px;
  user-select: none;
  touch-action: none;
  transition: color 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
}

.floor-drag-handle:hover {
  color: var(--el-color-primary);
  border-color: color-mix(in srgb, var(--el-color-primary) 45%, #dcdfe6);
  background: color-mix(in srgb, var(--el-color-primary) 6%, #fff);
}

.floor-drag-handle:focus-visible {
  outline: 2px solid color-mix(in srgb, var(--el-color-primary) 45%, transparent);
  outline-offset: 2px;
}

.floor-drag-handle:active,
.floor-row--dragging .floor-drag-handle {
  cursor: grabbing;
}

.floor-drag-handle span {
  font-size: 12px;
  line-height: 1;
}

.floor-move-button {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
  color: #606266;
  cursor: pointer;
  transition: color 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
}

.floor-move-button:hover:not(:disabled) {
  color: var(--el-color-primary);
  border-color: color-mix(in srgb, var(--el-color-primary) 45%, #dcdfe6);
  background: color-mix(in srgb, var(--el-color-primary) 6%, #fff);
}

.floor-move-button:disabled {
  color: #c0c4cc;
  background: #f5f7fa;
  cursor: not-allowed;
}

.floor-row--chosen {
  border-color: color-mix(in srgb, var(--el-color-primary) 48%, #ebeef5);
  background: #fff;
}

.floor-row--ghost {
  opacity: 0.48;
  border-style: dashed;
  border-color: var(--el-color-primary);
  background: color-mix(in srgb, var(--el-color-primary) 7%, #fff);
}

.floor-row--dragging {
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.16);
}

.floor-del {
  margin-left: auto;
}

.floor-row__fields {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 8px;
}

.floor-select {
  width: 100%;
}

.theme-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(88px, 1fr));
  gap: 12px;
  padding: 16px 20px 20px;
}

.theme-item {
  padding: 14px 10px;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.08);
  border: 2px solid transparent;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  transition: border-color 0.2s, transform 0.15s, box-shadow 0.2s;
}

.theme-item:active {
  transform: scale(0.97);
}

.theme-item.active {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.25);
}

.theme-item__name {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.theme-item__name--red_theme,
.theme-item__name--orange_theme,
.theme-item__name--pink_theme,
.theme-item__name--gold_theme,
.theme-item__name--blue_theme,
.theme-item__name--green_theme,
.theme-item__name--purple_theme,
.theme-item__name--custom_theme {
  background: linear-gradient(
    90deg,
    var(--theme-color) 30%,
    var(--theme-dark-color) 100%
  );
}

.theme-item__title {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
  font-size: 13px;
  text-align: center;
  line-height: 1.3;
  color: #303133;
}

.footer-btns {
  position: fixed;
  bottom: 0;
  right: 0;
  left: var(--sidebar-width, 220px);
  min-height: 64px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
  padding: 10px 16px;
  box-shadow: 0 -4px 16px rgba(15, 23, 42, 0.06);
}

.footer-btns .el-button {
  min-width: 160px;
  min-height: 40px;
  border-radius: 10px;
}

.theme-form :deep(.el-form-item__content:has(.live-preview)),
.theme-form :deep(.el-form-item__content:has(.layout-cards)),
.theme-form :deep(.el-form-item__content:has(.editor-list)),
.theme-form :deep(.el-form-item__content:has(.feature-toggles)) {
  display: block;
  max-width: 100%;
}

@media (max-width: 1200px) {
  .option-grid {
    grid-template-columns: 1fr;
  }

  .layout-cards {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 991px) {
  .shop-theme {
    padding: 12px;
    padding-bottom: calc(80px + env(safe-area-inset-bottom, 0px));
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .section-block {
    padding: 16px;
  }

  .section-head {
    flex-direction: column;
    align-items: stretch;
  }

  .section-actions {
    justify-content: stretch;
  }

  .section-actions .el-button {
    flex: 1;
  }

  .theme-form :deep(.el-form-item) {
    display: block;
    margin-bottom: 16px;
  }

  .theme-form :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left;
    justify-content: flex-start;
    float: none;
    display: block;
    padding-bottom: 8px;
    line-height: 1.4;
    height: auto !important;
  }

  .theme-form :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }

  .layout-cards {
    grid-template-columns: 1fr;
  }

  .feature-toggles {
    grid-template-columns: 1fr;
  }

  .nav-row {
    grid-template-columns: 1fr;
  }

  .row-fields {
    grid-template-columns: 1fr;
  }

  .row-actions {
    justify-content: space-between;
    width: 100%;
  }

  .floor-row__fields {
    grid-template-columns: 1fr;
  }

  .color-input {
    width: 100%;
  }

  .theme-list {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
    padding: 12px 16px 16px;
  }

  .theme-item {
    padding: 12px 8px;
  }

  .footer-btns {
    left: 0;
    padding: 10px 16px calc(10px + env(safe-area-inset-bottom, 0px));
  }

  .footer-btns .el-button {
    flex: 1;
    max-width: 360px;
    min-height: 44px;
  }
}

@media (max-width: 480px) {
  .theme-list {
    gap: 8px;
  }

  .theme-item__title {
    font-size: 12px;
  }

  .layout-card__check {
    top: 8px;
    right: 8px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .floor-row,
  .floor-drag-handle {
    transition: none;
  }
}
</style>
