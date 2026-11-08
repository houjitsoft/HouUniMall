<template>
	<view class="moduleRecommendedTopic" :style="{paddingTop:propsobj.padTop,paddingBottom:propsobj.padBom}" :class="[propsobj.goodsStyle=='1'?'ads':'']">
		<div class="conts" :style="{borderRadius:propsobj.borderStyle}">
			<block v-if="propsobj.imageLink || currentImage">
				<navigator class="adTop" :url="propsobj.imageLink">
					<image class="wp100" mode='widthFix' :src="currentImage" alt=""></image>
				</navigator>
			</block>
			<block v-if="currentProducts.length>0">
				<view class="goods-items-style2 ul pt20 goodsBottoms" :class="[propsobj.goodsStyle=='1'?'goodsAds':'',propsobj.borderStyle !='0px'?'radius':'']">
					<view :class="styleName">
						<view class="li" v-for="(item,index) in currentProducts" :key="index" @click="goDetail(item.goodsId,item.commonId)">
							<image class='wp100 commonImage' :src='item.commonImage'></image>
							<view class="goods-text">
								<text class="goods-tit text-overflow block">{{ getTransNew(item.commonName)}}</text>
								<text v-if="propsobj.sale" class="goods-sale-style2">{{ getTransNew('已售')}}{{item.commonSalenum}}{{ getTransNew('件')}}</text>
								<view class="flex space-between goods-text-module">
									<view class="goods-price">
										<text class="rmb span" :style="{color:themeData.contMxColor}">{{currencySymbol()}}{{getRatePrice(item.commonPrice)}}</text>
									</view>
									<view class="goods-sale">
										<text type="value" v-if="propsobj.cart&& propsobj.cartStyle=='1'" class="iconfont icon-cart" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.goodsId)"></text>
										<text type="value" v-else-if="propsobj.cart&& propsobj.cartStyle=='2'" class="iconfont icon-jia1" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.goodsId)"></text>
									</view>
								</view>
							</view>

						</view>
					</view>
				</view>
			</block>
		</div>
	</view>
</template>

<script>
	import {checkToken} from '@/utils/util.js'
	import {
	  calculateCurrentImage, 
	  calculateNextSwitchTime, 
	  startAutoSwitch,
	  calculateCurrentProducts
	} from '@/utils/periodicUtils';
	let app = getApp({allowDefault: true});
	import {
		queryShoppingCart
	} from "@/api/pages/description"
	export default {
		name: 'moduleRecommendedTopic',
		props: ['propsobj'],
		data() {
			return {
				themeData: this.publicVariable.themeData,
				token: uni.getStorageSync('token'),
				styleName: '',
				currentIndex: 0,
				timer1: null,
				timer2: null,
				currentImage:'',
				currentImageIndex: 0,
				currentProducts:[],
			}
		},
		computed: {
			 // 当前显示的图片
			currentImage() {
				if (!this.propsobj.imageUrlList.length) return '';
				return this.propsobj.imageUrlList[this.currentImageIndex];
			}
		 },
		 watch:{
		 	propsobj: {
		 		handler(newVal, oldVal) {
		 			if(newVal.randomEnabled){
		 				this.updateCurrentProducts();
		 				this.startTimerGoods();
		 			}else{
		 				this.currentProducts=newVal.items;
		 			}
		 		},
		 		immediate: true,
		 		deep: true
		 	}
		 },
		mounted() {
			var that = this;
			if (this.propsobj.style == '1') {
				that.styleName = ''
			} else if (this.propsobj.style == '2') {
				that.styleName = 'goods-items-style3'
			} else if (this.propsobj.style == '3') {
				that.styleName = 'nowrap goods-items-style5'
			}
			if(this.propsobj.imageEnabled){
				this.updateCurrentImage();
				this.startTimerImg();
			}else{
				this.currentImage=this.propsobj.imageUrl;
			}
			if(this.propsobj.randomEnabled){
				this.updateCurrentProducts();
				this.startTimerGoods();
			}else{
				this.currentProducts=this.propsobj.items;
			}
		},
		methods: {
			//切换图片
			 updateCurrentImage() {
			  const result = calculateCurrentImage(this.propsobj.imageUrlList, this.propsobj.imageDays,this.propsobj.imageStartDate);
			  this.currentImageIndex = result.index;
			  this.currentImage=this.propsobj.imageUrlList[this.currentImageIndex];
			},
			//切换商品
			updateCurrentProducts() {
			  const result = calculateCurrentProducts(
				this.propsobj.items, 
				this.propsobj.goodsDays, 
				this.propsobj.goodsNum,
				this.propsobj.goodsStartDate
			  );
			  this.currentProducts = result.products;
			},
			startTimerImg() {
			  // 清理之前的定时器
			  if (this.timer1) {
				this.timer1.clear();
			  }
			  // 启动新的定时器
			  this.timer1 = startAutoSwitch(this.propsobj.imageDays, () => {
				this.updateCurrentImage();
			  }, this.propsobj.imageStartDate);
			},
			startTimerGoods() {
			  // 清理之前的定时器
			  if (this.timer2) {
				this.timer2.clear();
			  }
			  
			  // 启动新的定时器
			  this.timer2 = startAutoSwitch(this.propsobj.goodsDays, () => {
				this.updateCurrentProducts();
			  }, this.propsobj.goodsStartDate);
			},
			// 加入购物车
			addCart(id) {
				checkToken().then(async(vdata) => {
					if (vdata) {
						queryShoppingCart({
							goodsId: id,
							goodsNum: 1
						}).then(async res => {
							if (res.code == 1) {
								uni.showToast({
									icon:'none',
									title:await this.getTransNew("成功加入购物车")
								})
							} else {
								uni.showToast({
									icon: 'none',
									title: await this.getTransNew(res.message)
								})
							}
						})
					} else {
						uni.showToast({
							icon: 'none',
							title: await this.getTransNew('请先登录')
						})
						return false;
					}
				})



			},
			// 立即购买
			goBuy(goodsid, commonid) {
				uni.navigateTo({
					url: '../../pages/description/descrption?goodsId=' + goodsid + '&commonId=' + commonid
				})
			},
			// 进去详情
			goDetail(goodsId, commonId) {
				uni.navigateTo({
					url: '../../pages/description/descrption?goodsId=' + goodsId + '&commonId=' + commonId
				})
			}
		}
	}
