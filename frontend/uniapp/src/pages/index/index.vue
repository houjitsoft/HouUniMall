<template>
	<view :style="temp">
		<view v-if="status==1">
			<!--
				tea 版式：首页是固定编辑式版式（对齐参考站），与装修组件的货架版式结构不同，
				故整页交给 TeaHome 渲染；标品首页仍走 homeIndex 装修渲染。
			-->
			<tea-home v-if="isTeaLayout" />
			<homeIndex v-else ref="homeIndex" :isFixed="isFixed" :previewTpl="previewTpl" />
		</view>
		<view v-else class="default-nodata bgf">
			<view class="table-cell">
				<image :src="url+'/image/icon/nodata.png'"></image>
				<text>{{reason}}</text>
			</view>
		</view>
		<foo-bar/>
		<BaseTop :scrollTop="scrollTop" text="顶部" />
		<!-- 🔴 前台 AI 搜索对匿名访客开放：首页常驻入口，点击直达，无需登录 -->
		<view @click="goAiSearch"
			style="position:fixed;right:24rpx;bottom:220rpx;z-index:999;display:flex;align-items:center;justify-content:center;width:96rpx;height:96rpx;border-radius:50%;background:#3b6cff;color:#fff;font-size:24rpx;line-height:1.1;text-align:center;box-shadow:0 6rpx 16rpx rgba(0,0,0,.25);">
			AI<br/>搜索
		</view>
	</view>
	
</template>
<script>
	let app = getApp({allowDefault: true});
	import homeIndex from '@/components/homeIndex/homeIndex.vue'
	import teaHome from '@/components/teaHome/teaHome.vue'
	import fooBar from '@/components/fooBar/fooBar.vue'
	import BaseTop from '@/components/BaseTop/BaseTop.vue'
	// tea 版式判定：站点风格只认 mall_style 缓存（与内页同一口径）
	import { isTeaSkin, loadSiteSkin } from '@/utils/site-skin.js'
	import {
		quertNavLogo
	} from "@/api/pages/index"
	import {
		queryGetuser,
		queryBusinessSwitch,
		queryBottomNavigationIndex
	} from "@/api/pages/index"
	export default {
		components: {
			homeIndex,
			teaHome,
			fooBar,
			BaseTop
		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				isFixed:false,
				// tea 版式：整页换成编辑式首页（结构不同，不能只换配色）
				isTeaLayout: isTeaSkin(),
				// 后台「模板选择」卡片预览：/h5/#/pages/index/index?tpl=code
				previewTpl:'',
				diyId:0,
				index: 0,
				status: 1,
				reciveData: {},
				foo: false,
				staHeight: 0,
				params: {
					configKey: "h5_faXian_off",
					configType: "",
				},
				isOpenFind: true,
				options: {},
				responseData: '',
				pageLoaded: false,
				scrollTop: 0
			}

		},
		onLoad(options){
			if (options && options.tpl) this.previewTpl = String(options.tpl)
			// 分销推广归因，兼容三种入口：
			//  ① H5 / 小程序原生分享链接：query.shareUserId
			//  ② 微信小程序码：options.scene。1 代 createVkShareQrCode 生成的 scene 是
			//     "shareUserId=xxx"、page 指向本页；此前本页只认 ①，且 App.vue 只解析
			//     options.query.q，导致扫该小程序码进商城首页直接丢掉分享人。
			//  ③ 普通二维码带 ?q= 的入口由 App.vue 解析，这里不重复处理。
			let shareUserId = options && options.shareUserId ? String(options.shareUserId) : ''
			if (!shareUserId && options && options.scene) {
				try {
					const pairs = decodeURIComponent(options.scene).split('&')
					for (let i = 0; i < pairs.length; i++) {
						const idx = pairs[i].indexOf('=')
						if (idx > 0 && pairs[i].slice(0, idx) === 'shareUserId') {
							shareUserId = pairs[i].slice(idx + 1)
							break
						}
					}
				} catch (e) {
					// scene 非法编码时忽略，不影响首页其它逻辑
				}
			}
			// 后续加购、下单由 sureorder 读取 storage，否则只在内存里，进商品页就丢了。
			if (shareUserId) {
				uni.setStorageSync('shareUserId', shareUserId)
				this.publicVariable.shareUserId = shareUserId
			}
		},
		onPageScroll(e){
			this.scrollTop = e.scrollTop;
			if (this.$refs.homeIndex) {
				this.$refs.homeIndex.scrollChange(e);
			}
			this.isFixed = e.scrollTop > 30;
		},
		onShareAppMessage: function() {

		},
		/**
		 * 分享朋友圈
		 */
		// async onReady() {
		// 	await this.$onLaunched
		// },
		// onShareTimeline: function() {

		// },
		onReady(){
			// #ifndef MP-WEIXIN
			uni.hideTabBar();
			// #endif
		},
		onShow(options) {
			this.vkJumpTo(options);
			this.init();
			// 版式：先同步读缓存定版（避免标品首页先闪一帧），再异步补一次请求
			this.applySkin();
			loadSiteSkin().then(() => this.applySkin()).catch(() => {});
			if(this.$refs.homeIndex){
				this.$refs.homeIndex.reachBottomFun(1);
				this.$refs.homeIndex.init();
			}
			
			
		},
		methods: {
			/** 站点风格变化时切换首页版式 */
			applySkin() {
				const tea = isTeaSkin();
				if (tea !== this.isTeaLayout) this.isTeaLayout = tea;
			},
			/** 前台 AI 搜索对匿名访客开放，直接进，不强制登录 */
			goAiSearch() {
				uni.navigateTo({ url: '/aisearch/index/index' });
			},
			
			async init() {
				const resFind = await quertNavLogo(this.params).then(res => {
					// 配置项未初始化时接口会给 data: null，取 [0] 会抛 TypeError（首屏静默报错）
					if (res.code == 1 && res.data && res.data[0]) {
						this.isOpenFind = res.data[0].configValue;
						return res.code;
					}
					return 0;
				})
				return resFind;
			},
			toIndex(index) {
				this.index = index;
				if (index == 4) {
					uni.pageScrollTo({
						scrollTop: 0,
						duration: 10
					});
				}
				if(index==5){
					this.index = 0;
					this.$nextTick(() => {
						this.index =5;
					})
					this.diyId = uni.getStorageSync('navDiyId');
				}

			},
			// 扫描二维码进行跳转操作
			async vkJumpTo(shareUserId) {
				if (shareUserId) {
					const {
						code,
						data
					} = await queryGetuser();
					if (code != 1) {
						uni.setStorageSync('shareUserId', shareUserId);
						queryBusinessSwitch().then(res => {
							if (res.code == 1) {
								uni.navigateTo({
									url: "/general/reguster_enterprise/reguster_enterprise"
								})
							} else {
								uni.navigateTo({
									url: "/general/registrationPage/registrationPage"
								})
							}
						})
					}
				}

			},
			// onReachBottom() {
				
			// 	// if (this.index == 1) {
			// 	// 	this.$refs.index.reachBottom();
			// 	// }else if(this.index ==2){
			// 	// 	this.$refs.faxian.reachBottom();
			// 	// }
			// },
		},
		onReachBottom(){
			if (this.$refs.homeIndex) this.$refs.homeIndex.reachBottomFun()
		}
	}
</script>
<style scoped>
	.default-nodata {
		height: 100vh;
	}
	.fade-enter-active, .fade-leave-active {
	  transition: opacity 0.3s;
	}
	.fade-enter, .fade-leave-to {
	  opacity: 0;
	}
</style>