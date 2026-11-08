package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 拼团枚举
 */
public enum PtEnum {

    /** TODO 补充说明 */
    AUDIT_STATUS_WAIT_SUBMIT(0, "待提交"),
    /** TODO 补充说明 */
    AUDIT_STATUS_WAIT(10, "待审核"),
    /** TODO 补充说明 */
    AUDIT_STATUS_SUCCESS(20, "审核通过"),
    /** TODO 补充说明 */
    AUDIT_STATUS_FAILED(30, "审核失败"),

    // 未开始
    STATUS_WAIT(0, "未开始"),
    /** TODO 补充说明 */
    STATUS_START(1, "进行中"),
    /** TODO 补充说明 */
    STATUS_END(2, "已结束"),
    /** TODO 补充说明 */
    OPEN_STATUS_WAIT(0, "拼团中"),
    /** TODO 补充说明 */
    OPEN_STATUS_SUCCESS(1, "拼团成功"),
    /** TODO 补充说明 */
    OPEN_STATUS_FAILED(2, "拼团失败");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    PtEnum(int code, String msg) {
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


    /** 商品存在拼团记录的状态 */
    public static List<Integer> getStatusHasArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(STATUS_WAIT.getCode());
        ret.add(STATUS_START.getCode());
        return ret;
    }

    public static String getOpenStatusText(Integer code) {
        if (code.equals(OPEN_STATUS_WAIT.getCode())) {
            return OPEN_STATUS_WAIT.getMsg();
        }
        if (code.equals(OPEN_STATUS_SUCCESS.getCode())) {
            return OPEN_STATUS_SUCCESS.getMsg();
        }
        if (code.equals(OPEN_STATUS_FAILED.getCode())) {
            return OPEN_STATUS_FAILED.getMsg();
        }
        return "";
    }

    public static String getStatusTextArr(Integer code) {
        if (code.equals(STATUS_WAIT.getCode())) {
            return STATUS_WAIT.getMsg();
        }
        if (code.equals(STATUS_START.getCode())) {
            return STATUS_START.getMsg();
        }
        if (code.equals(STATUS_END.getCode())) {
            return STATUS_END.getMsg();
        }
        return "";
    }

    public static String getAuditStatusTextArr(Integer auditStatus) {
        AUDIT_STATUS_WAIT_SUBMIT.getCode();
        switch (auditStatus) {
            case 0:
                return AUDIT_STATUS_WAIT_SUBMIT.getMsg();
            case 10:
                return AUDIT_STATUS_WAIT.getMsg();
            case 20:
                return AUDIT_STATUS_SUCCESS.getMsg();
            case 30:
                return AUDIT_STATUS_FAILED.getMsg();
            default:
                return "";
        }
    }

    public static List<Integer> getAuditCanEditArr() {
        List<Integer> ret = new ArrayList<>();
        ret.add(AUDIT_STATUS_WAIT_SUBMIT.getCode());
        ret.add(AUDIT_STATUS_SUCCESS.getCode());
        ret.add(AUDIT_STATUS_FAILED.getCode());
        return ret;
    }
}
