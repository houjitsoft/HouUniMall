<template>
    <div class="store-top-section">
        <!-- 左侧：店铺信息 -->
        <div class="store-header">
            <div class="store-header-left">
                <div class="store-logo">
                    <img :src="shopDetail.shopLogo || shopDetail.wapShopLogo" :alt="shopDetail.shopName" />
                </div>
                <div class="store-info">
                    <h2 class="store-name" :title="$t(shopDetail.shopName || '')">{{ $t(shopDetail.shopName || '') }}</h2>
                    <div class="store-followers">
                        <span class="followers-count">{{ shopDetail.shopCollect || 0 }} {{ $t('人关注') }}</span>
                    </div>
                </div>
            </div>
            <div class="store-header-right">
                <button class="btn-follow" :class="{ active: isFavorite }" @click.stop="handleFollowClick"
                    :disabled="loadingFavorite">
                    <span class="follow-icon">{{ isFavorite ? '❤️' : '🤍' }}</span>
                    <span>{{ isFavorite ? $t('已关注') : $t('关注店铺') }}</span>
                </button>
                <button class="btn-qualification" @click.stop="handleQualificationClick">
                    {{ $t('店铺资质') }}
                </button>
            </div>
        </div>

        <!-- 右侧：店铺横幅 -->
        <div class="store-banner" v-if="bannerImage">
            <img :src="bannerImage" :alt="$t('店铺横幅') || ''" />
        </div>
        <div class="store-banner store-banner-placeholder" v-else>
            <div class="placeholder-content">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path
                        d="M4 16L8.586 11.414C9.367 10.633 10.633 10.633 11.414 11.414L16 16M14 14L15.586 12.414C16.367 11.633 17.633 11.633 18.414 12.414L22 16M2 18H22V20H2V18Z"
                        stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                <span>{{ $t('暂无横幅') }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { ShopDetail } from '~/types/goods'

const props = defineProps<{
    shopDetail: ShopDetail
    isFavorite?: boolean
    bannerImage?: string
    loadingFavorite?: boolean
}>()

const emit = defineEmits<{
    qualificationClick: []
    followClick: []
}>()

const handleFollowClick = () => emit('followClick')
const handleQualificationClick = () => emit('qualificationClick')
</script>

<style lang="scss" scoped>
// 变量定义
$gap-size: 20px;
$gap-size-md: 16px;
$gap-size-sm: 12px;
$border-radius: 12px;
$border-radius-sm: 10px;
$border-radius-xs: 8px;
$transition: all 0.3s ease;

.store-top-section {
    margin-bottom: 20px;
    display: flex;
    gap: $gap-size;
    align-items: stretch;
    width: 100%;
}

.store-header {
    flex: 0 0 calc((4 / 10) * (100% - $gap-size));
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: stretch;
    gap: 10px;
    padding: 14px 16px;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
    border-radius: $border-radius;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transition: $transition;

    &:hover {
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
    }

    .store-header-left {
        display: flex;
        align-items: center;
        gap: $gap-size-md;
        width: 100%;
        min-width: 0;

        .store-logo {
            width: 150px;
            height: 75px;
            border-radius: $border-radius-sm;
            overflow: hidden;
            background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
            flex-shrink: 0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;

            &:hover {
                transform: scale(1.05);
            }

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }

        .store-info {
            flex: 1;
            min-width: 0;

            .store-name {
                font-size: 20px;
                font-weight: 600;
                color: #1a1a1a;
                margin: 0 0 10px 0;
                line-height: 1.4;
                display: block;
                box-sizing: border-box;
                width: 100%;
                max-width: 275px;
                min-width: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .store-followers {
                font-size: 14px;
                color: #666;

                .followers-count {
                    color: #999;
                    font-weight: 500;
                }
            }
        }
    }

    .store-header-right {
        display: flex;
        flex-direction: row;
        gap: 8px;
        width: 100%;
        margin-left: 0;

        .btn-follow,
        .btn-qualification {
            padding: 8px 12px;
            border-radius: $border-radius-xs;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            transition: $transition;
            border: 1.5px solid #e0e0e0;
            background-color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            white-space: nowrap;
            flex: 1;

            &:hover {
                border-color: var(--color);
                color: var(--color);
                transform: translateY(-1px);
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            &:active {
                transform: translateY(0);
            }

            &:disabled {
                opacity: 0.6;
                cursor: not-allowed;
                transform: none;
            }
        }

        .btn-follow {
            &.active {
                background: linear-gradient(135deg, var(--color) 0%, var(--rgba4) 100%);
                border-color: var(--color);
                color: #fff;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

                &:hover {
                    background: linear-gradient(135deg, var(--rgba4) 0%, var(--color) 100%);
                    transform: translateY(-2px);
                    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
                }

                .follow-icon {
                    animation: heartbeat 1.5s ease-in-out infinite;
                }
            }

            .follow-icon {
                font-size: 18px;
                transition: transform 0.3s ease;
            }
        }

        .btn-qualification:hover {
            background-color: #f8f9fa;
        }
    }
}

.store-banner {
    flex: 0 0 calc((6 / 10) * (100% - $gap-size));
    border-radius: $border-radius;
    overflow: hidden;
    background-color: #f5f5f5;
    height: 160px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transition: $transition;

    &:hover {
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
    }

    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    &.store-banner-placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);

        .placeholder-content {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: $gap-size-md;
            color: #999;

            svg {
                opacity: 0.5;
            }

            span {
                font-size: 14px;
            }
        }
    }
}

@keyframes heartbeat {

    0%,
    100% {
        transform: scale(1);
    }

    10%,
    30% {
        transform: scale(1.1);
    }

    20%,
    40% {
        transform: scale(1);
    }
}

/* 响应式 */
@media (max-width: 968px) {
    .store-top-section {
        flex-direction: column;
        gap: $gap-size-md;
    }

    .store-header {
        flex: none;
        width: 100%;
        flex-direction: column;
        align-items: flex-start;
        gap: $gap-size-md;

        .store-header-left {
            width: 100%;

            .store-logo {
                width: 60px;
                height: 60px;
            }

            .store-info .store-name {
                font-size: 18px;
            }
        }

        .store-header-right {
            width: 100%;
            margin-left: 0;
            flex-direction: row;

            .btn-follow,
            .btn-qualification {
                flex: 1;
            }
        }
    }

    .store-banner {
        flex: none;
        width: 100%;
    }
}

@media (max-width: 640px) {
    .store-header {
        padding: $gap-size-md;
        gap: $gap-size-sm;

        .store-header-left {
            gap: $gap-size-sm;
            width: 100%;

            .store-logo {
                width: 50px;
                height: 50px;
            }

            .store-info {
                .store-name {
                    font-size: 16px;
                }

                .store-followers {
                    font-size: 12px;
                }
            }
        }

        .store-header-right {
            width: 100%;
            margin-left: 0;
            flex-direction: row;

            .btn-follow,
            .btn-qualification {
                padding: 10px $gap-size-md;
                font-size: 13px;
                flex: 1;
            }
        }
    }
}
</style>
