package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayUserBaseEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface PayUserBaseService extends IService<PayUserBaseEntity> {

    /**
     * 创建用户
     *
     * @param userId
     * @return
     */
    ResponseResult createUser(String userId);

    /**
     * 校验支付密码是否存在
     * @param paraMap
     * @return
     */
    ResponseResult checkPayPassword(Map<String, String> paraMap);

    /**
     * 修改用户支付密码
     * @param map
     * @return
     */
    ResponseResult updatePaymentPassword(Map<String, Object> map);

    /**
     * plus会员 支付 余额支付 购物卡支付
     * @param map
     * @return
     */
    ResponseResult plusMemberpay(Map<String, Object> map);



    /**
     * 设置支付密码与找回
     *
     * @param map
     * @return
     */
    ResponseResult setPaymentPassword(Map<String, Object> map);
}
