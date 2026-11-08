package com.houjiang.order.service;


import com.houjiang.commons.vo.order.OrderCommissionVO;
import com.houjiang.commons.vo.order.WapShippingQueryResultVO;
import com.houjiang.commons.vo.order.WapShopShippingQueryResultVO;
import com.houjiang.commons.vo.user.UserInfoVO;

import java.math.BigDecimal;

/**
 * @author Amos
 * @date 2021-06-0510:29
 * @Description: 分销
 **/
public interface FXService {

    /**
     * 创建分销佣金订单
     * 根据订单信息和微客相关信息，计算并添加订单佣金信息。
     *
     * @param shippingQueryResult     物流查询结果，包含订单的相关物流信息。
     * @param shopShippingQueryResult 商家物流查询结果，包含商家订单的物流信息。
     * @param userInfo                用户信息，用于查询微客绑定的上级信息。
     * @param shareUserId             分享者的用户ID，用于查询分享者的微客信息。
     * @param orderId                 订单ID，用于关联订单和佣金信息。
     * @param orderPrice              订单价格，用于计算佣金。
     * @param fxType                  分销类型，用于确定佣金计算规则。
     * @param vkShopId                微客店铺ID，用于查询微客店铺信息。
     * @return 返回订单佣金信息VO，如果不需要计算佣金则返回null。
     */
    OrderCommissionVO addOrderCommission(WapShippingQueryResultVO shippingQueryResult, WapShopShippingQueryResultVO shopShippingQueryResult, UserInfoVO userInfo, String shareUserId, String orderId, BigDecimal orderPrice, Integer fxType, Integer vkShopId);

    /**
     * 根据传入的运输查询结果、用户信息和分享用户ID，检查是否为VK配送信息。
     *
     * @param shippingQueryResult 运输查询结果对象，包含配送相关信息。
     * @param userInfo            用户信息对象，用于判断当前操作用户的身份。
     * @param shareUserId         分享者的用户ID，用于判断是否通过分享获取信息。
     * @param fxType              分享类型，用于区分不同的分享场景。
     * @return 返回经过检查后的运输查询结果对象，可能包含了更新的信息。
     */
    WapShippingQueryResultVO selectIsVkShippingInfo(WapShippingQueryResultVO shippingQueryResult, UserInfoVO userInfo, String shareUserId, Integer fxType);

}

