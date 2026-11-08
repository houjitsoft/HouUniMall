package com.houjiang.merchant.common.enums;

import java.util.ArrayList;
import java.util.List;

public enum MallGoodsCommentEnum {

    // 商品评论
    IS_REPLY(1, "已经评论");

    /**
     * 构造方法
     */
    private final int code;
    private final String msg;
    MallGoodsCommentEnum(int code, String msg) {
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
     *  分数对应评论等级
     */
    public static String getLevelAttr(Integer score) {
        if (score >= 5) {
            return "good";
        }
        if (score >= 3 && score <= 4) {
            return "middle";
        }
        if (score <= 2) {
            return "bad";
        }
        return "";
    }
    /**
     *  评论等级对应的字符
     */
    public static String getLevelTextAttr(String level) {
        if (level.equals("good")) {
            return "好评";
        }
        if (level.equals("middle")) {
            return "中评";
        }
        if (level.equals("bad")) {
            return "差评";
        }
        return "";
    }
    /**
     *  评论等级对应的字符
     */
    public static String getLevelTextAttr(Integer score) {
        String level = getLevelAttr(score);
        if (level.equals("good")) {
            return "好评";
        }
        if (level.equals("middle")) {
            return "中评";
        }
        if (level.equals("bad")) {
            return "差评";
        }
        return "";
    }

    /**
     * 返回分数
     */
    public static List<Integer> getScoreByLevel(String level) {
        List<Integer> ret = new ArrayList<>();
        if (level.equals("good")) {
            ret.add(5);
        }
        if (level.equals("middle")) {
            ret.add(3);
            ret.add(4);
        }
        if (level.equals("bad")) {
            ret.add(0);
            ret.add(2);
        }
        return ret;
    }
}
