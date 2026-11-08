const STORAGE_PREFIX = 'unimall-ai-shopping-v1'
const PRODUCT_TERMS = [
    '笔记本', '电脑', '手机', '打印机', '显示器', '路由器', '耳机', '键盘', '鼠标',
    '相机', '平板', '手表', '音箱', '充电器', '投影仪', '办公椅'
]

function hashOwner(owner) {
    const value = String(owner || 'guest')
    let hash = 2166136261
    for (let i = 0; i < value.length; i += 1) {
        hash ^= value.charCodeAt(i)
        hash = Math.imul(hash, 16777619)
    }
    return (hash >>> 0).toString(36)
}

function storageKey(owner) {
    return `${STORAGE_PREFIX}:${hashOwner(owner)}`
}

function emptyState() {
    return { conversations: [] }
}

function readState(storage, owner) {
    if (!storage) return emptyState()
    try {
        const raw = storage.getItem(storageKey(owner))
        if (!raw) return emptyState()
        const parsed = JSON.parse(raw)
        if (!parsed || !Array.isArray(parsed.conversations)) return emptyState()
        return parsed
    } catch {
        return emptyState()
    }
}

function writeState(storage, owner, state) {
    if (!storage) return
    storage.setItem(storageKey(owner), JSON.stringify(state))
}

function paginate(list, page, limit) {
    const pageNo = Math.max(1, Number(page) || 1)
    const pageSize = Math.max(1, Number(limit) || 20)
    const start = (pageNo - 1) * pageSize
    return {
        totalCount: list.length,
        pageSize,
        totalPage: Math.ceil(list.length / pageSize),
        currPage: pageNo,
        list: list.slice(start, start + pageSize)
    }
}

function nextId(items) {
    const largest = items.reduce((max, item) => Math.max(max, Number(item.id || item.conversationId) || 0), 0)
    return Math.max(Date.now(), largest + 1)
}

function compactMessageData(value) {
    if (!value || typeof value !== 'object') return value
    const cloned = { ...value }
    if (typeof cloned.imageUrl === 'string' && cloned.imageUrl.startsWith('data:') && cloned.imageUrl.length > 100_000) {
        delete cloned.imageUrl
    }
    return cloned
}

export function buildAiSearchTerms(prompt) {
    const original = String(prompt || '').trim()
    if (!original) return []
    const category = PRODUCT_TERMS.find(term => original.includes(term))
    return category && category !== original ? [original, category] : [original]
}

export function normalizeAiGoodsSearch(payload, searchKey) {
    const rawList = Array.isArray(payload?.lists)
        ? payload.lists
        : Array.isArray(payload?.list)
            ? payload.list
            : Array.isArray(payload?.goodsList)
                ? payload.goodsList
                : []
    const data = rawList.map((item) => {
        const goodsId = Number(item?.goods_id ?? item?.goodsId ?? item?.id ?? 0) || 0
        return {
            goods_id: goodsId,
            goods_price: String(item?.goods_price ?? item?.goodsPrice ?? item?.minPrice ?? item?.price ?? ''),
            goods_spec: String(item?.goods_spec ?? item?.goodsSpec ?? item?.descript ?? ''),
            common_id: Number(item?.common_id ?? item?.commonId ?? goodsId) || goodsId,
            goods_name: String(item?.goods_name ?? item?.goodsName ?? item?.name ?? ''),
            brand_name: String(item?.brand_name ?? item?.brandName ?? ''),
            goods_image: String(item?.goods_image ?? item?.goodsImage ?? item?.image ?? '')
        }
    }).filter(item => item.goods_id > 0)

    return {
        brandNameList: [],
        data,
        messageType: 1,
        conversationId: 0,
        searchKey: String(searchKey || ''),
        goodsSpecList: []
    }
}

export function appendAiSearch(storage, owner, input) {
    const state = readState(storage, owner)
    let conversation = state.conversations.find(item => Number(item.conversationId) === Number(input.conversationId))
    const now = new Date().toISOString()
    if (!conversation) {
        conversation = {
            conversationId: nextId(state.conversations),
            conversationName: String(input.searchKey || '新对话').slice(0, 30),
            userId: null,
            createTime: now,
            updateTime: now,
            messages: []
        }
        state.conversations.push(conversation)
    }
    const message = {
        id: nextId(conversation.messages || []),
        conversationId: conversation.conversationId,
        userId: null,
        searchKey: String(input.searchKey || ''),
        messageType: Number(input.messageType || 1),
        messageData: JSON.stringify(input.messageDataMap || {}),
        messageDataMap: compactMessageData(input.messageDataMap || {}),
        createTime: now
    }
    conversation.messages = Array.isArray(conversation.messages) ? conversation.messages : []
    conversation.messages.push(message)
    conversation.updateTime = now
    writeState(storage, owner, state)
    return { conversationId: conversation.conversationId, message }
}

export function listAiConversations(storage, owner, page = 1, limit = 20) {
    const state = readState(storage, owner)
    const list = [...state.conversations]
        .sort((a, b) => String(b.updateTime).localeCompare(String(a.updateTime)))
        .map(({ messages, createTime, ...conversation }) => conversation)
    return paginate(list, page, limit)
}

export function listAiMessages(storage, owner, conversationId, page = 1, limit = 10) {
    const state = readState(storage, owner)
    const conversation = state.conversations.find(item => Number(item.conversationId) === Number(conversationId))
    const list = conversation && Array.isArray(conversation.messages)
        ? [...conversation.messages].sort((a, b) => String(b.createTime).localeCompare(String(a.createTime)))
        : []
    return paginate(list, page, limit)
}

export function renameAiConversation(storage, owner, conversationId, conversationName) {
    const state = readState(storage, owner)
    const conversation = state.conversations.find(item => Number(item.conversationId) === Number(conversationId))
    if (!conversation) return false
    conversation.conversationName = String(conversationName || '').trim().slice(0, 50)
    conversation.updateTime = new Date().toISOString()
    writeState(storage, owner, state)
    return true
}

export function deleteAiConversation(storage, owner, conversationId) {
    const state = readState(storage, owner)
    const next = state.conversations.filter(item => Number(item.conversationId) !== Number(conversationId))
    if (next.length === state.conversations.length) return false
    state.conversations = next
    writeState(storage, owner, state)
    return true
}
