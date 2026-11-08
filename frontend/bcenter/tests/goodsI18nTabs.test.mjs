import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import { shouldReloadGoodsI18n } from '../src/views/goods/goods/component/goodsI18nId.mjs'

const componentUrl = new URL(
    '../src/views/goods/goods/component/goods-i18n.vue',
    import.meta.url
)
const editUrl = new URL('../src/views/goods/goods/edit.vue', import.meta.url)
const apiUrl = new URL('../src/api/goods/i18n.ts', import.meta.url)

test('new goods renders language tabs instead of an empty placeholder', async () => {
    const source = await readFile(componentUrl, 'utf8')

    assert.doesNotMatch(source, /<el-empty\s+v-if="!goodsId"/)
    assert.doesNotMatch(source, /<el-select[^>]*v-model="selectedLangs"/)
    assert.doesNotMatch(source, />保存译文<\/el-button>/)
    assert.match(source, /<el-tabs\s+v-model="activeLang"/)
    for (const [code, label] of [
        ['en', 'English'],
        ['ja', '日本語'],
        ['ko', '한국어'],
        ['fr', 'Français'],
        ['de', 'Deutsch'],
        ['es', 'Español']
    ]) {
        assert.match(source, new RegExp(`code: '${code}', label: '${label}'`))
    }
    assert.match(source, /v-for="l in langOptions"/)
    assert.match(source, /:label="l\.label"/)
    assert.match(source, /:name="l\.code"/)
    assert.match(source, /v-model="formMap\[l\.code\]\.commonName"/)
    assert.match(source, /v-model="formMap\[l\.code\]\.promotionTips"/)
    assert.match(source, /v-model="formMap\[l\.code\]\.commonBody"/)
})

test('page save persists staged translations after goods creation', async () => {
    const source = await readFile(editUrl, 'utf8')

    assert.match(source, /goodsI18nRef/)
    assert.match(source, /savedGoodsId\s*=\s*await\s+apiGoodsAdd\(formData\)/)
    assert.match(source, /await\s+goodsI18nRef\.value\?\.saveForGoods\(savedGoodsId\)/)
})

test('publish goods always exposes the multilingual tab without waiting for switches', async () => {
    const source = await readFile(editUrl, 'utf8')

    assert.doesNotMatch(source, /showGoodsI18nTab/)
    assert.doesNotMatch(source, /querySysSwitch/)
    assert.match(source, /TabsEnumMap:\s*\[\.\.\.baseTabs,\s*i18nTab\]/)
    assert.match(source, /if\s*\(savedGoodsId\)\s*{\s*await\s+goodsI18nRef\.value\?\.saveForGoods/)
})

test('bcenter multilingual requests use merchant-owned goods endpoints', async () => {
    const source = await readFile(apiUrl, 'utf8')

    assert.ok(!source.includes("urlPrefix: 'web'"))
    assert.doesNotMatch(source, /\/goods\/i18n\//)
    assert.match(source, /\/mall\.Goods\/i18n\/\$\{commonId\}/)
    assert.match(source, /\/mall\.Goods\/i18n\/\$\{commonId\}\/save/)
    assert.match(source, /\/mall\.Goods\/i18n\/\$\{commonId\}\/auto-translate/)
})

test('same canonical goods id does not trigger a duplicate translation request', async () => {
    assert.equal(shouldReloadGoodsI18n('1', undefined), true)
    assert.equal(shouldReloadGoodsI18n(1, '1'), false)
    assert.equal(shouldReloadGoodsI18n('2', 1), true)
    assert.equal(shouldReloadGoodsI18n('', 1), false)
})
