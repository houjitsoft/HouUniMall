package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
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
 * 余额充值 - 微信app支付
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXPayAPPBalanceRechargeServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {
    private static OrderServiceClient orderServiceClient;

    @Autowired
    public void setOrderServiceClient (OrderServiceClient orderServiceClient){
        WXPayAPPBalanceRechargeServicesImpl.orderServiceClient = orderServiceClient;
    };

    final static Logger log = LoggerFactory.getLogger(WXPayAPPBalanceRechargeServicesImpl.class);

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {
        //查询支付配置信息
        this.initWeChatPayConfig();
        String outTradeNo = (String) map.get("rechargeOrderId");
        // 判断订单是否支付过
        ResponseResult responseResult2 = basePayServiceImpl.isPayBalanceRechargeOrder(outTradeNo);
        if(responseResult2.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult2;
        }

        String goodsName = "订单编号" + outTradeNo;
        Map<String, String> params = new HashMap<String, String>();
        OrderBalanceRechargeVO orderBalanceRechargeVO = orderServiceClient.queryRechargeOrderById(outTradeNo);
        // 将订单总金额转换为字符串格式，用于后续支付接口调用
        String payAmount = String.valueOf(orderBalanceRechargeVO.getRechargeAmount());

        try {
            String sign = null;
            Map<String, String> returnMap = new HashMap<>();
            // 初始化微信支付配置工具类
            WXConfigUtil config = new WXConfigUtil();

            // 调用支付工具类处理支付金额，获取格式化后的支付金额
            String price = PayToolUtil.getMoney(payAmount);

            // 设置微信支付应用ID
            params.put("appid", config.getAppID());
            // 设置商户号
            params.put("mch_id", config.getMchID());
            // 生成随机字符串，用于签名
            params.put("nonce_str", PayToolUtil.create_nonce_str());
            // 设置商品描述
            params.put("body", goodsName);
            // 设置订单号
            params.put("out_trade_no", outTradeNo);
            // 设置订单总金额
            params.put("total_fee", price);

            // 设置终端IP地址
            params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));
            // 设置通知URL
            params.put("notify_url", config.BALANCE_RECHARGE_NOTIFY_URL);
            // 设置交易类型为APP
            params.put("trade_type", "APP");

            // 生成签名
            sign = WXPayUtil.generateSignature(params, config.getKey());
            // 将签名添加到参数中
            params.put("sign", sign);

            // 记录支付参数信息
            log.info("param{}", JSON.toJSONString(params));
            // 调用微信支付API发起支付请求
            String xmlResult = PaymentApi.pushOrder(params);
            // 初始化微信支付工具类
            WXPay wxpay = new WXPay(config);
            //使用官方API请求预付订单
            Map<String, String> wxResponse = wxpay.unifiedOrder(params);
            //获取返回码
            String returnCode = wxResponse.get("return_code");
            //若返回码为SUCCESS，则会返回一个result_code,再对该result_code进行判断
            if (returnCode.equals("SUCCESS")) {
                //主要返回以下5个参数(必须按照顺序，否则APP报错：-1)
                String resultCode = wxResponse.get("result_code");
                returnMap.put("appid", wxResponse.get("appid"));
                returnMap.put("noncestr", wxResponse.get("nonce_str"));
                returnMap.put("returnMsg", wxResponse.get("return_msg"));
                returnMap.put("errCodees", wxResponse.get("err_code_des"));
                //resultCode 为SUCCESS，才会返回prepay_id和trade_type
                if ("SUCCESS".equals(resultCode)) {
                    returnMap.put("package","Sign=WXPay");
                    returnMap.put("partnerid", wxResponse.get("mch_id"));
                    returnMap.put("prepayid", wxResponse.get("prepay_id"));
                    //单位为秒
                    returnMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
                    //签名
                    returnMap.put("sign",sign);
                    //获取预支付交易回话标志
                    returnMap.put("trade_type", wxResponse.get("trade_type"));
                    return ResponseResult.success(returnMap);
                } else {
                    //此时返回没有预付订单的数据
                    return ResponseResult.success(returnMap);
                }
            } else {
                return ResponseResult.success(returnMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }
}
