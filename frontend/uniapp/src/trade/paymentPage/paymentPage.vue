<template>
	<view :class="'payContainer wrap ' + skinClass" :style="temp">
		<!-- 页头：tea 版式自绘（pages.json 用 navigationStyle: custom），替代原生标题栏 -->
		<view class="teaPageHead">
			<view class="teaPageHeadBar">
				<view class="teaPageBack" @tap="navBack"><text class="teaPageBackIco"></text></view>
				<view class="teaPageHeadText">
					<text class="teaPageHeadTitle">{{ getTransNew('收银台')}}</text>
					<text class="teaPageHeadSub">{{ getTransNew('安全支付 · 即时到账')}}</text>
				</view>
			</view>
			<view class="teaHairline"></view>
		</view>
		<!-- 订单金额 -->
		<view :class="countNone" class="payCountDown mb20" v-if="!isPlusPay && isShowTime !=false&&lefttime != null&&lefttime !='undefined'">
			<view>
				<text class="timeName">{{ getTransNew("支付剩余时间")}}</text>	<view class="iblock">
					<countdown @timeup="timeup" comStyle="style2" :startTime="startTime" :endTime="lefttime | orderTime" />
				</view>
			</view>
		</view>
		<view class="orderAmountSty wrapRadius boxWrap1">
			<text class="amountOfMoney totalAmount" v-if="orderAmount">{{currencySymbol()}}{{ getRatePrice(orderAmount) }}</text>
			
			<view class="orderText">{{ isPlusPay ? getTransNew('开通PLUS会员') : getTransNew('应付总价')}}</view>
			
			<view class="orderPriDet" v-if="!isPlusPay">
				<view class="h5"><text>{{getTransNew('价格明细')}}</text></view>
				<view class="li">
					<text class="name">{{getTransNew('商品总价')}}</text>
					<view class="b flex1"></view>
					<text class="pri" v-if="orderGoodsPrice">{{currencySymbol()}} {{getRatePrice(orderGoodsPrice)}}</text>
				</view>
				<view class="li">
					<text class="name">{{getTransNew('运费')}}</text>
					<view class="b flex1"></view>
					<text class="pri">{{currencySymbol()}} {{getRatePrice(orderShippingFee)}}</text>
				</view>
			</view>
		</view>
		<view class="wrapRadius boxWrap2" v-if="!isPlusPay && totalIntegral > 0">
			<view class="orderPriDet">
				<view class="h5"><text>{{getTransNew('积分明细')}}</text></view>
				<view class="li">
					<text class="name">{{getTransNew('消耗积分')}}</text>
					<view class="b flex1"></view>
					<text class="pri">{{totalIntegral}}</text>
				</view>
				<view class="li">
					<text class="name">{{getTransNew('剩余可用积分')}}</text>
					<view class="b flex1"></view>
					<text class="pri">{{userIntegral}}</text>
				</view>
			</view>
		</view>
		
		<view class="orderId tr" v-if="!isPlusPay && ordernumber">{{getTransNew('订单号：')}}{{ ordernumber }}</view>
		
		<!-- 商城支付 -->
         <!-- 订单金额 -->
        <view v-if="!isPlusPay && [1, 2].includes(Number(isAccount))">
            <view class="teaSecLbl">
                <text class="teaSecName">{{ getTransNew('账期支付') }}</text>
                <view class="teaSecRule"></view>
            </view>
            <view class="mallPayment wrapRadius">
                <view class="mallPaymentItem">
                    <view class="mallPaymentItemChei">
                        <view class="fl">
                            <image class="mallLogo " mode="widthFix" :src="imgUrl + 'zqzf.png'"></image>
                            <text class="paymentMethod">{{ getTransNew('账期支付') }}</text>
                        </view>
                        <radio class="radio" value="24" :checked="bianhao === '24'" @click="radioFun('24')"
                            :color="color" style="transform: scale(.8);">
                        </radio>
                    </view>
                    <view class="balance" v-if="accountIsLimit == 1">({{ getTransNew("可用金额") }}{{ currencySymbol() }}{{
                        getRatePrice(availableAmount) }})</view>
                    <view class="balance" v-else>({{ getTransNew("可用金额") }}{{ getTransNew('不限') }})</view>
                </view>
            </view>
        </view>
		<view v-if="isolateShopid=='' && !tonglianSwitch">
			<view class="teaSecLbl">
				<text class="teaSecName">{{  getTransNew('商城支付') }}</text>
				<view class="teaSecRule"></view>
			</view>
			<view class="mallPayment wrapRadius">
				<view>
					<image class="mallLogo" mode="widthFix" :src="mallLogo"></image>
					<text class="paymentMethod oneOverflow">{{  getTransNew('余额支付') }}</text>
					<text class="balance oneOverflow">({{  getTransNew('当前可用金额')}}{{currencySymbol()}}{{ getRatePrice(accountBalance) }})</text>
					<radio class="radio" value="1" :checked="bianhao === '1'" @click="radioFun('1')" :color="color" style="transform: scale(.8);">
					</radio>
				</view>

				<view>
					<image class="mallLogo" mode="widthFix" :src="mallLogo"></image>
					<text class="paymentMethod oneOverflow">{{  getTransNew('充值卡支付') }}</text>
					<text class="balance oneOverflow">({{  getTransNew('当前可用金额') }}{{currencySymbol()}}{{getRatePrice(creditCardBalance)}})</text>
					<radio class="radio" value="2" :checked="bianhao === '2'" @click="radioFun('2')" :color="color" style="transform: scale(.8);">
					</radio>
				</view>
			</view>
		</view>

		<!-- 其他支付 -->
		<view v-if="orderAmount">
			<view class="teaSecLbl" v-if="isolateShopid==''&&(currencyCode=='CNY'||(!isPlusPay && (paypalSwitch||stripeSwitch)))">
				<text class="teaSecName">{{ getTransNew('其他支付')}}</text>
				<view class="teaSecRule"></view>
			</view>
			<view class="otherPayments wrapRadius">
				<view v-if="currencyCode=='CNY'">
					<image mode="widthFix" :src="imgUrl + 'wechatLogo.png'"></image>
					<text class="paymentMethod oneOverflow">{{   getTransNew('微信支付')}}</text>
					<radio class="radio" value="3" :checked="bianhao == '3'" @click="radioFun('3')" :color="color" style="transform: scale(.8);">
					</radio>
				</view>
				<!-- #ifdef APP -->
				<view v-if="currencyCode=='CNY'">
					<image mode="widthFix" :src="imgUrl + 'alipayLogo.png'"></image>
					<text class="paymentMethod oneOverflow">{{  getTransNew("支付宝支付")}}</text>
					<radio class="radio" value="4" :checked="bianhao == '4'" @click="radioFun('4')" :color="color" style="transform: scale(.8);">
					</radio>
				</view>
				<!-- #endif -->
				<!-- #ifdef H5 -->
				<view v-if="isPlusPay && currencyCode=='CNY'">
					<image mode="widthFix" :src="imgUrl + 'alipayLogo.png'"></image>
					<text class="paymentMethod oneOverflow">{{  getTransNew("支付宝支付")}}</text>
					<radio class="radio" value="4" :checked="bianhao == '4'" @click="radioFun('4')" :color="color" style="transform: scale(.8);">
					</radio>
				</view>
				<!-- #endif -->
				<!-- #ifndef MP-WEIXIN -->
				<view v-if="!isPlusPay && paypalSwitch && !tonglianSwitch">
					<image mode="widthFix" src="@/static/pages/images/paypal.png"></image>
					<text class="paymentMethod oneOverflow">PayPal</text>
					<radio class="radio" value="5" :checked="bianhao == '5'" @click="radioFun('5')" :color="color" style="transform: scale(.8);">
					</radio>
				</view>
				<view v-if="!isPlusPay && stripeSwitch && !tonglianSwitch">
					<image mode="widthFix" :src="imgUrl+'stripe.png'"></image>
					<text class="paymentMethod oneOverflow">Stripe</text>
					<radio class="radio" value="7" :checked="bianhao == '7'" @click="radioFun('7')" :color="color" style="transform: scale(.8);">
					</radio>
				</view>
				<!-- #endif -->

			</view>
		</view>


		<view class="teaPageFoot">{{ getTransNew('支付信息经安全通道加密传输') }}</view>
		<!-- 支付按钮：无金额置灰不可点；有金额时防连续点击 -->
		<view
			class="paymentButton"
			:class="{ paymentButtonDisabled: !orderAmount || isPaying }"
			@tap="paymentFun"
		>
			<text>{{getTransNew('支付')}}</text>
		</view>

		<!-- 支付密码弹窗 -->
		<jpPwd ref="jpPwds" :isSetPsw="isSetPassward" @completed="completed"></jpPwd>

		<!-- 微信支付授权 -->
		<!-- <web-view :src="wechatAuthorization"></web-view> -->
		<up-popup :show="isShowQrCode" @close="handleCloseQrCode">
            <view class="qrCodeBox">
                <view class="codeTips">
                    <text>请使用{{ bianhao == 4 ? '支付宝' : '微信' }}扫码完成支付</text>
                </view>
                <QrCode ref="QrCode" :size="400" :content="QrCodeContent"></QrCode> 
			</view>
		</up-popup>
	</view>
