<template>
  <div class="secondary-menu-wrapper" :class="{ 'is-mobile': isMobile }">
    <!-- 移动端：顶部切换条 + 横向三级 Tab + 底部宫格面板 -->
    <template v-if="isMobile">
      <div class="mobile-nav-stack">
        <button type="button" class="mobile-module-switcher" @click="mobileDrawerOpen = true">
          <el-icon class="switcher-icon"><Grid /></el-icon>
          <span class="switcher-text">
            <span class="switcher-module">{{ menuTitle || '模块菜单' }}</span>
            <span v-if="activeSecondaryLabel" class="switcher-current">{{ activeSecondaryLabel }}</span>
          </span>
          <el-icon class="switcher-chevron"><ArrowDown /></el-icon>
        </button>
        <MobileTertiaryNav
          :menu-items="sanitizedItems"
          :active-menu="activeMenu"
          :module-title="menuTitle"
        />
      </div>
      <el-drawer
        v-model="mobileDrawerOpen"
        direction="btt"
        size="auto"
        :title="menuTitle || '切换功能'"
        append-to-body
        class="mobile-secondary-sheet"
        :show-close="true"
      >
        <div class="mobile-sheet-grid">
          <button
            v-for="tile in mobileTiles"
            :key="tile.id"
            type="button"
            class="mobile-sheet-tile"
            :class="{ 'is-active': isTileActive(tile.path) }"
            @click="handleMobileTileSelect(tile.path)"
          >
            <span class="tile-icon-wrap">
              <el-icon v-if="getIconComponent(tile.icon)">
                <component :is="getIconComponent(tile.icon)" />
              </el-icon>
            </span>
            <span class="tile-name">{{ tile.name }}</span>
            <el-icon v-if="tile.hasChildren" class="tile-more"><ArrowRight /></el-icon>
          </button>
        </div>
      </el-drawer>
    </template>

    <!-- 桌面端：侧栏 -->
    <template v-else>
      <div class="secondary-menu-header">
        <el-button @click="toggleCollapse" class="secondary-collapse-btn" type="primary" link>
          <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
        </el-button>
        <span v-if="!isCollapse" class="secondary-menu-title">{{ menuTitle }}</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        class="secondary-menu"
        background-color="#fafbfc"
        text-color="#333"
        active-text-color="#409EFF"
        :collapse="isCollapse"
        :collapse-transition="false"
        @select="handleMenuSelect"
      >
        <SecondaryMenuItem
          v-for="item in sanitizedItems"
          :key="item.id"
          :item="item"
          :get-icon="getIconComponent"
        />
      </el-menu>
    </template>
  </div>
</template>

<script setup>

// 使用 Element Plus 国际化系统
import { useI18n } from '@/i18n-element'
const { t } = useI18n()
import { computed, ref } from 'vue'
import SecondaryMenuItem from '@/components/SecondaryMenuItem.vue'
import MobileTertiaryNav from '@/components/MobileTertiaryNav.vue'
import { buildMobileSecondaryTiles, collectOpenMenuIds, isMenuPathPrefix, sanitizeSidebarMenuTree } from '@/utils/menuNavigate'
import { 
  Expand, 
  Fold,
  DataBoard,
  User,
  Goods,
  Document,
  Setting,
  List,
  Promotion,
  Box,
  Wallet,
  Bell,
  Lock,
  Operation,
  Monitor,
  TrendCharts,
  Star,
  Coin,
  Money,
  CreditCard,
  Calendar,
  Clock,
  Timer,
  Message,
  ChatDotRound,
  UserFilled,
  OfficeBuilding,
  Location,
  MapLocation,
  Position,
  Phone,
  Iphone,
  Key,
  // Shield 不存在，用 Lock 替代
  Warning,
  InfoFilled,
  QuestionFilled,
  Search,
  Filter,
  Refresh,
  RefreshLeft,
  Download,
  Upload,
  Plus,
  Edit,
  Delete,
  Close,
  Check,
  CircleCheck,
  View,
  ZoomIn,
  Picture,
  VideoCamera,
  VideoPlay,
  VideoPause,
  MagicStick,
  // Code/Wrench 不存在，在 iconMap 里用 Cpu/Tools 替代
  Cpu,
  Tools,
  Switch,
  Share,
  Link,
  Connection,
  Grid,
  Menu,
  ArrowDown,
  Folder,
  DocumentAdd,
  DocumentChecked,
  CopyDocument,
  Notebook,
  Reading,
  Stamp,
  Printer,
  Flag,
  Trophy,
  // Target/Gift/Truck 不存在，在 iconMap 里替代
  Orange,
  Apple,
  Food,
  Bicycle,
  Van,
  Shop,
  ShoppingCart,
  PriceTag,
  Ticket,
  ArrowRight,
  ArrowUp,
  Top,
  // Exchange 不存在，用 Sort 替代——在 iconMap 里映射
  DataLine,
  DataAnalysis,
  Collection,
  Brush,
  Service,
  Basketball
} from '@element-plus/icons-vue'
// GEO 等模块图标名不在白名单时，回退到 Element Plus 全量图标库解析（与 frontend/geo 一致）
import * as ElementPlusIcons from '@element-plus/icons-vue'

