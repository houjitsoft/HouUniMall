<template>
	<view>
		<view class="moduleImg" :style="{background:propsobj.bgColor,paddingTop:propsobj.padTop,paddingBottom:propsobj.padBom,paddingLeft:propsobj.pageMargin,paddingRight:propsobj.pageMargin}">
			<view class="moduleRadius" :class="[propsobj.style=='1'? 'img-style1':propsobj.style=='2'?'swiper-style2-div':'swiper-style3-div',propsobj.isRadius==2?'radius':'']">
				<template v-if="propsobj.style=='1'||propsobj.style=='2'">
					<view class="item-li" v-for="(item,index) in propsobj.items" :key="index" @click="goInside(item.link,item.linkSource)">
						<up-lazy-load class="wp100" img-mode="widthFix" :image="item.imgUrl"></up-lazy-load>
					</view>
				</template>
				<template v-else>
					<scroll-view class="img-style3" scroll-x='true'>
						<view class="item-li" v-for="(item,index) in propsobj.items" :key="index" @click="goInside(item.link,item.linkSource)">
							<up-lazy-load class="wp100" img-mode="widthFix" :image="item.imgUrl"></up-lazy-load>
						</view>
					</scroll-view>
				</template>
			</view>
		</view>
	</view>
</template>

<script>
	import {
	    mallUrl
	} from "@/utils/request.js"
	export default {
		name: 'moduleImg',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				token:uni.getStorageSync('token'),
			}
		},
		
		methods: {
			navigateTo(link) {
			    uni.navigateTo({
			        url: link
			    })
			},
			goInside(link,linkSource){
				if(linkSource=='2'){//外部 
					uni.navigateTo({
						url:'../../general/webview/webview?url='+link
					})
				}else{//系统内部
					// #ifdef APP
					this.navigateTo(link);
					// #endif
					
					// #ifdef H5
					if (link === '/tuikit/livelist/index') {
					    this.navigateTo('/tuikit/webLiveList/webLiveList');
					} else {
					    this.navigateTo(link);
					}
					// #endif
					
					// #ifdef MP
					if (link === '/tuikit/livelist/index') {
					    if (uni.getStorageSync('token')) {
					        let customLink =
					            `${mallUrl}h5/#/tuikit/webLiveList/webLiveList?token=${uni.getStorageSync('token')}`
					        this.navigateTo(`/hybrid/web_view/web_view?url=${encodeURIComponent(customLink)}`);
					    } else {
					        this.navigateTo('/general/landingPage/landingPage')
					    }
					} else {
					    this.navigateTo(link);
					}
					// #endif
					
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.moduleRadius.radius{
		border-radius:16rpx;
		overflow:hidden;
	}
	.moduleImg{
		image{display:inline-block;vertical-align:top;}
		.img-style1{
			.item-li{
				width:100%;
			}
		}
		.swiper-style2-div{
			display:flex;
			.item-li{
				flex:1;
				margin-right:10rpx;
				&:last-child{
					margin-right:0;
				}
			}
		}
		.img-style3{
			font-size:0;
			white-space: nowrap;
			.item-li{
				display:inline-block;
				margin-right:20rpx;
				width:400rpx;
				vertical-align:top;
			}
		}
	}
	.item-li ::v-deep view{
		background-color:transparent !important;
	}
</style>
