<template>
	<view class="moduleDiscount activity-module" v-if="propsobj.items.length>0" :style="{temp,marginTop:propsobj.padding,marginBottom:propsobj.padding}">
		<block v-if="propsobj.items.length==1">
			<view class="flex space-between activity-header-specical dl">
				<text class="dt">{{getTransNew(propsobj.items[0].titleName)}}</text>
				<view class="dd flex" v-if="propsobj.isTimeLimit=='1'">
					<text class="mr10 mt6" v-if="open">{{getTransNew('距结束')}}</text>
					<countdown :startTime="startTime" :endTime="propsobj.items[0].end_time" @statusEdit="statusEdit(0)" comStyle="style3" />
				</view>
			</view>
		</block>
		<block v-else>
			<scroll-view scroll-x='true' class="goods-scroll activity-head-tabs li-two ul pt10" :class="[propsobj.items.length>3 ? 'li-more':'']">
				<view class="li" v-for="(item,index) in propsobj.items" :key="index" :class="[curClick==index?'active':'']" @click="selCurrent(index)">
					<text class="h5">{{getTransNew(item.titleName)}}</text>
					<view v-if="propsobj.isTimeLimit=='1'">
						<view class="curActivity activity-status" v-if="curClick==index">
							<countdown :startTime="startTime" :endTime="item.end_time" @statusEdit="statusEdit(index)" comStyle="style2" />
						</view>
						<view class="activity-status others" v-else>
							<countdown style="visibility:hidden;position:absolute;" :startTime="startTime" :endTime="item.end_time" @statusEdit="statusEdit(index)" comStyle="style2" />
							<text>{{getTransNew('进行中')}}</text>
						</view>
					</view>
				</view>
			</scroll-view>
			<view v-if="propsobj.items.length>3" class="scroll_right"></view>
		</block>
		<view class="ul activity-goods-items" v-if="propsobj.items[curClick]" :class="[propsobj.style=='2'?'style2':'']">
			<view class="li" v-if="propsobj.items[curClick].items==''">
				<text class="activity-end">{{getTransNew('商品已经被抢光了～')}}</text>
			</view>
			<view class="item li" v-else v-for="(item,index) in propsobj.items[curClick].items" :key="index" @click="goDetail(item.goodsId,item.commonId)">
				<view class="img-box span" :style="{backgroundImage:'url('+item.goodsImage+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}"></view>
				<view class="activity-text-text">
					<text class="tit text-overflow span">{{getTransNew(item.goodsName)}}</text>
					<view v-if="propsobj.isSale=='1'">
						<!-- <progress percent="20" stroke-width="3" activeColor="#ef1033"/> -->
						<text class="total-group b">已抢{{item.commonSalenum}}件</text>
					</view>
					<view class="goods-desc flex space-between">
						<view>
							<text class="sale-price theme-color" :style="{color:themeData.contMxColor}">{{currencySymbol()}}{{getRatePrice(item.discountPrice)}}</text>
							<text v-if="propsobj.isOriginPrice" class="underline-price b">{{currencySymbol()}}{{getRatePrice(item.goodsPrice)}}</text>
						</view>
						<button v-if="propsobj.style=='1'" class="activity-btn" :style="{backgroundColor:themeData.contMxColor}">{{propsobj.items[curClick].btnName}}</button>
						<icon v-else class="iconfont icon-cart theme-color" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.goodsId)"></icon>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import countdown from "@/components/cz-countdown/cz-countdown.vue"
	let app = getApp({allowDefault: true});
	import {
		queryShoppingCart
	} from "@/api/pages/description"
	export default {
		components: {
			countdown
		},
		name: 'moduleDiscount',
		props: ['propsobj'],
		watch: {

		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				token: uni.getStorageSync('token'),
				startTime: '',
				curClick: 0,
				open: true,
				themeData: this.publicVariable.themeData
			}
		},
		created() {
			var time = this.getTime();
			this.startTime = time;
		},
		methods: {
			getTime: function() {
				var date = new Date(),
					year = date.getFullYear(),
					month = date.getMonth() + 1,
					day = date.getDate(),
					hour = date.getHours() < 10 ? "0" + date.getHours() : date.getHours(),
					minute = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes(),
					second = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
				month >= 1 && month <= 9 ? (month = "0" + month) : "";
				day >= 0 && day <= 9 ? (day = "0" + day) : "";
				var timer = year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
				return timer;
			},
			selCurrent(index) {
				var time = this.getTime();
				this.startTime = time;
				this.curClick = index;
			},
			// 加入购物车
			async addCart(id) {
				if (this.token == '') {
					uni.showToast({
						icon: 'none',
						title: await this.getTransNew('请先登录')
					})
					return false;
				}
				var key = uni.getStorageSync('key');
				var uid = uni.getStorageSync('u');
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
			},
			// 进去详情
			goDetail(goodsId, commonId) {
				uni.navigateTo({
					url: '../../pages/description/descrption?goodsId=' + goodsId + '&commonId=' + commonId
				})
			},
			statusEdit(index) {
				this.open = false;
				this.propsobj.items.splice(index, 1);
				this.curClick = 0;
			}
		}
	}
</script>

