package com.houjiang.commons.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class PayToolUtil {

	private static Logger log = LoggerFactory.getLogger(PayToolUtil.class);

	private static final int BLACK = 0xff000000;
	private static final int WHITE = 0xFFFFFFFF;

	/** 金额为分的格式 */
	public static final String CURRENCY_FEN_REGEX = "\\-?[0-9]+";

	/**
	 * 是否签名正确,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 *
	 * @return boolean
	 */
	public static boolean isTenpaySign(String characterEncoding, SortedMap<Object, Object> packageParams,
			String API_KEY) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		sb.append("key=" + API_KEY);

		// 算出摘要
		//String mysign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toLowerCase();
		String mysign =PasswordUtils.BCryptPasswordEncoder(sb.toString());
		String tenpaySign = ((String) packageParams.get("sign")).toLowerCase();
		// System.out.println(tenpaySign + " " + mysign);
		return tenpaySign.equals(mysign);
	}

	/**
	 * @author Amos
	 * @date 2016-4-22
	 * @Description：sign签名
	 * @param characterEncoding
	 *            编码格式
	 * @param packageParams
	 *            请求参数
	 * @return
	 */
	public static String createSign(String characterEncoding, SortedMap<Object, Object> packageParams, String API_KEY) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + API_KEY);
		//String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
		String sign =PasswordUtils.BCryptPasswordEncoder(sb.toString());
		return sign;
	}

	/**
	 * @author Amos
	 * @date 2016-4-22
	 * @Description：将请求参数转换为xml格式的string
	 * @param parameters
	 *            请求参数
	 * @return
	 */
	public static String getRequestXml(SortedMap<Object, Object> parameters) {
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
				// sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k +
				// ">");
				sb.append("<" + k + ">" + v + "</" + k + ">");
			} else {
				sb.append("<" + k + ">" + v + "</" + k + ">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}

	/**
	 * 取出一个指定长度大小的随机正整数.
	 *
	 * @param length
	 *            int 设定所取出随机数的长度。length小于11
	 * @return int 返回生成的随机数。
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}

	/**
	 * 获取当前时间 yyyyMMddHHmmss
	 *
	 * @return String
	 */
	public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}

	/**
	 * 获取本机IP地址
	 *
	 * @author Amos
	 * @return
	 */
	public static String localIp() {
		String ip = null;
		Enumeration allNetInterfaces;
		try {
			allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
				List<InterfaceAddress> InterfaceAddress = netInterface.getInterfaceAddresses();
				for (InterfaceAddress add : InterfaceAddress) {
					InetAddress Ip = add.getAddress();
					if (Ip != null && Ip instanceof Inet4Address) {
						ip = Ip.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			log.warn("获取本机Ip失败:异常信息:" + e.getMessage());
		}
		return ip;
	}


	/** 生成随机数 */
	public static String create_nonce_str() {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String res = "";
		for (int i = 0; i < 16; i++) {
			Random rd = new Random();
			res += chars.charAt(rd.nextInt(chars.length() - 1));
		}
		return res;
	}

	/**
	 * 元转换成分
	 *
	 * @param amount
	 * @return
	 */
	public static String getMoney(String amount) {
		if (amount == null) {
			return "";
		}
		// 金额转化为分为单位
		// 处理包含, ￥ 或者$的金额
		String currency = amount.replaceAll("\\$|\\￥|\\,", "");
		int index = currency.indexOf(".");
		int length = currency.length();
		Long amLong = 0L;
		if (index == -1) {
			amLong = Long.valueOf(currency + "00");
		} else if (length - index >= 3) {
			amLong = Long.valueOf((currency.substring(0, index + 3)).replace(".", ""));
		} else if (length - index == 2) {
			amLong = Long.valueOf((currency.substring(0, index + 2)).replace(".", "") + 0);
		} else {
			amLong = Long.valueOf((currency.substring(0, index + 1)).replace(".", "") + "00");
		}
		return amLong.toString();
	}

	/**
	 * 将分为单位的转换为元 （除100）
	 *
	 * @param amount
	 * @return
	 * @throws Exception
	 */
	public static String changeF2Y(String amount) throws Exception {
		if (!amount.matches(CURRENCY_FEN_REGEX)) {
			throw new Exception("金额格式有误");
		}
		return BigDecimal.valueOf(Long.valueOf(amount)).divide(new BigDecimal(100)).toString();
	}

	/**
	 * 判断是否为整数
	 *
	 * @param str
	 * @return 是整数返回true,否则返回false
	 */
	public static boolean isInteger(String str) {
		String regex = "^[-\\+]?[\\d]*$";
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(str).matches();
	}


	/**
	 * 获取用户实际ip
	 *
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		// 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址
		String headerName = "x-forwarded-for";
		String ip = request.getHeader(headerName);
		if (null != ip && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个IP值，第一个IP才是真实IP,它们按照英文逗号','分割
			if (ip.indexOf(",") != -1) {
				ip = ip.split(",")[0];
			}
		}
		if (checkIp(ip)) {
			headerName = "Proxy-Client-IP";
			ip = request.getHeader(headerName);
		}
		if (checkIp(ip)) {
			headerName = "WL-Proxy-Client-IP";
			ip = request.getHeader(headerName);
		}
		if (checkIp(ip)) {
			headerName = "HTTP_CLIENT_IP";
			ip = request.getHeader(headerName);
		}
		if (checkIp(ip)) {
			headerName = "HTTP_X_FORWARDED_FOR";
			ip = request.getHeader(headerName);
		}
		if (checkIp(ip)) {
			headerName = "X-Real-IP";
			ip = request.getHeader(headerName);
		}
		if (checkIp(ip)) {
			headerName = "remote addr";
			ip = request.getRemoteAddr();
			// 127.0.0.1 ipv4, 0:0:0:0:0:0:0:1 ipv6
			if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
				//根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ip = inet.getHostAddress();
			}
		}
		log.info("getClientIp  IP is " + ip + ", headerName = " + headerName);
		return ip;
	}
	private static boolean checkIp(String ip) {
		if (null == ip || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			return true;
		}
		return false;
	}

	/**
	 * 生成指定长度的随机数字字符串。
	 * @param length 数字字符串的长度
	 * @return 长度为length的随机数字字符串
	 */
	public static String generateRandomNumber(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();

		// 确保第一个数字不是0，以避免生成小于指定长度的有效数字
		// 从1到9选择
		int firstDigit = 1 + random.nextInt(9);
		sb.append(firstDigit);

		for (int i = 1; i < length; i++) {
			// 对于其余位，可以从0到9任意选择
			sb.append(random.nextInt(10));
		}

		return sb.toString();
	}
}
