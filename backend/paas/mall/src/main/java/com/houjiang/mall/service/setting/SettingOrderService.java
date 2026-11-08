package com.houjiang.mall.service.setting;

import java.util.Map;

/**
 * 商城后台 - 订单设置 Service（交易 + 结算）。
 * 对应 like-admin SettingOrderService。
 *
 * @author Amos
 */
public interface SettingOrderService {

    /**
     * 交易设置详情（mall-order-transaction 分组）。
     */
    Map<String, Object> getTransaction();

    /**
     * 交易设置保存。
     */
    void saveTransaction(Map<String, Object> params);

    /**
     * 结算设置详情（mall-order-settlement 分组）+ 枚举（周/月/周期）。
     */
    Map<String, Object> getSettlement();

    /**
     * 结算设置保存。
     */
    void saveSettlement(Map<String, Object> params);
}
