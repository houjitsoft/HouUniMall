<template>
    <div class="plus-activate" :style="{ backgroundImage: `url(${IMAGE_URL('plusImg/nokaitong.jpg')})` }">
        <PlusHeroShell>
            <div class="plus-activate__hero">
                <div v-if="member" class="plus-activate__topCard">
                    <div class="plus-activate__topCard-left">
                        <div class="plus-activate__avatar">
                            <img v-if="avatarSrc" :src="avatarSrc" alt="" />
                        </div>

                        <div class="plus-activate__userMeta">
                            <div class="plus-activate__userHeadRow">
                                <div class="plus-activate__userName">{{ $t(member.userName || '') }}</div>

                                <div class="plus-activate__vipTag">
                                    <span class="plus-activate__vipTagIcon" aria-hidden="true" />
                                    <span class="plus-activate__vipTagPrefix">{{ $t(vipMemberTypeText) }}</span>
                                    <span class="plus-activate__vipTagText">{{ $t(thaliTypeText) }}</span>
                                </div>
                            </div>

                            <div class="plus-activate__expireRow">
                                <span class="plus-activate__expireLabel">{{ $t('会员到期时间') }}</span>
                                <span class="plus-activate__expireValue">{{ $t(expireTimeText) }}</span>
                                <button type="button" class="plus-activate__renewMiniBtn"
                                    @click="openRechargeRecordDialog">
                                    {{ $t('充值记录') }}
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="plus-activate__topCard-right">
                        <div class="plus-activate__counts">
                            <div class="plus-activate__countLine">
                                <span class="plus-activate__countLabel">{{ $t('可用红包券') }}：</span>
                                <span class="plus-activate__countValue">{{ redAvailableCount }}</span>
                                <span class="plus-activate__countSuffix">
                                    {{ $t('张（每月') }}{{ redTotalCount }}{{ $t('张）') }}
                                </span>
                                <img :src="IMAGE_URL('plusImg/rightjianto.png')" alt="" @click="handleRedPacketClick"
                                    class="plus-activate__countArrow" />
                            </div>
                            <div v-if="hasPoints" class="plus-activate__countLine">
                                <span class="plus-activate__countLabel">{{ $t('已获得积分') }}：</span>
                                <span class="plus-activate__countValue">{{ earnPointsCount }}</span>
                                <img :src="IMAGE_URL('plusImg/rightjianto.png')" alt="" @click="handleIntegralClick"
                                    class="plus-activate__countArrow" />
                            </div>
                        </div>

                        <div class="plus-activate__saveLine">
                            <div v-if="savedMoney > 0" class="plus-activate__saveText">
                                <span class="plus-activate__savePrefix">{{ $t('PLUS会员已为您节省') }}</span>
                                <span class="plus-activate__saveAmount">
                                    {{ formatPrice(savedMoney) }}{{ $t('元') }}
                                </span>
                            </div>
                            <div v-else class="plus-activate__saveText">
                                <span class="plus-activate__savePrefix">{{ $t('PLUS会员正在替您省钱的道路中') }}</span>
                            </div>
                            <button v-if="showRenewButton" type="button" class="plus-activate__renewBtn"
                                @click="handleRenewClick">
                                {{ $t('立即续费') }}
                            </button>
                        </div>
                    </div>
                </div>

                <!-- 会员特权（与开通页共用 PlusPrivilegesBar） -->
                <div v-if="overviewEquities.length" class="plus-activate__privileges">
                    <div class="plus-privileges-heading" aria-label="会员特权">
                        <img :src="IMAGE_URL('plusImg/titleZuoX2.png')" alt="" class="plus-privileges-heading__side" />
                        <span class="plus-privileges-heading__text">{{ $t('会员特权') }}</span>
                        <img :src="IMAGE_URL('plusImg/titleYouX2.png')" alt="" class="plus-privileges-heading__side" />
                    </div>
                    <PlusPrivilegesBar :background-style="plusPrivilegesBarBg" :equities="overviewEquities"
                        :max-icons="5" />
                </div>
            </div>
        </PlusHeroShell>

        <!-- PLUS为您推荐 -->
        <div class="plus-activate__recommendedMain">
            <div class="plus-activate__recommended">
                <div class="plus-privileges-heading" aria-label="PLUS为您推荐">
                    <img :src="IMAGE_URL('plusImg/titleZuoX2.png')" alt="" class="plus-privileges-heading__side" />
                    <span class="plus-privileges-heading__text">{{ $t('PLUS为您推荐') }}</span>
                    <img :src="IMAGE_URL('plusImg/titleYouX2.png')" alt="" class="plus-privileges-heading__side" />
                </div>

                <ul v-if="recommendedGoods.length" class="plus-activate__recommendedGoods">
                    <li v-for="g in recommendedGoods" :key="g.goodsId" class="itemsList plus-activate__goodsCard"
                        @click="navigateTo(`/product/${g.goodsId}`)">
                        <div class="plus-activate__goodsImgBox">
                            <img :src="g.commonImage" alt="" />
                        </div>

                        <div class="plus-activate__goodsPriceRow">
                            <div class="plus-activate__goodsPriceMain">
                                <div class="plus-activate__goodsPrice">
                                    {{ formatPriceWithSymbolSafe(g.commonPrice) }}
                                </div>
                                <img v-if="Number(g.plusGoods) === 1" :src="IMAGE_URL('plusImg/plus.png')" alt="PLUS"
                                    class="plus-activate__goodsPlusBadge" />
                            </div>
                            <div v-if="Number(g.commonMarketPrice ?? 0) > 0" class="plus-activate__goodsMarketPrice">
                                <del>{{ formatPriceWithSymbolSafe(g.commonMarketPrice) }}</del>
                            </div>
                        </div>

                        <div class="plus-activate__goodsName">{{ $t(g.commonName) }}</div>
                        <div class="plus-activate__goodsEvaluate">
                            {{ $t(Number(g.commonEvaluate ?? 0) + '条好评') }}
                        </div>
                        <div class="plus-activate__goodsShopName">{{ $t(g.shopName || '-') }}</div>

                    </li>
                </ul>
            </div>
        </div>

        <PlusRechargeRecordDialog v-model="rechargeDialogVisible" :limit="10" />
    </div>
