package com.houjiang.merchant.service;

import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * C 端商家入驻/开店状态。
 */
public interface ShopBaseStatusService {

    /**
     * 查询当前登录用户的商家店铺入驻状态（供 PC Header 判定商家中心入口）。
     */
    ResponseResult queryShopJoinStatus(Map<String, Object> param);
}
