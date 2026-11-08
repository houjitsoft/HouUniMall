package com.houjiang.mall.service.setting;

import com.houjiang.mall.vo.setting.SettingPaymentMethodVo;

import java.util.List;
import java.util.Map;

/**
 * 商城后台 - 支付方式 Service。
 *
 * @author Amos
 */
public interface SettingPayWayService {

    /**
     * 按场景分组返回支付方式列表。
     * key 为场景编码（1=微信小程序,2=微信公众号,3=H5），value 为该场景下的支付方式列表。
     */
    Map<Integer, List<SettingPaymentMethodVo>> list();

    /**
     * 批量更新支付方式（仅更新 isDefault / status）。
     * 入参结构：{1: [{id, isDefault, status}, ...], 2: [...], 3: [...]}
     */
    void edit(Map<Integer, List<SettingPaymentMethodVo>> data);
}
