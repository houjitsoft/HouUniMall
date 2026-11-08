package com.houjiang.merchant.service;

import com.houjiang.merchant.validate.marketing.MarketingRechargeValidate;
import com.houjiang.merchant.vo.marketing.MarketingRechargeVo;

/**
 * 营销充值服务接口类
 */
public interface IMarketingRechargeService {

    /**
     * 充值配置详情
     *
     * @author Amos
     * @return MarketingRechargeVo
     */
    MarketingRechargeVo detail();

    /**
     * 充值配置保存
     *
     * @author Amos
     * @param rechargeValidate 充值参数
     */
    void save(MarketingRechargeValidate rechargeValidate);

}
