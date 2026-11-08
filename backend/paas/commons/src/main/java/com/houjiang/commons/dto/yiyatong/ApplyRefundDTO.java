package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ApplyRefundDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class ApplyRefundDTO {

    /**
     * 退款申请的原因
     */
    private String reason;

    /**
     * 申请方式，1：主动申请；2：系统/客户代申请；默认为：1
     */
    private Integer applyWay;

    /**
     * 需要退款的订单编号
     */
    private String orderSn;

    /**
     * 退款原因的标识符
     */
    private String refundReasonId;

    /**
     * 退款申请的附加说明或备注
     */
    private String memo;

    /**
     * 退款申请相关的图片证据列表
     */
    private String pictureurlList;

    /**
     * 售后类型，1：退款，2：退货退款，3：换货，4：仅退款，5：部分退款
     */
    private Integer returnType;

    /**
     * 多商品规格id
     */
    private List<ApplyReturnGoodsDTO> goodsList;
}
