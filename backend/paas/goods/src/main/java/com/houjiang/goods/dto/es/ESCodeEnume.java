package com.houjiang.goods.dto.es;

/**
 * @author Amos
 */
public enum ESCodeEnume {



    /** 创建索引库成功 */
    INDEX_CREATE_SUCCESS(1, "创建索引库成功"),
    /** 创建索引库失败 */
    INDEX_CREATE_FAIL(1, "创建索引库失败"),
    /** 删除索引库成功 */
    INDEX_DELETE_SUCCESS(1, "删除索引库成功"),
    /** 删除索引库失败 */
    INDEX_DELETE_FAIL(1, "删除索引库失败"),
    /** 数据删除成功 */
    DATA_DELETE_SUCCESS(1,"数据删除成功"),
    /** 导入成功 */
    INPUT_SUCCESS(1,"导入成功"),
    /** 导入失败 */
    INPUT_FAIL(1,"导入失败"),
    /** 删除成功 */
    DELETE_SUCCESS(1,"删除成功"),

    ;
    private int code;
    private String msg;

    ESCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
