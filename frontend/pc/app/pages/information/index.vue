<template>
    <div class="information-container">
        <!-- 顶部区域：轮播图 + 热门推荐 -->
        <div class="top-section">
            <!-- 左侧轮播图 -->
            <div class="banner-section">
                <Swiper v-if="banners.length > 0" :loop="banners.length > 1"
                    :autoplay="banners.length > 1 ? { delay: 5000, disableOnInteraction: false, pauseOnMouseEnter: true } : false"
                    :modules="[Autoplay]" :speed="1000" class="banner-carousel">
                    <SwiperSlide v-for="(item, index) in banners" :key="index">
                        <div class="carousel-item">
                            <img :src="item.configValue" :alt="item.configComment" class="carousel-image" />
                        </div>
                    </SwiperSlide>
                </Swiper>
                <EmptyState v-else :text="$t('暂无轮播图')" compact />
            </div>

            <!-- 右侧热门推荐 -->
            <div class="hot-recommendations">
                <h3 class="section-title">{{ $t('热门推荐') }}</h3>
                <ul v-if="hotRecommendations.length > 0" class="hot-list" ref="hotListRef">
                    <li v-for="(item, index) in hotRecommendations" :key="`hot-${index}`" class="hot-item"
                        :class="{ 'hot-top': index < 3 }"
                        @click="item.informationId && viewArticle(item.informationId, item.classifyId)">
                        <span class="hot-number">{{ index + 1 }}</span>
                        <span class="hot-icon" v-if="index < 3">🔥</span>
                        <span class="hot-title" :class="{ 'hot-title-large': index < 3 }">{{ $t(item.informationTitle ||
                            '')
                        }}</span>
                    </li>
                </ul>
                <EmptyState v-else :text="$t('暂无热门推荐')" compact />
            </div>
        </div>

        <!-- 底部区域：左侧分类导航 + 右侧资讯列表 -->
        <div class="bottom-section">
            <!-- 左侧分类导航 -->
            <div class="sidebar">
                <div class="sidebar-title">{{ $t('内容中心') }}</div>
                <div class="category-list">
                    <!-- 全部资讯 -->
                    <div class="category-item" :class="{ active: activeCategoryId === 'all' }"
                        @click="selectCategory('all')">
                        <span class="category-name">{{ $t('全部资讯') }}</span>
                    </div>
                    <!-- 分类列表 -->
                    <div v-for="category in categories" :key="category.classifyId" class="category-wrapper">
                        <div class="category-item" :class="{
                            active: activeCategoryId === category.classifyId,
                            'has-children': category.childMenu && category.childMenu.length > 0,
                            'expanded': expandedCategories.includes(category.classifyId)
                        }" @click="toggleCategory(category)">
                            <span class="category-name">{{ $t(category.classifyName || '') }}</span>
                            <span v-if="category.childMenu && category.childMenu.length > 0" class="dropdown-icon"
                                :class="{ 'expanded': expandedCategories.includes(category.classifyId) }">
                                ▼
                            </span>
                        </div>
                        <!-- 子分类列表 -->
                        <div v-if="category.childMenu && category.childMenu.length > 0 && expandedCategories.includes(category.classifyId)"
                            class="child-category-list">
                            <div v-for="child in category.childMenu" :key="child.classifyId" class="child-category-item"
                                :class="{ active: activeCategoryId === child.classifyId }"
                                @click.stop="selectCategory(child.classifyId)">
                                <span class="child-category-name">{{ $t(child.classifyName || '') }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 右侧资讯列表 -->
            <div class="information-list">
                <h3 class="section-title">{{ $t(currentCategoryName || '') }}</h3>
                <div class="article-list">
                    <div v-if="articles.length > 0">
                        <div v-for="article in articles" :key="article.informationId" class="article-item"
                            @click="viewArticle(article.informationId, article.classifyId)">
                            <div class="article-image">
                                <img :src="article.informationCoverList && article.informationCoverList.length > 0 ? article.informationCoverList[0] : article.informationCover"
                                    :alt="article.informationTitle" />
                            </div>
                            <div class="article-info">
                                <h4 class="article-title">{{ $t(article.informationTitle || '') }}</h4>
                                <div class="article-meta">
                                    <span class="article-views">{{ $t('浏览量') }}: {{ article.pageView }}</span>
                                    <span class="article-date">{{ article.releaseTime || '' }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <EmptyState v-else :text="$t('暂无资讯内容')" :description="$t('该分类下还没有发布任何资讯')" />
                </div>

                <!-- 分页 -->
                <div v-if="articles.length > 0 && totalPages > 1" class="pagination">
                    <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
                        {{ $t('上一页') }}
                    </button>
                    <div class="page-numbers">
                        <button v-for="page in visiblePages" :key="page" class="page-number"
                            :class="{ active: page === currentPage }" @click="changePage(page)">
                            {{ page }}
                        </button>
                    </div>
                    <button class="page-btn" :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">
                        {{ $t('下一页') }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import type { InformationSliderConfig, InformationClassifyItem, InformationListItem } from '~/types/base'
import type { NewsItem } from '~/types/home'
import { queryInformationSlider, getUserQueryInformationInfo, queryAllInformationClassify, userQueryInformationInfoById } from '~/services/base.service'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay } from 'swiper/modules'
import 'swiper/css'

// 热门推荐列表引用
const hotListRef = ref<HTMLElement | null>(null)
let autoScrollTimer: ReturnType<typeof setInterval> | null = null

// 轮播图数据
const banners = ref<InformationSliderConfig[]>([])

// 分类数据
const categories = ref<InformationClassifyItem[]>([])

// 展开的分类ID列表
const expandedCategories = ref<number[]>([])

// 当前选中的分类
const activeCategoryId = ref<number | string>('all')

// 当前分类名称
const currentCategoryName = computed(() => {
    if (activeCategoryId.value === 'all') {
        return '全部资讯'
    }

    // 查找一级分类
    for (const category of categories.value) {
        if (category.classifyId === activeCategoryId.value) {
            return category.classifyName
        }
        // 查找子分类
        if (category.childMenu && category.childMenu.length > 0) {
            const child = category.childMenu.find(child => child.classifyId === activeCategoryId.value)
            if (child) {
                return child.classifyName
            }
        }
    }

    return '全部资讯'
})

// 热门推荐数据
const hotRecommendations = ref<NewsItem[]>([])

// 资讯列表数据
const articles = ref<InformationListItem[]>([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const totalCount = ref(0)

// 可见的页码
const visiblePages = computed(() => {
    const pages: number[] = []
    const maxVisible = 5
    const total = totalPages.value || 1
    let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
    let end = Math.min(total, start + maxVisible - 1)

    if (end - start < maxVisible - 1) {
        start = Math.max(1, end - maxVisible + 1)
    }

    for (let i = start; i <= end; i++) {
        pages.push(i)
    }
    return pages
})

// 切换分类展开/收起
const toggleCategory = (category: InformationClassifyItem) => {
    if (category.childMenu && category.childMenu.length > 0) {
        const index = expandedCategories.value.indexOf(category.classifyId)
        if (index > -1) {
            // 收起
            expandedCategories.value.splice(index, 1)
        } else {
            // 展开
            expandedCategories.value.push(category.classifyId)
        }
    } else {
        // 没有子分类，直接选择
        selectCategory(category.classifyId)
    }
}

// 选择分类
const selectCategory = (categoryId: number | string) => {
    activeCategoryId.value = categoryId
    currentPage.value = 1 // 切换分类时重置到第一页
    // 调用API获取该分类下的文章
    fetchArticles()
}

// 切换页码
const changePage = (page: number) => {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
        // 调用API获取该页的文章
        fetchArticles()
        // 滚动到顶部
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }
}

// 查看文章详情
const viewArticle = (articleId: number | undefined, classifyId?: number | string | null) => {
    if (!articleId) return

    // 构建查询参数
    const query: Record<string, string> = {}
    if (classifyId !== null && classifyId !== undefined && classifyId !== '') {
        query.classifyId = String(classifyId)
    }

    // 如果有查询参数，添加到URL中
    const queryString = Object.keys(query).length > 0
        ? '?' + new URLSearchParams(query).toString()
        : ''

    navigateTo(`/information/${articleId}${queryString}`)
}

// 自动滚动热门推荐列表
const startAutoScroll = () => {
    if (!hotListRef.value) return

    const listElement = hotListRef.value
    let scrollPosition = 0
    const scrollSpeed = 0.5 // 滚动速度（像素/帧）

    autoScrollTimer = setInterval(() => {
        if (listElement.scrollHeight > listElement.clientHeight) {
            scrollPosition += scrollSpeed
            const maxScroll = listElement.scrollHeight - listElement.clientHeight

            if (scrollPosition >= maxScroll) {
                // 滚动到底部后，重置到顶部
                scrollPosition = 0
                listElement.scrollTop = 0
            } else {
                listElement.scrollTop = scrollPosition
            }
        }
    }, 16) // 约60fps
}

// 停止自动滚动
const stopAutoScroll = () => {
    if (autoScrollTimer) {
        clearInterval(autoScrollTimer)
        autoScrollTimer = null
    }
}

// 获取轮播图数据
const fetchBannerData = async () => {
    try {
        const response = await queryInformationSlider()

        if (response.code === 1 && response.data && Array.isArray(response.data)) {
            // 按 configSort 排序，只取启用状态（configEnable）的数据
            banners.value = response.data
                .filter(item => item.configEnable === 1) // 只取启用状态的数据
                .sort((a, b) => (a.configSort || 0) - (b.configSort || 0)) // 按排序字段排序
        }
    } catch (error) {
        console.error('获取轮播图数据失败:', error)
        // 如果接口调用失败，保持空数组
        banners.value = []
    }
}

// 获取热门推荐数据
const fetchHotRecommendations = async () => {
    try {
        const response = await getUserQueryInformationInfo({
            isRecommend: 1
        })

        if (response?.data && Array.isArray(response.data)) {
            // 过滤掉没有标题的数据
            hotRecommendations.value = response.data.filter((item: NewsItem) => item.informationTitle)
        }
    } catch (error) {
        console.error('获取热门推荐数据失败:', error)
        // 如果接口调用失败，保持空数组
        hotRecommendations.value = []
    }
}

// 获取分类数据
const fetchCategories = async () => {
    try {
        const response = await queryAllInformationClassify()

        if (response?.data && Array.isArray(response.data)) {
            // 过滤显示状态为1的分类，并按排序字段排序
            categories.value = response.data
                .filter(item => item.classifyShowStatus === 1) // 只取显示状态为1的数据
                .sort((a, b) => (a.classifySort || 0) - (b.classifySort || 0)) // 按排序字段排序
                .map(item => ({
                    ...item,
                    // 对子分类也进行过滤和排序
                    childMenu: item.childMenu
                        ? item.childMenu
                            .filter(child => child.classifyShowStatus === 1)
                            .sort((a, b) => (a.classifySort || 0) - (b.classifySort || 0))
                        : []
                }))
        }
    } catch (error) {
        console.error('获取分类数据失败:', error)
        // 如果接口调用失败，保持空数组
        categories.value = []
    }
}

// 获取资讯列表数据
const fetchArticles = async () => {
    try {
        const classifyId = activeCategoryId.value === 'all' ? '' : String(activeCategoryId.value)
        const response = await userQueryInformationInfoById({
            classifyId: classifyId,
            page: currentPage.value,
            limit: pageSize.value
        })

        if (response?.data) {
            articles.value = response.data.list || []
            totalPages.value = response.data.totalPage || 1
            totalCount.value = response.data.totalCount || 0
        }
    } catch (error) {
        console.error('获取资讯列表失败:', error)
        // 如果接口调用失败，保持空数组
        articles.value = []
        totalPages.value = 1
        totalCount.value = 0
    }
}

onMounted(() => {
    // 获取轮播图数据
    fetchBannerData()

    // 获取热门推荐数据
    fetchHotRecommendations()

    // 获取分类数据
    fetchCategories()

    // 获取资讯列表数据
    fetchArticles()

    // 延迟启动自动滚动，确保DOM已渲染
    setTimeout(() => {
        startAutoScroll()
    }, 500)

    // 鼠标悬停时暂停滚动
    if (hotListRef.value) {
        hotListRef.value.addEventListener('mouseenter', stopAutoScroll)
        hotListRef.value.addEventListener('mouseleave', startAutoScroll)
    }
})

onUnmounted(() => {
    stopAutoScroll()
    if (hotListRef.value) {
        hotListRef.value.removeEventListener('mouseenter', stopAutoScroll)
        hotListRef.value.removeEventListener('mouseleave', startAutoScroll)
    }
})
</script>

<style lang="scss" scoped>
.information-container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

// 顶部区域：轮播图 + 热门推荐
.top-section {
    display: flex;
    gap: 20px; // 轮播图和热门推荐之间保持20px空白
    margin-bottom: 30px;
    align-items: flex-start; // 改为flex-start，确保顶部对齐
    height: 300px; // 统一高度
}

// 轮播图区域
.banner-section {
    flex: 2;
    height: 300px; // 固定高度300px
    border-radius: 8px; // 恢复完整圆角
    overflow: hidden;
    position: relative;

    // 确保 UCarousel 组件填充满容器
    :deep(> *) {
        width: 100%;
        height: 100%;
    }

    .banner-empty {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background-color: #f5f5f5;
    }
}

.banner-carousel {
    width: 100%;
    height: 100%; // 使用100%填充父容器
    position: relative;
    display: flex;
    flex-direction: column;

    // Swiper 填充容器
    :deep(.swiper),
    :deep(.swiper-wrapper),
    :deep(.swiper-slide) {
        height: 100%;
    }

    .carousel-item {
        width: 100%;
        height: 100%;
        position: relative;
        display: block;
        margin: 0;
        padding: 0;
    }

    .carousel-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
        margin: 0;
        padding: 0;
    }

    // 该页轮播不使用分页器/箭头（保留纯 autoplay + loop 体验）
}

// 热门推荐（顶部右侧）
.hot-recommendations {
    flex: 1;
    height: 300px; // 固定高度300px，与轮播图一致
    background-color: white;
    border-radius: 8px; // 恢复完整圆角
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-direction: column;
    overflow: hidden; // 防止内容溢出
    box-sizing: border-box; // 确保padding包含在高度内

    .section-title {
        font-size: 18px;
        font-weight: 600;
        color: var(--color);
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 2px solid var(--color);
        flex-shrink: 0;
        line-height: 1;
    }

    .hot-list {
        list-style: none;
        padding: 0;
        margin: 0;
        flex: 1;
        overflow-y: scroll; // 保持滚动功能
        overflow-x: hidden;
        min-height: 0; // 允许flex子元素缩小
        max-height: calc(300px - 60px); // 减去标题和padding的高度，确保不超过容器

        // 隐藏滚动条但保持滚动功能
        // WebKit浏览器 (Chrome, Safari, Edge)
        &::-webkit-scrollbar {
            display: none;
            width: 0;
        }

        // Firefox
        scrollbar-width: none;

        // IE/Edge旧版
        -ms-overflow-style: none;

        .hot-item {
            display: flex;
            align-items: center;
            padding: 12px 0;
            cursor: pointer;
            transition: all 0.2s ease;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
                border-bottom: none;
            }

            &:hover {
                background-color: #f9f9f9;
                padding-left: 10px;
            }

            // 前三条热门项样式
            &.hot-top {
                padding: 15px 0;

                .hot-number {
                    width: 28px;
                    height: 28px;
                    font-size: 16px;
                }
            }

            .hot-number {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                width: 24px;
                height: 24px;
                background-color: var(--color);
                color: white;
                border-radius: 4px;
                font-size: 14px;
                font-weight: 600;
                margin-right: 12px;
                flex-shrink: 0;
            }

            .hot-icon {
                font-size: 16px;
                margin-right: 8px;
                flex-shrink: 0;
            }

            .hot-title {
                font-size: 14px;
                color: #333;
                line-height: 1.6;
                flex: 1;

                // 前三条字体变大
                &.hot-title-large {
                    font-size: 16px;
                    font-weight: 600;
                }

                &:hover {
                    color: var(--color);
                }
            }
        }
    }

    .hot-empty {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        min-height: 200px;
    }
}

// 底部区域：左侧分类导航 + 右侧资讯列表
.bottom-section {
    display: flex;
    gap: 20px;
    align-items: flex-start;
}

// 左侧导航栏
.sidebar {
    width: 200px;
    background-color: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    flex-shrink: 0;

    .sidebar-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin-bottom: 20px;
        padding-bottom: 15px;
        border-bottom: 2px solid #f0f0f0;
    }

    .category-list {
        .category-wrapper {
            margin-bottom: 2px;
        }

        .category-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 10px;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s ease;
            color: #666;

            &:hover {
                background-color: #f5f5f5;
                color: #333;
            }

            &.active {
                background-color: color-mix(in srgb, var(--color) 5%, white);
                color: var(--color);
                border-bottom: 2px solid var(--color);
            }

            &.has-children {
                font-weight: 500;
            }

            &.expanded {
                background-color: #f9f9f9;

                .dropdown-icon {
                    transform: rotate(180deg);
                }
            }

            .category-name {
                font-size: 14px;
                flex: 1;
            }

            .dropdown-icon {
                font-size: 10px;
                color: #999;
                transition: transform 0.3s ease;
                margin-left: 8px;
            }
        }

        .child-category-list {
            padding-left: 20px;
            margin-top: 2px;
            animation: slideDown 0.3s ease;
        }

        .child-category-item {
            display: flex;
            align-items: center;
            padding: 10px 10px;
            margin-bottom: 2px;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s ease;
            color: #666;
            font-size: 13px;

            &:hover {
                background-color: #f5f5f5;
                color: #333;
            }

            &.active {
                background-color: color-mix(in srgb, var(--color) 5%, white);
                color: var(--color);
                border-left: 3px solid var(--color);
                padding-left: 7px;
            }

            .child-category-name {
                font-size: 13px;
            }
        }
    }
}

