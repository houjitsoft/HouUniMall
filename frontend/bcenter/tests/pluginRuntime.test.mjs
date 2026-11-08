import assert from 'node:assert/strict'
import test from 'node:test'

import {
    collectAuthorizedSellerComponents,
    createSellerPluginRegistry,
    resolveSellerEntry
} from '../src/plugins/runtimeCore.mjs'

const advertisingMenu = (overrides = {}) => ({
    type: 1,
    status: 1,
    show_status: 1,
    component: 'plugin:mall-advertising/merchant-applications',
    ...overrides
})

const installedAdvertising = (overrides = {}) => ({
    code: 'mall-advertising',
    installed: true,
    enabled: true,
    terminals: ['admin', 'seller', 'pc'],
    sellerEntry: '/web/plugin-assets/mall-advertising/1.0.0/bcenter.mjs',
    ...overrides
})

test('loads an enabled seller entry only when the merchant permission tree authorizes its component', async () => {
    const imported = []
    const component = { name: 'MerchantAdvertisingApplications' }
    const registry = createSellerPluginRegistry({
        importModule: async (url) => {
            imported.push(url)
            return { default: { views: { 'merchant-applications': component } } }
        }
    })

    await registry.sync([advertisingMenu()], [installedAdvertising()])

    assert.deepEqual(imported, ['/web/plugin-assets/mall-advertising/1.0.0/bcenter.mjs'])
    assert.equal(
        registry.resolve('plugin:mall-advertising/merchant-applications'),
        component
    )
})

test('does not load unauthorized, uninstalled, disabled, or non-seller plugins', async () => {
    const imported = []
    const registry = createSellerPluginRegistry({
        importModule: async (url) => {
            imported.push(url)
            return { default: { views: {} } }
        }
    })

    await registry.sync([], [installedAdvertising()])
    await registry.sync([advertisingMenu()], [installedAdvertising({ installed: false })])
    await registry.sync([advertisingMenu()], [installedAdvertising({ enabled: false })])
    await registry.sync([advertisingMenu()], [installedAdvertising({ terminals: ['admin'] })])

    assert.deepEqual(imported, [])
    assert.equal(registry.resolve('plugin:mall-advertising/merchant-applications'), null)
})

test('unloads plugin components and calls teardown when permission or installation disappears', async () => {
    let teardownCount = 0
    const registry = createSellerPluginRegistry({
        importModule: async () => ({
            default: {
                views: { 'merchant-applications': { name: 'AdvertisingPage' } },
                teardown() {
                    teardownCount += 1
                }
            }
        })
    })

    await registry.sync([advertisingMenu()], [installedAdvertising()])
    await registry.sync([], [installedAdvertising()])

    assert.equal(teardownCount, 1)
    assert.equal(registry.resolve('plugin:mall-advertising/merchant-applications'), null)
})

test('seller component parsing ignores hidden menu rows and nested non-plugin views', () => {
    const refs = collectAuthorizedSellerComponents([
        {
            type: 0,
            status: 1,
            show_status: 1,
            component: 'layout/index',
            sub: [
                advertisingMenu(),
                advertisingMenu({
                    show_status: 0,
                    component: 'plugin:hidden-plugin/page'
                }),
                { type: 1, status: 1, show_status: 1, component: 'order/lists' }
            ]
        }
    ])

    assert.deepEqual([...refs.keys()], ['mall-advertising'])
    assert.deepEqual([...refs.get('mall-advertising')], ['merchant-applications'])
})

test('seller entry resolution never loads the admin index bundle', () => {
    assert.equal(
        resolveSellerEntry(installedAdvertising()),
        '/web/plugin-assets/mall-advertising/1.0.0/bcenter.mjs'
    )
    assert.equal(
        resolveSellerEntry(installedAdvertising({
            sellerEntry: null,
            frontendUrl: '/web/plugin-assets/mall-advertising/1.0.0/index.mjs?rev=abc'
        })),
        '/web/plugin-assets/mall-advertising/1.0.0/bcenter.mjs?rev=abc'
    )
    assert.equal(
        resolveSellerEntry(installedAdvertising({
            sellerEntry: '/web/plugin-assets/mall-advertising/1.0.0/index.mjs'
        })),
        null
    )
})
