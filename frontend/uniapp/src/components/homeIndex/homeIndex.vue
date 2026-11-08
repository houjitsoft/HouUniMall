<template>
	<view v-if="decorationWidgets !== null" class="dc-home" :class="templateClass" :style="decorationPageStyle">
		<div v-if="!isTea" class="dc-home-title" :style="decorationTitleStyle">{{ decorationMeta.title || '首页' }}</div>
		<!--
			tea 模板的品牌头：覆盖在首屏大图上（透明、细线、衬线居中），
			移动端折成汉堡菜单，与 tea.houjit.com 的顶部导航一致。
		-->
		<div v-if="isTea" class="dc-tea-header">
			<div class="dc-tea-header-side">
				<button class="dc-tea-menu-btn" :aria-expanded="menuOpen ? 'true' : 'false'" aria-label="打开导航" @click="menuOpen = !menuOpen"><i></i><i></i></button>
				<!-- 桌面端直接平铺前三个入口，移动端收进汉堡菜单（与茶站一致） -->
				<span class="dc-tea-nav"><span v-for="link in teaLinks.slice(0, 3)" :key="link.path" @click="goTea(link.path)">{{ link.text }}</span></span>
			</div>
			<div class="dc-tea-logo">
				<strong>{{ siteName }}</strong>
				<small>{{ teaTagline }}</small>
			</div>
			<div class="dc-tea-header-side right">
				<span role="button" class="dc-tea-bag" aria-label="购物袋" @click="goTea('/pages/cart/cart')">
					<svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path d="M5 7h14l1 14H4L5 7Z" fill="none" stroke="currentColor" stroke-width="1.3" /><path d="M8 8V6a4 4 0 0 1 8 0v2" fill="none" stroke="currentColor" stroke-width="1.3" /></svg>
				</span>
			</div>
			<div v-if="menuOpen" class="dc-tea-menu-panel">
				<span v-for="link in teaLinks" :key="link.path" @click="goTea(link.path); menuOpen = false">{{ link.text }}</span>
			</div>
		</div>
		<DecorationWidget v-for="(widget, index) in displayWidgets" :key="widget.id || index" :widget="themedWidget(widget, decorationWebsiteStyle, templateView)" :runtime="decorationRuntime" :demo-map="shopDemoMap" :demo-assets="templateDemoAssets" :template-code="templateCode" @navigate="navigateDecoration" />
		<!-- tea 模板页脚：品牌 + 链接 + 版权，收整页的编辑感 -->
		<div v-if="isTea" class="dc-tea-footer">
			<div class="dc-tea-footer-top">
				<div>
					<div class="dc-tea-footer-brand">{{ siteName }}</div>
					<p>{{ teaTagline }}</p>
				</div>
				<div class="dc-tea-footer-links">
					<span role="button" v-for="link in teaLinks" :key="link.path" @click="goTea(link.path)">{{ link.text }}</span>
				</div>
			</div>
			<div class="dc-tea-footer-bottom">
				<span>© {{ copyrightYear }} {{ siteName }}</span>
				<span>演示内容，商品与会员权益以实际发布为准</span>
			</div>
		</div>
	</view>
	<view v-else class="pb-30">
		<!-- 头部自定义导航 -->
		<image v-if="isShowBg" class="pageBg wp100" :src="pageData.pageBaseInfo.bgImg" :style="{height:Number(pageData.pageBaseInfo.bgHeight)+'px'}"></image>
		<hx-navbar ref='hxnb' v-if="off&&isShowHead" :config="config">
			<template #left>
				<image class="ml20 headLeftImg" v-if="pageData.pageBaseInfo.headLeftImg!=''" :src="pageData.pageBaseInfo.headLeftImg" :style="{width:Number(pageData.pageBaseInfo.headLeftImgW)+'px',height:Number(pageData.pageBaseInfo.headLeftImgH)+'px'}"></image>
			</template>
		</hx-navbar>
		<!-- 页面 -->
		<view class="index-head-home" :class="!isShowHead?'yes':''">
			<home ref='homeChild' :allpropsobj="curProps()" :isFixed="isFixed" :templateView="templateView"/>
		</view>
		<view class="indexPopDialog" v-if="pageData?.pageBaseInfo?.popImgOff==1&&imgSwitch">
			<view class="mask"></view>
			<view class="contents">
				<image mode='widthFix' @click="handleDetail" :src="pageData.pageBaseInfo.popImg" :style="{width:pageData.pageBaseInfo.popImgW+'px',height:pageData.pageBaseInfo.popImgH+'px'}"></image>
				<view class="iconBtnClose tc mt20">
					<icon class="iconfont icon-guanbi1" @click="handleClosePop"></icon>
				</view>
			</view>
		</view>

	</view>
	<!-- 后台「模板选择」实时预览角标：仅 ?tpl=xxx 预览时出现，标明当前模板与其存储位置 -->
	<view v-if="previewTpl" class="tpl-preview-badge">
		<text class="tpl-preview-badge-main">模板预览：{{ (pageTemplates[previewTpl] || {}).name || previewTpl }}（{{ previewTpl }}）</text>
		<text class="tpl-preview-badge-sub">实现 {{ templateStorage.implement }}｜持久化 {{ templateStorage.persist }}</text>
	</view>
