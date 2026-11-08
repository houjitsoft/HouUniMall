<template>
    <div class="ai-assistant-page">
        <div class="ai-assistant-layout">
            <!-- 左侧对话列表 -->
            <aside class="ai-sidebar">
                <div class="sidebar-logo" @click="router.push('/')">
                    <img v-if="logoUrl" :src="logoUrl" alt="Logo" class="sidebar-logo-img" />
                    <span class="sidebar-brand-copy">商城 AI 导购</span>
                </div>
                <button type="button" class="btn-new-chat" aria-label="新建对话" @click="handleNewChat">
                    <UIcon name="i-lucide-plus" aria-hidden="true" />
                    {{ $t('新建对话') }}
                </button>
                <div class="conversation-list-wrap">
                    <template v-if="conversationLoading">
                        <div class="list-loading">{{ $t('加载中...') }}</div>
                    </template>
                    <template v-else-if="conversationList.length === 0">
                        <div class="list-empty">{{ $t('暂无对话') }}</div>
                    </template>
                    <ul v-else class="conversation-list">
                        <li v-for="item in conversationList" :key="item.conversationId" class="conversation-item"
                            :class="{ active: currentConversationId === item.conversationId }"
                            @click="handleConversationItemClick(item, $event)">
                            <template v-if="editingConversationId === item.conversationId">
                                <input v-model.trim="editingName" type="text" class="conversation-rename-input"
                                    maxlength="20" :placeholder="$t('请输入名称')"
                                    @keydown.enter="submitRename(item.conversationId)" @keydown.esc="cancelRename"
                                    @click.stop />
                            </template>
                            <template v-else>
                                <span class="conversation-label">{{ item.conversationName || `对话 ${item.conversationId}`
                                    }}</span>
                                <span class="conversation-actions">
                                    <button type="button" class="btn-item btn-rename" :title="$t('重命名')"
                                        @click.stop="startRename(item)">{{ $t('重命名') }}</button>
                                    <button type="button" class="btn-item btn-delete" :title="$t('删除')"
                                        @click.stop="handleDeleteClick(item)">{{ $t('删除') }}</button>
                                </span>
                            </template>
                        </li>
                    </ul>
                </div>
            </aside>

            <!-- 主内容区 -->
            <main class="ai-main">
                <header class="ai-main-header">
                    <div>
                        <span>AI 导购</span>
                        <strong>智能商品顾问</strong>
                    </div>
                    <button type="button" @click="router.push('/')">
                        <UIcon name="i-lucide-store" aria-hidden="true" />
                        返回商城
                    </button>
                </header>
                <div class="chat-area">
                    <template v-if="messageList.length === 0">
                        <!-- 无对话详情时使用默认 AI 回复：欢迎语 + 推荐问题（新对话或会话无历史均显示） -->
                        <div class="message-row ai default-reply">
                            <div class="message-bubble ai">
                                <img :src="aiAvatarUrl" alt="AI" class="avatar ai" />
                                <div class="content default-reply-content">
                                    <p class="reply-text welcome-msg reply-bubble">{{ defaultWelcome.welcome }}</p>
                                    <p class="reply-text section-title">{{ defaultWelcome.sectionTitle }}</p>
                                    <div class="welcome-cards">
                                        <div class="welcome-cards-left">
                                            <div v-for="(card, ci) in welcomeCardsLeft" :key="'left-' + ci"
                                                class="welcome-card" :class="{ 'card-highlight': card.highlight }">
                                                <div class="welcome-card-title">{{ card.title }}</div>
                                                <ul class="welcome-suggestions">
                                                    <li v-for="(s, si) in card.suggestions" :key="si"
                                                        class="suggestion-item">
                                                        <span class="suggestion-tag"
                                                            @click="applySuggestion(getSuggestionText(s))">{{
                                                                getSuggestionText(s) }}</span>
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                        <div class="welcome-cards-right">
                                            <div v-for="(card, ci) in welcomeCardsRight" :key="'right-' + ci"
                                                class="welcome-card" :class="{ 'card-highlight': card.highlight }">
                                                <div class="welcome-card-title">{{ card.title }}</div>
                                                <ul class="welcome-suggestions has-images">
                                                    <li v-for="(s, si) in card.suggestions" :key="si"
                                                        class="suggestion-item">
                                                        <div class="suggestion-with-image"
                                                            @click="applySuggestion(getSuggestionText(s))">
                                                            <span class="suggestion-tag">{{ getSuggestionText(s)
                                                            }}</span>
                                                            <img v-if="getSuggestionImage(s)"
                                                                :src="IMAGE_URL(getSuggestionImage(s))"
                                                                :alt="getSuggestionText(s)" class="suggestion-img" />
                                                        </div>
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                    <template v-else>
                        <div class="message-list" ref="messageListRef">
                            <template v-for="(msg, index) in messageList" :key="index">
                                <!-- 用户消息 -->
                                <div class="message-row user">
                                    <div class="message-bubble user">
                                        <img v-if="userStore.getUserInfo?.userAvatar"
                                            :src="userStore.getUserInfo.userAvatar" alt="用户" class="avatar user" />
                                        <div v-else class="avatar user" />
                                        <template v-if="msg.imageDataUrl">
                                            <img :src="msg.imageDataUrl" alt="搜索图片" class="user-msg-image" />
                                        </template>
                                        <span v-else class="text">{{ msg.searchKey }}</span>
                                    </div>
                                </div>
                                <!-- AI 回复 -->
                                <div class="message-row ai" :class="{ 'msg-loading': msg.loading }">
                                    <div class="message-bubble ai">
                                        <img :src="aiAvatarUrl" alt="AI" class="avatar ai" />
                                        <div class="content">
                                            <!-- 该条 AI 回复加载中 -->
                                            <p v-if="msg.loading" class="reply-text reply-bubble">
                                                正在为您推荐
                                                <span class="typing-dots">
                                                    <span class="dot" /> <span class="dot" /> <span class="dot" />
                                                </span>
                                            </p>
                                            <template v-else>
                                                <!-- 大模型未就绪：说明下面是关键词匹配结果，避免用户误以为是模型回答 -->
                                                <p v-if="msg.messageDataMap?.aiReady === false" class="ai-not-ready">
                                                    {{ $t('AI 模型尚未接入，以下为关键词匹配结果') }}
                                                </p>
                                                <!-- 无商品无规格时提示 -->
                                                <p v-if="!msg.messageDataMap?.aiReady && !msg.messageDataMap?.data?.length && !msg.messageDataMap?.goodsSpecList?.length"
                                                    class="reply-text empty-tip reply-bubble">{{ $t('未搜索到你想要的商品，你可以试试其他商品')
                                                    }}</p>
                                                <template v-if="msg.messageDataMap?.aiReady || msg.messageDataMap?.data?.length || msg.messageDataMap?.goodsSpecList?.length">
                                                <p class="reply-text">{{ $t(msg.messageDataMap?.assistantText || '以下是推荐的相关商品:') }}</p>
                                                <p v-if="msg.messageDataMap?.aiModel" class="ai-model-tag">
                                                    {{ $t('由模型') }} {{ msg.messageDataMap.aiModel }} {{ $t('生成') }}
                                                </p>
                                                <!-- 商品卡片列表 -->
                                                <div v-if="msg.messageDataMap?.data?.length" class="goods-cards">
                                                    <div v-for="goods in (msg.messageDataMap.data || []).slice(0, 5)"
                                                        :key="goods.goods_id" class="goods-card"
                                                        @click="goProduct(goods.goods_id)">
                                                        <div class="goods-img-wrap">
                                                            <img :src="goods.goods_image" :alt="goods.goods_name"
                                                                class="goods-img" />
                                                        </div>
                                                        <div class="goods-info">
                                                            <div class="goods-name" :title="goods.goods_name">{{
                                                                $t(goods.goods_name || '') }}</div>
                                                            <div class="goods-price">{{
                                                                formatPriceWithSymbolSafe(goods.goods_price) }}</div>
                                                        </div>
                                                    </div>
                                                </div>
                                                <a v-if="(msg.messageDataMap?.data?.length || 0) > 5"
                                                    href="javascript:;" class="link-more-sku"
                                                    @click.prevent="expandGoods(msg.searchKey)">{{ $t('查看更多SKU') }}</a>
                                                <!-- 规格建议 -->
                                                <div v-if="msg.messageDataMap?.goodsSpecList?.length"
                                                    class="spec-suggest"
                                                    :class="{ 'is-collapsed': !getSpecExpanded(index) && shouldShowSpecExpand(msg.messageDataMap.goodsSpecList) }">
                                                    <div class="spec-tags-wrap">
                                                        <span class="spec-label">{{ $t('您对规格有要求吗?比如:') }} </span>
                                                        <span v-for="(spec, si) in msg.messageDataMap.goodsSpecList"
                                                            :key="si" class="spec-tag"
                                                            @click="sendSpec(spec, msg.conversationId)">{{
                                                                spec.trim() }}</span>
                                                    </div>
                                                    <a v-if="shouldShowSpecExpand(msg.messageDataMap.goodsSpecList)"
                                                        href="javascript:;" class="spec-expand-btn"
                                                        @click.prevent="toggleSpecExpand(index)">{{
                                                            getSpecExpanded(index) ? $t('收起') : $t('展开') }}</a>
                                                </div>
                                                </template>
                                            </template>
                                        </div>
                                    </div>
                                </div>
                            </template>
                            <!-- 发送中占位（仅在没有“加载中”的消息时显示，避免重复） -->
                            <div v-if="sending && !(messageList.length && messageList[messageList.length - 1]?.loading)"
                                class="message-row ai sending-placeholder">
                                <div class="message-bubble ai">
                                    <img :src="aiAvatarUrl" alt="AI" class="avatar ai" />
                                    <div class="content">
                                        <p class="reply-text reply-bubble">
                                            {{ $t('正在为您推荐') }}
                                            <span class="typing-dots">
                                                <span class="dot" /> <span class="dot" /> <span class="dot" />
                                            </span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>

                <!-- 底部输入区 -->
                <div class="input-area">
                    <input ref="imageInputRef" type="file" accept="image/*" class="input-file-hidden"
                        @change="handleImageSelect" />
                    <button type="button" class="input-icon camera" :title="$t('上传图片')"
                        @click="triggerImageUpload">
                        <UIcon name="i-lucide-image-up" aria-hidden="true" />
                    </button>
                    <input v-model="inputText" type="text" class="input-field"
                        :placeholder="$t('您想要购买或咨询什么,支持上传图片,我们为您个性化推荐~')" @keydown.enter.prevent="handleSend" />
                    <button type="button" class="btn-send" :aria-label="$t('发送')" @click="handleSend"
                        :disabled="sending || !inputText.trim()">
                        <UIcon name="i-lucide-arrow-up" class="send-icon" aria-hidden="true" />
                    </button>
                </div>
            </main>
        </div>

        <!-- 底部版权与免责 -->
        <footer class="ai-footer">
            <p class="copyright">
                <template v-if="copyrightInfo">{{ copyrightInfo }}</template>
                <span v-if="copyrightInfo && icpInfo" class="icp-info"> · </span>
                <template v-if="icpInfo">{{ icpInfo }}</template>
            </p>
            <p class="disclaimer">{{ $t('以上内容由AI生成,无法保证真实准确,仅供参考') }}</p>
        </footer>
    </div>
