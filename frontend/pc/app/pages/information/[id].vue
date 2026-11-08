<template>
    <div class="article-detail-page">
        <div class="article-container">
            <!-- 返回按钮 -->
            <div class="back-button" @click="goBack">
                <span>{{ $t('← 返回') }}</span>
            </div>

            <!-- 文章内容 -->
            <div class="article-content" v-if="article">
                <!-- 文章标题 -->
                <h1 class="article-title">{{ $t(article.informationTitle || '') }}</h1>

                <!-- 文章元信息 -->
                <div class="article-meta">
                    <span class="meta-item">
                        <span class="meta-label">{{ $t('发布时间') }}：</span>
                        <span class="meta-value">{{ article.releaseTime || article.createTime || '-' }}</span>
                    </span>
                    <span class="meta-item">
                        <span class="meta-label">{{ $t('浏览量') }}：</span>
                        <span class="meta-value">{{ article.pageView || 0 }}</span>
                    </span>
                    <span class="meta-item" v-if="article.classifyName">
                        <span class="meta-label">{{ $t('分类') }}：</span>
                        <span class="meta-value">{{ $t(article.classifyName || '') }}</span>
                    </span>
                </div>

                <!-- 文章图片 -->
                <div class="article-image"
                    v-if="article.informationCoverList && article.informationCoverList.length > 0">
                    <img v-for="(item) in article.informationCoverList" :key="item" :src="item"
                        :alt="article.informationTitle" />
                </div>

                <!-- 文章正文 -->
                <div class="article-body">
                    <div v-html="$t(article.informationDesc || '')"></div>
                </div>

                <!-- 上一篇和下一篇 -->
                <div class="article-navigation" v-if="previousArticle?.informationId || nextArticle?.informationId"
                    :class="{ 'only-one': isOnlyOneNavigation }">
                    <div class="nav-item prev-article" v-if="previousArticle?.informationId">
                        <div class="nav-label">{{ $t('上一篇') }}</div>
                        <div class="nav-content"
                            @click="viewArticle(previousArticle.informationId, previousArticle.classifyId)">
                            <div class="nav-title">{{ $t(previousArticle.informationTitle || '') }}</div>
                        </div>
                    </div>
                    <div class="nav-item next-article" v-if="nextArticle?.informationId">
                        <div class="nav-label">{{ $t('下一篇') }}</div>
                        <div class="nav-content"
                            @click="viewArticle(nextArticle.informationId, nextArticle.classifyId)">
                            <div class="nav-title">{{ $t(nextArticle.informationTitle || '') }}</div>
                        </div>
                    </div>
                </div>

                <!-- 推荐资讯 -->
                <div class="recommended-articles" v-if="recommendedArticles.length > 0">
                    <h3 class="recommended-title">{{ $t('推荐资讯') }}</h3>
                    <div class="recommended-list">
                        <div v-for="item in recommendedArticles" :key="item.informationId" class="recommended-item"
                            @click="viewArticle(item.informationId, item.classifyId)">
                            <div class="recommended-image">
                                <img :src="item.informationCover" :alt="item.informationTitle" />
                            </div>
                            <div class="recommended-info">
                                <h4 class="recommended-item-title">{{ $t(item.informationTitle || '') }}</h4>
                                <div class="recommended-meta">
                                    <span class="recommended-views">{{ $t('浏览量') }}: {{ item.pageView || 0 }}</span>
                                    <span class="recommended-date">{{ item.releaseTime || '-' }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 加载中状态 -->
            <Loading v-else-if="loading" :text="$t('加载中...')" />

            <!-- 错误状态 -->
            <div class="error" v-else-if="error">
                <p>{{ error }}</p>
                <button @click="loadArticle" class="retry-btn">{{ $t('重试') }}</button>
            </div>

            <!-- 空状态 -->
            <EmptyState v-else :text="$t('文章不存在')" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import type { InformationDetailData, PreviousArticle, NextArticle, BottomArticle } from '~/types/base'
import { queryInformationInfo, queryInformationInfoNextChapter } from '~/services/base.service'

// 设置页面 key，确保路由参数变化时组件重新创建
definePageMeta({
    key: (route) => `article-${route.params.id}`
})

// 获取路由参数
const route = useRoute()
const articleId = computed(() => route.params.id as string)

// 获取查询参数中的 classifyId
const classifyId = computed(() => route.query.classifyId as string | undefined)

// 文章数据
const article = ref<InformationDetailData | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

// 上一篇文章
const previousArticle = ref<PreviousArticle | null>(null)

// 下一篇文章
const nextArticle = ref<NextArticle | null>(null)

// 上一篇/下一篇是否为单个展示（仅显示一个时的样式优化）
const isOnlyOneNavigation = computed(() => {
    const hasPrev = !!previousArticle.value?.informationId
    const hasNext = !!nextArticle.value?.informationId
    return (hasPrev && !hasNext) || (!hasPrev && hasNext)
})

// 推荐资讯列表
const recommendedArticles = ref<BottomArticle[]>([])

// SEO + Open Graph（资讯摘要需纯文本，避免 HTML）
const stripHtml = (html: string) => {
    if (!html) return ''
    return html.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim().slice(0, 160)
}
const seoTitle = computed(() => article.value ? `${article.value.informationTitle} - 资讯中心` : '资讯详情')
const seoDescription = computed(() => article.value
    ? stripHtml(article.value.informationDesc) || article.value.informationTitle
    : '资讯详情')
useHead({
    title: seoTitle,
    meta: [
        { key: 'description', name: 'description', content: seoDescription }
    ]
})
useSeoMeta({
    description: seoDescription,
    ogTitle: seoTitle,
    ogDescription: seoDescription,
    ogImage: computed(() => article.value?.informationCover || undefined),
    ogType: 'article'
})

// 加载文章详情
const loadArticle = async () => {
    const id = articleId.value
    if (!id) {
        error.value = '文章ID不能为空'
        return
    }

    loading.value = true
    error.value = null
    article.value = null

    try {
        const response = await queryInformationInfo({ informationId: id })

        if (response?.data) {
            article.value = response.data
            await loadNextChapter()
        } else {
            error.value = '文章不存在'
        }
    } catch (err: any) {
        console.error('获取文章详情失败:', err)
        error.value = err.message || '加载文章失败，请稍后重试'
    } finally {
        loading.value = false
    }
}

// 加载上一篇、下一篇和推荐资讯
const loadNextChapter = async () => {
    if (!article.value) return

    const currentClassifyId = classifyId.value || article.value.classifyId
    if (!currentClassifyId) return

    try {
        const response = await queryInformationInfoNextChapter({
            classifyId: currentClassifyId,
            informationId: article.value.informationId
        })

        if (response?.data) {
            previousArticle.value = response.data.previous?.informationId ? response.data.previous : null
            nextArticle.value = response.data.next?.informationId ? response.data.next : null
            recommendedArticles.value = Array.isArray(response.data.bottom) ? response.data.bottom : []
        }
    } catch (err: any) {
        console.error('获取上一篇、下一篇和推荐资讯失败:', err)
        previousArticle.value = null
        nextArticle.value = null
        recommendedArticles.value = []
    }
}

// 返回上一页
const goBack = () => {
    if (window.history.length > 1) {
        window.history.back()
    } else {
        navigateTo('/information')
    }
}

// 查看文章详情
const viewArticle = (articleId: number, articleClassifyId?: number | string | null) => {
    const params = new URLSearchParams()
    if (articleClassifyId) {
        params.set('classifyId', String(articleClassifyId))
    }
    const queryString = params.toString() ? `?${params.toString()}` : ''
    navigateTo(`/information/${articleId}${queryString}`)
}

// 监听路由参数变化，重新加载文章
watch(() => route.params.id, (newId, oldId) => {
    if (newId && newId !== oldId) {
        loadArticle()
    }
})

onMounted(() => {
    loadArticle()
})
</script>

<style lang="scss" scoped>
.article-detail-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0;
}

