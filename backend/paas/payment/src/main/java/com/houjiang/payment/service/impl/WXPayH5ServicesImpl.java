package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
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
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXPayH5ServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static PayConfigMapper payConfigMapper;

    private static PayConsumeTradeMapper payConsumeTradeMapper;

    private static PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    private static PayConsumeRecordMapper payConsumeRecordMapper;

    private static UsermsServiceClient usermsServiceClient;

    private static OrderServiceClient orderServiceClient;

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(UsermsServiceClient usermsServiceClient) {

        WXPayH5ServicesImpl.usermsServiceClient = usermsServiceClient;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper, PayConsumeTradeMapper payConsumeTradeMapper,
                          PayConsumeTradeBackMapper payConsumeTradeBackMapper, PayConsumeRecordMapper payConsumeRecordMapper) {
        WXPayH5ServicesImpl.payConfigMapper = payConfigMapper;
        WXPayH5ServicesImpl.payConsumeTradeMapper = payConsumeTradeMapper;
        WXPayH5ServicesImpl.payConsumeTradeBackMapper = payConsumeTradeBackMapper;
        WXPayH5ServicesImpl.payConsumeRecordMapper = payConsumeRecordMapper;
    }
    @Autowired
    public void setOrderServiceClient(OrderServiceClient orderServiceClient) {
        WXPayH5ServicesImpl.orderServiceClient = orderServiceClient;
    }
    final static Logger log = LoggerFactory.getLogger(WXPayH5ServicesImpl.class);

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {
        // 初始化微信支付pc、h5配置
        this.initWeChatPayConfig();
        String outTradeNo = (String) map.get("orderId");
        String token = (String) map.get("token");
        BigDecimal totalPrice = new BigDecimal(0);
        // 判断订单是否支付过
        ResponseResult responseResult = basePayServiceImpl.isPayOrder(outTradeNo);
        if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult;
        }
        // 来源
        Integer orderFromNum = map.get("platForm") == null ? 0 : Integer.parseInt(map.get("platForm").toString());
        // 订单来源存值
        RedisUtil.set(HouConfig.HOU_ORDERFORM + outTradeNo, orderFromNum.toString(), 86400);

        // 处理自营店铺
        if (!StringUtils.isEmpty(RedisUtil.get(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo))) {
            RedisUtil.remove(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo);
        }
        RedisUtil.set(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo, token, 86400);

        String goodsName = "订单编号" + outTradeNo;

        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        String userId = ui.getUserId();

        // 查询商品名与商品价格
        if (PayToolUtil.isInteger(outTradeNo)) {
            GoodsDTO queryGoodsPara = payConsumeTradeMapper.queryGoodsPriceAndName(outTradeNo);
            // 总价
            totalPrice = queryGoodsPara.getTradePaymentAmount();

            GoodsDTO queryGoodsPara2 = payConsumeTradeBackMapper.queryGoodsPriceAndName(outTradeNo, userId);
            //获取积分商品订单的积分
            Integer totalIntegral = queryGoodsPara2.getTotalIntegral();
            //积分商品 0  是  1 否
            integralGoods = queryGoodsPara2.getIntegralGoods();
            amountAotalIntegral = totalIntegral;
        } else {
            List<RecordVO> rp = payConsumeRecordMapper.queryRecordParaList(outTradeNo);
            for (RecordVO recordVO : rp) {
                BigDecimal price = recordVO.getRecordMoney();
                totalPrice = totalPrice.add(price);
            }
            // 获取积分商品的积分
            String totalIntegral = RedisUtil
                    .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + token + "--" + userId);
            if (null != totalIntegral) {
                //amountAotalIntegral 不为null 证明是积分订单
                amountAotalIntegral = Integer.valueOf(totalIntegral);
                //积分商品 0  是  1 否
                integralGoods = 0;
            }
        }
        if (null != integralGoods && integralGoods == 0) {
            //获取用户积分
            Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
            Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userId);
            userIntegral = userIntegral - frozenIntegra;
            if(userIntegral < 0){
                userIntegral = 0;
            }
            if (null != userIntegral && amountAotalIntegral > userIntegral) {
                //账户积分不足
                return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR);
            }
        }
        // 判断是否京东订单 且 是否当前支付是京东余额支付 且 余额是否够支付
        ResponseResult responseResult1 = orderServiceClient.queryJDBalanceIsEnough(outTradeNo, totalPrice);
        if(responseResult1.getCode() ==  CommonStatusEnum.SUCCESS.getCode() && responseResult1.getData().toString().equals("false")){
            // 京东账户余额不足！
            return ResponseResult.fail("京东账户余额不足！");
        }
        // 将总价转换为字符串格式，用于后续支付金额的处理
        String payAmount = String.valueOf(totalPrice);
        // 根据支付金额获取格式化后的支付价格
        String price = PayToolUtil.getMoney(payAmount);

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
        params.put("notify_url", WXPayConfig.NOTIFY_URL);
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
        String err_code_des = result.get("err_code_des");
        if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            // 如果支付失败，根据不同的情况返回相应的错误信息和处理方式
            if(orderFromNum!=0){
                // 对于非零来源订单，构造跳转URL并返回失败结果
                String urlString = Url.getURLEncoderString(WXPayConfig.REDIRECT_URL);
                String mweb_url = result.get("mweb_url") + "&redirect_url=" + urlString;
                return ResponseResult.fail(err_code_des,mweb_url);
            }else{
                // 对于零来源订单，直接返回失败消息
                return ResponseResult.fail(return_msg);
            }
        }
        // 支付成功，构造跳转URL并返回成功结果
        // h5端
        String urlString = Url.getURLEncoderString(WXPayConfig.REDIRECT_URL);
        String mweb_url = result.get("mweb_url") + "&redirect_url=" + urlString;
        return ResponseResult.success(mweb_url);

    }
}
