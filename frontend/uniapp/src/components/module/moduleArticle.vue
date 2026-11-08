<template>
	<view class="moduleArticle bgf">
		<view class='info-lists' v-if="propsobj.items.length>0">
			<view class="li"  v-for="(item,index) in propsobj.items" :key="index" @click="goDetails(item.informationId)">
				<text class="title">{{getTransNew(item.informationTitle,2)}}</text>
				<view v-if="item.informationCoverList">
					<view  :class="[item.informationCoverList.length==1?'style1':(item.informationCoverList.length==2?'style2':'style3')]">
						<view v-if="item.informationCoverList.length==1">
							<view class="img-box" v-for="(item2,index2) in item.informationCoverList" :key="index2">
								<image class="wp100" mode="widthFix" :src="item2"></image>
							</view>
						</view>
						<view class="flex-box" v-else>
							<view class="img-box" v-for="(item2,index2) in item.informationCoverList" :key="index2" :style="{backgroundImage:'url('+item2+')',backgroundSize:'cover',backgroundRepeat:'no-repeat',backgroundPosition:'center'}">
							</view>
						</view>
					</view>
				</view>
				<view class="bottom clearfix">
					<view class="fl">
						<text class="icon iconfont icon-views"></text>
						<text class="text">{{item.pageView}}</text>
					</view>
					<view class="fr">
						<text class="time">{{item.releaseTime}}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'moduleArticle',
		props: ['propsobj'],
		data() {
			return {
			}
		},
		methods: {
			goDetails(id){
				uni.navigateTo({
				  url: '/activities/informationDetails/informationDetails?id='+id
				});
			},
		}
	}
</script>

<style lang="scss">
	.info-lists .li{
		padding:28rpx 24rpx 22rpx;
		border-bottom:1px solid #F6F6F6;
	}
	.info-lists .li .title{
		display:block;
		width:100%;
		font-size:27rpx;
		color:#4A4A4A;
		line-height: 38rpx;
	}
	.info-lists .li .img-box{
		margin-top:19rpx;
		border-radius:10rpx;
		overflow:hidden;
		font-size:0;
	}
	.info-lists .li .style1 .img-box{
		width:100%;
		background:#eee;
	}
	.info-lists .li .style2 .img-box{
		width:48%;
		height:240rpx;
		background:#eee;
		border-radius:10rpx;
	}
	.info-lists .li .flex{
		justify-content: space-between;
	}
	.info-lists .li .style3 .img-box{
		width:32%;
		height:190rpx;
		background:#eee;
		border-radius:10rpx;
	}
	.info-lists .li .bottom{
		padding-top:24rpx;
	}
	.info-lists .li .bottom .icon{
		margin-right:4px;
		color:#D1D1D1;
		vertical-align: top;
		line-height:38rpx;
	}
	.info-lists .li .bottom .text,.info-lists .li .bottom .time{
		color:#9b9b9b;
		font-size:24rpx;
		vertical-align:top;
		line-height:36rpx;
	}
	.flex-box{
		display:flex;
		justify-content: space-around;
	}
</style>
