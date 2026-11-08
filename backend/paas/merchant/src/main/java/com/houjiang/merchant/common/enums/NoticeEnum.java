package com.houjiang.merchant.common.enums;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知枚举类
 */
public enum NoticeEnum {

    /** TODO 补充说明 */
    STATUS_WAIT(0, "等待"),
    /** TODO 补充说明 */
    STATUS_OK(1, "成功"),
    /** TODO 补充说明 */
    STATUS_FAIL(2, "失败"),

    /** TODO 补充说明 */
    VIEW_UNREAD(0, "未读"),
    /** TODO 补充说明 */
    VIEW_READ(0, "已读"),

    /** TODO 补充说明 */
    SENDER_SYS(1, "系统类型"),
    /** TODO 补充说明 */
    SENDER_SMS(2, "短信类型"),
    /** TODO 补充说明 */
    SENDER_MNP(3, "小程序类型"),
    /** TODO 补充说明 */
    SENDER_OA(4, "公众号类型"),

    // 验证码场景 注册验证码
    SCENE_CAPTCHA_REGISTER(100, "注册验证码"),
    /** TODO 补充说明 */
    LOGIN_CAPTCHA(101, "登录验证码"),
    /** TODO 补充说明 */
    BIND_MOBILE_CAPTCHA(102, "绑定手机验证码"),
    /** TODO 补充说明 */
    CHANGE_MOBILE_CAPTCHA(103, "变更手机验证码"),
    /** TODO 补充说明 */
    FIND_LOGIN_PASSWORD_CAPTCHA(104, "找回登录密码验证码"),
    /** TODO 补充说明 */
    SCENE_ORDER_PAY_TO_USER(106, "订单付款通知 通知买家"),
    /** TODO 补充说明 */
    SCENE_ORDER_SEND_TO_USER(107, "订单发货通知 通知买家"),
    /** TODO 补充说明 */
    SCENE_AFTER_REFUSE_TO_USER(108, "售后退款拒绝通知 通知买家"),
    /** TODO 补充说明 */
    SCENE_AFTER_SUCCESS_TO_USER(109, "售后退款成功通知 通知买家"),
    /** TODO 补充说明 */
    SCENE_ORDER_PAY_TO_SHOP(1100, "订单支付 通知商家"),
    /** TODO 补充说明 */
    SCENE_ORDER_AFTER_SALE_TO_SHOP(1200, "售后退款申请 通知商家"),
    /** TODO 补充说明 */
    SCENE_SHOP_APPLY_TO_PLATFORM(2100, "入驻申请 通知平台"),
    /** TODO 补充说明 */
    SCENE_SHOP_WITHDRAW_TO_PLATFORM(2200, "商家提现 通知平台");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    NoticeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取状态码
     *
     * @author Amos
     * @return Long
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取提示
     *
     * @author Amos
     * @return String
     */
    public String getMsg() {
        return this.msg;
    }

    /**
     * @notes 更具标记获取场景
     * @param $tag
     * @return int|string
     * @author Amos
     */
    public static Integer getSceneByTag( String tag ) {
        JSONObject scene = new JSONObject();
        // 手机验证码登录
        scene.put("YZMDL", LOGIN_CAPTCHA.getCode());
        // 绑定手机号验证码
        scene.put("BDSJHM", BIND_MOBILE_CAPTCHA.getCode());
        // 变更手机号验证码
        scene.put("BGSJHM", CHANGE_MOBILE_CAPTCHA.getCode());
        // 找回登录密码
        scene.put("ZHDLMM", FIND_LOGIN_PASSWORD_CAPTCHA.getCode());

        return scene.getInteger(tag);
    }

    /**
     * 验证码场景
     */
    public static List<Integer> getSmsScene() {
        List<Integer> ret = new ArrayList<>();
        ret.add(LOGIN_CAPTCHA.getCode());
        ret.add(BIND_MOBILE_CAPTCHA.getCode());
        ret.add(CHANGE_MOBILE_CAPTCHA.getCode());
        ret.add(FIND_LOGIN_PASSWORD_CAPTCHA.getCode());
        return ret;
    }

}
