<template>
    <div class="agreement-page">
        <div class="agreement-container">
            <Transition name="fade" mode="out-in">
                <Loading v-if="loading" key="loading" :text="$t('加载中...')" />
                <div v-else-if="error" key="error" class="state-container error">
                    <div class="error-icon">⚠️</div>
                    <div class="state-text">{{ $t(error || '') }}</div>
                    <button class="retry-btn" @click="loadAgreement">{{ $t('重试') }}</button>
                </div>
                <div v-else-if="agreementContent" key="content" class="agreement-content">
                    <h1 class="agreement-title">{{ $t(agreementTitle || '') }}</h1>
                    <div class="agreement-body" v-html="$t(agreementContent || '')"></div>
                </div>
                <EmptyState v-else key="empty" :text="$t('协议内容不存在')" />
            </Transition>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { useRoute } from 'vue-router'
import { queryMemberRuleAgreement } from '~/services/business.service'
import { queryMemberAuthorizationAgreement } from '~/services/base.service'

/** type=1 会员规则与隐私协议，type=2 会员授权协议 */
const STORE_AGREEMENT_CONFIG: Record<string, { title: string }> = {
    '1': { title: '会员规则与隐私协议' },
    '2': { title: '会员授权协议' }
}

const route = useRoute()

/** 店铺 ID（来自路由 params.id） */
const shopId = computed(() => {
    const id = route.params.id
    if (typeof id === 'string') {
        const parsed = parseInt(id, 10)
        return isNaN(parsed) ? 0 : parsed
    }
    if (Array.isArray(id) && id[0] && typeof id[0] === 'string') {
        const parsed = parseInt(id[0], 10)
        return isNaN(parsed) ? 0 : parsed
    }
    return 0
})

/** 协议类型（来自 query.type）：1-会员规则与隐私协议，2-会员授权协议 */
const agreementType = computed(() => String(route.query.type || '1'))

/** 当前协议标题 */
const agreementTitle = computed(() => STORE_AGREEMENT_CONFIG[agreementType.value]?.title || '协议')

const loading = ref(true)
const error = ref<string | null>(null)
const agreementContent = ref('')

/** 加载协议内容 */
async function loadAgreement() {
    if (!shopId.value) {
        loading.value = false
        error.value = '缺少店铺ID'
        return
    }

    const config = STORE_AGREEMENT_CONFIG[agreementType.value]
    if (!config) {
        loading.value = false
        error.value = '无效的协议类型'
        return
    }

    loading.value = true
    error.value = null
    agreementContent.value = ''

    try {
        const res = agreementType.value === '1'
            ? await queryMemberRuleAgreement({ shopId: shopId.value })
            : await queryMemberAuthorizationAgreement({ shopId: shopId.value })

        if (res?.code === 1 && res?.data != null) {
            const raw = res.data as string | Array<{ configValue?: string }> | undefined
            const html = Array.isArray(raw) && raw[0]?.configValue != null
                ? String(raw[0].configValue)
                : typeof raw === 'string'
                    ? raw
                    : ''
            agreementContent.value = html
            nextTick(() => {
                window.scrollTo({ top: 0, behavior: 'smooth' })
            })
        } else {
            agreementContent.value = ''
            error.value = res?.message || '协议内容不存在'
        }
    } catch (err: unknown) {
        error.value = err instanceof Error ? err.message : '加载协议失败'
        console.error('加载店铺协议失败:', err)
    } finally {
        loading.value = false
    }
}

useHead({
    title: computed(() => agreementTitle.value),
    meta: [
        { name: 'description', content: computed(() => agreementTitle.value) }
    ]
})

onMounted(() => {
    loadAgreement()
})
</script>

<style lang="scss" scoped>
.agreement-page {
    min-height: calc(100vh - 200px);
    background-color: var(--color-background, #f5f5f5);
    padding: 30px 20px;
    position: relative;
}

.agreement-container {
    max-width: 900px;
    margin: 0 auto;
    width: 100%;
}

.state-container {
    background-color: var(--color-background, #ffffff);
    border-radius: 8px;
    padding: 60px 40px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
}

.state-text {
    font-size: 16px;
    color: var(--color-text, #666);
    margin-top: 15px;
}

.error .error-icon {
    font-size: 48px;
    margin-bottom: 10px;
}

.error .state-text {
    color: var(--color-error, #f56c6c);
    margin-bottom: 20px;
}

.retry-btn {
    padding: 10px 24px;
    background-color: var(--color);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    margin-top: 10px;

    &:hover {
        opacity: 0.9;
    }
}

.agreement-content {
    background-color: var(--color-background, #ffffff);
    border-radius: 8px;
    padding: 40px 50px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.agreement-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--color-text, #333);
    text-align: center;
    margin-bottom: 40px;
    padding-bottom: 20px;
    border-bottom: 2px solid #f0f0f0;
    line-height: 1.4;
}

.agreement-body {
    color: var(--color-text, #333);
    line-height: 1.8;
    font-size: 15px;

    :deep(p) {
        margin-bottom: 16px;
        text-align: justify;
    }

    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4),
    :deep(h5),
    :deep(h6) {
        font-weight: 600;
        color: var(--color-text, #333);
        margin-top: 32px;
        margin-bottom: 16px;
        line-height: 1.4;
    }

    :deep(h2) {
        font-size: 22px;
        padding-left: 12px;
        border-left: 4px solid var(--color, #0a8cf0);
    }

    :deep(h3) {
        font-size: 20px;
    }

    :deep(h4) {
        font-size: 18px;
    }

    :deep(ol),
    :deep(ul) {
        margin-bottom: 16px;
        padding-left: 32px;
    }

    :deep(li) {
        margin-bottom: 12px;
        line-height: 1.8;
    }

    :deep(strong) {
        font-weight: 600;
        color: var(--color-text, #333);
    }

    :deep(a) {
        color: var(--color);
        text-decoration: none;
    }

    :deep(table) {
        width: 100%;
        border-collapse: collapse;
        margin: 20px 0;
    }

    :deep(th),
    :deep(td) {
        border: 1px solid #e0e0e0;
        padding: 12px;
        text-align: left;
    }

    :deep(th) {
        background-color: #f8f9fa;
        font-weight: 600;
    }
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

@media (max-width: 768px) {
    .agreement-page {
        padding: 20px 15px;
    }

    .state-container {
        padding: 40px 20px;
        min-height: 300px;
    }

    .agreement-content {
        padding: 24px 20px;
    }

    .agreement-title {
        font-size: 22px;
        margin-bottom: 30px;
        padding-bottom: 15px;
    }

    .agreement-body {
        font-size: 14px;
    }
}
</style>
