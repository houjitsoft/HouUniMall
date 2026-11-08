<template>
    <div class="article-page">
        <div class="article-container">
            <!-- 左侧导航栏 -->
            <div class="sidebar">
                <!-- 一级导航：文章分类 -->
                <div class="nav-section primary-nav">
                    <h3 class="nav-title">{{ $t('文章分类') }}</h3>
                    <div class="nav-grid">
                        <div v-for="item in primaryNavList" :key="item.articleGroupId" class="nav-item"
                            :class="{ active: activePrimaryId === item.articleGroupId }"
                            @click="item.articleGroupId && handlePrimaryNavClick(item.articleGroupId)">
                            {{ $t(item.articleGroupTitle || '') }}
                        </div>
                    </div>
                </div>

                <!-- 二级导航：最新文章 -->
                <div class="nav-section secondary-nav">
                    <h3 class="nav-title">{{ $t('最新文章') }}</h3>
                    <div v-if="loadingSecondary" class="nav-loading">
                        <div class="loading-text">{{ $t('加载中...') }}</div>
                    </div>
                    <div v-else class="nav-list">
                        <div v-if="secondaryNavList.length === 0" class="nav-empty">{{ $t('暂无文章') }}</div>
                        <div v-else v-for="item in secondaryNavList" :key="item.articleId" class="nav-item"
                            :class="{ active: activeArticleId === item.articleId }"
                            @click="handleSecondaryNavClick(item)">
                            {{ $t(item.articleTitle || '') }}
                        </div>
                    </div>
                </div>
            </div>

            <!-- 右侧主内容区 -->
            <div class="content-area">
                <Transition name="fade" mode="out-in">
                    <Loading v-if="loading" key="loading" :text="$t('加载中...')" />
                    <div v-else-if="error" key="error" class="state-container error">
                        <div class="error-icon">⚠️</div>
                        <div class="state-text">{{ $t(error || '') }}</div>
                        <button class="retry-btn" @click="handleRetry">{{ $t('重试') }}</button>
                    </div>
                    <div v-else-if="currentArticle" key="article" class="article-content">
                        <h1 class="article-title">{{ $t(currentArticle.articleTitle || '') }}</h1>
                        <div class="article-meta">
                            <span class="meta-item">
                                <span class="meta-label">{{ $t('分类') }}：</span>
                                <span class="meta-value">{{ $t(currentArticle.articleGroupTitle || '') }}</span>
                            </span>
                            <span class="meta-item" v-if="currentArticle.articleAddTime">
                                <span class="meta-label">{{ $t('发布时间') }}：</span>
                                <span class="meta-value">{{ formatDate(currentArticle.articleAddTime) }}</span>
                            </span>
                        </div>
                        <div class="article-body" v-html="$t(currentArticle.articleDesc || '')"></div>
                    </div>
                    <EmptyState v-else key="empty" :text="$t('暂无文章内容')" />
                </Transition>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { getHelpInfo, queryArticleGroupDetail } from '~/services/base.service'
import { useArticleStore } from '~/stores'
import type { BaseArticleGroupEntity, BaseArticleBaseEntity, HelpInfoArticleGroup } from '~/types/base'

// 获取文章 store
const articleStore = useArticleStore()
const { applyBackendTheme } = useTheme()

// 数据状态
const primaryNavList = ref<BaseArticleGroupEntity[]>([])
const secondaryNavList = ref<BaseArticleBaseEntity[]>([])
const currentArticle = ref<BaseArticleBaseEntity | null>(null)
const activePrimaryId = ref<number | null>(null)
const activeArticleId = ref<number | null>(null)
const loading = ref(false)
const loadingSecondary = ref(false) // 二级导航加载状态
const error = ref<string | null>(null)

// 防抖处理
let loadingTimer: ReturnType<typeof setTimeout> | null = null

// 二级导航数据缓存（key: groupId, value: 文章列表）
const secondaryNavCache = new Map<number, BaseArticleBaseEntity[]>()
// 分类标题缓存（key: groupId, value: 分类名）
const groupTitleCache = new Map<number, string>()

