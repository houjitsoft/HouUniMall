<template>
	<view class="pb-30">
		<image v-if="isShowBg" class="pageBg wp100" mode='widthFix' :src="pageData.pageBaseInfo.bgImg" :style="{height:Number(pageData.pageBaseInfo.bgHeight)+'px'}"></image>
		<hx-navbar ref='hxnb' v-if="titShow&&isShowHead" :config="config">
			<template #left>
				<image class="ml20 headLeftImg" v-if="pageData.pageBaseInfo.headLeftImg!=''&&isShowImg" :src="pageData.pageBaseInfo.headLeftImg" :style="{width:Number(pageData.pageBaseInfo.headLeftImgW)+'px',height:Number(pageData.pageBaseInfo.headLeftImgH)+'px'}"></image>
			</template>
		</hx-navbar>
		<home :allpropsobj="curProps()" :shopid='params.shopId' :isFixed="isFixed"/>
	</view>
</template>
<script>
	let app = getApp({allowDefault: true});

	import home from '@/components/home/home.vue'
	import hxNavbar from '@/components/hx-navbar/hx-navbar.nvue'

	import {
		quertNavLogo,
		queryGetuser
	} from "@/api/pages/index"
	import {
		queryModularizationdShopIndex,
	} from "@/api/store/index"
	import {
		queryRankCat
	} from "@/api/pages/index"
	export default {
		components: {
			home,
			hxNavbar
		},
		props: {
			shopid: {
				type: Number,
				requried: true
			},
			isFixed:{
				type:Boolean,
				default:false
			}

		},
		data() {
			return {
				isShowImg:true,
				isShowBg:false,
				isShowHead:false,
				titShow: false,
				mrt0: true,
				params: {
					shopId: ''
				},
				headShow: false,
				index: 0,
				title: '首页',
				skeletonOff: false,
				reciveData: {},
				foo: false,
				shows: 0,
				top: '44px',
				headBg: this.publicVariable.themeData.headBgColor,
				config: {
					title: '',
					color: '#ffffff',
					back: true,
					leftSlot:true,
					backgroundColor: [0,'#fff'],
					slideBackgroundColor: [1,'#fff'],
				},
				themeData: {},
				staHeight: 0,
				off: false,
				componentLists: [],
				scrollTop: 0,
				pageData:{}
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
		async onShow() {
			await this.$onLaunched
			this.themeData = this.publicVariable.themeData;
			this.getUserPhone();
			this.top = uni.getSystemInfoSync().statusBarHeight + Number(44) + 'px';
			this.init();

		},
		methods: {
			scrollChange(e){
				if(this.titShow&&this.isShowHead){
					this.$refs.hxnb.pageScroll(e);
					if(e.scrollTop==0){
						this.isShowImg=true;
					}else{
						this.isShowImg=false;
					}
				}
			},
			init() {
				if (this.shopid) {
					this.params.shopId = this.shopid;
				} else {
					this.params.shopId = '';
				}
				queryModularizationdShopIndex({
                    ...this.params,
                    shopId: Number(this.params.shopId)
                }).then(res => {
					var arr = JSON.parse(res.data.pagedata);
					this.pageData=arr;
					if(this.pageData.pageBaseInfo.bgImg!=''){
						this.isShowBg=true;
					}
					this.config.color=arr.pageBaseInfo.titleColor;
					if(arr.moduleList[0]&&arr.moduleList[0].moduleSign=="moduleCombination"){
						this.isShowHead=false;
					}else{
						this.isShowHead=true;
					}
					var goodsArr = res.data.goodsList;
					this.config.title = JSON.parse(res.data.pagedata).pageBaseInfo.title;
					uni.removeStorage('attentionData');
					uni.setStorage({
						key: 'attentionData',
						data: res.data,
					});
					if (this.publicVariable.thirdPart.shopId) { //品牌店铺
						this.config.back = false;
					}
					
					this.titShow = true;
					if (goodsArr) {
						goodsArr.forEach((item, index) => {
							arr.moduleList.forEach((item1, index1) => {
								if (item1.moduleSign == 'moduleGoods'|| item1.moduleSign == 'moduleRecommendedTopic') {
									if (item1.moduleJson.items&&item1.moduleJson.items.length > 0) {
										item1.moduleJson.items.forEach((item2, index2) => {
											if (item2.goodsId == item.goodsId) {
												if (item.goodsStart == 1) {
													arr.moduleList[index1].moduleJson.items[index2].commonPrice = item.commonPrice;
													arr.moduleList[index1].moduleJson.items[index2].commonSalenum = item.commonSalenum;
												} else {
													arr.moduleList[index1].moduleJson.items.splice(index2, 1);
												}
											}
							
							
										})
									}
							
								} else if (item1.moduleSign == "moduleDiscount" || item1.moduleSign == "moduleSecKill") {
									var lenNum = 0;
									if (item1.moduleJson.items && item1.moduleJson.items.length > 0) {
										item1.moduleJson.items.forEach((item2, index2) => {
											if (item2.items.length > 0) {
												item2.items.forEach((item3, index3) => {
													if (item3.goodsId == item.goodsId) {
														if (item.activityStatus == 1 && item.goodsStart == 1) {
															if (item1.moduleSign == "moduleSecKill") {
																arr.moduleList[index1].moduleJson.items[index2].items[index3].seckillPrice = item.commonPrice;
															} else if (item1.moduleSign == "moduleDiscount") {
																arr.moduleList[index1].moduleJson.items[index2].items[index3].discountPrice = item.commonPrice;
															} else {
																arr.moduleList[index1].moduleJson.items[index2].items[index3].commonPrice = item.commonPrice;
															}
															if (item.endTime) {
																arr.moduleList[index1].moduleJson.items[index2].end_time = item.endTime;
																arr.moduleList[index1].moduleJson.items[index2].items[index3].end_time = item.endTime;
															}
															arr.moduleList[index1].moduleJson.items[index2].items[index3].commonSalenum = item.commonSalenum;
							
														} else {
															if (item.activityStatus == 2) {
																lenNum++;
																var lens = item2.items.length;
																arr.moduleList[index1].moduleJson.items[index2].items.splice(index3, 1);
																if (lenNum == lens) {
																	arr.moduleList[index1].moduleJson.items.splice(index2, 1);
																}
															}
														}
							
													}
												})
											}
							
							
							
										})
									}
								} else if (item1.moduleSign == "modulePintuan") {
									var lenNum = 0;
									if (item1.moduleJson.items&&item1.moduleJson.items.length > 0) {
										item1.moduleJson.items.forEach((item2, index2) => {
											if (item2.items) {
												if (item2.items.goodsId == item.goodsId) {
													if (item.activityStatus == 1 && item.goodsStart == 1) {
														arr.moduleList[index1].moduleJson.items[index2].items.commonPrice = item.commonPrice;
														arr.moduleList[index1].moduleJson.items[index2].items.commonSalenum = item.commonSalenum;
													} else {
														if (item.activityStatus == 2) {
															lenNum++;
															var lens = item1.moduleJson.items.length;
															arr.moduleList[index1].moduleJson.items.splice(index2, 1);
															if (lenNum == lens) {
																arr.moduleList.splice(index1, 1);
															}
														}
													}
												}
											}
										})
									}
								}else if( item1.moduleSign == 'moduleGoodsGroup'){
									item1.moduleJson.items.forEach((item2,index2)=>{
										if(item2.lists && item2.lists.length > 0){
											item2.lists.forEach((item3,index3) =>{
												if(item.goodsId == item3.goodsId ){
													arr.moduleList[index1].moduleJson.items[index2].lists[index3] =item;
												}
											})
										}
									})
								}
							})
						})
					}
					this.componentLists = arr.moduleList;
				})
			},
			curProps() {
				
				return this.componentLists
			},
			async fetchData() {
				var title = '';
				quertNavLogo({
					configKey: 'logo_copywriting_ico_h5_title',
					configType: 'logo_copywriting'
				}).then(res => {
					if (res.code == 1) {
						title = res.data[0].configValue
					}
				})
				return title;
			},
			getUserPhone() {
				queryGetuser().then(res => {
					if (res.code == 1) {
						if (res.data.userMobile) {
							this.$token.setToken("mobilePhone", res.data.userMobile);
						}
					}
				})

			},
			//事件处理函数
			bindViewTap: function(e) {
				uni.navigateTo({
					url: '/general/searchpage/searchpage'
				})
			},
		}
	}
</script>

<style>
	@import '@/components/homeIndex/homeIndex.css';

	.diy-module {
		margin-top: 0 !important;
	}
</style>