</template>
<script>
	let app = getApp({allowDefault: true});
	import home from '@/components/home/home.vue'
	import DecorationWidget from '../DecorationWidget.vue'
	import { flattenWidgets, pageStyle, themedWidget, loadDecorationRuntime, pageTemplates, templateStorage, isTeaStyle, templateClass as resolveTemplateClass, templateDemoMap, currentTemplate, teaContent } from '../decoration-rules.mjs'
	import { mallDefaultUrl } from '@/utils/request.js'
	import hxNavbar from '@/components/hx-navbar/hx-navbar.nvue'
	import {
		quermoduIndex,
		quertNavLogo,
		queryGetuser
	} from "@/api/pages/index"
	
	export default {
		props:['isFixed', 'previewTpl'],
		components: {
			DecorationWidget,
			home,
			hxNavbar
		},
		data() {
			return {
				decorationWidgets: null,
				decorationMeta: {},
				decorationWebsiteStyle: {},
				decorationRuntime: { modules: {}, shops: [], errors: [] },
				imgSwitch:false,
				isShowBg:false,
				isShowHead:false,
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				index: 0,
				title: '首页',
				skeletonOff: false,
				reciveData: {},
				foo: false,
				shows: 0,
				top: '44rpx',
				headBg: this.publicVariable.themeData.contMxColor,
				webColor: this.publicVariable.webColor,
				config: {
					title: '商城',
					color: '#fff',
					back: false,
					leftSlot:true,
					backgroundColor: [0,'#fff'],
					slideBackgroundColor: [1,'#fff'],
				},
				themeData: {},
				staHeight: 0,
				off: false,
				componentLists: [],
				scrollTop: 0,
				classList: [],
				pageData:{},
				/**
				 * 后台「移动端装修 → 模板选择」下发的模板描述。
				 * 空对象 = 未套用模板，首页沿用页面原始版式。
				 */
				templateView:{},

				// tea 模板的品牌头 / 页脚：站点名取 /web/mall/index/config 的 website.name
				menuOpen: false,
				siteName: '商城',
				teaTagline: teaContent.hero.title,
				teaLinks: [
					{ text: '臻品分类', path: '/pages/information/information' },
					{ text: '我的收藏', path: '/member/collection/collection' },
					{ text: '会员中心', path: '/pages/personal/personal' },
					{ text: '购物袋', path: '/pages/cart/cart' }
				],
				copyrightYear: new Date().getFullYear(),

				// 模板注册表与存储位置说明，供预览角标展示
				pageTemplates,
				templateStorage
			}

		},
		async onLoad() {
			await this.$onLaunched
			this.top = uni.getSystemInfoSync().statusBarHeight + Number(44) + 'px';
			var themeData = this.publicVariable.themeData;
			this.themeData = themeData;
		},
		async mounted() {
			await this.$onLaunched;
			this.init();
			uni.$emit('setTabBarItem');
			var title1 = await this.fetchData();
			this.config.title = title1;
			this.off = true;
			this.checkStoredTime();
		},
		watch: {
			/**
			 * 预览模板 code 变化时同步刷新模板描述。
			 * onLoad 与组件挂载先后顺序在 H5 / 小程序下不一致，靠 watch 兜底。
			 * @param {string} value 模板 code
			 */
			previewTpl(value) {
				if (value) this.templateView = { templateCode: String(value) }
			}
		},
		computed: {
			decorationPageStyle() { return pageStyle(this.decorationMeta, this.decorationWebsiteStyle, this.templateView) },
			/**
			 * 页面根节点的模板 class，样式表据此整页换肤（如 tea 去掉白卡片、改茶褐金）。
			 * @returns {string} 形如 dc-tpl-tea；未套用模板为空串
			 */
			templateClass() { return resolveTemplateClass(this.decorationWebsiteStyle, this.templateView) },
			/**
			 * 实际渲染顺序。
			 *
			 * <p>tea 版式首屏必须是整屏主视觉：装修数据里搜索框排在 banner 前面，
			 * 原样渲染会压在品牌头上。这里把搜索挪到公告条之后，
			 * 其余组件顺序完全按装修页保存的顺序，不替商家改版。
			 * @returns {Array}
			 */
			displayWidgets() {
				const list = this.decorationWidgets || []
				if (!this.isTea) return list
				const search = list.filter(x => x.name === 'search')
				if (!search.length) return list
				const rest = list.filter(x => x.name !== 'search')
				const noticeIndex = rest.findIndex(x => x.name === 'notice')
				const at = noticeIndex >= 0 ? noticeIndex + 1 : 1
				return [...rest.slice(0, at), ...search, ...rest.slice(at)]
			},
			/**
			 * 当前生效的模板 code（tea / simple / vivid / premium / 空）。
			 * 下发给每个装修组件，决定用编辑式版式还是标准组件版式。
			 * @returns {string}
			 */
			templateCode() { return currentTemplate(this.decorationWebsiteStyle, this.templateView)?.code || '' },
			/**
			 * 是否走 tea 编辑式版式（品牌头 / 页脚 / 隐藏原生标题条）。
			 * @returns {boolean}
			 */
			isTea() { return this.templateCode === 'tea' },
			/**
			 * 店铺列表的货品图来自真实店铺接口，不经过 themedWidget，
			 * 这里把模板的演示素材表下发给组件自行替换（只影响内置演示图，商家真实图不动）。
			 * @returns {Object|null}
			 */
			shopDemoMap() { return templateDemoMap(this.decorationWebsiteStyle, this.templateView, 'shoplists') },
			/**
			 * 整张演示素材表下发给组件：运行时接口的商品图（moduleGoods / moduleSeckill）
			 * 不走 themedWidget，只有拿到整表才能换成模板素材。
			 * @returns {Object|null}
			 */
			templateDemoAssets() { return currentTemplate(this.decorationWebsiteStyle, this.templateView)?.demoAssets || null },
			decorationTitleStyle() {
				const m = this.decorationMeta
				// 奢侈品简单风格：衷线标题 + 茶褐金字身 + 宽字距，是 tea 模板最直接的识别特征
				if (isTeaStyle(this.decorationWebsiteStyle, this.templateView)) {
					return { color: '#221e17', background: 'transparent', fontFamily: 'Georgia, "Songti SC", "STSong", serif', letterSpacing: '3px', fontSize: '15px', fontWeight: '400' }
				}
				return { color: Number(m.title_color || 1) === 1 ? '#000' : '#fff', background: m.titlebg_type == 2 && m.titlebg_img ? `url(${m.titlebg_img}) center top / 100% no-repeat` : m.titlebg_color || '#fff' }
			}
		},
		methods: {
			themedWidget,
			/**
			 * tabBar 页面只能用 switchTab 打开，用 navigateTo 会静默失败。
			 * 模板里的「臻品分类 / 会员中心 / 购物袋」都指向 tab 页，这里统一兜住。
			 */
			goTea(path) {
				const tabRoutes = ['/pages/index/index', '/pages/information/information', '/pages/search/search', '/pages/cart/cart', '/pages/personal/personal', '/pages/finds/finds', '/pages/diy/diy', '/pages/storeIndex/storeIndex']
				if (tabRoutes.includes(path)) {
					uni.switchTab({ url: path, fail: () => uni.showToast({ title: '该页面暂不可用', icon: 'none' }) })
					return
				}
				uni.navigateTo({ url: path, fail: () => uni.showToast({ title: '该页面暂不可用', icon: 'none' }) })
			},
			navigateDecoration(link) {
				const aliases = { '/pages/news/news': '/general/news/news', '/pages/user_set/user_set': '/general/settingsPage/settingsPage', '/pages/as_us/as_us': '/pages/description/descrption', '/pages/agreement/agreement': '/general/privacyPage/privacyPage', '/pages/collection/collection': '/member/collection/collection', '/pages/news_detail/news_detail': '/activities/informationDetails/informationDetails' }
				let path = typeof link === 'string' ? link : link.path
				if (!path) return
				if (typeof link === 'object' && link.type === 'article') path = '/activities/informationDetails/informationDetails'
				if (typeof link === 'object') {
					const query = Object.entries(link.query || {}).map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`).join('&')
					path = (aliases[path] || path) + (query ? `${path.includes('?') ? '&' : '?'}${query}` : '')
				}
				if (!path.startsWith('/') || path.startsWith('//')) return
				uni.navigateTo({ url: path, fail: () => uni.showToast({ title: '该跳转暂不可用', icon: 'none' }) })
			},
			async init(){
				const post = (path, body) => mallDefaultUrl(path.replace(/^\//, ''), body, 'post')
				try {
					const result = await post('/web/mall/index/data', {})
					if (result?.code === 1 && Array.isArray(result.data?.page?.data)) {
						this.templateView = { templateCode: result.data.page.page_template_code || '' }
						if (this.previewTpl) this.templateView = { templateCode: this.previewTpl }
						const widgets = flattenWidgets(result.data.page.data)
						this.decorationMeta = widgets.find(x => x.name === 'page-meta')?.content || {}
						this.decorationWidgets = widgets.filter(x => !['page-meta', 'data-meta'].includes(x.name))
						try {
							const config = await post('/web/mall/index/config', {})
							if (config?.code === 1) {
								this.decorationWebsiteStyle = config.data?.mall_style || {}
								// tea 模板的品牌头与页脚用站点名，不写死演示品牌
								this.siteName = config.data?.website?.name || this.siteName
							}
						} catch { this.decorationWebsiteStyle = {} }
						this.decorationRuntime = await loadDecorationRuntime(post)
						return
					}
				} catch (error) { console.error('新版装修读取失败', error) }
				uni.showToast({ title: '装修配置读取失败，显示兼容页面', icon: 'none' })
				this.themeData = this.publicVariable.themeData;
				this.config.statusBarFontColor = this.themeData.headTxColor;
				this.headBg=this.publicVariable.themeData.contMxColor;
				this.getUserPhone();
				this.top = uni.getSystemInfoSync().statusBarHeight + Number(44) + 'px';
				quermoduIndex().then((res) => {
					var arr = JSON.parse(res.data.pageData);
					this.pageData=arr;
					// 后台模板选择：服务端按本页 page_template_code 下发模板描述
					// 未套模板时 templateView.style 为空，此处保持原版式
					this.templateView = res.data.templateView || {};
					if (this.previewTpl) this.templateView = { templateCode: this.previewTpl };
					if(this.pageData.pageBaseInfo.bgImg!=''){
						this.isShowBg=true;
					}
					this.config.color=arr.pageBaseInfo.titleColor;
					if(arr.moduleList[0]&&arr.moduleList[0].moduleSign=="moduleCombination"){
						this.isShowHead=false;
					}else{
						this.isShowHead=true;
					}
					var goodsArr = res.data.goodsList;
					arr.moduleList.forEach((item1, index1) => {
						// item1.moduleJson = JSON.parse(item1.moduleJson) 
						item1.moduleJson = item1.moduleJson
					})
					if (goodsArr) {
						goodsArr.forEach((item, index) => {
							arr.moduleList.forEach((item1, index1) => {
								if (item1.moduleSign == 'moduleGoods' || item1.moduleSign == 'moduleRecommendedTopic') {
									if (item1.moduleJson.items.length > 0) {
										item1.moduleJson.items.forEach((item2, index2) => {
											if (item2.goodsId == item.goodsId) {
												if (item.goodsStart == 1) {
													arr.moduleList[index1].moduleJson.items[index2].commonPrice = item.commonPrice;
													arr.moduleList[index1].moduleJson.items[index2].commonSalenum = item.commonSalenum;
													arr.moduleList[index1].moduleJson.items[index2].commonName = item.commonName;
													arr.moduleList[index1].moduleJson.items[index2].commonImage = item.commonImage;
												} else {
													arr.moduleList[index1].moduleJson.items.splice(index2, 1);
												}
											}
				
				
										})
									}
				
								} else if (item1.moduleSign == "moduleDiscount" || item1.moduleSign == "moduleSecKill") {
									var lenNum = 0;
									if (item1.moduleJson.items && item1.moduleJson.items.length > 0) {
										item1.moduleJson.items.forEach((item2, index2) => {
											if (item2.items.length > 0) {
												item2.items.forEach((item3, index3) => {
													if (item3.goodsId == item.goodsId) {
														if (item.activityStatus == 1 && item.goodsStart == 1) {
															if (item1.moduleSign == "moduleSecKill") {
																arr.moduleList[index1].moduleJson.items[index2].items[index3].seckillPrice = item.commonPrice;
															} else if (item1.moduleSign == "moduleDiscount") {
																arr.moduleList[index1].moduleJson.items[index2].items[index3].discountPrice = item.commonPrice;
															} else {
																arr.moduleList[index1].moduleJson.items[index2].items[index3].commonPrice = item.commonPrice;
															}
															if (item.endTime) {
																arr.moduleList[index1].moduleJson.items[index2].end_time = item.endTime;
																arr.moduleList[index1].moduleJson.items[index2].items[index3].end_time = item.endTime;
															}
															arr.moduleList[index1].moduleJson.items[index2].items[index3].commonSalenum = item.commonSalenum;
				                                            arr.moduleList[index1].moduleJson.items[index2].items[index3].commonName = item.commonName;
															arr.moduleList[index1].moduleJson.items[index2].items[index3].commonImage = item.commonImage;
														} else {
															if (item.activityStatus == 2) {
																lenNum++;
																var lens = item2.items.length;
																arr.moduleList[index1].moduleJson.items[index2].items.splice(index3, 1);
																if (lenNum == lens) {
																	arr.moduleList[index1].moduleJson.items.splice(index2, 1);
																}
															}
														}
				
													}
												})
											}
				
				
				
										})
									}
								} else if (item1.moduleSign == "modulePintuan") {
									var lenNum = 0;
									if (item1.moduleJson.items.length > 0) {
										item1.moduleJson.items.forEach((item2, index2) => {
											if (item2.items) {
												if (item2.items.goodsId == item.goodsId) {
													if (item.activityStatus == 1 && item.goodsStart == 1) {
														arr.moduleList[index1].moduleJson.items[index2].items.price = item.commonPrice;
														arr.moduleList[index1].moduleJson.items[index2].items.commonSalenum = item.commonSalenum;
														arr.moduleList[index1].moduleJson.items[index2].items.commonName = item.commonName;
														arr.moduleList[index1].moduleJson.items[index2].items.commonImage = item.commonImage;
													} else {
														if (item.activityStatus == 2) {
															lenNum++;
															var lens = item1.moduleJson.items.length;
															arr.moduleList[index1].moduleJson.items.splice(index2, 1);
															if (lenNum == lens) {
																arr.moduleList.splice(index1, 1);
															}
														}
													}
												}
											}
										})
									}
								}else if( item1.moduleSign == 'moduleGoodsGroup'){
									item1.moduleJson.items.forEach((item2,index2)=>{
										if(item2.lists && item2.lists.length > 0){
											item2.lists.forEach((item3,index3) =>{
												if(item.goodsId == item3.goodsId ){
                                                    if (item.goodsStart == 1) {
                                                        arr.moduleList[index1].moduleJson.items[index2].lists[index3] =item;
                                                    }
												}
											})
										}
									})
								}
							})
						})
					}
				
					this.componentLists = arr.moduleList;
				})
				
			},
			// 检查存储的时间是否显示弹窗
			checkStoredTime() {
			  const storedTime = uni.getStorageSync('futureTime')||0;
			  console.log(storedTime,'storedTime---')
			  if (storedTime) {
				const now = new Date().getTime();
				// 如果当前时间大于等于存储的时间
				if (now >= storedTime) {
				  // 显示弹框
				  this.imgSwitch=true;
				  this.setFetureTime();
				}else{
					this.imgSwitch=false;
				}
			  }else{
				  this.imgSwitch=true;
			  }
			},
			handleClosePop(){
				this.imgSwitch=false;
				this.setFetureTime();
			},
			setFetureTime(){
				// 获取当前时间
				  const now = new Date();
				// 获取后台设置的弹窗间隔时间
				const hoursToAdd = this.pageData.pageBaseInfo?.time;
				const futureTime = new Date(now.getTime() + hoursToAdd * 60 * 60 * 1000);
				// 存储到本地
				uni.setStorageSync('futureTime', futureTime.getTime());
			},
			scrollChange(e){
				if(this.off&&this.isShowHead){
					this.$refs.hxnb.pageScroll(e);
				}
			},
			curProps() {
				return this.componentLists
			},
			async fetchData() {
				var title = '';
				await quertNavLogo({
					configKey: 'logo_copywriting_ico_h5_title',
					configType: 'logo_copywriting'
				}).then(res => {
					title = res.data[0].configValue
				})
				return title;

			},
			getUserPhone() {
				queryGetuser().then(res => {
					if (res.code == 1) {
						if (res.data.userMobile) {
							this.$token.setToken("mobilePhone", res.data.userMobile);
						}
					}
				})

			},
			//事件处理函数
			bindViewTap: function(e) {
				uni.navigateTo({
					url: '/general/searchpage/searchpage'
				})
			},
			reachBottomFun(type){
				if (this.decorationWidgets !== null) return
				this.$refs.homeChild?.reachBottomFun(type);
			},
			handleDetail(){
				if(this.pageData.pageBaseInfo.popImgLink!=''){
					uni.navigateTo({
						url: this.pageData.pageBaseInfo.popImgLink
					})
				}
			}

		}
		
	}
</script>

<style>
	@import './homeIndex.css';
.dc-home { padding-bottom: 60px; }
.dc-home-title { font-family: Arial,"PingFang SC","Microsoft YaHei",sans-serif; font-size: 14px; font-weight: 400; text-align: center; height: 44px; line-height: 44px; }

.dc-tpl-tea { --theme-color: #b0805a; --theme-dark-color: #1d2420; --theme-light-color: #e6e2d9; }
.dc-tpl-tea .dc-home-title { font-family: Georgia,"Songti SC","STSong",serif; font-size: 15px; letter-spacing: 3px; color: #221e17; background: transparent; }

.tpl-preview-badge { position: fixed; left: 12px; right: 12px; bottom: 70px; z-index: 9999; display: flex; flex-direction: column; gap: 2px; padding: 8px 12px; border-radius: 8px; background: rgba(0,0,0,0.72); pointer-events: none; }
.tpl-preview-badge-main { color: #ffffff; font-size: 13px; font-weight: 600; }
.tpl-preview-badge-sub { color: rgba(255,255,255,0.78); font-size: 10px; word-break: break-all; }
</style>