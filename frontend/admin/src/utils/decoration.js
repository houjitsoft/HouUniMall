/**
 * 商城装修模块工具函数
 * 提供唯一ID生成、组件菜单数据、动态资源路径获取等功能
 */

import icon_search from '@/assets/mall/decoration/icon_search.svg'
import icon_notice from '@/assets/mall/decoration/icon_notice.svg'
import icon_banner from '@/assets/mall/decoration/icon_banner.svg'
import icon_nav from '@/assets/mall/decoration/icon_nav.svg'
import icon_choice from '@/assets/mall/decoration/icon_choice.svg'
import icon_cube from '@/assets/mall/decoration/icon_cube.svg'
import icon_goods from '@/assets/mall/decoration/icon_goods.svg'
import icon_shop from '@/assets/mall/decoration/icon_shop.svg'
import icon_seckill from '@/assets/mall/decoration/icon_seckill.svg'
import icon_team from '@/assets/mall/decoration/icon_team.svg'
import icon_subsidy from '@/assets/mall/decoration/icon_subsidy.svg'
import icon_title from '@/assets/mall/decoration/icon_title.svg'
import icon_blank from '@/assets/mall/decoration/icon_blank.svg'

/**
 * 生成唯一ID
 * 基于时间戳和随机数生成不重复的ID字符串
 * @returns {string} 唯一ID字符串
 */
export function getNonDuplicateID() {
  const timestamp = Date.now().toString(36)
  const randomStr = Math.random().toString(36).substring(2, 8)
  return `${timestamp}_${randomStr}`
}

/**
 * 页面编辑器组件菜单数据
 * 包含基础组件、营销组件、工具组件三大分类
 */
export const decorateMenu = [
  {
    title: '基础组件',
    name: 'basics',
    children: [
      { title: '公告', name: 'notice', icon: icon_notice },
      { title: '搜索框', name: 'search', icon: icon_search },
      { title: '轮播图', name: 'banner', icon: icon_banner },
      { title: '导航组', name: 'nav', icon: icon_nav },
      { title: '选项卡', name: 'choice', icon: icon_choice },
      { title: '图片魔方', name: 'cube', icon: icon_cube },
      { title: '商品列表', name: 'goodslists', icon: icon_goods },
      { title: '店铺列表', name: 'shoplists', icon: icon_shop }
    ]
  },
  {
    title: '营销组件',
    name: 'marketing',
    children: [
      { title: '秒杀', name: 'seckill', icon: icon_seckill },
      { title: '拼团', name: 'team', icon: icon_team },
      { title: '国补', name: 'subsidy', icon: icon_subsidy }
    ]
  },
  {
    title: '工具组件',
    name: 'tools',
    children: [
      { title: '标题栏', name: 'title', icon: icon_title },
      { title: '辅助空白', name: 'blank', icon: icon_blank }
    ]
  }
]

/**
 * 数据配置页面组件菜单
 * 用于数据配置页面的组件选择
 */
export const decorateDataMenu = [
  {
    title: '基础组件',
    name: 'basics',
    children: [
      { title: '轮播图', name: 'banner', icon: icon_banner },
      { title: '图片魔方', name: 'cube', icon: icon_cube },
      { title: '商品展示', name: 'showgoods', icon: icon_cube },
      { title: '推荐商品', name: 'recommended', icon: icon_cube }
    ]
  },
  {
    title: '工具组件',
    name: 'tools',
    children: [
      { title: '标题栏', name: 'title', icon: icon_title },
      { title: '辅助空白', name: 'blank', icon: icon_blank }
    ]
  }
]

/**
 * 动态获取资源文件路径
 * 用于在运行时根据URL动态加载图片等静态资源
 * @param {string} url - 资源相对路径（相对于assets目录）
 * @returns {string} 资源的完整URL路径
 */
export function getAssetsFile(url) {
  return new URL(`../assets/mall/decoration/${url}`, import.meta.url).href
}
