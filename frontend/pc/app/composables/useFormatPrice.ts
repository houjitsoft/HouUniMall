/**
 * 价格格式化 Composable
 *
 * 提供价格格式化相关的工具函数，用于统一格式化价格显示。
 * 货币符号优先从 mall store 的 baseCurrencySymbolEntity 读取（首页 queryCountryAndCurrency 写入），
 * 未设置或未传入 symbol 时回退到 DEFAULT_CURRENCY_SYMBOL。
 */

import { useMallStore } from '~/stores/modules/mall'

/** 默认货币符号（store 未加载或未设置时使用） */
export const DEFAULT_CURRENCY_SYMBOL = '¥'

/** 默认小数位数 */
export const DEFAULT_DECIMALS = 2

/** hydration 完成前不用持久化币种，保证与 SSR 默认符号一致 */
function isClientHydrated(): boolean {
    if (import.meta.server) return false
    try {
        return Boolean((window as any).__HOU_MALL_HYDRATED__)
    } catch {
        return false
    }
}

/**
 * 获取当前应使用的货币符号（来自 mall store 或默认值）
 * 用于在未显式传入 symbol 时与 store 中的 baseCurrencySymbolEntity 保持一致
 */
function getDefaultCurrencySymbol(): string {
    if (!isClientHydrated()) {
        return DEFAULT_CURRENCY_SYMBOL
    }
    try {
        const entity = useMallStore().getBaseCurrencySymbolEntity
        return entity?.currencySymbol ?? DEFAULT_CURRENCY_SYMBOL
    } catch {
        return DEFAULT_CURRENCY_SYMBOL
    }
}

/**
 * 获取当前汇率（来自 mall store 的 baseCurrencySymbolEntity.exchangeRate，未设置时为 1）
 * 用于将接口返回的“基础货币”价格换算为展示货币
 */
function getDefaultExchangeRate(): number {
    if (!isClientHydrated()) {
        return 1
    }
    try {
        const entity = useMallStore().getBaseCurrencySymbolEntity
        const rate = entity?.exchangeRate
        return rate !== undefined && rate !== null && !isNaN(Number(rate)) ? Number(rate) : 1
    } catch {
        return 1
    }
}

/**
 * 按当前汇率换算价格（价格 × exchangeRate）
 * 接口价格通常为基础货币，乘以 store 中的汇率后得到展示货币金额
 *
 * @param price 基础货币下的价格
 * @returns 换算后的价格数值；price 为 null/undefined 时返回 0
 */
export const applyExchangeRate = (price: number | undefined | null): number => {
    const p = price === undefined || price === null || isNaN(Number(price)) ? 0 : Number(price)
    return p * getDefaultExchangeRate()
}

/**
 * 格式化价格，保留指定小数位数
 * 
 * @param price 价格数值
 * @param decimals 小数位数，默认为 2
 * @returns 格式化后的价格字符串
 * 
 * @example
 * formatPrice(123.456) // "123.46"
 * formatPrice(123.4) // "123.40"
 * formatPrice(null) // "0.00"
 * formatPrice(123.456, 0) // "123"
 */
export const formatPrice = (price: number | undefined | null, decimals: number = DEFAULT_DECIMALS): string => {
    if (price === undefined || price === null || isNaN(price)) {
        return '0.' + '0'.repeat(decimals)
    }
    return Number(price).toFixed(decimals)
}

/**
 * 格式化价格并添加货币符号（内部按 store 汇率换算后再格式化）
 * symbol 未传时使用 mall store 中的 baseCurrencySymbolEntity.currencySymbol
 *
 * @param price 基础货币下的价格数值，会乘以 exchangeRate 后展示
 * @param symbol 货币符号，不传则从 store 读取
 * @param decimals 小数位数，默认为 2
 * @returns 格式化后的价格字符串（包含货币符号）
 */
export const formatPriceWithSymbol = (
    price: number | undefined | null,
    symbol?: string,
    decimals: number = DEFAULT_DECIMALS
): string => {
    const s = symbol ?? getDefaultCurrencySymbol()
    const displayPrice = applyExchangeRate(price)
    return s + formatPrice(displayPrice, decimals)
}

/**
 * 格式化大额价格，添加千分位分隔符
 * 
 * @param price 价格数值
 * @param decimals 小数位数，默认为 2
 * @returns 格式化后的价格字符串（包含千分位分隔符）
 * 
 * @example
 * formatPriceWithThousands(1234567.89) // "1,234,567.89"
 * formatPriceWithThousands(1234.5) // "1,234.50"
 */
export const formatPriceWithThousands = (
    price: number | undefined | null,
    decimals: number = DEFAULT_DECIMALS
): string => {
    const formatted = formatPrice(price, decimals)
    const parts = formatted.split('.')
    parts[0] = parts[0]!.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    return parts.join('.')
}

/**
 * 格式化价格并添加货币符号和千分位分隔符（内部按 store 汇率换算后再格式化）
 * symbol 未传时从 mall store 读取
 */
