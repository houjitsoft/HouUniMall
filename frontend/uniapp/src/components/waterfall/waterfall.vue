<template>
	<view class="waterfall-item" @tap="onTap">
		<view class="img-cont">
			<icon v-if="params.articleType==2" class="iconfont icon-bofang"></icon>
			<up-lazy-load class="wp100" img-mode="widthFix" :image="params.articleCover" threshold="100" :error-img="defaultImg"></up-lazy-load>
			<!-- <image :src="params.articleCover" mode="widthFix" @load="emitHeight" @error="emitHeight"></image> -->
			<view v-if="params.articleContent" class="article-tit title-overflow">{{ getTransNew(params.articleContent)}}</view>
			<view v-else class="article-tit title-overflow">{{ getTransNew(params.articleTitle)}}</view>
		</view>
		<view class="content clearfix">
			<view class="fl wp80">
				<view class="user-head" :style="{backgroundImage:'url('+params.userAvatar+')',backgroundSize:'cover',backgroundRepeat:'no-repeat',backgroundPosition:'center'}"></view>
				<view class="iblock user-tit">
					<text class="user-name text-overflow">{{ getTransNew(params.userName)}}</text>
				</view>
			</view>
			<view class="fr user-right-views">
				<icon class="iconfont icon-views"></icon><text class="views-nums" v-if="params.readNum">{{ getTransNew(params.readNum)}}</text>
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
</style>
