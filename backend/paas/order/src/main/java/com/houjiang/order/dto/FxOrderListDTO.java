package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @ClassName FxOrderListDTO
 * @Description 结算查询
 * @Author Amos
 * @Date 2023/8/21 18:32
 * @Version 1.0
 * @author Amos
 */
@Data
public class FxOrderListDTO extends PageHelper {
    private String token;

    /**
     * 1推广佣金  2分享佣金  3全部
     */
    private Integer commissionType;

    /**
     * 1全部结算  2今日结算  3七天  4一个月  5三个月   6一年
     */
    private Integer dateType;

    /**
     *0未结算  1已结算
     */
    private Integer settleState;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 状态 1待支付  2未入账  3已入账  4已退款  5已取消 6全部
     */
    private Integer orderStatus;

    /**
     * 支付时间
     */
    private String payTime;

    /**
     * 1 佣金查询  2订单查询
     */
    private Integer settleQuery;
}