export const formatPriceFull = (
    price: number | undefined | null,
    symbol?: string,
    decimals: number = DEFAULT_DECIMALS
): string => {
    const s = symbol ?? getDefaultCurrencySymbol()
    const displayPrice = applyExchangeRate(price)
    return s + formatPriceWithThousands(displayPrice, decimals)
}

/**
 * 格式化价格显示（支持字符串、空值、特殊字符如 '*'）
 * 用于积分商城等需要处理多种输入格式的场景
 *
 * @param price 价格值，支持 number | string | undefined | null
 * @param decimals 小数位数，默认为 2
 * @returns 格式化后的价格字符串
 */
export const formatPriceDisplay = (
    price: number | string | undefined | null,
    decimals: number = DEFAULT_DECIMALS
): string => {
    if (price === undefined || price === null || price === '') {
        return '0.' + '0'.repeat(decimals)
    }
    if (price === '*') {
        return '*.' + '0'.repeat(decimals)
    }
    return formatPrice(Number(price), decimals)
}

/**
 * 格式化价格并添加货币符号（支持字符串输入，内部按 store 汇率换算）
 * symbol 未传时从 mall store 读取
 *
 * @param price 价格值（基础货币），支持 number | string | undefined | null，会按汇率换算后展示
 * @param symbol 货币符号，不传则从 store 读取
 * @param decimals 小数位数，默认为 2
 * @returns 格式化后的价格字符串（包含货币符号）
 */
export const formatPriceWithSymbolSafe = (
    price: number | string | undefined | null,
    symbol?: string,
    decimals: number = DEFAULT_DECIMALS
): string => {
    const s = symbol ?? getDefaultCurrencySymbol()
    if (price === undefined || price === null || price === '') {
        return s + formatPrice(0, decimals)
    }
    if (price === '*') {
        return s + '*.' + '0'.repeat(decimals)
    }
    const num = Number(price)
    const displayPrice = applyExchangeRate(isNaN(num) ? 0 : num)
    return s + formatPrice(displayPrice, decimals)
}

/**
 * 格式化价格或用占位符（当价格为空时）；有价格时按 store 汇率换算后格式化
 * symbol 未传时从 mall store 读取
 *
 * @param price 基础货币下的价格数值，会按汇率换算后展示
 * @param placeholder 占位符，默认为 '--'
 * @param symbol 货币符号，不传则从 store 读取
 * @param decimals 小数位数，默认为 2
 * @returns 格式化后的价格字符串或占位符
 */
export const formatPriceOrPlaceholder = (
    price: number | undefined | null,
    placeholder: string = '--',
    symbol?: string,
    decimals: number = DEFAULT_DECIMALS
): string => {
    if (price === undefined || price === null || isNaN(Number(price))) {
        return placeholder
    }
    const s = symbol ?? getDefaultCurrencySymbol()
    const displayPrice = applyExchangeRate(price)
    return s + formatPrice(displayPrice, decimals)
}

/**
 * 格式化价格（不含货币符号）
 * 等价于 formatPriceWithSymbol(price).replace(symbol, '')
 *
 * @param price 价格数值
 * @param decimals 小数位数，默认为 2
 * @returns 格式化后的价格字符串（不含货币符号）
 */
export const formatPriceWithoutSymbol = (
    price: number | undefined | null,
    decimals: number = DEFAULT_DECIMALS
): string => {
    return formatPrice(price, decimals)
}

/**
 * 格式化数字，添加千分位分隔符
 *
 * @param num 数字值
 * @returns 格式化后的数字字符串
 */
export const formatNumber = (num: number | undefined | null): string => {
    if (num === undefined || num === null) {
        return '0'
    }
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 价格格式化 Composable
 *
 * 返回所有价格格式化相关函数及当前货币符号、汇率（来自 mall store，响应式）。
 * 未传 symbol 的格式化函数会使用 store 中的 baseCurrencySymbolEntity.currencySymbol。
 * 需要按汇率展示时，可先对价格调用 applyExchangeRate(price) 再传入格式化函数。
 */
export const useFormatPrice = () => {
    const mallStore = useMallStore()
    /** 当前货币符号（来自 store，用于模板或需要响应式的场景） */
    const currencySymbol = computed(() => mallStore.getBaseCurrencySymbolEntity?.currencySymbol ?? DEFAULT_CURRENCY_SYMBOL)
    /** 当前汇率（来自 store baseCurrencySymbolEntity.exchangeRate，未设置时为 1） */
    const exchangeRate = computed(() => {
        const entity = mallStore.getBaseCurrencySymbolEntity
        const rate = entity?.exchangeRate
        return rate !== undefined && rate !== null && !isNaN(Number(rate)) ? Number(rate) : 1
    })

    return {
        DEFAULT_CURRENCY_SYMBOL,
        DEFAULT_DECIMALS,
        /** 当前货币符号（与 store 同步） */
        currencySymbol,
        /** 当前汇率（与 store 同步），用于换算展示金额 */
        exchangeRate,
        applyExchangeRate,
        formatPrice,
        formatPriceWithSymbol,
        formatPriceWithSymbolSafe,
        formatPriceOrPlaceholder,
        formatPriceWithThousands,
        formatPriceFull,
        formatPriceDisplay,
        formatPriceWithoutSymbol,
        formatNumber
    }
}
