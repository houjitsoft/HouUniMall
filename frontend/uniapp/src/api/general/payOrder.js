import {
	baseRequest,
	paymentRequest,
	usermsRequest,
	authServerRequest,
	businessRequest,
	goodsRequest,
	thirdPartyRequest,
	orderRequest,
	activityRequest,
	gatewayRequest,
	commonsRequest
} from '@/utils/request.js';

// 检测是否所有商品都在发货区域内
export function queryTwo(data) {
	return businessRequest('/shop/transportArea/queryTwo', data,'post')
}

// 计算价格
export function queryCalculatePrice(data) {
	return orderRequest('/order/calculatePrice', data,'post')
}

// 查询订单商家是否开通货到付款功能 
export function queryIsDelivery(data) {
	return businessRequest('/shop/cashOnDelivery/queryIsDelivery', data,'post')
}

// 获取会员折扣
export function queryPersonalDiscount(data) {
	return usermsRequest('/user/ms/queryPersonalDiscount', data,'post')
}

// 查询是否有可用的优惠券
export function selectavailablevcbase(data) {
	return activityRequest('/activity/voucherBase/selectavailablevcbase', data,'post')
}

// 提交创建订单
export function querappCreateOrders(data) {
	return orderRequest('/order/appCreateOrders', data,'post',false,true)
}

// 发票
export function querbatch(data) {
	return orderRequest('/orderInvoiceInfo/apply/invoice/batch', data,'post')
}

// 查询是否有可用的红包(积分商城)
export function selectavaliablerpbase(data) {
	return activityRequest('/activity/redpacketBase/selectByDeposit', data,'post')
}

// 查询账户余额
export function querbalanceDisplay(data) {
	return paymentRequest('/payment/userResource/balanceDisplay', data,'post')
}

// 查询是否已设置支付密码
export function quercheckPayPassword(data) {
	return paymentRequest('/pay/userBase/checkPayPassword', data,'post')
}

/** ========== 统一收银台（BFF web/pay/cashier/*） ========== */
let lastCashierNo = ''

export function cashierCreate(data) {
	return paymentRequest('/cashier/create', data, 'post')
}

export function cashierStatus(data) {
	return paymentRequest('/cashier/status', data, 'post')
}

export function cashierClose(data) {
	return paymentRequest('/cashier/close', data, 'post')
}

export function getLastCashierNo() {
	return lastCashierNo
}

/**
 * 将历史 entrance/userPay 的 type 映射到收银台 terminal + payMethod。
 * 未映射的 type（余额/卡/账期等）继续走 legacy entrance。
 */
function mapLegacyTypeToCashier(data = {}) {
	const type = Number(data.type)
	const amount = Number(
		data.amount ?? data.payPrice ?? data.money ?? data.orderPaymentAmount ?? 0
	)
	const mallOrderNo = data.orderId || data.mallOrderNo || data.orderIdList || ''
	const openId = data.openId || data.openid || ''
	const base = {
		amount,
		mallOrderNo: mallOrderNo || undefined,
		mchOrderNo: mallOrderNo || undefined,
		requestNo: mallOrderNo ? `m-${type}-${mallOrderNo}` : undefined,
		subject: data.subject || (data.thaliType != null ? 'PLUS会员开通' : '商城订单支付'),
		currency: data.currency || 'CNY',
		openId: openId || undefined,
		channelCode: data.channelCode || undefined,
		returnUrl: data.returnUrl || data.url || undefined
	}
	if (!(base.amount > 0) && data.payPrice) {
		base.amount = Number(data.payPrice)
	}

	// WeChat family
	const wechatTypes = new Set([7, 8, 9, 10, 11, 22, 26, 28, 30])
	// Alipay family
	const alipayTypes = new Set([3, 4, 5, 6, 27, 29])
	// Stripe
	const stripeTypes = new Set([15])

	if (wechatTypes.has(type)) {
		let terminal = 'WAP'
		if (type === 7 || type === 10) terminal = 'PC'
		else if (type === 9 || type === 30) terminal = 'APP'
		else if (type === 11 || type === 28) terminal = 'WX_MINI'
		else if (type === 22 || type === 26 || type === 8) terminal = 'WAP'
		return { ...base, terminal, payMethod: 'WECHAT' }
	}
	if (alipayTypes.has(type)) {
		let terminal = 'WAP'
		if (type === 3 || type === 6) terminal = 'PC'
		else if (type === 29 || type === 4) terminal = 'APP'
		else terminal = 'WAP'
		return { ...base, terminal, payMethod: 'ALIPAY' }
	}
	if (stripeTypes.has(type)) {
		return { ...base, terminal: data.terminal || 'WAP', payMethod: 'STRIPE' }
	}
	return null
}

