package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.entity.PayFxRecordEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface PayFxRecordService extends IService<PayFxRecordEntity> {

    /**
     * 用户提现记录列表
     *
     * @param param
     * @return
     */
    ResponseResult query(Map<String, Object> param);
}
