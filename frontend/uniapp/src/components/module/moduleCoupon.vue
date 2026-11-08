<template>
	<view>
		<view class="moduleCoupon" v-if="couponLists.length>0" :style="{temp,paddingTop:propsobj.padTop,paddingBottom:propsobj.padBom,paddingLeft:propsobj.pageMargin,paddingRight:propsobj.pageMargin}" :class="[propsobj.style=='1'? 'img-style1':propsobj.style=='2'?'swiper-style2-div':'swiper-style3-div']">
			<scroll-view class="moduleBg" :class="`coupon${propsobj.style}`,[propsobj.isRadius==2?'radius':'']" scroll-x='true'>
				<view class="listUl ul">
				  <view class="li" v-for="(item,index) in couponLists" :key='item'>
				    <view class="itemLi">
				      <view class="money">
				        <view>
				          <text class="rmb">{{currencySymbol()}}</text>
				          <text class="strong">{{item.voucherTPrice}}</text>
				        </view>
				        <text class="fitText block text-overflow">满{{item.voucherTLimit}}可用</text>
				      </view>
				      <text class="sill block" @click="handleGetCoupon(item.voucherTId)">{{getTransNew('立即领取')}}</text>
				      <image class="img" mode='widthFix' :src="imgUrl+'place.png'" alt=""></image>
				    </view>
				  </view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
	import {checkToken} from '@/utils/util.js'
	import {
		queryLimitVoucherTempPort,
	} from "@/api/pages/index"
	import {
		queryInsertvcbase
	} from "@/api/activities/index"
	export default {
		name: 'moduleCoupon',
		props: ['propsobj','shopId'],
		watch: {
			
		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				imgUrl:this.publicVariable.imgUrl,
				params:{
					num:0,
					shopId:this.shopId
				},
				couponLists:[],
				getParams:{
					voucherTId:'',
					platForm:2
				}
			}
		},
		mounted() {
			this.getCouponList();
		},
		methods: {
			getCouponList(){
				this.params.num=this.propsobj.size;
				queryLimitVoucherTempPort(this.params).then(res=>{
					if(res.code==1){
						this.couponLists=res.data.list;
					}
				})
			},
			handleGetCoupon(id){
				checkToken().then(async (vdata) => {
					if (vdata) {
						this.getParams.voucherTId=id;
						queryInsertvcbase(this.getParams).then(async res => {
							if (res.code == 1) {
								uni.showToast({
									icon:'none',
									title:await this.getTransNew("成功领取")
								})
								this.getCouponList();
							} else {
								uni.showToast({
									icon: 'none',
									title:await this.getTransNew(res.message)
								})
							}
						})
					}
				})
			}
		}
	}
</script>

<style lang="scss">
	.moduleBg{
		padding:20rpx 24rpx;
		background:#fff;
		&.radius{
			border-radius:16rpx;
		}
	}
