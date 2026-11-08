<template>
    <header class="header">
        <!-- 顶部通知栏（登录/注册/忘记密码页面不显示） -->
        <div v-if="!isAuthHeader" class="top-bar">
            <div class="top-bar-content">
                <div class="top-bar-left">
                    <VisitorRegionPicker />
                    <span class="top-bar-separator">|</span>
                    <span>{{ $t(logo_copywriting_ico_pc_hello)
                    }}</span>
                    <span class="top-bar-separator">|</span>
                    <span v-if="!authHydrated || !isLogin" @click="handleLoginClick">{{ $t('登录') }}</span>
                    <span v-else class="user-name-row">
                        <span @click="handleUserClick" class="user-name">{{ displayUserName }}</span>
                        <div v-if="showHeaderPlusBadge" class="plus-header-entry"
                            @click.stop="handleHeaderPlusEntryClick">
                            <div class="plus-header-entry-pop">
                                <img :src="plusHeaderBadgeImg" alt="" class="plus-header-entry-icon" />
                                <span class="plus-header-entry-text">{{ plusActionText }}</span>
                            </div>
                        </div>
                    </span>
                    <span class="top-bar-separator">|</span>
                    <span v-if="!authHydrated || !isLogin" @click="handleRegisterClick">{{ $t('免费注册') }}</span>
                    <span v-else @click="handleLogoutClick">{{ $t('退出') }}</span>
                </div>
                <div class="top-bar-right">
                    <span @click="UCENTER_URL('/member/messageList')">{{ $t('我的消息') }}</span>
                    <span class="top-bar-separator">|</span>
                    <span @click="UCENTER_URL('/home')">{{ $t('买家中心') }}</span>
                    <span class="top-bar-separator">|</span>
                    <span @click="handleCartClick">{{ $t('购物车') }}</span>
                    <template v-if="showMobileEntry">
                        <span class="top-bar-separator">|</span>
                        <span ref="mobileTriggerRef" @mouseenter="onMobileDropdownEnter"
                            @mouseleave="onMobileDropdownLeave">{{ $t('手机端') }}</span>
                        <Teleport to="body">
                            <div v-show="showMobileDropdown" class="mobile-dropdown-teleport"
                                :style="mobileDropdownStyle" @mouseenter="onMobileDropdownEnter"
                                @mouseleave="onMobileDropdownLeave">
                                <div class="mobile-dropdown">
                                    <div v-for="(item, idx) in mobileQrcodeList" :key="`mobile-qr-${idx}`"
                                        class="mobile-dropdown-item">
                                        <img :src="item.image" alt="" class="mobile-qr" />
                                        <div class="mobile-qr-meta">
                                            <div class="mobile-qr-title">{{ item.title }}</div>
                                            <div class="mobile-qr-tip">{{ item.tips }}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Teleport>
                    </template>
                    <span class="top-bar-separator">|</span>
                    <span v-if="showSupplierCenter" @click="handleSupplierCenterClick">{{ $t('供应商中心') }}</span>
                    <span v-if="showSupplierCenter" class="top-bar-separator">|</span>
                    <span v-if="showBusinessCenter" @click="handleBusinessCenterClick">{{ $t('商家中心') }}</span>
                    <span v-if="showBusinessCenter" class="top-bar-separator">|</span>
                    <template v-if="showLanguageCurrency">
                        <div class="currency-dropdown-wrap" @mouseenter="onCurrencyDropdownEnter"
                            @mouseleave="onCurrencyDropdownLeave">
                            <span ref="currencyTriggerRef" class="currency-trigger">{{ $t('语言/币种') }}<template
                                    v-if="currentCurrencyLabel">({{ currentCurrencyLabel }})</template></span>
                        </div>
                        <Teleport to="body">
                            <div v-show="showCurrencyDropdown" class="currency-dropdown-teleport"
                                :style="currencyDropdownStyle" @mouseenter="onCurrencyDropdownEnter"
                                @mouseleave="onCurrencyDropdownLeave">
                                <div class="currency-dropdown">
                                    <div class="currency-dropdown-tabs">
                                        <span class="currency-dropdown-tab"
                                            :class="{ active: languageCurrencyTab === 'language' }"
                                            @click="languageCurrencyTab = 'language'">{{ $t('选择语言') }}</span>
                                        <span class="currency-dropdown-tab"
                                            :class="{ active: languageCurrencyTab === 'currency' }"
                                            @click="languageCurrencyTab = 'currency'">{{ $t('选择币种') }}</span>
                                    </div>
                                    <template v-if="languageCurrencyTab === 'language'">
                                        <div class="currency-dropdown-list">
                                            <div v-for="item in languageList" :key="item.code" class="currency-item"
                                                :class="{ active: currentLanguageCode === item.code }"
                                                @click="handleSelectLanguage(item)">
                                                <span class="currency-item-left">
                                                    <span class="currency-remark">{{ item.label }}</span>
                                                </span>
                                                <span v-if="currentLanguageCode === item.code"
                                                    class="currency-item-check">✓</span>
                                            </div>
                                        </div>
                                        <div v-if="languageList.length === 0" class="currency-empty">{{ $t('暂无语言数据') }}
                                        </div>
                                    </template>
                                    <template v-else>
                                        <div class="currency-dropdown-list">
                                            <div v-for="item in currencyList" :key="item.id" class="currency-item"
                                                :class="{ active: mallStore.getBaseCurrencySymbolEntity?.id === item.id }"
                                                @click="handleSelectCurrency(item)">
                                                <span class="currency-item-left">
                                                    <span class="currency-symbol">{{ item.currencySymbol }}</span>
                                                    <span class="currency-remark">{{ item.remark || item.currencyCode
                                                    }}</span>
                                                </span>
                                                <span v-if="mallStore.getBaseCurrencySymbolEntity?.id === item.id"
                                                    class="currency-item-check">✓</span>
                                            </div>
                                        </div>
                                        <div v-if="currencyList.length === 0" class="currency-empty">{{ $t('暂无币种数据') }}
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </Teleport>
                    </template>
                </div>
            </div>
        </div>

        <!-- 登录/注册/忘记密码页面：显示简化头部 -->
        <div v-if="isAuthHeader" class="auth-header">
            <div class="auth-header-content">
                <div class="auth-logo-section" @click="navigateTo('/')">
                    <div class="auth-logo-icon">
                        <img v-if="logoUrl" :src="logoUrl" alt="Logo" />
                    </div>
                </div>
                <div class="auth-nav-links">
                    <!-- 登录页：副标题图 + 文案 -->
                    <template v-if="currentPath === '/login'">
                        <img :src="IMAGE_URL('logoSubtitleIcon.png')" alt="" class="auth-subtitle-icon" />
                        <span>{{ $t('请开始您的数字新商业之旅') }}</span>
                    </template>
                    <!-- 注册/忘记密码页：已有账号？登录 -->
                    <template v-else>
                        <span class="auth-hint-text">{{ $t('已有账号？') }}</span>
                        <a :href="ucenterAuthUrl('login')" class="auth-nav-link">{{ $t('登录') }}</a>
                    </template>
                </div>
            </div>
        </div>

        <!-- 协议和文章页面：只显示 top-bar，不显示其他内容 -->
        <!-- 其他页面：显示步骤条或主头部 -->
        <template v-if="!isSimpleHeader && !isAuthHeader && !isPlusPaymentPage">
            <!-- 步骤条（购物车、确认订单、支付页显示） -->
            <div v-if="showStepNav" class="step-nav-container">
                <div class="step-nav-content">
                    <!-- Logo区域 -->
                    <div class="step-logo-section" @click="navigateTo('/')">
                        <div class="step-logo-icon">
                            <img v-if="logoUrl" :src="logoUrl" alt="Logo" />
                        </div>
                    </div>
                    <!-- 步骤条区域 -->
                    <div class="step-nav">
                        <template v-for="(step, index) in stepList" :key="index">
                            <div class="step-item" :class="{ active: currentStep >= step.step }">
                                <div class="step-number">{{ step.step }}</div>
                                <div class="step-title">{{ $t(step.title) }}</div>
                            </div>
                            <div v-if="Number(index) < stepList.length - 1" class="step-line"
                                :class="{ active: currentStep >= (stepList[Number(index) + 1]?.step || 0) }"></div>
                        </template>
                    </div>
                </div>
            </div>

            <!-- 主头部和导航栏（非购物车、确认订单、支付页显示） -->
            <div v-else>
                <!-- 主头部 -->
                <div class="main-header">
                    <div class="main-header-content">
                        <div class="logo-section" @click="navigateTo('/')">
                            <div class="logo-icon">
                                <img v-if="logoUrl" :src="logoUrl" alt="Logo" @error="logoUrl = ''" />
                                <div v-else class="brand-lockup"><span class="brand-symbol">U</span><div><strong>UniMall</strong><span>{{ $t('发现好物，轻松选购') }}</span></div></div>
                            </div>
                        </div>

                        <div class="search-container">
                            <div class="search-box">
                                <button ref="categoryTriggerRef" type="button" class="search-category"
                                    :aria-expanded="showCategoryDropdown" aria-haspopup="listbox"
                                    @click="toggleCategoryDropdown">
                                    <span class="search-category-text">{{ selectedCategoryOption?.label }}</span>
                                    <UIcon name="i-heroicons-chevron-down" class="search-category-chevron"
                                        :class="{ open: showCategoryDropdown }" />
                                </button>

                                <Teleport to="body">
                                    <div v-show="showCategoryDropdown" ref="categoryDropdownRef"
                                        class="category-dropdown-teleport" :style="categoryDropdownStyle">
                                        <div class="category-dropdown" role="listbox" aria-label="$t('搜索类型')">
                                            <div class="category-dropdown-list">
                                                <div v-for="item in categories" :key="item.value"
                                                    class="category-dropdown-item"
                                                    :class="{ active: selectedCategory === item.value }" role="option"
                                                    :aria-selected="selectedCategory === item.value"
                                                    @mousedown.prevent="handleSelectCategory(item.value)">
                                                    <span class="category-dropdown-item-label">{{ item.label }}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </Teleport>
                                <div class="search-input-wrap">
                                    <input v-model="searchQuery" :placeholder="$t('搜索商品名称 / 订货编码')" class="search-input" @keydown.enter="handleSearch"
                                        :class="{ 'search-input-with-camera': showCameraIcon }" />
                                    <template v-if="showCameraIcon">
                                        <input ref="cameraInputRef" type="file" accept="image/*"
                                            class="search-camera-input-hidden" @change="handleCameraImage" />
                                        <span class="search-camera-icon" @click="triggerCameraInput"
                                            :title="$t('以图搜商品')">
                                            <UIcon name="i-heroicons-camera" class="search-camera-icon-svg" />
                                        </span>
                                    </template>
                                </div>
                                <button class="search-btn" @click="handleSearch">{{ $t('搜索') }}</button>
                            </div>
                            <div class="search-tags">
                                <template v-for="(word, index) in hotWords" :key="index">
                                    <span class="hot-word-tag" @click="handleHotWordClick(word)">{{ $t(word) }}</span>
                                    <span class="tag-separator" v-if="Number(index) < hotWords.length - 1">|</span>
                                </template>
                            </div>
                        </div>

                        <div class="header-icons">
                            <div class="icon-item">
                                <button variant="ghost" class="icon-btn" @click="handleAiAssistantClick">
                                    <span class="icon">
                                        <img class="icon-img-ai" :src="IMAGE_URL('/head-ai.png')" alt="" />
                                    </span>
                                    <span class="icon-text">{{ $t('AI助手') }}</span>
                                </button>
                            </div>
                            <div class="icon-item">
                                <button variant="ghost" class="icon-btn" @click="handleCartClick">
                                    <span class="icon-wrap">
                                        <span class="icon">
                                            <img class="icon-img-ai" :src="IMAGE_URL('/head-cart.png')" alt="" />
                                        </span>
                                        <span v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' :
                                            cartCount
                                        }}</span>
                                    </span>
                                    <span class="icon-text">{{ $t('购物车') }}</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 导航栏 -->
                <div class="nav-bar">
                    <div class="nav-bar-content">
                        <div class="nav-category-wrap" @mouseenter="onNavCategoryEnter"
                            @mouseleave="onNavCategoryLeave">
                            <div class="nav-category">
                                <span>{{ $t('所有商品分类') }}</span>
                            </div>
                            <!-- 非首页时悬停显示分类面板 -->
                            <div v-show="showNavCategoryPanel && !isHomePage" class="nav-category-dropdown"
                                @mouseenter="onNavCategoryEnter">
                                <BannerLeftCategory class="nav-category-panel" />
                            </div>
                        </div>
                        <nav class="nav-menu">
                            <template v-for="(nav, index) in platformNav" :key="index">
                                <NuxtLink :to="nav.navUrl || '/'" :target="nav.navNewOpen === 1 ? '_blank' : undefined"
                                    :rel="nav.navNewOpen === 1 ? 'noopener noreferrer' : undefined" class="nav-link">
                                    <img v-if="nav.navImage" :src="nav.navImage" :alt="nav.navTitle" class="nav-link-icon" />
                                    <span>{{ $t(nav.navTitle) }}</span>
                                </NuxtLink>
                            </template>
                            <!-- 分销推广：固定入口（后台 platformNav 未配置时也要能进） -->
                            <NuxtLink to="/distribution" class="nav-link">
                                <span>{{ $t('分销推广') }}</span>
                            </NuxtLink>
                        </nav>
                    </div>
                </div>
            </div>
        </template>
    </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { getIndexSlides } from '~/services/base.service'
