<template>
    <div v-if="list.length > 0 || loading" class="coupon-section">
        <span class="label">{{ $t('优惠券') }}:</span>
        <Loading v-if="loading" compact :text="$t('加载中...')" />
        <div v-else class="coupon-content">
            <div class="coupon-list">
                <span v-for="item in displayList" :key="item.voucherTId" class="coupon-tag"
                    :class="{ 'coupon-tag--disabled': isDisabled(item), 'coupon-tag--receive': !isDisabled(item) && item.receiveOrNot === 0 }"
                    @click="handleCouponItemClick(item)">
                    <span class="coupon-tag-amount">{{ formatPriceWithSymbolSafe(item.voucherTPrice) }}</span>
                    <span class="coupon-tag-desc">{{ $t('满') }}{{ item.voucherTLimit || 0 }}{{ $t('可用') }}</span>
                    <span class="coupon-tag-status status-unused" v-if="item.receiveOrNot === 1">{{ $t('未使用') }}</span>
                    <span class="coupon-tag-status status-used" v-else-if="item.receiveOrNot === 3">{{ $t('已使用')
                    }}</span>
                    <span class="coupon-tag-status status-expired" v-else-if="item.receiveOrNot === 4">{{ $t('已过期')
                    }}</span>
                    <span class="coupon-tag-status status-receive" v-else-if="item.receiveOrNot === 0">{{ $t('领取')
                    }}</span>
                </span>
            </div>
            <button v-if="list.length > maxDisplay" type="button" class="coupon-more" @click="openDrawer"
                :aria-label="$t('更多优惠券')">
                <svg class="more-arrow" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor"
                    stroke-width="2" aria-hidden="true">
                    <path d="M9 18l6-6-6-6" />
                </svg>
            </button>
        </div>

        <!-- 抽屉弹窗 -->
        <Teleport to="body">
            <Transition name="drawer-fade">
                <div v-if="drawerVisible" class="coupon-drawer-overlay" @click.self="closeDrawer">
                    <div class="coupon-drawer" :class="{ 'drawer-visible': drawerVisible }">
                        <div class="drawer-header">
                            <div class="drawer-title">
                                <span class="title-bar"></span>
                                <span>{{ $t('优惠券') }}</span>
                            </div>
                            <button type="button" class="drawer-close" @click="closeDrawer" aria-label="关闭">
                                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor"
                                    stroke-width="2">
                                    <path d="M18 6L6 18M6 6l12 12" />
                                </svg>
                            </button>
                        </div>

                        <div class="drawer-tabs">
                            <div class="tab-item" :class="{ active: activeTab === 'available' }"
                                @click="activeTab = 'available'">
                                <span>{{ $t('可领取的券') }}</span>
                                <span class="tab-line" v-if="activeTab === 'available'"></span>
                            </div>
                            <div class="tab-item" :class="{ active: activeTab === 'received' }"
                                @click="activeTab = 'received'">
                                <span>{{ $t('已领取的券') }}</span>
                                <span class="tab-line" v-if="activeTab === 'received'"></span>
                            </div>
                        </div>

                        <div class="drawer-body">
                            <div v-if="activeTab === 'available'" class="coupon-drawer-list">
                                <div v-for="item in availableList" :key="item.voucherTId" class="coupon-card">
                                    <div class="card-tag">{{ $t('优惠券') }}</div>
                                    <div class="card-left">
                                        <span class="card-amount">{{ formatPriceWithSymbolSafe(item.voucherTPrice)
                                            }}</span>
                                    </div>
                                    <div class="card-middle">
                                        <div class="card-desc">
                                            {{ item.voucherTTitle || `${$t('满')}${item.voucherTLimit || 0}${$t('减')}${item.voucherTPrice
                                                || 0}` }}
                                        </div>
                                        <div class="card-date" v-if="item.voucherTStartDate">
                                            {{ $t('有效期始') }}: {{ item.voucherTStartDate }}
                                        </div>
                                        <div class="card-date" v-if="item.voucherTEndDate">
                                            {{ $t('有效期至') }}: {{ item.voucherTEndDate }}
                                        </div>
                                    </div>
                                    <div class="card-right">
                                        <span class="card-action" @click="handleReceive(item)">
                                            {{ canReceive(item) ? $t('领取') : $t('已领取') }}
                                        </span>
                                    </div>
                                </div>
                                <EmptyState v-if="availableList.length === 0" :text="$t('暂无可领取的优惠券')" compact />
                            </div>
                            <div v-else class="coupon-drawer-list">
                                <div v-for="item in receivedList" :key="item.voucherTId" class="coupon-card">
                                    <div class="card-tag">{{ $t('优惠券') }}</div>
                                    <div class="card-left">
                                        <span class="card-amount">{{ formatPriceWithSymbolSafe(item.voucherTPrice)
                                            }}</span>
                                    </div>
                                    <div class="card-middle">
                                        <div class="card-desc">
                                            {{ item.voucherTTitle || `${$t('满')}${item.voucherTLimit ||
                                                0}${$t('减')}${item.voucherTPrice
                                            || 0}` }}
                                        </div>
                                        <div class="card-date" v-if="item.voucherTStartDate">
                                            {{ $t('有效期始') }}: {{ item.voucherTStartDate }}
                                        </div>
                                        <div class="card-date" v-if="item.voucherTEndDate">
                                            {{ $t('有效期至') }}: {{ item.voucherTEndDate }}
                                        </div>
                                    </div>
                                    <div class="card-right">
                                        <span class="card-action received">
                                            {{ getReceivedStatusText(item) }}
                                        </span>
                                    </div>
                                </div>
                                <EmptyState v-if="receivedList.length === 0" :text="$t('暂无已领取的优惠券')" compact />
                            </div>
                        </div>
                    </div>
                </div>
            </Transition>
        </Teleport>
    </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import type { VoucherTemplateVO } from '~/types/goods'

