<template>
  <view class="moduleHotArea" :style="{marginLeft: pageMargin + 'px', marginRight: pageMargin + 'px'}">
  	<view>
		<view class="li" v-for="(item,index) in propsobj.items" :key="index">
			<view class="relative fz0" :style="{marginBottom: padBom + 'px'}">
				<image mode="widthFix" class="wp100 ee"  :src="item.img"></image>
				<template v-if="item.hotLists">
					<view
						class="hotItems"
						v-for="(item2,index2) in item.hotLists"
						:key="index2"
						:style="getHotStyle(item2)"
						@click="goDetail(item2)"
					></view>
				</template>
				<view v-if="item.tipsText" class="textTips"><text>{{ getTransNew(item.tipsText)}}</text></view>
			</view>
		</view>
	</view>
  </view>
</template>

<script>
	
  export default {
	  
    name:'moduleHotArea',
    props: ['propsobj', 'index', 'idx'],
    data() {
      return {
      }
    },
	computed: {
		pageMargin() {
			return parseFloat(this.propsobj?.pageMargin) || 0
		},
		padBom() {
			return parseFloat(this.propsobj?.padBom) || 0
		},
	},
    methods:{
		getWidth(oldWidth){
			const systemInfo = uni.getSystemInfoSync();
			const screenWidth = systemInfo.windowWidth;
			const pageWidth = screenWidth - this.pageMargin * 2;
			const value = Number(oldWidth) || 0;
			return value / 375 * pageWidth;
		},
		getHotStyle(item) {
			return {
				width: this.getWidth(item.width) + 'px',
				height: this.getWidth(item.height) + 'px',
				left: this.getWidth(item.x) + 'px',
				top: this.getWidth(item.y) + 'px',
			}
		},
		goDetail(item) {
			if(item.link ==''){
				return false;
			}
			uni.navigateTo({
				url:item.link
			})
			
		}
    },
  }
</script>

<style>
	.fz0{
		font-size:0;
	}
	.moduleHotArea .li{
		position: relative;
	}
	.hotItems{
		display:inline-block;
		position:absolute;
		z-index:3;
	}
	.textTips{
		position:absolute;
		bottom:0;
		z-index:2;
		width:100%;
		left:0;
		padding:6px 10px;
		background:rgba(0,0,0,.5);
		text-align:center;
	}
	.textTips text{
	  font-size:26rpx;
	  color:#fff;
	}
</style>
