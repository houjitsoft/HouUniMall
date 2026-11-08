package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SubmitOrderDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 17:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class SubmitOrderDTO {

    /**
     * 订单来源：1 开放平台
     */
    private String orderSource;

    /**
     * 商品列表，包含订单中的所有商品信息
     */
    private List<GoodsDTO> goodsList;

    /**
     * 下单用户名称
     */
    private String buyerName;

    /**
     * 收货联系人姓名
     */
    private String shipName;

    /**
     * 收货地址，如创业路东
     */
    private String shipAddress;

    /**
     * 收货电话，可选
     */
    private String shipPhone;

    /**
     * 收货手机
     */
    private String shipMobile;

    /**
     * 纳税人识别号，用于开具发票时记录纳税人信息
     */
    private String taxPayer;

    /**
     * 外部订单号，用于关联外部系统中的订单
     */
    private String outOrderSn;

    /**
     * 发票类型（1;个人，2;公司，0;不开发票,默认值0）
     */
    private String invoiceType;

    /**
     * 发票抬头
     */
    private String invoice;

    /**
     * 邮政编码，用于指定收货地址的邮编
     */
    private String shipZipCode;

    /**
     * 订单备注
     */
    private String buyerMemo;

    /**
     * 收货地区编码，例如:省ID,市ID,区ID,镇ID（三级与四级地址均可下单）
     */
    private String shipAreaCode;
}