const props = withDefaults(defineProps<{
    list: VoucherTemplateVO[]
    loading?: boolean
}>(), {
    loading: false
})

const emit = defineEmits<{
    receive: [item: VoucherTemplateVO]
}>()

const drawerVisible = ref(false)
const activeTab = ref<'available' | 'received'>('available')

const maxDisplay = 3
const displayList = computed(() => props.list.slice(0, maxDisplay))

// 可领取的券：receiveOrNot === 0
const availableList = computed(() =>
    props.list.filter(item => item.receiveOrNot === 0)
)

// 已领取的券：receiveOrNot 为 1/3/4/5
const receivedList = computed(() =>
    props.list.filter(item => [1, 3, 4, 5].includes(item.receiveOrNot ?? -1))
)

const isDisabled = (item: VoucherTemplateVO) => {
    return [2, 3, 4, 5].includes(item.receiveOrNot ?? -1);
}

const canReceive = (item: VoucherTemplateVO) => {
    return item.receiveOrNot === 0;
}

const getReceivedStatusText = (item: VoucherTemplateVO) => {
    const map: Record<number, string> = {
        1: '未使用',
        3: '已使用',
        4: '已过期',
        5: '回收'
    }
    return map[item.receiveOrNot ?? -1] ?? '已领取'
}


const openDrawer = () => {
    drawerVisible.value = true
    activeTab.value = availableList.value.length > 0 ? 'available' : 'received'
}

const closeDrawer = () => {
    drawerVisible.value = false
}

const handleReceive = (item: VoucherTemplateVO) => {
    if (canReceive(item)) {
        emit('receive', item)
    }
}

const handleCouponItemClick = (item: VoucherTemplateVO) => {
    if (canReceive(item)) {
        emit('receive', item)
    }
}
</script>

