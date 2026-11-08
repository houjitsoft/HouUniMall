<template>
	<!-- 瀑布流模板 -->
	<view class="">
		<template v-if="listData.length > 0 || false">
			<u-waterfall class="waterList" :modelValue="listData">
				<template v-slot:left="{leftList}">
					<view class="leftGoods" v-for="(item, index) in leftList" :key="item.commonId">
						<image mode="widthFix"  @click="handleGoDescription(item)" :src="item.commonImage"></image>
						<text class="commonName">{{item.commonName}}</text>
						<view class="bottomBtn">
							<view class="price">
								<text class="fz24">{{currencySymbol()}}</text>
								<text class="fz30">{{getRatePrice(item.commonPrice)}}</text>
							</view>
							<view>
								<button v-show="item.shareFlag"  @click="handleShare(item)">{{'立即分享'}}</button>
							</view>
						</view>
					</view>
				</template>
				<template v-slot:right="{rightList}">
					<view class="rightGoods" v-for="(item, index) in rightList" :key="item.commonId">
						<image mode="widthFix" @click="handleGoDescription(item)" :src="item.commonImage"></image>
						<text class="commonName">{{item.commonName}}</text>
						<view class="bottomBtn">
							<view class="price">
								<text class="fz24">{{currencySymbol()}}</text>
								<text class="fz30">{{getRatePrice(item.commonPrice)}}</text>
							</view>
							<view>
								<button v-show="item.shareFlag"  @click="handleShare(item)">{{'立即分享'}}</button>
							</view>
						</view>
					</view>
				</template>
			</u-waterfall>
		</template>
		<template v-else>
			<view class="recordListEmpty pt200">
				<view class="recordListEmptyContainer">
					<image class="deleteImage" :src="appPreImg+'wudingdan.png'" mode="widthFix"></image>
					<view class="color9e9e9e">{{getTransNew('—— 暂无更多数据 ——')}}</view>
				</view>
			</view>
		</template>

		<u-popup mode="bottom" border-radius="20" :show="shareShow" @close="shareShow=false">
			<view class="live-share-dialog">
				<button class="live-share-li" @click="handleCopy">
					<image mode="widthFix" class="img" :src="appPreImg + 'h5Lianjie.png'"></image>
					<view class="text">{{ getTransNew('复制H5链接')}}</view>
				</button>
				<!-- #ifndef H5 -->
				<button class="live-share-li" open-type="share">
					<image mode="widthFix" class="img" :src="appPreImg + 'wechat.png'"></image>
					<view class="text">{{ getTransNew('发给朋友')}}</view>
				</button>
				<!-- #endif -->
			</view>
		</u-popup>
	</view>
</template>

<script>
	
	export default {
		name: 'goodsList',
		props: {
			listData: {
				type: Array,
				default: () => {
					return []
				}
			},
			share:{
				type: Boolean,
				default: false
			}
		},
		data() {
			return {
				
				appPreImg:this.appPreImg,
				shareShow:false
			}
		},
		methods: {
			handleShare(item){
				this.shareShow = true;
				this.goodsItem = item;
				// this.$emit("share", this.goodsItem,类型 1点击立即分享按钮 2点击复制)
				this.$emit("share", this.goodsItem,1)
			},
			handleCopy(){
				this.$emit("share", this.goodsItem)
				this.shareShow = false;
			},
			handleGoDescription(item) {
				this.$emit("clickGoods", item)
			},
			

		},
	}
</script>

<style lang="scss" scoped>
	.u-waterList {
		display: flex !important;
		justify-content: space-around !important;
	}

	 .u-column {
		border-radius: 20rpx !important;
		margin: 20rpx !important;
	}

	.price {
		color: #e40505;
		font-weight: bold;
		margin-left: 10rpx;
	}

	.leftGoods,
	.rightGoods {
		/* #ifdef H5 */
		width: 95%;
		/* #endif */
		/* #ifndef H5 */
		width: 95%;
		/* #endif */
		border-radius: 20rpx;
		background: white;
		display: flex;
		flex-direction: column;
		overflow: hidden;
		
		padding: 0rpx 0rpx 15rpx;
	}
	.leftGoods{
		margin: 18rpx 10rpx;
	}
	.rightGoods{
		/* #ifdef H5 */
		margin: 18rpx 10rpx;
		/* #endif */
		/* #ifndef H5 */
		margin: 30rpx 10rpx;
		/* #endif */
		
	}

	.leftGoods image,
	.rightGoods image {
		width: 100%;
		height: 350rpx;
	}

	.leftGoods .commonName,
	.rightGoods .commonName {
		line-height: 2;
		margin: 10rpx;
		display: -webkit-box;
		overflow: hidden;
		text-overflow: ellipsis;
		-webkit-line-clamp: 3;
		-webkit-box-orient: vertical;
	}
	
.bottomBtn{
	display: flex;
	justify-content: space-between;
	align-items: center;
}
.bottomBtn button{
	padding: 6rpx 15rpx;
	line-height: 35rpx;
	font-size: 24rpx;
	border: 1rpx solid var(--webColor);
	border-radius: 25rpx;
	color: var(--webColor);
	margin: 20rpx 15rpx 15rpx;
}
/* 分享 */
.live-share-dialog {
	width: 100%;
	z-index: 999;
	padding: 30rpx 0 60rpx;
	border-top-left-radius: 20rpx;
	border-top-right-radius: 20rpx;
	text-align: center;

}

.live-share-head {
	margin-bottom: 50rpx;
	font-size: 26rpx;
	color: #4a4a4a;
}

.live-share-li .text {
	margin-top: 14rpx;
	font-size: 24rpx;
	color: #4a4a4a;
}

.live-share-li {
	display: inline-block;
	margin: 0 80rpx;
	text-align: center;
	line-height: 28rpx;
	background: transparent;
}

.live-share-li .img {
	width: 80rpx;
	height: 80rpx;
}
</style>