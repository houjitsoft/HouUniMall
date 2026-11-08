package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.BalanceWithdrawDTO;
import com.houjiang.payment.service.ALiPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付宝支付
 * @author Amos
 */
@RestController
@RequestMapping("pay/aLiPay")
public class ALiPayController {

    @Autowired
    protected ALiPayService aLiPayService;

    /**
     * 支付宝回调
     *
     * @param request
     * @param response
     */
    @RequestMapping("/alipayNotifyUrl")
    public void alipayNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        aLiPayService.alipayNotifyUrl(request, response);
    }

    /**
     * plus会员 支付宝异步回调
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/alipayPlusNotifyUrl")
    public void alipayPlusNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        aLiPayService.alipayPlusNotifyUrl(request, response);
    }

    /**
     * 余额充值 支付宝异步回调
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/alipayBalanceRechargeNotifyUrl")
    public void alipayBalanceRechargeNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        aLiPayService.alipayBalanceRechargeNotifyUrl(request, response);
    }
    /**
     * 判断支付宝是否支付成功  feign
     * @param param
     * @return
     */
    @RequestMapping("/alipayQuery")
    public ResponseResult alipayQuery(@RequestBody Map<String, Object> param) {
        return aLiPayService.alipayQuery(param);
    }

    /**
     * 对订单支付宝 已支付成功 回调失败的异常单进行处理 feign
     * @param param
     * @return
     */
    @RequestMapping("/alipayCompensate")
    public ResponseResult alipayCompensate(@RequestBody Map<String, Object> param) {
        return aLiPayService.alipayCompensate(param);
    }
    /**
     * 余额支付宝提现
     * @param balanceWithdrawDTO
     * @return
     */
    @RequestMapping("/balanceWithdraw")
    public ResponseResult balanceWithdraw(@RequestBody BalanceWithdrawDTO balanceWithdrawDTO) {
        return aLiPayService.balanceWithdraw(balanceWithdrawDTO);
    }
}
