import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

import { getLoginBrand } from './loginBrands.js'

const EXPECTED_BRANDS = {
    wechat: '微信',
    wecom: '企业微信',
    dingtalk: '钉钉',
    qq: 'QQ',
    feishu: '飞书'
}

test('为五个快捷登录渠道提供真实品牌图标', () => {
    for (const [code, label] of Object.entries(EXPECTED_BRANDS)) {
        const brand = getLoginBrand(code)

        assert.equal(brand.label, label)
        assert.match(brand.viewBox, /^0 0 \d+ \d+$/)
        assert.ok(Array.isArray(brand.paths) && brand.paths.length > 0)
        assert.ok(brand.paths.every((path) => path.d && path.fill))
        assert.equal(brand.fallback, false)
    }
})

test('未知渠道回退到通用登录图标而不是名称首字', () => {
    const brand = getLoginBrand('custom-sso', '公司 SSO')

    assert.equal(brand.label, '公司 SSO')
    assert.equal(brand.fallback, true)
    assert.ok(brand.paths.every((path) => path.d && path.fill))
})

test('快捷登录按钮渲染品牌 SVG 和平台名称', async () => {
    const component = await readFile(new URL('../components/login/ThirdPartyLogin.vue', import.meta.url), 'utf8')

    assert.match(component, /<svg[\s\S]*class="tp-brand-icon"/)
    assert.match(component, /class="tp-label"[\s\S]*item\.name/)
    assert.match(component, /getLoginBrand/)
    assert.doesNotMatch(component, /shortName\(/)
})
