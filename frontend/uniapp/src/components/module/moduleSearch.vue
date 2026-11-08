<template>
	<view class="moduleSearch tc" @tap="toListFn">
		<view class="searchModule" :style="{marginTop:propsobj.padTop,marginBottom:propsobj.padBom}">
		  <view class="search-boxs flex1" :style="{borderColor:propsobj.borderColor,borderRadius:propsobj.radius+'px',backgroundColor:propsobj.inputBgColor,backgroundImage:'url('+propsobj.inputBg+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}">
		    <view class="search-input" :class="[propsobj.style=='1'?'style1':'style2',propsobj.textPosition=='1'?'tl':'tc',propsobj.borderStyle=='1'?'bdradius':'']">
		      <text class="iblock iconfont icon-sousuo align-middle" :style="{color:propsobj.inputColor}"></text>
		      <text class="text align-top" :style="{color:propsobj.inputColor}">{{propsobj.text?propsobj.text:placeText}}</text>
		      <image class="ml10 align-middle" mode="widthFix"  v-if="propsobj.inputImg!=''" :src="propsobj.inputImg" alt="" :style="{width:propsobj.inputImgW+'px',height:propsobj.inputImgH+'px'}"></image>
			  <view class="img-search-btn flex fr iblock" v-if="publicVariable.isShowImgSearch">
				  <view class="" @click.stop="handleScan">
					<up-icon name="scan" :color="propsobj.inputColor" size="23" top="5"></up-icon>
				  </view>
				<up-icon  name="camera" :color="propsobj.inputColor" size="25" top='5'></up-icon>
			  </view>
			</view>
		  </view>
		  <image mode="widthFix" v-if="propsobj.rightImg!=''" :src="propsobj.rightImg" alt="" :style="{width:propsobj.rightImgW+'px',height:propsobj.rightImgH+'px'}"></image>
		</view>
	</view>
</template>

<script>
	const app = getApp({allowDefault: true});
	
	import { checkToken } from '@/utils/util.js';
	export default {
		
		name: 'moduleSearch',
		props: ['propsobj','shopId'],
		async mounted() {
			if(this.propsobj.text==''){
				this.propsobj.text=await this.getTransNew('请输入你想要的商品')
			}
		},
		data() {
			return {
				placeText:'请输入你想要的商品',
				token:uni.getStorageSync('token'),
			}
		},
		methods: {
			// 头部跳转函数
			toListFn: function() {
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
	.mt4{
		margin-top:4rpx;
	}
	.searchModule{
	  padding:0 20rpx;
	  align-items: center;
	  display: flex;
	  justify-content: space-between;
	  position: relative;
	  height: 80rpx !important;
	  min-height: 80rpx !important;
	  .search-boxs{
	    padding:0 20rpx;
	    height:72rpx;
	    line-height:70rpx;
	    border:2rpx solid transparent;
	  }
	  // .search-input .iconfont{
	  //   vertical-align:middle;
	  // }
	}
</style>
