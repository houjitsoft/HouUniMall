/**
 * 菜单配置工具函数
 */

// A级菜单路径映射
// 已清空：原来的子路径现在都有各自的独立页面和路由，不再需要被视为 A 级菜单作重定向或过滤
const A_LEVEL_MENU_MAP = {}

/**
 * 判断是否为A级菜单
 * @param {string} path - 路由路径
 * @returns {boolean} 是否为A级菜单
 */
export function isALevelMenu(path) {
  return Object.keys(A_LEVEL_MENU_MAP).includes(path)
}

/**
 * 获取A级菜单的父级路径
 * @param {string} path - A级菜单路径
 * @returns {string} 父级路径
 */
export function getParentPath(path) {
  return A_LEVEL_MENU_MAP[path] || path
}

/**
 * 获取菜单层级
 * @param {string} path - 路由路径
 * @returns {number} 菜单层级
 */
export function getMenuLevel(path) {
  // 移除前导斜杠并按斜杠分割
  const segments = path.replace(/^\//, '').split('/')
  return segments.length
}

/**
 * 获取菜单配置
 * @returns {Array} 菜单配置数组
 */
export function getMenuConfig() {
  return [
    {
      path: '/plugin/oa/apps',
      name: 'OA办公',
      meta: { title: 'OA办公', icon: 'OfficeBuilding' },
      children: [
        {
          path: '/plugin/oa/attendance',
          name: 'Attendance',
          meta: { title: '考勤管理', icon: 'Clock' },
          children: [
            { path: '/plugin/oa/attendance', meta: { title: '考勤管理' } },
            { path: '/plugin/oa/attendance/lists', meta: { title: '考勤列表' } },
            { path: '/plugin/oa/attendance/statistics', meta: { title: '考勤统计' } },
            { path: '/plugin/oa/attendance/record', meta: { title: '考勤记录' } },
            { path: '/plugin/oa/attendance/setting', meta: { title: '考勤设置' } },
            { path: '/plugin/oa/attendance/travel', meta: { title: '出差管理' } },
            { path: '/plugin/oa/attendance/overtime', meta: { title: '加班管理' } },
            { path: '/plugin/oa/attendance/leave', meta: { title: '请假管理' } },
            { path: '/plugin/oa/attendance/report-deduction', meta: { title: '日报扣款' } }
          ]
        },
        {
          path: '/plugin/oa/salary',
          name: 'Salary',
          meta: { title: '薪资管理', icon: 'Money' },
          children: [
            { path: '/plugin/oa/salary/setting', meta: { title: '薪资列表' } },
            { path: '/plugin/oa/salary/calculate', meta: { title: '薪资计算' } },
            { path: '/plugin/oa/salary/payment', meta: { title: '薪资发放' } },
            { path: '/plugin/oa/salary/rules', meta: { title: '薪资规则' } },
            { path: '/plugin/oa/salary/structure', meta: { title: '根据规则设计金额' } },
            { path: '/plugin/oa/salary/statistics', meta: { title: '薪资统计' } },
            { path: '/plugin/oa/salary/payroll', meta: { title: '月度工资条' } },
            { path: '/plugin/oa/salary/payslip', meta: { title: '工资条管理' } }
          ]
        },
        {
          path: '/plugin/oa/contact',
          name: 'Contact',
          meta: { title: '联系人管理', icon: 'User' },
          children: [
            { path: '/plugin/oa/contact', meta: { title: '联系人管理' } },
            { path: '/plugin/oa/contact/setting', meta: { title: '联系人设置' } },
            { path: '/plugin/oa/contact/list', meta: { title: '联系人列表' } },
            { path: '/plugin/oa/contact/group', meta: { title: '联系人分组' } }
          ]
        }
      ]
    }
  ]
}

export default {
  isALevelMenu,
  getParentPath,
  getMenuLevel,
  getMenuConfig
}