</template>

<script setup lang="ts">
import PlusHeroShell from '~/components/member/PlusHeroShell.vue'
import PlusPrivilegesBar from '~/components/member/PlusPrivilegesBar.vue'
import PlusRechargeRecordDialog from '~/components/member/PlusRechargeRecordDialog.vue'
import { IMAGE_URL, UCENTER_URL } from '~/utils/externalUrl'
import { getToken } from '~/utils/auth'
import { selectPlusMember } from '~/services/userms.service'
import { selectPlusGoods } from '~/services/goods.service'
import { formatPriceWithSymbolSafe } from '~/composables/useFormatPrice'
import { onMounted, ref, computed } from 'vue'
import type { PlusEquity, PlusMemberPackageDatum, PlusRecommendedGoodsItem } from '~/types/plus'

definePageMeta({ layout: 'full-screen' })

const memberPackages = ref<PlusMemberPackageDatum[] | null>(null)
const recommendedGoods = ref<PlusRecommendedGoodsItem[]>([])
const rechargeDialogVisible = ref(false)

const member = computed(() => memberPackages.value?.[0] ?? null)

/** 套餐类型映射 */
const THALI_TYPE_NAME_MAP: Record<number, string> = {
    1: '月卡',
    2: '季卡',
    3: '年卡'
}

/**
 * 根据套餐类型获取中文名称。
 * @param type 套餐类型
 * @returns 套餐名称（如月卡）
 */
function getThaliTypeName(type: unknown): string | null {
    const t = Number(type ?? 0)
    return THALI_TYPE_NAME_MAP[t] ?? null
}

const avatarSrc = computed(() => {
    const v = member.value?.userAvatar
    if (!v) return ''
    const s = String(v)
    return s.startsWith('http') ? s : IMAGE_URL(s)
})

