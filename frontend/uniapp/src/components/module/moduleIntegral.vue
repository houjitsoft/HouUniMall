<template>
	<view>
		<view class="moduleIntegral" :style="{temp,paddingTop:propsobj.padTop,paddingBottom:propsobj.padBom,paddingLeft:propsobj.pageMargin,paddingRight:propsobj.pageMargin}" :class="[propsobj.style=='1'? 'img-style1':propsobj.style=='2'?'swiper-style2-div':'swiper-style3-div']">
			<view :class="`integral${propsobj.style}`">
			  <view class="moduleRadius integralBox" :class="[propsobj.isRadius=='2'?'radius':'']">
			    <view class="h4" :style="[propsobj.style==1?titStyle1():titStyle2()]">
			      <view>
			        <text v-if="propsobj.titleType==1" class="tit span" :style="{'color':propsobj.titleColor}">{{propsobj.titleText}}</text>
			        <view v-else>
			          <image class="img" mode='heightFix' v-if="propsobj.titleImg!=''" :src="propsobj.titleImg" alt=""></image>
			          <view v-else>
			            <!-- <image class="img" mode='heightFix' v-if="propsobj.style==1" :src="imgUrl+'integralTitDemoBlack.png'" alt=""></image> -->
			            <image class="img" mode='heightFix' :src="imgUrl+'integralTitDemo.png'" alt=""></image>
			          </view>
			        </view>
			      </view>
			      <view class="right" @click="handleMore">
			        <text class="text" :style="{'color':propsobj.rightBtnColor}">{{propsobj.rightBtnText}}</text>
			        <icon class="iconfont icon-arrow-right" :style="{'color':propsobj.rightBtnColor}"></icon>
			      </view>
			    </view>
				<view class="integralGoodsList" :style="{'background': 'linear-gradient(90deg, '+propsobj.bgColor1+','+ propsobj.bgColor2+')'}">
					<scroll-view :class="`goodsStyle${propsobj.goodsType}`" scroll-x='true'>
						<view class="ul">
						  <view class="li" v-for="item in goodsLists" :key="item.goodsId" @click="handleDetail(item.goodsId)">
								<view class="liItem">
									<view class="img-size" :style="{'backgroundImage':'url('+item.commonImage+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}">
										<!--  <image mode='widthFix' class="wp100" :src='item.commonImage' alt=""></image> -->
									</view>
									<view class="goodsText">
									  <view class="price text-overflow" :style="{'background': 'linear-gradient(90deg, '+propsobj.bgColor1+','+ propsobj.bgColor2+')'}">
										<image mode='widthFix' v-if="propsobj.goodsType!=1" :src="imgUrl+'points.png'" alt=""></image>
										<text class="text span">{{item.needIntegral}}</text><text class="text" v-if="propsobj.goodsType==1">积分</text>
										<text class="text span" v-if="item.commonPrice!=''">+{{currencySymbol()}}{{getRatePrice(item.commonPrice)}}</text>
									  </view>
									  <text class='name text-overflow' v-if="propsobj.goodsType!=1">{{item.commonName}}</text>
									</view>
								</view>
						  </view>
						</view>
					</scroll-view>
				</view>
			  </view>
			</view>
		</view>
	</view>
</template>

<script>
	import {checkToken} from '@/utils/util.js'
	import {
		queryIndexIntegralGoods,
	} from "@/api/pages/index"
	export default {
		name: 'moduleIntegral',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				imgUrl:this.publicVariable.imgUrl,
				token:uni.getStorageSync('token'),
				params:{
					num:0
				},
				goodsLists:[],
				titleBg:''
			}
		},
		mounted() {
			this.getCouponList();
			this.titleBg=this.imgUrl+'integralBg.png'
			if(this.propsobj.bgType==1){
				this.propsobj.bgColor1=this.propsobj.titleBgColor1=this.publicVariable.themeData.contMxColor;
				this.propsobj.bgColor2=this.propsobj.titleBgColor2=this.publicVariable.themeData.contFsColor;
			}
		},
		methods: {
			titStyle1() {
			   return {
			     background: `linear-gradient(90deg, ${this.propsobj.titleBgColor1}, ${this.propsobj.titleBgColor2})`
			   }
			 },
			 titStyle2() {
			   return {
			     backgroundImage: `url(${this.propsobj.titleBg || this.titleBg})`,
			     backgroundRepeat: 'no-repeat',
			     backgroundPosition: 'center',
			     backgroundSize: 'cover'
			   }
			 },
			getCouponList(){
				this.params.num=this.propsobj.goodsNum;
				queryIndexIntegralGoods(this.params).then(res=>{
					if(res.code==1){
						this.goodsLists=res.data.list;
					}
				})
			},
			handleDetail(id){
				checkToken().then(async (vdata) => {
					if (vdata) {
						uni.navigateTo({
							url:'/integral/integral_product_detail/integral_product_detail?id='+id
						})
					}
				})
			},
			handleMore(){
				checkToken().then(async (vdata) => {
					if (vdata) {
						uni.navigateTo({
							url:'/integral/index/index'
						})
					}
				})
			}
			
		}
	}
