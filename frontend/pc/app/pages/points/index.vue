<template>
    <div class="points-page">
        <div class="points-container">
            <!-- 顶部横幅区域：左右布局 -->
            <div class="points-header-section">
                <!-- 左侧：大图 -->
                <div class="points-header-left">
                    <img v-if="indexImage" :src="indexImage" :alt="$t('积分商城')" class="header-image" />
                    <div v-else class="header-image-placeholder">{{ $t('加载中...') }}</div>
                </div>

                <!-- 右侧：上部分为基本信息，下部分为广告图 -->
                <div class="points-header-right">
                    <!-- 上部分：基本信息 -->
                    <div class="points-user-info">
                        <div class="welcome-section">
                            <div class="welcome-icon">
                                <div class="logo-icon" v-if="displayAvatar">
                                    <img :src="displayAvatar" :alt="$t('用户头像')" class="user-logo" />
                                </div>
                                <div class="logo-icon" v-else-if="userLogo">
                                    <img :src="userLogo" :alt="$t('用户logo')" class="user-logo" />
                                </div>
                            </div>
                            <div class="welcome-main">
                                <div class="welcome-userline">{{ displayMemberUserName }}</div>
                                <div class="welcome-levelline">
                                    <span>{{ $t('当前等级') }}:</span>
                                    <strong class="welcome-level-name">{{ displayMemberName }}</strong>
                                   <!-- <span v-if="displayGradeDiscount !== ''" class="welcome-level-discount">({{
                                        displayGradeDiscount }}{{ $t('折') }})</span> -->
                                </div>
                            </div>
                        </div>
                        <div class="plus-member-pill" v-if="displayIsPlusMember === 2">
                            <img :src="IMAGE_URL('pcImg/integral-vip.png')" :alt="$t('超级会员提示') || ''"
                                class="plus-member-pill__image" />
                        </div>
                        <div class="login-register" v-if="!isLogin">
                            <span class="login-link" @click="handleLogin">{{ $t('登录') }}</span>
                            <span class="separator">|</span>
                            <span class="register-link" @click="handleRegister">{{ $t('注册') }}</span>
                        </div>
                        <div class="points-summary" @click="handleIntegralClick">
                            <div class="points-item">
                                <div class="points-value">{{ formatNumber(userIntegral) }}</div>
                                <div class="points-label">{{ $t('我的积分') }}</div>
                            </div>
                            <div class="points-divider"></div>
                            <div class="points-item">
                                <div class="points-value">{{ formatNumber(exchangedCount) }}</div>
                                <div class="points-label">{{ $t('已兑换礼品') }}</div>
                            </div>
                        </div>
                    </div>

                    <!-- 每日签到（插件 userms-checkin）：连续天数 + 立即签到 + 7 天阶梯 -->
                    <div class="checkin-card">
                        <div class="checkin-card__top">
                            <div>
                                <div class="checkin-card__days">
                                    {{ checkinStatus?.continuousDays || 0 }} {{ $t('天') }}
                                </div>
                                <div class="checkin-card__hint">
                                    {{ checkinStatus?.checkedInToday ? $t('今日已签到，明天再来') : $t('连续签到可领更多积分') }}
                                </div>
                            </div>
                            <button class="checkin-card__btn" :disabled="checkinLoading || checkinStatus?.checkedInToday"
                                @click="handleCheckin" type="button">
                                {{ checkinStatus?.checkedInToday ? $t('已签到') : (checkinLoading ? $t('签到中...') : $t('立即签到')) }}
                            </button>
                        </div>
                        <div v-if="checkinStatus?.rewards?.length" class="checkin-card__rewards">
                            <div v-for="r in checkinStatus.rewards" :key="r.dayNo" class="checkin-reward">
                                <div class="checkin-reward__day">{{ $t('第') }}{{ r.dayNo }}{{ $t('天') }}</div>
                                <div class="checkin-reward__points" :class="{ 'is-zero': !(r.points > 0) }">
                                    {{ r.points > 0 ? '+' + r.points : '—' }}
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 下部分：广告图 -->
                    <div class="points-advertising">
                        <img v-if="advertisingImage" :src="advertisingImage" :alt="$t('广告')"
                            class="advertising-image" />
                        <div v-else class="advertising-image-placeholder">{{ $t('加载中...') }}</div>
                    </div>
                </div>
            </div>

            <!-- 热门推荐区域 -->
            <div class="hot-recommend-section">
                <h2 class="hot-recommend-title">{{ $t('热门推荐') }}</h2>
                <div class="product-grid">
                    <div v-for="product in hotProducts" :key="product.commonId || product.goodsId" class="product-item"
                        @click="handleProductClick(product)">
                        <div class="product-image">
                            <img :src="product.commonImage" :alt="product.commonName" />
                        </div>
                        <div class="product-title">{{ $t(product.commonName || '') }}</div>
                        <div class="product-exchange-price">
                            <span class="exchange-label">{{ $t('兑换价') }}</span>
                            <span class="exchange-value">
                                {{ product.needIntegral || 0 }} {{ $t('积分') }}
                                <span v-if="Number(product.commonPrice) > 0">+{{
                                    formatPriceWithSymbolSafe(Number(product.commonPrice))
                                }}</span>
                            </span>
                        </div>
                        <div class="product-reference-price">
                            <span class="reference-label">{{ $t('参考价') }}</span>
                            <span class="reference-value">{{ formatPriceWithSymbolSafe(product.commonMarketPrice)
                                }}</span>
                        </div>
                    </div>
                </div>
                <EmptyState v-if="hotProducts.length === 0 && !loading" :text="$t('暂无热门推荐商品')" compact />
                <Loading v-if="loading" compact :text="$t('加载中...')" />
                <div v-if="totalCount > 30" class="more-button-wrapper">
                    <button class="more-button" @click="handleViewMore">
                        {{ $t('查看更多') }}
                        <span class="arrow">→</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { fetchCheckinStatus, doCheckin, type CheckinStatus } from '~/services/storefront-plugin.service'
