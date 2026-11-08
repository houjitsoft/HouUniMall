<template>
    <div class="category-box">
        <!-- 顶部标题区域 -->
        <div class="header-section">
            <h2 class="category-title">{{ $t(categoryTitle) }}</h2>
            <div class="tabs-container">
                <button class="tab-button" :class="{ active: activeTab === tab.catId }" v-for="tab in tabs"
                    :key="tab.catId" @click="handleTabClick(tab.catId, tab.catId)">
                    {{ $t(tab.catName) }}
                </button>
                <button class="more-button" @click="handleMoreButtonClick">{{ $t('更多') }} 〉</button>
            </div>
        </div>

        <!-- 内容区域 -->
        <div class="content-section">
            <!-- 左侧区域 -->
            <div class="left-section">
                <!-- 商品轮播图 -->
                <div class="swiper-container">
                    <swiper :slides-per-view="1" :space-between="0" :loop="true" :autoplay="{ delay: 2000 }"
                        :grab-cursor="true">
                        <swiper-slide v-for="(item, index) in swiperData" :key="index">
                            <div class="product-card" @click="handleProductClick(item)">
                                <div class="product-image">
                                    <img :src="item.goodsImage || item.commonImage"
                                        :alt="item.goodsName || item.commonName" />
                                </div>
                                <div class="product-title">{{ $t(formatProductTitle(item.goodsName || item.commonName || '')) }}
                                </div>
                                <div class="product-price">
                                    <span class="current-price"
                                        v-if="item.commonPrice">{{ formatPriceWithSymbolSafe(item.commonPrice) }}</span>
                                    <span class="consult-price"
                                        v-if="!item.commonPrice && item.goodsPrice">{{ formatPriceWithSymbolSafe(item.goodsPrice) }}</span>
                                </div>
                            </div>
                        </swiper-slide>
                    </swiper>
                </div>

                <!-- 产品分类标签 -->
                <div class="product-tags-container">
                    <div class="tag-item" v-for="(tab, index) in leftAdpageNavList" :key="index"
                        @click="handleCategoryTagClick(tab.leftAdpageNavUrl)">
                        {{ $t(tab.leftAdpageNavName) }}
                    </div>
                </div>
            </div>

            <!-- 右侧商品展示区域 -->
            <div class="right-section">
                <!-- 加载状态 -->
                <Loading v-if="loading" :text="$t('加载中...')" />

                <!-- 商品网格 -->
                <div v-else-if="currentProducts && currentProducts.length > 0" class="product-grid">
                    <div class="product-item" v-for="product in currentProducts" :key="product.catId"
                        @click="handleProductClick(product)">
                        <div class="product-image">
                            <img :src="product.goodsImage || product.commonImage"
                                :alt="product.goodsName || product.commonName" />
                        </div>
                        <div class="product-title">{{ $t(formatProductTitle(product.goodsName || product.commonName || '')) }}
                        </div>
                        <div class="product-price">
                            <span class="current-price" v-if="product.commonPrice">{{ formatPriceWithSymbolSafe(product.commonPrice) }}</span>
                            <span class="consult-price"
                                v-if="!product.commonPrice && product.goodsPrice">{{ formatPriceWithSymbolSafe(product.goodsPrice) }}</span>
                        </div>
                        <div class="product-store">{{ $t(product.shopName || '') }}</div>
                    </div>
                </div>

                <!-- 空状态 -->
                <EmptyState v-else :text="$t('暂无商品')" :description="$t('该分类下暂无商品，换个分类试试吧')" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { Swiper, SwiperSlide } from 'swiper/vue'
import 'swiper/css'
import 'swiper/css/navigation'
import { ref, computed } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { useRouter } from 'vue-router'
import { getCategoryGoodsList } from '~/services/goods.service'

import type { HomeProduct, CategoryTab, CategoryDataItem } from '~/types/home'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'

// 组件属性
const props = defineProps({
    dataInfo: {
        type: Array as PropType<CategoryDataItem[]>,
        default: () => []
    }
})

const router = useRouter()

// 响应式数据
const activeTab = ref(1)
const goodsList = ref<HomeProduct[]>([])
const loading = ref(false)

// 计算属性
const swiperData = computed(() => {
    return props.dataInfo?.[0]?.adpageBannerList || []
})

const tabs = computed(() => {
    if (props.dataInfo?.[0]) {
        const { rightRec, rightAdpageNavList } = props.dataInfo[0]
        const tabsList = [...(rightAdpageNavList || [])]

        // 将 rightRec.rightAdpageNavName 添加到 tabs 中的第一个值
        if (rightRec?.rightAdpageNavName) {
            tabsList.unshift({
                catId: 1,
                leftAdpageNavUrl: '',
                leftAdpageNavName: '',
                catName: rightRec.rightAdpageNavName
            })
        }

        return tabsList
    }
    return []
})

const leftAdpageNavList = computed(() => {
    return props.dataInfo?.[0]?.leftAdpageNavList || []
})

