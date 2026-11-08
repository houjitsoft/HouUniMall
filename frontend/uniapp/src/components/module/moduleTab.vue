<template>
    <view class="moduleTab" :style="maskPaddingStyle">
        <view v-if="tabStyle === '1'" class="tab-panel tab-s1" :style="panelStyle">
            <scroll-view scroll-x :show-scrollbar="false" enable-flex class="tab-s1__scroll">
                <view class="tab-s1__track">
                    <view v-for="(item, index) in items" :key="item.id ?? index" class="tab-cell tab-s1__cell"
                        :style="s1CellStyle" @click="handleNavigatorTo(item.link)">
                        <view class="tab-cell__img-box">
                            <image class="tab-cell__img" :src="item.imgUrl" mode="aspectFit" lazy-load />
                        </view>
                        <text class="tab-cell__name tab-cell__name--1line" :style="{ color: propsobj.textColor }">
                            {{ getTransNew(item.name) }}
                        </text>
                    </view>
                </view>
            </scroll-view>
        </view>

        <view v-if="tabStyle === '2'" class="tab-panel tab-s2" :style="panelStyle">
            <scroll-view v-if="groupedItems.length" scroll-x :show-scrollbar="false" enable-flex class="tab-s2__scroll">
                <view class="tab-s2__track">
                    <view v-for="(group, gIndex) in groupedItems" :key="gIndex" class="tab-s2__page"
                        :style="s2PageStyle">
                        <view class="tab-s2__grid" :style="s2GridStyle">
                            <view v-for="(item, i) in group" :key="`${gIndex}-${i}`" class="tab-cell tab-cell--grid"
                                @click="handleNavigatorTo(item.link)">
                                <view class="tab-cell__img-box tab-cell__img-box--grid">
                                    <image class="tab-cell__img" :src="item.imgUrl" mode="aspectFit" lazy-load />
                                </view>
                                <text class="tab-cell__name" :style="{ color: propsobj.textColor }">
                                    {{ getTransNew(item.name) }}
                                </text>
                            </view>
                        </view>
                    </view>
                </view>
            </scroll-view>
        </view>
    </view>
</template>

<script>
import { mallUrl } from '@/utils/request.js'

