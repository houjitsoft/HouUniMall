package com.houjiang.payment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 终端类型枚举（统一收银台 Task 0）
 * 覆盖 14 个小程序/APP/PC/WAP 终端
 *
 * @author Amos
 */
@Getter
@AllArgsConstructor
public enum TerminalTypeEnum {

    /** 微信小程序 */
    WX_MINI("WX_MINI", "微信小程序"),
    /** 支付宝小程序 */
    ALI_MINI("ALI_MINI", "支付宝小程序"),
    /** 百度智能小程序 */
    BD_MINI("BD_MINI", "百度智能小程序"),
    /** 抖音/字节小程序 */
    TT_MINI("TT_MINI", "抖音/字节小程序"),
    /** QQ小程序 */
    QQ_MINI("QQ_MINI", "QQ小程序"),
    /** 360小程序 */
    MINI_360("360_MINI", "360小程序"),
    /** 快手小程序 */
    KS_MINI("KS_MINI", "快手小程序"),
    /** 飞书小程序 */
    LARK_MINI("LARK_MINI", "飞书小程序"),
    /** 京东小程序 */
    JD_MINI("JD_MINI", "京东小程序"),
    /** 小红书小程序 */
    XHS_MINI("XHS_MINI", "小红书小程序"),
    /** 原生APP */
    APP("APP", "原生APP"),
    /** 华为快服务 */
    HW("HW", "华为快服务"),
    /** 快应用 */
    QUICKAPP("QUICKAPP", "快应用"),
    /** 鸿蒙 */
    HARMONY("HARMONY", "鸿蒙"),
    /** H5移动站 */
    WAP("WAP", "H5移动站"),
    /** PC网页 */
    PC("PC", "PC网页");

    /** 终端编码（与前端 terminal 参数一致） */
    private final String code;
    /** 终端名称 */
    private final String name;

    /**
     * 根据编码查找终端
     * @param code 终端编码
     * @return 枚举；未找到返回 null
     */
    public static TerminalTypeEnum ofCode(String code) {
        if (code == null) {
            return null;
        }
        for (TerminalTypeEnum t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        return null;
    }
}
