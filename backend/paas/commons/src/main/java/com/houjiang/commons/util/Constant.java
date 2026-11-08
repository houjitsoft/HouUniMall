/**
 * 厚匠科技版权所有
 */

package com.houjiang.commons.util;

/**
 * 常量
 *
 * @author Amos
 */
public class Constant {
	/** 超级管理员ID */
	public static final int SUPER_ADMIN = 1;
    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "limit";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "sidx";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    /**
     * 升序
     */
    public static final String ASC = "asc";
    /**
     * 商家后台和店铺个人入驻查询所有分类,根据子类分好
     */
    public static final String CACHE_GOODS_CAT_MANAGE_ALL = "GOODS_CAT_ALL_QUERY";
    /**
     * 分类 WEB商品列表页根据分类id查询子类
     */
    public static final String CACHE_GOODS_WEB_GOODS_LIST_HEADER_PREFIX = "GOODS_CAT_WEB_GOODS_LIST_FIRST_CACHE_KEY";

    // 店铺设置
    public final static String SERLLER_SHOP_SETSHOP = "Seller_Shop_Setshop";

    //请求头存放token
    public final static String AUTHORIZATION="Authorization";

    public final static String WEBPOS_USER_TOKEN_USER = "webpos-user-token-user_";

    public final static String PLATFORM_TOKEN_USER = "platform-user_";

    public final static String PLATFORM_SMS_CODE = "platform-sms-code_";

    /** 登录信息保存redis时间 */
    public static final Integer WEBPOS_TOKEN_EXPIRE_TIME=1296000;
    /** 短信保存redis时间 */
    public static final Integer WEBPOS_SMS_EXPIRE_TIME=300;
    /** 订单前缀 */
    public static final String ORDER_PREFIX="WPS";

    /**
     * 菜单类型
     *
     * @author Amos
     * @date 2016年11月15日 下午1:24:29
     */
    public enum MenuType {
        /**
         * 目录
         */
    	CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 定时任务状态
     *
     * @author Amos
     * @date 2016年12月3日 上午12:07:22
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
    	NORMAL(0),
        /**
         * 暂停
         */
    	PAUSE(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 云服务商
     */
    public enum CloudService {
        /**
         * 七牛云
         */
        QINIU(1),
        /**
         * 阿里云
         */
        ALIYUN(2),
        /**
         * 腾讯云
         */
        QCLOUD(3);

        private int value;

        CloudService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
