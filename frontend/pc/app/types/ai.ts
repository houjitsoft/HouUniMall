/**
 * AI 相关类型定义
 */

/** 对话列表项 */
export interface AiConversationItem {
    conversationId: number
    conversationName: string
    userId: number | null
    updateTime: string
}

/** 对话列表分页结果 */
export interface AiConversationListData {
    totalCount: number
    pageSize: number
    totalPage: number
    currPage: number
    list: AiConversationItem[]
}

/** AI 商品搜索结果项 */
export interface AiGoodsSearchItem {
    goods_id: number
    goods_price: string
    goods_spec: string
    common_id: number
    goods_name: string
    brand_name: string
    goods_image: string
}

/** 消息历史项 */
export interface AiMessageHistoryItem {
    id: number | null
    conversationId: number
    userId: number | null
    searchKey: string
    messageType: number
    messageData: string
    messageDataMap: {
        brandNameList?: string[]
        data?: AiGoodsSearchItem[]
        messageType?: number
        conversationId?: number
        searchKey?: string
        goodsSpecList?: string[]
        /** 图片搜索时，图片链接（历史消息） */
        imageUrl?: string
    }
    createTime: string
}

/** 消息历史分页结果 */
export interface AiMessageHistoryListData {
    totalCount: number
    pageSize: number
    totalPage: number
    currPage: number
    list: AiMessageHistoryItem[]
}

/** AI 商品搜索响应 data */
export interface AiGoodsSearchData {
    brandNameList: string[]
    data: AiGoodsSearchItem[]
    messageType: number
    conversationId: number
    searchKey: string
    goodsSpecList: string[]
    /** 图片搜索时，图片链接（历史消息） */
    imageUrl?: string
    /** 助手对本次推荐的说明 */
    assistantText?: string
    /** 大模型是否已就绪（aiagent /web/ai/chat 真实返回时为 true） */
    aiReady?: boolean
    /** 模型未就绪或调用失败时的提示文案 */
    aiMessage?: string
    /** 实际回答的模型名 */
    aiModel?: string
}

/** AI 中台（aiagent）对话响应 */
export interface AiChatData {
    success: boolean
    sessionNo: string
    content: string
    model?: string
    message?: string
    errorCode?: string
}