.article-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.back-button {
    margin-bottom: 20px;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    color: #666;
    font-size: 14px;
    transition: color 0.3s ease;

    &:hover {
        color: var(--color);
    }

    span {
        margin-left: 5px;
    }
}

.article-content {
    background-color: white;
    border-radius: 8px;
    padding: 40px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.article-title {
    font-size: 28px;
    font-weight: 600;
    color: #333;
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

    .meta-item {
        font-size: 14px;
        color: #666;

        .meta-label {
            color: #999;
        }

        .meta-value {
            color: #333;
            margin-left: 5px;
        }
    }
}

.article-image {
    width: 100%;
    margin-bottom: 30px;
    border-radius: 8px;
    overflow: hidden;

    img {
        width: 100%;
        height: auto;
        display: block;
    }
}

.article-body {
    font-size: 16px;
    line-height: 1.8;
    color: #333;

    :deep(p) {
        margin-bottom: 20px;
    }

    :deep(ul),
    :deep(ol) {
        margin-bottom: 20px;
        padding-left: 30px;

        li {
            margin-bottom: 10px;
        }
    }

    :deep(h2) {
        font-size: 24px;
        font-weight: 600;
        margin: 30px 0 20px 0;
        color: #333;
    }

    :deep(h3) {
        font-size: 20px;
        font-weight: 600;
        margin: 25px 0 15px 0;
        color: #333;
    }

    :deep(img) {
        max-width: 100%;
        height: auto;
        border-radius: 8px;
        margin: 20px 0;
    }
}

// 错误状态容器
.error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    padding: 40px 20px;
    text-align: center;
    color: var(--color);
    font-size: 16px;

    .retry-btn {
        margin-top: 20px;
        padding: 10px 20px;
        background-color: var(--color);
        color: white;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        font-size: 14px;
        transition: background-color 0.3s ease;

        &:hover {
            background-color: color-mix(in srgb, var(--color) 90%, black);
        }
    }
}

