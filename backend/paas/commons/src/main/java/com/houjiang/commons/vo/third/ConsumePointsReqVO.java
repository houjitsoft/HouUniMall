package com.houjiang.commons.vo.third;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:
 * @CreateDate: 2025/7/8 23:40
 * @author Amos
 */
@Data
public class ConsumePointsReqVO {

    /**
     * 店铺id，唯一标识，跳转h5链接会带过来
     */
    private Integer shopId;

    /**
     * 用户id，唯一标识，跳转h5链接会带过来
     */
    private Integer userId;

    /**
     * 货币id
     */
    private Integer currencyId;

    /**
     * 货币
     */
    private String currencyName;

    /**
     * 货币数量，BigDecimal类型
     */
    private BigDecimal points;

    /**
     * 扣减原因，例如：下单订单号:洗衣液商品2件,茅台1件 （字段不超过255）
     */
    private String consumeTitle;

    /**
     * 订单id，数据库id主键，不必填，便于后期排查数据
     */
    private String orderid;

    /**
     * 业务uuid，确保消耗积分和获取积分唯一性
     */
    private String businessUuid;

}
