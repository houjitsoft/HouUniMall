package com.houjiang.merchant.common.enums;

/**
 * @notes 平台 公告
 * @author Amos
 * @datetime 2024-10-21 14:34:43
 * @class AnnouncementEnum
 */
public enum AnnouncementEnum {

    /** TODO 补充说明 */
    PRIORITY_EXIGENCE(1, "紧急"),
    /** TODO 补充说明 */
    PRIORITY_IMPORTANCE(2, "重要"),
    /** TODO 补充说明 */
    PRIORITY_HINT(3, "提示");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    AnnouncementEnum(int code, String msg) {
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
     * 公告类型标识
     * @author Amos
     * @param code 编码
     * @return String
     */
    public static String getPriorityTextArr(Integer code){
        switch (code) {
            case 1:
                return "紧急";
            case 2:
                return "重要";
            case 3:
                return "提示";
        }
        return "未知";
    }

}