</script>

<style lang="scss">
	.moduleRadius.radius{
	  border-radius:12rpx;
	  overflow:hidden;
	}
	.integral1{
	  .right{
	    .text,.iconfont{
	      font-size:24rpx;
	      color:#999;
	    }
	  }
	  .goodsStyle2,.goodsStyle3{
	    .name{
	      // color:#666 !important;
	    }
	    .price{
	      .span{
	        // color:#e93323 !important;
	        font-weight:bold;
	      }
	    }
	  }
	}
	.integral2{
		.h4{
			.tit{
				color:#fff;
			}
		}
	  .right{
	    .text,.iconfont{
	      font-size:24rpx;
	      color:#fff;
	    }
	  }
	}
	
	.integralBox{
	  background:#fff;
	  .h4{
	    padding:0 10px;
	    display:flex;
	    justify-content:space-between;
	    font-size: 32rpx;
	    font-weight: 500;
	    height:96rpx;
	    line-height:96rpx;
	    .tit{
	      font-weight: bold;
	      color: #333;
	      font-size: 32rpx;
	    }
	    .img{
	      height:32rpx;
	    }
	
	  }
	  .integralGoodsList{
	    padding:0 0 20rpx 20rpx;
	    background: #C92A2A;
		margin-top:-1px;
	    .ul{
	      display: flex;
		  flex-wrap: nowrap;
	     .li{
	        margin-right: 20rpx;
	        margin-bottom:20rpx;
			.liItem{
				 width: 224rpx;
			}
	        .img-size{
	          display:inline-block;
	          width: 100%;
			  height:224rpx;
	          border-radius:12rpx;
	          overflow:hidden;
	        }
	        .goodsText{
	          display:flex;
	          flex-direction:column;
	        }
	
	      }
	    }
	  }
	}
	.goodsStyle1{
		background:#fff;
		padding:20rpx;
		border-radius:16rpx 0 0 16rpx;
	  .ul{
	    .li{
	      .price{
	        width: 100%;
	        height: 36rpx;
	        background: #C92A2A;
	        background: #C92A2A;
	        border-radius: 1px 20rpx 20rpx 20rpx;
	        text-align: center;
	        line-height: 36rpx;
	        color: #fff;
	        font-size: 22rpx;
	        margin-top: 16rpx;
	      }
	    }
	  }
	}
	.price{
	  image{
	    width:32rpx;
	    margin-right:8rpx;
		vertical-align:middle;
	  }
	}
	.goodsStyle2,.goodsStyle3{
	  .ul{
	    padding:0 !important;
	    .li{
	      .goodsText{
	        padding:12rpx 0 0;
	        text{
	          margin-bottom:12rpx;
			  background:none !important;
	        }
	        .price{
				background:none !important;
	          .span{
	            color:#fff;
	          }
	        }
	        .name{
	          color:#fff;
	        }
	      }
	
	    }
	  }
	
	}
	.goodsStyle3{
	  .ul{
	    display:block !important;
	    .li{
	      display:inline-block;
	      vertical-align:top;
	      width:47.4% !important;
		  .liItem{
			  width:100% !important;
			  .img-size{
				  height:320rpx;
			  }
		  }
	      &:nth-child(even){
	        margin-right:0 !important;
	      }
	      .goodsText{
	        flex-direction: column-reverse !important;
	      }
	    }
	  }
	}
</style>
