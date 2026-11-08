package com.houjiang.payment.service;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.BalanceWithdrawDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Amos
 */
public interface WeChatPayServices {

    /**
     * 处理微信支付通知
     * 当微信支付成功或失败时，微信服务器会发送一个通知到商户服务器，本方法用于处理该通知
     *
     * @param request HTTP请求对象，包含微信服务器发送的通知数据
     * @param response HTTP响应对象，用于向微信服务器发送处理结果
     * @return 返回处理结果字符串
     */
    String notifyWeiXinPay(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取微信网页code链接
     *
     */
    Map<String, Object> getCode(Map<String, String> paraMap);

    /**
     * 获取微信网页OpenId
     *
     */
    Map<String, Object> getOpenId(Map<String, String> paraMap);

    /**
     * 处理微信支付的加签通知。
     *
     * 此方法用于接收微信支付的加签通知请求，并进行相应的处理。在支付过程中，微信支付会向商户系统发送通知，
     * 通知可能包含支付结果、订单状态等信息。这个方法的作用是对接收到的微信支付通知进行处理，
     * 主要包括验证通知的真伪、解析通知内容等操作。
     *
     * @param request 用于接收微信支付通知的HttpServletRequest对象。其中可能包含支付结果、订单状态等信息。
     * @param response 用于向微信支付返回处理结果的HttpServletResponse对象。根据处理结果的不同，
     *                 可能需要向微信支付返回不同的响应，以指示通知是否被成功处理。
     * @return 返回处理结果的字符串。具体的返回内容可能根据处理逻辑的不同而有所变化，
     *         但一般会包含处理结果的状态码或描述信息。
     */
    String plusNotifyWeiXinPay(HttpServletRequest request, HttpServletResponse response);

    /**
     * 余额充值 微信支付回调
     * @param request
     * @param response
     * @return
     */
    String balanceRechargeNotifyWeiXinPay(HttpServletRequest request, HttpServletResponse response);

    /**
     * 轮询微信支付后状态
     * @param map
     * @return
     */
    ResponseResult queryWxPayStatus(Map<Object, Object> map);

    /**
     * 轮询plus 会员微信支付后状态
     * @param map
     * @return
     */
    ResponseResult queryWxPayplus(Map<Object, Object> map);

    /**
     * 轮询 余额充值 微信支付后状态
     * @param map
     * @return
     */
    ResponseResult queryWxPayBalanceRecharge(Map<Object, Object> map);
    /**
     * 根据订单号查询微信支付状态
     * @param map
     * @return
     */
    ResponseResult queryWxPay(Map<String, Object> map) ;

    /**
     * 对订单微信 已支付成功 回调失败的异常单进行处理
     * @param param
     * @return
     */
    ResponseResult wxPayCompensate(Map<String, Object> param);

    /**
     * 向微信服务器发送POST请求并获取响应
     * 该方法用于处理与微信服务器之间的数据交互， specifically用于发送数据
     *
     * @param requestBody 请求体内容，通常是一个JSON格式的字符串，包含要发送给微信服务器的所有信息
     * @return 微信服务器返回的响应内容，通常也是一个JSON格式的字符串
     * @throws Exception 如果在执行POST请求过程中发生错误，该方法将抛出一个异常
     */
    String wechatPost(String requestBody) throws Exception;

    /**
     * 余额微信提现
     * @param balanceWithdrawDTO
     * @return
     */
    ResponseResult balanceWithdraw(BalanceWithdrawDTO balanceWithdrawDTO);
}