const currentProducts = computed(() => {
    // 第一个tab显示rightRecGoodsList，其他tab显示接口数据
    if (activeTab.value === 1 && props.dataInfo?.[0]?.rightRecGoodsList) {
        return props.dataInfo[0].rightRecGoodsList
    }
    return goodsList.value
})

const categoryTitle = computed(() => {
    return props.dataInfo?.[0]?.floorAdpageNameNew?.catName || ''
})

const categoryId = computed(() => {
    return props.dataInfo?.[0]?.floorAdpageNameNew?.catId || 0
})
// 处理商品标题的硬编码换行
const formatProductTitle = (title: string) => {
    if (!title) return '未命名商品'

    // 对于包含字母、数字、符号的长字符串，添加软换行提示
    // 匹配连续的字母数字符号（至少4个字符），在适当位置添加零宽空格
    return title.replace(/([a-zA-Z0-9_]{4})/g, '$1\u200B')
}

// 方法
const handleTabClick = async (tabId: number, catId?: number) => {
    activeTab.value = tabId

    // 第一个tab显示rightRecGoodsList，其他tab需要调用接口
    if (tabId !== 1 && catId) {
        loading.value = true
        try {
            const response = await getCategoryGoodsList({ catId })
            // 假设接口返回的数据结构包含data字段，只取前四个商品
            goodsList.value = (response.data || []).slice(0, 4)
        } catch (error) {
            console.error('获取商品列表失败:', error)
            goodsList.value = []
        } finally {
            loading.value = false
        }
    }
}

const handleCategoryTagClick = (leftAdpageNavUrl: string) => {
    let catId: number = JSON.parse(leftAdpageNavUrl).catId;
    navigateTo(`/product/list?catId=${catId}`)
}

// 处理点击商品
const handleProductClick = (item: any) => {
    navigateTo(`/product/${item.goodsId}`)
}

// 处理点击更多按钮
const handleMoreButtonClick = () => {
    navigateTo(`/product/list?catId=${categoryId.value}`)
}
</script>

<style lang="scss" scoped>
.category-box {
    width: 100%;
    border-radius: 8px;
    background-color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    padding: 20px;
    margin-bottom: 20px;

    .header-section {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;

        .category-title {
            font-size: 20px;
            font-weight: bold;
            color: var(--color);
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;

            &::before {
                content: '|';
                font-size: 24px;
                color: var(--color);
            }
        }

        .tabs-container {
            display: flex;
            align-items: center;
            gap: 20px;

            .tab-button {
                background: none;
                border: none;
                font-size: 16px;
                color: #666;
                cursor: pointer;
                padding: 5px 10px;
                border-radius: 4px;
                transition: all 0.2s ease;
                position: relative;

                &:hover {
                    color: #333;
                }

                &.active {
                    color: #333;
                    font-weight: bold;

                    &::after {
                        content: '';
                        position: absolute;
                        bottom: -12px;
                        left: 0;
                        width: 100%;
                        height: 2px;
                        background-color: var(--color);
                    }
                }
            }

            .more-button {
                background: none;
                border: none;
                font-size: 14px;
                color: #666;
                cursor: pointer;
                padding: 5px 10px;
                border-radius: 4px;
                transition: all 0.2s ease;
                margin-left: auto;

                &:hover {
                    color: #333;
                }
            }
        }
    }

    .content-section {
        display: flex;
        gap: 15px;
    }

    .left-section {
        width: calc(21% - 15px);
        background-color: white;
        border-radius: 8px;
        padding: 15px 5px;

        .product-tags-container {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 8px;

            .tag-item {
                padding: 6px 5px;
                background-color: #f0f0f0;
                border-radius: 4px;
                font-size: 12px;
                color: #666;
                cursor: pointer;
                transition: all 0.2s ease;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                text-align: center;

                &:hover {
                    background-color: #e0e0e0;
                    color: #333;
                }

                &.active {
                    background-color: var(--color);
                    color: white;
                }
            }
        }

        .swiper-container {
            position: relative;
            border-radius: 6px;
            overflow: hidden;
            margin-bottom: 10px;

            :deep(.swiper-slide) {
                display: flex;
                justify-content: center;
            }

            .product-card {
                width: 100%;
                height: 250px;
                padding: 0 15px;
                background-color: #f8f9fa;
                border-radius: 6px;
                cursor: pointer;
                transition: all 0.2s ease;
                display: flex;
                flex-direction: column;
                justify-content: center;
            }

            .product-image {
                width: 100%;
                aspect-ratio: 1 / 1;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 10px;
                background-color: white;
                border-radius: 4px;

                img {
                    max-width: 100%;
                    max-height: 100%;
                    object-fit: contain;
                }
            }

            .product-title {
                font-size: 13px;
                color: #333;
                text-align: left;
                line-height: 1.4;
                margin-bottom: 10px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                font-weight: 500;
            }

            .product-price {
                display: flex;
                align-items: center;
                gap: 5px;
                margin-bottom: 1px;
                justify-content: flex-start;

                .consult-price {
                    margin-bottom: 1px;
                }
            }

            :deep(.swiper-button-prev),
            :deep(.swiper-button-next) {
                color: #007bff;
                width: 30px;
                height: 30px;
                font-size: 14px;
                background-color: rgba(255, 255, 255, 0.8);
                border-radius: 50%;
                transition: all 0.2s ease;

                &:hover {
                    background-color: white;
                    transform: scale(1.1);
                }

                &::after {
                    font-size: 16px;
                }
            }

            :deep(.swiper-button-prev) {
                left: 10px;
            }

            :deep(.swiper-button-next) {
                right: 10px;
            }
        }
    }

    .right-section {
        flex: 1;
        background-color: white;
        border-radius: 8px;
        padding: 15px;

    }

    @keyframes spin {
        0% {
            transform: rotate(0deg);
        }

        100% {
            transform: rotate(360deg);
        }
    }

    .product-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 20px;

        .product-item {
            display: flex;
            flex-direction: column;
            padding: 15px;
            border-radius: 6px;
            background-color: #f8f9fa;
            cursor: pointer;
            transition: all 0.2s ease;
            height: 100%;
            min-height: 300px;

            &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                background-color: white;
            }

            .product-image {
                width: 100%;
                aspect-ratio: 1 / 1;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 10px;
                background-color: white;
                border-radius: 4px;

                img {
                    max-width: 100%;
                    max-height: 100%;
                    object-fit: contain;
                }
            }

            .product-title {
                font-size: 13px;
                color: #333;
                text-align: left;
                line-height: 1.4;
                margin-bottom: 10px;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                word-break: break-all;
                height: 38px;
                font-weight: 500;
                flex-grow: 1;
            }

            .product-price {
                display: flex;
                align-items: center;
                gap: 5px;
                margin-bottom: 10px;
                justify-content: flex-start;
            }

            .product-store {
                font-size: 12px;
                color: #999;
                text-align: left;
                margin-top: auto;
            }
        }
    }
}

