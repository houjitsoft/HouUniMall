<template>
	<view class="li index-width-half" @click="goDetail(params.goodsId,params.commonId)">
		<view class="goods-img-wrap">
			<up-lazy-load class="wp100" img-mode="widthFix" :image="params.commonImage" threshold="100" :error-img="defaultImg"></up-lazy-load>
			<goods-badge :propsobj="propsobjs"></goods-badge>
		</view>
		<view class="goods-text">
			<text class="goods-tit title-overflow block">{{ getTransNew(params.commonName)}}</text>
			<text v-if="propsobjs.sale" class="goods-sale-style2">{{ getTransNew('已售')}}{{params.commonSalenum}}{{ getTransNew('件')}}</text>
			<view class="flex space-between goods-text-module">
				<view class="goods-price">
					<text class="symbol" :style="{color:themeData.contMxColor}">{{currencySymbol()}}</text>
					<text class="rmb span" :style="{color:themeData.contMxColor}">{{getRatePrice(params.commonPrice)}}</text>
					
				</view>
				<view class="goods-sale">
					<text v-if="propsobjs.cart" class="iconfont icon-cart" :style="{color:themeData.contMxColor}" @click.stop="addCart(params.goodsId)"></text>
				</view>
			</view>
		</view>
	
	</view>
	
</template>

<script>
	let app = getApp({allowDefault: true});
	import {checkToken} from '@/utils/util.js'
	import GoodsBadge from "@/components/module/goodsBadge.vue";
	import {
		queryShoppingCart
	} from "@/api/pages/description"
	export default {
		name:"helang-waterfall",
		components: {
			GoodsBadge
		},
		props:{
			params:{
				type: Object,
				default(){
					return {}
				}
			},
			propsobjs:{
				type: Object,
				default(){
					return {}
				}
			},
			tag:{
				type:[String, Number],
				default:''
			},
			index:{
				type:Number,
				default:-1
			}
		},
		data() {
			return {
				themeData: this.publicVariable.themeData,
				defaultImg:this.publicVariable.defaultImg
			};
		},
		methods:{
			// 加入购物车
			addCart(goodsId) {
				checkToken().then(async (vdata) => {
					if (vdata) {
						queryShoppingCart({
							goodsId: goodsId,
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
									title:await this.getTransNew(res.message)
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
			// 发出组件高度信息，在此处可以区分正确和错误的加载，给予错误的提示图片
			emitHeight(e){
				const query = uni.createSelectorQuery().in(this);
				query.select('.waterfall-item').boundingClientRect(data => {
					let height = Math.floor(data.height);
					this.$emit("height",height,this.$props.tag);
				}).exec();
			},
			
			// 进去详情
			goDetail(goodsId, commonId) {
				uni.navigateTo({
					url: '../../pages/description/descrption?goodsId=' + goodsId + '&commonId=' + commonId
				})
			},
			
		}
	}
</script>

<style lang="scss" scoped>
	.goods-img-wrap {
		position: relative;
	}
	.goods-text .goods-tit{
		margin-bottom:10rpx;
		font-size:25rpx;
		color:#010101;
		line-height:36rpx;
	}
	.wp80{width:78%;}
	.user-head{
		display:inline-block;
		margin-right:10rpx;
		width:40rpx;
		height:40rpx;
		border-radius:100%;
		overflow:hidden;
		vertical-align:top;
	}
	.user-name{
		font-size:24rpx;
		color:#999;
	}
	.user-tit{
		display:inline-block;
		width:80%;
		line-height:40rpx;
		vertical-align:top;
	}
	.icon-views{
		display: inline-block;
		margin-right:10rpx;
		font-size:26rpx;
		color:#777;
		vertical-align:top;
		/* #ifdef H5 */
		margin-top:8rpx;
		/* #endif */
		/* #ifdef MP */
		margin-top:-26rpx;
		/* #endif */
	}
	.views-nums{
		display:inline-block;
		margin-right:6rpx;
		font-size:24rpx;
		color:#777;
		vertical-align:top;
	}
	.article-tit{
		font-size:26rpx;
		color:#222;
	}
	.user-right-views{
		/* #ifdef APP-PLUS */
		margin-top:8rpx;
		/* #endif */
	}
.waterfall-item{
	font-size: 28rpx;
	color: #666;
	margin: 10rpx 0 30rpx;
	.img-cont{
		position:relative;
		padding-bottom:20rpx;
		background:#fff;
		border-radius:10rpx;
		overflow: hidden;
		.article-tit{
			margin:10rpx 20rpx;
			line-height:40rpx;
		}
		icon{
			position:absolute;
			right:20rpx;
			top:20rpx;
			z-index:2;
			font-size:28rpx;
			color:#fff;
		}
	}
	image{
		display: block;
		width: 100%;
		// 默认设置一个图片的大约值
		height: 350rpx;
		border-radius:10rpx;
		overflow:hidden;
	}
	
	.content{
		margin-top: 16rpx;
		
		.money{
			color: #fa3534;
			margin-top: 8rpx;
		}
		
		.label{
			background-color: #fa3534;
			color: #fff;
			font-size: 20rpx;
			padding: 4rpx 16rpx;
			border-radius: 20rpx;
		}
		
		.shop-name{
			font-size: 20rpx;
			color: #999;
		}
	}
}
.index-width-half{
	display: inline-block;
	margin: 0 0 5% !important;
	width:100% !important;
	font-size:26rpx !important;
	border-radius: 8rpx;
	background:#fff;
	overflow: hidden;
	.goods-text{
		padding:20rpx;
	}
	
}
.goods-sale-style2 {
	display: block;
	margin-bottom: 16rpx;
	font-size: 24rpx;
	color: #9e9e9e;
	margin-top: 10rpx;
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
		color: #FF3401;
	}
	
</style>