@keyframes slideDown {
    from {
        opacity: 0;
        max-height: 0;
    }

    to {
        opacity: 1;
        max-height: 500px;
    }
}

// 空状态通用样式
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    color: #999;

    .empty-icon {
        font-size: 48px;
        margin-bottom: 16px;
        opacity: 0.6;
    }

    .empty-text {
        font-size: 16px;
        color: #666;
        margin-bottom: 8px;
    }

    .empty-desc {
        font-size: 14px;
        color: #999;
    }
}

// 资讯列表
.information-list {
    flex: 1;
    background-color: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    display: flex;
    flex-direction: column;
    min-height: 500px; // 设置最小高度

    .section-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin-bottom: 20px;
        padding-bottom: 10px;
        border-bottom: 2px solid #f0f0f0;
        flex-shrink: 0;
    }

    .article-list {
        flex: 1;
        overflow-y: auto;
        overflow-x: hidden;
        min-height: 0; // 允许flex子元素缩小

        .article-empty {
            min-height: 400px;
            padding: 60px 20px;
        }

        // 自定义滚动条样式
        &::-webkit-scrollbar {
            width: 6px;
        }

        &::-webkit-scrollbar-track {
            background: #f5f5f5;
            border-radius: 3px;
        }

        &::-webkit-scrollbar-thumb {
            background: #ccc;
            border-radius: 3px;

            &:hover {
                background: #999;
            }
        }

        .article-item {
            display: flex;
            gap: 15px;
            padding: 20px 0;
            border-bottom: 1px solid #f0f0f0;
            cursor: pointer;
            transition: all 0.3s ease;

            &:last-child {
                border-bottom: none;
            }

            &:hover {
                background-color: #f9f9f9;
                padding-left: 10px;
                padding-right: 10px;

                .article-title {
                    color: var(--color);
                }
            }

            .article-image {
                width: 200px;
                height: 150px;
                flex-shrink: 0;
                border-radius: 6px;
                overflow: hidden;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                    transition: transform 0.3s ease;
                }
            }

            &:hover .article-image img {
                transform: scale(1.05);
            }

            .article-info {
                flex: 1;
                display: flex;
                flex-direction: column;
                justify-content: space-between;

                .article-title {
                    font-size: 16px;
                    font-weight: 500;
                    color: #333;
                    margin-bottom: 15px;
                    line-height: 1.6;
                    transition: color 0.3s ease;
                }

                .article-meta {
                    display: flex;
                    align-items: center;
                    gap: 20px;
                    font-size: 13px;
                    color: #999;

                    .article-views {
                        color: #666;
                    }

                    .article-date {
                        color: #999;
                    }
                }
            }
        }
    }
}

