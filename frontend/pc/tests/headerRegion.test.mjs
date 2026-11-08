import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const projectDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const read = (relativePath) => readFileSync(path.join(projectDir, relativePath), 'utf8')

test('商城顶部区域入口接入统一访客区域状态', () => {
    const header = read('app/components/Header.vue')
    const picker = read('app/components/header/VisitorRegionPicker.vue')

    assert.match(header, /<VisitorRegionPicker\s*\/>/)
    assert.match(header, /initializeVisitorRegion\(isLogin\.value\)/)
    assert.match(picker, /用于商品库存、配送范围和预计送达时间/)
    assert.match(picker, /source:\s*'manual'/)
    assert.match(picker, /locateByIp\(true\)/)
})

test('区域优先级和商品详情同步使用同一状态源', () => {
    const composable = read('app/composables/useVisitorRegion.ts')
    const product = read('app/components/product/ProductInformation.vue')

    const manualIndex = composable.indexOf("cached?.source === 'manual'")
    const addressIndex = composable.indexOf('resolveAddressLocation()', manualIndex)
    const cachedIndex = composable.indexOf('if (cached)', addressIndex)
    const ipIndex = composable.indexOf('locateByIp(false)', cachedIndex)

    assert.ok(manualIndex >= 0, '应优先保留用户手动选择')
    assert.ok(addressIndex > manualIndex, '登录默认地址应排在手动选择之后')
    assert.ok(cachedIndex > addressIndex, '缓存的 IP/兜底区域应排在登录地址之后')
    assert.ok(ipIndex > cachedIndex, '无可用状态时才发起 IP 定位')
    assert.match(product, /watch\(visitorRegion/)
    assert.match(product, /persistCurrentRegion/)
})