/** 将收银台 payData 适配成历史 userPay 返回结构，尽量少改 paymentPage.vue */
function adaptCashierPayData(createData = {}) {
	const payData = createData.payData || {}
	const packageParams = payData.packageParams || {}
	// JSAPI / 小程序
	if (packageParams && (packageParams.appId || packageParams.package || packageParams.paySign)) {
		return {
			...packageParams,
			appId: packageParams.appId || packageParams.appid,
			appid: packageParams.appid || packageParams.appId,
			timeStamp: packageParams.timeStamp || packageParams.timestamp,
			timestamp: packageParams.timestamp || packageParams.timeStamp,
			nonceStr: packageParams.nonceStr || packageParams.noncestr,
			noncestr: packageParams.noncestr || packageParams.nonceStr,
			package: packageParams.package,
			signType: packageParams.signType || 'RSA',
			paySign: packageParams.paySign || packageParams.sign,
			sign: packageParams.sign || packageParams.paySign,
			cashierNo: createData.cashierNo,
			channel: payData.channel
		}
	}
	// Native / H5 URL
	if (payData.codeUrl || payData.payUrl || payData.url || payData.checkoutUrl) {
		const url = payData.codeUrl || payData.payUrl || payData.url || payData.checkoutUrl
		return { url, codeUrl: payData.codeUrl, payUrl: payData.payUrl || payData.checkoutUrl, cashierNo: createData.cashierNo, channel: payData.channel, ...payData }
	}
	// Alipay form
	if (payData.formHtml) {
		return { formHtml: payData.formHtml, cashierNo: createData.cashierNo, channel: payData.channel }
	}
	if (payData.orderString || payData.orderStr) {
		return { orderStr: payData.orderString || payData.orderStr, cashierNo: createData.cashierNo, channel: payData.channel }
	}
	return { ...payData, cashierNo: createData.cashierNo }
}

// 微信支付调用&h5微信支付测试通过&小程序支付前 授权&支付订单
// 第三方渠道改走统一收银台；余额/卡/账期等仍走 legacy entrance
export function querH5andWXuserPay(data) {
	const mapped = mapLegacyTypeToCashier(data || {})
	if (!mapped) {
		return paymentRequest('/pay/entrance/userPay', data, 'post')
	}
	if (!(mapped.amount > 0)) {
		return Promise.resolve({ code: 0, message: '支付金额无效，无法创建收银台订单', data: null })
	}
	return cashierCreate(mapped).then((res) => {
		if (res && res.code === 1 && res.data) {
			lastCashierNo = res.data.cashierNo || ''
			try {
				uni.setStorageSync('lastCashierNo', lastCashierNo)
			} catch (e) {}
			const adapted = adaptCashierPayData(res.data)
			return {
				...res,
				data: adapted,
				cashierNo: lastCashierNo
			}
		}
		return res
	})
}

// 查询微信\支付宝支付状态 → 优先收银台 status（cashierNo）
export function apiQueryWxPayStatus(data = {}) {
	let cashierNo = data.cashierNo || lastCashierNo
	if (!cashierNo) {
		try {
			cashierNo = uni.getStorageSync('lastCashierNo') || ''
		} catch (e) {}
	}
	if (cashierNo) {
		return cashierStatus({ cashierNo }).then((res) => {
			if (res && res.code === 1 && res.data) {
				const st = res.data.status
				const code = res.data.statusCode
				const ok = st === 2 || code === 'SUCCESS'
				// 历史约定：code===1 表示已支付成功
				return {
					...res,
					code: ok ? 1 : 0,
					message: ok ? (res.message || '支付成功') : (res.data.statusDesc || res.message || '待支付'),
					data: ok ? 1 : 0,
					statusCode: code,
					cashierNo
				}
			}
			return res
		})
	}
	return paymentRequest('/pay/WXPay/queryWxPayStatus', data, 'post')
}
