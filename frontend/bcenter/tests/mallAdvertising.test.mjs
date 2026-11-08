import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath } from 'node:url'

const root = fileURLToPath(new URL('../../../', import.meta.url))
const frontend = path.join(root, 'plugins/order/mall-advertising/frontend/src')

test('merchant advertising API uses only seller actions and strips browser tenant identities', async () => {
    const { setMerchantPluginContext } = await import(path.join(frontend, 'merchantContext.js'))
    const api = await import(path.join(frontend, 'merchantApi.js'))
    const calls = []
    setMerchantPluginContext({
        callMerchantApi(action, body) {
            calls.push({ action, body })
            return Promise.resolve([])
        }
    })

    await api.listMerchantPositions()
    await api.listMerchantApplications()
    await api.saveMerchantApplication({
        id: 8,
        title: '秋季上新',
        shopId: 999,
        actorId: 777
    })
    await api.submitMerchantApplication(8)
    await api.withdrawMerchantApplication(8)

    assert.deepEqual(calls, [
        { action: 'positions', body: {} },
        { action: 'list', body: {} },
        { action: 'save', body: { application: { id: 8, title: '秋季上新' } } },
        { action: 'submit', body: { applicationId: 8 } },
        { action: 'withdraw', body: { applicationId: 8 } }
    ])
    setMerchantPluginContext(null)
})

test('seller manifest exposes the merchant application view without admin routes', () => {
    const seller = readFileSync(path.join(frontend, 'bcenter.js'), 'utf8')
    assert.match(seller, /merchant-applications/)
    assert.match(seller, /MerchantAdvertisingApplications/)
    assert.doesNotMatch(seller, /AdvertisingOverview|AdvertisingPositions/)

    const view = readFileSync(
        path.join(frontend, 'views', 'MerchantAdvertisingApplications.vue'),
        'utf8'
    )
    for (const label of ['广告投放', '新建申请', '提交审核', '撤回申请', '有效时间']) {
        assert.match(view, new RegExp(label), `商家申请页缺少 ${label}`)
    }
})

test('paid plugin build emits a dedicated seller bundle using the host Vue runtime', () => {
    const sellerArtifact = path.join(root, 'plugins/order/mall-advertising/frontend/bcenter.mjs')
    assert.equal(existsSync(sellerArtifact), true)
    const artifact = readFileSync(sellerArtifact, 'utf8')
    assert.match(artifact, /__HOUCLOUD_PLUGIN_RUNTIME__/)
    assert.match(artifact, /merchant-applications/)
    assert.doesNotMatch(artifact, /AdvertisingOverview|AdvertisingPositions/)
})