</template>

<script setup lang="ts">
import type { AiConversationItem, AiMessageHistoryItem, AiGoodsSearchData } from '~/types/ai'
import {
    selectAllConversation,
    queryAllByConversationId,
    aiGoodsSearch,
    aiImagesGoodsSearch,
    deleteConversation,
    updateConversation
} from '~/services/ai.service'
import { runAgent } from '~/services/agent.service'

/**
 * 本页使用的智能体编码。
 * 模型、提示词、触发与生效范围全部由后台「智能体管理」配置，改配置无需改代码。
 */
const AGENT_INTENT_RECOGNIZE = 'intent_recognize'
import { useAiStore, useUserStore } from '~/stores'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { selectByConfigKey } from '~/services/base.service'
import { IMAGE_URL } from '~/utils/externalUrl'
import type { ApiResponse } from '~/types/api'

defineOptions({ name: 'AiAssistant' })

/** AI 头像 URL */
const aiAvatarUrl = computed(() => IMAGE_URL('pcImg/AIAssistant.png'))

const router = useRouter()
const aiStore = useAiStore()
const userStore = useUserStore()

/** 左侧对话列表 */
const conversationList = ref<AiConversationItem[]>([])
const conversationLoading = ref(false)
/** 当前选中的会话 ID，新建对话时为 null */
const currentConversationId = ref<number | null>(null)