/**
 * 会员权益状态文案：
 * - nextThaliType 为空：按当前 thaliType 显示“已生效”
 * - nextThaliType 有值：按 nextThaliType 显示“待生效”
 */
const thaliTypeText = computed(() => {
    const data = member.value
    if (!data) return 'PLUS会员'

    if (data.nextThaliType == null) {
        const currName = getThaliTypeName(data.thaliType)
        return currName ? `${currName}权益已生效` : 'PLUS权益已生效'
    }

    const nextName = getThaliTypeName(data.nextThaliType)
    return nextName ? `${nextName}权益待生效` : 'PLUS权益待生效'
})

/** 会员标签前缀文案：年卡/季卡/月卡会员 */
const vipMemberTypeText = computed(() => {
    const name = getThaliTypeName(member.value?.thaliType)
    if (name) return `${name}会员`
    return 'PLUS会员'
})

const expireTimeText = computed(() => {
    const v = member.value?.thaliEndtime
    if (!v) return ''
    return String(v)
})

const plusEquities = computed((): PlusEquity[] => member.value?.plusEquitys ?? [])

const overviewEquities = computed(() => {
    return plusEquities.value
        .filter((eq) => [1, 2, 3, 4, 5].includes(Number(eq.equityType)))
        .sort((a, b) => Number(a.equityType ?? 0) - Number(b.equityType ?? 0))
})

const hasPoints = computed(() => plusEquities.value.some((eq) => Number(eq.equityType) === 4))

const redAvailableCount = computed(() => Number(member.value?.redAvailable ?? 0))
const redTotalCount = computed(() => Number(member.value?.redTotal ?? 0))
const earnPointsCount = computed(() => Number(member.value?.earnPoints ?? 0))

const totalAmount = computed(() => Number(member.value?.totalAmount ?? 0))
const savedMoney = computed(() => Math.max(totalAmount.value, 0))
const showRenewButton = computed(() => Number(member.value?.renewalDisplay ?? 0) === 1)

/** 权益条背景，与开通页 fbagive 一致 */
const plusPrivilegesBarBg = { backgroundImage: `url(${IMAGE_URL('plusImg/fbagive.png')})` }
/** 顶部会员卡背景图 */
const topCardBg = `url(${IMAGE_URL('plusImg/yesX2.png')})`
/** 会员标签背景图 */
const vipTagBg = `url(${IMAGE_URL('plusImg/plusCart.png')})`
/** 会员标签前缀图标背景 */
const vipTagIconBg = `url(${IMAGE_URL('plusImg/zuanshi.png')})`

/**
 * 跳转续费页：携带当前会员 thaliId / thaliType。
 * @returns {Promise<void>}
 */
const handleRenewClick = async (): Promise<void> => {
    const thaliId = member.value?.thaliId != null ? Number(member.value.thaliId) : undefined
    const thaliType = member.value?.thaliType != null ? Number(member.value.thaliType) : undefined

    await navigateTo({
        path: '/member/plusRecharge',
        query: {
            ...(Number.isFinite(thaliId) && (thaliId as number) > 0 ? { thaliId: String(thaliId) } : {}),
            ...(Number.isFinite(thaliType) && (thaliType as number) > 0 ? { thaliType: String(thaliType) } : {})
        }
    })
}

/**
 * 跳转用户中心红包券页。
 * @returns {void}
 */
const handleRedPacketClick = (): void => {
    UCENTER_URL('/property/redPacket')
}

/**
 * 跳转用户中心积分页。
 * @returns {void}
 */
const handleIntegralClick = (): void => {
    UCENTER_URL('/property/integral')
}

/**
 * 打开充值记录弹窗并拉取数据。
 * @returns {void}
 */
const openRechargeRecordDialog = (): void => {
    rechargeDialogVisible.value = true
}

