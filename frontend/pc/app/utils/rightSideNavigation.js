export const RIGHT_SIDE_NAV_ITEMS = Object.freeze([
    { key: 'cart', label: '购物车', icon: 'i-lucide-shopping-cart', target: '/cart/shopping', destination: 'mall', requiresAuth: true, badge: 'cart' },
    { key: 'coupon', label: '优惠券', icon: 'i-lucide-ticket-percent', target: '/property/voucher', destination: 'ucenter', requiresAuth: true, badge: 'coupon' },
    { key: 'order', label: '订单', icon: 'i-lucide-receipt-text', target: '/order/list', destination: 'ucenter', requiresAuth: true },
    { key: 'favorite', label: '收藏', icon: 'i-lucide-star', target: '/attention/collect', destination: 'ucenter', requiresAuth: true },
    { key: 'help', label: '帮助', icon: 'i-lucide-circle-help', target: '/article', destination: 'mall', requiresAuth: false },
    { key: 'customer-service', label: '客服', icon: 'i-lucide-headphones', target: '', destination: 'customer-service', requiresAuth: false }
])

export const formatRailBadge = (value) => {
    const count = Math.max(0, Number(value) || 0)
    return count > 99 ? '99+' : String(Math.floor(count))
}

export const readVoucherCount = (response) => {
    if (![0, 1].includes(Number(response?.code))) return 0
    return Math.max(0, Number(response?.data?.totalCount) || 0)
}
