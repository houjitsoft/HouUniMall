import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import {
  DataBoard,
  User,
  Document,
  Setting,
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
  Message,
  ChatDotRound,
  UserFilled,
  OfficeBuilding,
  Location,
  Warning,
  Search,
  Refresh,
  Download,
  Upload,
  Plus,
  Edit,
  Delete,
  View,
  Picture,
  VideoCamera,
  VideoPlay,
  MagicStick,
  Cpu,
  Tools,
  Connection,
  Grid,
  Folder,
  CopyDocument,
  Notebook,
  Reading,
  Stamp,
  Flag,
  Trophy,
  Van,
  Shop,
  ShoppingCart,
  DataAnalysis,
  Collection,
  Service,
  Box,
  ArrowRight,
  Position
} from '@element-plus/icons-vue'

/** 数据库 icon 字段别名 → Element Plus 图标组件 */
const ICON_ALIAS_MAP = {
  DataBoard,
  User,
  Document,
  Setting,
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
  Message,
  ChatDotRound,
  UserFilled,
  OfficeBuilding,
  Location,
  Warning,
  Search,
  Refresh,
  Download,
  Upload,
  Plus,
  Edit,
  Delete,
  View,
  Picture,
  VideoCamera,
  VideoPlay,
  MagicStick,
  Cpu,
  Tools,
  Connection,
  Grid,
  Folder,
  CopyDocument,
  Notebook,
  Reading,
  Stamp,
  Flag,
  Trophy,
  Van,
  Shop,
  ShoppingCart,
  DataAnalysis,
  Collection,
  Service,
  Box,
  ArrowRight,
  Position,
  Finance: Money,
  Robot: MagicStick,
  Workflow: Connection,
  Dashboard: DataBoard,
  Date: Calendar,
  Gift: Star,
  Truck: Van,
  Target: Trophy,
  Code: Cpu,
  Wrench: Tools,
  Calculator: Coin,
  coordinate: Position,
  news: ChatDotRound,
  school: OfficeBuilding,
  reading: Reading,
  'data-analysis': DataAnalysis,
  'copy-document': CopyDocument,
  'takeaway-box': Box
}

/**
 * 将权限菜单 icon 字段解析为 Element Plus 图标组件
 * @param {string} iconName 菜单 icon 字段
 * @returns {import('vue').Component} 图标组件
 */
export function resolveMenuIcon(iconName) {
  if (!iconName || typeof iconName !== 'string') {
    return Document
  }
  if (ICON_ALIAS_MAP[iconName]) {
    return ICON_ALIAS_MAP[iconName]
  }
  if (ElementPlusIconsVue[iconName]) {
    return ElementPlusIconsVue[iconName]
  }
  return Document
}

/** OA 工作台应用名称 → 图标名（icon 字段缺失时的兜底） */
const WORKBENCH_NAME_ICON = {
  投屏: 'Monitor',
  审批: 'Stamp',
  汇报: 'Document',
  客户联系: 'ChatDotRound',
  企业名片: 'CreditCard',
  服务商管理: 'OfficeBuilding',
  打卡: 'Clock',
  会议室: 'OfficeBuilding',
  公告: 'Bell',
  工资条: 'Money',
  人事助手: 'User',
  行业资讯: 'Reading',
  学习园地: 'Reading',
  会议: 'VideoCamera',
  直播: 'VideoPlay',
  '智能机器人 AI+': 'MagicStick',
  爱番番: 'DataAnalysis',
  客户群: 'UserFilled'
}

/** OA 工作台应用名称 → 简短说明 */
export const WORKBENCH_APP_DESCRIPTIONS = {
  投屏: '数据大屏与经营看板',
  审批: '请假、报销、出差等流程审批',
  汇报: '日报、周报与工作汇报',
  客户联系: '客户沟通与跟进记录',
  企业名片: '对外展示企业信息与名片',
  服务商管理: '服务商档案与协作管理',
  打卡: '上下班考勤打卡',
  会议室: '会议室预约与使用情况',
  公告: '企业公告与通知发布',
  工资条: '员工薪资条查询',
  人事助手: '入转调离与人事自助',
  行业资讯: '行业动态与资讯阅读',
  学习园地: '内部培训与学习资料',
  会议: '会议安排与会议纪要',
  直播: '企业直播与活动',
  '智能机器人 AI+': '智能问答与办公助手',
  爱番番: '营销线索与客户运营',
  客户群: '客户群运营与管理'
}

/**
 * 解析 OA 工作台应用图标
 * @param {string} name 应用名称
 * @param {string} iconField 菜单 icon 字段
 * @returns {import('vue').Component} 图标组件
 */
export function resolveWorkbenchIcon(name, iconField) {
  const byField = resolveMenuIcon(iconField)
  if (iconField && byField !== Document) {
    return byField
  }
  const fallbackName = WORKBENCH_NAME_ICON[name]
  if (fallbackName) {
    return resolveMenuIcon(fallbackName)
  }
  return Document
}

/**
 * 获取 OA 工作台应用说明文案
 * @param {string} name 应用名称
 * @param {string} [description] 后端描述
 * @returns {string} 展示用说明
 */
export function getWorkbenchAppDescription(name, description) {
  if (description && String(description).trim()) {
    return description
  }
  return WORKBENCH_APP_DESCRIPTIONS[name] || '点击进入应用'
}
