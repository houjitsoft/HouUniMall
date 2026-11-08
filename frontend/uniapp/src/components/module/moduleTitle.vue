<template>
	<view class="moduleTitle-wrap" :style="{background: propsobj.bgColor, marginTop: propsobj.padTop, marginBottom: propsobj.padBom}">
		<navigator :url="propsobj.titleLink" class="moduleTitle clearfix"
			:style="{textAlign: propsobj.align, fontSize: propsobj.fzSize}">
			<text class="moduleTitle__main" :style="{color: propsobj.textColor}">{{ getTransNew(propsobj.titleText)}}</text>
			<text v-if="propsobj.subtitle" class="moduleTitle__sub"
				:style="{color: propsobj.subtitleColor, fontSize: propsobj.subtitleSize}">{{ getTransNew(propsobj.subtitle)}}</text>
			<icon :style="{color: propsobj.iconColor}" class="iconfont icon-arrow-right fr" v-if="propsobj.icon!='无'"></icon>
		</navigator>
		<!-- 后台标题栏 style=1 的「更多」：老版本没下发也没渲染，这里补上 -->
		<navigator v-if="propsobj.rightText" :url="propsobj.titleLink" class="moduleTitle__more"
			:style="{color: propsobj.rightColor}">
			{{ getTransNew(propsobj.rightText)}}
		</navigator>
	</view>
</template>

<script>
	
	export default {
		name: 'moduleTitle',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				
				token:uni.getStorageSync('token'),
			}
		},
		methods: {
			
		}
	}
</script>

<style lang="scss">
	.moduleTitle-wrap{
		position: relative;
	}
	.moduleTitle{
		padding:0 20rpx;
		line-height:80rpx;
		iconfont{
			font-size:28rpx;
		}
	}
	.moduleTitle__sub{
		margin-left:12rpx;
	}
	.moduleTitle__more{
		position:absolute;
		right:20rpx;
		top:0;
		line-height:80rpx;
		font-size:26rpx;
	}
</style>