</template>

<script>
	import jpPwd from '../../components/jp-pwd/jp-pwd.vue'
	import countdown from "@/components/cz-countdown/cz-countdown.vue"
	import QrCode from '@/components/QrCode/QrCode.vue';
	import {
		querbalanceDisplay,
		quercheckPayPassword,
		querH5andWXuserPay,apiQueryWxPayStatus
	} from "@/api/general/payOrder"
	import {
		quertNavLogo,
		queryPaypal,
		queryAppPaypal,
		querySwitch,
		code2SessionopenId,
		apiGetOpenId,
		apiGetCode,
		queryPayStripe,
		queryAppStripe,
        apiBalanceDisplay,
		plusMemberPay
	} from "@/api/pages/index"
	
	import {
		mallUrl
	} from '@/utils/request.js';
	import JSEncrypt from '@/utils/JSEncrypt';
	// tea 版式：站点风格只认 mall_style 缓存（与购物袋、结算页同一口径）
	import { siteSkinClass, loadSiteSkin } from '@/utils/site-skin.js'
	export default {
		
		components: {
			jpPwd,
            QrCode,
			countdown
		},
		filters: {
			orderTime(time) {
				var time = Number(time);
				var now = new Date(time)
				var nian = now.getFullYear() //获取年份
				var yue = now.getMonth() + 1 //月份
				var tian = now.getDate() // 日
				var hour = now.getHours() // 时
				var minu = now.getMinutes(); //分钟
				var sec = now.getSeconds(); //得到秒
				var ii = `${nian}-${yue}-${tian} ${hour}:${minu}:${sec}`;
				return `${nian}-${yue}-${tian} ${hour}:${minu}:${sec}`
			}
		},
		data() {
			return {
				skinClass: '',
				payTime:'',
				QrCodeContent:'',
				isShowQrCode:false,
			tonglianSwitch: false,
				currencyCode:'',
				userIntegral:0,
				color: '#EEBF41',
				orderGoodsPrice:'',
				orderShippingFee:'',
				paypalSwitch:false,
				stripeSwitch:false,
				rider:false,
				isolateShopid: this.publicVariable.thirdPart.shopId,
				imgUrl: this.publicVariable.imgUrl,
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				url: this.publicVariable.ShoppingPayment,
				orderAmount: '',
				accountBalance: '0.00',
				creditCardBalance: '0.00',
				bianhao: '1',
				serviceProvider: [],
				ordernumber: '',
				tradeName: '',
				paymentPassword: null,
				wechatAuthorization: '',
				type: '',
				totalIntegral: 0,
				mallLogo: '',
				isSetPassward: 0,
				countNone: "",
				down: null,
				lefttime: null,
				leftd: 0,
				lefth: 0,
				leftm: 0,
				lefts: 0,
				isShowTime: false,
				startTime: '',
			    isAccount: null,
				authCode: '',
				payFlag:false,
				/** PLUS 开通支付 */
				isPlusPay: false,
				plusThaliType: 0,
				plusThaliEquity: '',
				isPaying: false
			}
		},
		async onReady() {
			await this.$onLaunched
			var headcolor = this.publicVariable.themeData;
			uni.setNavigationBarColor({
				frontColor: headcolor.headTxColor,
				backgroundColor: headcolor.headBgColor,
			})
			this.temp = '--webColor: ' + this.publicVariable.webColor + ';';
			this.color = this.publicVariable.webColor;
		},
		onLoad(options) {
			if (options.type) {
				this.type = options.type;
			};
			if (String(options.type || '') === 'plus') {
				this.initPlusPay(options);
			}
			if(options.rider){
				this.rider=true;
			}
			if (options.leftTime != 'null' && options.leftTime != null) {
				this.isShowTime = true;
				this.lefttime = options.leftTime;
			} else {}
			if (!this.isPlusPay) {
            	this.initialization(options.number || 0,options.amount || 0,options.fee || 0);
			}
			this.currencyCode=uni.getStorageSync('currencyCode');
		},
		async onShow() {
			this.initSkin();
			// #ifdef H5
			var ua = window.navigator.userAgent.toLowerCase();
			// 判断是否在微信浏览器内
			if (ua.match(/MicroMessenger/i) == "micromessenger") {
				this.handleAuthCode();
			}
			// #endif
			var time = this.getTime();
			this.startTime = time;
			uni.setNavigationBarTitle({
				title: await this.getTransNew("收银台")
			})
			this.fetchData();
			uni.getProvider({
				service: 'payment',
				success: (res) => {
					this.serviceProvider = res.provider
				}
			});
			if(this.isolateShopid){
				// 品牌小程序
				this.bianhao = '3';
			}
			if(this.payFlag){
				uni.showLoading({
					title:"加载中..."
				})
				this.getPalStatus();
			}
		    if (!this.isPlusPay) {
				this.balanceDisplay();
			}
			this.getbalanceDisplay();
			this.configKeySwitch('paypal_switch');
			this.configKeySwitch('stripe_switch');
		    this.configKeySwitch1('tongLian_switch');
		},

		methods: {
			/* ---------------- tea 版式 ---------------- */
			initSkin() {
				this.applySkin();
				loadSiteSkin().then(() => this.applySkin()).catch(() => {});
			},
			applySkin() {
				this.skinClass = siteSkinClass();
			},
			/** 自绘页头返回：没有历史时回首页（收银台常从外部/新窗口打开） */
			navBack() {
				const pages = getCurrentPages();
				if (pages && pages.length > 1) {
					uni.navigateBack();
					return;
				}
				uni.reLaunch({ url: '/pages/index/index' });
			},
			async balanceDisplay() {
                const {
                    code,
                    data
                } = await apiBalanceDisplay({
                    platForm: '2',
                    orderId: this.ordernumber
                });
                if (code == 1) {
                    this.isAccount = data.isAccount
                    this.accountIsLimit = data.accountIsLimit
                    this.availableAmount = data.availableAmount
                    this.accountBalance = data.userMoney
                    this.creditCardBalance = data.userRechargeCard
                    this.userIntegral = data.userIntegral
                }
            },
			// 授权获取code
			async handleAuthCode() {
				var local = encodeURIComponent(window.location.href);
				const {
					code,
					data
				} = await apiGetCode({
					redirect_uri: local
				})
				this.authCode = this.getUrlParam('code')
				if (!this.authCode) {
					// window.location.replace(data);
					window.close();
					this.$safeOpenUrl(data,'_self');
				}
			},
			// 截取url中的code方法
			getUrlParam(name) {
				var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)');
				let url = window.location.href.split('#')[0];
				let search = url.split('?')[1];
				console.log(search);
				if (search) {
					var r = search.substr(0).match(reg);
					if (r !== null) {
						return unescape(r[2]);
					}
					return null;
				} else return null;
			},
			async configKeySwitch(name){
				let { code ,data } = await querySwitch(name);
				if(code == 1){
					if(name=='paypal_switch'){
						this.paypalSwitch = data.configValue == 1 ? true :false;
					}else if(name=='stripe_switch'){
						this.stripeSwitch = data.configValue == 1 ? true :false;
					}
					
				}
			},
		async configKeySwitch1(name) {
			let {
				code,
				data
			} = await querySwitch(name);
			if (code == 1) {
				this.tonglianSwitch = data.configValue == '1' ? true : false;
				this.bianhao = data.configValue == '1' ? '3' : this.bianhao;
			}
		},
			fetchData() {
				quertNavLogo({
					configKey: 'logo_copywriting_ico_pay',
					configType: 'logo_copywriting'
				}).then(res => {
					this.mallLogo = res.data[0].configValue;
				})
			},
			// 获取余额信息
			getbalanceDisplay() {
				querbalanceDisplay({
					platForm: '2',
				}).then(res => {
					if (res.code == 1) {
						this.accountBalance = res.data.userMoney
						this.creditCardBalance = res.data.userRechargeCard
						this.userIntegral=res.data.userIntegral
					}
				})
			},
			// 获取时间
			getTime() {
				var date = new Date(),
					year = date.getFullYear(),
					month = date.getMonth() + 1,
					day = date.getDate(),
					hour = date.getHours() < 10 ? "0" + date.getHours() : date.getHours(),
					minute = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes(),
					second = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
				month >= 1 && month <= 9 ? (month = "0" + month) : "";
				day >= 0 && day <= 9 ? (day = "0" + day) : "";
				var timer = year + '-' + month + '-' + day + ' ' + hour + ':' + minute + ':' + second;
				return timer;
			},
			// 页面加载时获取值
			initialization(number,amount,fee) {

				try {
					const value1 = uni.getStorageSync('orderId');
					const value2 = uni.getStorageSync('goodsName');
					const value3 = uni.getStorageSync('totalPrice');
					const value4 = uni.getStorageSync('totalIntegral');
					const value5 = uni.getStorageSync('orderShippingFee');
					if (value1) {
						this.ordernumber = number*1 || value1;
					}
					if (value2) {
						this.tradeName = value2
					}
					if (value3) {
						this.orderAmount = amount*1 || value3
					}
					if (value4) {
						this.totalIntegral = value4
					}
					this.orderShippingFee= fee*1 || value5 || 0;
					console.log(value3,'---',value5);
					this.orderGoodsPrice=value3-value5;
				} catch (e) {
					// error
				}
			},

			// 选择支付方式
			radioFun(e) {
				this.bianhao = e
			},

			/** 初始化 PLUS 支付上下文 */
			initPlusPay(options = {}) {
				this.isPlusPay = true
				const cached = uni.getStorageSync('plusPayContext') || {}
				const payPrice = Number(options.payPrice != null ? options.payPrice : cached.payPrice)
				const thaliType = Number(options.thaliType != null ? options.thaliType : cached.thaliType)
				let thaliEquity = options.thaliEquity != null ? options.thaliEquity : cached.thaliEquity
				try {
					thaliEquity = thaliEquity != null ? decodeURIComponent(String(thaliEquity)) : ''
				} catch (e) {
					thaliEquity = String(thaliEquity || '')
				}
				this.orderAmount = Number.isFinite(payPrice) ? payPrice : 0
				this.plusThaliType = Number.isFinite(thaliType) ? thaliType : 0
				this.plusThaliEquity = String(thaliEquity || '')
				this.orderShippingFee = 0
				this.orderGoodsPrice = this.orderAmount
				this.isShowTime = false
				this.bianhao = '1'
			},

			/** PLUS 支付公共入参 */
			buildPlusPayParams(extra = {}) {
				return {
					passwd: '',
					payPrice: Number(this.orderAmount || 0),
					thaliEquity: String(this.plusThaliEquity || ''),
					thaliType: Number(this.plusThaliType || 0),
					...extra
				}
			},

			/** PLUS 支付成功跳转 */
			goPlusPaySuccess() {
				try {
					uni.removeStorageSync('plusPayContext')
				} catch (e) {}
				uni.redirectTo({
					url: '/member/plus/plusActivate'
				})
			},

			/** PLUS 第三方支付 type：26微信H5 / 27支付宝H5 / 28微信小程序 / 29支付宝APP / 30微信APP */
			getPlusThirdPayType(channel) {
				// channel: wechat | alipay
				// #ifdef MP-WEIXIN
				if (channel === 'wechat') return 28
				// #endif
				// #ifdef APP-PLUS
				if (channel === 'wechat') return 30
				if (channel === 'alipay') return 29
				// #endif
				// #ifdef H5
				if (channel === 'wechat') return 26
				if (channel === 'alipay') return 27
				// #endif
				return channel === 'alipay' ? 27 : 26
			},

			// 支付按钮
			async paymentFun() {
				if (!this.orderAmount || this.isPaying) return
				this.isPaying = true
				setTimeout(() => {
					this.isPaying = false
				}, 2000)
				// 余额或充值卡支付 // 24 账期支付
				if (this.bianhao == '1' || this.bianhao == '2' || this.bianhao == '24') {
					if (this.isPlusPay && this.bianhao == '24') {
						uni.showToast({ title: await this.getTransNew('PLUS开通不支持账期支付'), icon: 'none' })
						return
					}
					//查询是否已设置支付密码
					quercheckPayPassword().then(res => {
						if (res.code == 1) {
							this.isSetPassward = res.data;
							this.toOpen(this.isSetPassward);
						}
					})
				}


				if (this.bianhao == '3') {
					uni.showLoading();
					// #ifdef APP-PLUS
					uni.getProvider({
						service: 'payment',
						success: async res => {
							uni.showLoading({
								title: await this.getTransNew('正在调起支付')
							});
							

							// 微信支付调用
							querH5andWXuserPay(this.isPlusPay ? this.buildPlusPayParams({
								type: this.getPlusThirdPayType('wechat')
							}) : {
								type: 9,
								orderId: this.ordernumber
							}).then(async res => {
								if(res.code==1){
									let resobj = res.data;
									let payInfo1 = {
										appid: resobj.appid,
										noncestr: resobj.noncestr,
										package: resobj.package,
										partnerid: resobj.partnerid,
										prepayid: resobj.prepayid,
										timestamp: resobj.timestamp,
										sign: resobj.paySign,
									}
									
									if(this.tonglianSwitch && !this.isPlusPay){
										this.tlAppPay(1,resobj);
										return
									}
									uni.requestPayment({
										provider: 'wxpay',
										orderInfo: payInfo1, //微信订单数据（object类型）
										_debug: 1,
										success: res => {
											uni.hideLoading();
											if (this.isPlusPay) {
												this.goPlusPaySuccess()
												return
											}
											uni.redirectTo({
												url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount
											})

										},
										fail: async err => {
                                            console.log("err==",err);
											uni.hideLoading();
											uni.showToast({
												title: await this.getTransNew('支付取消，请稍后再试'),
												icon: 'none'
											})
										}
									});
								}else{
									uni.hideLoading();
									uni.showToast({
										title: await this.getTransNew(res.message),
										icon: 'none'
									})
								}

							})
						}
					});
					// #endif
					// #ifdef H5
					var ua = window.navigator.userAgent.toLowerCase();
					if (ua.match(/MicroMessenger/i) == "micromessenger") {
						const resOpenId = await apiGetOpenId({
							code: this.authCode
						});
						if (resOpenId.code == 1) {
							const resSix = await querH5andWXuserPay(this.isPlusPay ? this.buildPlusPayParams({
								type: this.getPlusThirdPayType('wechat'),
								openId: resOpenId.openid
							}) : {
								type: 22,
								orderId: this.ordernumber,
								shopId: this.isolateShopid,
								openId:resOpenId.openid
							})
							var requestPaymentObj = typeof resSix.data === 'string' ? JSON.parse(resSix.data) : resSix.data;
							// 创建支付参数对象
							var payParams = {
								appId: requestPaymentObj.appId, // 微信公众号的 app ID
								timeStamp: requestPaymentObj.timeStamp, // 支付时间戳
								nonceStr: requestPaymentObj.nonceStr, // 随机字符串
								package: requestPaymentObj.package, // 打包信息
								signType: requestPaymentObj.signType, // 签名类型
								paySign: requestPaymentObj.paySign // 签名
							};
					
							// 调用 getBrandWCPayRequest 函数发起支付请求
							WeixinJSBridge.invoke('getBrandWCPayRequest', payParams, async (
								res) => {
								if (res.err_msg === 'get_brand_wcpay_request:ok') {
									// 支付成功的处理逻辑
									uni.hideLoading();
									if (this.isPlusPay) {
										this.goPlusPaySuccess()
										return
									}
									uni.redirectTo({
										url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount+'&rider='+this.rider
									})
									
								} else {
									// 支付失败或取消支付的处理逻辑
									console.log('Payment failed or canceled!');
									uni.hideLoading();
									uni.showToast({
										title: await this.getTransNew(
											'支付取消，请稍后再试'),
										icon: 'none'
									})
									this.authCode = '';
									uni.navigateBack({
										changed: true
									}); //返回上一页
								}
							});
					
						}
					} else {
						querH5andWXuserPay(this.isPlusPay ? this.buildPlusPayParams({
							type: this.getPlusThirdPayType('wechat')
						}) : {
							type: 8,
							orderId: this.ordernumber
						}).then(async res => {
							if(res.code==1){
								if(this.tonglianSwitch && !this.isPlusPay){
								    this.tlH5Pay(2,res.data);
								    return;
								}
								const jumpUrl = typeof res.data === 'string' ? res.data : (res.data && res.data.url)
								if (jumpUrl) this.$safeOpenUrl(jumpUrl);
								else {
									this.QrCodeContent = typeof res.data === 'string' ? res.data : JSON.stringify(res.data || '')
									this.isShowQrCode = true
								}
							}else{
								uni.showToast({
									title: await this.getTransNew(res.message),
									icon: 'none'
								})
							}
						
						})
					}
					// #endif
					// #ifdef MP-WEIXIN
					uni.login({
						success: res => {
							if (res.code) {
								var wxCode = res.code;
								code2SessionopenId({
									code: wxCode,
									platForm: 6,
									shopId: this.isolateShopid
								}).then(res2=>{
									querH5andWXuserPay(this.isPlusPay ? this.buildPlusPayParams({
										type: this.getPlusThirdPayType('wechat'),
										code: res.code,
										openId: res2.data
									}) : {
										type: 11,
										code: res.code,
										orderId: this.ordernumber,
										shopId: this.isolateShopid,
										openId:res2.data
									}).then(resOne => {
										var requestPaymentObj = typeof resOne.data === 'string' ? JSON.parse(resOne.data) : resOne.data;
										uni.requestPayment({
											timeStamp: requestPaymentObj.timeStamp,
											nonceStr: requestPaymentObj.nonceStr,
											package: requestPaymentObj.package,
											signType: requestPaymentObj.signType,
											paySign: requestPaymentObj.paySign,
											success: (res) => {
												if (this.isPlusPay) {
													this.goPlusPaySuccess()
													return
												}
												uni.redirectTo({
													url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount+'&rider='+this.rider
												})
									
											},
											fail: (res) => {
												uni.redirectTo({
													url: '/trade/payFail/payFail?type='+this.type
												})
											}
										});
									})
								})
								
							}
						}
					});
					// #endif

				}
				// 支付宝支付
				if (this.bianhao == '4') {
					// #ifdef H5
					// 支付宝支付调用
					querH5andWXuserPay(this.isPlusPay ? this.buildPlusPayParams({
						type: this.getPlusThirdPayType('alipay')
					}) : {
						type: 4,
						orderId: this.ordernumber,
					}).then(async res => {
						if (res.code == 1) {
							if(this.tonglianSwitch && !this.isPlusPay){
							    this.tlH5Pay(2,res.data);
							    return;
							}
							if (typeof res.data === 'string') {
								document.write(res.data);
							} else if (res.data && res.data.url) {
								this.$safeOpenUrl(res.data.url)
							}
						}else{
							uni.showToast({
								title: await this.getTransNew(res.message),
								icon: 'none'
							})
						}
					})
					// #endif
					// #ifdef APP-PLUS
					// h5支付宝支付测试通过
					querH5andWXuserPay(this.isPlusPay ? this.buildPlusPayParams({
						type: this.getPlusThirdPayType('alipay')
					}) : {
						type: 5,
						orderId: this.ordernumber,
					}).then(async resOne => {
						if (resOne.code == 1) {
							if(this.tonglianSwitch && !this.isPlusPay){
							    this.tlAppPay(2,resOne.data);
							    return
							}
							uni.requestPayment({
								provider: 'alipay',
								orderInfo: resOne.data, //支付宝订单数据
								success: () => {
									if (this.isPlusPay) {
										this.goPlusPaySuccess()
										return
									}
									uni.redirectTo({
										url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount+'&rider='+this.rider
									})
								},
								fail: () => {
									uni.redirectTo({
										url: '/trade/payfail/payfail?type='+this.type
									})
								}
							});
						}else{
							uni.showToast({
								title: await this.getTransNew(resOne.message),
								icon: 'none'
							})
						}
					})
					// #endif

				}
				
				// PayPal支付
				if (this.bianhao == '5') {
					uni.showLoading({
						title: await this.getTransNew('正在调起支付')
					});
					// #ifdef APP
					queryAppPaypal({
						currencyCode:uni.getStorageSync("currencyCode"),
						orderId:this.ordernumber,
						token:uni.getStorageSync('token')
					}).then(async res=>{
						uni.hideLoading()
						if (res.code == 1) {
							this.paypal(res.data)
						} else {
							uni.showToast({
								title: await this.getTransNew(res.message),
								icon: 'none'
							})
						}
					})
					// #endif
					// #ifdef H5
					queryPaypal({
						currencyCode:uni.getStorageSync("currencyCode"),
						orderId:this.ordernumber,
						url: mallUrl + 'h5/%23/member/orderList/orderList',
						token:uni.getStorageSync('token')
					}).then(async res=>{
						uni.hideLoading()
						if (res.code == 1) {
							this.$safeOpenUrl(res.data);
						} else {
							uni.showToast({
								title: await this.getTransNew(res.message),
								icon: 'none'
							})
						}
					})
					// #endif
				}
				// stripe支付
				if (this.bianhao == '6') {
					uni.showLoading({
						title: await this.getTransNew('正在调起支付')
					});
					// #ifdef APP
					queryAppStripe({
						currencyCode:uni.getStorageSync("currencyCode"),
						orderId:this.ordernumber,
						token:uni.getStorageSync('token')
					}).then(async res=>{
						uni.hideLoading()
						if (res.code == 1) {
							this.stripe(res.data)
						} else {
							uni.showToast({
								title: await this.getTransNew(res.message),
								icon: 'none'
							})
						}
					})
					// #endif
					// #ifdef H5
					queryPayStripe({
						currencyCode:uni.getStorageSync("currencyCode"),
						orderId:this.ordernumber,
						type:2,
						token:uni.getStorageSync('token')
					}).then(async res=>{
						uni.hideLoading()
						if (res.code == 1) {
							this.$safeOpenUrl(res.data);
						} else {
							uni.showToast({
								title: await this.getTransNew(res.message),
								icon: 'none'
							})
						}
					})
					// #endif
				}
			},
			paypal(item) {
				let orderInfo = {
					clientId: item.clientId, //客户端Id
					orderId: item.orderId, //订单id
					amount: this.orderAmount,
					userAction: "continue", //按钮样式 paynow/continue
					currency: item.currency, //币种
					environment: item.mode //运行环境 sandbox(沙盒) / live(线上)
				};
				uni.getProvider({
					service: 'payment',
					success: async (res) => {
						if (~res.provider.indexOf('paypal')) {
							uni.requestPayment({
								provider: "paypal",
								orderInfo: orderInfo,
								success: (response) => {
									uni.redirectTo({
										url: '/member/orderList/orderList?paypalType=1&tokenId=' +
											item.orderId
									})
								},
								fail: (err) => {
									uni.redirectTo({
										url: '/member/orderList/orderList?paypalType=0'
									})
								}
							});
						} else {
							uni.showToast({
								title: await this.getTransNew('抱歉，当前您的系统暂不支持使用Paypal支付。'),
								icon: 'none'
							})
						}
					}
				});
			},
			stripe(item) {
				let orderInfo = {
					merchantName: item.merchantName,
					publishKey: item.publishKey, //客户端Id
					paymentIntent: item.clientSecret //订单id
				};
				uni.getProvider({
					service: 'payment',
					success: async (res) => {
						if (~res.provider.indexOf('stripe')) {
							uni.requestPayment({
								provider: "stripe",
								orderInfo: orderInfo,
								success: (response) => {
									uni.redirectTo({
									  url: '/trade/paySuccess/paySuccess?paypalType=2&orderId=' + item.orderId+'&total='+this.orderAmount
									})
								},
								fail: (err) => {
									uni.redirectTo({
										url: '/member/orderList/orderList?status=4&paypalType=0'
									})
								}
							});
						} else {
							uni.showToast({
								duration:2500,
								title: await this.getTransNewAsync('抱歉，当前您的系统暂不支持使用stripe支付。'),
								icon: 'none'
							})
						}
					}
				});
			},
			// 密码输入框
			toOpen(ispsw) {
				this.$refs.jpPwds.toOpen(ispsw);
			},
            // 账期支付
            async toAccountPay(Password2) { 
                if (this.orderAmount * 1 <= this.availableAmount * 1) {
                    if (this.orderAmount * 1 <= this.availableAmount * 1) {

                    }else{
                        uni.showToast({
							title: await this.getTransNew('账期余额不足'),
							icon: 'none'
						})
                        return false;
                    }
                }
                const resTen = await querH5andWXuserPay({
                    platForm: '2',
                    passwd: Password2,
                    orderIdList: this.ordernumber,
                    type: 24
                })
                if (resTen.code == 1) {
                    uni.redirectTo({
                            url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount+'&rider='+this.rider
                        })
                } else {
                    uni.showToast({
                        title: await this.getTransNew(resTen.message),
                        icon: 'none'
                    })
                }
            },
			async completed(e) {
				this.paymentPassword = e
				this.$refs.jpPwds.toCancel()
				// 支付密码按明文提交（HTTPS 传输）：本部署未配置 RSA 私钥，
				// 支付服务的 BCrypt 校验只认明文，前端做 RSA 会导致密码恒判「错误」。
				var Password2 = this.paymentPassword;
				// PLUS：余额 / 购物卡走 plusMemberpay
				if (this.isPlusPay && (this.bianhao == '1' || this.bianhao == '2')) {
					const balance = this.bianhao == '1' ? this.accountBalance : this.creditCardBalance
					if (this.orderAmount * 1 > balance * 1) {
						uni.showToast({
							title: await this.getTransNew(this.bianhao == '1' ? '账户余额不足' : '购物卡余额不足'),
							icon: 'none'
						})
						return
					}
					try {
						const res = await plusMemberPay(this.buildPlusPayParams({
							passwd: Password2,
							type: this.bianhao == '2' ? 2 : 1
						}))
						if (res.code == 1) {
							this.goPlusPaySuccess()
						} else {
							uni.showToast({
								title: await this.getTransNew(res.message || '支付失败'),
								icon: 'none'
							})
						}
					} catch (err) {
						uni.showToast({
							title: await this.getTransNew('支付失败'),
							icon: 'none'
						})
					}
					return
				}
				// 余额支付
				if (this.bianhao == '1') {
					if (this.orderAmount * 1 <= this.accountBalance * 1) {
						querH5andWXuserPay({
							platForm: '2',
							passwd: Password2,
							orderIdList: this.ordernumber,
							type: 1
						}).then(async res => {
							if (res.code == 1) {
								uni.redirectTo({
									url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount+'&rider='+this.rider
								})
							} else {
								uni.showToast({
									title: await this.getTransNew(res.message),
									icon: 'none'
								})
							}
						})
					} else {
						uni.showToast({
							title: await this.getTransNew('账户余额不足'),
							icon: 'none'
						})
					}
				}

				// 充值卡支付
				if (this.bianhao == '2') {
					if (this.orderAmount * 1 <= this.creditCardBalance * 1) {
						querH5andWXuserPay({
							platForm: '2',
							passwd: Password2,
							orderIdList: this.ordernumber,
							type: 2
						}).then(async res => {
							if (res.code == 1) {
								uni.redirectTo({
									url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount+'&rider='+this.rider
								})
							} else{
								uni.showToast({
									title: await this.getTransNew(res.message),
									icon: 'none'
								})
							}
						})
					} else {
						uni.showToast({
							title: await this.getTransNew('账户余额不足'),
							icon: 'none'
						})
					}
				}

                if (this.bianhao == '24') {
                    this.toAccountPay(Password2)
			    }
			},
            /**
             * 通联H5支付
             * @param {Number} type 支付类型 1 微信浏览器 2 其他浏览器
             * @param {String} url 支付跳转链接
             */
            tlH5Pay( type, url){
				this.getPalStatus();
				let APPID = 'wx8639411a560926cc';
				let PATH = 'trade/paymentPage/paymentPage';
				let QUERY = `number=${this.ordernumber}&amount=${this.orderAmount}&fee=${this.orderShippingFee}`;
				window.location.href = `weixin://dl/business/?appid=${APPID}&path=${PATH}&query=${QUERY}&env_version=develop`
				uni.hideLoading();
            },
			/**
			 * 通联App支付
			 * @param {Number} type 支付类型 1 微信 2 支付宝
			 * @param {Object} params 支付参数
			 */
            tlAppPay(type,params = {}){
				console.log("通联app支付：",type,params);
				this.payFlag = true;
                uni.hideLoading();
				let pathStr = '';
				let paramsParse = typeof params === 'string' ? JSON.parse(params) : {};
				for(let key in paramsParse){
					pathStr += `${key}=${paramsParse[key]}&`
				}
				console.log(type,params,pathStr);
				if(type === 1){
					try{
						plus.share.getServices((data) => {
								let sweixin = null;
								data.forEach(item=>{
									if(item.id==="weixin"){
									  sweixin = item
									}
								})
								if (sweixin) {
									// 跳转到微信小程序
									sweixin.launchMiniProgram({
										id: "gh_e64a1a89a0ad", //微信小程序原生id
										path: `pages/orderDetail/orderDetail?${pathStr}`,
										// 可取值：0-正式版； 1-测试版； 2-体验版。默认值为0。
										type: 0, // 微信小程序版本类型，
									});
								} else {
									uni.showToast({
										title: "无法打开微信，请检查是否已安装",
										icon: "none",
									});
								}
							},
							(err) => {
								console.log("跳转失败",err);
							}
						);
					}catch(error){
						console.log("APP微信调用失败：",error);
					}
				}else{
					try{
						const query = encodeURIComponent("payinfo=" + encodeURIComponent(params));
						const thirdPartSchema = encodeURIComponent("unimallbtoc://");
						const alipayUrl = `alipays://platformapi/startapp?appId=2021001104615521&page=pages/orderDetail/orderDetail&thirdPartSchema=${thirdPartSchema}&query=${query}`;
						// console.log("alipayUrl",alipayUrl);
						plus.runtime.openURL(alipayUrl, (error) => {
						    console.error('无法打开支付宝，请检查是否已安装', error);
						    uni.showToast({ title: '无法打开支付宝，请检查是否已安装', icon: 'none' });
						});
					}catch(error){
						console.log("APP支付包调用失败：",error);
					}
				}
            },
            // 关闭二维码弹窗
            handleCloseQrCode(){
                this.isShowQrCode = false;
                this.QrCodeContent = '';
                this.payTime && clearInterval(this.payTime);
            },
            // 查询支付状态
            getPalStatus(){
                this.payTime = setInterval(async ()=>{
                    const res = await apiQueryWxPayStatus({
                        orderId:this.ordernumber
                    });
                    if(res.code == 1){
                        this.isShowQrCode = false;
                        this.QrCodeContent = '';
                        this.payTime && clearInterval(this.payTime);
                        uni.redirectTo({
                            url: '/trade/paySuccess/paySuccess?type='+this.type+'&total='+this.orderAmount+'&rider='+this.rider
                        })
                    }else{
						if(this.payFlag){
							this.payTime && clearInterval(this.payTime);
							uni.redirectTo({
							    url: '/trade/payFail/payFail?type='+this.type
							})
						}
					}
                },1000)
            }
		},
        onUnload(){
            this.payTime && clearInterval(this.payTime);
        },
        onHide(){
            this.payTime && clearInterval(this.payTime);
        }
	}
</script>

<style lang='scss'>
	@import url("./paymentPage.css");

	.qrCodeBox {
		width: 100%;
	    display: flex;
	    flex-direction: column;
	    justify-content: center;
	    align-items: center;
	    margin: 20rpx 0 30rpx;
	}
	
	.codeTips{
	    margin: 20rpx 0;
	}
	
</style>