// 定义组件属性
const props = defineProps({
  menuItems: {
    type: Array,
    default: () => []
  },
  menuTitle: {
    type: String,
    default: ''
  },
  activeMenu: {
    type: String,
    default: ''
  },
  isCollapse: {
    type: Boolean,
    default: false
  },
  isMobile: {
    type: Boolean,
    default: false
  }
})

const mobileDrawerOpen = ref(false)

// 定义事件
const emit = defineEmits(['update:isCollapse', 'select'])

// 图标映射（包含所有数据库中配置的图标名称）
const iconMap = {
  // 基础图标
  DataBoard, User, Goods, Document, Setting, List, Promotion, Box, Wallet,
  Bell, Lock, Operation, Expand, Fold, DataBoard,
  // 财务类
  Monitor, TrendCharts, Star, Coin, Money, CreditCard,
  // 时间类
  Calendar, Clock, Timer,
  // 通信类
  Message, ChatDotRound, Phone, Iphone,
  // 用户权限
  UserFilled, Key,
  // 地址位置
  OfficeBuilding, Location, MapLocation, Position,
  // 提示状态
  Warning, InfoFilled, QuestionFilled, CircleCheck, Check, Close,
  // 操作类
  Search, Filter, Refresh, RefreshLeft, Download, Upload,
  Plus, Edit, Delete, View, ZoomIn, Share, Link, Switch,
  // 媒体类
  Picture, VideoCamera, VideoPlay, VideoPause,
  // 技术类
  MagicStick, Cpu, Tools, Connection,
  // 文件文档
  Grid, Menu, Folder, DocumentAdd, DocumentChecked, CopyDocument,
  Notebook, Reading, Stamp, Printer,
  // 商业类
  Shop, ShoppingCart, PriceTag, Ticket,
  // 物流运输
  Bicycle, Van,
  // 标志目标
  Flag, Trophy,
  // 分析统计
  DataLine, DataAnalysis, Collection,
  // 其他实际存在的
  Brush, Service, Basketball, Orange, Apple, Food,
  // Gift/Truck/Target/Code/Wrench 不存在，在 iconMap 里用替代映射
  // ArrowRight etc
  ArrowRight, ArrowUp, Top,
  // 不存在的图标用相似的替代（常属映射）
  Finance: Wallet,
  Robot: MagicStick,
  Workflow: Connection,
  Translation: Share,
  Template: Document,
  Record: Notebook,
  Version: Document,
  Attribute: Setting,
  Brand: Flag,
  Sensitive: Lock,
  Shield: Lock,
  Addons: Grid,
  Dashboard: DataBoard,
  Date: Calendar,
  Exchange: ArrowRight,
  Gift: Star,
  Truck: Van,
  Target: Trophy,
  Code: Cpu,
  Wrench: Tools,
  Calculator: Coin,
  // 小写别名写法兼容（数据库里可能用小写开头或带连字符）
  'add': Plus,
  'data-analysis': DataAnalysis,
  'copy-document': CopyDocument,
  'takeaway-box': Box,
  'coordinate': Position,
  'detail': Document,
  'news': Document,
  'school': OfficeBuilding,
  'domesticSubstitution': ArrowRight,
  'Ai': MagicStick,
  'Bi': DataAnalysis,
  'school': OfficeBuilding
}

// 获取图标组件：白名单映射优先，其次全量 Element Plus 图标，最后兜底 Document
const getIconComponent = (iconName) => {
  if (!iconName || typeof iconName !== 'string') {
    return Document // 默认图标
  }
  const icon = iconMap[iconName] || ElementPlusIcons[iconName]
  if (!icon) {
    return Document // 默认图标
  }
  return icon
}

/** 桌面端：当前路由下应展开的目录 */
const defaultOpeneds = computed(() => collectOpenMenuIds(props.menuItems, props.activeMenu))

const sanitizedItems = computed(() => sanitizeSidebarMenuTree(props.menuItems || []))

const mobileTiles = computed(() => buildMobileSecondaryTiles(sanitizedItems.value))

