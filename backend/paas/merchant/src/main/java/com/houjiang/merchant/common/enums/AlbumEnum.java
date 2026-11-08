package com.houjiang.merchant.common.enums;

/**
 * 相册枚举
 */
public enum AlbumEnum {

    /** TODO 补充说明 */
    IMAGE(10, "图片"),
    Video(20, "视频"),
    Doc(30, "文件");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    AlbumEnum(int code, String msg) {
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

}