const applyThemeFromBackend = (color?: string) => {
    if (!color) return
    applyBackendTheme({
        primary: color,
        background: '#ffffff',
        text: '#303133'
    })
}

const filterEnabledArticles = (articles: BaseArticleBaseEntity[] = []) =>
    articles.filter(item => item.articleStatus === 1)

const cacheHelpGroups = (groups: HelpInfoArticleGroup[] = []) => {
    groups.forEach((group) => {
        const groupId = group.articleGroupId
        if (!groupId) return
        if (group.articleGroupTitle) {
            groupTitleCache.set(groupId, group.articleGroupTitle)
        }
        const articles = filterEnabledArticles(group.articletitle2 || [])
        secondaryNavCache.set(groupId, articles)
    })
}

const refreshHelpInfoCache = async () => {
    const response = await getHelpInfo()
    if (response.code === 1 && response.data?.articleGroups) {
        primaryNavList.value = response.data.articleGroups
        secondaryNavCache.clear()
        groupTitleCache.clear()
        cacheHelpGroups(response.data.articleGroups)
        applyThemeFromBackend(response.data.webColor)
        return true
    }
    return false
}

// 设置激活的一级导航并同步到 store
const setActivePrimaryId = (groupId: number | null) => {
    activePrimaryId.value = groupId
    articleStore.setArticleGroupId(groupId)
}

// 设置当前文章（提取公共逻辑）
const setCurrentArticle = (article: BaseArticleBaseEntity | null) => {
    currentArticle.value = article
    activeArticleId.value = article?.articleId || null
    // 内容加载后滚动到顶部
    if (article) {
        nextTick(() => {
            window.scrollTo({ top: 0, behavior: 'smooth' })
        })
    }
}

// 处理加载错误（提取公共逻辑）
const handleLoadError = (errorMsg: string) => {
    error.value = errorMsg
    secondaryNavList.value = []
    setCurrentArticle(null)
}

// 加载一级导航数据
const loadPrimaryNav = async () => {
    try {
        loading.value = true
        error.value = null
        const response = await getHelpInfo()

        if (response.code === 1 && response.data?.articleGroups) {
            const groups = response.data.articleGroups
            primaryNavList.value = groups
            cacheHelpGroups(groups)
            applyThemeFromBackend(response.data.webColor)

            const storedGroupId = articleStore.getArticleGroupId
            if (storedGroupId && groups.some(item => item.articleGroupId === storedGroupId)) {
                setActivePrimaryId(storedGroupId)
            } else if (groups.length > 0) {
                setActivePrimaryId(groups[0]?.articleGroupId || null)
            }
        } else {
            error.value = response.message || '加载文章分类失败'
        }
    } catch (err: any) {
        error.value = err.message || '加载文章分类失败'
        console.error('加载一级导航失败:', err)
    } finally {
        loading.value = false
    }
}

// 根据 articleId 选择文章并加载详情
const setCurrentArticleById = async (articles: BaseArticleBaseEntity[]) => {
    if (articles.length === 0) {
        setCurrentArticle(null)
        return
    }

    const storedArticleId = articleStore.getArticleId
    const targetArticle = storedArticleId
        ? articles.find(item => item.articleId === storedArticleId)
        : null

    const articleId = targetArticle?.articleId || articles[0]?.articleId
    if (storedArticleId) {
        articleStore.clearArticleId()
    }
    if (articleId) {
        await loadArticleDetail(articleId)
    } else {
        setCurrentArticle(null)
    }
}