// 分页
.pagination {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
    flex-shrink: 0; // 分页区域不收缩

    .page-btn {
        padding: 8px 16px;
        border: 1px solid #e0e0e0;
        background-color: white;
        color: #666;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s ease;
        font-size: 14px;

        &:hover:not(:disabled) {
            border-color: var(--color);
            color: var(--color);
            background-color: color-mix(in srgb, var(--color) 5%, white);
        }

        &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
    }

    .page-numbers {
        display: flex;
        gap: 5px;

        .page-number {
            min-width: 36px;
            height: 36px;
            padding: 0 8px;
            border: 1px solid #e0e0e0;
            background-color: white;
            color: #666;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s ease;
            font-size: 14px;

            &:hover {
                border-color: var(--color);
                color: var(--color);
                background-color: color-mix(in srgb, var(--color) 5%, white);
            }

            &.active {
                background-color: var(--color);
                color: white;
                border-color: var(--color);
            }
        }
    }
}

// 响应式设计
@media (max-width: 768px) {
    .information-container {
        padding: 15px;
    }

    .top-section {
        flex-direction: column;
    }

    .banner-section {
        width: 100%;
    }

    .banner-carousel {
        height: 250px;
    }

    .hot-recommendations {
        width: 100%;

        .hot-item {
            .hot-title {
                font-size: 13px;
            }
        }
    }

    .bottom-section {
        flex-direction: column;
    }

    .sidebar {
        width: 100%;
        margin-bottom: 20px;
    }

    .article-item {
        flex-direction: column;

        .article-image {
            width: 100%;
            height: 200px;
        }
    }

    .pagination {
        flex-wrap: wrap;

        .page-numbers {
            flex-wrap: wrap;
        }
    }
}
</style>
