<template>
    <aside class="right-side-navigation" aria-label="快捷工具栏">
        <nav class="right-side-navigation__items">
            <button
                v-for="item in visibleItems"
                :key="item.key"
                type="button"
                class="rail-item"
                :aria-label="item.label"
                @click="handleItemClick(item)"
                @mouseenter="handleItemMouseEnter(item)"
                @mouseleave="showPhoneTooltip = false"
            >
                <span class="rail-item__icon-wrap">
                    <UIcon :name="item.icon" class="rail-item__icon" aria-hidden="true" />
                    <span v-if="item.badge" class="rail-item__badge">
                        {{ badgeText(item.badge) }}
                    </span>
                </span>
                <span class="rail-item__label">{{ $t(item.label) }}</span>

                <span
                    v-if="item.key === 'customer-service' && showPhoneTooltip && customerConfig?.configValue2"
                    class="rail-item__tooltip"
                    role="tooltip"
                >
                    {{ customerConfig.configValue2 }}
                </span>
            </button>
        </nav>

        <button type="button" class="rail-backtop" aria-label="回到顶部" @click="scrollToTop">
            <UIcon name="i-lucide-arrow-up-to-line" class="rail-backtop__icon" aria-hidden="true" />
        </button>
    </aside>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useAuth } from '~/composables/useAuth'
import { detectingImStatus } from '~/services/base.service'
import { queryUserVoucherList } from '~/services/activity.service'
import type { DetectingImStatusData } from '~/types/base'
import { IMCHAT_URL, UCENTER_URL } from '~/utils/externalUrl'
import {
    RIGHT_SIDE_NAV_ITEMS,
    formatRailBadge,
    readVoucherCount
} from '~/utils/rightSideNavigation.js'
import { useGoodsStore } from '~/stores/modules/goods'
import { useUserStore } from '~/stores/modules/user'

type RailItem = (typeof RIGHT_SIDE_NAV_ITEMS)[number]
type BadgeKey = 'cart' | 'coupon'

const route = useRoute()
const goodsStore = useGoodsStore()
const userStore = useUserStore()
const { isLogin: checkLogin } = useAuth()

const customerConfig = ref<DetectingImStatusData | null>(null)
const couponCount = ref(0)
const showPhoneTooltip = ref(false)

const showCustomerService = computed(() => {
    const config = customerConfig.value
    return !!(config && config.configEnable === 1)
})

const visibleItems = computed(() => RIGHT_SIDE_NAV_ITEMS.filter((item) => (
    item.key !== 'customer-service' || showCustomerService.value
)))

const badgeText = (key: BadgeKey) => {
    return formatRailBadge(key === 'cart' ? goodsStore.getCartCount : couponCount.value)
}

const fetchCustomerServiceConfig = async () => {
    try {
        const response = await detectingImStatus({ configKey: 'customer_service_type' })
        customerConfig.value = response?.code === 1 && response?.data ? response.data : null
    } catch (error) {
        console.error('获取客服配置失败:', error)
        customerConfig.value = null
    }
}

const fetchCouponCount = async () => {
    if (!userStore.isLogin) {
        couponCount.value = 0
        return
    }
    try {
        couponCount.value = readVoucherCount(await queryUserVoucherList())
    } catch (_) {
        couponCount.value = 0
    }
}

const openCustomerService = () => {
    const config = customerConfig.value
    if (!config) return

    if (config.configValue === '1') {
        IMCHAT_URL('/user/chat?isMall=1')
    } else if (config.configValue === '3' && config.configValue2) {
        window.open(config.configValue2, '_blank', 'noopener,noreferrer')
    }
}

const navigateItem = (item: RailItem) => {
    if (item.destination === 'customer-service') {
        openCustomerService()
        return
    }
    if (item.destination === 'ucenter') {
        UCENTER_URL(item.target)
        return
    }
    navigateTo(item.target)
}

const handleItemClick = (item: RailItem) => {
    if (item.requiresAuth && !checkLogin(route.fullPath)) return
    navigateItem(item)
}

const handleItemMouseEnter = (item: RailItem) => {
    showPhoneTooltip.value = item.key === 'customer-service' && customerConfig.value?.configValue === '2'
}

const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
    void fetchCustomerServiceConfig()
    void fetchCouponCount()
})

watch(
    () => userStore.isLogin,
    () => {
        void fetchCouponCount()
    }
)
</script>

<style lang="scss" scoped>
.right-side-navigation {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    z-index: 1020;
    width: 62px;
    background: #fff;
    border-left: 1px solid #e5e7eb;
    box-shadow: -4px 0 14px rgba(15, 23, 42, 0.07);
    color: #4b5563;
    font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Arial, 'PingFang SC', sans-serif;
}

.right-side-navigation__items {
    position: absolute;
    top: 50%;
    right: 0;
    left: 0;
    display: flex;
    flex-direction: column;
    transform: translateY(-50%);
}

.rail-item,
.rail-backtop {
    appearance: none;
    width: 100%;
    margin: 0;
    padding: 0;
    border: 0;
    border-radius: 0;
    background: #fff;
    color: inherit;
    cursor: pointer;
    font: inherit;
}

.rail-item {
    position: relative;
    display: flex;
    min-height: 67px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    transition: background-color 160ms ease, color 160ms ease;

    &::before {
        position: absolute;
        top: 9px;
        bottom: 9px;
        left: 0;
        width: 2px;
        background: #e4002b;
        content: '';
        opacity: 0;
        transform: scaleY(0.35);
        transition: opacity 160ms ease, transform 160ms ease;
    }

    &:hover,
    &:focus-visible {
        background: #f7f7f8;
        color: #111827;
        outline: none;
    }

    &:hover::before,
    &:focus-visible::before {
        opacity: 1;
        transform: scaleY(1);
    }
}

.rail-item__icon-wrap {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
}

.rail-item__icon {
    width: 20px;
    height: 20px;
    color: #59616a;
    stroke-width: 1.6;
}

.rail-item__badge {
    position: absolute;
    top: -7px;
    right: -10px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 17px;
    height: 17px;
    padding: 0 4px;
    border: 1px solid #fff;
    border-radius: 999px;
    background: #e4002b;
    color: #fff;
    font-size: 10px;
    font-weight: 700;
    line-height: 1;
    font-variant-numeric: tabular-nums;
}

.rail-item__label {
    color: #525a63;
    font-size: 13px;
    line-height: 1;
    white-space: nowrap;
}

.rail-item__tooltip {
    position: absolute;
    top: 50%;
    right: calc(100% + 10px);
    padding: 8px 12px;
    border-radius: 2px;
    background: #111827;
    color: #fff;
    font-size: 13px;
    line-height: 1.4;
    white-space: nowrap;
    transform: translateY(-50%);
    box-shadow: 0 8px 20px rgba(15, 23, 42, 0.18);
}

.rail-backtop {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    display: flex;
    height: 51px;
    align-items: center;
    justify-content: center;
    border-top: 1px solid #e5e7eb;
    transition: background-color 160ms ease, color 160ms ease;

    &:hover,
    &:focus-visible {
        background: #f7f7f8;
        color: #e4002b;
        outline: none;
    }
}

.rail-backtop__icon {
    width: 21px;
    height: 21px;
}

@media (max-width: 991px) {
    .right-side-navigation {
        display: none;
    }
}

@media (prefers-reduced-motion: reduce) {
    .rail-item,
    .rail-item::before,
    .rail-backtop {
        transition: none;
    }
}
</style>
