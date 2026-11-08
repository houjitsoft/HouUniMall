package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName OrderReturnLogVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:45
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderReturnLogVO {

    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 订单编号
     */
    private String orderSn;
    /**
     * 退货编号
     */
    private String returnSn;
    /**
     * 售后状态（1,待售后审核2,拒绝退款：3,退款成功：4,拒绝退货退款：5,待买家退货：6,买家已退货，待售后收货：7,买家已退货，售后拒绝收货：8,卖家已收货,待确认退款：9,退货退款成功：10,拒绝换货：11,买家已退货，待售后换货：12,售后已换货，待买家收货：13,换货成功：14,已关闭：15,卖家同意退款,16：卖家同意退款, 17：卖家拒绝退款，18：退款成功）
     */
    private Integer returnStatus;
    /**
     * 主键ID
     */
    private String id;
    /**
     * 日志信息
     */
    private String logInfo;
    /**
     * 买家ID
     */
    private String buyerId;
    /**
     * 操作者
     */
    private String operator;
}