/** 当前会话的消息列表（用于展示），loading 为 true 表示该条 AI 回复正在加载 */
const messageList = ref<Array<{
    searchKey: string
    conversationId?: number
    messageDataMap?: AiGoodsSearchData
    loading?: boolean
    /** 图片搜索时，发送的图片 dataUrl（用于展示） */
    imageDataUrl?: string
}>>([])
const messageListRef = ref<HTMLElement | null>(null)
const historyLoading = ref(false)

const inputText = ref('')
const sending = ref(false)
const imageInputRef = ref<HTMLInputElement | null>(null)
/** 规格建议展开状态，key 为消息索引 */
const specExpandedMap = ref<Record<number, boolean>>({})

/** 系统 Logo（侧边栏） */
const logoUrl = ref<string>('')
/** 系统版权信息（版本号，底部） */
const copyrightInfo = ref<string>('')
/** 系统备案号（底部） */
const icpInfo = ref<string>('')

/** 重命名：当前编辑的会话 ID 与输入内容 */
const editingConversationId = ref<number | null>(null)
const editingName = ref('')

/** 新对话默认回复内容（欢迎语 + 推荐问题） */
const defaultWelcome = {
    welcome: '您好，我是商城AI助手，欢迎提出您的采购需求，我会为您个性化推荐商品，给您提供更高效、便捷的搜索体验。',
    sectionTitle: '我们可以为您解答如下内容:',
    cards: [
        {
            title: '品名/品牌/参数/场景等信息搜索',
            highlight: false,
            suggestions: ['精选好物推荐', '询问关于大牌优选的商品']
        },
        {
            title: '图片搜索',
            highlight: true,
            suggestions: [
                { text: '推荐相关商品', image: 'aiImgDemo/demo1.png' },
                { text: '有同款显示屏吗?', image: 'aiImgDemo/demo2.png' },
                { text: '解析图片并找商品', image: 'aiImgDemo/demo3.png' }
            ]
        },
        {
            title: '数码产品搜索',
            highlight: false,
            suggestions: ['推荐一款性价比高的电脑', '专业化的办公数码设备有哪些']
        }
    ]
}

/** 左侧纯文案推荐卡片（无图片） */
const welcomeCardsLeft = computed(() =>
    (defaultWelcome.cards || []).filter(
        card => !card.suggestions?.[0] || typeof card.suggestions[0] !== 'object'
    )
)
/** 右侧带图推荐卡片（has-images） */
const welcomeCardsRight = computed(() =>
    (defaultWelcome.cards || []).filter(
        card => card.suggestions?.[0] && typeof card.suggestions[0] === 'object'
    )
)

/** 点击推荐问题，填入输入框并发送 */
function applySuggestion(text: string) {
    inputText.value = text
    doSend(text)
}

/** 带图推荐项取文案（用于右侧 has-images 卡片，s 实际为对象） */
function getSuggestionText(s: string | { text: string; image: string }): string {
    return typeof s === 'string' ? s : s.text
}
/** 带图推荐项取图片（用于右侧 has-images 卡片） */
function getSuggestionImage(s: string | { text: string; image: string }): string {
    return typeof s === 'object' && s && 'image' in s ? s.image : ''
}

/** 加载对话列表，默认选中第一项或已存储的会话 */
async function loadConversationList() {
    conversationLoading.value = true
    try {
        const res = await selectAllConversation({ page: 1, limit: 20 })
        if (res?.code === 1 && res?.data?.list) {
            conversationList.value = res.data.list
            const list = conversationList.value
            if (list.length > 0) {
                const storedId = aiStore.getCurrentConversationId
                const item = list.find(c => c.conversationId === storedId) ?? list[0]!
                await handleSelectConversation(item)
            }
        } else {
            conversationList.value = []
        }
    } catch (e) {
        conversationList.value = []
    } finally {
        conversationLoading.value = false
    }
}

/** 点击列表项：若点击的是操作按钮区域则不切换会话，否则选中该会话 */
function handleConversationItemClick(item: AiConversationItem, e: MouseEvent) {
    const target = e.target as HTMLElement
    if (target.closest('.conversation-actions') || target.closest('.conversation-rename-input')) return
    handleSelectConversation(item)
}

/** 开始重命名 */
function startRename(item: AiConversationItem) {
    editingConversationId.value = item.conversationId
    editingName.value = item.conversationName || ''
    nextTick(() => {
        const input = document.querySelector('.conversation-rename-input') as HTMLInputElement
        input?.focus()
    })
}

/** 取消重命名 */
function cancelRename() {
    editingConversationId.value = null
    editingName.value = ''
}

/** 提交重命名 */
async function submitRename(conversationId: number) {
    const name = editingName.value.trim()
    if (!name) {
        cancelRename()
        return
    }
    try {
        const res = await updateConversation({ conversationId, conversationName: name })
        if (res?.code === 1) {
            cancelRename()
            await loadConversationList()
        }
    } catch (_) {
        cancelRename()
    }
}

/** 点击删除：弹窗确认后调用删除接口，成功则刷新列表 */
async function handleDeleteClick(item: AiConversationItem) {
    const confirmed = await ConfirmModal.confirm({
        message: '确定删除该对话？',
        title: '温馨提示',
        confirmText: '删除',
        cancelText: '取消'
    })
    if (!confirmed) return
    try {
        const res = await deleteConversation({ conversationId: item.conversationId })
        if (res?.code === 1) {
            if (currentConversationId.value === item.conversationId) {
                clearCurrentConversation()
            }
            await loadConversationList()
        }
    } catch (_) {
        // 删除失败可在此提示
    }
}

/** 清除当前会话和消息状态 */
function clearCurrentConversation() {
    currentConversationId.value = null
    aiStore.clearCurrentConversationId()
    messageList.value = []
    specExpandedMap.value = {}
}

/** 新建对话 */
function handleNewChat() {
    clearCurrentConversation()
    inputText.value = ''
    cancelRename()
}

/** 判断是否为图片链接（http/https 或 data:） */
function isImageUrl(s: string | undefined): boolean {
    if (!s?.trim()) return false
    return /^https?:\/\//i.test(s.trim()) || /^data:image\//i.test(s.trim())
}

