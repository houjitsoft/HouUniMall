<template>
    <view :style="temp" class="hp100">
        <view class="header" :style="{ top: top }">
            <view class="inputModal" @tap="toListFn">
                <text class="iconfont icon-sousuo1 fz-30 align-top c-888"></text>
                <text class="text align-top">{{ getTransNew('搜索您心怡的产品') }}</text>
            </view>
        </view>
        <view class="contentContainer clearfix style2">
            <view class="catDiy" :class="[type == 3 || type == 4 || type == 5 ? 'bgf' : '']">
                <view class="cate-left iblock" v-if="type != 4">
                    <view class="cate-item li" @click="changeMenu(index)" :class="[index == activeidx ? 'active' : '']"
                        v-for="(item, index) in defaultAllData" :key="index">
                        <text>{{ getTransNew(item.catName) }}</text>
                    </view>
                </view>
                <view class="cate-right iblock" v-if="rightActiveData" :class="[type == 4 ? 'wp100 ml0' : '']">
                    <view v-if="type == 1 || type == 2">
                        <view class="cate-banner" v-if="rightActiveData.modularizationdCatImageData"
                            @click="defaultLinkTo(rightActiveData.modularizationdCatImageData)">
                            <image mode="widthFix" v-if="rightActiveData.modularizationdCatImageData.catImage == ''"
                                :src="bannerImg" alt=""></image>
                            <image mode="widthFix" v-else :src="rightActiveData.modularizationdCatImageData.catImage"
                                alt=""></image>
                        </view>
                        <view class="cat-cont" v-if="rightActiveData.modularizationdCatDataList">
                            <view class="cat-cont-item"
                                v-for="(item, index) in rightActiveData.modularizationdCatDataList" :key="index">
                                <view class="h4" v-if="type == 1">{{ getTransNew(item.catName) }}</view>
                                <view class="ul" v-if="item.modularizationdCatImageDataList">
                                    <view class='active li' @click="defaultLinkTo(item2)"
                                        v-for="(item2, index2) in item.modularizationdCatImageDataList" :key="index2">
                                        <image v-if="item2.catImage == ''" :src="cateImg" alt=""></image>
                                        <image v-else :src="item2.catImage" alt=""></image>
                                        <text class="em">{{ getTransNew(item2.catName) }}</text>
                                    </view>
                                </view>
                            </view>
                        </view>
                    </view>
                    <view v-else-if="type == 3">
                        <view class="second-head-uls" v-if="rightActiveData.allCatNames?.length">
                            <view class="li" @click="onType3AllCatNameClick(index, catName)"
                                :class="[activeidx2 == index ? 'active' : '']"
                                v-for="(catName, index) in rightActiveData.allCatNames" :key="index">
                                {{ getTransNew(catName) }}</view>
                        </view>
                        <view class="second-head-uls" v-else-if="rightActiveData.modularizationdCatDataList">
                            <view class="li" @click="changeMenu2(index)" :class="[activeidx2 == index ? 'active' : '']"
                                v-for="(item, index) in rightActiveData.modularizationdCatDataList" :key="index">
                                {{ getTransNew(item.catName) }}</view>
                        </view>
                        <view class="goods-skel-list" v-if="goodsInitialLoading">
                            <view class="goods-skel-li" v-for="n in 5" :key="'gsk3-' + n">
                                <view class="goods-skel-img"></view>
                                <view class="goods-skel-body">
                                    <view class="goods-skel-line"></view>
                                    <view class="goods-skel-line short"></view>
                                </view>
                            </view>
                        </view>
                        <view class="goods-lists-uls" v-if="!goodsInitialLoading && type3ActiveGoodsList.length">
                            <view class="li" @click="toDetails(item)" v-for="(item, index) in type3ActiveGoodsList"
                                :key="index">
                                <image :src="item.commonImage" mode="widthFix" class="img-box"></image>
                                <view class="div">
                                    <view class="h5 title-overflow">{{ getTransNew(item.commonName) }}</view>
                                    <view class="p"><text class="rmb">{{ currencySymbol()
                                            }}</text><text class="em">{{ item.commonPrice ==
                                                '*' ? '***' : getRatePrice(item.commonPrice) }}</text></view>
                                </view>
                            </view>
                        </view>
                        <view class="goods-load-footer" v-if="!goodsInitialLoading && goodsLoadingMore">{{ getTransNew('加载中...')
                            }}</view>
                        <view class="goods-load-footer muted"
                            v-if="!goodsInitialLoading && !goodsLoadingMore && !goodsHasMore && currentGoodsCount > 0">
                            {{ getTransNew('没有更多了') }}</view>
                    </view>
                    <view v-else-if="type == 4 || type == 5">
                        <view class="second-head-uls" v-if="type == 4">
                            <view class="li" @click="changeMenu(index)" :class="[activeidx == index ? 'active' : '']"
                                v-for="(item, index) in defaultAllData" :key="index">{{ getTransNew(item.catName) }}</view>
                        </view>
                        <view class="goods-skel-list plr24" v-if="goodsInitialLoading">
                            <view class="goods-skel-li" v-for="n in 5" :key="'gsk45-' + n">
                                <view class="goods-skel-img"></view>
                                <view class="goods-skel-body">
                                    <view class="goods-skel-line"></view>
                                    <view class="goods-skel-line short"></view>
                                </view>
                            </view>
                        </view>
                        <view class="goods-lists-uls plr24"
                            v-if="!goodsInitialLoading && rightActiveData.goodsDataList">
                            <view class="li" @click="toDetails(item)"
                                v-for="(item, index) in rightActiveData.goodsDataList" :key="index">
                                <image :src="item.commonImage" mode="widthFix" class="img-box"></image>
                                <view class="div">
                                    <view class="h5 title-overflow">{{ getTransNew(item.commonName) }}</view>
                                    <view class="p"><text class="rmb">{{ currencySymbol()
                                            }}</text><text class="em">{{ item.commonPrice ==
                                                '*' ? '***' : getRatePrice(item.commonPrice) }}</text></view>
                                </view>
                            </view>
                        </view>
                        <view class="goods-load-footer" v-if="!goodsInitialLoading && goodsLoadingMore">{{ getTransNew('加载中...')
                            }}</view>
                        <view class="goods-load-footer muted"
                            v-if="!goodsInitialLoading && !goodsLoadingMore && !goodsHasMore && currentGoodsCount > 0">
                            {{ getTransNew('没有更多了') }}</view>
                    </view>
                </view>
            </view>
        </view>

    </view>
