<template>
    <div class="product-page">
        <div class="product-container">
            <!-- 加载状态 -->
            <Loading v-if="loading" :text="$t('加载中...')" />

            <!-- 错误状态 -->
            <div v-else-if="error" class="error-container">
                <div class="error-text">{{ $t(error || '') }}</div>
                <button class="retry-btn" @click="() => fetchGoodsDetail()">{{ $t('重试') }}</button>
            </div>

            <!-- 产品信息 -->
            <div v-else-if="goodsData" class="product-section">
                <ProductInformation :shopDetail="goodsData.shopDetail" :goods-bases="goodsData.goodsBases"
                    :gds-common="goodsData.gdsCommon" :goods-images="goodsData.goodsImages"
                    @buy-now="handleBuyNow" />
                <!-- 国补标识：未命中不渲染任何入口（未实名 / 类目地区不符 / 与券互斥） -->
                <SubsidyBadge v-if="goodsData.goodsBases?.goodsId" :quote="subsidyQuote" />
            </div>
            <!-- 组合商品 -->
            <div v-if="goodsData?.goodsBases?.goodsId" class="product-section">
                <ProductCombination :goods-id="goodsData.goodsBases.goodsId" />
            </div>

            <!-- 产品详情 -->
            <div v-if="goodsData" class="product-section">
                <ProductDetail :shopDetail="goodsData.shopDetail" :mallStore="mallStore"
                    :commonId="goodsData.gdsCommon.commonId" :goodsId="goodsData.goodsBases.goodsId"
                    :commonBody="goodsData.gdsCommon.commonBody"
                    :categoryAttributesList="goodsData.gdsCommon.categoryAttributesList" />
            </div>
        </div>
        <!-- 商品对比栏 -->
        <ProductComparison />
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import SubsidyBadge from '~/components/subsidy/SubsidyBadge.vue'
import { getGoodsDetail } from '~/services/goods.service'
import { fetchSubsidyQuote } from '~/services/subsidy.service'
import { verifyPayable, type SubsidyQuote } from '~/utils/subsidy'
import type { GoodsDetailData } from '~/types/goods'
import { useMallStore } from '~/stores'
// 获取路由参数
const route = useRoute()
const productId = computed(() => route.params.id as string)

const mallStore = useMallStore()

// 预渲染/SSR 与客户端共用：硬刷新时必须带上 /product/:id 的 payload，避免回落首页
const {
    data: goodsData,
    pending: loading,
    error: loadError,
    refresh: fetchGoodsDetail
} = await useAsyncData<GoodsDetailData>(
    () => `mall-pc-product-${productId.value}`,
    async () => {
        const goodsId = Number(productId.value)
        if (!productId.value || Number.isNaN(goodsId) || goodsId <= 0) {
            throw new Error('商品ID不存在')
        }
        const response = await getGoodsDetail({ goodsId })
        if (response.code === 1 && response.data) {
            return response.data
        }
        throw new Error(response.message || '获取商品详情失败')
    },
    { watch: [productId] }
)

const error = computed(() => {
    const err = loadError.value
    if (!err) return null
    return err instanceof Error ? err.message : String(err)
})

// 处理购买事件
const handleBuyNow = () => {
    navigateTo('/confirmOrder')
}

// 国补试算：详情页只做展示，不占额度。
// 插件未安装时返回 null → SubsidyBadge 不渲染，页面行为与加国补前完全一致。
const subsidyQuote = ref<SubsidyQuote | null>(null)

watch([productId, () => goodsData.value], async () => {
    const detail = goodsData.value
    const goodsId = detail?.goodsBases?.goodsId
    if (!goodsId) {
        subsidyQuote.value = null
        return
    }
    const quote = await fetchSubsidyQuote({
        items: [{
            goodsId: Number(goodsId),
            categoryId: String((detail?.gdsCommon as any)?.categoryId ?? ''),
            quantity: 1,
            unitPrice: Number(detail?.goodsBases?.goodsPrice ?? 0)
        }]
    })
    subsidyQuote.value = quote
    // 对账自检：原价 − 国补 应等于应付，对不上只告警不弹窗，避免打扰用户
    if (quote?.hit) {
        const mismatch = verifyPayable(Number(detail?.goodsBases?.goodsPrice ?? 0), quote)
        if (mismatch) console.warn('[national-subsidy]', mismatch)
    }
}, { immediate: true })

// 页面元信息（SEO + Open Graph）
const seoTitle = computed(() => goodsData.value
    ? `${goodsData.value.goodsBases.goodsName} - 商品详情`
    : `产品详情 - ${productId.value}`)
const seoDescription = computed(() => goodsData.value
    ? `查看商品 ${goodsData.value.goodsBases.goodsName} 的详细信息、规格和价格`
    : `查看产品 ${productId.value} 的详细信息、规格和价格`)
const ogImage = computed(() => {
    const imgs = goodsData.value?.goodsImages
    if (!imgs?.length) return undefined
    const first = imgs[0] as { imagesImage?: string }
    return first?.imagesImage || undefined
})
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
    ogImage,
    ogType: 'website'
})
</script>

<style lang="scss" scoped>
.product-page {
    min-height: 100vh;
    padding: 20px 0 40px;
}

.product-container {
    max-width: 1200px;
    margin: 0 auto;
}

.product-section {
    margin-bottom: 20px;
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    transition: box-shadow 0.3s ease;

    &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &:last-child {
        margin-bottom: 0;
    }
}

// 响应式布局
@media (max-width: 1200px) {
    .product-container {
        max-width: 100%;
        padding: 0 16px;
    }
}

@media (max-width: 768px) {
    .product-page {
        padding: 12px 0 24px;
    }

    .product-container {
        padding: 0 12px;
    }

    .product-section {
        margin-bottom: 16px;
        border-radius: 6px;
    }
}

@media (max-width: 480px) {
    .product-page {
        padding: 8px 0 16px;
    }

    .product-container {
        padding: 0 8px;
    }

    .product-section {
        margin-bottom: 12px;
        border-radius: 4px;
    }
}

.error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    padding: 40px;
    background-color: #fff;
    border-radius: 8px;
}

.error-text {
    font-size: 16px;
    color: #666;
    margin-bottom: 20px;
}

.error-text {
    color: #f5222d;
}

.retry-btn {
    padding: 8px 24px;
    font-size: 14px;
    color: #fff;
    background-color: var(--color);
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
        background-color: #40a9ff;
        transform: translateY(-1px);
        box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
    }

    &:active {
        transform: translateY(0);
    }
}
</style>