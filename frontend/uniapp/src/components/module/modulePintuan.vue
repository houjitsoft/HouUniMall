<template>
	<view class="modulePintuan activity-module" :class="[propsobj.isBorder=='2'?'border':'',propsobj.isRadius=='2'?'radius':'']" :style="{marginTop:propsobj.padding,marginBottom:propsobj.padding}">
		<view class="activity-title h5"><text>{{ getTransNew(propsobj.titleName)}}</text></view>
		<view class="activity-goods-items ul" :class="[propsobj.style=='2'?'style2':'']">
			<view class="item li flex"  v-for="(item,index) in propsobj.items" :key="index" @click="goDetail(item.items.goodsId,item.items.commonId)">
				<view class="img-box" :style="{backgroundImage:'url('+item.items.commonImage+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}"></view>
				<view class="activity-text-text flex1 wp100">
					<text class="tit text-overflow span">{{ getTransNew(item.items.commonName)}}</text>
					<view v-if="propsobj.isSale=='1'">
						<text class="total-people b" :style="{color:themeData.contMxColor}">{{item.items.personNum}}{{ getTransNew('人团')}}</text>
					</view>
					<view class="goods-desc flex space-between">
						<text class="sale-price theme-color em" :style="{color:themeData.contMxColor}">{{currencySymbol()}}{{getRatePrice(item.items.price)}}</text>
						<button v-if="propsobj.style=='1'" class="activity-btn" :style="{background:themeData.contMxColor}">{{ getTransNew(propsobj.btnName)}}</button>
						<icon v-else class="iconfont icon-cart theme-color" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.items.goodsId)"></icon>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	let app = getApp({allowDefault: true});
	import{
		queryShoppingCart
	} from "@/api/pages/description"
	
	export default {
		
		name: 'modulePintuan',
		props: ['propsobj'],
		data() {
			return {
				
			themeData: this.publicVariable.themeData,
			token:uni.getStorageSync('token'),
			}
		},
		methods: {
			
			// 加入购物车
			async addCart(id){
				if(this.token==''){
					uni.showToast({
						icon:'none',
						title:await this.getTransNew('请先登录')
					})
					return false;
				}
				var key = uni.getStorageSync('key');
				var uid = uni.getStorageSync('u');
				queryShoppingCart({
					goodsId: id,
					goodsNum: 1
				}).then(async res=>{
					if(res.code==1){
						uni.showToast({
							icon:'none',
							title:await this.getTransNew("成功加入购物车")
						})
					}else{
						uni.showToast({
							icon:'none',
							title:await this.getTransNew(res.message)
						})
					}
				})
				
			},
			// 进去详情
			goDetail(goodsId,commonId){
				uni.navigateTo({
					url:'../../pages/description/descrption?goodsId='+goodsId+'&commonId='+commonId
				})
			}
		}
	}
</script>

<style>
	.flex{display:flex;}
	.flex1{flex:1;}
	.space-between {
	    justify-content: space-between;
	}
	.activity-module {
	    margin: 0 20rpx;
	    padding-left: 20rpx;
	    padding-right: 20rpx;
		padding-bottom:10rpx;
	    box-shadow: none;
		background:#fff;
	}
	.activity-module.radius {
	    border-radius: 4px;
	}
	.activity-module.border {
	    border: 1px solid #e0e0e0;
	    box-shadow: none;
	}
	.activity-title{
		font-weight: bold;
		font-size: 28rpx;
		line-height: 80rpx;
		color:#5c5c5c;
	}
	.activity-goods-items .li{
		position:relative;
		margin-bottom:20rpx;
	}
	.activity-goods-items .li:last-child{
		/* margin-bottom:0; */
	}
	.activity-goods-items .li .img-box{
		display:inline-block;
		width:200rpx;
		height:200rpx;
	}
	.activity-goods-items .li .activity-text-text{
		flex:1;
		display: inline-block;
		margin-left:20rpx;
		position:relative;
		height:200rpx;
		vertical-align: top;
	}
	.activity-text-text .tit{
		display: inline-block;
		margin-bottom: 12rpx;
		font-size: 28rpx;
		color: #212121;
		line-height: 48rpx;
		width:100%;
	}
	.activity-text-text .total-group{
		display: inline-block;
		margin-top: 8rpx;
		line-height: 48rpx;
		font-size:24rpx;
		color:#999;
	}
	.total-people {
	    display: inline-block;
	    padding: 0 3px;
	    font-size: 24rpx;
	    color: #fd3d53;
	    line-height: 40rpx;
	    background-color: rgba(239, 16, 51, 0.1);
	}
	.goods-desc .sale-price {
	    font-family: -webkit-body;
	    font-size: 28rpx;
	    font-weight: bold;
	    line-height: 60rpx;
		vertical-align: top;
	}
	.theme-color {
	    color: #fd3d53;
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
	.activity-goods-items.style2 .li{
		display:inline-block;
		width:313rpx;
	}
	.activity-goods-items.style2 .li:nth-child(even){
		margin-left:40rpx;
	}
	.activity-goods-items.style2 .li .img-box{
		width: 100%;
		 height: 313rpx;
	}
	.activity-goods-items.style2 .li .goods-desc{
		position:relative;
		bottom:-12rpx;
	}
	.activity-goods-items.style2 .li .iconfont{
		font-size:32rpx;
		padding-right:10rpx;
		vertical-align:top;
		line-height:60rpx;
	}
	.activity-goods-items.style2 .li .sale-price{
		font-size:32rpx;
	}
</style>
