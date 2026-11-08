package com.houjiang.commons.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Amos
 *
 */
public class MathUtil {
	private static Pattern patternNumeric = Pattern.compile("[0-9]*");
	private static Pattern patternInteger = Pattern.compile("^[-\\+]?[\\d]*$");
	private static Pattern patternMoney = Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$");
	/**
	 * 判断纯数字
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Matcher isNum = patternNumeric.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 判断是否为整数
	 *
	 * @param str
	 * @return 是整数返回true,否则返回false
	 */
	public static boolean isInteger(String str) {
		return patternInteger.matcher(str).matches();
	}

	/**
	 * list转String
	 *
	 * @param mList
	 * @return
	 */
	/** 采用Stringbuilder.append()的方式追加 */
	public static String listToString2(List<String> mList) {
		final String SEPARATOR = ",";
		// mList = Arrays.asList("AAA", "BBB", "CCC");
		StringBuilder sb = new StringBuilder();
		String convertedListStr = "";
		if (null != mList && mList.size() > 0) {
			for (String item : mList) {
				sb.append(item);
				sb.append(SEPARATOR);
			}
			convertedListStr = sb.toString();
			convertedListStr = convertedListStr.substring(0, convertedListStr.length() - SEPARATOR.length());
			return convertedListStr;
		} else {
            return "List is null!!!";
        }
	}

	/**
	 * 判断字符串是否是金额
	 * @param str
	 * @return
	 */
	public static boolean isMoney(String str){
		// 判断小数点后2位的数字的正则表达式
		Matcher match=patternMoney.matcher(str);
		if(match.matches()==false){
			return false;
		}
		else{
			return true;
		}
	}
	/**
	 * 随机获取两位数
	 *
	 * @return
	 */
	public static int randomLengthByTwo() {
		int random = (int) (Math.random() * 90 + 10);
		return random;
	}

	public static boolean checkNumber(String number) {
		String reg = "\\d+(\\.\\d+)?";
		return number.matches(reg);
	}

	public static boolean checkSendBack(String sendBack) {
		if ("是".equals(sendBack)){
			return true;
		}else if ("否".equals(sendBack)){
			return true;
		}else {
			return false;
		}

	}
}