import { selectByConfigKey, queryNavLogo, queryCountryAndCurrency } from '~/services/base.service'
import { getToken } from '~/utils/auth'
import { useMallStore, usePaymentStore } from '~/stores'
import { useUserStore } from '~/stores/modules/user'
import { useGoodsStore } from '~/stores/modules/goods'
import { useAuth } from '~/composables/useAuth'
import { usePlus, refreshPlusState } from '~/composables/usePlus'
import { useIsKeySwitch } from '~/composables/useIsKeySwitch'
import { shopJoinAgree } from '~/services/business.service'
import { supplierShopJoinBaseAgree } from '~/services/supplier.service'
import { queryLanguageAll } from '~/services/base.service'
import type { ShopJoinAgreeData } from '~/types/business'
import type { BaseCurrencySymbolEntity } from '~/types/mall'
import { UCENTER_URL, BCENTER_URL, SUPPLIER_URL, TCENTER_URL, IMAGE_URL } from '~/utils/externalUrl'
import { ucenterAuthUrl } from '~/utils/ucenterAuthUrl'
import { useTranslate } from '~/composables/useTranslate'
import { Toast } from '~/composables/useToast'
import VisitorRegionPicker from '~/components/header/VisitorRegionPicker.vue'
import { useVisitorRegion } from '~/composables/useVisitorRegion'

const route = useRoute()
const paymentStore = usePaymentStore()
const { t } = useTranslate()
const { showHeaderPlusBadge, plusActionText, plusStore } = usePlus()
const { initializeVisitorRegion } = useVisitorRegion()

const plusHeaderBadgeImg = computed(() =>
    plusStore.isPlusMember
        ? IMAGE_URL('plusImg/headerPlusYse.png')
        : IMAGE_URL('plusImg/headerplusNo.png')
)
const selectedCategory = ref('1')
const searchQuery = ref('')
const hotWords = ref<string[]>([])
const platformNav = ref<{ navTitle: string; navUrl: string; navNewOpen?: number; navImage?: string }[]>([])
const logoUrl = ref<string>('')
const logo_copywriting_ico_pc_hello = computed(() => useMallStore().getConfigValueByKey('logo_copywriting_ico_pc_hello') || 'Hi~欢迎来到UniMall商城')
const categories = computed(() => [
    { label: t('商品'), value: '1' },
    { label: t('店铺'), value: '2' }
])

// 搜索类型下拉：用自定义浮层替代 USelect
const showCategoryDropdown = ref(false)
const categoryTriggerRef = ref<HTMLElement | null>(null)
const categoryDropdownRef = ref<HTMLElement | null>(null)
const categoryDropdownStyle = ref<{ top: string; left: string; width: string }>({ top: '0px', left: '0px', width: '120px' })

const selectedCategoryOption = computed(() => {
    const found = categories.value.find((x) => x.value === selectedCategory.value)
    return found || categories.value[0]
})

const updateCategoryDropdownPosition = () => {
    const el = categoryTriggerRef.value
    if (!el) return
    const rect = el.getBoundingClientRect()
    categoryDropdownStyle.value = {
        top: `${rect.bottom + 4}px`,
        left: `${rect.left}px`,
        width: `${Math.max(100, rect.width)}px`
    }
}

const openCategoryDropdown = () => {
    updateCategoryDropdownPosition()
    showCategoryDropdown.value = true
}

const closeCategoryDropdown = () => {
    showCategoryDropdown.value = false
}

const toggleCategoryDropdown = () => {
    if (showCategoryDropdown.value) closeCategoryDropdown()
    else openCategoryDropdown()
}

const handleSelectCategory = (value: string) => {
    selectedCategory.value = value
    closeCategoryDropdown()
    categoryTriggerRef.value?.focus?.()
}

const handleDocumentMouseDown = (e: MouseEvent) => {
    if (!showCategoryDropdown.value) return
    const target = e.target as Node | null
    if (!target) return

    const triggerEl = categoryTriggerRef.value
    const dropdownEl = categoryDropdownRef.value

    if (triggerEl?.contains(target)) return
    if (dropdownEl?.contains(target)) return

    closeCategoryDropdown()
}

const handleDocumentKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Escape') closeCategoryDropdown()
}

/**
 * 从路由参数初始化搜索关键字和搜索类型
 */
