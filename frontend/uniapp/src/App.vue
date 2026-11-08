<script lang="ts">
	import {
		getQueryString
	} from'./utils/split.js';
import {
	mallUrl,
	mallImgUrl,
	mallDefaultUrl
} from './utils/request.js';
	import { MALL_THEME_CACHE_KEY } from './utils/publicVariable.js'
	import {
		queryAutoLogin,
		queryCode2Session
	} from "@/api/general/login"
	import {
		queryWapColorForUniApp,
		queryGetuser,
		quertNavLogo,
		querySwitch,
        apiGetShopIdByAppId
	} from "@/api/pages/index"
	import {
		detectingApplicationstatus
	} from "@/api/store/index"
	
	import { ensureLangPackLoaded } from '@/utils/locale.js'
	import { updateJPushAlias as syncJPushAlias } from '@/utils/jpushAlias.js'
	
	// #ifdef APP-PLUS
	import { setSdkLanguageFromSystem } from '@/uni_modules/tuikit-atomic-x/utils/setSdkLanguageFromSystem'
	// #endif
	// 极光推送
	// #ifdef APP-PLUS
	var jpushModule = uni.requireNativePlugin("JG-JPush");
	var audioObj = uni.getBackgroundAudioManager();
	// #endif

export default {
  onLaunch: async function (options) {
    // 若当前为非中文且无语言包（如首次打开或缓存被清），先拉包再渲染，与「切换语言时先拉包」一致
    ensureLangPackLoaded();
	// #ifdef MP-WEIXIN
	// ====================== 微信小程序 更新逻辑（有提示、不可取消） ======================
	if (uni.canIUse('getUpdateManager')) {
		const updateManager = uni.getUpdateManager()
		// 检测到新版本，后台自动下载
		updateManager.onCheckForUpdate(() => {})
		// 下载完成，弹出强更弹窗，无法取消
		updateManager.onUpdateReady(() => {
			uni.showModal({
				title: '版本更新',
				content: '检测到新版本，需要更新后继续使用',
				showCancel: false, // 隐藏取消按钮
				confirmText: '立即更新',
				success: () => {
					updateManager.applyUpdate()
				}
			})
		})
		// 更新失败静默处理
		updateManager.onUpdateFailed(() => {
			uni.showToast({
				title: '版本更新失败，请稍后重试',
				icon: 'none'
			})
		})
	}	
	// ====================================================================================
	
	if(this.globalData.isBrandWeChat){//品牌小程序
		if(!this.publicVariable.thirdPart.shopId){
			this.getExtStoreId()
		}else{
			const routePages = this.globalData.getCurrentPages();
			const routeList = ['pages/storeIndex/storeIndex']
			console.log('currentPage--brand',routePages);
			if((routePages!=''&&routeList.includes(routePages))||routePages==''){
			   if(routePages!=''){
			   	uni.switchTab({
			   	    url: '/' + routePages
			   	})
			   }else{
			   	uni.switchTab({
			   	    url: '/pages/storeIndex/storeIndex'
			   	})
			   }
			}else{
			    uni.navigateTo({
			        url: '/' + routePages
			    })
			}
		}
	}else{//商城小程序
		const routePages = this.globalData.getCurrentPages();
		const routeList = ['pages/index/index']
		console.log('currentPage--shop',routePages);
		if((routePages!=''&&routeList.includes(routePages))||routePages==''){
			if(routePages!=''){
				uni.switchTab({
				    url: '/' + routePages
				})
			}else{
				uni.switchTab({
				    url: '/pages/index/index'
				})
			}
		    
		}else{
		    uni.navigateTo({
		        url: '/' + routePages
		    })
		}
	}
	// #endif
	 // #ifdef APP-PLUS
        setSdkLanguageFromSystem();
	 //App-ios查询网络是否已连接
	 uni.onNetworkStatusChange(function (resNetwork) {
	 	if(resNetwork.isConnected){
	 		if (plus.os.name.toLowerCase() === 'ios') {
	 			 plus.runtime.restart(function() {
	 				  //应用已重启
	 			 }, function(error) {
	 				//应用重启失败
	 			 });
	 		}
	 	}else{
	 		const errorMsg = '网络链接失败，请稍后再试';
	 		uni.showToast({
	 			title:errorMsg,
	 			 icon:'none'
	 		})
	 	}
	 		
	 });
	 jpushModule.initJPushService();
	 jpushModule.setLoggerEnable(true);
	 plus.screen.lockOrientation("portrait-primary");
	 this.updateJPushAlias();
	 uni.$on('userLoginChanged', () => {
	 	this.updateJPushAlias(true);
	 });
	 //监听 极光推送连接状态
	 this.getNotificationEnabled();
	 jpushModule.addConnectEventListener(result => {
	 	let connectEnable = result.connectEnable
	 	uni.$emit('connectStatusChange', connectEnable)
	 });
	 jpushModule.addNotificationListener(result => { //极光推送的消息通知回调
	 	jpushModule.setBadge(0);
	 	plus.runtime.setBadgeNumber(0);
	 	let notificationEventType = result.notificationEventType
	 	let woopId = result.extras && result.extras.dataType === 'woop' && result.extras.value;
	 	console.log("通知", result, notificationEventType)
	 	// 点击事件
	 	if (notificationEventType == 'notificationOpened') {
	 		uni.navigateTo({
	 			url: '/pages/taskDetail/taskDetail?' + 'woopId=' + woopId
	 		});
	 	}
	 });
	 uni.$on('connectStatusChange', (connectStatus) => {
	 	var connectStr = ''
	 	if (connectStatus == true) {
	 		connectStr = '已连接'
	 		this.updateJPushAlias()
	 	} else {
	 		connectStr = '未连接'
	 	}
	 	console.log('监听到了连接状态变化 --- ', connectStr)
	 	this.connectStatus = connectStr
	 })
	 jpushModule.isPushStopped(res => {
	 	// code   0已停止推送  1未停止推送
	 	const { code } = res
	 	console.log(res, '安卓连接状态');
	 })
	 //#endif
	  
   // 主题色拉取失败也必须 resolve，否则首页等页面 await $onLaunched 会永久卡住白屏
   queryWapColorForUniApp({
   	key: "uni_color"
   }).then(res => {
   	const colorList = res?.data?.colorList
   	if (!colorList || !colorList[0] || !colorList[1]) {
   		return
   	}
   	this.publicVariable.webColor = colorList[0]
   	this.publicVariable.subColor = colorList[1]
   	this.publicVariable.themeData.contMxColor = colorList[0]
   	this.publicVariable.themeData.contFsColor = colorList[1]
   	this.publicVariable.themeData.headBgColor = colorList[2]
   	this.publicVariable.themeData.headTxColor = colorList[3]
   	var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(colorList[0]);
   	if (!result) {
   		return
   	}
   	var r = parseInt(result[1], 16);
   	var g = parseInt(result[2], 16);
   	var b = parseInt(result[3], 16);
   	var rgba9 = "rgba(" + r + "," + g + "," + b + "," + 0.9 + ")";
   	var rgba8 = "rgba(" + r + "," + g + "," + b + "," + 0.8 + ")";
   	var rgba7 = "rgba(" + r + "," + g + "," + b + "," + 0.7 + ")";
   	var rgba6 = "rgba(" + r + "," + g + "," + b + "," + 0.6 + ")";
   	var rgba5 = "rgba(" + r + "," + g + "," + b + "," + 0.5 + ")";
   	var rgba4 = "rgba(" + r + "," + g + "," + b + "," + 0.4 + ")";
   	var rgba3 = "rgba(" + r + "," + g + "," + b + "," + 0.3 + ")";
   	var rgba2 = "rgba(" + r + "," + g + "," + b + "," + 0.2 + ")";
   	var rgba1 = "rgba(" + r + "," + g + "," + b + "," + 0.1 + ")";
   	this.publicVariable.rgba9 = rgba9
   	this.publicVariable.rgba8 = rgba8
   	this.publicVariable.rgba7 = rgba7
   	this.publicVariable.rgba6 = rgba6
   	this.publicVariable.rgba5 = rgba5
   	this.publicVariable.rgba4 = rgba4
   	this.publicVariable.rgba3 = rgba3
   	this.publicVariable.rgba2 = rgba2
   	this.publicVariable.rgba1 = rgba1
   	var results = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(colorList[1]);
   	if (!results) {
   		return
   	}
   	var rs = parseInt(results[1], 16);
   	var gs = parseInt(results[2], 16);
   	var bs = parseInt(results[3], 16);
   	var rgbas9 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.9 + ")";
   	var rgbas8 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.8 + ")";
   	var rgbas7 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.7 + ")";
   	var rgbas6 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.6 + ")";
   	var rgbas5 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.5 + ")";
   	var rgbas4 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.4 + ")";
   	var rgbas3 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.3 + ")";
   	var rgbas2 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.2 + ")";
   	var rgbas1 = "rgba(" + rs + "," + gs + "," + bs + "," + 0.1 + ")";
   	this.publicVariable.rgbas9 = rgbas9
   	this.publicVariable.rgbas8 = rgbas8
   	this.publicVariable.rgbas7 = rgbas7
   	this.publicVariable.rgbas6 = rgbas6
   	this.publicVariable.rgbas5 = rgbas5
   	this.publicVariable.rgbas4 = rgbas4
   	this.publicVariable.rgbas3 = rgbas3
   	this.publicVariable.rgbas2 = rgbas2
   	this.publicVariable.rgbas1 = rgbas1
  }).catch(() => {
  	// 网络/域名异常时忽略，避免阻断启动
  }).finally(() => {
  	// 进一步读取后台「商城风格」里用户真正选中的网站风格，覆盖到内页 themeData，
  	// 让导航栏 / 头部 / 主色按钮跟随后台设置（uni_color 仅作兜底）。
  	// 必须等它落地再 resolve：各页面 onLoad 里立刻读 themeData 建导航栏，
  	// 早一步 resolve 会把 uni_color 的旧配色（绿色/蓝色头部）烤进页面，后台换了风格也不变。
  	// 用 1.5s 兜底，网络异常时不至于卡住启动。
  	const settle = () => this.$isResolve()
  	Promise.race([
  		this.applyMallStyleTheme(),
  		new Promise(resolve => setTimeout(resolve, 1500))
  	]).then(settle, settle)
  })
   if (options.query.q) {
   	let q = decodeURIComponent(options.query.q);
   	var voucherId = getQueryString(q, 'id');
   	var relevanceId = getQueryString(q, 'relevanceId');
   	var shareType = getQueryString(q, 'shareType');
   	var shareUserId = getQueryString(q, 'shareUserId');
   	this.publicVariable.relevanceId = relevanceId
   	this.publicVariable.shareType = shareType
   	this.publicVariable.shareUserId = shareUserId
   	this.publicVariable.voucherId = voucherId;
   	// 分享人必须落 storage：下单链路（sureorder）只认 uni.getStorageSync('shareUserId')，
   	// 只写 publicVariable 会在跳页后丢失，导致扫码进入的订单不归因。
   	if (shareUserId) {
   		uni.setStorageSync('shareUserId', shareUserId)
   	}
   }
  
   // 展示本地存储能力
   var logs = uni.getStorageSync('logs') || [];
   logs.unshift(Date.now());
   uni.setStorageSync('logs', logs);
   // 登录逻辑开始
   // 登录
   // 查看是否授权
   // #ifdef MP-WEIXIN 
   if (uni.getStorageSync('token')) {
   	this.publicVariable.nekot = uni.getStorageSync('token')
   }
   uni.getProvider({
   	service: 'oauth',
   	success: (res) => {
   		if (res.service == "oauth") {
   			uni.login({
   				success: resloginOne => {
   					if (resloginOne.code) {
   						var wxCode = resloginOne.code;
   						queryAutoLogin({
   							code: wxCode,
   							platForm: 6,
   							shopId: this.publicVariable.thirdPart.shopId
   						}).then(resOne => {
   							if (resOne.autoLogin != 2) {
   								uni.login({
   									success: resloginTwo => {
   										if (resloginTwo.code) {
   											var wxCode2 =
   												resloginTwo
   												.code;
   											queryCode2Session({
   												code: wxCode2,
   												platForm: 6,
   												shopId: this
   													.publicVariable
   													.thirdPart
   													.shopId
   											}).then((
   												resTow) => {
   												if (resTow
   													.code ==
   													1
   													) {
   													return false;
   												} else if (
   													resTow
   													.code ==
   													2
   													) {
   													uni.showToast({
   														title: resTow
   															.message,
   														icon: 'none'
   													})
   													return false;
   												} else if (
   													resTow
   													.code ==
   													4
   													) {
   													this.publicVariable
   														.nekot =
   														resTow
   														.token;
   													queryGetuser
   														()
   														.then(
   															(
   																resThree) => {
   																if (resThree
   																	.code ==
   																	1
   																	) {
   																	this.publicVariable
   																		.userSelfInfo =
   																		resThree
   																		.data
   																	// #ifdef APP-PLUS
   																	uni.$emit('userLoginChanged')
   																	// #endif
   																} else {
   																	// #ifdef MP-WEIXIN
   																	this.publicVariable
   																		.nekot =
   																		'';
   																	// #endif
   
   																	// #ifndef MP-WEIXIN
   																	this.$token
   																		.setToken(
   																			'token',
   																			''
   																			);
   																	// #endif
   																}
   															}
   															)
   													uni.setStorage({
   														key: 'token',
   														data: resTow
   															.token
   													});
   													//返回上一页  
   												}
   											})
   										}
   									}
   								})
   							} else {
   								return false;
   							}
   						})
   					}
   				}
   			});
   		} else {
   			uni.navigateTo({
   				url: '/general/login/login'
   			});
   		}
   	}
   });
   // #endif
  },
  methods: {
	  updateJPushAlias(onlyAlias = false) {
	  	return syncJPushAlias(onlyAlias)
	  },
    /**
     * 读取后台「商城风格」里用户选中的网站风格 mall_style，把颜色写入全局 themeData，
     * 使 H5 内页（导航栏 / 头部 / 主色按钮）跟随后台设置，而不是只认 uni_color。
     * 仅在拿到有效主色时才覆盖；失败静默回退到 uni_color / 默认值。
     */
    applyMallStyleTheme() {
      return mallDefaultUrl('web/mall/index/config', {}, 'post').then(res => {
        const ms = res && res.data && res.data.mall_style
        if (!ms || typeof ms !== 'object') return
        const variable = (ms.variable && typeof ms.variable === 'object') ? ms.variable : {}
        const primary = variable['--theme-color'] || ms.color
        if (!primary) return
        const dark = variable['--theme-dark-color'] || ms.dark_color || primary
        const navBg = ms.nav_bg_color || '#FFFFFF'
        const navText = ms.nav_text_color || '#1A1A1A'
        this.publicVariable.webColor = primary
        this.publicVariable.subColor = dark
        this.publicVariable.themeData.contMxColor = primary
        this.publicVariable.themeData.contFsColor = dark
        this.publicVariable.themeData.headBgColor = navBg
        this.publicVariable.themeData.headTxColor = navText
        this.applyThemeAlpha(primary, 'rgba')
        this.applyThemeAlpha(dark, 'rgbas')
        // 落地一份到本地缓存：各页面建导航栏时读的是 publicVariable.themeData，
        // 接口回来得比首屏晚，不缓存的话换风格后首屏仍是旧配色。
        try {
          uni.setStorageSync(MALL_THEME_CACHE_KEY, {
            webColor: primary,
            subColor: dark,
            contMxColor: primary,
            contFsColor: dark,
            headBgColor: navBg,
            headTxColor: navText,
            // 站点风格名（tea_theme 等）：内页没有装修记录，只能靠它判定版式
            skin: ms.current || ''
          })
        } catch (e) { /* 存储不可用时忽略，仅影响首屏 */ }
        // #ifdef H5
        this.paintThemeVars({ primary, dark, bg: ms.bg_color || '', border: variable['--theme-border-color'] || '' })
        // uni-app H5 每次路由切换都会重建 uni-page-body，而它身上的 --theme-color 是
        // 编译期 pages.json tabBar.selectedColor 注入的，会把运行时的主题色顶掉。
        // 这里挂一次 hashchange 重绘，保证内页始终跟随后台风格接口。
        if (!this.__themeBound && typeof window !== 'undefined') {
          this.__themeBound = true
          window.addEventListener('hashchange', () => {
            setTimeout(() => this.paintThemeVars(this.__themeColors || {}), 80)
          })
        }
        // #endif
      }).catch(() => {})
    },
    /**
     * 把主题色写进 CSS 变量。
     *
     * <p>H5 下 uni-app 会把 pages.json 里 tabBar.selectedColor 注入到每个
     * uni-page-body 的 --theme-color，优先级高于 :root，只改 documentElement
     * 的话内页/底部菜单仍然是编译期的颜色。所以根节点和所有 uni-page-body 都要写。
     *
     * @param {Object} colors { primary, dark, bg, border }
     */
    paintThemeVars(colors) {
      // #ifdef H5
      if (typeof document === 'undefined' || !colors || !colors.primary) return
      this.__themeColors = colors
      const targets = [document.documentElement]
      document.querySelectorAll('uni-page-body').forEach(el => targets.push(el))
      for (const el of targets) {
        if (!el || !el.style) continue
        el.style.setProperty('--theme-color', colors.primary)
        if (colors.dark) el.style.setProperty('--theme-dark-color', colors.dark)
        if (colors.bg) el.style.setProperty('--theme-bg-color', colors.bg)
        if (colors.border) el.style.setProperty('--theme-border-color', colors.border)
      }
      // #endif
    },
    /**
     * 由主色生成 rgba1~rgba9 透明度色阶写入 publicVariable，供渐变背景使用
     */
    applyThemeAlpha(hex, prefix) {
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
      if (!result) return
      const r = parseInt(result[1], 16)
      const g = parseInt(result[2], 16)
      const b = parseInt(result[3], 16)
      for (let i = 1; i <= 9; i++) {
        this.publicVariable[prefix + i] = 'rgba(' + r + ',' + g + ',' + b + ',' + (i / 10) + ')'
      }
    },
		getNotificationEnabled() {
			if (uni.getSystemInfoSync().platform == "ios") {
				jpushModule.requestNotificationAuthorization((result) => {
					let status = result.status
					if (status < 2) {
						this.noticMsgTool()
					}
				})
			} else {
				jpushModule.isNotificationEnabled((result) => { //判断android是否打开权限
					if (result.code == 0) { //如果为0则表示 未打开通知权限 
						this.noticMsgTool()
					}
				})
			}
		},
		noticMsgTool() {
			if (uni.getSystemInfoSync().platform == "ios") {
				//苹果打开对应的通知栏
				uni.showModal({
					title: '通知权限开启提醒',
					content: '您还没有开启通知权限，无法接受到消息通知，请前往设置！',
					showCancel: false,
					confirmText: '去设置',
					success: function(res) {
						if (res.confirm) {
							var app = plus.ios.invoke('UIApplication', 'sharedApplication');
							var setting = plus.ios.invoke('NSURL', 'URLWithString:', 'app-settings:');
							plus.ios.invoke(app, 'openURL:', setting);
							plus.ios.deleteObject(setting);
							plus.ios.deleteObject(app);
						}
					}
				});
			} else {
				//android打开对应的通知栏
				var main = plus.android.runtimeMainActivity();
				var pkName = main.getPackageName();
				var uid = main.getApplicationInfo().plusGetAttribute("uid");
				uni.showModal({
					title: '通知权限开启提醒',
					content: '您还没有开启通知权限，无法接受到消息通知，请前往设置！',
					showCancel: false,
					confirmText: '去设置',
					success: function(res) {
						if (res.confirm) {
							var Intent = plus.android.importClass('android.content.Intent');
							var Build = plus.android.importClass("android.os.Build");
							//android 8.0引导  
							if (Build.VERSION.SDK_INT >= 26) {
								var intent = new Intent('android.settings.APP_NOTIFICATION_SETTINGS');
								intent.putExtra('android.provider.extra.APP_PACKAGE', pkName);
							} else if (Build.VERSION.SDK_INT >= 21) { //android 5.0-7.0  
								var intent = new Intent('android.settings.APP_NOTIFICATION_SETTINGS');
								intent.putExtra("app_package", pkName);
								intent.putExtra("app_uid", uid);
							} else { //(<21)其他--跳转到该应用管理的详情页  
								intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								var uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
								intent.setData(uri);
							}
							// 跳转到该应用的系统通知设置页  
							main.startActivity(intent);
						}
					}
				});
			}
		},
	  async getExtStoreId() {
		var that=this;
	    try {
            const accountInfo = uni.getAccountInfoSync();
            const res = await apiGetShopIdByAppId({
                appId: accountInfo.miniProgram.appId
            });
	      console.log(res,'res-ext返回')
	      if ( res.code === 1 && res.data) {
	        // 跳转到店铺首页
	       that.publicVariable.thirdPart.shopId = res.data;
	       uni.switchTab({
	       	url: '/pages/storeIndex/storeIndex'
	       })
	      }
	    } catch (error) {
	      console.error('获取配置失败:', error);
	      // 处理获取配置失败的情况，跳转到默认首页
	     that.publicVariable.thirdPart.shopId ='';
		 //以下禁止放开，影响分享
	     // uni.switchTab({
	     //     url: '/pages/index/index'
	     // });
	    }
	  },

  	getIsOpenService() {
  		detectingApplicationstatus().then(res => {
  			if (res.code == 1) {
  				var resData = res.data;
  				resData.forEach(item => {
  					if (item.configKey == '骑手配送') {
  						this.publicVariable.riderIsOpen = item.configStatus;
  						return;
  					}
  				});
  			}
  		})
  	},
	//获取默认商品图
	getDefaultImg(){
		const params={
			configType:'logo_copywriting',
			configKey:'logo_copywriting_ico_goods'
		}
		quertNavLogo(params).then(res=>{
			if(res.code==1){
				this.publicVariable.defaultImg=res.data[0].configValue;
			}
		})
	},
	async getImgSearch(){
		let { code ,data } = await querySwitch('aiImages_switch');
		if(code == 1){
			this.publicVariable.isShowImgSearch= data.configValue == '1' ? true :false;
		}
	},
	async getInternational(){
		let { code ,data } = await querySwitch('internationalization_switch');
		if(code == 1){
			this.publicVariable.isShowInternational= data.configValue == '1' ? true :false;
			if(!this.publicVariable.isShowInternational){
				uni.setStorageSync("currencySymbol",'￥');
				uni.setStorageSync("currencyRemark", '元');
				uni.setStorageSync("currencyCode",'CNY');
				uni.setStorageSync("exchangeRate",1);
			}
		}
	},
  	
  },
  globalData: {
  	userInfo: null,
  	// 正式数据库
  	url: mallUrl,
  	imgUrl: mallImgUrl,
  	oAuth: [],
  	themeData: {
  		contFsColor: "#1d2420",
  		contMxColor: "#b0805a",
  		contRadio: 2,
  		headBgColor: "#f4f1ea",
  		headRadio: 2,
  		headTxColor: "#221e17"
  	},
  	find: {
  		selData: [],
  		selTopic: [],
  		selImg: [],
  		inputConts: ""
  	},
  	selInvoiceInfos: {
  		invoiceModel: {},
  		invoiceAddrModel: {}
  	},
  	isShowLikeModule: true,
  	thirdPart: {
  		shopId:''
  	},
  	riderIsOpen: false,
	isBrandWeChat:false, //是否是打包成品牌小程序
  	getCurrentPages: function() {
  		var pages = getCurrentPages(); //获取加载的页面
  		var currentPage = pages[pages.length - 1]; //获取当前页面的对象
		console.log(currentPage,'currentPage---')
		if(currentPage){
			var url = currentPage.route; //当前页面url
			var options = currentPage.options; //获取url中所带的参数
			//拼接url的参数
			var currentPage = url + '?';
			for (var key in options) {
				var value = options[key];
				currentPage += key + '=' + value + '&';
			}
			currentPage = currentPage.substring(0, currentPage.length - 1);
		}else{
			currentPage=''
		}
		console.log(currentPage,'currentPage-')
  		return currentPage;
  	}
  },
  onShow:function () {
	// #ifdef APP-PLUS
	if (uni.getStorageSync('token')) {
		this.updateJPushAlias(true);
	}
	// #endif
	this.getIsOpenService();
	this.getDefaultImg();
	this.getImgSearch();
	this.getInternational();
	
  },
  onHide: function () {
  },
}
</script>

<style lang="scss">
/* #ifndef APP-NVUE */
	@import "uview-plus/index.scss";
	@import "./app.css";
	@import "static/css/public.css";
	@import "static/css/iconfont/iconfont.css";
	@import "static/css/base.css";
	@import "static/css/swiss-commerce.scss";
	/* tea 模板版式（奢侈品简单风格）：全局样式，选择器已限定在 .dc-tpl-tea / .dc-tea- 下 */
	@import "./components/tea-template.css";
/* #endif */
</style>
