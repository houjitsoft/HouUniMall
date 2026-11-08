<template>
	<view>
		<view class="modulePlace" :style="{backgroundColor:propsobj.isBackground?propsobj.bgColor:''}">
			<view v-if="propsobj.style=='1'" :style="{height:propsobj.height+'rpx'}">
			</view>
			<view class="lineModule" v-else-if="propsobj.style=='2'" :style="{height:propsobj.height+'rpx'}">
			  <view class="placeLine" v-if="propsobj.lineType==1" :style="{backgroundColor:propsobj.lineColor}"></view>
			  <view class="placeLine line2" v-else-if="propsobj.lineType==2" :style="{backgroundImage:'linear-gradient(to right, '+propsobj.lineColor+' 0%,'+propsobj.lineColor+' 50%, transparent 50%)'}"></view>
			  <view class="placeLine line3 " v-else-if="propsobj.lineType==3" :style="{backgroundImage:'linear-gradient(to right, '+propsobj.lineColor+' 0%,'+propsobj.lineColor+' 50%, transparent 50%)'}"></view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'modulePlace',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				
			}
		},
		methods: {
			
		}
	}
</script>

<style lang="scss">
	.lineModule{
	  display: flex;
	  flex-direction: column;
	  justify-content: center;
	  align-items:center;
	}
	.placeLine{
	  width:95%;
	  height:1px;
	}
	.line2{
	  background-size: 12px 1px;
	}
	.line3{
	  background-size: 6px 2px;
	}
</style>
