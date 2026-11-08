<template>
	<view class="waterfall-item" @tap="onTap">
		<up-lazy-load class="wp100" img-mode="widthFix" :image="params.commonImage" threshold="100" :error-img="defaultImg"></up-lazy-load>
		<view class="text-conts">
			<text class="goods-name title-overflow">{{ getTransNew(params.commonName)}}</text>
			<view class="goods-rel clearfix">
				<view class="iblock fl">
					<text class="symbol">{{currencySymbol()}}</text>
					<text class="rmb-det" v-if="params.commonPrice">{{getRatePrice(params.commonPrice)}}</text>
				</view>
				<view class="iblock fr sales" v-if="isShowSold">
					<text>{{ getTransNew('已售')}}</text>
					<text>{{params.commonSalenum}}</text>
				</view>
				
			</view>
		</view>
	</view>
</template>

<script>
	
	export default {
		
		name:"helang-waterfall",
		props:{
			params:{
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
			},
			isShowSold:{
				type:Boolean,
				default:true
			}
		},
		data() {
			return {
				defaultImg:this.publicVariable.defaultImg
			};
		},
		methods:{
			
			// 发出组件高度信息，在此处可以区分正确和错误的加载，给予错误的提示图片
			emitHeight(e){
				const query = uni.createSelectorQuery().in(this);
				query.select('.waterfall-item').boundingClientRect(data => {
					let height = Math.floor(data.height);
					this.$emit("height",height,this.$props.tag);
				}).exec();
			},
			onTap(){
				this.$emit("click",this.$props.index,this.$props.tag);
			}
		}
	}
</script>

<style lang="scss" scoped>
.waterfall-item{
	display:inline-block;
	font-size: 28rpx;
	color: #666;
	margin: 10rpx 0 0;
	background:#fff;
	border-radius:15rpx;
	overflow:hidden;
	width:100%;
	image{
		display:inline-block;
		width:100%;
	}
	.text-conts{
		padding:20rpx;
	}
	.goods-name{
		margin-bottom:20rpx;
		font-size:26rpx;
		color:#000000;
		line-height:36rpx;
	}
	.goods-rel{
		.rmb,.rmb-det{
			font-weight:bold;
			// color:#FC3233;
			color: var(--webColor);
		}
		.rmb{
			font-size:24rpx;
		}
		.rmb-det{
			font-size:30rpx;
		}
		.sales text{
			font-size:24rpx;
			color:#aaa;
		}
	}
	
}


</style>
