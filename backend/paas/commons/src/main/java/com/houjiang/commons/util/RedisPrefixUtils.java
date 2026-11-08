package com.houjiang.commons.util;

/**
 * @author Amos
 */
public class RedisPrefixUtils {

    /** 乘客验证码的前缀 */
    public static String verificationCodePrefix = "verification-code-";

    /** 用户操作ip地址的前缀 */
    public static String userHostAddressCodePrefix = "userHostAddress-code-";


    /** token存储的前缀 */
    public static String tokenPrefix = "token-";

    public static String imConfigPrefix = "IM_CONFIG";

    /** 黑名单设备号 */
    public static String blackDeviceCodePrefix = "black-device-";

    public static String goodsClassify = "GOODS_CAT_WAP_GOODS_QUERY_TEST_";

    public static String pcIndexConfig = "PC_INDEX_CONFIG_TEST";

    public static String pcIndexConfig2 = "PC_INDEX_CONFIG_TEST_TWO";
    /** 货币符号key前缀 */
    public static String currencySymbol = "CURRENCY_SYMBOL";
    /** 购物车信息key前缀 */
    public static String unimallCartUserId = "unimall_cart_userId_";

    /**
     * 根据手机号，生成key
     * @param phone
     * @param identity
     * @return
     */
    public static String generatorKeyByPhone(String phone, String identity){
        return verificationCodePrefix + identity+ "-" + phone;
    }

    /**
     * 根据手机号和身份标识，生成token
     * @param phone
     * @param identity
     * @return
     */
    public static String generatorTokenKey(String phone , String identity , String tokenType){
        return tokenPrefix + phone + "-" + identity + "-" + tokenType;
    }
}