/** 当前激活的二级菜单名称 */
const activeSecondaryLabel = computed(() => {
  const target = props.activeMenu
  if (!target) return ''
  const hit = mobileTiles.value.find((t) => isMenuPathPrefix(target, t.path))
  return hit?.name || ''
})

/**
 * 宫格项是否为当前路由
 * @param {string} path 菜单路径
 * @returns {boolean}
 */
const isTileActive = (path) => {
  if (!props.activeMenu || !path) return false
  return isMenuPathPrefix(props.activeMenu, path) || isMenuPathPrefix(path, props.activeMenu)
}

/**
 * 底部宫格选择后跳转并关闭面板
 * @param {string} path 目标路径
 */
const handleMobileTileSelect = (path) => {
  if (!path) return
  mobileDrawerOpen.value = false
  emit('select', path)
}

// 切换折叠状态
const toggleCollapse = (event) => {
  // 防止事件冒泡
  if (event) {
    event.preventDefault()
    event.stopPropagation()
  }
  emit('update:isCollapse', !props.isCollapse)
}

// 处理菜单选择
const handleMenuSelect = (index, indexPath) => {
  // 验证参数
  if (!index) {
    return
  }

  emit('select', index)
}
</script>

<style lang="scss" scoped>
.secondary-menu-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .secondary-menu-header {
    height: 50px;
    display: flex;
    align-items: center;
    padding: 0 10px;
    border-bottom: 1px solid #e6e6e6;
    background-color: #fff;
  }

  .secondary-menu-title {
    margin-left: 10px;
    font-size: 16px;
    font-weight: 500;
    color: #333;
    white-space: nowrap;
  }

  .secondary-collapse-btn {
    font-size: 18px;
  }

  .secondary-menu {
    border: none;
    flex: 1;
    overflow-y: auto;
    --el-menu-base-level-padding: 14px;
    --el-menu-level-padding: 14px;
    --el-menu-icon-width: 20px;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      height: 46px;
      line-height: 46px;
      padding-right: 36px !important;
    }

    :deep(.el-menu-item .el-icon),
    :deep(.el-sub-menu__title .el-icon) {
      margin-right: 10px;
      font-size: 18px;
      flex-shrink: 0;
    }

    :deep(.el-sub-menu__icon-arrow) {
      right: 14px;
    }

    :deep(.el-menu-item span),
    :deep(.el-sub-menu__title span) {
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  :deep(.el-menu--collapse) {
    .el-menu-item span,
    .el-sub-menu__title span {
      display: none;
    }

    .el-menu-item .el-icon,
    .el-sub-menu__title .el-icon {
      margin-right: 0;
    }
  }

  &.is-mobile {
    height: auto;
  }

  .mobile-nav-stack {
    position: sticky;
    top: 0;
    z-index: 8;
    background: #fff;
    box-shadow: 0 1px 0 rgba(16, 24, 40, 0.06);
  }

  .mobile-module-switcher {
    display: flex;
    align-items: center;
    gap: 10px;
    width: 100%;
    margin: 0;
    padding: 10px 12px;
    border: none;
    border-bottom: 1px solid #ebeef5;
    background: #fff;
    text-align: left;
    cursor: pointer;
    font-family: inherit;
  }

  .mobile-module-switcher:active {
    background: #f5f7fa;
  }

  .switcher-icon {
    flex-shrink: 0;
    font-size: 18px;
    color: #409eff;
  }

  .switcher-text {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .switcher-module {
    font-size: 12px;
    color: #909399;
    line-height: 1.2;
  }

  .switcher-current {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.3;
  }

  .switcher-chevron {
    flex-shrink: 0;
    color: #c0c4cc;
    font-size: 14px;
  }
}

:global(.mobile-secondary-sheet .el-drawer__header) {
  margin-bottom: 0;
  padding-bottom: 8px;
}

:global(.mobile-secondary-sheet .el-drawer__body) {
  padding: 8px 12px calc(12px + env(safe-area-inset-bottom, 0px));
}

.mobile-sheet-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.mobile-sheet-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 88px;
  padding: 12px 8px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #f8fafc;
  color: #303133;
  cursor: pointer;
  position: relative;
  font-family: inherit;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
}

.mobile-sheet-tile.is-active {
  border-color: #b3d8ff;
  background: #ecf5ff;
  color: #409eff;
}

.mobile-sheet-tile:active {
  transform: scale(0.98);
}

.tile-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fff;
  font-size: 20px;
  box-shadow: 0 1px 3px rgba(16, 24, 40, 0.08);
}

.mobile-sheet-tile.is-active .tile-icon-wrap {
  color: #409eff;
}

.tile-name {
  font-size: 12px;
  line-height: 1.3;
  text-align: center;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tile-more {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
