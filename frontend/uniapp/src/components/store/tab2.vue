<template>
	<view class="storeModule" :style="temp">
		<view class="shopHeader">
			<image class="shopBgc" :src="shopInfo.shopImage"></image>
			<view class="shopHeaderMasker ab">
				<view class="shopHeaderTop clearfix">
					<view class="toIndexPages fl">
						<image class="indexhImage" :src="imgUrl+'indexpages.png'" v-if="isBrandStore !=3&&isolateShopid==''" @tap="clickIndexPagesFn"></image>
					</view>
					<view class="shopSearchContainer fl re" @tap="clickShopSearchFn">
						<image class="searchImage" :src="imgUrl+'fangdajing.png'"></image>
						<text class="ab">{{getTransNew('搜索店内商品')}}</text>
					</view>
				</view>
				<view class="shopHeaderMiddle clearfix">
					<view class="shopLogo fl">
						<image class="shopLogoImage" :src="shopInfo.wapShopLogo"></image>
					</view>
					<view class="shopNameContainer fl">
						<view class="ShopName">
							<view class="fl">
								<span>{{getTransNew(shopInfo.shopName)}}</span>
								<text v-if="isolateShopid==''">
									<text v-if="shopInfo.shopSelfSupport == 'true'" class="SelfownedShop">自营</text>
								</text>
							</view>
							<view v-if="isolateShopid==''" class="fr" @tap="clickFollowFn">
								<image class="shopLogoImage" :src="followImages"></image>
								<text v-if="isFollow">{{getTransNew('已关注')}}</text>
								<text v-else>{{getTransNew('关注')}}</text>
							</view>
						</view>
						<view class="followNumberContainer clearfix">
							<text class="SaleNumber fl">{{shopInfo.shopCompanyAddress ? shopInfo.shopCompanyAddress : '' }}</text>
							<text v-if="isolateShopid==''" class="followNumber fr">{{shopInfo.shopCollect}}{{getTransNew('人')}}{{getTransNew('关注')}}</text>
						</view>
						<text v-if="shopInfo.shopSelfSupport=='false'" class="shopQualInfos fr" @click="handleQual">{{getTransNew('店铺资质')}}</text>
					</view>
				</view>
				<view class="shopItensTypeContainer">
					<view :class="'re ' + indexPageNow" @tap="clickIndexFn">
						<view class="typeText">{{getTransNew('首页')}}</view>
						<view class="line"></view>
					</view>
					<view :class="'re ' + allPagesNow" @tap="clickAllFn">
						<view class="typeText">{{getTransNew('全部宝贝')}}</view>
						<view class="line"></view>
					</view>
					<view :class="'re ' + newPagesNow" @tap="clicknewFn">
						<view class="typeText">{{getTransNew('新品推荐')}}</view>
						<view class="line"></view>
					</view>
				</view>
			</view>
		</view>
		<!-- 主题正文 -->
		<view class="contentContainer" :style="{top:top}">
			<!-- 首页 -->
			<view :class="'hp100 indexPages ' + indexListHide">
				<swiper :class="bannerhide" :indicator-dots="indicatorDots" :autoplay="autoplay" :interval="interval" :duration="duration">
					<block v-for="(item, index) in shopInfo.shopSlideurlList" :key="index" class="bannerBox">
						<swiper-item>
							<image class="swiper-item-images" :src="item.shopSlideUrl" :data-opobj="item.commonIdAndGoodsId" :data-optype="item.opType" @tap="clickBannerFn">
							</image>
						</swiper-item>
					</block>
				</swiper>
				<view class="itemsListContainer hp100">
					<scroll-view class="scroll-view" scroll-y @scrolltolower="allScrolltolower">
						<view v-for="(item, index) in goodslistArr" :key="index">
							<view class="itemsList clearfix" :data-goodsid="item.goodsId" :data-commonid="item.commonId" @tap="clickItemsFn">
								<view class="fl itemImage">
									<image class="itemsImg" :src="item.commonImage"></image>
								</view>
								<view class="itemsInfo fl">
									<view class="itemsTitle">{{getTransNew(item.commonName)}}</view>
									<view class="itemsTips" v-if="item.commonPromotionTips">{{getTransNew(item.commonPromotionTips)}}</view>
									<view class="itemsNumber clearfix">
										<view class="fl itemsPriceNumber">{{currencySymbol()}}{{getRatePrice(item.commonPrice)}}</view>
										<view class="fr itemsSaleNumber">{{getTransNew('已售')}}{{item.commonSalenum}}</view>
									</view>
								</view>
							</view>
						</view>
					</scroll-view>
				</view>
			</view>
			<!-- 全部宝贝 -->
			<view :class="'hp100 allItemsPages ' + allListHide">
				<view class="tabBarContainer">
					<view :class="'allProducts ' + typeAllNow" @tap="clickAllFn">{{getTransNew('全部')}}</view>
					<view :class="'allProducts ' + typeSaleNow" @tap="clickSaleFn">{{getTransNew('销量')}}</view>
					<view :class="'allProducts ' + typeNewNow" @tap="clickAllNewFn">{{getTransNew('新品')}}</view>
					<view :class="'allProducts ' + typePriceNow" @tap="clickPriceFn">
						<text>{{getTransNew('价格')}}</text>
						<image class="priceImage" :src="priceImage"></image>
					</view>
				</view>
				<view class="itemsListContainer hp100">
					<scroll-view class="scroll-view" scroll-y @scrolltolower="allScrolltolower">
						<view v-for="(item, index) in goodslistArr" :key="index">
							<view class="itemsList clearfix" :data-goodsid="item.goodsId" :data-commonid="item.commonId" @tap="clickItemsFn">
								<view class="fl itemImage">
									<image class="itemsImg" :src="item.commonImage"></image>
								</view>
								<view class="itemsInfo fl">
									<view class="itemsTitle">{{getTransNew(item.commonName)}}</view>
									<view class="itemsTips">{{item.commonPromotionTips}}</view>
									<view class="itemsNumber clearfix">
										<view class="fl itemsPriceNumber">{{currencySymbol()}}{{getRatePrice(item.commonPrice)}}</view>
										<view class="fr itemsSaleNumber">{{getTransNew('已售')}}{{item.commonSalenum}}</view>
									</view>
								</view>
							</view>
						</view>
					</scroll-view>

				</view>
			</view>
			<!-- 新品推荐 -->
			<view :class="'hp100 NewItemsPages ' + newListHide">
				<view class="itemsListContainer hp100">
					<scroll-view class="scroll-view" scroll-y @scrolltolower="allScrolltolower">
						<view v-for="(item, index) in goodslistArr" :key="index">
							<view class="itemsList clearfix" :data-goodsid="item.goodsId" :data-commonid="item.commonId" @tap="clickItemsFn">
								<view class="fl itemImage">
									<image class="itemsImg" :src="item.commonImage"></image>
								</view>
								<view class="itemsInfo fl">
									<view class="itemsTitle">{{getTransNew(item.commonName)}}</view>
									<view class="itemsTips">{{item.commonPromotionTips}}</view>
									<view class="itemsNumber clearfix">
										<view class="fl itemsPriceNumber">{{currencySymbol()}}{{getRatePrice(item.commonPrice)}}</view>
										<view class="fr itemsSaleNumber">{{getTransNew('已售')}} {{item.commonSalenum}}</view>
									</view>
								</view>
							</view>
						</view>
					</scroll-view>
				</view>
			</view>
		</view>
		<!-- 底部领取优惠券 -->
		<view class="clearfix footerContainer" v-if="isBrandStore !=3&&isolateShopid==''&&isHasShopIm">
			<view class="fl serviceContainer re" @click="toKefu">
				<image :src="imgUrl+'cdetails_icon_collection_wpss.png'"></image>
				<view>{{getTransNew('客服')}}</view>
				<button class="ab servicerBtn"></button>
			</view>
			<view class="fl getCouponBtns" @tap="clickGetCouponFn">{{getTransNew('领取优惠券')}}</view>
		</view>
		<popup ref="popup5" type="center" height="150" width="400" radius="6" :scrollY="true" :showCloseIcon="true">
			<view class="tc interest-module imTels">
				<text class="title">{{ getTransNew('客服电话:')}}</text>
				<text class="block mt10 cont">{{imShopUrl}}</text>
			</view>
		</popup>
	</view>
