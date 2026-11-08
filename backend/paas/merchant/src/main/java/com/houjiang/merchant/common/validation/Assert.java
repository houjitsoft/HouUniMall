package com.houjiang.merchant.common.validation;

import com.houjiang.merchant.common.exception.OperateException;

/**
 * Merchant 业务断言。失败消息属于可向客户端展示的可信业务文案。
 * @author Amos
 */
public final class Assert {

    private Assert() {
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new OperateException(message);
        }
    }

    public static void isNull(Object value, String message) {
        if (value != null) {
            throw new OperateException(message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new OperateException(message);
        }
    }

    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new OperateException(message);
        }
    }
}
