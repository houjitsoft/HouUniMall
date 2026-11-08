<template>
    <div class="seckill-page">
        <!-- 顶部标题和倒计时区域 -->
        <div class="seckill-header">
            <div class="header-left">
                <span class="seckill-icon">⚡</span>
                <span class="seckill-title">{{ $t('限时秒杀') }}</span>
            </div>
            <div class="header-right">
                <span class="countdown-label">{{ countdownLabel }}</span>
                <span class="countdown-time">{{ formatCountdown(countdownTime) }}</span>
            </div>
        </div>

        <!-- 时间场次选择区域 -->
        <div class="time-slots">
            <div v-for="slot in timeSlots" :key="slot.startTime" class="time-slot-item" :class="{
                active: currentTimeSlot === slot.startTime,
                'coming-soon': slot.status === 'coming',
                'in-progress': slot.status === 'active'
            }" @click="handleTimeSlotChange(slot.startTime)">
                <div class="time-text">{{ slot.timeText }}</div>
                <div class="status-text">{{ slot.statusText }}</div>
            </div>
        </div>

        <!-- 商品列表区域 -->
        <div class="product-list" v-if="!loading && products.length > 0">
            <div class="product-item" v-for="product in products" :key="product.goodsId"
                @click="handleProductClick(product)">
                <!-- 商品图片 -->
                <div class="product-image">
                    <img :src="product.commonImage" :alt="product.commonName" />
                </div>

                <!-- 价格信息 -->
                <div class="product-price">
                    <span class="seckill-price">
                        {{ formatPriceWithSymbolSafe(product.seckillPrice) }}
                    </span>
                    <span class="original-price">
                        {{ formatPriceWithSymbolSafe(product.commonPrice) }}
                    </span>
                </div>

                <!-- 商品名称 -->
                <div class="product-name">{{ $t(product.commonName || '') }}</div>

                <!-- 评价和销量信息 -->
                <div class="product-stats">
                    <span class="comment-count-tag">{{ product.commonEvaluate || 0 }}{{ $t('条好评') }}</span>
                    <span class="sales-text">{{ $t('销售') }}:{{ product.commonSalenum || 0 }}{{ $t('件') }}</span>
                </div>

                <!-- 店铺名称 + 抢购/预约按钮（左右布局） -->
                <div class="shop-action-row">
                    <div class="shop-name" :title="product.shopName">{{ $t(product.shopName || '') }}</div>
                    <!-- 未开始场次：预约提醒按钮 -->
                    <button v-if="product.activityStartStatus === 0" class="seckill-btn"
                        :class="isSubscribed(product.goodsId) ? 'btn-subscribed' : 'btn-coming'"
                        :disabled="subscribingIds.includes(product.goodsId)" @click.stop="handleSubscribeClick(product)">
                        {{ subscribingIds.includes(product.goodsId) ? '处理中...' : (isSubscribed(product.goodsId) ? '已预约' : '开抢提醒') }}
                    </button>
                    <!-- 进行中 / 已结束：抢购按钮 -->
                    <button v-else class="seckill-btn" :class="{
                        'btn-active': product.activityStartStatus === 1 && product.seckillGoodsState === 1,
                        'btn-sold-out': product.activityStartStatus !== 1 || product.seckillGoodsState !== 1
                    }" @click.stop="handleSeckillClick(product)"
                        :disabled="product.activityStartStatus !== 1 || product.seckillGoodsState !== 1">
                        {{ getButtonText(product) }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 加载状态 -->
        <Loading v-if="loading" :text="$t('正在加载...')" />

        <!-- 空状态 -->
        <EmptyState v-if="!loading && products.length === 0" :text="$t('暂无秒杀商品')" />

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="!loading && totalCount > 0">
            <div class="pagination-info">
                <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="pageSize"
                    class="pagination-right" @update:page="handlePageChange" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import {
    querySeckillGoods,
    seckillSubscribe,
    seckillCancelSubscribe,
    seckillMySubscribes
} from '~/services/activity.service'
import { Toast } from '~/composables/useToast'
import type { SeckillProduct, TimeSlot } from '~/types/activity'

// 响应式数据
const products = ref<SeckillProduct[]>([])
const loading = ref(false)
const totalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(60)
const currentTimeSlot = ref<string>('')
const countdownTime = ref(0) // 倒计时剩余时间（毫秒）
const currentTime = ref(Date.now()) // 当前时间，用于倒计时计算

// 时间场次列表
const timeSlots = ref<TimeSlot[]>([])

// 当前用户已预约的秒杀商品 goodsId
const subscribedIds = ref<number[]>([])
// 正在提交预约/取消的 goodsId（防重复点击）
const subscribingIds = ref<number[]>([])

// 倒计时定时器
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 生成时间场次列表（每4小时一段）
const generateTimeSlots = () => {
    const slots: TimeSlot[] = []
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())

    // 定义秒杀时间段，每4小时一段
    // 00:00-04:00, 04:00-08:00, 08:00-12:00, 12:00-16:00, 16:00-20:00, 20:00-24:00
    const timePoints = [
        { hour: 0, minute: 0 },   // 00:00
        { hour: 4, minute: 0 },   // 04:00
        { hour: 8, minute: 0 },   // 08:00
        { hour: 12, minute: 0 },  // 12:00
        { hour: 16, minute: 0 },  // 16:00
        { hour: 20, minute: 0 }   // 20:00
    ]

    // 生成今天和明天的时间场次（最多显示到明日00:00，明日只保留 00:00 这一场）
    for (let day = 0; day < 2; day++) {
        const date = new Date(today)
        date.setDate(date.getDate() + day)

        timePoints.forEach(({ hour, minute }) => {
            // 明日只显示 00:00，不显示 04:00、08:00 等
            if (day === 1 && (hour !== 0 || minute !== 0)) {
                return
            }

            const slotTime = new Date(date)
            slotTime.setHours(hour, minute, 0, 0)

            // 计算时间段结束时间（4小时后）
            const slotEndTime = new Date(slotTime)
            slotEndTime.setHours(slotTime.getHours() + 4)

            // 跳过今天已经结束的时间段
            if (day === 0 && slotEndTime.getTime() <= now.getTime()) {
                return
            }

            const startTimeStr = formatDateTime(slotTime)
            let status: 'active' | 'coming' | 'ended' = 'coming'
            let statusText = '即将开始'

            if (day === 0) {
                // 判断当前时间段状态
                if (now.getTime() >= slotTime.getTime() && now.getTime() < slotEndTime.getTime()) {
                    // 当前正在进行
                    status = 'active'
                    statusText = '正在抢购'
                } else if (now.getTime() >= slotEndTime.getTime()) {
                    // 已结束
                    status = 'ended'
                    statusText = '已结束'
                } else {
                    // 即将开始
                    status = 'coming'
                    statusText = '即将开始'
                }
            } else {
                // 明天的时间段都是即将开始
                status = 'coming'
                statusText = '即将开始'
            }

            let timeText = ''
            if (day === 0) {
                timeText = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
            } else {
                timeText = `明日${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
            }

            slots.push({
                startTime: startTimeStr,
                timeText,
                status,
                statusText
            })
        })
    }

    // 设置默认选中的时间场次（优先选择正在进行的，其次选择即将开始的）
    const activeSlot = slots.find(slot => slot.status === 'active')
    const comingSlot = slots.find(slot => slot.status === 'coming')
    currentTimeSlot.value = activeSlot?.startTime || comingSlot?.startTime || slots[0]?.startTime || ''

    return slots
}

// 格式化日期时间为字符串
const formatDateTime = (date: Date): string => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化倒计时
const formatCountdown = (milliseconds: number): string => {
    if (milliseconds <= 0) return '00 : 00 : 00'

    const totalSeconds = Math.floor(milliseconds / 1000)
    const hours = Math.floor(totalSeconds / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const seconds = totalSeconds % 60

    return `${String(hours).padStart(2, '0')} : ${String(minutes).padStart(2, '0')} : ${String(seconds).padStart(2, '0')}`
}

// 获取按钮文本
const getButtonText = (product: SeckillProduct): string => {
    if (product.activityStartStatus === 0) {
        return '即将开始'
    }else if (product.activityStartStatus === 1) {
        return '正在抢购'
    }else if (product.activityStartStatus === 2) {
	    return '已抢完'
	}else{
		return '抢购'
	}
    
}

// 时间场次切换
const handleTimeSlotChange = (startTime: string) => {
    if (currentTimeSlot.value === startTime) return
    currentTimeSlot.value = startTime
    currentPage.value = 1
    fetchProducts()
}

// 计算当前时间段结束的剩余时间（本场结束）
const calculateRemainingTime = (startTimeStr: string): number => {
    if (!startTimeStr) return 0
    const startTime = new Date(startTimeStr).getTime()
    const endTime = startTime + 4 * 60 * 60 * 1000 // 4小时后
    return Math.max(0, endTime - Date.now())
}

/** 当前选中的场次是否为「未开始」 */
const currentSlotIsComing = computed(() => {
    const slot = timeSlots.value.find(s => s.startTime === currentTimeSlot.value)
    return slot?.status === 'coming'
})

/** 倒计时文案：未开始显示「距离本场秒杀开始剩余」，进行中显示「本场开始至下一场开始」 */
const countdownLabel = computed(() =>
    currentSlotIsComing.value ? '距离本场秒杀开始剩余:' : '距离本场秒杀结束:'
)

/** 计算到本场开始时间的剩余毫秒数（用于未开始的场次） */
const getTimeToCurrentSlotStart = (): number => {
    if (!currentTimeSlot.value) return 0
    const start = new Date(currentTimeSlot.value).getTime()
    return Math.max(0, start - Date.now())
}

/** 计算到下一场开始时间的剩余毫秒数（本场开始→下一场开始）；若无下一场则为本场结束剩余时间 */
const getTimeToNextSlotStart = (): number => {
    const slots = timeSlots.value
    const idx = slots.findIndex(s => s.startTime === currentTimeSlot.value)
    if (idx < 0) return 0
    if (idx >= slots.length - 1) {
        return calculateRemainingTime(currentTimeSlot.value)
    }
    const nextStart = new Date(slots[idx + 1]!.startTime).getTime()
    return Math.max(0, nextStart - Date.now())
}

// 获取商品列表
const fetchProducts = async () => {
    try {
        loading.value = true

        const params = {
            commonName: '',
            goodsStartTime: currentTimeSlot.value,
            page: currentPage.value,
            limit: pageSize.value
        }

        const res = await querySeckillGoods<SeckillProduct>(params)

        if (res && res.code === 1 && res.data) {
            products.value = res.data.promotionGoodsList || []
            totalCount.value = res.data.totalCount || 0

            // 更新倒计时：未开始场次显示到本场开始，进行中显示到下一场开始
            countdownTime.value = currentSlotIsComing.value ? getTimeToCurrentSlotStart() : getTimeToNextSlotStart()

            // 启动倒计时更新
            startCountdownTimer()
        } else {
            products.value = []
            totalCount.value = 0
            // 即使没有商品，也按当前场次状态计算倒计时
            countdownTime.value = currentSlotIsComing.value ? getTimeToCurrentSlotStart() : getTimeToNextSlotStart()
            startCountdownTimer()
        }
    } catch (error) {
        console.error('获取秒杀商品失败:', error)
        products.value = []
        totalCount.value = 0
        // 出错时也按当前场次状态计算倒计时
        countdownTime.value = currentSlotIsComing.value ? getTimeToCurrentSlotStart() : getTimeToNextSlotStart()
        startCountdownTimer()
    } finally {
        loading.value = false
    }
}

// 启动倒计时定时器
const startCountdownTimer = () => {
    // 清除之前的定时器
    clearCountdownTimer()

    // 创建定时器更新倒计时
    countdownTimer = setInterval(() => {
        currentTime.value = Date.now()

        // 更新倒计时
        if (countdownTime.value > 0) {
            countdownTime.value = Math.max(0, countdownTime.value - 1000)
        } else {
            // 倒计时结束，重新生成时间场次并获取数据
            timeSlots.value = generateTimeSlots()
            fetchProducts()
        }
    }, 1000)
}

// 清除倒计时定时器
const clearCountdownTimer = () => {
    if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
    }
}

// 分页切换
const handlePageChange = (page: number) => {
    currentPage.value = page
    fetchProducts()
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 商品点击
const handleProductClick = (product: SeckillProduct) => {
    navigateTo(`/product/${product.goodsId}`)
}

// 抢购按钮点击
const handleSeckillClick = (product: SeckillProduct) => {
    if (product.activityStartStatus !== 1 || product.seckillGoodsState !== 1) {
        return
    }
    // 跳转到商品详情页
    navigateTo(`/product/${product.goodsId}`)
}

// 是否已预约
const isSubscribed = (goodsId: number): boolean => subscribedIds.value.includes(goodsId)

// 加载当前用户已预约列表（未登录返回空，不打断页面）
const loadMySubscribes = async () => {
    try {
        const res = await seckillMySubscribes()
        if (res && res.code === 1 && Array.isArray(res.data)) {
            subscribedIds.value = res.data.map(Number)
        }
    } catch (e) {
        // 未登录或接口异常时静默处理
    }
}

// 预约 / 取消预约按钮点击
const handleSubscribeClick = async (product: SeckillProduct) => {
    const { isLogin } = useAuth()
    if (!isLogin()) return
    const goodsId = Number(product.goodsId)
    if (!goodsId || subscribingIds.value.includes(goodsId)) return

    subscribingIds.value = [...subscribingIds.value, goodsId]
    const wasSubscribed = isSubscribed(goodsId)
    try {
        const res = wasSubscribed
            ? await seckillCancelSubscribe({ goodsId })
            : await seckillSubscribe({ goodsId })
        if (res && res.code === 1) {
            if (wasSubscribed) {
                subscribedIds.value = subscribedIds.value.filter(id => id !== goodsId)
                Toast.success(res.message || '已取消预约')
            } else {
                subscribedIds.value = [...subscribedIds.value, goodsId]
                Toast.success(res.message || '预约成功，开抢前将提醒你')
            }
        } else {
            Toast.error(res?.message || '操作失败，请稍后重试')
        }
    } catch (e) {
        Toast.error('网络异常，请稍后重试')
    } finally {
        subscribingIds.value = subscribingIds.value.filter(id => id !== goodsId)
    }
}

// 页面加载时初始化
onMounted(() => {
    // 生成时间场次列表
    timeSlots.value = generateTimeSlots()
    // 获取商品列表
    fetchProducts()
    // 加载当前用户已预约状态
    loadMySubscribes()
})

// 组件卸载时清理定时器
onBeforeUnmount(() => {
    clearCountdownTimer()
})
</script>

<style lang="scss" scoped>
.seckill-page {
    width: 100%;
    min-height: 100vh;
    background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
    padding: 32px 0;
    margin: 0 auto;

    .seckill-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: linear-gradient(135deg, #fff 0%, color-mix(in srgb, var(--color) 4%, #fff) 100%);
        padding: 20px 28px;
        border-radius: 12px 12px 0 0;
        margin-bottom: 0;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
        border-bottom: 1px solid color-mix(in srgb, var(--color) 12%, #fff);
        position: relative;
        overflow: hidden;

        &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 4px;
            height: 28px;
            background: var(--color);
            border-radius: 0 4px 4px 0;
        }

        .header-left {
            display: flex;
            align-items: center;
            gap: 14px;
            padding-left: 4px;

            .seckill-icon {
                font-size: 28px;
                filter: drop-shadow(0 1px 4px rgba(0, 0, 0, 0.1));
                opacity: 0.95;
            }

            .seckill-title {
                font-size: 22px;
                font-weight: 700;
                color: var(--color);
                letter-spacing: 0.5px;
                line-height: 1.3;
            }
        }

        .header-right {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 4px 12px;
            background: color-mix(in srgb, var(--color) 6%, #fff);
            border-radius: 8px;
            border: 1px solid color-mix(in srgb, var(--color) 15%, #fff);

            .countdown-label {
                font-size: 12px;
                color: #606266;
                font-weight: 500;
                white-space: nowrap;
            }

            .countdown-time {
                font-size: 15px;
                font-weight: 700;
                color: var(--color);
                font-variant-numeric: tabular-nums;
                letter-spacing: 1px;
                min-width: 1.2em;
                text-align: center;
            }
        }
    }

    .time-slots {
        display: flex;
        gap: 10px;
        padding: 16px 24px;
        background: color-mix(in srgb, var(--color) 3%, #fff);
        border-bottom: 1px solid color-mix(in srgb, var(--color) 10%, #fff);
        overflow-x: auto;
        scrollbar-width: thin;
        scrollbar-color: color-mix(in srgb, var(--color) 25%, transparent) transparent;

        &::-webkit-scrollbar {
            height: 5px;
        }

        &::-webkit-scrollbar-track {
            background: transparent;
        }

        &::-webkit-scrollbar-thumb {
            background: color-mix(in srgb, var(--color) 25%, transparent);
            border-radius: 3px;

            &:hover {
                background: color-mix(in srgb, var(--color) 40%, transparent);
            }
        }

        .time-slot-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-width: 110px;
            padding: 10px 18px;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.25s ease;
            background: #fff;
            border: 1px solid color-mix(in srgb, var(--color) 12%, #fff);
            flex-shrink: 0;

            .time-text {
                font-size: 15px;
                font-weight: 700;
                color: #303133;
                margin-bottom: 4px;
                transition: color 0.25s ease;
                white-space: nowrap;
            }

            .status-text {
                font-size: 11px;
                color: #909399;
                transition: color 0.25s ease;
                font-weight: 500;
            }

            &:hover:not(.active) {
                background: color-mix(in srgb, var(--color) 6%, #fff);
                border-color: color-mix(in srgb, var(--color) 22%, #fff);
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
            }

            &.active {
                background: var(--color);
                border-color: var(--color);
                box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 35%, transparent);
                transform: translateY(-1px);

                .time-text {
                    color: #fff;
                }

                .status-text {
                    color: rgba(255, 255, 255, 0.9);
                }
            }

            &.coming-soon {
                .status-text {
                    color: #909399;
                }
            }

            &.in-progress:not(.active) {
                .status-text {
                    color: var(--color);
                    font-weight: 600;
                }
            }
        }
    }

    .product-list {
        display: grid;
        grid-template-columns: repeat(5, 1fr);
        gap: 24px;
        padding: 32px;
        background: #fff;
        border-radius: 0 0 16px 16px;
        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);

        .product-item {
            display: flex;
            flex-direction: column;
            background: #fff;
            border: 1.5px solid #f0f2f5;
            border-radius: 14px;
            overflow: hidden;
            cursor: pointer;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06),
                0 1px 4px rgba(0, 0, 0, 0.04);

            &::after {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 4px;
                background: linear-gradient(90deg,
                        var(--color, #0a8cf0) 0%,
                        var(--rgba6, rgba(10, 140, 240, 0.6)) 50%,
                        transparent 100%);
                opacity: 0;
                transition: opacity 0.4s ease;
            }

            &:hover {
                border-color: var(--color, #0a8cf0);
                box-shadow: 0 16px 40px rgba(0, 0, 0, 0.14),
                    0 6px 16px var(--rgba2, rgba(10, 140, 240, 0.25));
                transform: translateY(-8px) scale(1.02);

                &::after {
                    opacity: 1;
                }
            }

            .product-image {
                width: 100%;
                aspect-ratio: 1/1;
                overflow: hidden;
                display: flex;
                align-items: center;
                justify-content: center;
                background: linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
                position: relative;

                &::after {
                    content: '';
                    position: absolute;
                    inset: 0;
                    background: linear-gradient(135deg,
                            var(--rgba2, rgba(10, 140, 240, 0.1)) 0%,
                            transparent 100%);
                    opacity: 0;
                    transition: opacity 0.3s ease;
                    z-index: 1;
                }

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                    transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                    position: relative;
                    z-index: 0;
                }

                &:hover {
                    img {
                        transform: scale(1.08);
                    }

                    &::after {
                        opacity: 1;
                    }
                }
            }

            .product-stats {
                padding: 14px 16px;
                font-size: 12px;
                display: flex;
                align-items: center;
                gap: 12px;
                background: linear-gradient(180deg, #fafbfc 0%, #ffffff 100%);
                border-top: 1px solid #f0f2f5;

                .comment-count-tag {
                    display: inline-flex;
                    align-items: center;
                    background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
                    color: #2e7d32;
                    padding: 5px 12px;
                    border-radius: 8px;
                    font-weight: 600;
                    box-shadow: 0 2px 6px rgba(46, 125, 50, 0.15);
                    transition: all 0.3s ease;

                    &:hover {
                        transform: translateY(-1px);
                        box-shadow: 0 4px 8px rgba(46, 125, 50, 0.2);
                    }
                }

                .sales-text {
                    color: #909399;
                    font-weight: 500;
                    flex: 1;
                }
            }

            .product-name {
                padding: 14px 16px;
                font-size: 14px;
                color: #303133;
                line-height: 1.6;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                height: 60px;
                font-weight: 600;
                transition: color 0.3s ease;

                &:hover {
                    color: var(--color, #0a8cf0);
                }
            }

            .product-price {
                padding: 12px 14px 0;
                display: flex;
                align-items: baseline;
                gap: 12px;

                .seckill-price {
                    font-size: 22px;
                    font-weight: 800;
                    color: var(--color, #0a8cf0);
                    line-height: 1;
                    display: flex;
                    align-items: baseline;
                    gap: 2px;

                    .currency-symbol {
                        color: var(--color, #0a8cf0);
                        font-size: 18px;
                        font-weight: 700;
                    }
                }

                .original-price {
                    font-size: 14px;
                    color: #c0c4cc;
                    text-decoration: line-through;
                    font-weight: 400;
                    display: flex;
                    align-items: baseline;
                    gap: 2px;

                    .currency-symbol {
                        color: #c0c4cc;
                    }
                }
            }

            .shop-action-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 12px;
                padding: 12px 14px 14px;
            }

            .shop-name {
                font-size: 12px;
                color: #909399;
                font-weight: 500;
                flex: 1;
                min-width: 0;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .seckill-btn {
                flex-shrink: 0;
                padding: 5px 8px;
                border: none;
                border-radius: 8px;
                font-size: 12px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.25s ease;
                background: var(--color);
                color: #fff;
                white-space: nowrap;

                &.btn-active {
                    background: var(--color);
                    box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 35%, transparent);

                    &:hover:not(:disabled) {
                        filter: brightness(1.08);
                        transform: translateY(-1px);
                        box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 40%, transparent);
                    }

                    &:active:not(:disabled) {
                        transform: translateY(0);
                    }
                }

                &.btn-coming {
                    background: transparent;
                    color: var(--color);
                    border: 1px solid var(--color);
                    cursor: pointer;

                    &:hover:not(:disabled) {
                        background: color-mix(in srgb, var(--color) 10%, transparent);
                    }
                }

                &.btn-subscribed {
                    background: #eef0f3;
                    color: #909399;
                    border: 1px solid #dcdfe6;
                    cursor: pointer;

                    &:hover:not(:disabled) {
                        background: #e5e7eb;
                    }
                }

                &.btn-sold-out {
                    background: #e5e7eb;
                    color: #909399;
                    cursor: not-allowed;
                    box-shadow: none;
                }

                &:disabled {
                    cursor: not-allowed;
                    opacity: 0.8;
                }
            }
        }
    }

    .pagination-wrapper {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 40px;
        padding: 28px;
        background: linear-gradient(135deg, #fff 0%, #fafbfc 100%);
        border-radius: 16px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06),
            0 1px 3px rgba(0, 0, 0, 0.04);

        .pagination-info {
            font-size: 14px;
            color: #606266;
            font-weight: 500;

            .pagination-right {
                .nuxt-ui-pagination {
                    .nuxt-ui-pagination__items {
                        gap: 12px;
                    }
                }
            }
        }
    }
}

// 响应式设计
@media (max-width: 1600px) {
    .seckill-page {
        padding: 20px;
    }
}

@media (max-width: 1400px) {
    .seckill-page {
        .product-list {
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
        }
    }
}

@media (max-width: 1200px) {
    .seckill-page {
        padding: 16px;

        .product-list {
            grid-template-columns: repeat(3, 1fr);
            gap: 16px;
            padding: 20px;
        }

        .seckill-header {
            padding: 20px 24px;

            .header-left .seckill-title {
                font-size: 24px;
            }
        }
    }
}

@media (max-width: 992px) {
    .seckill-page {
        .product-list {
            grid-template-columns: repeat(2, 1fr);
            gap: 16px;
        }

        .seckill-header {
            flex-direction: column;
            gap: 16px;
            align-items: flex-start;
            padding: 20px;

            .header-right {
                width: 100%;
                justify-content: flex-start;
            }
        }
    }
}

@media (max-width: 768px) {
    .seckill-page {
        padding: 12px;

        .product-list {
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
            padding: 12px;
        }

        .seckill-header {
            padding: 16px;
            border-radius: 8px 8px 0 0;

            .header-left {
                .seckill-icon {
                    font-size: 24px;
                }

                .seckill-title {
                    font-size: 20px;
                }
            }
        }

        .time-slots {
            padding: 12px;
            gap: 8px;

            .time-slot-item {
                min-width: 100px;
                padding: 10px 16px;

                .time-text {
                    font-size: 16px;
                }
            }
        }

        .pagination-wrapper {
            padding: 16px;
        }
    }
}

@media (max-width: 480px) {
    .seckill-page {
        .product-list {
            grid-template-columns: 1fr;
            gap: 12px;
        }

        .product-item {
            .product-price .seckill-price {
                font-size: 18px;
            }
        }
    }
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

@keyframes pulse-glow {

    0%,
    100% {
        opacity: 1;
        transform: scale(1);
    }

    50% {
        opacity: 0.8;
        transform: scale(1.05);
    }
}

@keyframes countdown-pulse {

    0%,
    100% {
        opacity: 1;
    }

    50% {
        opacity: 0.85;
    }
}
</style>
