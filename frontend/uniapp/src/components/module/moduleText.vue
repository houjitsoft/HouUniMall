<template>
	<view class="moduleText" :style="{paddingTop:propsobj.padding,paddingBottom:propsobj.padding,background:propsobj.bgColor,textAlign:propsobj.align,color:propsobj.textColor,fontSize:propsobj.fzSize}">
		<text :style="{color:propsobj.textColor,fontSize:propsobj.fzSize}" v-if="propsobj.text">{{ getTransNew(propsobj.text)}}</text>
	</view>
</template>

<script>
	
	export default {
		
		name: 'moduleSwiper',
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
	.bgf{background:#fff;}
	.moduleText{
		padding-left:20rpx;
		padding-right:20rpx;
	}
</style>