import { ref, computed, onMounted } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { useUserStore } from '~/stores/modules/user'
import { formatNumber, formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import { Toast } from '~/composables/useToast'
import { queryIntegralIndex, selectByConfigKey } from '~/services/base.service'
import { queryGoodsFilter } from '~/services/goods.service'
import { IMAGE_URL, UCENTER_URL } from '~/utils/externalUrl'
import { ucenterAuthUrl } from '~/utils/ucenterAuthUrl'
import type { IntegralConfigItem, IntegralResultMap } from '~/types/base'
import type { GoodsFilterQueryItem } from '~/types/goods'

const userStore = useUserStore()

// 响应式数据
const indexImage = ref<string>('')
const advertisingImage = ref<string>('')
const userLogo = ref<string>('')
const hotProducts = ref<GoodsFilterQueryItem[]>([])
const loading = ref<boolean>(false)
const currentPage = ref<number>(1)
const pageSize = ref<number>(30)
const totalCount = ref<number>(0)
const memberProfile = ref<IntegralResultMap>({})

// 计算属性
const isLogin = computed(() => userStore.isLogin)
const userInfo = computed(() => userStore.userInfo)
const userIntegral = computed(() => Number(memberProfile.value.memberIntegral || userStore.userInfo?.userIntegral || 0))
const exchangedCount = computed(() => Number(memberProfile.value.orderCount || 0))
const userAvatar = computed(() => userInfo.value?.userAvatar || '')
const displayAvatar = computed(() => memberProfile.value.userAvatar || userAvatar.value || '')
const displayMemberUserName = computed(() => {
    return memberProfile.value.userName || displayUserName.value || 'Hi'
})
const displayMemberName = computed(() => memberProfile.value.memberName || '普通会员')
const displayGradeDiscount = computed(() => String(memberProfile.value.gradeDiscount || '').trim())
const displayIsPlusMember = computed(() => Number(memberProfile.value.code || 0))
const displayUserName = computed(() => {
    if (!userInfo.value) return ''
    return userInfo.value.userNickname || userInfo.value.userName || userInfo.value.userMobile || '用户'
})

const hasMore = computed(() => {
    return totalCount.value > hotProducts.value.length
})

/**
 * 同步积分到用户状态
 * @param integral 当前积分值
 * @returns void
 */
const syncUserIntegralToStore = (integral: number): void => {
    if (userStore.userInfo) {
        userStore.updateUserInfo({
            userIntegral: integral
        })
    }
}

/**
 * 获取积分商城首页配置数据
 * 调用接口获取左侧大图和右侧广告图的URL
 */
const fetchIntegralIndex = async () => {
    try {
        loading.value = true
        const response = await queryIntegralIndex({})

        if (response.code === 1 && response.data?.list) {
            const configList = response.data.list

            // 查找左侧大图配置
            const indexImageConfig = configList.find((item: IntegralConfigItem) => item.configKey === 'index_integral_pc_image')
            if (indexImageConfig?.configValue) {
                indexImage.value = indexImageConfig.configValue
            }

            // 查找右侧广告图配置
            const advertisingImageConfig = configList.find((item: IntegralConfigItem) => item.configKey === 'index_advertising_pc_image')
            if (advertisingImageConfig?.configValue) {
                advertisingImage.value = advertisingImageConfig.configValue
            }
        }
        if (response.code === 1 && response.data?.resultmap) {
            memberProfile.value = (response.data.resultmap as IntegralResultMap) || {}
            syncUserIntegralToStore(Number(memberProfile.value.memberIntegral || 0))
        }
    } catch (error) {
        console.error('获取积分商城首页配置失败:', error)
    } finally {
        loading.value = false
    }
}

/**
 * 获取用户默认 logo（当无头像时兜底）
 */
const getUserLogo = async () => {
    try {
        const response = await selectByConfigKey('photo_user_logo')
        if (response?.code === 1 && response?.data?.configValue) {
            userLogo.value = response.data.configValue
        } else {
            userLogo.value = ''
        }
    } catch {
        userLogo.value = ''
    }
}

/**
 * 获取热门推荐商品列表
 * 调用接口获取热门推荐商品数据
 */
const fetchHotProducts = async (page: number = 1) => {
    try {
        loading.value = true
        const response = await queryGoodsFilter({
            token: null,
            brandId: '',
            catId: '',
            commonName: '',
            minPrice: '',
            maxPrice: '',
            shopSelfSupport: '',
            orderType: 5,
            commentOrder: 1,
            priceOrder: '',
            saleNum: '',
            currentPage: page,
            pageSize: pageSize.value,
            integralGoods: 0,
            commonIsRecommend: 1
        })

        if (response.code === 1 && response.data?.list) {
            const productList = response.data.list
            if (page === 1) {
                hotProducts.value = productList
            } else {
                hotProducts.value = [...hotProducts.value, ...productList]
            }
            totalCount.value = response.data.totalCount || 0
            currentPage.value = page
        }
    } catch (error) {
        console.error('获取热门推荐商品失败:', error)
    } finally {
        loading.value = false
    }
}

/**
 * 加载更多商品
 */
const loadMore = () => {
    if (hasMore.value && !loading.value) {
        fetchHotProducts(currentPage.value + 1)
    }
}

/**
 * 处理查看更多点击事件
 * 跳转到积分商品列表页
 */
const handleViewMore = () => {
    navigateTo('/points/list')
}

/**
 * 处理商品点击事件
 * @param product 商品信息
 */
const handleProductClick = (product: GoodsFilterQueryItem) => {
    navigateTo(`/points/${product.goodsId || product.commonId}`)
}

/**
 * 处理登录点击事件
 */
const handleLogin = () => {
    navigateTo(ucenterAuthUrl('login', '/points'), { external: true })
}

/**
 * 处理注册点击事件
 */
const handleRegister = () => {
    navigateTo(ucenterAuthUrl('register', '/points'), { external: true })
}

/**
 * 跳转用户中心积分页面
 */
const handleIntegralClick = () => {
    UCENTER_URL('/property/integral')
}

// 组件挂载时获取数据
// 每日签到（插件 userms-checkin）
const checkinStatus = ref<CheckinStatus | null>(null)
const checkinLoading = ref(false)

const loadCheckin = async () => {
    checkinStatus.value = await fetchCheckinStatus()
}

const handleCheckin = async () => {
    if (checkinLoading.value || checkinStatus.value?.checkedInToday) return
    checkinLoading.value = true
    try {
        const res = await doCheckin()
        if (res) {
            Toast.success(`签到成功 +${res.points || 0} 积分`)
            await loadCheckin()
        } else {
            Toast.error('签到失败，请稍后再试')
        }
    } finally {
        checkinLoading.value = false
    }
}

onMounted(() => {
    fetchIntegralIndex()
    fetchHotProducts(1)
    getUserLogo()
    void loadCheckin()
})
</script>

<style lang="scss" scoped>
.points-page {
    width: 100%;
    min-height: 100vh;
    background-color: #f5f5f5;
    padding: 20px 0;

    .points-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    // 顶部横幅区域
    .points-header-section {
        display: flex;
        align-items: flex-start;
        gap: 20px;
        margin-bottom: 30px;

        // 左侧大图
        .points-header-left {
            flex: 1;
            height: 400px;
            border-radius: 8px;
            overflow: hidden;
            background-color: #fff;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

            .header-image {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .header-image-placeholder {
                width: 100%;
                height: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #999;
                background-color: #f0f0f0;
            }
        }

        // 右侧区域
        .points-header-right {
            width: 300px;
            height: 400px;
            display: flex;
            flex-direction: column;
            gap: 16px;

            // 上部分：用户信息
            .points-user-info {
                background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
                border-radius: 12px;
                padding: 16px 18px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
                transition: all 0.3s ease;
                position: relative;
                overflow: hidden;
                flex-shrink: 0;

                // 背景装饰
                &::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    right: 0;
                    width: 100px;
                    height: 100px;
                    background: radial-gradient(circle, color-mix(in srgb, var(--color) 10%, transparent) 0%, transparent 70%);
                    border-radius: 50%;
                    transform: translate(30%, -30%);
                }

                &:hover {
                    box-shadow: 0 6px 14px rgba(0, 0, 0, 0.1);
                    transform: translateY(-1px);
                }

                .welcome-section {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    margin-bottom: 12px;
                    position: relative;
                    z-index: 1;

                    .welcome-icon {
                        width: 46px;
                        height: 46px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        background: #fff;
                        border: 2px solid #e6eef8;
                        border-radius: 50%;
                        filter: grayscale(0);
                        transition: transform 0.3s ease;
                        overflow: hidden;

                        .logo-icon {
                            width: 100%;
                            height: 100%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            overflow: hidden;
                        }

                        .user-logo {
                            width: 100%;
                            height: 100%;
                            border-radius: 50%;
                            object-fit: cover;
                        }

                        &:hover {
                            transform: scale(1.03);
                        }
                    }

                    .welcome-main {
                        min-width: 0;
                        flex: 1;
                    }

                    .welcome-userline {
                        font-size: 14px;
                        color: #111;
                        font-weight: 400;
                        line-height: 1.15;
                        margin-bottom: 4px;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                    }

                    .welcome-levelline {
                        font-size: 14px;
                        color: #333;
                        line-height: 1.2;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }

                    .welcome-level-name {
                        font-weight: 700;
                        margin-left: 4px;
                    }

                    .welcome-level-discount {
                        color: #f08c00;
                        margin-left: 4px;
                        font-size: 13px;
                    }
                }

                .login-register {
                    margin-bottom: 10px;
                    font-size: 13px;
                    position: relative;
                    z-index: 1;

                    .login-link,
                    .register-link {
                        color: var(--color);
                        cursor: pointer;
                        text-decoration: none;
                        font-weight: 500;
                        padding: 4px 8px;
                        border-radius: 4px;
                        transition: all 0.2s ease;
                        position: relative;

                        &:hover {
                            color: var(--color);
                            background-color: color-mix(in srgb, var(--color) 10%, transparent);
                            text-decoration: none;
                        }

                        &:active {
                            transform: scale(0.95);
                        }
                    }

                    .separator {
                        margin: 0 10px;
                        color: #ddd;
                        font-weight: 300;
                    }

                    .user-name {
                        color: #333;
                        font-weight: 600;
                        font-size: 15px;
                        padding: 6px 12px;
                        background: color-mix(in srgb, var(--color) 10%, transparent);
                        border-radius: 6px;
                        display: inline-block;
                    }
                }

                .plus-member-pill {
                    display: inline-flex;
                    align-items: center;
                    width: 100%;
                    min-height: 36px;
                    padding: 0;
                    border-radius: 6px;
                    margin-bottom: 10px;
                    box-sizing: border-box;
                    overflow: hidden;

                    &__image {
                        width: 100%;
                        height: 100%;
                        max-height: 36px;
                        object-fit: cover;
                        display: block;
                    }
                }

                .points-summary {
                    display: flex;
                    align-items: center;
                    justify-content: space-around;
                    padding: 12px 0 2px;
                    border-top: 1px solid #eceff3;
                    position: relative;
                    z-index: 1;
                    background: linear-gradient(to bottom, transparent 0%, rgba(255, 255, 255, 0.5) 100%);
                    cursor: pointer;

                    .points-item {
                        flex: 1;
                        text-align: center;
                        padding: 6px;
                        border-radius: 8px;
                        transition: all 0.3s ease;

                        &:hover {
                            background-color: color-mix(in srgb, var(--color) 5%, transparent);
                            transform: translateY(-2px);
                        }

                        .points-value {
                            font-size: 18px;
                            font-weight: 700;
                            background: var(--color);
                            -webkit-background-clip: text;
                            -webkit-text-fill-color: transparent;
                            background-clip: text;
                            margin-bottom: 4px;
                            line-height: 1.2;
                            letter-spacing: -0.5px;
                            position: relative;

                            // 数字动画效果
                            &::after {
                                content: '';
                                position: absolute;
                                bottom: -2px;
                                left: 50%;
                                transform: translateX(-50%);
                                width: 0;
                                height: 2px;
                                background: var(--color);
                                transition: width 0.3s ease;
                            }
                        }

                        &:hover .points-value::after {
                            width: 60%;
                        }

                        .points-label {
                            font-size: 10px;
                            color: #666;
                            font-weight: 500;
                            margin-top: 2px;
                            letter-spacing: 0.3px;
                        }
                    }

                    .points-divider {
                        width: 1px;
                        height: 34px;
                        background: linear-gradient(to bottom, transparent 0%, #e3e7ed 20%, #e3e7ed 80%, transparent 100%);
                        margin: 0 10px;
                        border-radius: 2px;
                    }
                }
            }

            // 下部分：广告图
            .points-advertising {
                flex: 1;
                min-height: 0;
                border-radius: 8px;
                overflow: hidden;
                background-color: #fff;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

                .advertising-image {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }

                .advertising-image-placeholder {
                    width: 100%;
                    height: 100%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #999;
                    background-color: #f0f0f0;
                }
            }
        }
    }

    // 热门推荐区域
    .hot-recommend-section {
        background-color: #fff;
        border-radius: 8px;
        padding: 30px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .hot-recommend-title {
            text-align: center;
            font-size: 24px;
            font-weight: bold;
            color: var(--color);
            margin-bottom: 30px;
        }

        .product-grid {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 20px;
            margin-bottom: 30px;

            .product-item {
                display: flex;
                flex-direction: column;
                background-color: #f8f9fa;
                border: 1px solid #e9ecef;
                border-radius: 6px;
                padding: 10px;
                cursor: pointer;
                transition: all 0.3s ease;

                &:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                    background-color: #fff;
                }

                .product-image {
                    width: 100%;
                    aspect-ratio: 1 / 1;
                    background-color: #fff;
                    border-radius: 4px;
                    margin-bottom: 10px;
                    overflow: hidden;
                    display: flex;
                    align-items: center;
                    justify-content: center;

                    img {
                        max-width: 100%;
                        max-height: 100%;
                        object-fit: contain;
                    }
                }

                .product-title {
                    font-size: 13px;
                    color: #333;
                    line-height: 1.4;
                    margin-bottom: 10px;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    line-clamp: 2;
                    -webkit-box-orient: vertical;
                    overflow: hidden;
                    height: 38px;
                    font-weight: 500;
                }

                .product-exchange-price {
                    margin-bottom: 8px;
                    font-size: 14px;

                    .exchange-label {
                        color: #666;
                        margin-right: 5px;
                    }

                    .exchange-value {
                        color: var(--color);
                        font-weight: bold;
                    }
                }

                .product-reference-price {
                    font-size: 12px;

                    .reference-label {
                        color: #999;
                        margin-right: 5px;
                    }

                    .reference-value {
                        color: #666;
                        text-decoration: line-through;
                    }
                }
            }
        }

        .more-button-wrapper {
            text-align: center;
            margin-top: 20px;

            .more-button {
                background-color: var(--color);
                color: #fff;
                border: none;
                padding: 12px 30px;
                border-radius: 4px;
                font-size: 14px;
                cursor: pointer;
                transition: background-color 0.3s ease;
                display: inline-flex;
                align-items: center;
                gap: 8px;

                &:hover {
                    background-color: color-mix(in srgb, var(--color) 90%, black);
                }

                .arrow {
                    font-size: 16px;
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 1200px) {
    .points-page {
        .hot-recommend-section {
            .product-grid {
                grid-template-columns: repeat(4, 1fr);
            }
        }
    }
}

@media (max-width: 992px) {
    .points-page {
        .points-header-section {
            flex-direction: column;

            .points-header-right {
                width: 100%;

                .points-user-info {
                    padding: 20px;

                    .welcome-section {
                        .welcome-icon {
                            width: 28px;
                            height: 28px;
                        }

                        .welcome-text {
                            font-size: 14px;
                        }
                    }

                    .points-summary {
                        padding: 16px 0 6px;

                        .points-item {
                            .points-value {
                                font-size: 24px;
                            }

                            .points-label {
                                font-size: 12px;
                            }
                        }

                        .points-divider {
                            height: 40px;
                            margin: 0 10px;
                        }
                    }
                }
            }
        }

        .hot-recommend-section {
            .product-grid {
                grid-template-columns: repeat(3, 1fr);
            }
        }
    }
}

@media (max-width: 768px) {
    .points-page {
        .points-header-section {
            .points-header-right {
                .points-user-info {
                    padding: 18px;

                    .welcome-section {
                        margin-bottom: 16px;
                    }

                    .login-register {
                        margin-bottom: 20px;
                    }

                    .points-summary {
                        flex-direction: column;
                        gap: 16px;
                        padding: 20px 0;

                        .points-item {
                            width: 100%;
                            padding: 12px;
                            background-color: color-mix(in srgb, var(--color) 3%, transparent);
                            border-radius: 8px;

                            .points-value {
                                font-size: 26px;
                            }
                        }

                        .points-divider {
                            width: 60%;
                            height: 2px;
                            margin: 0 auto;
                            background: linear-gradient(to right, transparent 0%, #e0e0e0 20%, #e0e0e0 80%, transparent 100%);
                        }
                    }
                }
            }
        }

        .hot-recommend-section {
            .product-grid {
                grid-template-columns: repeat(2, 1fr);
                gap: 15px;
            }
        }
    }
}

@media (max-width: 480px) {
    .points-page {
        .hot-recommend-section {
            .product-grid {
                grid-template-columns: repeat(1, 1fr);
            }
        }
    }
}

/* 每日签到 */
.checkin-card {
    margin: 12px 0;
    padding: 14px 18px;
    background: #fff7f0;
    border: 1px solid #ffd8bf;
    border-radius: 8px;

    &__top {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    &__days {
        font-size: 20px;
        font-weight: 700;
        color: #ff5000;
    }

    &__hint {
        margin-top: 2px;
        font-size: 12px;
        color: #bb8b6f;
    }

    &__btn {
        height: 34px;
        padding: 0 20px;
        border: none;
        border-radius: 17px;
        background: #ff5000;
        color: #fff;
        font-size: 14px;
        cursor: pointer;

        &:disabled {
            background: #ffb899;
            cursor: not-allowed;
        }
    }

    &__rewards {
        display: flex;
        justify-content: space-between;
        margin-top: 14px;
    }
}

.checkin-reward {
    text-align: center;
    flex: 1;

    &__day {
        font-size: 11px;
        color: #bb8b6f;
    }

    &__points {
        font-size: 13px;
        color: #ff5000;

        &.is-zero {
            color: #ccc;
        }
    }
}
</style>