const initSearchFromRoute = () => {
    // 切换路由时关闭下拉，避免浮层错位或遮挡
    showCategoryDropdown.value = false

    const query = route.query
    const path = route.path

    // 从URL参数中获取搜索关键字
    if (query.kws) {
        const keyword = typeof query.kws === 'string'
            ? query.kws
            : (Array.isArray(query.kws) && query.kws[0] ? query.kws[0] : '')
        if (keyword && typeof keyword === 'string') {
            searchQuery.value = decodeURIComponent(keyword)
        } else {
            searchQuery.value = ''
        }
    } else {
        // 如果没有搜索参数，清空搜索框
        searchQuery.value = ''
    }

    // 根据当前路径判断搜索类型
    if (path.startsWith('/store/list')) {
        // 店铺列表页
        selectedCategory.value = '2'
    } else if (path.startsWith('/product/list')) {
        // 商品列表页
        selectedCategory.value = '1'
    }
    // 其他页面保持当前选择不变
}

// 监听路由变化，同步搜索关键字和类型
watch(() => route.query.kws, () => {
    initSearchFromRoute()
}, { immediate: true })

watch(() => route.path, () => {
    initSearchFromRoute()
}, { immediate: true })

/**
 * 处理搜索按钮点击：按分类直接跳转（店铺/商品列表）
 */
const handleSearch = async () => {
    if (!searchQuery.value || !searchQuery.value.trim()) {
        return
    }
    const keyword = searchQuery.value.trim()
    if (selectedCategory.value === '2') {
        navigateTo(`/store/list?kws=${encodeURIComponent(keyword)}`)
        return
    }
    navigateTo(`/product/list?kws=${encodeURIComponent(keyword)}&t=${Date.now()}`)
}

/**
 * 处理热搜词点击
 * @param word 热搜词
 */
const handleHotWordClick = (word: string) => {
    if (!word || !word.trim()) {
        return
    }
    // 跳转到商品列表页，传递搜索关键词
    navigateTo(`/product/list?kws=${encodeURIComponent(word.trim())}`)
}

const userStore = useUserStore()
const goodsStore = useGoodsStore()
const mallStore = useMallStore()

// 语言/币种下拉：列表与显隐（用 Teleport 挂到 body，避免被 top-bar overflow 裁剪）
const currencyTriggerRef = ref<HTMLElement | null>(null)
const currencyList = ref<BaseCurrencySymbolEntity[]>([])
/** 语言列表（来自 queryLanguageAll，每项为 { code, label }） */
const languageList = ref<{ code: string; label: string }[]>([])
const showCurrencyDropdown = ref(false)
/** 语言/币种下拉当前 Tab：'language' | 'currency' */
const languageCurrencyTab = ref<'language' | 'currency'>('language')
const currencyDropdownStyle = ref<{ top: string; left: string }>({ top: '0px', left: '0px' })
let currencyDropdownHideTimer: ReturnType<typeof setTimeout> | null = null
const currentCurrencyLabel = computed(() => {
    // hydrate 前不展示持久化币种，避免 Header「语言/币种(￥)」与 SSR 不一致
    if (import.meta.client && !(window as any).__HOU_MALL_HYDRATED__) {
        return ''
    }
    return mallStore.getBaseCurrencySymbolEntity?.currencySymbol
        ?? mallStore.getBaseCurrencySymbolEntity?.remark
        ?? ''
})
const currentLanguageCode = computed(() => {
    if (import.meta.client && !(window as any).__HOU_MALL_HYDRATED__) {
        return null
    }
    return mallStore.getLanguageCode ?? null
})

/** 获取国家与货币数据，写入 mall store 并填充下拉列表（status===1 为当前选中） */
const fetchCountryAndCurrency = async () => {
    try {
        const res = await queryCountryAndCurrency({})
        const list = res?.data?.baseCurrencySymbolEntities
        if (Array.isArray(list)) {
            currencyList.value = list
            mallStore.setBaseCurrencySymbolEntities(list)
        }
    } catch (error) {
        console.error('获取国家与货币数据失败:', error)
    }
}

const DEFAULT_LANGUAGE_OPTION = [{ code: 'cn', label: '中文' }] as const

/** 获取语言列表：首项固定中文，其余来自 queryLanguageAll */
const fetchLanguageList = async () => {
    try {
        const res = await queryLanguageAll()
        const raw = res?.data
        if (Array.isArray(raw)) {
            const fromApi = raw
                .map((item) => {
                    const code = Object.keys(item)[0]
                    if (!code) return null
                    return { code, label: item[code] ?? code }
                })
                .filter((x): x is { code: string; label: string } => x != null)
            languageList.value = [...DEFAULT_LANGUAGE_OPTION, ...fromApi]
        } else {
            languageList.value = [...DEFAULT_LANGUAGE_OPTION]
        }
    } catch (error) {
        console.error('获取语言列表失败:', error)
        languageList.value = [...DEFAULT_LANGUAGE_OPTION]
    }
}

/** 根据触发元素位置更新下拉定位（下拉宽 180px，右对齐触发元素） */
const updateCurrencyDropdownPosition = () => {
    const el = currencyTriggerRef.value
    if (el && typeof document !== 'undefined') {
        const rect = el.getBoundingClientRect()
        const minW = 180
        currencyDropdownStyle.value = {
            top: `${rect.bottom + 4}px`,
            left: `${Math.max(8, rect.right - minW)}px`
        }
    }
}

const onCurrencyDropdownEnter = () => {
    if (currencyDropdownHideTimer) {
        clearTimeout(currencyDropdownHideTimer)
        currencyDropdownHideTimer = null
    }
    updateCurrencyDropdownPosition()
    showCurrencyDropdown.value = true
}

const onCurrencyDropdownLeave = () => {
    currencyDropdownHideTimer = setTimeout(() => {
        showCurrencyDropdown.value = false
        currencyDropdownHideTimer = null
    }, 200)
}

/** 选择币种后更新 store 并关闭下拉 */
const handleSelectCurrency = (item: BaseCurrencySymbolEntity) => {
    mallStore.setBaseCurrencySymbolEntity(item)
    showCurrencyDropdown.value = false
}

/** 选择语言后更新 store（由 pinia persist 持久化）；app.vue 以 languageCode 为 key 触发重渲染 */
const handleSelectLanguage = (item: { code: string; label: string }): void => {
    if (!item?.code) return
    mallStore.setLanguageCode(item.code)
    showCurrencyDropdown.value = false
}
const { logout, isLogin: checkLogin } = useAuth()

// 手机端下拉：二维码展示（用 Teleport 挂到 body，避免被 top-bar overflow 裁剪）
const mobileTriggerRef = ref<HTMLElement | null>(null)
const showMobileDropdown = ref(false)
const mobileDropdownStyle = ref<{ top: string; left: string }>({ top: '0px', left: '0px' })
let mobileDropdownHideTimer: ReturnType<typeof setTimeout> | null = null
type MobileQrcodeItem = {
    title: string
    image: string
    tips: string
}
const mobileQrcodeList = ref<MobileQrcodeItem[]>([])
const showMobileEntry = computed(() => mobileQrcodeList.value.length > 0)

const updateMobileDropdownPosition = () => {
    const el = mobileTriggerRef.value
    if (el && typeof document !== 'undefined') {
        const rect = el.getBoundingClientRect()
        const minW = 260
        mobileDropdownStyle.value = {
            top: `${rect.bottom + 6}px`,
            left: `${Math.max(8, rect.right - minW)}px`
        }
    }
}

const onMobileDropdownEnter = () => {
    if (!showMobileEntry.value) return
    if (mobileDropdownHideTimer) {
        clearTimeout(mobileDropdownHideTimer)
        mobileDropdownHideTimer = null
    }
    updateMobileDropdownPosition()
    showMobileDropdown.value = true
}

const onMobileDropdownLeave = () => {
    if (!showMobileEntry.value) return
    mobileDropdownHideTimer = setTimeout(() => {
        showMobileDropdown.value = false
        mobileDropdownHideTimer = null
    }, 200)
}

/**
 * 获取手机端二维码配置（mobile_qrcode_image）：
 * - configValue 为空或不是数组 -> 隐藏手机端入口
 * - 为数组时按 title/image/tips 渲染，过滤无图片项
 */
const fetchMobileQrcodeConfig = async () => {
    try {
        const res = await selectByConfigKey('mobile_qrcode_image')
        const raw = String(res?.data?.configValue || '').trim()
        if (!raw) {
            mobileQrcodeList.value = []
            showMobileDropdown.value = false
            return
        }
        let parsed: unknown = null
        try {
            parsed = JSON.parse(raw)
        } catch {
            parsed = null
        }
        if (!Array.isArray(parsed)) {
            mobileQrcodeList.value = []
            showMobileDropdown.value = false
            return
        }
        const list = parsed
            .map((item: any) => ({
                title: String(item?.title || '').trim(),
                image: String(item?.image || '').trim(),
                tips: String(item?.tips || '').trim()
            }))
            .filter((item: MobileQrcodeItem) => item.image !== '')
        mobileQrcodeList.value = list
        if (!list.length) showMobileDropdown.value = false
    } catch (error) {
        console.error('获取手机端二维码配置失败:', error)
        mobileQrcodeList.value = []
        showMobileDropdown.value = false
    }
}

// 步骤条配置
const stepList = [
    { step: 1, title: '商品确认' },
    { step: 2, title: '提交订单' },
    { step: 3, title: '订单支付' },
    { step: 4, title: '订单完成' }
]