// 加载文章详情
const loadArticleDetail = async (articleId: number) => {
    try {
        loading.value = true
        error.value = null
        const response = await queryArticleGroupDetail(articleId)

        if (response.code === 1 && response.data) {
            const groupTitle = response.data.articleGroupId
                ? groupTitleCache.get(response.data.articleGroupId)
                : undefined
            setCurrentArticle({
                ...response.data,
                articleGroupTitle: response.data.articleGroupTitle || groupTitle
            })
        } else {
            handleLoadError(response.message || '加载文章内容失败')
        }
    } catch (err: any) {
        handleLoadError(err.message || '加载文章内容失败')
        console.error('加载文章详情失败:', err)
    } finally {
        loading.value = false
    }
}

// 加载二级导航数据
const loadSecondaryNav = async (groupId: number, immediate = false, forceRefresh = false) => {
    if (forceRefresh) {
        const refreshed = await refreshHelpInfoCache()
        if (!refreshed) {
            handleLoadError('加载文章列表失败')
            return
        }
    }

    if (!secondaryNavCache.has(groupId)) {
        handleLoadError('加载文章列表失败')
        return
    }

    if (loadingTimer) {
        clearTimeout(loadingTimer)
        loadingTimer = null
    }

    return new Promise<void>((resolve) => {
        loadingTimer = setTimeout(async () => {
            try {
                loadingSecondary.value = true
                error.value = null
                secondaryNavList.value = secondaryNavCache.get(groupId) || []
                await setCurrentArticleById(secondaryNavList.value)
            } catch (err: any) {
                handleLoadError(err.message || '加载文章列表失败')
                console.error('加载二级导航失败:', err)
            } finally {
                loadingSecondary.value = false
                loadingTimer = null
                resolve()
            }
        }, immediate ? 0 : 100)
    })
}

// 处理一级导航点击
const handlePrimaryNavClick = async (groupId: number) => {
    // 防止重复点击
    if (activePrimaryId.value === groupId || loadingSecondary.value) return
    setActivePrimaryId(groupId)
    // 清除 articleId，因为切换分类后应该显示该分类的第一篇文章
    articleStore.clearArticleId()
    await loadSecondaryNav(groupId, false)
}

// 处理二级导航点击
const handleSecondaryNavClick = async (article: BaseArticleBaseEntity) => {
    if (activeArticleId.value === article.articleId || !article.articleId) return
    await loadArticleDetail(article.articleId)
}

// 处理重试（强制刷新）
const handleRetry = async () => {
    if (activeArticleId.value) {
        await loadArticleDetail(activeArticleId.value)
        return
    }
    if (activePrimaryId.value) {
        await loadSecondaryNav(activePrimaryId.value, true, true)
        return
    }
    await loadPrimaryNav()
}

// 格式化日期
const formatDate = (dateStr?: string) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    })
}

// 监听 store 中的 articleGroupId 变化
watch(() => articleStore.getArticleGroupId, async (newId, oldId) => {
    if (newId && newId !== oldId && newId !== activePrimaryId.value) {
        setActivePrimaryId(newId)
        await loadSecondaryNav(newId, true)
    }
}, { immediate: false })

// 初始化
onMounted(async () => {
    await loadPrimaryNav()
    if (activePrimaryId.value) {
        await loadSecondaryNav(activePrimaryId.value, true)
    }
})

// 组件卸载时清理定时器和缓存
onBeforeUnmount(() => {
    if (loadingTimer) {
        clearTimeout(loadingTimer)
        loadingTimer = null
    }
    // 可选：清理缓存（如果需要的话，可以保留缓存以便下次访问更快）
    // secondaryNavCache.clear()
})
</script>

<style lang="scss" scoped>
.article-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0;
}

.article-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    gap: 20px;
    align-items: flex-start;
}

