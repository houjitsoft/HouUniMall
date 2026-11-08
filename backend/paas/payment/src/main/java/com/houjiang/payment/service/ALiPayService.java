package com.houjiang.payment.service;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.BalanceWithdrawDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * @author Amos
 */
public interface ALiPayService {

    /**
     * 支付宝回调
     *
     * @param request
     * @param response
     */
    void alipayNotifyUrl(HttpServletRequest request, HttpServletResponse response);

    /**
     * plus会员 支付宝异步回调
     *
     * @param request
     * @param response
     */
    void alipayPlusNotifyUrl(HttpServletRequest request, HttpServletResponse response);

    /**
     * 余额充值 支付宝异步回调
     *
     * @param request
     * @param response
     * @return
     */
    void alipayBalanceRechargeNotifyUrl(HttpServletRequest request, HttpServletResponse response);

    /**
     * 支付宝查询是否支付成功
     *
     * @param param
     * @return
     */
    ResponseResult alipayQuery(Map<String, Object> param);

    /**
     * 对订单已支付成功 回调失败的异常单进行处理
     *
     * @param param
     * @return
     */
    ResponseResult alipayCompensate(Map<String, Object> param);


    /**
     * 余额支付宝提现
     *
     * @param balanceWithdrawDTO
     * @return
     */
    ResponseResult balanceWithdraw(BalanceWithdrawDTO balanceWithdrawDTO);
}