// 登录状态
const isLogin = computed(() => userStore.isLogin)

// 购物车角标数量（来自 goods store，添加购物车后由各页调用 goodsStore.fetchCartCount 刷新）
const cartCount = computed(() => goodsStore.getCartCount)

// 是否显示搜索框右侧相机图标：由 aiImages_switch 配置决定，configValue == 1 时显示
const showCameraIcon = ref(false)
const cameraInputRef = ref<HTMLInputElement | null>(null)

// 是否显示语言/币种：由 useIsKeySwitch('language_switch') 返回值决定
const { isKeyEnabled } = useIsKeySwitch()
const showLanguageCurrency = ref(false)

// 是否首页（首页不显示导航栏悬停分类面板）
const isHomePage = computed(() => route.path === '/')
// 导航栏「所有商品分类」悬停是否显示分类面板
const showNavCategoryPanel = ref(false)
let navCategoryHideTimer: ReturnType<typeof setTimeout> | null = null
const onNavCategoryEnter = () => {
    if (navCategoryHideTimer) {
        clearTimeout(navCategoryHideTimer)
        navCategoryHideTimer = null
    }
    if (!isHomePage.value) showNavCategoryPanel.value = true
}
const onNavCategoryLeave = () => {
    if (navCategoryHideTimer) clearTimeout(navCategoryHideTimer)
    navCategoryHideTimer = setTimeout(() => {
        showNavCategoryPanel.value = false
        navCategoryHideTimer = null
    }, 200)
}

// 商家入驻状态（mall-business/shopBase/info/shop/status/query）
const shopJoinData = ref<ShopJoinAgreeData>({} as ShopJoinAgreeData)
// 供应商入驻状态（mall-supplier/supplierShopJoinBase/agree）
const supplierJoinData = ref<ShopJoinAgreeData>({} as ShopJoinAgreeData)

/**
 * 判定当前用户是否为供应商（与 mall-business 店铺状态一致）
 * @param d 店铺/入驻状态数据
 * @returns 是否满足：shopType === 2 且 shopBaseExist ∈ [1, 2]
 */
const isSupplierUser = (d: ShopJoinAgreeData): boolean => {
    return Number(d.shopType ?? -1) === 2
        && [1, 2].includes(Number(d.shopBaseExist ?? -1))
}

/**
 * 判定当前用户是否为商家（与 mall-supplier 入驻数据一致） 1商家 3品牌
 * @param d 供应商侧入驻状态数据
 * @returns 是否满足：shopType === 1 或 sellerisAdmin === 0 或 shopStatus === 3
 */
const isMerchantUser = (d: ShopJoinAgreeData): boolean => {
    return  [1,3].includes(Number(d.shopType ?? -1)) && Number(d.shopStatus ?? -1) === 3
}

// 是否显示供应商中心入口：是商家则隐藏供应商；两者都不是则两个入口都显示
const showSupplierCenter = computed(() => !isMerchantUser(shopJoinData.value))

// 是否显示商家中心入口：是供应商则隐藏商家；两者都不是则两个入口都显示
const showBusinessCenter = computed(() => !isSupplierUser(supplierJoinData.value))

// 获取商家/供应商入驻状态
const fetchShopJoinStatus = async () => {
    if (!isLogin.value) return
    try {
        const [shopRes, supplierRes] = await Promise.all([
            shopJoinAgree(),
            supplierShopJoinBaseAgree()
        ])
        if (shopRes?.code === 1 && shopRes?.data) {
            shopJoinData.value = shopRes.data
        } else {
            shopJoinData.value.message = shopRes.message || '';
        }
        if (supplierRes?.code === 1 && supplierRes?.data) {
            supplierJoinData.value = supplierRes.data
        } else {
            supplierJoinData.value.message = supplierRes.message || '';
        }
    } catch (error) {
        console.error('获取入驻状态失败:', error)
    }
}

// 点击供应商中心：已是供应商进 SUPPLIER_URL，否则进入驻台
const handleSupplierCenterClick = () => {
    // 入口对应 public/mall/supplier；状态接口异常时也必须允许跳转。
    SUPPLIER_URL('/home')
    return
    if (supplierJoinData.value?.message) {
        Toast.error(supplierJoinData.value?.message)
        return
    }
    if (supplierJoinData.value?.shopType === 1 || supplierJoinData.value?.shopType === 3) {
        Toast.error('您无权限进入供应商中心')
        return
    }
    if (isSupplierUser(supplierJoinData.value)) {
        SUPPLIER_URL('/home')
    } else {
        TCENTER_URL('/steps/index')
    }
}

// 点击商家中心：已是商家进 BCENTER_URL，否则进入驻台
const handleBusinessCenterClick = () => {
    // 入口对应 public/mall/bcenter；状态接口异常时也必须允许跳转。
    BCENTER_URL('/home')
    return
    if (shopJoinData.value?.message) {
        Toast.error(shopJoinData.value.message)
        return
    }
    if (shopJoinData.value?.shopType === 2) {
        Toast.error('您无权限进入商家中心')
        return
    }
    if (isMerchantUser(shopJoinData.value)) {
        BCENTER_URL('/home')
    } else {
        TCENTER_URL('/')
    }
}

// 当前路径
// Nuxt may preserve a trailing slash when a user opens /login/register/ directly.
// Keep header routing decisions stable for both canonical and slash-suffixed URLs.
const currentPath = computed(() => {
    const rawPath = String(route.path || '/')
    if (rawPath === '/') return rawPath
    return rawPath.replace(/\/+$/, '') || '/'
})
const isPlusPaymentPage = computed(() => {
    const path = currentPath.value
    return path.startsWith('/payment/') && (
        String(route.query.plusStatus ?? '') === '1' ||
        !!paymentStore.getPlusPaymentContext
    )
})

// 判断是否是登录/注册/忘记密码页面（显示简化头部）
const isAuthHeader = computed(() => {
    const path = currentPath.value
    return path === '/login' || path === '/login/register' || path === '/login/forgetPassword'
})

// 判断是否是协议、文章、PLUS 会员相关页等（只显示 top-bar，不显示主导航/搜索区）
const isSimpleHeader = computed(() => {
    const path = currentPath.value
    return (
        path.startsWith('/agreement/') ||
        path === '/article' ||
        path.startsWith('/article/') ||
        path === '/member/plus' ||
        path === '/member/plusActivate' ||
        path === '/member/plusRecharge'
    )
})

// 判断是否显示步骤条
const showStepNav = computed(() => {
    if (isSimpleHeader.value || isAuthHeader.value) return false
    const path = currentPath.value
    return path.startsWith('/cart/') || path === '/confirmOrder' || path.startsWith('/payment/') || path === '/points/redeem'
})

// 获取当前步骤
const currentStep = computed(() => {
    const path = currentPath.value
    if (path.startsWith('/cart/')) {
        return 1 // 购物车页：步骤1
    } else if (path === '/confirmOrder' || path === '/points/redeem') {
        return 2 // 确认订单页：步骤2
    } else if (path.startsWith('/payment/')) {
        return 3 // 支付页：步骤3
    }
    return 0
})

// 获取显示的用户名称（优先昵称，然后是用户名，最后是手机号）
const displayUserName = computed(() => {
    const userInfo = userStore.userInfo
    if (!userInfo) return ''

    if (userInfo.userNickname) {
        return userInfo.userNickname
    }
    if (userInfo.userName) {
        return userInfo.userName
    }
    if (userInfo.userMobile) {
        return userInfo.userMobile
    }
    return '用户'
})

const handleLoginClick = async () => {
    await navigateTo(ucenterAuthUrl('login'), { external: true })
}

const handleRegisterClick = async () => {
    await navigateTo(ucenterAuthUrl('register'), { external: true })
}

const handleForgotPasswordClick = async () => {
    await navigateTo(ucenterAuthUrl('forgot-password'), { external: true })
}

const handleCartClick = async () => {
    // 判断是否登录
    if (!isLogin.value) {
        // 未登录，跳转到登录页
        await navigateTo(ucenterAuthUrl('login', '/cart/shopping'), { external: true })
    } else {
        // 已登录，跳转到购物车页
        await navigateTo('/cart/shopping')
    }
}

const handleAiAssistantClick = async () => {
    // 获取认证实例
    const { isLogin: checkLogin } = useAuth()
    // 判断是否登录，如果未登录会自动跳转到登录页，并设置重定向路径
    if (checkLogin('/ai/assistant')) {
        // 已登录，跳转到Ai助手页
        await navigateTo('/ai/assistant')
    }
    // 如果未登录，useAuth().isLogin() 会自动跳转到登录页，无需额外处理
}

const handleUserClick = async () => {
    // 跳转到用户中心页面
    await UCENTER_URL('/home')
}

/**
 * 头部 PLUS 入口点击：已开通会员进入权益/激活页，否则进入开通介绍页。
 * @returns {Promise<void>}
 */
const handleHeaderPlusEntryClick = async () => {
    const path = plusStore.isPlusMember ? '/member/plusActivate' : '/member/plus'
    await navigateTo(path)
}

const handleLogoutClick = async () => {
    await logout()
}

