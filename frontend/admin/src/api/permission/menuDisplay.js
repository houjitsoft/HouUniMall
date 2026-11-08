/**
 * 菜单显示位置 / 插件分类相关接口。
 *
 * 位置由 hou_permission_menu.display_pos 决定，在「系统设置 → 权限 → 菜单管理」里逐条配置；
 * 插件分类来自插件中心分类表 hou_addon_cate。
 */
import request from '@/utils/request'

// 兼容已有调用名称，分类统一由 base 插件中心提供。
export { getPluginCategories as getAddonCateList } from '@/api/plugin-center/pluginCenter'

/**
 * 更新菜单显示位置
 * @param {number|string} id 菜单ID
 * @param {'left'|'plugin'|'hidden'} displayPos 显示位置
 * @param {number} [cateId] 插件分类ID（displayPos=plugin 时生效）
 */
export async function updateMenuDisplay({ id, displayPos, cateId }) {
  return request.post('/web/permission/menu/display/update', {
    id,
    displayPos,
    cateId: Number(cateId) || 0
  })
}
