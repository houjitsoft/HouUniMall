package com.houjiang.merchant.service.impl;

import com.houjiang.merchant.service.IMarketingRechargeService;
import com.houjiang.merchant.validate.marketing.MarketingRechargeValidate;
import com.houjiang.merchant.vo.marketing.MarketingRechargeVo;
import com.houjiang.merchant.common.util.ConfigUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 营销充值服务实现类
 */
@Service
public class MarketingRechargeServiceImpl implements IMarketingRechargeService {

    /**
     * 充值配置详情
     *
     * @author Amos
     * @return MarketingRechargeVo
     */
    @Override
    public MarketingRechargeVo detail() {
        Map<String, String> config = ConfigUtils.get("recharge");

        MarketingRechargeVo vo = new MarketingRechargeVo();
        vo.setStatus(Integer.parseInt(config.getOrDefault("status", "0")));
        vo.setMinAmount(new BigDecimal(config.getOrDefault("min_amount", "0")));
        return vo;
    }

    /**
     * 充值配置保存
     *
     * @author Amos
     * @param rechargeValidate 充值参数
     */
    @Override
    public void save(MarketingRechargeValidate rechargeValidate) {
        ConfigUtils.set("recharge", "status", rechargeValidate.getStatus().toString());
        ConfigUtils.set("recharge", "min_amount", rechargeValidate.getMinAmount().toString());
    }

}
