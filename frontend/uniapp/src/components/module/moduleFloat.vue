<template>
	<view>
		<view class="moduleFloat" :class="[propsobj.position=='left'?'left':'right']" :style="{paddingTop:propsobj.padTop,paddingBottom:propsobj.padBom}">
			<view class="floatImgMask" v-if="propsobj.style==2"></view>
			<view class="iblock floatImg" :style="{'backgroundImage':'url('+propsobj.img+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}" @click="goInside(propsobj.link,propsobj.style)">
			</view>
		</view>
	</view>
</template>

<script>
	import {checkToken} from '@/utils/util.js'
	import {mallRequest} from '@/utils/request.js';
	export default {
		name: 'moduleFloat',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				token:uni.getStorageSync('token'),
			}
		},
		methods: {
			goInside(link,linkSource){
				checkToken().then((vdata) => {
					if (vdata) {
						if(linkSource==2){//平台客服入口
							var customLink=mallRequest.MALL_IM+'wapChat';
							// #ifdef H5
                            this.$safeOpenUrl(customLink,{
                                isMall:1,
                                pageType:2,
                                token:uni.getStorageSync('token')
                            })
							// #endif
							// #ifndef H5
							uni.navigateTo({
								url: '/hybrid/im_web_view/im_web_view?url=' + encodeURIComponent(customLink)
							})
							// #endif
						}else{//页面链接
							if(!link){return;}
							uni.navigateTo({
								url:link
							})
							
						}
					}
				})
				
			}
		}
	}
</script>

<style lang="scss">
	@keyframes move-kefu{
		0%   {box-shadow: none;
	    transform: scale(.8);}
		50%  {box-shadow: none;
	    transform: scale(1.2);
	    opacity: 0;}
		100% { opacity: 0;
	    transform: scale(.7);}
	}
	.moduleFloat{
		position:fixed;
		bottom:260rpx;
		z-index:10;
		
		&.left{
			left:50rpx;
		}
		&.right{
			right:50rpx;
		}
		.floatImg{
			position:relative;
			z-index:2;
		}
		.floatImg,.floatImgMask{
			display:inline-block;
			width:120rpx;
			height:120rpx;
			overflow:hidden;
			border-radius:100%;
		}
		.floatImgMask{
			animation: move-kefu 2s linear 1.2s infinite;
			position:absolute;
			left:0;
			top:0;
			bottom:0;
		}
		
	}
	
	
</style>