// 上一篇和下一篇导航
.article-navigation {
    margin-top: 40px;
    padding-top: 30px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    gap: 20px;

    &.only-one {
        gap: 0;

        .nav-item {
            flex: 0 0 100%;
        }

        // 仅展示单条导航时，左右不对称的 text-align 会显得“偏”，统一居中更自然
        .nav-item.prev-article .nav-content,
        .nav-item.next-article .nav-content {
            text-align: center;
        }
    }

    .nav-item {
        flex: 1;

        .nav-label {
            font-size: 14px;
            color: #999;
            margin-bottom: 10px;
        }

        .nav-content {
            padding: 15px 20px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            background-color: #fff;
            cursor: pointer;
            transition: all 0.3s ease;

            &:hover {
                border-color: var(--color);
                background-color: color-mix(in srgb, var(--color) 5%, white);

                .nav-title {
                    color: var(--color);
                }
            }

            .nav-title {
                font-size: 14px;
                color: #333;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                -webkit-box-orient: vertical;
                line-height: 1.5;
                transition: color 0.3s ease;
            }
        }

        &.prev-article .nav-content {
            text-align: left;
        }

        &.next-article .nav-content {
            text-align: right;
        }
    }
}

// 推荐资讯模块
.recommended-articles {
    margin-top: 40px;
    padding-top: 30px;
    border-top: 1px solid #f0f0f0;

    .recommended-title {
        font-size: 20px;
        font-weight: 600;
        color: #333;
        margin-bottom: 20px;
    }

    .recommended-list {
        display: flex;
        flex-direction: column;
        gap: 20px;
    }

    .recommended-item {
        display: flex;
        gap: 15px;
        padding: 15px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
            background-color: #f9f9f9;

            .recommended-item-title {
                color: var(--color);
            }
        }

        .recommended-image {
            width: 120px;
            height: 80px;
            flex-shrink: 0;
            border-radius: 6px;
            overflow: hidden;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .recommended-info {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;

            .recommended-item-title {
                font-size: 16px;
                font-weight: 500;
                color: #333;
                margin-bottom: 10px;
                line-height: 1.5;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                transition: color 0.3s ease;
            }

            .recommended-meta {
                display: flex;
                align-items: center;
                gap: 20px;
                font-size: 13px;
                color: #999;

                .recommended-views {
                    color: #666;
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .article-container {
        padding: 0 15px;
    }

    .article-content {
        padding: 20px;
    }

    .article-title {
        font-size: 22px;
    }

    .article-meta {
        flex-direction: column;
        align-items: flex-start;
        gap: 15px;
    }

    .article-body {
        font-size: 14px;
    }

    .article-navigation {
        flex-direction: column;
        gap: 15px;
    }

    .recommended-item {
        flex-direction: column;

        .recommended-image {
            width: 100%;
            height: 200px;
        }
    }
}
</style>