<style>
	.mask {
		position: absolute;
		left: 0;
		top: 0;
		z-index: 2;
		width: 100%;
		height: 100%;
		background: rgba(153, 153, 153, .4);
	}

	.mt6 {
		margin-top: 12rpx;
	}

	.align-middle {
		vertical-align: middle;
	}

	.goods-scroll {
		white-space: nowrap;
	}

	.theme-color {
		color: #fd3d53;
	}

	.space-between {
		justify-content: space-between;
	}

	.activity-module {
		position: relative;
		margin: 0 20rpx;
		padding-left: 20rpx;
		padding-right: 20rpx;
		padding-bottom: 10rpx;
		box-shadow: none;
		background: #fff;
	}

	.activity-goods-items {
		/* background:#fff; */
	}

	.activity-goods-items .li {
		display: flex;
	}

	.activity-text-text {
		flex: 1;
	}

	.activity-status {
		font-size: 24rpx;
	}

	.activity-head-tabs {
		margin-bottom: 20rpx;
		/* display: flex;
		justify-content: space-between;
		align-items: center; */
		width: 100%;
		height: 60px;
		text-align: center;
		/* 	overflow:hidden; */
		/* background:#fff; */
	}

	.activity-head-tabs .h5 {
		font-size: 32rpx;
	}

	.activity-head-tabs.li-two .li {
		display: inline-block;
		/* padding:10px 0; */
		width: 50%;
		border-right: 1px solid #ededed;
		box-sizing: border-box;
		vertical-align: top;
	}

	.activity-head-tabs.li-two .li:last-child {
		border-right: 0;
	}

	.activity-head-tabs.li-two .li.active .h5 {
		color: #ef1033;
	}

	.activity-head-tabs.li-more .li {
		display: inline-block;
		padding: 0 10rpx;
		min-width: 90px;
		width: auto;
	}

	.activity-goods-items .li {
		position: relative;
		padding-bottom: 20rpx;
	}

	.activity-goods-items .li:last-child {
		/* margin-bottom:0; */
	}

	.activity-goods-items .li .img-box {
		display: inline-block;
		width: 200rpx;
		height: 200rpx;
	}

	.activity-goods-items .li .activity-text-text {
		display: inline-block;
		margin-left: 20rpx;
		position: relative;
		height: 200rpx;
		vertical-align: top;
	}

	.activity-module.border {
		border: 1px solid #e0e0e0;
		box-shadow: none;
	}

	.activity-module.radius {
		border-radius: 4px;
	}

	.activity-text-text .tit {
		display: inline-block;
		margin-bottom: 12rpx;
		font-size: 28rpx;
		color: #212121;
		line-height: 48rpx;
		width: 100%;
	}

	.activity-text-text .total-group {
		display: inline-block;
		margin-top: 8rpx;
		line-height: 48rpx;
		font-size: 24rpx;
		color: #999;
	}

	.activity-btn {
		padding: 0 30rpx;
		font-size: 24rpx;
		color: #fff;
		border-radius: 4rpx;
		border: 0;
		background: #f53d53;
	}

	.goods-desc {
		position: absolute;
		border: 0;
		width: 100%;
		bottom: 12rpx;
	}

	.goods-desc .sale-price {
		font-family: -webkit-body;
		font-size: 30rpx;
		font-weight: bold;
		line-height: 60rpx;
	}

	.underline-price {
		margin-left: 10rpx;
		font-size: 24rpx;
		color: #9e9e9e;
		text-decoration: line-through;
	}

	.activity-goods-items.style2 .li {
		display: inline-block;
		width: 324rpx;
	}

	.activity-goods-items.style2 .li:nth-child(even) {
		margin-left: 20rpx;
	}

	.activity-goods-items.style2 .li .img-box {
		width: 100%;
		height: 298rpx;
	}

	.activity-goods-items.style2 .li .goods-desc {
		position: relative;
		bottom: -12rpx;
	}

	.activity-goods-items.style2 .li .iconfont {
		font-size: 40rpx;
		line-height: 60rpx;
	}

	.activity-goods-items.style2 .li .sale-price {
		font-size: 36rpx;
	}

	.activity-active {
		color: #2589ff;
	}

	.curActivity .b {
		display: inline-block;
		background: #fd3d53;
		color: #fff;
	}

	.curActivity {
		font-size: 12px;
		color: #fd3d53;
	}

	.activity-header-specical {
		padding: 15px 0 10px;
		margin-bottom: 10px;
		border-bottom: 1px solid #e3e3e3;
	}

	.activity-header-specical .dt {
		font-size: 34rpx;
		font-weight: bold;
		color: #000;
	}

	.activity-header-specical .dd {
		font-size: 24rpx;
	}

	.activity-header-specical .b {
		display: inline-block;
		margin: 0 6rpx;
		background: #2e2d48;
		border-radius: 4rpx;
		color: #fff;
		line-height: 32rpx;
		padding: 0 4rpx;
	}

	.activity-goods-items.style2 li .activity-text-text {
		margin-bottom: 20rpx;
		width: 280rpx;
		height: auto;
	}

	.activity-goods-items.style2 li .goods-desc {
		width: 300rpx;
		right: 8rpx;
	}

	.activity-end {
		margin-top: 140rpx;
		padding-bottom: 70rpx;
		font-size: 28rpx;
		color: #9e9e9e;
		text-align: center;
	}

	.scroll_right {
		position: absolute;
		top: 0;
		right: 40rpx;
		width: 100rpx;
		height: 120rpx;
		z-index: 100;
		pointer-events: none;
		background: #FFFFFF;
		background: #FFFFFF;
	}
</style>