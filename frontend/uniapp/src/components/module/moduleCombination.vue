<template>
	<view class="combination-container">
	  <swiper ref="secondSwiper" class="bg-img" :autoplay="secondAutoplay" :interval="3000" :duration="300" :circular="true" :current="secondCurrent">
	  	<swiper-item v-for="(item,index) in currentList" :key="index">
	  		<image class="wp100 hp100" :src="item.img" ref="proImg"></image>
	  	</swiper-item>
	  </swiper>
	  <view class='bg-gradient'>
		  
	  </view>
	  <view class="com-head-main 111" :class="[isFixed?'scrolled':'']" :style="{paddingTop:top}">
		  <view class="search-head">
		    <image mode="widthFix" v-if="propsobj.logo" :src="propsobj.logo" alt=""></image>
		    <view class="box flex">
				<view class="flex flex1"  @click='goSearch' v-if="propsobj.hotItems.length>0">
					<text class="iconfont icon-sousuo1 mr20"></text>
					<swiper class="comIndexMenus flex1" :style="{height:'62rpx'}" :autoplay="true" :interval="getInterval" :duration="300" :circular="true" :vertical='true'>
						<swiper-item v-for="(item,index) in propsobj.hotItems" :key="index">
							<text>{{item.text}}</text>
						</swiper-item>
					</swiper>
				</view>
                    <!-- 扫码登录 -->
					<up-icon v-if="publicVariable.isShowImgSearch" name="scan" color="#fff" size="23" top="-2rpx" @click="handleScan"></up-icon>
		  			<!-- 上传图搜索 -->
					<up-icon v-if="publicVariable.isShowImgSearch" name="camera" color="#fff" size="23" top="-2rpx" @click="handleImgSearch"></up-icon>
		  		</view>
				<!-- #ifdef H5 ||APP -->
					<icon class="iconfont icon-pinglun ml10" @click="goNews"></icon>
				<!-- #endif -->
		  </view>
		  <scroll-view scroll-x="true" class="menus" v-if="propsobj.classItems.length>0" :show-scrollbar="false">
		  		  <text :class="active==0?'active':''" @click="goLink('',-1,2)">{{getTransNew('推荐')}}</text>
		  		  <text v-for="(item,index) in propsobj.classItems" @click="goLink(item.link,index,2)" :class="active==(index+1)?'active':''" :key="index">{{getTransNew(item.title)}}</text>
		  </scroll-view>
	  </view>
	  <view class="combination-banner" :style="{marginTop:top2}">
		  <swiper class="moduleSwiper" @change="onSwiperChange" :style="{height:swiperHeight}" :indicator-dots="true" :indicator-active-color="webColor" indicator-color="#fff6" :autoplay="true" :interval="3000" :duration="300" :circular="true">
		  	<swiper-item v-for="(item,index) in currentList" :key="index">
		  		<image :class="[{ active: currentIndex === index }]" mode="widthFix" @load="fixSwiperHeight" @click="goLink(item.link)" class="wp100" :src="item.img" ref="proImg"></image>
		  	</swiper-item>
		  </swiper>
	  </view>
	</view>
</template>