// 获取logo配置
const getLogo = async () => {
    try {
        const res = await selectByConfigKey('setting_logo')
        if (res?.data?.configValue) {
            logoUrl.value = res.data.configValue
        }
    } catch (error) {
        console.error('获取logo失败:', error)
        // 默认为空，或者可以设置一个默认logo
        logoUrl.value = ''
    }
}

// 调用接口获取导航Logo和文案配置
const fetchNavLogoConfigs = async () => {
    try {
        const response = await queryNavLogo({
            configKey: '',
            configType: 'logo_copywriting'
        });

        if (response.code === 1 && response.data) {
            const mallStore = useMallStore();
            mallStore.setNavLogoConfigs(response.data);
        }
    } catch (error) {
        console.error('获取导航Logo和文案配置失败:', error);
    }
}

/**
 * 获取 aiImages_switch 配置，决定是否显示搜索框相机图标
 * 传参 configKey: aiImages_switch，当 configValue == 1 时显示
 */
const fetchAiImagesSwitch = async () => {
    try {
        const res = await selectByConfigKey('aiImages_switch')
        const v = res?.data?.configValue
        showCameraIcon.value = v === 1 || v === '1'
    } catch (_) {
        showCameraIcon.value = false
    }
}

/**
 * 触发隐藏的图片选择 input
 */
const triggerCameraInput = () => {
    cameraInputRef.value?.click()
}

/**
 * 相机图标选择图片后：读取为 base64，写入 sessionStorage，跳转商品列表页（image=true）
 * 商品列表页会从 sessionStorage 取 searchBase64Str 并以 base64Str 传给搜索接口
 */
const handleCameraImage = (e: Event) => {
    const target = e.target as HTMLInputElement
    const file = target.files?.[0]
    if (!file || !file.type.startsWith('image/')) {
        target.value = ''
        return
    }
    const reader = new FileReader()
    reader.onload = () => {
        const dataUrl = reader.result as string
        const base64 = dataUrl.includes(',') ? dataUrl.split(',')[1]! : dataUrl
        if (typeof sessionStorage !== 'undefined') {
            sessionStorage.setItem('searchBase64Str', base64)
        }
        navigateTo('/product/list?image=true')
        target.value = ''
    }
    reader.readAsDataURL(file)
}

// 获取首页数据
const getHomeData = async () => {
    try {
        const res = await getIndexSlides({ integralType: 0 })
        const rawHotWords = res?.data?.hotWords
        hotWords.value = Array.isArray(rawHotWords)
            ? rawHotWords
                .map((item: unknown) => {
                    if (typeof item === 'string' || typeof item === 'number') return String(item).trim()
                    if (item && typeof item === 'object') {
                        const value = item as Record<string, unknown>
                        return String(value.name ?? value.keyword ?? value.word ?? value.title ?? value.label ?? '').trim()
                    }
                    return ''
                })
                .filter(Boolean)
            : []
        if (res?.data?.platformNav) {
            platformNav.value = res.data.platformNav
        }
    } catch (error) {
        console.error('获取首页数据失败:', error)
        hotWords.value = []
        platformNav.value = []
    }
}

const authHydrated = ref(false)

// 组件挂载时初始化搜索状态
onMounted(async () => {
    // 先恢复登录态，但仍保持 authHydrated=false，直到本轮 tick 结束再打开，避免 hydration mismatch
    const bootToken = userStore.token || getToken()
    if (bootToken) {
        userStore.restoreLoginState()
    }
    await nextTick()
    authHydrated.value = true
    void initializeVisitorRegion(isLogin.value)
    if (bootToken) {
        void userStore.refreshUserInfo()
    }

    if (typeof document !== 'undefined') {
        document.addEventListener('mousedown', handleDocumentMouseDown)
        document.addEventListener('keydown', handleDocumentKeyDown)
    }

    // 初始化搜索关键字和类型
    initSearchFromRoute()
    // 开关查询尽早并行发起，避免排在慢接口之后被全局超时拖死
    const languageSwitchPromise = isKeyEnabled('language_switch')
    await Promise.all([
        getHomeData(),
        getLogo(),
        fetchMobileQrcodeConfig(),
        fetchNavLogoConfigs()
    ])
    showLanguageCurrency.value = await languageSwitchPromise
    if (showLanguageCurrency.value) {
        await Promise.all([fetchCountryAndCurrency(), fetchLanguageList()])
    }
    await Promise.all([
        fetchShopJoinStatus(),
        goodsStore.fetchCartCount(),
        fetchAiImagesSwitch(),
        refreshPlusState()
    ])
})

onBeforeUnmount(() => {
    if (typeof document !== 'undefined') {
        document.removeEventListener('mousedown', handleDocumentMouseDown)
        document.removeEventListener('keydown', handleDocumentKeyDown)
    }
})

// 登录状态变化时重新获取入驻状态及购物车数量
watch(isLogin, (loggedIn: boolean) => {
    if (loggedIn) {
        userStore.refreshUserInfo()
        fetchShopJoinStatus()
        goodsStore.fetchCartCount()
    } else {
        shopJoinData.value = {} as ShopJoinAgreeData
        supplierJoinData.value = {} as ShopJoinAgreeData
        goodsStore.fetchCartCount()
    }
    refreshPlusState()
})
// 有 token 时：进入页面、路由变化或 token 变化均可刷新用户信息；禁止 immediate，避免 hydration mismatch
watch(
    [() => route.path, () => userStore.token || getToken()],
    ([, token]) => {
        if (!import.meta.client || !authHydrated.value || !token) return
        userStore.refreshUserInfo()
    }
)
</script>

<style lang="scss" scoped>
// Logo公共样式 mixin
@mixin logo-base-style {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: transform 0.3s ease;

    &:hover {
        transform: none;
        opacity: 0.92;
    }
}

@mixin logo-icon-style {
    width: 240px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    img {
        width: 100%;
        height: 100%;
        object-fit: contain;
        display: block;
    }
}

