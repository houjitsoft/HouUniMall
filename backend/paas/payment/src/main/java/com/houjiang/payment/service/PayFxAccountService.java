package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.WithdrawRequestDTO;
import com.houjiang.payment.entity.PayFxAccountEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Amos
 */
public interface PayFxAccountService extends IService<PayFxAccountEntity> {

    /**
     * 用户提现
     *
     * @param param
     * @param request
     * @return
     */
    ResponseResult withdraw(WithdrawRequestDTO param, HttpServletRequest request);

}