// 左侧导航栏
.sidebar {
    width: 240px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.nav-section {
    background-color: var(--color-background, #ffffff);
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.nav-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text, #333);
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #f0f0f0;
}

// 导航项公共样式
.nav-item {
    padding: 12px 16px;
    background-color: #f8f9fa;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 14px;
    color: #666;
    border: 1px solid transparent;

    &:hover {
        background-color: #e9ecef;
        color: var(--color-text, #333);
    }

    &.active {
        background-color: var(--color);
        color: white;
        border-color: var(--color);
    }
}

// 一级导航网格布局
.primary-nav {
    .nav-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 10px;
    }

    .nav-item {
        text-align: center;

        &.disabled {
            opacity: 0.6;
            cursor: not-allowed;
            pointer-events: none;
        }
    }
}

// 二级导航列表布局
.secondary-nav {
    .nav-loading {
        padding: 20px;
        text-align: center;

        .loading-text {
            font-size: 14px;
            color: #999;
        }
    }

    .nav-empty {
        padding: 20px;
        text-align: center;
        color: #999;
        font-size: 14px;
    }

    .nav-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    .nav-item {
        text-align: left;
    }
}

// 右侧内容区
.content-area {
    flex: 1;
    min-width: 0;
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

.article-content {
    background-color: var(--color-background, #ffffff);
    border-radius: 8px;
    padding: 40px;
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

.article-title {
    font-size: 28px;
    font-weight: 600;
    color: var(--color-text, #333);
    margin-bottom: 20px;
    line-height: 1.5;
}

.article-meta {
    display: flex;
    align-items: center;
    gap: 30px;
    padding-bottom: 20px;
    margin-bottom: 30px;
    border-bottom: 1px solid #f0f0f0;
    flex-wrap: wrap;
}

.meta-item {
    font-size: 14px;
    color: #666;

    .meta-label {
        color: #999;
    }

    .meta-value {
        color: var(--color-text, #333);
        margin-left: 5px;
    }
}

.article-body {
    font-size: 16px;
    line-height: 1.8;
    color: var(--color-text, #333);

    :deep(p) {
        margin-bottom: 20px;
    }

    :deep(ul),
    :deep(ol) {
        margin-bottom: 20px;
        padding-left: 30px;
    }

    :deep(li) {
        margin-bottom: 10px;
    }

    :deep(img) {
        max-width: 100%;
        height: auto;
        border-radius: 8px;
        margin: 20px 0;
    }

    :deep(h1),
    :deep(h2),
    :deep(h3),
    :deep(h4),
    :deep(h5),
    :deep(h6) {
        font-weight: 600;
        color: var(--color-text, #333);
        margin-top: 30px;
        margin-bottom: 15px;
    }

    :deep(h1) {
        font-size: 24px;
    }

    :deep(h2) {
        font-size: 22px;
    }

    :deep(h3) {
        font-size: 20px;
    }

    :deep(blockquote) {
        border-left: 4px solid #e0e0e0;
        padding-left: 20px;
        margin: 20px 0;
        color: #666;
        font-style: italic;
        background-color: #f8f9fa;
        border-radius: 4px;
    }

    :deep(a) {
        color: var(--color-text, #333);
        text-decoration: none;
        transition: color 0.3s ease;

        &:hover {
            text-decoration: underline;
            color: #666;
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
        color: var(--color-text, #333);
    }

    :deep(code) {
        background-color: #f5f5f5;
        padding: 2px 6px;
        border-radius: 3px;
        font-family: 'Courier New', monospace;
        font-size: 0.9em;
        color: var(--color-text, #333);
    }

    :deep(pre) {
        background-color: #f5f5f5;
        padding: 15px;
        border-radius: 4px;
        overflow-x: auto;
        margin: 20px 0;

        code {
            background-color: transparent;
            padding: 0;
        }
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
    .article-container {
        flex-direction: column;
        padding: 0 15px;
        gap: 15px;
    }

    .sidebar {
        width: 100%;
    }

    .nav-section {
        padding: 15px;
    }

    .primary-nav .nav-grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;
    }

    .primary-nav .nav-item,
    .secondary-nav .nav-item {
        padding: 10px 12px;
        font-size: 13px;
    }

    .article-content {
        padding: 20px;
    }

    .article-title {
        font-size: 22px;
    }

    .article-body {
        font-size: 14px;
        line-height: 1.6;
    }

    .state-container {
        padding: 40px 20px;
        min-height: 300px;
    }
}
</style>
