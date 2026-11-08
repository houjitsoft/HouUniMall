<template>
	<view>
		<view class="moduleHotGoods" :style="{background:propsobj.bgColor,paddingTop:propsobj.padTop,paddingBottom:propsobj.padBom,paddingLeft:propsobj.pageMargin,paddingRight:propsobj.pageMargin}" :class="[propsobj.style=='1'? 'img-style1':propsobj.style=='2'?'swiper-style2-div':'swiper-style3-div']">
			<view class="flex hotGoodsBox fz0">
			  <view class="flex1 hotItem" :class="[item.bgOp==1?'opacity2':'']" v-for="(item,index) in propsobj.items" :style="{marginRight:`${propsobj.interval}px`,'backgroundColor':item.bgColor,borderTopLeftRadius:item.rdsTL+'px',borderTopRightRadius:item.rdsTR+'px',borderBottomLeftRadius:item.rdsBL+'px',borderBottomRightRadius:item.rdsBR+'px'}">
			    <view :style="{'backgroundImage':'url('+item.bgImg+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}">
			      <view class="h5">
			        <text class="strong" :style="{'color':item.titleColor,'fontSize':`${item.titleSize}px`}">{{item.titleName}}</text><text class="b" :style="{'fontSize':`${item.slotSize}px`,'color':item.slotColor,'backgroundColor':item.slotBgColor}">{{item.slotName}}</text>
			      </view>
			      <view class="swiper-style3-div flex ul">
			      	<view class="flex1 li" v-for="(goods,index) in item.list" @click="goInside(goods.link)">
			      		<image mode="widthFix" class="wp100" v-if="goods.imgUrl!=''" :src="goods.imgUrl"></image>
						<text class="block" :style="{marginTop:`${item.labelTop}px`,'color':goods.color,'fontSize':`${goods.size}px`,'backgroundImage':'url('+item.labelImg+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'contain'}">{{goods.goodsName}}</text>
			      	</view>
			      </view>
			    </view>
			  </view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'moduleHotGoods',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				token:uni.getStorageSync('token'),
			}
		},
		methods: {
			goInside(link){
				if(!link){return;}
				uni.navigateTo({
					url:link
				})
			}
		}
	}
</script>

<style lang="scss">
	.hotGoodsBox{
	  .hotItem{
		  &.opacity2{
		     background: #FFFFFF;
		  }
		 &:last-child{
			margin-right:0 !important;
		 }
		 &.radius{
			 border-radius:16rpx;
		 }
	    .h5{
	      padding:10px;
		  .strong{
			  font-weight:bold;
		  }
	      .b{
	        display:inline-block;
	        padding:3px 5px;
	        margin-left:10px;
	        border-radius:10px;
	      }
	
	    }
	    .ul{
	      padding:0 20rpx;
		  // margin-bottom:-4rpx;
	      .li{
	        &:first-child{
	          margin-right:20rpx;
	        }
	        text{
			  position:relative;
	          padding:10rpx 0;
	          text-align:center;
			  margin-bottom: 5px;
	        }
	      }
	    }
	  }
	}
</style>
