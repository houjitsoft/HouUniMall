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
 * 余额充值 - 微信pc支付
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXPayPCBalanceRechargeServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static OrderServiceClient orderServiceClient;

    @Autowired
    public void setOrderServiceClient (OrderServiceClient orderServiceClient){
        WXPayPCBalanceRechargeServicesImpl.orderServiceClient = orderServiceClient;
    };

    final static Logger log = LoggerFactory.getLogger(WXPayPCBalanceRechargeServicesImpl.class);

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

        // 将订单总金额转换为字符串格式，用于后续支付接口调用
        String payAmount = String.valueOf(orderBalanceRechargeVO.getRechargeAmount());
        // 根据订单金额获取格式化后的支付金额
        String price = PayToolUtil.getMoney(payAmount);

        // 初始化参数映射表，用于构建微信支付请求
        Map<String, String> params = new HashMap<String, String>();
        // 设置微信支付应用ID
        params.put("appid", WXPayConfig.APP_ID);
        // 设置微信支付商户号
        params.put("mch_id", WXPayConfig.MCH_ID);
        // 生成随机字符串，作为请求的一部分
        params.put("nonce_str", PayToolUtil.create_nonce_str());
        // 设置商品描述
        params.put("body", goodsName);
        // 设置订单号
        params.put("out_trade_no", outTradeNo);
        // 设置订单总金额
        params.put("total_fee", price);
        // 设置订单支付的IP地址
        params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));
        // 设置通知URL，用于接收微信支付异步通知
        params.put("notify_url", WXPayConfig.BALANCE_RECHARGE_NOTIFY_URL);
        // 设置交易类型为Native，用于生成二维码支付
        params.put("trade_type", "NATIVE");

        // 生成签名，确保请求的安全性
        String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
        // 将签名添加到参数映射表中
        params.put("sign", sign);
        // 日志记录请求参数
        log.info("param{}", JSON.toJSONString(params));

        // 调用微信支付API，发起支付请求
        String xmlResult = PaymentApi.pushOrder(params);
        // 将返回的XML数据转换为映射表格式
        Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

        // 获取返回的代码和消息，用于判断支付请求的结果
        String return_code = result.get("return_code");
        String return_msg = result.get("return_msg");
        // 如果返回代码不为SUCCESS，则处理异常情况
        if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            return ResponseResult.fail(return_msg);
        }
        // 如果支付请求成功，获取二维码链接，返回给前端
        //PC端
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("payAmount",payAmount);
        resultMap.put("qrCodeUrl",result.get("code_url"));
        return ResponseResult.success(resultMap);

    }
}