</template>

<script>
const app = getApp({ allowDefault: true });

import {
    queryAnalysisPageDataRight,
    queryApplyPageLeft
} from "@/api/pages/index"
export default {

    data() {
        return {

            top: '44px',
            contTop: '0px',
            defaultAllData: [],
            rightActiveData: {},
            activeidx: 0,
            type: 1,
            activeidx2: 0,
            isolateShopid: this.publicVariable.thirdPart.shopId,
            temp: '--webColor: ' + this.publicVariable.webColor + ';',
            /** 最近一次商品列表请求的入参（不含 page/limit），用于触底追加 */
            lastGoodsQueryObj: null,
            /** 左侧分类索引 -> 已加载的右侧数据快照（避免切回时重复请求） */
            goodsRightPaneCache: {},
            /** type 3 下各二级 Tab 的分页游标（避免切换 Tab 后重复加载或页码错乱） */
            type3SubPaging: {},
            /** type 3 allCatNames：当前选中项，对应接口 targetCatName */
            activeTargetCatName: '',
        };
    },
    props: {},
    computed: {
        /** 是否为支持商品触底加载的 DIY 类型（3 二级 Tab 商品、4/5 单列商品） */
        supportsGoodsInfinite() {
            return (this.type === 3 || this.type === 4 || this.type === 5);
        },
        /** type 3 当前 Tab 下的商品列表 */
        type3ActiveGoodsList() {
            if (this.type !== 3 || !this.rightActiveData) return [];
            const tab = this.getType3TabEntry(this.rightActiveData, this.activeidx2);
            return tab?.goodsDataList || [];
        },
        /** 当前展示的商品条数，用于「没有更多了」等展示 */
        currentGoodsCount() {
            if (!this.supportsGoodsInfinite || !this.rightActiveData) return 0;
            if (this.type === 3) {
                return this.type3ActiveGoodsList.length;
            }
            if (this.type === 4 || this.type === 5) {
                return this.rightActiveData.goodsDataList?.length || 0;
            }
            return 0;
        },
    },
    mounted() {
        this.top = uni.getSystemInfoSync().statusBarHeight + Number(44) + 'px';
        this.contTop = uni.getSystemInfoSync().statusBarHeight + Number(44) + Number(60) + 'px';
        this.initData();
    },
    methods: {

        defaultLinkTo(item) {
            if (item.catImageType == 1) {
                uni.navigateTo({
                    url: '/pages/description/descrption?goodsId=' + item.goodsId + '&commonId=' + item.commonId
                })
            } else {
                if (item.catId) {
                    uni.navigateTo({
                        url: '/general/productlist/productlist?catId=' + item.catId
                    })
                } else {
                    return false;
                }

            }
        },
        toDetails(item) {
            uni.navigateTo({
                url: '/pages/description/descrption?goodsId=' + item.goodsId + '&commonId=' + item.commonId
            })
        },
        /**
         * 拉取自定义分类右侧数据；支持商品列表分页（每页 6 条）与触底追加。
         * @param {Record<string, any> | null} obj 后台分析入参（来自 defaultAllData 项）；触底追加时可传 null
         * @param {boolean} [append=false] 为 true 时在现有商品列表后合并本页数据
         * @returns {Promise<void>}
         */
        async getRightData(obj, append = false) {
            const GOODS_LIMIT = 6;
            if (append) {
                if (!this.supportsGoodsInfinite || !this.goodsHasMore || this.goodsLoadingMore) return;
                if (!this.lastGoodsQueryObj) return;
                this.goodsLoadingMore = true;
            } else {
                if (this.supportsGoodsInfinite) this.goodsInitialLoading = true;
                this.lastGoodsQueryObj = obj && typeof obj === 'object' ? { ...obj } : null;
            }
            try {
                const base = append ? { ...this.lastGoodsQueryObj } : { ...(obj && typeof obj === 'object' ? obj : {}) };
                const payload = {
                    ...base,
                    page: append ? this.goodsNextPage : 1,
                    limit: GOODS_LIMIT,
                    targetCatName: base.targetCatName ?? '',
                };
                if (this.type === 3) {
                    payload.subTabIndex = this.activeidx2;
                }
                const {
                    code,
                    data
                } = await queryAnalysisPageDataRight(payload);
                if (code == 1 && data) {
                    if (append) {
                        const newItems = this.extractNewGoodsRows(data);
                        if (newItems.length === 0) {
                            this.goodsHasMore = false;
                        } else {
                            this.mergeGoodsRowsIntoRightActive(newItems);
                            this.goodsNextPage += 1;
                            this.goodsHasMore = newItems.length >= GOODS_LIMIT;
                        }
                        if (this.type === 3) {
                            this.type3SubPaging = {
                                ...this.type3SubPaging,
                                [this.activeidx2]: {
                                    nextPage: this.goodsNextPage,
                                    hasMore: this.goodsHasMore,
                                },
                            };
                        }
                        this.saveGoodsPaneCache(this.activeidx);
                    } else {
                        this.rightActiveData = data;
                        const first = this.extractNewGoodsRows(data);
                        this.goodsNextPage = 2;
                        this.goodsHasMore = first.length >= GOODS_LIMIT;
                        if (this.type === 3) {
                            this.type3SubPaging = {
                                ...this.type3SubPaging,
                                [this.activeidx2]: {
                                    nextPage: 2,
                                    hasMore: first.length >= GOODS_LIMIT,
                                },
                            };
                        } else {
                            this.type3SubPaging = {};
                        }
                        this.saveGoodsPaneCache(this.activeidx);
                    }
                }
            } finally {
                this.goodsInitialLoading = false;
                this.goodsLoadingMore = false;
            }
        },
        async initData() {
            const {
                code,
                data
            } = await queryApplyPageLeft({
                shopId: this.publicVariable.thirdPart.shopId || null
            });
            if (code == 1 && data) {
                var resData = JSON.parse(data.pageData)
                this.defaultAllData = resData;
                this.type = data.pageType;
                if (resData[0]) {
                    this.getRightData(resData[0])
                }
            }
        },
        /**
         * 切换左侧一级分类：已加载过的分类从缓存恢复，不重复请求接口。
         * @param {number} index 左侧分类下标
         * @returns {void}
         */
        changeMenu(index) {
            if (index === this.activeidx) return;
            this.saveGoodsPaneCache(this.activeidx);
            this.activeidx = index;
            this.activeidx2 = 0;
            this.activeTargetCatName = '';
            const cached = this.goodsRightPaneCache[index];
            if (cached && cached.rightActiveData) {
                try {
                    this.rightActiveData = JSON.parse(JSON.stringify(cached.rightActiveData));
                    if (this.supportsGoodsInfinite) {
                        if (this.type === 3 && cached.type3SubPaging) {
                            this.type3SubPaging = { ...cached.type3SubPaging };
                            const m0 = this.type3SubPaging[0];
                            if (m0) {
                                this.goodsNextPage = m0.nextPage;
                                this.goodsHasMore = m0.hasMore;
                            } else {
                                this.applyType3SubPagingFallback(0);
                            }
                        } else {
                            this.goodsNextPage = cached.goodsNextPage;
                            this.goodsHasMore = cached.goodsHasMore;
                        }
                    }
                    const baseObj =
                        this.defaultAllData[index] && typeof this.defaultAllData[index] === 'object'
                            ? { ...this.defaultAllData[index] }
                            : null;
                    const cachedCatName = cached.targetCatName || cached.activeTargetCatName || '';
                    this.lastGoodsQueryObj = baseObj
                        ? { ...baseObj, targetCatName: cachedCatName }
                        : null;
                    this.activeTargetCatName = cachedCatName;
                    return;
                } catch (e) {
                    console.warn('changeMenu restore cache', e);
                }
            }
            this.type3SubPaging = {};
            this.getRightData(this.defaultAllData[index]);
        },
        /**
         * 切换二级 Tab（type 3）：已切过的 Tab 恢复其分页游标，避免重复请求已加载的页。
         * @param {number} index 二级 Tab 下标
         * @returns {void}
         */
        changeMenu2(index) {
            if (index === this.activeidx2) return;
            if (this.type === 3) {
                this.type3SubPaging = {
                    ...this.type3SubPaging,
                    [this.activeidx2]: {
                        nextPage: this.goodsNextPage,
                        hasMore: this.goodsHasMore,
                    },
                };
            }
            this.activeidx2 = index;
            if (this.type !== 3) return;
            const saved = this.type3SubPaging[index];
            if (saved) {
                this.goodsNextPage = saved.nextPage;
                this.goodsHasMore = saved.hasMore;
                return;
            }
            this.applyType3SubPagingFallback(index);
        },
        /**
        * type 3：根据某二级 Tab 已有列表长度推导分页游标（无单独记录时使用）。
        * @param {number} subIdx 二级 Tab 下标
        * @returns {void}
        */
        applyType3SubPagingFallback(subIdx) {
            const GOODS_LIMIT = 6;
            const tab = this.getType3TabEntry(this.rightActiveData, subIdx);
            const len = tab?.goodsDataList?.length || 0;
            if (len === 0) {
                this.goodsNextPage = 2;
                this.goodsHasMore = false;
                return;
            }
            this.goodsNextPage = Math.floor((len - 1) / GOODS_LIMIT) + 2;
            this.goodsHasMore = len >= GOODS_LIMIT;
        },
        /**
         * 将本页商品行合并进当前右侧展示数据（不替换整包 DIY 数据，避免冲掉其它二级 Tab 结构）。
         * @param {any[]} newItems 本页商品数组
         * @returns {void}
         */
        mergeGoodsRowsIntoRightActive(newItems) {
            if (!newItems.length) return;
            if (this.type === 3) {
                const tab = this.getType3TabEntry(this.rightActiveData, this.activeidx2);
                if (!tab) return;
                if (!tab.goodsDataList) tab.goodsDataList = [];
                tab.goodsDataList.push(...newItems);
            } else if (this.type === 4 || this.type === 5) {
                if (!this.rightActiveData.goodsDataList) this.rightActiveData.goodsDataList = [];
                this.rightActiveData.goodsDataList.push(...newItems);
            }
        },
        /**
        * 从 analysisPageData 返回体中取出当前类型对应的「本页商品行」数组（用于首屏条数判断或追加合并）。
        * @param {Record<string, any>} data 接口 data
        * @returns {any[]}
        */
        extractNewGoodsRows(data) {
            if (!data) return [];
            if (this.type === 3) {
                const tab = this.getType3TabEntry(data, this.activeidx2);
                return tab?.goodsDataList || [];
            }
            if ((this.type === 4 || this.type === 5) && data.goodsDataList) {
                return data.goodsDataList;
            }
            return [];
        },
        /**
        * type 3：取当前二级 Tab 对应的 modularizationdCatDataList 项（allCatNames 模式下兼容仅返回单条）。
        * @param {Record<string, any>} data 右侧数据
        * @param {number} subIdx 二级 Tab 下标
        * @returns {Record<string, any> | null}
        */
        getType3TabEntry(data, subIdx) {
            const list = data?.modularizationdCatDataList;
            if (!list?.length) return null;
            const idx = typeof subIdx === 'number' ? subIdx : this.activeidx2;
            const tab = list[idx];
            if (tab?.goodsDataList) return tab;
            return list.find((t) => t?.goodsDataList?.length) || list[0] || null;
        },
        /**
         * 将当前右侧 DIY 数据写入内存缓存（按左侧分类下标），用于再次进入该分类时免请求。
         * @param {number} leftIdx 左侧一级分类下标
         * @returns {void}
         */
        saveGoodsPaneCache(leftIdx) {
            if (!this.customType || !this.rightActiveData || typeof leftIdx !== 'number') return;
            const keys = Object.keys(this.rightActiveData);
            if (!keys.length) return;
            try {
                const entry = {
                    rightActiveData: JSON.parse(JSON.stringify(this.rightActiveData)),
                };
                if (this.supportsGoodsInfinite) {
                    entry.goodsNextPage = this.goodsNextPage;
                    entry.goodsHasMore = this.goodsHasMore;
                    if (this.type === 3) {
                        entry.type3SubPaging = { ...this.type3SubPaging };
                        entry.activeTargetCatName = this.activeTargetCatName;
                        entry.targetCatName = this.lastGoodsQueryObj?.targetCatName ?? '';
                    }
                }
                this.goodsRightPaneCache = { ...this.goodsRightPaneCache, [leftIdx]: entry };
            } catch (e) {
                console.warn('saveGoodsPaneCache', e);
            }
        },
        /**
         * 触底时尝试请求下一页商品（仅 type 3/4/5）。
         * @returns {void}
         */
        onGoodsReachBottom() {
            if (!this.supportsGoodsInfinite) return;
            if (this.goodsInitialLoading || this.goodsLoadingMore || !this.goodsHasMore) return;
            if (!this.lastGoodsQueryObj) return;
            this.getRightData(null, true);
        },
        /**
         * type 3：点击 allCatNames 某项，以 targetCatName 重新拉取商品列表。
         * @param {number} index Tab 下标
         * @param {string} catName 分类名称
         * @returns {void}
         */
        onType3AllCatNameClick(index, catName) {
            if (index === this.activeidx2 && this.activeTargetCatName === catName) return;
            if (this.type === 3) {
                this.type3SubPaging = {
                    ...this.type3SubPaging,
                    [this.activeidx2]: {
                        nextPage: this.goodsNextPage,
                        hasMore: this.goodsHasMore,
                    },
                };
            }
            this.activeidx2 = index;
            this.activeTargetCatName = catName || '';
            const obj = this.defaultAllData[this.activeidx];
            if (!obj || typeof obj !== 'object') return;
            this.getRightData({ ...obj, targetCatName: catName || '' });
        },
        // 头部跳转函数
        toListFn() {
            if (this.publicVariable.thirdPart.shopId) { //品牌店铺
                uni.navigateTo({
                    url: '/store/shopsearch/shopsearch'
                })
            } else { // 全平台
                uni.navigateTo({
                    url: '/general/searchpage/searchpage'
                })
            }
        }

    }
};
</script>
<style>
@import "./search.css";
</style>