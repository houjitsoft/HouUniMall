package com.houjiang.commons.util;


import com.houjiang.commons.dto.goods.UserShoppingCartDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Amos
 *
 */
public class UnicodeUtils {

	private static Logger logger = LoggerFactory.getLogger(UnicodeUtils.class);

	/**
	 * 把中文转成Unicode码
	 *
	 * @param str
	 * @return
	 */
	public static String chinaToUnicode(String str) {
		String result = "";
		for (int i = 0; i < str.length(); i++) {
			int chr1 = (char) str.charAt(i);
			// 汉字范围 \u4e00-\u9fa5 (中文)
			if (chr1 >= 19968 && chr1 <= 171941) {
				result += "\\u" + Integer.toHexString(chr1);
			} else {
				result += str.charAt(i);
			}
		}
		return result;
	}

	/**
	 * 判断是否为中文字符
	 *
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unused")
	private static String asciiToNative(String asciicode) {
		String[] asciis = asciicode.split("\\\\u");
		String nativeValue = asciis[0];
		try {
			for (int i = 1; i < asciis.length; i++) {
				String code = asciis[i];
				nativeValue += (char) Integer.parseInt(code.substring(0, 4), 16);
				if (code.length() > 4) {
					nativeValue += code.substring(4, code.length());
				}
			}
		} catch (NumberFormatException e) {
			return asciicode;
		}
		return nativeValue;
	}

	/**
	 * 查询是否包含表情
	 *
	 * @param source
	 * @return
	 */
	public static boolean containsEmoji(String source) {
		int len = source.length();
		boolean isEmoji = false;
		for (int i = 0; i < len; i++) {
			char hs = source.charAt(i);
			if (0xd800 <= hs && hs <= 0xdbff) {
				if (source.length() > 1) {
					char ls = source.charAt(i + 1);
					int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
					if (0x1d000 <= uc && uc <= 0x1f77f) {
						return true;
					}
				}
			} else {
				// non surrogate
				if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
					return true;
				} else if (0x2B05 <= hs && hs <= 0x2b07) {
					return true;
				} else if (0x2934 <= hs && hs <= 0x2935) {
					return true;
				} else if (0x3297 <= hs && hs <= 0x3299) {
					return true;
				} else if (hs == 0xa9 || hs == 0xae || hs == 0x303d
						|| hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c
						|| hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
					return true;
				}
				if (!isEmoji && source.length() > 1 && i < source.length() - 1) {
					char ls = source.charAt(i + 1);
					if (ls == 0x20e3) {
						return true;
					}
				}
			}
		}
		return isEmoji;
	}

	/**
	 * unicode 转 中文
	 */
	public static String unicode2String(String unicode) {

		StringBuffer string = new StringBuffer();

		UserShoppingCartDTO usc = new UserShoppingCartDTO();

		String[] hex = unicode.split("\\\\u");

		for (int i = 0; i < hex.length; i++) {

			try {
				// 汉字范围 \u4e00-\u9fa5 (中文)
				// 取前四个，判断是否是汉字
				if (hex[i].length() >= 4) {
					String chinese = hex[i].substring(0, 4);
					try {
						int chr = Integer.parseInt(chinese, 16);
						boolean isChinese = UnicodeUtils.isChinese((char) chr);
						// 转化成功，判断是否在 汉字范围内
						// 在汉字范围内
						if (isChinese) {
							// 追加成string
							string.append((char) chr);
							// 并且追加 后面的字符
							String behindString = hex[i].substring(4);
							string.append(behindString);
						} else {
							string.append(hex[i]);
						}
					} catch (NumberFormatException e1) {
						string.append(hex[i]);
					}

				} else {
					string.append(hex[i]);
				}
			} catch (NumberFormatException e) {
				string.append(hex[i]);
			}
		}
		usc.setGoodsSpec(string.toString());
		return usc.getGoodsSpec();
	}

	public static boolean isChinese(String str) {
		if (str == null) {
			return false;
		}
		// 使用正则表达式匹配整个字符串是否由中文字符组成
		return str.matches("[\\u4e00-\\u9fa5]+");
	}

	public static boolean containsChinese(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		// 修改正则表达式以检查字符串中是否包含中文字符
		return str.matches(".*[\\u4e00-\\u9fa5].*");
	}

}
