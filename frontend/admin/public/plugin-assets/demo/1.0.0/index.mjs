/**
 * 内置演示插件前端产物（T10 动态路由 / 插件前端加载器验收用）。
 *
 * 契约：《开源插件化架构》8.2 —— ESM 默认导出 { menus, routes, setup, teardown }。
 * 说明：
 * - 宿主按契约在运行时 `import()` 本文件，因此不得使用裸模块说明符（如 `import { h } from 'vue'`），
 *   Vue 运行时由宿主通过 `ctx.host.vue` 注入；
 * - 本文件随 cloud 静态目录发布（源码 frontend/admin/public → 产物 public/cloud），
 *   线上访问地址：https://cloud.houjit.com/plugin-assets/demo/1.0.0/index.mjs
 * - 版本号在路径里，天然解决浏览器缓存失效问题。
 */

/** 宿主注入的上下文（setup 时赋值） */
let ctx = null

/** 演示页面：验证「动态路由 + 宿主 Vue 运行时注入」可用 */
const CouponIndex = {
  name: 'DemoCouponIndex',
  setup() {
    const { h, ref } = ctx.host.vue
    const clicks = ref(0)
    return () =>
      h(
        'div',
        { style: 'padding:24px;max-width:720px;' },
        [
          h('h2', { style: 'margin:0 0 12px;' }, '优惠券（内置演示插件）'),
          h(
            'p',
            { style: 'color:#606266;line-height:1.7;' },
            '本页面不是宿主源码里的页面：它由插件前端产物在运行时注册（动态路由 + 动态菜单 + 菜单权限联动）。'
          ),
          h(
            'ul',
            { style: 'color:#606266;line-height:1.8;' },
            [
              h('li', null, `插件编码：${ctx.pluginCode}`),
              h('li', null, `插件版本：${ctx.version}`),
              h('li', null, '菜单路径：/plugin/demo/coupon')
            ]
          ),
          h(
            'button',
            {
              type: 'button',
              onClick: () => {
                clicks.value += 1
              },
              style:
                'margin-top:12px;padding:8px 16px;border:1px solid #409eff;background:#409eff;color:#fff;border-radius:4px;cursor:pointer;'
            },
            `宿主注入的 Vue 运行时正常：点击 ${clicks.value} 次`
          )
        ]
      )
  }
}

export default {
  menus: [{ path: '/plugin/demo/coupon', title: '优惠券示例', icon: 'Ticket', sort: 910 }],
  routes: [{ path: '/plugin/demo/coupon', component: CouponIndex, meta: { title: '优惠券示例' } }],
  setup(hostContext) {
    ctx = hostContext
    console.log('[demo-coupon] 插件已就绪', ctx?.pluginCode, ctx?.version)
  },
  teardown() {
    console.log('[demo-coupon] 插件已卸载')
    ctx = null
  }
}