async function initData() {
    const token = getToken()
    if (!token) return

    const memberRes = await selectPlusMember({ token })
    if (memberRes.code === 1 && Array.isArray(memberRes.data)) {
        memberPackages.value = memberRes.data
    }

    const goodsRes = await selectPlusGoods({ token })
    if (goodsRes.code === 1 && Array.isArray(goodsRes.data)) {
        recommendedGoods.value = goodsRes.data ?? []
    }
}

onMounted(() => {
    initData()
})
</script>

<style lang="scss" scoped>
.plus-activate {
    width: 100%;
    min-height: 100%;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
}

.plus-activate__hero {
    position: relative;
    z-index: 1;
    min-height: 320px;
    padding: 70px 0 0;
    box-sizing: border-box;
}

.plus-activate__topCard {
    position: relative;
    z-index: 1;
    max-width: 1200px;
    margin: 0 auto;
    height: 192px;
    border-radius: 18px;
    background-image: v-bind('topCardBg');
    background-size: 100% 100%;
    background-position: center;
    background-repeat: no-repeat;
    border: 1px solid rgba(201, 160, 99, 0.22);
    display: flex;
    padding: 26px 34px;
    box-sizing: border-box;
}

.plus-activate__topCard-left {
    display: flex;
    flex: 1;
    min-width: 0;
    align-items: center;
    gap: 22px;
}

.plus-activate__avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    border: 1px solid rgba(0, 0, 0, 0.25);
    overflow: hidden;
    flex: 0 0 auto;
    background: rgba(255, 255, 255, 0.35);
}

.plus-activate__avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
}

.plus-activate__userMeta {
    min-width: 0;
}

.plus-activate__userHeadRow {
    display: flex;
    align-items: center;
    gap: 14px;
}

.plus-activate__userName {
    font-size: 26px;
    font-weight: 600;
    color: #855d29;
    text-shadow: 0px 2px 6px rgba(0, 0, 0, 0.08);
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.plus-activate__vipTag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 4px 12px;
    background-image: v-bind('vipTagBg');
    background-size: 100% 100%;
    background-position: center;
    background-repeat: no-repeat;
    font-size: 14px;
}

.plus-activate__vipTagIcon {
    width: 16px;
    height: 16px;
    flex: 0 0 auto;
    background-image: v-bind('vipTagIconBg');
    background-size: 200%;
    background-position: center;
    background-repeat: no-repeat;
}

.plus-activate__vipTagPrefix {
    color: #FDE7CB;
}

.plus-activate__vipTagText {
    color: #333333;
    margin-left: 20px;
}

.plus-activate__expireRow {
    margin-top: 40px;
    display: flex;
    gap: 8px;
    align-items: baseline;
    color: #855d29;
    font-size: 14px;
}

.plus-activate__expireLabel {
    color: #855d29;
    font-weight: 500;
}

.plus-activate__expireValue {
    font-weight: 600;
    border-bottom: 2px solid rgba(133, 93, 41, 0.55);
}