<style scoped lang="scss">
.coupon-section {
    display: flex;
    align-items: flex-start;
    align-items: center;
    gap: 12px;
    margin-top: 12px;

    .label {
        font-size: 14px;
        color: #666;
        flex-shrink: 0;
        line-height: 40px;
    }

    .coupon-content {
        display: flex;
        align-items: stretch;
        flex-wrap: wrap;
        gap: 10px;
    }

    .coupon-list {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 8px;
    }

    .coupon-tag {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        border-radius: 4px;
        font-size: 12px;
        border: 1px solid #ffccc7;
        background: #fff5f5;
        color: #333;
        cursor: default;
        transition: border-color 0.2s ease, background 0.2s ease;

        .coupon-tag-amount {
            font-weight: 600;
            color: #ff4d4f;
        }

        .coupon-tag-desc {
            color: #666;
        }

        .coupon-tag-status {
            font-weight: 500;
            margin-left: 2px;

            &.status-unused {
                color: var(--color);
            }

            &.status-used,
            &.status-expired {
                color: #999;
            }

            &.status-receive {
                color: #ff4d4f;
            }
        }

        &--receive {
            cursor: pointer;

            &:hover {
                border-color: #ff4d4f;
                background: #fff1f0;
            }
        }

        &--disabled {
            border-color: #e8e8e8;
            background: #fafafa;
            cursor: default;

            .coupon-tag-amount,
            .coupon-tag-desc {
                color: #999;
            }
        }
    }

    .coupon-more {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 4px 8px;
        min-height: 28px;
        color: var(--color);
        background: #fff;
        border: 1px solid #d9f7be;
        border-radius: 4px;
        cursor: pointer;
        flex-shrink: 0;
        transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;

        .more-arrow {
            flex-shrink: 0;
            transition: transform 0.2s ease;
        }

        &:hover {
            background: rgba(82, 196, 26, 0.08);
            border-color: var(--color);

            .more-arrow {
                transform: translateX(3px);
            }
        }
    }
}

/* 抽屉 */
.coupon-drawer-overlay {
    position: fixed;
    inset: 0;
    z-index: 9999;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    justify-content: flex-end;
}

.coupon-drawer {
    width: 100%;
    max-width: 420px;
    height: 100%;
    background: #fff;
    box-shadow: -4px 0 20px rgba(0, 0, 0, 0.15);
    display: flex;
    flex-direction: column;
    animation: drawerSlideIn 0.3s ease-out;
}

@keyframes drawerSlideIn {
    from {
        transform: translateX(100%);
    }

    to {
        transform: translateX(0);
    }
}

.drawer-fade-enter-active,
.drawer-fade-leave-active {
    transition: opacity 0.3s;
}

.drawer-fade-enter-from,
.drawer-fade-leave-to {
    opacity: 0;

    .coupon-drawer {
        transform: translateX(100%);
    }
}

.drawer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;
}

.drawer-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 18px;
    font-weight: 600;
    color: #333;

    .title-bar {
        width: 4px;
        height: 20px;
        background: var(--color, #52c41a);
        border-radius: 2px;
    }
}

.drawer-close {
    padding: 4px;
    border: none;
    background: none;
    cursor: pointer;
    color: #666;

    &:hover {
        color: #333;
    }
}

.drawer-tabs {
    display: flex;
    flex-direction: row;
    border-bottom: 1px solid #f0f0f0;
}

.tab-item {
    flex: 1;
    padding: 14px 24px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    position: relative;
    text-align: center;

    &.active {
        color: #333;
        font-weight: 500;
    }

    .tab-line {
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60px;
        height: 2px;
        background: var(--color, #52c41a);
    }
}

.drawer-body {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
}

.coupon-drawer-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.coupon-card {
    position: relative;
    display: flex;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    overflow: hidden;
    background: #fff;
    min-height: 100px;

    .card-tag {
        position: absolute;
        top: 8px;
        left: -20px;
        padding: 4px 28px;
        font-size: 12px;
        color: #fff;
        background: #ff7a45;
        transform: rotate(-45deg);
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }

    .card-left {
        width: 90px;
        padding: 20px 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fff;
        border-right: 2px dashed #e8e8e8;

        .card-amount {
            font-size: 24px;
            font-weight: 700;
            color: #ff4d4f;
        }
    }

    .card-middle {
        flex: 1;
        padding: 16px 16px 16px 12px;
        display: flex;
        flex-direction: column;
        gap: 6px;

        .card-desc {
            font-size: 14px;
            color: #333;
            font-weight: 500;
        }

        .card-date {
            font-size: 12px;
            color: #999;
        }
    }

    .card-right {
        width: 70px;
        padding: 16px 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fafafa;
        border-left: 2px dashed #e8e8e8;

        .card-action {
            font-size: 14px;
            color: var(--color);
            font-weight: 500;
            cursor: pointer;

            &.received {
                color: #999;
                cursor: default;
            }
        }
    }
}
</style>
