package com.houjiang.merchant.common.enums;


import java.util.Arrays;
import java.util.List;

public enum AuditEnum {

    /** TODO 补充说明 */
    WAIT(0, "待审核"),
    /** TODO 补充说明 */
    PASS(1, "已通过"),
    /** TODO 补充说明 */
    FAIL(2, "已拒绝");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    AuditEnum(int code, String msg) {
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

    public static String getStatusDesc(Integer code){
        switch (code) {
            case 0:
                return "待审核";
            case 1:
                return "已通过";
            case 2:
                return "已拒绝";
        }
        return "";
    }

    public static List<Integer> getAuditHasArr() {
        return Arrays.asList(WAIT.getCode(), PASS.getCode());
    }
}
