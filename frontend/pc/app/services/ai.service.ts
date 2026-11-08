import type { ApiResponse } from '~/types/api'
import type {
    AiConversationListData,
    AiMessageHistoryListData,
    AiGoodsSearchData,
    AiChatData
} from '~/types/ai'
import { getToken } from '~/utils/auth'
import {
    appendAiSearch,
    buildAiSearchTerms,
    deleteAiConversation as deleteLocalConversation,
    listAiConversations,
    listAiMessages,
    normalizeAiGoodsSearch,
    renameAiConversation
} from '~/utils/aiShoppingSession.js'

const success = <T>(data: T): ApiResponse<T> => ({ code: 1, message: '操作成功', data })

const localStorageContext = () => ({
    storage: import.meta.client ? window.localStorage : null,
    owner: getToken() || 'guest'
})

/**
 * 查询左侧对话列表
 */
export const selectAllConversation = async (params: { page: number; limit: number }): Promise<ApiResponse<AiConversationListData>> => {
    const { storage, owner } = localStorageContext()
    return success(listAiConversations(storage, owner, params.page ?? 1, params.limit ?? 20))
}

/**
 * 根据会话 ID 查询对话消息历史
 */
export const queryAllByConversationId = async (params: {
    conversationId: number
    page: number
    limit: number
}): Promise<ApiResponse<AiMessageHistoryListData>> => {
    const { storage, owner } = localStorageContext()
    return success(listAiMessages(storage, owner, params.conversationId, params.page ?? 1, params.limit ?? 10))
}

/**
 * 删除对话
 */
export const deleteConversation = async (params: { conversationId: number }): Promise<ApiResponse<unknown>> => {
    const { storage, owner } = localStorageContext()
    const deleted = deleteLocalConversation(storage, owner, params.conversationId)
    return deleted ? success(null) : { code: 0, message: '对话不存在', data: null }
}

/**
 * 更新对话（重命名）
 */
export const updateConversation = async (params: {
    conversationId: number
    conversationName: string
}): Promise<ApiResponse<unknown>> => {
    const { storage, owner } = localStorageContext()
    const updated = renameAiConversation(storage, owner, params.conversationId, params.conversationName)
    return updated ? success(null) : { code: 0, message: '对话不存在', data: null }
}

/**
 * AI 中台（aiagent）真实大模型对话。
 *
 * 说明：模型密钥尚未录入时，中台会返回 success=false + message（不会抛异常），
 * 页面据此降级展示商品匹配结果并给出「模型未就绪」提示，不白屏、不报错。
 */
export const aiChat = async (params: {
    content: string
    sessionNo: string
    model?: string
}): Promise<ApiResponse<AiChatData>> => {
    const { post } = useRequest()
    return await post<AiChatData>('/web/ai/chat', {
        content: params.content,
        sessionNo: params.sessionNo,
        ...(params.model ? { model: params.model } : {})
    })
}

/**
 * 发送对话查询（商品搜索）
 */
export const aiGoodsSearch = async (params: {
    prompt: string
    conversationId: number | null
}): Promise<ApiResponse<AiGoodsSearchData>> => {
    const { post } = useRequest()
    const terms = buildAiSearchTerms(params.prompt)
    let response: ApiResponse<any> | undefined
    for (const name of [...terms, '']) {
        response = await post<any>('/web/mall/goods/front/lists', {
            page: 1,
            limit: 20,
            ...(name ? { name } : {})
        })
        const rows = response?.data?.lists ?? response?.data?.list ?? response?.data?.goodsList
        if (response?.code !== 1 || (Array.isArray(rows) && rows.length > 0)) break
    }
    if (!response || response.code !== 1) {
        return response as ApiResponse<AiGoodsSearchData>
    }
    const data = normalizeAiGoodsSearch(response.data, params.prompt) as AiGoodsSearchData
    data.assistantText = data.data.length > 0
        ? '已根据你的需求从在售商品中筛选出这些推荐。'
        : '暂时没有找到匹配的在售商品，可以换个品类或关键词试试。'
    const { storage, owner } = localStorageContext()
    const saved = appendAiSearch(storage, owner, {
        conversationId: params.conversationId,
        searchKey: params.prompt,
        messageType: data.messageType,
        messageDataMap: data
    })
    data.conversationId = saved.conversationId
    return success(data)
}

/**
 * 图片搜索商品
 */
export const aiImagesGoodsSearch = async (params: {
    base64Str: string
    conversationId?: string
    brandIdList?: number[]
    catId?: string
    commentOrder?: number
    limit?: number
    maxPrice?: string
    minPrice?: string
    orderType?: number
    page?: number
    priceOrder?: string
    saleNum?: string
    shopSelfSupport?: string
    streamName?: string
    attrName?: string
}): Promise<ApiResponse<AiGoodsSearchData>> => {
    const { post } = useRequest()
    const response = await post<any>('/web/mall/goods/front/lists', {
        page: params.page ?? 1,
        limit: params.limit ?? 20,
        ...(params.attrName ? { name: params.attrName } : {})
    })
    if (response.code !== 1) return response as ApiResponse<AiGoodsSearchData>
    const searchKey = '图片推荐'
    const data = normalizeAiGoodsSearch(response.data, searchKey) as AiGoodsSearchData
    data.assistantText = '图片已收到。识图模型接入前，先为你展示当前热门在售商品。'
    const { storage, owner } = localStorageContext()
    const conversationId = params.conversationId ? Number(params.conversationId) : null
    const saved = appendAiSearch(storage, owner, {
        conversationId,
        searchKey,
        messageType: data.messageType,
        messageDataMap: data
    })
    data.conversationId = saved.conversationId
    return success(data)
}
