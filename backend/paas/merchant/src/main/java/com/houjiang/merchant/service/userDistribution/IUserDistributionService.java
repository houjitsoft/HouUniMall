package com.houjiang.merchant.service.userDistribution;

import com.alibaba.fastjson2.JSONObject;

/**
 * @author Amos
 */
public interface IUserDistributionService {
    /**
     * 分销概览
     * @param shopId
     * @return
     */
    JSONObject count(Integer shopId);
}