</template>

<script>
	const app = getApp({allowDefault: true}); //实例化app.js中的变量和常量
	import {checkToken} from'@/utils/util.js';
	import {mallRequest} from '@/utils/request.js';
	import {
		queryCheckFavoriteShoporNot,
		queryAddfavoriteshop,
		queryDeletefavoriteshop,
		queryConfig
	} from "@/api/pages/description"
	import {
		querybabyClassFilter,
		queryShopindexList
	} from "@/api/store/index"
	import {detectingImStatus} from '@/api/pages/index'
	import popup from '@/components/wyb-popup/wyb-popup.vue'
	export default {
		components: {
			popup
		},
		data() {
			return {
				isHasShopIm:false,
				imShopType:'',
				imShopUrl:'',
				isolateShopid: this.publicVariable.thirdPart.shopId,
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				token: uni.getStorageSync('token'),
				imgUrl: this.publicVariable.imgUrl,
				shopId: '',
				swiperhide: 'hide',
				indicatorDots: true,
				autoplay: true,
				interval: 5000,
				duration: 1000,
				indexPageNow: 'now',
				allPagesNow: '',
				newPagesNow: '',
				indexListHide: '',
				allListHide: 'hide',
				newListHide: 'hide',
				typeAllNow: 'now',
				typeSaleNow: '',
				typeNewNow: '',
				typePriceNow: '',
				priceImage: this.imgUrl + 'store_icon_adjustment_wps (1).png',
				priceTypeFlag: '',
				followImages: this.imgUrl + 'icon_collection_normal_wps.png',
				isFollow: false,
				bannerhide: '',
				shopInfo: "",
				emptyhide: "",
				innerContainerHide: "",
				options: "",
				searchObj: "",
				checkTypeObj: "",
				goodslistArr: [],
				dataObj: {},
				isBrandStore: 1,
				top: '440rpx'
			};
		},
		props: {
			shopid: {
				type: Number,
				requried: true
			}
		},
		created() {
			this.init();
		},
		watch: {
			shopid: {
				deep: true,
				handler(val) {
					this.init();
				}
			}
		},
		onReachBottom() {

		},
		onShareAppMessage() {},
		
		methods: {
			
			// 查询是否关注该店铺
			checkFollowFn() {
				queryCheckFavoriteShoporNot({
					shopId: this.searchObj.shopId
				}).then(res => {
					if (res.code == 1) {
						if (res.data == 1) {
							// 已收藏
							this.setData({
								followImages: this.$data.imgUrl + 'icon_collection_highlight_wps.png',
								isFollow: true
							});
						} else {
							//未收藏
							this.setData({
								followImages: this.imgUrl + 'icon_collection_normal_wps.png',
								isFollow: false
							});
						}
					} else {
						//未收藏
						this.setData({
							followImages: this.imgUrl + 'icon_collection_normal_wps.png',
							isFollow: false
						});
					}
				})
			},
			// 全部宝贝下面分类
			allTypeListFn(obj) {
				querybabyClassFilter(obj).then(res => {
					if (res.code == 1) {
						if (res.data.records.length > 0) {
							for (var i = 0; i < res.data.records.length; i++) {
								res.data.records[i].commonPrice = res.data.records[i].commonPrice;
							}

							if (obj.page == 1) {
								this.goodslistArr = res.data.records;
							} else {
								this.goodslistArr = this.goodslistArr.concat(res.data.records);
							}

							this.setData({
								emptyhide: 'hide',
								innerContainerHide: '',
							});
						} else {
							if (obj.page == 1) {
								this.setData({
									emptyhide: '',
									innerContainerHide: 'hide'
								});
							} else {
								this.setData({
									emptyhide: 'hide',
									innerContainerHide: ''
								});
							}
						}
					} else {
						this.setData({
							emptyhide: 'hide',
							innerContainerHide: ''
						});
					}
				})
			},
			// 滚动加载
			allScrolltolower() {
				if (this.indexPageNow == 'now') {
					// 首页
					this.searchObj.page = this.searchObj.page + 1;
					this.shopListFn(this.searchObj);
				} else if (this.allPagesNow == 'now') {
					// 全部宝贝
					var checkTypeObj = this.checkTypeObj;
					checkTypeObj.page = checkTypeObj.page + 1;
					this.allTypeListFn(checkTypeObj);
				} else if (this.newPagesNow == 'now') {
					// 新品推荐
					this.searchObj.page = this.searchObj.page + 1;
					this.shopListFn(this.searchObj);
				}
				
			},
			shopListFn(obj) {
				queryShopindexList(obj).then(res => {
					if (res.code == 1) {
						this.isBrandStore = res.data.shopType
						if (res.data.gdsList.length > 0) {
							this.dataObj = res.data;
							for (var i = 0; i < res.data.gdsList.length; i++) {
								if (res.data.gdsList[i].commonPrice) {
									res.data.gdsList[i].commonPrice = res.data.gdsList[i].commonPrice;
								}
							}
							if (res.data.shopSlideurlList) {
								// bannerhide
								this.setData({
									bannerhide: 'hide'
								});
							} else {
								this.setData({
									bannerhide: 'hide'
								});
							}
							if (obj.page == 1) {
								this.goodslistArr = res.data.gdsList;
							} else {
								this.goodslistArr = this.goodslistArr.concat(res.data.gdsList);
							}
							this.setData({
								emptyhide: 'hide',
								innerContainerHide: '',
								shopInfo: res.data
							});
							// 查询是否关注该店铺
							this.checkFollowFn();
						} else {
							if (obj.page == 1) {
								this.setData({
									emptyhide: '',
									innerContainerHide: 'hide'
								});
							} else {
								this.setData({
									emptyhide: 'hide',
									innerContainerHide: ''
								});
							}
						}
					} else {
						this.setData({
							emptyhide: 'hide',
							innerContainerHide: ''
						});
					}
				})
			},
			init() {

				var searchObj = {
					shopId: this.shopid,
					orderType: 1,
					page: 1,
					limit: 20
				};
				var checkTypeObj = {
					shopId: this.shopid,
					orderType: '',
					saleNum: '',
					priceOrder: '',
					page: 1,
					limit: 20
				};
				this.setData({
					searchObj: searchObj,
					checkTypeObj: checkTypeObj
				});


				this.shopListFn(searchObj);
				this.getImSetting();
			},
			//   点击领取优惠券
			clickGetCouponFn() {
				uni.navigateTo({
					url: '/activities/getcoupon/getcoupon?typeId=1&shopId=' + this.shopid
				});
			},
			//   点击店铺内搜索
			clickShopSearchFn() {
				uni.navigateTo({
					url: '/store/shopsearch/shopsearch?shopId=' + this.shopid
				});
			},
			// 点击跳转首页
			clickIndexPagesFn() {
				uni.switchTab({
					url: '/pages/index/index'
				});
			},
			clickFollowFn() {
				if (this.token) {

					if (this.isFollow == false) {
						// 还没有关注，添加关注
						queryAddfavoriteshop({
							shopId: this.shopid,
							shopName: '',
							shopLogo: ''
						}).then(res => {
							if (res.code == 1) {
								// 已收藏 shopInfo: res.data
								var shopInfo = this.shopInfo;
								shopInfo.shopCollect = this.shopInfo.shopCollect - 0 + 1;
								this.setData({
									followImages: this.imgUrl +
										'icon_collection_highlight_wps.png',
									shopInfo: shopInfo,
									isFollow: true
								});
							} else {
								//未收藏
								this.setData({
									followImages: this.imgUrl + 'icon_collection_normal_wps.png',
									isFollow: true
								});
							}
						})
					} else {
						// 已关注，取消关注 
						queryDeletefavoriteshop({
							shopIds: [String(this.shopid)]
						}).then(res => {
							if (res.code == 1) {
								// 已收藏 shopInfo: res.data
								this.shopInfo.shopCollect = this.shopInfo.shopCollect - 1;
								this.setData({
									followImages: this.imgUrl + 'icon_collection_normal_wps.png',
									isFollow: false
								});
							} else {
								//未收藏
								this.setData({
									followImages: this.imgUrl +
										'icon_collection_highlight_wps.png',
									isFollow: true
								});
							}
						})
					}
				} else {
					uni.showToast({
						title: this.getTransNew('请先登录'),
						icon: 'none'
					})
				}

			},
			clickIndexFn() {
				var searchObj = this.searchObj;
				this.goodslistArr = [];
				searchObj.orderType = 1;
				searchObj.page = 1;
				this.setData({
					indexPageNow: 'now',
					allPagesNow: '',
					newPagesNow: '',
					indexListHide: '',
					allListHide: 'hide',
					newListHide: 'hide',
					top:'440rpx'
				});
				this.shopListFn(searchObj);
			},
			clickAllFn() {
				var checkTypeObj = this.checkTypeObj;
				this.goodslistArr = [];
				checkTypeObj.orderType = 3;
				checkTypeObj.page = 1;
				checkTypeObj.priceOrder = ''; //按照价格
				checkTypeObj.saleNum = ''; //按销量
				this.setData({
					indexPageNow: '',
					allPagesNow: 'now',
					newPagesNow: '',
					indexListHide: 'hide',
					allListHide: '',
					newListHide: 'hide',
					typeAllNow: 'now',
					typeSaleNow: '',
					typeNewNow: '',
					typePriceNow: '',
					priceImage: this.imgUrl + 'store_icon_adjustment_wps (1).png',
					top:'540rpx'
				});
				this.allTypeListFn(checkTypeObj);
			},
			// 点击销量过滤
			clickSaleFn() {

				var checkTypeObj = this.checkTypeObj;
				this.goodslistArr = [];
				checkTypeObj.orderType = 2;
				checkTypeObj.page = 1;
				checkTypeObj.priceOrder = ''; //按照价格

				checkTypeObj.saleNum = 1; //按销量

				this.setData({
					indexPageNow: '',
					allPagesNow: 'now',
					newPagesNow: '',
					indexListHide: 'hide',
					allListHide: '',
					newListHide: 'hide',
					typeAllNow: '',
					typeSaleNow: 'now',
					typeNewNow: '',
					typePriceNow: '',
					priceImage: this.imgUrl + 'store_icon_adjustment_wps (1).png'
				});
				this.allTypeListFn(checkTypeObj);
			},
			// 新品
			clickAllNewFn() {

				var checkTypeObj = this.checkTypeObj;
				this.goodslistArr = [];
				checkTypeObj.orderType = 4;
				checkTypeObj.page = 1;
				checkTypeObj.priceOrder = ''; //按照价格

				checkTypeObj.saleNum = ''; //按销量

				this.setData({
					indexPageNow: '',
					allPagesNow: 'now',
					newPagesNow: '',
					indexListHide: 'hide',
					allListHide: '',
					newListHide: 'hide',
					typeAllNow: '',
					typeSaleNow: '',
					typeNewNow: 'now',
					typePriceNow: '',
					priceImage: this.imgUrl + 'store_icon_adjustment_wps (1).png'
				});
				this.allTypeListFn(checkTypeObj);
			},
			// 价格排序
			clickPriceFn() {

				var checkTypeObj = this.checkTypeObj;
				this.goodslistArr = [];
				checkTypeObj.orderType = 1;

				if (this.priceTypeFlag == 1) {
					// 此时为升序改降序
					checkTypeObj.priceOrder = 2;
					var PriceImagesUrl = this.imgUrl + 'store_icon_adjustment_wps (3).png';
				} else if (this.priceTypeFlag == 2) {
					// 此时为降序改升序
					checkTypeObj.priceOrder = 1;
					var PriceImagesUrl = this.imgUrl + 'store_icon_adjustment_wps (2).png';
				} else {
					// 第一次点击,默认升序
					checkTypeObj.priceOrder = 2;
					var PriceImagesUrl = this.imgUrl + 'store_icon_adjustment_wps (3).png';
				}

				checkTypeObj.page = 1;
				checkTypeObj.saleNum = ''; //按销量

				this.setData({
					priceTypeFlag: checkTypeObj.priceOrder,
					indexPageNow: '',
					allPagesNow: 'now',
					newPagesNow: '',
					indexListHide: 'hide',
					allListHide: '',
					newListHide: 'hide',
					typeAllNow: '',
					typeSaleNow: '',
					typeNewNow: '',
					typePriceNow: 'now',
					priceImage: PriceImagesUrl
				});
				this.allTypeListFn(checkTypeObj);
			},
			clicknewFn() {

				var searchObj = this.searchObj;
				this.goodslistArr = [];
				this.searchObj.orderType = 4;
				this.searchObj.page = 1;
				this.setData({
					indexPageNow: '',
					allPagesNow: '',
					newPagesNow: 'now',
					indexListHide: 'hide',
					allListHide: 'hide',
					newListHide: '',
					top:'440rpx'
				});
				this.shopListFn(this.searchObj);
			},
			// 点击商品
			clickItemsFn(e) {
				var data = e.currentTarget.dataset;
				
				uni.navigateTo({
					url: '/pages/description/descrption?goodsid=' + data.goodsid + '&commonId=' + data.commonid
				});
			},
			// 点击轮播图
			clickBannerFn(e) {
				var data = e.currentTarget.dataset;
				if (data.optype == 2) {
					// 跳商品
					uni.navigateTo({
						url: '/pages/description/descrption?goodsid=' + data.opobj.goodsId + '&commonId=' + data.opobj.commonid
					});
				}
			},
			toKefu() {
				checkToken().then((vdata) => {
					if (vdata) {
						var shopId=this.shopid;
                        let link;
						if(this.imShopType=='1'||this.imShopType=='3'){
							if(this.imShopType=='1'){
								 link=mallRequest.MALL_IM+'wapChat?isMall=1&pageType=2&shopId='+shopId+'&token='+uni.getStorageSync('token');
							}else if(this.imShopType=='3'){
								 link=this.imShopUrl;
							}
							// #ifdef H5
                            this.$safeOpenUrl(link);
							// #endif
							// #ifndef H5
							uni.navigateTo({
								url: '/hybrid/im_web_view/im_web_view?url=' + encodeURIComponent(link)
							})
							// #endif
						}else{
							this.$refs.popup5.show();
						}
					}
				})

			},
			//查询im配置
			getImSetting(){
				var params='customer_service_type_'+this.shopid;
				detectingImStatus(params).then(res=>{
					if(res.code==1){
						if(res.data!=null){
							if(res.data.configEnable==1){
								this.isHasShopIm=true;
								this.imShopType=res.data.configValue;
								this.imShopUrl=res.data.configValue2;
							}else{
								this.isHasShopIm=false;
							}
							
						}
					}
				})
			},
			handleQual(){
				var that=this;
				checkToken().then((vdata) => {
					if (vdata) {
						var that = this;
						uni.navigateTo({
							url: `/general/shopdesc/shopdesc?shopId=${that.shopid}`
						})
					}
				})
				
			}
		}
	};
