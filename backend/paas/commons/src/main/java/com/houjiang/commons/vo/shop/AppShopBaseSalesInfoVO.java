package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class AppShopBaseSalesInfoVO {

    /**
     * 今日数
     */
    private String todayCount;

    /**
     * 昨日支付金额
     */
    private String yesterdayCount;
}
