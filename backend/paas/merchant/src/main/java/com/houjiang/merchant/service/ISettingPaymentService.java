package com.houjiang.merchant.service;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.validate.setting.SettingPayConfigValidate;
import com.houjiang.merchant.vo.setting.SettingPaymentMethodVo;
import com.houjiang.merchant.common.entity.setting.MallPay;

import java.util.HashMap;
import java.util.List;

/**
 * 支付配置服务接口类
 */
public interface ISettingPaymentService {

    /**
     * 支付方式
     *
     * @author Amos
     * @return List<List<SettingPaymentMethodVo>>
     */
    HashMap getPayWay();

    /**
     * 配置列表
     *
     * @author Amos
     * @return List<DevPayConfig>
     */
    JSONObject list();

    /**
     * 配置详情
     *
     * @author Amos
     * @param id 主键
     * @return SettingPaymentMethodVo
     */
    MallPay getConfig(Integer id);

    /**
     * 编辑支付配置
     *
     * @author Amos
     * @param configValidate 参数
     */
    void setConfig(SettingPayConfigValidate configValidate);

    /**
     * 编辑支付方式
     *
     * @author Amos
     * @param data 参数
     */
    void setPayWay(HashMap<Integer, List<SettingPaymentMethodVo>> data);

}
