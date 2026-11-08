<template>
	<view class="activity-module" :style="{marginTop:propsobj.padding,marginBottom:propsobj.padding}" >
		<view class="chain-lists">
			<view class="chainTit clearfix">
				<text class="fl tit">{{propsobj.title}}</text>
				<view class="fr" @click="handleMoreChain">
					<text class="more">更多</text>
					<icon class="iconfont icon-arrow-right"></icon>
				</view>
			</view>
			<view class="ul" :class="[propsobj.isBorder=='2'?'border':'',propsobj.isRadius==2?'radius':'']">
				<view class="li flex" v-for="(item,index) in chainLists" :key="item.shopId" @click="handleGoChain(item.shopId,item.shopGateId)">
					<view class="img-box span" :style="{backgroundImage:'url('+item.gatePic+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}"></view>
					<view class="flex1">
					  <view class="hp100 flex style-tb">
						  <view>
							  <view class="h5">{{item.shopGateName}}</view>
							  <view class="addressInfo">{{item.province}}{{item.city}}{{item.county}}{{item.detailAddress}}</view>
						  </view>
						  <view class="addressDistance">
							<view> 
							  <icon class="iconfont icon-location"></icon>
							  <text class="em" v-if="item.distance>1000">{{(item.distance/1000).toFixed(2)}}KM</text>
							  <text class="em" v-else>{{item.distance}}m</text>
							</view>
							<button class="btnDet" type="text" size='mini' :style="temp">{{propsobj.btnText}}</button>
						   </view>
					  </view>
					</view>
			
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	let app = getApp({allowDefault: true});
	import {
		queryShopNearBy
	} from "@/api/store/index"
	export default {
		name: 'moduleChain',
		props:{
			propsobj:{
				type:Object,
				required:true
			},
			shopId:{
				type:Number,
				default:null
			}
		},
		watch: {

		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				token: uni.getStorageSync('token'),
				themeData: this.publicVariable.themeData,
				params: {
					longitude: 0,
					latitude: 0,
					kilo: 1000,
					page: 1,
					limit: 10,
					shopId: '',
				},
				chainLists:[]
			}
		},
		mounted() {
			this.params.shopId=this.shopId;
			uni.getLocation({
				type: 'gcj02',
				success: (res) => {
					this.params.longitude = res.longitude
					this.params.latitude =res.latitude
					this.getChainList();
				}
			})
		},
		
		methods: {
			//查询门店列表
			getChainList(){
				queryShopNearBy(this.params).then(res=>{
					if(res.code==1){
						this.chainLists=res.data.list;
					}
				})
			},
			//查询更多门店
			handleMoreChain(){
				uni.navigateTo({
					url: '/store/nearbyStores/nearbyStores?shopId='+this.params.shopId
				})
			},
			//进门店
			handleGoChain(shopId,shopGateId){
				uni.navigateTo({
					url: `/store/chainStore/chainStore?shopId=${shopId}&shopGateId=${shopGateId}`
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

<style lang='scss'>
	.chainTit{
		
		margin-bottom:20rpx;
	}
	.chainTit .tit{
		font-size:28rpx;
		font-weight:bold;
	}
	.chainTit .more{
		font-size:24rpx;
	}
	.chainTit .iconfont{
		font-size:24rpx;
	}
	.chain-lists{
		margin:20rpx;
	  background:#fff;
	  padding:20rpx;
	}
	.chain-lists .li{
	  margin-bottom:20rpx;
	}
	.chain-lists .img-box{
		display:inline-block;
		margin-right:20rpx;
		width:170rpx;
		height:170rpx;
		background-color:#f5f5f5;
		border-radius:6rpx;
	}
	.chain-lists .li .h5{
		margin-bottom:10rpx;
		font-size:26rpx;
		font-weight:bold;
	}
	.addressInfo{
		font-size:24rpx;
		color:#666;
	}
	.addressDistance .em{
		font-size:24rpx;
	}
	.addressDistance{
	  display: flex;
	  justify-content:space-between;
	}
	.addressDistance i{
	  font-size:32rpx;
	}
	.chain-head .strong{
	  font-size:28rpx;
	}
	.btnDet{
		display:inline-block;
		margin:0;
		background:none;
		border:0;
		background:var(--webColor);
		color:#fff;
		font-size:24rpx !important;
		line-height:50rpx !important;
	}
	
	
	
	
	
</style>