export default {
    name: 'moduleTab',
    props: ['propsobj'],
    data() {
        return {
            s2ScrollWidthPx: 375,
        }
    },
    computed: {
        items() {
            return this.propsobj?.items ?? []
        },
        tabStyle() {
            return String(this.propsobj?.style ?? '1')
        },
        rowNum() {
            const n = Number(this.propsobj?.rowNum)
            return n === 3 || n === 4 || n === 5 ? n : 5
        },
        chunkSize() {
            return this.rowNum * 2
        },
        maskPaddingStyle() {
            return {
                paddingTop: `${this.propsobj.mrT}px`,
                paddingBottom: `${this.propsobj.mrB}px`,
                paddingLeft: `${this.propsobj.mrL}px`,
                paddingRight: `${this.propsobj.mrR}px`,
            }
        },
        panelStyle() {
            return {
                backgroundColor: this.propsobj.bgColor,
                paddingTop: `${this.propsobj.pdT}px`,
                paddingRight: `${this.propsobj.pdR}px`,
                paddingBottom: `${this.propsobj.pdB}px`,
                paddingLeft: `${this.propsobj.pdL}px`,
                borderTopLeftRadius: `${this.propsobj.rdsTL}px`,
                borderTopRightRadius: `${this.propsobj.rdsTR}px`,
                borderBottomLeftRadius: `${this.propsobj.rdsBL}px`,
                borderBottomRightRadius: `${this.propsobj.rdsBR}px`,
            }
        },
        s1CellStyle() {
            const n = this.rowNum
            const gap = 16
            return {
                width: `${(750 - (n - 1) * gap) / n}rpx`,
                flexShrink: 0,
            }
        },
        s2GridStyle() {
            return {
                gridTemplateColumns: `repeat(${this.rowNum}, 1fr)`,
            }
        },
        groupedItems() {
            const list = this.items
            const size = this.chunkSize
            const groups = []
            for (let i = 0; i < list.length; i += size) {
                groups.push(list.slice(i, i + size))
            }
            return groups
        },
        s2PageStyle() {
            return {
                width: `${this.s2ScrollWidthPx}px`,
                flexShrink: 0,
                boxSizing: 'border-box',
            }
        },
        setUrlLink() {
            return function (link) {
                // #ifdef APP
                return link
                // #endif
                // #ifdef H5
                if (link === '/tuikit/livelist/index') {
                    return '/tuikit/webLiveList/webLiveList'
                }
                return link
                // #endif
                // #ifdef MP
                if (link === '/tuikit/livelist/index') {
                    const customLink = `${mallUrl}h5/#/tuikit/webLiveList/webLiveList?token=${uni.getStorageSync('token')}`
                    return `/hybrid/web_view/web_view?url=${encodeURIComponent(customLink)}`
                }
                return link
                // #endif
            }
        },
    },
    mounted() {
        this.measureS2ScrollWidth()
    },
    watch: {
        tabStyle() {
            this.measureS2ScrollWidth()
        },
        rowNum() {
            this.measureS2ScrollWidth()
        },
        'groupedItems.length'() {
            this.measureS2ScrollWidth()
        },
        'items.length'() {
            this.measureS2ScrollWidth()
        },
    },
    methods: {
        measureS2ScrollWidth() {
            if (this.tabStyle !== '2' || !this.groupedItems.length) return
            this.$nextTick(() => {
                uni.createSelectorQuery()
                    .in(this)
                    .select('.tab-s2__scroll')
                    .boundingClientRect((rect) => {
                        if (rect?.width > 0) {
                            this.s2ScrollWidthPx = rect.width
                        }
                    })
                    .exec()
            })
        },
        navigateTo(url) {
            uni.navigateTo({ url })
        },
        handleNavigatorTo(link) {
            // #ifdef APP
            this.navigateTo(link)
            // #endif
            // #ifdef H5
            if (link === '/tuikit/livelist/index') {
                this.navigateTo('/tuikit/webLiveList/webLiveList')
            } else {
                this.navigateTo(link)
            }
            // #endif
            // #ifdef MP
            if (link === '/tuikit/livelist/index') {
                if (uni.getStorageSync('token')) {
                    const customLink = `${mallUrl}h5/#/tuikit/webLiveList/webLiveList?token=${uni.getStorageSync('token')}`
                    this.navigateTo(`/hybrid/web_view/web_view?url=${encodeURIComponent(customLink)}`)
                } else {
                    this.navigateTo('/general/landingPage/landingPage')
                }
            } else {
                this.navigateTo(link)
            }
            // #endif
        },
    },
}
</script>

<style lang="scss" scoped>
$tab-gap: 16rpx;
$tab-img: 90rpx;
$tab-img-grid-max: 104rpx;

.moduleTab {
    overflow: hidden;
    box-sizing: border-box;
}

.tab-panel {
    box-sizing: border-box;
    overflow: hidden;
}

.tab-s1__scroll,
.tab-s2__scroll {
    width: 100%;
    white-space: nowrap;
}

.tab-s1__track {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    gap: $tab-gap;
    padding: 0 0 8rpx;
    box-sizing: border-box;
}

.tab-s1__cell {
    flex-shrink: 0;
}

.tab-s2__track {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: flex-start;
    padding: 0 0 8rpx;
    box-sizing: border-box;
}

.tab-s2__page {
    box-sizing: border-box;
}

.tab-s2__grid {
    display: grid;
    grid-template-rows: repeat(2, auto);
    gap: 20rpx $tab-gap;
    width: 100%;
    box-sizing: border-box;
    padding: 4rpx 12rpx 16rpx;
}

.tab-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    text-align: center;
    min-width: 0;
}

.tab-cell__img-box {
    width: $tab-img;
    height: $tab-img;
    flex-shrink: 0;
    overflow: hidden;
    box-sizing: border-box;
}

.tab-cell__img-box--grid {
    position: relative;
    width: 100%;
    max-width: $tab-img-grid-max;
    height: 0;
    padding-bottom: 100%;
}

.tab-cell__img {
    display: block;
    width: 100%;
    height: 100%;
}

.tab-cell__img-box--grid .tab-cell__img {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
}

.tab-cell__name {
    margin-top: 8rpx;
    line-height: 1.25;
    width: 100%;
    font-size: 24rpx;
    word-break: break-word;
}

.tab-cell__name--1line {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.tab-cell--grid .tab-cell__name {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    overflow: hidden;
}
</style>