</script>

<style lang="scss">
	.pt20 {
		padding-top: 20rpx !important
	}

	;

	.pl20 {
		padding-left: 20rpx !important;
	}

	.iblock {
		display: inline-block;
	}

	.flex {
		display: flex;
	}

	.space-between {
		justify-content: space-between;
	}

	.img-box {
		display: inline-block;
		width: 100%;
		height: 300rpx;
	}

	.goods-style1 {
		padding-bottom: 20rpx;
		margin-bottom: 20rpx;
		background: #fff;
	}

	.goods-default-img {
		padding: 20rpx;
		padding-bottom: 0;
		box-sizing: border-box;
	}

	.goods-price .rmb,
	.goods-price .pri {
		margin-right: 6rpx;
		font-size: 32rpx;
	}

	.goods-price .dot-pri,
	.goods-price .ori-pri {
		font-size: 26rpx;
	}

	.goods-price .rmb,
	.goods-price .pri,
	.goods-price .dot-pri {
		font-weight: bold;
		color: #fa6868;
	}

	.goods-tit {
		display: inline-block;
		margin-bottom: 20rpx;
		font-size: 28rpx;
		color: #383d41;
		width: 100%;
		white-space: normal;
	}

	.goods-text-module {
		padding-top: 10rpx;
	}

	.goods-text-module .del {
		font-size: 24rpx;
		margin-left: 8rpx;
		color: #9e9e9e;
	}

	.goods-text-module .icon-cart {
		font-size: 32rpx;
		color: #fa6868;
	}


	.goods-items-style2 {
		overflow: hidden;
		padding-left: 2%;
	}

	.goods-items-style2 {
		.li {
			display: inline-block;
			margin: 0 2% 2% 0;
			// padding:0 10rpx;
			width: 48%;
			vertical-align: top;
			box-sizing: border-box;
			font-size: 0;
			background: #fff;
		}

	}

	.goods-text {
		padding: 20rpx;
	}

	.goods-sale-style2 {
		display: block;
		margin-bottom: 16rpx;
		font-size: 24rpx;
		color: #9e9e9e;
		margin-top: 10rpx;
	}

	.goods-items-style2 .icon-cart {
		font-size: 36rpx;
	}

	.goods-items-style4 {
		padding: 10rpx 20rpx;
	}

	.goods-items-style4 .li {
		display: flex;
		margin-bottom: 20rpx;
	}

	.goods-items-style4 .img {
		width: 200rpx;
		height: 200rpx;
	}

	.goods-items-style4 .goods-text {
		padding-top: 0;
		padding-bottom: 0;
	}

	.goods-items-style4 .goods-text-module {
		padding-top: 30rpx;
	}

	.goods-items-style5 {
		overflow: auto;
		white-space: nowrap;
	}

	.goods-items-style5 .li {
		width: 200rpx;
		flex-shrink: 0;
	}

	.goods-items-style5 .goods-tit {
		font-size: 24rpx;
	}

	.btn-goods {
		margin-top: -6rpx;
		padding: 0;
		line-height: 60rpx;
		height: 60rpx;
		width: 172rpx;
		font-size: 24rpx;
		color: #fff;
		background: #fd3d53;
		border: none;
		border-radius: 4rpx;
		vertical-align: top;
	}

	.goods-items-style3 .img-box {
		height: 214rpx;
	}

	.goods-items-style3 .li {
		width: 31.3%;
		// flex:0 0 120px;
	}

	.goods-items-style3 .li .rmb,
	.goods-items-style5 .li .rmb {
		font-size: 26rpx;
	}

	.goods-items-style3 .li .icon-cart,
	.goods-items-style5 .li .icon-cart {
		font-size: 28rpx;
	}

	.goods-items-style3 .goods-text,
	.goods-items-style5 .goods-text {
		padding: 20rpx 0;
	}

	.goods-items-style4 .li .img-box {
		display: inline-block;
		width: 200rpx;
		height: 200rpx;
	}

	.goods-items-style5 .li .img-box {
		display: inline-block;
		width: 180rpx;
		height: 180rpx;
	}

	.goods-scroll {
		white-space: nowrap;
	}

	.moduleRecommendedTopic {
		display: inline-block;
		width: 100%;
		position: relative;
		padding-left: 20rpx;
		padding-right: 20rpx;
		box-sizing: border-box;
	}

	.moduleRecommendedTopic .conts {
		box-shadow: none;
		background: #fff;
		overflow: hidden;
	}

	.moduleRecommendedTopic.ads .adTop {
		position: absolute;
		z-index: 0;
		left: 0;
		top: 0;
		width: 100%;
	}

	.moduleRecommendedTopic.ads .goodsBottoms {
		position: relative;
		z-index: 2;
		margin-top: 80px;
		background: #fff;
	}
    .commonImage{
        height: auto;
        aspect-ratio: 1 / 1;
        object-fit: cover;
    }
</style>