/** 选中某个对话，加载历史消息并持久化到 store */
async function handleSelectConversation(item: AiConversationItem) {
    if (currentConversationId.value === item.conversationId) return
    currentConversationId.value = item.conversationId
    aiStore.setCurrentConversationId(item.conversationId)
    historyLoading.value = true
    messageList.value = []
    specExpandedMap.value = {}
    try {
        const res = await queryAllByConversationId({
            conversationId: item.conversationId,
            page: 1,
            limit: 10
        })
        if (res?.code === 1 && res?.data?.list?.length) {
            const list = [...res.data.list].reverse()
            messageList.value = list.map((m: AiMessageHistoryItem) => {
                const dataMap = m.messageDataMap as AiGoodsSearchData | undefined
                const imageDataUrl = dataMap?.imageUrl || (isImageUrl(m.searchKey) ? m.searchKey : undefined)
                return {
                    searchKey: m.searchKey || '',
                    conversationId: m.conversationId,
                    messageDataMap: dataMap,
                    imageDataUrl
                }
            })
        }
        nextTick(() => scrollToBottom())
    } catch (e) {
        messageList.value = []
    } finally {
        historyLoading.value = false
    }
}

/** 应用搜索接口响应：更新会话列表、最后一条消息 */
function applySearchResponse(
    res: ApiResponse<AiGoodsSearchData> | undefined,
    last: (typeof messageList.value)[number],
    fallbackSearchKey: string
) {
    if (res?.code === 1 && res?.data && last?.loading) {
        const data = res.data
        if (data.conversationId != null) {
            currentConversationId.value = data.conversationId
            aiStore.setCurrentConversationId(data.conversationId)
            const exists = conversationList.value.some(c => c.conversationId === data.conversationId)
            if (!exists) {
                conversationList.value = [
                    {
                        conversationId: data.conversationId,
                        conversationName: data.searchKey || String(data.conversationId),
                        userId: null,
                        updateTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
                    },
                    ...conversationList.value
                ]
            }
        }
        last.searchKey = data.searchKey || fallbackSearchKey
        last.conversationId = data.conversationId
        last.messageDataMap = data
        last.loading = false
    } else if (last?.loading) {
        last.loading = false
    }
}

/**
 * 用「用户意图识别」智能体的回复覆盖助手文案。
 *
 * 走统一接入层 runAgent：先取后台配置的智能体（模型 + 提示词），
 * 未启用/未绑模型/调用失败一律保留商品匹配文案并置 aiReady=false，
 * 页面显示提示条——不白屏、不报错、购物流程不中断。
 */
async function enrichWithAiChat(
    last: { messageDataMap?: AiGoodsSearchData; conversationId?: number },
    prompt: string
) {
    const conversationId = last.messageDataMap?.conversationId ?? last.conversationId ?? currentConversationId.value
    const sessionNo = `pc-${conversationId ?? 'guest'}`
    const base = (last.messageDataMap || {}) as AiGoodsSearchData
    const result = await runAgent<string>(AGENT_INTENT_RECOGNIZE, prompt, {
        sessionNo,
        bizId: conversationId,
        subjectType: 'goods_search',
        subjectKey: prompt,
        // 兜底：智能体不可用时沿用商品匹配文案
        fallback: () => base.assistantText || ''
    })
    if (result.byAgent && result.data) {
        last.messageDataMap = {
            ...base,
            assistantText: result.data,
            aiReady: true,
            aiModel: result.model || ''
        }
    } else {
        last.messageDataMap = {
            ...base,
            aiReady: false,
            aiMessage: result.reason || '智能体未启用'
        }
    }
}

/** 发送文本（或规格）：先伪发送用户内容，再请求接口，响应完成后显示 AI 回复 */
async function doSend(prompt: string) {
    const trimPrompt = prompt?.trim()
    if (!trimPrompt) return
    const cid = currentConversationId.value
    messageList.value.push({ searchKey: trimPrompt, loading: true })
    inputText.value = ''
    nextTick(() => scrollToBottom())
    sending.value = true
    try {
        const res = await aiGoodsSearch({ prompt: trimPrompt, conversationId: cid })
        const last = messageList.value[messageList.value.length - 1]!
        applySearchResponse(res, last, trimPrompt)
        // 商品匹配完成后，再用中台大模型给出真实回答（未接入时自动降级）
        await enrichWithAiChat(last, trimPrompt)
        nextTick(() => scrollToBottom())
    } finally {
        sending.value = false
    }
}

function handleSend() {
    doSend(inputText.value)
}

/** 触发图片选择 */
function triggerImageUpload() {
    imageInputRef.value?.click()
}

/** 图片选择后：转为 base64 并调用图片搜索接口 */
function handleImageSelect(e: Event) {
    const input = e.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) return
    input.value = ''
    const reader = new FileReader()
    reader.onload = () => {
        const dataUrl = reader.result as string
        if (dataUrl) {
            const base64 = dataUrl.includes(',') ? dataUrl.split(',')[1]! : dataUrl
            doSendImage(base64, dataUrl)
        }
    }
    reader.readAsDataURL(file)
}

/** 图片搜索接口默认参数 */
const AI_IMAGES_SEARCH_DEFAULTS = {
    brandIdList: [] as number[],
    catId: '',
    commentOrder: 1,
    limit: 20,
    maxPrice: '',
    minPrice: '',
    orderType: 3,
    page: 1,
    priceOrder: '',
    saleNum: '',
    shopSelfSupport: '',
    streamName: '.png',
    attrName: ''
} as const

/** 发送图片搜索：先伪发送用户内容，再请求接口，响应完成后显示 AI 回复 */
async function doSendImage(base64Str: string, imageDataUrl: string) {
    const cid = currentConversationId.value
    const searchKey = '图片搜索'
    messageList.value.push({ searchKey, loading: true, imageDataUrl })
    inputText.value = ''
    nextTick(() => scrollToBottom())
    sending.value = true
    try {
        const res = await aiImagesGoodsSearch({
            ...AI_IMAGES_SEARCH_DEFAULTS,
            base64Str,
            conversationId: cid != null ? String(cid) : ''
        })
        const last = messageList.value[messageList.value.length - 1]!
        applySearchResponse(res, last, searchKey)
        // 识图模型接入前，明确标注为关键词匹配结果
        if (last.messageDataMap) last.messageDataMap.aiReady = false
        nextTick(() => scrollToBottom())
    } finally {
        sending.value = false
    }
}