.coupon1{
    overflow:hidden;
    .listUl{
      display: inline-flex;
      flex-wrap: nowrap;
      padding-top:40rpx;
      .li{
        width: 165rpx;
        height: 155rpx;
        background: #f12a13;
        position: relative;
        border-radius: 12rpx 12rpx 30rpx 30rpx;
        margin-right: 12rpx;
        .money{
          width: 154rpx;
          height: 124rpx;
          background: #fff;
          border: 1px solid #fceae9;
          position: absolute;
          left: 6rpx;
          top: -36rpx;
          text-align: center;
          font-size: 40rpx;
          font-weight: 600;
          color: #f12a13;
          padding-top: 12rpx;
          border-radius: 12rpx 12rpx 0 0;
          .rmb{
            font-size: 24rpx;
          }
          .strong{
            font-size:40rpx;
            font-family: SemiBold;
          }
          .fitText{
            font-size: 20rpx;
            color: #999;
            font-weight: 400;
          }
        }
        .sill{
          position: absolute;
          bottom: 0;
          left: 0;
          width: 100%;
          height: 78rpx;
          color: #fff;
          line-height: 92rpx;
          font-size: 24rpx;
          text-align: center;
          border-radius: 0 0 20rpx 20rpx;
          background: var(--theme-color, #002FA7);
        }
        .img{
          position: absolute;
          left: 0;
          width: 156rpx;
          bottom: 64rpx;
        }
      }
    }
  }
  .coupon2{
    overflow:hidden;
    .listUl{
      display:flex;
      flex-wrap: nowrap;
      .li{
        border: 2rpx solid #e93323;
        border-radius: 16rpx;
        margin-top: 0;
        margin-right: 6px;
        background: #e93323;
        .itemLi{
          display:flex;
          flex-shrink: 0;
          width: 290rpx;
          height: 140rpx;
          border-radius: 14rpx;
          background: hsla(0, 0%, 100%, .9);
        }
        .money{
          flex: 1;
          position: relative;
          display:flex;
          flex-direction: column;
          justify-content: center;
          text-align: center;
          &::before{
            content: "";
            position: absolute;
            top: 0;
            right: 0;
            z-index: 2;
            width: 16rpx;
            height: 16rpx;
            border: 2rpx solid #e93323;
            border-radius: 50%;
            background: #ffffff;
            -webkit-transform: translate(50%, -50%);
            transform: translate(50%, -50%);
          }
          &::after{
            content: "";
            position: absolute;
            bottom: 0;
            right: 0;
            z-index: 2;
            width: 16rpx;
            height: 16rpx;
            border: 2rpx solid #e93323;
            border-radius: 50%;
            background: #ffffff;
            -webkit-transform: translate(50%, 50%);
            transform: translate(50%, 50%);
          }
          .rmb{
            font-size: 28rpx;
          }
          .strong{
            font-size:42rpx;
            font-family: SemiBold;
          }
          .rmb,.strong{
            color:#e93323;
          }
          .fitText{
            font-size: 22rpx;
            color:#e93323;
            font-weight: 400;
          }
        }
        .sill{
          position:relative;
            width: 58rpx;
             padding: 0 16rpx;
             font-size: 24rpx;
             color: #e93323;
             display: flex;
             flex-direction: column;
             justify-content: center;
			 writing-mode: vertical-lr;
			 align-items: center;
			 letter-spacing: 3rpx;
			 box-sizing:border-box;
             &::before{
               content: "";
               position: absolute;
               top: 12rpx;
               bottom: 12rpx;
               left: 0;
               border-left: 2rpx dashed #e93323;
             }
        }
        .img{
         display:none;
        }
      }
    }
  }
  .coupon3{
    overflow:hidden;
    .listUl{
      display:flex;
      flex-wrap: nowrap;
      .li{
         border-radius: 12rpx;
         margin-right: 12rpx;
         background: #C92A2A;
         .itemLi{
           display:flex;
           flex-shrink: 0;
           width: 250rpx;
           height:114rpx;
         }
        .money{
          display: flex;
          flex-direction: column;
          justify-content: center;
          flex: 1;
          border-radius: 12rpx;
          background: #FFFFFF;
          text-align:center;
          .rmb{
            font-size: 28rpx;
          }
          .strong{
            font-size:42rpx;
            font-family: SemiBold;
          }
          .rmb,.strong{
            color:#e93323;
          }
          .fitText{
            font-size: 22rpx;
            color:#e93323;
            font-weight: 400;
          }
        }
        .sill{
            width: 58rpx;
             padding: 0 16rpx;
             font-size: 22rpx;
             color: #fff;
             display: flex;
             flex-direction: column;
             justify-content: center;
			 writing-mode: vertical-lr;
			 align-items: center;
			 letter-spacing: 2rpx;
			 box-sizing:border-box;
        }
        .img{
         display:none;
        }
      }
    }
  }
</style>
