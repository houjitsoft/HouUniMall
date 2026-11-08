package com.houjiang.payment.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收银台订单状态枚举
 *
 * @author Amos
 */
@Getter
@AllArgsConstructor
public enum CashierOrderStatusEnum {

    /** 待支付 */
    WAIT_PAY(0, "待支付"),
    /** 支付中 */
    PAYING(1, "支付中"),
    /** 支付成功 */
    SUCCESS(2, "支付成功"),
    /** 支付失败 */
    FAIL(3, "支付失败"),
    /** 已关闭 */
    CLOSED(4, "已关闭"),
    /** 已退款 */
    REFUND(5, "已退款");

    private final int code;
    private final String name;

    public static CashierOrderStatusEnum ofCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CashierOrderStatusEnum s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }
}
