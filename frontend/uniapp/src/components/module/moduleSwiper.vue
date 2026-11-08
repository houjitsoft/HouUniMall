<template>
	<view>
		<swiper class="moduleSwiper" :style="{height:swiperHeight,paddingTop:propsobj.padding,paddingBottom:propsobj.padding,paddingLeft:propsobj.pageMargin,paddingRight:propsobj.pageMargin}" :indicator-dots="true" :indicator-active-color="themeData.contMxColor" indicator-color="#fff" :autoplay="true" :interval="3000" :duration="1000" :circular="true">
			<swiper-item v-for="(item,index) in currentList" :key="index">
				<view @click="goInside(item.link,item.linkSource)">
					<image mode="widthFix" @load="fixSwiperHeight" class="wp100" :src="item.imgUrl" ref="proImg"></image>
				</view>
			</swiper-item>
		</swiper>
	</view>
</template>

<script>
	let app = getApp({allowDefault: true});
	import {
	  calculateCurrentImage, 
	  startAutoSwitch
	} from '@/utils/periodicUtils';
	export default {
		name: 'moduleSwiper',
		props: ['propsobj'],
		computed:{
			// 当前显示的图片组
			currentList() {
				if (!this.propsobj.items.length) return '';
				if(this.propsobj.items.length>0){
					this.propsobj.items.forEach((item)=>{
						  item.imgUrl=item.imgUrlList[this.currentImageIndex];
					})
				}
				return this.propsobj.items;
			}
		},
		data() {
			return {
				swiperHeight:'',
				themeData:this.publicVariable.themeData,
				swiperMaxHeight:0,
				timer: null,
				currentImageIndex:0,
			}
		},
		mounted(){
			if(this.propsobj.imageEnabled){
				this.updateCurrentImage();
				this.startTimerImg();
			}
		},
		methods: {
			//切换图片
			updateCurrentImage() {
			  const result = calculateCurrentImage(this.propsobj.items[0].imgUrlList, this.propsobj.imageDays,this.propsobj.startDate);
			  this.currentImageIndex = result.index;
			  if(this.propsobj.items.length>0){
				  this.propsobj.items.forEach((item)=>{
					  item.imgUrl=item.imgUrlList[this.currentImageIndex];
				  })
			  }
			},
			startTimerImg() {
			  // 清理之前的定时器
			  if (this.timer) {
				this.timer.clear();
			  }
			  // 启动新的定时器
			  this.timer = startAutoSwitch(this.propsobj.imageDays, () => {
				this.updateCurrentImage();
			  }, this.propsobj.startDate);
			},
			fixSwiperHeight(event){
				
				let that = this
				var imgwidth = event.detail.width,
				imgheight = event.detail.height,
				  //宽高比  
				ratio = imgwidth / imgheight;
				var imgheight = 750 / ratio+'rpx';
				var curentheight=750 / ratio;
				if(curentheight>that.swiperMaxHeight){
					that.swiperMaxHeight=curentheight;
					that.swiperHeight=imgheight;
				}
				
			},
			goInside(link,linkSource){
				if(!link){return;}
				if(linkSource=='2'){//外部 
					uni.navigateTo({
						url:'../../general/webview/webview?url='+link
					})
				}else{//系统内部
					uni.navigateTo({
						url:link
					})
					
				}
			}
		}
	}
</script>
<style lang="scss">
	.moduleSwiper{	
		.swiper-item{
			height:100%;
		}
	}
</style>
