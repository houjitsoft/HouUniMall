<template>
	<view
		:class="['bottom', isTeaLayout ? 'dc-tab-tea' : '', isTeaLayout && showIcon ? 'dc-tab-dual' : '']"
		:style="barStyle"
		v-if="isTabShow">
		<view class="nav-box" :style="barStyle" v-if="visibleNav.length>0">
			<view
				class="nav-tab"
				:class="{'is-active': activeIndex === index}"
				v-for="(item,index) in visibleNav"
				:key="index"
				@click="toIndex(item,index)">
				<!-- 图标：后台可配「显示图标」；tea 版式默认纯文字（照参考站），标品保持图标栏 -->
				<view class="nav-icon" v-if="showIcon">
					<image
						v-if="isImageIcon(item, index)"
						class="nav-img"
						:src="iconOf(item, index)"
						mode="aspectFit" />
					<icon
						v-else
						class="iconfont"
						:class="iconOf(item, index)"
						:style="{ color: colorOf(index) }"></icon>
				</view>
				<view class="nav-text" :style="{ color: colorOf(index) }">
					{{ getTransNew(item.navTitle) }}
				</view>
			</view>
		</view>
	</view>
</template>

<script>

	let app = getApp({allowDefault: true});
	import {
		queryBottomNavigationIndex
	} from "@/api/pages/index"
	import {
		querybottomNavigation
	} from "@/api/store/index"
	import {
		cachedSkin,
		loadSiteSkin
	} from "@/utils/site-skin.js"

	/**
	 * 底部导航配置（后台「装修管理 → 底部导航」可改，H5 立即生效）。
	 *
	 * page_data 结构（hou_base_modularizationd.page_is_index = 3）：
	 * {
	 *   color: '#B8976A',        // 选中色
	 *   defaultColor: '#8A8578', // 未选色
	 *   background: '#F7F5F0',   // 底色（留空跟主题）
	 *   showIcon: false,         // 是否显示图标（tea 版式默认 false = 纯文字）
	 *   items: [{ navTitle, navUrl, icon, selectedIcon, show, navIndex }]
	 * }
	 * icon 既可以是字体图标类名（icon-home-s1），也可以是图片地址（/static/... 或 http(s)://）。
	 */
	const TEA_SELECTED = '#b8976a'
	const TEA_DEFAULT = '#8a8578'

	export default {
		props: ['navIndex','isOpenFind','reciveInfo'],
		components: {},
		data() {
			return {
				isTabShow:false,
				// tea 版式判定：内页没有自己的装修记录，只认站点 mall_style（缓存同步读，异步补一次）
				isTeaLayout: cachedSkin().skin === 'tea_theme',
				home: 0,
				navList: [],
				selectColor:'',
				defaultColor:'',
				navBackground:'',
				// 是否显示图标：tea 默认纯文字，标品默认显示（后台显式配置则以后台为准）
				showIconConfig: null,
				currentTabIndex : 0,
				curUrl:'',
				footParams: {
					shopId: 0
				},
			}
		},
		computed: {
			/** 后台显式关掉的项不渲染 */
			visibleNav() {
				return (this.navList || []).filter(item => item && item.show !== false)
			},
			activeIndex() {
				const cur = this.curUrl || ''
				return this.visibleNav.findIndex(item => item.navUrl && cur && item.navUrl.includes(cur))
			},
			showIcon() {
				if (this.showIconConfig !== null) return !!this.showIconConfig
				return !this.isTeaLayout
			},
			barStyle() {
				return this.navBackground ? { background: this.navBackground } : {}
			}
		},
		onReady(){
			
		},
		async mounted() {
			await this.$onLaunched
			loadSiteSkin().then(skin => {
				this.isTeaLayout = skin && skin.skin === 'tea_theme';
			})
			/*
			 * 当前页路由：正常取 getCurrentPages() 栈顶。
			 * 直开内页（新窗口、分享链接、以及部分 switchTab 场景）时栈可能是空的，
			 * 此时挂不上 curUrl → 下面 some() 恒 false → 整条底栏不渲染、也不发请求。
			 * 故退回 location.hash 兜底，保证底栏在任何进入方式下都在。
			 */
			let cur = '';
			try {
				const stack = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
				const currentPage = stack.length ? stack[stack.length - 1] : null;
				if (currentPage && currentPage.route) cur = '/' + currentPage.route;
			} catch (e) { /* 取不到就交给下面的兜底 */ }
			if (!cur && typeof location !== 'undefined' && location.hash) {
				cur = '/' + location.hash.replace(/^#/, '').replace(/^\//, '').split('?')[0];
			}
			this.curUrl = cur;
			this.selectColor= this.publicVariable.themeData.contMxColor
			if(this.publicVariable.thirdPart.shopId){
				this.footParams.shopId=this.publicVariable.thirdPart.shopId;
				this.getShopFootNav();
			}else{
				this.getFootNav();
			}
			
		},
		methods: {
			/**
			 * 底栏显隐：默认只在导航项所在页面出现（内页不占屏）。
			 *
			 * <p>购物袋不在导航项里（菜单里没有它），但它是一级页 —— 从商品详情
			 * 「加入购物袋」进来后若底栏也没有，整页就没有回退入口了，故常显。
			 */
			resolveTabShow() {
				const always = ['/pages/cart/cart'];
				const cur = this.curUrl || '';
				if (cur && always.includes(cur)) return true;
				return this.visibleNav.some(item => item.navUrl && item.navUrl.includes(cur));
			},
			//查询平台底部导航
			getFootNav() {
				queryBottomNavigationIndex({}).then(res => {
					if (res.code == 1) {
						var resData = JSON.parse(res.data.pagedata);
						this.applyNavConfig(resData);
						this.isTabShow = this.resolveTabShow();
						this.$emit('isTabShow',this.isTabShow)
					}
				})
			},
			//查询品牌店铺底部导航
			getShopFootNav() {
				querybottomNavigation(this.footParams).then(res => {
					if (res.code == 1) {
						var resData = JSON.parse(res.data.pagedata);
						this.applyNavConfig(resData);
						this.isTabShow = this.resolveTabShow();
					}
				})
			},
			/** 把后台 page_data 落到渲染字段上，缺省值保证老配置照旧能用 */
			applyNavConfig(resData) {
				const cfg = resData || {}
				this.navList = cfg.items || []
				if (cfg.color) this.selectColor = cfg.color
				if (cfg.defaultColor) this.defaultColor = cfg.defaultColor
				this.navBackground = cfg.background || ''
				this.showIconConfig = typeof cfg.showIcon === 'boolean' ? cfg.showIcon : null
			},
			/** 选中项用 selectedIcon（配置了才换），其余用 icon */
			iconOf(item, index) {
				if (index === this.activeIndex && item.selectedIcon) return item.selectedIcon
				return item.icon || ''
			},
			isImageIcon(item, index) {
				const v = this.iconOf(item, index) || ''
				return /^(https?:)?\/\//i.test(v) || v.indexOf('/') === 0 || v.indexOf('data:') === 0
			},
			colorOf(index) {
				if (index === this.activeIndex) {
					return this.selectColor || (this.isTeaLayout ? TEA_SELECTED : '')
				}
				return this.defaultColor || (this.isTeaLayout ? TEA_DEFAULT : '')
			},
			toIndex(tab, index) {
				const url = tab && tab.navUrl
				if (!url) return
				if((this.publicVariable.thirdPart.shopId&&tab.navIndex==4)||tab.navIndex==5){
					app.globalData.diyId=tab.pageId;
				}
				// switchTab 只能跳 tabBar 里声明过的页；站长配了普通页时退回 reLaunch，别让点击无反应
				uni.switchTab({
					url,
					fail: () => {
						uni.reLaunch({ url, fail: () => uni.navigateTo({ url }) })
					}
				});
			}
		}
	}
</script>

<style scoped lang="scss">
	.bottom {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		width: 100%;
		height: 49px;
		box-shadow: none;
		/* 底色跟随后台主题（tea=暖象牙 #f4f1ea），无主题时退回白 */
		background: var(--theme-bg-color, #fff);
		border-top: 1px solid var(--theme-border-color, rgba(0, 0, 0, 0.06));
		z-index: 10;
	}

	.nav-tab {
		flex: 1;
		text-align: center;
		width: 100%;
		// height: 49px;
	}

	.nav-icon {
		// height: 24px;
		color:  #8A8378;
		box-sizing: border-box;
		padding: 4px 0 2px 0;
	}

	.nav-icon .img2 {
		display: none;
	}

	.nav-icon image {
		width: 25px;
	}

	.nav-img {
		width: 22px;
		height: 22px;
		display: inline-block;
	}

	.nav-tab-active .nav-icon .img1 {
		display: none;
	}

	.nav-tab-active .nav-icon .img2 {
		display: inline-block;
	}

	.nav-tab-active .nav-text {
		
	}

	.nav-text {
		font-size: 11px;
		padding-bottom:6px;
		color:  #8A8378;
		font-weight: 400;
	}

	/*
	 * 选中态跟随后台主色（--theme-color）。
	 * 选择器必须带 .bottom .nav-tab 前缀：全局有一条
	 * `uni-page-body .nav-text { color: var(--um-muted) }`，特异性比单个 class 高，
	 * 只写 .nav-tab-active .nav-text 会被它压掉，选中项永远灰着。
	 * 注：后台配了色时由组件内联 style 覆盖（内联优先级最高）。
	 */
	.bottom .nav-tab.is-active .nav-icon,
	.bottom .nav-tab.is-active .nav-icon .iconfont,
	.bottom .nav-tab.is-active .nav-text {
		color: var(--theme-color, #B0805A);
	}

	.nav-tab-active .circle {
		height: 38px;
		width: 38px;
		line-height: 38px;
		position: relative;
		top: -14px;
		left: calc(50% - 19px);
		border-radius: 50%;
		background: var(--theme-color, #002FA7);
		box-shadow: none;
	}

	.nav-tab-active .iconfont {
		color: #fff;
	}

	.nav-tab-active .nav-text {
		color: var(--theme-color, #002FA7);
	}

	.nav-box {
		/* position: absolute; */
		display: flex;
		width: 100%;
		box-sizing: border-box;
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		background: transparent;
		z-index: 4000;
		/* iPhone X 以上留出位置 */
		padding-bottom: constant(safe-area-inset-bottom);
		padding-bottom: env(safe-area-inset-bottom);
	}

	/* =====================================================================
	   tea 版式底部栏 —— 照参考站做「米白底、金色选中」
	   参考值：底 #f7f5f0 / 未选 #8a8578 / 选中 #b8976a / 10px / 高 50px / 顶部 1px 细线
	   带 .dc-tab-tea 前缀，标品的图标栏不受影响。
	   具体颜色由后台配置（内联 style）覆盖，这里只是缺省值。
	   ===================================================================== */
	.bottom.dc-tab-tea {
		height: 50px;
		background: #f7f5f0;
		border-top: 1px solid #e6e1d5;
	}
	.bottom.dc-tab-tea .nav-box {
		background: transparent;
		border-top: 1px solid #e6e1d5;
		height: 50px;
	}
	.bottom.dc-tab-tea .nav-tab {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 50px;
	}
	.bottom.dc-tab-tea .nav-text {
		padding-bottom: 0;
		font-size: 10px;
		line-height: 1;
		letter-spacing: 0.06em;
		color: #8a8578;
		font-weight: 400;
	}
	.bottom.dc-tab-tea .nav-tab.is-active .nav-text {
		color: #b8976a;
	}
	/* tea + 显示图标时：图标在上、文字在下（tea 默认版式是纯文字，横向 flex 会把图标和字挤成一排） */
	.bottom.dc-tab-tea.dc-tab-dual {
		height: 56px;
	}
	.bottom.dc-tab-tea.dc-tab-dual .nav-box {
		height: 56px;
	}
	.bottom.dc-tab-tea.dc-tab-dual .nav-tab {
		flex-direction: column;
		height: 56px;
	}
	.bottom.dc-tab-tea.dc-tab-dual .nav-icon {
		padding: 6px 0 0 0;
		line-height: 1;
		height: 20px;
	}
	.bottom.dc-tab-tea.dc-tab-dual .nav-icon .iconfont {
		font-size: 18px;
		line-height: 1;
	}
	.bottom.dc-tab-tea.dc-tab-dual .nav-img {
		width: 18px;
		height: 18px;
	}
	.bottom.dc-tab-tea.dc-tab-dual .nav-text {
		margin-top: 3px;
		font-size: 10px;
	}

	/*苹果x适配 H5APP*/
	@media only screen and (device-width: 375px) and (device-height: 812px) and (-webkit-device-pixel-ratio: 3) {
		.bottom {
			//height: 49px + $navBoxHeight;
			height: 49px;
		}
	}

	/*苹果xs适配 H5APP*/
	@media only screen and (device-width: 414px) and (device-height: 896px) and (-webkit-device-pixel-ratio: 3) {
		.bottom {
			//height: 49px + $navBoxHeight;
			height: 49px;
		}
	}

	/*苹果xr适配 H5APP*/
	@media only screen and (device-width: 414px) and (device-height: 896px) and (-webkit-device-pixel-ratio: 2) {
		.bottom {
			// height: 49px + $navBoxHeight;
			 height: 49px;
		}
	}
</style>