.plus-activate__renewMiniBtn {
    margin-left: 12px;
    height: 28px;
    padding: 0 14px;
    border: 0;
    border-radius: 999px;
    background: linear-gradient(180deg, #fde2b9 0%, #d9a86f 100%);
    color: #5a3312;
    font-size: 12px;
    cursor: pointer;
}

.plus-activate__topCard-right {
    flex: 0 0 460px;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    align-items: flex-start;
}

.plus-activate__counts {
    display: flex;
    flex-direction: row;
    gap: 24px;
    align-items: flex-end;
    justify-content: flex-end;
    flex-wrap: nowrap;
}

.plus-activate__countLine {
    display: inline-flex;
    align-items: center;
    font-size: 16px;
    color: #FDDFBF;
    white-space: nowrap;
}

.plus-activate__countArrow {
    width: 16px;
    height: 16px;
    margin-left: 6px;
    object-fit: contain;
    align-self: center;
    cursor: pointer;
}

.plus-activate__countLabel {
    font-weight: 400;
}

.plus-activate__countValue {
    font-weight: 400;
}

.plus-activate__saveLine {
    margin-top: 8px;
    text-align: right;
    font-size: 24px;
    font-weight: 400;
    color: #fff;
    text-shadow: none;
}

.plus-activate__saveText {
    display: inline-block;
    padding: 0;
    border-radius: 0;
    background: transparent;
}

.plus-activate__saveAmount {
    color: #fff;
    margin-left: 6px;
}

.plus-activate__renewBtn {
    margin-top: 10px;
    height: 38px;
    padding: 0 22px;
    border: 0;
    border-radius: 999px;
    background: linear-gradient(180deg, #fde2b9 0%, #d9a86f 100%);
    color: #5a3312;
    font-size: 16px;
    cursor: pointer;
}

.plus-activate__privileges {
    position: relative;
    z-index: 1;
    max-width: 1200px;
    margin: 20px auto 20px;
    padding-bottom: 0;
}

/* 与 plus.vue 权益区标题一致；激活页深色背景上略调字色 */
.plus-activate__privileges .plus-privileges-heading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 18px;
    margin: 50px 0;
}

.plus-activate__privileges .plus-privileges-heading__side {
    width: 99px;
    height: 22px;
    object-fit: contain;
    display: block;
    pointer-events: none;
}

.plus-activate__privileges .plus-privileges-heading__text {
    font-size: 32px;
    font-weight: 800;
    background: linear-gradient(180deg, #F0DBC5 0%, #CC9B7B 100%);
    background-clip: text;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

/* 推荐区标题：与会员特权同款左右装饰，正文黑色 */
.plus-activate__recommended .plus-privileges-heading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 18px;
    margin: 0 0 20px;
}

.plus-activate__recommended .plus-privileges-heading__side {
    width: 99px;
    height: 22px;
    object-fit: contain;
    display: block;
    pointer-events: none;
}

.plus-activate__recommended .plus-privileges-heading__text {
    font-size: 36px;
    font-weight: 400;
    color: #000;
}

.plus-activate__recommendedMain {
    margin-top: 0;
    padding: 0 0 60px;
    overflow: hidden;
}

.plus-activate__recommended {
    max-width: 1200px;
    margin: 0 auto;
    padding-top: 40px;
}

.plus-activate__recommendedGoods {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 16px;
    padding-top: 20px;
}

.plus-activate__goodsCard {
    width: 100%;
    min-height: 320px;
    height: auto;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.1), 0px 6px 12px 0px rgba(255, 255, 255, 0.33);
    padding: 16px 17px;
    box-sizing: border-box;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.plus-activate__goodsCard:hover {
    transform: translateY(-2px);
    box-shadow: 0px 6px 18px rgba(0, 0, 0, 0.14), 0px 10px 18px rgba(255, 255, 255, 0.2);
}

.plus-activate__goodsImgBox {
    width: 100%;
    aspect-ratio: 250 / 227;
    border-radius: 12px;
    overflow: hidden;
    background: #d8d8d8;
}

.plus-activate__goodsImgBox img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.25s ease;
}

.plus-activate__goodsCard:hover .plus-activate__goodsImgBox img {
    transform: scale(1.03);
}

.plus-activate__goodsPriceRow {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: 12px;
}

.plus-activate__goodsPriceMain {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
}

.plus-activate__goodsPrice {
    font-size: 16px;
    font-weight: 800;
    color: #333;
}

.plus-activate__goodsPlusBadge {
    display: block;
    width: 50px;
    height: 22px;
    object-fit: cover;
    flex: 0 0 auto;
}

.plus-activate__goodsMarketPrice {
    font-size: 12px;
    color: #999;
    white-space: nowrap;
    margin-left: auto;
}

.plus-activate__goodsName {
    margin-top: 16px;
    font-size: 13px;
    color: #333;
    line-height: 1.4;
    min-height: calc(1.4em * 2);
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
}

.plus-activate__goodsShopName {
    margin-top: 8px;
    font-size: 12px;
    color: #666;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.plus-activate__goodsEvaluate {
    margin-top: 6px;
    font-size: 12px;
    color: #999;
}
</style>