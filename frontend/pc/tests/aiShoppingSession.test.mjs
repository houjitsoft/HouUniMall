import test from 'node:test'
import assert from 'node:assert/strict'

import {
    appendAiSearch,
    buildAiSearchTerms,
    deleteAiConversation,
    listAiConversations,
    listAiMessages,
    normalizeAiGoodsSearch,
    renameAiConversation
} from '../app/utils/aiShoppingSession.js'

class MemoryStorage {
    values = new Map()

    getItem(key) {
        return this.values.has(key) ? this.values.get(key) : null
    }

    setItem(key, value) {
        this.values.set(key, String(value))
    }

    removeItem(key) {
        this.values.delete(key)
    }
}

test('商品搜索结果转换为 AI 推荐卡片契约', () => {
    const data = normalizeAiGoodsSearch({
        lists: [{
            id: 4,
            name: '旗舰智能手机 5G',
            image: '/pc/demo/goods-phone.svg',
            minPrice: 2999,
            descript: '高刷屏、长续航'
        }]
    }, '适合出差的手机')

    assert.deepEqual(data, {
        brandNameList: [],
        data: [{
            goods_id: 4,
            goods_price: '2999',
            goods_spec: '高刷屏、长续航',
            common_id: 4,
            goods_name: '旗舰智能手机 5G',
            brand_name: '',
            goods_image: '/pc/demo/goods-phone.svg'
        }],
        messageType: 1,
        conversationId: 0,
        searchKey: '适合出差的手机',
        goodsSpecList: []
    })
})

test('自然语言购物需求会提取可用于商品库搜索的品类词', () => {
    assert.deepEqual(buildAiSearchTerms('推荐一款适合出差、性价比高的办公电脑'), [
        '推荐一款适合出差、性价比高的办公电脑',
        '电脑'
    ])
    assert.deepEqual(buildAiSearchTerms('看看手机'), ['看看手机', '手机'])
    assert.deepEqual(buildAiSearchTerms('随便看看'), ['随便看看'])
})

test('搜索会创建会话并按用户隔离保存消息', () => {
    const storage = new MemoryStorage()
    const first = appendAiSearch(storage, 'user-7', {
        conversationId: null,
        searchKey: '办公电脑',
        messageDataMap: { data: [{ goods_id: 9 }] }
    })

    assert.ok(first.conversationId > 0)
    assert.equal(listAiConversations(storage, 'user-7', 1, 20).list[0].conversationName, '办公电脑')
    assert.equal(listAiMessages(storage, 'user-7', first.conversationId, 1, 10).list[0].searchKey, '办公电脑')
    assert.deepEqual(listAiConversations(storage, 'user-8', 1, 20).list, [])
})

test('会话可以重命名并删除且不能操作其他用户数据', () => {
    const storage = new MemoryStorage()
    const created = appendAiSearch(storage, 'user-7', {
        conversationId: null,
        searchKey: '办公电脑',
        messageDataMap: { data: [] }
    })

    assert.equal(renameAiConversation(storage, 'user-8', created.conversationId, '越权修改'), false)
    assert.equal(renameAiConversation(storage, 'user-7', created.conversationId, '差旅设备'), true)
    assert.equal(listAiConversations(storage, 'user-7', 1, 20).list[0].conversationName, '差旅设备')
    assert.equal(deleteAiConversation(storage, 'user-7', created.conversationId), true)
    assert.deepEqual(listAiConversations(storage, 'user-7', 1, 20).list, [])
})
