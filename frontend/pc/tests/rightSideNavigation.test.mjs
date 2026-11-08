import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const navigationModule = await import('../app/utils/rightSideNavigation.js').catch(() => null)

test('PC 右侧工具栏提供确认的六个真实业务入口', () => {
    assert.ok(navigationModule, '缺少右侧工具栏业务配置')

    assert.deepEqual(
        navigationModule.RIGHT_SIDE_NAV_ITEMS.map(({ key, label, target }) => ({ key, label, target })),
        [
            { key: 'cart', label: '购物车', target: '/cart/shopping' },
            { key: 'coupon', label: '优惠券', target: '/property/voucher' },
            { key: 'order', label: '订单', target: '/order/list' },
            { key: 'favorite', label: '收藏', target: '/attention/collect' },
            { key: 'help', label: '帮助', target: '/article' },
            { key: 'customer-service', label: '客服', target: '' }
        ]
    )
})

test('数量徽标展示真实零值并在大数量时收敛', () => {
    assert.ok(navigationModule, '缺少右侧工具栏数量格式化逻辑')
    assert.equal(navigationModule.formatRailBadge(0), '0')
    assert.equal(navigationModule.formatRailBadge(8), '8')
    assert.equal(navigationModule.formatRailBadge(128), '99+')
    assert.equal(navigationModule.readVoucherCount({ code: 1, data: { totalCount: 6 } }), 6)
    assert.equal(navigationModule.readVoucherCount({ code: 0, data: { totalCount: '4' } }), 4)
    assert.equal(navigationModule.readVoucherCount({ code: 500, data: { totalCount: 9 } }), 0)
})

test('默认 PC 布局挂载工具栏且不重复挂载旧回顶按钮', async () => {
    const layout = await readFile(new URL('../app/layouts/default.vue', import.meta.url), 'utf8')
    assert.match(layout, /<RightSideNavigation\s*\/>/)
    assert.doesNotMatch(layout, /<Backtop\s*\/>/)
})