<script>
	const app = getApp({allowDefault: true});
	import { checkToken } from '@/utils/util.js';
	import {
	  calculateCurrentImage, 
	  startAutoSwitch
	} from '@/utils/periodicUtils';
	export default {
		name: 'moduleCombination',
		props:{
			propsobj:{
				type:Object,
				default:()=>{}
			},
			isFixed:{
				type:Boolean,
				default:false
			},
			shopId:{
				type:Number,
				default:null
			}
		},
		computed:{
			getInterval(){
				var time=5000;
				if(this.propsobj.duration!=''){
					time=this.propsobj.duration*1000
				}
				return time
			},
			// 当前显示的图片组
			currentList() {
				if (!this.propsobj.swiperItems.length) return '';
				console.log(this.propsobj.swiperItems.length,'this.propsobj.swiperItems.length--')
				if(this.propsobj.swiperItems.length>0){
					this.propsobj.swiperItems.forEach((item)=>{
						  item.img=item.imgList[this.currentImageIndex];
					})
				}
				return this.propsobj.swiperItems;
			}
		},
		data() {
			return {
				top2:'200rpx',
				webColor:this.publicVariable.webColor,
				currentIndex:0,
				secondCurrent:0,
				secondAutoplay:true,
				swiperHeight:'',
				top: '44rpx',
				active:0,
				token:uni.getStorageSync('token'),
				timer: null,
				currentImageIndex:0,
				currentList:[]
			}
		},
		mounted() {
			this.top = uni.getSystemInfoSync().statusBarHeight+ 'px';
			this.top2 =uni.getSystemInfoSync().statusBarHeight+80+ 'px';
			this.currentList=this.propsobj.swiperItems;
			if(this.propsobj.imageEnabled){
				this.updateCurrentImage();
				this.startTimerImg();
			}
		},
		methods: {
			//切换图片
			 updateCurrentImage() {
			  const result = calculateCurrentImage(this.propsobj.swiperItems[0].imgList, this.propsobj.imageDays,this.propsobj.startDate);
			  this.currentImageIndex = result.index;
			  if(this.propsobj.swiperItems.length>0){
				  this.propsobj.swiperItems.forEach((item)=>{
					  item.img=item.imgList[this.currentImageIndex];
				  })
			  }
			  this.currentList=this.propsobj.swiperItems;
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
			handleImgSearch(){
				this.$emit('imgSearch')
			},
			onSwiperChange(event) {
				this.currentIndex= event.detail.current;
				this.secondCurrent = event.detail.current;
				this.secondAutoplay=false;
				 setTimeout(() => {
					this.secondAutoplay = true; // 重新启动第二个 Swiper 的自动播放
				}, 1000); // 可根据需求调整时间
				
			},
			fixSwiperHeight(event){
				
				let that = this
				var imgwidth = event.detail.width,
				imgheight = event.detail.height,
				  //宽高比  
				ratio = imgwidth / imgheight;
				var imgheight = 750 / ratio-10+'rpx';
				that.swiperHeight=imgheight;
			},
			goLink(link,index,type){
				if(!link){return;}
				if(type==2){//选项卡
					this.active=index+1;
				}
				uni.navigateTo({
					url:link
				})
			},
			goSearch(){
				if (this.shopId!=null&&this.shopId!='') {
					uni.navigateTo({
						url: '/store/shopsearch/shopsearch?shopId='+this.shopId
					})
				}else{
					uni.navigateTo({
						url: '/general/searchpage/searchpage'
					})
				}
			},
			goNews(){
				uni.navigateTo({
					url:'/general/news/news'
				})
			},
			handleScan(){
				checkToken().then(()=>{
					// 只允许通过相机扫码
					uni.scanCode({
						onlyFromCamera: true,
						success: function (res) {
							console.log('条码内容：' + res.result);
							uni.navigateTo({
								url: '/general/scanlogin/scanlogin?scancode=' + res.result 
							});
						}
					});
				})
			}
		}
	}
</script>

<style lang="scss">
	.bg-gradient{
		position: absolute;
		top: 0;
		width: 100%;
		height: 100%;
		z-index:0;
		background: var(--theme-color, #002FA7);
	}
 .combination-container{
    position: relative;
    // padding-bottom: 10px;
	overflow:hidden;
  }
  .bg-img{
    position: absolute;
    top: 0;
    width: 100%;
    height: 100%;
    filter: blur(0);
    overflow: hidden;
  }
  .bg-img image{
    width: 100%;
    height: 100%;
    transform: scale(1.2);
    filter: blur(30px);
  }
  .combination-banner{
	 margin-top: 200rpx;
    padding: 0 24rpx;
    width: 100%;
	
  }
  .combination-banner icon{
    font-size:30px;
    color:#bbbfc8;
  }.combination-banner image{
	  border-radius:10rpx;
	  transform: scale(.93);
  }
  .combination-banner .active{
	   transform: scale(1);
  }
  .search-head{
    display: flex;
    align-items: center;
	/* #ifdef MP-WEIXIN */
	width: 72%;
	 padding: 10rpx 10px 0;
	/* #endif */
    /* #ifndef MP-WEIXIN */
    width:100%;
	 padding: 10px 10px 0;
    /* #endif */
   
    cursor: pointer;
  }
  .search-head .box{
    flex: 1;
    height: 62rpx;
    line-height: 62rpx;
    color: #999;
    font-size: 26rpx;
    padding-left: 24rpx;
	padding-right:20rpx;
    background: rgba(0,0,0,.2);
    border-radius: 32rpx;
    position: relative;
  }
  .search-head image{
    margin-right:22rpx;
    width: 80rpx;
  }
  .search-head .box icon{
    font-size: 28rpx;
    color:#fff;
  }
  .search-head .box text{
	 font-size: 26rpx;
	 color:#fff; 
  }
  .menus{
    position: relative;
	padding: 0 20rpx 0 15rpx;
    width: 100%;
    cursor: pointer;
    overflow: hidden;
	white-space:nowrap;
  }
  .menus text{
	  display:inline-block;
    margin-right: 25rpx;
    position: relative;
    font-size: 28rpx;
    text-align: center;
    color: #fff;
    height: 66rpx;
    line-height:66rpx;
  }
  .menus text.active{
	  font-weight:bold;
	  font-size:30rpx;
  }
  .menus text.active:after{
    content:'';
    position:absolute;
    left:50%;
    bottom: 0rpx;
    width: 36rpx;
    height: 2px;
    transform: translateX(-50%);
    background: #fff;
  }
  ::v-deep {
	  .combination-banner{
		  .uni-swiper-dot,.wx-swiper-dot{
			  width:18rpx;
			  height:10rpx;
			  border-radius:10rpx;
			  margin-right:8rpx;
		  }
		  .uni-swiper-dot-active,.wx-swiper-dot-active{
		  	  width:36rpx;
		  }
	  } 
	  
  }
  .comIndexMenus{
	  height:60rpx;
	  flex:1;
  }
  .search-head .icon-pinglun{
	  font-size:54rpx;
	  color:#fff;
  }
	.com-head-main{
		left: 0;
		position: fixed;
		top: 0;
		width: 100%;
		z-index: 30;
	}
	.com-head-main.scrolled{
		background: #fff !important;
		.icon-pinglun{
			color:#333;
		}
		.menus text{
			color:#000;
		}
	}
</style>