/** 点击规格标签，作为下一句发送 */
function sendSpec(spec: string, conversationId?: number) {
    if (currentConversationId.value == null && conversationId != null) {
        currentConversationId.value = conversationId
    }
    doSend(spec.trim())
}

function scrollToBottom() {
    nextTick(() => {
        const el = messageListRef.value
        if (el) el.scrollTop = el.scrollHeight
    })
}

function expandGoods(searchKey: string) {
    // 可在此展开该条消息的更多商品
    navigateTo(`/product/list?kws=${searchKey}`)
}

/** 是否显示规格展开按钮（超过约 5 行时显示，按标签数量估算） */
function shouldShowSpecExpand(specList: string[] | undefined): boolean {
    return (specList?.length ?? 0) > 15
}

function getSpecExpanded(index: number): boolean {
    return !!specExpandedMap.value[index]
}

function toggleSpecExpand(index: number) {
    specExpandedMap.value[index] = !specExpandedMap.value[index]
}

function goProduct(goodsId: number) {
    router.push(`/product/${goodsId}`)
}

/** 根据配置 key 获取配置值 */
async function fetchConfig(key: string): Promise<string> {
    try {
        const res = await selectByConfigKey(key)
        return res?.code === 1 && res?.data?.configValue ? res.data.configValue : ''
    } catch {
        return ''
    }
}

/** 初始化页面配置 */
async function initPageConfig() {
    const [logo, copyright, icp] = await Promise.all([
        fetchConfig('setting_logo'),
        fetchConfig('setting_copyright'),
        fetchConfig('setting_records')
    ])
    logoUrl.value = logo
    copyrightInfo.value = copyright
    icpInfo.value = icp
}

onMounted(() => {
    initPageConfig()
    loadConversationList()
})
</script>

