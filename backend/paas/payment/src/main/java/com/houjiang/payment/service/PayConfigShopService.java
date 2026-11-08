package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayConfigShopEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface PayConfigShopService extends IService<PayConfigShopEntity> {


    /**
     * 查询支付配置
     * @param map
     * @return
     */
    ResponseResult select(Map<String,Object> map);


    /**
     * 支付配置
     * @param map
     * @return
     */
    ResponseResult update(Map<String,Object> map);

    /**
     * 获取品牌小程序支付配置 feign
     * @param map
     * @return
     */
    ResponseResult selectPayConfigFeign(Map<String, Object> map);
}
