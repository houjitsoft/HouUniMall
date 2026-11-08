<template>
    <div class="featured-pavilion-box">
        <!-- 顶部标题区域 -->
        <div class="header-section">
            <h2 class="pavilion-title">{{ $t(pavilionTitle) }}</h2>
            <div class="more-link" @click="handleMoreClick">
                {{ $t('更多') }} 〉
            </div>
        </div>

        <!-- 主要内容区域 -->
        <div class="content-section">
            <!-- 左侧大模块 -->
            <div class="left-section">
                <div class="main-product-card" v-if="mainCat" @click="handleCatClick(mainCat)">
                    <div class="product-image">
                        <img :src="mainCat.catImage" :alt="mainCat.catUrl" />
                    </div>
                </div>
            </div>

            <!-- 右侧模块（上下两行，每行三个） -->
            <div class="right-section">
                <!-- 顶部一行 -->
                <div class="top-row">
                    <div class="product-item" v-for="(item, index) in topRowCats" :key="index"
                        @click="handleCatClick(item)">
                        <div class="item-image">
                            <img :src="item.catImage" :alt="item.catUrl" />
                        </div>
                    </div>
                </div>

                <!-- 底部一行 -->
                <div class="bottom-row">
                    <div class="product-item" v-for="(item, index) in bottomRowCats" :key="index"
                        @click="handleCatClick(item)">
                        <div class="item-image">
                            <img :src="item.catImage" :alt="item.catUrl" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

import type { FeaturedPavilionDataItem } from '~/types/component'
import type { Cat } from '~/types/home'

const props = defineProps({
    dataInfo: {
        type: Array as PropType<FeaturedPavilionDataItem[]>,
        default: () => []
    }
})

const router = useRouter()

// 计算属性
const pavilionTitle = computed(() => {
    return props.dataInfo?.[0]?.pageName || '特色馆'
})

// 左侧大模块：第一个cat
const mainCat = computed(() => {
    return props.dataInfo?.[0]?.cat || null
})

// 右侧顶部一行：第2-4个cat
const topRowCats = computed(() => {
    const cats = props.dataInfo?.map(item => item.cat).filter(Boolean) || []
    return cats.slice(1, 4)
})

// 右侧底部一行：第5-7个cat
const bottomRowCats = computed(() => {
    const cats = props.dataInfo?.map(item => item.cat).filter(Boolean) || []
    return cats.slice(4, 7)
})

// 方法
const handleMoreClick = () => {
    // 处理"更多"点击事件
    router.push(`/product/list`)
}

const handleCatClick = (cat: Cat) => {
    if (!cat || !cat.catUrl) return
    // 处理分类点击事件，跳转到分类页面
    router.push(`/product/list?catId=${cat.catUrl}`)
}
</script>

<style lang="scss" scoped>
.featured-pavilion-box {
    width: 100%;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 20px;
    margin-bottom: 20px;

    .header-section {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;

        .pavilion-title {
            font-size: 20px;
            font-weight: bold;
            color: var(--color);
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;

            &::before {
                content: '';
                width: 4px;
                height: 20px;
                background-color: var(--color);
                border-radius: 2px;
            }
        }

        .more-link {
            font-size: 14px;
            color: var(--color);
            cursor: pointer;
            transition: all 0.2s ease;

            &:hover {
                color: #333;
            }
        }
    }

    .content-section {
        display: flex;
        gap: 15px;
    }

    .left-section {
        flex: 1;
        min-width: 0;

        .main-product-card {
            width: 100%;
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 0;
            cursor: pointer;
            transition: all 0.2s ease;
            display: flex;
            flex-direction: column;
            height: 100%;

            &:hover {
                background-color: white;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            }

            .product-image {
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                background-color: white;
                border-radius: 6px;
                overflow: hidden;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }
        }
    }

    .right-section {
        flex: 2.7;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 15px;

        .top-row,
        .bottom-row {
            display: flex;
            gap: 15px;
            flex: 1;
        }

        .product-item {
            flex: 1;
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 0;
            cursor: pointer;
            transition: all 0.2s ease;
            display: flex;
            flex-direction: column;
            min-height: 0;
            overflow: hidden;

            &:hover {
                background-color: white;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            }

            .item-image {
                width: 100%;
                flex: 1;
                min-height: 150px;
                display: flex;
                align-items: center;
                justify-content: center;
                background-color: white;
                border-radius: 6px;
                overflow: hidden;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 1200px) {
    .featured-pavilion-box {
        .content-section {
            flex-direction: column;
            gap: 20px;
        }

        .left-section {
            width: 100%;
        }

        .right-section {
            width: 100%;

            .top-row,
            .bottom-row {
                flex-wrap: wrap;
            }

            .product-item {
                flex: 0 0 calc(33.333% - 10px);
            }
        }
    }
}

@media (max-width: 992px) {
    .featured-pavilion-box {
        .right-section {
            .product-item {
                flex: 0 0 calc(50% - 7.5px);
            }
        }
    }
}

@media (max-width: 768px) {
    .featured-pavilion-box {
        padding: 15px;

        .header-section {
            .pavilion-title {
                font-size: 18px;
            }

            .more-link {
                font-size: 12px;
            }
        }

        .right-section {

            .top-row,
            .bottom-row {
                flex-direction: column;
            }

            .product-item {
                flex: 1;
            }
        }
    }
}
</style>