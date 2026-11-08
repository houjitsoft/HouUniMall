import {
	mallUrl,
	mallImgUrl
} from './request.js';

/**
 * 商城风格的本地缓存键。
 *
 * <p>各页面在 onLoad / data() 里第一时间读 publicVariable.themeData 去建导航栏，
 * 而真正的网站风格要等 /web/mall/index/config 回来才拿到。不做缓存的话，
 * 首屏导航栏永远是 uni_color 或硬编码默认色（绿 #449210 / 黄 #F8CE3A），
 * 后台「主题风格」换成什么都不生效。
 */
export const MALL_THEME_CACHE_KEY = 'houcloud_mall_theme'

/**
 * 读取上一次落地的商城风格。
 *
 * @returns {Object} 形如 { webColor, subColor, contMxColor, headBgColor, headTxColor }；没有缓存时为空对象
 */
function readCachedMallTheme() {
	try {
		const cached = uni.getStorageSync(MALL_THEME_CACHE_KEY)
		return cached && typeof cached === 'object' ? cached : {}
	} catch (e) {
		return {}
	}
}

const cachedMallTheme = readCachedMallTheme()

export const publicVariable = {
	userInfo: null,
	// 正式数据库
	url: mallUrl,
	imgUrl: mallImgUrl,
	oAuth: [],
	webColor: cachedMallTheme.webColor || '',
	subColor: cachedMallTheme.subColor || '',
	themeData: {
		// 无缓存时的兜底配色，对齐站点当前使用的 tea 风格（暖象牙 / 墨绿 / 茶褐金）。
		// 原来的 #F8CE3A 亮黄会在首次访问、风格接口尚未回来时把导航头刷成黄色，
		// 与页面主题明显割裂；这里改成中性米色，接口回来后再由 applyMallStyleTheme 覆盖。
		contFsColor: cachedMallTheme.contFsColor || "#1d2420",
		contMxColor: cachedMallTheme.contMxColor || "#b0805a",
		contRadio: 2,
		headBgColor: cachedMallTheme.headBgColor || "#f4f1ea",
		headRadio: 2,
		headTxColor: cachedMallTheme.headTxColor || "#221e17"
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
		shopId: ''
	},
	riderIsOpen: false,
	/** 周期切换全局起始日，可由接口初始化时通过 setPeriodStartDate 写入 */
	periodStartDate: '',
	getCurrentPages: function() {
		var pages = getCurrentPages(); //获取加载的页面
		var currentPage = pages[pages.length - 1]; //获取当前页面的对象
		var url = currentPage.route; //当前页面url
		var options = currentPage.options; //获取url中所带的参数
		//拼接url的参数
		var currentPage = url + '?';
		for (var key in options) {
			var value = options[key];
			currentPage += key + '=' + value + '&';
		}
		currentPage = currentPage.substring(0, currentPage.length - 1);
		return currentPage;
	}
}