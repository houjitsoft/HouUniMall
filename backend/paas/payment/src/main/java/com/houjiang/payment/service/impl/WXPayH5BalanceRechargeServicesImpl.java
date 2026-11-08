package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.vo.RecordVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余额充值 - 微信h5支付
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXPayH5BalanceRechargeServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static OrderServiceClient orderServiceClient;

    @Autowired
    public void setOrderServiceClient (OrderServiceClient orderServiceClient){
        WXPayH5BalanceRechargeServicesImpl.orderServiceClient = orderServiceClient;
    };

    final static Logger log = LoggerFactory.getLogger(WXPayH5BalanceRechargeServicesImpl.class);

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {
        // 初始化微信支付pc、h5配置
        this.initWeChatPayConfig();
        String outTradeNo = (String) map.get("rechargeOrderId");
        // 判断订单是否支付过
        ResponseResult responseResult2 = basePayServiceImpl.isPayBalanceRechargeOrder(outTradeNo);
        if(responseResult2.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult2;
        }

        String goodsName = "订单编号" + outTradeNo;
        OrderBalanceRechargeVO orderBalanceRechargeVO = orderServiceClient.queryRechargeOrderById(outTradeNo);
        String price = PayToolUtil.getMoney(orderBalanceRechargeVO.getRechargeAmount().toString());
        // 初始化参数映射表，用于存储微信支付所需的参数
        Map<String, String> params = new HashMap<String, String>();
        // 添加必要的支付参数，如应用ID、商户号等
        params.put("appid", WXPayConfig.APP_ID);
        params.put("mch_id", WXPayConfig.MCH_ID);
        params.put("nonce_str", PayToolUtil.create_nonce_str());
        params.put("body", goodsName);
        params.put("out_trade_no", outTradeNo);
        params.put("total_fee", price);

        // 添加请求的IP地址和通知URL
        params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));
        params.put("notify_url", WXPayConfig.BALANCE_RECHARGE_NOTIFY_URL);
        // 设置交易类型为MWEB，适用于H5支付
        params.put("trade_type", "MWEB");

        // 生成签名，确保支付请求的安全性
        String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
        params.put("sign", sign);

        // 日志记录支付参数，便于调试和跟踪
        log.info("param{}", JSON.toJSONString(params));

        // 调用微信支付API发起支付请求
        String xmlResult = PaymentApi.pushOrder(params);

        // 将支付结果从XML格式转换为MAP格式，方便处理
        Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

        // 日志记录支付结果，便于调试和跟踪
        log.info("result{}", JSON.toJSONString(result));

        // 检查支付结果是否成功
        String return_code = result.get("return_code");
        String return_msg = result.get("return_msg");
        if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
           return ResponseResult.fail(return_msg);
        }
        // 支付成功，构造跳转URL并返回成功结果
        // h5端
        String urlString = Url.getURLEncoderString(WXPayConfig.REDIRECT_URL);
        String mweb_url = result.get("mweb_url") + "&redirect_url=" + urlString;
        return ResponseResult.success(mweb_url);

    }
}
