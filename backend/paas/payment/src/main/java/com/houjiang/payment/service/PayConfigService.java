package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayConfigEntity;

import java.util.Map;

/**
 * 支付配置信息表(PayConfig)表
 * @author Amos
 */
public interface PayConfigService extends IService<PayConfigEntity> {

    /**
     * 查询支付配置信息 feign 后端查询
     *
     * @param map
     * @return
     */
    ResponseResult queryFeign(Map<String, Object> map);


    /**
     * 平台修改支付宝配置
     *
     * @param map
     * @return
     */
    ResponseResult updateZFBPayConfig(Map<String, Object> map);


    /**
     * 平台查询支付宝配置
     *
     * @param map
     * @return
     */
    ResponseResult queryZFBPayConfig(Map<String, Object> map);

    /**
     * 平台修改微信配置
     *
     * @param map
     * @return
     */
    ResponseResult updateWXPayConfig(Map<String, Object> map);

    /**
     * 平台查询微信配置
     *
     * @param map
     * @return
     */
    ResponseResult queryWXPayConfig(Map<String, Object> map);

    /**
     * 查询提现的金额配置接口
     * @return
     */
    ResponseResult queryWithdrawConfig();

    /**
     * 删除微客信息
     * @param userId
     * @return
     */
    ResponseResult delFxInfo(String userId);
    /**
     * 查询支付配置信息 feign 后端查询
     *
     * @param map
     * @return
     */
    ResponseResult queryPayConfigFeign(Map<String, Object> map);
}
