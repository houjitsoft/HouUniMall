import test from 'node:test'
import assert from 'node:assert/strict'

import { buildConsultationChatPath } from '../app/utils/imConsultation.js'

test('商品咨询入口携带店铺与可展示的商品快照', () => {
    const path = buildConsultationChatPath({
        shopId: 28,
        goodsId: 901,
        commonId: 77,
        commonName: '轻薄办公本 14 英寸',
        commonImage: 'https://cdn.example.com/goods/901.png',
        commonPrice: 3999.5
    })

    const url = new URL(path, 'https://example.test')
    assert.equal(url.pathname, '/user/chat')
    assert.equal(url.searchParams.get('isMall'), '1')
    assert.equal(url.searchParams.get('shopId'), '28')
    assert.equal(url.searchParams.get('consultGoodsId'), '901')
    assert.equal(url.searchParams.get('consultCommonId'), '77')
    assert.equal(url.searchParams.get('consultName'), '轻薄办公本 14 英寸')
    assert.equal(url.searchParams.get('consultImage'), 'https://cdn.example.com/goods/901.png')
    assert.equal(url.searchParams.get('consultPrice'), '3999.5')
})

test('缺少必要商品或店铺信息时不生成咨询入口', () => {
    assert.equal(buildConsultationChatPath({ shopId: 28 }), '')
    assert.equal(buildConsultationChatPath({ goodsId: 901 }), '')
})