<style lang="scss" scoped>
/* 页面内使用的 CSS 变量（与全局 --color 一致，无则回退） */
.ai-assistant-page {
    --ai-bg: var(--color-bg-page, #f5f5f5);
    --ai-surface: var(--color-bg, #fff);
    --ai-border: var(--color-border, #eee);
    --ai-text: var(--color-text, #333);
    --ai-text-secondary: var(--color-text-secondary, #666);
    --ai-text-muted: var(--color-text-muted, #999);
    --ai-primary: var(--color);
    --ai-primary-hover: var(--rgba4, color-mix(in srgb, var(--color) 90%, black));
    --ai-accent: var(--color);
}

.ai-assistant-page {
    position: fixed;
    inset: 0;
    width: 100vw;
    height: 100vh;
    height: 100dvh;
    display: flex;
    flex-direction: column;
    background: var(--ai-bg);
    overflow: hidden;
    z-index: 100;
}

.ai-assistant-layout {
    flex: 1;
    min-height: 0;
    display: flex;
    width: 100%;
    overflow: hidden;
}

/* 左侧边栏 */
.ai-sidebar {
    width: 240px;
    flex-shrink: 0;
    background: var(--ai-surface);
    border-right: 1px solid var(--ai-border);
    display: flex;
    flex-direction: column;
    padding: 16px 0;
    box-shadow: 1px 0 0 0 var(--ai-border);
}

.sidebar-logo {
    flex-shrink: 0;
    margin: 0 12px 16px;
    padding: 14px 0;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border-bottom: 1px solid var(--ai-border);
    border-radius: 8px;
    transition: background 0.2s ease, opacity 0.2s ease;

    &:hover {
        background: color-mix(in srgb, var(--ai-primary) 8%, transparent);
        opacity: 0.95;
    }

    &:active {
        opacity: 0.9;
    }
}

.sidebar-logo-img {
    max-width: 100%;
    max-height: 44px;
    object-fit: contain;
}

.sidebar-logo-placeholder {
    font-size: 13px;
    color: var(--ai-text-muted);
}

.btn-new-chat {
    margin: 0 12px 16px;
    padding: 12px 18px;
    background: linear-gradient(135deg, var(--rgba4, color-mix(in srgb, var(--ai-primary) 85%, black)) 0%, var(--ai-primary) 100%);
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    text-align: center;
    transition: transform 0.15s ease, box-shadow 0.2s ease, opacity 0.2s ease;
    box-shadow: 0 2px 6px color-mix(in srgb, var(--ai-primary) 25%, transparent);
    outline: none;

    &:hover {
        opacity: 0.95;
        box-shadow: 0 4px 12px color-mix(in srgb, var(--ai-primary) 35%, transparent);
        transform: translateY(-1px);
    }

    &:active {
        transform: translateY(0);
    }

    &:focus-visible {
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--ai-primary) 35%, transparent);
    }
}

.conversation-list-wrap {
    flex: 1;
    overflow-y: auto;
    padding: 0 4px;
}

.list-loading,
.list-empty {
    padding: 20px 16px;
    text-align: center;
    color: var(--ai-text-muted);
    font-size: 13px;
}

.conversation-list {
    list-style: none;
    margin: 0;
    padding: 0 6px;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.conversation-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 14px;
    border-radius: 10px;
    font-size: 13px;
    color: var(--ai-text);
    cursor: pointer;
    overflow: hidden;
    border: 1px solid transparent;
    transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;

    &:hover {
        background: color-mix(in srgb, var(--ai-primary) 8%, transparent);
        border-color: color-mix(in srgb, var(--ai-primary) 15%, transparent);
    }

    &.active {
        background: color-mix(in srgb, var(--ai-primary) 12%, transparent);
        border-color: color-mix(in srgb, var(--ai-primary) 25%, transparent);
        color: var(--ai-primary);
        box-shadow: 0 1px 4px color-mix(in srgb, var(--ai-primary) 15%, transparent);

        .conversation-label {
            font-weight: 500;
        }

        .conversation-actions .btn-item {
            color: var(--ai-primary);
        }
    }
}

.conversation-label {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    line-height: 1.4;
}

.conversation-actions {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 2px;
    opacity: 0;
    transition: opacity 0.2s ease;

    .conversation-item:hover &,
    .conversation-item.active & {
        opacity: 1;
    }
}

.btn-item {
    padding: 5px 10px;
    font-size: 12px;
    border: none;
    background: transparent;
    color: var(--ai-text-secondary);
    cursor: pointer;
    border-radius: 6px;
    transition: color 0.2s ease, background 0.2s ease;

    &:hover {
        background: color-mix(in srgb, var(--ai-primary) 12%, transparent);
        color: var(--ai-primary);
    }

    &.btn-delete:hover {
        color: #e53935;
        background: color-mix(in srgb, #e53935 12%, transparent);
    }
}

.conversation-rename-input {
    flex: 1;
    min-width: 0;
    padding: 8px 12px;
    font-size: 13px;
    border: 1px solid var(--ai-primary);
    border-radius: 8px;
    outline: none;
    background: var(--ai-surface);
    color: var(--ai-text);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &:focus {
        box-shadow: 0 0 0 2px color-mix(in srgb, var(--ai-primary) 25%, transparent);
    }

    &::placeholder {
        color: var(--ai-text-muted);
    }
}

/* 主内容区 */
.ai-main {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    background: var(--ai-surface);
    min-width: 0;
    overflow: hidden;
}

.chat-area {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    padding: 24px;
}

.message-list {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
}

.welcome-tip {
    text-align: center;
    color: var(--ai-text-muted);
    padding: 60px 20px;
    font-size: 14px;
}

/* 新对话默认回复 */
.default-reply .default-reply-content {
    max-width: 720px;
}

.default-reply .welcome-msg {
    margin-bottom: 18px;
    line-height: 1.65;
    color: var(--ai-text);
}

.default-reply .welcome-msg.reply-bubble {
    margin-bottom: 18px;
}

.default-reply .section-title {
    margin-bottom: 14px;
    font-weight: 600;
    color: var(--ai-text);
}

.welcome-cards {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    gap: 20px;
}

.welcome-cards-left {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 14px;
}

/* 左侧推荐项上下排版 */
.welcome-cards-left .welcome-suggestions {
    flex-direction: column;
    flex-wrap: nowrap;
    align-items: flex-start;
    gap: 10px;
}

.welcome-cards-right {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.welcome-card {
    padding: 16px 18px;
    border-radius: 12px;
    background: color-mix(in srgb, var(--ai-primary) 6%, white);
    border: 1px solid color-mix(in srgb, var(--ai-primary) 12%, transparent);
    transition: background 0.2s ease, border-color 0.2s ease, transform 0.15s ease;

    &:hover {
        background: color-mix(in srgb, var(--ai-primary) 10%, white);
    }

    &.card-highlight {
        background: color-mix(in srgb, var(--ai-primary) 12%, white);
        border-color: color-mix(in srgb, var(--ai-primary) 20%, transparent);
    }
}

.welcome-card-title {
    font-size: 13px;
    color: var(--ai-text);
    margin-bottom: 10px;
    font-weight: 500;
}

.welcome-suggestions {
    margin: 0;
    padding: 0;
    list-style: none;
    display: flex;
    flex-wrap: wrap;
    gap: 8px 14px;

    &.has-images {
        gap: 16px 20px;
    }
}

.suggestion-item {
    display: flex;
}

.suggestion-with-image {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 8px 10px;
    border-radius: 8px;
    transition: background 0.2s ease;

    &:hover {
        background: color-mix(in srgb, var(--ai-primary) 10%, transparent);
    }

    .suggestion-img {
        width: 80px;
        height: 80px;
        object-fit: cover;
        border-radius: 8px;
        border: 1px solid color-mix(in srgb, var(--ai-primary) 15%, transparent);
    }

    .suggestion-tag {
        font-size: 12px;
        max-width: 100px;
        text-align: center;
    }
}

.suggestion-tag {
    font-size: 13px;
    color: var(--ai-primary);
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 6px;
    transition: background 0.2s ease, color 0.2s ease;

    &::before {
        content: '';
        display: inline-block;
        width: 6px;
        height: 6px;
        margin-right: 6px;
        vertical-align: 0.15em;
        border-radius: 50%;
        background-color: var(--ai-primary);
    }

    &:hover {
        background: color-mix(in srgb, var(--ai-primary) 12%, transparent);
        text-decoration: none;
    }
}

/* 右侧带图推荐项前的圆点稍大 */
.suggestion-with-image .suggestion-tag::before {
    width: 8px;
    height: 8px;
    margin-right: 8px;
    vertical-align: 0.12em;
}

.message-row {
    margin-bottom: 20px;
    animation: message-in 0.25s ease-out;

    &.user {
        display: flex;
        justify-content: flex-end;
    }

    &.ai {
        display: flex;
        justify-content: flex-start;
    }
}

@keyframes message-in {
    from {
        opacity: 0;
        transform: translateY(6px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.message-bubble {
    display: flex;
    gap: 12px;
    max-width: 85%;

    &.user {
        flex-direction: row-reverse;

        .text {
            background: color-mix(in srgb, var(--ai-primary) 10%, white);
            color: var(--ai-text);
            border-radius: 14px 14px 4px 14px;
            padding: 12px 16px;
            border: 1px solid color-mix(in srgb, var(--ai-primary) 15%, transparent);
            transition: box-shadow 0.2s ease;
        }

        .user-msg-image {
            max-width: 180px;
            max-height: 180px;
            border-radius: 14px 14px 4px 14px;
            overflow: hidden;
            border: 1px solid color-mix(in srgb, var(--ai-primary) 15%, transparent);
            object-fit: contain;
            display: block;
        }
    }

    &.ai .content {
        flex: 1;
        min-width: 0;
    }
}

.avatar {
    width: 38px;
    height: 38px;
    flex-shrink: 0;
    border-radius: 50%;
    transition: transform 0.2s ease;

    &.user {
        background: linear-gradient(135deg, var(--rgba4, var(--ai-primary)) 0%, var(--ai-primary) 100%);
        object-fit: cover;
    }

    &.ai {
        object-fit: cover;
    }
}

.reply-text {
    margin: 0 0 12px;
    font-size: 14px;
    color: var(--ai-text);
    line-height: 1.5;

    &.empty-tip {
        color: var(--ai-text-secondary);
    }

    /* AI 侧纯文案气泡样式（欢迎语、加载中、空结果提示等） */
    &.reply-bubble {
        display: inline-block;
        padding: 12px 16px;
        border-radius: 14px 14px 14px 4px;
        background: color-mix(in srgb, var(--ai-primary) 6%, white);
        border: 1px solid color-mix(in srgb, var(--ai-primary) 12%, transparent);
        margin-bottom: 12px;
        max-width: 100%;
        box-sizing: border-box;
    }
}

/* 发送中占位动态效果 */
.sending-placeholder .typing-dots {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    margin-left: 2px;
}

.sending-placeholder .typing-dots .dot {
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: var(--ai-primary);
    animation: typing-bounce 1.4s ease-in-out infinite both;
}

.sending-placeholder .typing-dots .dot:nth-child(1) {
    animation-delay: 0s;
}

.sending-placeholder .typing-dots .dot:nth-child(2) {
    animation-delay: 0.2s;
}

.sending-placeholder .typing-dots .dot:nth-child(3) {
    animation-delay: 0.4s;
}

/* 消息行内 AI 加载态复用同一套打字点动画 */
.msg-loading .typing-dots {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    margin-left: 2px;
}

.msg-loading .typing-dots .dot {
    width: 4px;
    height: 4px;
    border-radius: 50%;
    background: var(--ai-primary);
    animation: typing-bounce 1.4s ease-in-out infinite both;
}

.msg-loading .typing-dots .dot:nth-child(1) {
    animation-delay: 0s;
}

.msg-loading .typing-dots .dot:nth-child(2) {
    animation-delay: 0.2s;
}

.msg-loading .typing-dots .dot:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes typing-bounce {

    0%,
    60%,
    100% {
        transform: translateY(0);
        opacity: 0.4;
    }

    30% {
        transform: translateY(-4px);
        opacity: 1;
    }
}

.goods-cards {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    margin-bottom: 10px;
}

.goods-card {
    width: 144px;
    cursor: pointer;
    border: 1px solid var(--ai-border);
    border-radius: 10px;
    overflow: hidden;
    transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.15s ease;
    background: var(--ai-surface);

    &:hover {
        border-color: color-mix(in srgb, var(--ai-primary) 30%, transparent);
        box-shadow: 0 4px 12px color-mix(in srgb, var(--ai-primary) 12%, transparent);
        transform: translateY(-2px);
    }
}

.goods-img-wrap {
    width: 100%;
    height: 144px;
    background: var(--ai-bg);
    display: flex;
    align-items: center;
    justify-content: center;
}

.goods-img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.goods-info {
    padding: 10px;
}

.goods-name {
    font-size: 12px;
    color: var(--ai-text);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
}

.goods-price {
    font-size: 14px;
    color: var(--ai-primary);
    margin-top: 6px;
    font-weight: 500;
}

.link-more-sku {
    font-size: 13px;
    color: var(--ai-primary);
    margin-bottom: 12px;
    display: inline-block;
    padding: 4px 0;
    border-radius: 4px;
    transition: opacity 0.2s ease;

    &:hover {
        text-decoration: underline;
        opacity: 0.9;
    }
}

.spec-suggest {
    margin-top: 14px;
    font-size: 13px;
    color: var(--ai-text-secondary);

    .spec-tags-wrap {
        display: flex;
        flex-wrap: wrap;
        align-items: baseline;
        gap: 6px 8px;
    }

    &.is-collapsed .spec-tags-wrap {
        max-height: 8.2em;
        overflow: hidden;
    }

    .spec-expand-btn {
        display: inline-block;
        margin-top: 6px;
        font-size: 12px;
        color: var(--ai-primary);
        cursor: pointer;

        &:hover {
            text-decoration: underline;
        }
    }
}

.spec-label {
    margin-right: 2px;
}

.spec-tag {
    color: var(--ai-primary);
    cursor: pointer;
    padding: 2px 6px;
    border-radius: 4px;
    transition: background 0.2s ease;

    &:hover {
        text-decoration: underline;
        background: color-mix(in srgb, var(--ai-primary) 10%, transparent);
    }
}

/* 输入区 */
.input-area {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 24px;
    border-top: 1px solid var(--ai-border);
    background: var(--ai-surface);
}

.input-file-hidden {
    position: absolute;
    width: 0;
    height: 0;
    opacity: 0;
    pointer-events: none;
}

.input-icon.camera {
    font-size: 20px;
    cursor: pointer;
    opacity: 0.75;
    transition: opacity 0.2s ease;

    &:hover {
        opacity: 1;
    }
}

.input-field {
    flex: 1;
    height: 44px;
    padding: 0 16px;
    border: 1px solid var(--ai-border);
    border-radius: 10px;
    font-size: 14px;
    outline: none;
    background: var(--ai-surface);
    color: var(--ai-text);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;

    &::placeholder {
        color: var(--ai-text-muted);
    }

    &:focus {
        border-color: var(--ai-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--ai-primary) 18%, transparent);
    }
}

.btn-send {
    width: 44px;
    height: 44px;
    border: none;
    border-radius: 10px;
    background: linear-gradient(135deg, var(--rgba4, var(--ai-primary)) 0%, var(--ai-primary) 100%);
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.15s ease, box-shadow 0.2s ease, opacity 0.2s ease;
    box-shadow: 0 2px 8px color-mix(in srgb, var(--ai-primary) 30%, transparent);
    outline: none;

    &:hover:not(:disabled) {
        opacity: 0.95;
        transform: scale(1.02);
        box-shadow: 0 4px 12px color-mix(in srgb, var(--ai-primary) 40%, transparent);
    }

    &:active:not(:disabled) {
        transform: scale(0.98);
    }

    &:focus-visible {
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--ai-primary) 40%, transparent);
    }

    &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        transform: none;
    }
}

.send-icon {
    font-size: 18px;
}

/* 底部版权 */
.ai-footer {
    flex-shrink: 0;
    padding: 14px 24px;
    text-align: center;
    background: var(--ai-surface);
    border-top: 1px solid var(--ai-border);
}

.copyright {
    font-size: 12px;
    color: var(--ai-text-muted);
    margin: 0 0 4px;

    .icp-info {
        margin: 0 4px;
    }
}

.disclaimer {
    font-size: 11px;
    color: var(--ai-text-muted);
    margin: 0;
    opacity: 0.85;
}

/* Swiss 蓝白 AI 导购：清晰网格、单一 Yves Klein Blue 强调、无装饰阴影。 */
.ai-assistant-page {
    --ai-bg: #f7f7f8;
    --ai-surface: #ffffff;
    --ai-border: #e4e8f0;
    --ai-text: #172033;
    --ai-text-secondary: #5f6b80;
    --ai-text-muted: #8b95a7;
    --ai-primary: #3f73f6;
    --ai-primary-hover: #2f61df;
    color: var(--ai-text);
    background: var(--ai-bg);
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
}

.ai-sidebar {
    width: 272px;
    padding: 0;
    box-shadow: none;
}

.sidebar-logo {
    height: 72px;
    margin: 0;
    padding: 0 22px;
    justify-content: flex-start;
    gap: 12px;
    border-bottom: 1px solid var(--ai-border);
    border-radius: 0;
}

.sidebar-logo:hover {
    background: #f7faff;
}

.sidebar-logo-img {
    width: auto;
    max-width: 120px;
    max-height: 34px;
}

.sidebar-brand-copy {
    color: var(--ai-text);
    font-size: 15px;
    font-weight: 600;
    white-space: nowrap;
}

.btn-new-chat {
    height: 44px;
    margin: 18px 18px 14px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: var(--ai-primary);
    border: 1px solid var(--ai-primary);
    border-radius: 0;
    box-shadow: none;
}

.btn-new-chat:hover {
    background: var(--ai-primary-hover);
    border-color: var(--ai-primary-hover);
    box-shadow: none;
    transform: none;
}

.conversation-list-wrap {
    padding: 0;
    border-top: 1px solid var(--ai-border);
}

.list-loading,
.list-empty {
    padding: 28px 18px;
}

.conversation-list {
    gap: 0;
    padding: 0;
}

.conversation-item {
    min-height: 54px;
    padding: 10px 18px;
    border: 0;
    border-bottom: 1px solid var(--ai-border);
    border-radius: 0;
}

.conversation-item:hover,
.conversation-item.active {
    background: #edf3ff;
    border-color: var(--ai-border);
    box-shadow: none;
}

.conversation-item.active {
    color: var(--ai-primary-hover);
    box-shadow: inset 3px 0 0 var(--ai-primary);
}

.btn-item,
.conversation-rename-input {
    border-radius: 0;
}

.ai-main-header {
    height: 72px;
    flex: 0 0 72px;
    padding: 0 28px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--ai-border);
    box-sizing: border-box;
}

.ai-main-header > div {
    display: flex;
    align-items: baseline;
    gap: 14px;
}

.ai-main-header span {
    color: var(--ai-primary-hover);
    font-size: 12px;
    font-weight: 600;
}

.ai-main-header strong {
    font-size: 17px;
}

.ai-main-header button {
    padding: 8px 12px;
    display: flex;
    align-items: center;
    gap: 7px;
    color: var(--ai-text-secondary);
    background: #fff;
    border: 1px solid var(--ai-border);
    border-radius: 0;
    font-size: 13px;
}

.ai-main-header button:hover {
    color: var(--ai-primary-hover);
    border-color: #b9cbf8;
}

.chat-area {
    padding: 30px clamp(28px, 5vw, 76px);
    background: var(--ai-bg);
}

.message-list,
.default-reply {
    max-width: 1120px;
    margin: 0 auto;
}

.default-reply .default-reply-content {
    max-width: 900px;
}

.welcome-cards {
    display: grid;
    grid-template-columns: minmax(260px, .8fr) minmax(440px, 1.2fr);
    gap: 18px;
}

.welcome-card {
    padding: 18px 20px;
    background: #fff;
    border: 1px solid var(--ai-border);
    border-radius: 0;
}

.welcome-card:hover {
    background: #f7faff;
}

.welcome-card.card-highlight {
    background: #f1f5ff;
    border-color: #cfdbf7;
}

.suggestion-with-image,
.suggestion-tag {
    border-radius: 0;
}

.suggestion-with-image .suggestion-img {
    width: 92px;
    height: 92px;
    border-radius: 0;
}

.message-bubble.user .text,
.message-bubble.user .user-msg-image,
/* 大模型未接入提示条 */
.ai-not-ready {
    margin: 0 0 8px;
    padding: 4px 10px;
    font-size: 12px;
    color: #a2650a;
    background: #fdf3e2;
    border: 1px solid #f6e2c0;
    border-radius: 4px;
    display: inline-block;
}

/* 由哪个模型生成 */
.ai-model-tag {
    margin: -4px 0 12px;
    font-size: 12px;
    color: var(--ai-text-secondary);
}

.reply-text.reply-bubble {
    border-radius: 0;
}

.message-bubble.user .text {
    background: #dfe9ff;
    border-color: #cfdbf7;
}

.reply-text.reply-bubble {
    background: #fff;
    border-color: var(--ai-border);
}

.goods-card {
    width: 168px;
    background: #fff;
    border-radius: 0;
    box-shadow: none;
}

.goods-card:hover {
    border-color: #b9cbf8;
    box-shadow: none;
    transform: translateY(-2px);
}

.goods-img-wrap {
    height: 168px;
    border-bottom: 1px solid var(--ai-border);
}

.goods-price {
    color: #e4002b;
}

.input-area {
    padding: 14px clamp(28px, 5vw, 76px);
}

.input-icon.camera {
    width: 44px;
    height: 44px;
    padding: 0;
    display: grid;
    place-items: center;
    color: var(--ai-primary-hover);
    background: #fff;
    border: 1px solid var(--ai-border);
    border-radius: 0;
    opacity: 1;
}

.input-icon.camera:hover {
    border-color: #b9cbf8;
}

.input-field,
.btn-send {
    border-radius: 0;
    box-shadow: none;
}

.input-field:focus {
    border-color: var(--ai-primary);
    box-shadow: inset 0 -2px 0 var(--ai-primary);
}

.btn-send {
    background: var(--ai-primary);
}

.btn-send:hover:not(:disabled) {
    background: var(--ai-primary-hover);
    box-shadow: none;
    transform: none;
}

.ai-footer {
    min-height: 42px;
    padding: 8px 24px;
    box-sizing: border-box;
}

@media (max-width: 960px) {
    .ai-sidebar {
        width: 230px;
    }

    .welcome-cards {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 700px) {
    .ai-sidebar {
        width: 84px;
    }

    .sidebar-logo {
        justify-content: center;
        padding: 0 10px;
    }

    .sidebar-logo-img {
        max-width: 62px;
    }

    .sidebar-brand-copy,
    .conversation-list-wrap {
        display: none;
    }

    .btn-new-chat {
        width: 52px;
        margin: 16px;
        padding: 0;
        font-size: 0;
    }

    .btn-new-chat :deep(svg) {
        width: 18px;
        height: 18px;
    }

    .ai-main-header {
        padding: 0 18px;
    }

    .ai-main-header span,
    .ai-main-header button {
        display: none;
    }

    .chat-area,
    .input-area {
        padding-left: 18px;
        padding-right: 18px;
    }
}
</style>
