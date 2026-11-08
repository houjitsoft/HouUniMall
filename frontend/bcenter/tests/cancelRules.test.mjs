import assert from 'node:assert/strict'
import { buildRequestKey } from '../src/utils/request/cancelRules.mjs'

function test(name, run) {
    Promise.resolve()
        .then(run)
        .then(() => console.log(`PASS ${name}`))
        .catch((error) => {
            console.error(`FAIL ${name}`)
            console.error(error)
            process.exitCode = 1
        })
}

test('same switch endpoint with different configKey is not treated as duplicate', () => {
    const languageKey = buildRequestKey({
        method: 'get',
        url: 'web/base/sys-config/switch/query-language',
        params: { configKey: 'language_switch' }
    })
    const autoTranslateKey = buildRequestKey({
        method: 'get',
        url: 'web/base/sys-config/switch/query-language',
        params: { configKey: 'auto_translate_switch' }
    })

    assert.notEqual(languageKey, autoTranslateKey)
})

test('identical requests still share one duplicate-request key', () => {
    const request = {
        method: 'get',
        url: 'web/base/sys-config/switch/query-language',
        params: { configKey: 'language_switch' }
    }

    assert.equal(buildRequestKey(request), buildRequestKey({ ...request }))
})

test('object key order does not change the duplicate-request key', () => {
    const first = buildRequestKey({
        method: 'post',
        url: 'web/merchant/mall.Goods/i18n/14/save',
        data: { commonId: 14, items: [{ langCode: 'en', commonName: 'Name' }] }
    })
    const second = buildRequestKey({
        url: 'web/merchant/mall.Goods/i18n/14/save',
        method: 'POST',
        data: { items: [{ commonName: 'Name', langCode: 'en' }], commonId: 14 }
    })

    assert.equal(first, second)
})
