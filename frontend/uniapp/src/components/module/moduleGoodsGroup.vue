<template>
	<view :style="temp" class="moduleGoodsGroup">
		<view class="conts">
			<scroll-view :scroll-left="scrollTop" scroll-x="true" class="scroll-view-class-module group-uls" :show-scrollbar="false">
			    <view class="li" v-for="(item,index) in propsobj.items" :key='index' @click="changeItems(index)"  :class="[activeIndex==index?'active':'']">
					<view class="h4 oneOverflow" v-if="item.groupName!=''">
						<text class="tit">{{  getTransNew(item.groupName)}}</text>
					</view>	
					<text v-if="item.groupSecondName !=null" class="secondName">{{  getTransNew(item.groupSecondName)}}</text>
				</view>
			</scroll-view>
			<view class="recommend-conts ml10 mr10">
			    <view class="goodsBottoms">
			        <view class="goods-items-style2" v-if="currentProducts.length>0">
						<template v-for="(item,index) in currentProducts" :key="index"> <!-- v-if="item.goodsStart==1 -->
							<view class="li" @click="goDetail(item.goodsId,item.commonId)">
								<view class="img-box" :style="{backgroundImage:'url('+item.commonImage+')'}"></view>
								<view class="goods-text">
									<text class="goods-tit text-overflow">{{ getTransNew(item.commonName)}}</text>
									<text v-if="propsobj.sale" class="goods-sale-style2">{{ getTransNew('已售')}}{{item.commonSalenum}}{{ getTransNew('件')}}</text>
									<view class="flex space-between goods-text-module">
										<view class="goods-price">
											<text class="symbol">{{currencySymbol()}}</text>
											<text class="rmb" v-if="item.commonPrice">{{getRatePrice(item.commonPrice)}}</text>
										</view>
									</view>
								</view>
							</view>
						</template>
						
			        </view>
			    </view>
			</view>
		</view>
	</view>
</template>

<script>
	let app = getApp({allowDefault: true});
	import {
	  startAutoSwitch,
	  calculateCurrentProducts
	} from '@/utils/periodicUtils';
	export default {
		name: 'moduleGoodsGroup',
		props: ['propsobj'],
		watch: {
			propsobj: {
				handler(newName, oldName) {
					 // this.propsobj=newName;
					this.selArrList=newName.items[this.activeIndex].lists;
					if(this.propsobj.randomEnabled){
						this.updateCurrentProducts();
						this.startTimerGoods();
					}else{
						this.currentProducts=this.selArrList;
					}
				},
				immediate: true,
				deep: true
			}
			
		},
		mounted(){
			this.selArrList=this.propsobj.items[this.activeIndex].lists;
			if(this.propsobj.randomEnabled){
				this.updateCurrentProducts();
				this.startTimerGoods();
			}else{
				this.currentProducts=this.selArrList;
			}
		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				themeData:this.publicVariable.themeData,
				token:uni.getStorageSync('token'),
				activeIndex:0,
				scrollTop:0,
				selArrList:[],
				timer:null,
				currentProducts:[]
			}
		},
		methods: {
			//切换商品
			updateCurrentProducts() {
			  const result = calculateCurrentProducts(
				this.selArrList,
				this.propsobj.goodsDays, 
				this.propsobj.goodsNum,
				this.propsobj.startDate
			  );
			  this.currentProducts = result.products;			  
			},
			startTimerGoods() {
			  // 清理之前的定时器
			  if (this.timer) {
				this.timer.clear();
			  }
			  
			  // 启动新的定时器
			  this.timer = startAutoSwitch(this.propsobj.goodsDays, () => {
				this.updateCurrentProducts();
			  }, this.propsobj.startDate);
			},
			changeItems(index){
				this.activeIndex=index;
				this.selArrList=[];
				this.selArrList=this.propsobj.items[this.activeIndex].lists;
				if(this.propsobj.randomEnabled){
					this.updateCurrentProducts();
				}else{
					this.currentProducts=this.selArrList;
				}
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

<style lang="scss">
	.pt20{padding-top:20rpx !important};
	.pl20{padding-left:20rpx !important;}
	.iblock{display:inline-block;}
	.flex{
		display:flex;
	}
	.space-between{
		justify-content:space-between;
	}
	.img-box {
	    display: block;
	    width: 100%;
	    height: 0;
	    padding-bottom: 100%; /* 移动端保持 1:1，避免 cover 裁切字和图 */
	    overflow: hidden;
	    background-repeat: no-repeat;
	    background-position: center;
	    background-size: cover;
	}
	.goods-price .rmb,.goods-price .pri{
		margin-right:6rpx;
		font-size:32rpx;
	}
	.goods-price .dot-pri,.goods-price .ori-pri{
		font-size:26rpx;
	}
	.goods-price .rmb,.goods-price .pri,.goods-price .dot-pri{
		font-weight:bold;
		color:var(--webColor);
	}
	.goods-tit{
		display:inline-block;
		margin-bottom: 10rpx;
		font-size:25rpx;
		color:#010101;
		line-height:36rpx;
		width:100%;
	}
	.goods-text-module{
		// padding-top:10rpx;
	}
	.goods-text-module .del{
		font-size:24rpx;
		margin-left: 8rpx;
		color:#9e9e9e;
	}
	.goods-text-module .icon-cart{
		font-size:32rpx;
		color:#fa6868;
	}
	.goods-sale text{
		display: inline-block;
		margin-right:14rpx;
		vertical-align: middle;
		line-height: 44rpx;
		font-size:24rpx;
		color:#5c5c5c;
	}
	.goods-items-style2{
		overflow:hidden;
	}
	.goods-items-style2{
		.li{
			display:inline-block;
			margin:0 1% 2%;
			// padding:0 10rpx;
			width:48%;
			vertical-align:top;
			box-sizing:border-box;
			font-size:0;
			background:#fff;
			border-radius: 15rpx;
			overflow:hidden;
		}
		
	}
	.goods-text{
		padding:20rpx;
	}
	.scroll-view-class-module{
		padding:20rpx 20rpx 0;
		overflow:auto hidden;
		white-space: nowrap;
		margin-bottom:20rpx;
	}
	.scroll-view-class-module .li{
		display:inline-block;
		// width:25%;
		vertical-align:top;
		text-align:center;
	}
	.scroll-view-class-module text{
		margin:10rpx 16rpx;
		font-size:26rpx;
		color:#333;
		
	}
	.scroll-view-class-module text.active{
		// font-size:32rpx;
		background:var(--webColor);
		// font-weight:normal;
		border-radius:50%;
		color:#fff;
		// color:var(--webColor);
	}
	scroll-view ::-webkit-scrollbar {
	  display:none;
	  width:0;
	  height:0;
	  color:transparent;
	}
	.group-uls .li .h4{
	  margin-bottom:6px;
	}
	.group-uls .li .tit{
	  font-weight:bold;
	  font-size:14px;
	  color:#333;
	}
	.group-uls .li .secondName{
	  display:inline-block;
	  padding:4rpx 6px;
	  font-size:20rpx;
	  color:#888;
	}
	.group-uls .li.active .secondName{
	  background:var(--webColor);
	  color:#fff;
	  border-radius:40px;
	}
</style>
