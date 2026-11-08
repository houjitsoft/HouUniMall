package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.BalanceWithdrawDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 微信支付
 *
 * @author Amos
 */
@RestController
@RequestMapping("pay/WXPay")
public class WXPayController extends BaseController {


	/**
	 * 微信支付回调
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/notifyWeiXinPay")
	public String notifyWeiXinPay(HttpServletRequest request, HttpServletResponse response) {
		String msg = weChatPayServices.notifyWeiXinPay(request, response);
		return msg;
	}


	/**
	 * 获取微信网页code链接
	 *
	 */
	@PostMapping("/getCode")
	public Map<String, Object> getCode(@RequestBody Map<String, String> paraMap) {
		Map<String, Object> result = weChatPayServices.getCode(paraMap);
		return result;
	}

	/**
	 * 获取微信网页OpenId
	 *
	 */
	@PostMapping("/getOpenId")
	public Map<String, Object> getOpenId(@RequestBody Map<String, String> paraMap) {
		Map<String, Object> result = weChatPayServices.getOpenId(paraMap);
		return result;
	}


	/**
	 * plus 微信支付回调
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/plusNotifyWeiXinPay")
	public String plusNotifyWeiXinPay(HttpServletRequest request, HttpServletResponse response) {
		String msg = weChatPayServices.plusNotifyWeiXinPay(request, response);
		return msg;
	}
	/**
	 * 余额充值 微信支付回调
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/balanceRechargeNotifyWeiXinPay")
	public String balanceRechargeNotifyWeiXinPay(HttpServletRequest request, HttpServletResponse response) {
		String msg = weChatPayServices.balanceRechargeNotifyWeiXinPay(request, response);
		return msg;
	}
	/**
	 * 轮询微信支付后状态
	 *
	 * @param map
	 * @return
	 */
	@PostMapping("/queryWxPayStatus")
	public ResponseResult queryWxPayStatus(@RequestBody Map<Object, Object> map) {
		return weChatPayServices.queryWxPayStatus(map);
	}

	/**
	 * 轮询plus 会员微信支付后状态
	 *
	 * @param map
	 * @return
	 */
	@PostMapping("/queryWxPayplus")
	public ResponseResult queryWxPayplus(@RequestBody Map<Object, Object> map) {
		return weChatPayServices.queryWxPayplus(map);
	}
	/**
	 * 轮询 余额充值 微信支付后状态
	 *
	 * @param map
	 * @return
	 */
	@PostMapping("/queryWxPayBalanceRecharge")
	public ResponseResult queryWxPayBalanceRecharge(@RequestBody Map<Object, Object> map) {
		return weChatPayServices.queryWxPayBalanceRecharge(map);
	}
	/**
	 * 根据订单号查询微信支付状态 feign
	 */
	@PostMapping("/queryWxPay")
	public ResponseResult queryWxPay(@RequestBody Map<String, Object> map) {
		return weChatPayServices.queryWxPay(map);
	}
	/**
	 * 对订单微信 已支付成功 回调失败的异常单进行处理 feign
	 * @param param
	 * @return
	 */
	@RequestMapping("/wxPayCompensate")
	public ResponseResult wxPayCompensate(@RequestBody Map<String, Object> param) {
		return weChatPayServices.wxPayCompensate(param);
	}

	/**
	 * 余额微信提现
	 * @param balanceWithdrawDTO
	 * @return
	 */
	@RequestMapping("/balanceWithdraw")
	public ResponseResult balanceWithdraw(@RequestBody BalanceWithdrawDTO balanceWithdrawDTO) {
		return weChatPayServices.balanceWithdraw(balanceWithdrawDTO);
	}
}