.header {
    background: #fff;
    border-bottom: 1px solid #f0f0f0;

    .auth-header {
        background: #fff;

        .auth-header-content {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 20px 20px;
            gap: 30px;

            .auth-logo-section {
                @include logo-base-style;

                .auth-logo-icon {
                    @include logo-icon-style;
                    width: 230px;
                    /* 200 * 1.15 */
                    height: 58px;
                    /* 50 * 1.15 */
                }
            }

            .auth-nav-links {
                display: flex;
                align-items: center;
                gap: 10px;
                flex-shrink: 0;

                .auth-subtitle-icon {
                    height: 20px;
                    width: auto;
                    display: block;
                    object-fit: contain;
                }

                .auth-hint-text {
                    color: #666;
                    font-size: 15px;
                    margin-right: 6px;
                }

                .auth-nav-link {
                    color: #666;
                    text-decoration: none;
                    font-size: 15px;
                    font-weight: 500;
                    padding: 8px 20px;
                    border-radius: 6px;
                    transition: all 0.3s ease;
                    position: relative;

                    &:hover {
                        color: var(--color);
                        background: var(--color-primary-soft, rgba(234, 84, 85, 0.08));
                    }

                    &.active {
                        color: var(--color);
                        font-weight: 600;
                        background: var(--color-primary-soft, rgba(234, 84, 85, 0.08));

                        &::after {
                            content: '';
                            position: absolute;
                            bottom: 0;
                            left: 50%;
                            transform: translateX(-50%);
                            width: 60%;
                            height: 2px;
                            background: var(--color);
                            border-radius: 2px;
                        }
                    }
                }

                .auth-nav-separator {
                    color: #e0e0e0;
                    font-size: 14px;
                    margin: 0 8px;
                    flex-shrink: 0;
                }
            }
        }
    }

    .top-bar {
        background: var(--bg-topbar, #2b2b2b);
        padding: 4px 0;
        min-height: 34px;
        display: flex;
        align-items: center;
        border-bottom: none;

        .top-bar-content {
            max-width: 1280px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 13px;
            color: #d7dadd;
            width: 100%;
            gap: 12px;

            .top-bar-left {
                display: flex;
                align-items: center;
                gap: 6px;
                flex-shrink: 0;

                .user-name-row {
                    display: inline-flex;
                    align-items: center;
                    gap: 4px;
                    flex-shrink: 0;
                    line-height: 1;
                }

                .user-name {
                    display: inline-flex;
                    align-items: center;
                    height: 20px;
					vertical-align:middle;
                }

                /* 仅展示图标（无背景）；悬停时在下方浮层展示文案 */
                .plus-header-entry {
                    margin-top: 0;
                    padding: 0;
                    cursor: pointer;
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    max-width: 100%;
                    box-sizing: border-box;
                    flex-shrink: 0;
					vertical-align:middle;
                    .plus-header-entry-pop {
                        position: relative;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        height: 20px;
                        line-height: 1;

                        /* 图标与下方文案之间的悬停桥，避免移入文案时闪灭 */
                        &::after {
                            content: '';
                            position: absolute;
                            left: 0;
                            right: 0;
                            top: 100%;
                            height: 10px;
                        }
                    }

                    .plus-header-entry-icon {
                        display: block;
                        flex-shrink: 0;
                        height: 20px;
                        width: auto;
                        object-fit: contain;
                    }

                    .plus-header-entry-text {
                        position: absolute;
                        top: calc(100% + 10px);
                        left: 50%;
                        transform: translateX(-50%);
                        z-index: 200;
                        min-width: max-content;
                        max-width: 260px;
                        padding: 5px 14px;
                        font-size: 12px;
                        line-height: 1.35;
                        color: #e8dcc8;
                        text-align: center;
                        white-space: normal;
                        word-break: break-all;
                        background: linear-gradient(110deg, #796a57 0%, #37332d 100%);
                        border-radius: 9999px;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
                        opacity: 0;
                        visibility: hidden;
                        pointer-events: none;
                        transition:
                            opacity 0.15s ease,
                            visibility 0.15s ease;
                    }

                    .plus-header-entry-pop:hover .plus-header-entry-text {
                        opacity: 1;
                        visibility: visible;
                        pointer-events: auto;
                    }

                    .plus-header-entry-pop:hover .plus-header-entry-icon {
                        filter: brightness(1.06);
                    }
                }
            }

            .top-bar-right {
                display: flex;
                align-items: center;
                gap: 6px;
                flex-wrap: nowrap;
                overflow-x: auto;
                overflow-y: hidden;
                -webkit-overflow-scrolling: touch;
                scrollbar-width: none;
                -ms-overflow-style: none;
                flex: 1;
                justify-content: flex-end;
                min-width: 0;

                &::-webkit-scrollbar {
                    display: none;
                }
            }

            .top-bar-separator {
                color: #666;
                font-size: 10px;
                flex-shrink: 0;
            }

            .currency-dropdown-wrap {
                position: relative;
                display: inline-flex;
                align-items: center;
            }

            .currency-trigger {
                cursor: pointer;
                padding: 2px 6px;
                border-radius: 4px;
                white-space: nowrap;
            }

            .currency-item {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 12px;
                padding: 8px 12px;
                font-size: 12px;
                color: #333;
                cursor: pointer;
                transition: background 0.2s;
            }

            span {
                cursor: pointer;
                transition: all 0.3s ease;
                padding: 2px 6px;
                border-radius: 4px;
                font-weight: 500;
                white-space: nowrap;
                flex-shrink: 0;

                &:hover {
                    color: #fff;
                    background: rgba(255, 255, 255, 0.08);
                }
            }
        }
    }

    .mobile-dropdown-teleport {
        position: fixed;
        z-index: 3000;
    }

    .mobile-dropdown {
        width: 260px;
        background: #fff;
        border: 1px solid rgba(0, 0, 0, 0.08);
        border-radius: 10px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        padding: 10px;
    }

    .mobile-dropdown-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 10px;
        border-radius: 8px;

        &:hover {
            background: rgba(0, 0, 0, 0.03);
        }
    }

    .mobile-qr {
        width: 72px;
        height: 72px;
        border-radius: 6px;
        background: #f6f6f6;
        object-fit: cover;
        flex-shrink: 0;
    }

    .mobile-qr-meta {
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 6px;
    }

    .mobile-qr-title {
        font-size: 13px;
        font-weight: 600;
        color: #333;
        line-height: 1.2;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .main-header {
        background: #fff;
        padding: 18px 0 14px;
        border-bottom: 1px solid var(--border-color, #e6e6e6);
        display: flex;
        align-items: center;
        // border-bottom: 1px solid #f0f0f0;

        .main-header-content {
            max-width: 1280px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 30px;
            width: 100%;
            box-sizing: border-box;

            .logo-section {
                @include logo-base-style;

                .logo-icon {
                    @include logo-icon-style;
                }
            }

            .search-container {
                flex: 1;
                max-width: 711px;
                display: flex;
                flex-direction: column;
                gap: 3px;
                min-width: 0;

                .search-box {
                    display: flex;
                    align-items: center;
                    background: #fff;
                    border-radius: 2px;
                    border: 1px solid #aeb9c2;
                    width: 100%;
                    height: 44px;
                    overflow: hidden;
                    box-sizing: border-box;
                    padding: 2px;
                    gap: 0;
                    transition: border-color 0.2s ease, box-shadow 0.2s ease;

                    &:hover,
                    &:focus-within {
                        border-color: var(--color);
                        box-shadow: 0 0 0 2px var(--rgba2);
                    }

                    .search-category {
                        min-width: auto;
                        color: #606060;
                        height: 100%;
                        font-size: 14px;
                        font-weight: 500;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        padding: 0 4px 0 10px;
                        flex-shrink: 0;
                        background: transparent;
                        border: none;
                        outline: none;
                        gap: 4px;
                        position: relative;

                        // &::after {
                        //     content: '';
                        //     position: absolute;
                        //     right: 0;
                        //     top: 50%;
                        //     transform: translateY(-50%);
                        //     width: 1px;
                        //     height: 16px;
                        //     background: #e8e8e8;
                        // }

                        .search-category-text {
                            white-space: nowrap;
                        }

                        .search-category-chevron {
                            width: 10px;
                            height: 10px;
                            flex-shrink: 0;
                            color: #999;
                            transition: transform 0.2s ease;
                        }

                        .search-category-chevron.open {
                            transform: rotate(180deg);
                        }

                        &:focus-visible {
                            box-shadow: none;
                        }
                    }

                    .search-input-wrap {
                        flex: 1;
                        position: relative;
                        display: flex;
                        align-items: center;
                        min-width: 0;
                        height: 100%;
                    }

                    .search-input {
                        flex: 1;
                        font-size: 14px;
                        border: none;
                        background: transparent;
                        height: 100%;
                        display: flex;
                        align-items: center;
                        min-width: 0;
                        outline: none;
                        padding: 0 12px 0 12px;
                        color: #333;

                        &::placeholder {
                            color: #8f8f8f;
                        }

                        &.search-input-with-camera {
                            padding-right: 40px;
                        }
                    }

                    .search-camera-input-hidden {
                        position: absolute;
                        width: 0;
                        height: 0;
                        opacity: 0;
                        pointer-events: none;
                    }

                    .search-camera-icon {
                        position: absolute;
                        right: 8px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        width: 28px;
                        height: 28px;
                        color: #8f8f8f;
                        cursor: pointer;
                        border-radius: 50%;
                        transition: color 0.2s, background 0.2s;

                        .search-camera-icon-svg {
                            width: 22px;
                            height: 22px;
                        }

                        &:hover {
                            color: var(--color);
                            background: color-mix(in srgb, var(--color) 10%, transparent);
                        }
                    }

                    .search-btn {
                        padding: 0;
                        width: 77px;
                        height: 37px;
                        background: var(--color);
                        color: #fff;
                        font-size: 14px;
                        font-weight: 500;
                        transition: filter 0.2s ease, opacity 0.2s ease;
                        border-radius: 1px;
                        border: none;
                        cursor: pointer;
                        flex-shrink: 0;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        position: relative;
                        overflow: hidden;

                        &::before {
                            display: none;
                        }

                        &:hover {
                            background: var(--color-primary-hover, var(--color));
                            transform: none;
                            box-shadow: none;
                        }

                        &:active {
                            filter: brightness(0.96);
                            transform: none;
                        }
                    }
                }

                .search-tags {
                    display: flex;
                    align-items: center;
                    gap: 0;
                    font-size: 13px;
                    color: #75818b;
                    padding-left: 5px;
                    flex-wrap: wrap;
                    margin-top: 0;
                    line-height: 17px;

                    .hot-word-tag {
                        cursor: pointer;
                        transition: color 0.2s ease;
                        color: #75818b;
                        white-space: nowrap;
                        display: inline-block;

                        &:hover {
                            color: var(--color);
                            text-decoration: none;
                        }
                    }

                    span:not(.tag-separator):not(.hot-word-tag) {
                        cursor: pointer;
                        transition: color 0.2s ease;
                        color: #75818b;
                        white-space: nowrap;
                        display: inline-block;

                        &:hover {
                            color: var(--color);
                        }
                    }

                    .tag-separator {
                        margin: 0 6px;
                        color: #cfcfcf !important;
                        cursor: default;
                        font-size: 12px;
                        user-select: none;
                        display: inline-block !important;
                        opacity: 1 !important;
                        visibility: visible !important;
                        width: auto !important;
                        height: auto !important;
                        line-height: 1;
                        vertical-align: middle;

                        &:hover {
                            color: #cfcfcf !important;
                        }
                    }
                }
            }

            .header-icons {
                display: flex;
                gap: 20px;
                flex-shrink: 0;
                align-items: center;
                justify-content: flex-end;

                .icon-item {
                    .icon-btn {
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        gap: 6px;
                        color: #495057;
                        background-color: transparent;
                        padding: 0;
                        border-radius: 0;
                        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                        position: relative;
                        border: none;

                        .icon-wrap {
                            position: relative;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }

                        .cart-badge {
                            position: absolute;
                            top: -6px;
                            right: -6px;
                            min-width: 18px;
                            height: 18px;
                            padding: 0 5px;
                            font-size: 11px;
                            font-weight: 600;
                            line-height: 18px;
                            text-align: center;
                            color: #fff;
                            background: var(--color-price, var(--color));
                            border-radius: 9px;
                            border: 1px solid #fff;
                            box-sizing: border-box;
							z-index:2;
                        }

                        .icon {
                            
                            width: 40px;
                            
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            
                            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                            position: relative;
                            overflow: hidden;

                            .icon-img-ai {
                                width: 100%;
                                height: 100%;
                                max-width: 45px;
                                max-height: 45px;
                                
                                object-fit: contain;
                                position: relative;
                                z-index: 1;
                            }

                            .icon-header-svg {
                                width: 26px;
                                height: 26px;
                                position: relative;
                                z-index: 1;
                            }

                            &::before {
                                content: '';
                                position: absolute;
                                top: 50%;
                                left: 50%;
                                width: 0;
                                height: 0;
                                border-radius: 50%;
                                background: rgba(255, 255, 255, 0.2);
                                transform: translate(-50%, -50%);
                                transition: width 0.3s ease, height 0.3s ease;
                            }
                        }

                        .icon-text {
                            font-size: 13px;
                            font-weight: 500;
                            color: #2f2f2f;
                            transition: color 0.3s ease;
                        }

                       
                    }
                }
            }
        }
    }

    .nav-bar {
        background: #fff;
        position: relative;
        height: 48px;
        display: flex;
        align-items: center;
        border-bottom: 1px solid #dfe4e8;
        overflow: visible;

        .nav-bar-content {
            max-width: 1280px;
            margin: 0 auto;
            display: flex;
            align-items: center;
            gap: 0;
            height: 100%;
            width: 100%;
            box-sizing: border-box;
            overflow: visible;

            .nav-category-wrap {
                position: relative;
                height: 100%;
                overflow: visible;
            }

            .nav-category-dropdown {
                position: absolute;
                top: 100%;
                left: 0;
                z-index: 1100;
                padding-top: 0;
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
                border-radius: 0 0 8px 8px;
                overflow: visible;
                /* 子分类面板在右侧 750px，需可见才不裁切 */
                background: transparent;

                .nav-category-panel {
                    width: 240px;
                    max-height: 465px;
                    background: #fff;
                    border-radius: 0 0 8px 8px;
                }
            }

            .nav-category {
                width: 240px;
                background: var(--nav-bg-color, var(--color));
                color: var(--nav-text-color, #fff);
                display: flex;
                justify-content: space-between;
                align-items: center;
                gap: 10px;
                font-size: 15px;
                font-weight: 600;
                cursor: pointer;
                height: 100%;
                box-sizing: border-box;
                transition: all 0.3s ease;
                position: relative;
                overflow: hidden;
                border-radius: 0;
				padding: 14px 18px;
                &::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: -100%;
                    width: 100%;
                    height: 100%;
                    background: rgba(255, 255, 255, 0.15);
                    transition: left 0.3s ease;
                }

                &::after {
                    content: '☰';
                    margin-left: 8px;
                    font-size: 18px;
                    position: relative;
                    z-index: 1;
                }

                span {
                    position: relative;
                    z-index: 1;
                }

                &:hover {
                    background: var(--color-primary-hover, var(--color));
                    box-shadow: none;

                    &::before {
                        left: 100%;
                    }
                }
            }

            .nav-menu {
                display: flex;
                gap: 0;
                align-items: center;
                flex: 1;
                padding-left: 10px;
                overflow-x: auto;
                overflow-y: hidden;
                -webkit-overflow-scrolling: touch;
                scrollbar-width: none;
                -ms-overflow-style: none;
                min-width: 0;

                &::-webkit-scrollbar {
                    display: none;
                }

                .nav-link {
                    color: #353535;
                    text-decoration: none;
                    font-size: 15px;
                    font-weight: 600;
                    padding: 0 14px;
                    height: 48px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 4px;
                    border-bottom: 2px solid transparent;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    position: relative;
                    white-space: nowrap;
                    flex-shrink: 0;

                    .nav-link-icon {
                        width: 16px;
                        height: 16px;
                        object-fit: contain;
                        flex-shrink: 0;
                    }

                    &::after {
                        content: '';
                        position: absolute;
                        bottom: 0;
                        left: 50%;
                        width: 0;
                        height: 3px;
                        background: var(--color);
                        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                        transform: translateX(-50%);
                    }

                    &:hover {
                        color: var(--color);
                        background: #f3f5f6;

                        &::after {
                            width: 80%;
                        }
                    }
                }

                .nav-separator {
                    color: #e0e0e0;
                    font-size: 14px;
                    margin: 0 4px;
                    flex-shrink: 0;
                }
            }
        }
    }
}

/* 步骤导航容器样式 */
.step-nav-container {
    background-color: #fff;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .step-nav-content {
        max-width: 1200px;
        margin: 0 auto;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 30px 20px;
        gap: 40px;

        .step-logo-section {
            @include logo-base-style;

            .step-logo-icon {
                @include logo-icon-style;
            }
        }

        .step-nav {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;

            .step-item {
                display: flex;
                flex-direction: column;
                align-items: center;
                position: relative;

                .step-number {
                    width: 36px;
                    height: 36px;
                    border-radius: 50%;
                    background-color: #fff;
                    border: 2px solid #e0e0e0;
                    color: #999;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-weight: bold;
                    font-size: 16px;
                    margin-bottom: 8px;
                }

                .step-title {
                    font-size: 14px;
                    color: #999;
                }

                &.active {
                    .step-number {
                        background-color: var(--color);
                        border-color: var(--color);
                        color: #fff;
                    }

                    .step-title {
                        color: #333;
                        font-weight: bold;
                    }
                }
            }

            .step-line {
                width: 120px;
                height: 2px;
                background-color: #e0e0e0;
                margin: 0 20px;

                &.active {
                    background-color: var(--color);
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 1200px) {
    .header {

        .top-bar-content,
        .main-header-content,
        .nav-bar-content,
        .step-nav-content {
            max-width: 100%;
            padding: 0 15px;
        }

        .main-header {
            .main-header-content {
                gap: 25px;

                .search-container {
                    max-width: 500px;
                }
            }
        }
    }
}

@media (max-width: 968px) {
    .header {
        .main-header {
            .main-header-content {
                flex-wrap: wrap;
                gap: 20px;

                .logo-section {
                    order: 1;
                }

                .search-container {
                    order: 3;
                    max-width: 100%;
                    width: 100%;
                }

                .header-icons {
                    order: 2;
                    gap: 15px;
                }
            }
        }

        .nav-bar {
            .nav-bar-content {
                .nav-menu {
                    overflow-x: auto;
                    padding-left: 15px;
                    gap: 0;
                    -webkit-overflow-scrolling: touch;

                    &::-webkit-scrollbar {
                        display: none;
                    }

                    scrollbar-width: none;

                    .nav-link {
                        padding: 0 15px;
                        font-size: 14px;
                        white-space: nowrap;
                    }

                    .nav-separator {
                        display: none;
                    }
                }
            }
        }
    }
}

@media (max-width: 768px) {
    .header {
        .auth-header {
            .auth-header-content {
                padding: 15px;
                flex-direction: column;
                gap: 20px;

                .auth-logo-section {
                    width: 100%;
                    justify-content: center;

                    .auth-logo-icon {
                        width: 207px;
                        /* 180 * 1.15 */
                        height: 52px;
                        /* 45 * 1.15 */
                    }
                }

                .auth-nav-links {
                    width: 100%;
                    justify-content: center;
                    flex-wrap: wrap;

                    .auth-nav-link {
                        font-size: 14px;
                        padding: 6px 16px;
                    }

                    .auth-nav-separator {
                        margin: 0 6px;
                    }
                }
            }
        }

        .step-nav-container {
            .step-nav-content {
                padding: 20px 15px;
                flex-direction: column;
                gap: 20px;

                .step-logo-section {
                    width: 100%;
                    justify-content: center;

                    .step-logo-icon {
                        width: 200px;
                        height: 50px;
                    }
                }

                .step-nav {
                    flex-wrap: wrap;
                    width: 100%;

                    .step-item {
                        .step-number {
                            width: 30px;
                            height: 30px;
                            font-size: 14px;
                        }

                        .step-title {
                            font-size: 12px;
                        }
                    }

                    .step-line {
                        width: 60px;
                        margin: 0 10px;
                    }
                }
            }
        }

        .top-bar {
            .top-bar-content {
                font-size: 12px;
                padding: 0 10px;

                .top-bar-left,
                .top-bar-right {
                    gap: 8px;
                }

                span {
                    padding: 2px 6px;
                    font-size: 11px;
                }
            }
        }

        .main-header {
            padding: 32px 0;

            .main-header-content {
                padding: 0 15px;
                gap: 15px;

                .logo-section .logo-icon,
                .step-logo-section .step-logo-icon {
                    width: 200px;
                    height: 50px;
                }

                .search-container {
                    .search-box {
                        height: 44px;

                        .search-category {
                            min-width: auto;
                            font-size: 14px;
                        }

                        .search-input :deep(input) {
                            font-size: 14px;
                            padding: 0 12px 0 16px;
                        }

                        .search-btn {
                            width: 77px;
                            height: 37px;
                            padding: 0;
                            font-size: 14px;
                        }
                    }

                    .search-tags {
                        margin-top: 0;
                        gap: 0;
                        font-size: 12px;

                        span {
                            padding: 0;
                        }
                    }
                }

                .header-icons {
                    gap: 12px;

                    .icon-item .icon-btn {
                        padding: 6px 8px;

                        .icon {
                            width: 42px;
                            height: 42px;
                            font-size: 24px;
                        }

                        .icon-text {
                            font-size: 12px;
                        }
                    }
                }
            }
        }

        .nav-bar {
            height: auto;
            min-height: 48px;

            .nav-bar-content {
                padding: 0 15px;
                flex-wrap: wrap;

                .nav-category {
                    width: 100%;
                    height: 44px;
                    border-radius: 0;
                }

                .nav-menu {
                    width: 100%;
                    padding: 10px 0;
                    overflow-x: auto;
                    -webkit-overflow-scrolling: touch;

                    &::-webkit-scrollbar {
                        display: none;
                    }

                    scrollbar-width: none;
                }
            }
        }
    }
}

/* Storefront refresh: a compact brand masthead and a single, confident search action. */
.main-header {
    padding: 22px 0 18px;
    border-bottom: 0;
}

.main-header-content .logo-section .logo-icon {
    justify-content: flex-start;
}

.brand-lockup {
    display: flex;
    align-items: center;
    gap: 11px;
    color: #16191d;
    text-align: left;

    .brand-symbol {
        width: 46px;
        height: 46px;
        border-radius: 14px 14px 14px 4px;
        display: grid;
        place-items: center;
        color: #fff;
        background: var(--color, #e83943);
        font: 800 27px/1 Arial, sans-serif;
        box-shadow: 0 9px 20px color-mix(in srgb, var(--color, #e83943) 24%, transparent);
    }

    div { display: flex; flex-direction: column; gap: 1px; }
    strong { font-size: 25px; line-height: 29px; letter-spacing: -0.8px; }
    div > span { color: #8b9299; font-size: 11px; line-height: 16px; letter-spacing: 1px; }
}

.main-header-content .search-container {
    gap: 7px;
}

.main-header-content .search-container .search-box {
    height: 48px;
    padding: 3px;
    border: 2px solid var(--color, #e83943);
    border-radius: 10px;

    .search-category { padding: 0 13px; color: #34393e; }
    .search-category::after { content: ''; width: 1px; height: 18px; background: #e6e9ec; margin-left: 9px; }
    .search-btn { height: 38px; min-width: 84px; border-radius: 7px; font-weight: 600; }
}

.nav-bar {
    border-top: 1px solid #f0f2f4;
    border-bottom: 1px solid #e6e9ec;
    box-shadow: 0 3px 10px rgba(28, 34, 39, 0.035);
}

.nav-bar .nav-bar-content .nav-category {
    border-radius: 9px 9px 0 0;
}

.nav-bar .nav-bar-content .nav-menu .nav-link {
    font-weight: 600;
    color: #30353a;
    padding: 0 17px;
}

@media (max-width: 768px) {
    .brand-lockup div { display: none; }
    .brand-lockup .brand-symbol { width: 40px; height: 40px; }
}

@media (max-width: 480px) {
    .header {
        .auth-header {
            .auth-header-content {
                padding: 12px;

                .auth-logo-section .auth-logo-icon {
                    width: 173px;
                    /* 150 * 1.15 */
                    height: 46px;
                    /* 40 * 1.15 */
                }

                .auth-nav-links {
                    .auth-nav-link {
                        font-size: 13px;
                        padding: 5px 12px;
                    }

                    .auth-nav-separator {
                        margin: 0 4px;
                        font-size: 12px;
                    }
                }
            }
        }

        .top-bar {
            display: none;
        }

        .main-header {
            .main-header-content {
                .header-icons {
                    gap: 8px;

                    .icon-item .icon-btn {
                        padding: 4px 6px;

                        .icon {
                            width: 38px;
                            height: 38px;
                            font-size: 20px;
                        }

                        .icon-text {
                            display: none;
                        }
                    }
                }
            }
        }

        .nav-bar {
            .nav-bar-content {
                .nav-menu {
                    .nav-link {
                        font-size: 13px;
                        padding: 0 12px;
                    }
                }
            }
        }
    }
}

/* 语言/币种下拉（Teleport 到 body，需独立于 .header 以匹配挂载在 body 的节点） */
.currency-dropdown-teleport {
    position: fixed;
    z-index: 9999;
    min-width: 180px;
}

.currency-dropdown-teleport .currency-dropdown {
    min-width: 180px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(0, 0, 0, 0.06);
    overflow: hidden;
}

.currency-dropdown-teleport .currency-dropdown-tabs {
    display: flex;
    border-bottom: 1px solid #f0f0f0;
}

.currency-dropdown-teleport .currency-dropdown-tab {
    flex: 1;
    padding: 10px 14px;
    font-size: 13px;
    color: #666;
    text-align: center;
    cursor: pointer;
    transition: color 0.15s ease, background 0.15s ease;
}

.currency-dropdown-teleport .currency-dropdown-tab:hover {
    color: var(--color);
    background: var(--color-primary-soft, rgba(234, 84, 85, 0.04));
}

.currency-dropdown-teleport .currency-dropdown-tab.active {
    color: var(--color);
    font-weight: 600;
    background: var(--color-primary-soft, rgba(234, 84, 85, 0.08));
    border-bottom: 2px solid var(--color);
    margin-bottom: -1px;
}

.currency-dropdown-teleport .currency-dropdown-list {
    max-height: 260px;
    overflow-y: auto;
    padding: 4px 0;
}

.currency-dropdown-teleport .currency-dropdown-list::-webkit-scrollbar {
    width: 6px;
}

.currency-dropdown-teleport .currency-dropdown-list::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 3px;
}

.currency-dropdown-teleport .currency-dropdown-list::-webkit-scrollbar-thumb:hover {
    background: #ccc;
}

.currency-dropdown-teleport .currency-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    padding: 10px 14px;
    font-size: 13px;
    color: #333;
    cursor: pointer;
    transition: background 0.15s ease, color 0.15s ease;
}

.currency-dropdown-teleport .currency-item:hover {
    background: var(--color-primary-soft, rgba(234, 84, 85, 0.06));
    color: var(--color);
}

.currency-dropdown-teleport .currency-item.active {
    background: var(--color-primary-soft, rgba(234, 84, 85, 0.1));
    color: var(--color);
}

.currency-dropdown-teleport .currency-item-left {
    display: flex;
    align-items: baseline;
    gap: 8px;
    min-width: 0;
}

.currency-dropdown-teleport .currency-symbol {
    font-weight: 600;
    flex-shrink: 0;
}

.currency-dropdown-teleport .currency-remark {
    color: #888;
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.currency-dropdown-teleport .currency-item:hover .currency-remark,
.currency-dropdown-teleport .currency-item.active .currency-remark {
    color: var(--color);
    opacity: 0.9;
}

.currency-dropdown-teleport .currency-item-check {
    flex-shrink: 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--color);
}

.currency-dropdown-teleport .currency-empty {
    padding: 20px 14px;
    font-size: 12px;
    color: #999;
    text-align: center;
    line-height: 1.5;
}

/* 搜索类型下拉（商品/店铺） */
.category-dropdown-teleport {
    position: fixed;
    z-index: 9999;
}

.category-dropdown-teleport .category-dropdown {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(0, 0, 0, 0.06);
    overflow: hidden;
}

.category-dropdown-teleport .category-dropdown-list {
    padding: 2px 0;
}

.category-dropdown-teleport .category-dropdown-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 8px 12px;
    font-size: 13px;
    color: #333;
    cursor: pointer;
    transition: background 0.15s ease, color 0.15s ease;
    user-select: none;
    box-sizing: border-box;
    border: 1px solid transparent;
    border-radius: 8px;
    margin: 2px 8px;
}

.category-dropdown-teleport .category-dropdown-item:hover {
    background: rgba(0, 0, 0, 0.04);
}

.category-dropdown-teleport .category-dropdown-item.active {
    background: rgba(0, 0, 0, 0.04);
    color: var(--color);
    border-color: var(--color);
    font-weight: 600;
}

.category-dropdown-teleport .category-dropdown-item-label {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    text-align: center;
}

/* 手机端二维码下拉（Teleport 到 body，需要全局选择器避免被 .header 作用域限制） */
.mobile-dropdown-teleport {
    position: fixed;
    z-index: 3000;
}

.mobile-dropdown {
    width: 260px;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 10px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    padding: 10px;
}

.mobile-dropdown-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px;
    border-radius: 8px;
}

.mobile-dropdown-item:hover {
    background: rgba(0, 0, 0, 0.03);
}

.mobile-qr {
    width: 72px;
    height: 72px;
    border-radius: 6px;
    background: #f6f6f6;
    object-fit: cover;
    flex-shrink: 0;
}

.mobile-qr-meta {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.mobile-qr-title {
    font-size: 13px;
    font-weight: 600;
    color: #333;
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.mobile-qr-tip {
    font-size: 12px;
    font-weight: 400;
    color: #999;
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>
