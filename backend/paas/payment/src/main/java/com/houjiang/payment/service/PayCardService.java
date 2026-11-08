package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.CheckRechargeCardDTO;
import com.houjiang.payment.entity.PayCardEntity;

/**
 * @author Amos
 */
public interface PayCardService extends IService<PayCardEntity> {

    /**
     * 查询激活数据
     *
     * @param param
     * @return
     */
    ResponseResult query(CheckRechargeCardDTO param);
}