// 共享样式
.product-image {
    width: 100%;
    aspect-ratio: 1 / 1;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 10px;

    img {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
    }
}

.product-title {
    font-size: 13px;
    color: #333;
    text-align: left;
    line-height: 1.4;
    margin-bottom: 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    word-break: break-all;
    height: 38px;
    font-weight: 500;
}

.product-price {
    display: flex;
    align-items: center;
    gap: 5px;
    margin-bottom: 10px;
    justify-content: center;

    .price-label {
        background-color: var(--color);
        color: white;
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: bold;
    }

    .current-price {
        font-size: 16px;
        color: var(--color);
        font-weight: bold;
    }

    .consult-price {
        font-size: 16px;
        color: var(--color);
        font-weight: bold;
    }
}

.product-store {
    font-size: 12px;
    color: #999;
}

// 响应式设计
@media (max-width: 1200px) {
    .category-box {
        .content-section {
            flex-direction: column;
            gap: 20px;
        }

        .left-section {
            width: 100%;
        }

        .right-section {
            .product-grid {
                grid-template-columns: repeat(4, 1fr);
                gap: 15px;
            }
        }
    }
}

@media (max-width: 992px) {
    .category-box {
        .left-section {
            width: calc(33.333% - 10px);
        }

        .right-section {
            .product-grid {
                grid-template-columns: repeat(3, 1fr);
                gap: 12px;
            }
        }
    }
}

@media (max-width: 768px) {
    .category-box {
        padding: 15px;
        gap: 15px;

        .content-section {
            flex-direction: column;
            gap: 15px;
        }

        .left-section {
            width: 100%;
            padding: 10px;

            .product-tags-container {
                margin-top: 12px;
                padding-top: 12px;
            }

            .swiper-container {
                height: 260px;

                :deep(.swiper-slide) {
                    height: 260px;
                }

                :deep(.swiper-wrapper) {
                    height: 260px !important;
                }

                .product-card {
                    height: 230px;
                }
            }
        }

        .right-section {
            padding: 10px;

            .tabs-container {
                gap: 15px;
                padding-bottom: 8px;

                .tab-button {
                    font-size: 14px;
                    padding: 4px 8px;
                }

                .more-button {
                    font-size: 12px;
                    padding: 4px 8px;
                }
            }

            .product-grid {
                grid-template-columns: repeat(2, 1fr);
                gap: 10px;

                .product-item {
                    padding: 10px;

                    .product-image {
                        aspect-ratio: 1 / 1;
                    }

                    .product-title {
                        font-size: 12px;
                        height: 36px;
                    }

                    .product-price {
                        .current-price {
                            font-size: 14px;
                        }

                        .consult-price {
                            font-size: 14px;
                            color: var(--color);
                            font-weight: bold;
                        }
                    }

                    .product-store {
                        font-size: 11px;
                    }
                }
            }
        }

        .product-title {
            font-size: 12px;
            height: 36px;
        }

        .product-price {
            .current-price {
                font-size: 14px;
            }

            .consult-price {
                font-size: 14px;
                color: var(--color);
                font-weight: bold;
            }
        }

        .tab-button.active::after {
            bottom: -10px;
        }
    }
}

@media (max-width: 480px) {
    .category-box {
        .right-section {
            .product-grid {
                grid-template-columns: repeat(1, 1fr);
            }
        }
    }
}
</style>