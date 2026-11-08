package com.houjiang.merchant.common.plugin.delivery.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class KdQueryTrackParam {

    /**
     * [必须] 查询的快递公司的编码,一律用小写字母
     */
    private String com;

    /**
     * [必须] 查询的快递单号,单号的最小长度6个字符,最大长度32个字符
     */
    private String num;

    /**
     * 收/寄件人的电话号码
     * 顺丰速运/顺丰快运和丰网速运必填
     */
    private String phone;

    /**
     * 出发地城市
     */
    private String from;

    /**
     * 目的地城市
     */
    private String to;

    /**
     * (快递鸟) 客户姓名
     */
    private String customerName = "";

    /**
     * (快递鸟) 订单编号
     */
    private String orderCode = "";

}