</script>
<style lang="scss">

	.shopHeader {
		width: 100%;
		position: fixed;
		top: 0;
		height: 440rpx;
		left: 0;
		background-color: #fff;
		z-index: 333;
	}

	.shopBgc {
		width: 100%;
		height: 100%;
	}

	.shopHeaderMasker {
		width: 100%;
		height: 100%;
		top: 0;
		left: 0;
		background-color: rgba(0, 0, 0, 0.2);
		/* #ifdef MP */
		padding-top: 24rpx;
		/* #endif */
	}

	.shopHeaderTop {
		/* #ifdef H5 */
		padding-top: 20rpx;
		/* #endif */
		/* #ifndef H5 */
		padding-top: 60rpx;
		/* #endif */
		height: 80rpx;
	}

	.shopSearchContainer text {
		font-size: 24rpx;
		color: #fff;
		top: 50%;
		left: 80rpx;
		transform: translateY(-50%);
	}

	.toIndexPages {
		width: 80rpx;
		height: 80rpx;
		margin-left: 2%;
		margin-top: 3%;
	}

	.toIndexPages image {
		width: 60rpx;
		height: 60rpx;
		margin: 10rpx;
	}

	.shopSearchContainer {
		width: 520rpx;
		height: 60rpx;
		background-color: rgba(255, 255, 255, 0.3);
		margin: 38rpx 20rpx;
		border-radius: 35rpx;
		/* #ifdef MP */
		width: 420rpx;
		margin-top:20rpx;
		/* #endif */
	}

	.shopSearchContainer image {
		width: 40rpx;
		height: 40rpx;
		margin: 10rpx 20rpx;
	}

	.moreContainer {
		width: 80rpx;
		height: 80rpx;
	}

	.moreContainer image {
		width: 60rpx;
		height: 60rpx;
		margin: 10rpx;
	}

	.shopHeaderMiddle {
		width: 96%;
		margin: 70rpx 2% 50rpx;
		display: flex;
		/* #ifdef MP */
		margin-bottom: 30rpx;
		/* #endif */
	}

	.shopLogo {
		width: 80rpx;
		height: 80rpx;
		margin-right: 20rpx;
	}

	.shopLogo image {
		width: 100%;
		height: 100%;
		border-radius: 8rpx;
	}

	.shopNameContainer {
		color: #fff;
		width: 620rpx;
		height: 80rpx;
	}

	.shopNameContainer .ShopName {
		height: 40rpx;
		line-height: 40rpx;
	}

	.shopNameContainer .ShopName .fl {
		font-size: 30rpx;
		width: 450rpx;
		height: 40rpx;
		overflow: hidden;
	}

	.shopNameContainer .ShopName .fr {
		background-color: rgba(0, 0, 0, 0.15);
		padding: 5rpx 20rpx;
		border-radius: 30rpx;
		font-size: 22rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		min-width: 144rpx;
	}

	.shopNameContainer .ShopName .fr image {
		width: 30rpx;
		height: 30rpx;
		vertical-align: middle;
		margin-right: 6rpx;
	}

	.followNumberContainer {
		height: 40rpx;
		line-height: 40rpx;
		width: 100%;
		overflow: hidden;
		font-size: 22rpx;
		color: #fff;
	}

	.followNumberContainer .followNumber {
		min-width: 144rpx;
		text-align: center;
	}

	.shopItensTypeContainer {
		color: #fff;
		display: flex;
		margin-top: 20rpx;
		position: absolute;
		left:0;
		width:100%;
		bottom:20rpx;
		z-index:2;
	}

	.shopItensTypeContainer .re {
		flex: 1;
		text-align: center;
		font-size: 28rpx;
	}

	.shopItensTypeContainer .typeText {
		height: 60rpx;
		line-break: 60rpx;
		letter-spacing: 4rpx;
	}

	.now {
		font-size: 32rpx;
	}

	.now .line {
		background-color: #fff;
	}

	.shopItensTypeContainer .line {
		height: 10rpx;
		width: 30%;
		margin-left: 35%;
		border-radius: 8px;
	}

	/* 主题正文 */
	.contentContainer {
		width: 100%;
		/* #ifdef H5 */
		// padding-top: 360rpx;
		/* #endif */
		/* #ifndef H5 */
		// padding-top: 440rpx;
		/* #endif */
		//padding-bottom: 130rpx;
	}

	.indexPages,
	.allItemsPages,
	.NewItemsPages {
		width: 100%;
	}

	.swiper,
	.swiper-item-images {
		width: 100%;
		height: 312rpx;
	}

	.itemsListContainer {
		margin-top: 20rpx;
	}

	.itemsList {
		width: 90%;
		margin: 0 5% 20rpx;
		border-radius: 10rpx;
		box-shadow: none;
		height: 202rpx;
		background-color: #fff;
		overflow: hidden;
	}

	.itemImage {
		width: 30%;
		height: 202rpx;
	}

	.itemsList .itemsImg {
		width: 100%;
		height: 202rpx;
	}

	.itemsInfo {
		width: 70%;
		height: 202rpx;
		padding: 10rpx 4%;
		box-sizing: border-box;
		font-size: 24rpx;
	}

	.itemsTitle {
		width: 100%;
		font-size: 26rpx;
		height: 60rpx;
		line-height: 30rpx;
		overflow: hidden;
	}

	.itemsTips {
		width: 100%;
		height: 70rpx;
		line-height: 70rpx;
		font-size: 22rpx;
		color: #aaa;
		overflow: hidden;
	}

	.itemsNumber {
		height: 50rpx;
		line-height: 50rpx;
	}

	.itemsPriceNumber {
		font-size: 30rpx;
		/* color: red; */
		color: var(--webColor);
	}

	.itemsSaleNumber {
		font-size: 24rpx;
		color: #aaa;
	}

	/* 全部宝贝的菜单tabbar */
	.tabBarContainer {
		height: 100rpx;
		line-height: 100rpx;
		display: flex;
		background-color: #fff;
		font-size: 24rpx;
		position:fixed;
		top:440rpx;
		width:100%;
		z-index:2;
	}

	.tabBarContainer view {
		flex: 1;
		text-align: center;
	}

	.tabBarContainer .now {
		font-size: 26rpx;
		/* color: rgb(255, 181, 1); */
		color: var(--webColor);
	}

	.priceImage {
		width: 40rpx;
		height: 40rpx;
		vertical-align: middle;
	}

	.footerContainer {
		position: fixed;
		height: 100rpx;
		width: 100%;
		bottom: 0;
		left: 0;
		background-color: #fff;
	}

	.serviceContainer {
		width: 30%;
		text-align: center;
		font-size: 26rpx;
	}

	.serviceContainer image {
		width: 50rpx;
		height: 50rpx;
		margin-top: 10rpx;
	}

	.serviceContainer view {
		line-height: 30rpx;
	}

	.serviceContainer .servicerBtn {
		width: 100%;
		height: 100%;
		left: 0;
		top: 0;
		opacity: 0;
	}

	.getCouponBtns {
		width: 70%;
		/* background-color: rgb(255, 181, 1); */
		background-color: var(--webColor);
		line-height: 100rpx;
		text-align: center;
		color: #fff;
		font-size: 30rpx;
		letter-spacing: 2px;
	}

	.SelfownedShop {
		font-size: 22rpx;
		margin-left: 20rpx;
		border: 2rpx solid white;
		color: white;
		border-radius: 10rpx;
		padding: 0rpx 7rpx;
	}

	// ::v-deep .uni-scroll-view {
	// 	height: 1250rpx;
	// }
	.scroll-view{
		height:100%;
	}
	.storeModule{

	}
	.contentContainer{
		position: absolute;
		top: 440rpx;
		bottom: 130rpx;
	}
	.shopQualInfos{
		  margin-top:20rpx;
		  color: #fff;
		  font-size: 24rpx;
		  border: 1px solid #fff;
		  padding: 4rpx 20rpx !important;
		  border-radius: 8rpx !important;
		  background:none !important;
		  min-width:auto !important;
	}
</style>