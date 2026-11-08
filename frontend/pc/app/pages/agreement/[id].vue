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
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { useRoute } from 'vue-router'
import { useMallStore } from '~/stores'
import { queryNavLogo } from '~/services/base.service'

// 协议配置映射
const AGREEMENT_CONFIG: Record<number, { key: string; title: string }> = {
    1: { key: 'logo_copywriting_ico_serviceAgre_toB', title: '企业注册协议' },
    2: { key: 'logo_copywriting_ico_privacyPoli', title: '隐私协议' },
    3: { key: 'logo_copywriting_ico_serviceAgre', title: '用户注册协议' }
}

const route = useRoute()
const mallStore = useMallStore()

// 解析路由 ID
const id = computed(() => {
    const routeId = route.params.id
    if (typeof routeId === 'string') {
        const parsed = parseInt(routeId, 10)
        return isNaN(parsed) ? 0 : parsed
    }
    if (Array.isArray(routeId) && routeId[0] && typeof routeId[0] === 'string') {
        const parsed = parseInt(routeId[0], 10)
        return isNaN(parsed) ? 0 : parsed
    }
    return 0
})

// 数据状态
const loading = ref(true) // 初始状态为加载中
const error = ref<string | null>(null)
const agreementContent = ref<string>('')

// 协议标题
const agreementTitle = computed(() => AGREEMENT_CONFIG[id.value]?.title || '协议')

// 加载配置数据
const loadConfigs = async (): Promise<boolean> => {
    try {
        // 如果 store 中已有数据，直接返回
        if (mallStore.getNavLogoConfigs.length > 0) {
            return true
        }

        // 否则调用接口加载配置数据
        const response = await queryNavLogo({
            configKey: '',
            configType: 'logo_copywriting'
        })

        if (response.code === 1 && response.data) {
            mallStore.setNavLogoConfigs(response.data)
            return true
        }
        return false
    } catch (err: any) {
        console.error('加载配置数据失败:', err)
        return false
    }
}

// 加载协议内容
const loadAgreement = async () => {
    const config = AGREEMENT_CONFIG[id.value]

    if (!config) {
        loading.value = false
        error.value = '无效的协议ID'
        return
    }

    loading.value = true
    error.value = null
    agreementContent.value = ''

    try {
        // 先加载配置数据
        const configLoaded = await loadConfigs()

        if (!configLoaded) {
            error.value = '加载配置数据失败，请稍后再试'
            return
        }

        // 然后从 store 中获取协议内容
        const content = mallStore.getConfigValueByKey(config.key)

        if (content) {
            agreementContent.value = content
            // 内容加载后滚动到顶部
            nextTick(() => {
                window.scrollTo({ top: 0, behavior: 'smooth' })
            })
        } else {
            // 只有在配置数据加载完成后，才显示"协议内容不存在"
            error.value = '协议内容不存在'
        }
    } catch (err: any) {
        error.value = err.message || '加载协议内容失败'
        console.error('加载协议内容失败:', err)
    } finally {
        loading.value = false
    }
}

// 页面元信息
useHead({
    title: computed(() => `${agreementTitle.value}`),
    meta: [
        {
            name: 'description',
            content: computed(() => `${agreementTitle.value}`)
        }
    ]
})

// 生命周期
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

// 状态容器公共样式
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

// 错误状态
.error {
    .error-icon {
        font-size: 48px;
        margin-bottom: 10px;
    }

    .state-text {
        color: var(--color-error, #f56c6c);
        margin-bottom: 20px;
    }
}

// 重试按钮
.retry-btn {
    padding: 10px 24px;
    background-color: var(--color);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.3s ease;
    margin-top: 10px;

    &:hover {
        background-color: var(--color-primary, #0971c7);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &:active {
        transform: translateY(0);
    }
}

// 协议内容
.agreement-content {
    background-color: var(--color-background, #ffffff);
    border-radius: 8px;
    padding: 40px 50px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
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
        transition: color 0.3s ease;
        border-bottom: 1px solid transparent;

        &:hover {
            color: var(--color-primary, #0971c7);
            border-bottom-color: var(--color-primary, #0971c7);
        }
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

    :deep(blockquote) {
        border-left: 4px solid #e0e0e0;
        padding-left: 20px;
        margin: 20px 0;
        color: #666;
        font-style: italic;
        background-color: #f8f9fa;
        border-radius: 4px;
        padding: 15px 20px;
    }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

// 响应式设计
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
        line-height: 1.7;

        :deep(h2) {
            font-size: 20px;
        }

        :deep(h3) {
            font-size: 18px;
        }

        :deep(ol),
        :deep(ul) {
            padding-left: 24px;
        }
    }
}
</style>
