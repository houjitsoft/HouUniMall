<template>
    <div class="modal-overlay" v-if="visible" @click="handleCancel">
        <div class="modal-content" @click.stop>
            <div class="modal-header">
                <span class="modal-title">{{ $t('温馨提示') }}</span>
                <span class="modal-close" @click="handleCancel">×</span>
            </div>
            <div class="modal-body">
                <div class="empty-illustration">
                    <span class="cup-icon">☕</span>
                </div>
                <div class="message-text">{{ $t('以下商品在当前地址无货,是否移除无货商品继续购买?') }}</div>
                <div class="product-list">
                    <div v-for="item in outOfStockList" :key="item.goodsId" class="product-item">
                        <img :src="item.goodsImage || 'https://via.placeholder.com/60x60'" :alt="item.goodsName"
                            class="product-image" />
                        <div class="product-info">
                            <div class="product-desc">{{ $t(item.goodsName || '') }} {{ $t(item.goodsSpecifications || '') }}</div>
                            <div class="product-qty">x {{ item.goodsNumber }}</div>
                        </div>
                        <span class="out-of-stock-tag">{{ $t('无货') }}</span>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn-cancel" @click="handleCancel">{{ $t('取消') }}</button>
                <button class="btn-confirm" @click="handleConfirm">{{ $t('确定') }}</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    visible: boolean
    outOfStockList: Array<{
        goodsId: string
        goodsName: string
        goodsSpecifications?: string
        goodsNumber: number
        goodsImage?: string
    }>
}>()

const emit = defineEmits<{
    cancel: []
    confirm: []
}>()

const handleCancel = () => {
    emit('cancel')
}

const handleConfirm = () => {
    emit('confirm')
}
</script>

<style lang="scss" scoped>
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15), 0 0 0 1px color-mix(in srgb, var(--color) 10%, transparent);
    width: 480px;
    max-width: 90%;
    max-height: 80vh;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

.modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    background-color: color-mix(in srgb, var(--color) 6%, white);
    border-bottom: 1px solid color-mix(in srgb, var(--color) 15%, transparent);

    .modal-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
    }

    .modal-close {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: #999;
        font-size: 20px;
        line-height: 1;

        &:hover {
            color: var(--color);
        }
    }
}

.modal-body {
    padding: 24px 20px;
    overflow-y: auto;
    flex: 1;

    .empty-illustration {
        text-align: center;
        margin-bottom: 16px;

        .cup-icon {
            font-size: 48px;
            opacity: 0.3;
        }
    }

    .message-text {
        font-size: 16px;
        color: #333;
        text-align: center;
        margin-bottom: 20px;
        line-height: 1.6;
    }

    .product-list {
        background-color: color-mix(in srgb, var(--color) 4%, white);
        border-radius: 6px;
        padding: 12px;
        max-height: 240px;
        overflow-y: auto;
        border: 1px solid color-mix(in srgb, var(--color) 12%, transparent);

        .product-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 10px 0;
            border-bottom: 1px solid color-mix(in srgb, var(--color) 8%, transparent);

            &:last-child {
                border-bottom: none;
            }

            .product-image {
                width: 60px;
                height: 60px;
                object-fit: cover;
                border-radius: 4px;
                flex-shrink: 0;
            }

            .product-info {
                flex: 1;
                min-width: 0;

                .product-desc {
                    font-size: 14px;
                    color: #333;
                    margin-bottom: 4px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                }

                .product-qty {
                    font-size: 12px;
                    color: #666;
                }
            }

            .out-of-stock-tag {
                font-size: 14px;
                color: var(--color);
                font-weight: 500;
                flex-shrink: 0;
            }
        }
    }
}

.modal-footer {
    display: flex;
    border-top: 1px solid color-mix(in srgb, var(--color) 15%, transparent);

    button {
        flex: 1;
        padding: 14px 20px;
        border: none;
        font-size: 16px;
        cursor: pointer;
        transition: all 0.3s;

        &:focus {
            outline: none;
        }
    }

    .btn-cancel {
        background-color: #fff;
        color: var(--color);
        border: 1px solid var(--color);
        border-radius: 0 0 0 8px;

        &:hover {
            background-color: color-mix(in srgb, var(--color) 8%, white);
        }
    }

    .btn-confirm {
        background-color: var(--color);
        color: #fff;
        border-radius: 0 0 8px 0;

        &:hover {
            background-color: color-mix(in srgb, var(--color) 90%, black);
        }
    }
}
</style>
