package com.houjiang.order.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 * @date 2021-05-1511:31
 * @Description: 订单列表查询请求参数
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SoldOrderRequestDTO extends PageHelper {

    /**
     * 用户授权令牌
     */
    private String token;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 订单开始时间
     */
    private String orderStartTime;

    /**
     * 订单结束时间
     */
    private String orderEndTime;

    /**
     * 订单状态（如待支付、已支付、已发货等）的整数表示
     */
    private Integer status;

    /**
     * 订单类型（如普通订单、优惠券订单等）的整数表示
     */
    private Integer orderType;
    /**
     * 是否 代发货 1是 2否
     * */
    private Integer dropShipping;

    /**
     * 1供应商中心订单   2商家中心订单
     */
    private Integer orderSource;
}
