package com.houjiang.commons.util;

import com.houjiang.commons.config.WXPayConfig;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Amos
 */
public class PayCommonUtil {
	/** xml解析 */
	public static Map doXMLParse(String strxml) throws JDOMException, IOException {
		strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");

		if (null == strxml || "".equals(strxml)) {
			return null;
		}

		Map m = new HashMap();

		InputStream in = new ByteArrayInputStream(strxml.getBytes(StandardCharsets.UTF_8));
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(in);
		Element root = doc.getRootElement();
		List list = root.getChildren();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Element e = (Element) it.next();
			String k = e.getName();
			String v = "";
			List children = e.getChildren();
			if (children.isEmpty()) {
				v = e.getTextNormalize();
			} else {
				v = getChildrenText(children);
			}

			m.put(k, v);
		}

		// 关闭流
		in.close();

		return m;
	}

	/**
	 * 验证回调签名
	 *
	 * @return
	 */
	public static boolean isTenpaySign(Map<String, String> map) {
		String characterEncoding = "utf-8";
		String charset = "utf-8";
		String signFromAPIResponse = map.get("sign");
		if (signFromAPIResponse == null || signFromAPIResponse.equals("")) {
			System.out.println("API返回的数据签名数据不存在，有可能被第三方篡改!!!");
			return false;
		}
		System.out.println("服务器回包里面的签名是:" + signFromAPIResponse);
		// 过滤空 设置 TreeMap
		SortedMap<String, String> packageParams = new TreeMap();

		for (String parameter : map.keySet()) {
			String parameterValue = map.get(parameter);
			String v = "";
			if (null != parameterValue) {
				v = parameterValue.trim();
			}
			packageParams.put(parameter, v);
		}

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
		sb.append("key=" + WXPayConfig.API_KEY);
		System.out.println("isTenpaySign方法中的 WXPayConfig.API_KEY值==" + WXPayConfig.API_KEY);
		System.out.println("isTenpaySign方法中的 WXPayConfig.MCH_ID值==" + WXPayConfig.MCH_ID);
		// 将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
		// 算出签名
		String resultSign = "";
		String tobesign = sb.toString();

		if (null == charset) {
			resultSign = MD5Util.MD5Encode(tobesign, characterEncoding).toUpperCase();
		} else {
			try {
				resultSign = MD5Util.MD5Encode(tobesign, characterEncoding).toUpperCase();
			} catch (Exception e) {
				resultSign = MD5Util.MD5Encode(tobesign, characterEncoding).toUpperCase();
			}
		}

		String tenpaySign = packageParams.get("sign").toUpperCase();
		return tenpaySign.equals(resultSign);
	}

	/**
	 * 验证回调签名小程序
	 *
	 * @param map
	 * @return
	 */
	public static boolean isTenpaySignApplet(Map<String, String> map) {
		String characterEncoding = "utf-8";
		String charset = "utf-8";
		String signFromAPIResponse = map.get("sign");
		if (signFromAPIResponse == null || signFromAPIResponse.equals("")) {
			System.out.println("API返回的数据签名数据不存在，有可能被第三方篡改!!!");
			return false;
		}
		System.out.println("服务器回包里面的签名是:" + signFromAPIResponse);
		// 过滤空 设置 TreeMap
		SortedMap<String, String> packageParams = new TreeMap<>();

		for (String parameter : map.keySet()) {
			String parameterValue = map.get(parameter);
			String v = "";
			if (null != parameterValue) {
				v = parameterValue.trim();
			}
			packageParams.put(parameter, v);
		}

		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();

		for (Object o : es) {
			Map.Entry entry = (Map.Entry) o;
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		sb.append("key=" + WXPayConfig.API_KEY);

		// 将API返回的数据根据用签名算法进行计算新的签名，用来跟API返回的签名进行比较
		// 算出签名
		String resultSign = "";
		String tobesign = sb.toString();

		if (null == charset || "".equals(charset)) {
			resultSign = MD5Util.MD5Encode(tobesign, characterEncoding).toUpperCase();
		} else {
			try {
				resultSign = MD5Util.MD5Encode(tobesign, characterEncoding).toUpperCase();
			} catch (Exception e) {
				resultSign = MD5Util.MD5Encode(tobesign, characterEncoding).toUpperCase();
			}
		}

		String tenpaySign = packageParams.get("sign").toUpperCase();
		return tenpaySign.equals(resultSign);
	}

	public static String getChildrenText(List children) {
		StringBuffer sb = new StringBuffer();
		if (!children.isEmpty()) {
			Iterator it = children.iterator();
			while (it.hasNext()) {
				Element e = (Element) it.next();
				String name = e.getName();
				String value = e.getTextNormalize();
				List list = e.getChildren();
				sb.append("<" + name + ">");
				if (!list.isEmpty()) {
					sb.append(getChildrenText(list));
				}
				sb.append(value);
				sb.append("</" + name + ">");
			}
		}

		return sb.toString();
	}

}
